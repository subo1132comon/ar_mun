package com.byt.market.service;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.util.Log;

import com.byt.market.MarketContext;
import com.byt.ar.R;
import com.byt.market.activity.DownLoadManageActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadAppItem;
import com.byt.market.data.LoginInfo;
import com.byt.market.data.PUSH;
import com.byt.market.data.PushInfo;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.net.NetworkUtil;
import com.byt.market.tools.LogCart;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LPUtil;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.SysNotifyUtil;
import com.byt.market.util.Util;

import java.util.ArrayList;
import java.util.List;

public class HomeLoadService extends Service implements TaskListener {

	private ProtocolTask mTask;
	private MarketContext maContext;
	private SharedPreferences sp;
	private Context mContext;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public void initData(Context context, MarketContext maContext, Object url,
			Object content, Object tag, Object header) {
		this.mContext = context;
		this.maContext = maContext;
		syncDownloadStates();
		sp = mContext
				.getSharedPreferences("openfailtime", Context.MODE_PRIVATE);
		long fail = sp.getLong("fail", 1);
		if (fail == 0)
			Util.addData(maContext);
		if (NetworkUtil.isNetWorking(mContext)) {
			mTask = new ProtocolTask(mContext);
			mTask.setListener(this);
			mTask.execute(url, content, tag, header);
		} else {
		}
	}

	public void syncDownloadStates() {
		try {
			final boolean downloadingSyncd = DownloadTaskManager.getInstance()
					.syncDownloadStates();
			final boolean installUninstallSyncd = PackageUtil
					.syncInstallUninstallStates();
			if (downloadingSyncd || installUninstallSyncd) {
				DownloadTaskManager.getInstance().reloadMemeryTasks();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onNoNetworking() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNetworkingError() {
		// TODO Auto-generated method stub

	}

	private LoginInfo parseData(JSONObject result) throws Exception {
		LPUtil lpUtil = new LPUtil();
		lpUtil.parserContent(result);
		return lpUtil.getlInfo();
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		try {
			maContext.opData(LogModel.DATA_DEL, LogModel.DATA_ALL, null);
			if (bytes != null) {
				String datas = new String(bytes);
				if (datas == null || datas.equals("")) {
					return;
				}
				JSONObject result = new JSONObject(datas);
				int status = JsonParse.parseResultStatus(result);
				if (status == 1) {
					LoginInfo loginInfo = parseData(result);
					if (loginInfo != null) {
						doLoginInfo(loginInfo);
					}
				} else {
				}
			} else {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.gc();
	}

	public void doLoginInfo(final LoginInfo loginInfo) {
		new Thread() {
			public void run() {
				if (loginInfo.page != null)
					Util.loadInfos = loginInfo.page;
				if (loginInfo.upate != null)
					Util.update = null;
				if (loginInfo.pturntime != 0)
					Util.UPDATERULETIME = loginInfo.pturntime * Util.MIN;
				if (loginInfo.pInfo != null)
					Util.pInfo = loginInfo.pInfo;
				SharedPrefManager.setPushTime(mContext, Util.UPDATERULETIME);
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
					Util.addData(maContext, p);
					Editor editor = sp.edit();
					editor.putInt("pid", Util.pInfo.id);
					editor.commit();
					showNotify(Util.pInfo);
				}
			}
		}.start();
	}

	protected void showNotify(PushInfo pInfo) {
		SysNotifyUtil.notifyPushInfo(mContext, maContext,
				R.drawable.app_content_btn, pInfo);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
