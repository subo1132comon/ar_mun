package com.byt.market.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.PushInfo;
import com.byt.market.data.UserData;
import com.byt.market.service.HomeLoadService;
import com.byt.market.util.FileUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.StringUtil;
import com.byt.market.util.SysNotifyUtil;
import com.byt.market.util.Util;



/**
 * Splash闪屏界面
 */
public class HomeActivity extends BaseActivity implements TaskListener{
	/*add by zengxiao*/
	private LayoutInflater mLayoutInflater;
	ViewPager mPager;
	private final String INITSTATE="initstate";
	private final String INITSTRING="isinit";
	int init=0;
	 List<View>mViews;
	 private static final int mImgs[] = {
 };
	 
	 /*add end*/
	private HashMap<String, String> map;
	public static HomeActivity pActivity;
	private RelativeLayout market_loading_bg;
	private int mExtTabID = -1;
	private SharedPreferences spinfo;
	private Editor editorinfo;
	private HomeLoadService loadService;
	private TextView	tvVersion;
	private String userName=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mExtTabID = this.getIntent().getIntExtra(MainActivity.EXT_TAB_ID, -1);
		setContentView(R.layout.preview_view);
		pActivity = this;
		initView();
		addEvent();
		initdata();
		loadService = new HomeLoadService();
		startService(new Intent(HomeActivity.this, HomeLoadService.class));
		initScreenInfo();
//		UmengUpdateAgent.setUpdateOnlyWifi(false);
//		UmengUpdateAgent.update(this); // 暂用umeng更新
		userName=SharedPrefManager.getLastLoginUserName(this);
		if("".equals(userName)||userName==null){
			HashMap<String,String> infos=SharedPrefManager.getClintInfo(this);
			String cid=infos.get(SharedPrefManager.CID);
			if(cid!=null){
				userName=cid;
				SharedPrefManager.setLastLoginUserName(this, userName);
			}
		}
		if("".equals(userName)||userName==null){
			doUserRegister();
		}else {
			doUserLogin(userName);
		}
	}

	private void initdata() {
		// TODO Auto-generated method stub
		  View enditem = mLayoutInflater.inflate(R.layout.pager_end, null);
	            for(int i=0;i<mImgs.length;i++)
	            {
	            	
	                View item = mLayoutInflater.inflate(R.layout.pager_item, null);
	                LinearLayout viewbg=(LinearLayout) item.findViewById(R.id.pageshowbg);
	                viewbg.setBackgroundResource(mImgs[i]);
	                
	               TextView img = (TextView)item.findViewById(R.id.pageshowtext);
	               if(i==0)
	                img.setText(R.string.oobe_text1);
	               else
	            	   img.setText(R.string.oobe_text2);
	                mViews.add(item);
	            }
	            TextView endimg = (TextView)enditem.findViewById(R.id.pageendshowtext);
	            endimg.setText(R.string.oobe_text3);
	            View endPage = (View)enditem.findViewById(R.id.page_end);
	             
//	            endPage.setBackgroundResource(R.drawable.oobe_3);
	            mViews.add(enditem);
	            HelpPagerAdapter adapter = new HelpPagerAdapter(this, mViews);
	            
	            mPager.setAdapter(adapter);
	            mPager.setCurrentItem(0,false);
	            mPager.setVisibility(View.GONE);
	            Button endBtn = (Button)enditem.findViewById(R.id.endButton);
	            
	            endBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						SharedPreferences initshare=getSharedPreferences(INITSTATE, 0);
						SharedPreferences.Editor edit=initshare.edit();
						edit.putInt(INITSTRING, 1);
						edit.commit();
						handler.sendEmptyMessageDelayed(0, 1000);
						mPager.setVisibility(View.GONE);
						
					}
				});
	        
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		loadService = new HomeLoadService();
	}

	private void initScreenInfo() {
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		MyApplication.density = outMetrics.density;
		MyApplication.mScreenHeight = outMetrics.heightPixels;
		MyApplication.mScreenWidth = outMetrics.widthPixels;
	}

	private String loadWel() {
		String iconName = StringUtil.md5Encoding(Constants.IMG_URL + Constants.HOME_BG);
		String path = FileUtil.getIconPath() + iconName + ".png";
		File f = new File(path);
		if (f.exists())
			return path;
		return null;
	}

	@Override
	public void initView() {
		  mPager = (ViewPager)findViewById(R.id.pager);
	        
	        mLayoutInflater = LayoutInflater.from(this);
	        
	        mViews = new ArrayList<View>();
		market_loading_bg = (RelativeLayout) findViewById(R.id.market_loading_bg);
		tvVersion = (TextView) findViewById(R.id.tv_version);
		PackageInfo info;
		try {
			info = getPackageManager().getPackageInfo( getPackageName(), 0);
			String vName = info.versionName;
			tvVersion.setText(getString(R.string.down_summary) + vName);
		} catch (NameNotFoundException e) {
			tvVersion.setVisibility(View.GONE);
			e.printStackTrace();
		}
		String path = loadWel();
		if (path != null)
			market_loading_bg.setBackgroundDrawable(BitmapDrawable.createFromPath(path));
		spinfo = getSharedPreferences("info", Context.MODE_PRIVATE);
		sp = getSharedPreferences("openfailtime", Context.MODE_PRIVATE);
		editor = sp.edit();
		editorinfo = spinfo.edit();
		editorinfo.putBoolean("isClientActive", true);
		editorinfo.commit();
		SharedPreferences initshare=getSharedPreferences(INITSTATE, 0);
		init=initshare.getInt(INITSTRING, 0);
		if(init!=1)
		{
			mPager.setVisibility(View.VISIBLE);
		}
		
		new Thread() {
			public void run() {
				String apkFilename = Constants.APKNAME;
				// 删除临时文件
				File downFile = new File(MyApplication.DATA_DIR + File.separator + apkFilename);
				if (downFile != null && downFile.exists())
					downFile.delete();
				downFile = new File(getFilesDir() + File.separator + apkFilename);// getFilesDir()=/data/data/com.byt.market/files
				if (downFile != null && downFile.exists())
					downFile.delete();
				map = Util.getUplodMap(HomeActivity.this, Constants.LOGIN);
				Util.POSTDATA = null;
				Util.POSTDATA = Util.getLogPostData(HomeActivity.this);
				Util.getDataPointMap(HomeActivity.this);
				/*if(init==1)
				{*/
					handler.sendEmptyMessageDelayed(0, 2500);
				/*}*/
				

			}
		}.start();
	}

	@Override
	public void initData() {

	}

	private Object getHeader() {
		return map;
	}

	private Object tag() {
		return getClass().getSimpleName();
	}

	private Object getRequestContent() {
		return Util.POSTDATA;
		// return null;
	}

	private Object getRequestUrl() {
		return Constants.LIST_URL + "?qt=" + Constants.PLINFO;
	}

	@Override
	public void addEvent() {
		
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				loadService.initData(HomeActivity.this, maContext, getRequestUrl(), getRequestContent(), tag(), getHeader());

				editor.putLong("fail", 1);
				editor.commit();
				Intent intent = new Intent(HomeActivity.this, MainActivity.class);
				if (mExtTabID != -1) {
					intent.putExtra(MainActivity.EXT_TAB_ID, mExtTabID);
				}
				startActivity(intent);
				finish();
				break;
			case 2:
				editor.putLong("fail", 0);
				editor.commit();
				break;
			}
		};
	};

	protected void showNotify(PushInfo pInfo) {
		SysNotifyUtil.notifyPushInfo(this, maContext, R.drawable.app_content_btn, pInfo);
	}
	
	private void doUserLogin(String userName){
		ProtocolTask task = new ProtocolTask(this);
		task.setListener(this);
		HashMap<String,String> infos=SharedPrefManager.getClintInfo(this);
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
		ProtocolTask task = new ProtocolTask(this);
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
				SharedPrefManager.setLastLoginUserName(this,
						user.getUid());
				MyApplication.getInstance().setUser(user);
				MyApplication.getInstance().getUser().setLogin(true);
				MyApplication.getInstance().updateUserInfo();
				Constants.ISSHOW = true;
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
						SharedPrefManager.setLastLoginUserName(this,
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
	/*add by zengxiao for:增加首次进入功能介绍
	 * 20140620
	 * */
private class HelpPagerAdapter extends PagerAdapter {       
        
        List<View> mViews;
        Context mContext;
        
        public HelpPagerAdapter(Context context, List<View> views){
            this.mViews = views;
            this.mContext=context;
        }
        
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if(null != mViews)
            {
                return mViews.size();
            }
            
            return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            // TODO Auto-generated method stub
            return view == (object);
        }
        
        @Override
        public Object instantiateItem(View collection, int position) {
            
            try {
                ((ViewPager) collection).addView(mViews.get(position),0);
            } catch (Exception e) {
            }
            
            return mViews.get(position);
        }
        
        @Override
        public void destroyItem(View collection, int position, Object arg2) {
            ((ViewPager) collection).removeView(mViews.get(position));
        }
    }
	
	/*add end*/
	
	
}
