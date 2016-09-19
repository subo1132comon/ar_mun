package com.byt.market.download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.BigItem;
import com.byt.market.data.DownloadAppItem;
import com.byt.market.data.DownloadItem;
import com.byt.market.data.GAME;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadContent.DownloadTaskColumn;
import com.byt.market.download.Downloader.DownloadListener;
import com.byt.market.log.LogModel;
import com.byt.market.net.NetworkUtil;
import com.byt.market.service.DownloadService;
import com.byt.market.ui.MineFragment;
import com.byt.market.util.DataUtil;
import com.byt.market.util.DateUtil;
import com.byt.market.util.FileCheckUtil;
import com.byt.market.util.FileUtil;
import com.byt.market.util.LocalGameDBManager;
import com.byt.market.util.LogUtil;
import com.byt.market.util.MakeFileHash;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.StringUtil;
import com.byt.market.util.SysNotifyUtil;
import com.byt.market.util.SystemUtil;
import com.byt.market.util.Util;

public class DownloadTaskManager implements ConnectionListener {

	public static final int FILE_TYPE_BACKGROUND = 1;
	public static final int FILE_TYPE_APK = 2;

	public static final int MAX_DOWNLOAD = 3;
	public static final String DOWNLOADING_FILE_EXT = ".temp";
	private boolean mSuccess;
	private boolean mdestroy;
	private boolean mApplicationExit;
	private boolean mstopTask = true;
	private Context mContext;

	private boolean mConnectionState;

	private int mTotalTask;
	private int mProgressTask;

	private DownloadListenerImpl mDownloadListener;
	private static DownloadTaskManager mInstance;
	private MyThreadPoolExecutor mThreadPool;
	private BlockingQueue<Runnable> mDownloadWorkQueue;

	/**
	 * 将所有DownloadTask放入内存
	 * */
	private HashMap<String, DownloadTask> mAllTasks;

	/**
	 * 存储所有已安装游戏的包名<pName,>
	 * */
	private HashMap<String, Integer> mInstalledGames;

	private ConcurrentHashMap<String, DownloadTaskInfo> downloadTasks = new ConcurrentHashMap<String, DownloadTaskInfo>();

	private ConcurrentHashMap<DownloadTaskListener, Object> mListenersMap = new ConcurrentHashMap<DownloadTaskListener, Object>();
	private Set<DownloadTaskListener> mListeners = mListenersMap.keySet();

	public synchronized static DownloadTaskManager getInstance() {
		if (mInstance == null) {
			mInstance = new DownloadTaskManager();
		}
		return mInstance;
	}

	public void initConnectPackageListener() {
		ConnectionMonitor.registerConnectionMonitor(this);
	}

	public void destoryConnectPackageListener() {
		ConnectionMonitor.unregisterConnectionMonitor(this);
	}

	/**
	 * 添加下载进度观察者
	 * 
	 * @param listener
	 */
	public void addListener(DownloadTaskListener listener) {
		mListenersMap.put(listener, this);
		listener.refreshUI();
	}

	/**
	 * 移除下载进度观察者
	 * 
	 * @param listener
	 */
	public void removeListener(DownloadTaskListener listener) {
		mListenersMap.remove(listener);
	}

	private DownloadTaskManager() {
		mContext = MyApplication.getInstance().getApplicationContext();
		mDownloadListener = new DownloadListenerImpl();
		mConnectionState = NetworkStatus.getInstance(mContext).isConnected();
		mInstalledGames = new HashMap<String, Integer>();
		mAllTasks = DownloadTask.getAllDownloadTasks(mContext, mInstalledGames);
	}

	public boolean isInstalled(String pkn) {
		return mInstalledGames.containsKey(pkn);
	}

	public void onDestroy() {
		applicationExit();
		resetTask();
		mdestroy = true;
		mInstance = null;
	}

	public void applicationExit() {
		stopDownloadTask();
		mApplicationExit = true;

		if (mThreadPool != null) {
			mThreadPool.shutdownNow();
			mThreadPool = null;
		}
	}

	public void stopDownloadTask() {
		mstopTask = true;
	}

	public synchronized void notifyAppInstalled(DownloadTask downloadTask) {
		if (mApplicationExit)
			return;
		updateTaskDB(downloadTask, AppItemState.STATE_INSTALLED_NEW);
		DownloadLog.debug(DownloadTaskManager.class, "notifyAppInstalled");
		for (DownloadTaskListener listener : mListeners) {
			listener.installedSucess(downloadTask);
		}
	}

	public synchronized void notifyAppUnstalled(String packageName) {
		if (mApplicationExit)
			return;
		for (DownloadTaskListener listener : mListeners) {
			listener.unInstalledSucess(packageName);
		}
	}

	/**
	 * 开启下载任务
	 */
	public synchronized void startDownloadTask(DownloadTask task) {
		String selection = DownloadTaskColumn.COLUMN_STATE + " < ?";
		String[] selectionArgs = new String[] { String
				.valueOf(AppItemState.STATE_PAUSE) };
		mTotalTask = DownloadContent.count(mContext, DownloadTask.CONTENT_URI,
				selection, selectionArgs);

		if (mProgressTask > 0) {
			mTotalTask += mProgressTask;
		}

		mstopTask = false;
		mApplicationExit = false;
		addTask(0, task);
		refreshUI();
	}

	/**
	 * 获得所有已安装的列表
	 */
	public List<AppItem> getAllInstalledApps() {
		List<AppItem> installedAppItems = new ArrayList<AppItem>();
		Iterator iterator = mAllTasks.keySet().iterator();
		if (iterator.hasNext()) {
			do {
				final DownloadItem item = mAllTasks.get(iterator.next()).mDownloadItem;
				if (PackageUtil.isInstalledApk(mContext, item.pname, null)) {
					installedAppItems.add(item);
				}
			} while (iterator.hasNext());
		}
		return installedAppItems;
	}

	/**
	 * 添加下载任务
	 */
	public synchronized void addDownloadTask(DownloadTask task) {
		if (task.mDownloadItem.apk == null)
			return;
		mTotalTask++;
		mAllTasks.put(String.valueOf(task.mDownloadItem.sid), task);
		DownloadTask.addDownloadTask(mContext, task, false);
		startDownloadTask(task);
		// TODO: 开始下载
		if (task.mDownloadItem.state == AppItemState.STATE_IDLE
				|| task.mDownloadItem.state == AppItemState.STATE_NEED_UPDATE)
			if (!task.mDownloadItem.pname.equals("com.byt.market"))
				updateDataDB(task.mDownloadItem.sid,
						task.mDownloadItem.list_id,
						task.mDownloadItem.list_cateid, 0);
	}

	public synchronized void addFav(AppItem item) {
		LogUtil.i("0425", "addFav item = " + item.state);
		if (item.state != AppItemState.STATE_IDLE)
			return;
		final DownloadTask task = new DownloadTask();
		task.downloadType = DownloadTaskManager.FILE_TYPE_APK;
		final DownloadItem downloadItem = new DownloadItem();
		downloadItem.fill(item);
		task.mDownloadItem = downloadItem;
		if (task.mDownloadItem.apk == null)
			return;
		mAllTasks.put(String.valueOf(task.mDownloadItem.sid), task);
		DownloadTask.addDownloadTask(mContext, task, false);
	}

	public synchronized void delFav(AppItem item) {
		final MarketContext context = MarketContext.getInstance();
		if (DataUtil.getInstance(mContext).hasFavor(item.sid)) {
			Util.delData(context, item.sid);
		}
	}

	/**
	 * 将本地已安装游戏插入数据库
	 * */
	public synchronized void addLocalGame(AppItem item) {
		if (mAllTasks.containsKey(String.valueOf(item.sid))
				|| DownloadTask.getDownloadTaskByPname(mContext, item.pname) != null) {
			// 已经存在
			return;
		}
		final DownloadTask task = new DownloadTask();
		task.downloadType = DownloadTaskManager.FILE_TYPE_APK;
		final DownloadItem downloadItem = new DownloadItem();
		downloadItem.fill(item);
		task.mDownloadItem = downloadItem;

		mAllTasks.put(String.valueOf(task.mDownloadItem.sid), task);
		DownloadTask.addDownloadTask(mContext, task, false);
		if (PackageUtil.isInstalledApk(MyApplication.getInstance(), item.pname,
				null)) {
			mInstalledGames.put(item.pname, 0);
		}
	}

	/**
	 * 暂停下载任务
	 */
	public synchronized void pauseDownloadTask(DownloadTask task) {
		long dowloadedSize = -1;
		boolean notifyMine = false;
		if (task.mDownloadItem.state == AppItemState.STATE_WAIT) {
			notifyMine = true;
		}
		/*
		 * if (mAllTasks.containsKey(String.valueOf(task.mDownloadItem.sid))) {
		 * mAllTasks
		 * .get(String.valueOf(task.mDownloadItem.sid)).mDownloadItem.state =
		 * AppItemState.STATE_PAUSE; dowloadedSize = mAllTasks.get(String
		 * .valueOf(task.mDownloadItem.sid)).mDownloadItem.lastDSize; }
		 * task.updateState(mContext, AppItemState.STATE_PAUSE, dowloadedSize);
		 */

		if (notifyMine) {
			// 重试状态时候暂停，“我的”事件特殊处理
			mineSpecilaCallBack(task);
		}
		refreshUI();
		if (mThreadPool != null) {
			cancelTask(task, AppItemState.STATE_PAUSE);
		}
	}

	/**
	 * 删除更新任务
	 */
	public synchronized void cancelDownloadTask(DownloadTask task) {
		/*
		 * if (mThreadPool != null) { cancelTask(task,
		 * AppItemState.STATE_NEED_UPDATE); }
		 */
		long dowloadedSize = -1;
		if (mAllTasks.containsKey(String.valueOf(task.mDownloadItem.sid))) {
			mAllTasks.get(String.valueOf(task.mDownloadItem.sid)).mDownloadItem.state = AppItemState.STATE_NEED_UPDATE;
			dowloadedSize = 0;
		}
		task.updateState(mContext, AppItemState.STATE_NEED_UPDATE,
				dowloadedSize);
		for (DownloadTaskListener listener : mListeners) {
			listener.endConnecting(task, mTotalTask, mProgressTask,
					new DownloadException(DownloadException.CODE_UNKNOWN));
		}
	}

	/**
	 * 安装中、安装失败、卸载中和卸载失败会调用
	 * */
	public synchronized void updateInstallState(DownloadTask task, int state) {
		if (mAllTasks.containsKey(String.valueOf(task.mDownloadItem.sid))) {
			mAllTasks.get(String.valueOf(task.mDownloadItem.sid)).mDownloadItem.state = state;
		}
		task.updateState(mContext, state, -1);
		for (DownloadTaskListener listener : mListeners) {

			listener.endConnecting(task, mTotalTask, mProgressTask,
					new DownloadException(DownloadException.CODE_UNKNOWN));
		}
	}

	/**
	 * 删除下载任务
	 * 
	 * @param task
	 */
	public synchronized void deleteDownloadTask(DownloadTask task,
			boolean notifyChanged, boolean isUninstall) {
		try{
		if (mThreadPool != null) {
			cancelTask(task, AppItemState.STATE_PAUSE);
		}
		if (mAllTasks.containsKey(String.valueOf(task.mDownloadItem.sid))) {
			mAllTasks.remove(String.valueOf(task.mDownloadItem.sid));
		}
		DownloadTask.deleteDownloadTask(mContext, task);
		/*
		 * 
		 * delete by zengxiao for:删除下载任务时不下载收藏
		 * 
		 * */
		//delFav(task.mDownloadItem);
		/*delete end*/
		if (notifyChanged) {
			refreshUI();
		}
		// TODO 下载删除
		String list_cate_id = task.mDownloadItem.list_cateid;
		String list_id = task.mDownloadItem.list_id;
		if (list_cate_id == null || list_id == null)
			return;

		if (!task.mDownloadItem.pname.equals("com.byt.market")
				&& ((list_cate_id.equals(LogModel.P_LIST) && !list_id
						.equals(LogModel.L_DOWN_MANAGER)) || (!list_cate_id
						.equals(LogModel.P_LIST) && list_id
						.equals(LogModel.L_DOWN_MANAGER)))
				&& (task.mDownloadItem.state != AppItemState.STATE_IDLE || isUninstall))
			updateDataDB(task.mDownloadItem.sid, task.mDownloadItem.list_id,
					task.mDownloadItem.list_cateid, 3);
		}
		catch(Exception e){}
	}

	/**
	 * 删除下载文件
	 * 
	 */
	public synchronized void deleteDownlaodFile(DownloadItem item) {
		File file = new File(DownloadUtils.getTempFileDownloadPath(item));
		if (file.exists()) {
			file.delete();
		} else {
			file = new File(DownloadUtils.getFileDownloadPath(item));
			LogUtil.i("zc", "item = "+item.name+",path = "+file.getAbsolutePath());
			file.delete();
		}
	}

	/**
	 * 继续指定应用
	 */
	public synchronized void goOnDownloadTask(DownloadTask task) {
		// 如果
		if (task.mDownloadItem.state == AppItemState.STATE_ONGOING) {
			return;
		}
		mTotalTask++;
		long dowloadedSize = -1;
		if (mAllTasks.containsKey(String.valueOf(task.mDownloadItem.sid))) {
			mAllTasks.get(String.valueOf(task.mDownloadItem.sid)).mDownloadItem.state = AppItemState.STATE_WAIT;
			dowloadedSize = mAllTasks.get(String
					.valueOf(task.mDownloadItem.sid)).mDownloadItem.lastDSize;
		}
		task.updateState(mContext, AppItemState.STATE_WAIT, dowloadedSize);
		startDownloadTask(task);
	}

	/**
	 * 取消指定下载任务
	 * 
	 * @param task
	 * @param state
	 */
	private synchronized void cancelTask(DownloadTask task, int state) {
		try{
		DownloadTaskInfo downloadTaskInfo = downloadTasks.get(getMapKey(task));
		cancelTask(downloadTaskInfo, state);
		}catch(Exception e){}
	}

