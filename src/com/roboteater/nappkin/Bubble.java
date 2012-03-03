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
	private Paint strokePaint;
	private RectF shadowR;
	private int x,y;
	private boolean selected = false;
	
	private LinearGradient gradient;

	public Bubble(Context context, int x, int y) {
		super(context);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		strokePaint.setColor(Color.rgb(25, 62, 147));
		strokePaint.setStrokeWidth(2);
	    strokePaint.setStyle(Paint.Style.STROKE);
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
	
	protected boolean select() {
		if (!selected) {
			strokePaint.setColor(Color.rgb(255, 96, 0));
			strokePaint.setStrokeWidth(7);
			invalidate();
			selected = true;
			return true;
		} else {
			strokePaint.setColor(Color.rgb(25, 62, 147));
			strokePaint.setStrokeWidth(2);
			invalidate();
			selected = false;
			return false;
		}
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
		canvas.drawRoundRect(r,30,30, strokePaint);
	}
	
	protected boolean contains(int x, int y) {
		return r.contains(x, y);
	}

}