package com.byt.market.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.byt.market.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.util.SystemUtil;
import com.byt.market.util.Util;

public class AboutActivity extends BaseActivity implements OnClickListener {

	private View mGoBack;
	private View mTitleBarIcon;
	private TextView mTitle;
	private View mSearchBtn;
	private View mRightMenu;
	private TextView mVersion;
	private TextView mVersionData;
	private ImageView img_sina_webor;
	private ImageView img_tencent_webor;
	private RelativeLayout	lv_weibo;
	private TextView	tvWeiboTitle;
	private TextView app_ver_info;
	private TextView updated_time;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.jinli_about_xml);
		initView();
		initData();
		addEvent();
		// Util.killApps(this);
	}

	@SuppressLint("NewApi")
	@Override
	public void initView() {
		mGoBack = findViewById(R.id.titlebar_back_arrow);
		mTitleBarIcon = findViewById(R.id.titlebar_icon);
		mTitle = (TextView) findViewById(R.id.titlebar_title);
		mSearchBtn = findViewById(R.id.titlebar_search_button);
		mRightMenu = findViewById(R.id.titlebar_applist_button_container);
		img_sina_webor = (ImageView) findViewById(R.id.img_sina_webor);
		img_tencent_webor = (ImageView) findViewById(R.id.img_tencent_webor);
		mVersion = (TextView) findViewById(R.id.tv_app_version);
		mVersionData = (TextView) findViewById(R.id.app_version_data);
		lv_weibo = (RelativeLayout) findViewById(R.id.lv_weibo);
		tvWeiboTitle = (TextView) findViewById(R.id.tv_weibo_title);
		app_ver_info = (TextView) findViewById(R.id.app_ver_info);
		updated_time = (TextView) findViewById(R.id.updated_time);
		
		if(!TextUtils.isEmpty(Util.getChannelName(this))){
			if("obx_03".equals(Util.getChannelName(this))){
				lv_weibo.setVisibility(View.VISIBLE);
				tvWeiboTitle.setVisibility(View.VISIBLE);
			}else{
				lv_weibo.setVisibility(View.GONE);
				tvWeiboTitle.setVisibility(View.GONE);
			}
		}else{
			lv_weibo.setVisibility(View.GONE);
			tvWeiboTitle.setVisibility(View.GONE);
		}
		PackageInfo info;
		try {
			info = getPackageManager().getPackageInfo( getPackageName(), 0);
			String vName = info.versionName;
			long mLastUpdateTime = info.lastUpdateTime;
			
			mVersion.setText(getString(R.string.about_down_summary));
			mVersionData.setText(vName);	
			
			app_ver_info.setText(getString(R.string.app_name) +" "+ vName);
			updated_time.setText(getString(R.string.updated_time) + Util.getStrTime(mLastUpdateTime)); 
			
		} catch (NameNotFoundException e) {
			mVersion.setVisibility(View.GONE);
			app_ver_info.setText(getString(R.string.app_name));
			updated_time.setVisibility(View.GONE);
			e.printStackTrace();
		}
	}

	@Override
	public void initData() {
		mGoBack.setVisibility(View.VISIBLE);
		mTitleBarIcon.setVisibility(View.GONE);
		mSearchBtn.setVisibility(View.GONE);
		mRightMenu.setVisibility(View.INVISIBLE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		mTitle.setText(R.string.titlebar_title_about);
		// try {
		// PackageInfo info = getPackageManager().getPackageInfo(
		// getPackageName(), 0);
		// String vName = info.versionName;
		// mVersion.setText("版本：" + vName);
		// } catch (NameNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	@Override
	public void addEvent() {
		mGoBack.setOnClickListener(this);
		mTitle.setOnClickListener(this);
		img_sina_webor.setOnClickListener(this);
		img_tencent_webor.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.titlebar_back_arrow:
		case R.id.titlebar_title:
			finishWindow();
			break;
		case R.id.img_sina_webor:
			startBrowser("http://weibo.com/opsson?s=6cm7D0");
			break;
		case R.id.img_tencent_webor:
			startBrowser("http://t.qq.com/opssonmobile");
			break;
		}
	}
	
	public void startBrowser(String url){
		   Intent intent = new Intent();        
	        intent.setAction("android.intent.action.VIEW");    
	        Uri content_url = Uri.parse(url);   
	        intent.setData(content_url);  
	        startActivity(intent);
	}
}
