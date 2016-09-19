package com.byt.market.activity.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.byt.market.ActivityInit;
import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.MenuBaseActivity;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.bitmaputil.core.display.RoundedBitmapDisplayer;
import com.byt.market.bitmaputil.core.display.SimpleBitmapDisplayer;

public abstract class BaseActivity extends FragmentActivity implements
		ActivityInit {
	protected static String TAG = "BaseActivity";
	public MarketContext maContext;
	public SharedPreferences sp;
	public Editor editor;
	private ProgressDialog progressDialog;
	private MyApplication mAPP;
	protected ImageLoader imageLoader;
	protected DisplayImageOptions options;
	public Toast toast;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		mAPP = MyApplication.getInstance();
		MyApplication.getInstance().mWillKillSelfAfterExit = false;
		maContext = MarketContext.getInstance();
//		StatService.trackCustomEvent(this, "onCreate", "");
//		StatConfig.setAutoExceptionCaught(true);
		MenuBaseActivity.isMainFrame = false;
		if (!MarketContext.isDataOk) {
			maContext.initData(this);
			MarketContext.isDataOk = true;
		}
//		mAPP.addActivity(this);
		initImageLoader();
	}

	private void initImageLoader() {
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
				.cacheOnDisc().delayBeforeLoading(200)
				.displayer(new SimpleBitmapDisplayer()).build();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Constants.Settings.readSettings(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (imageLoader != null) {
			imageLoader.clearMemoryCache();
//			imageLoader.clearDiscCache();
		}
		System.gc();
	}

	public void showProgressDialog(String msg) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(msg);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	public void dismissProgressDialog() {
		progressDialog.dismiss();
	}

	@Override
	protected void onResume() {
		super.onResume();
//		StatService.onResume(this);
//		MobclickAgent.onPageStart(this.getClass().getSimpleName());
//		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
//		StatService.onPause(this);
//		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
//		MobclickAgent.onPause(this);
	}

	/**
	 * 启动新activity
	 * 
	 * @param intent
	 */
	public void startNewWindow(Intent intent) {
		startActivity(intent);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	/**
	 * 关闭当前activity
	 */
	public void finishWindow() {
		finish();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	/*
	 * 处理内存空间不足
	 * 
	 * @see android.app.Activity#onLowMemory()
	 */
	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	/**
	 * 创建对话框
	 * 
	 * @param title
	 *            标题
	 * @param msg
	 *            内容
	 * @return
	 */
	public Dialog createDialog(String title, String msg) {
		return new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
				.setIcon(R.drawable.icon).create();
	}

	/**
	 * 关闭对话框
	 * 
	 * @param d
	 */
	public void dismissDialog(Dialog d) {
		if (d != null && d.isShowing()) {
			d.dismiss();
		}
	}

	public void showShortToast(String msg) {
		if (toast == null) {
			toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		} else {
			toast.setText(msg);
			toast.setDuration(Toast.LENGTH_SHORT);
		}
		toast.show();
	}

	public void showLongToast(String msg) {
		if (toast == null) {
			toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		} else {
			toast.setText(msg);
			toast.setDuration(Toast.LENGTH_LONG);
		}
		toast.show();
	}
}
