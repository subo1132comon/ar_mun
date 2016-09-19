package com.byt.market.service;

import com.byt.market.MyApplication;
import com.byt.market.activity.ProuthouseActivity;
import com.byt.market.util.RapitUtile;
import com.byt.market.util.Util;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyumService extends Service{

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("myservice", "onCreate");
		
		MobclickAgent.onEvent(MyApplication.getInstance().getApplicationContext(), "umService");
		StatService.trackCustomEvent(this,"umService");//腾讯
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d("nnlog", "onStartCommand");
		
		if(!Util.isAppOnForeground()){
			
			if((System.currentTimeMillis()-RapitUtile.getEnterApptime())>1000*60*60*12){
				if(MyApplication.getInstance().getMainactivity()!=null){
					MyApplication.getInstance().getMainactivity().finish();
				}
				Intent intents = new Intent(getApplicationContext(), ProuthouseActivity.class);
				intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intents);
		   }
		
	    }
		startForeground(0, new Notification());
		return START_STICKY;
	}

}
