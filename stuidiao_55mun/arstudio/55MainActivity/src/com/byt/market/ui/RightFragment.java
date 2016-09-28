package com.byt.market.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.R;
import com.byt.market.activity.AboutActivity;
import com.byt.market.activity.DownLoadManageActivity;
import com.byt.market.activity.FeedBackActivity;
import com.byt.market.activity.SettingsActivity;
import com.byt.market.activity.SoftwareUninstallActivity;
import com.byt.market.asynctask.FileCacehTask;
import com.byt.market.download.DownloadContent;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.net.NetworkUtil;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.ImageCache;
import com.byt.market.util.LogUtil;
import com.byt.market.util.StorageUtil;

/**
 * 应用设置界面
 */
public class RightFragment extends BaseUIFragment implements OnClickListener,
//		UmengUpdateListener,
		DownloadTaskListener {

	// views
	private View v_set_app;
	private View v_update_app;
	private View v_clean_app;
	private View v_suggest_app;
	private View v_uninstall_app;
	private View v_about_app;
	private View v_download_app;
	private View v_update_mamager_app;
	private TextView tv_pop_down_manager;
	private TextView tv_pop_update;
	/** 手机大小使用情况 **/
	private TextView tv_phone_storage_status;
	/** sdcard大小使用情况 **/
	private TextView tv_scdcard_status;
	/** 手机大小使用情况进度条 **/
	private ProgressBar progressbar_rom;
	/** sdcard大小使用情况进度条 **/
	private ProgressBar progressbar_sdcard;

	// variables
	private int allDownloadCount, allUpdateCount;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	};

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.settings, null);
		initView(view);
		bindData();
		addEvent();
		return view;
	}

	@Override
	public void onPause() {
		super.onPause();
		// 这时巨坑呀,加上它会导致主界面下载状态失灵,而且还有导致TAB切换也有问题
		// DownloadTaskManager.getInstance().removeListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		LogUtil.i("0425", "right fr onresuem");
		// DownloadTaskManager.getInstance().addListener(this);
		initDownLoadAndNeedUpdateData();
		bindPhoneStorageStatus();
		bindData();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private void initDownLoadAndNeedUpdateData() {
		allDownloadCount = DownloadTaskManager.getInstance()
				.getAllDowoLoadCount();
		allUpdateCount = DownloadTaskManager.getInstance()
				.getNeedUpdateAppCount();
		if (allDownloadCount > 0) {
			tv_pop_down_manager.setVisibility(View.VISIBLE);
			tv_pop_down_manager.setText(String.valueOf(allDownloadCount));
		} else {
			tv_pop_down_manager.setVisibility(View.GONE);
		}

		if (allUpdateCount > 0) {
			tv_pop_update.setVisibility(View.VISIBLE);
			tv_pop_update.setText(String.valueOf(allUpdateCount));
		} else {
			tv_pop_update.setVisibility(View.GONE);
		}

		LogUtil.i("0425", "initdata allDownloadCount = " + allDownloadCount
				+ ",allUpdateCount = " + allUpdateCount);
	}

	private void initView(View view) {
		v_set_app = view.findViewById(R.id.tv_set_app);
		//v_update_app = view.findViewById(R.id.tv_update_app);
		v_clean_app = view.findViewById(R.id.tv_clean_app);
		//v_suggest_app = view.findViewById(R.id.tv_suggest_app);
		v_uninstall_app = view.findViewById(R.id.img_uninstall_app);
		v_about_app = view.findViewById(R.id.tv_about_app);
		v_download_app = view.findViewById(R.id.img_down);
		v_update_mamager_app = view.findViewById(R.id.img_update);
		tv_pop_down_manager = (TextView) view
				.findViewById(R.id.tv_pop_down_manager);
		tv_pop_update = (TextView) view.findViewById(R.id.tv_pop_update);
		tv_phone_storage_status = (TextView) view
				.findViewById(R.id.tv_phone_storage_status);
		tv_scdcard_status = (TextView) view
				.findViewById(R.id.tv_scdcard_status);
		progressbar_rom = (ProgressBar) view.findViewById(R.id.progressbar_rom);
		progressbar_sdcard = (ProgressBar) view
				.findViewById(R.id.progressbar_sdcard);
	}

	private void bindData() {
		bindPhoneStorageStatus();
	} 

	private void addEvent() {
		v_set_app.setOnClickListener(this);
		//v_update_app.setOnClickListener(this);
		v_clean_app.setOnClickListener(this);
		//v_suggest_app.setOnClickListener(this);
		v_uninstall_app.setOnClickListener(this);
		v_about_app.setOnClickListener(this);
		v_download_app.setOnClickListener(this);
		v_update_mamager_app.setOnClickListener(this);
	}

	private void bindPhoneStorageStatus() {
		long phoneTotalMemorySize = (long) StorageUtil
				.getTotalInternalMemorySize();
		long sdTotalMemorySize = (long) StorageUtil
				.getTotalExternalMemorySize();
		long phoneUsedMemorySize = phoneTotalMemorySize
				- (long) StorageUtil.getAvailableInternalMemorySize();
		long sdUsedMemorySize = sdTotalMemorySize
				- (long) StorageUtil.getAvailableExternalMemorySize();
		int phTotalKB = (int) (phoneTotalMemorySize / 1024);
		int phUsedKB = (int) (phoneUsedMemorySize / 1024);
		int sdTotalKB = (int) (sdTotalMemorySize / 1024);
		int sdUsedKB = (int) (sdUsedMemorySize / 1024);
		/*add by "zengxiao"*/
		int phaviKB=(int) (phoneTotalMemorySize-phoneUsedMemorySize);
		int SDaviKB=(int)(sdTotalMemorySize-sdUsedMemorySize);	
		/*add end*/
		progressbar_rom.setMax(phTotalKB);
		progressbar_sdcard.setMax(sdTotalKB);

		if (phoneUsedMemorySize > phoneTotalMemorySize / 3 * 2) {
			progressbar_rom.setSecondaryProgress(phUsedKB);
		} else {
			progressbar_rom.setProgress(phUsedKB);
		}
		if (sdUsedMemorySize > sdTotalMemorySize / 3 * 2) {
			progressbar_sdcard.setSecondaryProgress(sdUsedKB);
		} else {
			progressbar_sdcard.setProgress(sdUsedKB);
		}

		tv_phone_storage_status.setText(getString(R.string.storage_fomat,
				StorageUtil.formatSize(phoneUsedMemorySize),
				StorageUtil.formatSize(phaviKB)));
			if (!StorageUtil.externalMemoryAvailable()) {
			tv_scdcard_status.setText(R.string.storage_unaviable);
		} else {
			tv_scdcard_status.setText(getString(R.string.storage_fomat,
					StorageUtil.formatSize(sdUsedMemorySize),
					StorageUtil.formatSize(StorageUtil.getAvailableExternalMemorySize())));
		}
		
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.tv_set_app) {
			startActivity(new Intent(this.getActivity(), SettingsActivity.class));
			this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
		}/* else if (v.getId() == R.id.tv_update_app) {
			if (!NetworkUtil.isNetWorking(getActivity())) {
				showShortToast(getString(R.string.toast_no_network));
				return;
			}
//			UmengUpdateAgent.setUpdateOnlyWifi(false);
			showShortToast(getString(R.string.scannowversion));
//			UmengUpdateAgent.update(getActivity());
//			UmengUpdateAgent.setUpdateListener(this);
		} */else if (v.getId() == R.id.tv_clean_app) {
			new FileCacehTask(getActivity()).execute();
		} /*else if (v.getId() == R.id.tv_suggest_app) {
			startActivity(new Intent(this.getActivity(), FeedBackActivity.class));
			this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
		} */else if (v.getId() == R.id.tv_about_app) {
			startActivity(new Intent(this.getActivity(), AboutActivity.class));
			this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
		} else if (v.getId() == R.id.img_uninstall_app) {
			startActivity(new Intent(this.getActivity(),
					SoftwareUninstallActivity.class));
			this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
		} else if (v.getId() == R.id.img_down) {
			Intent downloadIntent = new Intent(getActivity(),
					DownLoadManageActivity.class);
			downloadIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
					DownLoadManageActivity.TYPE_FROM_DOWNLOAD);
			startActivity(downloadIntent);
			this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
		} else if (v.getId() == R.id.img_update) {
			Intent updateIntent = new Intent(getActivity(),
					DownLoadManageActivity.class);
			updateIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
					DownLoadManageActivity.TYPE_FROM_UPDATE);
			updateIntent.putExtra(DownLoadManageActivity.ALL_UPDATE_COUNT,
					allUpdateCount);
			startActivity(updateIntent);
			this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ImageCache.getInstance().clearCache();
	}

