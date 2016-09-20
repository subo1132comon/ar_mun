package com.byt.market.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byt.ar.R;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.data.BigItem;
import com.byt.market.qiushibaike.ui.JokeImageFragment;
import com.byt.market.qiushibaike.ui.JokeTextImageFragment2;
import com.byt.market.tools.LogCart;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.umeng.analytics.MobclickAgent;

public class JokeFragment extends ListViewFragment implements OnClickListener, OnPageChangeListener {

	private static final String TAG = "JokeActivity";
	private AlwsydMarqueeTextView tv_title;
	//, button_joke_text
	private Button button_jokehome, button_joke_image;
    private View jokehome_line, joke_text_tab_line, joke_image_tab_line;
    private View jokeView;
    private ViewPager mPager;
    private ArrayList<BaseUIFragment> fragmentList; 
    private JokeTextImageFragment2 jokeTextImageFragment;
  //  private JokeTextFragment jokeTextFragment;
    private JokeImageFragment jokeImageFragment;
    private int REQUEST_REFRESH = 0;
    private MyFragmentPagerAdapter mAdapter;
    private Fragment fragment;
    private RelativeLayout qiushilayout;//糗事百科布局
	public static final String DATABASE_NAME = "jokefeedback";
	public static final int DATABASE_VERSION = 1;    
	
	
	@Override
	public void onAppClick(Object obj, String action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getRequestUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getRefoushtUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getRequestContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getLayoutResId() {
		// TODO Auto-generated method stub
		return R.layout.joke_main;
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(res, container, false);
		Log.d("nnlog", "onCreateView--joke");
		return view;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		MobclickAgent.onPageStart("底部糗百"); //统计页面
//	    MobclickAgent.onResume(JokeFragment.this.getActivity());//时长  
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		MobclickAgent.onPageEnd("底部糗百"); 
//		MobclickAgent.onPause(JokeFragment.this.getActivity());//时长  
	}
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		Log.d("nnlog", "onAttach");
	}
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		Log.d("nnlog", "onDetach");
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		Log.d("nnlog", "onDestroyView");
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("nnlog", "onDestroy");
	}
	@Override
	public void initViewBYT(View view) {
		// TODO Auto-generated method stub
		super.initViewBYT(view);
		try {
			view.findViewById(R.id.topline4).setVisibility(View.GONE);
			view.findViewById(R.id.top_downbutton).setVisibility(View.GONE);
			view.findViewById(R.id.titlebar_applist_button_container).setVisibility(View.GONE);
			view.findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
			view.findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
			view.findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);			
			tv_title = (AlwsydMarqueeTextView) view.findViewById(R.id.titlebar_title);
			tv_title.setText(getString(R.string.joke));
			view.findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
			view.findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
			view.findViewById(R.id.top_downbutton).setOnClickListener(this);		
			button_jokehome = (Button)view.findViewById(R.id.jokehome_button);
			button_jokehome.setOnClickListener(this);
			button_jokehome.setSelected(true);
		//	button_joke_text = (Button)findViewById(R.id.joke_text_button);
		//	button_joke_text.setOnClickListener(this);
			button_joke_image = (Button)view.findViewById(R.id.joke_image_button);
			button_joke_image.setOnClickListener(this);		
			jokehome_line = (View)view.findViewById(R.id.jokehome_line);
			jokehome_line.setVisibility(View.VISIBLE);
		//	joke_text_tab_line = (View)findViewById(R.id.joke_text_tab_line);
		//	joke_text_tab_line.setVisibility(View.INVISIBLE);
			joke_image_tab_line = (View)view.findViewById(R.id.joke_image_tab_line);
			joke_image_tab_line.setVisibility(View.INVISIBLE);		
			jokeView = view.findViewById(R.id.joke_area);
			jokeView.setVisibility(View.VISIBLE);
			qiushilayout=(RelativeLayout) view.findViewById(R.id.qiushiheadlayout);
			TextView switch_date=(TextView)view.findViewById(R.id.switch_date);
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
		
		mPager = (ViewPager)view.findViewById(R.id.jokeviewpager);
		fragmentList = new ArrayList<BaseUIFragment>();
		
		//jokeTextImageFragment = new JokeTextImageFragment(true);
		jokeTextImageFragment = new JokeTextImageFragment2();
		jokeTextImageFragment.setISjoke(true);
		
	//	jokeTextFragment = new JokeTextFragment();
		jokeImageFragment = new JokeImageFragment();
		jokeImageFragment.setISjoke(true);
		fragmentList.clear();
		fragmentList.add(jokeTextImageFragment);
	//	fragmentList.add(jokeTextFragment);
		fragmentList.add(jokeImageFragment);		
		mAdapter = new MyFragmentPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
		mPager.setAdapter(mAdapter); 
        mPager.setCurrentItem(0); 
        mPager.setOnPageChangeListener(this);	
        fragment = mAdapter.list.get(0);
	}
	
	@Override
	protected ImageAdapter createAdapter() {
		// TODO Auto-generated method stub
		setIsjoke(true);
		return new Jokeadpter();
	}

	@Override
	protected void onDownloadStateChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
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
	}

	@Override
	public void onPageSelected(int page) {
		// TODO Auto-generated method stub
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
	//
	
	
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
	
	private class Jokeadpter extends ImageAdapter{

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void bindView(int position, BigItem item, BigHolder holder) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class MyFragmentPagerAdapter extends FragmentPagerAdapter
	{
		ArrayList<BaseUIFragment> list;  
        public MyFragmentPagerAdapter(FragmentManager fm,ArrayList<BaseUIFragment> list) {  
            super(getActivity().getSupportFragmentManager());  
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
		getActivity().finish();
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch(v.getId())
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
	protected List<BigItem> dblistData() {
		// TODO Auto-generated method stub
		return null;
	}

}
