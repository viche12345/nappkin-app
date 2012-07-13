package com.roboteater.nappkin;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

public class AddUserActivity extends Activity {
	
	public JSONObject map;
	private Object args[] = new Object[1];
	private OSCPortOut sender = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.adduser);
		
		Button b = (Button) findViewById(R.id.submitButton);
		b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				EditText et = (EditText) findViewById(R.id.email);
				String email = et.getText().toString();
				new Update().execute(getIntent().getExtras().getInt("mapId")+"","adduser",email);
				finish();
			}
		});
	}
	
	class Update extends AsyncTask<String,Void,Void>{

		@Override
		protected Void doInBackground(String... params) {
			try {
				//TODO: CHANGE THIS IP
				sender = new OSCPortOut(InetAddress.getByName("169.234.22.158"));
			} catch (SocketException e2) {
				e2.printStackTrace();
			} catch (UnknownHostException e2) {
				e2.printStackTrace();
			}
			map = new JSONObject();
			
				JSONObject contents = new JSONObject();
				try {
					contents.put("action", params[1]);
					contents.put("parameters", params[0]);
					contents.put("mindmap", map.toString());
					contents.put("email", params[2]);

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
				sender.close();
				return null;
			
		}
		
	}

}
