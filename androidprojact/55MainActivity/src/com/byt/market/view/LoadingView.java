package com.byt.market.view;

import java.util.Calendar;

import com.byt.market.R;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.ImageView;

public class LoadingView extends ImageView {

	private Calendar mCalendar;
	private Runnable mTicker;
	private Handler mHandler;
	private boolean mTickerStopped = false;
	long mDuration = 400;
	int index = 0;
	int[] ids = new int[]{R.drawable.loading01,R.drawable.loading02,R.drawable.loading03,R.drawable.loading04};

	public LoadingView(Context context) {
		super(context);
		initCalendar();
	}

	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCalendar();
	}

	public LoadingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initCalendar();
	}

	private void initCalendar() {
		if(mCalendar == null){
			mCalendar = Calendar.getInstance();
		}
	}

	@Override
	protected void onAttachedToWindow() {
		mTickerStopped = false;
		super.onAttachedToWindow();
		mHandler = new Handler();
		mTicker = new Runnable() {
			
			@Override
			public void run() {
				if(mTickerStopped){
					return;
				}
				setImageResource(ids[index++]);
				if(index == ids.length){
					index = 0;
				}
				long now = SystemClock.uptimeMillis();
				long next = now + (mDuration - now % mDuration);
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mTickerStopped = true;
	}
}
