package com.byt.market.activity;

import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadItem;
import com.byt.market.data.PUSH;
import com.byt.market.download.DownloadContent;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.ui.DetailFragment;
import com.byt.market.util.DataUtil;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;

/**
 * 主页面，控制其他五个页面
 * 
 * @author Administrator
 * 
 */
public class DetailFrame extends BaseActivity implements OnClickListener,
		DownloadTaskListener {
	//View
	/**分享按钮**/
	private ImageButton shareFunButton;
	/**收藏按钮**/
	private ImageButton favorFunButton;
	/**整个下载按钮区域的布局**/
	private LinearLayout ll_download;
	private View ll_pb;
	/**下载按钮**/
	private Button merFunButton;
	/**游戏下载时的进度条**/
	private ProgressBar downloadProgressBar;
	
	/**游戏详情Fragment**/
	private DetailFragment detailFragment;

	//Variable
	private String from;
	private boolean isFavor;
	private boolean ispush;
	private int p_id;
	public AppItem app;

	DownloadTaskManager manager = DownloadTaskManager.getInstance();
	DownloadTask downloadTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_frame);
		initView();
		initData();
		addEvent();
	}

	@Override
	public void initView() {
		app = getIntent().getParcelableExtra("app");
		ispush = getIntent().getBooleanExtra("push", false);
		p_id = getIntent().getIntExtra("p_id", -1);

		from = getIntent().getStringExtra(Constants.LIST_FROM);
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);

		favorFunButton = (ImageButton) findViewById(R.id.favorFunButton);
		shareFunButton = (ImageButton) findViewById(R.id.shareFunButton);
		//ll_download = (LinearLayout) findViewById(R.id.ll_download);
		//ll_pb = findViewById(R.id.lay_pb);
		downloadProgressBar = (ProgressBar) findViewById(R.id.downloadProgressBar);
		//merFunButton = (Button) findViewById(R.id.merFunButton);
		AlwsydMarqueeTextView tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
		tv_title.setText(getResources().getString(R.string.detail));
		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		detailFragment = new DetailFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable("app", app);
		bundle.putString(Constants.LIST_FROM, from);
		detailFragment.setArguments(bundle);
		transaction.replace(R.id.contentFrame, detailFragment);
		transaction.commit();
		isFavor = DataUtil.getInstance(this).hasFavor(app.sid);
		favorFunButton.setSelected(isFavor);
		manager.addListener(this);
	}

	@Override
	public void initData() {
		ArrayList<AppItem> items = new ArrayList<AppItem>();
		items.add(app);
		manager.fillAppStates(items);
		downloadTask = manager.getDownloadTask(this, String.valueOf(app.sid),
				String.valueOf(app.vcode));
		if (downloadTask == null) {
			downloadTask = new DownloadContent.DownloadTask();
			DownloadItem item = new DownloadItem();
			item.fill(app);
			downloadTask.mDownloadItem = item;
		}

		downloadTask.mDownloadItem.sid = app.sid;
		long length = downloadTask.mDownloadItem.dSize;
		long lastDSize = downloadTask.mDownloadItem.lastDSize;
		if (length > 0 && lastDSize > 0) {
			Message messge = mHandler.obtainMessage(MSG_REFRESH);
			messge.obj = (int) (lastDSize * 100 / length);
			mHandler.sendMessage(messge);
		}
		doRefresh();
		if (LogModel.hasMap.size() == 0
				|| !LogModel.hasMap.containsKey(LogModel.L_DETAIL)
				|| (LogModel.hasMap.containsKey(LogModel.L_DETAIL) && LogModel.hasMap
						.get(LogModel.L_DETAIL) == 1))
			Util.addListData(maContext, LogModel.L_DETAIL, from);
		if (ispush) {
			PUSH push = new PUSH();
			push.appid = app.sid;
			push.type = 2;
			push.id = p_id;
			push.state = 1;
			Util.addData(maContext, push);
		}

	}

	@Override
	public void addEvent() {
		favorFunButton.setOnClickListener(this);
		shareFunButton.setOnClickListener(this);
		ll_download.setOnClickListener(this);
		findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
		findViewById(R.id.titlebar_search_button).setOnClickListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
		boolean flag = false;
		// 后退按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (ispush) {
				startActivity(new Intent(Constants.TOMAIN));
			}
			finishWindow();
		}
		return flag;
	}

	/**
	 * 点击事件处理
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.favorFunButton) {
			doFavorApp(this);
		} else if (v.getId() == R.id.shareFunButton) {
			if (!detailFragment.isLoading()) {
				doshareapp(this);
			} else {
				showShortToast(getString(R.string.can_not_share_info2));
			}
		} else if (v.getId() == R.id.ll_download) {
			if (isFavor && app.state == AppItemState.STATE_IDLE
					&& from.equals(LogModel.L_DOWN_MANAGER)) {
				app.list_id = LogModel.L_DOWN_MANAGER;
				app.list_cateid = LogModel.L_APP_FAVOR;
				new Thread() {
					public void run() {
						DownloadTaskManager.getInstance().updateAppItem(app);
						handler.sendEmptyMessage(0);
					}
				}.start();
			} else
				manager.onDownloadBtnClick(app);
		} else if (v.getId() == R.id.titlebar_back_arrow) {
			if (ispush) {
				startActivity(new Intent(Constants.TOMAIN));
			}
			finishWindow();
		} else if (v.getId() == R.id.titlebar_search_button) {
			startActivity(new Intent(Constants.TOSEARCH));
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			manager.onDownloadBtnClick(app);
		};
	};

	private void doFavorApp(Context context) {
		if (isFavor) {
			Util.delData(maContext, app.sid);
			isFavor = false;
			Toast.makeText(this, R.string.expend_child5_del_fav, 150).show();
		} else {
			Util.addData(maContext, app);
			isFavor = true;
			Toast.makeText(this, R.string.addfavourite, 150).show();
		}
		favorFunButton.setSelected(isFavor);
	}

	/**
	 * 处理分享Apk文件
	 * 
	 * @param msg
	 */
	public void doshareapp(Context context) {
		Intent shareIntent = new Intent();
		shareIntent.setAction("android.intent.action.SEND");
		shareIntent.setType("text/plain");
		String sharemessage = context.getResources().getString(
				R.string.share_app_content);
		if (app.apk.startsWith("http://")) {
			sharemessage = String.format(sharemessage, new Object[] { app.name,
					app.apk });
		} else {
			sharemessage = String.format(sharemessage, new Object[] { app.name,
					Constants.APK_URL + app.apk });
		}
		shareIntent.putExtra("android.intent.extra.TEXT", sharemessage);
		shareIntent = Intent.createChooser(shareIntent,
				context.getString(R.string.share_info_to_friend));

		try {
			context.startActivity(shareIntent);
		} catch (ActivityNotFoundException localActivityNotFoundException) {
			Toast.makeText(context,
					context.getString(R.string.can_not_share_info),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void startConnecting(DownloadTask task, int totalTask,
			int progressTask) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadStarted(DownloadTask task, int totalTask,
			int progressTask, long totalSize) {
		mHandler.sendEmptyMessage(MSG_REFRESH_BUTTON);
	}

	@Override
	public void downloadProgress(DownloadTask task, int totalTask,
			int progressTask, long progressSize, long totalSize) {
		if (task.mDownloadItem.sid == downloadTask.mDownloadItem.sid) {
			Message messge = mHandler.obtainMessage(MSG_REFRESH);
			messge.obj = (int) (progressSize * 100 / totalSize);
			mHandler.sendMessage(messge);
		}
	}

	@Override
	public void downloadEnded(DownloadTask task, int totalTask, int progressTask) {

	}

	@Override
	public void endConnecting(DownloadTask task, int totalTask,
			int progressTask, DownloadException result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {
		mHandler.sendEmptyMessage(MSG_REFRESH_BUTTON);
	}

	@Override
	public void refreshUI() {
		mHandler.sendEmptyMessage(MSG_REFRESH_BUTTON);
	}

	private void doRefresh() {
		DownloadTask task = DownloadTaskManager.getInstance().getDownloadTask(
				this, String.valueOf(downloadTask.mDownloadItem.sid),
				String.valueOf(downloadTask.mDownloadItem.vcode));

		if (task != null) {
			DownloadTaskManager.getInstance().updateByState(
					this.getApplicationContext(), merFunButton,
					task.mDownloadItem, null, null, true, false, false, null,
					null);
			app.state = task.mDownloadItem.state;
			doUpdateLlBg(task.mDownloadItem.state);
		} else if (downloadTask != null && downloadTask.mDownloadItem != null) {
			final ArrayList<AppItem> items = new ArrayList<AppItem>();
			items.add(downloadTask.mDownloadItem);
			manager.fillAppStates(items);
			app.state = downloadTask.mDownloadItem.state;
			DownloadTaskManager.getInstance().updateByState(
					this.getApplicationContext(), merFunButton,
					downloadTask.mDownloadItem, null, null, true, false, false,
					null, null);
			doUpdateLlBg(downloadTask.mDownloadItem.state);
		}
	}

	private void doUpdateLlBg(int state) {
		if (state == AppItemState.STATE_DOWNLOAD_FINISH
				|| state == AppItemState.STATE_NEED_UPDATE
				|| state == AppItemState.STATE_IDLE) {
			ll_download.setBackgroundResource(R.drawable.btn_down_rec_bg);
		} else if (state == AppItemState.STATE_INSTALLED_NEW) {
			ll_download.setBackgroundResource(R.drawable.search_btn_orange);
		} else {
			ll_download.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	private static final int MSG_REFRESH = 1;
	private static final int MSG_REFRESH_BUTTON = 2;
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_REFRESH: {
				// ll_download.setBackgroundColor(Color.TRANSPARENT);
				downloadProgressBar.setProgress((Integer) msg.obj);
				break;
			}
			case MSG_REFRESH_BUTTON: {
				doRefresh();
				break;
			}
			}
		}

	};

	@Override
	public void unInstalledSucess(String packageName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void installedSucess(DownloadTask downloadTask) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void networkIsOk() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void installedSucess(String packageName) {
		// TODO Auto-generated method stub
		
	}
}
