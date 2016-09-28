package com.byt.market.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AlwsydMarqueeTextView extends TextView{

    public AlwsydMarqueeTextView(Context context) {
        super(context);
    }

    public AlwsydMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlwsydMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
