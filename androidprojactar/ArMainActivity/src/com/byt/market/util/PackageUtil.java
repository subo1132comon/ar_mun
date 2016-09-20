/**
 * @author jiangxiaoliang
 * ��ȡapk��װ����Ϣ�����ࣨicon��appname��version�ȣ�
 */

package com.byt.market.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.byt.market.Constants.Settings;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.ApkUninstallActivity;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadTaskManager;

public class PackageUtil {

	public static final int TYPE_MOVE_TO_ROM = 1;// can move to rom
	public static final int TYPE_MOVE_TO_SDCARD = 2;// can move to sdcard
	public static final int TYPE_CANNOT_MOVE = 3;// cannot move

	/**
	 * @param context
	 * @param packageName
	 * @param version
	 * 
	 */
	public static int isApkInstalled(Context context, String packageName,
			int version) {
		try {
			int installedState = AppItemState.STATE_IDLE;
			if (packageName == null || packageName.length() == 0)
				return AppItemState.STATE_IDLE;
			PackageInfo sPackageInfo = context.getPackageManager()
					.getPackageInfo(packageName, 0);
			if (sPackageInfo == null)
				return AppItemState.STATE_IDLE;
			if (version < sPackageInfo.versionCode) {
				installedState = AppItemState.STATE_INSTALLED_NEW;
			} else if (version == sPackageInfo.versionCode) {
				installedState = AppItemState.STATE_INSTALLED_NEW;
			} else {
				installedState = AppItemState.STATE_NEED_UPDATE;
				
			}
			return installedState;
		} catch (Exception e) {
			// LogUtil.e(PackageUtil.class.getSimpleName(), "isApkInstalled:" +
			// e.toString());
		}
		return AppItemState.STATE_IDLE;
	}
	public static int isApkInstalledHome(Context context, String packageName,
			int version,AppItem appitem) {
		try {
			int installedState = AppItemState.STATE_IDLE;
			if (packageName == null || packageName.length() == 0)
				return AppItemState.STATE_IDLE;
			PackageInfo sPackageInfo = context.getPackageManager()
					.getPackageInfo(packageName, 0);
			if (sPackageInfo == null)
				return AppItemState.STATE_IDLE;
			if (version < sPackageInfo.versionCode) {
				installedState = AppItemState.STATE_INSTALLED_NEW;
			} else if (version == sPackageInfo.versionCode) {
				installedState = AppItemState.STATE_INSTALLED_NEW;
			} else {
				
				DownloadTaskManager.getInstance().updateAfterUpdateGamesItem(appitem);	
				installedState = AppItemState.STATE_NEED_UPDATE;
			}
			return installedState;
		} catch (Exception e) {
			// LogUtil.e(PackageUtil.class.getSimpleName(), "isApkInstalled:" +
			// e.toString());
		}
		return AppItemState.STATE_IDLE;
	}
	/**
	 * 
	 * @param packageName
	 * @param version
	 * @return
	 */
	public static boolean isInstalledApk(Context context, String packageName,
			String version) {
		try {
			if (packageName == null || packageName.length() == 0) {
				return false;
			}

			PackageInfo sPackageInfo = context.getPackageManager()
					.getPackageInfo(packageName, 0);
			if (version != null && version.length() > 0) {
				if (sPackageInfo.versionName.equals(version)) {
					return true;
				}
			} else {
				return true;
			}
		} catch (Exception e) {
//			LogUtil.e(PackageUtil.class.getSimpleName(),
//					"isInstalledApk:" + e.toString());
		}
		return false;
	}

	public static final int SYSTEM_STATE_DATA = 1; // 安装在data分区
	public static final int SYSTEM_STATE_SYSTEM_UPDATED = 2;// 安装在system分区，但是已经更新到data分区，可以卸载更新
	public static final int SYSTEM_STATE_SYSTEM = 3;// 安装在system分区

