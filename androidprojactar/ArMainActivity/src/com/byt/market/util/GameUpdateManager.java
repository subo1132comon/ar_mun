package com.byt.market.util;

import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.byt.ar.R;
import com.byt.market.MyApplication;
import com.byt.market.Constants;
import com.byt.market.Constants.Settings;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.AppItem;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.download.DownloadContent.DownloadTask;

public class GameUpdateManager implements TaskListener{

	public static final String PREF_GAME_UPDATE = "gameUpdate";
	public static final String PREF_GAME_UPDATE_KEY = "lastUpdate";
	private static final long REPEAT_ALARM_TIME = 30 * Util.MIN;
	private static final long PUSH_BETWEEN = 30 * Util.MIN; // one day
	private static final long LAUNCHE_MIN_BETWEEN = 30 * Util.MIN;
	
	private ProtocolTask mTask;
	
	private static GameUpdateManager mGameUpdateManager;
	
	private SharedPreferences mSharedPreferences;
	
	private boolean mRepeatSeted;
	private GameUpdateManager(){	
	}
	private Object tag() {
		return getClass().getSimpleName();
	}
	public static GameUpdateManager getInstance(){
		if(mGameUpdateManager == null) {
			mGameUpdateManager = new GameUpdateManager();
		}
		return mGameUpdateManager;
	}

	public void tryCheckGameUpdate(Context context,boolean push){
        Log.i("appupdate","tryCheckGameUpdate");
		if(mSharedPreferences == null){
			mSharedPreferences = context.getSharedPreferences(PREF_GAME_UPDATE, Context.MODE_PRIVATE);
		}
		long lastUpdate = mSharedPreferences.getLong(PREF_GAME_UPDATE_KEY, 0);
       // Log.i("appupdate","lastUpdate = "+);
		final long between = push ? PUSH_BETWEEN : LAUNCHE_MIN_BETWEEN;
		if(System.currentTimeMillis() - lastUpdate > between || System.currentTimeMillis() - lastUpdate <= 0 ){
			if(mTask == null){
				mTask = new ProtocolTask(context);
				mTask.setListener(this);
				mTask.execute(getRequestUrl(), getPostData(), tag(), getHeader(context));
                Log.i("appupdate","execute");
			}
		}
		setRepeatUpdate(context);
	}
	
	 private void setRepeatUpdate(Context context){
		 if(mRepeatSeted){
			 return;
		 }

		 Intent intent=new Intent(Constants.ACTION_REQUEST_GAME_UPDATE_CHECK);  
	     PendingIntent refreshIntent=PendingIntent.getBroadcast(context, 1 , intent, PendingIntent.FLAG_UPDATE_CURRENT);  
	     AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	     alarm.setRepeating(AlarmManager.RTC, REPEAT_ALARM_TIME, REPEAT_ALARM_TIME , refreshIntent);
	     mRepeatSeted = true;
	}
	
	public HashMap<String, String> getHeader(Context context) {
		String model = Util.mobile;
		String imei = Util.imie;
    	String vcode = Util.vcode;
    	String channel = Util.channel;
		if (model == null)
			model = Util.getModel();
		if (imei == null)
			imei = Util.getIMEI(context);
		if (vcode == null)
			vcode = Util.getVcode(context);
		if (TextUtils.isEmpty(channel)){
			channel = Util.getChannelName(context);
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("model", model);
		map.put("channel", channel);
		return map;
	}
	
	private String getPostData(){
		List<AppItem> items = DownloadTaskManager.getInstance().getAllDownloadItem(false);
		StringBuilder sb = new StringBuilder();
		for (AppItem item : items) {
			if(item.state == AppItem.STATE_INSTALLED_NEW){
				sb.append(item.pname).append(":")
				.append(item.vcode).append("|");
			}
		}
        Log.i("appupdate","GameUpdateManager getPostData sb = "+sb.toString());
		return sb.toString();
	}
	
	private Object getRequestUrl() {
        return Constants.LIST_URL + "?qt=" + Constants.UPDATE;
	}
	@Override
	public void onNetworkingError() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNoNetworking() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		// TODO Auto-generated method stub
        Log.i("appupdate","touch GameUpdateManager onPostExecute");
		if (bytes != null) {
			String datas = new String(bytes);
            Log.i("appupdate","touch GameUpdateManager onPostExecute datas = "+datas);
			try {
				final JSONObject obj = new JSONObject(datas);
				if(obj!=null){
					final List<AppItem> list = JsonParse.parseUpdateGames(obj
							.getJSONArray("data"));
					DownloadTaskManager.getInstance().updateAfterUpdateGamesGot(list);
	                Log.i("appupdate","JsonParse.parseResultStatus(obj) = "+JsonParse.parseResultStatus(obj));
					if(JsonParse.parseResultStatus(obj) == 1){
						mSharedPreferences.edit().putLong(PREF_GAME_UPDATE_KEY, System.currentTimeMillis()).commit();
						final int updateCount = DownloadTask.getNeedUpdateCount();
	                    Log.i("appupdate","updateCount = "+updateCount);
						if(list != null && updateCount > 0 && Settings.userUpdateNotify){
							SysNotifyUtil.notifyGameListUpdate(MyApplication.getInstance(), R.drawable.icon, updateCount);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mTask = null;
	}
	
	
}
