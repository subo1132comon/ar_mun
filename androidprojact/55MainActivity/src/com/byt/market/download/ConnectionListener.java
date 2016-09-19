package com.byt.market.download;

public interface ConnectionListener {

	/** 没有任何连线 */
	public final static int CONN_NONE = -1;
	/** 来自未知网络的不明连线 */
	public final static int CONN_UNKNOWN = 0;
	/** WIFI */
	public final static int CONN_WIFI = 1;
	/** GPRS, CDMA, EDGE */
	public final static int CONN_GPRS = 2;
	/** 3G */
	public final static int CONN_3G = 3;
	
	/**
	 * 继承用
	 * @param state
	 */
	public void connectionStateChanged(int state);
	
	
	
	public static final int ACTION_PACKAGE_ADD = 1;
	public static final int ACTION_PACKAGE_REMOVED = 2;
	/**
	 * 继承用
	 * @param state
	 */
	public void onPackageChanged(String pkn , int action);
	
	
	
}
