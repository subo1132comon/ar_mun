package com.byt.market.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.umeng.analytics.MobclickAgent;

public class UMstatisticsUtls {
	
	public static void umCunt(Context context,String type){
		SharedPreferences rapit_p = MyApplication.getInstance()
				.getSharedPreferences(Constants.UMSTAT_KEY, 0);
		SharedPreferences.Editor editor = rapit_p.edit();
		
		long startime = rapit_p.getLong("startime", 0);
		long currentime = System.currentTimeMillis();
		int time;
		
		switch (rapit_p.getInt("bttpye", 0)) {
		case 0:
			break;
		case 1:
			time = (int) (currentime-startime);
			MobclickAgent.onEventValue(context, "Home", null,time/1000);
			Log.d("nnlog", "统计Home");
			break;
		case 2:
			time = (int) (currentime-startime);
			MobclickAgent.onEventValue(context, "Funb", null,time/1000);
			Log.d("nnlog", "统计Funb");
			break;
		case 3:
			time = (int) (currentime-startime);
			MobclickAgent.onEventValue(context, "Newsb", null,time/1000);
			Log.d("nnlog", "统计Newsb");
			break;
		case 4:
			time = (int) (currentime-startime);
			MobclickAgent.onEventValue(context, "AVb", null,time/1000);
			Log.d("nnlog", "统计AVb");
			break;
		case 5:
			time = (int) (currentime-startime);
			MobclickAgent.onEventValue(context, "Mine", null,time/1000);
			Log.d("nnlog", "统计Mine");
			break;
		}
		if("home".equals(type)){
			editor.putInt("bttpye", 1);
		}else if("fun".equals(type)){
			editor.putInt("bttpye", 2);
		}else if("news".equals(type)){
			editor.putInt("bttpye", 3);
		}else if("av".equals(type)){
			editor.putInt("bttpye", 4);
		}else if("mine".equals(type)){
			editor.putInt("bttpye", 5);
		}
		editor.putLong("startime", currentime);
		editor.commit();
	}
}
