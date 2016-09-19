package com.byt.market.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ControlScrollViewPager extends ViewPager {

	private boolean cantScroll = false;

	public boolean isCantScroll() {
		return cantScroll;
	}

	public void setCantScroll(boolean cantScroll) {
		this.cantScroll = cantScroll;
	}

	public ControlScrollViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ControlScrollViewPager(Context context) {
		super(context);
	}

	@Override
	protected boolean canScroll(View arg0, boolean arg1, int arg2, int arg3,
			int arg4) {
		if (cantScroll)
			return true;
		return super.canScroll(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		if (cantScroll)
			return true;
		return super.onTouchEvent(arg0);
	}

}
