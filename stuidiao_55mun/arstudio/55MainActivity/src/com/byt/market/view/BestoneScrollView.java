package com.byt.market.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class BestoneScrollView extends ScrollView {
	  private ScrollViewListener scrollViewListener = null;  
	  
	    public BestoneScrollView(Context context) {  
	        super(context);  
	    }  
	  
	    public BestoneScrollView(Context context, AttributeSet attrs,  
	            int defStyle) {  
	        super(context, attrs, defStyle);  
	    }  
	  
	    public BestoneScrollView(Context context, AttributeSet attrs) {  
	        super(context, attrs);  
	    }  
	  
	    public void setScrollViewListener(ScrollViewListener scrollViewListener) {  
	        this.scrollViewListener = scrollViewListener;  
	    }  
	  
	    @Override  
	    protected void onScrollChanged(int x, int y, int oldx, int oldy) {  
	        super.onScrollChanged(x, y, oldx, oldy);  
	        if (scrollViewListener != null) {  
	            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);  
	        }  
	    }  
}
