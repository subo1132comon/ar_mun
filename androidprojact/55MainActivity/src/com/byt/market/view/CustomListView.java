package com.byt.market.view;

import com.byt.market.MarketContext;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class CustomListView extends ListView {

	public CustomListView(Context context) {
		super(context);
	}
	
	public CustomListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (MarketContext.getInstance().isGalleryMove) {
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}
}
