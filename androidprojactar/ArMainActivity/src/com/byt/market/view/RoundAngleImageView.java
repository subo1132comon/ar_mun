package com.byt.market.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.byt.market.util.BitmapUtil;
import com.byt.market.util.ImageCache;
import com.byt.market.util.LogUtil;

public class RoundAngleImageView extends ImageView {
	private Paint paint = new Paint();

	public RoundAngleImageView(Context context) {
		super(context);
	}

	public RoundAngleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RoundAngleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		LogUtil.e("cexo", "RoundAngleImageView onDraw()");
		Drawable drawable = getDrawable();
		if (null != drawable) {
			Bitmap bitmap = BitmapUtil.drawableToBitmap(drawable);
			Bitmap b = ImageCache.getInstance().getBitmap("roundAngleImage");
			if (b == null) {
				b = toRoundCorner(bitmap, getWidth());
				ImageCache.getInstance().cacheImage("roundAngleImage", b);
			}
			final Rect rect = new Rect(0, 0, b.getWidth(), b.getHeight());
			this.paint.setARGB(255, 170, 170, 170);
			this.paint.setStrokeWidth(2);
			canvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2,
					this.paint);
			paint.reset();
			canvas.drawBitmap(b, rect, rect, paint);

		} else {
			super.onDraw(canvas);
		}
	}

	private Bitmap toRoundCorner(Bitmap bitmap, int x) {
		LogUtil.e("cexo", "RoundAngleImageView toRoundCorner()");
		Bitmap zoomImg = BitmapUtil.zoomImg(bitmap, x);
		Bitmap output = Bitmap.createBitmap(x, x, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Rect rect = new Rect(0, 0, x, x);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawCircle(x / 2, x / 2, x / 2 - 2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(zoomImg, rect, rect, paint);
		zoomImg.recycle();
		return output;
	}

}