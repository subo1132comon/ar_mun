package com.byt.market.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.market.OnAppClickListener;
import com.byt.market.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.MenuBaseActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.asynctask.FileCacehTask;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.data.AppItem;
import com.byt.market.data.BigItem;
import com.byt.market.data.PageInfo;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.net.NetworkUtil;
import com.byt.market.tools.LogCart;
import com.byt.market.tools.MD5Tools;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LogUtil;
import com.byt.market.util.Util;
import com.byt.market.util.filecache.FileCacheUtil;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.CusPullListView.OnRefreshListener;
import com.byt.market.view.CusPullListView.ScrollRefreshListener;
import com.byt.market.view.LoadFailedView;

public abstract class ListViewFragment extends BaseUIFragment implements
		OnScrollListener, TaskListener, OnClickListener, DownloadTaskListener,
		OnAppClickListener {

	protected String tag() {
		return getClass().getSimpleName();
	}
	public ArrayList<AppItem> homeADItems = new ArrayList<AppItem>();
	private ProtocolTask mTask;
	protected ImageAdapter mAdapter;
	protected boolean isRequesting;
	protected boolean isLoading = true;
	protected float rating;
	protected View loading;
	protected LoadFailedView loadfailed;
	protected CusPullListView listview;
	public TextView listview_loadfailed_text;
	private ImageView listview_loadfailed_icon;
	public MarketContext maContext;
	public DisplayMetrics outMetrics = new DisplayMetrics();
	public boolean isonepagere=false;
	public boolean isempty = false;
	public Map<String, Integer> payArgs=new HashMap<String, Integer>();
	public boolean isPayTemp;
	
	private String md5 = null;
	/**
	 * 返回数据请求地址
	 * 
	 * @return
	 */
	protected abstract String getRequestUrl();
	//protected abstract String getRefoushtUrl();
	protected abstract String getRefoushtUrl();
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
	protected abstract List<BigItem> dblistData();
	public List<BigItem> dblistData(int index,int to){
		return null;
	};
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
			Log.d("hometest", "visibler---");
			loading.setVisibility(View.VISIBLE);
			loadfailed.setVisibility(View.GONE);
		} else {
			if (mAdapter.getLastItemType() == BigItem.Type.LAYOUT_LOADFAILED) {
				return;
			} else {
				mAdapter.addLast(BigItem.Type.LAYOUT_LOADING);
			}
		}
		mTask = new ProtocolTask(getActivity());
		mTask.setListener(this);
		mTask.setListViewFragment(this);
		
