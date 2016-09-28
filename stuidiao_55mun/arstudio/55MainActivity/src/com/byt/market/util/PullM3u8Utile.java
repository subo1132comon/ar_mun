package com.byt.market.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.util.Log;
public class PullM3u8Utile {
	
	/**解析m3u8格式地址*/
	public static void getM3u8path(final String path,final M3u8callback back){
		// 为了从Web获取M3U文件，可以使用Apache软件基金会的HttpClient库，
				// 首先创建一个HttpClient对象，其代表类似Web浏览器的事物；
		Log.d("nnlog", "thread--"+path);
		new Thread(){
			public void run() {
				HttpClient httpClient = new DefaultHttpClient();
				// 然后创建一个HttpGet对象，其表示指向一个文件的具体请求。
				HttpGet getRequest = new HttpGet(
						"http://real3.atimemedia.com/live/chill.sdp/playlist.m3u8");
				//http://real3.atimemedia.com/live/chill.sdp/chunklist_w1754693987.m3u8
				// HttpClient将执行HttpGet，并返回一个HttpResponse
				String filePath = "";
				try {
					HttpResponse httpResponse = httpClient.execute(getRequest);
					if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
						Log.v("HTTP ERROR", httpResponse.getStatusLine()
								.getReasonPhrase());
					} else {
						// 在发出请求之后，可以从HttpRequest中获取一个InputStream，
						// 其包含了所请求文件的内容
						InputStream inputStream = httpResponse.getEntity().getContent();
						// 借助一个BufferedReader可以逐行得遍历该文件
						BufferedReader bufferedReader = new BufferedReader(
								new InputStreamReader(inputStream));
						String line;
						while ((line = bufferedReader.readLine()) != null) {
							Log.v("PLAYLISTLINE", "ORIG:" + line);
							if (line.startsWith("#")) {
								// 元数据，可以做更多的处理，但现在忽略它
							} else if (line.length() > 0) {
								// 如果它的长度大于0，那么就假设它是一个播放列表条目
									filePath = "";
								if (line.startsWith("http://")) {
									// 如果行以“http://”开头那么就把它作为流的完整URL
									filePath = line;
								} else {
									// 否则把它作为一个相对的URL，
									// 同时把针对该M3U文件的原始请求的URL附加上去
									filePath = getRequest.getURI().resolve(line)
											.toString();
								}
								// 将其添加到播放列表条目的容器中去
								// 添加播放文件到播放列表
							}
						}
						back.resoultM3u8path(filePath);
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	public interface M3u8callback{
		public void resoultM3u8path(String path);
	}
}
