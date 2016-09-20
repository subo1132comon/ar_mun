package com.byt.market.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.data.AppItem;
import com.byt.market.data.CateItem;
import com.byt.market.data.HLeaderItem;
import com.byt.market.data.SubjectItem;
import com.byt.market.log.LogModel;
import com.byt.market.ui.CateListFragment;
import com.byt.market.ui.FavListFragment;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.ui.SubListFragment;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.tencent.stat.StatService;

/**
 * 列表分类详情
 */
public class ListInfoFrame extends BaseActivity implements View.OnClickListener {
	private AlwsydMarqueeTextView tv_title;
	private String from;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		try {
			setContentView(R.layout.list_info_new_frame);
			initView();
			addEvent();
			initData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initView() {
		findViewById(R.id.topline4).setVisibility(View.VISIBLE);
		findViewById(R.id.top_downbutton).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
	}

	@Override
	public void initData() {
		from = getIntent().getStringExtra("from");
		Object obj = getIntent().getParcelableExtra("app");
		Bundle newBundle = new Bundle();
		int cid = 0;
		if (obj instanceof CateItem) {
			CateItem cate = (CateItem) obj;
			newBundle.putInt("cateId", cate.id);
			showCateListView(cate.id);
			tv_title.setText(cate.cTitle);
			cid = cate.id;
			CateListFragment cateListFragment = new CateListFragment();
			newBundle.putString("hot", Constants.CATE_HOT);
			cateListFragment.setArguments(newBundle);
			addFragment(cateListFragment);
		} else if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			newBundle.putInt("cateId", app.sid);
			cid = app.sid;
			newBundle.putString(Constants.LIST_FROM, LogModel.L_BANNER + ","
					+ cid);
		} else if (obj instanceof HLeaderItem) {
			HLeaderItem app = (HLeaderItem) obj;
			newBundle.putInt("cateId", app.sid);
			cid = app.sid;
			newBundle.putString(Constants.LIST_FROM, LogModel.L_HOME_LEADER
					+ "," + cid);
			addFragment(showSubListView(app));

		} else if (obj instanceof SubjectItem) {
			if (from.equals(LogModel.L_SUBJECT_HOME)) {
				SubListFragment subFrag = new SubListFragment();
				Bundle bundle = new Bundle();
				Object object = getIntent().getParcelableExtra("app");
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
				addFragment(subFrag);
			}
		} else if (LogModel.L_FAV_USER.equals(from)) {
			tv_title.setText(R.string.favuser);
			addFragment(new FavListFragment());
		}

	}

	private void addFragment(ListViewFragment listviewFragment) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.contentFrame, listviewFragment).commit();
	}

	@Override
	public void addEvent() {
		findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
		findViewById(R.id.titlebar_search_button).setOnClickListener(this);
		findViewById(R.id.top_downbutton).setOnClickListener(this);
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

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.titlebar_back_arrow) {
			finishWindow();
		} else if (v.getId() == R.id.titlebar_search_button) {
			startActivity(new Intent(Constants.TOSEARCH));
		} else if (v.getId() == R.id.top_downbutton) {
			Intent downloadIntent = new Intent(this,
					DownLoadManageActivity.class);
			downloadIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
					DownLoadManageActivity.TYPE_FROM_DOWNLOAD);
			startActivity(downloadIntent);
			this.overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
		}
	}

	public void showCateListView(int cid) {
		if (LogModel.hasMap.size() == 0
				|| !LogModel.hasMap.containsKey(LogModel.L_CATE_HOT)
				|| (LogModel.hasMap.containsKey(LogModel.L_CATE_HOT) && LogModel.hasMap
						.get(LogModel.L_CATE_HOT) == 1)) {
			Util.addListData(maContext, LogModel.L_CATE_HOT,
					String.valueOf(cid));
		}

	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		StatService.trackCustomEndEvent(this, "runkgame", "");
		StatService.trackCustomEndEvent(this, "runkapp", "");
	}
}
