package com.roboteater.nappkin;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
	private GestureDetector gestureDetector;
	private Bubble selectedBubble;
	
	private int count = 0;
	boolean registered = true;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if(!registered)
        	register();
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
        
        mainView = (RelativeLayout) findViewById(R.id.mainCanvas);
        mainView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
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
				// TODO Auto-generated method stub
				Intent intent = new Intent(arg0.getContext(), ListButton.class);
				startActivityForResult(intent, intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			}
		});
    }
    
    private void register() {
    	Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
    	registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
    	registrationIntent.putExtra("sender", "qasidsadiq@gmail.com");
    	this.startService(registrationIntent);
    	registered = true;
	}

    @Override
	protected Dialog onCreateDialog(int id) {
    	LayoutInflater factory = LayoutInflater.from(this);
    	View dialogLayout = factory.inflate(R.layout.editbubble, null);
    	final EditText et = (EditText) dialogLayout.findViewById(R.id.editText1);
    	Log.d("Nappkin",selectedBubble.getText());
    	et.setText(selectedBubble.getText());
    	
    	return new AlertDialog.Builder(this).setIconAttribute(android.R.attr.dialogIcon)
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
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (selectedBubble != null && selectedBubble.isSelected()) {
							selectedBubble.select();
						}
						selectedBubble = null;
					}
				}).create();
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
			if (selectedBubble != null) showDialog(selectedBubble.getId());
			else Toast.makeText(this, "Click on an idea first!", Toast.LENGTH_LONG).show();
			return true;
		case R.id.menu_delete:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
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
				showDialog(selectedBubble.getId());
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