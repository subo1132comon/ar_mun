package com.byt.market.bitmaputil.core.display;

import com.byt.market.MyApplication;
import com.byt.market.util.BitmapUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Just displays {@link Bitmap} in {@link ImageView}
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public final class ZoomBitmapDisplayer implements BitmapDisplayer {
	private int zoomScale;
	public ZoomBitmapDisplayer(int zoomScale) {
		this.zoomScale = zoomScale;
	}
	@Override
	public Bitmap display(Bitmap bitmap, ImageView imageView) {
		Bitmap mBitmap = zoomImg(bitmap,zoomScale);
		imageView.setImageBitmap(mBitmap);
		return bitmap;
	}

	private Bitmap zoomImg(Bitmap bitmap, int newWidth) {
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
//		bitmap.recycle();
		return newbm;
	}
}