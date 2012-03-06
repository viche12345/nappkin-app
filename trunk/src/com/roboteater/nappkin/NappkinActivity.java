package com.roboteater.nappkin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

public class NappkinActivity extends Activity {
	
	private RelativeLayout mainView;
	private FrameLayout bubbleView;
	private FrameLayout lineView;
	private ArrayList<Bubble> listOfBubbles = new ArrayList<Bubble>();
	private ArrayList<Line> listOfLines = new ArrayList<Line>();
	private GestureDetector gestureDetector;
	private Bubble selectedBubble;
	public JSONObject map;
	
	
	private int count = 0;
	private Random gen = new Random();
	public String username;
	public String password;
	boolean registered = false;
	
	private int mapId = 0;
	private boolean mapExistsInDB;
	
	private boolean updatingBubble;
	
	private OSCPortOut sender = null;
	private OSCPortIn receiver = null;
	private Object args[] = new Object[1];
	
	private Timer t = new Timer();
	
	private static URL conn = null;
	
	private int shiftX = 0;
	private int shiftY = 0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
        
        mainView = (RelativeLayout) findViewById(R.id.mainCanvas);
        mainView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (updatingBubble && event.getAction()==MotionEvent.ACTION_UP) {
					new Update().execute(null, "update");
					updatingBubble = false;
				}
				return gestureDetector.onTouchEvent(event);
			}
		});
        
        bubbleView = (FrameLayout) findViewById(R.id.bubbleCanvas);
        lineView = (FrameLayout) findViewById(R.id.lineCanvas);
        
        Button forwardButton = (Button) findViewById(R.id.list);
        forwardButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View arg0)
			{
				Intent intent = new Intent(arg0.getContext(), ListMaps.class);
				startActivityForResult(intent, 0);
			}
		});
        
        mapExistsInDB = false;
        if (getIntent().hasExtra("mapId")) {
        	mapId = getIntent().getExtras().getInt("mapId");
        	mapExistsInDB = true;
        } else {
        	mapId = gen.nextInt(Integer.MAX_VALUE);
        	mapExistsInDB = false;
        }
        
        Log.d("nappkin",mapId+"");
        
        if (!mapExistsInDB) {
        	new Update().execute(null, "newmap");
        	t.schedule(new TimerTask() {
			
			@Override
			public void run() {
				String possibleEmail = "";
			       Account[] accounts = AccountManager.get(NappkinActivity.this).getAccounts();
			       for (Account account : accounts) {
			         if (account.name.contains("@")) {
			             possibleEmail = account.name;
			         }
			       }
				new Update().execute(mapId+"","adduser",possibleEmail);
			}
		}, 1000);
        } else {
        	new Fetch().execute(mapId+"");
        }
    }
   

	@Override
	protected Dialog onCreateDialog(int id) {
    	LayoutInflater factory = LayoutInflater.from(this);
    	View dialogLayout = factory.inflate(R.layout.editbubble, null);
    	final EditText et = (EditText) dialogLayout.findViewById(R.id.editText1);
    	et.setText((selectedBubble != null) ? "New Idea" : selectedBubble.getText());
    	
    	Dialog dialog = new AlertDialog.Builder(this).setIconAttribute(android.R.attr.dialogIcon)
    			.setTitle("Edit Idea")
    			.setView(dialogLayout)
    			.setPositiveButton("Save", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (selectedBubble != null && selectedBubble.isSelected()) {
							selectedBubble.select();
						}
						selectedBubble.setText(et.getText().toString());
						selectedBubble = null;
						new Update().execute(null, "update");
						dialog.cancel();
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (selectedBubble != null && selectedBubble.isSelected()) {
							selectedBubble.select();
						}
						selectedBubble = null;
						dialog.cancel();
					}
				}).setCancelable(false).create();
    	
    	dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
		        et.selectAll();
			}
		});
    	
    	return dialog;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_edit:
			if (selectedBubble != null) showDialog(0);
			else Toast.makeText(this, "Click on an idea first!", Toast.LENGTH_LONG).show();
			return true;
		case R.id.menu_delete:
			if (selectedBubble != null) {
				bubbleView.removeView(selectedBubble);
				listOfBubbles.remove(selectedBubble);
				ArrayList<Line> linesToRemove = new ArrayList<Line>();
				for (Line line : listOfLines) {
					if (line.getStartBubble().equals(selectedBubble) || line.getEndBubble().equals(selectedBubble)){
						linesToRemove.add(line);
						lineView.removeView(line);
					}
				}
				for (Line pending : linesToRemove) {
					listOfLines.remove(pending);
				}
				new Update().execute(null, "update");
				selectedBubble = null;
			}
			else Toast.makeText(this, "Click on an idea first!", Toast.LENGTH_LONG).show();
			return true;
		case R.id.menu_share:
			new Update().execute(null, "update");
			Intent i2 = new Intent(getApplicationContext(), AddUserActivity.class);
			i2.putExtra("mapId", mapId);
			startActivity(i2);
			return true;
		case R.id.menu_more:
			Intent i = new Intent(getApplicationContext(), MapListActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		sender.close();
		receiver.stopListening();
		receiver.close();
	}


	@Override
	protected void onResume() {
		super.onResume();
		new Connect().execute();
	}

	public String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
    }
	/*
	 * A class to genericcly send messages
	 */
	public void updateMap(ArrayList<Bubble> bubbles, int id)
	{
		try{
				map.put("id", id);
				if (bubbles.size() > 0) map.put("name", bubbles.get(0).getText());
				
				JSONArray  bubbleArray = new JSONArray();
				for(int i = 0; i< bubbles.size(); i++)
				{
					Bubble nextBubble = bubbles.get(i);
					JSONObject bubble = new JSONObject();
					bubble.put("id",nextBubble.getId());
					bubble.put("x", nextBubble.getStartingX());
					bubble.put("y", nextBubble.getStartingY());
					bubble.put("user", nextBubble.getUser());
					bubble.put("text", nextBubble.getText());
					JSONArray connected = new JSONArray();
					Set<Integer> connectedBubbles = nextBubble.getConnected();
					Iterator<Integer> it = connectedBubbles.iterator();
					while(it.hasNext())
					{
						JSONObject connectedID = new JSONObject();
						connectedID.put("id", it.next());
						connected.put(connectedID);
					}
					bubble.put("connected", connected);
					bubbleArray.put(bubble);
				}
				map.put("Bubbles", bubbleArray);
			}
		catch(JSONException e)
		{
			
		}
		
	}
	
	class Update extends AsyncTask<String,Void,Void>{

		@Override
		protected Void doInBackground(String... params) {
			map = new JSONObject();
			String possibleEmail = "";
		       Account[] accounts = AccountManager.get(NappkinActivity.this).getAccounts();
		       for (Account account : accounts) {
		         if (account.name.contains("@")) {
		             possibleEmail = account.name;
		         }
		       }
			
			updateMap(listOfBubbles, mapId);
				JSONObject contents = new JSONObject();
				try {
					contents.put("action", params[1]);
					contents.put("mindmap",map.toString());
					contents.put("parameters", params[0]);

					contents.put("email", possibleEmail);
					contents.put("ip", getLocalIpAddress());

				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				args[0] = contents.toString();
				OSCMessage msg = new OSCMessage("/nappkin", args);
				try {
					sender.send(msg);
					Log.d("Nappkin",(String) args[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			
		}
		
	}
	
	class Connect extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				sender = new OSCPortOut(InetAddress.getByName("team8.appjam.roboteater.com"));
				receiver = new OSCPortIn(OSCPort.defaultSCOSCPort()+5);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
			
			OSCListener listener = new OSCListener() {
				public void acceptMessage(java.util.Date time,
						OSCMessage message) {
					Log.d("OSCMESSAGE",(String)message.getArguments()[0]);
					try {
						JSONObject rMap = new JSONObject((String)message.getArguments()[0]);
						if(Integer.parseInt(rMap.getString("id")) == mapId){
							convertJson((String)message.getArguments()[0],false);
							runOnUiThread(new Drawer());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			};
			receiver.addListener("/nappkinResponse", listener);
			receiver.startListening();
			
			return null;
		}
		
	}
	
	private class Drawer implements Runnable {

		@Override
		public void run() {
			boolean lineExists = false;
			for (Bubble b : listOfBubbles) {
				bubbleView.addView(b);
				for (Integer i : b.getConnected()) {
					Bubble newB = getBubbleById(i);
					Log.d("nappkin",b.toString());
					Log.d("nappkin",newB.toString());
					Line line = new Line(getApplicationContext(), b, newB);
					for (Line existing : listOfLines) {
						if ((existing.getStartBubble().getId() == b.getId() && existing.getEndBubble().getId() == newB.getId() ) 
								|| (existing.getStartBubble().getId() == newB.getId() && existing.getEndBubble().getId() == b.getId() )) {
							lineExists = true;
						}
					}
					if (!lineExists) {
						Log.d("nappkin","ADDING LINE");
						listOfLines.add(line);
						lineView.addView(line);
					}
				}
			}
		}
		
	}
	
	private Runnable resetView = new Runnable() {
		
		@Override
		public void run() {
			bubbleView.removeAllViews();
			lineView.removeAllViews();
		}
	};
	
	private void convertJson(String json, boolean isArray) {
		JSONArray obj = null;
		if (isArray) {
			try {
				obj = new JSONArray(json);
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
			try {
				map = new JSONObject(obj.getJSONObject(0).getString("json"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				map = new JSONObject(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		try {
			mapId = map.getInt("id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		/*try {
			String name = map.getString("name");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}*/
		JSONArray bubbleArray = null;
		try {
			bubbleArray = map.getJSONArray("Bubbles");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		listOfBubbles.clear();
		listOfLines.clear();
		runOnUiThread(resetView);
		for (int i = 0; i < bubbleArray.length(); i++) {

			JSONObject jb = null;
			try {
				jb = (JSONObject) bubbleArray.get(i);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}

			int bubID = -1;
			try {
				bubID = jb.getInt("id");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			int x = -1;
			try {
				x = (jb.getInt("x"));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			int y = -1;
			try {
				y = jb.getInt("y");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			Bubble b = new Bubble(getApplicationContext(), x+shiftX, y+shiftY,
					bubID);

			try {
				b.setUser((String) jb.get("user"));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			try {
				b.setText((String) jb.get("text"));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			Set<Integer> connected = new TreeSet<Integer>();

			JSONArray conn = null;
			try {
				conn = jb.getJSONArray("connected");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			for (int j = 0; j < conn.length(); j++)

			{

				int connid = 0;
				try {
					connid = ((JSONObject) (conn.get(j)))
							.getInt("id");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				connected.add(connid);

			}

			b.setConnected(connected);

			listOfBubbles.add(b);
			Log.d("nappkin",listOfBubbles.get(0).getText());
		}
	}
	
	private class Fetch extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				conn = new URL("http://team8.appjam.roboteater.com/index.php?mode=map&id=" + params[0]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			Log.d("nappkin",params[0]);
			
			BufferedReader in;
			String inputLine = "";
			try {
				in = new BufferedReader(
				        new InputStreamReader(conn.openStream()));
				inputLine = in.readLine();
		        in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return inputLine;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d("nappkin",result);
			convertJson(result,true);
			boolean lineExists = false;
			for (Bubble b : listOfBubbles) {
				bubbleView.addView(b);
				for (Integer i : b.getConnected()) {
					Bubble newB = getBubbleById(i);
					Log.d("nappkin",b.toString());
					Log.d("nappkin",newB.toString());
					Line line = new Line(getApplicationContext(), b, newB);
					for (Line existing : listOfLines) {
						if ((existing.getStartBubble().getId() == b.getId() && existing.getEndBubble().getId() == newB.getId() ) 
								|| (existing.getStartBubble().getId() == newB.getId() && existing.getEndBubble().getId() == b.getId() )) {
							lineExists = true;
						}
					}
					if (!lineExists) {
						Log.d("nappkin","ADDING LINE");
						listOfLines.add(line);
						lineView.addView(line);
					}
				}
			}
		}
		
	}
	
	private Bubble getBubbleById(int id) {
		for (Bubble b : listOfBubbles) {
			if (b.getId() == id) return b;
		}
		return null;
	}

	class MyGestureDetector extends SimpleOnGestureListener {
    	
    	boolean draggingBubble;
    	
		@Override
		public boolean onDown(MotionEvent e) {
			if (selectedBubble != null && selectedBubble.contains((int)e.getX(), (int)e.getY())){
				draggingBubble = true;
			} else draggingBubble = false;
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			boolean selected = false;
			int x = (int)e.getX();
			int y = (int)e.getY();
			for (Bubble b : listOfBubbles) {
				if (b.contains(x,y)) {
					boolean highlighted = b.select();
					if (highlighted) {
						Bubble oldBubble = null;
						if (selectedBubble != null) {
							//Deselect previously selected bubble
							oldBubble = selectedBubble;
							oldBubble.select();
						}
						selectedBubble = b;
						if (oldBubble != null) {
							//If there was a previously selected bubble, connect bubbles!
							Line line = new Line(getApplicationContext(), oldBubble,selectedBubble);
							if (oldBubble.addConnection(selectedBubble)) {
								lineView.addView(line);
								listOfLines.add(line);
								selectedBubble.addConnection(oldBubble);
								new Update().execute(null, "update");
							}
						}
					}
					else {
						//Previously highlighted bubble is deselected
						selectedBubble = null;
					}
					selected = true;
				}
			}
			if (!selected) {
				if (selectedBubble != null) {
					//User clicked whitespace that will just deselect the bubble
					selectedBubble.select();
					selectedBubble = null;
					return true;
				}
				//Here, user clicked whitespace but no bubble was selected before so
				//create a new one!
				count = gen.nextInt(Integer.MAX_VALUE);
				Bubble b = new Bubble(getApplicationContext(), x, y, count);
				b.setText("New Idea");
				bubbleView.addView(b);
				listOfBubbles.add(b);
				
				selectedBubble=b;
				new Update().execute(null, "update");
				showDialog(0);
			}
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (draggingBubble) {
				updatingBubble = true;
				selectedBubble.updateStartingCoords((int)-distanceX, (int)-distanceY);
			}
			else {
				shiftX += -distanceX;
				shiftY += -distanceY;
				for (Bubble circ : listOfBubbles) {
					circ.shift((int)-distanceX, (int)-distanceY);
				}
			}
			return true;
		}
		
	}
}