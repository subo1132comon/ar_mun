package com.byt.market.qiushibaike.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class DownloadInfo {
	public static final int DOWNLOAD_SUCCESS = 1;
	public static final int DOWNLOAD_FAINL = 2;
	public static final int DOWNLOAD_FILE_NOT_FOURND = 3;
	public static final int DOWNLOAD_COMPLETE = 4;

	public static final String DOWNLOAD_URL_KEY = "download_url_key";
	public static final String ITEM_SAVE_PATH_DIR_KEY = "item_save_path_dir_key";
	public static final String ITEM_ALL_SIZE_KEY = "item_all_size_key";
	private Context context;
	public int downloadState = 0;
	public String savePath = null;
	private String downloadUrl;
	public long allSize;
	public long curSize;
	public boolean isStop=false;
	public DownloadInfo(Context context, String downloadUrl, String savePath) {
		this.context = context;
		this.savePath = savePath;
		this.downloadUrl = downloadUrl;
	}

	public void downloadFile() {
		File file = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		RandomAccessFile fos = null;
		try {
			file = new File(savePath);
			Log.i("xj", "file is==>" + file.exists()+" downloadUrl==>"+downloadUrl);
			URL url = new URL(downloadUrl);
			conn = (HttpURLConnection) url.openConnection();
			allSize=conn.getContentLength();
		} catch (MalformedURLException e1) {
			downloadState = DOWNLOAD_FILE_NOT_FOURND;
			e1.printStackTrace();
		} catch (Exception e1) {
			downloadState = DOWNLOAD_FILE_NOT_FOURND;
			e1.printStackTrace();
		}
		try {
			fos = new RandomAccessFile(savePath, "rw");
			if (file.exists()) {
				conn.setRequestMethod("GET");
				String start = "bytes=" + file.length() + "-";
				conn.setRequestProperty("RANGE", start);
				fos.seek(file.length());
			}
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (ProtocolException e2) {
			downloadState = DOWNLOAD_FILE_NOT_FOURND;
			e2.printStackTrace();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		try {
			is = conn.getInputStream();
			conn.connect();
		} catch (Exception e1) {
			downloadState = DOWNLOAD_FILE_NOT_FOURND;
			e1.printStackTrace();
		}
		try {
			try {
				byte[] buf = new byte[256];
				double count = 0;
				if (conn.getResponseCode() < 400) {
//					Log.i("xj", "count==>" + count);
					while (count <= 100) {
//						Log.i("xj", "count111==>" + count);
						if (is != null) {
							int numRead = is.read(buf);
//							Log.i("xj", "numRead==>" + numRead);
							if (numRead <= 0) {
								break;
							} else {
								fos.write(buf, 0, numRead);
							}
						} else {
							break;
						}
						if (file != null) {
							curSize = file.length();
						}
						if(isStop){
							break;
						}
					}
					downloadState = DOWNLOAD_SUCCESS;
				} else {
					downloadState = DOWNLOAD_FAINL;
				}
				conn.disconnect();
				fos.close();
				is.close();
			} catch (Exception e) {
				downloadState = DOWNLOAD_FAINL;
				e.printStackTrace();
			}
		} catch (Exception e) {
			downloadState = DOWNLOAD_FAINL;
			e.printStackTrace();
		}
		if (downloadState != DOWNLOAD_SUCCESS) {
			deleDownloadFailFile();
		}
		deleSaveNativeInfo();
	}

	public void deleDownloadFailFile() {
		try {
			File file = new File(savePath);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleSaveNativeInfo() {
		try {
			SharedPreferences sharedata = context.getSharedPreferences(
					"downloadData", 0);
			Editor editor = sharedata.edit();
			editor.remove(new File(savePath).getName());
			editor.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
