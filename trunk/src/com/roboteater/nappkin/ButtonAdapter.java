package com.roboteater.nappkin;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ButtonAdapter extends ArrayAdapter
{
	private final Activity activity;
	private final List<MapButtons> buttons;

	public ButtonAdapter(Activity activity, List<MapButtons> objects)
	{
		super(activity, R.layout.map_items, objects);
		this.activity = activity;
		this.buttons = objects;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View concertView, ViewGroup parent)
	{
		View viewRow = concertView;
		MapsView mapView = null;

		if (viewRow == null)
		{
			LayoutInflater inflater = activity.getLayoutInflater();
			viewRow = inflater.inflate(R.layout.map_items, null);

			mapView = new MapsView();
			mapView.name = (TextView) viewRow.findViewById(R.id.map_name);

			viewRow.setTag(mapView);
		}

		else
		{
			mapView = (MapsView) viewRow.getTag();
		}

		MapButtons currentMap = (MapButtons) buttons.get(position);
		mapView.name.setText(currentMap.getName());

		return viewRow;
	}

	protected static class MapsView
	{
		protected TextView name;
	}
}
