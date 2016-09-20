package com.byt.market.util;

import com.byt.market.MyApplication;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ToastUtile {
	private Context mcontext;
	private String mcontent = "";
	public ToastUtile(Context context){
		this.mcontext = context;
	}
	 
	private Handler mhandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Log.d("nnlog", "cicicici------");
			Toast.makeText(mcontext, mcontent, Toast.LENGTH_LONG).show();
		};
	};
	
	public void show(String content,final int num){
		this.mcontent = content;
		new Thread(){
			public void run() {
				int i=0;
				while (i<num) {
					
					Message msg = mhandler.obtainMessage();
					mhandler.sendMessage(msg);
					try {
						sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					i++;
				}
			};
		}.start();
	}

	
}
