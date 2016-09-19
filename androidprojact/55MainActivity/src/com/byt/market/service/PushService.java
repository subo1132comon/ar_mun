package com.byt.market.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.byt.market.R;
import com.byt.market.MyApplication;
import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.LoginInfo;
import com.byt.market.data.MarketUpdateInfo;
import com.byt.market.data.PUSH;
import com.byt.market.data.PushInfo;
import com.byt.market.download.DownloadUtils;
import com.byt.market.log.LogModel;
import com.byt.market.net.NetworkUtil;
import com.byt.market.tools.RootTools;
import com.byt.market.util.InstallUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LPUtil;
import com.byt.market.util.LogUtil;
import com.byt.market.util.LoginDBUtil;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.SysNotifyUtil;
import com.byt.market.util.Util;

public class PushService extends IntentService implements TaskListener {

	public static final String TAG = "PushService";
	public SharedPreferences sp;
	public Editor editor;
	public MarketContext mcContext;
	private HashMap<String, String> map;
	private File downFile;

	public PushService() {
		super("PushService");
	}

	@Override
	public void onCreate() {
		mcContext = MarketContext.getInstance();
		if (!MarketContext.isDataOk) {
			mcContext.initData(getApplicationContext());
		}
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (setRuleTime(getApplicationContext()))
			startTask();
	}

	private void startTask() {
		map = Util.getUplodMap(getApplicationContext(), Constants.PUSH);
		Util.POSTDATA = null;
		Util.POSTDATA = Util.getLogPostData(getApplicationContext());
		Util.getDataPointMap(getApplicationContext());
		writeRuleUpdateInfo2File2(new Date(System.currentTimeMillis()));
		ProtocolTask mTask = new ProtocolTask(this);
		mTask.setListener(this);
		mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
	}

	private Object getHeader() {
		return map;
	}

	private Object tag() {
		return getClass().getSimpleName();
	}

	private Object getRequestContent() {
		return Util.POSTDATA;
	}

	private Object getRequestUrl() {
		return Constants.LIST_URL + "?qt=" + Constants.PLINFO;
	}

