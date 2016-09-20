package com.byt.market.mediaplayer.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.data.BigItem;
import com.byt.market.data.DownloadItem;
import com.byt.market.data.RingItem;
import com.byt.market.mediaplayer.data.PlayDownloadItem;
import com.byt.market.mediaplayer.data.VideoItem;
import com.byt.market.mediaplayer.db.InfoDao;
import com.byt.market.service.DownloadService;
import com.byt.market.tools.LogCart;
import com.byt.market.util.StringUtil;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

public class PlayDownloadService extends Service {
	// public static ArrayList<PlayDownloadItem> PlayDownloadItems = new
	// ArrayList<PlayDownloadItem>();
	public static Map<String, PlayDownloadItem> PlayDownloadItems = new HashMap<String, PlayDownloadItem>();
//	public static List<PlayDownloadItem> playDownloadItemsall=new ArrayList<PlayDownloadItem>();
	boolean isVdiao = false;
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		int type=1;
		super.onStart(intent, startId);
		try{
			RingItem ringItemtmp = null;
			VideoItem videoItemtmp = null;
		Object PlayDownloadItemtmp=intent
				.getParcelableExtra(PlayDownloadItem.DOWN_ITEM); 
		String downloadUrl = null;
		String saveDir = null;
		if(PlayDownloadItemtmp instanceof RingItem)
		{
			ringItemtmp=(RingItem)PlayDownloadItemtmp;
			if(isMP3File(ringItemtmp.musicuri)){
			type=1;
			downloadUrl = ringItemtmp.musicuri;
			saveDir=PlayDownloadItem.MUSIC_DIR;
			MobclickAgent.onEvent(this, "Musicdown");
			StatService.trackCustomEvent(this, "Musicdown", "");
			}else{
				type=2;
				downloadUrl = ringItemtmp.adesc;
				saveDir=PlayDownloadItem.VIDEO_DIR;
				MobclickAgent.onEvent(this, "Vadiodown");
				StatService.trackCustomEvent(this, "Vadiodown", "");
				}
			//videoItemtmp=(VideoItem) PlayDownloadItemtmp;
		}else if(PlayDownloadItemtmp instanceof VideoItem){
			videoItemtmp=(VideoItem) PlayDownloadItemtmp;
			type=2;
			isVdiao = true;
			downloadUrl = videoItemtmp.webURL;
			saveDir=PlayDownloadItem.VIDEO_DIR;
			MobclickAgent.onEvent(this, "Vadiodown");
			StatService.trackCustomEvent(this, "Vadiodown", "");
		}
		
		if (downloadUrl != null) {
			File dir = new File(saveDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		if (PlayDownloadItems.size() < 3) {
			PlayDownloadItem playDownloadItem = new PlayDownloadItem(this);
			String savePath = null;
			if(type==1)
			{
				playDownloadItem.setType(1);
			}else if(type==2){
				playDownloadItem.setType(2);
			}
			if(isVdiao){
				savePath =saveDir +videoItemtmp.name
						+ downloadUrl.substring(downloadUrl.lastIndexOf("."));
				playDownloadItem.fillWithOutScreenAndListInfo(videoItemtmp);
				Log.d("subo", "就是这里---"+savePath);
			}else{
				savePath =saveDir +ringItemtmp.name
						+ downloadUrl.substring(downloadUrl.lastIndexOf("."));
				playDownloadItem.fillWithOutScreenAndListInfo(ringItemtmp);
			}
			
			playDownloadItem.setSavePath(savePath);
			
			PlayDownloadItems.put(savePath, playDownloadItem);
			//playDownloadItemsall.add(playDownloadItem);
			new Thread(new DownloadTask(playDownloadItem)).start();
			InfoDao infodao=new InfoDao(this);
			infodao.insert(playDownloadItem);
			Toast.makeText(this, R.string.downalreadyadd, Toast.LENGTH_SHORT)
			.show();
		} else {
			Toast.makeText(this, R.string.download_alert, Toast.LENGTH_SHORT)
					.show();
		}
		}catch(Exception e){
			Log.d("nnlog", "eeeeeeee"+e.toString());
		}

	}

	static class DownloadTask implements Runnable {
		private PlayDownloadItem PlayDownloadItem;

		public DownloadTask(PlayDownloadItem PlayDownloadItem) {
			this.PlayDownloadItem = PlayDownloadItem;
		}

		public Handler handler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				//PlayDownloadItem.downloadFile();
				Log.i("xj", "size==>" + PlayDownloadItems.size());
				if(!PlayDownloadItem.isPause)
				{
				PlayDownloadItems.remove(PlayDownloadItem.savePath);
				InfoDao infodao=new InfoDao(MyApplication.getInstance());
				infodao.delete(PlayDownloadItem.sid,PlayDownloadItem.savePath);	
				
				Intent intent = new Intent("com.byt.music.downcomplet");
				intent.putExtra("DownloadItemUri", PlayDownloadItem.musicuri);
				intent.putExtra("DownloadItemName", PlayDownloadItem.name);
				MyApplication.getInstance().sendBroadcast(intent);
				insertMusicToDB(MyApplication.getInstance());
				
				}					
//				playDownloadItemsall.remove(PlayDownloadItem);
				Log.w("xj", "size==>" + PlayDownloadItems.size());
			};
		};
		@Override
		public void run() {
			if(PlayDownloadItem.type==2){
				PlayDownloadItem.downloadFileVdio(handler);
			}else{
				PlayDownloadItem.downloadFile();
				//PlayDownloadItem.downloadFile();
				Log.i("xj", "size==>" + PlayDownloadItems.size());
				if(!PlayDownloadItem.isPause)
				{
				PlayDownloadItems.remove(PlayDownloadItem.savePath);
				InfoDao infodao=new InfoDao(MyApplication.getInstance());
				infodao.delete(PlayDownloadItem.sid,PlayDownloadItem.savePath);	
				
				Intent intent = new Intent("com.byt.music.downcomplet");
				intent.putExtra("DownloadItemUri", PlayDownloadItem.musicuri);
				intent.putExtra("DownloadItemName", PlayDownloadItem.name);
				MyApplication.getInstance().sendBroadcast(intent);
				insertMusicToDB(MyApplication.getInstance());
				
				}					
//				playDownloadItemsall.remove(PlayDownloadItem);
				Log.w("xj", "size==>" + PlayDownloadItems.size());
			}
		}

	}
	 private static void insertMusicToDB(Context context) {
	        try {
				Bundle args = new Bundle();
				args.putString("volume", "external");
				Intent intent = new Intent();
				intent.setAction("android.media.IMediaScannerService");
				intent.putExtras(args);
				context.startService(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	public static List<String> getAlldownFile() {
		List<String> allFileString=new ArrayList<String>();
		File file = new File(PlayDownloadItem.MUSIC_DIR);
        File[] f = file.listFiles();
          for (int i = 0; i < f.length; i++)

        {
        	  String tmp=f[i].getPath();
       	  allFileString.add(tmp.substring(tmp.lastIndexOf("/")+1));
        }
       return allFileString;
	}
	public static void isdownedFile(List<BigItem> list) {
		for(BigItem itemtmp:list)
		{
			try {
				RingItem item=itemtmp.ringhomeItems.get(0);
				String savepaht = null;
				if(isMP3File(item.musicuri)){
					try {
						savepaht=PlayDownloadItem.MUSIC_DIR+item.name+item.musicuri.substring(item.musicuri.lastIndexOf("."));
					} catch (Exception e) {
						// TODO: handle exception
					}
				}else{
					try {
						savepaht=PlayDownloadItem.VIDEO_DIR+item.name+item.adesc.substring(item.adesc.lastIndexOf("."));
					} catch (Exception e) {
					}
					//---------------------------------------------------------------------
				}
				File file=new File(savepaht);
				if(file.exists())
				{	
					item.state=2;
				}else{
					item.state=0;
				}
				if( PlayDownloadItems.size()>0)
				{
					PlayDownloadItem downloadItem = PlayDownloadItems.get(savepaht);
					if(downloadItem!=null)
					{
						item.state=1;
					}			
				}
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("nnlog", "异常--"+e);
				
			}
		}
	}
	public static String mVdiaoPath = null;
	public static boolean isVideodownedFile(VideoItem item) {
			try {
				String savepaht=PlayDownloadItem.VIDEO_DIR+item.cTitle+item.webURL.substring(item.webURL.lastIndexOf("."));
				File file=new File(savepaht);
				if(file.exists())
				{
					mVdiaoPath = savepaht;
					return true;
				}else{
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}		
			return false;
	}
	/**
	 * av 和电影 下载状态的判断
	 * @param item
	 * @return
	 */
	public static void isVideoStatdownedFile(List<BigItem> list) {
		for(BigItem bgitem:list){
			try {
				for(int i = 0;i<bgitem.videoItems.size();i++){
					VideoItem item = bgitem.videoItems.get(i);
					
					String savepaht=PlayDownloadItem.VIDEO_DIR+item.cTitle+item.webURL.substring(item.webURL.lastIndexOf("."));
					Log.d("nnlog", "savepaht-----"+savepaht);
					Log.d("nnlog", "savepaht-----"+savepaht);
					if(savepaht!=null){
						File file=new File(savepaht);
						if(file.exists())
						{
							item.state = 2;
						}else{
							item.state = 0;
						}
						if( PlayDownloadItems.size()>0)
						{
							PlayDownloadItem downloadItem = PlayDownloadItems.get(savepaht);
							if(downloadItem!=null)
							{
								item.state=1;
							}			
						}
					}
				}
			} catch (Exception e) {
				Log.d("nnlog", "错误---"+e);
				e.printStackTrace();
			}		
		}
	}
	public static void deletedownfile(PlayDownloadItem item) {
		PlayDownloadService.PlayDownloadItems.remove(item.savePath);
		File file=new File(item.savePath);
		if(file.exists()){
			file.delete();
		}	
		InfoDao infodao=new InfoDao(MyApplication.getInstance());
		infodao.delete(item.sid,item.savePath);
		MyApplication.getInstance().sendBroadcast(new Intent("com.byt.music.downcomplet"));
	}
	public static void deleteAll() {
		Iterator<PlayDownloadItem> it=PlayDownloadService.PlayDownloadItems.values().iterator();
		List<PlayDownloadItem> list=new ArrayList<PlayDownloadItem>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		for(PlayDownloadItem playdown:list){
			File file=new File(playdown.savePath);
			if(file.exists()){
				file.delete();
			}
			InfoDao infodao=new InfoDao(MyApplication.getInstance());
			infodao.delete(playdown.sid,playdown.savePath);
		}
		PlayDownloadService.PlayDownloadItems.clear();
		MyApplication.getInstance().sendBroadcast(new Intent("com.byt.music.downcomplet"));
	}
	public static void pauseAll() {
		Iterator<PlayDownloadItem> it=PlayDownloadService.PlayDownloadItems.values().iterator();
		List<PlayDownloadItem> list=new ArrayList<PlayDownloadItem>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		for(PlayDownloadItem playdown:list){
			playdown.setPause(true);
		}
	}
	public static void startAll() {
		Iterator<PlayDownloadItem> it=PlayDownloadService.PlayDownloadItems.values().iterator();
		List<PlayDownloadItem> list=new ArrayList<PlayDownloadItem>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		for(PlayDownloadItem playdown:list){
			playdown.setPause(false);
			new Thread(new DownloadTask(playdown)).start();
		}
	}
	/**
	 * 获取文件类型
	 */
	public static Boolean isMP3File(String url){
		return url.endsWith("mp3");
	}
}
