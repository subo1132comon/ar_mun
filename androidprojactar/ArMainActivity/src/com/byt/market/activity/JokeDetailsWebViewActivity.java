package com.byt.market.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.byt.ar.R;
import com.byt.market.qiushibaike.JokeDetailsActivity;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;
public class JokeDetailsWebViewActivity extends Activity implements OnClickListener{
	
	int msid = 0;
	private String mtype = null;
	private String shere_titile = null;
	private TextView mRelativelayout;
	private WebView webview;
	private ProgressBar bar;
	private ScrollView mscroview;
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jokewebview);
		webview = (WebView) findViewById(R.id.joke_webView);
		mscroview = (ScrollView) findViewById(R.id.scrollView);
		bar = (ProgressBar)findViewById(R.id.progressBar);

		WebSettings singts = webview.getSettings();
		singts.setUserAgentString("55web"+"; HFWSH /"+Util.channel);
		singts.setJavaScriptEnabled(true);
//		//不使用缓存: 
//		singts.setCacheMode(WebSettings.LOAD_NO_CACHE);
		singts.setUseWideViewPort(false);
		singts.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		singts.setSupportZoom(true);
		singts.setBuiltInZoomControls(true);
		singts.setJavaScriptCanOpenWindowsAutomatically(true);// 支持通过JS打开新窗口
		singts.setLoadsImagesAutomatically(true);// 支持自动加载图片
		
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);// 使滚动条不占位
		webview.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				
				webview.loadUrl("javascript:App.resize(document.body.getBoundingClientRect().height)");  
				bar.setVisibility(View.GONE);
				mRelativelayout.setVisibility(View.VISIBLE);
				//-----------------
				if(url.equals("file:///android_asset/404.html")){
					mRelativelayout.setVisibility(View.GONE);
				}
				super.onPageFinished(view, url);
			}
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
				super.onReceivedError(view, errorCode, description, failingUrl);
				 view.stopLoading();
	                view.clearView();
//	                Message msg = handler.obtainMessage();// 发送通知，加入线程
//	                msg.what = 1;// 通知加载自定义404页面
//	                handler.sendMessage(msg);// 通知发送！
	                webview.stopLoading();  
	                //载入本地assets文件夹下面的错误提示页面404.html 
	                webview.loadUrl("file:///android_asset/404.html");
			}
			@Override
	            public boolean shouldOverrideUrlLoading(WebView view, String url) {
				//mRelativelayout.setVisibility(View.GONE);
				bar.setVisibility(View.VISIBLE);
				mscroview.scrollTo(0, 0);
				webview.loadUrl(url);
	                return true;
	            }
		});
		webview.addJavascriptInterface(this, "App");  
		webview.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// TODO Auto-generated method stub
				super.onProgressChanged(view, newProgress);
				int currtpros = 25;
				currtpros = currtpros+newProgress;
				Message msg = handler.obtainMessage();
				msg.what = currtpros;
				handler.sendMessage(msg);
			}
			
			
		});
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		if(bundle!=null){
			msid=bundle.getInt("msid");
			mtype = bundle.getString("type");
			shere_titile = bundle.getString("title");
			webview.loadUrl(com.byt.market.Constants.NEWS_JOKE_WEBBASE+"/m_"+msid+".html");
			//http://www.jokedeedee.com/m_6261.html
		}
		initView();
	}
	@JavascriptInterface  
    public void resize(final float height) {  
		JokeDetailsWebViewActivity.this.runOnUiThread(new Runnable() {  
            @Override  
            public void run() {  
                //Toast.makeText(getActivity(), height + "", Toast.LENGTH_LONG).show();  
            	webview.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, (int) (height * getResources().getDisplayMetrics().density)));  
            }  
        });  
    }  
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			bar.setProgress(msg.what);
		}
	};
	
	private void initView(){
		ImageView back = (ImageView) findViewById(R.id.titlebar_back_arrow);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(this);
		findViewById(R.id.topline4).setVisibility(View.GONE);
		findViewById(R.id.top_downbutton).setVisibility(View.GONE);
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
		mRelativelayout = (TextView) findViewById(R.id.relalayout_shere);
		mRelativelayout.setOnClickListener(this);
		TextView tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
		if(mtype!=null){
			if("new".equals(mtype)){
				tv_title.setText(getString(R.string.news));
			}else if("joke".equals(mtype)){
				tv_title.setText(getString(R.string.joke));
			}
		}
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.relalayout_shere:
			Intent intent =new Intent();
			Bundle bundle = new Bundle();
			bundle.putInt("sid", msid);
			bundle.putString("title",shere_titile );
			bundle.putString("type","jn" );
			intent.putExtras(bundle);
			intent.setClass(JokeDetailsWebViewActivity.this,ShareActivity.class);		
			JokeDetailsWebViewActivity.this.startActivity(intent);
			break;
		case R.id.titlebar_back_arrow:
			JokeDetailsWebViewActivity.this.finish();
			break;
		}
	}
	
	 @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {

	        // TODO Auto-generated method stub
	        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
	           // webview.goBack(); // goBack()表示返回WebView的上一页面
	        	//this.finish();
	        	webview.goBack();
	            return true;
	        }
	        return super.onKeyDown(keyCode, event);
	    }
//	 /**
//	     * 按键响应，在WebView中查看网页时，按返回键的时候按浏览历史退回,如果不做此项处理则整个WebView返回退出
//	     */
//	    @Override
//	    public boolean onKeyDown(int keyCode, KeyEvent event)
//	    {
//	        // Check if the key event was the Back button and if there's history
//	        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack())
//	        {
//	            // 返回键退回
//	            myWebView.goBack();
//	            return true;
//	        }
//	        // If it wasn't the Back key or there's no web page history, bubble up
//	        // to the default
//	        // system behavior (probably exit the activity)
//	        return super.onKeyDown(keyCode, event);
//	    }
}
