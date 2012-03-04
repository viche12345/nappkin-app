package com.roboteater.nappkin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ListButton extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		
//        Button newButton = (Button) findViewById(R.id.NewButton);
		createButton();
//		newButton.setOnClickListener(new View.OnClickListener()
//		{
//			public void onClick(View arg0)
//			{
//				Button newButton = createButton();
//			}
//		});		
	}

	public void createButton()
	{
		Button start = (Button) findViewById(R.id.Button01);
		
		start.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View arg0)
			{
				
				Intent myIntent = new Intent(arg0.getContext(), NappkinActivity.class);
				startActivityForResult(myIntent, 0);
			}
		});	
		
		
	}

}
