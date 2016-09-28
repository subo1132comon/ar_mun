package com.byt.market.bitmaputil.core.display;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.byt.market.MyApplication;
import com.byt.market.bitmaputil.utils.L;
import com.byt.market.util.BitmapUtil;

/**
 * Displays bitmap with rounded corners. <br />
 * <b>NOTE:</b> New {@link Bitmap} object is created for displaying. So this class needs more memory and can cause
 * {@link OutOfMemoryError}.
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class BannerBitmapDisplayer implements BitmapDisplayer {

	private int roundPixels;

	public BannerBitmapDisplayer(int roundPixels) {
		this.roundPixels = roundPixels;
	}

	@Override
	public Bitmap display(Bitmap bitmap, ImageView imageView) {
		Bitmap roundBitmap;
		try {
			roundBitmap = getRoundedCornerBitmap(bitmap);
		} catch (OutOfMemoryError e) {
			L.e(e, "Can't create bitmap with rounded corners. Not enough memory.");
			roundBitmap = bitmap;
		}
		imageView.setImageBitmap(roundBitmap);
		return roundBitmap;
	}

	private Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		DisplayMetrics metrics = MyApplication.getInstance().outMetrics;
		Bitmap mBitmap = BitmapUtil.zoomImg(bitmap, (int) ((metrics.widthPixels - 20 * metrics.density) / 2));
		Bitmap output = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Config.ARGB_4444);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(0xFFFFFFFF);
		canvas.drawRoundRect(rectF, roundPixels, roundPixels, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(mBitmap, rect, rect, paint);

		return output;
	}
}
