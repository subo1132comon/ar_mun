package com.byt.market.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.activity.base.BaseSlideFragmentActivity;
import com.byt.market.data.AppItem;
import com.byt.market.data.CateItem;
import com.byt.market.data.HLeaderItem;
import com.byt.market.data.SubjectItem;
import com.byt.market.log.LogModel;
import com.byt.market.ui.CateListFragment;
import com.byt.market.ui.FavListFragment;
import com.byt.market.ui.SubListFragment;
import com.byt.market.ui.base.BaseFragment;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;

/**
 * 列表分类详情
 */
public class ListInfoFrameOld extends BaseSlideFragmentActivity implements
		OnClickListener, OnPageChangeListener {

	// views
	/** 最热游戏列表 **/
	private CateListFragment newCateList;
	/** 最新游戏列表 **/
	private CateListFragment hotCateList;
	/*bestonet add by "zengxiao" for:添加猜你喜欢*/
	private FavListFragment favCateList;
	// variables
	private String from;
	private AlwsydMarqueeTextView tv_title;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		addEvent();
	}

	@Override
	public void initView() {
		super.initView();
		Intent intent = getIntent();
		favCateList=new FavListFragment();		
		findViewById(R.id.topline4).setVisibility(View.VISIBLE);
		findViewById(R.id.top_downbutton).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
		from = intent.getStringExtra("from");
		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
	
		 if (from.equals(LogModel.L_CATE_HOME))
		 { CateItem cate =getIntent().getParcelableExtra("app");
		 tv_title.setText(cate.cTitle);
		 showCateListView(cate.id); 
		 transaction.replace(R.id.contentFrame,hotCateList); 
		 } else if (from.equals(LogModel.L_SUBJECT_HOME)) {/*
			SubListFragment subFrag = new SubListFragment();
			Bundle bundle = new Bundle();
			Object object = intent.getParcelableExtra("app");
			if (object instanceof SubjectItem) {
				SubjectItem item = (SubjectItem) object;
				tv_title.setText(item.name);
				bundle.putParcelable("app", item);
				if (LogModel.hasMap.size() == 0
						|| !LogModel.hasMap
								.containsKey(LogModel.L_SUBJECT_LIST)
						|| (LogModel.hasMap
								.containsKey(LogModel.L_SUBJECT_LIST) && LogModel.hasMap
								.get(LogModel.L_SUBJECT_LIST) == 1))
					Util.addListData(maContext, LogModel.L_SUBJECT_LIST,
							String.valueOf(item.sid));
			}
			bundle.putString(Constants.LIST_FROM, LogModel.L_SUBJECT_LIST);
			subFrag.setArguments(bundle);
			transaction.replace(R.id.contentFrame, subFrag);
		*/} else if (from.equals(LogModel.L_HOME_LEADER_LATEST_COLLECTION)
				|| from.equals(LogModel.L_HOME_LEADER_CLASSIC_PLAY)) {
			// 来自首界面导航:最新收录,经典必玩
			Object object = intent.getParcelableExtra("app");
			if (object instanceof HLeaderItem) {
				/*HLeaderItem item = (HLeaderItem) object;
				transaction.replace(R.id.contentFrame,
						showSubListView(item));*/
			/*	if (item.stype == 2) {
					transaction.replace(R.id.contentFrame,
							showSubListView(item));
				} else if (item.stype == 1) {
					tv_title.setText(item.name);
					showCateListView(item.sid);
					transaction.replace(R.id.contentFrame, hotCateList);
				}*/

			}
		} else if (from.equals(LogModel.L_BANNER)) {
			Object object = intent.getParcelableExtra("app");
			if (object instanceof AppItem) {
				AppItem item = (AppItem) object;
				if (item.cateid == -1) {
					SubListFragment subFrag = new SubListFragment();
					Bundle bundle = new Bundle();
					tv_title.setText(item.name);
					bundle.putParcelable("app", item);
					if (LogModel.hasMap.size() == 0
							|| !LogModel.hasMap
									.containsKey(LogModel.L_BANNER_LIST)
							|| (LogModel.hasMap
									.containsKey(LogModel.L_BANNER_LIST) && LogModel.hasMap
									.get(LogModel.L_BANNER_LIST) == 1))
						Util.addListData(maContext, LogModel.L_BANNER_LIST,
								String.valueOf(item.sid));
					bundle.putString(Constants.LIST_FROM,
							LogModel.L_BANNER_LIST);
					subFrag.setArguments(bundle);
					transaction.replace(R.id.contentFrame, subFrag);
				} else if (item.cateid == -2) {
					tv_title.setText(item.name);
					showCateListView(item.sid);
					transaction.replace(R.id.contentFrame, hotCateList);
				}
			}
		} else if (LogModel.L_HOME_LEADER.equals(from)) {
			Object object = intent.getParcelableExtra("app");
			HLeaderItem app = (HLeaderItem) object;
			tv_title.setText(app.name);
		}else if(LogModel.L_FAV_USER.equals(from)){//添加猜你喜欢
			 tv_title.setText(R.string.favuser);
			transaction.replace(R.id.contentFrame, favCateList);
		}
		 
		transaction.commit();
	}

	public SubListFragment showSubListView(HLeaderItem item) {
		SubListFragment subFrag = new SubListFragment();
		Bundle bundle = new Bundle();
		tv_title.setText(item.name);
		bundle.putParcelable("app", item);
		if (LogModel.hasMap.size() == 0
				|| !LogModel.hasMap.containsKey(LogModel.L_HOME_LEADER)
				|| (LogModel.hasMap.containsKey(LogModel.L_HOME_LEADER) && LogModel.hasMap
						.get(LogModel.L_HOME_LEADER) == 1))
			Util.addListData(maContext, LogModel.L_HOME_LEADER,
					String.valueOf(item.sid) + "," + item.stype);
		if (!TextUtils.isEmpty(from)) {
			bundle.putString(Constants.LIST_FROM, from);
		}
		subFrag.setArguments(bundle);
		return subFrag;
	}

	public void showCateListView(int cid) {
		Bundle bundle = new Bundle();
		bundle.putInt("cateId", cid);
		bundle.putString("hot", Constants.CATE_HOT);
		if (hotCateList == null) {
			hotCateList = new CateListFragment();
		}
		if (LogModel.hasMap.size() == 0
				|| !LogModel.hasMap.containsKey(LogModel.L_CATE_HOT)
				|| (LogModel.hasMap.containsKey(LogModel.L_CATE_HOT) && LogModel.hasMap
						.get(LogModel.L_CATE_HOT) == 1)) {
			if (from.equals(LogModel.L_BANNER)) {
				Util.addListData(maContext, LogModel.L_CATE_HOT,
						LogModel.L_BANNER + "," + String.valueOf(cid));
				bundle.putString(Constants.LIST_FROM, LogModel.L_BANNER + ","
						+ cid);
			} else if (from.equals(LogModel.L_HOME_LEADER)) {
				Util.addListData(maContext, LogModel.L_CATE_HOT,
						LogModel.L_HOME_LEADER + "," + String.valueOf(cid));
				bundle.putString(Constants.LIST_FROM, LogModel.L_HOME_LEADER
						+ "," + cid);
			} else
				Util.addListData(maContext, LogModel.L_CATE_HOT,
						String.valueOf(cid));
		}

		hotCateList.setArguments(bundle);
	}

	@Override
	public void addEvent() {
		findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
		findViewById(R.id.titlebar_search_button).setOnClickListener(this);
		
		findViewById(R.id.top_downbutton).setOnClickListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
		boolean flag = false;
		// 后退按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finishWindow();
		}
		return flag;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		hotCateList = null;
		newCateList = null;
	}

	/**
	 * 点击事件处理
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.titlebar_back_arrow) {
			finishWindow();
		} else if (v.getId() == R.id.titlebar_search_button) {
			startActivity(new Intent(Constants.TOSEARCH));
		}else if(v.getId()==R.id.top_downbutton)
		{
			Intent downloadIntent = new Intent(this,
					DownLoadManageActivity.class);
			downloadIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
					DownLoadManageActivity.TYPE_FROM_DOWNLOAD);
			startActivity(downloadIntent);
			this.overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
		}
	}

	@Override
	protected int getResLayoutId() {
		return R.layout.list_info_top_tab_frame;
	}

	@Override
	protected void fillUpViewPager() {
		boolean istop=false;
		List<BaseFragment> fragments = new ArrayList<BaseFragment>();

		Object obj = getIntent().getParcelableExtra("app");
		Bundle newBundle = new Bundle();
		Bundle hotBundle = new Bundle();
		int cid = 0;
		if (obj instanceof CateItem) {
			CateItem cate = (CateItem) obj;
			newBundle.putInt("cateId", cate.id);
			hotBundle.putInt("cateId", cate.id);
			tv_title.setText(cate.cTitle);
			cid = cate.id;
		} else if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			newBundle.putInt("cateId", app.sid);
			hotBundle.putInt("cateId", app.sid);
			cid = app.sid;
			newBundle.putString(Constants.LIST_FROM, LogModel.L_BANNER + ","
					+ cid);
		} else if (obj instanceof HLeaderItem) {
			istop=true;
			HLeaderItem app = (HLeaderItem) obj;
			newBundle.putInt("cateId", app.sid);
			hotBundle.putInt("cateId", app.sid);
			cid = app.sid;
			newBundle.putString(Constants.LIST_FROM, LogModel.L_HOME_LEADER
					+ "," + cid);
			
		}

		newCateList = new CateListFragment();
		if(istop)
		{
			newCateList.setTabTitle(getString(R.string.top_free));
		}
		else{
		newCateList.setTabTitle(getString(R.string.app_list_new));
		}
		newBundle.putString("hot", Constants.CATE_NEW);
		newCateList.setArguments(newBundle);
		fragments.add(newCateList);

		hotCateList = new CateListFragment();
		if(istop)
		{
		hotCateList.setTabTitle(getString(R.string.top_paid));
		}
		else{
			hotCateList.setTabTitle(getString(R.string.app_list_hot));
		}
		hotBundle.putString("hot", Constants.CATE_HOT);
		hotCateList.setArguments(hotBundle);
		fragments.add(hotCateList);

		tabsAdapter.initViewPagerAdapter(fragments);

		setViewPagerCurrentItem(0);
		tabsAdapter.setOnPageChangeListener(this);
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

	}
}
