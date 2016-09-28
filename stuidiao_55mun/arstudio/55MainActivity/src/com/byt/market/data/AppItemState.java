package com.byt.market.data;

public interface AppItemState {
    //public final static int STATE_IDLE_NO_APK_FILE  = -2;	//未安装，本地无APK，或APK已被删除
	public final static int STATE_IDLE  = -1;	//未安装
	public final static int STATE_WAIT    = 0;	//等待
	public final static int STATE_ONGOING = 1;	//正在下载
	public final static int STATE_PAUSE   = 2;	//暂停、失败
	public final static int STATE_RETRY   = 3;	//重试
	public final static int STATE_DOWNLOAD_FINISH  = 4;	//下载完成
	public final static int STATE_INSTALLING = 5;  //正在安装
	public final static int STATE_INSTALL_FAILED = 6;  //安装失败
	public final static int STATE_UNINSTALLING = 7;  //卸载中
	
	public final static int STATE_NEED_UPDATE  = 11;	//已安装，需要更新
	public final static int STATE_INSTALLED_NEW  = 12;	//已安装最新版
	
	
}
