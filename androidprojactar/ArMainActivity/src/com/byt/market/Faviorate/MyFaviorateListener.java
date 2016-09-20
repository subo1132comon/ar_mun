/**
 * startConnecting() --> downloadStarted --> downloadProgress --> downloadEnded -->endConnecting
 * used to add listener on the DownloadTaskmanager
 */

package com.byt.market.Faviorate;

import com.byt.market.data.AppItem;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;

public interface MyFaviorateListener {

	/**
     * 通知游戏被收藏
	 */
	public void FaviorateAppAdded(AppItem appItem);

	/**
     * 通知游戏被取消收藏
	 */
	public void FaviorateAppDeled(AppItem appItem);


	/**
	 * 通知UI 刷新
	public void refreshUI();
     */



	/**
	 * 当网络状态正常时，List页面去刷新数据

	public void networkIsOk();
     */
}