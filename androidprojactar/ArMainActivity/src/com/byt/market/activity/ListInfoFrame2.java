package com.byt.market.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.byt.ar.R;
import com.byt.market.Constants;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.data.AppItem;
import com.byt.market.data.CateItem;
import com.byt.market.data.HLeaderItem;
import com.byt.market.data.SubjectItem;
import com.byt.market.log.LogModel;
import com.byt.market.ui.CateListFragment;
import com.byt.market.ui.SubListFragment;
import com.byt.market.ui.CateListFragment.OnTitleBtnClick;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;

/**
 * 列表分类详情
 */
public class ListInfoFrame2 extends BaseActivity implements OnClickListener,
		OnTitleBtnClick {

	public String from;
	public AlwsydMarqueeTextView tv_title;

	// *******分类子列表用
	public CateListFragment mHotCateList;
	public CateListFragment mNewCateList;
	public String currentType;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_info_frame);
		initView();
		initData();
		addEvent();
	}

	@Override
	public void initView() {
		Intent intent = getIntent();
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
		from = intent.getStringExtra("from");
		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		if (from.equals(LogModel.L_CATE_HOME)) {
			CateItem cate = getIntent().getParcelableExtra("app");
			tv_title.setText(cate.cTitle);
			showCateListView(cate.id);
			transaction.replace(R.id.list_info_contentframe, mHotCateList);
		} else if (from.equals(LogModel.L_SUBJECT_HOME)) {
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
			transaction.replace(R.id.list_info_contentframe, subFrag);
		} else if (from.equals(LogModel.L_HOME_LEADER)) {
			Object object = intent.getParcelableExtra("app");
			if (object instanceof HLeaderItem) {
				HLeaderItem item = (HLeaderItem) object;
				if (item.stype == 1) {
					transaction.replace(R.id.list_info_contentframe,
							showSubListView(item));
				} else if (item.stype == 2) {
					tv_title.setText(item.name);
					showCateListView(item.sid);
					transaction.replace(R.id.list_info_contentframe,
							mHotCateList);
				}

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
					transaction.replace(R.id.list_info_contentframe, subFrag);
				} else if (item.cateid == -2) {
					tv_title.setText(item.name);
					showCateListView(item.sid);
					transaction.replace(R.id.list_info_contentframe,
							mHotCateList);
				}
			}
		}
		transaction.commit();
	}

	public SubListFragment showSubListView(HLeaderItem item) {
		SubListFragment subFrag = new SubListFragment();
		Bundle bundle = new Bundle();
//		tv_title.setText(item.name);
		tv_title.setText(R.string.sp_list);
		bundle.putParcelable("app", item);
		if (LogModel.hasMap.size() == 0
				|| !LogModel.hasMap.containsKey(LogModel.L_HOME_LEADER)
				|| (LogModel.hasMap.containsKey(LogModel.L_HOME_LEADER) && LogModel.hasMap
						.get(LogModel.L_HOME_LEADER) == 1))
			Util.addListData(maContext, LogModel.L_HOME_LEADER,
					String.valueOf(item.sid) + "," + item.stype);
		bundle.putString(Constants.LIST_FROM, LogModel.L_HOME_LEADER);
		subFrag.setArguments(bundle);
		return subFrag;
	}

	public void showCateListView(int cid) {
		Bundle bundle = new Bundle();
		bundle.putInt("cateId", cid);
		bundle.putString("hot", Constants.CATE_HOT);
		if (mHotCateList == null) {
			mHotCateList = new CateListFragment();
		}
		// CateListFragment cateFrag = new CateListFragment();
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

		// mHotCateList.setOnTitleBtnClickListener(this);
		mHotCateList.setArguments(bundle);
		currentType = Constants.CATE_HOT;
	}

	@Override
	public void initData() {

	}

	@Override
	public void addEvent() {
		findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
		findViewById(R.id.titlebar_search_button).setOnClickListener(this);
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
		mHotCateList = null;
		mNewCateList = null;
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
		}
	}

	@Override
	public void onTitleBtnClick(String type) {
		if (currentType != null && currentType.equals(type)) {
			return;
		}
		Object obj = getIntent().getParcelableExtra("app");
		Bundle bundle = new Bundle();
		int cid = 0;
		if (obj instanceof CateItem) {
			CateItem cate = (CateItem) obj;
			bundle.putInt("cateId", cate.id);
			cid = cate.id;
		} else if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			bundle.putInt("cateId", app.sid);
			cid = app.sid;
			bundle.putString(Constants.LIST_FROM, LogModel.L_BANNER + "," + cid);
		} else if (obj instanceof HLeaderItem) {
			HLeaderItem app = (HLeaderItem) obj;
			bundle.putInt("cateId", app.sid);
			cid = app.sid;
			bundle.putString(Constants.LIST_FROM, LogModel.L_HOME_LEADER + ","
					+ cid);
		}
		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		if (Constants.CATE_HOT.equals(type)) {
			if (mHotCateList == null) {
				mHotCateList = new CateListFragment();
			}
			bundle.putString("hot", Constants.CATE_HOT);
			// mHotCateList.setOnTitleBtnClickListener(this);
			mHotCateList.setArguments(bundle);
			currentType = Constants.CATE_HOT;
			if (LogModel.hasMap.size() == 0
					|| !LogModel.hasMap.containsKey(LogModel.L_CATE_HOT)
					|| (LogModel.hasMap.containsKey(LogModel.L_CATE_HOT) && LogModel.hasMap
							.get(LogModel.L_CATE_HOT) == 1)) {
				if (from.equals(LogModel.L_BANNER))
					Util.addListData(maContext, LogModel.L_CATE_HOT,
							LogModel.L_BANNER + "," + String.valueOf(cid));
				else if (from.equals(LogModel.L_HOME_LEADER))
					Util.addListData(maContext, LogModel.L_CATE_HOT,
							LogModel.L_HOME_LEADER + "," + String.valueOf(cid));
				else
					Util.addListData(maContext, LogModel.L_CATE_HOT,
							String.valueOf(cid));
			}
			transaction.replace(R.id.list_info_contentframe, mHotCateList);
		} else {
			if (mNewCateList == null) {
				mNewCateList = new CateListFragment();
			}
			bundle.putString("hot", Constants.CATE_NEW);
			// mNewCateList.setOnTitleBtnClickListener(this);
			mNewCateList.setArguments(bundle);
			currentType = Constants.CATE_NEW;
			if (LogModel.hasMap.size() == 0
					|| !LogModel.hasMap.containsKey(LogModel.L_CATE_NEW)
					|| (LogModel.hasMap.containsKey(LogModel.L_CATE_NEW) && LogModel.hasMap
							.get(LogModel.L_CATE_NEW) == 1)) {
				if (from.equals(LogModel.L_BANNER))
					Util.addListData(maContext, LogModel.L_CATE_NEW,
							LogModel.L_BANNER + "," + String.valueOf(cid));
				else if (from.equals(LogModel.L_HOME_LEADER))
					Util.addListData(maContext, LogModel.L_CATE_NEW,
							LogModel.L_HOME_LEADER + "," + String.valueOf(cid));
				else
					Util.addListData(maContext, LogModel.L_CATE_NEW,
							String.valueOf(cid));
			}
			transaction.replace(R.id.list_info_contentframe, mNewCateList);
		}
		transaction.commit();
	}
}
