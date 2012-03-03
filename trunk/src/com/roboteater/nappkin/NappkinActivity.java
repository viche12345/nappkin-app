package com.roboteater.nappkin;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class NappkinActivity extends Activity {
	
	private FrameLayout mainView;
	private ArrayList<Bubble> listOfBubbles = new ArrayList<Bubble>();
	private GestureDetector gestureDetector;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
        
        mainView = (FrameLayout) findViewById(R.id.bubbleCanvas);
        mainView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
    }
    
    class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			int x = (int)e.getX();
			int y = (int)e.getY();
			for (Bubble b : listOfBubbles) {
				if (b.contains(x,y)) {
					Toast.makeText(getApplicationContext(), "Clicked!", Toast.LENGTH_SHORT).show();
					return true;
				}
			}
			Bubble c = new Bubble(getApplicationContext(), x, y);
			mainView.addView(c);
			listOfBubbles.add(c);
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			for (Bubble circ : listOfBubbles) {
				circ.shift((int)-distanceX, (int)-distanceY);
			}
			return true;
		}
		
	}
}