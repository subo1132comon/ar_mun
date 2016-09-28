package com.byt.market.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.DownLoadManageActivity;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.MyFaviorateActivity;
import com.byt.market.activity.UserActivity;
import com.byt.market.data.AppItem;
import com.byt.market.data.UserData;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.BitmapUtil;
import com.byt.market.util.DataUtil;
import com.byt.market.util.LogUtil;
import com.byt.market.util.PackageUtil;

/**
 * 我的游戏
 */
public class MineFragment extends BaseUIFragment implements OnClickListener,
		OnItemClickListener, DownloadTaskListener {

	// views
	/** 登录状态控制总view **/
	private LinearLayout lay_loginned_status;
	/** 未登录状态控制总view **/
	private RelativeLayout lay_login_status;
	/** 用户头像,头像只分男女两个头像,不允许用户进行编辑 **/
	private ImageView img_user_avatar;
	/** 用户登录 **/
	private Button btn_user_login;
	/** 用户注册 **/
	private Button btn_user_register;
	/** 用户设置 **/
	private Button btn_user_settings;
	/** 用户昵称 **/
	private TextView tv_user_name;
	/** 用户性别 **/
	private TextView tv_user_sex;
	/** 我的收藏总控制view **/
	private LinearLayout lay_my_collect;
	/** 已本地安装的游戏数量提示 **/
	private TextView txt_my_installed_game_count;
	/** 可升级游戏,点击查看升级 **/
	private Button btn_my_installed_game_updatable;
	/** 本地已安装游戏 **/
	private GridView gv_my_game;
	/** 我的游戏列表区 **/
	private LinearLayout lay_my_game;
	/** 正在加载view **/
	private RelativeLayout lay_loading;
	/** 加载失败view **/
	private RelativeLayout lay_loadfailed;
	/** 找游戏去 **/
	private Button btn_find_game;
	/** 我的收藏以及数量 **/
	private TextView tv_my_fav_desc;

	// variables
	/** 是否用户已经登录了 **/
	private boolean isUserLoginned;
	/** 是否是男性 **/
	private boolean isFemale;
	/** 用户昵称 **/
	private String userNickName;
	TextView userNameID;
	TextView userPoint;
	/*
	 *  ------------The Bestone modifed start--------------
	 *   Modified by "zengxiao"  Date:20140512
	 *   Modified for: 在收藏后刷新我的收藏页面
	 */
	BroadcastReceiver updateReceiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			refreshStatus();
		}
	};
	
	/*
　　　　 ------------The Bestone modifed end--------------
　　　*/
	/** 我的游戏列表 **/
	private List<AppItem> myAppItems = new ArrayList<AppItem>();
	private MyGamesAdapter myGamesAdapter;
	private RefreshLocalGameReceiver refreshLocalGameReceiver;

	public MineFragment() {

	}

	private static final int MSG_NOTIFY_FAV_COUNT_CHANGED = 0;
	private static final int MSG_NOTIFY_UPDATE_COUNT_CHANGED = 1;
	private static final int MSG_NOTIFY_UPDATE_VIEW_CHANGED = 2;
	private int allUpdateCount;

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NOTIFY_FAV_COUNT_CHANGED:
				tv_my_fav_desc
						.setText(getString(R.string.text_my_faviorate_title)
								+ "(" + msg.arg1 + ")");
				break;
			case MSG_NOTIFY_UPDATE_COUNT_CHANGED:
				if (allUpdateCount == 0) {
					btn_my_installed_game_updatable.setVisibility(View.GONE);
				} else {
					btn_my_installed_game_updatable
							.setText(getString(R.string.text_game_can_update_count)

									+ getString(R.string.leftkh)
									+ allUpdateCount
									+ getString(R.string.rightkh));
				}
				break;
			case MSG_NOTIFY_UPDATE_VIEW_CHANGED:
				new MyGameTask().execute(false);
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DownloadTaskManager.getInstance().addListener(this);
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.mine_frame, container, false);
		initViews(view);
		initVariables();
		initListeners();
		bindData();
		registerReceivers();
		reload();
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		LogUtil.i("0419", "minefragment onResume ");
		refreshStatus();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (refreshLocalGameReceiver != null) {
			getActivity().unregisterReceiver(refreshLocalGameReceiver);
			getActivity().unregisterReceiver(updateReceiver);
		}
		DownloadTaskManager.getInstance().removeListener(this);
	}

	private void registerReceivers() {
		refreshLocalGameReceiver = new RefreshLocalGameReceiver();
		IntentFilter intentFilter = new IntentFilter(
				Constants.Action.REFRESH_LOCAL_GAME);
		getActivity().registerReceiver(refreshLocalGameReceiver, intentFilter);
		getActivity().registerReceiver(updateReceiver,new IntentFilter("com.byt.market.updatemycellet"));
		
	}

	private void initViews(View rootView) {
		/*lay_loginned_status = (LinearLayout) rootView
				.findViewById(R.id.lay_loginned_status);
		lay_login_status = (RelativeLayout) rootView
				.findViewById(R.id.lay_login_status);*/
		//img_user_avatar = (ImageView) rootView
			//	.findViewById(R.id.img_user_avatar);
/*		btn_user_login = (Button) rootView.findViewById(R.id.btn_user_login);
		btn_user_register = (Button) rootView
				.findViewById(R.id.btn_user_register);
		btn_user_settings = (Button) rootView
				.findViewById(R.id.btn_user_settings);*/
		//tv_user_name = (TextView) rootView.findViewById(R.id.tv_user_name);
		//tv_user_sex = (TextView) rootView.findViewById(R.id.tv_user_sex);
		lay_my_collect = (LinearLayout) rootView
				.findViewById(R.id.lay_my_collect);
		txt_my_installed_game_count = (TextView) rootView
				.findViewById(R.id.txt_my_installed_game_count);
		btn_my_installed_game_updatable = (Button) rootView
				.findViewById(R.id.btn_my_installed_game_updatable);
		gv_my_game = (GridView) rootView.findViewById(R.id.gv_my_game);
		lay_my_game = (LinearLayout) rootView.findViewById(R.id.lay_my_game);
		lay_loading = (RelativeLayout) rootView.findViewById(R.id.lay_loading);
		lay_loadfailed = (RelativeLayout) rootView
				.findViewById(R.id.lay_loadfailed);
		btn_find_game = (Button) rootView.findViewById(R.id.btn_find_game);
		tv_my_fav_desc = (TextView) rootView.findViewById(R.id.txt_my_collect);
		userNameID= (TextView) rootView.findViewById(R.id.username);
		userPoint= (TextView) rootView.findViewById(R.id.userpoint);
	}

	private void initVariables() {
		myGamesAdapter = new MyGamesAdapter();
	}

	private void initListeners() {
		//btn_user_login.setOnClickListener(this);
		//btn_user_register.setOnClickListener(this);
		//btn_user_settings.setOnClickListener(this);
		lay_my_collect.setOnClickListener(this);
		btn_my_installed_game_updatable.setOnClickListener(this);
		gv_my_game.setOnItemClickListener(this);
		btn_find_game.setOnClickListener(this);

	}

	private void bindData() {
		gv_my_game.setAdapter(myGamesAdapter);
		refreshPoint();
	}
	
	private void reload() {
//		new MyGameTask().execute(false);
		handler.sendEmptyMessage(MSG_NOTIFY_UPDATE_VIEW_CHANGED);
	}

	/**
	 * 刷新界面状态
	 */
	private void refreshStatus() {
		//refreshUserName();
		//refreshUserLoginStatus();
		//refreshUserAvatarStatus();
		refreshPoint();
		refreshMyFavCountAndNeedUpdateCountAndMessageCount();
	}/* bestone add by zengxiao  for:添加积分 */
	private void refreshPoint() {
		UserData user = MyApplication.getInstance().getUser();
		if (user.isLogin()) {
			isUserLoginned = true;
			userNickName = user.getUid();
			if (!TextUtils.isEmpty(userNickName)) {
				userNameID.setText(getString(R.string.username)+userNickName);			
			}
			userPoint.setText(getString(R.string.userpoint)+user.getCredit());
		} else {
			isUserLoginned = false;
		}
	
	}
	/* bestone add end */
	private void refreshMyFavCountAndNeedUpdateCountAndMessageCount() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<AppItem> favList = DataUtil.getInstance(getActivity())
						.getFaviorateList();
				Message favMessage = new Message();
				favMessage.what = MSG_NOTIFY_FAV_COUNT_CHANGED;
				favMessage.arg1 = favList.size();
				handler.sendMessage(favMessage);
				allUpdateCount = DownloadTaskManager.getInstance()
						.getNeedUpdateAppCount();
				LogUtil.i("0419", "needUpdateCount = " + allUpdateCount);
				handler.sendEmptyMessage(MSG_NOTIFY_UPDATE_COUNT_CHANGED);
			}
		}).start();
	}

	/*private void refreshUserLoginStatus() {
		if (isUserLoginned) {
			lay_loginned_status.setVisibility(View.VISIBLE);
			lay_login_status.setVisibility(View.GONE);
		} else {
			lay_loginned_status.setVisibility(View.GONE);
			lay_login_status.setVisibility(View.VISIBLE);
		}
	}*/

	/*private void refreshUserAvatarStatus() {
		if (isUserLoginned) {
			UserData user = MyApplication.getInstance().getUser();
			if (UserData.FEMALE.equals(user.getGender())) {
				tv_user_sex.setText(R.string.txt_female_user);
				//img_user_avatar.setImageResource(R.drawable.icon_user_default);
			} else {
				tv_user_sex.setText(R.string.txt_male_user);
				//img_user_avatar.setImageResource(R.drawable.icon_user_default);
			}
		} else {
			//img_user_avatar.setImageResource(R.drawable.icon_user_default);
		}
	}*/

	private void refreshUserName() {
		UserData user = MyApplication.getInstance().getUser();
		if (user.isLogin()) {
			isUserLoginned = true;
			userNickName = user.getNickname();
			if (!TextUtils.isEmpty(userNickName)) {
				tv_user_name.setText(userNickName);
			}
		} else {
			isUserLoginned = false;
		}
	}

	/**
	 * 如果事件已经被处理，返回true，这样事件不会在Activity里再处理
	 * */
	public boolean onBackDown() {
		return MyApplication.getInstance().mMineViewManager.onBackDown();
	}

	private class MyGameTask extends AsyncTask<Boolean, AppItem, List<AppItem>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			lay_my_game.setVisibility(View.GONE);
			lay_loadfailed.setVisibility(View.GONE);
			lay_loading.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<AppItem> doInBackground(Boolean... params) {
			final List<AppItem> list = DownloadTaskManager.getInstance()
					.getMyDownloadItems();
			return list;
		}

		@Override
		protected void onProgressUpdate(AppItem... values) {
		}

		@Override
		protected void onPostExecute(List<AppItem> result) {
			super.onPostExecute(result);
			LogUtil.e("cexo", "MineFragment onPostExecute()");
			lay_loading.setVisibility(View.GONE);
			try {
				txt_my_installed_game_count.setText(String.format(MyApplication
						.getInstance().getString(R.string.txt_my_game_count),
						result.size()));
				myAppItems = result;
				myGamesAdapter.notifyDataSetChanged();
				checkDataEmptyStatus();
				LogUtil.e("cexo",
						"MineFragment onPostExecute() gv_my_game.getAdapter:"
								+ gv_my_game.getAdapter());
			} catch (Exception e) {
				LogUtil.e("cexo",
						"MineFragment onPostExecute():" + e.getMessage());
				myAppItems = result;
				myGamesAdapter.notifyDataSetChanged();
				checkDataEmptyStatus();
			}
			LogUtil.e("cexo", "MineFragment onPostExecute() visible:"
					+ lay_my_game.getVisibility());
		}
	}

	private void checkDataEmptyStatus() {
		if (myAppItems == null || myAppItems.size() == 0) {
			lay_loadfailed.setVisibility(View.VISIBLE);
			lay_my_game.setVisibility(View.GONE);
		} else {
			lay_loadfailed.setVisibility(View.GONE);
			lay_my_game.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 我的游戏Adapter
	 */
	private final class MyGamesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return myAppItems.size();
		}

		@Override
		public AppItem getItem(int position) {
			return myAppItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AppItem appItem = getItem(position);
			MyViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.my_game_list_item, null);
				holder = new MyViewHolder();
				holder.img_my_game_icon = (ImageView) convertView
						.findViewById(R.id.img_my_game_icon);
				holder.img_my_game_new_icon = (ImageView) convertView
						.findViewById(R.id.img_my_game_new_icon);
				holder.tv_my_game_name = (TextView) convertView
						.findViewById(R.id.tv_my_game_name);

				convertView.setTag(holder);
			} else {
				holder = (MyViewHolder) convertView.getTag();
			}
			holder.tv_my_game_name.setText(appItem.name);
			if (appItem.icon != null) {
				holder.img_my_game_icon.setImageBitmap(appItem.icon);
			} else {
				holder.img_my_game_icon
						.setImageResource(android.R.drawable.sym_def_app_icon);
			}
			if (appItem.isInstalledButNotLauchered(getActivity())) {
				holder.img_my_game_new_icon.setVisibility(View.VISIBLE);
				holder.img_my_game_new_icon.setImageBitmap(BitmapUtil
						.createNewTextImage(getActivity(), R.drawable.icon_new,
								R.string.alert_new));
			} else {
				holder.img_my_game_new_icon.setVisibility(View.GONE);
			}
			return convertView;
		}

	}

	public static class MyViewHolder {
		ImageView img_my_game_icon;
		TextView tv_my_game_name;
		ImageView img_my_game_new_icon;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		/*case R.id.btn_user_login:// 用户登录
			doUserLogin();
			break;
		case R.id.btn_user_register:// 用户注册
			doUserRegister();
			break;
		case R.id.btn_user_settings:// 用户设置
			doUserSettings();
			break;*/
		case R.id.lay_my_collect:// 我的收藏
			doMyCollect();
			break;
		case R.id.btn_my_installed_game_updatable:// 可升级游戏
			doUpdatableGames();
			break;
		case R.id.btn_find_game:// 找游戏去
			doFindGames();
			break;
		}
	}

	/**
	 * 用户登录
	 */
	private void doUserLogin() {
		Intent intent = new Intent(getActivity(), UserActivity.class);
		intent.putExtra(UserActivity.FRAGMENT_USER_ACTION,
				UserActivity.TYPE_USER_LOGIN);
		startActivity(intent);
	}

	/**
	 * 用户注册
	 */
	private void doUserRegister() {
		Intent intent = new Intent(getActivity(), UserActivity.class);
		intent.putExtra(UserActivity.FRAGMENT_USER_ACTION,
				UserActivity.TYPE_USER_REGIST);
		startActivity(intent);
	}

	/**
	 * 用户设置
	 */
	private void doUserSettings() {
		Intent intent = new Intent(getActivity(), UserActivity.class);
		startActivity(intent);
	}

	/**
	 * 我的收藏
	 */
	private void doMyCollect() {
		Intent intent = new Intent(getActivity(), MyFaviorateActivity.class);
		startActivity(intent);
	}

	private void doUpdatableGames() {
		Intent intent = new Intent(getActivity(), DownLoadManageActivity.class);
		intent.putExtra(DownLoadManageActivity.TYPE_FROM,
				DownLoadManageActivity.TYPE_FROM_UPDATE);
		intent.putExtra(DownLoadManageActivity.ALL_UPDATE_COUNT, allUpdateCount);
		startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AppItem appItem = myGamesAdapter.getItem(position);
		boolean isReload = false;
		if (appItem.isInstalledButNotLauchered(getActivity())) {
			isReload = true;
		}
		appItem.isOpenned = 1;
		DownloadTaskManager.getInstance().updateAppItem(appItem);
		myGamesAdapter.notifyDataSetChanged();
		PackageUtil.startApp(MyApplication.getInstance()
				.getApplicationContext(), appItem.pname);
		if (isReload) {
//			new MyGameTask().execute(false);
			handler.sendEmptyMessage(MSG_NOTIFY_UPDATE_VIEW_CHANGED);
		}
	}

	/**
	 * 找游戏去
	 */
	private void doFindGames() {
		((MainActivity) getActivity()).changeToHome();
	}

	@Override
	public void startConnecting(DownloadTask task, int totalTask,
			int progressTask) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadStarted(DownloadTask task, int totalTask,
			int progressTask, long totalSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadProgress(DownloadTask task, int totalTask,
			int progressTask, long progressSize, long totalSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadEnded(DownloadTask task, int totalTask, int progressTask) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endConnecting(DownloadTask task, int totalTask,
			int progressTask, DownloadException result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refreshUI() {
		LogUtil.e("cexo", "MineFragment refreshUI()");
		// new MyGameTask().execute(false);
	}

	@Override
	public void installedSucess(DownloadTask downloadTask) {
		LogUtil.e("cexo", "MineFragment installedSucess");
//		new MyGameTask().execute(false);
		handler.sendEmptyMessage(MSG_NOTIFY_UPDATE_VIEW_CHANGED);
	}

	@Override
	public void unInstalledSucess(String packageName) {
//		new MyGameTask().execute(false);
		handler.sendEmptyMessage(MSG_NOTIFY_UPDATE_VIEW_CHANGED);
	}

	/**
	 * 刷新我的游戏
	 */
	private final class RefreshLocalGameReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.Action.REFRESH_LOCAL_GAME.equals(intent.getAction())) {
				LogUtil.e("cexo", "MyGameActivity REFRESH_LOCAL_GAME");
//				new MyGameTask().execute(false);
				handler.sendEmptyMessage(MSG_NOTIFY_UPDATE_VIEW_CHANGED);
			}
		}
	}

	@Override
	public void networkIsOk() {
		// TODO Auto-generated method stub

	}

	@Override
	public void installedSucess(String packageName) {
		handler.sendEmptyMessage(MSG_NOTIFY_UPDATE_VIEW_CHANGED);
	}

}
