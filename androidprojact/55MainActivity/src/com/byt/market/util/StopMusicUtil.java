package com.byt.market.util;

import com.byt.market.Constants;
import com.byt.market.mediaplayer.music.PlayMusicService;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;

public class StopMusicUtil {
	
	
	public static void stopMusic(Context context){
		//获取音乐播放硬件焦点
//		AudioManager mAudioManager = (AudioManager) context  
//		        .getSystemService(Context.AUDIO_SERVICE);  
//		if (mAudioManager != null) {  
//		    try {  
//		        mAudioManager.requestAudioFocus(null,  
//		                AudioManager.STREAM_MUSIC,  
//		                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);  
//		    } catch (Exception ex) {  
//		  
//		    }  
//		}  
//		Intent i = new Intent("com.android.music.musicservicecommand");  
//		i.putExtra("command", "pause");  
//		context.sendBroadcast(i);
		
		//Intent.ACTION_NEW_OUTGOING_CALL
		Intent intent = new Intent();
		intent.setAction(Constants.stopMusicBrodcast);
		context.sendBroadcast(intent);
		
	//	context.sendBroadcast(new Intent(Intent.ACTION_NEW_OUTGOING_CALL, Uri.parse("file://" + Environment.getExternalStorageDirectory())));  
	}
}