	public static int isInSystemState(Context context, String packageName) {
		int state = SYSTEM_STATE_DATA;
		try {
			if (packageName == null || packageName.length() == 0) {
				return state;
			}

			PackageInfo sPackageInfo = context.getPackageManager()
					.getPackageInfo(packageName, 0);

			if (sPackageInfo != null) {
				if ((sPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
					state = SYSTEM_STATE_SYSTEM_UPDATED;
				} else if ((sPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					state = SYSTEM_STATE_SYSTEM;
				}

			}
		} catch (Exception e) {
//			LogUtil.e(PackageUtil.class.getSimpleName(),
//					"isInstalledApk:" + e.toString());
		}
		return state;
	}

	/**
	 * 
	 * @param context
	 * @param filePath
	 *            abc.apk
	 */
	public static void installApkByUser(Context context, String filePath) {
		File file = new File(filePath);
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		context.startActivity(intent);

		// try {
		// PackageManager sPackageManager = context.getPackageManager();
		// Method sMethod =
		// sPackageManager.getClass().getMethod("installPackage", Uri.class,
		// IPackageInstallObserver.class, int.class, String.class);
		// Uri sUri = Uri.parse(filePath);
		// AppSnippet sAppSnippet = getAppSnippet(context, sUri);
		//
		// sMethod.invoke(sPackageManager, sUri, new
		// IPackageInstallObserver.Stub() {
		// @Override
		// public void packageInstalled(String packageName, int returnCode)
		// throws RemoteException {
		//
		// }
		// }, 0, sAppSnippet.packageName);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	/**
	 * 卸载应用，仅在DownloadTaskManger调用
	 * 
	 * @param packageName
	 */
	public static void uninstallApp(Context context, String packageName,
			String sid, boolean removeFile) {
		/*
		 * if(mSystemUninstallTask !=null || mSystemInstallTask != null){
		 * App.getInstance().showToast(R.string.taost_pkg_mgr_busy); } else
		 */{
			final SystemUninstallTask unInstallTask = new SystemUninstallTask();
			mSystemUninstallTask.put(unInstallTask, 1);
			unInstallTask.execute(context, packageName, sid, removeFile);
		}

		/*
		 * if(unInstallApkBySystem(packageName)){
		 * DownloadTaskManager.getInstance().updateAfterApkUninstalled(sid,
		 * removeFile); } else { //静默卸载失败，调用系统卸载功能 Intent intent = new
		 * Intent(context,ApkUninstallActivity.class);
		 * intent.putExtra(ApkUninstallActivity.EXT_PNAME, packageName);
		 * intent.putExtra(ApkUninstallActivity.EXT_REMOVE_FILE, removeFile);
		 * intent.putExtra(ApkUninstallActivity.EXT_SID, sid);
		 * intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
		 * context.startActivity(intent); }
		 */
	}

	private static Map<SystemUninstallTask, Integer> mSystemUninstallTask = new HashMap<SystemUninstallTask, Integer>();

	public static class SystemUninstallTask extends AsyncTask {

		Context mContext;
		String mPname;
		String mSid;
		boolean mRemoveFile;

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			mContext = (Context) params[0];
			mPname = (String) params[1];
			mSid = (String) params[2];
			mRemoveFile = (Boolean) params[3];
			return Settings.quickInstall ? unInstallApkBySystem(mPname) : false;
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			final boolean success = (Boolean) result;
			// if(!PackageUtil.isInstalledApk(mContext, mPname, null)){
			// DownloadTaskManager.getInstance().updateAfterApkUninstalled(null,mSid,
			// mRemoveFile);
			if (success) {
				DownloadTaskManager.getInstance().updateAfterApkUninstalled(
						null, mSid, mRemoveFile, mPname);
			} else {
				// 静默卸载失败，调用系统卸载功能
				Intent intent = new Intent(mContext, ApkUninstallActivity.class);
				intent.putExtra(ApkUninstallActivity.EXT_PNAME, mPname);
				intent.putExtra(ApkUninstallActivity.EXT_REMOVE_FILE,
						mRemoveFile);
				intent.putExtra(ApkUninstallActivity.EXT_SID, mSid);
				intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			}
			if (mSystemUninstallTask.containsKey(this)) {
				mSystemUninstallTask.remove(this);
			}
			killSelfIfNeed();
		}

	}

	public static void uninstallAppByUser(ApkUninstallActivity context,
			String packageName) {
		if (isInstalledApk(context, packageName, null)) {
			Uri packageUri = Uri.parse("package:" + packageName);
			Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
			context.startActivityForResult(intent,
					ApkUninstallActivity.REQUEST_CODE_UNINSTALL);
		}
	}

	/**
	 * 判断安装或者卸载是否在进行
	 * */
	public static boolean isPkgMgrRunning() {
		return mSystemUninstallTask.keySet().size() != 0
				|| mSystemInstallTask.keySet().size() != 0;
	}

	public static boolean syncInstallUninstallStates() {
		Map<String, Integer> sids = new HashMap<String, Integer>();
		if (mSystemInstallTask != null
				&& mSystemInstallTask.keySet().size() > 0) {
			final Iterator iterator = mSystemInstallTask.keySet().iterator();
			while (iterator.hasNext()) {
				final SystemInstallTask task = (SystemInstallTask) iterator
						.next();
				sids.put(String.valueOf(task.mDownloadTask.mDownloadItem.sid),
						1);
			}
		}
		boolean installingSync = DownloadTaskManager.getInstance()
				.syncInstallingTasks(sids);

		sids.clear();
		if (mSystemUninstallTask != null
				&& mSystemUninstallTask.keySet().size() > 0) {
			final Iterator iterator = mSystemUninstallTask.keySet().iterator();
			while (iterator.hasNext()) {
				final SystemUninstallTask task = (SystemUninstallTask) iterator
						.next();
				sids.put(task.mSid, 1);
			}
		}
		boolean unInstallingSync = DownloadTaskManager.getInstance()
				.syncUninstallingTasks(sids);

		return installingSync || unInstallingSync;

	}

	private static void killSelfIfNeed() {
		if (!isPkgMgrRunning()
				&& MyApplication.getInstance().mWillKillSelfAfterExit) {
			System.exit(0);
		}
	}

	/**
	 * 
	 * @param packageName
	 */
	public static void startApp(Context context, String packageName) {
		try{
		if (isInstalledApk(context, packageName, null)) {
			PackageManager packageManager = context.getPackageManager();
			Intent intent = packageManager
					.getLaunchIntentForPackage(packageName);
			if (intent != null) {
				context.startActivity(intent);
			}
			else{
				Toast.makeText(context, R.string.appcanuseless, Toast.LENGTH_SHORT).show();;
			}
		}
		}catch(Exception e){
			
		}
	}

	/**
	 * 
	 * @param ctx
	 * @param packageName
	 * @return TYPE_MOVE_TO_ROM = 1;// can move to rom TYPE_MOVE_TO_SDCARD
	 *         =2;//can move to sdcard TYPE_CANNOT_MOVE = 3;//cannot move
	 */
	public static int getAppInstalledLocation(Context ctx, String packageName) {
		int result = TYPE_CANNOT_MOVE;

		if (android.os.Build.VERSION.SDK_INT >= 8) {
			if (canAppMove(ctx, packageName)) {
				try {
					PackageManager sPackageManager = ctx.getPackageManager();
					ApplicationInfo sApplicationInfo = sPackageManager
							.getApplicationInfo(packageName, 0);

					if ((sApplicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0) {// ���ֻ���
						result = TYPE_MOVE_TO_SDCARD;
					} else {
						result = TYPE_MOVE_TO_ROM;
					}
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * @param packageName
	 * @return
	 */
	private static boolean canAppMove(Context ctx, String packageName) {
		try {
			if (android.os.Build.VERSION.SDK_INT < 8) {
				return false;
			}

			PackageManager sPackageManager = ctx.getPackageManager();
			PackageInfo sPackageInfo = sPackageManager.getPackageInfo(
					packageName, 0);
			Class sClass = sPackageInfo.getClass();
			Field sField = sClass.getField("installLocation");
			int installlocation = sField.getInt(sPackageInfo);

			if (installlocation >= 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static Resources getResources(Context context, String apkPath)
			throws Exception {
		String PATH_AssetManager = "android.content.res.AssetManager";
		Class<?> assetMagCls = Class.forName(PATH_AssetManager);
		Constructor<?> assetMagCt = assetMagCls.getConstructor((Class[]) null);
		Object assetMag = assetMagCt.newInstance((Object[]) null);
		Class<?>[] typeArgs = new Class[1];
		typeArgs[0] = String.class;
		Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
				"addAssetPath", typeArgs);
		Object[] valueArgs = new Object[1];
		valueArgs[0] = apkPath;
		assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
		Resources res = context.getResources();
		typeArgs = new Class[3];
		typeArgs[0] = assetMag.getClass();
		typeArgs[1] = res.getDisplayMetrics().getClass();
		typeArgs[2] = res.getConfiguration().getClass();
		Constructor<?> resCt = Resources.class.getConstructor(typeArgs);
		valueArgs = new Object[3];
		valueArgs[0] = assetMag;
		valueArgs[1] = res.getDisplayMetrics();
		valueArgs[2] = res.getConfiguration();
		res = (Resources) resCt.newInstance(valueArgs);
		return res;
	}

	private static Drawable getUninstallAPKIcon(Context context,
			PackageInfo info, String apkPath) {
		Resources res = null;
		try {
			res = getResources(context, apkPath);
		} catch (Exception e) {
			return null;
		}
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;
			return res.getDrawable(appInfo.icon);
		}
		return null;
	}

	/**
	 * 获取应用详情页面的intent
	 * 
	 * @param context
	 * @param packageName
	 */
	public static Intent getAppDetailsIntent(String packageName) {
		String SCHEME = "package";
		// 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
		String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
		// 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
		String APP_PKG_NAME_22 = "pkg";

		String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
		// InstalledAppDetails类名
		String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

		String ACTION_APPLICATION_DETAILS_SETTINGS = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;

		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) { // 2.3（ApiLevel 9）以上，使用SDK提供的接口
			intent.setAction(ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts(SCHEME, packageName, null);
			intent.setData(uri);
		} else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
			// 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
			final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
					: APP_PKG_NAME_21);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(APP_DETAILS_PACKAGE_NAME,
					APP_DETAILS_CLASS_NAME);
			intent.putExtra(appPkgName, packageName);
		}
		return intent;
	}

	public static void doInstall(final Context context, String filePath,
			final DownloadTask downloadTask) {
		/*
		 * if(mSystemUninstallTask !=null || mSystemInstallTask != null){
		 * App.getInstance().showToast(R.string.taost_pkg_mgr_busy); } else
		 */{
			SystemInstallTask instalTask = new SystemInstallTask();
			mSystemInstallTask.put(instalTask, 1);
			instalTask.execute(context, filePath, downloadTask);
		}

		/*
		 * final boolean success =
		 * com.byt.market.util.PackageUtil.installApk(filePath, new
		 * Runnable() {
		 * 
		 * @Override public void run() { new AlertDialog.Builder(context)
		 * .setTitle(R.string.dialog_install_no_file_title)
		 * .setMessage(R.string.dialog_install_no_file_title)
		 * .setPositiveButton(android.R.string.ok, new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * DownloadTaskManager.getInstance().goOnDownloadTask(downloadTask); }
		 * }).setNegativeButton(android.R.string.cancel, null).show(); } });
		 * if(!success){
		 * DownloadTaskManager.getInstance().updateInstallState(downloadTask
		 * ,AppItemState.STATE_INSTALL_FAILED); }//安装成功会在收到应用安装的广播时修改
		 */
	}

	private static Map<SystemInstallTask, Integer> mSystemInstallTask = new HashMap<SystemInstallTask, Integer>();

	public static class SystemInstallTask extends AsyncTask {

		Context mContext;
		String mFilePath;
		DownloadTask mDownloadTask;

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			mContext = (Context) params[0];
			mFilePath = (String) params[1];
			mDownloadTask = (DownloadTask) params[2];
			try{
			return com.byt.market.util.PackageUtil.installApk(
					mFilePath, new Runnable() {

						@Override
						public void run() {
							new AlertDialog.Builder(mContext)
									.setTitle(
											R.string.dialog_install_no_file_title)
									.setMessage(
											R.string.dialog_install_no_file_title)
									.setPositiveButton(
											android.R.string.ok,
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													DownloadTaskManager
															.getInstance()
															.goOnDownloadTask(
																	mDownloadTask);
												}
											})
									.setNegativeButton(android.R.string.cancel,
											null).show();
						}
					});
			}
			catch(Exception e){
				return false;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			final boolean success = (Boolean) result;
			// !PackageUtil.isInstalledApk(mContext,
			// mDownloadTask.mDownloadItem.pname,
			// String.valueOf(mDownloadTask.mDownloadItem.vcode))
			if (!success) {
				if (!Settings.quickInstall) {
					DownloadTaskManager.getInstance().updateInstallState(
							mDownloadTask, AppItemState.STATE_DOWNLOAD_FINISH);
				} else {
					DownloadTaskManager.getInstance().updateInstallState(
							mDownloadTask, AppItemState.STATE_INSTALL_FAILED);
				}
			} else {
				DownloadTaskManager.getInstance().notifyAppInstalled(
						mDownloadTask);
			}
			if (mSystemInstallTask.containsKey(this)) {
				mSystemInstallTask.remove(this);
			}
			killSelfIfNeed();
		}
	}

	/**
	 * 执行安装
	 * 
	 * @param appId
	 *            游戏id
	 * @return 如果后台安装成功返回true，后台安装失败或用户自主安装返回false
	 */
	public static boolean installApk(String apkPath, Runnable run) {
		if (FileUtil.checkSDCardStateAndNote()) {
			if (apkPath != null && apkPath.startsWith("/data")) {
				chmod(apkPath, "777");
			}
			File f = new File(apkPath);

			if (f.exists() && f.canRead()) {
				// Logs.printNetDownloadLog("installHiApk--执行安装，文件存在");
				return installSoftware(apkPath);
			} else {
				// Logs.printNetDownloadLog("installHiApk--执行安装，文件不存在");
				if (!apkPath.startsWith("/data")
						&& !Environment.MEDIA_MOUNTED.equals(Environment
								.getExternalStorageState())) {
					// apkPath = getSoftwareSavePath(
					// com.byt.market.util.Constants.Pkg_Apk_Data_Cache_Dir,
					// appId);
					if (f.exists()) {
						// Logs.printNetDownloadLog("installHiApk--执行安装，文件存在于程序目录");
						return installSoftware(apkPath);
					} else {
						// Logs.printNetDownloadLog("installHiApk--执行安装，文件不存在于程序目录");
						return false;
					}
				} else {
					if (run != null) {
						run.run();
					}
					return false;
				}
			}
		} else {
			// Logs.printNetDownloadLog("installHiApk--执行安装，没有存储目录");
			return false;
		}
	}

	/**
	 * 系统后台卸载游戏
	 * 
	 * @return
	 */
	public static boolean unInstallApkBySystem(String pname) {
		return InstallUtil.uninstallApk(pname);
	}

	/**
	 * 系统后台安装游戏
	 * 
	 * @return
	 */
	public static boolean installApkBySystem(String apkpath) {
		final boolean success = InstallUtil.installData(new File(apkpath));
		return success;
	}

	/**
	 * 安装游戏
	 * 
	 * @param apkpath
	 * @return 如果后台安装成功返回true，后台安装失败或用户自主安装返回false
	 */
	public static boolean installSoftware(String apkpath) {
		if (true/* isFast_install() */) {
			if (!Settings.quickInstall || !installApkBySystem(apkpath)) {
				// if (downloaditem != null) {
				/*
				 * GAME game = new GAME(); game.app_id =
				 * downloaditem.getAppId(); String list_c =
				 * downloaditem.getList_cateid(); if(list_c == null) list_c =
				 * "0"; game.list_c = list_c; game.list_id =
				 * downloaditem.getList_id(); game.i_f = 1;
				 * Util.addData(marketContext, game);
				 */
				// }

				installApkByUser(
						com.byt.market.MyApplication.getInstance(),
						apkpath);
				return false;
			} else {
				return true;
			}
		} else {
			installApkByUser(
					com.byt.market.MyApplication.getInstance(),
					apkpath);
			return false;
		}
	}

	public static void chmod(String filePath, String mode) {
		try {
			String str = "chmod " + mode + " " + filePath
					+ " && busybox chmod " + mode + " " + filePath;
			Runtime.getRuntime().exec(str);
		} catch (Exception localException) {
		}
	}

	/**
	 * 隐藏系统键盘
	 */
	public static void hideKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) view.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	/**
	 * 显示系统键盘
	 */
	public static void showInputMethod(View view) {
		InputMethodManager imm = (InputMethodManager) view.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(view, 0);
		}
	}

	/**
	 * 根据包名得到程序图标
	 */
	public static Drawable getAppIcon(Context context, String packname) {
		PackageManager packageManager = context.getPackageManager();
		try {
			ApplicationInfo info = packageManager.getApplicationInfo(packname,
					0);
			return resizeDrawable(info.loadIcon(packageManager), 2);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 根据包名得到程序名称
	 */
	public static String getAppName(Context context, String packname) {
		PackageManager packageManager = context.getPackageManager();
		try {
			ApplicationInfo info = packageManager.getApplicationInfo(packname,
					0);
			if (info == null) {
				return null;
			}
			CharSequence label = info.loadLabel(packageManager);
			return TextUtils.isEmpty(label) ? "" : label.toString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 根据包名得到程序安装时间
	 */
	@SuppressLint("NewApi") public static long getAppInstallTime(Context context, String packname) {
		PackageManager packageManager = context.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager
					.getPackageInfo(packname, 0);
			if (packageInfo == null) {
				return 0;
			}
			return packageInfo.firstInstallTime;
		} catch (Exception e) {
			return 0;
		}
	}

	public static final int DRAWABLE_SIZE_TYPE_ZERO = 0;
	public static final int DRAWABLE_SIZE_TYPE_ONE = 1;
	public static final int DRAWABLE_SIZE_TYPE_TWO = 2;
	public static final int DRAWABLE_SIZE_TYPE_THREE = 3;
	public static final int DRAWABLE_SIZE_TYPE_FOUR = 4;

	public static Drawable resizeDrawable(Drawable mDrawable, int type) {// add
		// by
		// yangdaoyong
		Drawable mNewIcon = null;
		try {
			if (mDrawable == null)
				return mNewIcon;
			if (!(mDrawable instanceof BitmapDrawable)) {
				return mDrawable;
			}
			Bitmap mBitmap = (Bitmap) ((BitmapDrawable) mDrawable).getBitmap();
			// mBitmap.recycle();
			if (mBitmap == null || mBitmap.isRecycled()) {
				return mNewIcon;
			}

			if (mBitmap.getWidth() > 768 || mBitmap.getHeight() > 768) {// 处理异常尺寸
				mBitmap = null;
				return null;
			}
			switch (type) {
			case DRAWABLE_SIZE_TYPE_ZERO:
				mNewIcon = new BitmapDrawable(Bitmap.createScaledBitmap(
						mBitmap, 30, 30, false));

				break;
			case DRAWABLE_SIZE_TYPE_ONE:
				if (mBitmap.getWidth() > 48 && mBitmap.getHeight() > 48) {
					// Log.i("ydy","SystemUtil resizeDrawable DRAWABLE_SIZE_TYPE_ONE");
					mNewIcon = new BitmapDrawable(Bitmap.createScaledBitmap(
							mBitmap, 48, 48, false));
				}
				break;
			case DRAWABLE_SIZE_TYPE_TWO:
				if (mBitmap.getWidth() > 96 && mBitmap.getHeight() > 96) {
					// Log.i("ydy","SystemUtil resizeDrawable DRAWABLE_SIZE_TYPE_TWO");
					mNewIcon = new BitmapDrawable(Bitmap.createScaledBitmap(
							mBitmap, 96, 96, false));
				}
				break;
			case DRAWABLE_SIZE_TYPE_THREE:
				if (mBitmap.getWidth() > 144 && mBitmap.getHeight() > 144) {
					mNewIcon = new BitmapDrawable(Bitmap.createScaledBitmap(
							mBitmap, 144, 144, false));
				}
				break;
			case DRAWABLE_SIZE_TYPE_FOUR:
				if (mBitmap.getWidth() > 256 && mBitmap.getHeight() > 256) {
					mNewIcon = new BitmapDrawable(Bitmap.createScaledBitmap(
							mBitmap, 256, 256, false));
				}
				break;
			default:
				break;
			}
			//mBitmap.recycle();
			mBitmap = null;
			if (mNewIcon == null)
				mNewIcon = mDrawable;
			mDrawable = null;
			return mNewIcon;
		} catch (Exception e) {
			e.printStackTrace();
			mNewIcon = mDrawable;
		}
		return mNewIcon;
	}

}
