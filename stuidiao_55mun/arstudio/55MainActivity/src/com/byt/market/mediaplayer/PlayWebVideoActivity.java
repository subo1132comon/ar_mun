package com.byt.market.mediaplayer;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.byt.market.R;

public class PlayWebVideoActivity extends Activity {
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.play_web_video_activity);
		webView = (WebView) findViewById(R.id.web_video);
		String url = getIntent().getStringExtra("videoUrl");
		int w = getResources().getDisplayMetrics().widthPixels;
		int h = getResources().getDisplayMetrics().heightPixels
				- getStatusBarHeight(this);
		WebSettings setting = webView.getSettings();
		setSettings(setting);
		webView.setWebChromeClient(new WebChromeClient());
		webView.setWebViewClient(new WebViewClient());
		webView.loadUrl(url + "&w=" + w + "&h=" + h);
	}

	private void setSettings(WebSettings setting) {
		setting.setJavaScriptEnabled(true);
		setting.setBuiltInZoomControls(true);
		// setting.setDisplayZoomControls(false);
		setting.setSupportZoom(true);

		setting.setDomStorageEnabled(true);
		setting.setDatabaseEnabled(true);
		// 全屏显示
		setting.setLoadWithOverviewMode(true);
		setting.setUseWideViewPort(true);
	}

	public int getStatusBarHeight(Context context) {

		Class<?> c = null;

		Object obj = null;

		Field field = null;

		int x = 0, statusBarHeight = 0;

		try {

			c = Class.forName("com.android.internal.R$dimen");

			obj = c.newInstance();

			field = c.getField("status_bar_height");

			x = Integer.parseInt(field.get(obj).toString());

			statusBarHeight = context.getResources().getDimensionPixelSize(x);

		} catch (Exception e1) {

			e1.printStackTrace();

		}
		return statusBarHeight;

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			webView.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}