package com.byt.market.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.download.DownloadContent;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.download.GamesProvider;
import com.byt.market.log.LogModel;
import com.byt.market.tools.LogCart;

/**
 * 用来管理-“所有游戏的包名列表” 不是从游戏大厅下载安装的游戏也会显示到“我的”
 * */
public class LocalGameDBManager implements TaskListener {

	public static final Uri CONTENT_URI = Uri.parse(DownloadContent.CONTENT_URI
			+ "/" + GamesProvider.ALL_GAMES_TABLE);

	private static LocalGameDBManager mLocalGameManager;

	public static final String PREF_LOCAL_GAME = "localGame";
	public static final String PREF_LOCAL_GAME_KEY = "lastUpdate";
	public static final String PREF_CURRENT_LOCAL_GAME = "currentLocal";
	private SharedPreferences mSharedPreferences;
	private ProtocolTask mTask;
	private boolean isFistsend = true;

	/**
	 * 只在本次进入应用第一次进入“我的”，才同步本地游戏
	 * */
	private static boolean mIsSynced;

	private LocalGameDBManager() {
	}

	private Object tag() {
		return getClass().getSimpleName();
	}

	public static LocalGameDBManager getInstance() {
		if (mLocalGameManager == null) {
			mLocalGameManager = new LocalGameDBManager();
		}
		return mLocalGameManager;
	}

	public int getGameID(Context context, String pname) {
		int sid = -1;
		final Cursor cursor = context.getContentResolver().query(CONTENT_URI,
				null, "pname = '" + pname + "'", null, null);
		if (cursor.moveToFirst()) {
			sid = cursor.getInt(1);
		}
		if (cursor != null) {
			cursor.close();
		}
		return sid;
	}

	/**
	 * 
	 * 同步本地游戏，首先从服务器获取本地游戏是否更新，再doLocalGames()
	 * */
	public void syncLocalGames(Context context) {
		tryCheckLocalGameUpdate(context);
	}

	/**
	 * 同步本地游戏，将其他市场安装的游戏同步到“我的游戏”
	 * */
	private void doLocalGames() {
		if (mIsSynced) {
			return;
		}
		new Thread() {
			@Override
			public void run() {
				super.run();
				final PackageManager pm = MyApplication.getInstance()
						.getPackageManager();
				final List<PackageInfo> packages = pm.getInstalledPackages(0);
				for (int i = 0; i < packages.size(); i++) {
					PackageInfo info = packages.get(i);
					if (info.packageName.startsWith("com.android.")
							|| info.packageName
									.startsWith("com.google.android.")
							|| info.packageName.startsWith("android")
							|| info.packageName.startsWith("com.sec.android.")) {
						continue;
					}
					AppItem appItem = getAppItemFromPkgInfo(pm, info);
					appItem.isInnerGame = 0;
					addIfGame(appItem);
				}
				GameUpdateManager.getInstance().tryCheckGameUpdate(
						MyApplication.getInstance(), false);
				mIsSynced = true;
				// 由于同步本地游戏比较慢,所以当同步完成之后,再让我的游戏界面进行重新刷新
				if (MyApplication.getInstance() != null) {
					MyApplication.getInstance().sendBroadcast(
							new Intent(Constants.Action.REFRESH_LOCAL_GAME));
				}
			}
		}.start();
	}

	public void tryCheckLocalGameUpdate(Context context) {
		if (mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(PREF_LOCAL_GAME,
					Context.MODE_PRIVATE);
		}
		final long lastUpdate = mSharedPreferences.getLong(PREF_LOCAL_GAME_KEY,
				0);
		if (System.currentTimeMillis() - lastUpdate > 60 * 24 * Util.MIN
				|| System.currentTimeMillis() - lastUpdate <= 0) {
			if (mTask == null) {
				mTask = new ProtocolTask(context);
				mTask.setListener(this);
				mTask.execute(getRequestUrl(), null, tag(), getHeader(context));
			}
		} else {
			doLocalGames();
		}
	}

	public void sendLocalUseAppStatusToServer(Context context,
			long lastLoginTime, long loginTime, long logoutTime) {
		mTask = new ProtocolTask(context);
		mTask.setListener(new TaskListener() {

			@Override
			public void onPostExecute(byte[] bytes) {
				int a = 0;
				int b = 0;
			}

			@Override
			public void onNoNetworking() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNetworkingError() {

			}
		});
		Log.d("nnlog", "xiang 退出---请求");
		if(isFistsend){
			Log.d("nnlog", "退出---请求");
			mTask.execute(Constants.LIST_URL + "?qt=" + "logout&lastlogintime="
					+ lastLoginTime + "&logintime=" + loginTime +"&usid="+SharedPrefManager.getLastLoginUserName(context)+"&logouttime="
					+ logoutTime, null, tag(), getHeader(context));
			isFistsend = false;
		}
	}

