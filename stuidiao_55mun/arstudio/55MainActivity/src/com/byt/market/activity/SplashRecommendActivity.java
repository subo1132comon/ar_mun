package com.byt.market.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.ui.HomeFragment;
import com.byt.market.ui.SplashRecommendFragment;
import com.byt.market.util.SharedPrefManager;

public class SplashRecommendActivity extends BaseActivity implements OnClickListener{
	
	public static final String SHARE_START_REC_TIME = "start_rec_time";
	
	public SharedPreferences spinfo;
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.list_startrec_frame);
		SharedPrefManager.setRecStartTime(this, System.currentTimeMillis());
		initView();
		initData();
		addEvent();
	}

	@Override
	public void initView() {
		SplashRecommendFragment fragment = new SplashRecommendFragment();
//		HomeFragment fragment = new HomeFragment();
		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.list_info_contentframe, fragment);
		transaction.commit();
		spinfo = getSharedPreferences("info", Context.MODE_PRIVATE);
		spinfo.edit().putLong(SHARE_START_REC_TIME, System.currentTimeMillis()).commit();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
		boolean flag = false;
		// 后退按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finishWindow();
		}
		return flag;
	}
	
	@Override
	public void initData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addEvent() {
		// TODO Auto-generated method stub
		findViewById(R.id.btn_finish).setOnClickListener(this);
		//findViewById(R.id.titlebar_search_button).setOnClickListener(this);
	}
	
	/**
	 * 点击事件处理
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_finish) {
			finish();
		} 
	}

}
