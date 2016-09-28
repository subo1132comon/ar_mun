package com.byt.market.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.util.Log;

public class NotifaHttpUtil {

	//public static final String path = "http://122.155.202.149:8022/Joke/v1.php?qt=Push42&sid=";
	public static void getJson(final String path, final NotifaHttpResalout notif) {

		// 建立一个URL对象
		new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				URL url;
				try {
					url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					// 设置请求超时与请求方式
					conn.setReadTimeout(5 * 1000);
					conn.setRequestMethod("GET");  
					// 从链接中获取一个输入流对象
					InputStream inStream = conn.getInputStream();
					// 调用数据流处理方法
					byte[] data = readInputStream(inStream);
					String json = new String(data);
					notif.reaslout(json);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 得到打开的链接对象
				catch (ProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}

	public static void shareHttp(final String path){
		new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				URL url;
				try {
					url = new URL(path);
					Log.d("nnlog", "path--"+path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					// 设置请求超时与请求方式
					conn.setReadTimeout(30 * 1000);
					conn.setRequestMethod("GET");  
					int cod = conn.getResponseCode();
					Log.d("nnlog", "code--"+cod);
					// 从链接中获取一个输入流对象
					// 调用数据流处理方法
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d("nnlog", "异常--");
					e.printStackTrace();
				}
			}
		}.start();
	}
	/**
	 * 从输入流中获取数据
	 * 
	 * @param inStream
	 *            输入流
	 * @return
	 * @throws Exception
	 */
	public static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();
	}

	public interface NotifaHttpResalout {
		public void reaslout(String json);
	}

}
