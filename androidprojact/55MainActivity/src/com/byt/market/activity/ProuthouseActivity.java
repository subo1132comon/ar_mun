package com.byt.market.activity;

import com.byt.market.R;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class ProuthouseActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window wind = getWindow();
		wind.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
		
//		//设置黑暗度
//		WindowManager.LayoutParams lp=getWindow().getAttributes();
//		lp.dimAmount=0.0f;
//		wind.setAttributes(lp);
//		
//		//全透明
//		WindowManager.LayoutParams p=getWindow().getAttributes();
//		p.alpha=0.0f;
//		wind.setAttributes(p);
//		wind.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		setContentView(R.layout.activity_prouthoues);
		new Thread(){
			int i = 0;
			public void run() {
				while (i<20) {
					try {
						sleep(1000);
						i++;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ProuthouseActivity.this.finish();
			};
		}.start();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		//Log.d("nnlog", "ProuthouseActivity----onResume");
		StatService.trackCustomEvent(this, "umService1", "");
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
		//Log.d("nnlog", "ProuthouseActivity----onPause");
		//MobclickAgent.onEvent(this, "umService");
		MobclickAgent.onEvent(this, "umService1");
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//Toast.makeText(ProuthouseActivity.this, "被拉起1", Toast.LENGTH_LONG).show();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//Toast.makeText(ProuthouseActivity.this, "被拉起2", Toast.LENGTH_LONG).show();
		
	}
}
