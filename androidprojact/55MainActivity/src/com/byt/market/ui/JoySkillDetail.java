package com.byt.market.ui;


import com.byt.market.R;
import com.byt.market.ui.base.BaseUIFragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class JoySkillDetail extends BaseUIFragment {

	public static final String EXTRA_URL = "extra_url";
	String mUrl;
	WebView mWebView;
	RelativeLayout mRoot;
	ProgressBar mProgressBar;
	public void setUrl(String url){
		mUrl = url;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//mUrl = this.getActivity().getIntent().getStringExtra(EXTRA_URL);
		mRoot = new RelativeLayout(this.getActivity());
		mWebView = new WebView(this.getActivity());
		mProgressBar = new ProgressBar(this.getActivity());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mRoot.addView(mProgressBar, params);
		
		mProgressBar.setId(0x10000);
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.BELOW, 0x10000);
		mRoot.addView(mWebView, params);

		mWebView.setWebViewClient(new MyWebViewClient());
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDefaultTextEncodingName("UTF-8");
		mWebView.loadUrl(mUrl);
		return mRoot;
	}

	private class MyWebViewClient extends WebViewClient 
	{ 
	   @Override 
	   public boolean shouldOverrideUrlLoading(WebView view, String url) 
	   { 
	      view.loadUrl(url); 
	      return true; 
	   }

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			mProgressBar.setVisibility(View.GONE);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			mProgressBar.setVisibility(View.VISIBLE);
		} 
		   
		
		   
	}
	
	public boolean goBack(){
		if(mWebView.canGoBack()){
			mWebView.goBack();
			return true;
		} else {
			return false;
		}
		
	}
	
	
}
