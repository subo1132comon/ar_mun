package com.byt.market.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.download.DownloadUtils;
import com.byt.market.util.LogUtil;
import com.byt.market.util.PackageUtil;

public class UpdateDownloadService extends Service {
	private static final int NOTIFY_DOW_ID = 0;
	private static final int NOTIFY_OK_ID = 1;

	private Context mContext = this;
	private boolean cancelled;
	private int progress;
	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private DownloadBinder binder = new DownloadBinder();

	private int fileSize; // 文件大小
	private int readSize = -1; // 读取长度
	private int downSize; // 已下载大小
	private File downFile; // 下载的文件

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				// 更新进度
				RemoteViews contentView = mNotification.contentView;
				contentView.setTextViewText(R.id.rate, MyApplication.getInstance().getResources().getString(R.string.down_speed)
						+ (readSize < 0 ? 0 : (int) (readSize / 1024))
						+ "k/s   "+ MyApplication.getInstance().getResources().getString(R.string.down_progress)+ msg.arg1 + "%");
				contentView.setProgressBar(R.id.progress, 100, msg.arg1, false);

				// 更新UI
				mNotificationManager.notify(NOTIFY_DOW_ID, mNotification);

				break;
			case 1:
				mNotificationManager.cancel(NOTIFY_DOW_ID);
				createNotification(NOTIFY_OK_ID);
				/* 打开文件进行安装 */
				openFile(downFile);
				break;
			case 2:
				mNotificationManager.cancel(NOTIFY_DOW_ID);
				break;
			}
		};
	};

	private Handler handMessage = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(mContext, MyApplication.getInstance().getResources().getString(R.string.down_neerror), Toast.LENGTH_SHORT)
						.show();
				if (downFile.exists())
					downFile.delete();
				break;
			case 1:
				Toast.makeText(mContext,MyApplication.getInstance().getResources().getString(R.string.down_netnot), Toast.LENGTH_SHORT)
						.show();
				if (downFile.exists())
					downFile.delete();
				break;
			case 2:
				Toast.makeText(getApplicationContext(),MyApplication.getInstance().getResources().getString(R.string.down_nocache), 150)
						.show();
				break;
			}

			handler.sendEmptyMessage(2);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		cancelled = true;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// 返回自定义的DownloadBinder实例
		return binder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelled = true; // 取消下载线程
	}

	/**
	 * 创建通知
	 */
	@SuppressLint("NewApi")
	private void createNotification(int notifyId) {
		switch (notifyId) {
		case NOTIFY_DOW_ID:
			int icon = R.drawable.icon;
			CharSequence tickerText = MyApplication.getInstance().getResources().getString(R.string.down_gamecenterdowns);
			long when = System.currentTimeMillis();
			mNotification = new Notification(icon, tickerText, when);

			// 放置在"正在运行"栏目中
			mNotification.flags = Notification.FLAG_ONGOING_EVENT;

			RemoteViews contentView = new RemoteViews(
					mContext.getPackageName(),
					R.layout.download_notification_layout);
			contentView.setTextViewText(R.id.fileName,MyApplication.getInstance().getResources().getString(R.string.down_gamecenterdownu));

			// 指定个性化视图
			mNotification.contentView = contentView;

			Intent intent = new Intent();
			PendingIntent contentIntent = PendingIntent.getActivity(mContext,
					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			// 指定内容意图
			mNotification.contentIntent = contentIntent;

			break;
		case NOTIFY_OK_ID:
			int icon2 = R.drawable.icon;
			CharSequence tickerText2 = MyApplication.getInstance().getResources().getString(R.string.down_gamecenterdownend);
			long when2 = System.currentTimeMillis();
			PendingIntent contentInten2 = PendingIntent.getActivity(mContext,
					0, new Intent(), 0);
			mNotification = new Notification.Builder(mContext)
			 .setAutoCancel(true)    
	         .setContentTitle(tickerText2)    
	         .setContentText(MyApplication.getInstance().getResources().getString(R.string.down_gamecenterdownend2))    
	         .setContentIntent(contentInten2)    
	         .setSmallIcon(icon2)    
	         .setWhen(when2)  
			 .build();  
			//mNotification = new Notification(icon2, tickerText2, when2);
			// 放置在"通知形"栏目中
			mNotification.flags = Notification.FLAG_AUTO_CANCEL;
//			PendingIntent contentInten2 = PendingIntent.getActivity(mContext,
//					0, new Intent(), 0);
//			mNotification.setLatestEventInfo(mContext, MyApplication.getInstance().getResources().getString(R.string.down_gamecenterdownend), MyApplication.getInstance().getResources().getString(R.string.down_gamecenterdownend2),
//					contentInten2);
			stopSelf();// 停掉服务自身
			cancelled = true;
			break;
		}

		// 最后别忘了通知一下,否则不会更新
		mNotificationManager.notify(notifyId, mNotification);
	}

	/**
	 * 下载模块
	 */
	private void startDownload(String dowUrl) {
		// 初始化数据
		fileSize = 0;
		readSize = 0;
		downSize = 0;
		progress = 0;

		InputStream is = null;
		FileOutputStream fos = null;

		try {
			URL myURL = new URL(dowUrl); // 取得URL
			URLConnection conn = myURL.openConnection(); // 建立联机
			conn.connect();
			fileSize = conn.getContentLength(); // 获取文件长度
			String apkFilename = Constants.APKNAME;
			// 建立临时文件
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				if (DownloadUtils.getSDCardAvailableSpace() <= fileSize) {
					downFile = new File(getFilesDir() + File.separator
							+ apkFilename);
				} else {
					String downloadDir = MyApplication.DATA_DIR;
					File file = new File(downloadDir);
					if (!file.exists()) {
						file.mkdirs();
					}
					downFile = new File(file.getAbsolutePath() + File.separator
							+ apkFilename);
				}
			} else {
				downFile = new File(getFilesDir() + File.separator
						+ apkFilename);
			}
			is = conn.getInputStream(); // InputStream 下载文件
			if (is == null) {
				LogUtil.d("tag", "error");
				throw new RuntimeException("stream is null");
			}
			if (downFile == null)
				return;
			if (downFile.exists())
				downFile.delete();
			// 将文件写入临时盘
			fos = new FileOutputStream(downFile);
			byte buf[] = new byte[1024 * 1024];
			while (!cancelled && (readSize = is.read(buf)) != -1) {
				fos.write(buf, 0, readSize);
				downSize += readSize;
				sendMessage(0);
			}

			if (cancelled) {
				handler.sendEmptyMessage(2);
				downFile.delete();
			} else {
				handler.sendEmptyMessage(1);
			}
		} catch (MalformedURLException e) {
			handMessage.sendEmptyMessage(0);
		} catch (IOException e) {
			handMessage.sendEmptyMessage(1);
		} catch (Exception e) {
			handMessage.sendEmptyMessage(0);
		} finally {
			try {
				if (null != fos)
					fos.close();
				if (null != is)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendMessage(int what) {
		int num = (int) ((double) downSize / (double) fileSize * 100);

		if (num > progress + 1) {
			progress = num;

			Message msg0 = handler.obtainMessage();
			msg0.what = what;
			msg0.arg1 = progress;
			handler.sendMessage(msg0);
		}
	}

	// 在手机上打开文件的method
	private void openFile(File f) {
		PackageUtil.chmod(f.getAbsolutePath(), "777");
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		// 调用getMIMEType()来取得MimeType
		String type = getMIMEType(f);
		// 设定intent的file与MimeType
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
		mNotificationManager.cancel(NOTIFY_OK_ID);
	}

	// 判断文件MimeType的method
	private String getMIMEType(File f) {
		String type = "";
		String fName = f.getName();
		// 取得扩展名
		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();

		// 按扩展名的类型决定MimeType
		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("3gp") || end.equals("mp4")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image";
		} else if (end.equals("apk")) {
			// android.permission.INSTALL_PACKAGES
			type = "application/vnd.android.package-archive";
		} else {
			type = "*";
		}
		// 如果无法直接打开，就跳出软件清单给使用者选择
		if (!end.equals("apk")) {
			type += "/*";
		}

		return type;
	}

	/**
	 * DownloadBinder中定义了一些实用的方法
	 * 
	 * @author user
	 */
	public class DownloadBinder extends Binder {
		/**
		 * 开始
		 */
		public void start(final String url) {
			cancelled = false;
			new Thread() {
				public void run() {
					createNotification(NOTIFY_DOW_ID); // 创建通知
					startDownload(url); // 下载
					cancelled = true;
				};
			}.start();
		}

		/**
		 * 获取进度
		 * 
		 * @return
		 */
		public int getProgress() {
			return progress;
		}

		/**
		 * 取消下载
		 */
		public void cancel() {
			cancelled = true;
		}

		/**
		 * 是否已被取消
		 * 
		 * @return
		 */
		public boolean isCancelled() {
			return cancelled;
		}

	}
}