	public HashMap<String, String> getHeader(Context context) {
		String model = Util.mobile;
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		String usid = SharedPrefManager.getLastLoginUserName(context);
		if (model == null)
			model = Util.getModel();
		if (imei == null)
			imei = Util.getIMEI(context);
		if (vcode == null)
			vcode = Util.getVcode(context);
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(context);
		}
		if(usid == null){
			usid = "";
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("model", model);
		map.put("channel", channel);
		map.put("usid", usid);
		return map;
	}

	private Object getRequestUrl() {
		LogUtil.i(
				"0425",
				"url = "
						+ Constants.LIST_URL
						+ "?qt="
						+ Constants.LOCAL_GAME_UPDATE
						+ Uri.encode(mSharedPreferences.getString(
								PREF_CURRENT_LOCAL_GAME,
								Constants.DEFAULT_LOCAL_UPDATE)));
		return Constants.LIST_URL
				+ "?qt="
				+ Constants.LOCAL_GAME_UPDATE
				+ Uri.encode(mSharedPreferences
						.getString(PREF_CURRENT_LOCAL_GAME,
								Constants.DEFAULT_LOCAL_UPDATE));
	}

	@Override
	public void onNetworkingError() {
		// TODO Auto-generated method stub
		doLocalGames();
	}

	@Override
	public void onNoNetworking() {
		// TODO Auto-generated method stub
		doLocalGames();
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		// TODO Auto-generated method stub
		new UpdateLocalGameDBTask().execute(bytes);

	}

	// edit by wangxin,因为此处数据解析量比较大,如果放在主线程,会阻塞主UI导致卡顿或黑屏
	class UpdateLocalGameDBTask extends AsyncTask<byte[], Void, byte[]> {

		@Override
		protected byte[] doInBackground(byte[]... params) {
			// TODO Auto-generated method stub
			byte[] bytes = params[0];
			if (bytes != null) {
				String datas = new String(bytes);
				try {
					final JSONObject obj = new JSONObject(datas);
					final JSONObject dataObj = obj.getJSONObject("data");
					final Map<String, String> idToPname = JsonParse
							.parseLocalGames(dataObj);
					Iterator<String> mIterator = idToPname.keySet().iterator();
					String sid = null;
					while (mIterator.hasNext()) {
						sid = mIterator.next();
						addLocalGame(sid, idToPname.get(sid));
					}
					if (JsonParse.parseResultStatus(obj) == 1) {
						mSharedPreferences
								.edit()
								.putLong(PREF_LOCAL_GAME_KEY,
										System.currentTimeMillis()).commit();
						final String localUpdateDate = JsonParse
								.parseLocalGameMDate(dataObj);
						if (!TextUtils.isEmpty(localUpdateDate)) {
							mSharedPreferences
									.edit()
									.putString(PREF_CURRENT_LOCAL_GAME,
											localUpdateDate).commit();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();

				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(byte[] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mTask = null;
			doLocalGames();
		}

	}

	public boolean isLocalSynced() {
		return mIsSynced;
	}

	private AppItem getAppItemFromPkgInfo(PackageManager pm, PackageInfo info) {
		final AppItem item = new AppItem();
		item.pname = info.packageName;
		item.vname = info.versionName;
		item.vcode = info.versionCode;
		item.name = info.applicationInfo.loadLabel(pm).toString();
		item.state = AppItemState.STATE_INSTALLED_NEW;
		item.list_id = LogModel.L_DOWN_MANAGER;
		item.list_cateid = LogModel.P_LIST;
		return item;
	}

	private int addIfGame(AppItem item) {
		if (MyApplication.getInstance().getPackageName().equals(item.pname)) {
			return -1;
		}
		final int sid = getGameID(MyApplication.getInstance(), item.pname);
		if (sid > 0) {
			item.sid = sid;
			DownloadTaskManager.getInstance().addLocalGame(item);
		}
		return sid;
	}

	/**
	 * 安装完成，将其他市场安装的游戏添加到“我的游戏”
	 * */
	public int addGameAfterInstalled(String pkn) {
		if (pkn.equals("com.byt.market")) {
			File f = new File(MyApplication.getInstance().getFilesDir()
					+ File.separator + Constants.APKNAME);
			if (f.exists())
				f.delete();
			f = new File(MyApplication.DATA_DIR + File.separator
					+ Constants.APKNAME);
			if (f.exists())
				f.delete();
			return -1;
		}
		final PackageManager pm = MyApplication.getInstance()
				.getPackageManager();
		PackageInfo info = null;
		try {
			info = pm.getPackageInfo(pkn, 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (info != null) {
			return addIfGame(getAppItemFromPkgInfo(pm, info));
		}
		return -1;
	}

	private void addLocalGame(String sid, String pname) {
		final Cursor cursor = MyApplication
				.getInstance()
				.getContentResolver()
				.query(CONTENT_URI, null, "pname = '" + pname + "'", null, null);
		if (cursor.moveToFirst()) {
			if (cursor != null) {
				cursor.close();
			}
			// do nothing
		} else {
			if (cursor != null) {
				cursor.close();
			}
			ContentValues values = new ContentValues();
			values.put("sid", sid);
			values.put("pname", pname);
			Uri uri = MyApplication.getInstance().getContentResolver()
					.insert(CONTENT_URI, values);
		}

	}
}
