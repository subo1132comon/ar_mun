package com.byt.market.download;

import com.byt.market.util.LogUtil;

public class DownloadLog {

	public static void info(Class<?> name , String str){
		//LogUtil.i("Log", str);
	}

	public static void debug(Class<?> name , String str){
		//LogUtil.d("Log", str);
	}

	public static void error(Class<?> name , String str){
		//LogUtil.e("appupdate", str);
	}
	
	
	public static void error(Class<?> name , String str,Exception e){
		//LogUtil.e("Log", str + " -- > "+e);
		e.printStackTrace();
	}
}
