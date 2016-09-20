package com.byt.market.activity;

import java.io.File;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;

import com.byt.market.ActivityInit;
import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.data.MarketUpdateInfo;
import com.byt.market.service.UpdateDownloadService;
import com.byt.market.util.InstallUtil;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.Util;


/**
 * 主要是提供带前后界面切换的基础类
 */
public abstract class MenuBaseActivity extends FragmentActivity implements ActivityInit {

	//protected SlidingMenu slidingMenu;

	/** 当前的TAB **/
	public static int currentView;
	/** 上一个打开的TAB，以便在选择新的TAB时将状态还原 **/
	public static int lastFrame;
	/** 是否当前在主框架界面上 **/
	public static boolean isMainFrame;
	public MarketContext marketContext;
	private File downFile;

	public int getCurrentView() {
		return currentView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().mWillKillSelfAfterExit = false;
		marketContext = MarketContext.getInstance();
		if (!MarketContext.isDataOk) {
			marketContext.initData(this);
		}

		//setBehindContentView(R.layout.settings);
		//slidingMenu = getSlidingMenu();
	//	slidingMenu.setMode(SlidingMenu.LEFT);
//		slidingMenu.setShadowDrawable(R.drawable.shadow);
		//slidingMenu.setShadowWidthRes(R.dimen.sliding_menu_shadow_width);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		//slidingMenu.setBehindOffset((int) (dm.widthPixels * 0.14));
		//slidingMenu.setFadeEnabled(true);
		//slidingMenu.setFadeDegree(0.4f);
		//slidingMenu.setBehindScrollScale(0);
	//	slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	public void showFragmentView(int currentFrame) {
		
		onBeforeShow(currentFrame);
		showContentView(currentFrame);
		onAfterShow(currentFrame);
	}

	public void showContentView(int currentFrame) {

	}

	public void onBeforeShow(int currentFrame) {

	}

	public void onAfterShow(int currentFrame) {

	}

	/**
	 * 市场更新提示
	 */
	protected void handleMarketUpdateNotify() {
		final MarketUpdateInfo marketUpdateInfo = Util.update;
		if (marketUpdateInfo == null)
			return;
		if (marketUpdateInfo != null) {
			/*
			 * if (marketUpdateInfo.right == 1) { new Thread() { public void run() { startDown(Util.update.apk); } }.start(); }
			 * else
			 */
			showNoRootUpdate(marketUpdateInfo);
		}
	}

	public void showNoRootUpdate(MarketUpdateInfo marketUpdateInfo) {
	};

	public void updateNow(MarketUpdateInfo marketUpdateInfo) {
		if (apkPath.startsWith("http://")) {
			apkPath = marketUpdateInfo.apk;
		}else{
			apkPath = Constants.APK_URL + marketUpdateInfo.apk;
		}
		if (apkPath != null) {
			Intent intent = new Intent(this, UpdateDownloadService.class);
			startService(intent); //
			// 如果先调用startService,则在多个服务绑定对象调用unbindService后服务仍不会被销毁
			if (binder == null)
				bindService(intent, conn, Context.BIND_AUTO_CREATE);
			else
				binder.start(apkPath);
		}
	}

	protected UpdateDownloadService.DownloadBinder binder;
	protected String apkPath;
	protected ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (UpdateDownloadService.DownloadBinder) service;
			binder.start(apkPath);
		}

		public void onServiceDisconnected(ComponentName name) {
		}
	};

	public void cancel(View view) {
		if (null != binder && !binder.isCancelled()) {
			binder.cancel();
		}
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			showNoRootUpdate(Util.update);
		};
	};

	public void startDown(String apkPath) {
		if (apkPath == null)
			return;
		if (apkPath.startsWith("http://")) {
		}else{
			apkPath = Constants.APK_URL + apkPath;
		}
		try {
			if ((downFile = Util.startDownload(getApplicationContext(), apkPath)) != null) {
				if (!downFile.exists())
					return;
				PackageUtil.chmod(downFile.getAbsolutePath(), "777");
				if (!InstallUtil.installData(downFile)) {
					handler.sendEmptyMessage(0);
				}
			}
		} catch (Exception e) {
			downFile.delete();
			e.printStackTrace();
		}
	}
}
