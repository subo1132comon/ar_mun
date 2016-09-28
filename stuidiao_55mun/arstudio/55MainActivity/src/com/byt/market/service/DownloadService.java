package com.byt.market.service;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadItem;
import com.byt.market.download.ConnectionListener;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.download.DownloadUtils;
import com.byt.market.download.NetworkStatus;
import com.byt.market.log.LogModel;
import com.byt.market.net.NetworkUtil;
import com.byt.market.util.LogUtil;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.Util;

public class DownloadService extends Service {

	public static final String EXT_APPITEM = "EXT_APPITEM";
	public static final String EXT_COMMAND = "EXT_COMMAND";

	public static final int COMMAND_USER_CLICK = 1;

	// private List<DownloadTask> mDownloadTasks = new
	// ArrayList<DownloadTask>();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public class DownloadBinder extends Binder {

		public DownloadService getService() {
			return DownloadService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			final AppItem item = intent.getParcelableExtra(EXT_APPITEM);
			final int command = intent.getIntExtra(EXT_COMMAND, 0);
			if (COMMAND_USER_CLICK == command) {
				userClicked(item);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void userClicked(AppItem item) {
		DownloadTask task = DownloadTaskManager.getInstance().getDownloadTask(
				MyApplication.getInstance().getApplicationContext(),
				String.valueOf(item.sid), String.valueOf(item.vcode));
		if (task != null) {
			switch (task.mDownloadItem.state) {
			case AppItemState.STATE_WAIT: {
				DownloadTaskManager.getInstance().pauseDownloadTask(task);
				break;
			}
			case AppItemState.STATE_RETRY: {
				// startOrResumeDownload(task,item);
				showRetryDialog(task);
				break;
			}
			case AppItemState.STATE_ONGOING: {
				DownloadTaskManager.getInstance().pauseDownloadTask(task);
				break;
			}
			case AppItemState.STATE_PAUSE: {
				startOrResumeDownload(task, item, true);
				break;
			}
			case AppItemState.STATE_DOWNLOAD_FINISH: {
				DownloadTaskManager.getInstance().openDownloadFile(task, false);
				break;
			}
			case AppItemState.STATE_INSTALLING: {
				// do nothing
				break;
			}
			case AppItemState.STATE_INSTALL_FAILED: {
				DownloadTaskManager.getInstance().openDownloadFile(task, false);
				break;
			}
			case AppItemState.STATE_UNINSTALLING: {
				// do nothing
				break;
			}
			case AppItemState.STATE_NEED_UPDATE: {
//                String path = DownloadTaskManager.getInstance().getApkFilePath(item);
//                File file = new File(path);
//                if(file.exists()){
//                    LogUtil.i("appupdate","touch AppItemState.STATE_NEED_UPDATE and delete");
//                    file.delete();
//                }
				startOrResumeDownload(task, item, true);
				break;
			}
			case AppItemState.STATE_IDLE: {
				startOrResumeDownload(task, item, false);
				break;
			}
			case AppItemState.STATE_INSTALLED_NEW: {
				// Util.killApps(this);//等之后有需求再加
				item.isOpenned = 1;
				DownloadTaskManager.getInstance().updateAppItem(item);
				com.byt.market.util.PackageUtil.startApp(
						MyApplication.getInstance().getApplicationContext(),
						item.pname);
				sendBroadcast(new Intent(Constants.Action.REFRESH_LOCAL_GAME));
				break;
			}
			}
		} else {
			if (item.state == AppItemState.STATE_INSTALLED_NEW) {
				Util.killApps(this);
				item.isOpenned = 1;
				DownloadTaskManager.getInstance().updateAppItem(item);
				com.byt.market.util.PackageUtil.startApp(
						MyApplication.getInstance().getApplicationContext(),
						item.pname);
				sendBroadcast(new Intent(Constants.Action.REFRESH_LOCAL_GAME));
			} else {
				startOrResumeDownload(null, item, false);
			}

		}
	}

	private void startOrResumeDownload(DownloadTask task, AppItem item,
			boolean resume) {
		if (NetworkUtil.isNetWorking(this)) {
			if (NetworkStatus.getInstance(this).getNetWorkState() != ConnectionListener.CONN_WIFI
					&& SharedPrefManager
							.getDownloadTipsInNonWifiEnvironment(this)) {
				showNoWifiDialog(task, item, resume);
			} else {
				doStartOrResumeDownload(task, item, resume);
			}
		} else {
			Toast.makeText(this, getString(R.string.toast_no_network),
					Toast.LENGTH_SHORT).show();
			return;
		}
	}

	private void doStartOrResumeDownload(DownloadTask task, AppItem item,
			boolean resume) {

		if (task == null) {
            LogUtil.i("appupdate","doStartOrResumeDownload task == null");
			LogUtil.i("appupdate", "task == null");
			task = new DownloadTask();
			DownloadItem downloadItem = new DownloadItem();
			downloadItem.fill(item);
			task.mDownloadItem = downloadItem;
		}

		if (resume) {
            LogUtil.i("appupdate","doStartOrResumeDownload task state = "+task.mDownloadItem.state);
			LogUtil.i("appupdate", "task state = " + task.mDownloadItem.state);
			DownloadTaskManager.getInstance().goOnDownloadTask(task);
		} else {
			DownloadTaskManager.getInstance().addDownloadTask(task);
		}
	}

	public void showRetryDialog(DownloadTask item) {
		final RetryDialogListener listener = new RetryDialogListener(item);
		final AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_retry_title)
				.setMessage(
						this.getString(R.string.dialog_retry_msg,
								item.mDownloadItem.name))
				.setPositiveButton(R.string.dialog_retry_btn_ok, listener)
				.setNegativeButton(R.string.dialog_retry_btn_back, null)				
				.create();
		dialog.getWindow()
				.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}

	public void showNoWifiDialog(DownloadTask item, AppItem appItem,
			final boolean resume) {
		if (item == null) {
			item = new DownloadTask();
			DownloadItem downloadItem = new DownloadItem();
			downloadItem.fill(appItem);
			item.mDownloadItem = downloadItem;
		}
		final DownloadTask downloadTask = item;
		final Dialog dialog = new Dialog(this, R.style.DialogTheme);
		// 设置它的ContentView
		View view = LayoutInflater.from(this).inflate(
				R.layout.dialog_no_wifi_download_tips, null);
		final ImageView check_select_all = (ImageView) view
				.findViewById(R.id.check_select_all);
		final LinearLayout btn_select_all = (LinearLayout) view
				.findViewById(R.id.btn_select_all);
		btn_select_all.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				check_select_all.setSelected(!check_select_all.isSelected());
			}
		});
		Button btn_go_on = (Button) view.findViewById(R.id.btn_go_on);
		btn_go_on.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (check_select_all.isSelected()) {
					SharedPrefManager.setDownloadTipsInNonWifiEnvironment(
							DownloadService.this, false);
				}
				DownloadService.this.doStartOrResumeDownload(downloadTask,
						downloadTask.mDownloadItem, resume);
				dialog.dismiss();
			}
		});
		Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		dialog.setContentView(view);
		dialog.getWindow()
				.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}

	private class RetryDialogListener implements
			DialogInterface.OnClickListener {
		DownloadTask mDownloadTask;

		public RetryDialogListener(DownloadTask item) {
			mDownloadTask = item;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch (which) {
			case Dialog.BUTTON_POSITIVE: {
				final File file = new File(
						DownloadUtils
								.getTempFileDownloadPath(mDownloadTask.mDownloadItem));
				if (file.exists()) {
					file.delete();
				}
				DownloadTaskManager.getInstance().updateTaskInfoBeforeRetry(
						mDownloadTask);
				DownloadService.this.startOrResumeDownload(mDownloadTask,
						mDownloadTask.mDownloadItem, false);
				break;
			}
			case Dialog.BUTTON_NEGATIVE: {
				dialog.dismiss();
				break;
			}
			case Dialog.BUTTON_NEUTRAL: {
				DownloadTaskManager.getInstance().openDownloadFile(
						mDownloadTask, true);
				break;
			}
			}
		}

	}

	private class NoWifiDialogListener implements
			DialogInterface.OnClickListener {
		DownloadTask mDownloadTask;
		boolean mResume;

		public NoWifiDialogListener(DownloadTask item, boolean resume) {
			mDownloadTask = item;
			mResume = resume;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch (which) {
			case Dialog.BUTTON_POSITIVE: {
				DownloadService.this.doStartOrResumeDownload(mDownloadTask,
						mDownloadTask.mDownloadItem, mResume);
				dialog.dismiss();
				break;
			}
			case Dialog.BUTTON_NEGATIVE: {
				dialog.dismiss();
				break;
			}
			case Dialog.BUTTON_NEUTRAL: {
				mDownloadTask.mDownloadItem.list_id = LogModel.L_DOWN_MANAGER;
				mDownloadTask.mDownloadItem.list_cateid = LogModel.L_APP_FAVOR;
				Util.addData(MarketContext.getInstance(),
						mDownloadTask.mDownloadItem);
				dialog.dismiss();
				break;
			}
			}
		}

	}
}
