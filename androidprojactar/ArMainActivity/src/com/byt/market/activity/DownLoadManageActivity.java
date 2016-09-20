package com.byt.market.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.adapter.MyListAdapter;
import com.byt.market.adapter.MyListAdapter.RecordLayoutHolder;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.display.RoundedBitmapDisplayer;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadAppItem;
import com.byt.market.data.DownloadItem;
import com.byt.market.data.UserData;
import com.byt.market.download.DownloadBtnClickListener;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.util.DataUtil;
import com.byt.market.util.LogUtil;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.SystemUtil;
import com.byt.market.util.Util;
import com.byt.market.view.LoadFailedView;

/**
 * 下载管理
 */
public class DownLoadManageActivity extends BaseActivity implements
		OnClickListener, DownloadTaskListener {
	public static final int TYPE_FROM_DOWNLOAD = 4;
	public static final int TYPE_FROM_UPDATE = 5;
	public static final String TYPE_FROM = "type_from";
	public static final String ALL_UPDATE_COUNT = "all_update_count";
	private static final String TO_DETAIL = "to_detail";
	private static final long NOTIFY_DELAY = 1000;
	private static final int MSG_NOTIFY_DATA_CHANGED = 1;
	private static final int MSG_NOTIFY_DATA_EMPTY = 2;
	private static final int MSG_NOTIFY_GAMES_DOWNLOADING = 3;
	private static final int MSG_NOTIFY_GAMES_DOWNUPDAING = 8;
	private static final int MSG_UPDATE_TITLE = 6;
	private int all_update_count;

	// views
	private LoadFailedView noContentLayout;
	private ListView lv_download_list;
	private TextView tv_topbar_title;
	private ImageView iv_menu_settings;
	private ImageView btn_menu_search;
	private ImageView iv_titlebar_back_arrow;
	/** 更多操作，全部继续，暂停，取消等 **/
	private PopupWindow popupWindow;
	private RelativeLayout operateCtrlBar;
	private Button update_all_button;

	private DownloadAdapter downloadAdapter;
	private List<DownloadAppItem> downLoadAppItemList = new ArrayList<DownloadAppItem>();
	private PackageManager pm;
	private DownloadAppItem mShowDownloadAppItem;
	private String intentTo;
	// int mScrollState;
	AppItem mAppitem;
	PopupWindow moptionmenu;
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NOTIFY_DATA_CHANGED:
				onDownloadStateChanged();
				if (downLoadAppItemList.size() > 0) {
					if (iv_menu_settings != null && isShowDownloadContent()) {
						iv_menu_settings.setVisibility(View.GONE);
					}
				} else {
					if (iv_menu_settings != null) {
						iv_menu_settings.setVisibility(View.GONE);
					}
				}
				break;
			case MSG_NOTIFY_DATA_EMPTY:
				lv_download_list.setVisibility(View.GONE);
				noContentLayout.setVisibility(View.VISIBLE);
				if (iv_menu_settings != null) {
					iv_menu_settings.setVisibility(View.GONE);
				}
				break;
			case MSG_NOTIFY_GAMES_DOWNLOADING:
				lv_download_list.setVisibility(View.VISIBLE);
				noContentLayout.setVisibility(View.GONE);
				break;
			case MSG_UPDATE_TITLE:
				all_update_count = downLoadAppItemList.size();
				tv_topbar_title
						.setText(getString(R.string.text_game_update_title)
								+ "(" + all_update_count + ")");
				// if(all_update_count > 0){
				// operateCtrlBar.setVisibility(View.VISIBLE);
				// }
				if (all_update_count < 2) {
					operateCtrlBar.setVisibility(View.GONE);
				}
				try {
					downloadAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case MSG_NOTIFY_GAMES_DOWNUPDAING:
				try {
					downloadAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.listview_common);
		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		// getDimenByResId(R.dimen.listitem_app_icon)
		initView();
		initData();
		addEvent();
		loadData();
	}

	/**
	 * 是否显示下载管理，否则显示游戏更新
	 * 
	 * @return
	 */
	private boolean isShowDownloadContent() {
		return getIntent().getIntExtra(TYPE_FROM, TYPE_FROM_DOWNLOAD) == TYPE_FROM_DOWNLOAD;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (TO_DETAIL.equals(intentTo)) {
			LogUtil.i("0426", "touch onResume intent to ");
			intentTo = "";
		} else {
			LogUtil.i("0426", "touch onResume onDownloadStateChanged");
			onDownloadStateChanged();
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			if (moptionmenu != null && moptionmenu.isShowing()) {
				moptionmenu.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			DownloadTaskManager.getInstance().removeListener(this);
			lv_download_list.setAdapter(null);
			downloadAdapter=null;
			downLoadAppItemList.clear();
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initView() {
		noContentLayout = (LoadFailedView) findViewById(R.id.rl_nocontent);
		operateCtrlBar = (RelativeLayout) findViewById(R.id.operateCtrlBar);
		update_all_button = (Button) findViewById(R.id.button_update_all);
		if (isShowDownloadContent()) {
			noContentLayout
					.setText(getString(R.string.list_empty_view_tip_content2));
			noContentLayout.setImageIcon(R.drawable.nothing_cute);
			noContentLayout.setButtonVisible(View.VISIBLE);
			operateCtrlBar.setVisibility(View.GONE);
		} else {
			noContentLayout
					.setText(getString(R.string.list_empty_view_tip_content));
			noContentLayout.setImageIcon(R.drawable.nothing_smile);
			noContentLayout.setButtonVisible(View.GONE);
			operateCtrlBar.setVisibility(View.VISIBLE);
		}

		noContentLayout.setVisibility(View.GONE);
		lv_download_list = (ListView) findViewById(R.id.lv_list);
		initTtileBar();
	}

	private void initTtileBar() {
		btn_menu_search = (ImageView) findViewById(R.id.titlebar_search_button);
		btn_menu_search.setVisibility(View.INVISIBLE);
		tv_topbar_title = (TextView) findViewById(R.id.titlebar_title);
		iv_menu_settings = (ImageView) findViewById(R.id.iv_settings);
		iv_menu_settings.setVisibility(View.GONE);

		iv_menu_settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showMoreOptions(v);
			}
		});
		if (isShowDownloadContent()) {
			tv_topbar_title.setText(R.string.text_download_manager_title);
		} else {
			all_update_count = getIntent().getIntExtra(ALL_UPDATE_COUNT, 0);
			tv_topbar_title.setText(getString(R.string.text_game_update_title)
					+ "(" + all_update_count + ")");
			iv_menu_settings.setVisibility(View.GONE);
		}
		iv_titlebar_back_arrow = (ImageView) findViewById(R.id.titlebar_back_arrow);
		iv_titlebar_back_arrow.setVisibility(View.VISIBLE);
		ImageView iv_titlebar_icon = (ImageView) findViewById(R.id.titlebar_icon);
		iv_titlebar_icon.setVisibility(View.GONE);
	}

	@Override
	public void initData() {
		downloadAdapter = new DownloadAdapter();
		lv_download_list.setAdapter(downloadAdapter);
	}

	@Override
	public void addEvent() {
		iv_titlebar_back_arrow.setOnClickListener(this);
		update_all_button.setOnClickListener(this);
		pm = getPackageManager();
	}

	private void loadData() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				fillListData();
				checkListEmpty();
				DownloadTaskManager.getInstance().addListener(
						DownLoadManageActivity.this);
			}
		}).start();
	}

	private void fillDownloadItemListData() {
		ArrayList<DownloadAppItem> downdingItemList = new ArrayList<DownloadAppItem>();
		ArrayList<DownloadAppItem> downdedItemList = new ArrayList<DownloadAppItem>();
		List<DownloadAppItem> temp=new ArrayList<DownloadAppItem>();
		final List<AppItem> alllist = DownloadTaskManager.getInstance()
				.getAllDownloadItem(false);
		DownloadTaskManager.getInstance().fillAppStates(alllist);
		for (AppItem appItem : alllist) {
			// Log.i("zc","appItem name = "+appItem.name+",state = "+appItem.state);
			if (isDownloading(appItem.state)) {
				downdingItemList.add(new DownloadAppItem(appItem, null));
			} else {
				// 已安装的包不显示
				if (PackageUtil.isInstalledApk(this, appItem.pname, null)) {
					continue;
				}
				// 未安装的包且安装包不存在的不显示，若安装包存在则显示安装
				if (!PackageUtil.isInstalledApk(this, appItem.pname, null)
						&& !DownloadTaskManager.getInstance().apkFileExist(
								appItem)) {
					continue;
				}
				downdedItemList.add(new DownloadAppItem(appItem, null));
			}
		}
		int downdingItemListSize = downdingItemList.size();
		if (downdingItemListSize > 0) {			
			temp.add(new DownloadAppItem(null,
					getString(R.string.downloading_title_desc) + "("
							+ downdingItemListSize + ")"));
			temp.addAll(downdingItemList);
		}
		int downdedItemListSize = downdedItemList.size();
		if (downdedItemListSize > 0) {
			Collections.reverse(downdedItemList);
			temp.add(new DownloadAppItem(null,
					getString(R.string.downloaded_title_desc) + "("
							+ downdedItemListSize + ")"));
			temp.addAll(downdedItemList);
		}	
		downLoadAppItemList=temp;
		handler.removeMessages(MSG_NOTIFY_GAMES_DOWNUPDAING);
		handler.sendEmptyMessage(MSG_NOTIFY_GAMES_DOWNUPDAING);
	}

	private void fillUpdateItemListData() {
		final List<AppItem> alllist = DownloadTaskManager.getInstance()
				.getAllInstallDownloadItem(false);
		DownloadTaskManager.getInstance().fillAppStates(alllist);
		List<DownloadAppItem> temp=new ArrayList<DownloadAppItem>();
		for (AppItem appItem : alllist) {
			int localeVersionCode = 0;
			try {
				localeVersionCode = pm.getPackageInfo(appItem.pname, 0).versionCode;
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			// LogUtil.i("appupdate","localeVersionCode = "+a);
			if (appItem.vcode > localeVersionCode) {
				temp.add(new DownloadAppItem(appItem, null));
//				downLoadAppItemList.add(new DownloadAppItem(appItem, null));
			}
		}
		downLoadAppItemList=temp;
		handler.sendEmptyMessage(MSG_UPDATE_TITLE);
//		Log.i("0416",
//				"needUpdateAppItemList size = " + downLoadAppItemList.size());
//		Log.i("appupdate",
//				"needUpdateAppItemList size = " + DownloadTaskManager.getInstance().getAllUpdatingAppList().size());
	}

	private void fillListData() {
		if (isShowDownloadContent()) {
			fillDownloadItemListData();
		} else {
			fillUpdateItemListData();
		}
	}

	private void checkListEmpty() {
		if (downLoadAppItemList.size() > 0) {
			handler.sendEmptyMessage(MSG_NOTIFY_GAMES_DOWNLOADING);
		} else {
			handler.sendEmptyMessage(MSG_NOTIFY_DATA_EMPTY);
		}
		handler.removeMessages(MSG_NOTIFY_DATA_CHANGED);
		handler.sendEmptyMessage(MSG_NOTIFY_DATA_CHANGED);
	}

	/**
	 * 是否属于正在下载列表
	 * 
	 * @param state
	 * @return
	 */
	private boolean isDownloading(int state) {
		if (state < AppItemState.STATE_DOWNLOAD_FINISH
				&& state > AppItemState.STATE_IDLE) {
			return true;
		}
		return false;
	}

	private final class DownloadAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return downLoadAppItemList.size();
		}

		@Override
		public Object getItem(int position) {
			return downLoadAppItemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean isEnabled(int position) {
			return downLoadAppItemList.get(position).isClickable();
		}

		@Override
		public View getView(int position, View convertView,
				ViewGroup parent) {
			DownTaskItem myDowntaskItem;
			DownloadAppItem downLoadAppItem = downLoadAppItemList.get(position);
			final AppItem appItem = downLoadAppItem.getAppItem();
			if (appItem == null) {
				View view = getLayoutInflater().inflate(
						R.layout.listitem_download_manager_category_item, null);
				view.setClickable(false);
				view.setEnabled(false);
				TextView textView = (TextView) view
						.findViewById(R.id.tv_download_count_title);
				textView.setText(downLoadAppItem.getText());
				return view;
			} else {
				if(convertView==null||!(convertView.getTag() instanceof DownTaskItem))
				{
				myDowntaskItem=new DownTaskItem();
				convertView = getLayoutInflater().inflate(
						R.layout.listitem_download_manage_item, null);
				
				RelativeLayout item_choose_layout = (RelativeLayout) convertView
						.findViewById(R.id.item_choose_layout);
				final MyViewHolder myViewHolder = new MyViewHolder();
				myViewHolder.downloadAppItem = downLoadAppItem;
				myViewHolder.position = position;				
				
				myDowntaskItem.listitem_rec_icon = (ImageView) convertView
						.findViewById(R.id.listitem_rec_icon);
				myDowntaskItem.tv_name = (TextView) convertView
						.findViewById(R.id.listitem_rec_name);
				myDowntaskItem.listitem_rec_category = (TextView) convertView
						.findViewById(R.id.listitem_rec_category);
				myDowntaskItem.listitem_rec_btn = (Button) convertView
						.findViewById(R.id.bt_down_btn_update);
				myDowntaskItem.homedownloadProgressBar = (ProgressBar) convertView
						.findViewById(R.id.homedownloadProgressBar);
				myDowntaskItem.tv_download_summary = (TextView) convertView
						.findViewById(R.id.listitem_rec_category2);
				myDowntaskItem.tv_catrgory2_right = (TextView) convertView
						.findViewById(R.id.listitem_rec_category2_right);
				
				myDowntaskItem.statesDesc = (TextView) convertView
						.findViewById(R.id.listitem_rec_category_right);
				convertView.setTag(myDowntaskItem);
				}else{
					
					myDowntaskItem=	(DownTaskItem) convertView.getTag();
				}
				final View myview=convertView;
				if (!isShowDownloadContent()) {
					myDowntaskItem.tv_catrgory2_right.setVisibility(View.VISIBLE);
					myDowntaskItem.tv_catrgory2_right.setText(appItem.strLength);
					if (appItem.state != AppItemState.STATE_NEED_UPDATE) {
						myDowntaskItem.tv_catrgory2_right.setVisibility(View.GONE);
					}
				}
				
				appItem.strLength = Formatter.formatShortFileSize(
						DownLoadManageActivity.this, appItem.length);
			
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (isShowDownloadContent()) {
							/*addb by zengxiao for:添加弹出对话框*/
							List<String> myList = new ArrayList<String>();
							myList.add(getString(R.string.delete_downtask));
							ViewGroup optionViewGroup = (ViewGroup) getLayoutInflater()
									.inflate(
											R.layout.bestone_optionmenu,
											null);
							ListView mylistview = (ListView) optionViewGroup
									.findViewById(R.id.optionmenulist);
							PopupWindow optionmenu = new PopupWindow(
									optionViewGroup,
									getResources()
											.getDimensionPixelSize(
													R.dimen.popupWindow_width),
									LayoutParams.WRAP_CONTENT);
							DownListAdapter optiondialog = new DownListAdapter(myList);
							mylistview.setAdapter(optiondialog);

							optionmenu.setFocusable(true);
							optionmenu
									.setBackgroundDrawable(new BitmapDrawable());
							optionmenu.setOutsideTouchable(true);	
							int[] location = new  int[2] ;
							myview.getLocationInWindow(location); //获取在当前窗口内的绝对坐标
						    
							optionmenu
									.showAtLocation(myview, Gravity.TOP, location[0], location[1]+100);
							mAppitem=appItem;
							moptionmenu = optionmenu;																											
						} else {
							toDetail(appItem);
						}
					}
				});

				StringBuilder sb = new StringBuilder();
				sb.append(appItem.cateName).append(" | ");
				sb.append(appItem.strLength).append(" | ");
				sb.append(appItem.downNum);
				myDowntaskItem.listitem_rec_category.setText(sb.toString());
				myDowntaskItem.listitem_rec_btn
						.setOnClickListener(new DownloadBtnClickListener(
								appItem));
				if (TextUtils.isEmpty(appItem.iconUrl)) {
					Drawable drawable = getResources().getDrawable(
							R.drawable.app_empty_icon);
					if (isDownloading(appItem.state)) {
						myDowntaskItem.listitem_rec_icon.setImageDrawable(drawable);
					} else {
						try {
							drawable = pm.getApplicationIcon(appItem.pname);
						} catch (PackageManager.NameNotFoundException e) {
							e.printStackTrace();
						}
						myDowntaskItem.listitem_rec_icon.setImageDrawable(drawable);
					}

				} else {
					if(!String.valueOf(myDowntaskItem.tv_name.getText()).equals(appItem.name))
					imageLoader.displayImage(appItem.iconUrl,
							myDowntaskItem.listitem_rec_icon, options);
				}
				myDowntaskItem.tv_download_summary.setVisibility(View.GONE);
				myDowntaskItem.tv_name.setText(appItem.name);
				DownloadTaskManager.getInstance().updateByDownLoadState(
						myDowntaskItem.listitem_rec_btn, appItem, myDowntaskItem.homedownloadProgressBar,
						myDowntaskItem.listitem_rec_category, true, false, false, null,
						LogModel.L_HOME_REC, myDowntaskItem.tv_download_summary,
						isShowDownloadContent(), myDowntaskItem.statesDesc);
				return convertView;
			}

		}

	}

	public static class MyViewHolder {
		int position;
		RelativeLayout item_choose_layout;
		DownloadAppItem downloadAppItem;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.titlebar_back_arrow:
			finish();
			break;
		case R.id.button_update_all:
			LogUtil.i("0426", "touch button_update_all");
			Toast.makeText(this, R.string.text_update_all_desc, Toast.LENGTH_SHORT).show();
			updateAllGame();
			break;
		}
	}

	private void updateAllGame() {
		for (DownloadAppItem downloadAppItem : downLoadAppItemList) {
			DownloadTask task = DownloadTaskManager
					.getInstance()
					.getDownloadTask(
							MyApplication.getInstance().getApplicationContext(),
							String.valueOf(downloadAppItem.getAppItem().sid),
							String.valueOf(downloadAppItem.getAppItem().vcode));
			DownloadTaskManager.getInstance().goOnDownloadTask(task);
		}
	}

	/**
	 * 更多选项
	 */
	private void showMoreOptions(View view) {
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(
				R.layout.main_page_more, null, true);
		if (popupWindow == null) {
			popupWindow = new PopupWindow(menuView,
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, true);
			popupWindow.setBackgroundDrawable(new BitmapDrawable());
			popupWindow.showAsDropDown(view, SystemUtil.dip2px(this, -10),
					SystemUtil.dip2px(this, -6));
			
			popupWindow.update();
			setOnclickEvent(menuView);
		}else{
			popupWindow.showAsDropDown(view, SystemUtil.dip2px(this, -10),
					SystemUtil.dip2px(this, -6));
		}
/*		// 全部继续
		LinearLayout btn_download_all_go_on = (LinearLayout) menuView
				.findViewById(R.id.btn_download_all_go_on);
		btn_download_all_go_on.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				letAllDownningTaskGoOn();
				popupWindow.dismiss();
			}
		});*/
		
	}

	/**
	 * item更多选项(查看详情，取消下载等等)
	 */
	private void showItemMoreOptions(View view, final AppItem appItem,
			int postion) {
		LogUtil.i("0426", "touch showItemMoreOptions postion = " + postion);
		int state = appItem.state;
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(
				R.layout.item_page_more, null, true);
		LinearLayout parent = (LinearLayout) menuView
				.findViewById(R.id.ll_item_more_option_parent);
		popupWindow = new PopupWindow(menuView,
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setOutsideTouchable(true);
		if (postion == downLoadAppItemList.size() - 1 && postion > 1) {
			parent.setBackgroundResource(R.drawable.item_more_option_bg_down);
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			popupWindow.showAtLocation(
					view,
					Gravity.NO_GRAVITY,
					location[0],
					location[1] - popupWindow.getHeight()
							+ SystemUtil.dip2px(this, -20));
		} else {
			popupWindow.showAsDropDown(view, SystemUtil.dip2px(this, -10),
					SystemUtil.dip2px(this, -6));
		}
		// popupWindow2.showAsDropDown(view, SystemUtil.dip2px(this, -10),
		// SystemUtil.dip2px(this, -6));
		popupWindow.update();
		Button btn_left = (Button) menuView
				.findViewById(R.id.btn_option_item_left);
		Button btn_right = (Button) menuView
				.findViewById(R.id.btn_option_item_right);
		btn_left.setText(R.string.text_look_over_detail_desc);
		if (isDownloading(state)) {
			btn_right.setText(R.string.text_cancel_download_desc);
		} else {
			btn_right.setText(R.string.expend_child5_remove);
		}

		btn_left.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toDetail(appItem);
				popupWindow.dismiss();
			}
		});
		btn_right.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DownloadTask downloadTask = DownloadTaskManager.getInstance()
						.getDownloadTask(
								MyApplication.getInstance()
										.getApplicationContext(),
								String.valueOf(appItem.sid),
								String.valueOf(appItem.vcode));
				DownloadTaskManager.getInstance().deleteDownloadTask(
						downloadTask, true, false);
				doNotifyChange();
				popupWindow.dismiss();
				new Thread(new Runnable() {
					@Override
					public void run() {
						DownloadTaskManager.getInstance().deleteDownlaodFile(
								(DownloadItem) appItem);
					}
				}).start();
			}
		});

	}

	private void toDetail(AppItem appItem) {
		intentTo = TO_DETAIL;
		Intent intent = new Intent(Constants.TODETAIL);
		intent.putExtra("app", appItem);
		intent.putExtra(Constants.LIST_FROM, LogModel.L_HOME_REC);
		startActivity(intent);
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	public void setOnclickEvent(ViewGroup menuView) {
		// 全部继续
		LinearLayout btn_download_all_go_on = (LinearLayout) menuView
				.findViewById(R.id.btn_download_all_go_on);
		btn_download_all_go_on.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				letAllDownningTaskGoOn();
				popupWindow.dismiss();									
			}
		});
		// 全部暂停
		LinearLayout btn_download_all_pause = (LinearLayout) menuView
				.findViewById(R.id.btn_download_all_pause);
		btn_download_all_pause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				letAllDownningTaskPause();
				popupWindow.dismiss();				
			}
		});
		// 全部取消
		LinearLayout btn_download_all_cancel = (LinearLayout) menuView
				.findViewById(R.id.btn_download_all_cancel);
		btn_download_all_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				letAllDownningTaskCancel();
				popupWindow.dismiss();
			}
		});
	}

	private void letAllDownningTaskGoOn() {
		DownloadTaskManager.getInstance().goOnDownloadAllTasks();
	}

	private void letAllDownningTaskPause() {
		DownloadTaskManager.getInstance().letAllDownningTaskPause();
	}

	private void letAllDownningTaskCancel() {
		DownLoadManageActivity.this.sendBroadcast(new Intent("com.intent.refreshupdate"));
		DownloadTaskManager.getInstance().cancelAllTasks(true);
		doNotifyChange();
	}

	@Override
	public void downloadEnded(DownloadTask task, int totalTask, int progressTask) {
		Log.i("rxmyzx", "downloadEnded");
		// doNotifyChange();

	}

	@Override
	public void downloadProgress(DownloadTask task, int totalTask,
			int progressTask, long progressSize, long totalSize) {
		// Log.i("zc", "downloadProgress");
		tryNotifyChange();
	}

	@Override
	public void downloadStarted(DownloadTask task, int totalTask,
			int progressTask, long totalSize) {
		// Log.i("zc","downloadStarted");
		tryNotifyChange();
	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {
		// Log.i("zc","downloadTaskDone");
		/*modify by zengxiao for:网络不好中断是刷新界面*/
		 tryNotifyChange();
		 /*modify end */
	}

	@Override
	public void endConnecting(DownloadTask task, int totalTask,
			int progressTask, DownloadException result) {
	}

	@Override
	public void startConnecting(DownloadTask task, int totalTask,
			int progressTask) {
	}

	// @Override
	// public void installedSucess(DownloadTask downloadTask) {
	// doNotifyChange();;
	// }

	@Override
	public void refreshUI() {
		tryNotifyChange();
	}

	private void tryNotifyChange() {
		if (!handler.hasMessages(MSG_NOTIFY_DATA_CHANGED)) {
			handler.sendEmptyMessageDelayed(MSG_NOTIFY_DATA_CHANGED,
					NOTIFY_DELAY);
		}
	}

	private void doNotifyChange() {
		try {
			downLoadAppItemList.clear();
			downloadAdapter.notifyDataSetChanged();
			loadData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void onDownloadStateChanged() {
		DownloadTaskManager.getInstance().fillDownloadAppItemStates(
				downLoadAppItemList);
		// Log.i("zc", "onDownloadStateChanged");
		try {
			downloadAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unInstalledSucess(String packageName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void installedSucess(DownloadTask downloadTask) {
		// root调用
		// LogUtil.i();
		// doNotifyChange();
	}

	@Override
	public void installedSucess(String packageName) {
		// 非root调用

	}

	@Override
	public void networkIsOk() {
		// TODO Auto-generated method stub

	}
	
	/*
	 * add by zengxiao
	 * for:弹出删除按钮
	 * 
	 * */
	 class DownListAdapter extends BaseAdapter {

		private List<String> myArrList = null;

		private int selectedPosition = -1;//	
		public DownListAdapter(List<String> buttonListView) {
			myArrList = buttonListView;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return myArrList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return myArrList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public void setSelectedPosition(int position) {
			selectedPosition = position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int myposition = position;
			final RecordLayoutHolder mlayout;
			RecordLayoutHolder layoutHolder = null;
			if (convertView == null) {
				layoutHolder = new RecordLayoutHolder();
				convertView = LayoutInflater.from(DownLoadManageActivity.this).inflate(
						R.layout.bestone_choicebutton, null, false);
				layoutHolder.setas = (Button) convertView.findViewById(R.id.setas);
				convertView.setTag(layoutHolder);
			} else {
				layoutHolder = (RecordLayoutHolder) convertView.getTag();
			}
			mlayout = layoutHolder;
			layoutHolder.setas.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (moptionmenu != null && moptionmenu.isShowing()) {
						moptionmenu.dismiss();
					}
					switch (myposition) {
					case 0:
						/*add by zengxiao for:修改在删除任务时判断是否是已安装的任务*/
						if (mAppitem != null) {
							if(PackageUtil.isInstalledApk(DownLoadManageActivity.this, mAppitem.pname, null))
									{
										DownLoadManageActivity.this.sendBroadcast(new Intent("com.intent.refreshupdate"));
									}
							DownloadTask downloadTask = DownloadTaskManager
									.getInstance().getDownloadTask(
											MyApplication.getInstance()
													.getApplicationContext(),
											String.valueOf(mAppitem.sid),
											String.valueOf(mAppitem.vcode));
		
								DownloadTaskManager.getInstance()
										.deleteDownloadTask(downloadTask, true,
												false);
								new Thread(new Runnable() {
									@Override
									public void run() {
										DownloadTaskManager.getInstance()
												.deleteDownlaodFile(
														(DownloadItem) mAppitem);
									}
								}).start();
								doNotifyChange();
						
							break;
						}
					}

				}
			});

			layoutHolder.setas.setText(myArrList.get(myposition));
					return convertView;

		}
		public class RecordLayoutHolder {
			public Button setas;

		}
	}
	 public class DownTaskItem {
		 public ImageView listitem_rec_icon;
		 public TextView tv_name ;
		 public TextView listitem_rec_category;
		 public Button listitem_rec_btn;
		 public ProgressBar homedownloadProgressBar ;
		 public TextView tv_download_summary ;
		 public TextView statesDesc ;
		 public TextView tv_catrgory2_right;
		}
	/*
	 * add end
	 * */

}
