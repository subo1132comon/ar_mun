package com.byt.market.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.JetPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.CustomPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.PushNotificationBuilder;
import cn.jpush.android.api.TagAliasCallback;

import com.bluepay.data.Config;
import com.bluepay.interfaceClass.BlueInitCallback;
import com.bluepay.pay.BlueMessage;
import com.bluepay.pay.BluePay;
import com.bluepay.pay.Client;
import com.bluepay.pay.ClientHelper;
import com.bluepay.pay.IPayCallback;
import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.Constants.Settings;
import com.byt.market.SlidingMenu.OnClosedListener;
import com.byt.market.SlidingMenu.OnOpenedListener;
import com.byt.market.activity.SoftwareUninstallActivity.AppEntry;
import com.byt.market.adapter.MainFrameAdapter;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ReadHttpGet;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.asynctask.ProtocolTaskUser;
import com.byt.market.asynctask.ProtocolTaskUser.TaskListenerUser;
import com.byt.market.asynctask.ReadHttpGet.TaskListenerRead;
import com.byt.market.data.AppItem;
import com.byt.market.data.MarketUpdateInfo;
import com.byt.market.data.UserData;
import com.byt.market.download.ConnectionListener;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.download.GamesProvider;
import com.byt.market.download.NetworkStatus;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.MusicDownLoadManageActivity;
import com.byt.market.mediaplayer.data.PlayDownloadItem;
import com.byt.market.mediaplayer.db.InfoDao;
import com.byt.market.mediaplayer.music.PlayMusicService;
import com.byt.market.mediaplayer.service.PlayDownloadService;
import com.byt.market.net.NetworkUtil;
import com.byt.market.net.UrlHttpConnection;
import com.byt.market.net.UrlHttpConnection.Result;
import com.byt.market.qiushibaike.JokeActivity;
import com.byt.market.qiushibaike.database.JokeDataDatabaseHelper;
import com.byt.market.qiushibaike.database.JokeDataDatabaseHelper.joke_feedback_list;
import com.byt.market.qiushibaike.news.NewsActivity;
import com.byt.market.receiver.AlarmManagerRecive;
import com.byt.market.receiver.CancleRepitRecive;
import com.byt.market.receiver.ProgreesBarRecive;
import com.byt.market.service.HomeLoadService;
import com.byt.market.service.MyInstalledToastService;
import com.byt.market.tools.Dailog;
import com.byt.market.tools.LogCart;
import com.byt.market.ui.AVFragment;
import com.byt.market.ui.CateMainFragment;
import com.byt.market.ui.HomeFragment;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.ui.ManagerFragment;
import com.byt.market.ui.MineFragment;
import com.byt.market.ui.RankMainFragment;
import com.byt.market.ui.RightFragment;
import com.byt.market.ui.SubFragment;
import com.byt.market.ui.SubListFragment;
import com.byt.market.util.BluePayUtil;
import com.byt.market.util.FileUtil;
import com.byt.market.util.JPushSetAlias;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LocalGameDBManager;
import com.byt.market.util.LoginDBUtil;
import com.byt.market.util.NotifaHttpUtil;
import com.byt.market.util.NotifalUtile;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.RapitUtile;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.StringUtil;
import com.byt.market.util.SysNotifyUtil;
import com.byt.market.util.TextUtil;
import com.byt.market.util.UMstatisticsUtls;
import com.byt.market.util.Util;
import com.byt.market.util.NotifaHttpUtil.NotifaHttpResalout;
import com.byt.market.util.filecache.FileUtils;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.byt.market.view.CustomViewPager;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatReportStrategy;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;

/**
 * 涓婚〉闈紝鎺у埗鍏朵粬浜斾釜椤甸潰
 */
