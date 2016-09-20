package com.byt.market.view;

import com.byt.market.util.ScreenTool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class IntervalTextview extends TextView{

	 private int sroke_width = 1;  
	public IntervalTextview(Context context) {
		super(context);
	}
	public IntervalTextview(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
	//  将边框设为黑色  
        paint.setColor(android.graphics.Color.BLACK); 
        paint.setAlpha(20);
        paint.setStrokeWidth(ScreenTool.dpToPx((float) 0.02));
        //  画TextView的4个边  
        canvas.drawLine(0, 0, this.getWidth() - sroke_width, 0, paint);  
       // canvas.drawLine(0, 0, 0, this.getHeight() - sroke_width, paint);  
        //canvas.drawLine(this.getWidth() - sroke_width, 0, this.getWidth() - sroke_width, this.getHeight() - sroke_width, paint);  
        canvas.drawLine(0, this.getHeight() - sroke_width, this.getWidth() - sroke_width, this.getHeight() - sroke_width, paint);  
        super.onDraw(canvas);
	}
}
