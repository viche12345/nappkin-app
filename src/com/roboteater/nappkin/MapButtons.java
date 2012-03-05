package com.roboteater.nappkin;

import java.util.Random;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog.Builder;

public class MapButtons extends Activity
{
	private int id;
	private String name;
	private JSONObject json;
	private Class<?> activity;

	public MapButtons(String id, String name, JSONObject json)
	{
		Random random = new Random();
		this.id = random.nextInt(Integer.MAX_VALUE);
		this.setName(name);
		this.setJson(json);
		this.setActivity(NappkinActivity.class);
		
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public JSONObject getJson()
	{
		return json;
	}

	public void setJson(JSONObject json)
	{
		this.json = json;
	}

	public int getId()
	{
		return id;
	}

	public Class<?> getActivity()
	{
		return activity;
	}

	public void setActivity(Class<?> activity)
	{
		this.activity = activity;
	}
}
