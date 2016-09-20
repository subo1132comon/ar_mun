package com.byt.market.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.Faviorate.MyFaviorateListener;
import com.byt.market.Faviorate.MyFaviorateManager;
import com.byt.market.activity.DownLoadManageActivity.DownListAdapter;
import com.byt.market.activity.DownLoadManageActivity.DownListAdapter.RecordLayoutHolder;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.adapter.MyListAdapter;
import com.byt.market.data.AppItem;
import com.byt.market.data.DownloadItem;
import com.byt.market.data.UserData;
import com.byt.market.data.ViewHolder;
import com.byt.market.download.DownloadBtnClickListener;
import com.byt.market.download.DownloadContent;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.log.LogModel;
import com.byt.market.ui.HomeFragment;
import com.byt.market.ui.ManagerFragment;
import com.byt.market.util.DataUtil;
import com.byt.market.util.DownloadUIUtil;
import com.byt.market.util.LogUtil;
import com.byt.market.util.StringUtil;
import com.byt.market.util.SystemUtil;
import com.byt.market.util.Util;
import com.byt.market.view.LoadFailedView;

/**
 * 我的收藏
 */
public class MyFaviorateActivity extends BaseActivity implements
		DownloadTaskListener, MyFaviorateListener {
	private String COLECCTTEXT="colecctnew";
	private static final int MSG_NOTIFY_DATA_CHANGED = 1;
	private static final int MSG_NOTIFY_DATA_EMPTY = 2;
	/* add by zengxiao */
	LayoutInflater minflater;
	PopupWindow moptionmenu;
	protected int[] optiontextlist = { R.string.expend_child1_text,
			R.string.state_idle_text, R.string.dialog_nowifi_btn_fav };
	/* add end */
	// views
	private TextView tv_topbar_title;
	private ImageView iv_menu_settings;
	private ImageView btn_menu_search;
	private ListView lv_faviorate_list;
	private Context context;
	private LoadFailedView noContentLayout;
	private ImageView iv_titlebar_back_arrow;

	private List<AppItem> myFaviorateList = new ArrayList<AppItem>();
	private MyFaviorateAdapter adapter;
	private PopupWindow popupWindow;
	private AppItem mAppitem;
	private AppItem mShowAppItem;

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NOTIFY_DATA_CHANGED:
				stateChanged();
				break;
			case MSG_NOTIFY_DATA_EMPTY:
				tv_topbar_title
						.setText(getString(R.string.text_my_faviorate_title)
								+ "(0)");
				lv_faviorate_list.setVisibility(View.GONE);
				noContentLayout.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listview_common);
		context = this;
		initView();
		initData();
		addEvent();
		loadData();
	}

	private void stateChanged() {
		lv_faviorate_list.setVisibility(View.VISIBLE);
		noContentLayout.setVisibility(View.GONE);
		tv_topbar_title.setText(getString(R.string.text_my_faviorate_title)
				+ "(" + myFaviorateList.size() + ")");
		DownloadTaskManager.getInstance().fillAppStates(myFaviorateList);
		adapter.notifyDataSetChanged();
		
		MyApplication mMyApplication = (MyApplication)getApplication();
		//清零，只显示最近收藏的总数，一旦进入我的收藏页面后清零重新计数
		mMyApplication.setCollectNum(0);
	}

	@Override
	public void initView() {
		initTtileBar();
		noContentLayout = (LoadFailedView) findViewById(R.id.rl_nocontent);
		noContentLayout.setVisibility(View.GONE);
		noContentLayout.setText(getString(R.string.listview_loadding3));
		lv_faviorate_list = (ListView) findViewById(R.id.lv_list);
	}

	@Override
	public void initData() {
		adapter = new MyFaviorateAdapter();
		lv_faviorate_list.setPadding(0, 3, 0, 0);
		lv_faviorate_list.setAdapter(adapter);
		

	}

	@Override
	public void addEvent() {
		MyFaviorateManager.getInstance().addListener(this);
		iv_titlebar_back_arrow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	@Override
	public void onResume() {
		Util.delAllMyData(maContext);
		SharedPreferences newFavor=getSharedPreferences(COLECCTTEXT,0);
		SharedPreferences.Editor edite=newFavor.edit();	
		edite.putInt("cout",0);
		edite.commit();	
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			MyFaviorateManager.getInstance().removeListener(this);
			DownloadTaskManager.getInstance().removeListener(this);
			lv_faviorate_list.setAdapter(null);
			myFaviorateList.clear();
			myFaviorateList=null;
			adapter=null;
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadData() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				myFaviorateList = DataUtil.getInstance(context)
						.getFaviorateList();
				Log.i("0424",
						"myFaviorateList size = " + myFaviorateList.size());
				checkListEmpty();
				DownloadTaskManager.getInstance().addListener(
						MyFaviorateActivity.this);
			}
		}).start();
	}

	private void checkListEmpty() {
		if (myFaviorateList.size() > 0) {
			handler.sendEmptyMessageDelayed(MSG_NOTIFY_DATA_CHANGED, 1000);
		} else {
			handler.sendEmptyMessage(MSG_NOTIFY_DATA_EMPTY);
		}
	}

	private void initTtileBar() {
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		findViewById(R.id.feedback_back_btn).setVisibility(View.GONE);
		tv_topbar_title = (TextView) findViewById(R.id.titlebar_title);
		iv_menu_settings = (ImageView) findViewById(R.id.iv_settings);
		tv_topbar_title.setText(R.string.text_my_faviorate_title);
		btn_menu_search = (ImageView) findViewById(R.id.titlebar_search_button);
		btn_menu_search.setVisibility(View.INVISIBLE);
		iv_menu_settings.setVisibility(View.GONE);
		iv_titlebar_back_arrow = (ImageView) findViewById(R.id.titlebar_back_arrow);
		iv_titlebar_back_arrow.setVisibility(View.VISIBLE);
		ImageView iv_titlebar_icon = (ImageView) findViewById(R.id.titlebar_icon);
		iv_titlebar_icon.setVisibility(View.GONE);
	}

	/**
	 * item更多选项(查看详情，取消下载等等)
	 */
	private void showItemMoreOptions(View view, final AppItem appItem,
			int postion) {
		LogUtil.i("0426", "postion = " + postion);
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(
				R.layout.item_page_more, null, true);

		popupWindow = new PopupWindow(menuView,
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		if (postion == myFaviorateList.size() - 1 && postion > 1) {
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
		popupWindow.update();
		Button btn_left = (Button) menuView
				.findViewById(R.id.btn_option_item_left);
		Button btn_right = (Button) menuView
				.findViewById(R.id.btn_option_item_right);
		btn_left.setText(R.string.text_look_over_detail_desc);
		btn_right.setText(R.string.expend_child5_del_fav);
		btn_left.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DownloadUIUtil.toAppDetail(appItem, MyFaviorateActivity.this);
				popupWindow.dismiss();
			}
		});
		// 取消收藏
		btn_right.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
						Util.delData(maContext, appItem.sid);
						myFaviorateList.remove(appItem);
						popupWindow.dismiss();
				
			}
		});

	}

	@Override
	public void startConnecting(DownloadContent.DownloadTask task,
			int totalTask, int progressTask) {

	}

	@Override
	public void downloadStarted(DownloadContent.DownloadTask task,
			int totalTask, int progressTask, long totalSize) {
		LogUtil.i("0424", "touch downloadStarted");
		checkListEmpty();
	}

	@Override
	public void downloadProgress(DownloadContent.DownloadTask task,
			int totalTask, int progressTask, long progressSize, long totalSize) {

	}

	@Override
	public void downloadEnded(DownloadContent.DownloadTask task, int totalTask,
			int progressTask) {

	}

	@Override
	public void endConnecting(DownloadContent.DownloadTask task, int totalTask,
			int progressTask, DownloadException result) {

	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {

	}

	@Override
	public void refreshUI() {
		LogUtil.i("0424", "touch myf refreshUI");
		checkListEmpty();
	}

	@Override
	public void installedSucess(DownloadContent.DownloadTask downloadTask) {

	}

	@Override
	public void unInstalledSucess(String packageName) {

	}

	@Override
	public void networkIsOk() {

	}

	@Override
	public void FaviorateAppAdded(AppItem appItem) {
		LogUtil.i("0424", "touch FaviorateAppAdded = " + appItem.name);
		myFaviorateList.add(appItem);
		checkListEmpty();
	}

	@Override
	public void FaviorateAppDeled(AppItem appItem) {
		removeMyFav(appItem.sid);
		LogUtil.i("0424", "touch FaviorateAppDeled = " + appItem.name);
		checkListEmpty();
	}

	private void removeMyFav(int sid) {
		AppItem removeApp = null;
		for (AppItem appItem : myFaviorateList) {
			if (appItem.sid == sid) {
				removeApp = appItem;
				break;
			}
		}
		if (removeApp != null) {
			myFaviorateList.remove(removeApp);
		}
	}

	public  class MyFaviorateHolder extends ViewHolder {
		public ImageView appIcon;
		public TextView name;
		public TextView downum;
		public RatingBar rating;

		public TextView size;
		public TextView downBtn;
		public TextView sdesc; // 描述 or 小编点评
		public LinearLayout descLayout;
		public LinearLayout rootView;
		public RelativeLayout itemLayout;
		public int position;
		public RelativeLayout item_choose_layout;
		public LinearLayout lay_look_over_detail_desc;
		public LinearLayout lay_cancel_collect;
		public TextView appprice;//应用价格
		public TextView freestype;//限时免费
		public TextView appvip;//应用所需等级
		public FrameLayout pricelayout;//价格布局
		//public TextView bt_free_btn;//价格布局
	//	public FrameLayout googlepriceFram;//google价格布局
		public ProgressBar DownloadProgressBar;//下载进度条
		public TextView progressnumtext;//下载进度
		public ProgressBar DownloadProgressBar2;//分享下载进度条
		public TextView progressnumtext2;//分享下载进度
		public TextView downBtn2;//分享下载按钮
		public RelativeLayout sharelayout;//分享按钮icon
		public TextView item_up_bg;
		//public TextView googlefreebg;
		//public ImageView googlepriceline;
		//public ImageView googleicon;
		public TextView sizedivider;
	}

	private final class MyFaviorateAdapter extends BaseAdapter {
		MyFaviorateHolder holder;

		@Override
		public int getCount() {
			return myFaviorateList.size();
		}

		@Override
		public Object getItem(int position) {
			return myFaviorateList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			
			final AppItem appItem = myFaviorateList.get(position);
			if (convertView == null) {
				holder = new MyFaviorateHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.listitem_common_layout_myfa, null);

				holder.appIcon = (ImageView) convertView
						.findViewById(R.id.img_icon_View);
				holder.name = (TextView) convertView
						.findViewById(R.id.tv_name_lable);
				holder.rating = (RatingBar) convertView
						.findViewById(R.id.rb_rating_view);
				holder.downum = (TextView) convertView
						.findViewById(R.id.tv_downum_view);
				holder.size = (TextView) convertView
						.findViewById(R.id.tv_size_view);
				holder.size.setVisibility(View.VISIBLE);
				holder.sdesc = (TextView) convertView
						.findViewById(R.id.tv_desc_view);
				holder.downBtn = (TextView) convertView
						.findViewById(R.id.bt_down_btn);
				holder.descLayout = (LinearLayout) convertView
						.findViewById(R.id.txt_desc_layout);
				holder.itemLayout = (RelativeLayout) convertView
						.findViewById(R.id.more_item_layout);
				/*holder.item_choose_layout = (RelativeLayout) convertView
						.findViewById(R.id.item_choose_layout);
				holder.lay_look_over_detail_desc = (LinearLayout) convertView
						.findViewById(R.id.lay_look_over_detail_desc);
				holder.lay_cancel_collect = (LinearLayout) convertView
						.findViewById(R.id.lay_cancel_collect);
				holder.rootView = (LinearLayout) convertView;*/
				/*modify bu znegxiao for:修改item样式*/
				//holder.bt_free_btn=(TextView) convertView
				//		.findViewById(R.id.bt_free_btn);
				holder.item_up_bg=(TextView) convertView
						.findViewById(R.id.item_up_bg);
				//holder.googlefreebg=(TextView) convertView
				//		.findViewById(R.id.googlefreebg);
				//holder.googlepriceline=(ImageView) convertView
				//		.findViewById(R.id.googlepriceline);
				
				holder.DownloadProgressBar= (ProgressBar) convertView
						.findViewById(R.id.DownloadProgressBar);
				holder.progressnumtext=(TextView) convertView
						.findViewById(R.id.progressnumtext);
				holder.downBtn2=(TextView) convertView.findViewById(R.id.bt_down_btn2);
				//holder.googleicon=(ImageView) convertView.findViewById(R.id.googleicon);
				holder.sizedivider=(TextView) convertView.findViewById(R.id.sizedivider);	
				/*modify end*/
				
				convertView.setTag(holder);
			} else {
				holder = (MyFaviorateHolder) convertView.getTag();
			}
			final View myview = convertView;
			/*add by zengxiao for:修改item样式*/
			/*if(appItem.googlePrice>0&&appItem.googlemarket>0)
			{
				holder.googlepriceline.setVisibility(View.VISIBLE);
				holder.googlefreebg.setVisibility(View.VISIBLE);
				holder.bt_free_btn.setVisibility(View.VISIBLE);
				holder.googleicon.setVisibility(View.VISIBLE);
				holder.bt_free_btn.setText("$"+
						appItem.googlePrice);																		
			}
			else{
				holder.googlepriceline.setVisibility(View.GONE);
				holder.googlefreebg.setVisibility(View.GONE);
				holder.bt_free_btn.setVisibility(View.GONE);
				holder.googleicon.setVisibility(View.GONE);
		
			}*/	
			/*add end*/
		/*	holder.lay_look_over_detail_desc.setTag(holder);
			holder.lay_look_over_detail_desc
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							MyFaviorateHolder myViewHolder = (MyFaviorateHolder) view
									.getTag();
							AppItem appItem = myFaviorateList
									.get(myViewHolder.position);
							DownloadUIUtil.toAppDetail(appItem,
									MyFaviorateActivity.this);
						}
					});
			holder.lay_cancel_collect.setTag(holder);
			holder.lay_cancel_collect.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View view) {
					
					new AlertDialog.Builder(MyFaviorateActivity.this)
					.setTitle(R.string.dialog_retry_title)  
					.setMessage(MyFaviorateActivity.this.getString(R.string.cancollet_dialogtext))  
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
					        MyApplication mMyApplication = (MyApplication)getApplication();				        
					        int count = mMyApplication.getCollectNum();		
					        mMyApplication.setCollectNum(--count);//
					        
							// TODO Auto-generated method stub
							Toast.makeText(MyApplication.getInstance(), R.string.expend_child5_del_fav, Toast.LENGTH_SHORT).show();
							MyFaviorateHolder myViewHolder = (MyFaviorateHolder) view
									.getTag();
							AppItem appItem = myFaviorateList
									.get(myViewHolder.position);
							Util.delData(maContext, appItem.sid);
							myFaviorateList.remove(appItem);
							checkListEmpty();
						}
					})  
					.setNegativeButton(R.string.cancel, null)  
					.show();  
					
				}
			});*/
			
			holder.position = position;
			holder.name.setText(appItem.name);
			holder.sizedivider.setText(" | ");
			holder.rating.setRating(appItem.rating);
			appItem.strLength = StringUtil.resultBitTranslate(appItem.length);
			holder.size.setText(appItem.strLength);
			holder.downum.setText(appItem.downNum);
			holder.descLayout.setVisibility(View.GONE);
			if (TextUtils.isEmpty(appItem.iconUrl)) {
				holder.appIcon.setBackgroundResource(R.drawable.app_empty_icon);
			} else {
				imageLoader.displayImage(appItem.iconUrl, holder.appIcon,
						options);
			}

			holder.itemLayout.setTag(holder);
			holder.itemLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					try{
					List<String> myList = new ArrayList<String>();
					myList.add(getString(R.string.text_look_over_detail_desc));
					ViewGroup optionViewGroup = (ViewGroup) getLayoutInflater()
							.inflate(
									R.layout.bestone_optionmenuho,
									null);
					ListView mylistview = (ListView) optionViewGroup
							.findViewById(R.id.optionmenulist);
					PopupWindow optionmenu = new PopupWindow(
							optionViewGroup,
							getResources()
							.getDimensionPixelSize(
									R.dimen.popupWindow_widthho),
							LayoutParams.WRAP_CONTENT);
					DownListAdapter optiondialog = new DownListAdapter(myList);
					mylistview.setAdapter(optiondialog);

					optionmenu.setFocusable(true);
					optionmenu
							.setBackgroundDrawable(new BitmapDrawable());
					optionmenu.setOutsideTouchable(true);	
					int[] location = new  int[2] ;
				    view.getLocationInWindow(location); //获取在当前窗口内的绝对坐标
				    
					optionmenu
							.showAtLocation(view, Gravity.TOP, location[0], location[1]+100);
					MyFaviorateHolder myViewHolder = (MyFaviorateHolder) view
							.getTag();
					mAppitem= myFaviorateList.get(myViewHolder.position);
					moptionmenu = optionmenu;
					}catch(Exception e){
						
					}
				/*	MyFaviorateHolder myViewHolder = (MyFaviorateHolder) view
							.getTag();
					AppItem appItem = myFaviorateList
							.get(myViewHolder.position);
					if (mShowAppItem != null && mShowAppItem == appItem) {
						if (appItem.isShowPopup) {
							appItem.isShowPopup = false;
							myViewHolder.item_choose_layout
									.setVisibility(View.GONE);
						} else {
							appItem.isShowPopup = true;
							myViewHolder.item_choose_layout
									.setVisibility(View.VISIBLE);
						}
					} else {
						appItem.isShowPopup = true;
						myViewHolder.item_choose_layout
								.setVisibility(View.VISIBLE);
					}
					MyFaviorateAdapter.this.notifyDataSetChanged();
					mShowAppItem = appItem;
					if (myViewHolder.position == getCount() - 1) {
						lv_faviorate_list.setSelection(position);
					}*/
				}
			});

			/*if (appItem.isShowPopup) {
				if (mShowAppItem != null && mShowAppItem == appItem) {
					holder.item_choose_layout.setVisibility(View.VISIBLE);
				} else if (mShowAppItem == null) {
					holder.item_choose_layout.setVisibility(View.VISIBLE);
				} else {
					holder.item_choose_layout.setVisibility(View.GONE);
				}
			} else {
				holder.item_choose_layout.setVisibility(View.GONE);
			}*/
			if (minflater == null) {
				minflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			}
			/* add by zengxiao for change UI */
				if(appItem.isshare==1){
					holder.downBtn.setVisibility(View.GONE);
					holder.downBtn2.setVisibility(View.VISIBLE);
					holder.downBtn2.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						
						if(holder.downBtn2.getText().toString().equals(MyFaviorateActivity.this.getString(R.string.sharedown)))
						{
							Intent intent =new Intent();
							intent.setClass(MyFaviorateActivity.this,ShareActivity.class);						
							intent.putExtra("sendstring", appItem); 
							MyFaviorateActivity.this.startActivity(intent);
						}
						else
						{
						DownloadTaskManager.getInstance().onDownloadBtnClick(appItem);
						}
						
					}
				});
					Drawable drawable=MyFaviorateActivity.this.getResources().getDrawable(R.drawable.progress_listitem);
					holder.DownloadProgressBar.setProgressDrawable(drawable);
					holder.item_up_bg.setBackgroundResource(R.drawable.bg_press_draw);
				
				DownloadTaskManager.getInstance().updateByDownLoadStateFav2(holder.downBtn2,
						appItem, holder.DownloadProgressBar, holder.progressnumtext, false, false, false, null,
						LogModel.L_HOME_REC, null, false, null,holder.sharelayout,null);
				
				
			}
			else
			{
				holder.downBtn.setVisibility(View.VISIBLE);
				holder.downBtn2.setVisibility(View.GONE);
			
			holder.downBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
				DownloadTaskManager.getInstance().onDownloadBtnClick(appItem);
					}
				});
				/* add by "zengxiao" */
				Drawable drawable=MyFaviorateActivity.this.getResources().getDrawable(R.drawable.progress_listitem);
				holder.DownloadProgressBar.setProgressDrawable(drawable);
				holder.item_up_bg.setBackgroundResource(R.drawable.bg_press_draw);
			
				DownloadTaskManager.getInstance().updateByDownLoadStateFav(holder.downBtn,
						appItem, holder.DownloadProgressBar, holder.progressnumtext, false, false, false, null,
						LogModel.L_HOME_REC, null, false, null);
				
			}
			
			
			return convertView;
		}
	}

	@Override
	public void installedSucess(String packageName) {
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
				convertView = LayoutInflater.from(MyFaviorateActivity.this).inflate(
						R.layout.bestone_choicebuttonho, null, false);
				layoutHolder.toDetail = (Button) convertView.findViewById(R.id.seedetail);
				layoutHolder.cancelCEL = (Button) convertView.findViewById(R.id.cancecel);
				convertView.setTag(layoutHolder);
			} else {
				layoutHolder = (RecordLayoutHolder) convertView.getTag();
			}
			mlayout = layoutHolder;
			layoutHolder.cancelCEL.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if (moptionmenu != null && moptionmenu.isShowing()) {
						moptionmenu.dismiss();
					}	
					// TODO Auto-generated method stub
					new AlertDialog.Builder(MyFaviorateActivity.this)
					.setTitle(R.string.dialog_retry_title)  
					.setMessage(MyFaviorateActivity.this.getString(R.string.cancollet_dialogtext))
					.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
					        /*MyApplication mMyApplication = (MyApplication)getApplication();				        
					        int count = mMyApplication.getCollectNum();		
					        mMyApplication.setCollectNum(--count);//*/
					        
							// TODO Auto-generated method stub
							Toast.makeText(MyApplication.getInstance(), R.string.expend_child5_del_fav, Toast.LENGTH_SHORT).show();
							Util.delData(maContext, mAppitem.sid);
							myFaviorateList.remove(mAppitem);
							checkListEmpty();
						}
					})  
					.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							arg0.dismiss();
							
						}
					})  					
					.show();  
				}
			});
			layoutHolder.toDetail.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (moptionmenu != null && moptionmenu.isShowing()) {
						moptionmenu.dismiss();
					}			
						DownloadUIUtil.toAppDetail(mAppitem,
								MyFaviorateActivity.this);
							
						
					}

				}
			);

			layoutHolder.toDetail.setText(getString(R.string.text_look_over_detail_desc));
			layoutHolder.cancelCEL.setText(getString(R.string.expend_child5_del_fav));
					return convertView;

		}
		public class RecordLayoutHolder {
			public Button toDetail;
			public Button cancelCEL;
		}
	}
	
	/*
	 * add end
	 * */
	
}