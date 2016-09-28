/**
 * startConnecting() --> downloadStarted --> downloadProgress --> downloadEnded -->endConnecting
 * used to add listener on the DownloadTaskmanager
 */

package com.byt.market.download;

import com.byt.market.download.DownloadContent.DownloadTask;

public interface DownloadTaskListener {

	/**
	 * start of the network connecting
	 * 
	 * @param task
	 *            Download object
	 * @param total
	 *            download task
	 * @param progress
	 *            download task
	 */
	public void startConnecting(DownloadTask task, int totalTask,
			int progressTask);

	/**
	 * Reports that a new download operation started.
	 * 
	 * @param task
	 *            Download object
	 * @param total
	 *            download task
	 * @param progress
	 *            download task
	 * @param totalSize
	 *            the total size of bytes to download
	 */
	public void downloadStarted(DownloadTask task, int totalTask,
			int progressTask, long totalSize);

	/**
	 * Reports the progress of a download operation.
	 * 
	 * @param task
	 *            Download object
	 * @param total
	 *            download task
	 * @param progress
	 *            download task
	 * @param progressSize
	 *            the total number of bytes received from the beginning
	 * @param totalSize
	 *            the total size of bytes to download
	 */
	public void downloadProgress(DownloadTask task, int totalTask,
			int progressTask, long progressSize, long totalSize);

	/**
	 * Reports that a download operation ended.
	 * 
	 * @param task
	 *            Download object
	 * @param total
	 *            download task
	 * @param progress
	 *            download task
	 */
	public void downloadEnded(DownloadTask task, int totalTask, int progressTask);

	/**
	 * end of the network connecting
	 * 
	 * @param task
	 *            Download object
	 * @param total
	 *            download task
	 * @param progress
	 *            download task
	 * @param result
	 *            result != null downlaod failure
	 */
	public void endConnecting(DownloadTask task, int totalTask,
			int progressTask, DownloadException result);

	/**
	 * 所有任务队列下载完成
	 * 
	 * @param total
	 *            download task
	 * @param progress
	 *            download task
	 * @param The
	 *            success of the last download
	 */
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success);

	/**
	 * 暂停 删除 启动 任务 通知UI 刷新
	 */
	public void refreshUI();

	/**
	 * 安装完成
	 */
	public void installedSucess(DownloadTask downloadTask);

	/**
	 * 安装完成
	 */
	public void installedSucess(String packageName);

	/**
	 * 卸载完成
	 */
	public void unInstalledSucess(String packageName);

	/**
	 * 当网络状态正常时，List页面去刷新数据
	 */
	public void networkIsOk();
}