package com.byt.market.receiver;

import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.byt.market.MyApplication;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.util.Util;


/**
 * 网络变化监听
 */
public class ConnectionChangeReceiver extends BroadcastReceiver implements
		TaskListener, DownloadTaskListener {
	private ConnectivityManager connectivityManager;
	private Context context;
	private ProtocolTask protocolTask;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null) {
			DownloadTaskManager.getInstance().networkIsOk();
			if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				// 3G网络
			} else if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				// WIFI网络
//				if (!SharedPrefManager.isNeedSyncFromServer(context)) {
//					DownloadTaskManager.getInstance().addListener(this);
//					SharedPrefManager.setCurrentSilentDownloadLimitSizeOn3G(
//							context, 0);
//					SharedPrefManager.setSyncFromServer(context);
//					syncDataFromServer();
//				}
			}

		}
	}


	public HashMap<String, String> getHeader() {
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		if (imei == null)
			imei = Util.getIMEI(context);
		if (vcode == null)
			vcode = Util.getVcode(context);
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(MyApplication.getInstance());
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("channel", channel);
		return map;
	}


	@Override
	public void onNoNetworking() {
	}

	@Override
	public void onNetworkingError() {
	}

	@Override
	public void onPostExecute(byte[] bytes) {
	}


	@Override
	public void startConnecting(DownloadTask task, int totalTask,
			int progressTask) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadStarted(DownloadTask task, int totalTask,
			int progressTask, long totalSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadProgress(DownloadTask task, int totalTask,
			int progressTask, long progressSize, long totalSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadEnded(DownloadTask task, int totalTask, int progressTask) {
	}

	@Override
	public void endConnecting(DownloadTask task, int totalTask,
			int progressTask, DownloadException result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refreshUI() {
		// TODO Auto-generated method stub

	}

	@Override
	public void installedSucess(DownloadTask downloadTask) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unInstalledSucess(String packageName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void networkIsOk() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void installedSucess(String packageName) {
		// TODO Auto-generated method stub
		
	}
}
