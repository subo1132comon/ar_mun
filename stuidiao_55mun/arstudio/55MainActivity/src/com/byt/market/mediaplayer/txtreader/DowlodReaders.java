package com.byt.market.mediaplayer.txtreader;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.byt.market.mediaplayer.voiced.VoicedDailogActivity;
import com.byt.market.util.FileUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;

public class DowlodReaders {

	private static Context mcontext;
	private static RelativeLayout mlodaing;
	private static boolean mIsCancel = false;
	private static String mSavePath;
	private static String filename;
	private static String mVersion_path;
	public static String NOVEL_DIRZIP = Environment.getExternalStorageDirectory()
			.getPath() + "/SYNC/novelzip/";
	public static String NOVEL_DIR = Environment.getExternalStorageDirectory()
			.getPath() + "/SYNC/novel/";
	public static void dowLod(Context context,String path,String bookname,RelativeLayout loading){
		mVersion_path = path;
		filename = bookname;
		mlodaing = loading;
		mcontext = context;
		loading.setVisibility(View.VISIBLE);
		new Thread(new ProgressRunable()).start();
		
	}
	public static void readBook(Context context,String path){
		
		//loading.setVisibility(View.VISIBLE);
		Intent intent = new Intent(context, TxtViewerActivity.class);
		intent.putExtra("path",path );
		context.startActivity(intent);
	}
	private static Handler mhandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			mlodaing.setVisibility(View.GONE);
			Intent intent = new Intent(mcontext, TxtViewerActivity.class);
			intent.putExtra("path", Environment.getExternalStorageDirectory()
					.getPath() + "/SYNC/novel/"+filename+".txt");
			mcontext.startActivity(intent);
		};
	};
	static class ProgressRunable implements Runnable {
		@Override
		public void run() {
			try{
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//					String sdPath = Environment.getExternalStorageDirectory() + "/";
//					mSavePath = sdPath + "jikedownload";
					mSavePath = NOVEL_DIRZIP;
					File dir = new File(mSavePath);
					if (!dir.exists())
						dir.mkdirs();
					
					// 下载文件
					HttpURLConnection conn = (HttpURLConnection) new URL(mVersion_path).openConnection();
					conn.connect();
					InputStream is = conn.getInputStream();
					int length = conn.getContentLength();
					
					
					File f = new File(mSavePath);
					File apkFile = null;
					if(f.exists()){
						 apkFile = new File(mSavePath+filename);
					}
					if(!apkFile.exists()){
						apkFile.createNewFile();
					}
					FileOutputStream fos = new FileOutputStream(apkFile);
					
					int count = 0;
					byte[] buffer = new byte[1024];
					while (!mIsCancel){
						int numread = is.read(buffer);
						count += numread;
						// 计算进度条的当前位置
//						mCurrentProgress = (int) (((float)count/length) * 100);
//						// 更新进度条
//						mUpdateProgressHandler.sendEmptyMessage(DOWNLOADING);
						
						// 下载完成
						if (numread < 0){
							//解压zip文件到 apkFile
							File noveldir = new File(NOVEL_DIR);
							if (!noveldir.exists())
								noveldir.mkdir();
							FileUtil.Unzip(apkFile.getAbsolutePath(), NOVEL_DIR);
							//删除zip
							FileUtil.DeleteFile(apkFile);
							//mUpdateProgressHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							Message msg = mhandler.obtainMessage();
							mhandler.sendMessage(msg);
							break;
						}
						fos.write(buffer, 0, numread);
					}
					fos.close();
					is.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
}
