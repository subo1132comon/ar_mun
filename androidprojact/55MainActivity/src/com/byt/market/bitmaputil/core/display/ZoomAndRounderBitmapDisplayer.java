package com.byt.market.bitmaputil.core.display;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.ImageView;

/**
 * Just displays {@link Bitmap} in {@link ImageView}
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public final class ZoomAndRounderBitmapDisplayer implements BitmapDisplayer {
	private int zoomScale;

	public ZoomAndRounderBitmapDisplayer(int zoomScale) {
		this.zoomScale = zoomScale;
	}

	@Override
	public Bitmap display(Bitmap bitmap, ImageView imageView) {
		try{
		Bitmap mBitmap = zoomImg(bitmap, zoomScale);
		imageView.setImageBitmap(mBitmap);
		return bitmap;
		}
		catch(Exception e)
		{
			return null;
		}
	}

	private Bitmap zoomImg(Bitmap bitmap, int newWidth) {
		try{
		// 获得图片的宽高
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / width;
		// float scaleHeight = ((float) newHeight) / height;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleWidth);
		// 得到新的图片
		if (bitmap.isRecycled())
			return null;
		Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
				true);
		// bitmap.recycle();
		Bitmap bitmap2 = fillet(newbm, 10);
		return bitmap2;
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public static Bitmap fillet(Bitmap bitmap, int roundPx) {
		try {
			// 其原理就是：先建立一个与图片大小相同的透明的Bitmap画板
			// 然后在画板上画出一个想要的形状的区域。
			// 最后把源图片帖上。
			final int width = bitmap.getWidth();
			final int height = bitmap.getHeight();

			Bitmap paintingBoard = Bitmap.createBitmap(width, height,
					Config.ARGB_4444);
			Canvas canvas = new Canvas(paintingBoard);
			canvas.drawARGB(Color.TRANSPARENT, Color.TRANSPARENT,
					Color.TRANSPARENT, Color.TRANSPARENT);

			final Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);

			clipTop(canvas, paint, roundPx, width, height);
//			clipLeft(canvas, paint, roundPx, width, height);
//			clipRight(canvas, paint, roundPx, width, height);

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			// 帖子图
			final Rect src = new Rect(0, 0, width, height);
			final Rect dst = src;
			canvas.drawBitmap(bitmap, src, dst, paint);
			return paintingBoard;
		} catch (Exception exp) {
			return bitmap;
		}
	}

	private static void clipLeft(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final Rect block = new Rect(offset, 0, width, height);
		canvas.drawRect(block, paint);
		final RectF rectF = new RectF(0, 0, offset * 2, height);
		canvas.drawRoundRect(rectF, offset, offset, paint);
	}

	private static void clipRight(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final Rect block = new Rect(0, 0, width - offset, height);
		canvas.drawRect(block, paint);
		final RectF rectF = new RectF(width - offset * 2, 0, width, height);
		canvas.drawRoundRect(rectF, offset, offset, paint);
	}

	private static void clipTop(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final Rect block = new Rect(0, offset, width, height);
		canvas.drawRect(block, paint);
		final RectF rectF = new RectF(0, 0, width, offset * 2);
		canvas.drawRoundRect(rectF, 0, 0, paint);
	}

	private static void clipBottom(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final Rect block = new Rect(0, 0, width, height - offset);
		canvas.drawRect(block, paint);
		final RectF rectF = new RectF(0, height - offset * 2, width, height);
		canvas.drawRoundRect(rectF, offset, offset, paint);
	}

	private static void clipAll(final Canvas canvas, final Paint paint,
			int offset, int width, int height) {
		final RectF rectF = new RectF(0, 0, width, height);
		canvas.drawRoundRect(rectF, offset, offset, paint);
	}
}