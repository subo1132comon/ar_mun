package com.byt.market.ui;


import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.Faviorate.MyFaviorateListener;
import com.byt.market.Faviorate.MyFaviorateManager;
import com.byt.market.activity.AboutActivity;
import com.byt.market.activity.DownLoadManageActivity;
import com.byt.market.activity.FeedBackActivity;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.MyFaviorateActivity;
import com.byt.market.activity.SettingsActivity;
import com.byt.market.activity.ShareActivity;
import com.byt.market.activity.ShowLevelActivity;
import com.byt.market.activity.SoftwareUninstallActivity;
import com.byt.market.asynctask.FileCacehTask;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.bitmaputil.core.assist.FailReason;
import com.byt.market.bitmaputil.core.assist.ImageLoadingListener;
import com.byt.market.data.AppItem;
import com.byt.market.data.UserData;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.voiced.MangerActivitys;
import com.byt.market.net.NetworkUtil;
import com.byt.market.tools.DownLoadVdioapkTools;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.DataUtil;
import com.byt.market.util.DisplayParams;
import com.byt.market.util.DisplayUtil;
import com.byt.market.util.FileCheckUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LogUtil;
import com.byt.market.util.NormalLoadPictrue;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.Singinstents;
import com.byt.market.util.StringUtil;
import com.byt.market.util.Util;
import com.byt.market.view.MyTextView;
import com.byt.market.view.MydownLoadDailog;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

public class ManagerFragment extends BaseUIFragment implements OnClickListener ,DownloadTaskListener,MyFaviorateListener,TaskListener{
	private boolean isupdatelocal=false;
	TextView userNameID;
	TextView unregist;
	TextView userVIPLevel;
	TextView userPoint;
	LinearLayout usericon;
	String userNickName ;
	private String COLECCTTEXT="colecctnew";
	public MarketContext maContext;
	private TextView tv_pop_down_manager,tv_down_num,tv_collect;
	private TextView tv_pop_update;	
	private TextView app_update_info;	
	private TextView appupdatelocal_status;
	private ImageView m_userIco;
	private DisplayImageOptions mOptions;
	//add  by  bobo 
	private LinearLayout mdownload_button;
	private LinearLayout mdownload_tv_button;
	//-----end
	private ImageView redpoint;
	private LinearLayout myVip;
	private int allDownloadCount, allUpdateCount;
	private static final int MSG_UPDATE_DOWN_NUM=1;
	private static final int MSG_UPDATE_OPEN_NUM=2;
	private static final int MSG_UPDATE_COLLECT=3;
	private static final int MSG_UPDATE_APP_UPDATE_INFO=4;
	private static final int MSG_UPDATE_USER=5;
	private String userName=null;
	

	
//	private void refreshStatus() {
//		// TODO Auto-generated method stub
//	try{	SharedPreferences newFavor=MyApplication.getInstance().getSharedPreferences(COLECCTTEXT,0);
//		//int cout=newFavor.getInt("cout", 0);
//		
//		MyApplication mMyApplication = (MyApplication) getActivity().getApplication();
//		int cout = mMyApplication.getCollectNum();
//		
//		//Toast.makeText(MyApplication.getInstance(), "cout = "+cout, Toast.LENGTH_LONG).show();
//		if (cout > 0) {
//			tv_collect.setVisibility(View.GONE);
//			tv_collect.setText(String.valueOf(cout));
//		} else {
//			tv_collect.setVisibility(View.GONE);
//		}
//	}
//	catch(Exception e){}
//	}
	/*
　　　　 ------------The Bestone modifed end--------------
　　　*/
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_UPDATE_USER:
			
