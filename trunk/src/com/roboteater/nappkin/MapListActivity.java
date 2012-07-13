package com.roboteater.nappkin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MapListActivity extends Activity {
	
	private static URL conn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maplist);
		String possibleEmail = "";
	       Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
	       for (Account account : accounts) {
	         if (account.name.contains("@")) {
	             possibleEmail = account.name;
	         }
	       }
		new Fetch().execute(possibleEmail);
	}
	
	private class Fetch extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			if (conn == null) {
				try {
					//TODO: CHANGE THIS IP
					//conn = new URL("http://team8.appjam.roboteater.com/index.php?mode=list&email=" + params[0]);
					conn = new URL("http://169.234.22.158/index.php?mode=list&email=" + params[0]);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			
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
			ListView lv = (ListView) findViewById(R.id.listView1);
			Log.d("nappkin",result);
			lv.setAdapter(new Adapter(result));
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Intent i = new Intent(getApplicationContext(), NappkinActivity.class);
					i.putExtra("mapId", (int)arg0.getItemIdAtPosition(arg2));
					startActivity(i);
				}
				
			});
		}
		
	}
	
	private class Adapter extends BaseAdapter {
		
		private JSONArray array;
		
		public Adapter(String param) {
			try {
				array = new JSONArray(param);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public int getCount() {
			return array.length();
		}

		@Override
		public Object getItem(int arg0) {
			try {
				return array.get(arg0);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			try {
				return ((JSONObject)array.get(arg0)).getInt("id");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return 0;
		}
		
		public String getMapName(int arg0) {
			try {
				String name = ((JSONObject)array.get(arg0)).getString("name");
				if (name.equals("")) name = "No idea";
				return name;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return "Defunct idea";
		}

		@Override
		public View getView(final int index, View arg1, ViewGroup arg2) {
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			TextView tv = new TextView(getApplicationContext());
			tv.setPadding(25, 25, 25, 25);
			tv.setText(getMapName(index));
			tv.setTextSize(20);
			tv.setTextColor(Color.DKGRAY);
			tv.setLayoutParams(lp);
	    	/*tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent i = new Intent(getApplicationContext(), NappkinActivity.class);
					i.putExtra("mapId", getItemId(index));
					startActivity(i);
				}
			});*/
			return tv;
		}
		
	}

}
