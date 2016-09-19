package com.byt.market.activity;

import com.byt.market.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.service.HomeLoadService;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.TextView;

public class ProtocolActivity extends BaseActivity implements OnClickListener {

	private View mGoBack;
	private View mTitleBarIcon;
	private TextView mTitle;
	private View mSearchBtn;
	private View mRightMenu;

	private WebView protocalWebView;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.protocal_layout);
		initView();
		initData();
		addEvent(); 
		protocalWebView = (WebView) findViewById(R.id.protocol_webview);
		protocalWebView.setBackgroundColor(0);
		protocalWebView.setBackgroundResource(R.color.white);
		protocalWebView.getSettings().setBuiltInZoomControls(false);
		protocalWebView.getSettings().setDefaultTextEncodingName("utf-8");
//		protocalWebView.loadUrl("file:///android_asset/protocol.html");
	}

	@Override
	public void initView() {
		mGoBack = findViewById(R.id.titlebar_back_arrow);
		mTitleBarIcon = findViewById(R.id.titlebar_icon);
		mTitle = (TextView) findViewById(R.id.titlebar_title);
		mSearchBtn = findViewById(R.id.titlebar_search_button);
		mRightMenu = findViewById(R.id.titlebar_applist_button_container);
	}

	@Override
	public void initData() {
		mGoBack.setVisibility(View.VISIBLE);
		mTitleBarIcon.setVisibility(View.GONE);
		mSearchBtn.setVisibility(View.GONE);
		mRightMenu.setVisibility(View.INVISIBLE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		mTitle.setText(R.string.titlebar_title_protocol);
	}

	@Override
	public void addEvent() {
		// TODO Auto-generated method stub
		mGoBack.setOnClickListener(this);
		mTitle.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.titlebar_back_arrow:
		case R.id.titlebar_title:
			finishWindow();
			break;
		}
	}

}
