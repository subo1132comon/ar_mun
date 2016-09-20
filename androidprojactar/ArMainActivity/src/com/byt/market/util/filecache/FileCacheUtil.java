package com.byt.market.util.filecache;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.byt.market.MyApplication;
import com.byt.market.activity.MainActivity;
import com.byt.market.tools.MD5Tools;

import android.R.bool;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * ���湤����
 * 
 * @author naibo-liao
 * @ʱ�䣺 2013-1-4����02:30:52
 */
public class FileCacheUtil {
	// private static final String TAG=ConfigCacheUtil.class.getName();

	/** 1�볬ʱʱ�� */
	// public static final int CONFIG_CACHE_SHORT_TIMEOUT=1000 * 60 * 5; // 5
	// ����

	/** 5���ӳ�ʱʱ�� */
	// public static final int CONFIG_CACHE_MEDIUM_TIMEOUT=1000 * 3600 * 2; //
	// 2Сʱ

	/** �г�����ʱ�� */
	 public static final long CONFIG_CACHE_ML_TIMEOUT=1000 * 60 * 60 * 24 * 1;
	 public static final long HOME_CACHE_ML_TIMEOUT=1000 * 60 * 30;
	// // 1��
	//public static final long CONFIG_CACHE_ML_TIMEOUT = 1000 * 60 * 60 * 2; // 1��

	// public static final long CONFIG_CACHE_ML_TIMEOUT=1000 * 60 * 5; // 1��

	/** ��󻺴�ʱ�� */
	// public static final int CONFIG_CACHE_MAX_TIMEOUT=1000 * 60 * 60 * 24 * 7;
	// // 7��

	/**
	 * CONFIG_CACHE_MODEL_LONG : ��ʱ��(7��)����ģʽ <br>
	 * CONFIG_CACHE_MODEL_ML : �г�ʱ��(12Сʱ)����ģʽ<br>
	 * CONFIG_CACHE_MODEL_MEDIUM: �е�ʱ��(2Сʱ)����ģʽ <br>
	 * CONFIG_CACHE_MODEL_SHORT : ��ʱ��(5����)����ģʽ
	 */
	// public enum ConfigCacheModel {
	// CONFIG_CACHE_MODEL_SHORT, CONFIG_CACHE_MODEL_MEDIUM,
	// CONFIG_CACHE_MODEL_ML, CONFIG_CACHE_MODEL_LONG;
	// }

