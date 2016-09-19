package com.byt.market.net;

import java.util.Locale;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author qiuximing
 */
public class NetworkUtil {

	/**
	 * 网络类型(全部小写)
	 */
	private static String mNetworkType = "";

	/**
	 * 判断网络是否连接
	 * 
	 * @param context
	 *            环境对象
	 * @return true 有网络，false 无网络
	 */
	public static boolean isNetWorking(Context context) {
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = cm.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isAvailable()) {
				if (networkInfo.getTypeName().equals("WIFI")) {
					mNetworkType = networkInfo.getTypeName().toLowerCase(
							Locale.ENGLISH);
				} else {
					if (networkInfo.getExtraInfo() == null) {
						mNetworkType = "";
					} else {
						mNetworkType = networkInfo.getExtraInfo().toLowerCase(
								Locale.ENGLISH);
					}
				}
				return true;
			} else {
				mNetworkType = "";
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public static void startNetSetting(Context context) {
		Intent intent = null;
		// 判断手机系统的版本 即API大于10 就是3.0或以上版本
		if (android.os.Build.VERSION.SDK_INT > 10) {			  
			intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		} else {
			intent = new Intent();
			 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			ComponentName component = new ComponentName("com.android.settings",
					"com.android.settings.WirelessSettings");
			intent.setComponent(component);
			intent.setAction("android.intent.action.VIEW");
			 
		}
		context.startActivity(intent);
	}

	/**
	 * 获取网络类型
	 * 
	 * @return 网络类型
	 */
	public static String getNetworkType() {
		return mNetworkType;
	}
}
