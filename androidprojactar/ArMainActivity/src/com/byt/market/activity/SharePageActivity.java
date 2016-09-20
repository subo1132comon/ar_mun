package com.byt.market.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.bitmaputil.core.display.RoundedBitmapDisplayer;
import com.byt.market.data.RingItem;
import com.byt.market.data.UserData;
import com.byt.market.database.ShareDatabase;
import com.byt.market.mediaplayer.ShareMusicActivity;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

//分享页面
public class SharePageActivity extends Activity implements OnClickListener{

	ResolveInfo[] showapplist = new ResolveInfo[2];
	LinearLayout m_line,m_face;
	ImageView musicicon,fb_image,line_image,m_close_img;
	TextView musicname,musicauther,m_desc,m_uninstens;
	RingItem bundle;
	Bundle mbundle;
	ImageLoader imageLoader;
	private DisplayImageOptions mOptions;
	private String sendtext;
	boolean issendonclick=false;
	boolean ischek  = false;
	boolean runonrestart = false;
	int type = 0;
	int[] stringids = new int[]{R.string.share_app_content1
			,R.string.share_app_content2
			,R.string.share_app_content3
			,R.string.share_app_content4
			,R.string.share_app_content5
			,R.string.share_app_content6
			};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sharepage);
		
		initView();
		getShareAppInfor(this);
		initData();
	}
	
	private void initData() {
		// TODO Auto-generated method stub
		m_line.setOnClickListener(this);
		m_face.setOnClickListener(this);
		m_close_img.setOnClickListener(this);
		imageLoader = ImageLoader.getInstance();
		mOptions = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.app_empty_icon)
		.showImageForEmptyUri(R.drawable.app_empty_icon)
		.cacheOnDisc()
		.displayer(new RoundedBitmapDisplayer(200)).build();
		
		Intent intent=getIntent();
		bundle=(RingItem) intent.getParcelableExtra("sendstring");
		 mbundle = intent.getExtras();
			sendtext = MyApplication.getInstance().getResources().getString(
					R.string.musicshare);
			if (bundle.musicuri.startsWith("http://")) {
				sendtext = sendtext+bundle.name+"--"+MyApplication.getInstance().getResources().getString(
						R.string.download)+":"+
						bundle.musicuri;
			} else {
				sendtext = sendtext+bundle.name+"--"+MyApplication.getInstance().getResources().getString(
						R.string.download)+":"+Constants.APK_URL+
						bundle.musicuri;
			}
			
		musicname.setText(bundle.name);
		musicauther.setText(bundle.user);
		imageLoader.displayImage(bundle.logo, musicicon, mOptions);
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		m_line = (LinearLayout) findViewById(R.id.gotoshare_line);
		m_face = (LinearLayout) findViewById(R.id.gotoshare_face);
		musicicon=(ImageView) findViewById(R.id.sharebg);
		musicname=(TextView) findViewById(R.id.musicname);
		musicauther=(TextView) findViewById(R.id.musicauther);
		fb_image = (ImageView) findViewById(R.id.fb_image);
		line_image = (ImageView) findViewById(R.id.line_image);
		m_desc = (TextView) findViewById(R.id.desc);
		m_uninstens = (TextView) findViewById(R.id.uninstens);
		m_close_img = (ImageView) findViewById(R.id.close_image);
	}

	private void getShareAppInfor(Context context){
		List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
		Intent intent = new Intent(Intent.ACTION_SEND, null);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setType("text/plain");
		PackageManager pManager = context.getPackageManager();
		mApps = pManager.queryIntentActivities(intent,
				PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
		
		if(mApps.size()<1){
			m_line.setVisibility(View.GONE);
			m_face.setVisibility(View.GONE);
			m_uninstens.setText(getString(R.string.shareneedinstall));
			m_uninstens.setTextColor(Color.GRAY);
			
		}else{
			for(ResolveInfo resolveInfo:mApps){
				if(resolveInfo.activityInfo.name.equals("com.mediatek.bluetooth.BluetoothShareGatewayActivity"))
				{
					mApps.remove(resolveInfo);
					break;
				}
			}
			PackageManager pm = SharePageActivity.this.getPackageManager();
			for(ResolveInfo resolve:mApps){						
				if (resolve.activityInfo.name
						.indexOf("com.facebook.composer")>-1) {
					showapplist[1] = resolve;
					m_face.setVisibility(View.VISIBLE);
					fb_image.setBackground(resolve.loadIcon(pm));
				} else if (resolve.activityInfo.name
						.equals("jp.naver.line.android.activity.selectchat.SelectChatActivity")) {
					showapplist[0] = resolve;
					m_line.setVisibility(View.VISIBLE);
					line_image.setBackground(resolve.loadIcon(pm));
				}
			}
			
			if(showapplist[0]==null&&showapplist[1]==null){
				m_line.setVisibility(View.GONE);
				m_face.setVisibility(View.GONE);
				m_uninstens.setText(getString(R.string.shareneedinstall));
				m_uninstens.setTextColor(Color.GRAY);
			}
		}
	}

	private void shareText(ResolveInfo appInfo){
		String name=appInfo.activityInfo.name;
	//分享网址出去
		UserData user = MyApplication.getInstance()
				.getUser();		
		if(user==null||user.getUid()==null||user.getUid().equals(""))
		{
			Toast.makeText(SharePageActivity.this, R.string.toast_uid_cannot_null, Toast.LENGTH_SHORT).show();
			SharePageActivity.this.finish();
			return;
			
		}	
		//add by bobo   R.string.musicshare
		if(mbundle!=null){
			if(appInfo.activityInfo.packageName.equals("com.facebook.katana")){
				if("music".equals(mbundle.get("type"))){
					sendtext =Constants.MUSIC_TV_WEBBASE+"/mshare_"+bundle.sid+".html";
					MobclickAgent.onEvent(this, "ShareMusic");
					StatService.trackCustomEvent(this, "ShareMusic", "");
				}else if("tv".equals(mbundle.get("type"))){
					sendtext =Constants.MUSIC_TV_WEBBASE+"/vshare_"+bundle.sid+".html";
					MobclickAgent.onEvent(this, "ShareTV");
					StatService.trackCustomEvent(this, "ShareTV", "");
				}else if("nover".equals(mbundle.get("type"))){
					sendtext =Constants.MUSIC_TV_WEBBASE+"/bshare_"+bundle.sid+".html";
					StatService.trackCustomEvent(this, "ShareNovel", "");
					MobclickAgent.onEvent(this, "ShareNovel");
				}
			}else{
				int rid = getRanDomID();
				String str = null;
				int unicod = 0;
				switch (rid) {
				case R.string.share_app_content2:
					unicod = 0x1F604;
					break;
				case R.string.share_app_content1:
					unicod = 0x1F44D;
					break;
				case R.string.share_app_content3:
					unicod = 0x1F603;
					break;   
				case R.string.share_app_content4:
					unicod = 0x1F60F;
					break;
				case R.string.share_app_content5:
					unicod = 0x1F601;
					break;
				case R.string.share_app_content6:
					unicod = 0x1F606;
					break;
				}
				str = MyApplication.getInstance().getResources().getString(rid)+setEmojiToTextView(unicod)+" ";
				
				if("music".equals(mbundle.get("type"))){
					sendtext = str+bundle.name+" "+Constants.MUSIC_TV_WEBBASE+"/mshare_"+bundle.sid+".html";
					MobclickAgent.onEvent(this, "ShareMusic");
					StatService.trackCustomEvent(this, "ShareMusic", "");
				}else if("tv".equals(mbundle.get("type"))){
					sendtext = str+bundle.name+" "+Constants.MUSIC_TV_WEBBASE+"/vshare_"+bundle.sid+".html";
					MobclickAgent.onEvent(this, "ShareTV");
					StatService.trackCustomEvent(this, "ShareTV", "");
				}else if("nover".equals(mbundle.get("type"))){
					sendtext = str+bundle.name+" "+Constants.MUSIC_TV_WEBBASE+"/bshare_"+bundle.sid+".html";
					MobclickAgent.onEvent(this, "ShareNovel");
					StatService.trackCustomEvent(this, "ShareNovel", "");
				}
			}
		}
		//--end
		
	    Intent shareIntent=new Intent(Intent.ACTION_SEND);
	    shareIntent.setComponent(new ComponentName(appInfo.activityInfo.packageName, appInfo.activityInfo.name));  
	   // shareIntent.setType("image/png"); 
	    shareIntent.setType("text/plain"); 
	    Log.d("nnlog",sendtext+"**********music");
	    shareIntent.putExtra(Intent.EXTRA_TEXT, sendtext);
	 //  shareIntent.putExtra("sms_body", sendtext);//解决短信分享获取不到图片信息
	   
	   // Uri uri =Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.ic_app+".png");					 
	 //   shareIntent.putExtra(Intent.EXTRA_STREAM, uri); 
	    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
	    startActivity(shareIntent);
//	    issendonclick=true;
//		Intent intent =new Intent();
//		intent.putExtra("isok", issendonclick);
//		setResult(RESULT_OK, intent);	
	    sendBroadcast(new Intent(Constants.SHARE_ACTION));
	    ischek = true; 
	//	}
	}
	
	/**
	 * 随机提示语
	 */
	public int getRanDomID(){
		Random rand = new Random();
		int i = rand.nextInt(stringids.length);
		return stringids[i];
	}
	private String setEmojiToTextView(int unicod){
	    String emojiString = getEmojiStringByUnicode(unicod);
	    return emojiString;
	}

	private String getEmojiStringByUnicode(int unicode){
	    return new String(Character.toChars(unicode));
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SharedPreferences yuVer = MyApplication.getInstance()
				.getSharedPreferences("share", 0);
		long ordtime = yuVer.getLong("time", 0);
		long currentTime = System.currentTimeMillis();
		long flogtime = currentTime-ordtime;
		long sizetime = 0;
		if(type==1){
			sizetime = 10000;
		}else{
			sizetime = 5000;
		}
		if(flogtime>sizetime&&ischek&&!runonrestart){
			Intent intent = new Intent();
			intent.putExtra("share", bundle);
			intent.setAction(Constants.SHARE_ACTION);
			sendBroadcast(intent);
			//存入数据库
			ShareDatabase dataHaple = new ShareDatabase(this);
			SQLiteDatabase db = dataHaple.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id",bundle.sid);
			values.put("name",bundle.name);
			db.insert("usershare", null, values);
			SharePageActivity.this.finish();
		}
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		SharedPreferences yuVer = MyApplication.getInstance()
				.getSharedPreferences("share", 0);
		long ordtime = yuVer.getLong("time", 0);
		long currentTime = System.currentTimeMillis();
		long flogtime = currentTime-ordtime;
		long sizetime = 0;
		if(type==1){
			sizetime = 10000;
		}else{
			sizetime = 5000;
		}
		if(flogtime>sizetime&&ischek){
			runonrestart = true;
			Intent intent = new Intent();
			intent.putExtra("share", bundle);
			intent.setAction(Constants.SHARE_ACTION);
			sendBroadcast(intent);
			//存入数据库
			ShareDatabase dataHaple = new ShareDatabase(this);
			SQLiteDatabase db = dataHaple.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id",bundle.sid);
			values.put("name",bundle.name);
			db.insert("usershare", null, values);
			SharePageActivity.this.finish();
		}
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		SharedPreferences yuVer = MyApplication.getInstance()
				.getSharedPreferences("share", 0);
		SharedPreferences.Editor editor = yuVer.edit();
		long thistime = System.currentTimeMillis();
		editor.putLong("time", thistime);
		editor.commit();
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		ResolveInfo appInfo = null;
		switch (arg0.getId()) {
		case R.id.gotoshare_line:
			appInfo = showapplist[0];
			type = 1;
			shareText(appInfo);
			break;
		case R.id.gotoshare_face:
			appInfo = showapplist[1];
			type = 2;
			shareText(appInfo);
			break;
		case R.id.close_image:
			Intent intent = new Intent();
			intent.putExtra("share", bundle);
			intent.setAction(Constants.SHARE_ACTION);
			sendBroadcast(intent);
			SharePageActivity.this.finish();
			break;
		}
	}
}
