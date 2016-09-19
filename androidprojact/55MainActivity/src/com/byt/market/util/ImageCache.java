package com.byt.market.util;

import java.lang.ref.ReferenceQueue;
import java.util.Hashtable;

import android.graphics.Bitmap;

/**
 * 
 * @author liuxing
 * 软引用缓存
 */
public class ImageCache {
	private Hashtable<String,BitmapRef> ImageRefs;
	private ReferenceQueue<Bitmap> q;// 垃圾Reference的队列
	private static ImageCache _inst;

	public static ImageCache getInstance() {
		if (_inst != null) {
			return _inst;
		} else {
			_inst = new ImageCache();
			return _inst;
		}
	}
// 构建一个缓存器实例
    private ImageCache() {
    	ImageRefs = new Hashtable<String,BitmapRef>();
        q = new ReferenceQueue<Bitmap>();
   }
 
// 以软引用的方式对一个Employee对象的实例进行引用并保存该引用
    public void cacheImage(String key,Bitmap bm) {
      cleanCache();// 清除垃圾引用
      BitmapRef ref = new BitmapRef(bm, q);
      ref.set_key(key);
      ImageRefs.put(key, ref);
    }
    
    private void cleanCache() {
    	BitmapRef ref = null;
      while ((ref = ( BitmapRef) q.poll()) != null) {
    	  ImageRefs.remove(ref.get_key());
      }
    }
    
 // 清除Cache内的全部内容
    public void clearCache() {
       cleanCache();
       ImageRefs.clear();
       System.gc();
       System.runFinalization();
    }
    
     public Bitmap getBitmap(String key) {
    	Bitmap bm = null;
        if (ImageRefs.containsKey(key)) {
        	BitmapRef ref = (BitmapRef) ImageRefs.get(key);
        	bm = (Bitmap) ref.get();
        }
        return bm;
     }
}
