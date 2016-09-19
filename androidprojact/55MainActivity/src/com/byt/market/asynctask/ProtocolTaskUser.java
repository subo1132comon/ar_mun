package com.byt.market.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.byt.market.Constants;
import com.byt.market.net.NetworkUtil;
import com.byt.market.net.UrlHttpConnection;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.DataUtil;
import com.byt.market.util.FileUtil;
import com.byt.market.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ProtocolTaskUser extends
		AsyncTask<Object, Void, UrlHttpConnection.Result> {
	public static final String TAG = "ProtocolTask";
	private Context mContext;
	UrlHttpConnection huc;
	TaskListenerUser mTaskListener;
	public ListViewFragment mFragment;

	public ProtocolTaskUser(Context context) {
		mContext = context;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected UrlHttpConnection.Result doInBackground(Object... params) {
		LogUtil.d(TAG, "doInBackground", "start");
		String url = (String) params[0];
		String content = null;
		byte[] userIcon = null;
		if (params[1] instanceof String) {
			content = (String) params[1];
		} else {
			userIcon = (byte[]) params[1];
		}
		String action = (String) params[2];
		HashMap<String, String> map = (HashMap<String, String>) params[3];
		int states = UrlHttpConnection.ERROR;
		if (Constants.IS_SAVE_PROTOCOL && content != null) {
			FileUtil.saveProtocelToFile(content.getBytes(), "Request_" + action);
		}
		if (url == null) {
			return new UrlHttpConnection.Result(states, null);
		}
		huc = new UrlHttpConnection(mContext);
		if (url.equals(Constants.LOCAL_URL_SK)) {
			byte[] receive = DataUtil.getInstance(mContext).getKeys();
			return new UrlHttpConnection.Result(UrlHttpConnection.OK, receive);
		}
		UrlHttpConnection.Result result = null;
		byte[] compress = null;
		byte[] receive = null;
		if (content != null) {
			try {
				LogUtil.d(TAG, "doInBackground", "compress content");
				// compress = compress(content.getBytes("UTF-8"));
				compress = content.getBytes("UTF-8");
			} catch (Exception e) {
				LogUtil.d(TAG, "doInBackground", "compress error");
				e.printStackTrace();
			}
		}
		if (compress != null) {
			if (Constants.LOCAL_PROTOCOL) {
				result = huc.connect(url, compress, map);// 联调前，从文件读取数据
			} else {
				result = huc.connect(url, compress, map);
			}
		} else if (userIcon != null) {
			result = huc.connect(url, userIcon, map);
		} else {
			if (Constants.LOCAL_PROTOCOL) {
				result = huc.connect(url, map);// 联调前，从文件读取数据
			} else {
				result = huc.connect(url, map);
			}
		}
		if (result != null) {
			states = result.getState();
			receive = result.getResult();
			// LogUtil.d(TAG, "doInBackground", "states:" + states + " bytes:" +
			// receive.length);
		}
		if (states == UrlHttpConnection.OK) {
			if (receive == null || receive.length == 0) {
				return new UrlHttpConnection.Result(UrlHttpConnection.ERROR,
						null);
			}
			byte[] body = null;
			// try {
			// LogUtil.d(TAG, "doInBackground", "decompress result");
			// body = decompress(receive);
			// } catch (IOException e) {
			// LogUtil.d(TAG, "doInBackground", "decompress error");
			// e.printStackTrace();
			// }
			body = receive;
			if (body == null || body.length == 0) {
				return new UrlHttpConnection.Result(UrlHttpConnection.ERROR,
						null);
			}
			receive = body;
			if (Constants.IS_SAVE_PROTOCOL) {
				FileUtil.saveProtocelToFile(body, "Response_" + action);
			}
		}

		return new UrlHttpConnection.Result(states, receive);
	}

	@Override
	protected void onPostExecute(UrlHttpConnection.Result result) {
		LogUtil.d(TAG, "onPostExecute", "states:" + result+"  ,result state = "+result.getResult());
		if (mTaskListener != null && !huc.isCanceled()) {
			switch (result.getState()) {
			case UrlHttpConnection.OK:
				mTaskListener.onPostExecuteUser(result.getResult());
				break;
			case UrlHttpConnection.NO_NETWORK:
				mTaskListener.onNoNetworkingUser();
				break;
			case UrlHttpConnection.CANCEL:// TODO
				break;
			default:// HttpUrlConnection.ERROR
				mTaskListener.onNetworkingErrorUser();
				break;
			}
		}
		super.onPostExecute(result);
	}

	/**
	 * 解压
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public static byte[] decompress(byte[] source) throws IOException {
		ByteArrayInputStream bin = new ByteArrayInputStream(source);
		GZIPInputStream gis = new GZIPInputStream(bin);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int length = 0;
		while ((length = gis.read(buf)) > 0) {
			bos.write(buf, 0, length);
		}
		return bos.toByteArray();
	}

	/**
	 * 压缩
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public static byte[] compress(byte[] source) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(source);
		gzip.close();
		return out.toByteArray();
	}

	/**
	 * 取消联网任务
	 */
	@Override
	public void onCancelled() {
		LogUtil.d(TAG, "onCancelled", "protocol task cancel");
		if (huc != null) {
			huc.cancel();
		}
		cancel(true);
	}

	/**
	 * 设置监听器
	 * 
	 * @param l
	 */
	public void setListener(TaskListenerUser l) {
		this.mTaskListener = l;
	}

	/**
	 * 接口访问监听接口
	 * 
	 * @author qiuximing
	 * 
	 */
	public interface TaskListenerUser {
		/**
		 * 没有网络连接
		 */
		void onNoNetworkingUser();

		/**
		 * 联网错误,返回null
		 */
		void onNetworkingErrorUser();

		/**
		 * 正常联网
		 * 
		 * @param is
		 */
		void onPostExecuteUser(byte[] bytes);

	}
}
