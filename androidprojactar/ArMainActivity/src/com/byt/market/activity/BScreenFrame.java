package com.byt.market.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.Constants;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.view.BScreenshotBand;

/**
 * 主页面，控制其他五个页面
 * 
 * @author Administrator
 * 
 */
public class BScreenFrame extends BaseActivity implements OnClickListener {

	private String from;
	public static BScreenFrame paFrame;
	private BScreenshotBand band;
	private int screen;
	
	protected DisplayImageOptions options;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bdetail_frame);
		initView();
		initData();
		addEvent();
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		MyApplication.density = outMetrics.density;
		MyApplication.mScreenHeight = outMetrics.heightPixels;
		MyApplication.mScreenWidth = outMetrics.widthPixels;
		options = new DisplayImageOptions.Builder()
		.cacheOnDisc().delayBeforeLoading(200).build();
	}

	@Override
	public void initView() {
		paFrame = this;
		from = getIntent().getStringExtra(Constants.LIST_FROM);
		String imagePath = getIntent().getStringExtra("bimgs");
		screen = getIntent().getIntExtra("screen", 0);
		band = (BScreenshotBand) findViewById(R.id.bsb_band);
		if ( imagePath != null && imagePath.contains(",")) {
			String[] imgs = imagePath.split(",");
			List<String> datas = new ArrayList<String>();
			for (String string : imgs) {
				datas.add(string);
			}
			band.flushAdvertiseBand(imageLoader,options,datas);
			band.setCurrentView(screen);
		}
	}

	@Override
	public void initData() {
	}

	@Override
	public void addEvent() {
		// TODO Auto-generated method stub

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

	/**
	 * 点击事件处理
	 */
	@Override
	public void onClick(View v) {

	}
}
