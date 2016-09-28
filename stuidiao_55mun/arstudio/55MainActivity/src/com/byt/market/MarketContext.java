package com.byt.market;

import java.util.LinkedList;

import android.content.Context;
import android.os.Message;

import com.byt.market.activity.base.BaseActivity;
import com.byt.market.util.DataUtil;

public class MarketContext {

	private static LinkedList<BaseActivity> mActivitys = new LinkedList<BaseActivity>();
	private static MarketContext instance;
	private boolean isAlive;
	public boolean isGalleryMove = false;
	private Context context;
	private DataUtil dUtil;
	public static boolean isDataOk = false;

	private MarketContext() {
		isAlive = true;
	}

	public static MarketContext getInstance() {
		synchronized (mActivitys) {
			if (instance == null) {
				instance = new MarketContext();
			}
			return instance;
		}
	}

	public void initData(Context context) {
		this.context = context;
		dUtil = DataUtil.getInstance(context);
		isDataOk = true;
	}

	public boolean isAlive() {
		return this.isAlive;
	}

	public void add(BaseActivity activity) {
		mActivitys.addFirst(activity);
	}

	public void remove(BaseActivity activity) {
		mActivitys.remove(activity);
	}

	public void handleMarketMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	// public void refresh(){
	// for (BaseActivity item : mActivitys) {
	// item.onUiRefresh();
	// }
	// }

	public void opData(int type, int tab, Object obj) {
		Object[] ojbs = new Object[2];
		ojbs[0] = tab;
		ojbs[1] = obj;
		dUtil.handleDatabase(type, ojbs);
	}

	public static boolean isLogin() {
		return MyApplication.getInstance().getUser().isLogin();
	}
}
