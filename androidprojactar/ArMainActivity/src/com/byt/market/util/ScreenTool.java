package com.byt.market.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;

public class ScreenTool {


	public static float scale;

	public static float fontScale;

	public static int screenWidth;
	public static int screenHeight;

	public static void init(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		scale = dm.density;
		fontScale = dm.scaledDensity;
		screenWidth = dm.widthPixels;
		screenHeight =dm.heightPixels;
		Log.d("screen", "屏幕宽度是:" + screenWidth +  "屏幕高度："+screenHeight +" dp:" + scale);
	}

	/**
	 * 根据指定的参数绘画一个圆弧
	 * 
	 * @param canvas
	 * @param paint
	 * @param x
	 * @param y
	 * @param color
	 * @param radius
	 * @param startAngle
	 * @param angle
	 */
	public static void drawAnnular(Canvas canvas, Paint paint, float x,
			float y, int color, float radius, float startAngle, float angle) {
		paint.setColor(color);
		RectF rf1 = new RectF(x - radius, y - radius, x + radius, y + radius);
		canvas.drawArc(rf1, startAngle, angle, false, paint);
	}

	/**
	 * 判断两个点是否足够接近
	 * 
	 * @param cx
	 * @param cy
	 * @param x
	 * @param y
	 * @param dis
	 * @return
	 */
	public static boolean closeEnough(float cx, float cy, float x, float y,
			float dis) {
		return x < cx + dis && x > cx - dis && y < cy + dis && y > cy - dis;
	}

	public static int dpToPx(float dp) {
		return (int) (dp * scale+0.5f);
	}

	public static float spToPx(float sp) {
		return sp * fontScale;
	}


}
