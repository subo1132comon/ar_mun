package com.byt.market.download;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

import com.byt.market.MyApplication;
import com.byt.market.data.AppItem;
import com.byt.market.data.DownloadItem;
import com.byt.market.download.DownloadContent.DownloadTask;



public class DownloadUtils {

	
	public static String getDownloadDirPath(int type) {

		return MyApplication.DATA_DIR;
	}

	public static String getFileDownloadPath(DownloadItem downloadItem){
		return getDownloadDirPath(DownloadTaskManager.FILE_TYPE_APK) + File.separator + DownloadUtils.getFileNameFromDownloadTask(downloadItem);
	}
	
	public static String getTempFileDownloadPath(DownloadItem downloadItem){
		return getDownloadDirPath(DownloadTaskManager.FILE_TYPE_APK) + File.separator + DownloadUtils.getFileNameFromDownloadTask(downloadItem)+DownloadTaskManager.DOWNLOADING_FILE_EXT;
	}
	
	public static String getFileDownloadPath(int sid){
		return getFileDownloadPath(DownloadTaskManager.getInstance().getDownloadTask(com.byt.market.MyApplication.getInstance(), String.valueOf(sid), null).mDownloadItem);
	}
	
	public static boolean hasDownloadFile(AppItem item){
		DownloadItem downloadItem = null;
		if(item instanceof DownloadItem){
			downloadItem = (DownloadItem)item;
		} else {
			downloadItem = new DownloadItem();
			downloadItem.fill(item);
		}
		File file = new File(getFileDownloadPath(downloadItem));
		if(file.exists()){
			return true;
		}
		file = new File(getTempFileDownloadPath(downloadItem));
		if(file.exists()){
			return true;
		}
		return false;
	}
	/**
	 * 根据下载任务获取文件名称
	 * 
	 * @param downloadTask
	 * @return
	 */
	private static String getFileNameFromDownloadTask(DownloadItem downloadItem) {
		try {
			return String.valueOf(downloadItem.sid) ;/*+ "_" + downloadItem.vcode
					+ ".apk"*/
		} catch (Exception e) {
			DownloadLog.error(DownloadUtils.class, "getFileNameFromDownloadTask", e);
			return null;
		}
	}
	
	
	/**
	 * 获取sdcard可用空间大小
	 * 
	 * @return
	 */
	public static long getSDCardAvailableSpace() {
		StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().toString());
		// 获取block的SIZE
		long blocSize = statFs.getBlockSize();
		// 己使用的Block的数量
		long availaBlock = statFs.getAvailableBlocks();
		return (availaBlock * blocSize);
	}
}
