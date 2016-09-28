package com.byt.market.net;

import android.content.Context;

import com.byt.market.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UrlHttpConnection {
	private final static String TAG = "HttpUrlConnection";

	public final static int OPEN = 1;
	public final static int SEND = 2;
	public final static int RECEIVE = 3;
	public final static int OK = 4;
	public final static int ERROR = 5;
	public final static int CANCEL = 6;

	/**
	 * 没有网络、网络类型为空
	 */
	public final static int NO_NETWORK = 7;
	public final static int JUMP = 8;

	private int mState;
	private Context mContext;

	/**
	 * 是否用 代理
	 */
	private boolean isPr = false;

	/**
	 * 是否尝试 过
	 */
	private boolean istrycon;
	HttpURLConnection httpCon;
	OutputStream os;
	boolean retry = true;

	public UrlHttpConnection(Context context) {
		this.mContext = context;
		if ((NetworkUtil.getNetworkType().indexOf("wap") != -1)) {
			isPr = true;
			if (NetworkUtil.getNetworkType().indexOf("uniwap") != -1) {
				isPr = false;
			}
		}
	}

	/**
	 * 取消连接
	 */
	public void cancel() {
		mState = CANCEL;
	}

	/**
	 * 连接是否已经取消
	 * 
	 * @return
	 */
	public boolean isCanceled() {
		return mState == CANCEL;
	}

	/**
	 * 建立连接
	 * 
	 * @param url
	 *            连接地址
	 * @param body
	 *            post内容
	 * @return
	 */
	public Result connect(String url, byte[] body, HashMap<String, String> map) {
		if (!NetworkUtil.isNetWorking(mContext)) {
			mState = NO_NETWORK;
		}
		if (NetworkUtil.getNetworkType().equals("")) {
			mState = NO_NETWORK;
			return new Result(mState, null);
		}
		if (mState == CANCEL) {
			return new Result(mState, null);
		}
		try {
			byte[] receiveData = null;
			mState = OPEN;
			requireConnection(url, map);
			if (mState == CANCEL) {
				return new Result(mState, null);
			}
			mState = SEND;
			sendBody(body);
			if (mState == CANCEL) {
				return new Result(mState, null);
			}
			mState = RECEIVE;
			receiveData = receiveBodyAsByte();
			if (mState == CANCEL) {
				return new Result(mState, null);
			}
			if (receiveData == null) {
				if (retry) {
					retry = false;
					return connect(url, body, map);
				}
			}
			mState = OK;
			return new Result(mState, receiveData);
		} catch (Exception e) {
			e.printStackTrace();
			if (istrycon) {
				mState = ERROR;
				return new Result(mState, null);
			} else {
				isPr = !isPr;
				istrycon = true;
				retry = true;
				return connect(url, body, map);
			}
		}
	}

	/**
	 * 建立连接
	 * 
	 * @param url
	 *            连接地址
	 * @param body
	 *            post内容
	 * @return
	 */
	public Result connect(String url, HashMap<String, String> map) {
		if (!NetworkUtil.isNetWorking(mContext)) {
			mState = NO_NETWORK;
		}
		if (NetworkUtil.getNetworkType().equals("")) {
			mState = NO_NETWORK;
			return new Result(mState, null);
		}
		if (mState == CANCEL) {
			return new Result(mState, null);
		}
		try {
			byte[] receiveData = null;
			mState = OPEN;
			requireConnection(url, map);
			if (mState == CANCEL) {
				return new Result(mState, null);
			}
			mState = SEND;
			if (mState == CANCEL) {
				return new Result(mState, null);
			}
			mState = RECEIVE;
			receiveData = receiveBodyAsByte();
			if (mState == CANCEL) {
				return new Result(mState, null);
			}
			if (receiveData == null) {
				if (retry) {
					retry = false;
					return connect(url, map);
				}
			}
			mState = OK;
			return new Result(mState, receiveData);
		} catch (Exception e) {
			e.printStackTrace();
			if (istrycon) {
				mState = ERROR;
				return new Result(mState, null);
			} else {
				isPr = !isPr;
				istrycon = true;
				retry = true;
				return connect(url, map);
			}
		}
	}

	private void requireConnection(String url, HashMap<String, String> map)
			throws Exception {
		httpCon = getHttpURLConnection(url);
		setRequestHead(httpCon, map);
	}

	private HttpURLConnection getHttpURLConnection(String url)
			throws MalformedURLException, IOException {
		LogUtil.d(TAG, "getHttpURLConnection", "use pr:" + isPr + " url:" + url);
		HttpURLConnection http;
		if (isPr) {
			http = (HttpURLConnection) new URL(url).openConnection(getProxy());
		} else {
			http = (HttpURLConnection) new URL(url).openConnection();
		}
		return http;
	}

	private Proxy getProxy() {
		String defaultHost = android.net.Proxy.getDefaultHost();
		int defaultPort = android.net.Proxy.getDefaultPort();
		Proxy proxy;
		if (defaultHost != null && defaultPort != -1) {
			LogUtil.d(TAG, "getProxy", "Host:" + defaultHost + ",Port:"
					+ defaultPort);
			proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(
					defaultHost, defaultPort));
		} else {
			proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(
					"10.0.0.172", 80));
			LogUtil.d(TAG, "getProxy", "Host:10.0.0.172,Port:80");
		}
		return proxy;
	}

	private void sendBody(byte[] body) throws Exception {
		LogUtil.d(TAG, "sendBody", "body length:" + body.length);
		OutputStream os = httpCon.getOutputStream();
		ByteArrayInputStream bis = new ByteArrayInputStream(body);
		byte[] buf = new byte[2048];
		int len = 0;
		while ((len = bis.read(buf)) != -1) {
			os.write(buf, 0, len);
			os.flush();
		}
		os.close();
	}

	private byte[] receiveBodyAsByte() throws IOException {
		LogUtil.d(TAG, "receiveBodyAsByte", "");
		httpCon.connect();
		String type = httpCon.getContentType();
		if (isWMLPage(type)) {
			LogUtil.d(TAG, "receiveBodyAsByte", "isWMLPage");
			return null;
		}
		int state = httpCon.getResponseCode();
		LogUtil.d(TAG, "receiveBodyAsByte", "getResponseCode:" + state);
		if (state == 302) {
			mState = JUMP;
			return null;
		}
		if (state != 200) {
			mState = ERROR;
			return null;
		}
		InputStream is = httpCon.getInputStream();
		int length = httpCon.getContentLength();
		LogUtil.d(TAG, "receiveBodyAsByte", "getContentLength:" + length
				+ " httpCon:" + httpCon.getContentEncoding());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] receiveBuffer = new byte[1024];
		int n = 0;
		while (mState != CANCEL) {
			n = is.read(receiveBuffer);
			if (n == -1) {
				break;
			}
			bos.write(receiveBuffer, 0, n);
		}
		return bos.toByteArray();
	}

	private void setRequestHead(HttpURLConnection httpCon,
			HashMap<String, String> map) throws ProtocolException {
		LogUtil.d(TAG, "setRequestHead", httpCon.hashCode() + "");
		httpCon.setDoOutput(true);
		httpCon.setDoInput(true);
		httpCon.setUseCaches(false);
		httpCon.setConnectTimeout(15000);
		httpCon.setRequestMethod("POST");
		// httpCon.setRequestProperty("Connection", "Kepp-Alive");
		httpCon.setRequestProperty("Content-Type", "text/xml");
		if (map != null && map.size() > 0) {
			if (map.containsKey("imei"))
				httpCon.setRequestProperty("imei", map.get("imei"));
			if (map.containsKey("model"))
				httpCon.setRequestProperty("model", map.get("model"));
			if (map.containsKey("net"))
				httpCon.setRequestProperty("net", map.get("net"));
			if (map.containsKey("api"))
				httpCon.setRequestProperty("api", map.get("api"));
			if (map.containsKey("type"))
				httpCon.setRequestProperty("type", map.get("type"));
			if (map.containsKey("vcode"))
				httpCon.setRequestProperty("vcode", map.get("vcode"));
			if (map.containsKey("reso"))
				httpCon.setRequestProperty("reso", map.get("reso"));
			if (map.containsKey("channel"))
				httpCon.setRequestProperty("channel", map.get("channel"));
			 if (map.containsKey("uid"))
			 httpCon.setRequestProperty("uid", map.get("uid"));
			 if (map.containsKey("lang"))
			 httpCon.setRequestProperty("lang", map.get("lang"));
			 if (map.containsKey("mcc"))
				 httpCon.setRequestProperty("mcc", map.get("mcc"));
			 if (map.containsKey("mnc"))
				 httpCon.setRequestProperty("mnc", map.get("mnc"));
			 if (map.containsKey("jpushid"))
				 httpCon.setRequestProperty("jpushid", map.get("jpushid"));
			 if(map.containsKey("imsi"))
				 httpCon.setRequestProperty("imsi", map.get("imsi"));
		}
	}

	/**
	 * 获取小文件,图片
	 * 
	 * @param url
	 * @return Object数组 网络状态和数据
	 */
	public Result downloadIcon(String url) {
		if (!NetworkUtil.isNetWorking(mContext)) {
			mState = NO_NETWORK;
		}
		if (NetworkUtil.getNetworkType().equals("")) {
			mState = NO_NETWORK;
			return new Result(mState, null);
		}
		if (mState == CANCEL) {
			return new Result(mState, null);
		}
		try {
			byte[] receiveData = null;
			mState = OPEN;
			httpCon = getHttpURLConnection(url);
			httpCon.setDoInput(true);
			httpCon.setRequestProperty("Connection", "Kepp-Alive");
			httpCon.setConnectTimeout(15000);
			if (mState == CANCEL) {
				return new Result(mState, null);
			}
			mState = RECEIVE;
			receiveData = receiveBodyAsByte();
			if (mState == CANCEL) {
				return new Result(mState, null);
			}
			if (mState == JUMP) {
				url = httpCon.getHeaderField("Location");
				if (url == null) {
					mState = ERROR;
					return new Result(mState, null);
				}
				return downloadIcon(url);
			}
			if (receiveData == null) {
				if (retry) {
					retry = false;
					return downloadIcon(url);
				}
				return new Result(mState, null);
			}
			mState = OK;
			return new Result(mState, receiveData);
		} catch (Exception e) {
			e.printStackTrace();
			if (istrycon) {
				mState = ERROR;
				return new Result(mState, null);
			} else {
				isPr = !isPr;
				istrycon = true;
				retry = true;
				return downloadIcon(url);
			}
		}
	}

	// /**
	// * 联网获取数据
	// *
	// * @param data
	// * 下载的文件
	// * @return InputStream 字节流
	// */
	// public InputStream getAppInputStream(DownloadApp data) {
	// if (data == null) {
	// return null;
	// }
	// InputStream is = null;
	// String url = data.appUrl;
	// if (URLUtil.isNetworkUrl(url)) {
	// try {
	// httpCon = getHttpURLConnection(url);
	// httpCon.setDoInput(true);
	// httpCon.setConnectTimeout(30000);
	// httpCon.setRequestProperty("RANGE", "bytes=" + data.downloadedSize +
	// "-");
	//
	// int responseCode = httpCon.getResponseCode();
	// System.out.println("responseCode==="+responseCode);
	// if (responseCode == 302) {
	// url = httpCon.getHeaderField("Location");
	// if (url == null) {
	// return null;
	// }
	// data.appUrl = url;
	// return getAppInputStream(data);
	// }
	// if (responseCode != 200 && responseCode != 206) {
	// if (retry) {
	// retry = false;
	// return getAppInputStream(data);
	// }
	// } else {
	// if (NetworkUtil.isNetWorking(mContext) &&
	// !isWMLPage(NetworkUtil.getNetworkType())) {
	// // int filesize = httpCon.getContentLength();
	// if (data.downloadedSize == 0) {
	// data.appLength = getTotalLength(httpCon);
	// }
	// httpCon.connect();
	// is = httpCon.getInputStream();
	// }
	// }
	// } catch (Exception e) {
	// if (istrycon) {
	// e.printStackTrace();
	// } else {
	// isPr = !isPr;
	// istrycon = true;
	// retry = true;
	// return getAppInputStream(data);
	// }
	// }
	// }
	// return is;
	// }
	//
	// public InputStream getAppInputStream(DownloadApp data, int
	// needDownloadSize) {
	// if (data == null) {
	// return null;
	// }
	// InputStream is = null;
	// String url = data.appUrl;
	// if (URLUtil.isNetworkUrl(url)) {
	// try {
	// httpCon = getHttpURLConnection(url);
	// httpCon.setDoInput(true);
	// httpCon.setConnectTimeout(30000);
	// httpCon.setRequestProperty("RANGE", "bytes=" + data.downloadedSize + "-"
	// + (data.downloadedSize + needDownloadSize));
	//
	// int responseCode = httpCon.getResponseCode();
	// if (responseCode == JUMP) {
	// url = httpCon.getHeaderField("Location");
	// if (url == null) {
	// // data.state = DownloadApp.STATE_INPUTSTREAM_NULL;
	// return null;
	// }
	// data.appUrl = url;
	// return getAppInputStream(data, needDownloadSize);
	// }
	// if (responseCode != 200 && responseCode != 206 && responseCode != 201 &&
	// responseCode != 302) {
	// if (retry) {
	// retry = false;
	// return getAppInputStream(data, needDownloadSize);
	// }
	// } else {
	// if (NetworkUtil.isNetWorking(mContext) &&
	// !isWMLPage(NetworkUtil.getNetworkType())) {
	// // int filesize = httpCon.getContentLength();
	// if (data.downloadedSize == 0) {
	// data.appLength = getTotalLength(httpCon);
	// }
	// httpCon.connect();
	// is = httpCon.getInputStream();
	// }
	// }
	// } catch (Exception e) {
	// if (istrycon) {
	// e.printStackTrace();
	// } else {
	// isPr = !isPr;
	// istrycon = true;
	// retry = true;
	// return getAppInputStream(data, needDownloadSize);
	// }
	// }
	//
	// // if (is == null){
	// // data.state = DownloadApp.STATE_INPUTSTREAM_NULL;
	// // } else {
	// // data.state = DownloadApp.STATE_INPUTSTREAM_READY;
	// // }
	// }
	//
	// return is;
	// }

	private int getTotalLength(HttpURLConnection conn) {
		int length = 0;
		String key = null;
		key = conn.getHeaderField("Content-Range");
		if (key == null) {
			Map<String, List<String>> map = conn.getHeaderFields();
			if (map != null) {
				for (int i = 0; i < map.size(); i++) {
					key = conn.getHeaderField(i);
					if (key != null && key.indexOf("bytes") != -1
							&& key.indexOf("-") != -1 && key.indexOf("/") != -1) {
						String s = key.substring(key.indexOf("/") + 1);
						length = Integer.parseInt(s);
					}
				}
			}
		} else {
			String s = key.substring(key.indexOf("/") + 1);
			length = Integer.parseInt(s);
		}
		return length;
	}

	private boolean isWMLPage(String contenType) {
		if (contenType != null
				&& contenType.toLowerCase(Locale.ENGLISH).startsWith(
						"text/vnd.wap.wml")) {
			return true;
		} else {
			return false;
		}
	}

	public static class Result {
		private int state;
		private byte[] result;

		public Result(int state, byte[] result) {
			this.state = state;
			this.result = result;
		}

		public int getState() {
			return state;
		}

		public byte[] getResult() {
			return result;
		}
	}
}
