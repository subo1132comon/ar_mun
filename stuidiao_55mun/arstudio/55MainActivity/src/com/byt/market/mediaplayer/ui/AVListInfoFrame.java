package com.byt.market.mediaplayer.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.data.CateItem;
import com.byt.market.data.RingItem;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.MusicDownLoadManageActivity;
import com.byt.market.mediaplayer.data.VideoItem;
import com.byt.market.mediaplayer.music.IPlayback;
import com.byt.market.mediaplayer.music.PlayMusicActivity;
import com.byt.market.mediaplayer.music.PlayMusicService;
import com.byt.market.mediaplayer.tv.NewTVListFragment;
import com.byt.market.mediaplayer.ui.NewMusicListFragment;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.tencent.stat.StatService;

/**
 * 列表分类详情
 */
public class AVListInfoFrame extends BaseActivity implements
		View.OnClickListener {
	private String from;
	private AlwsydMarqueeTextView tv_title;
	// 播放小窗口
	public RelativeLayout playbarlayout;
	public ProgressBar playbar_progress;
	public IPlayback service;
	//public NewMusicListFragment cateListFragment;
	public LiveSubFragment cateListFragment;
	

	// 播放小窗口
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_info_music_tab_frame);
		try {
			initView();
			initData();
			addEvent();
			//initReceiver();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initReceiver() {
//		IntentFilter filter = new IntentFilter();
//		filter.addAction(PlayMusicService.PREPARED_PLAY_MUSIC_ACTION);
//		filter.addAction(PlayMusicService.COMPLETE_PLAY_MUSIC_ACTION);
//		filter.addAction(PlayMusicService.START_PLAY_MUSIC_ACTION);
//		filter.addAction(PlayMusicService.NOTI_PLAY_MUSIC_ACTION);
//		filter.addAction(Constants.SHARE_ACTION);//add by bobo
	}

	@Override
	public void initData() {
		try {
//			from = getIntent().getStringExtra("from");
//			Bundle bundle = new Bundle();
//			int cid = 0;
//			if (from.equals(LogModel.L_MUSIC_CATE_HOME)) {
//
				VideoItem cate = getIntent().getParcelableExtra("app");
//				if (cate != null && cate.cCount == 1) {
//					tv_title.setText(R.string.musicland);
//				} else if (cate != null && cate.cCount == 2) {
//					tv_title.setText(R.string.musicrankstring);
//				}
////				if(cate.isNotif){
////					//腾讯
////					StatService.trackCustomEvent(getApplicationContext(),"Notification", String.valueOf(cate.id));
////				}
//				cid = cate.id;
//				bundle.putInt("cateId", cid);
//				bundle.putString("appicon", cate.ImagePath);
//				bundle.putString("appname", cate.cTitle);
//				bundle.putString("appsdc", cate.cDesc);
//				bundle.putString("hot", Constants.CATE_HOT);
//				bundle.putInt("iswhichMusic", cate.cCount);
//				bundle.putBoolean("isnotf", cate.isNotif);
//				showCateListView(cate.id);
//				tv_title.setText(cate.cTitle);
				Bundle avbundle = new Bundle();
				cateListFragment = new LiveSubFragment();
				//bundle.putString("hot", Constants.CATE_HOT);
				Log.d("nnlog", "getpriiiiiiiiiiii"+cate.getPRICE_WEEK());
				avbundle.putParcelable("avapp", cate);
				cateListFragment.setArguments(avbundle);
				addFragment(cateListFragment);
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initView() {

		findViewById(R.id.topline4).setVisibility(View.GONE);
		findViewById(R.id.top_downbutton).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
		playbarlayout = (RelativeLayout) findViewById(R.id.playbarlayout);
		playbarlayout.setVisibility(View.GONE);
		playbarlayout.setOnClickListener(this);
		playbar_progress = (ProgressBar) findViewById(R.id.playbar_progress);
	}

	private void addFragment(ListViewFragment listviewFragment) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.contentFrame, listviewFragment).commit();
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
	public void addEvent() {
		findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
		findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
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

	/**
	 * 点击事件处理
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.titlebar_back_arrow:
			finishWindow();
			break;
		case R.id.titlebar_search_button:
			startActivity(new Intent(Constants.TOSEARCH));
			break;
		case R.id.top_downbutton:
			Intent downloadIntent = new Intent(this,
					MusicDownLoadManageActivity.class);
			startActivity(downloadIntent);
			this.overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
