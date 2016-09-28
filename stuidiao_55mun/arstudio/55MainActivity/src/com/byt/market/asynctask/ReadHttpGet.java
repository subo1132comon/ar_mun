package com.byt.market.asynctask;

import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.byt.market.Constants;
import com.byt.market.net.UrlHttpConnection;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.DataUtil;
import com.byt.market.util.FileUtil;
import com.byt.market.util.LogUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ReadHttpGet   extends
AsyncTask<Object, Void, UrlHttpConnection.Result> 
{
	public static final String TAG = "ProtocolTask";
	private Context mContext;
	UrlHttpConnection huc;
	TaskListenerRead mTaskListener;
	public ListViewFragment mFragment;

	public ReadHttpGet(Context context) {
		mContext = context;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected UrlHttpConnection.Result doInBackground(Object... params) {
		LogUtil.d(TAG, "doInBackground", "start");
		String url = (String) params[0];
		String content = null;
		byte[] ReadIcon = null;
		if (params[1] instanceof String) {
			content = (String) params[1];
		} else {
			ReadIcon = (byte[]) params[1];
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
		} else if (ReadIcon != null) {
			result = huc.connect(url, ReadIcon, map);
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
				mTaskListener.onPostExecuteRead(result.getResult());
				break;
			case UrlHttpConnection.NO_NETWORK:
				mTaskListener.onNoNetworkingRead();
				break;
			case UrlHttpConnection.CANCEL:// TODO
				break;
			default:// HttpUrlConnection.ERROR
				mTaskListener.onNetworkingErrorRead();
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
	public void setListener(TaskListenerRead l) {
		this.mTaskListener = l;
	}

	/**
	 * 接口访问监听接口
	 * 
	 * @author qiuximing
	 * 
	 */
	public interface TaskListenerRead {
		/**
		 * 没有网络连接
		 */
		void onNoNetworkingRead();

		/**
		 * 联网错误,返回null
		 */
		void onNetworkingErrorRead();

		/**
		 * 正常联网
		 * 
		 * @param is
		 */
		void onPostExecuteRead(byte[] bytes);

	}	
}

