package com.byt.market.qiushibaike;


import java.util.ArrayList;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.data.CateItem;
import com.byt.market.download.GamesProvider.DatabaseHelper;
import com.byt.market.qiushibaike.database.JokeDataDatabaseHelper;
import com.byt.market.qiushibaike.database.JokeDataDatabaseHelper.joke_feedback_list;
import com.byt.market.qiushibaike.ui.JokeImageFragment;
import com.byt.market.qiushibaike.ui.JokeTextFragment;
import com.byt.market.qiushibaike.ui.JokeTextImageFragment;
import com.byt.market.ui.HomeFragment;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class JokeActivity extends FragmentActivity implements OnPageChangeListener, OnClickListener {
	private static final String TAG = "JokeActivity";
	private AlwsydMarqueeTextView tv_title;
	//, button_joke_text
	private Button button_jokehome, button_joke_image;
    private View jokehome_line, joke_text_tab_line, joke_image_tab_line;
    private View jokeView;
    private ViewPager mPager;
    private ArrayList<BaseUIFragment> fragmentList; 
    private JokeTextImageFragment jokeTextImageFragment;
  //  private JokeTextFragment jokeTextFragment;
    private JokeImageFragment jokeImageFragment;
    private int REQUEST_REFRESH = 0;
    private MyFragmentPagerAdapter mAdapter;
    private Fragment fragment;
    private RelativeLayout qiushilayout;//糗事百科布局
	public static final String DATABASE_NAME = "jokefeedback";
	public static final int DATABASE_VERSION = 1;    
	public static long umtime;
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.joke_main);
		if(getIntent().getStringExtra("notif")!=null){
			//腾讯
			//StatService.trackCustomEvent(this,"Notification", getIntent().getStringExtra("cid"));
			SharedPreferences yuVer = MyApplication.getInstance()
					.getSharedPreferences("yulever", 0);
			SharedPreferences.Editor editor = yuVer.edit();
			editor.putInt(MainActivity.JOKE_KEY, 1);
			editor.commit();
		}
		initView();
		initPager();
		
		createDatebase();
		umtime = System.currentTimeMillis();
	}
	
	private void initView()
	{
		try {
			findViewById(R.id.topline4).setVisibility(View.GONE);
			findViewById(R.id.top_downbutton).setVisibility(View.GONE);
			findViewById(R.id.titlebar_applist_button_container).setVisibility(View.GONE);
			findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
			findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
			findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);			
			tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
			tv_title.setText(getString(R.string.joke));
			findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
			findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
			findViewById(R.id.top_downbutton).setOnClickListener(this);		
			button_jokehome = (Button)findViewById(R.id.jokehome_button);
			button_jokehome.setOnClickListener(this);
			button_jokehome.setSelected(true);
		//	button_joke_text = (Button)findViewById(R.id.joke_text_button);
		//	button_joke_text.setOnClickListener(this);
			button_joke_image = (Button)findViewById(R.id.joke_image_button);
			button_joke_image.setOnClickListener(this);		
			jokehome_line = (View)findViewById(R.id.jokehome_line);
			jokehome_line.setVisibility(View.VISIBLE);
		//	joke_text_tab_line = (View)findViewById(R.id.joke_text_tab_line);
		//	joke_text_tab_line.setVisibility(View.INVISIBLE);
			joke_image_tab_line = (View)findViewById(R.id.joke_image_tab_line);
			joke_image_tab_line.setVisibility(View.INVISIBLE);		
			jokeView = findViewById(R.id.joke_area);
			jokeView.setVisibility(View.VISIBLE);
			qiushilayout=(RelativeLayout) findViewById(R.id.qiushiheadlayout);
			TextView switch_date=(TextView)findViewById(R.id.switch_date);
			switch_date.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View vv) {
					try {
						Log.d("xxx", "vvvvv");
						jokeImageFragment.updaterequest();						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
		}catch (Exception e) {
			Log.d(TAG,"initViewerror");
			e.printStackTrace();
		}

	}
	private void initPager()
	{
		mPager = (ViewPager)findViewById(R.id.jokeviewpager);
		fragmentList = new ArrayList<BaseUIFragment>();
		
		//jokeTextImageFragment = new JokeTextImageFragment(false);
		jokeTextImageFragment = new JokeTextImageFragment();
		jokeTextImageFragment.setISjoke(false);
	//	jokeTextFragment = new JokeTextFragment();
		jokeImageFragment = new JokeImageFragment();
		jokeImageFragment.setISjoke(false);
		fragmentList.clear();
		fragmentList.add(jokeTextImageFragment);
	//	fragmentList.add(jokeTextFragment);
		fragmentList.add(jokeImageFragment);		
		mAdapter = new MyFragmentPagerAdapter(this.getSupportFragmentManager(), fragmentList);
		mPager.setAdapter(mAdapter); 
        mPager.setCurrentItem(0); 
        mPager.setOnPageChangeListener(this);	
        fragment = mAdapter.list.get(0);
	}

	public class MyFragmentPagerAdapter extends FragmentPagerAdapter
	{
		ArrayList<BaseUIFragment> list;  
        public MyFragmentPagerAdapter(FragmentManager fm,ArrayList<BaseUIFragment> list) {  
            super(getSupportFragmentManager());  
            this.list = list;  
        } 

		@Override
		public Fragment getItem(int arg0) {			
			return list.get(arg0);  
		}

		@Override
		public int getCount() {			
			 return list.size();  
		}
		
	}
	
	public void finishWindow() {
		finish();
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch(view.getId())
		{
		    case R.id.jokehome_button:
		    	mPager.setCurrentItem(0);
			    break;
		    case R.id.joke_image_button:
		    	mPager.setCurrentItem(1);
			    break;	
		    case R.id.titlebar_back_arrow:
				finishWindow();
				break;			    
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		Log.d("newzx", "onPageScrollStateChanged arg0="+arg0);
		if(mPager.getCurrentItem()==1&&arg0==0)
		{
			qiushilayout.setVisibility(View.VISIBLE);
		}else{
			qiushilayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		qiushilayout.setVisibility(View.GONE);
		Log.d("newzx", "onPageScrolled arg0="+arg0+"arg1="+arg1+"arg2="+arg2);
	}

	@Override
	public void onPageSelected(int page) {
		Log.d("newzx", "onPageSelected page="+page);
		fragment = mAdapter.list.get(page);
		switch(page)
		{
		    case 0:
		    	onMyPageChange(0);
			    break;
		    case 1:
		    	onMyPageChange(1);
			    break;			    
		}		
	}
	
	private void onMyPageChange(int page)
	{
		switch(page)
		{
		    case 0:
				button_jokehome.setSelected(true);
				button_joke_image.setSelected(false);
				jokehome_line.setVisibility(View.VISIBLE);
				joke_image_tab_line.setVisibility(View.INVISIBLE);			    	
			    break;
		    case 1:
				button_jokehome.setSelected(false);
				button_joke_image.setSelected(true);
				jokehome_line.setVisibility(View.INVISIBLE);
				joke_image_tab_line.setVisibility(View.VISIBLE);	
				try {
					if (jokeImageFragment != null) {
						jokeImageFragment.request();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}				
			    break;		    
		}		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if(requestCode == REQUEST_REFRESH)
		{
			if(resultCode == Constants.COMMENT_RESPONSE_CODE)
			{
				Bundle extras = intent.getExtras();
				CateItem cate = extras.getParcelable("jokecommcate");
				int count = extras.getInt("jokecomm_count");
				if(fragment instanceof JokeTextImageFragment)
				{
					((JokeTextImageFragment)fragment).refreshItem(cate,count);
				}
				else if(fragment instanceof JokeTextFragment)
				{
					((JokeTextFragment)fragment).refreshItem(cate,count);
				}
				else if(fragment instanceof JokeImageFragment)
				{
					((JokeImageFragment)fragment).refreshItem(cate,count);
				}
				
				if(queryDataIsExists(Integer.toString(cate.sid)))
				{
					updateData(Integer.toString(cate.sid),1, 0);
				}
				else
				{
					insertData(Integer.toString(cate.sid),1, 0);
				}				
			}
		}
	}
	
	private void createDatebase()
	{
		//JokeDataDatabaseHelper dbHelper = new JokeDataDatabaseHelper(JokeActivity.this, DATABASE_NAME, DATABASE_VERSION);
		JokeDataDatabaseHelper dbHelper = JokeDataDatabaseHelper.getInstance(JokeActivity.this);
		SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
	}
	
	public void  insertData(String sid, int comflag, int voteflag)
	{
		ContentValues values = new ContentValues();
		values.put(joke_feedback_list.JOKESID, sid);
		//值为"1"时才写入数据库，"0"时则保持原数据
		if(comflag == 1)
		{
		    values.put(joke_feedback_list.JOKECOMMFLAG, comflag);
		}
		if(voteflag == 1)
		{
		    values.put(joke_feedback_list.JOKEVOTEFLAG, voteflag);
		}
		//JokeDataDatabaseHelper dbHelper = new JokeDataDatabaseHelper(JokeActivity.this, DATABASE_NAME, DATABASE_VERSION);
		JokeDataDatabaseHelper dbHelper = JokeDataDatabaseHelper.getInstance(JokeActivity.this);
		SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
		sqliteDatabase.insert(DATABASE_NAME, null, values);
	}
	
	public void  updateData(String sid, int comflag, int voteflag)
	{
		ContentValues values = new ContentValues();
		//值为"1"时才写入数据库，"0"时则保持原数据
		if(comflag == 1)
		{
		    values.put(joke_feedback_list.JOKECOMMFLAG, comflag);
		}
		if(voteflag == 1)
		{
		    values.put(joke_feedback_list.JOKEVOTEFLAG, voteflag);
		}		
		//JokeDataDatabaseHelper dbHelper = new JokeDataDatabaseHelper(JokeActivity.this, DATABASE_NAME, DATABASE_VERSION);
		
		JokeDataDatabaseHelper dbHelper = JokeDataDatabaseHelper.getInstance(JokeActivity.this);
		SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
		sqliteDatabase.update(DATABASE_NAME, values, "sid=?",new String[]{sid});
	}
	
	public boolean queryDataIsExists(String sid)
	{
        boolean isExists = false;
		
		//JokeDataDatabaseHelper dbHelper = new JokeDataDatabaseHelper(JokeActivity.this, DATABASE_NAME, DATABASE_VERSION);
        JokeDataDatabaseHelper dbHelper = JokeDataDatabaseHelper.getInstance(JokeActivity.this);
		SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
		Cursor cursor = sqliteDatabase.query(DATABASE_NAME, new String[]{joke_feedback_list.JOKESID}, "sid=?", new String[]{sid}, null, null, null);
		while(cursor.moveToNext())
		{
			sid = cursor.getString(cursor.getColumnIndex(joke_feedback_list.JOKESID));
			if(sid != null)
			{
				isExists = true;
			}
		}
		cursor.close();
		return isExists;
	}	
	public void queryAllData()
	{
		String sid;
		String iscommflag;
		String voteflag;
		
		//JokeDataDatabaseHelper dbHelper = new JokeDataDatabaseHelper(JokeActivity.this, DATABASE_NAME, DATABASE_VERSION);
		JokeDataDatabaseHelper dbHelper = JokeDataDatabaseHelper.getInstance(JokeActivity.this);
		SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
		Cursor cursor = sqliteDatabase.query(DATABASE_NAME, null, null, null, null, null, null);
		while(cursor.moveToNext())
		{
			sid = cursor.getString(cursor.getColumnIndex(joke_feedback_list.JOKESID));
			iscommflag = cursor.getString(cursor.getColumnIndex(joke_feedback_list.JOKECOMMFLAG));
			voteflag = cursor.getString(cursor.getColumnIndex(joke_feedback_list.JOKEVOTEFLAG));
		}
		cursor.close();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		int endtime = (int) (System.currentTimeMillis()-umtime);
		MobclickAgent.onEventValue(this, "Funn", null,endtime/1000);
		Log.d("nnlog", "糗百---"+endtime/1000);
	}
}