	/**
	 * 取消所有下载任务
	 */
	public void cancelAllTasks(boolean deleteDownloadFile) {
		final List<DownloadTask> downloadTasks = DownloadTask
				.getAllDownningTasks(mContext);
		for (DownloadTask downloadTask : downloadTasks) {
			LogUtil.i("zc", "cancelAllTasks foreach pname = "
					+ downloadTask.mDownloadItem.pname + ",state = "
					+ downloadTask.mDownloadItem.state);
			/*add by zengxiao for:修改在删除任务时判断是否是已安装的任务*/
				DownloadTaskManager.getInstance().deleteDownloadTask(downloadTask,
					true, false);
			/*add by zengxiao for:修改在删除任务时判断是否是已安装的任务*/
		}
		if (deleteDownloadFile) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (DownloadTask downloadTask : downloadTasks) {

						deleteDownlaodFile(downloadTask.mDownloadItem);

					}
				}
			}).start();
		}
	}

	/**
	 * 暂停所有下载任务
	 */
	public synchronized void pauseAllTasks() {
		List<DownloadTask> downloadTasks = DownloadTask
				.getAllDownningTasks(mContext);
		for (DownloadTask downloadTask : downloadTasks) {
			pauseDownloadTask(downloadTask);
		}
	}

	/**
	 * 继续所有下载任务
	 */
	public synchronized void goOnDownloadAllTasks() {
		List<DownloadTask> downloadTasks = DownloadTask
				.getAllDownningTasks2(mContext);
		for (DownloadTask downloadTask : downloadTasks) {
			if(downloadTask.downloadType==AppItemState.STATE_RETRY)
			{
				onDownloadBtnClick(downloadTask.mDownloadItem);
			}else{			
			goOnDownloadTask(downloadTask);
			}
		}
	}

	private synchronized void cancelTask(DownloadTaskInfo taskInfo, int state) {
		if (taskInfo == null)
			return;
		taskInfo.isCancel = true;
		DownloadTask sDownloadTask = taskInfo.downloadTask;
		if (sDownloadTask.mDownloadItem.state == AppItemState.STATE_ONGOING) {
			Downloader httpDownloader = taskInfo.downloader;
			if (httpDownloader != null) {
				httpDownloader.cancel();
			}
			if (mProgressTask > 0) {
				mProgressTask--;
			}
			mTotalTask--;
			sDownloadTask.mDownloadItem.state = state;// Task.STATE_PAUSE;
		}
		mThreadPool.remove(taskInfo.downloadRunnable);
		downloadTasks.remove(getMapKey(taskInfo.downloadTask));
	}

	/**
	 * 当前任务是否已经取消下载
	 * 
	 * @param task
	 * @return
	 */
	public boolean isCancel(DownloadTaskInfo task) {
		if (task != null && task.isCancel)
			return true;
		if (mThreadPool == null || mThreadPool.getTaskCount() == 0)
			return true;
		if (mApplicationExit)
			return true;
		if (!mConnectionState)
			return true;
		return false;
	}

	public boolean isStop() {
		return mstopTask;
	}

	public void autoResume() {
		ContentValues values = new ContentValues();
		values.put(DownloadTask.COLUMN_STATE, AppItemState.STATE_WAIT);
		int updateCount = DownloadTask.update(mContext,
				DownloadTask.CONTENT_URI, values, /*DownloadTask.COLUMN_STATE
						+ " IS " + AppItemState.STATE_IDLE + " OR "
						+ */DownloadTask.COLUMN_STATE + " IS "
						+ AppItemState.STATE_PAUSE, null);
		mAllTasks = DownloadTask.getAllDownloadTasks(mContext, mInstalledGames);
		mstopTask = false;
		addTask(0, null);
	}

	/**
	 * 向线程池中添加下载任务
	 * 
	 * @param offset
	 *            数据库中的偏移
	 */
	private synchronized void addTask(final int offset, DownloadTask task) {
		final DownloadTaskInfo downloadTaskInfo = new DownloadTaskInfo();
		DownloadTask downloadTask = task == null ? DownloadTask
				.loadOneDownloaddingTask(mContext, offset) : task;
		LogUtil.i("appupdate", "touch add task");
		downloadTaskInfo.downloadTask = downloadTask;
		// LogUtil.i("appupdate","touch add task sid = "+downloadTaskInfo.downloadTask.mDownloadItem.sid+",name ="+downloadTaskInfo.downloadTask.mDownloadItem.name);
		if (downloadTask == null) {
			LogUtil.i("appupdate", "touch downloadTask == null");
		}
		if (downloadTask == null || !mConnectionState || mstopTask) {
			LogUtil.i("appupdate", "touch add task 1");
			downloadTaskDone();
			resetTask();
			return;
		}

		if (false/*
				 * mUserSettingInfo.downloadOnlyWifi &&
				 * !NetworkStatus.getInstance(mContext).isWiFiConnected()
				 */) {
			// 设置了仅wifi上传 不是wifi网络下个任务不开始 直接进入暂停状态
			updateTaskDB(downloadTaskInfo.downloadTask,
					AppItemState.STATE_PAUSE);
			return;
		}

		if (!addedDownloadArray(downloadTaskInfo)) {
			LogUtil.i("appupdate", "touch !addedDownloadArray");
			addTask(offset + 1, null);
			return;
		}

		downloadTaskInfo.downloadRunnable = new Runnable() {
			@Override
			public void run() {
				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				DownloadLog.debug(DownloadTaskManager.class,
						downloadTaskInfo.downloadTask.mDownloadItem.apk);
				if (downloadTaskInfo.downloadTask.mDownloadItem.apk
						.startsWith("http://")) {
					downloadTaskInfo.downloader = createHttpDownoader(
							downloadTaskInfo.downloadTask,
							downloadTaskInfo.downloadTask.mDownloadItem.apk,
							mDownloadListener);
					LogUtil.i(
							"appupdate",
							"downloadTaskInfo.downloadRunnable if url ="
									+ downloadTaskInfo.downloadTask.mDownloadItem.apk);
				} else {
					downloadTaskInfo.downloader = createHttpDownoader(
							downloadTaskInfo.downloadTask,
							Constants.APK_URL
									+ downloadTaskInfo.downloadTask.mDownloadItem.apk,
							mDownloadListener);
					LogUtil.i(
							"appupdate",
							"downloadTaskInfo.downloadRunnable else url ="
									+ Constants.APK_URL
									+ downloadTaskInfo.downloadTask.mDownloadItem.apk);
				}

				updateTaskDB(downloadTaskInfo.downloadTask,
						AppItemState.STATE_ONGOING);
				startConnecting(downloadTaskInfo.downloadTask);

				if (downloadPre(downloadTaskInfo)) {
					if (downloading(downloadTaskInfo)) {
						LogUtil.i(
								"appupdate",
								"touch downloading(downloadTaskInfo) name ="
										+ downloadTaskInfo.downloadTask.mDownloadItem.name);
						updateTaskDB(downloadTaskInfo.downloadTask,
								AppItemState.STATE_DOWNLOAD_FINISH);
						endConnecting(downloadTaskInfo.downloadTask, null);
					}
				}
			}
		};
		if (executeTask(downloadTaskInfo) && task == null) {
			LogUtil.i("appupdate", "touch add task 3");
			addTask(0, null);
		}
	}

	/**
	 * 添加下载任务到队列中
	 * 
	 * @return
	 */
	private synchronized boolean addedDownloadArray(
			DownloadTaskInfo downloadTaskInfo) {
		if (downloadTaskInfo != null && downloadTasks != null) {
			DownloadTaskInfo sTaskInfo = downloadTasks
					.get(getMapKey(downloadTaskInfo.downloadTask));
			if (sTaskInfo != null) {
				return false;
			}
		}
		return true;
	}

	private Downloader createHttpDownoader(DownloadTask task,
			String downloadUrl, DownloadListener listener) {
		Downloader sHttpDownload = DownloadController.getInstance()
				.downloadFile(task, downloadUrl, listener);
		sHttpDownload.setDownloadListener(mDownloadListener);
		return sHttpDownload;
	}

	/**
	 * 向线程池添加下载任务
	 */
	private synchronized boolean executeTask(DownloadTaskInfo downloadTaskInfo) {
		LogUtil.i("appupdate", "touch executeTask");
		if (mdestroy || downloadTaskInfo == null)
			return false;
		int poolSize = Constants.Settings.downloadNum > 0 ? Constants.Settings.downloadNum
				: MAX_DOWNLOAD;

		if (mThreadPool == null) {
			mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();
			mThreadPool = new MyThreadPoolExecutor(3, 3, 1000,
					TimeUnit.SECONDS, mDownloadWorkQueue);
		}

		if (downloadTasks.size() < poolSize) {
			try {
				mThreadPool.execute(downloadTaskInfo.downloadRunnable);
				downloadTasks.put(getMapKey(downloadTaskInfo.downloadTask),
						downloadTaskInfo);
				return true;
			} catch (Exception e) {
				LogUtil.i("appupdate", "touch executeTask if exception");
				LogUtil.i("appupdate",
						"run DownloadTaskManager e =" + e.getMessage());
				DownloadLog.error(DownloadTaskManager.class, "executeTask", e);
			}
		} else {
			if (mAllTasks.containsKey(String
					.valueOf(downloadTaskInfo.downloadTask.mDownloadItem.sid))) {
				LogUtil.i("appupdate", "touch executeTask else");
				mAllTasks
						.get(String
								.valueOf(downloadTaskInfo.downloadTask.mDownloadItem.sid)).mDownloadItem.state = AppItemState.STATE_WAIT;
			}
			downloadTaskInfo.downloadTask.updateState(mContext,
					AppItemState.STATE_WAIT, -1);
			// callback

			endConnecting(downloadTaskInfo.downloadTask, new DownloadException(
					DownloadException.CODE_UNKNOWN));
		}
		return false;
	}

	/**
	 * 更新数据库下载状态
	 * 
	 * @param downloadTask
	 * @param state
	 */
	private synchronized void updateTaskDB(DownloadTask downloadTask, int state) {
		if (downloadTask == null)
			return;

		switch (state) {
		case AppItemState.STATE_ONGOING:
			mProgressTask++;
			break;
		case AppItemState.STATE_PAUSE:
			mProgressTask--;
			break;
		case AppItemState.STATE_RETRY:
			if (mProgressTask > 0)
				mProgressTask--;
			break;
		case AppItemState.STATE_WAIT:
			if (mProgressTask > 0)
				mProgressTask--;
			break;
		}
		long dowloadedSize = -1;
		if (mAllTasks.containsKey(String
				.valueOf(downloadTask.mDownloadItem.sid))) {
			mAllTasks.get(String.valueOf(downloadTask.mDownloadItem.sid)).mDownloadItem.state = state;
			if (state == AppItemState.STATE_DOWNLOAD_FINISH) {
				mAllTasks.get(String.valueOf(downloadTask.mDownloadItem.sid)).mDownloadItem.lastDSize = mAllTasks
						.get(String.valueOf(downloadTask.mDownloadItem.sid)).mDownloadItem.dSize;
				dowloadedSize = mAllTasks.get(String
						.valueOf(downloadTask.mDownloadItem.sid)).mDownloadItem.dSize <= 0 ? mAllTasks
						.get(String.valueOf(downloadTask.mDownloadItem.sid)).mDownloadItem.length
						: mAllTasks.get(String
								.valueOf(downloadTask.mDownloadItem.sid)).mDownloadItem.dSize;
			} else {
				dowloadedSize = mAllTasks.get(String
						.valueOf(downloadTask.mDownloadItem.sid)).mDownloadItem.lastDSize;
			}
		}
		downloadTask.updateState(mContext, state, dowloadedSize);
	}

	public synchronized void updateTaskInfoBeforeRetry(DownloadTask downloadTask) {
		final int state = AppItemState.STATE_PAUSE;
		if (mAllTasks.containsKey(String
				.valueOf(downloadTask.mDownloadItem.sid))) {
			mAllTasks.get(String.valueOf(downloadTask.mDownloadItem.sid)).mDownloadItem.state = state;
			mAllTasks.get(String.valueOf(downloadTask.mDownloadItem.sid)).mDownloadItem.lastDSize = 0;
		}
		downloadTask.updateState(mContext, state, 0);
	}

	/**
	 * 下载前 各个条件的检测
	 * 
	 * @param downloadTaskInfo
	 * @return
	 */
	private boolean downloadPre(DownloadTaskInfo downloadTaskInfo) {
		if (downloadTaskInfo == null)
			return false;
		DownloadTask downloadTask = downloadTaskInfo.downloadTask;
		if (downloadTask == null)
			return false;
		DownloadLog.info(DownloadTaskManager.class,
				"downloadPre...... + taskname="
						+ downloadTask.mDownloadItem.name);

		if (downloadTask.mDownloadItem.sid < 0) {
			updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);

			endConnecting(downloadTask, new DownloadException(
					DownloadException.CODE_DOWNLOADFILE));
			return false;
		}

		if (!FileUtil.haveSDCard()) {
			MyApplication.getInstance().showToast(R.string.sdcard_unavailable);
			stopDownloadTask();
			updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
			endConnecting(downloadTask, new DownloadException(
					DownloadException.CODE_SDCARDAVAILABLE));
			return false;
		}
		try {
			File file = new File(
					DownloadUtils
							.getFileDownloadPath(downloadTaskInfo.downloadTask.mDownloadItem));
			if (file.exists()) {
				if (downloadTask.mDownloadItem.vcode > FileCheckUtil
						.getApkFileVcode(mContext, file.getAbsolutePath())) {
					file.delete();
				} else {
					updateTaskDB(downloadTask,
							AppItemState.STATE_DOWNLOAD_FINISH);
					endConnecting(downloadTask, null);
					return false;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		long taskSize = downloadTask.mDownloadItem.length;

		/*
		 * try {
		 * if(String.valueOf(downloadTask.mDownloadItem.length).contains("M")) {
		 * String temp =
		 * String.valueOf(downloadTask.mDownloadItem.lastDSize).substring(0,
		 * String.valueOf(downloadTask.mDownloadItem.lastDSize).indexOf("M"));
		 * double size = Double.valueOf(temp); taskSize = (long) (size * 1024 *
		 * 1024); } } catch (Exception e) {
		 * DownloadLog.error(DownloadTaskManager.class, "downloadPre", e); }
		 */

		if (DownloadUtils.getSDCardAvailableSpace() < taskSize) {
			MyApplication.getInstance().showToast(
					R.string.downloadfile_sdcard_no_more_space);
			updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
			endConnecting(downloadTask, new DownloadException(
					DownloadException.CODE_SDCARDSPACE));
			return false;
		}

		// 检查是否取消
		if (isCancel(downloadTaskInfo)) {
			updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
			endConnecting(downloadTask, new DownloadException(
					DownloadException.CODE_CANCELLED));
			return false;
		}

		return true;
	}

	/**
	 * 开始下载
	 * 
	 * @param downloadTaskInfo
	 * @return
	 */
	private boolean downloading(DownloadTaskInfo downloadTaskInfo) {
		int sflag = 0;
		if (downloadTaskInfo == null)
			return false;
		DownloadTask downloadTask = downloadTaskInfo.downloadTask;
		do {
			sflag++;
			if (downloadTask == null)
				return false;
			String downloadDir = DownloadUtils
					.getDownloadDirPath(downloadTaskInfo.downloadTask.downloadType);
			File file = new File(downloadDir);

			if (!file.exists()) {
				file.mkdirs();
			}

			FileOutputStream fos = null;
			DownloadItem downloadItem = downloadTaskInfo.downloadTask.mDownloadItem;
			try {
				if(downloadItem.oobpackage!=null&&!downloadItem.oobpackage.equals(""))
				{
					String path=Environment.getExternalStorageDirectory().getAbsolutePath().toString();	
					 file=new File(path+"/Android/obb");
					if(!file.isDirectory())
					{
						file.mkdirs();
					}
					File filemytmp=new File(path+"/Android/obb/"+downloadItem.oobpackage);
					if(!filemytmp.isDirectory()){
						filemytmp.mkdirs();
					}
					file =new File(path+"/Android/obb/"+downloadItem.oobpackage+"/"+downloadItem.sid+".temp");
				}else{
				file = new File(
						DownloadUtils.getTempFileDownloadPath(downloadItem));
				}
				/*
				 * downloadDir + File.separator + DownloadUtils
				 * .getFileNameFromDownloadTask
				 * (downloadTaskInfo.downloadTask.mDownloadItem) +
				 * DOWNLOADING_FILE_EXT)
				 */;

				boolean resumeDownload = checkResumeDownload(downloadTask, file);
				LogUtil.i("appupdate", "----resumeDownload-------"
						+ resumeDownload + "--- " + file);
				DownloadLog.info(DownloadTaskManager.class,
						"----resumeDownload-------" + resumeDownload + "--- "
								+ file);
				long Offset = 0;
				if (resumeDownload) {
					Offset = file.length();
					fos = new FileOutputStream(file, true);
				} else {
					fos = new FileOutputStream(file);
				}
				fos.getChannel().tryLock();
				if (isCancel(downloadTaskInfo)) {
					LogUtil.i("appupdate", "touch is isCancel(downloadTaskInfo");
					updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
					endConnecting(downloadTask, new DownloadException(
							DownloadException.CODE_CANCELLED));
					return false;
				}
				if (resumeDownload) {
					LogUtil.i("appupdate", "touch resumeDownload");
					downloadTaskInfo.downloader.resume(fos, Offset);
				} else {
					downloadTaskInfo.downloader.download(fos, -1);
				}
				LogUtil.i(
						"appupdate",
						"file.length() = "
								+ file.length()
								+ ",(TextUtils.isEmpty(downloadTask.mDownloadItem.hash) = "
								+ (TextUtils
										.isEmpty(downloadTask.mDownloadItem.hash)
										+ ",downloadTask.mDownloadItem.hash = "
										+ downloadTask.mDownloadItem.hash
										+ "  ,getAbsolutePath = " + MakeFileHash
											.getFileMD5(file.getAbsolutePath())));
				
				if (file.length() == downloadTaskInfo.downloader.getTotalSize()
						&& (TextUtils.isEmpty(downloadTask.mDownloadItem.hash) ? true
								: downloadTask.mDownloadItem.hash
										.equals(MakeFileHash.getFileMD5(file
												.getAbsolutePath())))) {
					DownloadItem downitem=downloadTaskInfo.downloadTask.mDownloadItem;
					File dest = new File(
							DownloadUtils
									.getFileDownloadPath(downitem));
					if(downitem.oobpackage!=null&&!downitem.oobpackage.equals(""))
					{
						String path=Environment.getExternalStorageDirectory().getAbsolutePath().toString();		
						String unzippath=path+"/Android/obb/"+downloadItem.oobpackage+"/"+downloadItem.sid+".temp";
						String newzippath=path+"/Android/obb/"+downloadItem.oobpackage+"/"+downloadItem.sid+".zip";
						File filetmp =new File(unzippath);
						File fileend =new File(newzippath);
						filetmp.renameTo(fileend);
						unzip(newzippath, path+"/Android/obb/"+downitem.oobpackage,downitem.oobpackage,downloadDir,downitem.sid, true);
					}
					/*
					 * downloadDir + File.separator + DownloadUtils
					 * .getFileNameFromDownloadTask
					 * (downloadTaskInfo.downloadTask.mDownloadItem));
					 */
					// TODO:下载完成比较hash值
					if (!downloadItem.pname.equals("com.byt.market"))
						updateDataDB(downloadItem.sid, downloadItem.list_id,
								downloadItem.list_cateid, 1);
					file.renameTo(dest);
					file = null;
					dest = null;
					return true;
				} else {
					LogUtil.i(
							"appupdate",
							"downloading: file size is underflow!!! fileSize="
									+ file.length()
									+ ", downloadSize="
									+ downloadTaskInfo.downloader
											.getTotalSize());
					DownloadLog.error(
							DownloadTaskManager.class,
							"downloading: file size is underflow!!! fileSize="
									+ file.length()
									+ ", downloadSize="
									+ downloadTaskInfo.downloader
											.getTotalSize());
					// TODO:下载失败文件不完整
					if (!downloadItem.pname.equals("com.byt.market"))
						updateDataDB(downloadItem.sid, downloadItem.list_id,
								downloadItem.list_cateid, 2);
					
					 /* if (file.exists()) { file.delete(); } file = null;*/
					 
					updateTaskDB(downloadTask, AppItemState.STATE_RETRY);
					LogUtil.i("appupdate",
							"touch updateTaskDB(downloadTask, AppItemState.STATE_RETRY)");

					endConnecting(downloadTask, new DownloadException(
							DownloadException.CODE_DOWNLOAD_FAILURE));
					return false;
				}
			} catch (FileNotFoundException e) {
				updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
				LogUtil.i(
						"appupdate",
						"touch FileNotFoundException and updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);");
				endConnecting(downloadTask, new DownloadException(
						DownloadException.CODE_DOWNLOADFILE));
				DownloadLog.error(DownloadTaskManager.class, "Downloading", e);
				return false;
			} catch (DownloadException e) {
				DownloadLog.debug(DownloadTaskManager.class,
						"DownloadException code=" + e.getCode());
				LogUtil.i("appupdate",
						"DownloadException e code = " + e.getCode());
				if (e.getCode() == DownloadException.CODE_HTTP) {
					updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
					endConnecting(downloadTask, e);
				} else if (e.getCode() == DownloadException.CODE_NETWORK_ERROR) {
					if (isCancel(downloadTaskInfo)) {
						updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
						endConnecting(downloadTask, new DownloadException(
								DownloadException.CODE_CANCELLED));
						return false;
					} else {
						updateTaskDB(downloadTask, AppItemState.STATE_WAIT);
						endConnecting(downloadTask, e);
					}
				} else if (e.getCode() == DownloadException.CODE_CANCELLED) {
					updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
					endConnecting(downloadTask, e);
				} else {
					updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
					endConnecting(downloadTask, e);
				}
				DownloadLog.error(DownloadTaskManager.class, "Downloading", e);
				return false;
			} catch (UnknownHostException e) {
				LogUtil.i("appupdate", "UnknownHostException e");
				if (isCancel(downloadTaskInfo)) {
					LogUtil.i("appupdate", "UnknownHostException e is cancel");
					updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
					endConnecting(downloadTask, new DownloadException(
							DownloadException.CODE_CANCELLED));
					return false;
				}
			} catch (IOException e) {
				LogUtil.i("appupdate", "IOException e");
				updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
				endConnecting(downloadTask, new DownloadException(
						DownloadException.CODE_IO));
				DownloadLog.error(DownloadTaskManager.class, "Downloading", e);
				return false;
			} catch (Exception e) {
				DownloadLog.error(DownloadTaskManager.class, "Exception", e);
				LogUtil.i("appupdate", "Exception e = " + e.getMessage());
				if (isCancel(downloadTaskInfo)) {
					LogUtil.i("appupdate",
							"Exception e isCancel(downloadTaskInfo");
					updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
					endConnecting(downloadTask, new DownloadException(
							DownloadException.CODE_CANCELLED));
					return false;
				}
			} finally {
				file = null;
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}
				}
				fos = null;
			}
		} while (sflag < 3);
		updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
		endConnecting(downloadTask, new DownloadException(
				DownloadException.CODE_DOWNLOAD_FAILURE));
		return false;
	}

	/**
	 * 是否继续下载
	 * 
	 * @param file
	 * @return
	 */
	private boolean checkResumeDownload(DownloadTask downloadTask, File file) {
		if (file.exists() && file.length() > 0) {
			return true;
		}
		return false;
	}

	private void resetTask() {
		mTotalTask = 0;
		mProgressTask = 0;
	}

	/**
	 * 下载个数
	 * 
	 * @return
	 */
	public int getDownloadTaskCount() {
		return mTotalTask;
	}

	public int getProgressTask() {
		return mProgressTask;
	}

	/**
	 * 打开下载完成的文件
	 * 
	 * @param downloadTask
	 */
	public void openDownloadFile(DownloadTask downloadTask, boolean tempFile) {
		String filePath = tempFile ? DownloadUtils
				.getTempFileDownloadPath(downloadTask.mDownloadItem)
				: DownloadUtils.getFileDownloadPath(downloadTask.mDownloadItem);

		final File file = new File(filePath);
		if (!file.exists()) {
			/*
			 * updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
			 * addDownloadTask(downloadTask);
			 */
			updateTaskDB(downloadTask, AppItemState.STATE_PAUSE);
			addDownloadTask(downloadTask);

			MyApplication.getInstance().showToast(R.string.apk_not_exist2);
			return;
		}
		switch (downloadTask.downloadType) {
		case FILE_TYPE_BACKGROUND:
		case FILE_TYPE_APK:
			doInstall(mContext, filePath, downloadTask);
			// com.byt.market.util.PackageUtil.installApkByUser(mContext,
			// filePath);
			break;
		default:
			doInstall(mContext, filePath, downloadTask);
			// com.byt.market.util.PackageUtil.installApkByUser(mContext,
			// filePath);
			break;
		}
	}

	/**
	 * 下载完成播放提示音
	 * 
	 * @param downloadTask
	 */
	private void playDownloadedAudio(DownloadTask downloadTask) {
		/*
		 * if(mUserSettingInfo.warnningDownloaded && downloadTask.downloadType
		 * != Const.FILE_TYPE_BACKGROUND) {
		 * FileUtil.getInstance().playAudio(R.raw.download); }
		 */
	}

	private synchronized void startConnecting(DownloadTask downloadTask) {
		if (downloadTask == null)
			return;
		if (mApplicationExit)
			return;
		DownloadLog.debug(DownloadTaskManager.class,
				"startConnecting mprogressTask=" + mProgressTask
						+ " mcurrTask=" + downloadTask.mDownloadItem.name
						+ " mtotalTask=" + mTotalTask);
		if (downloadTask.downloadType == FILE_TYPE_BACKGROUND)
			return;

		for (DownloadTaskListener listener : mListeners) {
			listener.startConnecting(downloadTask, mTotalTask, mProgressTask);
		}
		SysNotifyUtil
				.notifyDownloadInfo(
						MyApplication.getInstance(),
						R.drawable.noti_icon,
						DownloadTask
								.getRealDownloaddingCount(DownloadTaskManager.FILE_TYPE_APK));
	}

	private synchronized void downloadStarted(DownloadTask downloadTask,
			long totalSize) {
		if (downloadTask == null)
			return;
		if (mApplicationExit)
			return;
		if (downloadTask.mDownloadItem.dSize <= 0) {
			if (mAllTasks.containsKey(String
					.valueOf(downloadTask.mDownloadItem.sid))) {
				mAllTasks.get(String.valueOf(downloadTask.mDownloadItem.sid)).mDownloadItem.dSize = totalSize;
			}
			downloadTask.updateRealState(MyApplication.getInstance()
					.getApplicationContext(), totalSize);
		}
		DownloadLog.debug(DownloadTaskManager.class,
				"downloadStarted currtask=" + downloadTask.mDownloadItem.name
						+ " totalSize=" + totalSize);
		if (downloadTask.downloadType == FILE_TYPE_BACKGROUND)
			return;

		for (DownloadTaskListener listener : mListeners) {
			listener.downloadStarted(downloadTask, mTotalTask, mProgressTask,
					totalSize);
		}

	}

	private synchronized void downloadProgress(DownloadTask downloadTask,
			long progressSize, long totalSize) {
		if (downloadTask == null)
			return;
		if (mApplicationExit)
			return;
		DownloadLog.debug(DownloadTaskManager.class,
				"downloadStarted currtask=" + downloadTask.mDownloadItem.name
						+ " progressSize=" + progressSize);
		if (downloadTask.downloadType == FILE_TYPE_BACKGROUND)
			return;
		if (mAllTasks.containsKey(String
				.valueOf(downloadTask.mDownloadItem.sid))) {
			mAllTasks.get(String.valueOf(downloadTask.mDownloadItem.sid)).mDownloadItem.lastDSize = progressSize;
		}
		downloadTask.mDownloadItem.lastDSize = progressSize;
		downloadTask.mDownloadItem.dSize = totalSize;
		for (DownloadTaskListener listener : mListeners) {
			listener.downloadProgress(downloadTask, mTotalTask, mProgressTask,
					progressSize, totalSize);
		}
	}

	private synchronized void downloadEnded(DownloadTask downloadTask) {
		if (downloadTask == null)
			return;
		if (mApplicationExit)
			return;
		DownloadLog.debug(DownloadTaskManager.class, "downloadEnded currtask="
				+ downloadTask.mDownloadItem.name);
		if (downloadTask.downloadType == FILE_TYPE_BACKGROUND)
			return;

		for (DownloadTaskListener listener : mListeners) {
			listener.downloadEnded(downloadTask, mTotalTask, mProgressTask);
		}
	}

	private synchronized void endConnecting(DownloadTask downloadTask,
			DownloadException result) {
		if (downloadTask == null)
			return;
		DownloadLog.debug(DownloadTaskManager.class, "endConnecting currtask="
				+ downloadTask.mDownloadItem.name + " mApplicationExit="
				+ mApplicationExit + " result=" + (result == null));
		downloadTasks.remove(getMapKey(downloadTask));
		if (mApplicationExit)
			return;
		mSuccess = result == null;

		if (downloadTask.downloadType != FILE_TYPE_BACKGROUND) {
			for (DownloadTaskListener listener : mListeners) {
				listener.endConnecting(downloadTask, mTotalTask, mProgressTask,
						result);
			}
		}

		if (result == null) {
			playDownloadedAudio(downloadTask);
			if (SharedPrefManager.isDownloadImmediatelyInstall(mContext)) {
				openDownloadFile(downloadTask, false);
			}
		} else {
			DownloadLog.error(DownloadTaskManager.class, "endConnecting",
					result);
		}
		SysNotifyUtil
				.notifyDownloadInfo(
						MyApplication.getInstance(),
						R.drawable.noti_icon,
						DownloadTask
								.getRealDownloaddingCount(DownloadTaskManager.FILE_TYPE_APK));
	}

	private synchronized void refreshUI() {
		if (mApplicationExit)
			return;
		DownloadLog.debug(DownloadTaskManager.class, "refreshUI");
		for (DownloadTaskListener listener : mListeners) {
			listener.refreshUI();
		}
	}

	private synchronized void downloadTaskDone() {
		if (mApplicationExit)
			return;
		DownloadLog.debug(DownloadTaskManager.class,
				"downloadTaskDone  totalSize=" + mTotalTask + " mprogressTask="
						+ mProgressTask);

		for (DownloadTaskListener listener : mListeners) {
			listener.downloadTaskDone(mTotalTask, mProgressTask, mSuccess);
		}
	}

	class DownloadListenerImpl implements DownloadListener {
		@Override
		public void downloadStarted(DownloadTask downloadTask, long totalSize) {
			DownloadTaskManager.this.downloadStarted(downloadTask, totalSize);
		}

		@Override
		public void downloadProgress(DownloadTask downloadTask,
				long progressSize, long totalSize) {
			DownloadTaskManager.this.downloadProgress(downloadTask,
					progressSize, totalSize);
		}

		@Override
		public void downloadEnded(DownloadTask downloadTask) {
			DownloadTaskManager.this.downloadEnded(downloadTask);
		}
	}

	@Override
	public void connectionStateChanged(int state) {
		mConnectionState = (state != ConnectionListener.CONN_NONE);
		if (mThreadPool != null && mThreadPool.getActiveCount() > 0) {
			if (state == ConnectionListener.CONN_3G) {
				// App.getInstance().showToast(R.string.net_connect_3g);
			} else if (state == ConnectionListener.CONN_GPRS) {
				// App.getInstance().showToast(R.string.net_connect_gprs);
			}
		}

		if (!mConnectionState) {
			if (mThreadPool != null && mThreadPool.getActiveCount() > 0) {
				// cancelAllTasks();
				pauseAllTasks();
			}
		}
	}

	class MyThreadPoolExecutor extends ThreadPoolExecutor {
		public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			super.beforeExecute(t, r);
			// AppInitializer.getInstance().acquireWakeLock();
			// AppInitializer.getInstance().acquireWiFiLock();
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			DownloadLog.info(DownloadTaskManager.class,
					"MyThreadPoolExecutor afterExecute......");
			super.afterExecute(r, t);
			remove(r);
			// AppInitializer.getInstance().releaseWakeLock();
			// AppInitializer.getInstance().releaseWiFiLock();

			if (!mdestroy) {
				addTask(0, null);
			}
		}
	}

	class DownloadTaskInfo {
		public DownloadTask downloadTask;
		public Runnable downloadRunnable;
		public Downloader downloader;
		public boolean isCancel = false;
	}

	/**
	 * 创建hashmap中的key
	 * 
	 * @param downloadTask
	 * @return
	 */
	private String getMapKey(DownloadTask downloadTask) {
		try{
		switch (downloadTask.downloadType) {
		case FILE_TYPE_BACKGROUND:
		case FILE_TYPE_APK:
			return downloadTask.mDownloadItem.sid + "_"
					+ downloadTask.mDownloadItem.vcode;
		default:
			return downloadTask.mDownloadItem.sid + "_"
					+ downloadTask.mDownloadItem.vcode;
		}
		}catch(Exception e){
			
			return "";
		}
	}

	public void onDownloadBtnClick(AppItem item) {
		if (!NetworkUtil.isNetWorking(MyApplication.getInstance()
				.getApplicationContext())) {
			SystemUtil.showToast(MyApplication.getInstance()
					.getApplicationContext(),
					MyApplication.getInstance().getApplicationContext()
							.getString(R.string.toast_no_network));
			return;
		}
		Intent intent = new Intent();
		intent.setClass(MyApplication.getInstance().getApplicationContext(),
				DownloadService.class);
		intent.putExtra(DownloadService.EXT_APPITEM, item);
		intent.putExtra(DownloadService.EXT_COMMAND,
				DownloadService.COMMAND_USER_CLICK);
		MyApplication.getInstance().getApplicationContext()
				.startService(intent);
	}

	public void updateItemBtnByState(Context context, TextView stateActionView,
			AppItem appItem, ProgressBar progressBar, TextView progressText) {
		updateItemBtnByState(context, stateActionView, appItem, true,  progressBar,  progressText);
	}

	public void updateItemBtnByState(Context context, TextView stateActionView,
			AppItem appItem, boolean updateStateTextBg, ProgressBar progressBar, TextView progressText) {
		if (stateActionView != null) {
			stateActionView.setMinEms(5);
		}
		boolean showProgress = true;
		switch (appItem.state) {
		case AppItemState.STATE_WAIT: {
			safeMoreGameSetText(context, stateActionView, R.drawable.beston_btn_down_waitrawable,
					R.string.state_wait_action_text,
					false || !updateStateTextBg);
			showProgress = true;
			break;
		}
		case AppItemState.STATE_RETRY: {
			safeMoreGameSetText(context, stateActionView, R.drawable.beston_btn_down_retryawable,
					R.string.state_retry_action_text,
					false || !updateStateTextBg);
			showProgress = false;

			break;
		}
		case AppItemState.STATE_ONGOING: {
			safeMoreGameSetText(context, stateActionView,
					R.drawable.beston_btn_down_downingrawable,
					R.string.notextbutton,
					false || !updateStateTextBg);
			break;
		}
		case AppItemState.STATE_PAUSE: {// 暂停
			safeMoreGameSetText(context, stateActionView,
					R.drawable.beston_btn_down_continuawable, R.string.state_pause_action_text,
					false || !updateStateTextBg);
			break;
		}
		case AppItemState.STATE_DOWNLOAD_FINISH: {
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(context, stateActionView,
						R.drawable.beston_btn_down_opendrawable,
						R.string.state_finish_need_update_action_text,
						false || !updateStateTextBg);

			} else {
				safeMoreGameSetText(context, stateActionView,
						R.drawable.bestone_down_drawable,
						R.string.state_finish_action_text,
						false || !updateStateTextBg);
			}
			showProgress = false;

			break;
		}
		case AppItemState.STATE_INSTALLING: {
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(context, stateActionView,
						R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_need_update_action_text,
						false || !updateStateTextBg);
			} else {
				if (null != stateActionView)
					stateActionView
							.setCompoundDrawables(null, null, null, null);
				safeMoreGameSetText(context, stateActionView,
						R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_action_text,
						false || !updateStateTextBg);
				
			}
			showProgress = false;

			break;
		}
		case AppItemState.STATE_INSTALL_FAILED: {
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(context, stateActionView,
						R.drawable.beston_btn_down_opendrawable,
						R.string.state_install_failed_need_update_action_text,
						false || !updateStateTextBg);
			} else {
				safeMoreGameSetText(context, stateActionView,
						R.drawable.bestone_down_drawable,
						R.string.state_install_failed_action_text,
						false || !updateStateTextBg);
			}
			showProgress = false;

			break;
		}
		case AppItemState.STATE_UNINSTALLING: {
			safeMoreGameSetText(context, stateActionView, R.drawable.beston_btn_down_updateingrawable,
					R.string.state_uninstalling_action_text,
					false || !updateStateTextBg);
			showProgress = false;

			break;
		}
		case AppItemState.STATE_NEED_UPDATE: {
			safeMoreGameSetText(context, stateActionView,
					R.drawable.beston_btn_down_opendrawable,
					R.string.state_update_action_text,
					false || !updateStateTextBg);
			showProgress = false;

			break;
		}
		case AppItemState.STATE_INSTALLED_NEW: {
			safeMoreGameSetText(context, stateActionView,
					R.drawable.beston_btn_down_opendrawable,
					R.string.state_installed_new_action_text,
					true || !updateStateTextBg);
			showProgress = false;

			break;
		}
		case AppItemState.STATE_IDLE: {
			{
				if (apkFileExist(appItem)) {
					safeMoreGameSetText(mContext, stateActionView,
							R.drawable.bestone_down_drawable, R.string.state_idle_action_text,
							true || !updateStateTextBg);
				} else {
					safeMoreGameSetText(mContext, stateActionView,
							R.drawable.bestone_down_drawable,
							R.string.freeinstall,
							false || !updateStateTextBg);
				}
				showProgress = false;

				// safeMoreGameSetText(context, stateActionView,
				// R.drawable.icon_btn_download,
				// R.string.state_idle_action_text,
				// false || !updateStateTextBg);
			}
			break;
		}
		}
		if (appItem instanceof DownloadItem && showProgress) {
			final DownloadItem downloadItem = (DownloadItem) appItem;
			final long totleSize = downloadItem.dSize > 0 ? downloadItem.dSize
					: downloadItem.length;
			final int percent = totleSize == 0 ? 100
					: (int) ((downloadItem.lastDSize * 100) / totleSize);
			if (progressBar != null) {
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setProgress(percent);
				if (progressText != null) {
					progressText.setVisibility(View.VISIBLE);
					if ((appItem.state == AppItemState.STATE_PAUSE)||(appItem.state ==  AppItemState.STATE_WAIT)) {
						progressText.setVisibility(View.GONE);
					} else {
						progressText.setText(percent + "%");
					}
				}

				if (appItem.state == AppItemState.STATE_INSTALLING) {
					if (progressText != null) {
						progressText.setVisibility(View.GONE);
					}
					progressBar.setIndeterminate(true);
				} else {
					progressBar.setIndeterminate(false);
				}
					
				progressText.setTag(0);
			}

		}
	}

	private void safeMoreGameSetText(Context context, TextView textView,
			int drawableResId, int strID, boolean txtcolorChange) {
		if (textView != null) {
			textView.setText(strID);
			if (drawableResId != 0) {				
				textView.setBackgroundResource(drawableResId); 
			}
		}
	}

	public void updateByState(Context context, TextView stateActionView,
			AppItem appItem, ProgressBar progressBar, TextView progressText,
			boolean showDetailProgress, boolean showInfo) {
		updateByState(context, stateActionView, appItem, progressBar,
				progressText, showDetailProgress, showInfo, true, null, null);
	}
	

	public void updateByState(Context context, TextView stateActionView,
			AppItem appItem, ProgressBar progressBar, TextView progressText,
			boolean showDetailProgress, boolean showInfo,
			boolean updateStateTextBg, TextView currentStateView, String which) {
		boolean showProgress = true;
		if (stateActionView != null) {
			stateActionView.setMinEms(5);
		}
		switch (appItem.state) {
		case AppItemState.STATE_WAIT: {
			safeSetText(context, stateActionView,
					R.string.state_wait_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(context, currentStateView, R.string.state_wait_text,
					false || !updateStateTextBg);
			showProgress = true;
			break;
		}
		case AppItemState.STATE_RETRY: {
			safeSetText(context, stateActionView,
					R.string.state_retry_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(context, currentStateView, R.string.state_retry_text,
					false || !updateStateTextBg);
			showProgress = false;
			break;
		}
		case AppItemState.STATE_ONGOING: {
			safeSetText(context, stateActionView,
					R.string.state_ongoing_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			if (isInstalled(appItem.pname)) {
				safeSetText(context, currentStateView,
						R.string.state_ongoing_need_update_text,
						false || !updateStateTextBg);
			} else {
				safeSetText(context, currentStateView,
						R.string.state_ongoing_text,
						false || !updateStateTextBg);
			}

			break;
		}
		case AppItemState.STATE_PAUSE: {
			safeSetText(context, stateActionView,
					R.string.state_pause_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(context, currentStateView, R.string.state_pause_text,
					false || !updateStateTextBg);
			break;
		}
		case AppItemState.STATE_DOWNLOAD_FINISH: {
			if (isInstalled(appItem.pname)) {
				safeSetText(context, stateActionView,
						R.string.state_finish_need_update_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(context, currentStateView,
						R.string.state_finish_need_update_text,
						false || !updateStateTextBg);
				showProgress = false;
			} else {
				safeSetText(context, stateActionView,
						R.string.state_finish_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(context, currentStateView,
						R.string.state_finish_text, false || !updateStateTextBg);
				showProgress = false;
			}
			break;
		}
		case AppItemState.STATE_INSTALLING: {
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			if (isInstalled(appItem.pname)) {
				safeSetText(context, stateActionView,
						R.string.state_installing_need_update_action_text,
						false || !updateStateTextBg);
				safeSetText(context, currentStateView,
						R.string.state_installing_need_update_text,
						false || !updateStateTextBg);
			} else {
				safeSetText(context, stateActionView,
						R.string.state_installing_action_text,
						false || !updateStateTextBg);
				safeSetText(context, currentStateView,
						R.string.state_installing_text,
						false || !updateStateTextBg);
			}
			break;
		}
		case AppItemState.STATE_INSTALL_FAILED: {
			if (isInstalled(appItem.pname)) {
				safeSetText(context, stateActionView,
						R.string.state_install_failed_need_update_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(context, currentStateView,
						R.string.state_install_failed_need_update_text,
						false || !updateStateTextBg);
				showProgress = false;
			} else {
				safeSetText(context, stateActionView,
						R.string.state_install_failed_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(context, currentStateView,
						R.string.state_install_failed_text,
						false || !updateStateTextBg);
				showProgress = false;
			}
			break;
		}
		case AppItemState.STATE_UNINSTALLING: {
			safeSetText(context, stateActionView,
					R.string.state_uninstalling_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(context, currentStateView,
					R.string.state_uninstalling_text,
					false || !updateStateTextBg);
			break;
		}
		case AppItemState.STATE_NEED_UPDATE: {
			safeSetText(context, stateActionView,
					R.string.state_update_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			showProgress = false;
			break;
		}
		case AppItemState.STATE_INSTALLED_NEW: {
			safeSetText(context, stateActionView,
					R.string.state_installed_new_action_text,
					true || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_orange);
			}
			showProgress = false;
			break;
		}
		case AppItemState.STATE_IDLE: {
			if (updateStateTextBg && which != null
					&& which.equals(LogModel.L_HOME_REC)) {
				safeSetBg(stateActionView, R.drawable.btn_down_rec_bg);
				safeSetText(context, stateActionView,
						R.string.state_idle_action_text,
						true || !updateStateTextBg);
			} else {
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(context, stateActionView,
						R.string.state_idle_action_text,
						false || !updateStateTextBg);
			}
			showProgress = false;
			break;
		}
		}
		if (appItem instanceof DownloadItem && showProgress) {
			final DownloadItem downloadItem = (DownloadItem) appItem;
			final long totleSize = downloadItem.dSize > 0 ? downloadItem.dSize
					: downloadItem.length;
			final int percent = totleSize == 0 ? 100
					: (int) ((downloadItem.lastDSize * 100) / totleSize);
			if (progressBar != null) {
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setProgress(percent);
				if (appItem.state == AppItemState.STATE_INSTALLING) {
					progressBar.setIndeterminate(true);
				} else {
					progressBar.setIndeterminate(false);
				}
			}
			if (progressText != null) {
				if (showDetailProgress) {
					progressText
							.setText(progressText
									.getContext()
									.getString(
											R.string.down_progress_detail,
											StringUtil
													.formageDownloadSize(downloadItem.lastDSize),
											StringUtil
													.formageDownloadSize(downloadItem.dSize > 0 ? downloadItem.dSize
															: downloadItem.length)));
				} else if (showInfo) {
					progressText.setVisibility(View.GONE);
				} else {
					progressText.setText(percent + "%");
					progressText.setTag(percent);
				}
			}
		} else {
			// 原版
			// if (progressBar != null) {
			// progressBar.setVisibility(View.GONE);
			// }
			// if (progressText != null) {
			// progressText.setVisibility(View.VISIBLE);
			// // progressText.setText("");
			// progressText.setTag(0);
			// }
			if (progressBar != null) {
				progressBar.setVisibility(View.GONE);
			}
			if (progressText != null) {
				progressText.setVisibility(View.VISIBLE);
				// progressText.setText("");
				/** BUG #72 下载中： 处于等待状态的游戏包大小显示为null start **/
				if ((appItem instanceof DownloadItem)) {
					final DownloadItem downloadItem = (DownloadItem) appItem;
					progressText
							.setText(progressText
									.getContext()
									.getString(
											R.string.down_progress_detail,
											StringUtil
													.formageDownloadSize(downloadItem.lastDSize),
											StringUtil
													.formageDownloadSize(downloadItem.dSize > 0 ? downloadItem.dSize
															: downloadItem.length)));
				}
				/** BUG #72 下载中： 处于等待状态的游戏包大小显示为null start end **/
				progressText.setTag(0);
				// Log.i("zc", "pname = " + appItem.pname + "progressText = "
				// + progressText.getText().toString());

			}

		}
	}

	private void safeSetText(Context context, TextView textView, int strID,
			boolean txtcolorChange) {
		if (textView != null) {
			textView.setText(strID);
			if (txtcolorChange)
				textView.setTextColor(context.getResources().getColor(
						android.R.color.white));
			else
				textView.setTextColor(context.getResources().getColor(
						android.R.color.black));
		}
	}

	private void safeSetBg(View view, int resID) {
		if (view != null) {
			//view.setBackgroundResource(resID);
		}
	}

	public void fillAppStates(List<AppItem> items) {
		if (items == null || items.size() == 0) {
			return;
		}

		final int size = items.size();
		int state = AppItemState.STATE_IDLE;
		for (int i = 0; i < size; i++) {
			state = PackageUtil.isApkInstalledHome(mContext, items.get(i).pname,
					items.get(i).vcode,items.get(i));
			if (state > AppItemState.STATE_NEED_UPDATE) {
				items.get(i).state = state;
				if (mAllTasks.containsKey(String.valueOf(items.get(i).sid))) {
					mAllTasks.get(String.valueOf(items.get(i).sid)).mDownloadItem.state = state;
				}
			} else if (mAllTasks.containsKey(String.valueOf(items.get(i).sid))) {
					items.get(i).state = mAllTasks
						.get(String.valueOf(items.get(i).sid)).mDownloadItem.state;
				DownloadItem downloadItem = new DownloadItem();
				downloadItem.fill(items.get(i));
				downloadItem.dSize = mAllTasks
						.get(String.valueOf(items.get(i).sid)).mDownloadItem.dSize;
				downloadItem.lastDSize = mAllTasks.get(String.valueOf(items
						.get(i).sid)).mDownloadItem.lastDSize;
				items.remove(i);
				items.add(i, downloadItem);
			} else {
				items.get(i).state = state;
			}
			if(items.get(i).cacheState==AppItemState.STATE_IDLE&&items.get(i).state!=AppItemState.STATE_IDLE){
				items.get(i).cacheState=items.get(i).state;
			}
		}
	}

	/**
	 * 获取已下载文件的路径
	 * 
	 * @param appItem
	 * @return
	 */
	public String getApkFilePath(AppItem appItem) {
		String apkPath = null;
		if (appItem.sid <= 0) {
			apkPath = appItem.name;
		} else {
			apkPath = String.valueOf(appItem.sid);
		}
		return DownloadUtils
				.getDownloadDirPath(DownloadTaskManager.FILE_TYPE_APK)
				+ File.separator + apkPath;
	}

	/**
	 * 下载文件是否还存在
	 * 
	 * @param appItem
	 * @return
	 */
	public boolean apkFileExist(AppItem appItem) {
		File file = new File(getApkFilePath(appItem));
		return file.exists();
	}

	/**
	 * 删除下载的安装文件或未下载完的临时下载文件
	 * 
	 * public void deleteApkFile(AppItem appItem){ File file = new
	 * File(getApkFilePath(appItem)); if(file.exists()){ file.delete(); } }
	 */

	/**
	 * 获取已下载中文件的路径
	 * 
	 * @return
	 * 
	 *         public String getDownloaddingApkFilePath(AppItem appItem){ String
	 *         apkPath = null; if (appItem.sid <= 0) { apkPath = appItem.name;
	 *         }else{ apkPath = String.valueOf(appItem.sid)+".temp"; } return
	 *         DownloadUtils
	 *         .getDownloadDirPath(DownloadTaskManager.FILE_TYPE_APK) +
	 *         File.separator + apkPath; }
	 */

	// public void deleteTempFile(AppItem appItem){
	// File file = new File(getDownloaddingApkFilePath(appItem));
	// if(file.exists()){
	// file.delete();
	// }
	// }

	public synchronized void fillDownloadAppItemStates(List<DownloadAppItem> items) {
		if (items == null || items.size() == 0) {
			return;
		}
		List<DownloadAppItem> itemsTemp=new ArrayList<DownloadAppItem>();
		itemsTemp.addAll(items);
		final int size = itemsTemp.size();
		int state = AppItemState.STATE_IDLE;
		for (int i = 0; i < size; i++) {
			DownloadAppItem downloadAppItem = itemsTemp.get(i);
			AppItem appItem = downloadAppItem.getAppItem();
			if (appItem == null) {
				continue;
			}
			state = PackageUtil.isApkInstalled(mContext, appItem.pname,
					appItem.vcode);
			if (state > AppItemState.STATE_NEED_UPDATE) {
				appItem.state = state;
				if (mAllTasks.containsKey(String.valueOf(appItem.sid))) {
					mAllTasks
							.get(String.valueOf(itemsTemp.get(i).getAppItem().sid)).mDownloadItem.state = state;
				}
			} else if (mAllTasks.containsKey(String.valueOf(appItem.sid))) {
				appItem.state = mAllTasks.get(String.valueOf(appItem.sid)).mDownloadItem.state;
				DownloadItem downloadItem = new DownloadItem();
				downloadItem.fill(appItem);
				downloadItem.dSize = mAllTasks.get(String.valueOf(appItem.sid)).mDownloadItem.dSize;
				downloadItem.lastDSize = mAllTasks.get(String
						.valueOf(appItem.sid)).mDownloadItem.lastDSize;
				itemsTemp.remove(i);
				downloadAppItem.setAppItem(downloadItem);
				itemsTemp.add(i, downloadAppItem);
			} else {
				itemsTemp.get(i).getAppItem().state = state;
			}
		}
		items=itemsTemp;
	}

	public static final int FILL_TYPE_HOMEITEMS = 1;
	public static final int FILL_TYPE_LEADERSITEMS = 2;
	public static final int FILL_TYPE_RANKITEMS = 3;
	public static final int FILL_TYPE_RECITEMS = 4;
	public static final int FILL_TYPE_SUBJECTITEMS = 5;
	public static final int FILL_TYPE_NEWITEMS = 7;
	public static final int FILL_TYPE_NEW_USER_ITEMS = 8;
	public static final int FILL_TYPE_SUBLIST_ITEMS = 9;
	public static final int FILL_TYPE_LOCAL_ITEMS = 10;
	public static final int FILL_TYPE_RESULTS = 11;

	public void fillBigItemStates(List<BigItem> items, int[] fillTypes) {
		if (items == null || items.size() == 0) {
			return;
		}
		final int size = items.size();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < fillTypes.length; j++) {
				ArrayList<AppItem> subItems = null;
				switch (fillTypes[j]) {
				case FILL_TYPE_HOMEITEMS: {
					subItems = items.get(i).homeItems;
					break;
				}
				// case FILL_TYPE_LEADERSITEMS: {
				// subItems = items.get(i).leaderItems;
				// break;
				// }
				case FILL_TYPE_RANKITEMS: {
					subItems = items.get(i).rankItems;
					break;
				}
				case FILL_TYPE_RECITEMS: {
					subItems = items.get(i).recItems;
					break;
				}
				// case FILL_TYPE_SUBJECTITEMS: {
				// subItems = items.get(i).subjectItems;
				// break;
				// }
				case FILL_TYPE_NEWITEMS: {
					subItems = items.get(i).newItems;
					break;
				}
				case FILL_TYPE_NEW_USER_ITEMS: {
					subItems = items.get(i).newUserItems;
					break;
				}
				case FILL_TYPE_SUBLIST_ITEMS: {
					subItems = items.get(i).subListItems;
					break;
				}
				case FILL_TYPE_LOCAL_ITEMS: {
					subItems = items.get(i).localItems;
					break;
				}
				case FILL_TYPE_RESULTS: {
					subItems = items.get(i).resultItems;
					break;
				}
				}
				fillAppStates(subItems);

			}
		}

	}

	public boolean inDownload(int state) {
		return !(state == AppItemState.STATE_IDLE
				|| state == AppItemState.STATE_NEED_UPDATE || state == AppItemState.STATE_INSTALLED_NEW);
	}

	@Override
	public void onPackageChanged(String pkn, int pknAction) {
		updateStateAfterPackageChanged(pkn, pknAction, true);
		if (ConnectionListener.ACTION_PACKAGE_ADD == pknAction) {
			// 非root手机安装成功后消息回调
			for (DownloadTaskListener listener : mListeners) {
				listener.installedSucess(pkn);
			}
		} else if (ConnectionListener.ACTION_PACKAGE_REMOVED == pknAction) {
			// 非root手机安装成功后消息回调
			for (DownloadTaskListener listener : mListeners) {
				listener.unInstalledSucess(pkn);
			}
		}
		refreshUI();
	}

	private void mineSpecilaCallBack(DownloadTask task) {
		// “我的”事件特殊处理
		for (DownloadTaskListener listener : mListeners) {
			if (listener instanceof MineFragment) {

				listener.endConnecting(task, mTotalTask, mProgressTask,
						new DownloadException(DownloadException.CODE_UNKNOWN));
			}
		}
	}

	public DownloadTask getDownloadTask(Context ctx, String app_id, String vcode) {
		if (mAllTasks.containsKey(app_id)) {
			return mAllTasks.get(app_id);
		}
		String selection = null;
		String[] selectionArgs = null;
		if (vcode != null) {
			selection = DownloadTask.COLUMN_SID + " = ? and "
					+ DownloadTask.COLUMN_VCODE + " = ?";
			selectionArgs = new String[] { app_id, vcode };
		} else {
			selection = DownloadTask.COLUMN_SID + " = ? ";
			selectionArgs = new String[] { app_id };
		}

		return DownloadTask.getDownloadTask(ctx, selection, selectionArgs);
	}

	public List<AppItem> getAllDownloadItem(boolean getUpdateOnly) {
		Iterator iterator = mAllTasks.keySet().iterator();
		final List<AppItem> items = new ArrayList<AppItem>();
		if (iterator.hasNext()) {
			do {
				final DownloadItem item = mAllTasks.get(iterator.next()).mDownloadItem;
				if (getUpdateOnly) {
					if (item.state == AppItemState.STATE_NEED_UPDATE) {
						items.add(item);
					}
				} else {
					items.add(item);
				}
			} while (iterator.hasNext());
		}
		return items;
	}

	public List<AppItem> getAllInstallDownloadItem(boolean getUpdateOnly) {
		Iterator iterator = mAllTasks.keySet().iterator();
		final List<AppItem> items = new ArrayList<AppItem>();
		if (iterator.hasNext()) {
			do {
				final DownloadItem item = mAllTasks.get(iterator.next()).mDownloadItem;
				if (!PackageUtil.isInstalledApk(mContext, item.pname, null)) {
					continue;
				}
				if (getUpdateOnly) {
					if (item.state == AppItemState.STATE_NEED_UPDATE) {
						items.add(item);
					}
				} else {
					items.add(item);
				}
			} while (iterator.hasNext());
		}

		return items;
	}

	/**
	 * 获得我的游戏列表
	 */
	public synchronized List<AppItem> getMyDownloadItems() {
		// 正常我的游戏下载列表
		List<AppItem> myAppItems = new ArrayList<AppItem>();
		try {
			Iterator iterator = mAllTasks.keySet().iterator();
			if (iterator.hasNext()) {
				do {
					final DownloadItem item = mAllTasks.get(iterator.next()).mDownloadItem;
					if (PackageUtil.isInstalledApk(mContext, item.pname, null)) {
						item.icon = SystemUtil.drawableToBitmap(PackageUtil
								.getAppIcon(mContext, item.pname));
						myAppItems.add(item);
					}
				} while (iterator.hasNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 对已装游戏进行排序,排序规则:已安装未启动的应用靠前,然后其它应用按安装时间进行倒序
		if (myAppItems != null && myAppItems.size() > 1) {
			final Comparator<AppItem> comparatorName = new Comparator<AppItem>() {
				public int compare(AppItem item_1, AppItem item_2) {
					long installTime1 = PackageUtil.getAppInstallTime(mContext,
							item_1.pname);
					long installTime2 = PackageUtil.getAppInstallTime(mContext,
							item_2.pname);
					if (item_1.isInstalledButNotLauchered(mContext)
							&& item_2.isInstalledButNotLauchered(mContext)) {
						return DateUtil.compareDate(new Date(installTime1),
								new Date(installTime2)) == 1 ? -1 : 1;
					} else {
						if (!item_1.isInstalledButNotLauchered(mContext)
								&& !item_2.isInstalledButNotLauchered(mContext)) {
							return DateUtil.compareDate(new Date(installTime1),
									new Date(installTime2)) == 1 ? -1 : 1;
						} else {
							if (!item_1.isInstalledButNotLauchered(mContext)) {
								return 1;
							} else {
								return -1;
							}
						}
					}
				}
			};
			Collections.sort(myAppItems, comparatorName);
		}

		return myAppItems;
	}
	
	/**
	 * 获得内部安装并未打开过的游戏的总数量
	 */
	public int getInnerInstalledButNotOpenedGamesCount() {	
		int count = 0;
		Iterator iterator = mAllTasks.keySet().iterator();
		if (iterator.hasNext()) {
			do {
				final DownloadItem item = mAllTasks.get(iterator.next()).mDownloadItem;
				if (PackageUtil.isInstalledApk(mContext, item.pname, null))
				{
				if (item.isInstalledButNotLauchered(mContext)) {
					count++;
				}
				}
			} while (iterator.hasNext());
		}
		return count;
	}

	public void updateStateAfterPackageChanged(String pkn, int pknAction,
			boolean flag) {
		final String selection = DownloadTask.COLUMN_PNAME + " = ? ";
		final String[] selectionArgs = { pkn };
		DownloadTask task = DownloadTask.getDownloadTask(mContext, selection,
				selectionArgs);
		if (task != null) {
			int state = AppItem.STATE_IDLE;
			if (pknAction == ConnectionListener.ACTION_PACKAGE_ADD) {
				state = PackageUtil.isApkInstalled(mContext,
						task.mDownloadItem.pname, task.mDownloadItem.vcode);
				if (!mInstalledGames.containsKey(task.mDownloadItem.pname)) {
					mInstalledGames.put(task.mDownloadItem.pname, 0);
				}
				if (!task.mDownloadItem.pname
						.equals("com.byt.market") && flag)
					updateDataDB(task.mDownloadItem.sid,
							task.mDownloadItem.list_id,
							task.mDownloadItem.list_cateid, 4);
			} else if (pknAction == ConnectionListener.ACTION_PACKAGE_REMOVED) {
				state = AppItemState.STATE_IDLE;
				task.mDownloadItem.state = state;
				if (TextUtils.isEmpty(task.mDownloadItem.apk)
						&& TextUtils.isEmpty(task.mDownloadItem.iconUrl)) {
					updateAfterApkUninstalled(task.mDownloadItem,
							String.valueOf(task.mDownloadItem.sid), true, null);
				}

			}
			if (mAllTasks.containsKey(String.valueOf(task.mDownloadItem.sid))) {
				mAllTasks.get(String.valueOf(task.mDownloadItem.sid)).mDownloadItem.state = state;
			}
			task.updateState(mContext, state, -1);
			task.mDownloadItem.state = state;
			if (mInstalledGames.containsKey(task.mDownloadItem.pname)) {
				mInstalledGames.remove(task.mDownloadItem.pname);
			}
			mineSpecilaCallBack(task);
		} else if (pknAction == ConnectionListener.ACTION_PACKAGE_ADD
				&& LocalGameDBManager.getInstance().addGameAfterInstalled(pkn) > 0) {
			updateStateAfterPackageChanged(pkn,
					ConnectionListener.ACTION_PACKAGE_ADD, false);
		}
	}

	public void uninstallApp(Context context, String packageName, String sid,
			boolean removeFile) {
		// update state
		final DownloadTask task = this.getDownloadTask(
				MyApplication.getInstance(), sid, null);
		if (task != null) {
			updateInstallState(task, AppItem.STATE_UNINSTALLING);
		}
		PackageUtil.uninstallApp(context, packageName, sid, removeFile);
	}

	/**
	 * 游戏被卸载之后调用
	 * */
	public void updateAfterApkUninstalled(DownloadItem downloadItem,
			String sid, boolean removeFile, String mPname) {
		final DownloadTask task = downloadItem == null ? this.getDownloadTask(
				MyApplication.getInstance(), sid, null) : new DownloadTask();
		if (downloadItem != null) {
			task.mDownloadItem = downloadItem;
		}
		notifyAppUnstalled(mPname);
		if (task == null) {
			return;
		}
		final int state = PackageUtil.isApkInstalled(
				MyApplication.getInstance(), task.mDownloadItem.pname,
				task.mDownloadItem.vcode);
		if (state >= AppItemState.STATE_NEED_UPDATE) {
			// 没有卸载或者卸载失败，更改状态为已安装
			updateInstallState(task, state);

			return;
		} else {
			DownloadTaskManager.getInstance().deleteDownloadTask(task, true,
					true);
			final View removeView = MyApplication.getInstance().mMineViewManager
					.getWorkspace().getItemBySid(Integer.parseInt(sid));
			if (removeView != null) {
				MyApplication.getInstance().mMineViewManager.getWorkspace()
						.removeItem(task.mDownloadItem, removeView);
			}
			if (removeFile) {
				deleteDownlaodFile(task.mDownloadItem);
			}
		}
	}

	public void updateAfterUpdateGamesGot(List<AppItem> items) {
		if (items == null || items.size() == 0) {
			return;
		}
		final String where = DownloadTaskColumn.COLUMN_SID + " IS ?";
		final String selection = DownloadTask.COLUMN_PNAME + " = ? ";
		for (AppItem item : items) {
			if (item.pname == null) {
				Log.i("0416", "sid = " + item.sid);
				continue;
			}
			 DownloadTask task = DownloadTask.getDownloadTask(mContext,
					selection, new String[] { item.pname });
			if(task==null)
			{
				DownloadTaskManager.getInstance().addLocalGame(item);
				  task = DownloadTask.getDownloadTask(mContext,
							selection, new String[] { item.pname });
			}
			final String sid = String.valueOf(task.mDownloadItem.sid);
			if (mAllTasks.containsKey(sid)) {
				mAllTasks.get(sid).mDownloadItem
						.fillWithOutScreenAndListInfo(item);
				mAllTasks.get(sid).update(MyApplication.getInstance(),
						DownloadTask.CONTENT_URI,
						mAllTasks.get(sid).toContentValues(), where,
						new String[] { sid });
				mineSpecilaCallBack(mAllTasks.get(sid));
			}

		}
	}
	public void updateAfterUpdateGamesItem(AppItem item) {
	final String where = DownloadTaskColumn.COLUMN_SID + " IS ?";
	final String selection = DownloadTask.COLUMN_PNAME + " = ? ";

		if (item.pname == null) {
			Log.i("0416", "sid = " + item.sid);
			return;
		}
		
		 DownloadTask task = DownloadTask.getDownloadTask(mContext,
				selection, new String[] { item.pname });
		if(task==null)
		{
			DownloadTaskManager.getInstance().addLocalGame(item);
			  task = DownloadTask.getDownloadTask(mContext,
						selection, new String[] { item.pname });
		}
		else{
			return;
		}
		final String sid = String.valueOf(task.mDownloadItem.sid);
		if (mAllTasks.containsKey(sid)) {
			item.state=AppItemState.STATE_NEED_UPDATE;
			mAllTasks.get(sid).mDownloadItem
					.fillWithOutScreenAndListInfo(item);
			mAllTasks.get(sid).update(MyApplication.getInstance(),
					DownloadTask.CONTENT_URI,
					mAllTasks.get(sid).toContentValues(), where,
					new String[] { sid });
			mineSpecilaCallBack(mAllTasks.get(sid));
		
	}

	}

	public void updateScreenPositionInMemory(String sid, int screen,
			int position) {
		if (mAllTasks.containsKey(sid)) {
			mAllTasks.get(sid).mDownloadItem.screen = screen;
			mAllTasks.get(sid).mDownloadItem.position = position;
		}
	}

	public void updateDataDB(int appid, String list_id, String list_c, int w) {
		GAME game = new GAME();
		game.app_id = appid;
		game.list_c = list_c;
		game.list_id = list_id;
		switch (w) {
		case 0:
			game.d_c = 1;
			break;
		case 1:
			game.d_o = 1;
			break;
		case 2:
			game.d_f = 1;
			break;
		case 3:
			game.d_d = 1;
			break;
		case 4:
			game.i_o = 1;
			break;
		case 5:
			game.i_f = 1;
			break;
		}
		Util.addData(MarketContext.getInstance(), game);
	}

	/*	*//**
	 * 执行安装
	 * 
	 */

	public void doInstall(final Context context, String filePath,
			final DownloadTask downloadTask) {
		updateInstallState(downloadTask, AppItemState.STATE_INSTALLING);
		PackageUtil.doInstall(context, filePath, downloadTask);
	}

	public Map<String, Integer> getRealDownloadTask() {
		Map<String, Integer> returnMap = null;
		if (downloadTasks != null) {
			final Iterator iterator = downloadTasks.values().iterator();
			DownloadTaskInfo taskInfo = null;
			returnMap = new HashMap<String, Integer>();
			while (iterator.hasNext()) {
				taskInfo = (DownloadTaskInfo) iterator.next();
				returnMap
						.put(String
								.valueOf(taskInfo.downloadTask.mDownloadItem.sid),
								1);
			}
		}
		return returnMap;
	}

	/**
	 * 返回是否有修改
	 * */
	public boolean syncDownloadStates() {
		final Map<String, Integer> map = getRealDownloadTask();
		if (map == null || !map.keySet().iterator().hasNext()) {
			// 没有真正的下载任务，将所有下载中、等待的任务改为暂停
			return syncAllDownloadStates2Pause();
			// ContentValues values = new ContentValues();
			// values.put(DownloadTask.COLUMN_STATE, AppItemState.STATE_PAUSE);
			// int updateCount = DownloadTask.update(mContext,
			// DownloadTask.CONTENT_URI, values, DownloadTask.COLUMN_STATE
			// + " IS " + AppItemState.STATE_ONGOING + " OR "
			// + DownloadTask.COLUMN_STATE + " IS "
			// + AppItemState.STATE_WAIT, null);
			// return updateCount > 0 ? true : false;
		} else {
			return false;
		}
	}

	public boolean syncInstallingTasks(Map<String, Integer> sids) {
		boolean syncd = false;
		final ArrayList<DownloadTask> tasks = DownloadTask.getAllDownloadTasks(
				mContext, AppItemState.STATE_INSTALLING);
		if (tasks != null) {
			// ArrayList<AppItem> items = new ArrayList<AppItem>();
			for (DownloadTask task : tasks) {
				if (!sids.containsKey(String.valueOf(task.mDownloadItem.sid))) {
					// 状态错误，修改
					/*
					 * items.clear(); items.add(task.mDownloadItem);
					 * fillAppStates(items);
					 */
					task.mDownloadItem.state = AppItemState.STATE_DOWNLOAD_FINISH;
					task.updateState(mContext, task.mDownloadItem.state, -1);
					syncd = true;
				}
			}
		}
		return syncd;
	}

	public boolean syncUninstallingTasks(Map<String, Integer> sids) {
		boolean syncd = false;
		final ArrayList<DownloadTask> tasks = DownloadTask.getAllDownloadTasks(
				mContext, AppItemState.STATE_UNINSTALLING);
		if (tasks != null) {
			// ArrayList<AppItem> items = new ArrayList<AppItem>();
			for (DownloadTask task : tasks) {
				if (!sids.containsKey(String.valueOf(task.mDownloadItem.sid))) {
					// 状态错误，修改
					/*
					 * items.clear(); items.add(task.mDownloadItem);
					 * fillAppStates(items);
					 */
					task.mDownloadItem.state = AppItemState.STATE_INSTALLED_NEW;
					task.updateState(mContext, task.mDownloadItem.state, -1);
					syncd = true;
				}
			}
		}
		return syncd;
	}

	public void reloadMemeryTasks() {
		mAllTasks = DownloadTask.getAllDownloadTasks(mContext, mInstalledGames);
	}

	public void updateAppItem(AppItem item) {
		final DownloadTask task = new DownloadTask();
		if (item instanceof DownloadItem) {
			task.mDownloadItem = (DownloadItem) item;
		} else {
			task.mDownloadItem = new DownloadItem();
			task.mDownloadItem.fill(item);
		}
		ContentValues values = task.toContentValues();
		task.update(mContext, DownloadTask.CONTENT_URI, values,
				DownloadTask.COLUMN_SID + " IS " + task.mDownloadItem.sid, null);
		reloadMemeryTasks();
	}
	public void clearallnotopen() {
		Iterator iterator = mAllTasks.keySet().iterator();
		if (iterator.hasNext()) {
			do {
				 DownloadItem item = mAllTasks.get(iterator.next()).mDownloadItem;
				if (PackageUtil.isInstalledApk(mContext, item.pname, null))
				{
					if (item.isInstalledButNotLauchered(mContext)) {
					 DownloadTask task = new DownloadTask();
					if (item instanceof DownloadItem) {
						task.mDownloadItem = item;
					} else {
						task.mDownloadItem = new DownloadItem();
						task.mDownloadItem.fill(item);
					}
					task.mDownloadItem.isOpenned=1;
					ContentValues values = task.toContentValues();
					task.update(mContext, DownloadTask.CONTENT_URI, values,
							DownloadTask.COLUMN_SID + " IS " + task.mDownloadItem.sid, null);
					}
					
				}
			} while (iterator.hasNext());
		}
		reloadMemeryTasks();
	
	}

	/**
	 * 将内存中（mAllTasks）的wait状态的任务置为暂停状态 下载管理中，点击全部暂停时调用
	 */
	public void modifyAllWailtState2Pause() {
		Iterator iterator = mAllTasks.keySet().iterator();
		while (iterator.hasNext()) {
			final DownloadItem item = mAllTasks.get(iterator.next()).mDownloadItem;
			if (item.state == AppItemState.STATE_WAIT) {
				item.state = AppItemState.STATE_PAUSE;
			}
		}
	}
	

	public void updateByDownLoadState(TextView stateActionView,
			AppItem appItem, ProgressBar progressBar, TextView progressText,
			boolean showDetailProgress, boolean showInfo,
			boolean updateStateTextBg, TextView currentStateView, String which,
			TextView summaryText, boolean isShowDownLoadContent,
			TextView downstateDesc) {
		boolean showProgress = true;
		if (stateActionView != null) {
			stateActionView.setMinEms(5);
		}
		LogUtil.i("0424", "updateByDownLoadState name = " + appItem.name
				+ ",state = " + appItem.state + ",apk = " + appItem.apk);
		if (downstateDesc != null) {
			downstateDesc.setVisibility(View.GONE);
		}
		switch (appItem.state) {
		case AppItemState.STATE_WAIT: {
			safeMoreGameSetText(mContext, stateActionView, R.drawable.beston_btn_downactivityawable,
					R.string.state_wait_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(mContext, currentStateView, R.string.state_wait_text,
					false || !updateStateTextBg);
			showProgress = true;
			break;
		}
		case AppItemState.STATE_RETRY: {
			safeMoreGameSetText(mContext, stateActionView, R.drawable.beston_btn_downactivityawable,
					R.string.state_retry_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(mContext, currentStateView, R.string.state_retry_text,
					false || !updateStateTextBg);
			showProgress = false;
			break;
		}
		case AppItemState.STATE_ONGOING: {
			safeMoreGameSetText(mContext, stateActionView,
					R.drawable.beston_btn_downactivityawable,
					R.string.state_ongoing_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			if (isInstalled(appItem.pname)) {
				safeSetText(mContext, currentStateView,
						R.string.state_ongoing_need_update_text,
						false || !updateStateTextBg);
			} else {
				safeSetText(mContext, currentStateView,
						R.string.state_ongoing_text,
						false || !updateStateTextBg);
			}
			break;
		}
		case AppItemState.STATE_PAUSE: {
			safeMoreGameSetText(mContext, stateActionView,
					R.drawable.beston_btn_downactivityawable, R.string.state_pause_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(mContext, currentStateView, R.string.state_pause_text,
					false || !updateStateTextBg);
			break;
		}
		case AppItemState.STATE_DOWNLOAD_FINISH: {
			LogUtil.i("0424", "touch AppItemState.STATE_DOWNLOAD_FINISH = "
					+ appItem.name);
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.beston_btn_down_opendrawable,
						R.string.state_finish_need_update_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView,
						R.string.state_finish_need_update_text,
						false || !updateStateTextBg);
				showProgress = false;
			} else {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.bestone_down_drawable,
						// R.string.state_finish_action_text,
						R.string.btn_install, false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView,
						R.string.state_finish_text, false || !updateStateTextBg);
				showProgress = false;
			}
			break;
		}
		case AppItemState.STATE_INSTALLING: {
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_need_update_action_text,
						false || !updateStateTextBg);
				safeSetText(mContext, currentStateView,
						R.string.state_installing_need_update_text,
						false || !updateStateTextBg);
			} else {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_action_text,
						false || !updateStateTextBg);
				safeSetText(mContext, currentStateView,
						R.string.state_installing_text,
						false || !updateStateTextBg);
			}
			break;
		}
		case AppItemState.STATE_INSTALL_FAILED: {
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.beston_btn_down_opendrawable,
						R.string.state_install_failed_need_update_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView,
						R.string.state_install_failed_need_update_text,
						false || !updateStateTextBg);
				showProgress = false;
			} else {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.bestone_down_drawable,
						R.string.state_install_failed_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView,
						R.string.state_install_failed_text,
						false || !updateStateTextBg);
				showProgress = false;
			}
			break;
		}
		case AppItemState.STATE_UNINSTALLING: {
			safeMoreGameSetText(mContext, stateActionView,
					 R.drawable.beston_btn_down_updateingrawable,
					R.string.state_update_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(mContext, currentStateView,
					R.string.state_uninstalling_text,
					false || !updateStateTextBg);
			break;
		}
		case AppItemState.STATE_NEED_UPDATE: {
			safeMoreGameSetText(mContext, stateActionView,
					R.drawable.beston_btn_down_opendrawable,
					R.string.state_update_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			showProgress = false;
			break;
		}
		case AppItemState.STATE_INSTALLED_NEW: {
			safeMoreGameSetText(mContext, stateActionView,
					R.drawable.beston_btn_down_opendrawable,
					R.string.state_installed_new_action_text,
					true || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_orange);
			}
			showProgress = false;
			break;
		}
		case AppItemState.STATE_IDLE: {
			LogUtil.i("0424", "touch STATE_IDLE = " + appItem.name);
			if (updateStateTextBg && which != null
					&& which.equals(LogModel.L_HOME_REC)) {
				// 如果安装文件还存在，则显示安装
				if (apkFileExist(appItem)) {
					safeSetBg(stateActionView, R.drawable.btn_down_rec_bg);
					safeMoreGameSetText(mContext, stateActionView,
							R.drawable.bestone_down_drawable, R.string.btn_install,
							true || !updateStateTextBg);
				} else {
					safeSetBg(stateActionView, R.drawable.btn_down_rec_bg);
					safeMoreGameSetText(mContext, stateActionView,
							R.drawable.bestone_down_drawable,
							R.string.download,
							false || !updateStateTextBg);
				}
			} else {
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				if (apkFileExist(appItem)) {
					safeMoreGameSetText(mContext, stateActionView,
							R.drawable.bestone_down_drawable, R.string.btn_install,
							false || !updateStateTextBg);
				} else {
					safeMoreGameSetText(mContext, stateActionView,
							R.drawable.bestone_down_drawable,
							R.string.download,
							false || !updateStateTextBg);
				}

			}
			showProgress = false;
			break;
		}
		}

		if (appItem instanceof DownloadItem && showProgress) {
			final DownloadItem downloadItem = (DownloadItem) appItem;
			final long totleSize = downloadItem.dSize > 0 ? downloadItem.dSize
					: downloadItem.length;
			final int percent = totleSize == 0 ? 100
					: (int) ((downloadItem.lastDSize * 100) / totleSize);
			if (progressBar != null) {
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setProgress(percent);
				if (downstateDesc != null) {
					downstateDesc.setVisibility(View.VISIBLE);
					if (appItem.state == AppItemState.STATE_PAUSE) {
						downstateDesc.setText(R.string.state_pause_text);
					} else {
						downstateDesc.setText(percent + "%");
					}
				}

				if (appItem.state == AppItemState.STATE_INSTALLING) {
					if (downstateDesc != null) {
						downstateDesc.setVisibility(View.GONE);
					}
					progressBar.setIndeterminate(true);
				} else {
					progressBar.setIndeterminate(false);
				}
			}
			if (progressText != null) {
				if (showDetailProgress) {
					String lastSizeStr = StringUtil
							.formageDownloadSize(downloadItem.lastDSize);
					String dSizeStr = StringUtil
							.formageDownloadSize(downloadItem.dSize > 0 ? downloadItem.dSize
									: downloadItem.length);
					String formatStr = mContext.getString(
							R.string.down_progress_detail, lastSizeStr,
							dSizeStr);
					progressText.setText(formatStr);
					StringUtil.setSpannableString(progressText, formatStr, 0,
							lastSizeStr.length(), Color.parseColor("#999999"));
				} else if (showInfo) {
					progressText.setVisibility(View.GONE);
				} else {
					progressText.setText(percent + "%");
					progressText.setTag(percent);
				}
			}
		} else {
			if (downstateDesc != null) {
				downstateDesc.setVisibility(View.GONE);
			}
			if (progressBar != null) {
				progressBar.setVisibility(View.GONE);
			}
			if (progressText != null) {
				progressText.setVisibility(View.VISIBLE);
				if ((appItem instanceof DownloadItem)) {
					final DownloadItem downloadItem = (DownloadItem) appItem;
					String versionDescText = "";
					summaryText.setVisibility(View.VISIBLE);
					if (isShowDownLoadContent) {
						// 无progress,属于已下载项中，progressText显示版本，summaryText显示大小
						versionDescText = mContext
								.getString(R.string.down_summary);
						progressText.setText(versionDescText
								+ downloadItem.vname);
						// summaryText.setText(context.getString(R.string.down_progress_detail2,
						// StringUtil.formageDownloadSize(downloadItem.dSize > 0
						// ? downloadItem.dSize : downloadItem.length)));
						long length = new File(getApkFilePath(appItem))
								.length();
						summaryText.setText(Formatter.formatShortFileSize(
								mContext, length));
					} else {
						// 无progress,属于游戏更新中，progressText显示当前版本，summaryText显示更新版本
						versionDescText = mContext
								.getString(R.string.text_game_update_desc);
						progressText.setText(versionDescText
								+ getPackageCurrentVersionName(
										downloadItem.pname, mContext));
						summaryText.setText(mContext
								.getString(R.string.text_game_update_desc2)
								+ downloadItem.vname);
					}
				}
				progressText.setTag(0);
			}

		}
	}

	public String getPackageCurrentVersionName(String packageName,
			Context context) {

		if (packageName == null || packageName.length() == 0)
			return null;
		PackageInfo sPackageInfo = null;
		try {
			sPackageInfo = context.getPackageManager().getPackageInfo(
					packageName, 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		if (sPackageInfo == null) {
			return null;
		}
		return sPackageInfo.versionName;
	}

	/**
	 * 将所有下载中、等待的任务改为暂停 返回是否有修改
	 * */
	public boolean syncAllDownloadStates2Pause() {

		ContentValues values = new ContentValues();
		values.put(DownloadTask.COLUMN_STATE, AppItemState.STATE_PAUSE);
		int updateCount = DownloadTask.update(mContext,
				DownloadTask.CONTENT_URI, values, DownloadTask.COLUMN_STATE
						+ " IS " + AppItemState.STATE_ONGOING + " OR "
						+ DownloadTask.COLUMN_STATE + " IS "
						+ AppItemState.STATE_WAIT, null);
		Log.i("zc", "updateCount = " + updateCount);
		return updateCount > 0 ? true : false;
	}

	/**
	 * 初始化下载管理和游戏更新的条目数，并保存
	 */
	public int getAllDowoLoadCount() {
		final List<AppItem> alllist = DownloadTaskManager.getInstance()
				.getAllDownloadItem(false);
		final ArrayList<AppItem> filterList = new ArrayList<AppItem>();
		for (AppItem appItem : alllist) {
			if (appItem.state < AppItemState.STATE_DOWNLOAD_FINISH
					&& appItem.state > AppItemState.STATE_IDLE) {
				filterList.add(appItem);
			} else {
				// 已安装的包不显示
				if (PackageUtil.isInstalledApk(mContext, appItem.pname, null)) {
					continue;
				}
				// 未安装的包且安装包不存在的不显示，若安装包存在则显示安装
				if (!PackageUtil.isInstalledApk(mContext, appItem.pname, null)
						&& !DownloadTaskManager.getInstance().apkFileExist(
								appItem)) {
					continue;
				}
				filterList.add(appItem);
			}
		}
		return filterList.size();
	}

	/**
	 * 获取所有需要更新的游戏数量
	 * 
	 * @return
	 */
	public int getNeedUpdateAppCount() {
		return getAllNeedUpdateAppList().size();
	}

	/**
	 * 获取所有需要更新的游戏列表
	 * 
	 * @return
	 */
	public List<AppItem> getAllNeedUpdateAppList() {
		final List<AppItem> alllist = DownloadTaskManager.getInstance()
				.getAllInstallDownloadItem(false);
		final List<AppItem> needUpdatelist = new ArrayList<AppItem>();
		DownloadTaskManager.getInstance().fillAppStates(alllist);
		PackageManager pm = mContext.getPackageManager();
		for (AppItem appItem : alllist) {
			int localeVersionCode = 0;
			try {
				localeVersionCode = pm.getPackageInfo(appItem.pname, 0).versionCode;
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			if (appItem.vcode > localeVersionCode) {
				needUpdatelist.add(appItem);
			}
		}
		return needUpdatelist;
	}

	/**
	 * 获取所有正在更新的游戏列表
	 */
	public List<AppItem> getAllUpdatingAppList() {
		final List<AppItem> alllist = DownloadTaskManager.getInstance()
				.getAllInstallDownloadItem(false);
		final List<AppItem> updatinglist = new ArrayList<AppItem>();
		DownloadTaskManager.getInstance().fillAppStates(alllist);
		PackageManager pm = mContext.getPackageManager();
		for (AppItem appItem : alllist) {
			int localeVersionCode = 0;
			try {
				localeVersionCode = pm.getPackageInfo(appItem.pname, 0).versionCode;
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			if (appItem.vcode > localeVersionCode
					&& appItem.state == AppItemState.STATE_ONGOING) {
				updatinglist.add(appItem);
			}
		}
		LogUtil.i("appupdate","getAllUpdatingAppList size = "+updatinglist.size());
		return updatinglist;
	}

	/**
	 * 根据包名获得游戏下载任务
	 */
	public DownloadTask getDownloadTaskByPackageName(String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return null;
		}
		Iterator iterator = mAllTasks.keySet().iterator();
		if (iterator.hasNext()) {
			do {
				final DownloadTask downloadTask = mAllTasks
						.get(iterator.next());
				if (packageName.equals(downloadTask.mDownloadItem.pname)) {
					return downloadTask;
				}
			} while (iterator.hasNext());
		}
		return null;
	}

	public void networkIsOk() {
		for (DownloadTaskListener listener : mListeners) {
			listener.networkIsOk();
		}
	}

	public void letAllDownningTaskPause() {
		DownloadTaskManager.getInstance().syncAllDownloadStates2Pause();
		DownloadTaskManager.getInstance().modifyAllWailtState2Pause();
		DownloadTaskManager.getInstance().pauseAllTasks();
	}
	/*
	 *  ------------The Bestone modifed start--------------
	 *   Modified by "zengxiao"  Date:20140513
	 *   Modified for:增加价格对比
	 */
							
	
	
	public void updateByState2(Context context, TextView stateActionView,
			AppItem appItem, ProgressBar progressBar, TextView progressText,
			boolean showDetailProgress, boolean showInfo) {
		updateByStateimpl2(context, stateActionView, appItem, progressBar,
				progressText, showDetailProgress,showInfo, true, null, null);
	}
	public void updateByStateimpl2(Context context, TextView stateActionView,
			AppItem appItem, ProgressBar progressBar, TextView progressText,
			boolean showDetailProgress, boolean showInfo,
			boolean updateStateTextBg, TextView currentStateView, String which) {
		progressText.setVisibility(View.VISIBLE);
		boolean showProgress = true;
		if (stateActionView != null) {
			stateActionView.setMinEms(5);
		}
		if(appItem==null)
		{
			return;
		}
		switch (appItem.state) {
		case AppItemState.STATE_WAIT: {
			safeMoreGameSetText(context, stateActionView,R.drawable.beston_btn_down_waitrawable,
					R.string.state_wait_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText2(context, currentStateView, R.string.state_wait_text,
					false || !updateStateTextBg);
			progressText.setVisibility(View.GONE);
			showProgress = true;
			break;
		}
		case AppItemState.STATE_RETRY: {
			safeMoreGameSetText(context, stateActionView, R.drawable.beston_btn_down_retryawable,
					R.string.state_retry_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText2(context, currentStateView, R.string.state_retry_text,
					false || !updateStateTextBg);
			showProgress = false;
			break;
		}
		case AppItemState.STATE_ONGOING: {
			safeMoreGameSetText(context, stateActionView,R.drawable.beston_btn_down_downingrawable,
					R.string.notextbutton,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			if (isInstalled(appItem.pname)) {
				safeSetText2(context, currentStateView,
						R.string.state_ongoing_need_update_text,
						false || !updateStateTextBg);
			} else {
				safeSetText2(context, currentStateView,
						R.string.state_ongoing_text,
						false || !updateStateTextBg);
			}

			break;
		}
		case AppItemState.STATE_PAUSE: {
			safeMoreGameSetText(context, stateActionView,	R.drawable.beston_btn_down_continuawable,
					R.string.state_pause_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText2(context, currentStateView, R.string.state_pause_text,
					false || !updateStateTextBg);
			progressText.setVisibility(View.GONE);
			break;
		}
		case AppItemState.STATE_DOWNLOAD_FINISH: {
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(context, stateActionView,R.drawable.beston_btn_down_opendrawable,
						R.string.state_finish_need_update_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText2(context, currentStateView,
						R.string.state_finish_need_update_text,
						false || !updateStateTextBg);
				showProgress = false;
			} else {
				safeMoreGameSetText(context, stateActionView,R.drawable.bestone_down_drawable,
						R.string.state_finish_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText2(context, currentStateView,
						R.string.state_finish_text, false || !updateStateTextBg);
				showProgress = false;
			}
			break;
		}
		case AppItemState.STATE_INSTALLING: {
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(context, stateActionView,R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_need_update_action_text,
						false || !updateStateTextBg);
				safeSetText2(context, currentStateView,
						R.string.state_installing_need_update_text,
						false || !updateStateTextBg);
			} else {
				safeMoreGameSetText(context, stateActionView,R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_action_text,
						false || !updateStateTextBg);
				safeSetText2(context, currentStateView,
						R.string.state_installing_text,
						false || !updateStateTextBg);
			}
			break;
		}
		case AppItemState.STATE_INSTALL_FAILED: {
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(context, stateActionView,	R.drawable.beston_btn_down_opendrawable,
						R.string.state_install_failed_need_update_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText2(context, currentStateView,
						R.string.state_install_failed_need_update_text,
						false || !updateStateTextBg);
				showProgress = false;
			} else {
				safeMoreGameSetText(context, stateActionView,R.drawable.bestone_down_drawable,
						R.string.state_install_failed_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText2(context, currentStateView,
						R.string.state_install_failed_text,
						false || !updateStateTextBg);
				showProgress = false;
			}
			break;
		}
		case AppItemState.STATE_UNINSTALLING: {
			safeMoreGameSetText(context, stateActionView,R.drawable.beston_btn_down_updateingrawable,
					R.string.state_uninstalling_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText2(context, currentStateView,
					R.string.state_uninstalling_text,
					false || !updateStateTextBg);
			break;
		}
		case AppItemState.STATE_NEED_UPDATE: {
			safeMoreGameSetText(context, stateActionView,R.drawable.beston_btn_down_opendrawable,
					R.string.state_update_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			showProgress = false;
			break;
		}
		case AppItemState.STATE_INSTALLED_NEW: {
			safeMoreGameSetText(context, stateActionView,	R.drawable.beston_btn_down_opendrawable,
					R.string.state_installed_new_action_text,
					true || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_orange);
			}
			showProgress = false;
			break;
		}
		case AppItemState.STATE_IDLE: {
			if (apkFileExist(appItem)) {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.bestone_down_drawable, R.string.state_idle_action_text,
						true || !updateStateTextBg);
			}
			else
			{
			if (updateStateTextBg && which != null
					&& which.equals(LogModel.L_HOME_REC)) {
				safeSetBg(stateActionView, R.drawable.btn_down_rec_bg);
				safeMoreGameSetText(context, stateActionView,R.drawable.bestone_down_drawable,
						R.string.detailfree,
						true || !updateStateTextBg);
			} else {
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeMoreGameSetText(context, stateActionView,R.drawable.bestone_down_drawable,
						R.string.detailfree,
						false || !updateStateTextBg);
			}
			}
			showProgress = false;
			break;
		}
		}
		
	}
	private void safeSetText2(Context context, TextView textView, int strID,
			boolean txtcolorChange) {
		if (textView != null) {
			textView.setText(strID);	
		}
	}
	
	public AppItem getAppItemStates(AppItem appItem) {
		try{
		if (appItem == null) {
			return null;
		}
		int state = AppItemState.STATE_IDLE;
			state = PackageUtil.isApkInstalled(mContext, appItem.pname,
					appItem.vcode);
			if (state > AppItemState.STATE_NEED_UPDATE) {
				appItem.state = state;
				if (mAllTasks.containsKey(String.valueOf(appItem.sid))) {
					mAllTasks
							.get(appItem.sid).mDownloadItem.state = state;
				}
			} else if (mAllTasks.containsKey(String.valueOf(appItem.sid))) {
				appItem.state = mAllTasks.get(String.valueOf(appItem.sid)).mDownloadItem.state;
			}
			return appItem;
		}catch(Exception e)
		{
			return appItem;
			
		}
	}
	//收藏下载更新按钮
	public void updateByDownLoadStateFav(TextView stateActionView,
			AppItem appItem, ProgressBar progressBar, TextView progressText,
			boolean showDetailProgress, boolean showInfo,
			boolean updateStateTextBg, TextView currentStateView, String which,
			TextView summaryText, boolean isShowDownLoadContent,
			TextView downstateDesc) {
		boolean showProgress = true;
		if (stateActionView != null) {
			stateActionView.setMinEms(5);
		}
		LogUtil.i("0424", "updateByDownLoadState name = " + appItem.name
				+ ",state = " + appItem.state + ",apk = " + appItem.apk);
		if (downstateDesc != null) {
			downstateDesc.setVisibility(View.GONE);
		}
		switch (appItem.state) {
		case AppItemState.STATE_WAIT: {
			safeMoreGameSetText(mContext, stateActionView, R.drawable.beston_btn_down_waitrawable,
					R.string.state_wait_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(mContext, currentStateView, R.string.state_wait_text,
					false || !updateStateTextBg);
			showProgress = true;
			break;
		}
		case AppItemState.STATE_RETRY: {
			safeMoreGameSetText(mContext, stateActionView, R.drawable.beston_btn_down_retryawable,
					R.string.state_retry_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(mContext, currentStateView, R.string.state_retry_text,
					false || !updateStateTextBg);
			showProgress = false;
			break;
		}
		case AppItemState.STATE_ONGOING: {
			safeMoreGameSetText(mContext, stateActionView,
					R.drawable.beston_btn_down_downingrawable,
					R.string.notextbutton,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			if (isInstalled(appItem.pname)) {
				safeSetText(mContext, currentStateView,
						R.string.state_ongoing_need_update_text,
						false || !updateStateTextBg);
			} else {
				safeSetText(mContext, currentStateView,
						R.string.state_ongoing_text,
						false || !updateStateTextBg);
			}
			break;
		}
		case AppItemState.STATE_PAUSE: {
			safeMoreGameSetText(mContext, stateActionView,
					R.drawable.beston_btn_down_continuawable, R.string.state_pause_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(mContext, currentStateView, R.string.state_pause_text,
					false || !updateStateTextBg);
			break;
		}
		case AppItemState.STATE_DOWNLOAD_FINISH: {
			LogUtil.i("0424", "touch AppItemState.STATE_DOWNLOAD_FINISH = "
					+ appItem.name);
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.beston_btn_down_opendrawable,
						R.string.state_finish_need_update_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView,
						R.string.state_finish_need_update_text,
						false || !updateStateTextBg);
				showProgress = false;
			} else {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.bestone_down_drawable,
						// R.string.state_finish_action_text,
						R.string.btn_install, false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView,
						R.string.state_finish_text, false || !updateStateTextBg);
				showProgress = false;
			}
			break;
		}
		case AppItemState.STATE_INSTALLING: {
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_need_update_action_text,
						false || !updateStateTextBg);
				safeSetText(mContext, currentStateView,
						R.string.state_installing_need_update_text,
						false || !updateStateTextBg);
			} else {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_action_text,
						false || !updateStateTextBg);
				safeSetText(mContext, currentStateView,
						R.string.state_installing_text,
						false || !updateStateTextBg);
			}
			break;
		}
		case AppItemState.STATE_INSTALL_FAILED: {
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.beston_btn_down_opendrawable,
						R.string.state_install_failed_need_update_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView,
						R.string.state_install_failed_need_update_text,
						false || !updateStateTextBg);
				showProgress = false;
			} else {
				safeMoreGameSetText(mContext, stateActionView,
						R.drawable.bestone_down_drawable,
						R.string.state_install_failed_action_text,
						false || !updateStateTextBg);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView,
						R.string.state_install_failed_text,
						false || !updateStateTextBg);
				showProgress = false;
			}
			break;
		}
		case AppItemState.STATE_UNINSTALLING: {
			safeMoreGameSetText(mContext, stateActionView,
					 R.drawable.beston_btn_down_updateingrawable,
					R.string.state_update_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText(mContext, currentStateView,
					R.string.state_uninstalling_text,
					false || !updateStateTextBg);
			break;
		}
		case AppItemState.STATE_NEED_UPDATE: {
			safeMoreGameSetText(mContext, stateActionView,
					R.drawable.bestone_down_drawable,
					R.string.state_update_action_text,
					false || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			showProgress = false;
			break;
		}
		case AppItemState.STATE_INSTALLED_NEW: {
			safeMoreGameSetText(mContext, stateActionView,
					R.drawable.beston_btn_down_opendrawable,
					R.string.state_installed_new_action_text,
					true || !updateStateTextBg);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_orange);
			}
			showProgress = false;
			break;
		}
		case AppItemState.STATE_IDLE: {
			LogUtil.i("0424", "touch STATE_IDLE = " + appItem.name);
			if (updateStateTextBg && which != null
					&& which.equals(LogModel.L_HOME_REC)) {
				// 如果安装文件还存在，则显示安装
				if (apkFileExist(appItem)) {
					safeSetBg(stateActionView, R.drawable.btn_down_rec_bg);
					safeMoreGameSetText(mContext, stateActionView,
							R.drawable.bestone_down_drawable, R.string.btn_install,
							true || !updateStateTextBg);
				} else {
					safeSetBg(stateActionView, R.drawable.btn_down_rec_bg);
					safeMoreGameSetText(mContext, stateActionView,
							R.drawable.bestone_down_drawable,
							R.string.download,
							false || !updateStateTextBg);
				}
			} else {
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				if (apkFileExist(appItem)) {
					safeMoreGameSetText(mContext, stateActionView,
							R.drawable.bestone_down_drawable, R.string.btn_install,
							false || !updateStateTextBg);
				} else {
					safeMoreGameSetText(mContext, stateActionView,
							R.drawable.bestone_down_drawable,
							R.string.download,
							false || !updateStateTextBg);
				}

			}
			showProgress = false;
			break;
		}
		}

		if (appItem instanceof DownloadItem && showProgress) {
			final DownloadItem downloadItem = (DownloadItem) appItem;
			final long totleSize = downloadItem.dSize > 0 ? downloadItem.dSize
					: downloadItem.length;
			final int percent = totleSize == 0 ? 100
					: (int) ((downloadItem.lastDSize * 100) / totleSize);
			if (progressBar != null) {
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setProgress(percent);
				if (progressText != null) {
					progressText.setVisibility(View.VISIBLE);
					if ((appItem.state == AppItemState.STATE_PAUSE)||(appItem.state ==  AppItemState.STATE_WAIT)) {
						progressText.setVisibility(View.GONE);
					} else {
						progressText.setText(percent + "%");
					}
				}

				if (appItem.state == AppItemState.STATE_INSTALLING) {
					if (progressText != null) {
						progressText.setVisibility(View.GONE);
					}
					progressBar.setIndeterminate(true);
				} else {
					progressBar.setIndeterminate(false);
				}
					
				progressText.setTag(0);
			}

		}
	}
	//收藏下载更新按钮
		public void updateByDownLoadStateFav2(TextView stateActionView,
				AppItem appItem, ProgressBar progressBar, TextView progressText,
				boolean showDetailProgress, boolean showInfo,
				boolean updateStateTextBg, TextView currentStateView, String which,
				TextView summaryText, boolean isShowDownLoadContent,
				TextView downstateDesc,RelativeLayout sharelayout,ImageView shareicon) {
			boolean showProgress = true;
			if (stateActionView != null) {
				stateActionView.setMinEms(5);
			}
			LogUtil.i("0424", "updateByDownLoadState name = " + appItem.name
					+ ",state = " + appItem.state + ",apk = " + appItem.apk);
			if (downstateDesc != null) {
				downstateDesc.setVisibility(View.GONE);
			}
			switch (appItem.state) {
			case AppItemState.STATE_WAIT: {
				//shareicon.setVisibility(View.GONE);
				safeMoreGameSetTextshare(mContext, stateActionView, R.drawable.beston_btn_down_waitrawable,
						R.string.state_wait_action_text,
						false || !updateStateTextBg,sharelayout,0);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView, R.string.state_wait_text,
						false || !updateStateTextBg);
				showProgress = true;
				break;
			}
			case AppItemState.STATE_RETRY: {
				//shareicon.setVisibility(View.GONE);
				safeMoreGameSetTextshare(mContext, stateActionView, R.drawable.beston_btn_down_retryawable,
						R.string.state_retry_action_text,
						false || !updateStateTextBg,sharelayout,0);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView, R.string.state_retry_text,
						false || !updateStateTextBg);
				showProgress = false;
				break;
			}
			case AppItemState.STATE_ONGOING: {
				//shareicon.setVisibility(View.GONE);
				safeMoreGameSetTextshare(mContext, stateActionView,
						R.drawable.beston_btn_down_downingrawable,
						R.string.notextbutton,
						false || !updateStateTextBg,sharelayout,0);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				if (isInstalled(appItem.pname)) {
					safeSetText(mContext, currentStateView,
							R.string.state_ongoing_need_update_text,
							false || !updateStateTextBg);
				} else {
					safeSetText(mContext, currentStateView,
							R.string.state_ongoing_text,
							false || !updateStateTextBg);
				}
				break;
			}
			case AppItemState.STATE_PAUSE: {
				//shareicon.setVisibility(View.GONE);
				safeMoreGameSetTextshare(mContext, stateActionView,
						R.drawable.beston_btn_down_continuawable, R.string.state_pause_action_text,
						false || !updateStateTextBg,sharelayout,0);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView, R.string.state_pause_text,
						false || !updateStateTextBg);
				break;
			}
			case AppItemState.STATE_DOWNLOAD_FINISH: {
				//shareicon.setVisibility(View.GONE);
				LogUtil.i("0424", "touch AppItemState.STATE_DOWNLOAD_FINISH = "
						+ appItem.name);
				if (isInstalled(appItem.pname)) {
					safeMoreGameSetTextshare(mContext, stateActionView,
							R.drawable.beston_btn_down_opendrawable,
							R.string.state_finish_need_update_action_text,
							false || !updateStateTextBg,sharelayout,0);
					if (updateStateTextBg) {
						safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
					}
					safeSetText(mContext, currentStateView,
							R.string.state_finish_need_update_text,
							false || !updateStateTextBg);
					showProgress = false;
				} else {
					safeMoreGameSetTextshare(mContext, stateActionView,
							R.drawable.bestone_down_drawable,
							// R.string.state_finish_action_text,
							R.string.btn_install, false || !updateStateTextBg,sharelayout,0);
					if (updateStateTextBg) {
						safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
					}
					safeSetText(mContext, currentStateView,
							R.string.state_finish_text, false || !updateStateTextBg);
					showProgress = false;
				}
				break;
			}
			case AppItemState.STATE_INSTALLING: {
			//	shareicon.setVisibility(View.GONE);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				if (isInstalled(appItem.pname)) {
					safeMoreGameSetTextshare(mContext, stateActionView,
							R.drawable.beston_btn_down_updateingrawable,
							R.string.state_installing_need_update_action_text,
							false || !updateStateTextBg,sharelayout,0);
					safeSetText(mContext, currentStateView,
							R.string.state_installing_need_update_text,
							false || !updateStateTextBg);
				} else {
					safeMoreGameSetTextshare(mContext, stateActionView,
							R.drawable.beston_btn_down_updateingrawable,
							R.string.state_installing_action_text,
							false || !updateStateTextBg,sharelayout,0);
					safeSetText(mContext, currentStateView,
							R.string.state_installing_text,
							false || !updateStateTextBg);
				}
				break;
			}
			case AppItemState.STATE_INSTALL_FAILED: {
			//	shareicon.setVisibility(View.GONE);
				if (isInstalled(appItem.pname)) {
					safeMoreGameSetTextshare(mContext, stateActionView,
							R.drawable.beston_btn_down_opendrawable,
							R.string.state_install_failed_need_update_action_text,
							false || !updateStateTextBg,sharelayout,0);
					if (updateStateTextBg) {
						safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
					}
					safeSetText(mContext, currentStateView,
							R.string.state_install_failed_need_update_text,
							false || !updateStateTextBg);
					showProgress = false;
				} else {
					safeMoreGameSetTextshare(mContext, stateActionView,
							R.drawable.bestone_down_drawable,
							R.string.state_install_failed_action_text,
							false || !updateStateTextBg,sharelayout,0);
					if (updateStateTextBg) {
						safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
					}
					safeSetText(mContext, currentStateView,
							R.string.state_install_failed_text,
							false || !updateStateTextBg);
					showProgress = false;
				}
				break;
			}
			case AppItemState.STATE_UNINSTALLING: {
				//shareicon.setVisibility(View.GONE);
				safeMoreGameSetTextshare(mContext, stateActionView,
						 R.drawable.beston_btn_down_updateingrawable,
						R.string.state_update_action_text,
						false || !updateStateTextBg,sharelayout,0);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText(mContext, currentStateView,
						R.string.state_uninstalling_text,
						false || !updateStateTextBg);
				break;
			}
			case AppItemState.STATE_NEED_UPDATE: {
				//shareicon.setVisibility(View.GONE);
				safeMoreGameSetTextshare(mContext, stateActionView,
						R.drawable.bestone_down_drawable,
						R.string.state_update_action_text,
						false || !updateStateTextBg,sharelayout,0);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				showProgress = false;
				break;
			}
			case AppItemState.STATE_INSTALLED_NEW: {
			//	shareicon.setVisibility(View.GONE);
				safeMoreGameSetTextshare(mContext, stateActionView,
						R.drawable.beston_btn_down_opendrawable,
						R.string.state_installed_new_action_text,
						true || !updateStateTextBg,sharelayout,0);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_orange);
				}
				showProgress = false;
				break;
			}
			case AppItemState.STATE_IDLE: {
				LogUtil.i("0424", "touch STATE_IDLE = " + appItem.name);
				if (updateStateTextBg && which != null
						&& which.equals(LogModel.L_HOME_REC)) {
					// 如果安装文件还存在，则显示安装
					if (apkFileExist(appItem)) {
					//	shareicon.setVisibility(View.GONE);
						safeSetBg(stateActionView, R.drawable.btn_down_rec_bg);
						safeMoreGameSetTextshare(mContext, stateActionView,
								R.drawable.bestone_down_drawable, R.string.btn_install,
								true || !updateStateTextBg,sharelayout,0);
					} else {
						//shareicon.setVisibility(View.VISIBLE);
						safeSetBg(stateActionView, R.drawable.btn_down_rec_bg);
						safeMoreGameSetTextshare(mContext, stateActionView,
								R.drawable.bestone_down_drawable,
								R.string.sharedown,
								false || !updateStateTextBg,sharelayout,1);
					}
				} else {
					if (updateStateTextBg) {
						safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
					}
					if (apkFileExist(appItem)) {
						//shareicon.setVisibility(View.GONE);
						safeMoreGameSetTextshare(mContext, stateActionView,
								R.drawable.bestone_down_drawable, R.string.btn_install,
								false || !updateStateTextBg,sharelayout,0);
					} else {
						//shareicon.setVisibility(View.VISIBLE);
						safeMoreGameSetTextshare(mContext, stateActionView,
								R.drawable.bestone_down_drawable,
								R.string.sharedown,
								false || !updateStateTextBg,sharelayout,1);
					}

				}
				showProgress = false;
				break;
			}
			}

			if (appItem instanceof DownloadItem && showProgress) {
				final DownloadItem downloadItem = (DownloadItem) appItem;
				final long totleSize = downloadItem.dSize > 0 ? downloadItem.dSize
						: downloadItem.length;
				final int percent = totleSize == 0 ? 100
						: (int) ((downloadItem.lastDSize * 100) / totleSize);
				if (progressBar != null) {
					progressBar.setVisibility(View.VISIBLE);
					progressBar.setProgress(percent);
					if (progressText != null) {
						progressText.setVisibility(View.VISIBLE);
						if ((appItem.state == AppItemState.STATE_PAUSE)||(appItem.state ==  AppItemState.STATE_WAIT)) {
							progressText.setVisibility(View.GONE);
						} else {
							progressText.setText(percent + "%");
						}
					}

					if (appItem.state == AppItemState.STATE_INSTALLING) {
						if (progressText != null) {
							progressText.setVisibility(View.GONE);
						}
						progressBar.setIndeterminate(true);
					} else {
						progressBar.setIndeterminate(false);
					}
						
					progressText.setTag(0);
				}

			}
		}
	 /*
		------------The Bestone modifed end--------------
		*/
/*
 * 
 * 
 * bestone add by zengxiao for:添加分享下载处理
 * 
 * */
	public void updateItemBtnByStateshare(Context context, TextView stateActionView,
			AppItem appItem, ProgressBar progressBar, TextView progressText,ImageView imageicon,RelativeLayout sharelayout) {
		updateItemBtnByStateshare(context, stateActionView, appItem, true,  progressBar,  progressText,imageicon,sharelayout);
	}

	public void updateItemBtnByStateshare(Context context, TextView stateActionView,
			AppItem appItem, boolean updateStateTextBg, ProgressBar progressBar, TextView progressText,ImageView imageicon,RelativeLayout sharelayout) {
		if (stateActionView != null) {
			//stateActionView.setMinEms(5);
		}		
		boolean showProgress = true;
		switch (appItem.state) {
		case AppItemState.STATE_WAIT: {
			
			//imageicon.setVisibility(View.GONE);
			safeMoreGameSetTextshare(context, stateActionView, R.drawable.beston_btn_down_waitrawable,
					R.string.state_wait_action_text,
					false || !updateStateTextBg,sharelayout,0);
			showProgress = true;
			break;
		}
		case AppItemState.STATE_RETRY: {
			//imageicon.setVisibility(View.GONE);
			safeMoreGameSetTextshare(context, stateActionView, R.drawable.beston_btn_down_retryawable,
					R.string.state_retry_action_text,
					false || !updateStateTextBg,sharelayout,0);
			showProgress = false;

			break;
		}
		case AppItemState.STATE_ONGOING: {
			//imageicon.setVisibility(View.GONE);
			safeMoreGameSetTextshare(context, stateActionView,
					R.drawable.beston_btn_down_downingrawable,
					R.string.notextbutton,
					false || !updateStateTextBg,sharelayout,0);
			break;
		}
		case AppItemState.STATE_PAUSE: {// 暂停
			//imageicon.setVisibility(View.GONE);
			safeMoreGameSetTextshare(context, stateActionView,
					R.drawable.beston_btn_down_continuawable, R.string.state_pause_action_text,
					false || !updateStateTextBg,sharelayout,0);
			break;
		}
		case AppItemState.STATE_DOWNLOAD_FINISH: {
			//imageicon.setVisibility(View.GONE);
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetTextshare(context, stateActionView,
						R.drawable.beston_btn_down_opendrawable,
						R.string.state_finish_need_update_action_text,
						false || !updateStateTextBg,sharelayout,0);

			} else {
				safeMoreGameSetTextshare(context, stateActionView,
						R.drawable.bestone_down_drawable,
						R.string.state_finish_action_text,
						false || !updateStateTextBg,sharelayout,0);
			}
			showProgress = false;

			break;
		}
		case AppItemState.STATE_INSTALLING: {
			//imageicon.setVisibility(View.GONE);
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetTextshare(context, stateActionView,
						R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_need_update_action_text,
						false || !updateStateTextBg,sharelayout,0);
			} else {
				if (null != stateActionView)
					stateActionView
							.setCompoundDrawables(null, null, null, null);
				safeMoreGameSetTextshare(context, stateActionView,
						R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_action_text,
						false || !updateStateTextBg,sharelayout,0);
				
			}
			showProgress = false;

			break;
		}
		case AppItemState.STATE_INSTALL_FAILED: {
			//imageicon.setVisibility(View.GONE);
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetTextshare(context, stateActionView,
						R.drawable.beston_btn_down_opendrawable,
						R.string.state_install_failed_need_update_action_text,
						false || !updateStateTextBg,sharelayout,0);
			} else {
				safeMoreGameSetTextshare(context, stateActionView,
						R.drawable.bestone_down_drawable,
						R.string.state_install_failed_action_text,
						false || !updateStateTextBg,sharelayout,0);
			}
			showProgress = false;

			break;
		}
		case AppItemState.STATE_UNINSTALLING: {
			//imageicon.setVisibility(View.GONE);
			safeMoreGameSetTextshare(context, stateActionView, R.drawable.beston_btn_down_updateingrawable,
					R.string.state_uninstalling_action_text,
					false || !updateStateTextBg,sharelayout,0);
			showProgress = false;

			break;
		}
		case AppItemState.STATE_NEED_UPDATE: {
			//imageicon.setVisibility(View.GONE);
			safeMoreGameSetTextshare(context, stateActionView,
					R.drawable.beston_btn_down_opendrawable,
					R.string.state_update_action_text,
					false || !updateStateTextBg,sharelayout,0);
			showProgress = false;

			break;
		}
		case AppItemState.STATE_INSTALLED_NEW: {
			//imageicon.setVisibility(View.GONE);
			safeMoreGameSetTextshare(context, stateActionView,
					R.drawable.beston_btn_down_opendrawable,
					R.string.state_installed_new_action_text,
					true || !updateStateTextBg,sharelayout,0);
			showProgress = false;

			break;
		}
		case AppItemState.STATE_IDLE: {
			{
				if (apkFileExist(appItem)) {
					//imageicon.setVisibility(View.GONE);
					safeMoreGameSetTextshare(mContext, stateActionView,
							R.drawable.bestone_down_drawable, R.string.state_idle_action_text,
							true || !updateStateTextBg,sharelayout,0);
				} else {
					//imageicon.setVisibility(View.VISIBLE);
					safeMoreGameSetTextshare(mContext, stateActionView,
							R.drawable.bestone_down_drawable,
							R.string.sharedown,
							false || !updateStateTextBg,sharelayout,1);
				}
				showProgress = false;

				// safeMoreGameSetText(context, stateActionView,
				// R.drawable.icon_btn_download,
				// R.string.state_idle_action_text,
				// false || !updateStateTextBg);
			}
			break;
		}
		}
		if (appItem instanceof DownloadItem && showProgress) {
			final DownloadItem downloadItem = (DownloadItem) appItem;
			final long totleSize = downloadItem.dSize > 0 ? downloadItem.dSize
					: downloadItem.length;
			final int percent = totleSize == 0 ? 100
					: (int) ((downloadItem.lastDSize * 100) / totleSize);
			if (progressBar != null) {
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setProgress(percent);
				if (progressText != null) {
					progressText.setVisibility(View.VISIBLE);
					if ((appItem.state == AppItemState.STATE_PAUSE)||(appItem.state ==  AppItemState.STATE_WAIT)) {
						progressText.setVisibility(View.GONE);
					} else {
						progressText.setText(percent + "%");
					}
				}

				if (appItem.state == AppItemState.STATE_INSTALLING) {
					if (progressText != null) {
						progressText.setVisibility(View.GONE);
					}
					progressBar.setIndeterminate(true);
				} else {
					progressBar.setIndeterminate(false);
				}
					
				progressText.setTag(0);
			}

		}
	}
	private void safeMoreGameSetTextshare(Context context, TextView textView,
			int drawableResId, int strID, boolean txtcolorChange,RelativeLayout sharelayout,int choice) {
		if (textView != null) {
			/*if(choice>0)
			{
				textView.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
				int bottom = textView.getPaddingBottom();
			    int top = textView.getPaddingTop();
			    int left = textView.getPaddingLeft();	
			    int righ=context.getResources().getDimensionPixelSize(R.dimen.downbuttonsharetextnonor);
			    textView.setPadding(left, top, righ, bottom);							
			}
			else{
				textView.setGravity(Gravity.CENTER);
				int bottom = textView.getPaddingBottom();
			    int top = textView.getPaddingTop();
			    int left = textView.getPaddingLeft();			  
			    textView.setPadding(left, top, left, bottom);	
				}*/
			textView.setText(strID);
			if (drawableResId != 0) {				
				
				int bottom2 = textView.getPaddingBottom();
			    int top2 = textView.getPaddingTop();
			    int right2 = textView.getPaddingRight();
			    int left2 = textView.getPaddingLeft();
			    textView.setBackgroundResource(drawableResId); 
			  
			    textView.setPadding(left2, top2, right2, bottom2);
			}
		}
	}
	private void safeMoreGameSetTextshareline(Context context, TextView textView,
			int drawableResId, int strID, boolean txtcolorChange,LinearLayout sharelayout) {
		if (textView != null) {
			textView.setText(strID);
			if (drawableResId != 0) {				
				textView.setBackgroundResource(drawableResId); 
			}
		}
	}
	public void updateByState2share(Context context, TextView stateActionView,
			AppItem appItem, ProgressBar progressBar, TextView progressText,
			boolean showDetailProgress, boolean showInfo,ImageView shareicon,LinearLayout sharelayout) {
		updateByStateimpl2share(context, stateActionView, appItem, progressBar,
				progressText, showDetailProgress,showInfo, true, null, null,shareicon,sharelayout);
	}
	public void updateByStateimpl2share(Context context, TextView stateActionView,
			AppItem appItem, ProgressBar progressBar, TextView progressText,
			boolean showDetailProgress, boolean showInfo,
			boolean updateStateTextBg, TextView currentStateView, String which,ImageView shareicon,LinearLayout sharelayout) {
		progressText.setVisibility(View.VISIBLE);
		boolean showProgress = true;
		if (stateActionView != null) {
			//stateActionView.setMinEms(5);
		}
		if(appItem==null)
		{
			return;
		}
		switch (appItem.state) {
		case AppItemState.STATE_WAIT: {
			safeMoreGameSetTextshareline(context, stateActionView,R.drawable.beston_btn_down_waitrawable,
					R.string.state_wait_action_text,
					false || !updateStateTextBg,sharelayout);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText2(context, currentStateView, R.string.state_wait_text,
					false || !updateStateTextBg);
			progressText.setVisibility(View.GONE);
			showProgress = true;
			break;
		}
		case AppItemState.STATE_RETRY: {
			safeMoreGameSetTextshareline(context, stateActionView, R.drawable.beston_btn_down_retryawable,
					R.string.state_retry_action_text,
					false || !updateStateTextBg,sharelayout);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText2(context, currentStateView, R.string.state_retry_text,
					false || !updateStateTextBg);
			showProgress = false;
			break;
		}
		case AppItemState.STATE_ONGOING: 
			{
				safeMoreGameSetTextshareline(context, stateActionView,R.drawable.beston_btn_down_downingrawable,
					R.string.notextbutton,
					false || !updateStateTextBg,sharelayout);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			if (isInstalled(appItem.pname)) {
				safeSetText2(context, currentStateView,
						R.string.state_ongoing_need_update_text,
						false || !updateStateTextBg);
			} else {
				safeSetText2(context, currentStateView,
						R.string.state_ongoing_text,
						false || !updateStateTextBg);
			}

			break;
		}
		case AppItemState.STATE_PAUSE: {
			safeMoreGameSetTextshareline(context, stateActionView,	R.drawable.beston_btn_down_continuawable,
					R.string.state_pause_action_text,
					false || !updateStateTextBg,sharelayout);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText2(context, currentStateView, R.string.state_pause_text,
					false || !updateStateTextBg);
			progressText.setVisibility(View.GONE);
			break;
		}
		case AppItemState.STATE_DOWNLOAD_FINISH: {
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetTextshareline(context, stateActionView,R.drawable.beston_btn_down_opendrawable,
						R.string.state_finish_need_update_action_text,
						false || !updateStateTextBg,sharelayout);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText2(context, currentStateView,
						R.string.state_finish_need_update_text,
						false || !updateStateTextBg);
				showProgress = false;
			} else {
				safeMoreGameSetTextshareline(context, stateActionView,R.drawable.bestone_down_drawable,
						R.string.state_finish_action_text,
						false || !updateStateTextBg,sharelayout);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText2(context, currentStateView,
						R.string.state_finish_text, false || !updateStateTextBg);
				showProgress = false;
			}
			break;
		}
		case AppItemState.STATE_INSTALLING: {
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetTextshareline(context, stateActionView,R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_need_update_action_text,
						false || !updateStateTextBg,sharelayout);
				safeSetText2(context, currentStateView,
						R.string.state_installing_need_update_text,
						false || !updateStateTextBg);
			} else {
				safeMoreGameSetTextshareline(context, stateActionView,R.drawable.beston_btn_down_updateingrawable,
						R.string.state_installing_action_text,
						false || !updateStateTextBg,sharelayout);
				safeSetText2(context, currentStateView,
						R.string.state_installing_text,
						false || !updateStateTextBg);
			}
			break;
		}
		case AppItemState.STATE_INSTALL_FAILED: {
			if (isInstalled(appItem.pname)) {
				safeMoreGameSetTextshareline(context, stateActionView,	R.drawable.beston_btn_down_opendrawable,
						R.string.state_install_failed_need_update_action_text,
						false || !updateStateTextBg,sharelayout);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText2(context, currentStateView,
						R.string.state_install_failed_need_update_text,
						false || !updateStateTextBg);
				showProgress = false;
			} else {
				safeMoreGameSetTextshareline(context, stateActionView,R.drawable.bestone_down_drawable,
						R.string.state_install_failed_action_text,
						false || !updateStateTextBg,sharelayout);
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeSetText2(context, currentStateView,
						R.string.state_install_failed_text,
						false || !updateStateTextBg);
				showProgress = false;
			}
			break;
		}
		case AppItemState.STATE_UNINSTALLING: {
			safeMoreGameSetTextshareline(context, stateActionView,R.drawable.beston_btn_down_updateingrawable,
					R.string.state_uninstalling_action_text,
					false || !updateStateTextBg,sharelayout);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			safeSetText2(context, currentStateView,
					R.string.state_uninstalling_text,
					false || !updateStateTextBg);
			break;
		}
		case AppItemState.STATE_NEED_UPDATE: {
			safeMoreGameSetTextshareline(context, stateActionView,R.drawable.beston_btn_down_opendrawable,
					R.string.state_update_action_text,
					false || !updateStateTextBg,sharelayout);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
			}
			showProgress = false;
			break;
		}
		case AppItemState.STATE_INSTALLED_NEW: {
			safeMoreGameSetTextshareline(context, stateActionView,	R.drawable.beston_btn_down_opendrawable,
					R.string.state_installed_new_action_text,
					true || !updateStateTextBg,sharelayout);
			if (updateStateTextBg) {
				safeSetBg(stateActionView, R.drawable.main_btn_bg_orange);
			}
			showProgress = false;
			break;
		}
		case AppItemState.STATE_IDLE: {
			if (apkFileExist(appItem)) {
				safeMoreGameSetTextshareline(mContext, stateActionView,
						R.drawable.bestone_down_drawable, R.string.state_idle_action_text,
						true || !updateStateTextBg,sharelayout);
			}
			else
			{
			if (updateStateTextBg && which != null
					&& which.equals(LogModel.L_HOME_REC)) {
				safeSetBg(stateActionView, R.drawable.btn_down_rec_bg);
				safeMoreGameSetTextshareline(context, stateActionView,R.drawable.bestone_down_drawable,
						R.string.sharedowndetail,
						true || !updateStateTextBg,sharelayout);
			} else {
				if (updateStateTextBg) {
					safeSetBg(stateActionView, R.drawable.main_btn_bg_white);
				}
				safeMoreGameSetTextshareline(context, stateActionView,R.drawable.bestone_down_drawable,
						R.string.sharedowndetail,
						false || !updateStateTextBg,sharelayout);
			}
			}
			showProgress = false;
			break;
		}
		}
		
	}
	private long unzip(String mInput,String mOutput,String obb,String parentfile,int sid,boolean mReplaceAll){
		long extractedSize = 0L;
		Enumeration<ZipEntry> entries;
		String copynameobb="";
		String copynametmp="";
		ZipFile zip = null;
		try {
			zip = new ZipFile(mInput);
			long uncompressedSize = getOriginalSize(zip);
			entries = (Enumeration<ZipEntry>) zip.entries();
			while(entries.hasMoreElements()){
				ZipEntry entry = entries.nextElement();
				if(entry.isDirectory()){
					continue;
				}
				String copyname=entry.getName();
				if(copyname.indexOf(".apk")>-1)
				{
					copynametmp=copyname;
				}else if(copyname.indexOf(".obb")>-1){
					copynameobb=copyname;
				}
				File destination = new File(mOutput, entry.getName());
				if(!destination.getParentFile().exists()){
					destination.getParentFile().mkdirs();
				}
				if(destination.exists()&&mContext!=null&&!mReplaceAll){
				}
				ProgressReportingOutputStream outStream = new ProgressReportingOutputStream(destination);
				extractedSize+=copy(zip.getInputStream(entry),outStream);
				outStream.close();
			}
			try {
				String path=Environment.getExternalStorageDirectory().getAbsolutePath().toString();	
				File file=new File(path+"/Android/obb/"+obb);
				if(!file.isDirectory())
				{
					file.mkdirs();
				}
				String copyname=mOutput+"/"+copynametmp;
				String tocopyname=mOutput+"/"+sid+".temp";
				File filetmp=new File(copyname);
				File filenew=new File(tocopyname);
				filetmp.renameTo(filenew);
				copyFile(tocopyname,parentfile+"/"+sid+".temp" ,true);
				File filedele =new File(tocopyname);
				file.delete();
				/*delete(filedele);*/
				File filedeleapk=new File(mInput);
				filedeleapk.delete();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				zip.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return extractedSize;
	}
	private long getOriginalSize(ZipFile file){
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();
		long originalSize = 0l;
		while(entries.hasMoreElements()){
			ZipEntry entry = entries.nextElement();
			if(entry.getSize()>=0){
				originalSize+=entry.getSize();
			}
		}
		return originalSize;
	}
	private int copy(InputStream input, OutputStream output){
		byte[] buffer = new byte[1024*8];
		BufferedInputStream in = new BufferedInputStream(input, 1024*8);
		BufferedOutputStream out  = new BufferedOutputStream(output, 1024*8);
		int count =0,n=0;
		try {
			while((n=in.read(buffer, 0, 1024*8))!=-1){
				out.write(buffer, 0, n);
				count+=n;
			}
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return count;
	}
	private final class ProgressReportingOutputStream extends FileOutputStream{
		public ProgressReportingOutputStream(File file)
				throws FileNotFoundException {
			super(file);
			// TODO Auto-generated constructor stub
		}
		@Override
		public void write(byte[] buffer, int byteOffset, int byteCount)
				throws IOException {
			// TODO Auto-generated method stub
			super.write(buffer, byteOffset, byteCount);
		}
	}
	public  boolean copyFile(String srcFileName, String destFileName, boolean reWrite) 
			throws IOException {
		File srcFile = new File(srcFileName);
		File destFile = new File(destFileName);        
		if(!srcFile.exists()) {
			return false;
		}
		if(!srcFile.isFile()) {
			return false;
		}
		if(!srcFile.canRead()) {
			return false;
		}
		if(destFile.exists() && reWrite){
			destFile.delete();
		}

		try {
			InputStream inStream = new FileInputStream(srcFile);
			FileOutputStream outStream = new FileOutputStream(destFile);
			byte[] buf = new byte[1024];
			int byteRead = 0;
			while ((byteRead = inStream.read(buf)) != -1) {
				outStream.write(buf, 0, byteRead);
			}
			outStream.flush();
			outStream.close();
			inStream.close();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
	public static void delete(File file) {  
		if (file.isFile()) {  
			file.delete();  
			return;  
		}  

		if(file.isDirectory()){  
			File[] childFiles = file.listFiles();  
			if (childFiles == null || childFiles.length == 0) {  
				file.delete();  
				return;  
			}  

			for (int i = 0; i < childFiles.length; i++) {  
				delete(childFiles[i]);  
			}  
			file.delete();  
		}  
	} 
	/*bestone add end
	 * 
	 * 
	 * */
}
