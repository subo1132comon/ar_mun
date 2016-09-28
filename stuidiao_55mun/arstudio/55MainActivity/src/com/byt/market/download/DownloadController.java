/**
 * @author jiangxiaoliang
 * used to network communicate
 */

package com.byt.market.download;

import com.byt.market.download.Downloader.DownloadListener;



public class DownloadController {
	private static DownloadController mInstance;
	private DownloadHttp mYoyoHttp;
	
	
	private DownloadController() {
		mYoyoHttp = new DownloadHttp();
	}
	
	public static DownloadController getInstance() {
		if(mInstance == null) {
			mInstance = new DownloadController();
		}
		return mInstance;
	}
	
	
    
	public Downloader downloadFile(DownloadContent.DownloadTask task, String url, DownloadListener listener){		
		DownloadLog.debug(DownloadController.class, "downloadFile downloadUrl = " + url);
		return new Downloader(task, mYoyoHttp, mYoyoHttp.createHttpGet(url), listener);		
	}

}
