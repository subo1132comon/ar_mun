/**
 * @author jiangxiaoliang
 * 文件下载器（只负责下载  不负责逻辑组织）
 */

package com.byt.market.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import com.byt.market.download.DownloadContent.DownloadTask;

public class Downloader {
	private static final int DEFAULT_CHUNK_SIZE = 4096;
	private static final int MAX_RETRY = 1;
	private static final int DEFAULT_REFRESH_RATE = 1000;

	private DownloadListener listener = null;

	private DownloadHttp mHttpApi;
	private HttpRequestBase mHttpRequest;
	private DownloadTask mDownloadTask;

	private long totalSize;
	
	/**
	 * This is the flag used to indicate that the current download shall be
	 * cancelled
	 */
	private boolean mcancel = false;
	private boolean mRequest = false;

	public Downloader(DownloadHttp httpApi, HttpRequestBase httpRequest) {
		this(null, httpApi, httpRequest);
	}
	
	public Downloader(DownloadTask downloadTask, DownloadHttp httpApi, HttpRequestBase httpRequest) {
		this(downloadTask, httpApi, httpRequest, null);
	}

	public Downloader(DownloadTask downloadTask, DownloadHttp httpApi, HttpRequestBase httpRequest, DownloadListener listener) {
		mHttpApi = httpApi;
		mHttpRequest = httpRequest;
		mDownloadTask = downloadTask;
		this.listener = listener;
	}

	public void setDownloadListener(DownloadListener listener) {
		this.listener = listener;
	}

	/**
	 *该方法 有自动重试功能 MAX_RETRY
	 * @throws Exception 
	 */
	public long download(OutputStream os) throws Exception {
		boolean retry;
		int i = 0;
		long downloadedSize = 0;

		do {
			++i;
			retry = false;
			try {
				if (downloadedSize == 0) {
					// Perform a full download
					downloadedSize = download(os, -1);
				} else {
					// Try to resume previous download
					DownloadLog.debug(Downloader.class, "Resuming download at " + downloadedSize);
					downloadedSize = download(os, downloadedSize);
				}
			} catch (DownloadException de) {
				// If we had a network error, then we try to resume the download
				// (until we reach a max number of errors)
				if (i < MAX_RETRY) {
					retry = true;
					downloadedSize = de.getPartialLength();
				} else {
					throw de;
				}
			}
		} while (retry);
		return downloadedSize;
	}

	/**
	 * Resumes the download of an interrupted item
	 * @throws Exception 
	 */
	public long resume(OutputStream os, long startOffset) throws Exception {
		return download(os, startOffset);
	}

	/**
	 * Cancels the current download
	 */
	public void cancel() {
		DownloadLog.debug(Downloader.class, "Cancelling current download");
		mcancel = true;
		if (mRequest) {
			mHttpRequest.abort();
		}
	}

	private boolean isDownloadCancelled() {
		return mcancel;
	}

