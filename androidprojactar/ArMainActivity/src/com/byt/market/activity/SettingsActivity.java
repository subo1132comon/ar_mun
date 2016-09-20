package com.byt.market.activity;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.MarketUpdateInfo;
import com.byt.market.log.LogModel;
import com.byt.market.net.NetworkUtil;
import com.byt.market.service.UpdateDownloadService;
import com.byt.market.ui.SettingFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.Util;

/**
 * 主页面，控制其他五个页面
 * 
 */
public class SettingsActivity extends BaseActivity implements OnClickListener {
    /** 检查版本更新移到设置下 start **/	
	public boolean isupdatelocal=false;
	protected UpdateDownloadService.DownloadBinder binder;
	protected String apkPath;
	protected ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (UpdateDownloadService.DownloadBinder) service;
			binder.start(apkPath);
		}

		public void onServiceDisconnected(ComponentName name) {
		}
	};
//	static SettingsActivity mSettingsActivity = null;
//	public static SettingsActivity getInstance(){
//		if(mSettingsActivity != null)
//		{
//			return mSettingsActivity;
//		}
//		return null;
//	}
	/** 检查版本更新移到设置下end **/	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_frame);
		initView();
		initData();
		addEvent();
//		mSettingsActivity = this;
	}
	
	@Override
    public void initView() {
        findViewById(R.id.titlebar_applist_button_container).setVisibility(View.GONE);
        findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
        SettingFragment sf = new SettingFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content, sf).commit();

    }

	@Override
    public void initData() {
        // TODO Auto-generated method stub
	    findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
        findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
        TextView title = (TextView) findViewById(R.id.titlebar_title);
        title.setText(R.string.titlebar_title_settings);
        findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
        findViewById(R.id.titlebar_applist_button_container).setVisibility(View.INVISIBLE);
        Util.addListData(maContext, LogModel.L_SETTINGS);
    }

	@Override
    public void addEvent() {
	    findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
    }

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
//		boolean flag = false;
//		// 后退按钮
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			finishWindow();
//		}
//		return flag;
//	}

	/**
	 * 点击事件处理
	 */
	@Override
	public void onClick(View v) {
	    switch(v.getId()){
	        case R.id.titlebar_back_arrow:
	            finish();
	            break;
	    }
	}
	/** 检查版本更新移到设置下 start **/	
	public void checkClientUpdateInSetting(){
		if (NetworkUtil.isNetWorking(this)) {
			ProtocolTask updateClient = new ProtocolTask(this);
			updateClient.setListener(new TaskListener() {

				@Override
				public void onNoNetworking() {

				}

				@Override
				public void onNetworkingError() {

				}

				@Override
				public void onPostExecute(byte[] bytes) {
					try {
						isupdatelocal = false;
						if (bytes != null) {
							JSONObject result = new JSONObject(
									new String(bytes));
							int status = JsonParse.parseResultStatus(result);
							int count = result.getInt("allCount");

							if (status == 1 && count > 0) {	
								parserUpdate((JSONObject) result.getJSONArray(
										"data").get(0));
							} 
							else 
							{
								Toast.makeText(MyApplication.getInstance(), R.string.newvbb,Toast.LENGTH_SHORT).show();	
							}
						} 
						else 
						{
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.gc();
				}

			});
			String userName=SharedPrefManager.getLastLoginUserName(this);
			try {
				int code = getPackageManager().getPackageInfo(getPackageName(),
						0).versionCode;
				updateClient.execute(Constants.LIST_URL
						+ "?qt=uclient&versioncode=" + code+ "&uid=" + userName, null, tag(), getHeader(SettingsActivity.this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private MarketUpdateInfo marketUpdateInfo;
	public void parserUpdate(JSONObject update) throws Exception {
		if (update == null)
			return;
		this.marketUpdateInfo = new MarketUpdateInfo();
		if (!update.isNull("CLVNAME")) {
			this.marketUpdateInfo.vname = update.getString("CLVNAME");
		}
		if (!update.isNull("CLCODE")) {
			this.marketUpdateInfo.vcode = update.getInt("CLCODE");
		}
		if (!update.isNull("CLSIZE")) {
			this.marketUpdateInfo.length = update.getLong("CLSIZE");
		}
		if (!update.isNull("CLUPDATE")) {
			this.marketUpdateInfo.updatetime = update.getString("CLUPDATE");
		}
		if (!update.isNull("APK")) {
			this.marketUpdateInfo.apk = update.getString("APK");
		}
		if (!update.isNull("UPINFO")) {
			this.marketUpdateInfo.describe = update.getString("UPINFO");
		}
		if (!update.isNull("RIGHT")) {
			this.marketUpdateInfo.right = update.getInt("RIGHT");
		}
		showNoRootUpdate(marketUpdateInfo);
	}
	
	public void showNoRootUpdate(final MarketUpdateInfo marketUpdateInfo) {
		Builder builder = new Builder(this);
		builder.setTitle(getString(R.string.updateversionprom));
		StringBuilder sb = new StringBuilder(getString(R.string.updateexpain)
				+ "\n\n");
		sb.append(getString(R.string.updateversion) + marketUpdateInfo.vname
				+ "\n");
		sb.append(marketUpdateInfo.describe + "\n\n");
		sb.append(getString(R.string.softwaresize)
				+ Util.apkSizeFormat(
						(marketUpdateInfo.length * 1.0) / 1024 / 1024, "M")
				+ "\n");
		sb.append(getString(R.string.isupdate));
		builder.setMessage(sb.toString());
		builder.setNegativeButton(getString(R.string.state_update_action_text),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						apkPath=marketUpdateInfo.apk;
						updateNow(marketUpdateInfo);
					}
				});
		builder.setPositiveButton(R.string.later,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		builder.create().show();

	}	
	
	public void updateNow(MarketUpdateInfo marketUpdateInfo) {
		if (apkPath.startsWith("http://")) {
			apkPath = marketUpdateInfo.apk;
		}else{
			apkPath = Constants.APK_URL + marketUpdateInfo.apk;
		}
		if (apkPath != null) {
			Intent intent = new Intent(this, UpdateDownloadService.class);
			startService(intent); //
			// 如果先调用startService,则在多个服务绑定对象调用unbindService后服务仍不会被销毁
			if (binder == null)
				bindService(intent, conn, Context.BIND_AUTO_CREATE);
			else
				binder.start(apkPath);
		}
	}
	public HashMap<String, String> getHeader(Context context) {
		String model = Util.mobile;
		String imei = Util.imie;
    	String vcode = Util.vcode;
    	String channel = Util.channel;
		if (model == null)
			model = Util.getModel();
		if (imei == null)
			imei = Util.getIMEI(context);
		if (vcode == null)
			vcode = Util.getVcode(context);
		if (TextUtils.isEmpty(channel)){
			channel = Util.getChannelName(context);
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("model", model);
		map.put("channel", channel);
		return map;
	}
	private Object tag() {
		return getClass().getSimpleName();
	}
	/** 检查版本更新移到设置下 end **/	
}
