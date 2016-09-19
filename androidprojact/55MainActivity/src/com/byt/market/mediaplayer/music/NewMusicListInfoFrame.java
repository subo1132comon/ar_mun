package com.byt.market.mediaplayer.music;

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
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.data.CateItem;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.MusicDownLoadManageActivity;
import com.byt.market.mediaplayer.ui.NewMusicListFragment;
import com.byt.market.tools.LogCart;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.tencent.stat.StatService;

/**
 * 列表分类详情
 */
public class NewMusicListInfoFrame extends BaseActivity implements
		View.OnClickListener {
	private String from;
	private AlwsydMarqueeTextView tv_title;
	// 播放小窗口
	private ImageView playBaricon;
	private TextView musicName, musicAuthor;
	private ImageView playBarButton, playBarNextButton;
	public RelativeLayout playbarlayout;
	public ProgressBar playbar_progress;
	public IPlayback service;
	public NewMusicListFragment cateListFragment;
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (PlayMusicService.PREPARED_PLAY_MUSIC_ACTION.equals(intent
						.getAction())) {
					refreshPlayStateIcon();
					handler.removeMessages(0);
					handler.sendEmptyMessageDelayed(0, 1000);
				} else if (PlayMusicService.COMPLETE_PLAY_MUSIC_ACTION
						.equals(intent.getAction())) {
					playBarButton.setImageResource(R.drawable.playbar_btn_play);
					handler.removeMessages(0);
				} else if (PlayMusicService.START_PLAY_MUSIC_ACTION
						.equals(intent.getAction())) {
					if (service == null || service.getCurMusicPath() == null) {
						playbarlayout.setVisibility(View.GONE);
					} else {
						playbarlayout.setVisibility(View.VISIBLE);
					}
					imageLoader.displayImage(service.getMusicLogo(), playBaricon, options);
					musicName.setText(service.getCurMusicName());
					musicAuthor.setText(service.getCurMusicAuthor());
					cateListFragment.getAdapter().notifyDataSetChanged();
				} else if (PlayMusicService.NOTI_PLAY_MUSIC_ACTION.equals(intent
						.getAction())) {
					refreshPlayStateIcon();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			try {
				playbar_progress.setMax(service.getDuration());
				playbar_progress.setProgress(service.getCurrentPosition());
				sendEmptyMessageDelayed(0, 1500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	};

	// 播放小窗口
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_info_music_tab_frame);
		try {
			initView();
			bindService();
			initData();
			addEvent();
			initReceiver();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(PlayMusicService.PREPARED_PLAY_MUSIC_ACTION);
		filter.addAction(PlayMusicService.COMPLETE_PLAY_MUSIC_ACTION);
		filter.addAction(PlayMusicService.START_PLAY_MUSIC_ACTION);
		filter.addAction(PlayMusicService.NOTI_PLAY_MUSIC_ACTION);
		registerReceiver(broadcastReceiver, filter);
	}

	@Override
	public void initData() {
		try {
			from = getIntent().getStringExtra("from");
			LogCart.Log("from--"+from);
			Bundle bundle = new Bundle();
			int cid = 0;
			if (from.equals(LogModel.L_MUSIC_CATE_HOME)) {

				CateItem cate = getIntent().getParcelableExtra("app");
				if (cate != null && cate.cCount == 1) {
					tv_title.setText(R.string.musicland);
				} else if (cate != null && cate.cCount == 2) {
					tv_title.setText(R.string.musicrankstring);
				}
//				if(cate.isNotif){
//					//腾讯
//					StatService.trackCustomEvent(getApplicationContext(),"Notification", String.valueOf(cate.id));
//				}
				cid = cate.id;
				bundle.putInt("cateId", cid);
				bundle.putString("appicon", cate.ImagePath);
				bundle.putString("appname", cate.cTitle);
				bundle.putString("appsdc", cate.cDesc);
				bundle.putString("hot", Constants.CATE_HOT);
				bundle.putInt("iswhichMusic", cate.cCount);
				bundle.putBoolean("isnotf", cate.isNotif);
				showCateListView(cate.id);
				tv_title.setText(cate.cTitle);

				cateListFragment = new NewMusicListFragment();
				bundle.putString("hot", Constants.CATE_HOT);
				cateListFragment.setArguments(bundle);
				addFragment(cateListFragment);
			}
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

		playBaricon = (ImageView) findViewById(R.id.musicplayicon);
		musicName = (TextView) findViewById(R.id.musicname);
		musicAuthor = (TextView) findViewById(R.id.musicauther);
		playBarButton = (ImageView) findViewById(R.id.playbutton);
		playBarNextButton = (ImageView) findViewById(R.id.playnextbutton);
		playbarlayout = (RelativeLayout) findViewById(R.id.playbarlayout);
		playBarButton.setOnClickListener(this);
		playBarNextButton.setOnClickListener(this);
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
		case R.id.playbutton:// 播放按钮
			try {
				if (service != null) {
					if (service.isPlaying()) {
						service.pause();
						playBarButton
								.setImageResource(R.drawable.playbar_btn_play);
					} else {
						playBarButton
								.setImageResource(R.drawable.playbar_btn_pause);
						service.start();
					}
					service.setIsHandlePause();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.playnextbutton:// 播放下一首
			try {
				if (service != null) {
					service.next();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.playbarlayout:
			Intent intent = new Intent(this, PlayMusicActivity.class);
			startActivity(intent);
			break;
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

	private void bindService() {
		Intent intent = new Intent(this, PlayMusicService.class);
		if (false == bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
			finish();
		}
	}

	private ServiceConnection connection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			try {
				service = IPlayback.Stub.asInterface(arg1);
				cateListFragment.setService(service);
				if (service == null || service.getCurMusicPath() == null) {
					playbarlayout.setVisibility(View.GONE);
				} else {
					playbarlayout.setVisibility(View.VISIBLE);
				}
				if (service != null) {
					musicName.setText(service.getCurMusicName());
					musicAuthor.setText(service.getCurMusicAuthor());
				}
				refreshPlayStateIcon();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName arg0) {
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unbindService(connection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			unregisterReceiver(broadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		handler.removeMessages(0);
	}

	@Override
	protected void onStop() {
		super.onStop();
		handler.removeMessages(0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			handler.removeMessages(0);
			handler.sendEmptyMessage(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		refreshPlayStateIcon();
	}
	private void refreshPlayStateIcon(){
		try {
			if (service.isPlaying()) {
				playBarButton
						.setImageResource(R.drawable.playbar_btn_pause);
			} else {
				playBarButton
						.setImageResource(R.drawable.playbar_btn_play);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
