package com.byt.market.activity;

import javax.security.auth.PrivateCredentialPermission;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.tools.LogCart;
/**
 * 
 * @author tanwenwei
 * 
 */
public class HtmlTVpalyActivity extends Activity{
	private FrameLayout videoview;// ȫ��ʱ��Ƶ����view
	private Button videolandport;
	private WebView videowebview;
	private Boolean islandport = true;//true��ʾ��ʱ��������false��ʾ��ʱ������
	private View xCustomView;
	private xWebChromeClient xwebchromeclient;
	//private String url = "http://look.appjx.cn/mobile_api.php?mod=news&id=12604";
	private String url = "http://player.youku.com/embed/XMTQyODk1ODc3Mg==";
	private WebChromeClient.CustomViewCallback 	xCustomViewCallback;
    //�������Ĵ��� 
    private int count = 0; 
    //��һ�ε����ʱ�� long�� 
    private long firClick,secClick; 
    //���һ�ε����ʱ�� 
    private long lastClick; 
    //��һ�ε����button��id 
    private int firstId; 
    //��ҳ���س������ʾҳ��
    private TextView videoerror;
    //��ҳ���س�������ˢ��
    private TextView videorefresh;
    private int msid = 0;
    private ProtocolTask mTask;//浏览量  请求接口
    private onDoubleClick listClick = new onDoubleClick();
    private GestureDetector gestureScanner;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//ȥ��Ӧ�ñ���
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_htmltv_play);
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		url = bundle.getString("url");
		msid = bundle.getInt("sid");
		//LogCart.Log("视频地址"+url);
		if(url==null||url.equals("")){
			//Toast.makeText(this, "视频地址有误", Toast.LENGTH_LONG).show();
			return;
		}
		initwidget();
		initListener();
		videowebview.loadUrl(url);
		
		mTask = new ProtocolTask(this);
		mTask.execute(Constants.JOKE_COMMENT_URL+"?qt=view&sid="+msid,null,null,null);
	}

	private void initListener() {
		// TODO Auto-generated method stub
		videolandport.setOnClickListener(new Listener());
	}

	@SuppressWarnings("deprecation")
	private void initwidget() {
		// TODO Auto-generated method stub
		videoview = (FrameLayout) findViewById(R.id.video_view);
		videoview.setOnTouchListener(listClick);
		videolandport = (Button) findViewById(R.id.video_landport);
		videowebview = (WebView) findViewById(R.id.video_webview);
		videoerror = (TextView) findViewById(R.id.video_error);
		videorefresh = (TextView) findViewById(R.id.video_refresh);
		videoerror.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
	   			videowebview.loadUrl("about:blank");
				videowebview.loadUrl(url);
				videoerror.setVisibility(View.GONE);
				videorefresh.setVisibility(View.VISIBLE);
				videowebview.setVisibility(View.GONE);
			}
		});
		WebSettings ws = videowebview.getSettings();
		/**
		 * setAllowFileAccess ���û��ֹWebView�����ļ����� setBlockNetworkImage �Ƿ���ʾ����ͼ��
		 * setBuiltInZoomControls �����Ƿ�֧������ setCacheMode ���û����ģʽ
		 * setDefaultFontSize ����Ĭ�ϵ������С setDefaultTextEncodingName �����ڽ���ʱʹ�õ�Ĭ�ϱ���
		 * setFixedFontFamily ���ù̶�ʹ�õ����� setJavaSciptEnabled �����Ƿ�֧��Javascript
		 * setLayoutAlgorithm ���ò��ַ�ʽ setLightTouchEnabled ��������꼤�ѡ��
		 * setSupportZoom �����Ƿ�֧�ֱ佹
		 * */
		ws.setBuiltInZoomControls(true);// �������Ű�ť
		ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);// �Ű���Ӧ��Ļ
		ws.setUseWideViewPort(true);// �������������
		ws.setLoadWithOverviewMode(true);// setUseWideViewPort��������webview�Ƽ�ʹ�õĴ��ڡ�setLoadWithOverviewMode����������webview���ص�ҳ���ģʽ��
		ws.setSavePassword(true);
		ws.setSaveFormData(true);// ���������
		ws.setJavaScriptEnabled(true);
		ws.setGeolocationEnabled(true);// ���õ���λ
		ws.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");// ���ö�λ�����ݿ�·��
		ws.setDomStorageEnabled(true);
		xwebchromeclient = new xWebChromeClient();
		videowebview.setWebChromeClient(xwebchromeclient);
		videowebview.setWebViewClient(new xWebViewClientent());		
	}

	class Listener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.video_landport:
				if (islandport) {
					Log.i("testwebview", "�����л�������");
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					videolandport.setText("ȫ������ʾ�ð�Ť������л�����");
				}else {

					Log.i("testwebview", "�����л�������");
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
					videolandport.setText("ȫ������ʾ�ð�Ť������л�����");
				}
				break;

			default:
				break;
			}
		}
	}
	   @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
	    	if (keyCode == KeyEvent.KEYCODE_BACK) {
	            if (inCustomView()) {
	            	hideCustomView();
	            	return true;
	            }else {
	            	//�˳�ʱ���ؿ���ַ��ֹ�˳�ʱ���ڲ�����Ƶ
		   			 videowebview.loadUrl("about:blank");
//		       		 mTestWebView.loadData("", "text/html; charset=UTF-8", null);
		   			 HtmlTVpalyActivity.this.finish();
		       		 Log.i("testwebview", "===>>>2");
					}
	    	}
	    	return true;
	    }
	   /**
	    * �ж��Ƿ���ȫ��
	    * @return
	    */
	    public boolean inCustomView() {
	 		return (xCustomView != null);
	 	}
	     /**
	      * ȫ��ʱ�����Ӽ�ִ���˳�ȫ������
	      */
	     public void hideCustomView() {
	    	 xwebchromeclient.onHideCustomView();
	 	}
	/**
	 * ����Javascript�ĶԻ�����վͼ�ꡢ��վ�����Լ���ҳ���ؽ��ȵ�
	 * @author
	 */
	public class xWebChromeClient extends WebChromeClient {
		private Bitmap xdefaltvideo;
		private View xprogressvideo;
		@Override
    	//����������Ƶʱȫ���ᱻ���õķ���
		public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback)
		{
			if (islandport) {

			}
			else{
				
//				ii = "1";
//				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
			}
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
	        videowebview.setVisibility(View.GONE);
	        //���һ����ͼ�Ѿ����ڣ���ô������ֹ���½�һ��
	        if (xCustomView != null) {
	            callback.onCustomViewHidden();
	            return;
	        }
	        
	        videoview.addView(view);
	        xCustomView = view;
	        xCustomViewCallback = callback;
	        videoview.setVisibility(View.VISIBLE);
		}
		
		@Override
		//��Ƶ�����˳�ȫ���ᱻ���õ�
		public void onHideCustomView() {
			
			if (xCustomView == null)//����ȫ������״̬
				return;	       
			
			// Hide the custom view.
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
			xCustomView.setVisibility(View.GONE);
			
			// Remove the custom view from its container.
			videoview.removeView(xCustomView);
			xCustomView = null;
			videoview.setVisibility(View.GONE);
			xCustomViewCallback.onCustomViewHidden();
			
			videowebview.setVisibility(View.VISIBLE);
			
	        //Log.i(LOGTAG, "set it to webVew");
		}
		//��Ƶ�������Ĭ��ͼ��
		@Override
		public Bitmap getDefaultVideoPoster() {
			//Log.i(LOGTAG, "here in on getDefaultVideoPoster");	
			if (xdefaltvideo == null) {
				xdefaltvideo = BitmapFactory.decodeResource(
						getResources(), R.drawable.playactivitbig);
		    }
			return xdefaltvideo;
		}
		//��Ƶ����ʱ����loading
		@Override
		public View getVideoLoadingProgressView() {
			//Log.i(LOGTAG, "here in on getVideoLoadingPregressView");
			
	        if (xprogressvideo == null) {
	            LayoutInflater inflater = LayoutInflater.from(HtmlTVpalyActivity.this);
	            xprogressvideo = inflater.inflate(R.layout.video_loading_progress, null);
	        }
	        return xprogressvideo; 
		}
    	//��ҳ����
    	 @Override
         public void onReceivedTitle(WebView view, String title) {
            (HtmlTVpalyActivity.this).setTitle(title);
         }

