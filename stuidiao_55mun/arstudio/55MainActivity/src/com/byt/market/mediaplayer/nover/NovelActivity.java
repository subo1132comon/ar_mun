package com.byt.market.mediaplayer.nover;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.mediaplayer.MusicDownLoadManageActivity;
import com.byt.market.mediaplayer.music.IPlayback;
import com.byt.market.mediaplayer.music.PlayMusicActivity;
import com.byt.market.mediaplayer.music.PlayMusicService;
import com.byt.market.mediaplayer.service.RediaoService;
import com.byt.market.mediaplayer.ui.MusicSubFragment;
import com.byt.market.mediaplayer.ui.RingFragment;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

public class NovelActivity extends FragmentActivity implements
		OnPageChangeListener, OnClickListener {
	
	private View recArea;
	AlwsydMarqueeTextView tv_title;
	public NovelSubFragment homeFragment;
	public RingNovelFragment homeFragment2;
	private View ringhome_line, ringrank_line;
	Button buttonringrec, buttonringhome, buttonringhot, buttonringrank;
	// 播放小窗口
	ImageView playBaricon;
	TextView musicName, musicAuthor;
	ImageView playBarButton, playBarNextButton;
	RelativeLayout playbarlayout;
	ProgressBar playbar_progress;
	// 播放小窗口
	ViewPager mPager;
	private ArrayList<BaseUIFragment> fragmentList;
	public IPlayback service;
	public static long umtime; 
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (PlayMusicService.PREPARED_PLAY_MUSIC_ACTION.equals(intent
						.getAction())) {
					if (service.getCurMusicPath() == null) {
						playbarlayout.setVisibility(View.GONE);
					} else {
						//playbarlayout.setVisibility(View.VISIBLE);
					}
					musicName.setText(service.getCurMusicName());
					musicAuthor.setText(service.getCurMusicAuthor());
					handler.removeMessages(0);
					handler.sendEmptyMessageDelayed(0, 1000);
					refreshPlayStateIcon();
				} else if (PlayMusicService.COMPLETE_PLAY_MUSIC_ACTION
						.equals(intent.getAction())) { 
					handler.removeMessages(0);
					playBarButton
							.setImageResource(R.drawable.playbar_btn_play);
				} else if (PlayMusicService.NOTI_PLAY_MUSIC_ACTION.equals(intent
						.getAction())) {
					refreshPlayStateIcon();
				}else if(RediaoService.PREPARED_PLAYRADIO_ACTION.equals(intent.getAction())){
					//add  by bobo
				//	playbarlayout.setVisibility(View.VISIBLE);
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ring_main_fragment);
		try {
			initView();
			bindService();
			initPaper();
			initReceiver();
			umtime = System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(PlayMusicService.PREPARED_PLAY_MUSIC_ACTION);
		filter.addAction(PlayMusicService.COMPLETE_PLAY_MUSIC_ACTION);
		filter.addAction(PlayMusicService.NOTI_PLAY_MUSIC_ACTION);
		filter.addAction(RediaoService.PREPARED_PLAYRADIO_ACTION);
		registerReceiver(broadcastReceiver, filter);
	}

	private void initPaper() {
		mPager = (ViewPager) findViewById(R.id.ringviewpager);
		fragmentList = new ArrayList<BaseUIFragment>();
		homeFragment2 = new RingNovelFragment();
		homeFragment = new NovelSubFragment();
		fragmentList.clear();
		fragmentList.add(homeFragment2);
		fragmentList.add(homeFragment);
		// 给ViewPager设置适配器
		mPager.setAdapter(new MyFragmentPagerAdapter(this
				.getSupportFragmentManager(), fragmentList));
		mPager.setCurrentItem(0);// 设置当前显示标签页为第一页
		mPager.setOnPageChangeListener(this);// 页面变化时的监听器

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
			tv_title.setText(R.string.voice_title);
			findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
			findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
			findViewById(R.id.top_downbutton).setOnClickListener(this);
			buttonringhome = (Button) findViewById(R.id.ringhome_button);
			buttonringrank = (Button) findViewById(R.id.ring_rank_button);
			//修改位置 add by bobo getString(R.string.ridiao)
			buttonringhome.setText(getString(R.string.voice));
			buttonringrank.setText(getString(R.string.ridiao));
			//end
			buttonringhome.setOnClickListener(this);
			buttonringrank.setOnClickListener(this);
			buttonringhome.setSelected(true);
			recArea = findViewById(R.id.ring_area);
			ringhome_line = findViewById(R.id.ringhome_line);
			ringrank_line = findViewById(R.id.ring_rank_tab_line);
			ringhome_line.setVisibility(View.VISIBLE);
			ringrank_line.setVisibility(View.INVISIBLE);
			recArea.setVisibility(View.VISIBLE);
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

		} catch (Exception e) {
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
			case R.id.top_downbutton:
				
				MobclickAgent.onEvent(this, "Radiodownmang");
				Intent downloadIntent = new Intent(this,
						MusicDownLoadManageActivity.class);
				startActivity(downloadIntent);
				this.overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
				break;
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
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void requestDelay() {
		try {
			if (homeFragment2 != null) {
				homeFragment2.request();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		ArrayList<BaseUIFragment> list;

		public MyFragmentPagerAdapter(FragmentManager fm,
				ArrayList<BaseUIFragment> list) {
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
		switch (arg0) {
		case 0:
			onMYPageChange(0);
			break;
		case 1:
			onMYPageChange(1);
			break;
		/*
		 * case 2: onMYPageChange(2); break; case 3: onMYPageChange(3); break;
		 */
		}
	}

	private void onMYPageChange(int page) {
		switch (page) {
		case 0:
			// buttonringrec.setSelected(true);
			buttonringhome.setSelected(true);
			// buttonringhot.setSelected(false);
			buttonringrank.setSelected(false);
			requestDelay();
			// ringrec_line.setVisibility(View.VISIBLE);
			ringhome_line.setVisibility(View.VISIBLE);
			// ringhot_line.setVisibility(View.INVISIBLE);
			ringrank_line.setVisibility(View.INVISIBLE);
			break;
		case 1:
			// buttonringrec.setSelected(false);
			buttonringhome.setSelected(false);
			// buttonringhot.setSelected(false);
			buttonringrank.setSelected(true);
			if (homeFragment != null) {
				homeFragment.request();
			}
			// ringrec_line.setVisibility(View.INVISIBLE);
			ringhome_line.setVisibility(View.INVISIBLE);
			// ringhot_line.setVisibility(View.INVISIBLE);
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
				if (service != null) {
					if (service == null || service.getCurMusicPath() == null) {
						playbarlayout.setVisibility(View.GONE);
					} else {
						playbarlayout.setVisibility(View.VISIBLE);
					}
					musicName.setText(service.getCurMusicName());
					musicAuthor.setText(service.getCurMusicAuthor());
					refreshPlayStateIcon();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName arg0) {
		}

	};
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		StatService.trackCustomEndEvent(this, "novel", "");
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
		int endtime = (int) (System.currentTimeMillis()-umtime);
		MobclickAgent.onEventValue(this, "Radio", null,endtime/1000);
		Log.d("nnlog", "统计Radio");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		StatService.trackCustomBeginEvent(this, "novel", "");

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
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
