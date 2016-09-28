//package com.byt.market.util.filecache;
//
//import android.graphics.Bitmap;
//import android.util.Log;
//import android.view.View;
//
//import com.nostra13.universalimageloader.core.DisplayImageOptions;
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.assist.FailReason;
//import com.nostra13.universalimageloader.core.assist.ImageSize;
//import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
//
//public  class ImageLoadingUtil {
//	
//	//private DisplayImageOptions options;
//	public static void LoadingImage(String imageUrl,ImageBack b,int[] drawables){
//		//显示图片的配置  
//				DisplayImageOptions  options = new DisplayImageOptions.Builder()  
//			            .cacheInMemory(true)  
//			            .cacheOnDisk(true) 
//			            .showImageOnFail(drawables[0])
//			            .showImageOnLoading(drawables[1])
//			            .bitmapConfig(Bitmap.Config.RGB_565)  
//			            .build();  
//				ImageSize size = new ImageSize(100, 100);
//		//ImageLoader.getInstance().loadImage(imageUrl, options, new MyimageResoultBack(b));  
//				ImageLoader.getInstance().loadImage(imageUrl, options, new ImageLoadingListener() {
//					
//					@Override
//					public void onLoadingStarted(String arg0, View arg1) {
//						// TODO Auto-generated method stub
//						
//					}
//					
//					@Override
//					public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
//						// TODO Auto-generated method stub
//						
//					}
//					
//					@Override
//					public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
//						// TODO Auto-generated method stub
//						//b.getBitMap(arg2);
//						Log.d("logcart", "imimimimimimii");
//					}
//					
//					@Override
//					public void onLoadingCancelled(String arg0, View arg1) {
//						// TODO Auto-generated method stub
//						
//					}
//				});  
//				//ImageLoader.getInstance().l
//	}
//	
//	
// private static class MyimageResoultBack implements ImageLoadingListener{
//	 
//	 private ImageBack back;
//	 public MyimageResoultBack(ImageBack b){
//		 this.back = b;
//	 }
//	@Override
//	public void onLoadingCancelled(String arg0, View arg1) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
//		// TODO Auto-generated method stub
//		back.getBitMap(arg2);
//	}
//
//	@Override
//	public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onLoadingStarted(String arg0, View arg1) {
//		// TODO Auto-generated method stub
//		
//	}
//	  
//	
//  }  
//public interface ImageBack{
//	public void getBitMap(Bitmap bitmap);
//}
// 
//}
