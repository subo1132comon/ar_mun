package com.byt.market.mediaplayer.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.DownLoadManageActivity;
import com.byt.market.activity.MainActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.BigItem;
import com.byt.market.data.CateItem;
import com.byt.market.data.PageInfo;
import com.byt.market.tools.LogCart;
import com.byt.market.tools.MD5Tools;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.util.filecache.FileCacheUtil;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.byt.market.view.LoadFailedView;

public class LiveMainFragment extends BaseUIFragment implements OnClickListener, OnPageChangeListener, TaskListener {
	private List<View> recArealist;
	private List<LiveFragment> movieFragmentlist;
	private List<View> tab_linelist;
	private List<Button> tab_buttonlist;
	ViewPager mPager;
	LinearLayout cate_bottom;// tab栏按钮
	private ArrayList<BaseUIFragment> fragmentList;
	private ProtocolTask mTask;
	private RelativeLayout listview_loading;
	private LoadFailedView listview_loadfailed;
	private TextView videotitlebg;
	private String md5 = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.livefragments, container, false);
		initView(view);
		initPaper(view);
		requestmain();
		return view;
	}

	private void initPaper(View view) {

		mPager = (ViewPager) view.findViewById(R.id.viewpager);
		fragmentList = new ArrayList<BaseUIFragment>();
		fragmentList.clear();
	}

	private void initView(View view) {
		try {
			cate_bottom = (LinearLayout) view.findViewById(R.id.cate_bottom);
			movieFragmentlist = new ArrayList<LiveFragment>();
			tab_linelist = new ArrayList<View>();
			tab_buttonlist = new ArrayList<Button>();
			listview_loading = (RelativeLayout) view.findViewById(R.id.listview_loading);
			listview_loadfailed = (LoadFailedView) view.findViewById(R.id.listview_loadfailed);
			listview_loadfailed.setButtonVisible(View.GONE);
			listview_loading.setVisibility(View.VISIBLE);
			videotitlebg = (TextView) view.findViewById(R.id.videotitlebg);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View view) {

	}

	public void requestDelay() {
		try {
			if (movieFragmentlist != null) {
				movieFragmentlist.get(0).request();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		ArrayList<BaseUIFragment> list;

		public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<BaseUIFragment> list) {
			super(LiveMainFragment.this.getChildFragmentManager());
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
		onMYPageChange(arg0);
	}

	private void onMYPageChange(int page) {
		for (int i = 0; i < movieFragmentlist.size(); i++) {
			if (page == i) {
				movieFragmentlist.get(i).request();
				tab_linelist.get(i).setVisibility(View.VISIBLE);
				tab_buttonlist.get(i).setSelected(true);

			} else {
				tab_linelist.get(i).setVisibility(View.INVISIBLE);
				tab_buttonlist.get(i).setSelected(false);
			}
		}

	}

	@Override
	public void onNoNetworking() {
		listview_loadfailed.setVisibility(View.GONE);
	}

	@Override
	public HashMap<String, String> getHeader() {
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		if (imei == null)
			imei = Util.getIMEI(MyApplication.getInstance());
		if (vcode == null)
			vcode = Util.getVcode(MyApplication.getInstance());
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(MyApplication.getInstance());
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("channel", channel);
		return map;
	}

	protected String tag() {
		return getClass().getSimpleName();
	}

	@Override
	public void onNetworkingError() {
		listview_loadfailed.setText(MyApplication.getInstance().getString(R.string.data_loadingerr));
		listview_loading.setVisibility(View.GONE);
		listview_loadfailed.setVisibility(View.VISIBLE);
		videotitlebg.setVisibility(View.GONE);
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		try {
			if (bytes != null) {
				JSONObject result = new JSONObject(new String(bytes));
				pullJson(false, result);
			} else {
				setLoadfailedView();
			}
		} catch (Exception e) {
			setLoadfailedView();
		}

	}

	public int getScreenWidth() {
		DisplayMetrics dm = new DisplayMetrics();
		this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	private void showNoResultView() {
		listview_loadfailed.setText(MyApplication.getInstance().getString(R.string.data_loadingerr));
		listview_loading.setVisibility(View.GONE);
		listview_loadfailed.setVisibility(View.VISIBLE);
		videotitlebg.setVisibility(View.GONE);
	}

	private void setLoadfailedView() {
		listview_loading.setVisibility(View.GONE);
		listview_loadfailed.setText(MyApplication.getInstance().getString(R.string.data_loadingerr));
		listview_loadfailed.setVisibility(View.VISIBLE);
		videotitlebg.setVisibility(View.GONE);
	}

	private void requestmain() {
		 mTask = new ProtocolTask(MyApplication.getInstance());
		 mTask.setListener(this);
		 
//		 String resout = null;
//		  md5 = MD5Tools.MD5(getRequestUrl());
//				resout = FileCacheUtil.getUrlCache(getActivity(), md5, getRequestUrl(),System.currentTimeMillis());
//		if(resout!=null){
//			try {
//				//getJsonfromCache(true, new JSONObject(resout));
//				pullJson(true, new JSONObject(resout));
//			} catch (NumberFormatException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		//}else{
			 mTask.execute(getRequestUrl(), getRequestContent(), tag(),
					 getHeader());
	//	}
		 
		 
		 
		/*
		 * LiveFragment movieFragment = new LiveFragment();
		 * movieFragmentlist.add(movieFragment); movieFragment.setType(1);
		 * fragmentList.add(movieFragment); View viewchild =
		 * LinearLayout.inflate(LiveMainFragment.this.getActivity(),
		 * R.layout.livetabheadview, null);
		 * 
		 * View viewline = viewchild.findViewById(R.id.cate_tab_line1);
		 * viewchild.setLayoutParams(new LayoutParams(getScreenWidth() / 2,
		 * LayoutParams.WRAP_CONTENT));
		 * 
		 * movieFragment.request(); // 给ViewPager设置适配器 mPager.setAdapter(new
		 * MyFragmentPagerAdapter
		 * (this.getActivity().getSupportFragmentManager(), fragmentList));
		 * mPager.setOnPageChangeListener(this);// 页面变化时的监听器
		 * mPager.setCurrentItem(0);// 设置当前显示标签页为第一页
		 * listview_loading.setVisibility(View.GONE);
		 * videotitlebg.setVisibility(View.VISIBLE);
		 */

		// ///////////

//		for (int i = 0; i < 2; i++) {
//			final int mi = i;
//
//			LiveFragment movieFragment = new LiveFragment();
//			movieFragmentlist.add(movieFragment);
//			if (i == 0) {
//				movieFragment.setType(2);
//			}
//			fragmentList.add(movieFragment);
//			View viewchild = LinearLayout.inflate(LiveMainFragment.this.getActivity(), R.layout.videotabheadview, null);
//			if (i == 0) {
//				viewchild.findViewById(R.id.tab_divider).setVisibility(View.VISIBLE);
//			}
//			Button button = (Button) viewchild.findViewById(R.id.bt_rec_button);
//
//			if (i == 0) {
//				button.setText(R.string.txt_detail_tag_free);
//			} else {
//				button.setText("VIP");
//			}
//			button.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//					mPager.setCurrentItem(mi);
//				}
//			});
//			View viewline = viewchild.findViewById(R.id.cate_tab_line1);
//			viewchild.setLayoutParams(new LayoutParams(getScreenWidth() / 2, LayoutParams.WRAP_CONTENT));
//
//			tab_linelist.add(viewline);
//			tab_buttonlist.add(button);
//			cate_bottom.addView(viewchild);
//			if (0 == i) {
////				movieFragmentlist.get(i).request();
//				tab_linelist.get(i).setVisibility(View.VISIBLE);
//				tab_buttonlist.get(i).setSelected(true);
//			} else {
//				tab_linelist.get(i).setVisibility(View.INVISIBLE);
//				tab_buttonlist.get(i).setSelected(false);
//			}
//		}
//
//		// 给ViewPager设置适配器
//		mPager.setAdapter(new MyFragmentPagerAdapter(this.getActivity().getSupportFragmentManager(), fragmentList));
//		mPager.setOnPageChangeListener(this);// 页面变化时的监听器
//		mPager.setCurrentItem(0);// 设置当前显示标签页为第一页
//		listview_loading.setVisibility(View.GONE);
//		videotitlebg.setVisibility(View.VISIBLE);
	}

	private String getRequestUrl() {

		String u = Constants.APK_URL+"Video/v1.php?qt=Category&ctype=film";
		return u;
	}

	private Object getRequestContent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	//-------------bobo
	private void pullJson(Boolean b,final JSONObject result) throws JSONException{
//		if(!b){
//			new Thread(){
//				@Override
//				public void run() {
//					super.run();
//					FileCacheUtil.setUrlCache(getActivity(), result.toString(), md5);
//				}
//			}.start();
//		}
		int status = JsonParse.parseResultStatus(result);
		if (status == 1) {
			List<CateItem> appendList = JsonParse.parseVideoCateItem(result);
			
			//appendList.size() 改为< 1
			if (appendList != null && !appendList.isEmpty()) {
				for (int i = 0; i < 3; i++) {
					CateItem cateitem = appendList.get(i);
					final int mi = i;
					LiveFragment movieFragment = new LiveFragment();
					movieFragment.setFEEID_WEEK(cateitem.FEEID_WEEK);
					movieFragment.setFEEID_2MONTHS(cateitem.FEEID_2MONTHS);
					movieFragment.setFEEID_YEAR(cateitem.FEEID_YEAR);
					movieFragment.setPRICE_WEEK(cateitem.PRICE_WEEK);
					movieFragment.setPRICE_2MONTHS(cateitem.PRICE_2MONTHS);
					movieFragment.setPRICE_YEAR(cateitem.PRICE_YEAR);
					movieFragment.setMid(cateitem.id);
					movieFragmentlist.add(movieFragment);
					
					if (i == 0) {
						movieFragment.setType(0);
						Log.d("nnlog", "live------------");
					} else if(i==1){
						try {
							CateItem cateitem1 = appendList.get(0);
							movieFragment.setPrice(cateitem1.cCount);
							movieFragment.setPrice_day(cateitem1.price_day);
							movieFragment.setFee_day_id(cateitem1.feeid_day);
							movieFragment.setFeeid(cateitem1.feeid);
							movieFragment.setPriodid(cateitem1.priodid);
//							movieFragment.setFEEID_WEEK(cateitem1.FEEID_WEEK);
//							movieFragment.setFEEID_2MONTHS(cateitem1.FEEID_2MONTHS);
//							movieFragment.setFEEID_YEAR(cateitem1.FEEID_YEAR);
//							movieFragment.setPRICE_WEEK(cateitem1.PRICE_WEEK);
//							movieFragment.setPRICE_2MONTHS(cateitem1.PRICE_2MONTHS);
//							movieFragment.setPRICE_YEAR(cateitem1.PRICE_YEAR);
							movieFragment.setType(1);
							
						} catch (Exception e) {
							// movieFragment.setPrice(30);
						}
					}else{
						movieFragment.setType(2);
					}
					fragmentList.add(movieFragment);
					View viewchild = LinearLayout.inflate(LiveMainFragment.this.getActivity(), R.layout.videotabheadview, null);
					if (i == (appendList.size() - 1)) {
						viewchild.findViewById(R.id.tab_divider).setVisibility(View.INVISIBLE);
					}
					Button button = (Button) viewchild.findViewById(R.id.bt_rec_button);
					if(i==0){
						//button.setText(appendList.get(1).cTitle);
						button.setText(R.string.txt_home_leader_latest_collection);
					}else if(i==1){
						button.setText("VIP");
					}else{
						button.setText(getString(R.string.cartoon_av));
					}
					button.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							mPager.setCurrentItem(mi);
						}
					});
					View viewline = viewchild.findViewById(R.id.cate_tab_line1);
					viewchild.setLayoutParams(new LayoutParams(getScreenWidth() / 3, LayoutParams.WRAP_CONTENT));

					tab_linelist.add(viewline);
					tab_buttonlist.add(button);
					cate_bottom.addView(viewchild);
					if (0 == i) {
						movieFragmentlist.get(i).request();
						tab_linelist.get(i).setVisibility(View.VISIBLE);
						tab_buttonlist.get(i).setSelected(true);

					} else {
						tab_linelist.get(i).setVisibility(View.INVISIBLE);
						tab_buttonlist.get(i).setSelected(false);
					}
				}

				// 给ViewPager设置适配器
				mPager.setAdapter(new MyFragmentPagerAdapter(this.getActivity().getSupportFragmentManager(), fragmentList));
				mPager.setOnPageChangeListener(this);// 页面变化时的监听器
				mPager.setCurrentItem(0);// 设置当前显示标签页为第一页
				listview_loading.setVisibility(View.GONE);
				videotitlebg.setVisibility(View.VISIBLE);
			} else {
				showNoResultView();
			}
		} else {
			setLoadfailedView();
		}
	}

}