//         @Override
//       //��WebView���ȸı�ʱ���´��ڽ���
//         public void onProgressChanged(WebView view, int newProgress) {
//        	 (MainActivity.this).getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress*100);
//         }
         

	}

	/**
	 * �������֪ͨ��������¼�
	 * @author
	 */
	public class xWebViewClientent extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.i("webviewtest", "shouldOverrideUrlLoading: " + url);
			return false;
		}


		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			// �������������������������Ը���errorCode��ֵ�����жϣ�������ϸ�Ĵ���
			// view.loadUrl(file:///android_asset/error.html );

			Log.i("onPageStarted", "����");
			videowebview.setVisibility(View.GONE);
			videoerror.setVisibility(View.VISIBLE);
			videorefresh.setVisibility(View.GONE);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.i("onPageStarted", "onPage               Started"+url);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.i("onPageFinished", "onPageFinished>>>>>>"+url);
			videowebview.setVisibility(View.VISIBLE);
			videoerror.setVisibility(View.GONE);
			videorefresh.setVisibility(View.GONE);
			super.onPageFinished(view, url);
		}
	}
	/**
	 * ���������л�ʱ����ø÷���
	 * @author
	 */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	Log.i("testwebview", "=====<<<  onConfigurationChanged  >>>=====");
         super.onConfigurationChanged(newConfig);
         
         if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
             Log.i("webview", "   �����Ǻ���1");
             islandport = false;
            }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
             Log.i("webview", "   ����������1");
             islandport = true;
            }
    }
    class onDoubleClick implements View.OnTouchListener{  
        @Override  
        public boolean onTouch(View v, MotionEvent event) {  
            if(MotionEvent.ACTION_DOWN == event.getAction()){  
                count++;  
                if(count == 1){  
                    firClick = System.currentTimeMillis();  
                      
                } else if (count == 2){  
                    secClick = System.currentTimeMillis();  
                    if(secClick - firClick < 500){  
                        //˫���¼�  
                          Toast.makeText(HtmlTVpalyActivity.this, "˫������Ļ", Toast.LENGTH_LONG).show();
                    } 
                    else {

					}
                    count = 0;  
                    firClick = 0;  
                    secClick = 0;  
                      
                }  
            }  
            return true;  
        }         
          
    }

}
