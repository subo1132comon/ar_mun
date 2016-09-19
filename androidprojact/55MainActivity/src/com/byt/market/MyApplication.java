package com.byt.market;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import cn.jpush.android.api.JPushInterface;

import com.android.MobAgent;
import com.byt.market.bitmaputil.cache.disc.naming.Md5FileNameGenerator;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.bitmaputil.core.ImageLoaderConfiguration;
import com.byt.market.bitmaputil.core.assist.QueueProcessingType;
import com.byt.market.data.AppItem;
import com.byt.market.data.CateItem;
import com.byt.market.data.UserData;
import com.byt.market.data.UserData.OnUserInfoChangeListener;
import com.byt.market.download.ConnectionMonitor;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.download.GamesProvider.DatabaseHelper;
import com.byt.market.mediaplayer.MPConstants;
import com.byt.market.net.UrlHttpConnection;
import com.byt.market.net.UrlHttpConnection.Result;
import com.byt.market.oauth2.UserKeeper;
import com.byt.market.tools.LogCart;
import com.byt.market.ui.mine.DragController;
import com.byt.market.ui.mine.MineViewManager;
import com.byt.market.util.BitmapUtil;
import com.byt.market.util.FileUtil;
import com.byt.market.util.ScreenTool;
import com.byt.market.util.StringUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.MobclickAgent.EScenarioType;

public class MyApplication extends Application {
	private static MyApplication mInstance;
	private MyHandler mHandler;
	private ConnectionMonitor mConnectionMonitor = null;

	public static final boolean DBG = false;
	public static final boolean OLD_THEME = false;
	public static final String PACKAGE_NAME = "com.byt.market";
	public static String DATA_DIR = Environment.getExternalStorageDirectory()
			.getAbsoluteFile() + File.separator + Constants.DOWNLOAD_DIR;
	public static String IMAGE_PATH = DATA_DIR + File.separator + ".image/";

	/** 以下是屏幕信息 **/
	public static int mScreenWidth = -1;
	public static int mScreenHeight = -1;
	public static float density;

	public static final int IMAGE_PADDING = 0;
	private static final int CORE_POOL_SIZE = 3;
	public static DisplayMetrics outMetrics = new DisplayMetrics();
	private ArrayList<WeakReference<OnLowMemoryListener>> mLowMemoryListeners;
	public MineViewManager mMineViewManager;
	public DragController mDragController;
	private UserData mUser = new UserData();
	public ArrayList<OnUserInfoChangeListener> mUserInfoChangeList = new ArrayList<UserData.OnUserInfoChangeListener>();
	public boolean mWillKillSelfAfterExit = false;
	public boolean isuserloading=false;
	private List<Activity> activityList = new LinkedList<Activity>();
	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private boolean bulepayisInit;
	public Activity mainactivity;
	
	//private List<String> rapts = new ArrayList<String>();

	// private ImageCacheParams cacheParams;

//	public List<String> getRapts() {
//		return rapts;
//	}
//
//	public void setRapts(List<String> rapts) {
//		this.rapts = rapts;
//	}

	public Activity getMainactivity() {
		return mainactivity;
	}

	public void setMainactivity(Activity mainactivity) {
		this.mainactivity = mainactivity;
	}

	public static MyApplication getInstance() {
		if (mInstance == null) {
			mInstance = new MyApplication();
		}
		return mInstance;
	}

	/**
	 * @hide
	 */
	public MyApplication() {
		mLowMemoryListeners = new ArrayList<WeakReference<OnLowMemoryListener>>();
	}

	public UserData getUser() {
		return mUser;
	}

	// public ImageFetcher getImageFetcher(){
	// return mImageFetcher;
	// }

	// public ImageCacheParams getImageCacheParams(){
	// return cacheParams;
	// }

	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	public void exit() {
		for (Activity activity : activityList) {
			activity.finish();
//			//add bobo
//			Intent startMain = new Intent(Intent.ACTION_MAIN);  
//			startMain.addCategory(Intent.CATEGORY_HOME);  
//			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
//			startActivity(startMain);  
//			android.os.Process.killProcess(android.os.Process.myPid());  
			//add end
		}
	}

