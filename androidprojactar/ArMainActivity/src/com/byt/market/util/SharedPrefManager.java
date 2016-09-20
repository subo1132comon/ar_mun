package com.byt.market.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.util.Log;

import com.byt.market.Constants;

/**
 * 本地配置工具类
 * */
public class SharedPrefManager {

	public static final String UPDATE_PUSH_TIME = "updatetime";
	/** 非WiFi网络下载提示 **/
	public static final String DOWNLOAD_TIPS_IN_NON_WIFI_ENVIRONMENT = "download_tips_in_non_wifi_environment";
	/** 显示图标和截图 **/
	public static final String IS_DISPLAY_ICON_SCREENSHOT = "is_display_icon_screenshot";
	/** 下载完成立即进行安装 **/
	public static final String IS_DOWNLOAD_IMMEDIATELY_INSTALL = "is_download_immediately_install";
	/** ROOT用户快速安装 **/
	public static final String IS_QUICK_INSTALLATION_IN_ROOT_USER = "is_quick_installation_in_root_user";
	/** 安装后删除安装包 **/
	public static final String IS_INSTALLED_DELETE_APK = "is_installed_delete_apk";
	/** 安装完成提示 **/
	public static final String IS_INSTALLED_NOTIFICATION = "is_installed_notification";
	/** 同时下载数量 **/
	public static final String CURRENT_DOWNLOAD_SUM = "current_download_sum";
	/** 大家一起玩 启动时间间隔 **/
	public static final String SHARE_START_REC_REPEAT_TIME = "start_rec_repeat_time";
	/** 大家一起完启动时间 **/
	public static final String SHARE_START_REC_TIME = "start_rec_time";
	/** 上次登录的用户名,在切换账号时需要记录它 **/
	public static final String LAST_LOGIN_USER_NAME = "last_login_user_name";
	public static final String UID = "uid";

	// ********************************辅助方法(start)***************************************

	public static SharedPreferences getAppPreferences(final Context context) {
		return context.getSharedPreferences(Constants.SETTINGS_PREF,
				Context.MODE_PRIVATE);
	}

