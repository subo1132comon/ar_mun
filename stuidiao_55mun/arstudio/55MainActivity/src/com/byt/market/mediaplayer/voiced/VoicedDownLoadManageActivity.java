package com.byt.market.mediaplayer.voiced;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.DownLoadManageActivity;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.adapter.MyListAdapter;
import com.byt.market.adapter.MyListAdapter.RecordLayoutHolder;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadAppItem;
import com.byt.market.data.DownloadItem;
import com.byt.market.data.UserData;
import com.byt.market.download.DownloadBtnClickListener;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.ui.MusicDownedFragment;
import com.byt.market.mediaplayer.ui.MusicDowningFragment;
import com.byt.market.mediaplayer.ui.MusicSubFragment;
import com.byt.market.mediaplayer.ui.RingFragment;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.DataUtil;
import com.byt.market.util.LogUtil;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.SystemUtil;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.byt.market.view.LoadFailedView;

/**
 * 音乐下载管理
 */
public class VoicedDownLoadManageActivity extends FragmentActivity implements OnPageChangeListener, OnClickListener  {


	private View recArea;
	AlwsydMarqueeTextView tv_title;
	private VoicedDownedFragment homeFragment;
	private MusicDowningFragment homeFragment2;
	private View ringhome_line,ringrank_line;
	Button buttonringrec,buttonringhome,buttonringhot,buttonringrank;
	//播放小窗口
	ImageView playBaricon;
	TextView musicName,musicAuthor;
	ImageButton playBarButton,playBarNextButton;
	int isfrome;
	//播放小窗口
	ViewPager mPager;
    private ArrayList<BaseUIFragment> fragmentList;  
    //public  static VoicedDownLoadManageActivity instence;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//instence=this;
		setContentView(R.layout.downloaded_media_main_fragment);
		initView();
		initPaper();
	}

	private void initPaper() {
		 isfrome=getIntent().getIntExtra("isfrome", 0);
		 mPager = (ViewPager)findViewById(R.id.ringviewpager);  
	        fragmentList = new ArrayList<BaseUIFragment>();  
	        homeFragment = new VoicedDownedFragment();
	        homeFragment2 = new MusicDowningFragment();	       
			fragmentList.clear();
	        fragmentList.add(homeFragment);  
	        fragmentList.add(homeFragment2);
	              //给ViewPager设置适配器  	       
	        mPager.setAdapter(new MyFragmentPagerAdapter(this.getSupportFragmentManager(), fragmentList));  
	        if(isfrome==1)
	        {
	        mPager.setCurrentItem(0);
	   	 buttonringhome.setSelected(true);
			buttonringrank.setSelected(false);
			ringhome_line.setVisibility(View.VISIBLE);
			ringrank_line.setVisibility(View.INVISIBLE);	
	        }else{
	        	 mPager.setCurrentItem(1);
	        	 buttonringhome.setSelected(false);
					buttonringrank.setSelected(true);
					ringhome_line.setVisibility(View.INVISIBLE);
					ringrank_line.setVisibility(View.VISIBLE);	
	        }
	        mPager.setOnPageChangeListener(this);//页面变化时的监听器  
		
	}

	private void initView() {
		try {
			findViewById(R.id.topline4).setVisibility(View.GONE);
			findViewById(R.id.top_downbutton).setVisibility(View.VISIBLE);
			findViewById(R.id.titlebar_applist_button_container).setVisibility(
					View.GONE);
			findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
			findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
			findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
			tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
			tv_title.setText(R.string.text_download_manager_title);
			findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
			findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
			findViewById(R.id.top_downbutton).setVisibility(View.GONE);
			buttonringhome=(Button) findViewById(R.id.ringhome_button);
			buttonringhome.setText(R.string.downloaded_title_desc);
			buttonringrank=(Button) findViewById(R.id.ring_rank_button);
			buttonringrank.setText(R.string.downloading_title_desc);
			buttonringhome.setOnClickListener(this);
			buttonringrank.setOnClickListener(this);
			recArea = findViewById(R.id.ring_area);
			ringhome_line=findViewById(R.id.ringhome_line);
			ringrank_line=findViewById(R.id.ring_rank_tab_line);		
			recArea.setVisibility(View.VISIBLE);
			 playBaricon=(ImageView) findViewById(R.id.musicplayicon);
			 musicName=(TextView) findViewById(R.id.musicname);
			 musicAuthor=(TextView) findViewById(R.id.musicauther);
			 playBarButton=(ImageButton) findViewById(R.id.playbutton);
			 playBarNextButton=(ImageButton) findViewById(R.id.playnextbutton);
			 playBarButton.setOnClickListener(this);
			 playBarNextButton.setOnClickListener(this);
			 findViewById(R.id.playbarlayout).setVisibility(View.GONE);
			
				 
			
		} catch (Exception e) {
			Log.d("myzx","initViewerror");
			e.printStackTrace();
		}

	}
	@Override
	public void onClick(View view) {
		try {
			switch (view.getId()) {
			case R.id.ringhome_button:
				 mPager.setCurrentItem(0);
				 break;
			case R.id.ring_rank_button:
				 mPager.setCurrentItem(1);
				break;
			case R.id.titlebar_back_arrow:
				finishWindow();
				break;
			case R.id.titlebar_search_button:
				startActivity(new Intent(Constants.TOSEARCH));
				break;
			case R.id.playbutton://播放按钮

				break;
			case R.id.playnextbutton://播放下一首

				break;
			default:
				break;
			}
		} catch (Exception e) {
			Log.d("myzx","ionClickViewerror");
			e.printStackTrace();
		}
	}

	public void requestDelay() {
		try {
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public class MyFragmentPagerAdapter extends FragmentPagerAdapter{  
        ArrayList<BaseUIFragment> list;  
        public MyFragmentPagerAdapter(FragmentManager fm,ArrayList<BaseUIFragment> list) {  
            super(getSupportFragmentManager());  
            this.list = list;               
        }  
          
          
        @Override  
        public int getCount() {  
            return list.size();  
        }  
          
        @Override  
        public BaseUIFragment getItem(int arg0) {  
            return list.get(arg0);  
        }  
          
          
          
          
    }
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		 switch(arg0){
         case 0:
        	onMYPageChange(0);
         	break;
         case 1:
        	 onMYPageChange(1);
         	 break;   
         }		
	}  
	private void onMYPageChange(int page) {
		switch(page)
		{
		case 0:
				buttonringhome.setSelected(true);
				buttonringrank.setSelected(false);
				requestDelay();
				ringhome_line.setVisibility(View.VISIBLE);
				ringrank_line.setVisibility(View.INVISIBLE);	
        	break; 
		case 1:
			buttonringhome.setSelected(false);
			buttonringrank.setSelected(true);
			ringhome_line.setVisibility(View.INVISIBLE);
			ringrank_line.setVisibility(View.VISIBLE);	
    	break; 
		
		}

	}
	public void finishWindow() {
		finish();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}
}
