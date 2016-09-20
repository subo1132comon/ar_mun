package com.byt.market.util;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.DetailFrame2;
import com.byt.market.activity.DownLoadManageActivity;
import com.byt.market.activity.GameListUpdateFrame;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.PushMessageFrame;
import com.byt.market.data.MarketUpdateInfo;
import com.byt.market.data.PUSH;
import com.byt.market.data.PushInfo;
import com.byt.market.log.LogModel;

public class SysNotifyUtil {

	public static final String TAG = "MarketNotify";
	private static NotificationManager notificationManager;
	public static final int SOFTINSTALL = 3;
	public static List<Integer> notifyIDs = new ArrayList<Integer>();

	/**
	 * ����֪ͨ
	 * 
	 * @param context
	 * @param NOTIFICATION
	 *            ֪ͨ���ID
	 */
	public static void clearNofity(Context context, int NOTIFICATION) {
		getNotificationManager(context).cancel(NOTIFICATION);
	}

	/**
	 * ȡ��֪ͨ������
	 * 
	 * @param context
	 * @return
	 */
	private static NotificationManager getNotificationManager(Context context) {
		if (notificationManager == null)
			notificationManager = (NotificationManager) context
					.getSystemService("notification");
		return notificationManager;
	}

	// /**
	// * Ӧ������֪ͨ
	// *
	// * @param context
	// * @param tickertext
	// */
	// public static void nofityAppDownload(Context context, String tickertext)
	// {
	// // Intent intent = new Intent("com.byt.market.ACTION_NOTIFY");
	// // intent.putExtra("text", tickertext);
	// // context.sendBroadcast(intent );
	// MarketNotify.nofityAppDownload(context, tickertext,
	// Notification.FLAG_AUTO_CANCEL, R.drawable.notify_downing);
	//
	// }
	//
	/**
	 * Ӧ������֪ͨ
	 * 
	 * @param context
	 * @param tickertext
	 */
	public static void nofityAppDownload(Context context, String tickertext,
			int icon) {
//		MarketNotify.nofityAppDownload(context, tickertext,
//				Notification.FLAG_AUTO_CANCEL, icon);

	}

	public static void saveNotifyId(int id) {
		if (!notifyIDs.contains(id)) {
			notifyIDs.add(id);
		}
	}

	/*public static void nofityAppDownload(Context context, String tickertext,
			int Status, int icon) {
		Notification newNotification = new Notification(icon, tickertext,
				System.currentTimeMillis());

		Intent notificationIntent = new Intent(context, DownloadActivity.class);
		notificationIntent.putExtra("from", 1);
		// ��תҳ��
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		String down_task_notify = context
				.getString(R.string.market_download_task);

		newNotification.setLatestEventInfo(context, down_task_notify,
				tickertext, pendingIntent);

		// ֪ͨ��ʾ��ʽ
		newNotification.flags |= Status;
		// ֪ͨ
		getNotificationManager(context).notify(R.string.market_download_task,
				newNotification);
		saveNotifyId(R.string.market_download_task);
	}*/