	/**
	 * 写入软件配置信息
	 * 
	 * @param ctx
	 *            当前上下文
	 * @param fileName
	 *            文件名
	 * @param key
	 *            信息键
	 * @param value
	 *            信息值
	 */
	public static void setString(Context context, String key, String value) {
		try {
			Editor er = getPreferenceEditor(context);
			er.putString(key, value);
			er.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 布尔值
	public static void setBoolean(Context context, String key, boolean value) {
		try {
			Editor er = getPreferenceEditor(context);
			er.putBoolean(key, value);
			er.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 长整型值
	public static void setLong(Context context, String key, long value) {
		try {
			Editor er = getPreferenceEditor(context);
			er.putLong(key, value);
			er.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 整型值
	public static void setInt(Context context, String key, int value) {
		try {
			Editor er = getPreferenceEditor(context);
			er.putInt(key, value);
			er.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getInt(Context context, String key, int defValue) {
		return getAppPreferences(context).getInt(key, defValue);
	}

	public static long getLong(Context context, String key, long defValue) {
		return getAppPreferences(context).getLong(key, defValue);
	}

	public static boolean getBoolean(Context context, String key,
			boolean defValue) {
		return getAppPreferences(context).getBoolean(key, defValue);
	}

	public static String getString(Context context, String key, String defValue) {
		return getAppPreferences(context).getString(key, defValue);
	}

	private static Editor getPreferenceEditor(Context context) {
		SharedPreferences pref = getAppPreferences(context);
		Editor er = pref.edit();
		return er;
	}

	// ******************************辅助方法(end)*****************************************

	public static long getPushtime(Context context) {
		return getLong(context, UPDATE_PUSH_TIME, Util.UPDATERULETIME);
	}

	public static void setPushTime(Context context, long updatetime) {
		setLong(context, UPDATE_PUSH_TIME, updatetime);
	}

	/**
	 * 非WiFi网络下载提示
	 */
	public static boolean getDownloadTipsInNonWifiEnvironment(Context context) {
		return getBoolean(context, DOWNLOAD_TIPS_IN_NON_WIFI_ENVIRONMENT, true);
	}

	public static void setDownloadTipsInNonWifiEnvironment(Context context,
			boolean value) {
		setBoolean(context, DOWNLOAD_TIPS_IN_NON_WIFI_ENVIRONMENT, value);
	}

	/**
	 * 显示图标和截图
	 */
	public static boolean isDisplayIconScreenshot(Context context) {
		return getBoolean(context, IS_DISPLAY_ICON_SCREENSHOT, true);
	}

	public static void setDisplayIconScreenshot(Context context, boolean value) {
		setBoolean(context, IS_DISPLAY_ICON_SCREENSHOT, value);
	}

	/**
	 * 下载完成立即进行安装
	 */
	public static boolean isDownloadImmediatelyInstall(Context context) {
		return getBoolean(context, IS_DOWNLOAD_IMMEDIATELY_INSTALL, true);
	}

	public static void setDownloadImmediatelyInstall(Context context,
			boolean value) {
		setBoolean(context, IS_DOWNLOAD_IMMEDIATELY_INSTALL, value);
	}

	/**
	 * ROOT用户快速安装
	 */
	public static boolean isQuickInstallationInRootUser(Context context) {
		return getBoolean(context, IS_QUICK_INSTALLATION_IN_ROOT_USER, true);
	}

	public static void setQuickInstallationInRootUser(Context context,
			boolean value) {
		setBoolean(context, IS_QUICK_INSTALLATION_IN_ROOT_USER, value);
	}

	/**
	 * 安装后删除安装包
	 */
	public static boolean isInstalledDeleteApk(Context context) {
		return getBoolean(context, IS_INSTALLED_DELETE_APK, true);
	}

	public static void setInstalledDeleteApk(Context context, boolean value) {
		setBoolean(context, IS_INSTALLED_DELETE_APK, value);
	}

	/**
	 * 安装完成提示
	 */
	public static boolean isInstalledNotification(Context context) {
		return getBoolean(context, IS_INSTALLED_NOTIFICATION, true);
	}

	public static void setInstalledNotification(Context context, boolean value) {
		setBoolean(context, IS_INSTALLED_NOTIFICATION, value);
	}

	/**
	 * 同时下载数量
	 */
	public static int getCurrentDownloadSum(Context context) {
		return getInt(context, CURRENT_DOWNLOAD_SUM, 2);
	}

	public static void setCurrentDownloadSum(Context context, int downSum) {
		setInt(context, CURRENT_DOWNLOAD_SUM, downSum);
	}

	/**
	 * 记录上次启动时间
	 */
	public static long getRecStartTime(Context context) {
		long time = getAppPreferences(context).getLong(SHARE_START_REC_TIME, 0);
		return time;
	}

	public static void setRecStartTime(Context context, long time) {
		setLong(context, SHARE_START_REC_TIME, time);
	}

	/**
	 * 是否需要与服务端进行数据同步,每天只请求一次
	 */
	public static long getRecRepeatTime(Context context) {
		long time = getAppPreferences(context).getLong(
				SHARE_START_REC_REPEAT_TIME, 0);
		return time;
	}

	public static void setRecRepeatTime(Context context, long time) {
		setLong(context, SHARE_START_REC_REPEAT_TIME, time);
	}

	/**
	 * 上次登录的用户名,在切换账号时需要记录它
	 */
	public static String getLastLoginUserName(Context context) {
		String lastUserName = getAppPreferences(context).getString(
				LAST_LOGIN_USER_NAME, "");
		return lastUserName;
	}
	
	//add  by  bobo
	public static String getLastUid(Context context) {
		String lastUserName = getAppPreferences(context).getString(
				UID, "");
		return lastUserName;
	}

	public static void setLastLoginUserName(Context context, String lastUserName) {
		setString(context, LAST_LOGIN_USER_NAME, lastUserName);
	}
	public final static String PID="PID:";
	public final static String CID="CID:";
	public static HashMap<String, String> getClintInfo(Context context){
		AssetManager assetManager = context.getAssets();
		InputStream inputStream = null;
		byte buf[] = new byte[1024];
		int len;
		StringBuffer sb = new StringBuffer();
		HashMap<String,String> infos=new HashMap<String, String>();
		try {
			inputStream = assetManager.open("config.txt");
			while ((len = inputStream.read(buf)) != -1) {
				String str = new String(buf, 0, len);
				sb.append(str);
			}
			String var=sb.toString();
			String pid=null;
			String cid=null;
			if(var.length()>0){
				if(var.contains(PID)){
					pid=var.substring(var.indexOf(PID));
					if(pid.contains(CID)){
						pid=pid.substring(0, pid.indexOf(CID));
					}
					pid=pid.replaceAll(PID, "").trim();
					infos.put(PID, pid);
				}
				if(var.contains(CID)){
					cid=var.substring(var.indexOf(CID));
					if(cid.contains(PID)){
						cid=cid.substring(0, cid.indexOf(PID));
					}
					cid=cid.replaceAll(CID, "").trim();
					infos.put(CID, cid);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(inputStream!=null){
					inputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return infos;
	}
}
