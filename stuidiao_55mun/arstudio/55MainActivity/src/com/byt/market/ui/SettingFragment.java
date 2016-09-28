package com.byt.market.ui;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.Constants.Settings;
import com.byt.market.R;
import com.byt.market.activity.AboutActivity;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.SettingsActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadAppItem;
import com.byt.market.data.DownloadItem;
import com.byt.market.data.MarketUpdateInfo;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.download.DownloadUtils;
import com.byt.market.net.NetworkUtil;
import com.byt.market.service.UpdateDownloadService;
import com.byt.market.tools.DownLoadVdioapkTools;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.DownloadUIUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LogUtil;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.Singinstents;
import com.byt.market.util.Util;

/**
 * 设置
 */
public class SettingFragment extends BaseUIFragment implements OnClickListener {

	// constants
	private static final int MSG_DELETEING_APK = 1;
	private static final int MSG_DELETE_APK_OVER = 2;

	/** 非wifi网络下载提示 **/
	private RelativeLayout lay_set_download_notification;
	private CheckBox cb_set_download_notification;
	/** 显示图标和截图 **/
	private RelativeLayout lay_set_display_icon;
	private CheckBox cb_set_display_icon;
	/** 下载完成立即进行安装 **/
	private RelativeLayout lay_set_download_to_install;
	private CheckBox cb_set_download_to_install;
	/** ROOT用户快速安装 **/
	private RelativeLayout lay_set_root_install;
	private CheckBox cb_set_root_install;
	/** 安装后删除安装包 **/
	private RelativeLayout lay_set_installed_delete_apk;
	private CheckBox cb_set_installed_delete_apk;
	/** 安装完成提示 **/
	private RelativeLayout lay_set_installed_notification;
	private CheckBox cb_set_installed_notification;
	/** 删除全部安装包 **/
	private RelativeLayout lay_set_delete_all_apk;
	/** 当前同时下载数量 **/
	private Button btn_set_down_sum_input;
	private Dialog deleteAllApkDialog;
	private Dialog deleteingAllApkDialog;
	/** 检查版本更新**/
	private RelativeLayout appupdatecheck;
	private ImageView update_app_flag;
	/** 关于**/
	private RelativeLayout lay_set_about_app;
	private TextView clearcathlen;
	private RelativeLayout tvpaly_dowlod;
	
	
	
