package com.byt.market.activity;

import com.byt.market.R;
import com.byt.market.adapter.GuideViewPagerAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;

/**
 * �������ָ������
 * */

public class GuideActivity extends FragmentActivity {

	private ViewPager mViewPager;// ����һ���Լ���viewpager

	// ViewPager �����ǵ�listview���ҲҪһ��������

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		initView();
	}

	private void initView() {
		mViewPager = (ViewPager) findViewById(R.id.MyViewPager);
		GuideViewPagerAdapter myAdapter = new GuideViewPagerAdapter(
				this.getSupportFragmentManager(), GuideActivity.this);
		mViewPager.setAdapter(myAdapter);
	}

	@Override
	protected void onStop() {
		finish();
		super.onStop();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {

		}
		return true;
	}
}