//		//先从数据库里面找 
//		if(!getdataformDb()){
//			mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
//		}
		
		//-----by ===========bobo------------------
		md5 = MD5Tools.MD5(getRequestUrl());
		if(FileCacheUtil.isgetForcache(getActivity(), getRequestUrl())||"notf".equals(FileCacheUtil.getType(getRequestUrl()))){
			mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
			FileCacheUtil.canleRpit(getRequestUrl());
		}else{
			String resout = null;
					resout = FileCacheUtil.getUrlCache(getActivity(), md5,getRequestUrl(), System.currentTimeMillis());
			if(resout!=null){
					try {
						getJsonfromCache(true, new JSONObject(resout));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}else{
				
			mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
			FileCacheUtil.canleRpit(getRequestUrl());
			}
		}
		
		
		
		//----------------------------------------------end
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

	public ListViewFragment() {
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
				.cacheOnDisc()
				.build();
		/*options2 = new DisplayImageOptions.Builder()
		.cacheOnDisc().delayBeforeLoading(150)
		.build();
		options3 = new DisplayImageOptions.Builder()
		.cacheOnDisc().delayBeforeLoading(0)
		.build();*/
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
	
		if(!isjoke){
			loading = view.findViewById(R.id.listview_loading);
			loadfailed = (LoadFailedView) view
					.findViewById(R.id.listview_loadfailed);
			listview_loadfailed_text = (TextView) view
					.findViewById(R.id.listview_loadfailed_text);
			listview_loadfailed_icon = (ImageView) view
					.findViewById(R.id.listview_loadfailed_icon);
			listview = (CusPullListView) view.findViewById(R.id.listview);
			listview.setScrollRefreshListener(new ScrollRefreshListener(){

				@Override
				public void scrollRefreshListener(long time) {
					mHandler.removeMessages(110);
					mHandler.sendEmptyMessageDelayed(110, time);
				}
				
			});
			loading.setVisibility(View.GONE);
			loadfailed.setVisibility(View.GONE);
			setStyle(listview);
			listview.setOnScrollListener(this);
//			if(MainActivity.mainprogressbar.getVisibility()==View.VISIBLE){
//				
//				MainActivity.mainprogressbar.setVisibility(View.GONE);
//				
//			}//-------
		}
		initViewBYT(view);
		// listview.setVisibility(View.GONE);
		
		// SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new
		// SwingBottomInAnimationAdapter(mAdapter);
		// swingBottomInAnimationAdapter.setListView(listview);
		// listview.setAdapter(swingBottomInAnimationAdapter);
		return view;
	}

	
	public void setStyle(CusPullListView listview2) {

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if(!isjoke){
			loadfailed.setOnClickListener(this);
		}
	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0){
				request();
			}else if(msg.what == 1){
				//--------------------------------------
				mTask = new ProtocolTask(getActivity());
				mTask.setListener(ListViewFragment.this);
				mTask.setListViewFragment(ListViewFragment.this);
				mTask.execute(getRefoushtUrl(), getRequestContent(), tag(), getHeader());
			}
		};
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		canRequestGet();
	}
	public void canRequestGet() {
		if (!MenuBaseActivity.isMainFrame){
			request();
			//Toast.makeText(getActivity(), "request()", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if(!isjoke){
			mAdapter.loadIcon(listview);
		}
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
		if(!isjoke){
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
	}

	@Override
	public void onResume() {
		super.onResume();
		// mImageFetcher.setExitTasksEarly(false);
		LogUtil.i("appupdate", "touch lf onResume");
		/*delete by zengxiao for:去掉onresume 时刷新数据的操作*/
		/*if (!mAdapter.isEmpty()) {
			mAdapter.notifyDataSetChanged();
			mAdapter.loadIcon(listview);
		}*/
		/*delete end*/
	}

	@Override
	public void onPause() {
		super.onPause();
		// mImageFetcher.setExitTasksEarly(true);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		if(!isjoke){
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
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(!isjoke){
			try {
				DownloadTaskManager.getInstance().removeListener(this);
				if (imageLoader != null) {
					imageLoader.clearMemoryCache();
				}
				mAdapter.clear();
				listview.setAdapter(null);
				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	protected boolean isScrolling=false;
	long time=0;
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollState = scrollState;
		
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			// add by wangxin
			// mImageFetcher.setPauseWork(false);
			if(imageLoader!=null){
				imageLoader.resume();
			}
			isScrolling=false;
			mAdapter.notifyDataSetChanged();
			mAdapter.loadIcon(view);
			mAdapter.clearListIcon(view);
//			if (mNeedNotifyChanged) {
//				onDownloadStateChanged();
//				mNeedNotifyChanged = false;
//			}
			addNewDataOnce();
//			if(imageLoader!=null){
//				imageLoader.clearMemoryCache();
//			}

			break;
		case OnScrollListener.SCROLL_STATE_FLING:
//			options=options2;
			isScrolling=true;
			if(imageLoader!=null){
				imageLoader.pause();
			}
			time=System.currentTimeMillis();
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
//			options=options1;
			isScrolling=false;
			
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
		/*Log.d("rmyzx","l-------------------istview.setRefreshable(false);visibleItemCount="+visibleItemCount+"totalItemCount="+totalItemCount
        		+"firstVisibleItem="+firstVisibleItem);*/
		/*add by zengxiao for:规定只有在顶端才可滑出下拉菜单*/
		if(listview.isCanrefresh()&&(mScrollState==OnScrollListener.SCROLL_STATE_TOUCH_SCROLL||mScrollState==OnScrollListener.SCROLL_STATE_IDLE)){
			if(firstVisibleItem==0&&totalItemCount!=0){
				listview.setRefreshable(true);
	        }else{
	        	Log.d("rmyzx","listview.setRefreshable(false);visibleItemCount="+visibleItemCount+"totalItemCount="+totalItemCount
	        		+"firstVisibleItem="+firstVisibleItem);
				listview.setRefreshable(false);   
			}
		}
		/*add end*/
		boolean isBottom = firstVisibleItem + visibleItemCount == totalItemCount
				&& totalItemCount != 0 && totalItemCount > visibleItemCount;
			//	Log.d("mylog", "---------isBottom---"+isBottom+"-----isRequesting---"+isRequesting);
		if (isBottom && !isRequesting) {
			Log.d("mylog", this + "onScroll isBottom");
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
			if(!ischhanger){
				requestData();
				//Log.d("mylog", "---------requestData()");
			}
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
	
	
	//解析数据库拿出的数据
	private Boolean getdataformDb(){
	//	final PageInfo pageInfo = null;
		//mAdapter.setPageInfo(pageInfo);
//		if (!result.isNull("rating")) {
//			rating = Float.valueOf(result.getString("rating"));
//		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final List<BigItem> appendList  = dblistData();
				if(appendList!=null){
					//return isempty;
					if (appendList.size()>=0){
						isempty = true;
					}
					mHandler.post(new Runnable() {
						
						@Override
						public void run() {
							
							if (appendList != null && !appendList.isEmpty()) {
								showNoResultView();
								//onPost(appendList);
								//setHome(pageInfo.getPageIndex(),appendList);
								if (listview.getAdapter() == null) {
									listview.setAdapter(mAdapter);
								}
								if (mAdapter.isEmpty()) {
									mAdapter.clear();
									mAdapter.add(appendList);
								} else {
									/*modify by zengxiao for:修改网络不好出现重复的item*/
									if(isonepagere||(getPageInfo().getPageIndex()==1))
									{
										mAdapter.clear();
										mAdapter.add(appendList);
										isonepagere=false;
									}
									else{
										deletelaseDate(appendList);											
									}
									/*modify end*/
								}
								setmusicisplay();
								mAdapter.notifyDataSetChanged();
								loading.setVisibility(View.GONE);
								loadfailed.setVisibility(View.GONE);
							} else {
								showNoResultView();
							}
							isRequesting = false;
							mAdapter.loadIcon(listview);
							System.gc();
							isLoading = false;
						}

						
					});
				}
			}
		}).start();
		return isempty;
	}
	// by bobo===========
	private void getJsonfromCache(boolean json,final JSONObject result ) throws NumberFormatException, JSONException{
		//final JSONObject result = null;
		if(!json){
			new Thread(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					super.run();
				//	String m = MD5Tools.MD5(getRequestUrl());
					//LogCart.Log("getRequestUrl--"+getRequestUrl()+"-------md5"+m);
					FileCacheUtil.setUrlCache(getActivity(), result.toString(), md5);
				}
			}.start();
		}
		final PageInfo pageInfo = JsonParse.parsePageInfo(result);
		mAdapter.setPageInfo(pageInfo);
		if (!result.isNull("rating")) {
			rating = Float.valueOf(result.getString("rating"));
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final List<BigItem> appendList = parseListData(result);
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						if (appendList != null && !appendList.isEmpty()) {
							showNoResultView();
							onPost(appendList);
							setHome(pageInfo.getPageIndex(),appendList);
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
									deletelaseDate(appendList);											
								}
								/*modify end*/
							}
							setmusicisplay();
							mAdapter.notifyDataSetChanged();
							loading.setVisibility(View.GONE);
							loadfailed.setVisibility(View.GONE);
						} else {
							showNoResultView();
						}
						isRequesting = false;
						mAdapter.loadIcon(listview);
						System.gc();
						isLoading = false;
					}

					
				});
			}
		}).start();
		
		//-----------------------------by bobo
	}
	@Override
	public void onPostExecute(final byte[] bytes) {
		try {
			if (bytes != null) {
				final JSONObject result = new JSONObject(new String(bytes));
				int status = JsonParse.parseResultStatus(result);
				if (status == 1) {
				//	LogCart.Log("url******************"+getRequestUrl());
					getJsonfromCache(false, result);
					mAdapter.notifyDataSetChanged();//----------------
				} else {
					setLoadfailedView();
					isRequesting = false;
					mAdapter.loadIcon(listview);
					System.gc();
					isLoading = false;
				}
			} else {
				setLoadfailedView();
				isRequesting = false;
				mAdapter.loadIcon(listview);
				System.gc();
				isLoading = false;
			}
		} catch (Exception e) {
			//e.printStackTrace();
			setLoadfailedView();
			isRequesting = false;
			mAdapter.loadIcon(listview);
			System.gc();
			isLoading = false;
		}
		
	}
	protected void setHome(int pageIndex,List<BigItem> appendList) {
		// TODO Auto-generated method stub
		
	}
	protected void setmusicisplay() {

	}
	protected void deletelaseDate( List<BigItem> appendList) {
		mAdapter.remove(mAdapter.getCount() - 1);
		mAdapter.add(appendList);
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
		mTask = new ProtocolTask(getActivity());
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
			case 110:
				if(imageLoader!=null){
					imageLoader.resume();
				}
				//by----bobo-------
//				mTask = new ProtocolTask(getActivity());
//				mTask.setListener(ListViewFragment.this);
//				mTask.setListViewFragment(ListViewFragment.this);
//				LogCart.Log("刷新------的url--"+getRequestUrl());
//				mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
				//mAdapter.notifyDataSetChanged();
				break;
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
	
	private boolean ischhanger = false;
	//add by bobo
	public void setIschang(boolean b){
		this.ischhanger = b;
	}
	private boolean isjoke = false;
	public void setIsjoke(boolean j){
		this.isjoke = j;
	}
	/*add by zengxiao */
	OnRefreshListener refreshListen=new OnRefreshListener() {
		
		@Override
		public void onRefresh() {
			if(!NetworkUtil.isNetWorking(MyApplication.getInstance()))
			{
				listview.onRefreshComplete();
				Toast.makeText(MyApplication.getInstance(), R.string.rastar_netcoll, Toast.LENGTH_SHORT).show();
			}else{
				handler.removeCallbacks(refreshRunnable);
				handler.postDelayed(refreshRunnable, 2000);
		}
		}
	};
	Runnable refreshRunnable= new Runnable() {				
		@Override
		public void run() {
			listview.setrefreshok();
//			mTask = new ProtocolTask(getActivity());
//			mTask.setListener(ListViewFragment.this);
//			mTask.setListViewFragment(ListViewFragment.this);
//			mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
//			listview.onRefreshComplete();
			
		}
	};
	/*add end*/
	//延迟加载
	public void setUserVisibleHint(boolean isVisibleToUser) {
//		Log.d("nnlog", "******延迟加载------"+isVisibleToUser);
//		if(isVisibleToUser){
//			canRequestGet();
//		}
	};
}
