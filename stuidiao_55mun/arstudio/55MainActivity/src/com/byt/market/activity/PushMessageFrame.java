package com.byt.market.activity;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.byt.market.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.data.PUSH;
import com.byt.market.data.PushInfo;
import com.byt.market.util.Util;

public class PushMessageFrame extends BaseActivity{
	public static PushMessageFrame group;
	public PushInfo pinfo;
	private WebView wv;
	private View pushload;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_push);
		initView();
		initData();

	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (pushload.getVisibility() == View.VISIBLE) {
				pushload.setVisibility(View.GONE);
			}
		};
	};

	private void loadPage() {
		new Thread() {
			public void run() {
				wv.loadUrl(pinfo.pvalue);
			}
		}.start();

	}

	public void init() {
		PUSH push = new PUSH();
		push.id = pinfo.id;
		push.type = 3;
		push.state = 1;
		Util.addData(maContext, push);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setScrollBarStyle(0);
		wv.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				setTitle(getString(R.string.lodingspeed)+ progress + "%");
				setProgress(progress * 100);
				if (progress == 100) {
					handler.sendEmptyMessage(1);
					setTitle(R.string.app_name);
				}
			}
		});

		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, android.net.http.SslError error) {
				handler.proceed();
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
			}
		});
	}

	public boolean onKeyDown(int keyCoder, KeyEvent event) {
		if (wv.canGoBack() && keyCoder == KeyEvent.KEYCODE_BACK) {
			wv.goBack();
			return true;
		}
		return super.onKeyDown(keyCoder, event);
	}

	@Override
	public void initView() {
		wv = (WebView) findViewById(R.id.wv);
		pushload = findViewById(R.id.pushload);
		group = this;
	}

	@Override
	public void initData() {
		Intent intent = getIntent();
		if (intent != null)
			pinfo = (PushInfo) intent.getParcelableExtra("pinfo");
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
				.cancel(R.drawable.icon);
		if (pinfo != null) {
			init();
			loadPage();
		}
	}

	@Override
	public void addEvent() {
		// TODO Auto-generated method stub
		
	}
}
