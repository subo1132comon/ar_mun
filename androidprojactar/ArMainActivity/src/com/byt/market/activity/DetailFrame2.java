package com.byt.market.activity;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.Faviorate.MyFaviorateManager;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.AppComment;
import com.byt.market.data.AppDetail;
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
import com.byt.market.net.NetworkUtil;
import com.byt.market.ui.CommentFragment;
import com.byt.market.ui.DetailFragment1;
import com.byt.market.ui.HomeFragment;
import com.byt.market.util.DataUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.byt.market.view.DetailTypeView;

/**
 * 主页面，控制其他五个页面
 */
@SuppressLint("HandlerLeak")
public class DetailFrame2 extends BaseActivity implements OnClickListener,
		DownloadTaskListener, TaskListener {
	private static final int DETAIL = 0;
	private static final int COMMENT = 1;
	private boolean isshowtitle = true;
	// View
	/** 分享按钮 **/
	private ImageView shareFunButton;
	/** 收藏按钮 **/
	private ImageView favorFunButton;
	/** 整个下载按钮区域的布局 **/
	private LinearLayout ll_download;
	/** 下载按钮 **/
	private Button merFunButton;
	/** 游戏下载时的进度条 **/
	private ProgressBar DownloadProgressBar;
	private TextView progressnumtext;
	/** 游戏详情Fragment **/
	private DetailFragment1 detailFragment;
	/** 评论Fragment **/
	private CommentFragment commentFragment;
	/* add by zengxiao */
	private RelativeLayout sendcommentlayout;
	private LinearLayout commArea;
	private LinearLayout introArea;
	private Button sendCommentbt;
	private EditText commentText;
	private boolean isRequesting;
	View detailfragmentlayout,commonfragmentlayout;
	/* add end */
	/* add by zengxiao for:t添加应用详情 */
	public TextView downBtn2;//分享下载按钮
	RelativeLayout appdetailinit;
	ImageView mappicon;
	TextView mappname;
	RatingBar mapprating;
	TextView mappdownnum;
	DetailTypeView mapptv_type;
	/* add end */
	// Variable
	private String from;
	private boolean isFavor;
	private boolean ispush;
	private int p_id;
	private int sid;
	public AppItem app;
	private int currentItem;
	private DownloadTaskManager manager = DownloadTaskManager.getInstance();
	private DownloadTask downloadTask;
	public RelativeLayout comparepri;
	public Button downbutton;
	public TextView textviewprice;
	public ImageView freeline;
	public AppDetail appDetail;
	private Button intro_bt, comm_bt, intro_bt_temp, comm_bt_temp;
	private View switch_area, main_top_frame;
	private ScrollView scrollView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_frame);
		//MenuBaseActivity.isMainFrame = true;
		initView();
		initData();
		fillUpViewPager();
		addEvent();
	}

	int space = 0;
	float dy = 0;

	@Override
	public void initView() {
		if (getIntent().getIntExtra(Constants.ExtraKey.SID, 0) != 0) {
			app = new AppItem();
			app.sid = getIntent().getIntExtra(Constants.ExtraKey.SID, 0);
			ispush = true;
			from = LogModel.L_DETAIL;
			p_id = -1;
		} else {
			app = getIntent().getParcelableExtra("app");
			ispush = getIntent().getBooleanExtra("push", false);
			p_id = getIntent().getIntExtra("p_id", -1);
		}
		from = getIntent().getStringExtra(Constants.LIST_FROM);
		findViewById(R.id.topline3).setVisibility(View.VISIBLE);
		findViewById(R.id.topline1).setVisibility(View.VISIBLE);
		
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		favorFunButton = (ImageView) findViewById(R.id.collect_button);
		shareFunButton = (ImageView) findViewById(R.id.share_button);
		favorFunButton.setVisibility(View.VISIBLE);
		shareFunButton.setVisibility(View.VISIBLE);
		appdetailinit = (RelativeLayout) findViewById(R.id.appdetailinit);
		/* add by zengxiao for:app详情 */
		mappicon = (ImageView) findViewById(R.id.appIconView);
		mappname = (TextView) findViewById(R.id.appNameLabel);
		mapprating = (RatingBar) findViewById(R.id.appRatingView);
		mappdownnum = (TextView) findViewById(R.id.appDownloadnumLabel);
		mapptv_type = (DetailTypeView) findViewById(R.id.tv_stype);

		/* add end */

		// ll_download = (LinearLayout) findViewById(R.id.ll_download);
		// downloadProgressBar = (ProgressBar)
		// findViewById(R.id.downloadProgressBar);
		  DownloadProgressBar = (ProgressBar) findViewById(R.id.DownloadProgressBar);
		  progressnumtext=(TextView) findViewById(R.id.progressnumtext);
		  progressnumtext.setText(0 + "%");
		merFunButton = null;
		AlwsydMarqueeTextView tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
		tv_title.setText(getResources().getString(R.string.detail));
		isFavor = DataUtil.getInstance(this).hasFavor(app.sid);
		favorFunButton.setSelected(isFavor);
		manager.addListener(this);
		/* add by zengxiao */
		commentText = (EditText) findViewById(R.id.commenttext);
		sendCommentbt = (Button) findViewById(R.id.sendCommentbt);
		sendcommentlayout = (RelativeLayout) findViewById(R.id.operateCtrlBar);
		commArea = (LinearLayout) findViewById(R.id.comm_area);
		introArea = (LinearLayout) findViewById(R.id.intro_area);
		commArea.setVisibility(View.GONE);
		comparepri = (RelativeLayout) findViewById(R.id.comparepri);
		textviewprice=(TextView) findViewById(R.id.bt_free_btn);
		freeline=(ImageView) findViewById(R.id.freeline);
		if(Constants.ISGOOGLE)
		{
			comparepri.setVisibility(View.GONE);
		}else
		{
		comparepri.setOnClickListener(this);
		comparepri.setEnabled(false);
		}
		downbutton = (Button) findViewById(R.id.downbutton);
		
		downbutton.setOnClickListener(this);
		intro_bt = (Button) findViewById(R.id.intro_bt);
		intro_bt.setOnClickListener(this);
		comm_bt = (Button) findViewById(R.id.comm_bt);
		comm_bt.setOnClickListener(this);
		detailfragmentlayout=findViewById(R.id.detailfragment);
		commonfragmentlayout=findViewById(R.id.commonfragment);
		intro_bt_temp = (Button) findViewById(R.id.intro_bt_temp);
		intro_bt_temp.setOnClickListener(this);
		comm_bt_temp = (Button) findViewById(R.id.comm_bt_temp);
		comm_bt_temp.setOnClickListener(this);
		switch_area = findViewById(R.id.switch_area);
		main_top_frame = findViewById(R.id.main_top_frame);
		intro_bt.setSelected(true);
		intro_bt_temp.setSelected(true);
		scrollView = (ScrollView) findViewById(R.id.scroller_area);
		downBtn2=(TextView) findViewById(R.id.bt_down_btn2);
		downBtn2.setOnClickListener(this);
		if(app.isshare==1){
			downbutton.setVisibility(View.GONE);
			downBtn2.setVisibility(View.VISIBLE);
		}
		scrollView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				int[] temp = new int[2];
				switch_area.getLocationOnScreen(temp);
				int y1 = temp[1];
				main_top_frame.getLocationOnScreen(temp);
				space = y1 - temp[1] - main_top_frame.getHeight();
				if (space <= 0) {
					findViewById(R.id.switch_area_temp).setVisibility(
							View.VISIBLE);
				} else {
					findViewById(R.id.switch_area_temp)
							.setVisibility(View.GONE);
				}
				if (event.getAction() == MotionEvent.ACTION_CANCEL
						|| MotionEvent.ACTION_UP == event.getAction()) {
					handler2.sendEmptyMessageDelayed(0, 100);
				}
				return false;
			}
		});
		
	}

	int endY;
	Handler handler2 = new Handler() {
		public void handleMessage(Message msg) {
			int[] temp = new int[2];
			switch_area.getLocationOnScreen(temp);
			int y1 = temp[1];
			main_top_frame.getLocationOnScreen(temp);
			space = y1 - temp[1] - main_top_frame.getHeight();

			if (space <= 0) {
				findViewById(R.id.switch_area_temp).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.switch_area_temp).setVisibility(View.GONE);
			}
			if (endY != y1) {
				endY = y1;
				sendEmptyMessageDelayed(0, 100);
			}
		};
	};

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
			Message messge = mHandler.obtainMessage(MSG_REFRESHINIT);
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

		mappname.setText(app.name);
		mapprating.setRating(app.rating);
		mappdownnum.setText(getString(R.string.detail_down) + app.downNum + "");
		if (TextUtils.isEmpty(app.iconUrl)) {
			mappicon.setImageResource(R.drawable.app_empty_icon);
		} else {
			// mImageFetcher.loadImage(app.iconUrl,
			// holder.detailHolder.icon);
			imageLoader.displayImage(app.iconUrl, mappicon, options);
		}
		if (app.stype != null && !app.stype.equals("")) {
			mapptv_type.setVisibility(View.VISIBLE);
			mapptv_type.flushAdvertiseBand(app.stype);
		} else {
			mapptv_type.setVisibility(View.GONE);
		}

	}

	@Override
	public void addEvent() {
		favorFunButton.setOnClickListener(this);
		shareFunButton.setOnClickListener(this);
		sendCommentbt.setOnClickListener(this);
		commentText.setOnClickListener(this);
		// ll_download.setOnClickListener(this);
		findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
		findViewById(R.id.titlebar_search_button).setOnClickListener(this);
	}

	protected void doComment() {
		/* if (MarketContext.isLogin()) { */
		/*
		 * if (!Util.isInstalled(this, app.pname)) {
		 * Toast.makeText(DetailFrame2.this, R.string.installcomment,
		 * 150).show(); return; }
		 */
		Constants.ISSHOW = false;
		Intent intent = new Intent();
		intent.putExtra("app", app);
		intent.setClass(this, SubCommentFrame.class);
		// ll_download.setClickable(false);
		startActivityForResult(intent, Constants.COMMENT_REQUEST_CODE);
		overridePendingTransition(R.anim.push_bottom_in, R.anim.push_top_out);
		/*
		 * } else { Intent intent = new Intent(); intent.setClass(this,
		 * UserActivity.class); startActivity(intent); return; }
		 */
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent intent) {
		super.onActivityResult(arg0, arg1, intent);
		// ll_download.setClickable(true);
		if (arg0 == Constants.COMMENT_REQUEST_CODE
				&& arg1 == Constants.COMMENT_RESPONSE_CODE) {
			commentFragment.pullRefresh();
		}
		if (arg1 == RESULT_OK) {
			detailFragment.setIssend(true);
		}
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
		switch (v.getId()) {
		case R.id.collect_button:
			doFavorApp(this);
			break;
		case R.id.share_button:
			if (!detailFragment.isLoading()) {
				doshareapp(this);
			} else {
				showShortToast(getString(R.string.can_not_share_info2));
			}
			break;
		case R.id.commenttext:
			showSoftInput(this, commentText);
			break;
		case R.id.titlebar_back_arrow:
			if (ispush) {
				startActivity(new Intent(Constants.TOMAIN));
			}
			finishWindow();
			break;
		case R.id.titlebar_search_button:
			startActivity(new Intent(Constants.TOSEARCH));
			break;
		case R.id.sendCommentbt:
			if (NetworkUtil.isNetWorking(this)) {
				String content = commentText.getText().toString();
				if (TextUtils.isEmpty(content)) {
					commentText.setHintTextColor(Color.RED);
					return;
				}
				if (content.length() < 5 || content.length() > 140) {
					Toast.makeText(DetailFrame2.this, R.string.commenttextmax,
							150).show();
					return;
				}
				doComments(content, app.sid, app.vname);
			} else {
				showShortToast(getString(R.string.toast_no_network));
				return;
			}
			break;
		case R.id.comparepri:
			gotoGooglePlay();
			break;
		case R.id.bt_down_btn2:
			if (appDetail != null) {
				detailFragment.downAppshare(app,downBtn2,DownloadProgressBar,progressnumtext);
			}
			break;
		case R.id.downbutton:
			if (appDetail != null) {
				detailFragment.downApp(app,downbutton,DownloadProgressBar,progressnumtext);
			}
			break;
		case R.id.intro_bt:
		case R.id.intro_bt_temp:
			detailfragmentlayout.setVisibility(View.VISIBLE);
			commonfragmentlayout.setVisibility(View.GONE);
			introArea.setVisibility(View.VISIBLE);
			commArea.setVisibility(View.GONE);
			detailFragment.requestData();
			selectState();
			handler2.sendEmptyMessageDelayed(0, 100);
			break;
		case R.id.comm_bt:
		case R.id.comm_bt_temp:
			if (R.id.comm_bt == v.getId()) {
				scrollView.smoothScrollTo(0, 0);
			}else {
				handler2.sendEmptyMessageDelayed(0, 100);
			}

			detailfragmentlayout.setVisibility(View.GONE);
			commonfragmentlayout.setVisibility(View.VISIBLE);
			introArea.setVisibility(View.GONE);
			commArea.setVisibility(View.VISIBLE);
			commentFragment.request();
			selectState();
			break;
		default:
			break;
		}
	}

	private void selectState() {
		if (detailfragmentlayout.getVisibility() == View.VISIBLE) {
			intro_bt_temp.setSelected(true);
			intro_bt.setSelected(true);
			comm_bt.setSelected(false);
			comm_bt_temp.setSelected(false);
			findViewById(R.id.cate_tab_line1).setVisibility(View.VISIBLE);
			findViewById(R.id.cate_tab_line1_temp).setVisibility(View.VISIBLE);
			findViewById(R.id.cate_tab_line2).setVisibility(View.INVISIBLE);
			findViewById(R.id.cate_tab_line2_temp).setVisibility(
					View.INVISIBLE);
		} else {
			intro_bt_temp.setSelected(false);
			intro_bt.setSelected(false);
			comm_bt.setSelected(true);
			comm_bt_temp.setSelected(true);
		
			findViewById(R.id.cate_tab_line1).setVisibility(View.INVISIBLE);
			findViewById(R.id.cate_tab_line1_temp).setVisibility(
					View.INVISIBLE);
			findViewById(R.id.cate_tab_line2).setVisibility(View.VISIBLE);
			findViewById(R.id.cate_tab_line2_temp).setVisibility(View.VISIBLE);
		}
	}

	private void gotoGooglePlay() {
		try {
			Uri uri = Uri.parse("market://details?id=" + app.pname);
			Intent it = new Intent(Intent.ACTION_VIEW, uri);
			it.setClassName("com.android.vending",
					"com.android.vending.AssetBrowserActivity");
			startActivity(it);
		} catch (Exception e) {
			Toast.makeText(this, R.string.notinstallgoogle, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			manager.onDownloadBtnClick(app);
		};
	};

	private void doFavorApp(Context context) {
		if (isFavor) {
			// Util.delData(maContext, app.sid);
			MyFaviorateManager.getInstance().delFavApp(maContext, app);
			isFavor = false;
			Toast.makeText(this, R.string.expend_child5_del_fav, 150).show();
		} else {
			// Util.addData(maContext, app);
			MyFaviorateManager.getInstance().addFavApp(maContext, app);
			isFavor = true;
			Toast.makeText(this, R.string.addfavourite, 150).show();
		}
		favorFunButton.setSelected(isFavor);
	}

	/**
	 * 处理分享Apk文件
	 * 
	 */
	public void doshareapp(Context context) {
		Intent shareIntent = new Intent();
		shareIntent.setAction("android.intent.action.SEND");
		shareIntent.setType("text/plain");
		String sharemessage = context.getResources().getString(
				R.string.share_app_content);
		if (app.apk.startsWith("http://")) {
			sharemessage =sharemessage+app.name+":"+app.apk ;
		} else {
			sharemessage =sharemessage+ Constants.APK_URL+app.apk;
		}
		Intent intent =new Intent();
		intent.setClass(this,ShareTextActivity.class);						
		intent.putExtra("sendstring",sharemessage ); 
		startActivity(intent);
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

	@Override
	protected void onResume() {
		super.onResume();
		// 当非root模式下安装界面用户点击取消，返回到此界后，状态没有
		// 及时从安装中切换为安装，所以在此刷新UI

		refreshUI();
	}

	@Override
	protected void onStop() {
		super.onStop();
		handler2.removeMessages(0);
	}

	private void doRefresh() {
		if (currentItem == COMMENT) {
			return;
		}
		DownloadTask task = DownloadTaskManager.getInstance().getDownloadTask(
				this, String.valueOf(downloadTask.mDownloadItem.sid),
				String.valueOf(downloadTask.mDownloadItem.vcode));

		if (task != null) {
			if(app.isshare==1)
			{
				DownloadTaskManager.getInstance().updateByStateimpl2share(
						this.getApplicationContext(), downBtn2,
						task.mDownloadItem, DownloadProgressBar, progressnumtext, true, false, false, null,
						null,null,null);
			}else{
			DownloadTaskManager.getInstance().updateByStateimpl2(
					this.getApplicationContext(), downbutton,
					task.mDownloadItem, DownloadProgressBar, progressnumtext, true, false, false, null,
					null);
			}
			app.state = task.mDownloadItem.state;
			doUpdateLlBg(task.mDownloadItem.state);
		} else if (downloadTask != null && downloadTask.mDownloadItem != null) {
			final ArrayList<AppItem> items = new ArrayList<AppItem>();
			items.add(downloadTask.mDownloadItem);
			manager.fillAppStates(items);
			app.state = downloadTask.mDownloadItem.state;
			if(app.isshare==1)
			{
			DownloadTaskManager.getInstance().updateByStateimpl2share(
					this.getApplicationContext(), downBtn2,
					downloadTask.mDownloadItem, DownloadProgressBar, progressnumtext, true, false, false,
					null, null,null,null);
			}
			else{
				DownloadTaskManager.getInstance().updateByStateimpl2(
						this.getApplicationContext(), downbutton,
						downloadTask.mDownloadItem, DownloadProgressBar, progressnumtext, true, false, false,
						null, null);
			}
			doUpdateLlBg(downloadTask.mDownloadItem.state);
		}
	}

	private void doUpdateLlBg(int state) {
		if (state == AppItemState.STATE_NEED_UPDATE
				|| state == AppItemState.STATE_IDLE) {
			// ll_download.setBackgroundResource(R.drawable.btn_down_rec_bg);
		} else if (state == AppItemState.STATE_INSTALLED_NEW) {
			// ll_download.setBackgroundResource(R.drawable.btn_open_bg);
		} else if (state == AppItemState.STATE_DOWNLOAD_FINISH
				|| state == AppItemState.STATE_INSTALL_FAILED) {
			// ll_download.setBackgroundResource(R.drawable.btn_install_bg);
		} else {
			// ll_download.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	private static final int MSG_REFRESH = 1;
	private static final int MSG_REFRESH_BUTTON = 2;
	private static final int MSG_REFRESHINIT=3;
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_REFRESH:
				Log.d("rmyzx","Refresh........");
				DownloadProgressBar.setProgress((Integer) msg.obj);
				progressnumtext.setText((Integer) msg.obj + "%");
				progressnumtext.setVisibility(View.VISIBLE);
				break;
			case MSG_REFRESHINIT: {
				Log.d("rmyzx","Refresh........");
				DownloadProgressBar.setProgress((Integer) msg.obj);
				if(downloadTask.downloadType==AppItemState.STATE_PAUSE)
				{
					progressnumtext.setVisibility(View.GONE);
				}
				else{
					progressnumtext.setVisibility(View.VISIBLE);
				progressnumtext.setText((Integer) msg.obj + "%");
				}
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
	}

	protected void fillUpViewPager() {
		Log.i("xj", "fillUpViewPager");
		Bundle bundle = new Bundle();
		bundle.putParcelable("app", app);
		bundle.putString(Constants.LIST_FROM, from);
		bundle.putInt("sid", app.sid);

		detailFragment = (DetailFragment1) getSupportFragmentManager()
				.findFragmentByTag("detail_info");
		// detailFragment.setViewPager(viewPager);
		detailFragment.setTabTitle(getString(R.string.sub_jianjie));

		commentFragment = (CommentFragment) getSupportFragmentManager()
				.findFragmentByTag("detail_comm");
		;
		commentFragment.setTabTitle(getString(R.string.comment_title));

		detailFragment.setBundle(bundle);
		commentFragment.setBundle(bundle);
		detailFragment.requestData();
		detailfragmentlayout.setVisibility(View.VISIBLE);
		commonfragmentlayout.setVisibility(View.GONE);

		// tabsAdapter.initViewPagerAdapter(fragments);
		//
		// setViewPagerCurrentItem(0);
		// tabsAdapter.setOnPageChangeListener(this);
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		super.onWindowFocusChanged(hasFocus);
		sendCommentbt.setBackgroundResource(R.drawable.beston_detail_installclick);
		comparepri.setBackgroundResource(R.drawable.beston_detail_priceclick);
	}

	/*
	 * @Override public void onPageScrollStateChanged(int arg0) { }
	 * 
	 * @Override public void onPageScrolled(int arg0, float arg1, int arg2) { }
	 * 
	 * @Override public void onPageSelected(int position) { currentItem =
	 * position; if (position == COMMENT) { //
	 * sendcommentlayout.setVisibility(View.VISIBLE);
	 * commArea.setVisibility(View.VISIBLE); introArea.setVisibility(View.GONE);
	 * } else if (position == 0) { //
	 * sendcommentlayout.setVisibility(View.GONE);
	 * commArea.setVisibility(View.GONE); introArea.setVisibility(View.VISIBLE);
	 * doRefresh(); } }
	 */

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

	/* add by zengxiao */

	protected void doComments(String content, int sid, String vname) {
		AppComment appComment = new AppComment();
		appComment.modle = Build.MODEL;
		appComment.name = "uname";
		appComment.sdesc = content;
		appComment.vname = vname;
		appComment.sid = sid;
		requestData(appComment);
	}

	public void requestData(AppComment comment) {
		if (isRequesting) {
			return;
		}
		String comm = comment.sdesc;
		String vname = comment.vname;

		JSONObject jObject = null;
		try {

			jObject = new JSONObject();
			jObject.put("USID", MyApplication.getInstance().getUser().getUid());
			jObject.put("TYPE", MyApplication.getInstance().getUser().getType());
			jObject.put("CONTENT", comm);
			jObject.put("VNAME", vname);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String comments = jObject.toString();
		isRequesting = true;
		ProtocolTask mTask = new ProtocolTask(this);
		mTask.setListener(this);
		HashMap<String, String> map = getHeader();
		if (map != null
				&& (map.get("vcode") == null || map.get("model") == null)) {
			Toast.makeText(this, getString(R.string.commentfail), 150).show();
			return;
		}
		mTask.execute(getRequestUrl(comment.sid), comments, tag(), map);
	}

	public HashMap<String, String> getHeader() {
		String model = Util.mobile;
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		if (model == null)
			model = Util.getModel();
		if (imei == null)
			imei = Util.getIMEI(this);
		if (vcode == null)
			vcode = Util.getVcode(this);
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(this);
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("model", model);
		map.put("channel", channel);
		return map;
	}

	private String tag() {
		return this.getClass().getSimpleName();
	}

	private String getRequestUrl(int sid) {
		return Constants.LIST_URL + "?qt=" + Constants.UCOMM + "&sid=" + sid
				+ "&uid=" + MyApplication.getInstance().getUser().getUid();
	}

	@Override
	public void onNoNetworking() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNetworkingError() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostExecute(byte[] bytes) {
		isRequesting = false;
		try {
			if (bytes != null) {
				JSONObject result = new JSONObject(new String(bytes));
				int status = JsonParse.parseResultStatus(result);
				if (status == 1) {
					Toast.makeText(DetailFrame2.this,
							getString(R.string.commentsuccess), 150).show();
					setResult(123456);
					// finish();
					commentFragment.pullRefresh();

					commentText.setText("");
					hideSoftInput(this, commentText);
					overridePendingTransition(0, R.anim.push_bottom_out);
				} else if (status == 0) {
					Toast.makeText(DetailFrame2.this,
							getString(R.string.submiterror), 150).show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(DetailFrame2.this, getString(R.string.submiterror),
					150).show();
		}
	}

	public static void showSoftInput(Context context, View view) {
		final InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, 0);
	}

	public static void hideSoftInput(Context context, View view) {
		final InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public void hideheaddetail()// 隐藏应用头
	{
		Log.d("zxnew", "hideheaddetail() isshowtitle=" + isshowtitle);
		if (isshowtitle) {

			mapptv_type.setVisibility(View.GONE);
			findViewById(R.id.imagedivider).setVisibility(View.GONE);
			appdetailinit.setVisibility(View.GONE);
			isshowtitle = false;
		}
	}

	public void showheaddetail()// 显示应用头
	{
		Log.d("zxnew", "showheaddetail() isshowtitle=" + isshowtitle);
		if (!isshowtitle) {

			mapptv_type.setVisibility(View.VISIBLE);
			findViewById(R.id.imagedivider).setVisibility(View.VISIBLE);
			appdetailinit.setVisibility(View.VISIBLE);
			isshowtitle = true;
		}
	}
	/* add end */

}
