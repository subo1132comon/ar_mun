package com.byt.market.bitmaputil.core;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.bitmaputil.core.assist.ImageLoadingListener;
import com.byt.market.bitmaputil.core.display.BitmapDisplayer;
import com.byt.market.bitmaputil.utils.L;
import com.byt.market.view.gifview.GifDecoderView;

/**
 * Displays bitmap in {@link ImageView}. Must be called on UI thread.
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see ImageLoadingListener
 * @see BitmapDisplayer
 */
final class DisplayBitmapTask implements Runnable {

	private static final String LOG_DISPLAY_IMAGE_IN_IMAGEVIEW = "Display image in ImageView [%s]";
	private static final String LOG_TASK_CANCELLED = "ImageView is reused for another image. Task is cancelled. [%s]";

	private final Bitmap bitmap;
	private final ImageView imageView;
	private final String memoryCacheKey;
	private final BitmapDisplayer bitmapDisplayer;
	private final ImageLoadingListener listener;

	private boolean loggingEnabled;

	public DisplayBitmapTask(Bitmap bitmap, ImageLoadingInfo imageLoadingInfo) {
		this.bitmap = bitmap;
		imageView = imageLoadingInfo.imageView;
		memoryCacheKey = imageLoadingInfo.memoryCacheKey;
		bitmapDisplayer = imageLoadingInfo.options.getDisplayer();
		listener = imageLoadingInfo.listener;
	}

	public void run() {
		try{
		if (isViewWasReused()) {
			if (loggingEnabled) L.i(LOG_TASK_CANCELLED, memoryCacheKey);
			listener.onLoadingCancelled();
		} else {
			if (loggingEnabled) L.i(LOG_DISPLAY_IMAGE_IN_IMAGEVIEW, memoryCacheKey); 
			if(bitmap==null){
				try{
					CharSequence type=imageView.getContentDescription();
					if(type!=null)
					{
						int x=Integer.parseInt(type.toString());
						switch(x){
						case 1://商店默认图
							imageView.setImageResource(R.drawable.app_empty_icon);
							break;
						case 2://音乐默认图
							imageView.setImageResource(R.drawable.default_disc_360);
							break;
						case 3://视频默认图
							imageView.setImageResource(R.drawable.videodefaultbg);
							break;
						}
					}
				}catch(Exception e){}

			}else {	
				CharSequence type=imageView.getContentDescription();
				if(type!=null)
				{
					int x=Integer.parseInt(type.toString());
					if(x==5)
					{
						int display=MyApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.nomoldip);
						ViewGroup.LayoutParams lp = imageView.getLayoutParams();
						float widthtmp=MyApplication.mScreenWidth-40-(display*2);
						float widthbm=bitmap.getWidth();
						float heightbm=bitmap.getHeight();
						 
						lp.height =(int) ((widthtmp/widthbm)*bitmap.getHeight());
						lp.width = MyApplication.mScreenWidth-40-(display*2);
						imageView.setLayoutParams(lp);
					}else if(x==4){
						int display=MyApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.nomlistdip);
						ViewGroup.LayoutParams lp = imageView.getLayoutParams();
						float widthtmp=MyApplication.mScreenWidth-40-(display*2);
						float widthbm=bitmap.getWidth();
						float heightbm=bitmap.getHeight();
						 
						lp.height =(int) ((widthtmp/widthbm)*bitmap.getHeight());
						lp.width = MyApplication.mScreenWidth-40-(display*2);
						imageView.setLayoutParams(lp);
					}
				}
				bitmapDisplayer.display(bitmap, imageView);
			}
			if(imageView.getTag(imageView.getId())!=null){
				listener.onLoadingComplete(bitmap);
			}
			ImageLoader.getInstance().cancelDisplayTask(imageView);
		}
		}catch(Exception e){
			
		}
	}

	/** Checks whether memory cache key (image URI) for current ImageView is actual */
	private boolean isViewWasReused() {
		String currentCacheKey = ImageLoader.getInstance().getLoadingUriForView(imageView);
		return !memoryCacheKey.equals(currentCacheKey);
	}

	void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}
}
