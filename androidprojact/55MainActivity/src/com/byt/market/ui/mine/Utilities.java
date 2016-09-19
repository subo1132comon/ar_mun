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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.DrawFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;

import com.byt.market.R;
import com.byt.market.util.LogUtil;


/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {
    private static final String TAG = "Launcher.Utilities";

    private static final boolean TEXT_BURN = false;

    public static int sIconWidth = -1;
    public static int sIconHeight = -1;
    public static int sIconTextureWidth = -1;
    public static int sIconTextureHeight = -1;

    private static final Paint sTextPaint = new Paint();
    private static final Paint sBlurPaint = new Paint();
    private static final Paint sGlowColorPressedPaint = new Paint();
    private static final Paint sGlowColorFocusedPaint = new Paint();
    private static final Paint sDisabledPaint = new Paint();
    private static final Rect sBounds = new Rect();
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));

    }

    private static DrawFilter sDrawFilter = new PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG);

    private static HashMap<String, Integer> sIconBitmapMap;
    private static HashMap<String, Integer> sIconBitmapMapPackage;
    private static HashMap<String, Integer> sIconBitmapMapPN;

    private static void addIconBitmap(String className, int id) {
        if (sIconBitmapMap == null) {
            sIconBitmapMap = new HashMap<String, Integer>();
        }
        if(sIconBitmapMapPackage == null){
                sIconBitmapMapPackage = new HashMap<String, Integer>();
        }
        sIconBitmapMap.put(className, id);
        sIconBitmapMapPackage.put(className.substring(0, className.lastIndexOf(".") ), id);
    }

    private static void addIconBitmapPN(String className, int id) {
        if (sIconBitmapMapPN == null) {
            sIconBitmapMapPN = new HashMap<String, Integer>();
        }

        sIconBitmapMapPN.put(className, id);
    }

    public static Bitmap getIconBitmapAsTheme(Context context, String className, Drawable defaultIcon) {
        return getIconBitmapAsTheme(context, className, defaultIcon, null);
    }

    public static Bitmap getIconBitmapAsTheme(Context context, String className, Drawable defaultIcon, Bitmap reusedBitmap) {
    	addIconBitmap("com.a",1);
        Integer id = sIconBitmapMap.get(className);
        if(id == null || id < 0){
                id = sIconBitmapMapPackage.get(className.substring(0, className.lastIndexOf(".")));
        }
        if (id != null) {
            Drawable icon = null;
            if (id < 0) {
                icon = defaultIcon;
            } else {
                icon = context.getResources().getDrawable(id);
            }

            if (reusedBitmap != null) reusedBitmap.eraseColor(0);
            Bitmap bm = createIconBitmap(icon, context, false, false, null, reusedBitmap);

            if (className.equals("com.rili.android.client.CalendarStartPage") ||
                className.equals("easyandroid.android.calendar.LaunchActivity") ||
                className.equals("com.android.apple.calendar.LaunchActivity")) {
                Canvas canvas = new Canvas(bm);
                canvas.setDrawFilter(sDrawFilter);

                Paint p = sTextPaint;
                p.setFakeBoldText(true);
                //if (false) LogUtil.d(TAG, "getIconBitmapAsTheme week:"+DateMonitor.getWeek()+" date:"+DateMonitor.getDate());

                /*
                try {
                    Typeface tf = Typeface.createFromAsset(context.getAssets(),
                            "fonts/yahei.ttf");
                    p.setTypeface(tf);
                } catch (Exception e) {
                }
                */

               /* int weekday_size = (int) context.getResources().getDimension(R.dimen.weekday_size);
                int weekday_vertical = (int) context.getResources().getDimension(R.dimen.weekday_vertical);

                p.setTextSize(weekday_size);
                p.setColor(Color.WHITE);

                String week = DateMonitor.getWeek();
                canvas.drawText(week, (sIconTextureWidth-p.measureText(week))/2, weekday_vertical, p);
 
                int date_size  = (int) context.getResources().getDimension(R.dimen.date_size);
                int date_vertical = (int) context.getResources().getDimension(R.dimen.date_vertical);

                p.setTextSize(date_size);
                p.setColor(Color.BLACK);

                String date = DateMonitor.getDate();
                canvas.drawText(date, (sIconTextureWidth-p.measureText(date))/2, date_vertical, p);*/
            }

            return bm;
        }

        return null;
    }

    static Bitmap centerToFit(Bitmap bitmap, int width, int height, Context context) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        if (bitmapWidth < width || bitmapHeight < height) {
            int color = Color.TRANSPARENT;

            Bitmap centered = Bitmap.createBitmap(bitmapWidth < width ? width : bitmapWidth,
                    bitmapHeight < height ? height : bitmapHeight, Bitmap.Config.ARGB_8888);
            centered.setDensity(bitmap.getDensity());
            Canvas canvas = new Canvas(centered);
            canvas.drawColor(color);
            canvas.drawBitmap(bitmap, (width - bitmapWidth) / 2.0f, (height - bitmapHeight) / 2.0f,
                    null);

            bitmap = centered;
        }

        return bitmap;
    }

    static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    static int sColorIndex = 0;

    private final static int sIconNum = 5;
    private final static Random mRandom = new Random();
    private static Drawable sIconBg[];

    private static int sPrinted = 0;

    private static double retrieveMainColor(Bitmap bitmap) {
        //if (sPrinted > 8) return 0;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //LogUtil.d(TAG, "w:"+width+" h:"+height);

        int bitsCount = width*height;
        int[] bits = new int[bitsCount];
        double[] harray = new double[bitsCount];
        double[] sarray = new double[bitsCount];
        double[] iarray = new double[bitsCount];
        double[] rarray = new double[bitsCount];
        double[] garray = new double[bitsCount];
        double[] barray = new double[bitsCount];

        //LogUtil.d(TAG, "retrieveMainColor bitsCount:"+bitsCount);
        bitmap.getPixels(bits, 0, width, 0, 0, width, height);

        /* switch rgb to hsi */
        int r = 0; int g = 0; int b = 0;
        double h = 0; double s = 0; double i = 0;
        for (int idx = 0; idx < bitsCount; idx++) {
            int bit = bits[idx];

            r = Color.red(bit);
            g = Color.green(bit);
            b = Color.blue(bit);

            rarray[idx] = r;
            garray[idx] = g;
            barray[idx] = b;

            if (r+g+b == 0 || r==g && g==b) {
                h = 0; s = 0; i = 0;
                continue;
            }

            double t1 = ((r - g) + (r - b)) / 2;
            double t2 = Math.sqrt(((r - g)*(r - g)) + ((r - b)*(g - b)));
            double x = Math.acos(t1 / t2);

            h = (g > b) ? x : (2*Math.PI - x);
            s = 1 - 3*(Math.min(r, Math.min(g,b)))/(r+g+b);
            i = (r+g+b) / 3;

            harray[idx] = h;
            sarray[idx] = s;
            iarray[idx] = i;

            if (false) {
            if (r+g+b != 0) {
                LogUtil.d(TAG, "r:"+r+" g:"+g+" b:"+b);
                LogUtil.d(TAG, "h:"+h+" s:"+s+" i:"+i);
            }
            }
        }

        if (false) {
            LogUtil.d(TAG, "------------- red ----------------");
            LogUtil.d(TAG, Arrays.toString(rarray));
            LogUtil.d(TAG, "------------- green ----------------");
            LogUtil.d(TAG, Arrays.toString(garray));
            LogUtil.d(TAG, "------------- blue ----------------");
            LogUtil.d(TAG, Arrays.toString(barray));
            LogUtil.d(TAG, "------------- h ----------------");
            LogUtil.d(TAG, Arrays.toString(harray));
            LogUtil.d(TAG, "------------- s ----------------");
            LogUtil.d(TAG, Arrays.toString(sarray));
            LogUtil.d(TAG, "------------- i ----------------");
            LogUtil.d(TAG, Arrays.toString(iarray));
        }
        sPrinted++;

        double winCenter = 0;
        int winSize = 0;
        double temp = 0;
        double precent = .0f;
        double center = 0;

        do {
            winSize++;
            for (winCenter = 0; winCenter <= 2*Math.PI; winCenter+=0.1) {
                temp = 0;
                for (int idx = 0; idx < bitsCount; idx++) {
                    if (harray[idx] == Double.NaN || harray[idx] == 0) continue;
                    //LogUtil.d(TAG, "harray:"+Arrays.toString(harray));
                    if (Math.abs(harray[idx] - winCenter) < winSize) {
                        temp++;
                    }
                }

                if (temp / bitsCount > precent) {
                    center = winCenter; precent = temp / bitsCount;
                }
            }
        } while (winSize < 30);

        //LogUtil.d(TAG, "center:"+center+" precent:"+precent);
        //LogUtil.d(TAG, log.toString());

        return center;
    }



    /**
     * Returns a bitmap suitable for the all apps view.  The bitmap will be a power
     * of two sized ARGB_8888 bitmap that can be used as a gl texture.
     */
    static Bitmap createIconBitmap(Drawable icon, Context context, String className) {
        return createIconBitmap(icon, context, true, true, className);
    }

    static Bitmap createIconBitmap(Drawable icon, Context context, boolean draw_bg, boolean draw_mask, String className) {
        return createIconBitmap(icon, context, draw_bg, draw_mask, className, null);
    }

    static Bitmap createIconBitmap(Drawable icon, Context context, boolean draw_bg, boolean draw_mask, String className, Bitmap reusedBitmap) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;

            Bitmap iconBitmap = null;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                iconBitmap = bitmap;
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }

            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                } else if (sourceWidth < width && sourceHeight < height) {
                    // It's small, use the size they gave us.
//                    width = sourceWidth;
//                    height = sourceHeight;
                }
            }


            // no intrinsic size --> use default size

            int textureWidth = sIconWidth;
            int textureHeight = sIconHeight;

            Bitmap bitmap = null;
            if (reusedBitmap != null) {
                bitmap = reusedBitmap;
            } else {
                bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                        Bitmap.Config.ARGB_8888);
            }

            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            int left = (textureWidth-width) / 2;
            int top = (textureHeight-height) / 2;

            if (false) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            if (false) {
                // draw transparent bg for the icon
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                //LinearGradient shader = new LinearGradient(0, 0, 0, height,
                //        0x11666666, 0x88000000, Shader.TileMode.CLAMP);
                LinearGradient shader = new LinearGradient(0, 0, 0, height,
                        0x11FFFFFF, 0x88666666, Shader.TileMode.CLAMP);

                //Set the paint to use this shader (linear gradient)
                paint.setShader(shader);

                RectF rect = new RectF(left, top, left+width, top+height);
                canvas.drawRoundRect(rect, 10.0f, 10.0f, paint);

                paint.setShader(null);
                paint.setColor(0xCCCCCCCC);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRoundRect(rect, 10.0f, 10.0f, paint);
            }

            if (draw_bg) {
                if (sIconBg == null) {
                    sIconBg = new Drawable[sIconNum];
                    /*sIconBg[0] = context.getResources().getDrawable(R.drawable.icon_mask0_bg);
                    sIconBg[1] = context.getResources().getDrawable(R.drawable.icon_mask1_bg);
                    sIconBg[2] = context.getResources().getDrawable(R.drawable.icon_mask2_bg);
                    sIconBg[3] = context.getResources().getDrawable(R.drawable.icon_mask3_bg);
                    sIconBg[4] = context.getResources().getDrawable(R.drawable.icon_mask4_bg);*/
                    
                    /*
                    sIconBg[5] = context.getResources().getDrawable(R.drawable.icon_mask5_bg);
                    sIconBg[6] = context.getResources().getDrawable(R.drawable.icon_mask6_bg);
                    sIconBg[7] = context.getResources().getDrawable(R.drawable.icon_mask7_bg);
                    sIconBg[8] = context.getResources().getDrawable(R.drawable.icon_mask8_bg);
                    sIconBg[9] = context.getResources().getDrawable(R.drawable.icon_mask9_bg);
                    */
                }

                int color = -1;//getIconColor(context, className);
                if (color < 0) {
                    color = mRandom.nextInt(sIconNum);
                }

                /*
                double color = retrieveMainColor(iconBitmap);
                if (DBG) LogUtil.d(TAG, "got icon color:"+color);
                if (color >= 0 && color < 0.6 || color > 2*Math.PI - 0.6) {
                    idx = 0;
                } else if (color >= 0.6 && color < 1.2) {
                    idx = 1;
                } else if (color >= 1.2 && color < 1.8) {
                    idx = 2;
                } else if (color >= 1.8 && color < 2.4) {
                    idx = 3;
                } else if (color >= 2.4 && color < Math.PI) {
                    idx = 4;
                } else if (color >= Math.PI && color < Math.PI+0.6) {
                    idx = 5;
                } else if (color >= Math.PI+0.6 && color < Math.PI+1.2) {
                    idx = 6;
                } else if (color >= Math.PI+1.2 && color < Math.PI+1.8) {
                    idx = 0;
                } else if (color >= Math.PI+1.8 && color < Math.PI+2.4) {
                    idx = 1;
                } else {
                    idx = 2;
                }
                 */
                Drawable bg = sIconBg[color];
                if (bg != null) {
                    sOldBounds.set(bg.getBounds());
                    bg.setBounds(0, 0, textureWidth, textureHeight);
                    bg.draw(canvas);
                    bg.setBounds(sOldBounds);
                }
            } else {
                /* if do not draw bg, fill the icon to whole filed. */
                left = 0;
                top = 0;
                width = textureWidth;
                height = textureHeight;
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);

            if (draw_mask) {
                Drawable mask = null;//context.getResources().getDrawable(R.drawable.icon_mask_front);
                if (mask != null) {
                    sOldBounds.set(mask.getBounds());
                    mask.setBounds(0, 0, textureWidth, textureHeight);
                    mask.draw(canvas);
                    mask.setBounds(sOldBounds);
                }
            }
            
            return bitmap;
        }
    }

    static void drawSelectedAllAppsBitmap(Canvas dest, int destWidth, int destHeight,
            boolean pressed, Bitmap src) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                // We can't have gotten to here without src being initialized, which
                // comes from this file already.  So just assert.
                //initStatics(context);
                throw new RuntimeException("Assertion failed: Utilities not initialized");
            }

            dest.drawColor(0, PorterDuff.Mode.CLEAR);

            int[] xy = new int[2];
            Bitmap mask = src.extractAlpha(sBlurPaint, xy);

            float px = (destWidth - src.getWidth()) / 2;
            float py = (destHeight - src.getHeight()) / 2;
            dest.drawBitmap(mask, px + xy[0], py + xy[1],
                    pressed ? sGlowColorPressedPaint : sGlowColorFocusedPaint);

            mask.recycle();
        }
    }

    /**
     * Returns a Bitmap representing the thumbnail of the specified Bitmap.
     * The size of the thumbnail is defined by the dimension
     * android.R.dimen.launcher_application_icon_size.
     *
     * @param bitmap The bitmap to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified bitmap or the bitmap itself if the
     *         thumbnail could not be created.
     */
    static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            if (bitmap.getWidth() == sIconWidth && bitmap.getHeight() == sIconHeight) {
                return bitmap;
            } else {
                return createIconBitmap(new BitmapDrawable(bitmap), context, null);
            }
        }
    }

    static Bitmap drawDisabledBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }
            final Bitmap disabled = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(disabled);
            
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, sDisabledPaint);

            return disabled;
        }
    }

    private static void initStatics(Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;
        sIconWidth = sIconHeight = /*metrics.widthPixels > 0*/ false ? metrics.widthPixels / 8 : (int) resources.getDimension(R.dimen.recent_applications_app_icon_size);
        sIconTextureWidth = sIconTextureHeight =/* metrics.widthPixels > 0*/ false ? metrics.widthPixels / 8 : (int) resources.getDimension(R.dimen.recent_applications_app_icon_mask);

        sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density, BlurMaskFilter.Blur.NORMAL));
        sGlowColorPressedPaint.setColor(0xffffc300);
        //sGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        sGlowColorFocusedPaint.setColor(0xffff8e00);
        //sGlowColorFocusedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
        sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        sDisabledPaint.setAlpha(0x88);
    }

    static class BubbleText {
        private static final int MAX_LINES = 2;

        private final TextPaint mTextPaint;

        private final RectF mBubbleRect = new RectF();

        private final float mTextWidth;
        private final int mLeading;
        private final int mFirstLineY;
        private final int mLineHeight;

        private final int mBitmapWidth;
        private final int mBitmapHeight;
        private final int mDensity;

        BubbleText(Context context) {
            final Resources resources = context.getResources();

            final DisplayMetrics metrics = resources.getDisplayMetrics();
            final float scale = metrics.density;
            mDensity = metrics.densityDpi;

            final float paddingLeft = 2.0f * scale;
            final float paddingRight = 2.0f * scale;
            final float cellWidth = resources.getDimension(R.dimen.title_texture_width);

            RectF bubbleRect = mBubbleRect;
            bubbleRect.left = 0;
            bubbleRect.top = 0;
            bubbleRect.right = (int) cellWidth;

            mTextWidth = cellWidth - paddingLeft - paddingRight;

            TextPaint textPaint = mTextPaint = new TextPaint();
            textPaint.setTypeface(Typeface.DEFAULT);
            textPaint.setTextSize(13*scale);
            textPaint.setColor(0xffffffff);
            textPaint.setAntiAlias(true);
            if (TEXT_BURN) {
                textPaint.setShadowLayer(8, 0, 0, 0xff000000);
            }

            float ascent = -textPaint.ascent();
            float descent = textPaint.descent();
            float leading = 0.0f;//(ascent+descent) * 0.1f;
            mLeading = (int)(leading + 0.5f);
            mFirstLineY = (int)(leading + ascent + 0.5f);
            mLineHeight = (int)(leading + ascent + descent + 0.5f);

            mBitmapWidth = (int)(mBubbleRect.width() + 0.5f);
            mBitmapHeight = roundToPow2((int)((MAX_LINES * mLineHeight) + leading + 0.5f));

            mBubbleRect.offsetTo((mBitmapWidth-mBubbleRect.width())/2, 0);

            if (false) {
                LogUtil.d(TAG, "mBitmapWidth=" + mBitmapWidth + " mBitmapHeight="
                        + mBitmapHeight + " w=" + ((int)(mBubbleRect.width() + 0.5f))
                        + " h=" + ((int)((MAX_LINES * mLineHeight) + leading + 0.5f)));
            }
        }

        /** You own the bitmap after this and you must call recycle on it. */
        Bitmap createTextBitmap(String text) {
            Bitmap b = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ALPHA_8);
            b.setDensity(mDensity);
            Canvas c = new Canvas(b);

            StaticLayout layout = new StaticLayout(text, mTextPaint, (int)mTextWidth,
                    Alignment.ALIGN_CENTER, 1, 0, true);
            int lineCount = layout.getLineCount();
            if (lineCount > MAX_LINES) {
                lineCount = MAX_LINES;
            }
            //if (!TEXT_BURN && lineCount > 0) {
                //RectF bubbleRect = mBubbleRect;
                //bubbleRect.bottom = height(lineCount);
                //c.drawRoundRect(bubbleRect, mCornerRadius, mCornerRadius, mRectPaint);
            //}
            for (int i=0; i<lineCount; i++) {
                //int x = (int)((mBubbleRect.width() - layout.getLineMax(i)) / 2.0f);
                //int y = mFirstLineY + (i * mLineHeight);
                final String lineText = text.substring(layout.getLineStart(i), layout.getLineEnd(i));
                int x = (int)(mBubbleRect.left
                        + ((mBubbleRect.width() - mTextPaint.measureText(lineText)) * 0.5f));
                int y = mFirstLineY + (i * mLineHeight);
                c.drawText(lineText, x, y, mTextPaint);
            }

            return b;
        }

        private int height(int lineCount) {
            return (int)((lineCount * mLineHeight) + mLeading + mLeading + 0.0f);
        }

        int getBubbleWidth() {
            return (int)(mBubbleRect.width() + 0.5f);
        }

        int getMaxBubbleHeight() {
            return height(MAX_LINES);
        }

        int getBitmapWidth() {
            return mBitmapWidth;
        }

        int getBitmapHeight() {
            return mBitmapHeight;
        }
    }

    /** Only works for positive numbers. */
    static int roundToPow2(int n) {
        int orig = n;
        n >>= 1;
        int mask = 0x8000000;
        while (mask != 0 && (n & mask) == 0) {
            mask >>= 1;
        }
        while (mask != 0) {
            n |= mask;
            mask >>= 1;
        }
        n += 1;
        if (n != orig) {
            n <<= 1;
        }
        return n;
    }
}