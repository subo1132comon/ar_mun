package com.byt.market.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.data.CateItem;
import com.byt.market.data.HomeItemBean;
import com.byt.market.data.UserData;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.LiveActivity;
import com.byt.market.mediaplayer.VideoActivity;
import com.byt.market.mediaplayer.ui.MovieFragment;
import com.byt.market.qiushibaike.JokeActivity;
import com.byt.market.qiushibaike.news.NewsActivity;
import com.byt.market.receiver.VdioinstensReciver;
import com.byt.market.ui.HomeFragment;
import com.byt.market.util.NotifaHttpUtil.NotifaHttpResalout;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.DownloadManager.Request;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

public class NotifalUtile {
	
	
	public static void formNotifcation(final Context context,JSONObject json){
		
		if(json!=null){
				try {
					if(!json.isNull("sid")){
						String url = "http://122.155.202.149:8022/Joke/v1.php?qt=Push42&sid="+json.getString("sid");
						NotifaHttpUtil.getJson(url, new NotifaHttpResalout() {
							
							@Override
							public void reaslout(String json) {
								try {
									Notifal(context, new JSONObject(json));
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						});
					}					
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}
		
		final Handler mhandler = new Handler(){
			public void handleMessage(Message msg) {
				String pricestring = String.format(context.getString(R.string.toast_login_succes2), msg.arg1);
				Toast.makeText(context, pricestring,
						Toast.LENGTH_LONG).show();
			};
		};
		
		//请求55服务器 
	    String shareurl = Constants.APK_URL+"v1.php?qt=reward&uid="+
	    		MyApplication.getInstance().getUser().getUid()+"&type=push";
	    NotifaHttpUtil.getJson(shareurl, new NotifaHttpResalout() {
			
			@Override
			public void reaslout(String json) {
				try {
					getShareResout(new JSONObject(json),mhandler);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		//积分
		
		
	}
	
	public static void getShareResout(JSONObject json,Handler handler){
		UserData user = new UserData();
		int resultStatus;
		try {
			resultStatus = json.getInt("resultStatus");
			if (resultStatus == 1) {
				int allcount = json.getInt("allCount");
				if (allcount == 0) {
					// 璇佹槑鐢ㄦ埛鍚嶆垨瀵嗙爜鏈夐棶棰�
				} else {
					JSONObject result = json.getJSONObject("data");
					user = JsonParse.parseUserInfo(result);
					String price = null;
					if(!result.isNull("PRICE")){
						price = result.getString("PRICE");
					}
					if(Integer.parseInt(price)>0){
						MyApplication.getInstance().setUser(user);
						Message msg = handler.obtainMessage();
						msg.what = 129;
						msg.arg1 = Integer.parseInt(price);
						handler.sendMessage(msg);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//通知
	@SuppressLint("NewApi")
	public static void Notifal(Context context,JSONObject json){
		//StatService.trackCustomEvent(context,"Notification");//腾讯
		//MobclickAgent.onEvent(context,"Notification");//友盟
		HomeItemBean beana  = null;
		if(json!=null){
			try {
				if(json.getInt("resultStatus")==1){
					if(!json.isNull("allCount")){
						if(json.getInt("allCount")==0){
							//参数错误 直接启动activity即可
							Intent cintent = new Intent();
							if(isApplicationBroughtToBackground(context)){
								cintent = context.getPackageManager().
					                    getLaunchIntentForPackage("com.byt.market");
								cintent.setFlags(
					                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
							}else{
								cintent.setClass(context, MainActivity.class);
							}
							context.startActivity(cintent);
						}else{
							if(!json.isNull("data")){
								JSONArray array = json.getJSONArray("data");
								for(int i = 0;i<array.length();i++){
									JSONObject pjson = array.getJSONObject(i);
									beana  = new HomeItemBean();
									if(!pjson.isNull("notificatType")){
											beana.type = pjson.getString("notificatType");
									}
									if(!pjson.isNull("cid")){
										beana.albumId = Integer.parseInt(pjson.getString("cid"));		
									}
									if(!pjson.isNull("ImagePath")){
										beana.ic_url = pjson.getString("ImagePath");
									}
									if(!pjson.isNull("cTitle")){
										beana.albumName = pjson.getString("cTitle");
									}
									if(!pjson.isNull("cDesc")){
										beana.abumDes = pjson.getString("cDesc");
									}
									if(!pjson.isNull("description")){
										beana.content = pjson.getString("description");
									}
								}
							}
							
							/**
							 * notificatType
								cid
								ImagePath
								cTitle
								cDesc
							 */
							//
							Intent  intent = null;
							switch (Integer.parseInt(beana.type)) {
							case 2://音乐	
								intent = new Intent(Constants.TOMusicLIST);
								CateItem app = new CateItem();
								
								app.id = beana.albumId;
								
								app.ImagePath = beana.ic_url;
								app.cTitle = beana.albumName;
								app.cDesc = beana.abumDes;
								app.cCount = 2;
								intent.putExtra("app", app);
								intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
								break;
							case 4://电视
								intent = new Intent(Constants.TOTVLIST);
								CateItem app3 = new CateItem();
								app3.id = beana.albumId;
								
								app3.ImagePath = beana.ic_url;
								app3.cTitle = beana.albumName;
								app3.cDesc = beana.abumDes;
								app3.cCount = 2;
								intent.putExtra("app", app3);
								intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
								break;
							case 6://动画
								intent = new Intent(Constants.TOTVLIST);
								CateItem app4 = new CateItem();
								app4.id = beana.albumId;
								
								app4.ImagePath = beana.ic_url;
								app4.cTitle = beana.albumName;
								app4.cDesc = beana.abumDes;
								app4.cCount = 2;
								intent.putExtra("app", app4);
								intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
								break;
							case 8://电台
								intent = new Intent(Constants.TOMusicLIST);
								CateItem app2 = new CateItem();
								app2.id = beana.albumId;
								
								app2.ImagePath = beana.ic_url;
								app2.cTitle = beana.albumName;
								app2.cDesc = beana.abumDes;
								app2.cCount = 2;
								intent.putExtra("app", app2);
								intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
								break;
							case 10://文本小说
								intent = new Intent(Constants.TONOVELLIST);
								CateItem app5 = new CateItem();
								app5.id = beana.albumId;
								
								app5.ImagePath = beana.ic_url;
								app5.cTitle = beana.albumName;
								app5.cDesc = beana.abumDes;
								app5.cCount = 2;
								intent.putExtra("app", app5);
								intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
								break;
							case 11://糗百
								intent = new Intent(context, JokeActivity.class);
								intent.putExtra("notif", "");
								break;
							case 12://新闻
								intent = new Intent(context, NewsActivity.class);
								intent.putExtra("notif", "");
								break;
							case 13://av
								intent = new Intent(context, LiveActivity.class);
								intent.putExtra("notif", "");
								break;
							case 14://电影
								intent = new Intent(context,VideoActivity.class);
								intent.putExtra("notif", "");
								break;
							default:
								break;
							}
							intent.setFlags(
					                Intent.FLAG_ACTIVITY_NEW_TASK );
							 // 如果程序在后台
							if(isApplicationBroughtToBackground(context)){
								intent = context.getPackageManager().
					                    getLaunchIntentForPackage("com.byt.market");
								intent.setFlags(
					                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					            Bundle args = new Bundle();
					            Log.d("nnlog", "在后台");
					            args.putString("notificatType", beana.type);
					            args.putInt("cid", beana.albumId);
					            args.putString("ImagePath", beana.ic_url);
					            args.putString("cTitle", beana.albumName);
					            args.putString("cDesc", beana.abumDes);
					            intent.putExtra(Constants.IS_NOTIF, args);
							}
							context.startActivity(intent);
							
						}
					}
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	//自定义消息
	@SuppressLint("NewApi")
	public static void showNotifal(Context context,JSONObject json,String content,boolean isRuning,String jup){
		
		HomeItemBean beana  = new HomeItemBean();
	//	CateItem app = new CateItem();
		//解析 jpush推送下来的 json
		try {
		if(!json.isNull("notificatType")){
				beana.type = json.getString("notificatType");
				Log.d("mylog",beana.type+"tttttttty" );
		}
		if(!json.isNull("cid")){
			beana.albumId = Integer.parseInt(json.getString("cid"));		
		}
		if(!json.isNull("ImagePath")){
			beana.ic_url = json.getString("ImagePath");
		}
		if(!json.isNull("cTitle")){
			beana.albumName = json.getString("cTitle");
		}
		if(!json.isNull("cDesc")){
			beana.abumDes = json.getString("cDesc");
		}
		if(!json.isNull("content")){
			beana.content = json.getString("content");
		}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/**
		 * notificatType
			cid
			ImagePath
			cTitle
			cDesc
		 */
		//
		Intent  intent = null;
		switch (Integer.parseInt(beana.type)) {
		case 2://音乐	
			intent = new Intent(Constants.TOMusicLIST);
			//SubjectItem app = new SubjectItem();
			CateItem app = new CateItem();
			//app.sid = appItem.musicRid;
			//app.id = appItem.musicRid;
			
			Log.d("test", "-----idid------"+beana.contentId);
			//Log.d("test", "-----idid------"+beana.contentId);
			//Log.d("test", "-----idid------"+beana.contentId);
			//Log.d("test", "-----idid------"+beana.contentId);
			app.id = beana.albumId;
			
			app.ImagePath = beana.ic_url;
			app.cTitle = beana.albumName;
			app.cDesc = beana.abumDes;
			app.cCount = 2;
			intent.putExtra("app", app);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
//			((FragmentActivity) context).startActivity(intent);
//			((FragmentActivity) context).overridePendingTransition(R.anim.push_left_in,
//					R.anim.push_left_out);		
			break;
		case 4://电视
			intent = new Intent(Constants.TOTVLIST);
			//SubjectItem app = new SubjectItem();
			CateItem app3 = new CateItem();
			//app.sid = appItem.musicRid;
			//app.id = appItem.musicRid;
			app3.id = beana.albumId;
			
			app3.ImagePath = beana.ic_url;
			app3.cTitle = beana.albumName;
			app3.cDesc = beana.abumDes;
			app3.cCount = 2;
			intent.putExtra("app", app3);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
//			((FragmentActivity) context).startActivity(intent);
//			((FragmentActivity) context).overridePendingTransition(R.anim.push_left_in,
//					R.anim.push_left_out);
			break;
		case 6://动画
			intent = new Intent(Constants.TOTVLIST);
			//SubjectItem app = new SubjectItem();
			CateItem app4 = new CateItem();
			//app.sid = appItem.musicRid;
			//app.id = appItem.musicRid;
			app4.id = beana.albumId;
			
			app4.ImagePath = beana.ic_url;
			app4.cTitle = beana.albumName;
			app4.cDesc = beana.abumDes;
			app4.cCount = 2;
			intent.putExtra("app", app4);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
//			((FragmentActivity) context).startActivity(intent);
//			((FragmentActivity) context).overridePendingTransition(R.anim.push_left_in,
//					R.anim.push_left_out);
			break;
		case 8://电台
			intent = new Intent(Constants.TOMusicLIST);
			//SubjectItem app = new SubjectItem();
			CateItem app2 = new CateItem();
			//app.sid = appItem.musicRid;
			//app.id = appItem.musicRid;
			app2.id = beana.albumId;
			
			app2.ImagePath = beana.ic_url;
			app2.cTitle = beana.albumName;
			app2.cDesc = beana.abumDes;
			app2.cCount = 2;
			intent.putExtra("app", app2);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
//			((FragmentActivity) context).startActivity(intent);
//			((FragmentActivity) context).overridePendingTransition(R.anim.push_left_in,
//					R.anim.push_left_out);
			break;
		case 10://文本小说
			intent = new Intent(Constants.TONOVELLIST);
			//SubjectItem app = new SubjectItem();
			CateItem app5 = new CateItem();
			//app.sid = appItem.musicRid;
			//app.id = appItem.musicRid;
			app5.id = beana.albumId;
			
			app5.ImagePath = beana.ic_url;
			app5.cTitle = beana.albumName;
			app5.cDesc = beana.abumDes;
			app5.cCount = 2;
			intent.putExtra("app", app5);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
//			((FragmentActivity) context).startActivity(intent);
//			((FragmentActivity) context).overridePendingTransition(R.anim.push_left_in,
//					R.anim.push_left_out);
			break;
		case 11:
			intent = new Intent(MyApplication.getInstance(), JokeActivity.class);
			intent.putExtra("notif", "");
			intent.putExtra("cid", beana.albumId);
			break;
		case 12:
			intent = new Intent(MyApplication.getInstance(), NewsActivity.class);
			intent.putExtra("notif", "");
			intent.putExtra("cid", beana.albumId);
			break;
		default:
			break;
		}
		
		
		 NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);  
		 Notification.Builder mBuilder = new Notification.Builder(context); 
		 
		 // 如果程序在后台
		if(isRuning){
			intent = context.getPackageManager().
                    getLaunchIntentForPackage("com.byt.market");
			intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Bundle args = new Bundle();
            /**
             * app.id = beana.albumId;
			
			app.ImagePath = beana.ic_url;
			app.cTitle = beana.albumName;
			app.cDesc = beana.abumDes;
             */
            args.putString("notificatType", beana.type);
            args.putInt("cid", beana.albumId);
            args.putString("ImagePath", beana.ic_url);
            args.putString("cTitle", beana.albumName);
            args.putString("cDesc", beana.abumDes);
            intent.putExtra(Constants.IS_NOTIF, args);
          //  context.startActivity(launchIntent);
		}
		//统计
		intent.putExtra("jup", jup);
		 PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);  
		 mBuilder//设置通知栏标题  
		    .setContentText(beana.content).setContentTitle(content);
		 
		// .setContentIntent(context.getgetDefalutIntent(Notification.FLAG_AUTO_CANCEL));
		//  .setNumber(number) //设置通知集合的数量  
		 mBuilder.setTicker(content) //通知首次出现在通知栏，带上升动画效果的  
		    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间  
		    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级  
		    .setContentIntent(pendingIntent)
		//  .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消    
		    .setDefaults(Notification.DEFAULT_SOUND|Notification.FLAG_AUTO_CANCEL)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合  
		    //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission  
		    .setSmallIcon(R.drawable.ic_app);//设置通知小ICON  
//		 .flags |= Notification.FLAG_AUTO_CANCEL
		 Notification noti = mBuilder.getNotification();
		 noti.flags =Notification.FLAG_AUTO_CANCEL;
		// nm.notify(1, mBuilder.build());  
		 nm.notify(1,noti);  
	}
	public static void formainTo(Context context,Bundle bundle){
		Intent  intent = null;
		switch (Integer.parseInt(bundle.getString("notificatType"))) {
		case 2://音乐	
			intent = new Intent(Constants.TOMusicLIST);
			CateItem app = new CateItem();
			app.id = bundle.getInt("cid");
			app.ImagePath = bundle.getString("ImagePath");
			app.cTitle = bundle.getString("cTitle");
			app.cDesc = bundle.getString("cDesc");
			app.cCount = 2;
			app.isNotif = true;
			intent.putExtra("app", app);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
			break;
		case 4://电视
			intent = new Intent(Constants.TOTVLIST);
			CateItem app3 = new CateItem();
			app3.id = bundle.getInt("cid");
			app3.ImagePath = bundle.getString("ImagePath");
			app3.cTitle = bundle.getString("cTitle");
			app3.cDesc = bundle.getString("cDesc");
			app3.cCount = 2;
			app3.isNotif = true;
			intent.putExtra("app", app3);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
			break;
		case 6://动画
			intent = new Intent(Constants.TOTVLIST);
			CateItem app4 = new CateItem();
			app4.id = bundle.getInt("cid");
			app4.ImagePath = bundle.getString("ImagePath");
			app4.cTitle = bundle.getString("cTitle");
			app4.cDesc = bundle.getString("cDesc");
			app4.cCount = 2;
			app4.isNotif = true;
			intent.putExtra("app", app4);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
			break;
		case 8://电台
			intent = new Intent(Constants.TOMusicLIST);
			CateItem app2 = new CateItem();
			app2.id = bundle.getInt("cid");
			app2.ImagePath = bundle.getString("ImagePath");
			app2.cTitle = bundle.getString("cTitle");
			app2.cDesc = bundle.getString("cDesc");
			app2.cCount = 2;
			app2.isNotif = true;
			intent.putExtra("app", app2);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
			break;
		case 10://文本小说
			intent = new Intent(Constants.TONOVELLIST);
			CateItem app5 = new CateItem();
			app5.id = bundle.getInt("cid");
			app5.ImagePath = bundle.getString("ImagePath");
			app5.cTitle = bundle.getString("cTitle");
			app5.cDesc = bundle.getString("cDesc");
			app5.cCount = 2;
			app5.isNotif = true;
			intent.putExtra("app", app5);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
			break;
		case 11:
			intent = new Intent(context, JokeActivity.class);
			intent.putExtra("notif", "");
			intent.putExtra("cid", bundle.getInt("cid"));
			break;
		case 12:
			intent = new Intent(context, NewsActivity.class);
			intent.putExtra("notif", "");
			intent.putExtra("cid", bundle.getInt("cid"));
			break;
		case 13://av
			intent = new Intent(context, LiveActivity.class);
			intent.putExtra("notif", "");
			break;
		case 14://电影
			intent = new Intent(context,VideoActivity.class);
			intent.putExtra("notif", "");
			break;
		default:
			break;
		}
		context.startActivity(intent);
		 ((Activity) context).overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}
	public static boolean isApplicationBroughtToBackground(Context context) { 
	    ActivityManager am = (ActivityManager) context 
	            .getSystemService(Context.ACTIVITY_SERVICE); 
	    List<RunningTaskInfo> tasks = am.getRunningTasks(1); 
	    if (tasks != null && !tasks.isEmpty()) { 
	      ComponentName topActivity = tasks.get(0).topActivity; 
	    //  Debug.i(TAG, "topActivity:" + topActivity.flattenToString()); 
	    //  Debug.f(TAG, "topActivity:" + topActivity.flattenToString()); 
	      if (!topActivity.getPackageName().equals(context.getPackageName())) { 
	        return true; 
	      } 
	    } 
	    return false; 
	  } 
	
}
