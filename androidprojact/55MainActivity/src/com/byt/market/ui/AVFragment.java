package com.byt.market.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.byt.market.R;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.adapter.ImageAdapter.BigHolder;
import com.byt.market.data.BigItem;
import com.byt.market.mediaplayer.MusicDownLoadManageActivity;
import com.byt.market.mediaplayer.ui.LiveMainFragment;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.view.AlwsydMarqueeTextView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class AVFragment extends ListViewFragment implements OnClickListener, OnPageChangeListener{
	
	private View recArea;
	AlwsydMarqueeTextView tv_title;
	private LiveMainFragment movieFragment;
	// private VallayFragment vallayFragment;
	private View movie_line, holly_line;
	Button buttonmovie, buttonholly;
	ViewPager mPager;
	private ArrayList<BaseUIFragment> fragmentList;
	
	@Override
	public void initViewBYT(View view) {
		// TODO Auto-generated method stub
		super.initViewBYT(view);
		try {
			view.findViewById(R.id.topline4).setVisibility(View.GONE);
			view.findViewById(R.id.top_downbutton).setVisibility(View.VISIBLE);
			view.findViewById(R.id.titlebar_applist_button_container).setVisibility(View.GONE);
			view.findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
			view.findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
			view.findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
			view.findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
			view.findViewById(R.id.top_downbutton).setOnClickListener(this);
			tv_title = (AlwsydMarqueeTextView) view.findViewById(R.id.titlebar_title);
			// tv_title.setText(R.string.movie_string);
			tv_title.setText("AV");
			view.findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
			recArea.setVisibility(View.VISIBLE);

		} catch (Exception e) {
			Log.d("myzx", "initViewerror");
			e.printStackTrace();
		}

	}
	
	public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		ArrayList<BaseUIFragment> list;

		public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<BaseUIFragment> list) {
			super(getActivity().getSupportFragmentManager());
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
	
	private class AVadpter extends ImageAdapter{

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
	
	@Override
	public void onAppClick(Object obj, String action) {
		// TODO Auto-generated method stub
		
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
	protected List<BigItem> dblistData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getLayoutResId() {
		// TODO Auto-generated method stub
		return R.layout.av_main_fragment;
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(res, container, false);
		return view;
	}

	@Override
	protected ImageAdapter createAdapter() {
		// TODO Auto-generated method stub
		setIsjoke(true);
		return new AVadpter();
	}

	@Override
	protected void onDownloadStateChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub
		
	}

	public void finishWindow() {
		getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}
	
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("nnlog", "av----onstart");
	}
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		Log.d("nnlog", "av---onAttach");
	}
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		Log.d("nnlog", "av---onDetach");
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		Log.d("nnlog", "av---onDestroyView");
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("nnlog", "av---onDestroy");
	}
	
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.titlebar_back_arrow:
			finishWindow();
			break;
		case R.id.top_downbutton:
			Intent downloadIntent = new Intent(getActivity(), MusicDownLoadManageActivity.class);
			startActivity(downloadIntent);
			getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		}

	}
}