	/**
	 * �����װ���֪ͨ
	 * 
	 * @param context
	 * @param packagename
	 *            ����
	 * @param tickertext
	 * @param contextText
	 */
	@SuppressLint("NewApi")
	public static void notifySoftwareInstallInfo(Context context, int id,
			String packagename, String tickertext, String contextText) {

//		Notification notification = new Notification(R.drawable.icon,
//				tickertext, System.currentTimeMillis());
		
		Intent intent = null;

		try {
			intent = context.getPackageManager().getLaunchIntentForPackage(
					packagename);
			if (intent == null)
				intent = new Intent("com.byt.market.not");
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, 0x8000000);
			Notification notification = new Notification.Builder(context)
			 .setAutoCancel(true)    
	         .setContentTitle(tickertext)    
	         .setContentText(MyApplication.getInstance().getResources().getString(R.string.down_gamecenterdownend2))    
	         .setContentIntent(pendingIntent)    
	         .setSmallIcon(R.drawable.icon)    
	         .setWhen(System.currentTimeMillis())  
			 .build();  

			// ֪ͨ��ʾ��ʽ,�Զ�ȡ��
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			// ֪ͨ
			getNotificationManager(context).notify(id, notification);
			saveNotifyId(id);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressLint("NewApi")
	public static void notifyPushInfo(Context context,
			MarketContext marketContext, int id, PushInfo pinfo) {
//		Notification notification = new Notification(R.drawable.icon,
//				pinfo.pname, System.currentTimeMillis());
		try {
			Intent intent = null;
			if (pinfo.ptype == 3) {
				intent = new Intent(context, PushMessageFrame.class);
				if (pinfo.pvalue != null)
					intent.putExtra("pinfo", pinfo);
			} else if (pinfo.ptype == 2) {
				intent = new Intent(context, DetailFrame2.class);
				if (pinfo.pvalue != null) {
					pinfo.app.list_id = LogModel.L_PUSH;
					pinfo.app.list_cateid = LogModel.P_LIST;
					intent.putExtra("app", pinfo.app);
					intent.putExtra("p_id", pinfo.id);
					intent.putExtra("push", true);
					intent.putExtra("from", LogModel.L_PUSH);
				}
			} else if (pinfo.ptype == 1) {
				PUSH push = new PUSH();
				push.type = 1;
				push.id = pinfo.id;
				push.state = 1;
				Util.addData(marketContext, push);
				intent = new Intent();
				intent.setAction("com.byt.market.not");
			}
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, 0x8000000);
			Notification notification = new Notification.Builder(context)
			 .setAutoCancel(true)    
	         .setContentTitle(pinfo.pname)    
	         .setContentText(pinfo.pdesc)    
	         .setContentIntent(pendingIntent)    
	         .setSmallIcon(R.drawable.icon)    
	         .setWhen(System.currentTimeMillis())  
			 .build();  
//			notification.setLatestEventInfo(context, pinfo.pname, pinfo.pdesc,
//					pendingIntent);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			getNotificationManager(context).notify(id, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*public static void notifySoftwareUpdateInfo(Context context,
			String tickertext, String contextText) {
		Notification notification = new Notification(R.drawable.icon,
				tickertext, System.currentTimeMillis());
		Intent intent = new Intent(
				"com.tunkoo.marker.ACTION_CHOOSE_SOFT_UPDATE_NOTIFY");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, 0);
		notification.setLatestEventInfo(context, tickertext, contextText,
				pendingIntent);
		// ֪ͨ��ʾ��ʽ,�Զ�ȡ��
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// �������֪ͨ
		getNotificationManager(context).notify(R.string.software_update_title,
				notification);
		saveNotifyId(R.string.software_update_title);
	}*/

	/**
	 * downCount + waitCount����������� downCount�������У�waitCount���ȴ��� 2�������У�����������ع���
	 * 
	 * @param context
	 * @param count
	 *//*
	public static void notifyDownloading(Context context, int downCount,
			int waitCount) {
		if (downCount == 0) {
			clearNofity(context, NOTIFY_ID_DOWNLOADING);
			return;
		}
		String title = "";
		String content = "";
		if (waitCount == 0) {
			title = (downCount + waitCount) + "�����������";
			content = downCount + "�������У�����������ع���";
		} else {
			title = (downCount + waitCount) + "�����������";
			content = downCount + "�������У�" + waitCount + "���ȴ���";
		}
		Intent intent = new Intent(context, DownloadActivity.class);
		intent.putExtra("from", 1);
		sendNotify(context, title, content, intent, NOTIFY_ID_DOWNLOADING,
				R.drawable.icon);
	}*/

	/**
	 * count����������� count��������ɣ��������������
	 * 
	 * @param context
	 * @param count
	 *//*
	public static void notifyDownloaded(Context context, int count) {
		String title = "";
		String content = "";
		title = count + "�������������";
		content = count + "��������ɣ�����������ع���";
		Intent intent = new Intent(context, DownloadActivity.class);
		intent.putExtra("from", 1);
		sendNotify(context, title, content, intent, NOTIFY_ID_DOWNLOADING,
				R.drawable.icon);
	}
*/
	// public static void

	public static final int NOTIFY_ID_DOWNLOADING = R.drawable.icon;

