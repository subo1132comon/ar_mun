package com.byt.market.bitmaputil.core.display;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Fake displayer which doesn't display Bitmap in ImageView. Should be used for in {@linkplain DisplayImageOptions
 * display options} for
 * {@link ImageLoader#loadImage(String, int, int, com.nostra13.universalimageloader.core.DisplayImageOptions, com.nostra13.universalimageloader.core.assist.ImageLoadingListener)
 * ImageLoader.loadImage()}
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public final class FakeBitmapDisplayer implements BitmapDisplayer {
	@Override
	public Bitmap display(Bitmap bitmap, ImageView imageView) {
		// Do nothing
		return bitmap;
	}
}
