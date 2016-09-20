package com.byt.market.mediaplayer.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.byt.ar.R;
import com.byt.market.mediaplayer.music.PlayMusicActivity;
import com.byt.market.mediaplayer.nover.NovelActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.text.BoringLayout;
import android.util.Log;
import android.widget.RemoteViews;

public class RediaoService extends Service{

	private Vector<PlaylistFile> playlistItems;
	private MediaPlayer mediaPlayer;
	int currentPlaylistItemNumber = 0;
	private String m_musicpath = "";
	public static final String NEXT_PLAY_RADIO_ACTION = "next_play_radio_action";
	public static final String PLAY_RADIO_ACTION = "play_radio_action";
	public static final String PREPARED_PLAYRADIO_ACTION = "prepared_action";
	public static final String IS_PLAYING_ACTION = "playing_action";
	public static final String STOP_ACTION = "stop_action";
	public static final String NOTIF_STOP_ACTION = "notif_stop_action";
	private String m_name = "";
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		try {
			m_musicpath = intent.getStringExtra("url");
			m_name = intent.getStringExtra("name");
			if(m_musicpath==null||m_name==null){
				m_musicpath = "";
				m_name = "";
			}
			playlistItems = new Vector();
			parsePlaylistFile();
			mediaPlayer.reset();
		} catch (Exception e) {
			// TODO: handle exception
		}
//		playPlaylistItems();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		initData();
		registRecive();
	}
	
	private void registRecive(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(STOP_ACTION);
		filter.addAction(PLAY_RADIO_ACTION);
		registerReceiver(new StatRceive(), filter);
	}
	
	private void initData(){
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				mediaPlayer.reset();
				if (playlistItems.size() > currentPlaylistItemNumber + 1) {
					currentPlaylistItemNumber++;
					String path = ((PlaylistFile) playlistItems
							.get(currentPlaylistItemNumber)).getFilePath();
					Log.d("nnlog",path+"-------path");
					try {
						mediaPlayer.setDataSource(path);
						mediaPlayer.prepareAsync();
						
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else{
					parsePlaylistFile();
					mediaPlayer.reset();
					playPlaylistItems();
				}
				
			}
		});
	}
	
	private void parsePlaylistFile() {
		
		new Thread(){
			public void run() {
				HttpClient httpClient = new DefaultHttpClient();
				m_musicpath = m_musicpath.replaceAll(" ","");
				m_musicpath = m_musicpath.replaceAll("\n","");
				m_musicpath = m_musicpath.replaceAll("\\s*", "");
				HttpGet getRequest = new HttpGet(m_musicpath);
				try {
					HttpResponse httpResponse = httpClient.execute(getRequest);
					if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
						Log.v("HTTP ERROR", httpResponse.getStatusLine()
								.getReasonPhrase());
					} else {
						InputStream inputStream = httpResponse.getEntity().getContent();
						BufferedReader bufferedReader = new BufferedReader(
								new InputStreamReader(inputStream));
						String line;
						while ((line = bufferedReader.readLine()) != null) {
							Log.v("PLAYLISTLINE", "ORIG:" + line);
							if (line.startsWith("#")) {
							} else if (line.length() > 0) {
								String filePath = "";
								if (line.startsWith("http://")) {
									filePath = line;
								} else {
									filePath = getRequest.getURI().resolve(line)
											.toString();
								}
								PlaylistFile playlistFile = new PlaylistFile(filePath);
								playlistItems.add(playlistFile);
							}
						}
						playPlaylistItems();
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			};
		}.start();
		
	}
	
	int notification_id = 19172435;
	NotificationManager nm;
	Notification notification;
	
	private void notifacationEvent() {
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification(R.drawable.icon,
				getString(R.string.app_name), System.currentTimeMillis());
		notification.contentView = new RemoteViews(getPackageName(),
				R.layout.notification);

//		notification.contentView.setTextViewText(R.id.noti_filename,
//				curRingItem.name);
//		Intent notificationIntent = new Intent(this, NovelActivity.class);
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//				notificationIntent, 0);
//		notification.contentIntent = contentIntent;

		Intent playclick = new Intent();
		playclick.setAction(PLAY_RADIO_ACTION);
		if (mediaPlayer.isPlaying()) {
			notification.contentView.setInt(R.id.noti_btn_play,
					"setBackgroundResource",
					R.drawable.byt_noti_pausebtnselector);
		} else {
			notification.contentView.setInt(R.id.noti_btn_play,
					"setBackgroundResource",
					R.drawable.byt_noti_playbtnselector);
		}
		PendingIntent playpengingitent = PendingIntent.getBroadcast(this, 0,
				playclick, 0);

//		Intent nextclick = new Intent();
//		nextclick.setAction(NEXT_PLAY_RADIO_ACTION);
//		PendingIntent nextpengingitent = PendingIntent.getBroadcast(this, 0,
//				nextclick, 0);

		notification.contentView.setOnClickPendingIntent(R.id.noti_btn_play,
				playpengingitent);
//		notification.contentView.setOnClickPendingIntent(R.id.noti_btn_next,
//				nextpengingitent);

		showNotification();
	}
	
	private void showNotification() {
		if (nm != null)
			nm.notify(notification_id, notification);
	}

	private void cancelNotification() {
		if (nm != null)
			nm.cancel(notification_id);
	}
	
	private class StatRceive extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if(STOP_ACTION.equals(arg1.getAction())){
				mediaPlayer.pause();
				cancelNotification();
			}else if(PLAY_RADIO_ACTION.equals(arg1.getAction())) {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
				} else {
					mediaPlayer.start();
				}
				sendBroadcast(new Intent(NOTIF_STOP_ACTION));
				cancelNotification();
			}
		}
		
	}
	
	private class MyOnPreparedListener implements OnPreparedListener{

		@Override
		public void onPrepared(MediaPlayer arg0) {
//			if(!mediaPlayer.isPlaying()){
//				notifacationEvent();
//			}
			mediaPlayer.start();
			Thread th = new Thread(new Scanplaying());
			th.start();
		}
		
	}
	// 监测是否正在播放
	class Scanplaying implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!mediaPlayer.isPlaying()){
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Log.d("nnlog", "监测是否正在播放");
			Intent intent = new Intent(IS_PLAYING_ACTION);
			intent.putExtra("name", m_name);
			sendBroadcast(intent);
			notifacationEvent();
		}
		
	}
	
	private void playPlaylistItems() {
		currentPlaylistItemNumber = 0;
		if (playlistItems.size() > 0) {
			String path = ((PlaylistFile) playlistItems
					.get(currentPlaylistItemNumber)).getFilePath();
			try {
				mediaPlayer.setDataSource(path);
				mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
				mediaPlayer.prepareAsync();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class PlaylistFile {
		String filePath;

		public PlaylistFile(String _filePath) {
			filePath = _filePath;
		}

		public void setFilePath(String _filePath) {
			filePath = _filePath;
		}

		public String getFilePath() {
			return filePath;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayer = null;
		try {
			cancelNotification();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