//	@Override
//	public void onUpdateReturned(int updateSatus, UpdateResponse updateResponse) {
//		switch (updateSatus) {
//		case UpdateStatus.Yes:// 发现更新
//
//			break;
//		case UpdateStatus.No:// 没有更新
//            //加判断，有可能空指针
//            if(getActivity() != null){
//                Toast.makeText(getActivity(), "没有更新", Toast.LENGTH_SHORT).show();
//            }
//			break;
//		}
//	}

	@Override
	public void startConnecting(DownloadContent.DownloadTask task,
			int totalTask, int progressTask) {

	}

	@Override
	public void downloadStarted(DownloadContent.DownloadTask task,
			int totalTask, int progressTask, long totalSize) {
		initDownLoadAndNeedUpdateData();
	}

	@Override
	public void downloadProgress(DownloadContent.DownloadTask task,
			int totalTask, int progressTask, long progressSize, long totalSize) {

	}

	@Override
	public void downloadEnded(DownloadContent.DownloadTask task, int totalTask,
			int progressTask) {

	}

	@Override
	public void endConnecting(DownloadContent.DownloadTask task, int totalTask,
			int progressTask, DownloadException result) {

	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {

	}

	@Override
	public void refreshUI() {

	}

	@Override
	public void unInstalledSucess(String packageName) {

	}

	@Override
	public void installedSucess(DownloadTask downloadTask) {

	}

	@Override
	public void networkIsOk() {
		// TODO Auto-generated method stub

	}

	@Override
	public void installedSucess(String packageName) {
		// TODO Auto-generated method stub
		
	}
}