			initData();
			break;
			case MSG_UPDATE_DOWN_NUM: {
				initDownLoadAndNeedUpdateData();
				break;
			}
			case MSG_UPDATE_OPEN_NUM:
				initinstalllData();
				break;
			case MSG_UPDATE_COLLECT://刷新分享页面
			
//				refreshStatus();
				break;
			case MSG_UPDATE_APP_UPDATE_INFO:
				refreshAppUpadteInfo();
				sendEmptyMessageDelayed(MSG_UPDATE_APP_UPDATE_INFO, 5000);
				break;
		
			}
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
					Bundle savedInstanceState) {
				View view = inflater.inflate(R.layout.manager_frame, container, false);
				mOptions = new DisplayImageOptions.Builder().cacheOnDisc().build();
				imageLoader = ImageLoader.getInstance();
				initViews(view);
				initListeners(view);				
				initData();
				DownloadTaskManager.getInstance().addListener(this);
				MyFaviorateManager.getInstance().addListener(this);				
				return view;
			}

			/** 检测商店版本是否有更新，由此判断"检查版本更新Icon"是否需要显示“New浮标” **/
			public void showRedPoint()
			{
				
				/*if(redpoint == null)
				{
					LayoutInflater mInflater = (LayoutInflater)MyApplication.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);					
					View mView = mInflater.inflate(R.layout.manager_frame, null, false);
					redpoint = (ImageView) mView.findViewById(R.id.redpoint);	
					redpoint.setVisibility(View.VISIBLE);
					
				}//*/
				if(redpoint != null)
				{
					redpoint.setVisibility(View.VISIBLE);
				}				
			}
			public void hideRedPoint()
			{
				/*if(redpoint == null)
				{
					LayoutInflater mInflater = (LayoutInflater)MyApplication.getInstance().getSystemService(Context.LAYOUT_INFLATER_SERVICE);					
					View mView = mInflater.inflate(R.layout.manager_frame, null, false);
					redpoint = (ImageView) mView.findViewById(R.id.redpoint);
				}	//*/			
				if(redpoint != null)
				{
					redpoint.setVisibility(View.GONE);
				}				
			}
			
			private void initData() {
				try{
				UserData user = MyApplication.getInstance().getUser();
				if (user.isLogin()) {		
					 unregist.setVisibility(View.GONE);
					 userNickName = user.getUid();
					int userlevel=user.getUlevel();
					
					//add by bobo
					try {
						String uico = MyApplication.getInstance().getUser().getIconUrl();
						//imageLoader.displayImage(uico, m_userIco, mOptions);
						new NormalLoadPictrue().getPicture(getActivity(),uico, m_userIco);
					} catch (Exception e) {
						// TODO: handle exception
						Log.d("nnlog", "头像eee"+e);
					}
					//---end
					
					if (!TextUtils.isEmpty(userNickName)) 
					{			
						
						userNameID.setText(userNickName );							
						userName=userNickName;	
						userVIPLevel.setText(getString(R.string.VIP_LEVEL1 + userlevel - 1));
						
						/** 用于级别信息 start**/
						Resources r = MyApplication.getInstance().getResources(); 
						int width = (int)r.getDimension(R.dimen.mytextview_width); 
						int height = 25;
						DisplayParams displayParams = DisplayParams.getInstance(MyApplication.getInstance());	
						
						MyTextView myTextView = new MyTextView(MyApplication.getInstance());
						myTextView.setText(getString(R.string.VIP_LEVEL1 + userlevel - 1));
						myTextView.setTextSize(15);
						myTextView.setTextAlign(MyTextView.TEXT_ALIGN_BOTTOM_WITHOUT_PADDING);
						myTextView.setTextColor(Color.BLACK);						
						myVip.addView(myTextView, DisplayUtil.dip2px(width, displayParams.scale), DisplayUtil.dip2px(height, displayParams.scale));	
						/** 用于级别信息 end**/						
					}

					/** 根据读到的userlevel值动态显示钻石个数**/					
					if(userlevel > 0)
					{
						for(int i=0; i< userlevel; i++)
						{
							ImageView imageView = new ImageView(MyApplication.getInstance());
//							imageView.setBackgroundResource(R.drawable.level);
							imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,R.dimen.level_height));
							usericon.addView(imageView);
						}
					}
					else if(userlevel == 0)/** 默认0分时也显示一颗星**/
					{						
						ImageView imageView = new ImageView(MyApplication.getInstance());
//						imageView.setBackgroundResource(R.drawable.level);
						imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,R.dimen.level_height));
						usericon.addView(imageView);
					}
					userPoint.setText(getString(R.string.userpoint));
				} 
				else
				{
					unregist.setVisibility(View.VISIBLE);
				}
				}
				catch(Exception e){
				}
			}

			private void initListeners(View view) {
				
				view.findViewById(R.id.userbg).setOnClickListener(this);
				view.findViewById(R.id.appmanager).setOnClickListener(this);
				view.findViewById(R.id.appupdate).setOnClickListener(this);
				view.findViewById(R.id.appdownload).setOnClickListener(this);
				view.findViewById(R.id.appsetting).setOnClickListener(this);
				view.findViewById(R.id.appabout).setOnClickListener(this);
				view.findViewById(R.id.appcellect).setOnClickListener(this);
				view.findViewById(R.id.appclear).setOnClickListener(this);
				view.findViewById(R.id.appfeedback).setOnClickListener(this);
				view.findViewById(R.id.appupdatelocal).setOnClickListener(this);				
				view.findViewById(R.id.btn_integral_set).setOnClickListener(this);
				view.findViewById(R.id.applike).setOnClickListener(this);
				view.findViewById(R.id.appshare).setOnClickListener(this);				
			
			}
			Animation animation;
			private void initViews(View view) {
				/*bestone add by "zengxiao" for:添加标题头*/
				/*view.findViewById(R.id.titlebar_title).setVisibility(View.GONE);
				view.findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
				view.findViewById(R.id.iv_settings).setVisibility(View.GONE);
				view.findViewById(R.id.search_page_view).setVisibility(View.VISIBLE);
				view.findViewById(R.id.search_page_view).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						ManagerFragment.this.getActivity().startActivity(new Intent(Constants.TOSEARCH));
						ManagerFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);	
					}
				});
				view.findViewById(R.id.top_downbutton).setVisibility(View.VISIBLE);
				view.findViewById(R.id.top_downbutton).setOnClickListener(this);//添加下载界面按钮
				view.findViewById(R.id.titlebar_applist_button_container).setVisibility(View.GONE);	//*/			
				/*bestone add end*/
				
				maContext = MarketContext.getInstance();
				tv_collect=(TextView) view.findViewById(R.id.tv_collect);
				tv_down_num=(TextView) view.findViewById(R.id.tv_down_update);
				usericon=(LinearLayout) view.findViewById(R.id.usericon);
				
				tv_pop_down_manager = (TextView) view
						.findViewById(R.id.tv_pop_down_manager);
				tv_pop_update = (TextView) view.findViewById(R.id.tv_pop_update);
				app_update_info = (TextView) view.findViewById(R.id.app_update_info);
				animation=AnimationUtils.loadAnimation(getActivity(),R.anim.push_top_in);
				userNameID=(TextView) view.findViewById(R.id.appusername);
				userPoint=(TextView) view.findViewById(R.id.appuserpoint);				
				appupdatelocal_status = (TextView)view.findViewById(R.id.appupdatelocal_status);
				userVIPLevel = (TextView) view.findViewById(R.id.appuservip);
				redpoint = (ImageView)view.findViewById(R.id.redpoint);
				m_userIco = (ImageView) view.findViewById(R.id.app_userpic);
				
				myVip = (LinearLayout)view.findViewById(R.id.my_vip_containter);
				unregist = (TextView) view.findViewById(R.id.unregist);
				//-----by bobo 
				mdownload_button = (LinearLayout) view.findViewById(R.id.donwload_layout);
				mdownload_button.setOnClickListener(this);
				mdownload_tv_button = (LinearLayout) view.findViewById(R.id.donwload_layout_tv);
				mdownload_tv_button.setOnClickListener(this);
				//end----
			}
			@Override
			public void onResume() {
				super.onResume();
				initinstalllData();
				initDownLoadAndNeedUpdateData();
//				refreshStatus();
				showAppUpdateInfo(isVisibleToUserNew);
				MobclickAgent.onPageStart("管理"); //统计页面
			    MobclickAgent.onResume(ManagerFragment.this.getActivity());//时长  
			}
			private void initDownLoadAndNeedUpdateData() {
				allDownloadCount = DownloadTaskManager.getInstance()
						.getAllDowoLoadCount();
				allUpdateCount = DownloadTaskManager.getInstance()
						.getNeedUpdateAppCount();
				
				if (allDownloadCount > 0) {
					tv_pop_down_manager.setVisibility(View.GONE);
					tv_pop_down_manager.setText(String.valueOf(allDownloadCount));
				} else {
					tv_pop_down_manager.setVisibility(View.GONE);
				}

				if (allUpdateCount > 0) {
					tv_pop_update.setVisibility(View.VISIBLE);
					tv_pop_update.setText(String.valueOf(allUpdateCount));	
				} 
				else 
				{
					tv_pop_update.setVisibility(View.GONE);					
				}
				
				//refreshAppUpadteInfo();
				

				LogUtil.i("0425", "initdata allDownloadCount = " + allDownloadCount
						+ ",allUpdateCount = " + allUpdateCount);
			}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.appmanager://应用管理
			startActivity(new Intent(MyApplication.getInstance(),
					SoftwareUninstallActivity.class));
			ManagerFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		case R.id.appupdate://应用更新
			Intent updateIntent = new Intent(getActivity(),
					DownLoadManageActivity.class);
			updateIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
					DownLoadManageActivity.TYPE_FROM_UPDATE);
			updateIntent.putExtra(DownLoadManageActivity.ALL_UPDATE_COUNT,
					allUpdateCount);
			startActivity(updateIntent);
			ManagerFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		case R.id.appdownload://下载管理
			Intent downloadIntent = new Intent(getActivity(),
					DownLoadManageActivity.class);
			downloadIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
					DownLoadManageActivity.TYPE_FROM_DOWNLOAD);
			startActivity(downloadIntent);
			ManagerFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		case R.id.appsetting://设置				
			startActivity(new Intent(MyApplication.getInstance(), SettingsActivity.class));
			ManagerFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		case R.id.appabout://关于
			startActivity(new Intent(MyApplication.getInstance(), AboutActivity.class));
			ManagerFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		case R.id.appcellect://我的收藏
			Intent intent = new Intent(getActivity(), MyFaviorateActivity.class);
			startActivity(intent);
			break;
		case R.id.appclear://缓存清理
			//new FileCacehTask(getActivity()).execute();
			
			FileCheckUtil.cleraCacel(getActivity(),mhandler);
			break;
		case R.id.appfeedback://反馈
			startActivity(new Intent(MyApplication.getInstance(), FeedBackActivity.class));
			ManagerFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
			
		case R.id.appupdatelocal:
			if(!isupdatelocal)
			{
				isupdatelocal=true;
				checkmyClientUpdate();				
					
			}
			break;
		case R.id.applike:
			
			Intent intentshare =new Intent();
			intentshare.setClass(ManagerFragment.this.getActivity(),ShareActivity.class);		
			ManagerFragment.this.getActivity().startActivity(intentshare);