	public long download(OutputStream os, long startOffset) throws Exception {
		mcancel = false;
		long downloadedSize = startOffset > 0 ? startOffset : 0;
		InputStream is = null;
		boolean errorWritingStream = false;
		boolean resume = false;
		
		try {
			if (startOffset > 0) {
				// This is a breakpoint download setting. Add the request proper
				mHttpRequest.setHeader("Range", "bytes=" + startOffset + "-");
				resume = true;
			}
			mRequest = true;
			HttpResponse httpResponse = mHttpApi.executeHttpRequest(mHttpRequest);
			int respCode = httpResponse.getStatusLine().getStatusCode();

			DownloadLog.debug(Downloader.class, "Response is: " + respCode);
			HttpEntity entity = httpResponse.getEntity();
			totalSize = entity.getContentLength() + (startOffset > 0 ? startOffset : 0);

			if (listener != null) {
				listener.downloadStarted(mDownloadTask, totalSize);
			}

			boolean ok;
			if (resume) {
                if (respCode == HttpURLConnection.HTTP_PARTIAL) {
                    ok = true;
                } else {
                	DownloadLog.debug(Downloader.class, "Server refused resuming download");
                    throw new Exception("Cannot resume download");
                }
            } else {
                ok = respCode == HttpURLConnection.HTTP_OK;
            }
			if (ok) {
				is = new InputStreamWrapper(entity.getContent(), entity);
				byte[] data = new byte[DEFAULT_CHUNK_SIZE];
				int n = 0;
				long lastListened = 0;
				int percent = 0;
				int percentNow = 0;
				while ((n = is.read(data)) != -1 && !isDownloadCancelled()) {
					try {
						os.write(data, 0, n);
					} catch (IOException ioe) {
						DownloadLog.error(Downloader.class, "Cannot write output stream", ioe);
						errorWritingStream = true;
						break;
					}
					// 下载了多少字节 保存在本地
					downloadedSize += n;
					long now = System.currentTimeMillis();
					percentNow = (int)(totalSize == 0 ? 0 : downloadedSize * 100 / totalSize);
					if (listener != null && (downloadedSize == totalSize || (now - lastListened > DEFAULT_REFRESH_RATE && percentNow != percent))) {
						lastListened = now;
						percent = percentNow;
						listener.downloadProgress(mDownloadTask, downloadedSize, totalSize);
					}
				}
			} else {
				DownloadLog.error(Downloader.class, "Http request failed. Server replied: " + respCode);
				throw new DownloadException("HTTP error code: " + respCode, 0, DownloadException.CODE_HTTP, respCode);
			}
		} catch (IOException ex) {
			DownloadLog.error(Downloader.class, "Http download failed with a io error " + downloadedSize, ex);
			throw new DownloadException(DownloadException.CODE_IO, ex.getMessage());
		} finally {
			mRequest = false;
			// Release all resources
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
				os = null;
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
				is = null;
			}
		}

		if (isDownloadCancelled()) {
			throw new DownloadException("Download cancelled", downloadedSize, DownloadException.CODE_CANCELLED);
		}

		if (errorWritingStream) {
			throw new DownloadException("Cannot write output stream", DownloadException.CODE_IO);
		}

		if (listener != null) {
			listener.downloadEnded(mDownloadTask);
		}

		return downloadedSize;
	}

	/**
	 *获取下载内容的总大小
	 * @return
	 */
	public long getTotalSize() {
		return totalSize;
	}
	
	private class InputStreamWrapper extends InputStream {

		private InputStream is;
		private HttpEntity entity;

		public InputStreamWrapper(InputStream is, HttpEntity entity) {
			this.is = is;
			this.entity = entity;
		}

		public int available() throws IOException {
			return is.available();
		}

		public void close() throws IOException {
			is.close();
			entity.consumeContent();
		}

		public void mark(int limit) {
			is.mark(limit);
		}

		public boolean markSupported() {
			return is.markSupported();
		}

		public int read() throws IOException {
			return is.read();
		}

		public int read(byte buf[]) throws IOException {
			return is.read(buf);
		}

		public int read(byte buf[], int off, int len) throws IOException {
			return is.read(buf, off, len);
		}

		public void reset() throws IOException {
			is.reset();
		}

		public long skip(long n) throws IOException {
			return is.skip(n);
		}
	}

	/**
	 * Used to monitor a download operation
	 */
	public interface DownloadListener {

		/**
		 * Reports that a new download operation started.
		 * @param totalSize the total size of bytes to download
		 */
		public void downloadStarted(DownloadTask downloadTask, long totalSize);

		/**
		 * Reports the progress of a download operation.
		 * @param progressSize the total number of bytes received from the beginning
		 * @param totalSize the total size of bytes to download
		 */
		public void downloadProgress(DownloadTask downloadTask, long progressSize, long totalSize);

		/**
		 * Reports that a download operation ended.
		 */
		public void downloadEnded(DownloadTask downloadTask);

	}
}
