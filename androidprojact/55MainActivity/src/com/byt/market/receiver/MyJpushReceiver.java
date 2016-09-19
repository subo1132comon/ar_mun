package com.byt.market.receiver;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.byt.market.MyApplication;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.ProuthouseActivity;
import com.byt.market.util.NotifalUtile;
import com.byt.market.util.RapitUtile;
import com.byt.market.util.Util;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import cn.jpush.android.api.JPushInterface;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.widget.Toast;

public class MyJpushReceiver extends BroadcastReceiver{
		
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		 Bundle bundle = intent.getExtras();
		 
		if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
			
			 String regId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
			 Log.d("nnlog", "[MyReceiver] 接收到推送下来的自定义"+regId);
        	try {
				JSONObject pjson = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
				//NotifalUtile.showNotifal(context,pjson);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
        }else if(JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())){
        	 String regId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
        	JSONObject pjson;
			try {
				pjson = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
				StatService.trackCustomEvent(context,"Notification");//腾讯
				MobclickAgent.onEvent(context,"Notification");//友盟
				NotifalUtile.formNotifcation(context, pjson);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())){
        	Log.d("nnlog", "通知");
        	if(!Util.isAppOnForeground()){
        		if((System.currentTimeMillis()-RapitUtile.getEnterApptime())>1000*60*60*12){
	        		if(MyApplication.getInstance().getMainactivity()!=null){
	        			MyApplication.getInstance().getMainactivity().finish();
	        		}
	            	Intent intents = new Intent(context, ProuthouseActivity.class);
	    			intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	    			context.startActivity(intents);
        		}
        	}
        }
	}
	

}
