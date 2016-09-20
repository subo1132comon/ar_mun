package com.byt.market.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.data.AppItem;
import com.byt.market.view.CustomToast;

/**
 * 已安装游戏提示
 */
public class MyInstalledToastService extends Service {

	private WindowManager winManager;
	private LayoutParams layoutParams;
	private CustomToast customToast;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		}
		winManager = (WindowManager) this.getApplicationContext().getSystemService("window");
		AppItem appItem = (AppItem) intent.getParcelableExtra(Constants.ExtraKey.APP_ITEM);
		//showToast(appItem);
		return super.onStartCommand(intent, flags, startId);
	}

	private void showToast(AppItem appItem) {
		Display mDisplay = winManager.getDefaultDisplay();
		int screenWidth = mDisplay.getWidth();
		layoutParams = new WindowManager.LayoutParams();
		layoutParams.type = 2003;
		layoutParams.format = PixelFormat.RGBA_8888;
		layoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
		layoutParams.y = 50;
		layoutParams.width = LayoutParams.FILL_PARENT;
		layoutParams.height = LayoutParams.WRAP_CONTENT;
		customToast = (CustomToast) LayoutInflater.from(this).inflate(R.layout.toast_installled, null);
		winManager.addView(customToast, layoutParams);
		customToast.showToast(appItem);
	}

}