public class MainActivity extends MenuBaseActivity implements OnClickListener,
		OnPageChangeListener, OnClosedListener, DownloadTaskListener,
		OnOpenedListener, TaskListener, TaskListenerUser, TaskListenerRead ,BlueInitCallback{
	// constants
	private boolean isexit = false;
	public static final String EXT_TAB_ID = "tab_id";
	public static final int MAIN_HOME = 0;
	public static final int MAIN_RANK = 1;
	public static final int MAIN_CATE = 2;
	public static final int MAIN_SUB = 3;
	public static final int MAIN_MINE = 4;
	private SharedPreferences mSharedPreferences;
	public static final String PREF_GAME_UPDATE = "gameUpdate";
	public static final String PREF_GAME_UPDATE_KEY = "lastUpdate";
	private static final long REPEAT_ALARM_TIME = 30 * Util.MIN;
	private static final long PUSH_BETWEEN = 30 * Util.MIN; // one day
	private static final long LAUNCHE_MIN_BETWEEN = 30 * Util.MIN;

	// add by bobo
	public RelativeLayout mainprogressbar;
	public boolean is_INIT_RAPIT = false;
	public static final String JOKE_KEY = "txt_net_ver_update";
	public static final String JOKE_KEY2 = "txt_net_ver_update2";
	public static final String MUSIC_KEY = "aud_net_ver_update";
	public static final String TV_KEY = "vdo_tv_ver_update";
	public static final String NOVEL_KEY = "novel_update";
	public static final String RIDIAO_KEY = "ridiao_update";
	public static final String ANIME_KEY = "anime_update";
	public static final String NEWS_KEY = "news_update";
	public static final String NEWS_KEY2 = "news_update2";
	public static final String HOME_KEY = "home_update";
	public static final String SHOW_KEY = "show_update";
	public static final String MV_KEY = "mv_update";
	public static final String AV_KEY = "av_update";
	public static final String MVOE_KEY = "move_update";
	

	private long unpdataValue = 0;

	private boolean mRepeatSeted;
	private boolean isFistRuning = false;
	private boolean isSeondRuning = false;

	// views
	public CustomViewPager cPager;
	/** 椤堕儴鑿滃崟鏍忚缃寜閽� **/
	private ImageView iv_menu_settings;
	/** 椤堕儴鑿滃崟鏍忔煡鎵炬寜閽� **/
	private ImageView btn_menu_search;
	/** 椤堕儴鑿滃崟鏍忔爣棰榲iew,璺戦┈鐏晥鏋� **/
	private AlwsydMarqueeTextView tv_menu_title;
	/** 浠ヤ笅浜斾釜涓哄簳閮ㄧ殑浜斾釜TAB **/
	private RelativeLayout v_home;
	private RelativeLayout v_rank;
	private RelativeLayout v_cate;
	private RelativeLayout v_sub;
	private RelativeLayout v_mine;
	private ImageView loadingbg;// 鍔犺浇鑳屾櫙
	/** 姝ｅ湪涓嬭浇鏂囦欢鐨勪釜鏁� **/
	private TextView tv_down_num;
	public ImageView subject_redpoint, news_redpoint;
	public static final String DATABASE_NAME = "jokefeedback";
	/** 妫�娴嬪晢搴楃増鏈槸鍚︽湁鏇存柊锛岀敱姝ゅ垽鏂�"妫�鏌ョ増鏈洿鏂癷con"鏄惁闇�瑕佹樉绀衡�淣ew娴爣鈥� **/
	public static boolean update_need_show_info;

	// vairables
	public MainFrameAdapter mAdapter;
	public SharedPreferences sp;
	public Editor meditor;
	/** 杩涘叆搴旂敤鐨勬椂闂� **/
	private long enterAppTime;
	/** 閫�鍑哄簲鐢ㄧ殑鏃堕棿 **/
	private long exitAppTime;
	private ProtocolTask mTask = null;
	private ProtocolTask requestRecRepeatTimeTask;
	private boolean isClickedMyGame;
	private ChangeToHomeTabReceiver changeToHomeTabReceiver;
	/** 璁剧疆鐣岄潰 **/
	// private RightFragment rightFragment;
	/** 鏄惁slideMenu澶勪簬鎵撳紑鐘舵�� **/
	private boolean isSlideMenuOpened;
	private HashMap<String, String> map;
	/*
	 * bestone add by zengxiao for:娣诲姞鎼滅储鐣岄潰 20140628
	 */
	TextView mSearchKey;
	ImageView mClearSearch;
	ImageView mDoSearch;
	private String key = "";
	private String userName = null;
	private int mExtTabID = -1;
	private HomeLoadService loadService;
	public MarketContext maContext;
	// 瀹炰緥鍖栨暣涓猅itleBar
	private LinearLayout mTitleBar;
	/* bestone add end */

	private HorizontalScrollView mHorizontalScrollView;// 涓婇潰鐨勬按骞虫粴鍔ㄦ帶浠�

	// static MainActivity mMainActivity = null;
	// public static MainActivity getInstance(){
	// if(mMainActivity != null)
	// {
	// return mMainActivity;
	// }
	// return null;
	// }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MyApplication.getInstance().setMainactivity(this);//服务myumservie
		testClom();//服务myumservie
//		showAd();
		MobclickAgent.openActivityDurationTrack(false); //禁止默认的页面统计方式，这样将不会再自动统计Activity。
		sp = getSharedPreferences("info", Context.MODE_PRIVATE);
		meditor = sp.edit();
		// MobclickAgent.openActivityDurationTrack(false);
		int from = getIntent().getIntExtra("from", 0);
		mExtTabID = this.getIntent().getIntExtra(MainActivity.EXT_TAB_ID, -1);
		setContentView(R.layout.main_frame);
		Client.init(this, this);//BluePay
		// StatConfig.
		// JPushInterface.init(this);
		RapitUtile.setisBro(false);//底部av支付逻辑
		if(!RapitUtile.getisAlias()){
			JPushSetAlias.setAlias("zhangsan");//极光推送设置别名
		}
		// add by bobo 判断是否有更新
		unpdataValue = RapitUtile.getUpdate(RapitUtile.UPDATA_KEY);
		RapitUtile.setUpdata(RapitUtile.UPDATA_KEY);
		RapitUtile.setUpdata(RapitUtile.PULL_KEY);
		// add end
		initView();
		// 查看是否是点击通知栏进来的
		Bundle bunlde = getIntent().getBundleExtra(Constants.IS_NOTIF);
		if (bunlde != null) {
			// 腾讯
			MobclickAgent.onEvent(this, "Notification");
			StatService.trackCustomEvent(this, "Notification",
					"");
			NotifalUtile.formainTo(MainActivity.this, bunlde);
		}
		if (isFristRun()) {
			isFistRuning = true;
		} else if (isSecondRun()) {
			isSeondRuning = true;
		}
		// String channel = Util.getChannelName(this);
		// channel.equals("2026")
		// if(false){
		if (from == 0 && !isFistRuning
				&& RapitUtile.tvToastShow("GuideActivity")) {
			SharedPreferences sharepreferences = getSharedPreferences(
					"mylogupdate", 0);
			// Editor edit = sharepreferences.edit();
			/* add by zengxiao for logupdate */
			String urllog = sharepreferences.getString("urllog", "");
			ImageLoader imageLoader;
			DisplayImageOptions options;
			imageLoader = ImageLoader.getInstance();
			options = new DisplayImageOptions.Builder().cacheOnDisc()
					.showImageForEmptyUri(R.drawable.bestone_mainbg).build();
			if (!imageLoader.getPause().get()) {
				imageLoader.displayImage(urllog, loadingbg, options);
			}
			/* add end */
			loadingbg.setVisibility(View.VISIBLE);
			// Intent intent = new Intent(MainActivity.this,HomeActivity.class);
			// startActivity(intent);

		}
		// }else{
		// if (from == 0 && !isFistRuning) {
		// SharedPreferences sharepreferences = getSharedPreferences(
		// "mylogupdate", 0);
		// //Editor edit = sharepreferences.edit();
		// /* add by zengxiao for logupdate */
		// String urllog = sharepreferences.getString("urllog", "");
		// ImageLoader imageLoader;
		// DisplayImageOptions options;
		// imageLoader = ImageLoader.getInstance();
		// options = new DisplayImageOptions.Builder().cacheOnDisc()
		// .showImageForEmptyUri(R.drawable.bestone_mainbg).build();
		// if (!imageLoader.getPause().get()) {
		// imageLoader.displayImage(urllog, loadingbg, options);
		// }
		// /* add end */
		// loadingbg.setVisibility(View.VISIBLE);
		// // Intent intent = new Intent(MainActivity.this,HomeActivity.class);
		// // startActivity(intent);
		//
		// }
		// }
		SharedPreferences rapit_p = MyApplication.getInstance()
				.getSharedPreferences(Constants.RAPIT_PLAYSP, 0);
		SharedPreferences.Editor editor = rapit_p.edit();
		// channel.equals("2026")
		// if(false){
		// if (isFistRuning||isSeondRuning) {
		// editor.putBoolean(Constants.RAPIT_KEY, true);
		// Intent intent = new Intent(MainActivity.this, GuideActivity.class);
		// startActivity(intent);
		// }
		// }else{
		// 如果第一次运行
		if (isFistRuning) {
			editor.putBoolean(Constants.RAPIT_KEY, true);
			Intent intent = new Intent(MainActivity.this, GuideActivity.class);
			startActivity(intent);
		} else {
			if (!RapitUtile.tvToastShow("GuideActivity")) {
				editor.putBoolean(Constants.RAPIT_KEY, true);
				Intent intent = new Intent(MainActivity.this,
						GuideActivity.class);
				startActivity(intent);
			}
		}
		// }
		editor.commit();

		new Thread(new Runnable() {

			@Override
			public void run() {

				if (!NetworkUtil.isNetWorking(MainActivity.this)) {
					mHandler.removeMessages(0);
					mHandler.sendEmptyMessageDelayed(0, 2500);

					mHandler.removeMessages(INIT_IP_FLAG);
					mHandler.sendEmptyMessage(INIT_IP_FLAG);
				} else {
					mHandler.removeMessages(0);
					mHandler.sendEmptyMessageDelayed(0, 5000);
					mHandler.removeMessages(INIT_IP_FLAG);
					Message message = new Message();
					message.what = INIT_IP_FLAG;
					message.arg1 = 112;
					mHandler.sendMessageDelayed(message, 5000);

					startTime = System.currentTimeMillis();
					Constants.isHasEffectiveURL(mHandler);
					if (loadingbg.getVisibility() == View.VISIBLE) {
						long endTime = System.currentTimeMillis() - startTime;
						mHandler.removeMessages(0);
						if (endTime > 3200) {
							mHandler.sendEmptyMessage(0);
						} else {
							mHandler.sendEmptyMessageDelayed(0, 3200 - endTime);
						}

					}
				}

			}
		}).start();

	}

	// 是否是 第一次运行
	private boolean isFristRun() {
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				"share", MODE_PRIVATE);
		boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
		Editor editor = sharedPreferences.edit();
		if (!isFirstRun) {
			return false;
		} else {
			editor.putBoolean("isFirstRun", false);
			editor.commit();
			return true;
		}
	}

	private boolean isSecondRun() {
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				"share", MODE_PRIVATE);
		boolean isSecondRun = sharedPreferences.getBoolean("isSecondRun", true);
		Editor editor = sharedPreferences.edit();
		if (!isFristRun()) {
			if (!isSecondRun) {
				return false;
			} else {
				editor.putBoolean("isSecondRun", false);
				editor.commit();
				return true;
			}
		}
		return false;
	}

	// 判断是否是第二次
	long startTime = 0;

	public void initNewData() {
		initData();
		addEvent();
		loadService = new HomeLoadService();
		startService(new Intent(MainActivity.this, HomeLoadService.class));
		initScreenInfo();
		registerReceivers();
		// bindBaiDuPushService();
		showFragmentView(MAIN_HOME);
		cPager.setCurrentItem(MAIN_HOME, false);
		UMstatisticsUtls.umCunt(this, "home");
//		showFragmentView(MAIN_RANK);
//		cPager.setCurrentItem(MAIN_RANK, false);
		maContext = MarketContext.getInstance();
		// handleMarketUpdateNotify(); //鍘熸洿鏂� 鎺ュ彛鍏堝仠鐢�
		DownloadTaskManager.getInstance().addListener(this);
		updateDownloadNum();

		long recTime = SharedPrefManager.getRecStartTime(this);
		long repeatTime = SharedPrefManager.getRecRepeatTime(this);
		/*
		 * if (NetworkUtil.isNetWorking(this)) { if (repeatTime == 0 ||
		 * System.currentTimeMillis() - recTime > repeatTime) { Intent intent =
		 * new Intent(this, SplashRecommendActivity.class); //
		 * startActivity(intent); } }
		 */
		requestRecRT();
		// UmengUpdateAgent.setUpdateOnlyWifi(false);
		// UmengUpdateAgent.update(this);
		// add by wangxin
		new CheckLocalAndUpdateTask().execute();
		enterAppTime = System.currentTimeMillis();
		// check clinet update

		SharedPrefManager.getClintInfo(this);
		// mMainActivity = this;
		userName = SharedPrefManager.getLastLoginUserName(this);
		if ("".equals(userName) || userName == null) {
			HashMap<String, String> infos = SharedPrefManager
					.getClintInfo(this);
			String cid = infos.get(SharedPrefManager.CID);
			if (cid != null) {
				userName = cid;
				SharedPrefManager.setLastLoginUserName(this, userName);
			}
		}
		if ("".equals(userName) || userName == null) {
			MyApplication.getInstance().isuserloading = true;
			doUserRegister();
		} else {
			MyApplication.getInstance().isuserloading = true;

			doUserLogin(userName);
		}
		new Thread() {
			public void run() {
				String apkFilename = Constants.APKNAME;
				// 鍒犻櫎涓存椂鏂囦欢
				File downFile = new File(MyApplication.DATA_DIR
						+ File.separator + apkFilename);
				if (downFile != null && downFile.exists())
					downFile.delete();
				downFile = new File(getFilesDir() + File.separator
						+ apkFilename);// getFilesDir()=/data/data/com.byt.market/files
				if (downFile != null && downFile.exists())
					downFile.delete();
				map = Util.getUplodMap(MainActivity.this, Constants.LOGIN);
				Util.POSTDATA = null;
				Util.POSTDATA = Util.getLogPostData(MainActivity.this);
				Util.getDataPointMap(MainActivity.this);
				/*
				 * if(init==1) {
				 */
				loadService.initData(MainActivity.this, maContext,
						getRequestUrl(), getRequestContent(), tag(),
						getHeader());

				// mHandler.sendEmptyMessageDelayed(0, 2500);
				/* } */

			}
		}.start();
		StatService.trackCustomEvent(this, "onCreate", "");
		StatConfig.initNativeCrashReport(this, null);
		/* add by zengxiao for:wifi */
		if (Util.getNetAvialbleType(this).equals("wifi")) {

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		showClose();
		try {
			if (changeToHomeTabReceiver != null) {
				unregisterReceiver(changeToHomeTabReceiver);
			}
			if (mupdateBroad != null) {
				unregisterReceiver(mupdateBroad);
			}
			// 鏆傚仠鎵�鏈夐煶涔愪笅杞�
			Iterator<PlayDownloadItem> it = PlayDownloadService.PlayDownloadItems
					.values().iterator();
			List<PlayDownloadItem> list = new ArrayList<PlayDownloadItem>();
			while (it.hasNext()) {
				list.add(it.next());
			}
			for (PlayDownloadItem playdown : list) {
				playdown.setPause(true);
			}
			mHandler.removeMessages(0);
			mHandler.removeMessages(INIT_IP_FLAG);
			Client.exit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
//			Log.i("xxx", "onDestroy BluePay_Exit==>");
//			// jsxltj.BluePay_Exit();
//			jsxltj.JsXltj_Exit();
//			jsxltj = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		RapitUtile.setTpay(false);
	}

	class CheckLocalAndUpdateTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			initAllGamesDB();
			return null;
		}

	}

	private void bindBaiDuPushService() {
		// if (!BaiDuPushUtil.hasBind(getApplicationContext())) {
		// PushManager.startWork(getApplicationContext(),
		// PushConstants.LOGIN_TYPE_API_KEY,
		// BaiDuPushUtil.getMetaValue(this, "api_key"));
		// // Push: 濡傛灉鎯冲熀浜庡湴鐞嗕綅缃帹閫侊紝鍙互鎵撳紑鏀寔鍦扮悊浣嶇疆鐨勬帹閫佺殑寮�鍏�
		// PushManager.enableLbs(getApplicationContext());
		// }
	}

	/**
	 * 璇锋眰鏈嶅姟鍣紝寮瑰睆鏃堕棿闂撮殧
	 */
	public void requestRecRT() {
		if (requestRecRepeatTimeTask == null) {
			requestRecRepeatTimeTask = new ProtocolTask(this);
		}
		requestRecRepeatTimeTask.setListener(new RecRepeatTaskListener());
		requestRecRepeatTimeTask.execute(getRecRepeatUrl(), null, getClass()
				.getSimpleName(), getHeader());
	}

	public String getRecRepeatUrl() {
		return Constants.LIST_URL + "?qt=" + Constants.STARTRECREPEATTIME;
	}

	class RecRepeatTaskListener implements TaskListener {
		@Override
		public void onNoNetworking() {
		}

		@Override
		public void onNetworkingError() {
		}

		@Override
		public void onPostExecute(byte[] bytes) {
			new RecRepeatParseTask().execute(bytes);
		}

	}

	class RecRepeatParseTask extends AsyncTask<byte[], Void, byte[]> {
		@Override
		protected byte[] doInBackground(byte[]... params) {
			// TODO Auto-generated method stub
			byte[] bytes = params[0];
			if (bytes != null) {
				String datas = new String(bytes);
				try {
					final JSONObject obj = new JSONObject(datas);
					final JSONObject dataObj = obj.getJSONObject("data");
					long time = JsonParse.pareseRecRepeatTime(dataObj);
					SharedPrefManager.setRecRepeatTime(getApplicationContext(),
							time);
				} catch (Exception e) {
					e.printStackTrace();

				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(byte[] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			requestRecRepeatTimeTask = null;
		}

	}

	// ------2
	public HashMap<String, String> getHeader() {
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		String model = "";
		String uid = SharedPrefManager.getLastLoginUserName(this);
		if (uid == null) {
			uid = "";
		}
		if (imei == null)
			imei = Util.getIMEI(this);
		if (vcode == null)
			vcode = Util.getVcode(this);
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(MyApplication.getInstance());
		}
		if (Util.getModel() != null) {
			model = Util.getModel();
		}
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language == null) {
			language = "";
		}
		HashMap<String, String> map = new HashMap<String, String>();
		// ------运营商
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = tm.getSubscriberId();
		// String ss = "520031001499489";
		if (imsi!= null) {
			String p = imsi.substring(0, 5);
			String mcc = p.substring(0, 3);
			String mnc = p.substring(3, 5);
			map.put("mcc", mcc);
			map.put("mnc", mnc);
			map.put("imsi", imsi);
		}
		String jpushid = Util.getJPushID(this);
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("channel", channel);
		map.put("model", model);
		map.put("uid", uid);
		map.put("lang", language);
		map.put("jpushid", jpushid);
		return map;
	}

	private void initAllGamesDB() {
		try {
			if (!this.getDatabasePath(GamesProvider.ALL_GAMES_NAME).exists()) {
				FileUtil.copyFile(
						this.getAssets().open(GamesProvider.ALL_GAMES_NAME),
						this.getDatabasePath(GamesProvider.ALL_GAMES_NAME));
			}
			LocalGameDBManager.getInstance().syncLocalGames(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent != null && intent.getIntExtra(EXT_TAB_ID, -1) == MAIN_MINE) {
			onClick(v_mine);
		}
	}

	@Override
	public void addEvent() {

		// mDoSearch.setOnClickListener(this);

		/* bestone add end */

		// iv_menu_settings.setOnClickListener(this);
		cPager.setOnPageChangeListener(this);
		v_home.setOnClickListener(this);
		v_rank.setOnClickListener(this);
		v_cate.setOnClickListener(this);
		v_sub.setOnClickListener(this);
		v_mine.setOnClickListener(this);
		slidingMenu.setOnClosedListener(this);
		slidingMenu.setOnOpenedListener(this);
		// btn_menu_search.setOnClickListener(this);
	}

	private void doSearchKeyPre() {
		Intent intent = new Intent(Constants.TOSEARCH);
		intent.putExtra("searchstring", key);
		startActivity(intent);
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	// //-------------------by bobo test-----------
	private Handler mhandler = new Handler() {
		public void handleMessage(Message msg) {
			Dailog.dismis();
			if (msg.what == 1) {
				Toast.makeText(MainActivity.this,
						"Has been deleted--" + msg.what, Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(MainActivity.this, "error--" + msg.what,
						Toast.LENGTH_LONG).show();
			}
		};
	};

	@Override
	public void initView() {
		mainprogressbar = (RelativeLayout) findViewById(R.id.r_main_progressBar);
		loadingbg = (ImageView) findViewById(R.id.loadingbg);
		cPager = (CustomViewPager) findViewById(R.id.vp_main_frame);
		cPager.setScrollable(false);// 绂佹婊戝姩
		v_home = (RelativeLayout) findViewById(R.id.homePageview);
		v_rank = (RelativeLayout) findViewById(R.id.rankButtonview);
		v_cate = (RelativeLayout) findViewById(R.id.categoryButtonview);
		v_sub = (RelativeLayout) findViewById(R.id.subjectButtonview);
		v_mine = (RelativeLayout) findViewById(R.id.mineButtonview);
		// mHorizontalScrollView =
		// (HorizontalScrollView)findViewById(R.id.horizontalScrollView);

		tv_down_num = (TextView) findViewById(R.id.tv_down_num);
		// mTitleBar = (LinearLayout)findViewById(R.id.main_top_frame);
		subject_redpoint = (ImageView) findViewById(R.id.subject_redpoint);
		news_redpoint = (ImageView) findViewById(R.id.new_redpoint);

		// FragmentTransaction transaction = this.getSupportFragmentManager()
		// .beginTransaction();
		// rightFragment = new RightFragment();
		// slidingMenu.setSecondaryMenu(R.layout.menu_frame);
		// transaction.replace(R.id.menu_frame, rightFragment);
		// transaction.commit();
		slidingMenu.setSlidingEnabled(false);

		if (Util.loadInfos != null) {
			new Thread() {
				public void run() {
					loadImage(Util.loadInfos.url);
				}
			}.start();
		}
		findViewById(R.id.titlebar_title).setVisibility(View.GONE);
		findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
		findViewById(R.id.iv_settings).setVisibility(View.GONE);
		findViewById(R.id.search_page_view).setVisibility(View.VISIBLE);
		findViewById(R.id.search_page_view).setOnClickListener(this);
		ImageView img = (ImageView) findViewById(R.id.top_downbutton);
		img.setImageResource(R.drawable.top_share);
		img.setVisibility(View.VISIBLE);
		img.setOnClickListener(this);// 娣诲姞涓嬭浇鐣岄潰鎸夐挳
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		registerReceiver(mupdateBroad, new IntentFilter(
				"com.intent.refreshupdate"));

		// 支付测试接口
		findViewById(R.id.test).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				Dailog.show2(MainActivity.this);
//
//				new Thread() {
//					public void run() {
//						int re = MainActivity.jsxltj.JsXltj_GetInitResult();
//						LogCart.Log("查看 是否  初始化------" + re);
//						int nCheckResult = 2;
//						String uename = MyApplication.getInstance().getUser()
//								.getNickname();
//						LogCart.Log("查看 是否  初始化------" + uename);
//						nCheckResult = MainActivity.jsxltj
//								.TestDelUsrFeeInfo(uename);
//
//						Message msg = mhandler.obtainMessage();
//						msg.what = nCheckResult;
//						mhandler.sendMessage(msg);
//
//					};
//				}.start();
				
			}
		});
	}

	private void loadImage(String url) {
		if (url == null || url.trim().equals("")) {
			return;
		}
		String iconName = StringUtil.md5Encoding(Constants.IMG_URL
				+ Constants.HOME_BG);
		String path = FileUtil.getIconPath() + iconName + ".png";
		File f = new File(path);
		if (f.exists())
			f.delete();
		UrlHttpConnection huc = new UrlHttpConnection(this);
		Result result = huc.downloadIcon(Constants.IMG_URL + url);
		byte[] buffer = result.getResult();
		if (result.getState() == UrlHttpConnection.OK && buffer != null) {
			FileUtil.saveBytesToFile(buffer, iconName);
			if (Util.loadInfos != null) {
				LoginDBUtil.add(this, Util.loadInfos);
			}
		}
	}

	@Override
	public void initData() {
		// sp = getSharedPreferences("info", Context.MODE_PRIVATE);
		// editor = sp.edit();
		// isMainFrame = true;
		// Util.OPENAPPTIME = System.currentTimeMillis();
		// long peroid = sp.getLong("peroid", 0);
		// Util.addData(marketContext, peroid);
		// mAdapter = new MainFrameAdapter(getSupportFragmentManager());
		// cPager.setOffscreenPageLimit(4);
		// cPager.setAdapter(mAdapter);
		// tryCheckGameUpdate(this, true);
	}

	/**
	 * 鐐瑰嚮浜嬩欢澶勭悊
	 */
	@Override
	public void onClick(View arg0) {

		/*
		 * if (arg0.getId() != R.id.mineButtonview) {
		 * mTitleBar.setVisibility(View.VISIBLE); }
		 * 
		 * if (arg0.getId() == R.id.iv_settings2) {
		 * slidingMenu.setSlidingEnabled(false); if
		 * (slidingMenu.isMenuShowing()) showContent(); else showMenu(); }else
		 */if (arg0.getId() == R.id.homePageview && currentView != MAIN_HOME) {
			changeToHome();
			UMstatisticsUtls.umCunt(this, "home");
			StatService.trackCustomBeginEvent(this, "home", "time");
			StatService.trackCustomEndEvent(this, "bav", "");
			StatService.trackCustomEndEvent(this, "newsb", "time");
			StatService.trackCustomEndEvent(this, "funbutton", "time");
			StatService.trackCustomEndEvent(this, "mine", "time");
		} else if (arg0.getId() == R.id.rankButtonview
				&& currentView != MAIN_RANK) {
			StatService.trackCustomBeginEvent(this, "funbutton", "time");
			StatService.trackCustomEndEvent(this, "bav", "");
			StatService.trackCustomEndEvent(this, "newsb", "time");
			StatService.trackCustomEndEvent(this, "home", "time");
			StatService.trackCustomEndEvent(this, "mine", "time");
			showFragmentView(1);
			cPager.setCurrentItem(MAIN_RANK, false);
//			subject_redpoint.setVisibility(View.GONE);
			
//			if (Regyrefsh(JOKE_KEY)) {
//				startActivity(new Intent(MyApplication.getInstance(),
//						JokeActivity.class));
//				overridePendingTransition(R.anim.push_left_in,
//						R.anim.push_left_out);
//			}
			UMstatisticsUtls.umCunt(this, "fun");
				sendBroadcast(new Intent("com.market.fun"));
		} else if (arg0.getId() == R.id.categoryButtonview
				&& currentView != MAIN_CATE) {
			StatService.trackCustomBeginEvent(this, "newsb", "time");
			StatService.trackCustomEndEvent(this, "funbutton", "time");
			StatService.trackCustomBeginEvent(this, "bav", "");
			StatService.trackCustomEndEvent(this, "home", "time");
			StatService.trackCustomEndEvent(this, "mine", "time");
//			news_redpoint.setVisibility(View.GONE);
//			if (Regyrefsh(NEWS_KEY)) {
//				startActivity(new Intent(MyApplication.getInstance(),
//						NewsActivity.class));
//				overridePendingTransition(R.anim.push_left_in,
//						R.anim.push_left_out);
//			}
			showFragmentView(2);
			cPager.setCurrentItem(MAIN_CATE, false);
			UMstatisticsUtls.umCunt(this, "news");
		} else if (arg0.getId() == R.id.subjectButtonview
				&& currentView != MAIN_SUB) {
			showFragmentView(3);
			StatService.trackCustomBeginEvent(this, "bav", "time");
			StatService.trackCustomEndEvent(this, "funbutton", "time");
			StatService.trackCustomEndEvent(this, "newsb", "time");
			StatService.trackCustomEndEvent(this, "home", "time");
			StatService.trackCustomEndEvent(this, "mine", "time");
			cPager.setCurrentItem(MAIN_SUB, false);
			UMstatisticsUtls.umCunt(this, "av");
			if(RapitUtile.queayIspay()){
				sendBroadcast(new Intent("com.market.avplay"));
			}
		} else if (arg0.getId() == R.id.mineButtonview
				&& currentView != MAIN_MINE) {
			isClickedMyGame = true;
			tv_down_num.setVisibility(View.GONE);// 褰撲竴鐐瑰嚮鍒欒TAB涓婄殑瑙掓爣闅愯棌
			showFragmentView(4);
			StatService.trackCustomEndEvent(this, "funbutton", "time");
			StatService.trackCustomEndEvent(this, "newsb", "time");
			StatService.trackCustomEndEvent(this, "bav", "");
			StatService.trackCustomEndEvent(this, "home", "time");
			StatService.trackCustomBeginEvent(this, "mine", "time");
			cPager.setCurrentItem(MAIN_MINE, false);
			// mTitleBar.setVisibility(View.GONE);
			UMstatisticsUtls.umCunt(this, "mine");
//			mAdapter.list.add(3, new AVFragment());
//			mAdapter.notifyDataSetChanged();
		} else if (arg0.getId() == R.id.search_page_view) {
			//搜索----------
			Toast.makeText(this, getString(R.string.resouch), Toast.LENGTH_LONG).show();
//			startActivity(new Intent(Constants.TOSEARCH));
//			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		} else if (arg0.getId() == R.id.top_downbutton) {
//			Intent downloadIntent = new Intent(MainActivity.this,
//					DownLoadManageActivity.class);
//			downloadIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
//					DownLoadManageActivity.TYPE_FROM_DOWNLOAD);
			
			MobclickAgent.onEvent(this, "HomeShare");
			Intent intentshare =new Intent();
			intentshare.setClass(this,ShareActivity.class);		
			startActivity(intentshare);
//			Intent downloadIntent = new Intent(MainActivity.this, MusicDownLoadManageActivity.class);
//			startActivity(downloadIntent);
//			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

		}
	}

	/**
	 * 鑾峰緱褰撳墠琚�変腑鐨凴adioButton璺濈宸︿晶鐨勮窛绂�
	 */
	private float getCurrentCheckedRadioLeft(int curId) {
		if (R.id.homePageview == curId) {
			return v_home.getLeft();
		} else if (R.id.rankButtonview == curId) {
			return v_rank.getLeft();
		} else if (R.id.categoryButtonview == curId) {
			return v_cate.getLeft();
		} else if (R.id.subjectButtonview == curId) {
			return v_sub.getLeft();
		} else if (R.id.mineButtonview == curId) {
			return v_mine.getLeft();
		}
		return 0f;
	}

	public void changeToHome() {
		if (isSlideMenuOpened) {
			// 濡傛灉鑿滃崟澶勪簬鎵撳紑鐘舵��,鍒欏厛鍏抽棴
			slidingMenu.toggle();
		}
		// mTitleBar.setVisibility(View.VISIBLE);
		showFragmentView(0);
		cPager.setCurrentItem(MAIN_HOME, false);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		if (arg0 == 0)
			// 鐢ㄦ潵鎺у埗婊戝姩menu鐨�
			slidingMenu.setSlidingEnabled(false);
		else
			slidingMenu.setSlidingEnabled(false);
		if (arg0 != currentView)
			showFragmentView(arg0);
		Fragment fragment = mAdapter.list.get(arg0);
		if (fragment instanceof HomeFragment) {
			// if (!((HomeFragment) fragment).flag) {
			// ((ListViewFragment) fragment).requestDelay();
			// }
		} else {
			if (fragment instanceof RankMainFragment) {
				((RankMainFragment) fragment).requestDelay();
			} else if (fragment instanceof CateMainFragment) {
				((CateMainFragment) fragment).requestDelay();
			} else if (fragment instanceof ManagerFragment) {
				((ManagerFragment) fragment).getuserMsg();
				if (checkVerNeedShowInfo()) {
					((ManagerFragment) fragment).showRedPoint();
				} else {
					((ManagerFragment) fragment).hideRedPoint();
				}
			} else if (fragment instanceof ListViewFragment) {
				((ListViewFragment) fragment).requestDelay();
			}
		}

		((MainFrameAdapter) cPager.getAdapter()).clearFragmentIcon(cPager);
		((MainFrameAdapter) cPager.getAdapter()).loadFragmentIcon(cPager);
	}

	/**
	 * 璁剧疆涓昏彍鍗曢�夋嫨椤�
	 * 
	 * @param arg0
	 * @param flag
	 */

	private void setButtonSelected(int arg0, boolean flag) {
		slidingMenu.setSlidingEnabled(false);
		// int curId=R.id.homePageview;
		switch (arg0) {
		case MAIN_CATE:

			v_home.setSelected(false);
			v_rank.setSelected(false);
			v_sub.setSelected(false);
			v_mine.setSelected(false);
			v_cate.setSelected(true);
			// MobclickAgent.onEvent(MyApplication.getInstance(), "tab_cate");
			Util.addListData(marketContext, LogModel.L_CATE_HOME);
			// tv_menu_title.setText(getString(R.string.bottom_cate));
			// curId=R.id.categoryButtonview;
			break;
		case MAIN_RANK:

			v_home.setSelected(false);
			v_sub.setSelected(false);
			v_mine.setSelected(false);
			v_cate.setSelected(false);
			v_rank.setSelected(true);
			// MobclickAgent.onEvent(MyApplication.getInstance(), "tab_rank");
			Util.addListData(marketContext, LogModel.L_RANK);
			// tv_menu_title.setText(getString(R.string.bottom_rank));
			// curId=R.id.rankButtonview;
			break;
		case MAIN_HOME:

			slidingMenu.setSlidingEnabled(false);
			v_rank.setSelected(false);
			v_sub.setSelected(false);
			v_mine.setSelected(false);
			v_cate.setSelected(false);
			v_home.setSelected(true);
			// MobclickAgent.onEvent(MyApplication.getInstance(), "tab_home");
			Util.addListData(marketContext, LogModel.L_HOME);
			// tv_menu_title.setText(getString(R.string.bottom_home));
			// curId=R.id.homePageview;
			break;
		case MAIN_SUB:

			v_home.setSelected(false);
			v_rank.setSelected(false);
			v_mine.setSelected(false);
			v_cate.setSelected(false);
			v_sub.setSelected(true);
			// MobclickAgent.onEvent(MyApplication.getInstance(), "tab_sub");
			Util.addListData(marketContext, LogModel.L_SUBJECT_HOME);
			// tv_menu_title.setText(getString(R.string.bottom_sub));
			// curId=R.id.subjectButtonview;
			break;
		case MAIN_MINE:

			// slidingMenu.setSlidingEnabled(true);
			v_home.setSelected(false);
			v_rank.setSelected(false);
			v_sub.setSelected(false);
			v_cate.setSelected(false);
			v_mine.setSelected(true);
			// MobclickAgent.onEvent(MyApplication.getInstance(), "tab_mine");
			Util.addListData(marketContext, LogModel.L_DOWN_MANAGER);
			// tv_menu_title.setText(getString(R.string.app_managertitlest));
			// curId=R.id.mineButtonview;
			break;
		}
		/*
		 * mHorizontalScrollView.smoothScrollTo( (int)
		 * getCurrentCheckedRadioLeft(curId) - v_rank.getLeft(), 0);
		 */
	}

	@Override
	public void onBeforeShow(int currentFrame) {
		lastFrame = getCurrentView();
		setButtonSelected(currentFrame, true);
	}

	@Override
	public void onAfterShow(int currentFrame) {
		currentView = currentFrame;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
		boolean flag = false;
		// 鍚庨��鎸夐挳
		try {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				/*
				 * if (MAIN_MINE == cPager.getCurrentItem() && ((MineFragment)
				 * mAdapter.getItem(MAIN_MINE)) .onBackDown()) { //
				 * 鍦ㄢ�滄垜鐨勨�濋〉闈㈣繘鍏ョ紪杈戠姸鎬佹椂锛屾寜杩斿洖鎸夐挳閫�鍑虹紪杈戠姸鎬� } else if
				 * (!slidingMenu.isMenuShowing()) {
				 */if (DownloadTask
						.getRealDownloaddingCount(DownloadTaskManager.FILE_TYPE_APK) > 0) {
					try {
						showDialog(R.string.exit_confirm_downloading);
					} catch (Exception e) {
					}
				} else {
					if (currentView != MAIN_HOME) {
						changeToHome();
					} else {/*
							 * try{ showDialog(R.string.exit_confirm);
							 * }catch(Exception e){}
							 */
						if (!isexit) {
							isexit = true;
							// editor.putLong("unpdata",System.currentTimeMillis());//add
							// by bobo
							Toast.makeText(MainActivity.this,
									getString(R.string.store_exittext),
									Toast.LENGTH_SHORT).show();
							new Handler().postDelayed(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									isexit = false;
								}
							}, 2000);
						} else {
							// System.currentTimeMillis();
							long peroid = System.currentTimeMillis()
									- Util.OPENAPPTIME;
							meditor.putLong("peroid", peroid);
							meditor.putBoolean("isClientActive", false);
							meditor.commit();
							mHandler.sendEmptyMessageDelayed(MSG_KILL_SELF, 500);
						}

					}
					/* } */

					flag = true;
				}
			} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {

			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				// 鎸塵enu閿鍔犲垎浜晢搴楀姛鑳�
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ShareActivity.class);
				MainActivity.this.startActivity(intent);

				/*
				 * slidingMenu.setSlidingEnabled(true); if
				 * (slidingMenu.isMenuShowing()) showContent(); else showMenu();
				 */
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == R.string.exit_confirm) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(id).setMessage(R.string.exit_confirm_msg);
			builder.setIcon(R.drawable.icon);
			builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					return true;
				}
			});

			builder.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							long peroid = System.currentTimeMillis()
									- Util.OPENAPPTIME;
							meditor.putLong("peroid", peroid);
							meditor.putBoolean("isClientActive", false);
							meditor.commit();
							mHandler.sendEmptyMessageDelayed(MSG_KILL_SELF, 500);
						}
					});

			builder.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			return builder.create();
		} else if (id == R.string.exit_confirm_downloading) {
			final CheckBox checkBox = new CheckBox(this);
			checkBox.setText(R.string.exit_confirm_goondownload_check);
			checkBox.setChecked(true);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(id).setMessage(
					R.string.exit_confirm_downloading_msg);
			builder.setIcon(R.drawable.icon);
			builder.setView(checkBox);
			builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					return true;
				}
			});

			builder.setNegativeButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							long peroid = System.currentTimeMillis()
									- Util.OPENAPPTIME;
							meditor.putLong("peroid", peroid);
							meditor.commit();
							if (checkBox.isChecked()) {
								MyApplication.getInstance().exit();
								finish();
								MyApplication.getInstance().mWillKillSelfAfterExit = true;
							} else {
								DownloadTaskManager.getInstance()
										.letAllDownningTaskPause();
								mHandler.sendEmptyMessageDelayed(MSG_KILL_SELF,
										500);
							}

						}
					});

			builder.setPositiveButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			return builder.create();
		} else if (id == R.string.dialog_goon_download_title) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(id)
					.setMessage(
							this.getString(
									R.string.dialog_goon_download_msg,
									DownloadTask
											.getIdleOrPauseCount(DownloadTaskManager.FILE_TYPE_APK)));
			builder.setIcon(R.drawable.icon);
			builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					return true;
				}
			});

			builder.setNegativeButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							DownloadTaskManager.getInstance().autoResume();
						}
					});

			builder.setPositiveButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onClosed() {
		isSlideMenuOpened = false;
		if (currentView == MAIN_HOME)
			slidingMenu.setSlidingEnabled(false);
		else
			slidingMenu.setSlidingEnabled(false);
	}

	@Override
	public void onOpened() {
		isSlideMenuOpened = true;
		// if (rightFragment != null) {
		// rightFragment.onResume();
		// }
	}

	@Override
	public void downloadEnded(DownloadTask task, int totalTask, int progressTask) {
		// TODO Auto-generated method stub
	}

	@Override
	public void downloadProgress(DownloadTask task, int totalTask,
			int progressTask, long progressSize, long totalSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadStarted(DownloadTask task, int totalTask,
			int progressTask, long totalSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {
		// TODO Auto-generated method stub
	}

	@Override
	public void endConnecting(DownloadTask task, int totalTask,
			int progressTask, DownloadException result) {
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage(MSG_UPDATE_DOWN_NUM);
	}

	@Override
	public void refreshUI() {
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage(MSG_UPDATE_DOWN_NUM);
	}

	@Override
	public void startConnecting(DownloadTask task, int totalTask,
			int progressTask) {
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage(MSG_UPDATE_DOWN_NUM);
	}

	private static final int MSG_UPDATE_DOWN_NUM = 1;
	private static final int MSG_KILL_SELF = 2;
	private static final int UPDATE_REDVIEW = 3;
	public static final int INIT_IP_FLAG = 1111;
	public static final int INIT_RAPIT = 1112;
	boolean isInited = false;
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_UPDATE_DOWN_NUM: {
				updateDownloadNum();
				break;
			}
			// ADD BY 初始化 红点
			case INIT_RAPIT:

				isMainFrame = true;
				Util.OPENAPPTIME = System.currentTimeMillis();
				long peroid = sp.getLong("peroid", 0);
				Util.addData(marketContext, peroid);
				mAdapter = new MainFrameAdapter(getSupportFragmentManager());
				cPager.setOffscreenPageLimit(4);
				cPager.setAdapter(mAdapter);
				is_INIT_RAPIT = true;
				mainprogressbar.setVisibility(View.GONE);
				Log.d("nnlog", "&&&&&&&&&-----View.GONE---------");
				// tryCheckGameUpdate(this, true);
				break;
			case MSG_KILL_SELF:
				exitAppTime = System.currentTimeMillis();
				LocalGameDBManager.getInstance().sendLocalUseAppStatusToServer(
						MainActivity.this, sp.getLong("lastEnterAppTime", 0),
						enterAppTime, exitAppTime);
				meditor.putLong("lastEnterAppTime", enterAppTime);
				meditor.commit();
				if (!PackageUtil.isPkgMgrRunning()) {
					// System.exit(0);
					finish();
				} else {
					MyApplication.getInstance().mWillKillSelfAfterExit = true;
					finish();
				}
				MyApplication.getInstance().exit();
				break;
			case 0:
				try {
					// if(!isCheckCompele){
					// Log.i("test", "timeout switch ip==>"+Constants.LIST_URL);
					// Constants.switchIP();
					// }
					// Log.i("test", "cur==>"+Constants.LIST_URL);
					// initNewData();
					// Intent intent = new Intent(MainActivity.this,
					// JokeActivity.class);
					// startActivity(intent);
					loadingbg.setVisibility(View.GONE);
					if(!is_INIT_RAPIT){
						
						mainprogressbar.setVisibility(View.VISIBLE);// add by bobo
					}
					//showFragmentView(1);//--------------------------------------------------------------------
					
					// loadService.initData(MainActivity.this, maContext,
					// getRequestUrlAd(), getRequestContent(), tag(),
					// getHeader());

					/*
					 * if (getIntent() != null &&
					 * getIntent().getIntExtra(EXT_TAB_ID, -1) == MAIN_MINE) {
					 * // 鏌ョ湅涓嬭浇鍒楄〃 if (HomeActivity.pActivity == null) { // not
					 * inited Intent intent = new Intent(MainActivity.this,
					 * HomeActivity.class); intent.putExtra(EXT_TAB_ID,
					 * MAIN_MINE);
					 * intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					 * finish(); MainActivity.this.startActivity(intent); } else
					 * { onClick(v_mine); } } else { // 姝ｅ父,鎻愮ず鐢ㄦ埛缁х画涓嬭浇 if
					 * (NetworkStatus
					 * .getInstance(MainActivity.this).getNetWorkState() ==
					 * ConnectionListener.CONN_WIFI && DownloadTask
					 * .getRealDownloaddingCount
					 * (DownloadTaskManager.FILE_TYPE_APK) == 0 && DownloadTask
					 * .getIdleOrPauseCount(DownloadTaskManager.FILE_TYPE_APK) >
					 * 0) { showDialog(R.string.dialog_goon_download_title); } }
					 * checkClientUpdate();
					 */
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case INIT_IP_FLAG:
				try {
					if (!isInited) {
						isInited = true;
						if (msg.arg1 == 112) {
							Log.i("test", "timeout switch ip==>"
									+ Constants.LIST_URL);
							Constants.switchIP();
						}
						yuleVersionRead();
						initNewData();
						loadService.initData(MainActivity.this, maContext,
								getRequestUrlAd(), getRequestContent(), tag(),
								getHeader());
						if (getIntent() != null
								&& getIntent().getIntExtra(EXT_TAB_ID, -1) == MAIN_MINE) {
							// 鏌ョ湅涓嬭浇鍒楄〃
							if (HomeActivity.pActivity == null) {
							} else {
								onClick(v_mine);
							}
						} else {
							// 姝ｅ父,鎻愮ず鐢ㄦ埛缁х画涓嬭浇
							if (NetworkStatus.getInstance(MainActivity.this)
									.getNetWorkState() == ConnectionListener.CONN_WIFI
									&& DownloadTask
											.getRealDownloaddingCount(DownloadTaskManager.FILE_TYPE_APK) == 0
									&& DownloadTask
											.getIdleOrPauseCount(DownloadTaskManager.FILE_TYPE_APK) > 0) {
								showDialog(R.string.dialog_goon_download_title);
							}
						}
						checkClientUpdate();
						//yuleVersionRead();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
	};

	private void updateDownloadNum() {
		if (isClickedMyGame) {
			return;
		}
		final int needUpdateCount = DownloadTask.getNeedUpdateCount();
		final int innerInstalledButNotOpenedGamesCount = DownloadTaskManager
				.getInstance().getInnerInstalledButNotOpenedGamesCount();
		int count = needUpdateCount
				+ innerInstalledButNotOpenedGamesCount
				+ DownloadTaskManager.getInstance().getAllUpdatingAppList()
						.size();
		if (count == 0) {
			tv_down_num.setVisibility(View.GONE);
		} else {
			tv_down_num.setVisibility(View.GONE);
			/*
			 * tv_down_num.setVisibility(View.VISIBLE); if (count >= 100) {
			 * tv_down_num.setText("n"); } else {
			 * tv_down_num.setText(String.valueOf(count)); }
			 */
		}

	}

	public interface OnMenuAnimation {
		public void onAnimationStart();

		public void onAnimationEnd();
	}

	@Override
	public void showNoRootUpdate(final MarketUpdateInfo marketUpdateInfo) {
		Builder builder = new Builder(this);
		builder.setTitle(getString(R.string.updateversionprom));
		StringBuilder sb = new StringBuilder(getString(R.string.updateexpain)
				+ "\n\n");
		sb.append(getString(R.string.updateversion) + marketUpdateInfo.vname
				+ "\n");
		sb.append(marketUpdateInfo.describe + "\n\n");
		sb.append(getString(R.string.softwaresize)
				+ Util.apkSizeFormat(
						(marketUpdateInfo.length * 1.0) / 1024 / 1024, "M")
				+ "\n");
		sb.append(getString(R.string.isupdate));
		builder.setMessage(sb.toString());
		builder.setNegativeButton(getString(R.string.state_update_action_text),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						apkPath = marketUpdateInfo.apk;
						updateNow(marketUpdateInfo);
					}
				});
		builder.setPositiveButton(R.string.later,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		builder.create().show();

	}

	@Override
	public void unInstalledSucess(String packageName) {

	}

	@Override
	public void installedSucess(DownloadTask downloadTask) {
		if (SharedPrefManager.isInstalledNotification(this)) {
			Intent intent = new Intent(this, MyInstalledToastService.class);
			intent.putExtra(Constants.ExtraKey.APP_ITEM,
					downloadTask.mDownloadItem);
			startService(intent);
		}
		if (SharedPrefManager.isInstalledDeleteApk(this)) {
			DownloadTaskManager.getInstance().deleteDownlaodFile(
					downloadTask.mDownloadItem);
		}
	}

	@Override
	public void networkIsOk() {
		// TODO Auto-generated method stub

	}

	private void registerReceivers() {
		changeToHomeTabReceiver = new ChangeToHomeTabReceiver();
		IntentFilter intentFilter = new IntentFilter(
				Constants.Action.CHANGE_TO_HOME);
		intentFilter.addAction("UPDATEREDPOINT");
		registerReceiver(changeToHomeTabReceiver, intentFilter);
	}

	/**
	 * 鍒囨崲鍒囦富鐣岄潰,姣斿鍗曞嚮"鎵炬父鎴�"
	 */
	private final class ChangeToHomeTabReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.Action.CHANGE_TO_HOME.equals(intent.getAction())) {
				changeToHome();
			} else if (intent.getAction().equals("UPDATEREDPOINT")) {
				updateSubjectRedpointstart();
			}
		}
	}

	@Override
	public void installedSucess(String packageName) {
		// TODO Auto-generated method stub

	}

	public void checkClientUpdate() {
		if (NetworkUtil.isNetWorking(this)) {
			ProtocolTask updateClient = new ProtocolTask(this);
			updateClient.setListener(new TaskListener() {

				@Override
				public void onNoNetworking() {

				}

				@Override
				public void onNetworkingError() {

				}

				@Override
				public void onPostExecute(byte[] bytes) {
					try {
						if (bytes != null) {
							JSONObject result = new JSONObject(
									new String(bytes));
							int status = JsonParse.parseResultStatus(result);
							int count = result.getInt("allCount");
							if (status == 1 && count > 0) {
								update_need_show_info = true;
								parserUpdate((JSONObject) result.getJSONArray(
										"data").get(0));
							} else {
								update_need_show_info = false;
							}
						} else {
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.gc();
				}

			});
			String userName = SharedPrefManager.getLastLoginUserName(this);
			try {
				int code = getPackageManager().getPackageInfo(getPackageName(),
						0).versionCode;
				updateClient.execute(Constants.LIST_URL
						+ "?qt=uclient&versioncode=" + code + "&uid="
						+ userName, null, tag(), getHeader(MainActivity.this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private MarketUpdateInfo marketUpdateInfo;

	public void parserUpdate(JSONObject update) throws Exception {
		if (update == null)
			return;
		this.marketUpdateInfo = new MarketUpdateInfo();
		if (!update.isNull("CLVNAME")) {
			this.marketUpdateInfo.vname = update.getString("CLVNAME");
		}
		if (!update.isNull("CLCODE")) {
			this.marketUpdateInfo.vcode = update.getInt("CLCODE");
		}
		if (!update.isNull("CLSIZE")) {
			this.marketUpdateInfo.length = update.getLong("CLSIZE");
		}
		if (!update.isNull("CLUPDATE")) {
			this.marketUpdateInfo.updatetime = update.getString("CLUPDATE");
		}
		if (!update.isNull("APK")) {
			this.marketUpdateInfo.apk = update.getString("APK");
		}
		if (!update.isNull("UPINFO")) {
			this.marketUpdateInfo.describe = update.getString("UPINFO");
		}
		if (!update.isNull("RIGHT")) {
			this.marketUpdateInfo.right = update.getInt("RIGHT");
		}
		showNoRootUpdate(marketUpdateInfo);
	}

	public void tryCheckGameUpdate(Context context, boolean push) {
		Log.i("appupdate", "tryCheckGameUpdate");
		if (mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(PREF_GAME_UPDATE,
					Context.MODE_PRIVATE);
		}
		long lastUpdate = mSharedPreferences.getLong(PREF_GAME_UPDATE_KEY, 0);
		// Log.i("appupdate","lastUpdate = "+);
		final long between = push ? PUSH_BETWEEN : LAUNCHE_MIN_BETWEEN;
		/*
		 * if(System.currentTimeMillis() - lastUpdate > between ||
		 * System.currentTimeMillis() - lastUpdate <= 0 ){
		 */if (mTask == null) {
			mTask = new ProtocolTask(context);
			mTask.setListener(this);
			mTask.execute(getRequestUrl(), getPostData(), tag(),
					getHeader(context));
			Log.i("appupdate", "execute");

			/* } */
		}
		setRepeatUpdate(context);
	}

	private void setRepeatUpdate(Context context) {
		if (mRepeatSeted) {
			return;
		}

		Intent intent = new Intent(Constants.ACTION_REQUEST_GAME_UPDATE_CHECK);
		PendingIntent refreshIntent = PendingIntent.getBroadcast(context, 1,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC, REPEAT_ALARM_TIME,
				REPEAT_ALARM_TIME, refreshIntent);
		mRepeatSeted = true;
	}

	// ---1
	public HashMap<String, String> getHeader(Context context) {
		String model = Util.mobile;
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		if (model == null)
			model = Util.getModel();
		if (imei == null)
			imei = Util.getIMEI(context);
		if (vcode == null)
			vcode = Util.getVcode(context);
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(context);
		}
		String uid = SharedPrefManager.getLastLoginUserName(this);
		if (uid == null) {
			uid = "";
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("model", model);
		map.put("channel", channel);
		map.put("uid", uid);
		return map;
	}

	private String getPostData() {
		List<PackageInfo> apps = getPackageManager().getInstalledPackages(0);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < apps.size(); i++) {
			PackageInfo tmppi = apps.get(i);
			if (((tmppi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
					|| ((tmppi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)) {
				sb.append(tmppi.packageName).append(":")
						.append(tmppi.versionCode).append("|");
			}

		}
		Log.i("appupdate",
				"GameUpdateManager getPostData sb = " + sb.toString());
		return sb.toString();
	}

	private Object tag() {
		return getClass().getSimpleName();
	}

	private Object getRequestUrl() {
		return Constants.LIST_URL + "?qt=" + Constants.UPDATE;
	}

	@Override
	public void onNoNetworking() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNetworkingError() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostExecute(byte[] bytes) {
		// TODO Auto-generated method stub
		// Log.i("rxmyzx", "onPostExecute");
		Log.i("appupdate", "touch GameUpdateManager onPostExecute");
		if (bytes != null) {
			String datas = new String(bytes);
			Log.i("appupdate", "touch GameUpdateManager onPostExecute datas = "
					+ datas);
			try {
				final JSONObject obj = new JSONObject(datas);
				if (obj != null) {
					JSONArray js = (obj.getJSONArray("data"));
					final List<AppItem> list = JsonParse.parseUpdateGames(js);
					// Log.i("rxmyzx", "list.size="+list.size());
					DownloadTaskManager.getInstance()
							.updateAfterUpdateGamesGot(list);
					Log.i("appupdate", "JsonParse.parseResultStatus(obj) = "
							+ JsonParse.parseResultStatus(obj));
					if (JsonParse.parseResultStatus(obj) == 1) {
						mSharedPreferences
								.edit()
								.putLong(PREF_GAME_UPDATE_KEY,
										System.currentTimeMillis()).commit();
						final int updateCount = DownloadTask
								.getNeedUpdateCount();
						Log.i("appupdate", "updateCount = " + updateCount);
						if (list != null && updateCount > 0
								&& Settings.userUpdateNotify) {
							SysNotifyUtil.notifyGameListUpdate(
									MyApplication.getInstance(),
									R.drawable.icon, updateCount);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mTask = null;
	}

	/** 妫�娴嬪晢搴楃増鏈槸鍚︽湁鏇存柊锛岀敱姝ゅ垽鏂�"妫�鏌ョ増鏈洿鏂癷con"鏄惁闇�瑕佹樉绀篘ew娴爣 **/
	public boolean checkVerNeedShowInfo() {
		return update_need_show_info;
	}

	/* add by zengxiao for:鍒锋柊瑕佹洿鏂扮殑搴旂敤 */
	BroadcastReceiver mupdateBroad = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if ("com.intent.refreshupdate".equals(arg1.getAction())) {

			}
			tryCheckGameUpdate(MainActivity.this, true);
		}
	};

	private void initScreenInfo() {
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		MyApplication.density = outMetrics.density;
		MyApplication.mScreenHeight = outMetrics.heightPixels;
		MyApplication.mScreenWidth = outMetrics.widthPixels;
	}

	private void doUserLogin(String userName) {
		ProtocolTaskUser task = new ProtocolTaskUser(this);
		task.setListener(this);
		HashMap<String, String> infos = SharedPrefManager.getClintInfo(this);
		String pid = infos.get(SharedPrefManager.PID);
		String var = Constants.LIST_URL + "?qt=" + Constants.USERLOGIN
				+ "&uid=" + userName + "&pwd="
				+ StringUtil.md5Encoding("123456");
		if (pid != null) {
			var = var + "&pid=" + pid;
		}

		Log.d("mylog", "----" + var);
		task.execute(var, null, "UserLoginFragment", getHeader());
	}

	private void doUserRegister() {
		ProtocolTaskUser task = new ProtocolTaskUser(this);
		String url = Constants.LIST_URL + "?qt=reg&uid=" + "abc" + "&pwd="
				+ StringUtil.md5Encoding("123456") + "&mac="
				+ Util.getMAC(this);
		task.setListener(this);
		Log.d("mylog", "--uu--" + url);
		task.execute(url, null, "UserRegisteActivity", getHeader());
	}

	/* add end */

	@Override
	public void onNoNetworkingUser() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNetworkingErrorUser() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostExecuteUser(byte[] bytes) {
		// TODO Auto-generated method stub
		if ("".equals(userName) || userName == null) {
			initRegisterInfo(bytes);
		} else {
			initLoginInfo(bytes);
		}
	}

	private void initRegisterInfo(byte[] bytes) {
		JSONObject json = null;
		UserData user = new UserData();
		try {

			json = new JSONObject(new String(bytes));
			int resultStatus = json.getInt("resultStatus");
			if (resultStatus == 1) {
				JSONObject data = json.getJSONObject("data");
				// Toast.makeText(this,
				// getString(R.string.toast_regist_success),
				// Toast.LENGTH_SHORT).show();

				user.setType(UserData.TYPE_ME);
				user.setNickname(data.getString("NICKNAME"));
				user.setIconUrl(data.getString("ICON"));
				user.setCredit(data.getInt("CREDIT"));
				if (data.isNull("SEX")) {
					user.setGender(UserData.MALE);
				} else {
					int sex = data.getInt("SEX");
					switch (sex) {
					case 0:
						user.setGender(UserData.FEMALE);
						break;
					case 1:
						user.setGender(UserData.MALE);
						break;
					default:
						user.setGender(UserData.MALE);
						break;
					}
				}

				user.setUid(data.getString("USID"));
				user.setType(UserData.TYPE_ME);
				user.setMd5(StringUtil.md5Encoding("123456"));
				SharedPrefManager.setLastLoginUserName(this, user.getUid());

				MyApplication.getInstance().setUser(user);
				MyApplication.getInstance().getUser().setLogin(true);
				MyApplication.getInstance().updateUserInfo();
				Constants.ISSHOW = true;
				MyApplication.getInstance().isuserloading = false;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
//			if (Util.isCanUseSim(this)) {
//				Intent intent1 = new Intent(this, jsxltjsrv.class);
//				if (intent1 != null) {
//					this.startService(intent1);
//					try {
//						Log.i("xxx", "start init ..==>" + jsxltj);
//						// jsxltj.BluePay_Init(MainActivity.this);
//						jsxltj.JsXltj_Init(MainActivity.this);
//						Log.i("xxx", "end init==>");
//					} catch (Exception e) {
//						e.printStackTrace();
//						Log.i("xxx", "Exception init==>" + e.toString());
//					}
//					Log.i("xxx", "end2222 init==>");
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initLoginInfo(byte[] bytes) {
		UserData user = new UserData();
		try {
			JSONObject json = new JSONObject(new String(bytes));
			int resultStatus = json.getInt("resultStatus");

			if (resultStatus == 1) {
				int allcount = json.getInt("allCount");
				if (allcount == 0) {
					// 璇佹槑鐢ㄦ埛鍚嶆垨瀵嗙爜鏈夐棶棰�
					String errorResult = json.optString("data");
				} else {
					JSONObject result = json.getJSONObject("data");
					user = JsonParse.parseUserInfo(result);
					String url = result.getString("LOADPIC");

					String logeststick = null;
					String price = null;
					if (!result.isNull("LONGESTSTICK")) {
						logeststick = result.getString("LONGESTSTICK");
					}
					if (!result.isNull("PRICE")) {
						price = result.getString("PRICE");
					}
					if (Integer.parseInt(price) > 0) {

						String pricestring = String.format(getResources()
								.getString(R.string.toast_login_succes2),
								Integer.parseInt(price));
						String logeststickstring = String.format(getResources()
								.getString(R.string.toast_login_success),
								Integer.parseInt(logeststick));
						Toast.makeText(this, logeststickstring + pricestring,
								Toast.LENGTH_LONG).show();
					}
					logUpdate(url);
					if (user != null) {
						if (result.isNull("SEX")) {
							user.setGender(UserData.MALE);
						} else {
							int sex = result.getInt("SEX");
							switch (sex) {
							case 0:
								user.setGender(UserData.FEMALE);
								break;
							case 1:
								user.setGender(UserData.MALE);
								break;
							default:
								user.setGender(UserData.MALE);
								break;
							}
						}
						user.setUid(result.getString("USID"));
						user.setType(UserData.TYPE_ME);
						user.setMd5(StringUtil.md5Encoding("123456"));
						SharedPrefManager.setLastLoginUserName(this,
								user.getUid());
						setLoginUserInfo(user);
					}
				}
			}
			MyApplication.getInstance().isuserloading = false;

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
//			if (Util.isCanUseSim(this)) {
//				Intent intent1 = new Intent(this, jsxltjsrv.class);
//				if (intent1 != null) {
//					this.startService(intent1);
//				}
//				try {
//					Log.i("xxx", "start init ..==>" + jsxltj);
//					// jsxltj.BluePay_Init(MainActivity.this);
//					jsxltj.JsXltj_Init(MainActivity.this);
//					Log.i("xxx", "end init==>");
//				} catch (Exception e) {
//					e.printStackTrace();
//					Log.i("xxx", "Exception init==>" + e.toString());
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//public static jsxltjsrv jsxltj = new jsxltjsrv();

	private Object getRequestContent() {
		return Util.POSTDATA;
		// return null;
	}

	private void setLoginUserInfo(UserData user) {
		MyApplication.getInstance().setUser(user);
		MyApplication.getInstance().getUser().setLogin(true);
		MyApplication.getInstance().updateUserInfo();
		Constants.ISSHOW = true;
	}

	private void initMTAConfig(boolean isDebugMode) {
		if (isDebugMode) { // 璋冭瘯鏃跺缓璁缃殑寮�鍏崇姸鎬�
			// 鏌ョ湅MTA鏃ュ織鍙婁笂鎶ユ暟鎹唴瀹�
			StatConfig.setDebugEnable(true);
			// 绂佺敤MTA瀵筧pp鏈鐞嗗紓甯哥殑鎹曡幏锛屾柟渚垮紑鍙戣�呰皟璇曟椂锛屽強鏃惰幏鐭ヨ缁嗛敊璇俊鎭��
			// StatConfig.setAutoExceptionCaught(false);
			// StatConfig.setEnableSmartReporting(false);
			// Thread.setDefaultUncaughtExceptionHandler(new
			// UncaughtExceptionHandler() {
			//
			// @Override
			// public void uncaughtException(Thread thread, Throwable ex) {
			// logger.error("setDefaultUncaughtExceptionHandler");
			// }
			// });
			// 璋冭瘯鏃讹紝浣跨敤瀹炴椂鍙戦��
			// StatConfig.setStatSendStrategy(StatReportStrategy.BATCH);
			// // 鏄惁鎸夐『搴忎笂鎶�
			// StatConfig.setReportEventsByOrder(false);
			// // 缂撳瓨鍦ㄥ唴瀛樼殑buffer鏃ュ織鏁伴噺,杈惧埌杩欎釜鏁伴噺鏃朵細琚啓鍏b
			// StatConfig.setNumEventsCachedInMemory(30);
			// // 缂撳瓨鍦ㄥ唴瀛樼殑buffer瀹氭湡鍐欏叆鐨勫懆鏈�
			// StatConfig.setFlushDBSpaceMS(10 * 1000);
			// // 濡傛灉鐢ㄦ埛閫�鍑哄悗鍙帮紝璁板緱璋冪敤浠ヤ笅鎺ュ彛锛屽皢buffer鍐欏叆db
			// StatService.flushDataToDB(getApplicationContext());

			// StatConfig.setEnableSmartReporting(false);
			// StatConfig.setSendPeriodMinutes(1);
			// StatConfig.setStatSendStrategy(StatReportStrategy.PERIOD);
		} else { // 鍙戝竷鏃讹紝寤鸿璁剧疆鐨勫紑鍏崇姸鎬侊紝璇风‘淇濅互涓嬪紑鍏虫槸鍚﹁缃悎鐞�
			// 绂佹MTA鎵撳嵃鏃ュ織
			StatConfig.setDebugEnable(false);
			// 鏍规嵁鎯呭喌锛屽喅瀹氭槸鍚﹀紑鍚疢TA瀵筧pp鏈鐞嗗紓甯哥殑鎹曡幏
			StatConfig.setAutoExceptionCaught(true);
			// 閫夋嫨榛樿鐨勪笂鎶ョ瓥鐣�
			StatConfig.setStatSendStrategy(StatReportStrategy.APP_LAUNCH);
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 注册 取消底部红点的 广播接收器
		this.registerReceiver(new CancleRepitRecive(subject_redpoint),
				new IntentFilter("com.tyb.mark.jokcanclerepit"));
		this.registerReceiver(new CancleRepitRecive(news_redpoint),
				new IntentFilter("com.tyb.mark.newscanclerepit"));
		 JPushInterface.setAliasAndTags(this, "2025", null, new
		 Myjpushcallback());
		this.registerReceiver(new ProgreesBarRecive(mainprogressbar), new IntentFilter(Constants.PROGREES_BAR_RECIVE));
		// ---------------------------------
		CustomPushNotificationBuilder builder = new CustomPushNotificationBuilder(
				MainActivity.this, R.layout.customer_notitfication_layout,
				R.id.icon, R.id.title, R.id.text);
		// 指定定制的 Notification Layout
		builder.statusBarDrawable = R.drawable.ic_app;
		// 指定最顶层状态栏小图标
		builder.layoutIconDrawable = R.drawable.ic_app;
		// 指定下拉状态栏时显示的通知图标
		JPushInterface.setPushNotificationBuilder(1, builder);
		Log.d("nnlog", "rid-----" + JPushInterface.getRegistrationID(this));
		// updateSubjectRedpointstart();
		// SharedPreferences sharepreferences=getSharedPreferences("myupdate",
		// 0);
		// unpdataValue = sharepreferences.getLong("unpdata", 0);
		// Log.d("mylog", " onStart()unpdataValue---"+unpdataValue);
		// if(unpdataValue == 0){
		// unpdataValue = System.currentTimeMillis();
		// }
		// Editor uedit=sharepreferences.edit();
		// uedit.putLong("unpdata",System.currentTimeMillis());//add by bobo
		// uedit.commit();
		// Log.d("mylog", " onStart()()---"+System.currentTimeMillis());
		//
	}

	public void updateSubjectRedpointstart() {

		SharedPreferences txtNetResVer = MyApplication.getInstance()
				.getSharedPreferences("yulever", 0);
		int net_ver1 = txtNetResVer.getInt(JOKE_KEY, 0);
		int net_ver2 = txtNetResVer.getInt(NEWS_KEY, 0);

		if (net_ver1 == 1) {
			subject_redpoint.setVisibility(View.VISIBLE);

		} else {
			subject_redpoint.setVisibility(View.GONE);
		}
		if (net_ver2 == 1) {
			news_redpoint.setVisibility(View.VISIBLE);
		} else {
			news_redpoint.setVisibility(View.GONE);
		}
	}

	public boolean Regyrefsh(String key) {
		boolean rb = false;
		SharedPreferences txtNetResVer = MyApplication.getInstance()
				.getSharedPreferences("yulever", 0);
		int net_ver1 = txtNetResVer.getInt(key, 0);

		if (net_ver1 == 1) {
			rb = true;
		}
		return rb;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);// 友盟
		StatService.onResume(this);
		JPushInterface.onResume(this);
		// JPushInterface.reportNotificationOpened(this, "1209569972");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);// 友盟
		StatService.onPause(this);
		JPushInterface.onPause(this);
	}

	private void yuleVersionRead() {

		// SharedPreferences sharepreferences=getSharedPreferences("myupdate",
		// 0);
		// Editor uedit=sharepreferences.edit();
		// uedit.putLong("unpdata",System.currentTimeMillis());//add by bobo
		// add by bobo
		// SharedPreferences sharepreferences=getSharedPreferences("myupdate",
		// 0);
		// Editor uedit=sharepreferences.edit();//end by bobo

		ReadHttpGet task = new ReadHttpGet(this);
		task.setListener(this);
		String k = String.valueOf(unpdataValue);
		Log.d("mylog", "获得--"
				+ "http://55mun.com:8022/Joke/v1.php?qt=Category1"
				+ "&timestamp=" + k);

		// task.execute(Constants.YULE_VER_URL+"&timestamp="+k, null, null,
		// getHeader());
		task.execute("http://55mun.com:8022/Joke/v1.php?qt=Category1"
				+ "&timestamp=" + k, null, null, getHeader());
		// http://55mun.com:8022/Joke/v1.php?qt=Category1
	}

	private void updateSubjectRedpoint() {
		mHandler.removeMessages(UPDATE_REDVIEW);
		mHandler.sendEmptyMessage(UPDATE_REDVIEW);
	}

	@Override
	public void onNoNetworkingRead() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNetworkingErrorRead() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostExecuteRead(byte[] bytes) {
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(new String(bytes));
			// int[] verNum = new int[6];

			if (jsonObject == null) {
				return;
			}

			SharedPreferences yuVer = MyApplication.getInstance()
					.getSharedPreferences("yulever", 0);
			SharedPreferences.Editor editor = yuVer.edit();

			String jsonStr = jsonObject.toString();
			JSONObject json = new JSONObject(jsonStr);
			JSONArray jsonArray = json.getJSONArray("data");

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = (JSONObject) jsonArray.get(i);
				// verNum[i] = obj.getInt("UPDATE");
				int v = obj.getInt("UPDATENUM");
				int v2 = obj.getInt("UPDATE");
				Log.d("mytest", "i==>" + i + " vv==>" + v);
				if (i == 0 && v2==1) {
					editor.putInt(MUSIC_KEY, v2);
				} else if (i == 1 && v2==1) {
					editor.putInt(TV_KEY, v2);
				} else if (i == 2 && v2==1) {
					editor.putInt(ANIME_KEY, v2);
				} else if (i == 3 && v2==1) {
					editor.putInt(RIDIAO_KEY, v2);
				} else if (i == 4 && v2 == 1) {
					editor.putInt(NOVEL_KEY, v2);
				} else if (i == 5 && v >= 1) {
					editor.putInt(JOKE_KEY, v);
				} else if (i == 6 && v2 >= 1) {
					editor.putInt(NEWS_KEY, v2);
				} else if (i == 7 && v2 >= 1) {
					editor.putInt(SHOW_KEY, v2);
				} else if (i == 8 && v2 >= 1) {
					editor.putInt(MV_KEY, v2);
				} else if(i == 9&& v>=1){
					editor.putInt(AV_KEY, v);
				} else if(i==10&&v>=1){
					editor.putInt(MVOE_KEY, v);
				}
			}
			editor.commit();
			Message msg = mHandler.obtainMessage();
			msg.what = INIT_RAPIT;
			mHandler.sendMessage(msg);
			// SharedPreferences txtNetResVer =
			// MyApplication.getInstance().getSharedPreferences("yulever", 0);
			// int net_ver = txtNetResVer.getInt(JOKE_KEY, 0);
			//

			// updateSubjectRedpoint();
		} catch (Exception e) {
			// TODO: handle exception
		}
	//	updateSubjectRedpointstart();//--------------------关闭底部红点
	}

	/* add Log change by zengxiao */

	private void logUpdate(String url) {
		SharedPreferences sharepreferences = getSharedPreferences(
				"mylogupdate", 0);
		Editor edit = sharepreferences.edit();
		/* add by zengxiao for logupdate */
		String urllog = sharepreferences.getString("urllog", "");
		if (!url.equals("") && !urllog.equals(url)) {
			ImageLoader imageLoader;
			DisplayImageOptions options;
			imageLoader = ImageLoader.getInstance();
			options = new DisplayImageOptions.Builder().cacheOnDisc().build();
			if (!imageLoader.getPause().get()) {
				imageLoader.displayImage(url, loadingbg, options);
			}
			edit.putString("urllog", url);
			edit.commit();
		}

	}

	// 糗百 点赞
	public boolean queryDataIsExists(String sid) {
		boolean isExists = false;

		// JokeDataDatabaseHelper dbHelper = new
		// JokeDataDatabaseHelper(JokeActivity.this, DATABASE_NAME,
		// DATABASE_VERSION);
		JokeDataDatabaseHelper dbHelper = JokeDataDatabaseHelper
				.getInstance(MainActivity.this);
		SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
		Cursor cursor = sqliteDatabase.query(DATABASE_NAME,
				new String[] { joke_feedback_list.JOKESID }, "sid=?",
				new String[] { sid }, null, null, null);
		while (cursor.moveToNext()) {
			sid = cursor.getString(cursor
					.getColumnIndex(joke_feedback_list.JOKESID));
			if (sid != null) {
				isExists = true;
			}
		}
		cursor.close();
		return isExists;
	}

	public void updateData(String sid, int comflag, int voteflag) {
		ContentValues values = new ContentValues();
		// 值为"1"时才写入数据库，"0"时则保持原数据
		if (comflag == 1) {
			values.put(joke_feedback_list.JOKECOMMFLAG, comflag);
		}
		if (voteflag == 1) {
			values.put(joke_feedback_list.JOKEVOTEFLAG, voteflag);
		}
		// JokeDataDatabaseHelper dbHelper = new
		// JokeDataDatabaseHelper(JokeActivity.this, DATABASE_NAME,
		// DATABASE_VERSION);

		JokeDataDatabaseHelper dbHelper = JokeDataDatabaseHelper
				.getInstance(MainActivity.this);
		SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
		sqliteDatabase.update(DATABASE_NAME, values, "sid=?",
				new String[] { sid });
	}

	public void insertData(String sid, int comflag, int voteflag) {
		ContentValues values = new ContentValues();
		values.put(joke_feedback_list.JOKESID, sid);
		// 值为"1"时才写入数据库，"0"时则保持原数据
		if (comflag == 1) {
			values.put(joke_feedback_list.JOKECOMMFLAG, comflag);
		}
		if (voteflag == 1) {
			values.put(joke_feedback_list.JOKEVOTEFLAG, voteflag);
		}
		// JokeDataDatabaseHelper dbHelper = new
		// JokeDataDatabaseHelper(JokeActivity.this, DATABASE_NAME,
		// DATABASE_VERSION);
		JokeDataDatabaseHelper dbHelper = JokeDataDatabaseHelper
				.getInstance(MainActivity.this);
		SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
		sqliteDatabase.insert(DATABASE_NAME, null, values);
	}

	// //end 糗百 点赞

	/* add end */
	private String getRequestUrlAd() {
		return Constants.LIST_URL + "?qt=" + Constants.PLINFO;
	}

	// add by bobo
	/**
	 * 记录本次登陆的时间
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// add by bobo
	}

//	private void showAd() {
//		SdkInit.currentActivity = this;
//		Intent intent = new Intent("com.android.ybx.adshow");
//		sendBroadcast(intent);
//	}
//
//	private void showClose() {
//		Intent intent = new Intent("com.android.ybx.adclose");
//		sendBroadcast(intent);
//	}

	private void testClom(){
		//创建Intent对象，action指向广播接收类，附加信息为字符串“你该打酱油了”  
		  
		Intent intent = new Intent("COM.MARKET.ALARM");  
		  
		//intent.putExtra("msg","你该打酱油了");  
		  
		//创建PendingIntent对象封装Intent，由于是使用广播，注意使用getBroadcast方法  
		  
		PendingIntent pi = PendingIntent.getBroadcast(this,0,intent,0);  
		  
		//获取AlarmManager对象  
		  
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);  
		  
		//设置闹钟从当前时间开始，每隔10分钟执行一次PendingIntent对象，注意第一个参数与第二个参数的关系  
		am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),12*60*60000,pi);   
		//am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),60000,pi);  
		
	}
	
	// 机关推送 别名设置 回掉接口
	private class Myjpushcallback implements TagAliasCallback {

		@Override
		public void gotResult(int arg0, String arg1, Set<String> arg2) {
			// TODO Auto-generated method stub
			Log.d("nnlog", "--jpush--" + arg0);
		}

	}

	@Override
	public void initComplete(String arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.d("nnlog", arg0+"--bulerpay--");
		if("200".equals(arg0)){
			MyApplication.getInstance().setBulepayInit(true);
		}
		
	}
}
