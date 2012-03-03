package com.roboteater.nappkin;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

public class Bubble extends View {
	
	private final int WIDTH=200;
	private final int HEIGHT=60;
	
	private RectF r;
	private Paint mPaint;
	private Paint shadowPaint;
	private RectF shadowR;
	private int x,y;
	
	private LinearGradient gradient;

	public Bubble(Context context, int x, int y) {
		super(context);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		shadowPaint.setMaskFilter(new BlurMaskFilter(5, Blur.NORMAL));
		shadowPaint.setColor(Color.DKGRAY);
		r = new RectF(x-(WIDTH/2), y-(HEIGHT/2), x+(WIDTH/2), y+(HEIGHT/2));
		shadowR = new RectF(r);
		
		this.x = x;
		this.y = y;
	}
	
	protected void shift(int shiftX, int shiftY) {
		x = x + shiftX;
		y = y + shiftY;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		gradient = new LinearGradient(0, r.top, 0, r.bottom, Color.rgb(47, 169, 255), Color.rgb(0,114,255), Shader.TileMode.CLAMP);
		mPaint.setShader(gradient);
		r.set(x-(WIDTH/2), y-(HEIGHT/2), x+(WIDTH/2), y+(HEIGHT/2));
		shadowR.set(x-(WIDTH/2), (y+10)-(HEIGHT/2), x+(WIDTH/2), (y+10)+(HEIGHT/2));
		canvas.drawRoundRect(shadowR, 30, 30, shadowPaint);
		canvas.drawRoundRect(r,30,30, mPaint);
	}
	
	protected boolean contains(int x, int y) {
		return r.contains(x, y);
	}

}