//			Intent intent2=new Intent(Constants.TOLIST);
//			intent2.putExtra(Constants.LIST_FROM, LogModel.L_FAV_USER);
//			startActivity(intent2);
//			getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		case R.id.userbg:	
		case R.id.btn_integral_set:				
			Intent inten=new Intent(MyApplication.getInstance(),ShowLevelActivity.class);
			
			startActivity(inten);
			ManagerFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;		
		/*case R.id.top_downbutton:
			Intent topdownloadIntent = new Intent(this.getActivity(),
					DownLoadManageActivity.class);
			topdownloadIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
					DownLoadManageActivity.TYPE_FROM_DOWNLOAD);
			this.getActivity().startActivity(topdownloadIntent);
			this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;	//*/
		case R.id.appshare:				
			startActivity(new Intent(MyApplication.getInstance(),ShareActivity.class));
			break;
		//分享 下载 
		case R.id.donwload_layout:
			new MydownLoadDailog(getActivity()).show();
			break;
		case R.id.donwload_layout_tv:
			DownLoadVdioapkTools dt = new DownLoadVdioapkTools(getActivity());
			//if(dt.checkApkExist(context, "com.tyb.fun.palyer")){
				StatService.trackCustomEvent(getActivity(),"installPlayer","");
				Singinstents.getInstents().setVdiouri("");
				Singinstents.getInstents().setAppPackageName("com.tyb.fun.palyer");
				dt.showNoticeDialog();
			break;
		}

	}

	private Handler mhandler = new Handler(){
		public void handleMessage(Message msg) {
	//		String Mstr = "";
//			int value = msg.arg1;
//			Log.d("nnlog", "mmmhandler--"+value);
//			double mb = value/1024/1024;
//			if(mb>0){
//				Mstr = " "+mb+"Mb ";
//			}else{
//				Mstr = " "+value+"b ";
//			}
			double v =(double)msg.arg1;
			double mb = v/1024/1024;
			Log.d("nnlog", "mmmhandler--"+mb);
			Toast.makeText(getActivity(), getActivity().getString(R.string.cleared) + Util.apkSizeFormat(mb, "M") + getActivity().getString(R.string.cache), Toast.LENGTH_SHORT).show();
		};
	};
	@Override
	public void startConnecting(DownloadTask task, int totalTask,
			int progressTask) {
		// TODO Auto-generated method stub
		if(isVisibleToUserNew){
			mHandler.sendEmptyMessage(MSG_UPDATE_OPEN_NUM);
		}
	}

	@Override
	public void downloadStarted(DownloadTask task, int totalTask,
			int progressTask, long totalSize) {	
		if(isVisibleToUserNew){
			mHandler.sendEmptyMessage(MSG_UPDATE_DOWN_NUM);
		}
	}

	@Override
	public void downloadProgress(DownloadTask task, int totalTask,
			int progressTask, long progressSize, long totalSize) {
	
		// TODO Auto-generated method stub
		
	}

	@Override
	public void downloadEnded(DownloadTask task, int totalTask, int progressTask) {
		// TODO Auto-generated method stub
	}

	private void initinstalllData() {
		
		// TODO Auto-generated method stub
	//	final int needUpdateCount = DownloadTask.getNeedUpdateCount();
		final int innerInstalledButNotOpenedGamesCount = DownloadTaskManager
				.getInstance().getInnerInstalledButNotOpenedGamesCount();
		int count = innerInstalledButNotOpenedGamesCount;				
		if (count == 0) {
			tv_down_num.setVisibility(View.GONE);
		} else {
			tv_down_num.setVisibility(View.GONE);
			if (count >= 100) {
				tv_down_num.setText("n");
			} else {
				tv_down_num.setText(String.valueOf(count));
			}
		}
	}

	@Override
	public void endConnecting(DownloadTask task, int totalTask,
			int progressTask, DownloadException result) {
		if(isVisibleToUserNew){
			mHandler.sendEmptyMessage(MSG_UPDATE_OPEN_NUM);
		}
	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {
		// TODO Auto-generated method stub
	}

	@Override
	public void refreshUI() {
		
		if(isVisibleToUserNew){
			mHandler.sendEmptyMessage(MSG_UPDATE_DOWN_NUM);
		}
		
	}

	@Override
	public void installedSucess(DownloadTask downloadTask) {
		// TODO Auto-generated method stub
		initDownLoadAndNeedUpdateData();
	}

	@Override
	public void installedSucess(String packageName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unInstalledSucess(String packageName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void networkIsOk() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void FaviorateAppAdded(AppItem appItem) {
		try{
					Util.addMyFavData(maContext, appItem.sid);
					SharedPreferences newFavor=MyApplication.getInstance().getSharedPreferences(COLECCTTEXT,0);
					SharedPreferences.Editor edite=newFavor.edit();
					int cout=newFavor.getInt("cout", 0);
					edite.putInt("cout",cout+1);
					edite.commit();	
//					mHandler.sendEmptyMessage(MSG_UPDATE_COLLECT);
		}catch(Exception e){
			
		}
	}
	@Override
	public void FaviorateAppDeled(AppItem appItem) {
		try{
		if(DataUtil.getInstance(MyApplication.getInstance()).hasMYFavor(appItem.sid))
		{
			Util.delMyData(maContext, appItem.sid);
			SharedPreferences newFavor=MyApplication.getInstance().getSharedPreferences(COLECCTTEXT,0);
			SharedPreferences.Editor edite=newFavor.edit();
			int cout=newFavor.getInt("cout", 0);
			edite.putInt("cout",cout-1);
			edite.commit();	
//			mHandler.sendEmptyMessage(MSG_UPDATE_COLLECT);
		}
}catch(Exception e){
			
		}
	}

			
	public void checkmyClientUpdate(){
		if (NetworkUtil.isNetWorking(MyApplication.getInstance())) {
			ProtocolTask updateClient = new ProtocolTask(MyApplication.getInstance());
			updateClient.setListener(new TaskListener() {

				@Override
				public void onNoNetworking() {

				}

				@Override
				public void onNetworkingError() {

				}

				@Override
				public void onPostExecute(byte[] bytes) {
					try {
						isupdatelocal=false;
						if (bytes != null) {
							JSONObject result = new JSONObject(
									new String(bytes));
							int status = JsonParse.parseResultStatus(result);
							int count = result.getInt("allCount");
							Log.v("checkClientUpdate","+++++++++++++++++++"+status);
							Log.v("checkClientUpdate","+++++++++++++++++++"+count);
							if (status == 1 && count > 0) {		
								
								 ((MainActivity)getActivity()).parserUpdate((JSONObject) result.getJSONArray(
										"data").get(0));
							} else {
								Toast.makeText(MyApplication.getInstance(), R.string.newvbb,Toast.LENGTH_SHORT).show();							}
						} else {
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.gc();
				}

			});
			String userName=SharedPrefManager.getLastLoginUserName(MyApplication.getInstance());
			try {
				int code = MyApplication.getInstance().getPackageManager().getPackageInfo(MyApplication.getInstance().getPackageName(),
						0).versionCode;
				updateClient.execute(Constants.LIST_URL
						+ "?qt=uclient&versioncode=" + code+ "&uid=" + userName, null, null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	
	
	/***应用更新公告栏刷新**/
	private void refreshAppUpdateInfo() 
	{
		if (tv_pop_update != null) {
			allUpdateCount = DownloadTaskManager.getInstance()
					.getNeedUpdateAppCount();
			if (allUpdateCount > 0) {
				tv_pop_update.setVisibility(View.VISIBLE);
				tv_pop_update.setText(String.valueOf(allUpdateCount));
			} else {
				tv_pop_update.setVisibility(View.GONE);
			}

//			mHandler.sendEmptyMessage(MSG_UPDATE_APP_UPDATE_INFO);
		}
	}	
	/*add by zengxiao for:添加用户数据刷新*/
	public void getuserMsg() {
		if(userNameID!=null){
			String temp=String.valueOf(userNameID.getText());
			if(temp!=null&&temp.length()>0){
				return;
			}
		}
		if("".equals(userNickName)||userNickName==null)
		{
		if("".equals(userName)||userName==null){
			HashMap<String,String> infos=SharedPrefManager.getClintInfo(MyApplication.getInstance());
			String cid=infos.get(SharedPrefManager.CID);
			if(cid!=null){
				userName=cid;
				SharedPrefManager.setLastLoginUserName(MyApplication.getInstance(), userName);
			}
		}
		if("".equals(userName)||userName==null){
			if(MyApplication.getInstance().isuserloading)
				return;
			UserData user = MyApplication.getInstance().getUser();
			if (user.isLogin()) {
				mHandler.sendEmptyMessage(MSG_UPDATE_USER);
			}else{
			doUserRegister();
			}
		}else {
			if(MyApplication.getInstance().isuserloading)
				return;
			UserData user = MyApplication.getInstance().getUser();
			if (user.isLogin()) {
				mHandler.sendEmptyMessage(MSG_UPDATE_USER);
			}else{
			doUserLogin(userName);
			}
			
		}
		}
		else{
			if(myVip.getChildCount()<1||usericon.getChildCount()<1||(unregist.getVisibility()==View.VISIBLE))
			{
			mHandler.sendEmptyMessage(MSG_UPDATE_USER);
			}
		}
		
		refreshAppUpdateInfo();
		
		
	}
	private void doUserLogin(String userName){
		ProtocolTask task = new ProtocolTask(MyApplication.getInstance());
		task.setListener(this);
		HashMap<String,String> infos=SharedPrefManager.getClintInfo(MyApplication.getInstance());
		String pid=infos.get(SharedPrefManager.PID);
		String var=Constants.LIST_URL + "?qt=" + Constants.USERLOGIN
				+ "&uid=" + userName + "&pwd=" + StringUtil.md5Encoding("123456");
		if(pid!=null){
			var=var+"&pid="+pid;
		}
		task.execute(var, null, "UserLoginFragment",
				getHeader());
	}
	private void doUserRegister(){
		ProtocolTask task = new ProtocolTask(MyApplication.getInstance());
		task.setListener(this);
		task.execute(Constants.LIST_URL + "?qt=reg&uid=" + "abc" + "&pwd="
				+ StringUtil.md5Encoding("123456"), null, "UserRegisteActivity", getHeader());
	}

	@Override
	public void onNoNetworking() {
		
	}

	@Override
	public void onNetworkingError() {
		
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		if("".equals(userName)||userName==null){
			initRegisterInfo(bytes);
		}else {	
			initLoginInfo(bytes);
		}
	}
	private void initRegisterInfo(byte[] bytes){
		JSONObject json = null;
		try {
			json = new JSONObject(new String(bytes));
			int resultStatus = json.getInt("resultStatus");
			if (resultStatus == 1) {
				JSONObject data = json.getJSONObject("data");
				showShortToast(getString(R.string.toast_regist_success));
				UserData user = new UserData();
				user.setType(UserData.TYPE_ME);
				user.setNickname(data.getString("NICKNAME"));
				user.setIconUrl(data.getString("ICON"));
				user.setCredit(data.getInt("CREDIT"));
				if (data.isNull("SEX")) {
					user.setGender(UserData.MALE);
				} else {
					int sex = data.getInt("SEX");
					switch (sex) {
					case 0:
						user.setGender(UserData.FEMALE);
						break;
					case 1:
						user.setGender(UserData.MALE);
						break;
					default:
						user.setGender(UserData.MALE);
						break;
					}
				}
				
				user.setUid(data.getString("USID"));
				user.setType(UserData.TYPE_ME);
				user.setMd5(StringUtil.md5Encoding("123456"));
				SharedPrefManager.setLastLoginUserName(MyApplication.getInstance(),
						user.getUid());
				MyApplication.getInstance().setUser(user);
				MyApplication.getInstance().getUser().setLogin(true);
				MyApplication.getInstance().updateUserInfo();
				Constants.ISSHOW = true;
				initData();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void initLoginInfo(byte[] bytes) {
		try {
			JSONObject json = new JSONObject(new String(bytes));
			int resultStatus = json.getInt("resultStatus");
			if (resultStatus == 1) {
				int allcount = json.getInt("allCount");
				if (allcount == 0) {
					// 证明用户名或密码有问题
					String errorResult = json.optString("data");
				} else {
					JSONObject result = json.getJSONObject("data");
					UserData user = JsonParse.parseUserInfo(result);
					if (user != null) {
						if (result.isNull("SEX")) {
							user.setGender(UserData.MALE);
						} else {
							int sex = result.getInt("SEX");
							switch (sex) {
							case 0:
								user.setGender(UserData.FEMALE);
								break;
							case 1:
								user.setGender(UserData.MALE);
								break;
							default:
								user.setGender(UserData.MALE);
								break;
							}
						}
						user.setUid(result.getString("USID"));
						user.setType(UserData.TYPE_ME);
						user.setMd5(StringUtil.md5Encoding("123456"));
						SharedPrefManager.setLastLoginUserName(MyApplication.getInstance(),
								user.getUid());
						setLoginUserInfo(user);
					} 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void setLoginUserInfo(UserData user) {
		MyApplication.getInstance().setUser(user);
		MyApplication.getInstance().getUser().setLogin(true);
		MyApplication.getInstance().updateUserInfo();
		Constants.ISSHOW = true;
	}
	
	/*add end */
	
	/*add by xiami for:应用更新之已更新用户数量信息显示*/
	static Drawable zoomDrawable(Drawable drawable, int w, int h)
    {
        int width = drawable.getIntrinsicWidth();
        int height= drawable.getIntrinsicHeight();
        Bitmap oldbmp = drawableToBitmap(drawable); 
        Matrix matrix = new Matrix();   
        float scaleWidth = ((float)w / width);   
        float scaleHeight = ((float)h / height);
        matrix.postScale(scaleWidth, scaleHeight);         
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);       
        return new BitmapDrawable(newbmp);       
    }

    static Bitmap drawableToBitmap(Drawable drawable) 
    {
        int width = drawable.getIntrinsicWidth();   
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565;         // 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);     
        Canvas canvas = new Canvas(bitmap);         
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);      
        return bitmap;
    }
	private void showAppUpdateInfo(boolean isShow)
	{
		try {
			if(isShow){
				int count = DownloadTaskManager.getInstance().getNeedUpdateAppCount();
				if(count > 0)
				{
					mHandler.removeMessages(MSG_UPDATE_APP_UPDATE_INFO);
					mHandler.sendEmptyMessageDelayed(MSG_UPDATE_APP_UPDATE_INFO, 1000);	
				}
				else
				{			
					app_update_info.setCompoundDrawablesWithIntrinsicBounds(null, null, null,null);	
					app_update_info.setText(getActivity().getString(R.string.mng_app_update_hint_info));
				}
			}else {
				mHandler.removeMessages(MSG_UPDATE_APP_UPDATE_INFO);
				mHandler.removeMessages(MSG_UPDATE_DOWN_NUM);
				mHandler.removeMessages(MSG_UPDATE_OPEN_NUM);
				app_update_info.clearAnimation();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}	
	
	private int currShowUpdateIndex = 0;
	private String[] updatedNum = null; 
	private String[] updateAppName = null;
	private String[] updateAppPName = null;
	private String[] updateAppIconUrl = null;
	
    /*private int getUpdatedAppInfo()
    {
    	int needUpdateCount = DownloadTaskManager.getInstance().getNeedUpdateAppCount();
    	
    	updatedNum = new String[needUpdateCount];
    	updateAppName = new String[needUpdateCount];  
    	updateAppPName = new String[needUpdateCount];  
    	updateAppIconUrl = new String[needUpdateCount]; 
    	
    	for(int i = 0; i < needUpdateCount; i++)
    	{
    		AppItem appitem = DownloadTaskManager.getInstance().getAllNeedUpdateAppList().get(i);
    		
    		//if(appitem.downNum!=null&&appitem.downNum.contains(MyApplication.getInstance().getResources().getString(R.string.downhistory)))
    		//{
    			//updatedNum[i] = appitem.downNum.replaceAll(MyApplication.getInstance().getResources().getString(R.string.downhistory), "");
    		//}
            if(appitem.downNum!=null)
    		{
    			updatedNum[i] = appitem.downNum;
    		}
    		updateAppName[i] = appitem.name;   
    		updateAppPName[i] = appitem.pname;   
    		updateAppIconUrl[i] = appitem.iconUrl;   
    	}
    	
    	return needUpdateCount;    	
    }//*/
    
    public Drawable getDrawable(String source) 
    {
        Drawable drawable=null;
        URL url;
        try {
        url = new URL(source);
        drawable = Drawable.createFromStream(url.openStream(), "");
        } catch (Exception e) {
        e.printStackTrace();
        return null;
        }
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());            
        return drawable;
    }
    
	int updateIndex=0;
	private void refreshAppUpadteInfo()
	{	
		try {
			List<AppItem> list=DownloadTaskManager.getInstance().getAllNeedUpdateAppList();
			int N=list.size();
			AppItem appItem=null;
			StringBuffer sb=new StringBuffer();
			if(updateIndex>=0&&updateIndex<N){
				appItem=list.get(updateIndex);
				sb.append(appItem.name);
				sb.append(getActivity().getResources().getString(R.string.mng_app_update_info_part2));
				int start=sb.length();
				sb.append(appItem.downNum);
				int end=sb.length();
				sb.append(getActivity().getResources().getString(R.string.mng_app_update_info_part4));
				SpannableStringBuilder spannable = new SpannableStringBuilder(sb.toString());
				spannable.setSpan(new ForegroundColorSpan(0xff5BC65D), start,
						 end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);	
				app_update_info.setText(spannable);
				updateIndex++;
				app_update_info.clearAnimation();
				app_update_info.startAnimation(animation);
			}else {
				updateIndex=0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	boolean isVisibleToUserNew;
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		isVisibleToUserNew=isVisibleToUser;
		showAppUpdateInfo(isVisibleToUser);
	}
	@Override
	public void onPause() {
		super.onPause();
		showAppUpdateInfo(false);
		MobclickAgent.onPageEnd("管理"); 
		MobclickAgent.onPause(ManagerFragment.this.getActivity());//时长  
	}
	
}