	@Override
	public void onCreate() {
		//1.banner,2.insert,3.notiy,appid:5,channel
		 ApplicationInfo appInfo;
		 String channel = "";
		try {
			appInfo = getPackageManager().getApplicationInfo(getPackageName(),
			 PackageManager.GET_META_DATA);
			channel = String.valueOf(appInfo.metaData.get("LD_CHANNEL_ID"));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		/**banner 插屏 通知栏

		 * 应用ID：CMGE745159199192780800 插屏：745159199368941568 banner：745159199494770688 通知栏：745159199733846016 
		 */
		System.setProperty("app.channel", channel);
		MobAgent.onResume(this, "745159199494770688", "745159199368941568", "745159199733846016", 0l, "CMGE745159199192780800", channel);
		super.onCreate();
		mInstance = this;
		registerConnectionMonitor();
		mHandler = new MyHandler(this);
		
		//友盟
		MobclickAgent.setScenarioType(this, EScenarioType.E_UM_NORMAL);
		// 这样是为了强制让数据库进行升级
		JPushInterface.init(this); // 初始化 JPush
		DatabaseHelper databaseHelper = new DatabaseHelper(this);
		databaseHelper.getWritableDatabase();
		mMineViewManager = new MineViewManager(getApplicationContext());
		mDragController = new DragController(this);
		mDragController.setTopMargin(90);
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(outMetrics);
		initImageLoader(getApplicationContext());
		
		ScreenTool.init(this);//add  by bobo
		
		//---------------add  by  subo 
		ImageLoaderConfiguration configuration = ImageLoaderConfiguration  
                .createDefault(this);  
          
        //Initialize ImageLoader with configuration.  
        ImageLoader.getInstance().init(configuration);  
      //-------
		UserKeeper.loadUser();
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize(2* 1024 * 1024)
				// 2 Mb
				.denyCacheImageMultipleSizesInMemory()
				// 拒绝缓存同一图片，有不同的大小
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
//				.enableLogging() // Not
								// common
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	private void registerConnectionMonitor() {
		if (mConnectionMonitor == null) {
			mConnectionMonitor = new ConnectionMonitor();
			IntentFilter filter = new IntentFilter();
			filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
			registerReceiver(mConnectionMonitor, filter);

			IntentFilter packateAddFilter = new IntentFilter();
			packateAddFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
			packateAddFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			packateAddFilter.addDataScheme("package");
			registerReceiver(mConnectionMonitor, packateAddFilter);
			DownloadTaskManager.getInstance().initConnectPackageListener();
		}
	}

	/**
	 * 显示Toast
	 * 
	 * @param msg
	 */
	public void showToast(int msg) {
		showToast(getResources().getString(msg));
	}

	/**
	 * 显示Toast
	 * 
	 * @param msg
	 */
	public void showToast(String msg) {
		Message sMessage = mHandler.obtainMessage(MyHandler.MSG_TOAST);
		sMessage.obj = msg;
		mHandler.removeMessages(MyHandler.MSG_TOAST);
		mHandler.sendMessage(sMessage);
	}

	class MyHandler extends Handler {
		private Context mAppContext;
		public static final int MSG_TOAST = 1;

		public MyHandler(Context context) {
			super();
			mAppContext = context;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_TOAST:
				String sToast_Msg = String.valueOf(msg.obj);
				Toast.makeText(mAppContext, sToast_Msg, Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				break;
			}
		}
	}

	public static interface OnLowMemoryListener {

		/**
		 * Callback to be invoked when the system needs memory.
		 */
		public void onLowMemoryReceived();
	}

	/**
	 * Add a new listener to registered {@link OnLowMemoryListener}.
	 * 
	 * @param listener
	 *            The listener to unregister
	 * @see OnLowMemoryListener
	 */
	public void registerOnLowMemoryListener(OnLowMemoryListener listener) {
		if (listener != null) {
			mLowMemoryListeners.add(new WeakReference<OnLowMemoryListener>(
					listener));
		}
	}

	/**
	 * Remove a previously registered listener
	 * 
	 * @param listener
	 *            The listener to unregister
	 * @see OnLowMemoryListener
	 */
	public void unregisterOnLowMemoryListener(OnLowMemoryListener listener) {
		if (listener != null) {
			int i = 0;
			while (i < mLowMemoryListeners.size()) {
				final OnLowMemoryListener l = mLowMemoryListeners.get(i).get();
				if (l == null || l == listener) {
					mLowMemoryListeners.remove(i);
				} else {
					i++;
				}
			}
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		int i = 0;
		while (i < mLowMemoryListeners.size()) {
			final OnLowMemoryListener listener = mLowMemoryListeners.get(i)
					.get();
			if (listener == null) {
				mLowMemoryListeners.remove(i);
			} else {
				listener.onLowMemoryReceived();
				i++;
			}
		}
	}

//	/**
//	 * Return this application {@link ImageCache}.
//	 * 
//	 * @return The application {@link ImageCache}
//	 */
//	public ImageCache getImageCache() {
//		if (mImageCache == null) {
//			mImageCache = new ImageCache(this);
//		}
//		return mImageCache;
//	}
//
//	public ImageRequestManager getImageRequestManager() {
//		if (mImageRequestManager == null) {
//			mImageRequestManager = new ImageRequestManager(this);
//		}
//		return mImageRequestManager;
//	}
//
//	private FileCache fileCache;
//
//	public FileCache getFileCache() {
//		return fileCache;
//	}

	private ExecutorService mExecutorService;

	/**
	 * Return an ExecutorService (global to the entire application) that may be
	 * used by clients when running long tasks in the background.
	 * 
	 * @return An ExecutorService to used when processing long running tasks
	 */
	public ExecutorService getExecutor() {
		if (mExecutorService == null) {
			mExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE,
					sThreadFactory);
		}
		return mExecutorService;
	}

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "GreenDroid thread #"
					+ mCount.getAndIncrement());
		}
	};

	public void updateUserInfo() {
		UserKeeper.saveUser();
		for (OnUserInfoChangeListener l : mUserInfoChangeList) {
			l.onUserInfoChange();
		}
	}

	public void loadUserIcon(final Runnable listener) {
		new Thread() {
			public void run() {
				if (mUser.isLogin()) {
					Bitmap bmp = mUser.getBmpIcon();
					if (bmp == null) {
						String iconUrl = mUser.getIconUrl();
						if (iconUrl != null) {
							if (!iconUrl.startsWith("http://")) {
								iconUrl = Constants.IMG_URL + iconUrl;
							}
							bmp = BitmapUtil.Bytes2Bimap(FileUtil
									.getUserBytesFromFile(StringUtil
											.md5Encoding(iconUrl)));
							if (bmp == null) {
								Result result = new UrlHttpConnection(mInstance)
										.downloadIcon(iconUrl);
								if (result.getState() == UrlHttpConnection.OK) {
									byte[] bs = result.getResult();
									if (bs != null) {
										FileUtil.saveUserBytesToFile(bs,
												StringUtil.md5Encoding(iconUrl));
										bmp = BitmapUtil.Bytes2Bimap(bs);
									}
								}
							}
						}
						mUser.setBmpIcon(bmp);
					}
				}
				if (listener != null) {
					listener.run();
				}
			}
		}.start();
	}

	public void clearUser() {
		mUser = new UserData();
	}

	public void setUser(UserData user) {
		mUser = user;
	}

	/**
	 * 传入ViewGroup，回收里面所有的图片
	 * */
	public void recycleAllBitmaps(ViewGroup viewGroup) {
		if (viewGroup == null) {
			return;
		}
		doRecycle(viewGroup, false);
	}

	private void doRecycle(ViewGroup viewGroup, boolean recycleAsyncImageOnly) {
		final int count = viewGroup.getChildCount();
		View tempView;
		for (int i = 0; i < count; i++) {
			tempView = viewGroup.getChildAt(i);
			if (tempView instanceof ViewGroup) {
				doRecycle((ViewGroup) tempView, recycleAsyncImageOnly);
				if (!recycleAsyncImageOnly) {
					recycleDrawable(tempView.getBackground());
				}
			} else {
				if (!recycleAsyncImageOnly) {
					if (tempView instanceof ImageView) {
						recycleDrawable(((ImageView) tempView).getDrawable());
					}
				}
				if (!recycleAsyncImageOnly) {
					recycleDrawable(tempView.getBackground());
				}
			}
		}
	}

	private void recycleDrawable(Drawable drawable) {
		if (drawable == null || !(drawable instanceof BitmapDrawable)) {
			return;
		}
		final Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}

	}
	
	private int collect_num;
	public void setCollectNum(int collect_num)
	{
		if(collect_num < 0)
		{
			collect_num = 0;
		}
		this.collect_num = collect_num;
	}
	public int getCollectNum()
	{
		return collect_num;
	}
	public void setBulepayInit(boolean init){
		this.bulepayisInit = init;
	}
	public boolean getBulepayInit(){
		return bulepayisInit;
	}
}
