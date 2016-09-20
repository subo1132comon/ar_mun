package com.byt.market.activity;

import com.byt.ar.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.ProgressBar;

public class PayWebviewActivity extends Activity{
	
	WebView webView;
	boolean isLoadUrl  = false;
	private ProgressBar bar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_paywenbview);
		String url = getIntent().getStringExtra("url");
		webView = (WebView) findViewById(R.id.webView);
		bar = (ProgressBar)findViewById(R.id.progressBar);
		WebSettings singts = webView.getSettings();
		//singts.setUserAgentString("55web"+"; HFWSH /"+Util.channel);
		singts.setJavaScriptEnabled(true);
		singts.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		singts.setJavaScriptCanOpenWindowsAutomatically(true);// 支持通过JS打开新窗口
		singts.setLoadsImagesAutomatically(true);// 支持自动加载图片
		// 设置可以支持缩放   
		singts.setSupportZoom(true);   
		// 设置出现缩放工具   
		singts.setBuiltInZoomControls(true);  
		//设置可在大视野范围内上下左右拖动，并且可以任意比例缩放  
		singts.setUseWideViewPort(true);  
		//设置默认加载的可视范围是大视野范围  
		singts.setLoadWithOverviewMode(true);  
		//自适应屏幕  
		singts.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);  
		
		//webView.setWebChromeClient(new ChromeClient());//对js交互的对话框、title以及页面加载进度条的管理  
		webView.setWebViewClient(new WebClient());//对webview页面加载管理、如url重定向  
		//webView.addJavascriptInterface(null, "game");  
		webView.clearCache(true);//支持缓存  
		webView.setWebChromeClient(new WebChromeClient(){
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
		if(url==null){
			url = "";
		}
		webView.loadUrl(url);
	}
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			bar.setProgress(msg.what);
		}
	};
	class WebClient extends WebViewClient {  
		  
        
        @Override  
        public void onLoadResource(WebView view, String url) {  
            super.onLoadResource(view, url);  
//            Log.d("nnlog","***********1*********"+url);
//            if("http://www.55mun.com/success.html".equals(url)){
//        		Log.d("nnlog", "银行---成功------1");
//        	}else if("http://www.55mun.com/fail.html".equals(url)){
//        		Log.d("nnlog", "银行---失败---1");
//        	}
        }  
  
        @Override  
        public void onReceivedError(WebView view, int errorCode,  
                String description, String failingUrl) {  
            super.onReceivedError(view, errorCode, description, failingUrl);  
          //  Log.d("nnlog", "cod--"+errorCode+"--description--"+description);
           // 
        }  
  
        @Override  
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//webview页面加载开始时就会执行此方法、一般用作重定向时的初始化工作  
//            Log.d("nnlog","**********3**********"+url);
//            
//        	if("http://www.55mun.com/success.html".equals(url)){
//        		
//        	}else if("http://www.55mun.com/fail.html".equals(url)){
//        		
//        	}
//            if(!isLoadUrl){
//            	isLoadUrl = true;
//            	view.loadUrl(url);
//            }
            super.onPageStarted(view, url, favicon);  
        }  
  
        /** 
         * url重定向会执行此方法以及点击页面某些链接也会执行此方法 
         *  
         * @param view 
         *            当前webview 
         * @param url 
         *            即将重定向的url 
         * @return true:表示当前url已经加载完成，即使url还会重定向都不会再进行加载 false 表示此url默认由系统处理，该重定向还是重定向，直到加载完成 
         */  
        @Override  
        public boolean shouldOverrideUrlLoading(WebView view, String url) {  
  
        	
        	//response_code%22:%22  000  周一改
        	//Intent intent = new Intent("bank.pay.com");
        	if(url.contains("response_code%22:%22")){
        		if(url.contains("response_code%22:%22000")){
//            		intent.putExtra("re", "success");
//            		sendBroadcast(intent);
        			showDailog(getString(R.string.pay_success),"success");
            	}else{
//            		intent.putExtra("re", "fail");
//            		sendBroadcast(intent);
            		showDailog(getString(R.string.pay_fail),"fail");
            	}
        	}
            return super.shouldOverrideUrlLoading(view, url);//（步骤三）  
        }  
  
        @Override  
        public void onPageFinished(WebView view, String url) {//  
        	bar.setVisibility(View.GONE);
            super.onPageFinished(view, url);  
        }  
        
        public void showDailog(String content,final String resoult){
        	AlertDialog.Builder builder=new AlertDialog.Builder(PayWebviewActivity.this);  //先得到构造器  
	        builder.setTitle(content); //设置标题  
	        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() { //设置确定按钮  
	            @Override  
	            public void onClick(DialogInterface dialog, int which) {  
	            	Intent intent = new Intent("bank.pay.com");
	            	intent.putExtra("re", resoult);
	            	sendBroadcast(intent);
	                dialog.dismiss(); //关闭dialog  
	                PayWebviewActivity.this.finish();
	            }  
	        });  
	        builder.show();
        }
    }

}
