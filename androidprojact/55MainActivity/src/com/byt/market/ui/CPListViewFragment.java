package com.byt.market.ui;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.MarketContext;
import com.byt.market.OnAppClickListener;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
//import com.byt.market.bitmaputil.ImageFetcher;
//import com.byt.market.bitmaputil.ImageCache.ImageCacheParams;
import com.byt.market.data.BigItem;
import com.byt.market.data.PageInfo;
import com.byt.market.data.ResultInfo;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.net.NetworkUtil;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LogUtil;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.CusPullListView.OnRefreshListener;

public abstract class CPListViewFragment extends BaseUIFragment implements
		OnScrollListener, TaskListener, OnClickListener, DownloadTaskListener,
		OnAppClickListener {

	protected String tag() {
		return getClass().getSimpleName();
	}

	private ProtocolTask mTask;
	protected ImageAdapter mAdapter;
	protected boolean isRequesting;

	protected View loading;
	protected View loadfailed;
	protected CusPullListView listview;
	public TextView listview_loadfailed_text;
	private ImageView listview_loadfailed_icon;
	public MarketContext maContext;
	public boolean isRefresh;
	public int cid;
	
	//edit by wangxin
	/**
	 * add by wangxin
	 * use Bitmapfun lib
	 */
//	protected ImageFetcher mImageFetcher;
	private int mImageThumbSize;
    private int mImageThumbSpacing;
	

	/**
	 * 返回数据请求地址
	 * 
	 * @return
	 */
	protected abstract String getRequestUrl();

	/**
	 * 返回数据请求地址
	 * 
	 * @return
	 */
	protected abstract String getNewRequestUrl();

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
	 * 卸载成功后
	 */
	@Override
	public void unInstalledSucess(String packageName) {
		// TODO Auto-generated method stub
		
	}

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

	public PageInfo getPageInfo() {
		if (mAdapter == null) {
			return null;
		}
		return mAdapter.getPageInfo();
	}

	public ImageAdapter getAdapter() {
		return mAdapter;
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
		if (mAdapter.isEmpty()) {
			if (loadfailed.getVisibility() == View.VISIBLE) {
				return;
			}
			loading.setVisibility(View.VISIBLE);
			loadfailed.setVisibility(View.GONE);
			// listview.setVisibility(View.GONE);
		} else {
			if (mAdapter.getLastItemType() == BigItem.Type.LAYOUT_LOADFAILED) {
				return;
			} else {
				mAdapter.addLast(BigItem.Type.LAYOUT_LOADING);
			}
		}
		mTask = new ProtocolTask(getActivity());
		mTask.setListener(this);
		mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
	}

	/**
	 * 请求数据
	 * 
	 * @param url
	 * @param content
	 */
	public void requestNewCommData() {
		if (isRequesting) {
			return;
		}
		isRefresh = true;
		isRequesting = true;
		loadfailed.setVisibility(View.GONE);
		mTask = new ProtocolTask(getActivity());
		mTask.setListener(this);
		mTask.execute(getNewRequestUrl(), getRequestContent(), tag(),
				getHeader());
	}

	@Override
	public HashMap<String, String> getHeader() {
		String imei = Util.imie;
    	String vcode = Util.vcode;
    	String channel = Util.channel;
		if (imei == null)
			imei = Util.getIMEI(getActivity());
		if (vcode == null)
			vcode = Util.getVcode(getActivity());
		if (TextUtils.isEmpty(channel)){
			channel = Util.getChannelName(MyApplication.getInstance());
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("channel",channel);
		return map;
	}

	public void pullRefresh() {
		requestNewCommData();
	}

	public CPListViewFragment() {
		if (mAdapter == null) {
			mAdapter = createAdapter();
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		LogUtil.e("TAGFRAGMENT", "onAttach-------------------");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		maContext = MarketContext.getInstance();
//		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), Constants.FOLDER_ICONS);
//		cacheParams.compressFormat = CompressFormat.PNG;
//        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
//        mImageFetcher = new ImageFetcher(getActivity(), 48);
//        mImageFetcher.setLoadingImage(R.drawable.app_empty_icon);
//        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
		LogUtil.e("TAGFRAGMENT", "onCreate-------------------");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LogUtil.e("TAGFRAGMENT", "onCreateView-------------------");
		View view = onCreateView(inflater, container, savedInstanceState,
				getLayoutResId());
		loading = view.findViewById(R.id.listview_loading);
		loadfailed = view.findViewById(R.id.listview_loadfailed);
		listview_loadfailed_text = (TextView) view
				.findViewById(R.id.listview_loadfailed_text);
		listview_loadfailed_icon = (ImageView) view
				.findViewById(R.id.listview_loadfailed_icon);
		listview = (CusPullListView) view.findViewById(R.id.listview);
		loading.setVisibility(View.GONE);
		loadfailed.setVisibility(View.GONE);
		// listview.setVisibility(View.GONE);
		setStyle(listview);
		// SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new
		// SwingBottomInAnimationAdapter(mAdapter);
		// swingBottomInAnimationAdapter.setListView(listview);
		// listview.setAdapter(swingBottomInAnimationAdapter);
		listview.setAdapter(mAdapter);
		listview.setOnScrollListener(this);
		listview.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				requestNewCommData();
			}
		});
		return view;
	}

	public void setStyle(CusPullListView listview2) {

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		LogUtil.e("TAGFRAGMENT", "onViewCreated-------------------");
		loadfailed.setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		LogUtil.e("TAGFRAGMENT", "onStart-------------------");
		request();
	}

	public void request() {
		if (mAdapter.isEmpty() && !isRefresh) {
			requestData();
		} else {
			loading.setVisibility(View.GONE);
			loadfailed.setVisibility(View.GONE);
			// listview.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		LogUtil.e("TAGFRAGMENT", "onStop-------------------");
		if (mTask != null) {
			isRequesting = false;
			mTask.onCancelled();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
//		mImageFetcher.setExitTasksEarly(false);
		LogUtil.e("TAGFRAGMENT", "onResume-------------------");
		DownloadTaskManager.getInstance().addListener(this);
		if (!mAdapter.isEmpty()) {
			mAdapter.notifyDataSetChanged();
			mAdapter.loadIcon(listview);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
//		mImageFetcher.setExitTasksEarly(true);
		LogUtil.e("TAGFRAGMENT", "onPause-------------------");
		DownloadTaskManager.getInstance().removeListener(this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
//		mImageFetcher.closeCache();
		LogUtil.e("TAGFRAGMENT", "onDestroyView-------------------");
		mAdapter.clearListIcon();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogUtil.e("TAGFRAGMENT", "onDestroy-------------------");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		LogUtil.e("TAGFRAGMENT", "onDetach-------------------");
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollState = scrollState;
		LogUtil.e("TAGFRAGMENT", "onScrollStateChanged-------------------"
				+ scrollState);
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			// add by wangxin
//			 mImageFetcher.setPauseWork(false);
			mAdapter.loadIcon(view);
			mAdapter.clearListIcon(view);
			if (mNeedNotifyChanged) {
				onDownloadStateChanged();
				mNeedNotifyChanged = false;
			}
			addNewDataOnce();
			break;
		default:
//			mImageFetcher.setPauseWork(true);
			mAdapter.cancelLoadIcon();
			break;
		}
	}

	public void addNewDataOnce() {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		listview.setFirstIndex(firstVisibleItem);
		boolean isBottom = firstVisibleItem + visibleItemCount == totalItemCount
				&& totalItemCount != 0 && totalItemCount > visibleItemCount;
		if (isBottom && !isRequesting) {
			if (mAdapter.getPageInfo().getPageIndex() == mAdapter.getPageInfo()
					.getPageNum()) {
				return;
			}
			requestData();
		}
	}

	@Override
	public void onNoNetworking() {
		// TODO 没有网络连接，提示打开网络？
		isRequesting = false;
		setLoadfailedView();
		System.gc();
	}

	@Override
	public void onNetworkingError() {
		isRequesting = false;
		setLoadfailedView();
		System.gc();
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		isRequesting = false;
		listview.onRefreshComplete();
		try {
			if (bytes != null) {
				JSONObject result = new JSONObject(new String(bytes));
				int status = JsonParse.parseResultStatus(result);
				if (status == 1) {
					PageInfo pageInfo = JsonParse.parsePageInfo(result);
					mAdapter.setPageInfo(pageInfo);
					List<BigItem> appendList = parseListData(result);
					if (isRefresh) {
						if (appendList != null && !appendList.isEmpty()) {
							cid = appendList.get(0).comments.get(0).sid;
							if (mAdapter.isEmpty()) {
								loading.setVisibility(View.GONE);
								loadfailed.setVisibility(View.GONE);
							}
							mAdapter.addFirst(appendList);
						} else if (!mAdapter.isEmpty()) {
							Toast.makeText(getActivity(), getActivity().getString(R.string.nocomment), 100).show();
						}
						isRefresh = false;
					} else {
						if (appendList != null && !appendList.isEmpty()) {
							if (pageInfo.getPageIndex() == 1) {
								cid = appendList.get(0).comments.get(0).sid;
							}
							if (mAdapter.isEmpty()) {
								loading.setVisibility(View.GONE);
								loadfailed.setVisibility(View.GONE);
								// listview.setVisibility(View.VISIBLE);
								mAdapter.add(appendList);
							} else {
								mAdapter.remove(mAdapter.getCount() - 1);
								mAdapter.add(appendList);
							}

						} else {
							showNoResultView();
						}
					}
				} else {
					setLoadfailedView();
				}
			} else {
				setLoadfailedView();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mAdapter.loadIcon(listview);
		System.gc();
	}

	public void showNoResultView() {
		if (mAdapter.isEmpty()) {
			loading.setVisibility(View.GONE);
			loadfailed.setVisibility(View.VISIBLE);
			listview_loadfailed_text.setText(getActivity().getString(R.string.nocomment_send));
		} else {
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
			// listview.setVisibility(View.GONE);
			if (!NetworkUtil.isNetWorking(getActivity())) {
				listview_loadfailed_icon.setImageResource(R.drawable.net);
				listview_loadfailed_text.setText(getActivity().getString(R.string.setnet));
			}
		} else {
			mAdapter.getItem(mAdapter.getCount() - 1).layoutType = BigItem.Type.LAYOUT_LOADFAILED;
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.listview_loadfailed:
//			retry();
			break;
		}
	}

	protected void retry() {
		mTask = new ProtocolTask(getActivity());
		mTask.setListener(this);
		if (mAdapter.isEmpty()) {
			if (loadfailed.getVisibility() == View.VISIBLE) {
				loading.setVisibility(View.VISIBLE);
				loadfailed.setVisibility(View.GONE);
				// listview.setVisibility(View.GONE);
			}
		} else {
			mAdapter.getItem(mAdapter.getCount() - 1).layoutType = BigItem.Type.LAYOUT_LOADING;
			mAdapter.notifyDataSetChanged();
		}
		mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
	}

	@Override
	public void downloadEnded(DownloadTask task, int totalTask, int progressTask) {
		mHandler.sendEmptyMessage(MSG_NOTIFY_DATA_CHANGED);
	}

	@Override
	public void downloadProgress(DownloadTask task, int totalTask,
			int progressTask, long progressSize, long totalSize) {
		mHandler.sendEmptyMessage(MSG_NOTIFY_DATA_CHANGED);
	}

	@Override
	public void downloadStarted(DownloadTask task, int totalTask,
			int progressTask, long totalSize) {
		mHandler.sendEmptyMessage(MSG_NOTIFY_DATA_CHANGED);
	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {
		mHandler.sendEmptyMessage(MSG_NOTIFY_DATA_CHANGED);
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
		mHandler.sendEmptyMessage(MSG_NOTIFY_DATA_CHANGED);
	}

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
}
