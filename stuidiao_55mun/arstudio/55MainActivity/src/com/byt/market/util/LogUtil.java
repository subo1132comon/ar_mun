package com.byt.market.util;

import android.util.Log;

import com.byt.market.Constants;

/**
 * LOG帮助类
 * 
 * @author qiuximing
 * 
 */
public final class LogUtil {
	/**
	 * 是否输出LOG信息
	 */
	private static final boolean ISLOG = Constants.ISLOG;

	/**
	 * 输出信息——1：普通级别
	 * 
	 * @param msg
	 *            信息
	 */
	public static void v(String tag, String msg) {
		if (ISLOG)
			Log.v(tag, msg);
	}

	/**
	 * 输出信息——2：调试级别
	 * 
	 * @param msg
	 *            信息
	 */
	public static void d(String tag, String msg) {
		if (ISLOG)
			Log.d(tag, msg);
	}

	/**
	 * 输出信息——3：消息级别
	 * 
	 * @param msg
	 *            信息
	 */
	public static void i(String tag, String msg) {
		if (ISLOG)
			Log.i(tag, msg);
	}

	/**
	 * 输出信息——4：警告级别
	 * 
	 * @param msg
	 *            信息
	 */
	public static void w(String tag, String msg) {
		if (ISLOG)
			Log.w(tag, msg);
	}

	/**
	 * 输出信息——5：异常级别
	 * 
	 * @param msg
	 *            信息
	 */
	public static void e(String tag, String msg) {
		if (ISLOG)
			Log.e(tag, msg);
	}

	/**
	 * 输出信息——1：普通级别
	 * 
	 * @param event
	 *            事件标识
	 * @param msg
	 *            信息
	 */
	public static void v(String tag, String event, String msg) {
		if (ISLOG)
			Log.v(tag, "[ " + event + " ] " + msg);
	}

	/**
	 * 输出信息——2：调试级别
	 * 
	 * @param event
	 *            事件标识
	 * @param msg
	 *            信息
	 */
	public static void d(String tag, String event, String msg) {
		if (ISLOG)
			Log.d(tag, "[ " + event + " ] " + msg);
	}

	/**
	 * 输出信息——3：消息级别
	 * 
	 * @param event
	 *            事件标识
	 * @param msg
	 *            信息
	 */
	public static void i(String tag, String event, String msg) {
		if (ISLOG)
			Log.i(tag, "[ " + event + " ] " + msg);
	}

	/**
	 * 输出信息——4：警告级别
	 * 
	 * @param event
	 *            事件标识
	 * @param msg
	 *            信息
	 */
	public static void w(String tag, String event, String msg) {
		if (ISLOG)
			Log.w(tag, "[ " + event + " ] " + msg);
	}

	/**
	 * 输出信息——5：异常级别
	 * 
	 * @param event
	 *            事件标识
	 * @param msg
	 *            信息
	 */
	public static void e(String tag, String event, String msg) {
		if (ISLOG)
			Log.e(tag, "[ " + event + " ] " + msg);
	}
}
