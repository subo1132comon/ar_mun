package com.byt.market.download;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.byt.market.Constants;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.service.MyInstalledToastService;
import com.byt.market.util.SharedPrefManager;

public class ConnectionMonitor extends BroadcastReceiver {

	private static ArrayList<ConnectionListener> sListtener = new ArrayList<ConnectionListener>();

	public static void registerConnectionMonitor(ConnectionListener listener) {
		if (!sListtener.contains(listener)) {
			sListtener.add(listener);
		}
	}

	public static void unregisterConnectionMonitor(ConnectionListener listener) {
		if (sListtener.contains(listener)) {
			sListtener.remove(listener);
		}
	}

	private void notification(final int state) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				int length = sListtener.size();
				DownloadLog.debug(ConnectionMonitor.class, "length=" + length
						+ "  state=" + state);

				try {
					for (int i = 0; i < length; i++) {
						sListtener.get(i).connectionStateChanged(state);
					}
				} catch (Exception e) {
				}
			}
		}).start();
	}

	private void notifyPackageChanged(final String pkn, final int action) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				int length = sListtener.size();
				try {
					for (int i = 0; i < length; i++) {
						sListtener.get(i).onPackageChanged(pkn, action);
					}
				} catch (Exception e) {
				}
			}
		}).start();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		DownloadLog.debug(ConnectionMonitor.class, "ConnectionMonitor");
		final String action = intent.getAction();
		if (android.net.ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			int state = NetworkStatus.getInstance(context).getNetWorkState();
			notification(state);
		} else if (Intent.ACTION_PACKAGE_ADDED.equals(action)
				|| Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
			String packageName = intent.getDataString();
			if (packageName != null && packageName.contains(":")) {
				packageName = packageName.subSequence(
						packageName.indexOf(":") + 1, packageName.length())
						.toString();
			}
			int packageAction = ConnectionListener.ACTION_PACKAGE_ADD;
			if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
				packageAction = ConnectionListener.ACTION_PACKAGE_REMOVED;
			}
			notifyPackageChanged(packageName, packageAction);
			if (packageAction == ConnectionListener.ACTION_PACKAGE_ADD) {
				if (!SharedPrefManager.isInstalledNotification(context)) {
					return;
				}
				DownloadTask downloadTask = DownloadTaskManager.getInstance()
						.getDownloadTaskByPackageName(packageName);
				if (downloadTask == null) {
					return;
				}
				Intent intent2 = new Intent(context,
						MyInstalledToastService.class);
				intent2.putExtra(Constants.ExtraKey.APP_ITEM,
						downloadTask.mDownloadItem);
				context.startService(intent2);
				
				if(SharedPrefManager.isInstalledDeleteApk(context)){
					DownloadTaskManager.getInstance().deleteDownlaodFile(downloadTask.mDownloadItem);
				}
			}
		}

	}

}
