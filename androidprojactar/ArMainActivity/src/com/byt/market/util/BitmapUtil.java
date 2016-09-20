package com.byt.market.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.byt.ar.R;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore.Images;

/**
 * Bitmap图片帮助类
 * 
 * @author qiuximing
 * 
 */
public class BitmapUtil {

	/**
	 * BITMAP转字节
	 * 
	 * @param bitm
	 *            BITMAP对象
	 * @return 返回bitmap对象的字节数组
	 */
	public static byte[] Bitmap2Bytes(Bitmap bitm) {
		if (bitm == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			bitm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		} catch (Exception e) {
			return null;
		}
		return baos.toByteArray();
	}

	/**
	 * 字节转BITMAP
	 * 
	 * @param b
	 *            字节数组
	 * @return 返回字节数组对应的bitmap对象
	 */
	public static Bitmap Bytes2Bimap(byte[] b) {
		if (b == null)
			return null;
		if (b.length != 0) {
//			 BitmapFactory.Options options = new BitmapFactory.Options(); 
//			 options.inJustDecodeBounds = true; 
//			 BitmapFactory.decodeByteArray(b, 0, b.length,options);
//			 options.inJustDecodeBounds = false;  
//			 int be = (int)(options.outHeight / (float)200); 
//	         if (be <= 0)   be = 1; 
//	         options.inSampleSize = be;     BitmapFactory.Options options = new BitmapFactory.Options(); 
//			 options.inJustDecodeBounds = true; 
//			 BitmapFactory.decodeByteArray(b, 0, b.length,options);
//			 options.inJustDecodeBounds = false;  
//			 int be = (int)(options.outHeight / (float)200); 
//	         if (be <= 0)   be = 1; 
//	         options.inSampleSize = be;    
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	/**
	 * 缩放图片，按比例缩放
	 * 
	 * @param bm
	 *            被缩放图片
	 * @param newWidth
	 *            缩放后图片的宽度
	 * @return 缩放后的图片
	 */
	public static Bitmap zoomImg(Bitmap bm, int newWidth) {
		// 获得图片的宽高
		int width = bm.getWidth();
		int height = bm.getHeight();
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / width;
		// float scaleHeight = ((float) newHeight) / height;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleWidth);
		// 得到新的图片
		if (bm.isRecycled())
			return null;
		Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
				true);
		// bm.recycle();
		return newbm;
	}

	/**
	 * Drawable转Bitmap
	 * 
	 * @param drawable
	 *            drawable对象
	 * @return Bitmap对象
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * 转换成圆形图片
	 * 
	 * @param bitmap
	 *            矩形图片
	 * @return 圆形图片
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_4444);
		Canvas canvas = new Canvas(outBitmap);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, outBitmap.getWidth(),
				outBitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPX = outBitmap.getWidth() / 2;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		// bitmap.recycle();
		return outBitmap;
	}

	/**
	 * 保存图片，并写入ContentProvider
	 * 
	 * @param cr
	 *            ContentResolver
	 * @param title
	 *            文件标题
	 * @param dateTaken
	 *            时间
	 * @param location
	 *            位置信息
	 * @param directory
	 *            文件保存目录
	 * @param filename
	 *            文件名
	 * @param source
	 *            图片资源
	 * @return 返回保存的文件位置Uri
	 */
	public static Uri addImage(ContentResolver cr, String title,
			long dateTaken, String directory, String filename, Bitmap source) {
		int degree = 0;
		// We should store image data earlier than insert it to ContentProvider,
		// otherwise we may not be able to generate thumbnail in time.
		OutputStream outputStream = null;
		String filePath = directory + "/" + filename;
		try {
			File dir = new File(directory);
			if (!dir.exists())
				dir.mkdirs();
			File file = new File(directory, filename);

			outputStream = new FileOutputStream(file);
			if (source != null) {
				source.compress(CompressFormat.JPEG, 75, outputStream);
			}
		} catch (FileNotFoundException ex) {
			LogUtil.w("TAG", "FileNotFoundException");
			return null;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Read back the compressed file size.
		long size = new File(directory, filename).length();

		ContentValues values = new ContentValues(9);
		values.put(Images.Media.TITLE, title); // IMAG0001
		values.put(Images.Media.DISPLAY_NAME, filename); // IMAG0001.jpg
		values.put(Images.Media.DATE_TAKEN, dateTaken); // 1340868282000
		values.put(Images.Media.MIME_TYPE, "image/jpeg");// image/jpeg
		values.put(Images.Media.ORIENTATION, degree); // 0
		values.put(Images.Media.DATA, filePath); // mnt/sdcard/dcim/100MEDIA/IMAG0001.jpg
		values.put(Images.Media.SIZE, size); // 123534

		// if (location != null) {
		// values.put(Images.Media.LATITUDE, location.getLatitude());
		// values.put(Images.Media.LONGITUDE, location.getLongitude());
		// }

		return cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
	}
	public static Bitmap createTextImage(Context context,int resIconId,int resStringId) {
		Bitmap originalImage=BitmapFactory.decodeResource(context.getResources(), resIconId);
		int h= originalImage.getHeight();
		Bitmap bitmapWithReflection = Bitmap.createBitmap(
				originalImage.getWidth(), h,
				Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(originalImage, 0, 0, null);
		Paint paint = new Paint();
		paint.setTextSize(SystemUtil.dip2px(context, 12));
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		drawText(canvas, context.getResources().getString(resStringId), SystemUtil.dip2px(context, 10), 2*h/3, paint, -50);
		return bitmapWithReflection;
	}

	public static void drawText(Canvas canvas, String text, float x, float y, Paint paint,
			float angle) {
		if (angle != 0) {
			canvas.rotate(angle, x, y);
		}
		canvas.drawText(text, x, y, paint);
		if (angle != 0) {
			canvas.rotate(-angle, x, y);
		}
	}
	public static Bitmap createNewTextImage(Context context,int resIconId,int resStringId) {
		Bitmap originalImage=BitmapFactory.decodeResource(context.getResources(), resIconId);
		int h= originalImage.getHeight();
		int w= originalImage.getWidth();
		Bitmap bitmapWithReflection = Bitmap.createBitmap(
				w, h,
				Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(originalImage, 0, 0, null);
		Paint paint = new Paint();
		paint.setTextSize(12);;
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		/*
		 *  ------------The Bestone modifed start--------------
		 *   Modified by "zengxiao"  Date:20140531
		 *   Modified for:修改文字显示
		 */
		
		String showtext=context.getResources().getString(resStringId);
		if(showtext.length()>4)
		{
		canvas.drawText(showtext, 0, 2*h/3, paint);
		}
		else if(showtext.length()==3){

			int size1=(int) context.getResources().getDimension(R.dimen.textsize_hot1);
			
			paint.setTextSize(size1);
			canvas.drawText(showtext, 0, 2*h/3, paint);
		}
		else if(showtext.length()==4){
			int size2=(int) context.getResources().getDimension(R.dimen.textsize_hot2);
			paint.setTextSize(size2);
			canvas.drawText(showtext, 0, 2*h/3, paint);
		}
		else if(showtext.length()==2){
			int size3=(int) context.getResources().getDimension(R.dimen.textsize_hot3);
			
			paint.setTextSize(size3);
			canvas.drawText(showtext, w/4, 2*h/3, paint);
		}
		else if(showtext.length()==1){
			int size4=(int) context.getResources().getDimension(R.dimen.textsize_hot4);
			
			paint.setTextSize(size4);
			canvas.drawText(showtext, w/6, 2*h/3, paint);
		}
		
/*
------------The Bestone modifed end--------------
*/
		return bitmapWithReflection;
	}
}
