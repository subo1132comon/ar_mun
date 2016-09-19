package com.byt.market.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.byt.market.R;
import com.byt.market.util.FileUtil;
import com.byt.market.util.Singinstents;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
public class DownLoadVdioapkTools {
	
	private int mProgress;
	private boolean mIsCancel = false;
	private ProgressBar mProgressBar;
	private Dialog mDownloadDialog;
	private Context mcontext;
	private String mSavePath;
	private String mVersion_name ="45";
	private String mVersion_path ="http://www.55mun.com/55player.apk";
	private static final int DOWNLOAD_FINISH = 2;
	private static final int DOWNLOADING = 1;
	
	public DownLoadVdioapkTools(Context context){
		this.mcontext = context;
	}
	
	private Handler mUpdateProgressHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what){
			case DOWNLOADING:
				// 设置进度条
				mProgressBar.setProgress(mProgress);
				break;
			case DOWNLOAD_FINISH:
				// 隐藏当前下载对话框
				mDownloadDialog.dismiss();
				// 安装 APK 文件
				installAPK();
			}
		};
	};
	
	public  boolean isRunningApp(Context context, String packageName) {
        boolean isAppRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(100);
        for (RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(packageName) && info.baseActivity.getPackageName().equals(packageName)) {
                isAppRunning = true;
                // find it, break
                break;
            }
        }
        return isAppRunning;
    }
	
	public void startAPP(String url){  
	    try{  
	    	//删除  视频播放器 下载包
	    	new Thread(){
	        	@Override
	        	public void run() {
	        		// TODO Auto-generated method stub
	        		super.run();
	        		 FileUtil.DeleteFile(new File(Environment.getExternalStorageDirectory() + "/"+"jikedownload"));
	        	}
	        }.start();
	       // Intent intent = mcontext.getPackageManager().getLaunchIntentForPackage(appPackageName);  
	        Intent intent = new Intent("my.player");
	    	intent.putExtra("url", url);
	    	intent.putExtra("packg", "com.byt.market");
	        mcontext.startActivity(intent); 
	        
	    }catch(Exception e){  
	        Toast.makeText(mcontext, "没有安装", Toast.LENGTH_LONG).show();  
	    }  
	}  
	
	public boolean checkApkExist(Context context, String packageName) {
    	if (packageName == null || "".equals(packageName))
    	return false;
    	try {
    	context.getPackageManager()
    	.getApplicationInfo(packageName,
    	PackageManager.GET_UNINSTALLED_PACKAGES);
    	return true;
    	} catch (NameNotFoundException e) {
    	return false;
    	}
    	}
	
	public void showNoticeDialog() {
		AlertDialog.Builder builder = new Builder(mcontext);
		builder.setTitle(mcontext.getResources().getString(R.string.dialog_retry_title));
		//String message = mcontext.getString(R.string.isdownload);
		//LogCart.Log(message+"jjjjjjjjjjj");
		builder.setMessage("โหลด Movie Player เพื่อดูหนังลื่นขึ้น");
		builder.setCancelable(false);
		builder.setPositiveButton(mcontext.getResources().getString(R.string.download),
				new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 隐藏当前对话框
				dialog.dismiss();
				// 显示下载对话框
				showDownloadDialog();
			}
		});
		
		builder.setNegativeButton(mcontext.getResources().getString(R.string.cancel),
				new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 隐藏当前对话框
				dialog.dismiss();
			}
		});
		
		builder.create().show();
	}
	
	/*
	 * 显示正在下载对话框
	 */
	protected void showDownloadDialog() {
		AlertDialog.Builder builder = new Builder(mcontext);
		builder.setTitle(mcontext.getResources().getString(R.string.downloading_title_desc));
		View view = LayoutInflater.from(mcontext).inflate(R.layout.dialog_progress_vdio, null);
		mProgressBar = (ProgressBar) view.findViewById(R.id.id_progress);
		builder.setView(view);
		builder.setCancelable(false);
		builder.setNegativeButton(mcontext.getResources().getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 隐藏当前对话框
				dialog.dismiss();
				// 设置下载状态为取消
				mIsCancel = true;
			}
		});
		
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		
		// 下载文件
		downloadAPK();
	}
	
	/*
	 * 开启新线程下载文件
	 */
	private void downloadAPK() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
						String sdPath = Environment.getExternalStorageDirectory() + "/";
						mSavePath = sdPath + "jikedownload";
						
						File dir = new File(mSavePath);
						if (!dir.exists())
							dir.mkdir();
						
						// 下载文件
						HttpURLConnection conn = (HttpURLConnection) new URL(mVersion_path).openConnection();
						conn.connect();
						InputStream is = conn.getInputStream();
						int length = conn.getContentLength();
						
						File apkFile = new File(mSavePath, mVersion_name);
						FileOutputStream fos = new FileOutputStream(apkFile);
						
						int count = 0;
						byte[] buffer = new byte[1024];
						while (!mIsCancel){
							int numread = is.read(buffer);
							count += numread;
							// 计算进度条的当前位置
							mProgress = (int) (((float)count/length) * 100);
							// 更新进度条
							mUpdateProgressHandler.sendEmptyMessage(DOWNLOADING);
							
							// 下载完成
							if (numread < 0){
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
		}).start();
	}
	
	/*
	 * 下载到本地后执行安装
	 */
	protected void installAPK() {
		SharedPreferences mySharedPreferences=mcontext.getSharedPreferences("data",mcontext.MODE_WORLD_READABLE);    
        SharedPreferences.Editor editor=mySharedPreferences.edit();    
        editor.putString("url",Singinstents.getInstents().getVdiouri());    
        editor.commit();   
		File apkFile = new File(mSavePath, mVersion_name);
		if (!apkFile.exists())
			return;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.parse("file://" + apkFile.toString());
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		mcontext.startActivity(intent);
		
	}
}
