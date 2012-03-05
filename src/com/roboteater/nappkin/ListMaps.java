package com.roboteater.nappkin;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class ListMaps extends ListActivity
{
	List<MapButtons> buttons = new ArrayList<MapButtons>();

	@Override
	public void onCreate(Bundle saveInstanceState)
	{
		super.onCreate(saveInstanceState);
		setContentView(R.layout.list_view);
		setListAdapter(new ButtonAdapter(this, buttons));
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
	}

	public View newMap(View view)
	{
//		PromptDialog pd = new PromptDialog(this, "Name new map",
//				"Name required")
//		{
//			
//			@Override
//			public boolean onOkClicked(String input)
//			{
//				buttons.add(new MapButtons("id", input, null))
//				return true;
//			}
//
//		};
		buttons.add(new MapButtons("", "New map", null));
		setListAdapter(new ButtonAdapter(this, buttons));
//		pd.show();
		return view;

	}
	
	public void addItem(MapButtons button)
	{
		buttons.add(button);
		setListAdapter(new ButtonAdapter(this, buttons));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		final Intent intent = new Intent(getBaseContext(), buttons.get(position)
				.getActivity());
		startActivityForResult(intent, position);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);
	}
}
