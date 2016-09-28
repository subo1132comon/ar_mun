/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.byt.market.ui.mine;



import com.byt.market.R;

import android.widget.TextView;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;


/**
 * TextView that draws a bubble behind the text. We cannot use a LineBackgroundSpan
 * because we want to make the bubble taller than the text and TextView's clip is
 * too aggressive.
 */
public class BubbleTextView extends TextView {
    static final float CORNER_RADIUS = 8.0f;
    static final float PADDING_H = 5.0f;
    static final float PADDING_V = 1.0f;

    private final RectF mRect = new RectF();
    private Paint mPaint;

    private final static String TAG = "Launcher.BubbleTextView";

    private boolean mBackgroundSizeChanged;
    private Drawable mBackground;

    private boolean mEnableReflection = false;
    private Bitmap mReflection;

    private static Drawable mShadowDrawable;

    private boolean mPressed = false;

    private Bitmap mIconMask = null;
    private Drawable mTopDrawable = null;

    private Rect mDrawingRect = null;

    private boolean mShowDownloadMask = false;

    private Bitmap mDownloadMask = null;
    
    public BubbleTextView(Context context) {
        super(context);
        init(context);
    }

    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BubbleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setPressed(boolean pressed) {
        if (mPressed != pressed) destroyDrawingCache();

        mPressed = pressed;

        invalidate();
    }
    
    public void setShowDownloadMask(boolean showMask){
    	if (mShowDownloadMask != showMask) destroyDrawingCache();

    	mShowDownloadMask = showMask;

        invalidate();
    }

    private Bitmap getIconReflection() {
        if (mReflection != null) return mReflection;

        Drawable[] icons = getCompoundDrawables();
        BitmapDrawable icon = (BitmapDrawable)icons[1]; // top

        Bitmap bp;
        
        int width = icon.getIntrinsicWidth();
        int height = icon.getIntrinsicHeight();
        
        bp = Bitmap.createScaledBitmap(icon.getBitmap(), width, height, false);

        //This will not scale but will flip on the Y axis
        Matrix matrix = new Matrix();
        matrix.preScale(1f, -1f);

        //Create a Bitmap with the flip matrix applied to it.
        //We only want the bottom half of the image
        Bitmap reflection = Bitmap.createBitmap(bp, 0, height/2, width, height/2, matrix, false);

        //Create a new Canvas with the bitmap.
        Canvas canvas = new Canvas(reflection);

        //Create a shader that is a linear gradient that covers the reflection
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, 0, 0, height/3,
                0x66ffffff, 0x44ffffff, Shader.TileMode.CLAMP);

        //Set the paint to use this shader (linear gradient)
        paint.setShader(shader);
        paint.setAntiAlias(true);

        //Set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        //Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, 0, width, (height / 2), paint);

        mReflection = reflection;

        return reflection;
    }

    public void enableReflection(boolean enable) {
        if (mEnableReflection != enable) destroyDrawingCache();
        mEnableReflection = enable;

        if (mReflection != null) {
            mReflection.recycle();
            mReflection = null;
        }
    }

    private void init(Context context) {
        setDrawingCacheEnabled(true);
        
        setFocusable(true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        if (mShadowDrawable == null) {
            mShadowDrawable = getContext().getResources().getDrawable(R.drawable.icon_shadow);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        Drawable topDrawable = mTopDrawable;
        if (topDrawable == null) {
            Drawable[] drawables = getCompoundDrawables();
            topDrawable = drawables[1];
            mTopDrawable = topDrawable;
        }

        if (mDrawingRect == null) {
            mDrawingRect = new Rect();
            getDrawingRect(mDrawingRect);
        }

        if (mEnableReflection) {
            Bitmap reflection = getIconReflection();

            int compoundPaddingRight = getCompoundPaddingRight();
            int compoundPaddingLeft = getCompoundPaddingLeft();
            int hspace = mDrawingRect.right - mDrawingRect.left - compoundPaddingRight - compoundPaddingLeft;
            int l = mDrawingRect.left + compoundPaddingLeft + (hspace - topDrawable.getMinimumWidth()) / 2;
            int t = mDrawingRect.top + getPaddingTop() + topDrawable.getMinimumHeight();

            canvas.drawBitmap(reflection, l, t, mPaint);
        } else {
            Drawable d = mShadowDrawable;
            Rect r = mDrawingRect;
            int w = d.getMinimumWidth();
            int h = d.getMinimumHeight();
            int l = r.left + (r.right - r.left - w)/2;
            int t = r.top;
            t = t + (r.bottom - t)/2 - getLineHeight() - 4;
            d.setBounds(l, t, l+w, t+h);
            d.draw(canvas);
            /*
            Paint p = new Paint(0xFFFFFFFF);
            canvas.drawRect(mDrawingRect, p);
            */
        }

        super.draw(canvas);

        
        /* draw mask if pressed */
        if (mPressed) {
            if (mIconMask == null) {
                int w = topDrawable.getMinimumWidth();
                int h = topDrawable.getMinimumHeight();

                Bitmap m = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

                Canvas c = new Canvas(m);

                topDrawable.draw(c);

                Paint p = new Paint();
                p.setColor(0x85000000);
                p.setAntiAlias(true);
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                c.drawPaint(p);

                mIconMask = m;
            }

            int compoundPaddingRight = getCompoundPaddingRight();
            int compoundPaddingLeft = getCompoundPaddingLeft();
            int hspace = mDrawingRect.right - mDrawingRect.left - compoundPaddingRight - compoundPaddingLeft;
            int l = mDrawingRect.left + compoundPaddingLeft + (hspace - topDrawable.getMinimumWidth()) / 2;
            int t = mDrawingRect.top + getPaddingTop();
            canvas.drawBitmap(mIconMask, l, t, mPaint);
        } else if (mShowDownloadMask) {
                if (mDownloadMask == null) {
                    int w = topDrawable.getMinimumWidth();
                    int h = topDrawable.getMinimumHeight();

                    final Bitmap m = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

                    Canvas c = new Canvas(m);

                    topDrawable.draw(c);

                    Paint p = new Paint();
                    p.setColor(0x55000000);
                    p.setAntiAlias(true);
                    p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                    c.drawPaint(p);

                    mDownloadMask = m;
                }

                int compoundPaddingRight = getCompoundPaddingRight();
                int compoundPaddingLeft = getCompoundPaddingLeft();
                int hspace = mDrawingRect.right - mDrawingRect.left - compoundPaddingRight - compoundPaddingLeft;
                int l = mDrawingRect.left + compoundPaddingLeft + (hspace - topDrawable.getMinimumWidth()) / 2;
                int t = mDrawingRect.top + getPaddingTop();
                canvas.drawBitmap(mDownloadMask, l, t, mPaint);
            }
        
        if (false) {
                mPaint.setColor(Color.RED);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(mDrawingRect, mPaint);
                mPaint.setColor(0);
        }
    }
}
