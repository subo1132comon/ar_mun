package com.byt.market.mediaplayer.music;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.data.CateItem;
import com.byt.market.data.RingItem;
import com.byt.market.mediaplayer.data.PlayDownloadItem;
import com.byt.market.tools.LogCart;

public class PlayMusicService extends Service {
	private MediaPlayer mediaPlayer;
	private File downloadingMediaFile;
	public static final String CUR_PLAY_LIST_KEY = "playListKey";
	public static final String CUR_PLAY_ITEM_POSITION_KEY = "playItemPositionKey";
	public static final String START_PLAY_MUSIC_ACTION = "start_play_music_action";
	public static final String COMPLETE_PLAY_MUSIC_ACTION = "complete_play_music_action";
	public static final String PREPARED_PLAY_MUSIC_ACTION = "prepared_play_music_action";
	public static final String NEXT_PLAY_MUSIC_ACTION = "next_play_music_action";
	public static final String PLAY_MUSIC_ACTION = "play_music_action";
	public static final String NOTI_PLAY_MUSIC_ACTION = "noti_play_music_action";
	public static final String NOTIF_CANCAL_ACTION = "notif_cancal_action";
	public static final int ORDER_MODE = 1;
	public static final int SINGLE_MODE = 2;
	public static final int ALL_MODE = 3;
	private ArrayList<RingItem> curPlayList = new ArrayList<RingItem>();
	private FileInputStream fis;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			isLoading = false;
			if (isInterrupted && !isStopService) {
				isInterrupted = false;
				playType();
			}

		};
	};
	private int totalKbRead;
	public final static int INTIAL_KB_BUFFER = 100;
	private boolean isInterrupted;
	private int counter;
	public String mediaUrl;
	public static final String MUSIC_PATH_KEY = "music_path_key";
	public boolean isLoading = false;
	public boolean isMediaPlayerReset = false;
	public boolean isHandlePause = false;
	public final static int COMPLETE = 1;
	private RingItem curRingItem;
	private boolean isStopService = false;
	private int curPlayMode = ORDER_MODE;
	private CateItem curPlayCate;
	private boolean isring=false;
	private Binder binder = new IPlayback.Stub() {

		public void next() throws RemoteException {
			if (curPlayList != null && curPlayList.size() > 0) {
				try {
					int index = curPlayList.indexOf(curRingItem);
					index++;
					if (index < curPlayList.size()) {
						playMusic(curPlayList.get(index));
					} else {
						playMusic(curPlayList.get(0));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (curPlayList.size() == 1) {
					playMusic(curPlayList.get(0));
				}
			}
			notifacationEvent();
		}

		public void pause() throws RemoteException {
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
			notifacationEvent();
		}

		public void start() throws RemoteException {
			if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
				mediaPlayer.start();
			}
			notifacationEvent();
		}

		public void stop() throws RemoteException {
			if (mediaPlayer != null) {
				mediaPlayer.stop();
			}
		}

		public void release() throws RemoteException {
			if (mediaPlayer != null) {
				mediaPlayer.release();
			}
		}

		public void previous() throws RemoteException {
			if (curPlayList != null && curPlayList.size() > 0) {
				try {
					int index = curPlayList.indexOf(curRingItem);
					index--;
					if (index >= 0) {
						playMusic(curPlayList.get(index));
					} else {
						playMusic(curPlayList.get(curPlayList.size() - 1));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (curPlayList.size() == 1) {
					playMusic(curPlayList.get(0));
				}
			}
			notifacationEvent();
		}

		public int getDuration() throws RemoteException {
			if (mediaPlayer != null) {
				return mediaPlayer.getDuration();
			}
			return 0;
		}

		public int getTime() throws RemoteException {
			return 0;
		}

		public void seek(int time) throws RemoteException {
			if (mediaPlayer != null) {
				mediaPlayer.seekTo(time);
			}
		}

		public boolean isPlaying() throws RemoteException {
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				return true;
			}
			return false;
		}

		@Override
		public int getCurrentPosition() throws RemoteException {
			if (mediaPlayer != null) {
				return mediaPlayer.getCurrentPosition();
			}
			return 0;
		}

		@Override
		public int getDownloadFileSize() throws RemoteException {
			return allTotalBytes;
		}

		@Override
		public int getCacheFileSize() throws RemoteException {
			return totalBytesReaded;
		}

		@Override
		public void setIsHandlePause() throws RemoteException {
			isHandlePause = !isHandlePause;
		}

		@Override
		public String getCurMusicPath() throws RemoteException {
			return mediaUrl;
		}

		@Override
		public String getCurMusicName() throws RemoteException {
			if (curRingItem != null) {
				return curRingItem.name;
			}
			return null;
		}

		@Override
		public String getCurMusicAuthor() throws RemoteException {
			if (curRingItem != null) {
				return curRingItem.user;
			}
			return null;
		}

		@Override
		public void setPlayMode(int mode) throws RemoteException {
			curPlayMode = mode;
		}

		@Override
		public int getPlayMode() throws RemoteException {
			return curPlayMode;
		}

		@Override
		public String getMusicLogo() throws RemoteException {
			if (curRingItem != null) {
				return curRingItem.logo;
			}
			return null;
		}

		@Override
		public int getMusicCateId() throws RemoteException {
			if (curPlayCate != null) {
				return curPlayCate.id;
			}
			return 0;
		}

		@Override
		public String getMusicCateLogoUrl() throws RemoteException {
			if (curPlayCate != null) {
				return curPlayCate.ImagePath;
			}
			return null;
		}

		@Override
		public String getMusicCateName() throws RemoteException {
			if (curPlayCate != null) {
				return curPlayCate.cTitle;
			}
			return null;
		}

		@Override
		public String getMusicCateUpdateTime() throws RemoteException {
			if (curPlayCate != null) {
				return curPlayCate.cDesc;
			}
			return null;
		}

		@Override
		public int getMusicPlayListCount() throws RemoteException {
			if (curPlayList != null && curPlayList.size() > 0) {
				return curPlayList.size();
			}
			return 0;
		}

	};

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		try {
			ArrayList<RingItem> playList = intent
					.getParcelableArrayListExtra(CUR_PLAY_LIST_KEY);
			if (playList != null) {
				curPlayList = playList;
			}
			RingItem ringItem = intent
					.getParcelableExtra(CUR_PLAY_ITEM_POSITION_KEY);
			try {
				curPlayCate = intent.getParcelableExtra("app");
			} catch (Exception e) {
				e.printStackTrace();
			}
			playMusic(ringItem);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void playMusic(RingItem ringItem) {
		if (ringItem != null) {
			this.curRingItem = ringItem;
			mediaUrl = ringItem.musicuri;
			String savepath = PlayDownloadItem.MUSIC_DIR
					+ curRingItem.name
					+ curRingItem.musicuri.substring(curRingItem.musicuri
							.lastIndexOf("."));
			if (curRingItem.musicuri.contains(PlayDownloadItem.MUSIC_DIR)
					&& new File(mediaUrl).exists()) {
				try {
					String selectCondition = MediaStore.Audio.Media.DATA
							+ " = ?";
					String[] args = new String[] { mediaUrl };
					curRingItem = getNativeMusicInfo(selectCondition, args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (new File(savepath).exists()) {
				this.mediaUrl = savepath;
			}
			sendAlertBroadcast(START_PLAY_MUSIC_ACTION);
			if (mediaUrl != null) {
				allTotalBytes = 0;
				totalBytesReaded = 0;
				try {
					if (mediaPlayer != null) {
						mediaPlayer.stop();
						mediaPlayer.reset();
					}
					if (fis != null) {
						fis.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				isHandlePause = false;
				if (isLoading) {
					interrupt();
				} else {
					playType();
				}

			}
		}
	}

	private void playType() {
		try {
			if (mediaUrl.contains(Environment.getExternalStorageDirectory()
					.getPath())) {
				downloadingMediaFile = new File(mediaUrl);
				startMediaPlayer();
			} else {
				startStreaming(mediaUrl);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startStreaming(final String mediaUrl) throws IOException {
		Runnable r = new Runnable() {
			public void run() {
				try {
					handler.removeMessages(COMPLETE);
					isMediaPlayerReset = true;
					isLoading = true;
					LogCart.Log("startStreaming-----播放");
					downloadAudioIncrement(mediaUrl);
				} catch (Exception e) {
					Log.e("PlayMusicService",
							"Unable to initialize the MediaPlayer for fileUrl="
									+ mediaUrl, e);
					isLoading = false;
				}
				handler.sendEmptyMessage(COMPLETE);
			}
		};
		new Thread(r).start();
	}

	int totalBytesReaded;
	int allTotalBytes;

	public void downloadAudioIncrement(String mediaUrl) throws IOException {
		URLConnection cn = new URL(mediaUrl).openConnection();
		cn.connect();
		InputStream stream = cn.getInputStream();
		if (stream == null) {
			Log.e("PlayMusicService",
					"Unable to create InputStream for mediaUrl:" + mediaUrl);
		}
		downloadingMediaFile = new File(getCacheDir(), "downloadingMedia.dat");
		if (downloadingMediaFile.exists()) {
			downloadingMediaFile.delete();
		}
		allTotalBytes = cn.getContentLength();
		RandomAccessFile out = new RandomAccessFile(downloadingMediaFile, "rw");
		out.setLength(allTotalBytes);
		byte buf[] = new byte[16384];
		totalBytesReaded = 0;
		do {
			int numread = stream.read(buf);
			if (numread <= 0)
				break;
			out.write(buf, 0, numread);
			totalBytesReaded += numread;
			totalKbRead = totalBytesReaded / 1000;
			//LogCart.Log("testMediaBuffer--------播放");
			testMediaBuffer();
		} while (validateNotInterrupted());
		try {
			
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (validateNotInterrupted()) {
			fireDataFullyLoaded();
		}
	}

	private MediaPlayer createMediaPlayer(File mediaFile) throws IOException {
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
		} else {
			mediaPlayer.reset();
		}

		mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e("PlayMusicService", "Error in MediaPlayer: (" + what
						+ ") with extra (" + extra + ")");
				mediaPlayer.setOnCompletionListener(null);
				return false;
			}
		});
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer player) {
				Log.i("hxj", "com dt==>" + player.getDuration() + " ct==>"
						+ player.getCurrentPosition());
				if (!isLoading) {
					sendBroadcast(new Intent(COMPLETE_PLAY_MUSIC_ACTION));
					setPlayShowMode(curPlayMode);
				}
			}
		});
		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer arg0) {
				sendBroadcast(new Intent(PREPARED_PLAY_MUSIC_ACTION));
				notifacationEvent();

			}
		});
		fis = new FileInputStream(mediaFile);

		mediaPlayer.setDataSource(fis.getFD());
		mediaPlayer.prepare();
		//mediaPlayer.prepareAsync();
		return mediaPlayer;
	}

	private void testMediaBuffer() {
		Runnable updater = new Runnable() {
			public void run() {
				try {
					if (!isStopService) {
						if (mediaPlayer == null || isMediaPlayerReset) {
							if (totalKbRead >= INTIAL_KB_BUFFER) {
								try {
									isMediaPlayerReset = false;
									startMediaPlayer();
								} catch (Exception e) {
								}
							}
						} else if (mediaPlayer != null
								&& !mediaPlayer.isPlaying() && !isHandlePause) {
							try {
								int secondaryProgress = totalBytesReaded * 100
										/ allTotalBytes;
								int allTime = mediaPlayer.getDuration();
								int curTime = mediaPlayer.getCurrentPosition();
								int progress = curTime * 100 / allTime;
								if (secondaryProgress > progress) {
									mediaPlayer.seekTo(mediaPlayer
											.getCurrentPosition());
									 if(!isring)
									 {	 
									mediaPlayer.start();
									 }
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		handler.post(updater);
	}

	private boolean validateNotInterrupted() {
		if (isInterrupted) {
			try {
				if (mediaPlayer != null) {
					mediaPlayer.pause();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		} else {
			return true;
		}
	}

	private void fireDataFullyLoaded() {
		Runnable updater = new Runnable() {
			public void run() {
				downloadingMediaFile.delete();
			}
		};
		handler.post(updater);
	}

	private void startMediaPlayer() {
		
		try {
			Thread.currentThread().sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			mediaPlayer = createMediaPlayer(downloadingMediaFile);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			if (!isHandlePause)
			{
				   mediaPlayer.start();
			}
				
		} catch (Exception e) {
			
		}
	}

	public void interrupt() {
		this.isInterrupted = true;
	}

	private void sendAlertBroadcast(String action) {
		Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			cancelNotification();
		} catch (Exception e) {
			e.printStackTrace();
		}
		isStopService = true;
		interrupt();
		try {
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			curPlayList.clear();
			curPlayList = null;
			handler.removeMessages(COMPLETE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			unregisterReceiver(broadcastReceiver);
			unregisterReceiver(phonebroadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private RingItem getNativeMusicInfo(String selectCondition, String[] args) {
		RingItem ringItem = null;
		try {
			ContentResolver resolver = getContentResolver();
			Cursor cursor = resolver.query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
							MediaStore.Audio.Media._ID,
							MediaStore.Audio.Media.TITLE,
							MediaStore.Audio.Media.ARTIST,
							MediaStore.Audio.Media.DURATION,
							MediaStore.Audio.Media.DATA,
							MediaStore.Audio.Media.ALBUM }, selectCondition,
					args, MediaStore.Audio.Media._ID);
			if (cursor != null && cursor.getCount() > 0) {
				ringItem = new RingItem();
				cursor.moveToFirst();
				String title = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.TITLE));
				ringItem.name = title;
				ringItem.user = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				ringItem.musicuri = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.DATA));
				ringItem.spename = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.ALBUM));

			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ringItem;
	}

	int notification_id = 19172439;
	NotificationManager nm;
	Notification notification;

	private void showNotification() {
		if (nm != null)
			nm.notify(notification_id, notification);
	}

	private void cancelNotification() {
		if (nm != null)
			nm.cancel(notification_id);
	}

	private void notifacationEvent() {
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification(R.drawable.icon,
				getString(R.string.app_name), System.currentTimeMillis());
		notification.contentView = new RemoteViews(getPackageName(),
				R.layout.notification);

//		notification.contentView.setTextViewText(R.id.noti_filename,
//				curRingItem.name);
		Intent notificationIntent = new Intent(this, PlayMusicActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.contentIntent = contentIntent;

		Intent playclick = new Intent();
		playclick.setAction(PLAY_MUSIC_ACTION);
		if (mediaPlayer.isPlaying()) {
			notification.contentView.setInt(R.id.noti_btn_play,
					"setBackgroundResource",
					R.drawable.byt_noti_pausebtnselector);
		} else {
			notification.contentView.setInt(R.id.noti_btn_play,
					"setBackgroundResource",
					R.drawable.byt_noti_playbtnselector);
		}
		//Log.d("nnlog", "notf--url--"+curRingItem.iconUrl);
		//Uri url = Uri.parse(curRingItem.iconUrl);
		//notification.contentView.setImageViewUri(R.id.play_ic, url);
		PendingIntent playpengingitent = PendingIntent.getBroadcast(this, 0,
				playclick, 0);

		Intent nextclick = new Intent();
		nextclick.setAction(NEXT_PLAY_MUSIC_ACTION);
		PendingIntent nextpengingitent = PendingIntent.getBroadcast(this, 0,
				nextclick, 0);

		notification.contentView.setOnClickPendingIntent(R.id.noti_btn_play,
				playpengingitent);
		notification.contentView.setOnClickPendingIntent(R.id.noti_btn_next,
				nextpengingitent);

		showNotification();
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (PLAY_MUSIC_ACTION.equals(intent.getAction())) {
					if (mediaPlayer.isPlaying()) {
						mediaPlayer.pause();
					} else {
						mediaPlayer.start();
					}
					notifacationEvent();
					isHandlePause = !isHandlePause;
					sendBroadcast(new Intent(NOTI_PLAY_MUSIC_ACTION));
				} else if (NEXT_PLAY_MUSIC_ACTION.equals(intent.getAction())) {
					if (curPlayList != null && curPlayList.size() > 0) {
						int index = curPlayList.indexOf(curRingItem);
						index++;
						if (index < curPlayList.size()) {
							playMusic(curPlayList.get(index));
						} else {
							playMusic(curPlayList.get(0));
						}
					}
					notifacationEvent();
				} else if(NOTIF_CANCAL_ACTION.equals(intent.getAction())){
					cancelNotification();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	//add by bobo 
	private BroadcastReceiver stopbroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
			if(Constants.stopMusicBrodcast.equals(intent.getAction()))
				{
				//Log.d("mylog", "暂停 音乐---------------");
					if(mediaPlayer!=null&&mediaPlayer.isPlaying())
					{
					mediaPlayer.pause();
					isring=true;
					}
				}
			else{

					if(mediaPlayer.isPlaying())
					{
						mediaPlayer.pause();
						isring=true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	private BroadcastReceiver phonebroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
			if(Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction()))
				{
					if(mediaPlayer!=null&&mediaPlayer.isPlaying())
					{
					mediaPlayer.pause();
					isring=true;
					}
				}
			else{

					if(mediaPlayer.isPlaying())
					{
						mediaPlayer.pause();
						isring=true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	 PhoneStateListener listener=new PhoneStateListener(){
		 
		  @Override
		  public void onCallStateChanged(int state, String incomingNumber) {
		   super.onCallStateChanged(state, incomingNumber);
		   try{
		   switch(state){
		   case TelephonyManager.CALL_STATE_IDLE:
			   if(isring)
			   {
				   if(mediaPlayer!=null)
				   {
				   mediaPlayer.start();
				   }
				   isring=false;
			   }
		    break;
		   case TelephonyManager.CALL_STATE_OFFHOOK:

		    break;
		   case TelephonyManager.CALL_STATE_RINGING:
			   if(mediaPlayer!=null&&mediaPlayer.isPlaying())
				{
					mediaPlayer.pause();
					isring=true;
				}

		    break;
		   }
		   }catch(Exception e){}
		  }
		 };
	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(PLAY_MUSIC_ACTION);
		filter.addAction(NEXT_PLAY_MUSIC_ACTION);
		filter.addAction(NOTIF_CANCAL_ACTION);
		registerReceiver(broadcastReceiver, filter);
		IntentFilter filterphone = new IntentFilter();
		filterphone.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		
		IntentFilter stopintent = new IntentFilter(Constants.stopMusicBrodcast);
		registerReceiver(stopbroadcastReceiver, stopintent);
		registerReceiver(phonebroadcastReceiver, filterphone);
		  TelephonyManager tm = (TelephonyManager)getSystemService(Service.TELEPHONY_SERVICE);  
		   tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		  
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initReceiver();
	}

	private void setPlayShowMode(int mode) {
		try {
			switch (mode) {
			case PlayMusicService.ORDER_MODE:
				if (curPlayList != null && curPlayList.size() > 0) {
					int index = curPlayList.indexOf(curRingItem);
					index++;
					if (index < curPlayList.size()) {
						playMusic(curPlayList.get(index));
					}
				}
				break;
			case PlayMusicService.SINGLE_MODE:
				sendBroadcast(new Intent(PREPARED_PLAY_MUSIC_ACTION));
				mediaPlayer.start();
				break;
			case PlayMusicService.ALL_MODE:
				if (curPlayList != null && curPlayList.size() > 0) {
					int index = curPlayList.indexOf(curRingItem);
					index++;
					if (index < curPlayList.size()) {
						playMusic(curPlayList.get(index));
					} else {
						playMusic(curPlayList.get(0));
					}
				}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
