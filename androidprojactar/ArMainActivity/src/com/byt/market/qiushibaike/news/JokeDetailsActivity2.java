package com.byt.market.qiushibaike.news;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.ShareActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.data.CateItem;
import com.byt.market.qiushibaike.download.DownloadInfo;
import com.byt.market.util.DateUtil;
import com.byt.market.util.StringUtil;
import com.byt.market.util.TextUtil;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.byt.market.view.BestoneScrollView;
import com.byt.market.view.CusPullListView.ScrollRefreshListener;
import com.byt.market.view.ScrollViewListener;
import com.byt.market.view.gifview.GifDecoderView;

public class JokeDetailsActivity2 extends Activity implements OnClickListener ,ScrollViewListener{
	private AlwsydMarqueeTextView tv_title;
	private TextView content,title_tv,time_tv;
	private ImageView imageView;
	private String imageUrl;
	GifDecoderView mGifView;
	// private ProgressDialog dialog;
	private ProgressBar loading_icon;

	public static final int SUCCESSED = 1;
	public static final int LOADING = 5;
	private static final int HANDLER_MSG_UPDATE_PROGRESS = 2;
	private static final int FAILURE = -1;
	private int downloadedSize = 0;
	private int fileTotalSize = 0;
	private TextView dlpercent;
	int mheight,mwidth;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private LinearLayout contentlayout;
	HashMap<Integer, String> mlist=new HashMap<Integer, String>();
	private BestoneScrollView bestonescroll;
	private LinearLayout sharelayout;
	private int msid=0;
	private boolean isshare=false;
	   private ProtocolTask mTask;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joke_details);
		try {
			
			initImageLoader();
			Intent intent = this.getIntent();
			Bundle bundle = intent.getExtras();
			msid=bundle.getInt("msid");
					if(bundle.getInt("isshare")==1)
					{
						isshare=false;
					}else{
						isshare=true;
					}
			imageUrl = bundle.getString("joke_image_path");
			initView(bundle);
			mTask = new ProtocolTask(this);
    		mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
			WindowManager wm=this.getWindowManager();
			 mheight=wm.getDefaultDisplay().getHeight();
			 mwidth=wm.getDefaultDisplay().getWidth();
			//initImage();
			//downloadGif();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void initImageLoader() {
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().cacheOnDisc().build();
	}

	private void initImage() {
		if ((imageUrl == null)) {
			imageView.setVisibility(View.GONE);
			return;
		} else if (!(imageUrl.startsWith("http"))) {
			imageView.setVisibility(View.GONE);
			return;
		}
		String type = imageUrl.substring(imageUrl.lastIndexOf('.') + 1);
		if (type.toString().equals("gif")) {
			imageView.setVisibility(View.GONE);
		} else {
			imageView.setVisibility(View.VISIBLE);
			imageLoader.displayImage(imageUrl, imageView, options);
		}
	}

	private void initView(Bundle bundle) {
		findViewById(R.id.topline4).setVisibility(View.GONE);
		findViewById(R.id.top_downbutton).setVisibility(View.GONE);
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
		tv_title.setText(getString(R.string.news));
		findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
		
		title_tv = (TextView) findViewById(R.id.joketile);
		time_tv = (TextView) findViewById(R.id.jokettime);
		title_tv.setText(getTitle(bundle.getString("title")));
		long time = Long.parseLong(bundle.getString("time"));
		String data = DateUtil.getFormatShortTime(time*1000);
		time_tv.setText(data);
		content = (TextView) findViewById(R.id.jokecontent);
		content.setVisibility(View.GONE);
		contentlayout=(LinearLayout) findViewById(R.id.contentlayout);	
		String contentstring=bundle.getString("joke_content");
		sharelayout=(LinearLayout) findViewById(R.id.sharelayout);
		sharelayout.setOnClickListener(this);
		bestonescroll=(BestoneScrollView) findViewById(R.id.bestonescroll);
		bestonescroll.setScrollViewListener(this);
		int i=0;
		if(contentstring!=null&&contentstring.trim().length()>0)
		{
			contentstring=contentstring.replaceAll("<p>", "");
			Spanned string333=Html.fromHtml(contentstring);
			while(!contentstring.equals(""))
			{
				int start=0;
				int end=0;
				String url="";
				String textstring="";
				String tmpString="";
				ImageView imageView ;
				TextView textview=new TextView(this);
				GifDecoderView gif=new GifDecoderView(this);
				LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
				lp1.setMargins(20, 0, 20, 0);//(int left, int top, int right, int bottom)
				LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); 
				lp2.setMargins(20, 12, 20, 12);
				FrameLayout.LayoutParams lp3=new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				lp3.setMargins(20, 12, 20, 12);
				textview.setTextColor(getResources().getColor(R.color.jokedetailcolr));
				textview.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
				// imageView.setPadding(0, 0, 0,0);
				// imageView.setScaleType(ScaleType.FIT_XY);
				textview.setPadding(0, 0, 0,0);
				textview.setGravity(Gravity.TOP|Gravity.LEFT);
				android.util.Log.d("newzx",contentstring);
				android.util.Log.d("newzx",contentstring);

				if(contentstring.startsWith("<img src="))
				{
					start=contentstring.indexOf("<img src=");
					contentstring=contentstring.substring(start+10, contentstring.length());
					end=contentstring.indexOf("\"");
					if(!contentstring.startsWith("http://")){
						url=Constants.APK_URL+contentstring.substring(0,end);
					}else{
						url=contentstring.substring(0,end); 
					}
					end=contentstring.indexOf("/>");
					contentstring=contentstring.substring(end+2, contentstring.length());
					String type = url.substring(url.lastIndexOf('.') + 1);
					if (type.toString().equals("gif")) {
						View view = LayoutInflater.from(this).inflate(
								R.layout.jokedetailimage, null);
						imageView=(ImageView) view.findViewById(R.id.gifimageview);
						imageView.setContentDescription("5"); 
						TextView textviewbyt=(TextView) view.findViewById(R.id.giftextview);
						textviewbyt.setId(110+i); 
						view.setLayoutParams(lp3);
						textviewbyt.setLayoutParams(lp3);
						mlist.put(110+i, url);
						imageView.setLayoutParams(lp3);
						textviewbyt.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {

								int id=arg0.getId();
								Intent intent = new Intent(JokeDetailsActivity2.this,PlayGifActivity.class);
								Bundle bundle = new Bundle();
								bundle.putString("joke_image_path", mlist.get(id));
								intent.putExtras(bundle);			

								JokeDetailsActivity2.this.startActivity(intent);
							}
						});
						if(!imageLoader.getPause().get())
						{
							int urlstart=url.lastIndexOf("/");
							int urlend=url.lastIndexOf('.');
							String tmpstring=url.substring(urlstart+1,urlend);
							url=url.replace(tmpstring, "thumb_"+tmpstring);
							imageLoader.displayImage(url, imageView, options);

						}contentlayout.addView(view);
					} else {
						View view = LayoutInflater.from(this).inflate(
								R.layout.jokedetailimagenotgif, null);
						view.setLayoutParams(lp3);

						imageView=(ImageView) view.findViewById(R.id.gifimageview);
						imageView.setContentDescription("5"); 
						imageView.setId(110+i); 
						imageView.setLayoutParams(lp3);
						if(!imageLoader.getPause().get())
							imageLoader.displayImage(url, imageView, options);
						contentlayout.addView(view);
					}


				}else{
					if(contentstring.indexOf("<img src=")>0)
					{
						end=contentstring.indexOf("<img src=");
						textstring=contentstring.substring(0,end);
						contentstring=contentstring.substring(end,contentstring.length());
					}else{
						textstring=contentstring;
						contentstring="";
					}
					if(textstring.indexOf("</p>")>-1)
					{
						textstring=textstring.replaceAll("</p>", "#9#9");
					}
					if(!TextUtils.isEmpty(textstring)&&textstring.trim().length()>0)
					{	
						textview.setLayoutParams(lp1);
						String string=Html.fromHtml(textstring).toString();
						String dsdfggg=" ";
						if(TextUtils.isEmpty(string)||string.trim().length()<1||string.trim().equals(dsdfggg))
						{
							continue;
						}
						if(string.indexOf("#9#9")>-1)
						{
							string=string.replaceAll("#9#9", "\n");
						}
						if(string.startsWith("\n"))
						{
							string=string.replace("\n","");
						}
						if(string.length()>2&&string.endsWith("\n"))
						{
							string=string.substring(0, string.length()-2);
						}
						textview.setText(string);
						contentlayout.addView(textview);
					}

				}
				i++;
			}
		}
		imageView = (ImageView) findViewById(R.id.jokeimage);
		imageView.setVisibility(View.GONE);
		mGifView = (GifDecoderView) findViewById(R.id.gif);

		loading_icon = (ProgressBar) findViewById(R.id.loading_icon);
		loading_icon.setVisibility(View.GONE);

		dlpercent = (TextView) findViewById(R.id.dlpercent);
		dlpercent.setVisibility(View.GONE);

	}

	/**
	 * bobo
	 * 处理 titile里面的时间 截取掉
	 */
	private String getTitle(String title){
		if(StringUtil.isNumeric(title.substring(0, 2))){
			return title.substring(10);
		}else{
			return title;
		}
	}
	@Override
	public void onClick(View view) {
		try {
			if ((view.getId() == R.id.titlebar_title)
					|| (view.getId() == R.id.titlebar_back_arrow)) {
				finish();
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			}else if(view.getId()==R.id.sharelayout){
				Intent intent =new Intent();
				intent.setClass(this,ShareActivity.class);	
				startActivityForResult(intent, 1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void downloadGif() {
		try {
			if ((imageUrl == null)) {
				imageView.setVisibility(View.GONE);
				return;
			} else if (!(imageUrl.startsWith("http"))) {
				loading_icon.setVisibility(View.GONE);
				dlpercent.setVisibility(View.GONE);
				return;
			}
			String type = imageUrl.substring(imageUrl.lastIndexOf('.') + 1);
			if (type.toString().equals("gif")) {
				downloadInBackground(imageUrl);
				loading_icon.setVisibility(View.VISIBLE);
				dlpercent.setVisibility(View.VISIBLE);
			} else {
				loading_icon.setVisibility(View.GONE);
				dlpercent.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	DownloadInfo downloadItem = null;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUCCESSED:
				try {
					loading_icon.setVisibility(View.GONE);
					dlpercent.setVisibility(View.GONE);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case HANDLER_MSG_UPDATE_PROGRESS:
				try {
					if (downloadItem != null) {
						String temp = String
								.valueOf(((double) downloadItem.curSize)
										/ downloadItem.allSize * 100);
						if (temp.contains(".")) {
							temp = temp.substring(0, temp.indexOf(".")) + "%";
						}
						dlpercent.setText(temp);
						sendEmptyMessageDelayed(HANDLER_MSG_UPDATE_PROGRESS, 1000);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				break;
			case LOADING:
				try {
					removeMessages(HANDLER_MSG_UPDATE_PROGRESS);
					dlpercent.setText("100%");
					mGifView.playGif(new FileInputStream(savePath), this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}

		};

	};

	String savePath = null;

	private void downloadInBackground(String downloadUrl) {
		String saveDir = Environment.getExternalStorageDirectory()
				+ Constants.JOKE_FOLDER;
		if (downloadUrl != null) {
			
			File dir = new File(saveDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		savePath = saveDir + StringUtil.md5Encoding(downloadUrl)
				+ downloadUrl.substring(downloadUrl.lastIndexOf("."));
		if (new File(savePath).exists()) {
			handler.sendEmptyMessage(LOADING);
		} else {
			downloadItem = new DownloadInfo(this, downloadUrl, savePath);
			new Thread(new DownloadTask(downloadItem)).start();
		}

	}

	class DownloadTask implements Runnable {
		private DownloadInfo downloadItem;

		public DownloadTask(DownloadInfo downloadItem) {
			this.downloadItem = downloadItem;
		}

		@Override
		public void run() {
			try {
				handler.sendEmptyMessageDelayed(HANDLER_MSG_UPDATE_PROGRESS, 1000);
				downloadItem.downloadFile();
				handler.removeMessages(HANDLER_MSG_UPDATE_PROGRESS);
				if (downloadItem.downloadState == DownloadInfo.DOWNLOAD_SUCCESS) {
					handler.sendEmptyMessage(LOADING);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		try {
			if(downloadItem!=null){
				downloadItem.isStop = true;
			}
			mGifView.recycleGif();
			imageLoader.cancelDisplayTask(imageView);
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onScrollChanged(BestoneScrollView scrollView, int x, int y,
			int oldx, int oldy) {
		 LinearLayout layout=(LinearLayout) findViewById(R.id.byttitlebar);
		 int height=(mheight-layout.getHeight())/2-50;
		 if(y>=height&&!isshare)
		 {
			 sharelayout.setVisibility(View.VISIBLE);
			 scrollView.scrollTo(0, height);
		 }else{
			 sharelayout.setVisibility(View.GONE);
		 }
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK&&data.getBooleanExtra("isok", false))
		{
			 sharelayout.setVisibility(View.GONE);
			 isshare=true;
		}else{
			 sharelayout.setVisibility(View.VISIBLE);
		}
	}
	private String getRequestUrl() {
    	//http://210.21.246.61:8022/Video/v1.php?qt=Info&name=Laurence%20Anyways
		//http://appstoredemo.szbytcloud.com/Joke/v1.php?qt=view&sid=847
		//public static  String JOKE_COMMENT_URL = "http://55mun.com:8022/Joke/v1.php";// / 糗百评论
		return Constants.JOKE_COMMENT_URL+"?qt=view&sid="+msid;
	}



	private Object getRequestContent() {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<String, String> getHeader() {
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		if (imei == null)
			imei = Util.getIMEI(MyApplication.getInstance());
		if (vcode == null)
			vcode = Util.getVcode(MyApplication.getInstance());
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(MyApplication.getInstance());
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("channel", channel);
		return map;
	}
	protected String tag() {
		return getClass().getSimpleName();
	}
}