	private boolean setRuleTime(final Context context) {
		Util.UPDATERULETIME = SharedPrefManager.getPushtime(context);
		long lastmill = getLastConnectTime(context);
		PendingIntent pi = getStartRuleServicePI();
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);
		if (System.currentTimeMillis() - lastmill >= Util.UPDATERULETIME) {
			am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
					+ Util.UPDATERULETIME, pi);
			return true;
		} else {
			am.set(AlarmManager.RTC_WAKEUP, lastmill + Util.UPDATERULETIME, pi);
			return false;
		}
	}

	private Long getLastConnectTime(final Context context) {
		FileInputStream fin = null;
		try {
			fin = context.openFileInput("flag2.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		long lastmill = 0l;
		if (fin != null) {
			String lasttime = Util.readFile(fin);
			if (lasttime != null && !lasttime.equals(""))
				lastmill = Long.valueOf(lasttime);
		}
		return lastmill;
	}

	private PendingIntent getStartRuleServicePI() {
		Intent i = new Intent();
		i.setClass(this, PushService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pi;
	}

	public void writeRuleUpdateInfo2File2(Date date) {
		FileOutputStream fos = null;
		try {
			fos = getApplicationContext().openFileOutput("flag2.txt", 1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (fos != null) {
			Util.saveFile(fos, String.valueOf(date.getTime()));
		}
	}

	@Override
	public void onNoNetworking() {
		setLoadfailedView();
		System.gc();
	}

	@Override
	public void onNetworkingError() {
		setLoadfailedView();
		System.gc();
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		try {
			mcContext.opData(LogModel.DATA_DEL, LogModel.DATA_ALL, null);
			if (bytes != null) {
				String datas = new String(bytes);
				JSONObject result = new JSONObject(datas);
				int status = JsonParse.parseResultStatus(result);
				if (status == 1) {
					LoginInfo loginInfo = parseData(result);
					if (loginInfo != null) {
						doLoginInfo(loginInfo);
					}
				} else {
					setLoadfailedView();
				}
			} else {
				setLoadfailedView();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.gc();
	}

	private void setLoadfailedView() {
		Toast.makeText(this, "网络异常", 150).show();
	}

	private LoginInfo parseData(JSONObject result) throws Exception {
		LPUtil lpUtil = new LPUtil();
		lpUtil.parserContent(result);
		return lpUtil.getlInfo();
	}

	public void doLoginInfo(final LoginInfo loginInfo) {
		if (loginInfo.page != null)
			Util.loadInfos = loginInfo.page;
		if (loginInfo.upate != null)
			Util.update = loginInfo.upate;
		if (loginInfo.pturntime != 0)
			Util.UPDATERULETIME = loginInfo.pturntime * Util.MIN;
		if (loginInfo.pInfo != null)
			Util.pInfo = loginInfo.pInfo;
		ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		SharedPreferences preferences = getSharedPreferences("info",
				MODE_PRIVATE);
		final boolean isClientActive = preferences.getBoolean("isClientActive",
				false);
		new Thread() {
			public void run() {
				if (loginInfo.page != null)
					Util.loadInfos = loginInfo.page;
				if (loginInfo.upate != null)
					Util.update = loginInfo.upate;
				if (loginInfo.pturntime != 0)
					Util.UPDATERULETIME = loginInfo.pturntime * Util.MIN;
				if (loginInfo.pInfo != null)
					Util.pInfo = loginInfo.pInfo;
				SharedPrefManager.setPushTime(PushService.this,
						Util.UPDATERULETIME);
				if (Util.loadInfos != null) {
					LoginDBUtil.add(getApplicationContext(), Util.loadInfos);
				}
				if (Util.pInfo != null) {
					SharedPreferences sp = getSharedPreferences("push",
							Context.MODE_PRIVATE);
					PUSH p = new PUSH();
					p.id = Util.pInfo.id;
					p.state = 0;
					if (Util.pInfo.ptype == 2) {
						try {
							Util.pInfo.app = JsonParse
									.parseAppItem(new JSONObject(
											Util.pInfo.pvalue));
						} catch (JSONException e) {
							e.printStackTrace();
						}
						p.appid = Util.pInfo.app.sid;
					}
					p.type = Util.pInfo.ptype;
					Util.addData(mcContext, p);
					Editor editor = sp.edit();
					editor.putInt("pid", Util.pInfo.id);
					editor.commit();
					showNotify(Util.pInfo);
				}
				if (Util.update != null) {

					if (Util.update.right == 1 && networkInfo != null
							&& networkInfo.getTypeName().equals("WIFI")
							&& !isClientActive) {
						startDown(Util.update.apk);
					} else
						showNotify(Util.update);
				}
			}
		}.start();
	}

	public void startDown(String apkPath) {
		if (apkPath == null)
			return;
		if (apkPath.startsWith("http://")) {
		} else {
			apkPath = Constants.APK_URL + apkPath;
		}
		try {
			if ((downFile = Util
					.startDownload(getApplicationContext(), apkPath)) != null) {
				if (!downFile.exists())
					return;
				PackageUtil.chmod(downFile.getAbsolutePath(), "777");
				if (!InstallUtil.installData(downFile)) {
					showNotify(Util.update);
				}
			}
		} catch (Exception e) {
			downFile.delete();
			e.printStackTrace();
		}
	}

	protected void showNotify(PushInfo pInfo) {
		SysNotifyUtil.notifyPushInfo(getApplicationContext(), mcContext,
				R.drawable.icon, pInfo);
	}

	protected void showNotify(MarketUpdateInfo update) {
		SysNotifyUtil.notifyMarketUpdateInfo(getApplicationContext(),
				mcContext, R.drawable.icon, update);
	}
}
