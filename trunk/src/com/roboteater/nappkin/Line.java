package com.roboteater.nappkin;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class Line extends View{
	
	private Paint mPaint;
	private Bubble startBubble,endBubble;
	private static Random gen = new Random();
	
	public Line(Context context, Bubble startBubble, Bubble endBubble) {
		super(context);
		this.startBubble = startBubble;
		this.endBubble = endBubble;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.rgb(gen.nextInt(255), gen.nextInt(255), gen.nextInt(255)));
		mPaint.setStrokeWidth(5);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawLine(startBubble.x, startBubble.y, endBubble.x, endBubble.y, mPaint);
	}

	public Bubble getStartBubble() {
		return startBubble;
	}

	public Bubble getEndBubble() {
		return endBubble;
	}
	
}
