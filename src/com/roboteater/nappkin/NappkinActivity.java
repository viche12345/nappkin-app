package com.roboteater.nappkin;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

public class NappkinActivity extends Activity {
	
	private RelativeLayout mainView;
	private FrameLayout bubbleView;
	private FrameLayout lineView;
	private ArrayList<Bubble> listOfBubbles = new ArrayList<Bubble>();
	private ArrayList<Line> listOfLines = new ArrayList<Line>();
	private GestureDetector gestureDetector;
	private Bubble selectedBubble;
	private JSONObject map;
	
	private int count = 0;
	private Chat chat;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ConnectionConfiguration config = new ConnectionConfiguration("jabber.org", 5222);
        Connection connection = new XMPPConnection(config);
        try{
        connection.connect();
        connection.login("nappkinclient@jabber.org", "nutella", "Client");
        ChatManager chatmanager = connection.getChatManager();
        chat = chatmanager.createChat("nappkinserver@jabber.org", new MessageListener() {
            public void processMessage(Chat chat, Message message) {
            	 try {
					map = new JSONObject(message.getBody());
				} catch (JSONException e) {	}
            }
        });
        }
        catch(XMPPException e){}
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
        
        mainView = (RelativeLayout) findViewById(R.id.mainCanvas);
        mainView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction()==MotionEvent.ACTION_UP) sendMessage(map, "update");
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
				Intent intent = new Intent(arg0.getContext(), ListButton.class);
				startActivityForResult(intent, 0);
			}
		});
    }
   

	@Override
	protected Dialog onCreateDialog(int id) {
    	LayoutInflater factory = LayoutInflater.from(this);
    	View dialogLayout = factory.inflate(R.layout.editbubble, null);
    	final EditText et = (EditText) dialogLayout.findViewById(R.id.editText1);
    	et.setText(selectedBubble.getText());
    	
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
				selectedBubble = null;
			}
			else Toast.makeText(this, "Click on an idea first!", Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	/*
	 * A class to genericcly send messages
	 */
	private void sendMessage(JSONObject value, String action){
		Message message = new Message();
		JSONObject contents = new JSONObject();
		try {
			contents.put("action", action);
			contents.put("mindmap",map);//ADD JSON VAIRABLE FOR MAP HERE
			contents.put("parameters", value);
		} catch (JSONException e1) {		}
		message.setBody(contents.toString());
		try {
			chat.sendMessage(message);
		} catch (XMPPException e) {}
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
							//If there was a previously selected bubble
							Line line = new Line(getApplicationContext(), oldBubble,selectedBubble);
							if (oldBubble.addConnection(selectedBubble)) {
								lineView.addView(line);
								listOfLines.add(line);
								selectedBubble.addConnection(oldBubble);
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
					selectedBubble.select();
					selectedBubble = null;
					return true;
				}
				Bubble b = new Bubble(getApplicationContext(), x, y, count);
				b.setText("New Idea");
				bubbleView.addView(b);
				listOfBubbles.add(b);
				count++;
				
				selectedBubble=b;
				showDialog(0);
			}
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (draggingBubble) selectedBubble.shift((int)-distanceX, (int)-distanceY);
			else {
				for (Bubble circ : listOfBubbles) {
					circ.shift((int)-distanceX, (int)-distanceY);
				}
			}
			return true;
		}
		
	}
}