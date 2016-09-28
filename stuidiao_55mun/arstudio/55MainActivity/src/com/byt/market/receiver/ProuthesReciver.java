package com.byt.market.receiver;

import com.byt.market.MyApplication;
import com.byt.market.activity.ProuthouseActivity;
import com.byt.market.util.RapitUtile;
import com.byt.market.util.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ProuthesReciver extends BroadcastReceiver{

	//boolean flog = false;
	@Override
	public void onReceive(Context arg0, Intent arg1) {
	//	if(!flog){
		//	flog = true;
			//arg0.registerReceiver(new FinishBrodcast(MyApplication.getInstance().getMainactivity()), new IntentFilter("com.markt.fnish"));
		if(!Util.isAppOnForeground()){
				if((System.currentTimeMillis()-RapitUtile.getUpdate(RapitUtile.PULL_KEY))>1000*60*60*12){
					if(MyApplication.getInstance().getMainactivity()!=null){
						MyApplication.getInstance().getMainactivity().finish();
					}
						Intent intents = new Intent(arg0, ProuthouseActivity.class);
						intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						arg0.startActivity(intents);
			}
			
		}
	}

}