	// variables
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_DELETEING_APK:
				if (deleteingAllApkDialog != null) {
					deleteingAllApkDialog.show();
				}
				break;
			case MSG_DELETE_APK_OVER:
				if (deleteingAllApkDialog != null) {
					deleteingAllApkDialog.dismiss();					
				}
				File file=new File(MyApplication.DATA_DIR+ File.separator );
				Double lenth=getDirSize(file);
				DecimalFormat    df   = new DecimalFormat("######0.0");  
				clearcathlen.setText(df.format(lenth)+" M");
				break;
			}
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.settings_fragment, container,
				false);
		initViews(view);
		initDatas();
		initListeners();
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Constants.Settings.readSettings(getActivity());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (deleteAllApkDialog != null && deleteAllApkDialog.isShowing()) {
			deleteAllApkDialog.dismiss();
			deleteAllApkDialog = null;
		}
		if (deleteingAllApkDialog != null && deleteingAllApkDialog.isShowing()) {
			deleteingAllApkDialog.dismiss();
			deleteingAllApkDialog = null;
		}
	}

	private void initViews(View rootView) {
		lay_set_download_notification = (RelativeLayout) rootView
				.findViewById(R.id.lay_set_download_notification);
		cb_set_download_notification = (CheckBox) rootView
				.findViewById(R.id.cb_set_download_notification);
		lay_set_display_icon = (RelativeLayout) rootView
				.findViewById(R.id.lay_set_display_icon);
		cb_set_display_icon = (CheckBox) rootView
				.findViewById(R.id.cb_set_display_icon);
		lay_set_download_to_install = (RelativeLayout) rootView
				.findViewById(R.id.lay_set_download_to_install);
		cb_set_download_to_install = (CheckBox) rootView
				.findViewById(R.id.cb_set_download_to_install);
		lay_set_root_install = (RelativeLayout) rootView
				.findViewById(R.id.lay_set_root_install);
		cb_set_root_install = (CheckBox) rootView
				.findViewById(R.id.cb_set_root_install);
		lay_set_installed_delete_apk = (RelativeLayout) rootView
				.findViewById(R.id.lay_set_installed_delete_apk);
		cb_set_installed_delete_apk = (CheckBox) rootView
				.findViewById(R.id.cb_set_installed_delete_apk);
		lay_set_installed_notification = (RelativeLayout) rootView
				.findViewById(R.id.lay_set_installed_notification);
		cb_set_installed_notification = (CheckBox) rootView
				.findViewById(R.id.cb_set_installed_notification);
		lay_set_delete_all_apk = (RelativeLayout) rootView
				.findViewById(R.id.lay_set_delete_all_apk);
		btn_set_down_sum_input = (Button) rootView
				.findViewById(R.id.btn_set_down_sum_input);
		deleteingAllApkDialog = createDialog(
				getString(R.string.dialog_title_hint),
				getString(R.string.dialog_msg_deleteing));
		appupdatecheck = (RelativeLayout) rootView
				.findViewById(R.id.appupdatecheck);		
		update_app_flag = (ImageView) rootView
				.findViewById(R.id.update_app_flag);
		lay_set_about_app = (RelativeLayout) rootView
		.findViewById(R.id.lay_set_about_app);
		clearcathlen=(TextView) rootView.findViewById(R.id.clearcathlen);
		tvpaly_dowlod = (RelativeLayout) rootView.findViewById(R.id.lay_dowlod_pay);
	}

	private void initDatas() {
		cb_set_download_notification.setChecked(SharedPrefManager
				.getDownloadTipsInNonWifiEnvironment(getActivity()));
		cb_set_display_icon.setChecked(SharedPrefManager
				.isDisplayIconScreenshot(getActivity()));
		cb_set_download_to_install.setChecked(SharedPrefManager
				.isDownloadImmediatelyInstall(getActivity()));
		cb_set_root_install.setChecked(SharedPrefManager
				.isQuickInstallationInRootUser(getActivity()));
		cb_set_installed_delete_apk.setChecked(SharedPrefManager
				.isInstalledDeleteApk(getActivity()));
		cb_set_installed_notification.setChecked(SharedPrefManager
				.isInstalledNotification(getActivity()));
		btn_set_down_sum_input.setText(SharedPrefManager
				.getCurrentDownloadSum(getActivity()) + "");
		tvpaly_dowlod.setOnClickListener(this);
	if(MainActivity.update_need_show_info)
		{
			update_app_flag.setVisibility(View.VISIBLE);
		}
		else
		{
			update_app_flag.setVisibility(View.GONE);
			
		}
		File file=new File(MyApplication.DATA_DIR+ File.separator );
		Double lenth=getDirSize(file);//----------------------------------------------------
		DecimalFormat    df   = new DecimalFormat("######0.0");  
		clearcathlen.setText(df.format(lenth)+" M");
	}

	private void initListeners() {
		lay_set_download_notification.setOnClickListener(this);
		lay_set_display_icon.setOnClickListener(this);
		lay_set_download_to_install.setOnClickListener(this);
		lay_set_root_install.setOnClickListener(this);
		lay_set_installed_delete_apk.setOnClickListener(this);
		lay_set_installed_notification.setOnClickListener(this);
		lay_set_delete_all_apk.setOnClickListener(this);
		appupdatecheck.setOnClickListener(this);
		lay_set_about_app.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lay_set_download_notification:
			setDownloadNotificationStatus();
			break;
		case R.id.lay_set_display_icon:
			setDisplayIconStatus();
			break;
		case R.id.lay_set_download_to_install:
			setDownloadToInstallStatus();
			break;
		case R.id.lay_set_root_install:
			setRootInstallStatus();
			break;
		case R.id.lay_set_installed_delete_apk:
			setInstalledDeleteApkStatus();
			break;
		case R.id.lay_set_installed_notification:
			setInstalledNotificationStatus();
			break;
		case R.id.lay_set_delete_all_apk:
			doDeleteAllDownloadedApk();
			break;
		case R.id.appupdatecheck:	
			SettingsActivity activity;
			if(getActivity()!=null){
				activity=(SettingsActivity)getActivity();
				if(!activity.isupdatelocal)
				{
					activity.isupdatelocal = true;
					activity.checkClientUpdateInSetting();
				}
			}
						
			break;
		case R.id.lay_set_about_app:
			startActivity(new Intent(getActivity(), AboutActivity.class));
			break;
		case R.id.lay_dowlod_pay:
			dowLoadThepaly();
			break;
		}
	}

	private void setDownloadNotificationStatus() {
		cb_set_download_notification.setChecked(!cb_set_download_notification
				.isChecked());
		SharedPrefManager.setDownloadTipsInNonWifiEnvironment(getActivity(),
				cb_set_download_notification.isChecked());
	}

	private void setDisplayIconStatus() {
		cb_set_display_icon.setChecked(!cb_set_display_icon.isChecked());
		SharedPrefManager.setDisplayIconScreenshot(getActivity(),
				cb_set_display_icon.isChecked());
	}

	private void setDownloadToInstallStatus() {
		cb_set_download_to_install.setChecked(!cb_set_download_to_install
				.isChecked());
		SharedPrefManager.setDownloadImmediatelyInstall(getActivity(),
				cb_set_download_to_install.isChecked());
	}

	private void setRootInstallStatus() {
		cb_set_root_install.setChecked(!cb_set_root_install.isChecked());
		SharedPrefManager.setQuickInstallationInRootUser(getActivity(),
				cb_set_root_install.isChecked());
		Settings.quickInstall = cb_set_root_install.isChecked();
	}

	private void setInstalledDeleteApkStatus() {
		cb_set_installed_delete_apk.setChecked(!cb_set_installed_delete_apk
				.isChecked());
		SharedPrefManager.setInstalledDeleteApk(getActivity(),
				cb_set_installed_delete_apk.isChecked());
	}

	private void setInstalledNotificationStatus() {
		cb_set_installed_notification.setChecked(!cb_set_installed_notification
				.isChecked());
		SharedPrefManager.setInstalledNotification(getActivity(),
				cb_set_installed_notification.isChecked());
	}
	
	private void dowLoadThepaly(){
		DownLoadVdioapkTools dt = new DownLoadVdioapkTools(getActivity());
		//if(dt.checkApkExist(context, "com.tyb.fun.palyer")){
			Singinstents.getInstents().setVdiouri("");
			Singinstents.getInstents().setAppPackageName("com.tyb.fun.palyer");
			dt.showNoticeDialog();
		//}
	}

	private void doDeleteAllDownloadedApk() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_retry_title).setMessage(
				R.string.dialog_delete_confirm_tips);
		builder.setIcon(R.drawable.icon);
		builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				
				return true;
			}
		});
		builder.setPositiveButton(R.string.cancel,
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.setNegativeButton(
				R.string.ok,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						new DeleteDownloadedApkThread().start();
					}
				});
		deleteAllApkDialog = builder.create();
		deleteAllApkDialog.show();
	}

	/**
	 * 删除本地安装包任务
	 */
	private final class DeleteDownloadedApkThread extends Thread {
		@Override
		public void run() {
			handler.sendEmptyMessage(MSG_DELETEING_APK);
			try {
				Thread.sleep(1000);
				//add  by  bobo
				File file=new File(MyApplication.DATA_DIR+ File.separator );
				deleteFile(file);
				//add  end--------------------
				
				List<AppItem> allInstalledApps = DownloadTaskManager
						.getInstance().getAllInstalledApps();
				for (AppItem appItem : allInstalledApps) {
					if (appItem instanceof DownloadItem) {
						DownloadItem downloadItem = (DownloadItem) appItem;
						if (PackageUtil.isInstalledApk(getActivity(),
								downloadItem.pname, null)) {
							LogUtil.i("zc", "run for pname = "+downloadItem.pname);
							DownloadTaskManager.getInstance()
									.deleteDownlaodFile(downloadItem);
						}
					}
				}
				
				final List<AppItem> alllist = DownloadTaskManager.getInstance()
						.getAllDownloadItem(false);
				DownloadTaskManager.getInstance().fillAppStates(alllist);
				for (AppItem appItem : alllist) {
					// Log.i("zc","appItem name = "+appItem.name+",state = "+appItem.state);
					if (isDownloading(appItem.state)) {
						//do nothing 不删除正在下载的安装包
					} else {
						if (appItem instanceof DownloadItem) {
							DownloadItem downloadItem = (DownloadItem) appItem;
								LogUtil.i("zc", "run else for pname = "+downloadItem.pname);
								DownloadTaskManager.getInstance()
										.deleteDownlaodFile(downloadItem);
						}
					}
				}
				
				
			} catch (Exception exception) {
			}
			handler.sendEmptyMessage(MSG_DELETE_APK_OVER);
		}

	}
	/**
	 * 是否属于正在下载列表
	 * 
	 * @param state
	 * @return
	 */
	private boolean isDownloading(int state) {
		if (state < AppItemState.STATE_DOWNLOAD_FINISH
				&& state > AppItemState.STATE_IDLE) {
			return true;
		}
		return false;
	}

	/****/
	 public static double getDirSize(File file) {     
	        //判断文件是否存在     
	        if (file.exists()) {     
	            //如果是目录则递归计算其内容的总大小    
	            if (file.isDirectory()) {     
	                File[] children = file.listFiles();     
	                double size = 0;     
	                for (File f : children)     
	                    size += getDirSize(f);     
	                return size;     
	            } else {//如果是文件则直接返回其大小,以“兆”为单位   
	                double size = (double) file.length() / 1024 / 1024;        
	                return size;     
	            }     
	        } else {     
	            System.out.println("文件或者文件夹不存在，请检查路径是否正确！");     
	            return 0.0;     
	        }     
	    }   
	 
	 public static void deleteFile(File file){
		//判断文件是否存在     
	        if (file.exists()) {     
	            //如果是目录则递归计算其内容的总大小    
	            if (file.isDirectory()) {     
	                File[] children = file.listFiles();   
	                for (File f : children)     
	                    deleteFile(f);     
	            } else {//如果是文件则直接返回其大小,以“兆”为单位   
	                //double size = (double) file.length() / 1024 / 1024;  
	            	file.delete();
	            }     
	        } else {     
	            System.out.println("文件或者文件夹不存在，请检查路径是否正确！");     
	        }     
	 }
	
}
