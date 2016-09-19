package com.byt.market.util;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.os.Environment;
import android.os.StatFs;

public class StorageUtil {

	private static final int ERROR = -1;
	private static NumberFormat apkSizeFormat = new DecimalFormat("0.##");
	private static final String PERCENT = "%";

	/**
	 * 是否有可用外部存储
	 */
	public static boolean externalMemoryAvailable() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取内部存储剩余大小
	 * 
	 * @return
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	public static String formatSize(double size){
		String returnSize = null;
		if(size >= 1024*1024*1024){
			returnSize = Util.apkSizeFormat((double) size
					/ (1024 * 1024 * 1024), "GB");
		}else{
			returnSize = Util.apkSizeFormat((double) size
					/ (1024 * 1024), "MB");
		}
		return returnSize;
	}
	/**
	 * 转换成百分比
	 * @param available
	 * @param total
	 * */
	public static String computePercent(long available , long total){
		return apkSizeFormat.format(available * 100 / total) + PERCENT;
	}
	
	/**
	 * 获取内部存储总大小
	 * 
	 * @return
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 获取外部存储剩余大小
	 * 
	 * @return
	 */
	public static long getAvailableExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else {
			return ERROR;
		}
	}

	/**
	 * 获取外部存储总大小
	 * 
	 * @return
	 */
	public static long getTotalExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return totalBlocks * blockSize;
		} else {
			return ERROR;
		}
	}
}