	// public static final int NOTIFY_ID_DOWNLOADED = R.drawable.icon + 1;

	
	@SuppressLint("NewApi")
	public static void sendNotify(Context context, String tickertext,
			String contextText, Intent intent, int id, int icon) {
		
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		Notification notification = new Notification.Builder(context)
		 .setAutoCancel(true)    
         .setContentTitle(tickertext)    
         .setContentText(MyApplication.getInstance().getResources().getString(R.string.down_gamecenterdownend2))    
         .setContentIntent(pendingIntent)    
         .setSmallIcon(icon)    
         .setWhen(System.currentTimeMillis())  
		 .build();  
		// Intent intent = new
		// Intent("com.tunkoo.marker.ACTION_CHOOSE_MARKET_UPDATE_NOTIFY");
		//notification.setLatestEventInfo(context, tickertext, contextText,
			//	pendingIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		getNotificationManager(context).notify(id, notification);
		saveNotifyId(id);
	}

	public static void cancelAllNotify(Context context) {
		getNotificationManager(context).cancelAll();
		// if(notifyIDs != null){
		// for (int i = 0; i < notifyIDs.size(); i++) {
		// Integer id = notifyIDs.get(i);
		// }
		// }
	}

	@SuppressLint("NewApi")
	public static void notifyMarketUpdateInfo(Context context,
			MarketContext mcContext, int icon, MarketUpdateInfo update) {
//		Notification notification = new Notification(R.drawable.icon,
//				context.getString(R.string.updatetip),
//				System.currentTimeMillis());
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				new Intent("com.byt.market.not"), 0x8000000);
		
		Notification notification = new Notification.Builder(context)
		.setAutoCancel(true)    
        .setContentTitle(context.getString(R.string.updatetip))    
        .setContentText(update.describe)    
        .setContentIntent(pendingIntent)    
        .setSmallIcon(R.drawable.icon)    
        .setWhen(System.currentTimeMillis())  
		 .build();  
//		notification.setLatestEventInfo(context,
//				context.getString(R.string.updatetip), update.describe,
//				pendingIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		getNotificationManager(context).notify(R.string.market_update_title,
				notification);
	}
	
	@SuppressLint("NewApi")
	public static void notifyGameListUpdate(Context context, int icon,int updateCount) {
//		Notification notification = new Notification(icon,
//				context.getString(R.string.game_update_notify_msg,updateCount),
//				System.currentTimeMillis());
//		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
//				new Intent(context,GameListUpdateFrame.class), 0x8000000);

        Intent updateIntent = new Intent(context,DownLoadManageActivity.class);
        updateIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
                DownLoadManageActivity.TYPE_FROM_UPDATE);
        updateIntent.putExtra(DownLoadManageActivity.ALL_UPDATE_COUNT,
                updateCount);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                updateIntent, 0x8000000);
        
        Notification notification = new Notification.Builder(context)
        .setAutoCancel(true)    
        .setContentTitle(context.getString(R.string.game_update_notify_msg,updateCount))    
        .setContentText(context.getString(R.string.game_update_notify_msg,updateCount))    
        .setContentIntent(pendingIntent)    
        .setSmallIcon(icon)    
        .setWhen(System.currentTimeMillis())  
		 .build();  
//		notification.setLatestEventInfo(context,
//				context.getString(R.string.game_update_notify_title), 
//				context.getString(R.string.game_update_notify_msg,updateCount),
//				pendingIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		getNotificationManager(context).notify(R.string.game_update_notify_title,
				notification);
	}
	
	
	@SuppressLint("NewApi")
	public static void notifyDownloadInfo(Context context, int icon,int downloadCount) {
		if(downloadCount == 0){
			getNotificationManager(context).cancel(R.string.game_download_notify_title);
			return;
		}
//		Notification notification = new Notification(icon,
//				context.getString(R.string.game_download_notify_msg,downloadCount),
//				System.currentTimeMillis());
        Intent intent = new Intent(context, DownLoadManageActivity.class);
        
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Notification notification = new Notification.Builder(context)
        .setAutoCancel(true)    
        .setContentTitle(context.getString(R.string.game_download_notify_msg,downloadCount))    
        .setContentText(context.getString(R.string.game_download_notify_msg,downloadCount))    
        .setContentIntent(pendingIntent)    
        .setSmallIcon(icon)    
        .setWhen(System.currentTimeMillis())  
		 .build();  
//		notification.setLatestEventInfo(context,
//				context.getString(R.string.game_download_notify_title), 
//				context.getString(R.string.game_download_notify_msg,downloadCount),
//				pendingIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		getNotificationManager(context).notify(R.string.game_download_notify_title,
				notification);
	}
	
}
