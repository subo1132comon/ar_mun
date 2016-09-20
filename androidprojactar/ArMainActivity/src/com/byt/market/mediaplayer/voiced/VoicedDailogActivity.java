package com.byt.market.mediaplayer.voiced;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.byt.ar.R;
import com.byt.market.mediaplayer.txtreader.TxtViewerActivity;
import com.byt.market.tools.LogCart;
import com.byt.market.util.FileUtil;
import com.byt.market.view.TasksCompletedView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

public class VoicedDailogActivity extends Activity{
	
private TasksCompletedView mTasksView;
	
	private int mTotalProgress;
	private int mCurrentProgress;
	private String mSavePath;
	private String filename;
	private String mVersion_path ="http://55mun.com:8022/music/201603/16/874f/56e8e0ebeca28.zip";
	private boolean mIsCancel = false;
	public static String MUSIC_DIR = Environment.getExternalStorageDirectory()
			.getPath() + "/SYNC/music/";
	
	public static String NOVEL_DIRZIP = Environment.getExternalStorageDirectory()
			.getPath() + "/SYNC/novelzip/";
	public static String NOVEL_DIR = Environment.getExternalStorageDirectory()
			.getPath() + "/SYNC/novel/";
	private static final int DOWNLOAD_FINISH = 2;
	private static final int DOWNLOADING = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_voiced_dailog);
		mTasksView = (TasksCompletedView) findViewById(R.id.tasks_view);
		mVersion_path = getIntent().getStringExtra("path");
		filename = getIntent().getStringExtra("name");
		LogCart.Log("-------wo wo wo"+mVersion_path+"--"+filename);
		mCurrentProgress = 0;
		new Thread(new ProgressRunable()).start();
	}
	
	private Handler mUpdateProgressHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what){
			case DOWNLOADING:
				// 设置进度条
				mTasksView.setProgress(mCurrentProgress);
				break;
			case DOWNLOAD_FINISH:
				// 隐藏当前下载对话框
				//mDownloadDialog.dismiss();
				VoicedDailogActivity.this.finish();
				Intent intent = new Intent(VoicedDailogActivity.this, TxtViewerActivity.class);
				intent.putExtra("path", Environment.getExternalStorageDirectory()
						.getPath() + "/SYNC/novel/"+filename+".txt");
				startActivity(intent);
			}
		};
	};
	
	
	class ProgressRunable implements Runnable {
		@Override
		public void run() {
			try{
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//					String sdPath = Environment.getExternalStorageDirectory() + "/";
//					mSavePath = sdPath + "jikedownload";
					mSavePath = NOVEL_DIRZIP;
					File dir = new File(mSavePath);
					if (!dir.exists())
						dir.mkdir();
					
					// 下载文件
					HttpURLConnection conn = (HttpURLConnection) new URL(mVersion_path).openConnection();
					conn.connect();
					InputStream is = conn.getInputStream();
					int length = conn.getContentLength();
					
					File apkFile = new File(mSavePath+filename);
					FileOutputStream fos = new FileOutputStream(apkFile);
					
					int count = 0;
					byte[] buffer = new byte[1024];
					while (!mIsCancel){
						int numread = is.read(buffer);
						count += numread;
						// 计算进度条的当前位置
						mCurrentProgress = (int) (((float)count/length) * 100);
						// 更新进度条
						mUpdateProgressHandler.sendEmptyMessage(DOWNLOADING);
						
						// 下载完成
						if (numread < 0){
							//解压zip文件到 apkFile
							File noveldir = new File(NOVEL_DIR);
							if (!noveldir.exists())
								noveldir.mkdir();
							FileUtil.Unzip(apkFile.getAbsolutePath(), NOVEL_DIR);
							//删除zip
							FileUtil.DeleteFile(apkFile);
							mUpdateProgressHandler.sendEmptyMessage(DOWNLOAD_FINISH);
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
