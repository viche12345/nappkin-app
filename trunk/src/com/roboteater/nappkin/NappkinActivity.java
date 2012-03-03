package com.roboteater.nappkin;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class NappkinActivity extends Activity {
	
	private FrameLayout mainView;
	private ArrayList<Bubble> listOfBubbles = new ArrayList<Bubble>();
	private GestureDetector gestureDetector;
	private Bubble selectedBubble;
	
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
						if (selectedBubble != null) selectedBubble.select();
						selectedBubble = b;
					}
					else {
						selectedBubble = null;
					}
					selected = true;
				}
			}
			if (!selected) {
				Bubble c = new Bubble(getApplicationContext(), x, y);
				mainView.addView(c);
				listOfBubbles.add(c);
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