package com.byt.market.ui;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.market.OnAppClickListener;
import com.byt.market.R;
import com.byt.market.activity.MenuBaseActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.bitmaputil.core.display.RoundedBitmapDisplayer;
import com.byt.market.bitmaputil.core.display.SimpleBitmapDisplayer;
import com.byt.market.data.BigItem;
import com.byt.market.data.PageInfo;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.net.NetworkUtil;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LogUtil;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.CusPullListView.OnRefreshListener;
import com.byt.market.view.LoadFailedView;
import com.byt.market.view.MyListView;

public abstract class CommListViewFragment extends BaseUIFragment implements
		OnScrollListener, TaskListener, OnClickListener, DownloadTaskListener,
		OnAppClickListener {

	protected String tag() {
		return getClass().getSimpleName();
	}

	private ProtocolTask mTask;
	protected ImageAdapter mAdapter;
	protected boolean isRequesting;
	protected boolean isLoading = true;
	protected float rating;
	protected View loading;
	protected LoadFailedView loadfailed;
	protected MyListView listview;
	public TextView listview_loadfailed_text;
	private ImageView listview_loadfailed_icon;
	public MarketContext maContext;
	public DisplayMetrics outMetrics = new DisplayMetrics();
	public boolean isonepagere=false;
	/**
	 * 返回数据请求地址
	 * 
	 * @return
	 */
	protected abstract String getRequestUrl();

	/**
	 * 返回数据请求内容
	 * 
	 * @return
	 */
	protected abstract String getRequestContent();

	/**
	 * 解析服务器返回数据列表
	 * 
	 * @param result
	 * @return
	 */
	protected abstract List<BigItem> parseListData(JSONObject result);

	/**
	 * 返回页面布局id（布局中必须include listview.xml布局）
	 * 
	 * @return
	 */
	protected abstract int getLayoutResId();

	protected abstract View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState, int res);

	protected abstract ImageAdapter createAdapter();

	/**
	 * 下载状态有变化时调用
	 * */
	protected abstract void onDownloadStateChanged();

	@Override
	public void unInstalledSucess(String packageName) {

	}

	public PageInfo getPageInfo() {
		if (mAdapter == null) {
			return null;
		}
		return mAdapter.getPageInfo();
	}

	public ImageAdapter getAdapter() {
		return mAdapter;
	}

	public ListView getListView() {
		return listview;
	}

	@Override
	public void networkIsOk() {
		if (mAdapter.isEmpty() && NetworkUtil.isNetWorking(MyApplication.getInstance())) {
			retry();
		}
	}

	/**
	 * 请求数据
	 * 
	 * @param url
	 * @param content
	 */
	public void requestData() {
		if (isRequesting) {
			return;
		}
		isRequesting = true;
		isLoading = true;
		if (mAdapter.isEmpty()) {
			if (loadfailed == null
					|| loadfailed.getVisibility() == View.VISIBLE
					|| loading.getVisibility() == View.VISIBLE) {
				isRequesting=false;
				return;
			}
			loading.setVisibility(View.VISIBLE);
			loadfailed.setVisibility(View.GONE);
		} else {
			if (mAdapter.getLastItemType() == BigItem.Type.LAYOUT_LOADFAILED) {
				return;
			} else {
				mAdapter.addLast(BigItem.Type.LAYOUT_LOADING);
			}
		}
		mTask = new ProtocolTask(MyApplication.getInstance());
		mTask.setListener(this);
		mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
	}

	@Override
	public HashMap<String, String> getHeader() {
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		if (imei == null)
			imei = Util.getIMEI(MyApplication.getInstance());
		if (vcode == null)
			vcode = Util.getVcode(MyApplication.getInstance());
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(MyApplication.getInstance());
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("channel", channel);
		return map;
	}

	public CommListViewFragment() {
		if (mAdapter == null) {
			mAdapter = createAdapter();
		}
	}
	
	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		maContext = MarketContext.getInstance();
		initImageLoader();
		WindowManager wm = (WindowManager) MyApplication.getInstance().getSystemService(
				Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(outMetrics);
		DownloadTaskManager.getInstance().addListener(this);

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		// getDimenByResId(R.dimen.listitem_app_icon)
	}

	protected void initImageLoader() {
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
				.cacheOnDisc().delayBeforeLoading(200)
				.displayer(new SimpleBitmapDisplayer()).build();
	}

	public int getDimenByResId(int resId) {
		return getResources().getDimensionPixelSize(resId);
	}
	public void initViewBYT(View view) {
		

	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = onCreateView(inflater, container, savedInstanceState,
				getLayoutResId());
	
		loading = view.findViewById(R.id.listview_loading);
		loadfailed = (LoadFailedView) view
				.findViewById(R.id.listview_loadfailed);
		listview_loadfailed_text = (TextView) view
				.findViewById(R.id.listview_loadfailed_text);
		listview_loadfailed_icon = (ImageView) view
				.findViewById(R.id.listview_loadfailed_icon);
		listview = (MyListView) view.findViewById(R.id.listview);
		loading.setVisibility(View.GONE);
		loadfailed.setVisibility(View.GONE);
		initViewBYT(view);
		Log.d("myzx", "onCreateView+ListVIew+loading="+loading);
		// listview.setVisibility(View.GONE);
//		setStyle(listview);
		
		// SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new
		// SwingBottomInAnimationAdapter(mAdapter);
		// swingBottomInAnimationAdapter.setListView(listview);
		// listview.setAdapter(swingBottomInAnimationAdapter);
		listview.setOnScrollListener(this);
		return view;
	}

	public void setStyle(CusPullListView listview2) {

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		loadfailed.setOnClickListener(this);
	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0)
				request();
		};
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (!MenuBaseActivity.isMainFrame)
			request();
	}

	@Override
	public void onStart() {
		super.onStart();
		mAdapter.loadIcon(listview);
	}

	public void requestDelay() {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.sendEmptyMessage(0);
				if (mAdapter != null && !mAdapter.isEmpty()) {
					tryNotifyChange();
				}
			}
		}.start();
	}

	public void request() {
		if (mAdapter.isEmpty()) {
			requestData();
		} else {
			loading.setVisibility(View.GONE);
			loadfailed.setVisibility(View.GONE);
			if (listview.getAdapter() == null) {
				listview.setAdapter(mAdapter);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// mImageFetcher.setExitTasksEarly(false);
		LogUtil.i("appupdate", "touch lf onResume");
		if (!mAdapter.isEmpty()) {
			mAdapter.notifyDataSetChanged();
			mAdapter.loadIcon(listview);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		// mImageFetcher.setExitTasksEarly(true);

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d("myzx", "ListView onDestroyViewa()");
		if (mTask != null) {
			isRequesting = false;
			isLoading = false;
			mTask.onCancelled();
			loading.setVisibility(View.GONE);
			if (mAdapter.getLastItemType() == BigItem.Type.LAYOUT_LOADING) {
				mAdapter.remove(mAdapter.getCount() - 1);
			}
		}
		mAdapter.clearListIconAll(listview);
	}

	@Override
	public void onDestroy() {
		Log.d("myzx", "ListView onDestroy()");
		super.onDestroy();
		try {
			DownloadTaskManager.getInstance().removeListener(this);
			if (imageLoader != null) {
				imageLoader.clearMemoryCache();
			}
			if(listview!=null){
				listview.setAdapter(null);
			}
			if(mAdapter!=null){
				mAdapter.clear();
			}
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollState = scrollState;
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			// add by wangxin
			// mImageFetcher.setPauseWork(false);
			mAdapter.loadIcon(view);
			mAdapter.clearListIcon(view);
			if (mNeedNotifyChanged) {
				onDownloadStateChanged();
				mNeedNotifyChanged = false;
			}
			addNewDataOnce();
			break;
		default:
			// add by wangxin
			// mImageFetcher.setPauseWork(true);
			mAdapter.cancelLoadIcon();
			break;
		}
	}

	public void addNewDataOnce() {

	}

	// 判断是否加载到底部，并提示已经到底的提示
	boolean isLoadBottom = true;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		boolean isBottom = firstVisibleItem + visibleItemCount == totalItemCount
				&& totalItemCount != 0 && totalItemCount > visibleItemCount;
		if (isBottom && !isRequesting) {
			LogUtil.e("cexo", this + "onScroll isBottom");
			if (mAdapter.getPageInfo().getPageIndex() >= mAdapter.getPageInfo()
					.getPageNum()) {
				// if(isBottom){
				// }
				if (isLoadBottom
						&& visibleItemCount + firstVisibleItem == totalItemCount) {
					isLoadBottom = false;
					showShortToast(MyApplication.getInstance().getString(R.string.toast_load_more_no_result));
				}
				return;
			}
			requestData();
		}
	}

	@Override
	public void onNoNetworking() {
		isRequesting = false;
		isLoading = false;
		setLoadfailedView();
		System.gc();
	}

	@Override
	public void onNetworkingError() {
		isRequesting = false;
		isLoading = false;
		setLoadfailedView();
		System.gc();
	}
	@Override
	public void onPostExecute(final byte[] bytes) {
		isRequesting = false;
		try {
			if (bytes != null) {
				JSONObject result = new JSONObject(new String(bytes));
				int status = JsonParse.parseResultStatus(result);
				if (status == 1) {
					PageInfo pageInfo = JsonParse.parsePageInfo(result);
					mAdapter.setPageInfo(pageInfo);
					if (!result.isNull("rating")) {
						rating = Float.valueOf(result.getString("rating"));
					}
					List<BigItem> appendList = parseListData(result);
					if (appendList != null && !appendList.isEmpty()) {
						onPost(appendList);
						if (listview.getAdapter() == null) {
							listview.setAdapter(mAdapter);
						}
						if (mAdapter.isEmpty()) {
							mAdapter.clear();
							mAdapter.add(appendList);
						} else {
							/*modify by zengxiao for:修改网络不好出现重复的item*/
							if(isonepagere||(pageInfo.getPageIndex()==1))
							{
								mAdapter.clear();
								mAdapter.add(appendList);
								isonepagere=false;
							}
							else{
							mAdapter.remove(mAdapter.getCount() - 1);
							mAdapter.add(appendList);
							}
							/*modify end*/
						}
						mAdapter.notifyDataSetChanged();
						loading.setVisibility(View.GONE);
						loadfailed.setVisibility(View.GONE);
					} else {
						showNoResultView();
					}
				} else {
					setLoadfailedView();
				}
			} else {
				setLoadfailedView();
			}
		} catch (Exception e) {
			//e.printStackTrace();
			setLoadfailedView();
		}
		mAdapter.loadIcon(listview);
		System.gc();
		isLoading = false;
	}

	public void showNoResultView() {
		if (mAdapter.isEmpty()) {
			loading.setVisibility(View.GONE);
			loadfailed.setVisibility(View.VISIBLE);
			setButtonInvi();
		} else {
			loading.setVisibility(View.GONE);
			loadfailed.setVisibility(View.GONE);
			mAdapter.getItem(mAdapter.getCount() - 1).layoutType = BigItem.Type.LAYOUT_LOADFAILED;
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 数据加载失败展示界面
	 */
	protected void setLoadfailedView() {
		if (mAdapter.isEmpty()) {
			loading.setVisibility(View.GONE);
			loadfailed.setVisibility(View.VISIBLE);
			setButtonInvi();
			if (!NetworkUtil.isNetWorking(MyApplication.getInstance())) {
				loadfailed.setImageIcon(R.drawable.net);
				loadfailed.setText(MyApplication.getInstance().getString(R.string.setnet));
				loadfailed.setButtonVisible(View.GONE);
			}
		} else {
			loading.setVisibility(View.GONE);
			loadfailed.setVisibility(View.GONE);
			mAdapter.getItem(mAdapter.getCount() - 1).layoutType = BigItem.Type.LAYOUT_LOADFAILED;
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 如对ListView 有特殊处理需求，请重写此方法 ，比如添加Header头
	 * 
	 * @param appendList
	 */
	public abstract void onPost(List<BigItem> appendList);

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.listview_loadfailed:
			if (!NetworkUtil.isNetWorking(MyApplication.getInstance())) {
				NetworkUtil.startNetSetting(MyApplication.getInstance());
			} else
				retry();
			break;
		}
	}

	protected void retry() {
		try{
		mTask = new ProtocolTask(MyApplication.getInstance());
		mTask.setListener(this);
		if (mAdapter.isEmpty()&&loading!=null&&loadfailed!=null ) {
			loading.setVisibility(View.VISIBLE);
			loadfailed.setVisibility(View.GONE);
		} else {
			/*modify by zengxiao for:修改网络不好出现重复的item*/
			if(getPageInfo().getPageNum()==1)
			{
			LogUtil.e("cexo", this + "retry BigItem.Type.LAYOUT_LOADING");
			isonepagere=true;
			}
			/*modify end*/
			if(mAdapter!=null)
			{
			mAdapter.getItem(mAdapter.getCount() - 1).layoutType = BigItem.Type.LAYOUT_LOADING;
			mAdapter.notifyDataSetChanged();
			}
		}
		mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
		}catch(Exception e){}
	}

	@Override
	public void downloadEnded(DownloadTask task, int totalTask, int progressTask) {
		doNotifyChange();
	}

	@Override
	public void downloadProgress(DownloadTask task, int totalTask,
			int progressTask, long progressSize, long totalSize) {
		tryNotifyChange();
	}

	@Override
	public void downloadStarted(DownloadTask task, int totalTask,
			int progressTask, long totalSize) {
		doNotifyChange();
	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {
		doNotifyChange();
	}

	@Override
	public void endConnecting(DownloadTask task, int totalTask,
			int progressTask, DownloadException result) {
	}

	@Override
	public void startConnecting(DownloadTask task, int totalTask,
			int progressTask) {
	}

	@Override
	public void refreshUI() {
		tryNotifyChange();
	}

	private void tryNotifyChange() {
		if (!mHandler.hasMessages(MSG_NOTIFY_DATA_CHANGED)) {
			mHandler.sendEmptyMessageDelayed(MSG_NOTIFY_DATA_CHANGED,
					NOTIFY_DELAY);
		}
	}

	private void doNotifyChange() {
		if (mHandler.hasMessages(MSG_NOTIFY_DATA_CHANGED)) {
			mHandler.removeMessages(MSG_NOTIFY_DATA_CHANGED);
			mHandler.sendEmptyMessage(MSG_NOTIFY_DATA_CHANGED);
		}
	}

	private static final long NOTIFY_DELAY = 1000;
	boolean mNeedNotifyChanged = false;
	private static final int MSG_NOTIFY_DATA_CHANGED = 1;
	int mScrollState;
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_NOTIFY_DATA_CHANGED: {
				if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					onDownloadStateChanged();
					mNeedNotifyChanged = false;
				} else {
					mNeedNotifyChanged = true;
				}

				break;
			}
			}
		}
	};

	public void onAppClick(Object obj, int what, String action) {

	};

	@Override
	public void installedSucess(DownloadTask downloadTask) {

	}

	@Override
	public void installedSucess(String packageName) {
		LogUtil.e("cexo", "installedSucess:" + packageName);
	}
	/*add for 修改提示语*/
	public void setButtonInvi(){
		
	}
}