	 /**
	  * 头像缓存
	  * bobo
	  */
	 public static String getUsercancel(String md5url,Context context){
		 String result = null;
		 if (md5url == null) {
				return result;
			}
		 FileUtils utils = new FileUtils(context);
			File myfile = utils.getFiele(md5url);
			if (myfile.exists()) {
					try {
						result = utils.readTextFile(myfile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			}
		return result;
		 
	 }
	/**
	 * ��ȡ����
	 * 
	 * @param md5url
	 *            ���������URL
	 * @return ��������
	 */
	public static String getUrlCache(Context context, String md5url,String url,
			long currenTime) {
		if (md5url == null) {
			return null;
		}
		String result = null;

		FileUtils utils = new FileUtils(context);
		File myfile = utils.getFiele(md5url);
		if (myfile.exists()) {
//			if("move".equals(getType(url))){
//				long l = currenTime - myfile.lastModified();
//				// long v = 1000 * 60 * 7;
//				if (l > CONFIG_CACHE_ML_TIMEOUT) {
//					utils.deleteFile(myfile);
//					return null;
//					// get net
//				} else {
//					try {
//						return utils.readTextFile(myfile);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
			if(MainActivity.HOME_KEY.equals(getType(url))){
				long l = currenTime - myfile.lastModified();
				// long v = 1000 * 60 * 7;
				if (l > HOME_CACHE_ML_TIMEOUT) {
					utils.deleteFile(myfile);
					return null;
					// get net
				} else {
					try {
						return utils.readTextFile(myfile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			try {
				result = utils.readTextFile(myfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return result;
	}

	
	//如果 之前没有缓存 肯定会有 红点 所以要取消
	public static void canleRpit(String url){
		String typ = getType(url);
		if(typ!=null&&!typ.equals("")){
			if(typ.equals(MainActivity.JOKE_KEY)){
				 setIsshow(MainActivity.JOKE_KEY);
		}else if(typ.equals(MainActivity.MUSIC_KEY)){
				 setIsshow(MainActivity.MUSIC_KEY);
		 } else if(typ.equals(MainActivity.TV_KEY)){
				 setIsshow(MainActivity.TV_KEY);
		 } else if(typ.equals(MainActivity.NOVEL_KEY)){
				 setIsshow(MainActivity.NOVEL_KEY);
		 }else if(typ.equals(MainActivity.RIDIAO_KEY)){
				 setIsshow(MainActivity.RIDIAO_KEY);
		 }else if(typ.equals(MainActivity.ANIME_KEY)){
				 setIsshow(MainActivity.ANIME_KEY);
		 }else if(typ.equals(MainActivity.NEWS_KEY)){
			 setIsshow(MainActivity.NEWS_KEY);
		 }else if(typ.equals(MainActivity.SHOW_KEY)){
			 setIsshow(MainActivity.SHOW_KEY);
		 }else if(MainActivity.MV_KEY.equals(typ)){
			 setIsshow(MainActivity.MV_KEY);
		 }else if(MainActivity.AV_KEY.equals(typ)){
			 setIsshow(MainActivity.AV_KEY);
		 }else if(MainActivity.MVOE_KEY.equals(typ)){
			 setIsshow(MainActivity.MVOE_KEY);
		 }
		}
	}
	
	public static boolean isgetForcache(Context context,String url){
	  boolean isforcache = false;
	  SharedPreferences txtNetResVer = MyApplication.getInstance().getSharedPreferences("yulever", 0);
	 String typ = getType(url);
	 if(typ.equals(MainActivity.JOKE_KEY)){
		 if(txtNetResVer.getInt(MainActivity.JOKE_KEY, 0)>=1){
			 isforcache = true;
			 setIsshow(MainActivity.JOKE_KEY);
		 }
	}else if(typ.equals(MainActivity.MUSIC_KEY)){
		 if(txtNetResVer.getInt(MainActivity.MUSIC_KEY, 0)>=1){
			 isforcache = true;
			 setIsshow(MainActivity.MUSIC_KEY);
		 }
	 } else if(typ.equals(MainActivity.TV_KEY)){
		 if(txtNetResVer.getInt(MainActivity.TV_KEY, 0)>=1){
			 isforcache = true;
			 setIsshow(MainActivity.TV_KEY);
		 }
	 } else if(typ.equals(MainActivity.NOVEL_KEY)){
		 if(txtNetResVer.getInt(MainActivity.NOVEL_KEY, 0)>=1){
			 isforcache = true;
			 setIsshow(MainActivity.NOVEL_KEY);
		 }
	 }else if(typ.equals(MainActivity.RIDIAO_KEY)){
		 if(txtNetResVer.getInt(MainActivity.RIDIAO_KEY, 0)>=1){
			 isforcache = true;
			 setIsshow(MainActivity.RIDIAO_KEY);
		 }
	 }else if(typ.equals(MainActivity.ANIME_KEY)){
		 if(txtNetResVer.getInt(MainActivity.ANIME_KEY, 0)>=1){
			 isforcache = true;
			 setIsshow(MainActivity.ANIME_KEY);
		 }
	 }else if(typ.equals(MainActivity.NEWS_KEY)){
		 if(txtNetResVer.getInt(MainActivity.NEWS_KEY, 0)>=1){
			 isforcache = true;
			 setIsshow(MainActivity.NEWS_KEY);
		 }
	 }else if(typ.equals(MainActivity.NEWS_KEY2)){
		 if(txtNetResVer.getInt(MainActivity.NEWS_KEY, 0)>=1){
			 isforcache = true;
		 }
	 }else if(typ.equals(MainActivity.JOKE_KEY2)){
		 if(txtNetResVer.getInt(MainActivity.JOKE_KEY, 0)>=1){
			 isforcache = true;
		 }
	 }else if(typ.equals(MainActivity.SHOW_KEY)){
		 if(txtNetResVer.getInt(MainActivity.SHOW_KEY, 0)>=1){
			 isforcache = true;
		 }
		 
	 }else if(MainActivity.MV_KEY.equals(typ)){
		 if(txtNetResVer.getInt(MainActivity.MV_KEY, 0)>=1){
			 isforcache = true;
		 }
	 }else if(MainActivity.MVOE_KEY.equals(typ)){
		 if(txtNetResVer.getInt(MainActivity.MVOE_KEY, 0)>=1){
			 isforcache = true;
		 }
	 }else if(MainActivity.AV_KEY.equals(typ)){
		 if(txtNetResVer.getInt(MainActivity.AV_KEY, 0)>=1){
			 isforcache = true;
		 }
	 }
	return isforcache;
	 
  }
	//取消 红点
	public static void setIsshow(String skey){
		SharedPreferences txtNetResVer = MyApplication.getInstance().getSharedPreferences("yulever", 0);
		//int net_ver3 = txtNetResVer.getInt(skey, 0);
		//SharedPreferences txtResVer = MyApplication.getInstance().getSharedPreferences(tkey, 0);
		Log.d("nnlog", "取消红点--"+skey);
		SharedPreferences.Editor editor3 = txtNetResVer.edit();
		editor3.putInt(skey, 0);
		editor3.commit();
	}
	public static String getType(String res) {
		if (res.contains("stype")) {
			int i = res.indexOf("stype");
			String rr = res.substring(i + 6);
			return rr;
		}
		return "";
	}

	public static boolean deleTrapitfile(Context context, String url,
			String type) {
		Log.d("mylog", "type--" + type + "------ut-" + getType(url));
		if (type.equals(getType(url))) {
			String murl = MD5Tools.MD5(url);
			FileUtils utils = new FileUtils(context);
			File myfile = utils.getFiele(murl);
			if (myfile.exists()) {
				utils.deleteFile(myfile);
				Log.d("mylog", "删除--" + myfile.getName());
				return true;
			}
		}
		return false;
	}

	/**
	 * ���û���
	 * 
	 * @param data
	 * @param url
	 */
	public static void setUrlCache(Context context, String data, String url) {
		try {
			FileUtils utils = new FileUtils(context);
			File myfile = utils.getFiele(url);
			if (myfile.exists()) {
				utils.deleteFile(myfile);
			}
			utils.writeTextFile(myfile, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * ɾ����ʷ�����ļ�
	 * 
	 * @param cacheFile
	 */
	// public static void clearCache(File cacheFile) {
	// if(cacheFile == null) {
	// if(Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
	// {
	// try {
	// File cacheDir=new
	// File(Environment.getExternalStorageDirectory().getPath() +
	// "/hulutan/cache/");
	// if(cacheDir.exists()) {
	// clearCache(cacheDir);
	// }
	// } catch(Exception e) {
	// e.printStackTrace();
	// }
	// }
	// } else if(cacheFile.isFile()) {
	// cacheFile.delete();
	// } else if(cacheFile.isDirectory()) {
	// File[] childFiles=cacheFile.listFiles();
	// for(int i=0; i < childFiles.length; i++) {
	// clearCache(childFiles[i]);
	// }
	// }
	// }
}