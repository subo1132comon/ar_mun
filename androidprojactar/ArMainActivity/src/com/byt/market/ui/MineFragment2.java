package com.byt.market.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.data.AppItem;
import com.byt.market.data.DownloadItem;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.net.UrlHttpConnection;
import com.byt.market.net.UrlHttpConnection.Result;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.ui.mine.DragLayer;
import com.byt.market.ui.mine.MineViewManager.WorkSpaceChildRemovedListener;
import com.byt.market.ui.mine.Utilities;
import com.byt.market.ui.mine.WorkspaceContainer;
import com.byt.market.util.BitmapUtil;
import com.byt.market.util.FileUtil;
import com.byt.market.util.LocalGameDBManager;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.StringUtil;
//import android.annotation.SuppressLint;


/**
 * 父Fragment
 * @author Administrator
 *
 */
@SuppressLint("ValidFragment")
public class MineFragment2 extends BaseUIFragment implements DownloadTaskListener,WorkSpaceChildRemovedListener{
    private View loading;
    private View loadfailed;
    private WorkspaceContainer mProcessManagerView;
    private DragLayer mDragLayer;
    private List<AppItem> mItems;
    boolean mFirstInitData = true;
    private ImageLoader mImageLoader;
    private boolean mShowUpdateOnly = false;
    private int mWaitSyncTime = 0;
    public MineFragment2(){
    	
    }
   
    public  MineFragment2(boolean showUpdateOnly){
    	mShowUpdateOnly = showUpdateOnly;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	mWaitSyncTime = 0;
        View view = inflater.inflate(R.layout.mine_frame2, container, false);
        loading = view.findViewById(R.id.listview_loading);
        loadfailed = view.findViewById(R.id.listview_loadfailed);
        mProcessManagerView = (WorkspaceContainer) view.findViewById(R.id.mine_mgr);
        mProcessManagerView.setLauncher(MyApplication.getInstance().mMineViewManager);
        mProcessManagerView.setDragController(MyApplication.getInstance().mDragController);
        mDragLayer = (DragLayer)view.findViewById(R.id.drag_layer);
        mDragLayer.setDragController(MyApplication.getInstance().mDragController);
        MyApplication.getInstance().mMineViewManager.setContext(this.getActivity());
        MyApplication.getInstance().mDragController.setContext(this.getActivity());
        MyApplication.getInstance().mMineViewManager.setDragLayer(mDragLayer);
        MyApplication.getInstance().mMineViewManager.setWorkspace(mProcessManagerView.getWorkSpace());
        //App.getInstance().mMineViewManager.setWorkspaceEditStateListener(this);
        MyApplication.getInstance().mDragController.setDragScoller(mProcessManagerView.getWorkSpace());
        MyApplication.getInstance().mDragController.setScrollView(mDragLayer);
        MyApplication.getInstance().mDragController.setMoveTarget(mProcessManagerView.getWorkSpace());

		// The order here is bottom to top.
        MyApplication.getInstance().mDragController.addDropTarget(mProcessManagerView.getWorkSpace());
		
        loadfailed.setVisibility(View.GONE);  
        mFirstInitData = true;
        //new MyGameTask().execute();
        mHandler.sendEmptyMessage(MSG_CHECK_LOCAL_GAME_SYNCED);
        ((TextView)view.findViewById(R.id.listview_loadfailed_text)).setText( R.string.mine_empty_message/*mShowUpdateOnly ? R.string.mine_update_empty_message : R.string.mine_empty_message*/);
        if(!mShowUpdateOnly && this.getActivity() instanceof MainActivity){
        	final MainActivity activity = (MainActivity)this.getActivity();
        	loadfailed.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					activity.onClick(activity.findViewById(R.id.homePageview));
				}
			});
        }
        DownloadTaskManager.getInstance().addListener(this);
        MyApplication.getInstance().mMineViewManager.setWorkSpaceChildRemovedListener(this);
        return view;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        loadfailed.setOnClickListener(this);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    
    
    
    @Override
    public void onStart() {
        super.onStart();
        if(!mFirstInitData){
        	//new MyGameTask().execute();
        	mHandler.sendEmptyMessage(MSG_CHECK_LOCAL_GAME_SYNCED);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if(mItems != null && mItems.size() > 0) {
        	mHandler.sendEmptyMessage(MSG_UPDATE_ALL_ITEM);
        }
     }
    
    @Override
    public void onStop() {
        super.onStop();
        if(mHandler.hasMessages(MSG_CHECK_LOCAL_GAME_SYNCED)){
        	mHandler.removeMessages(MSG_CHECK_LOCAL_GAME_SYNCED);
        }
    }

	/**
     * 如果事件已经被处理，返回true，这样事件不会在Activity里再处理
     * */
    public boolean onBackDown(){
    	return MyApplication.getInstance().mMineViewManager.onBackDown();   	
    }
    private class MyGameTask extends AsyncTask<Boolean, AppItem, List<AppItem>>{

        @Override
        protected List<AppItem> doInBackground(Boolean... params) {
        	final List<AppItem> list = DownloadTaskManager.getInstance().getAllDownloadItem(params[0]);
        	DownloadTaskManager.getInstance().fillAppStates(list);
            return list;
        }
        
        
        @Override
        protected void onProgressUpdate(AppItem... values) {
            //mAdapter.add(values[0]);
        }

		@Override
		protected void onPostExecute(List<AppItem> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			onDateGot(result);
		}
    }
    
    private void onDateGot(List<AppItem> result){
    	if(mFirstInitData){
    		mItems = result;
    		mProcessManagerView.initViewFromData(result);
        	loading.setVisibility(View.GONE);
        	mHandler.sendEmptyMessage(MSG_UPDATE_ALL_ITEM);
        	mFirstInitData = false;
    	} else {
    		mItems = result;
    		mHandler.sendEmptyMessage(MSG_UPDATE_ALL_ITEM);
    		loading.setVisibility(View.GONE);
    	}
    	checkIsEmpty();
    	mImageLoader = new ImageLoader(MyApplication.getInstance(), mItems);
    	mImageLoader.loading();
    	
    }
    
    private void checkIsEmpty(){
    	if(mItems == null || mItems.size() == 0){
    		loadfailed.setVisibility(View.VISIBLE);
    	} else {
    		loadfailed.setVisibility(View.GONE);
    	}
    }
    
    
    @Override
    public void downloadEnded(DownloadTask task, int totalTask, int progressTask) {
        mHandler.sendEmptyMessage(MSG_NOTIFY_DATA_CHANGED);
    }

    @Override
    public void downloadProgress(DownloadTask task, int totalTask,
            int progressTask, long progressSize, long totalSize) {
    	final Message msg = mHandler.obtainMessage(MSG_UPDATE_SINGLE_ITEM);
    	msg.obj = task;
    	mHandler.sendMessage(msg);
    }

    @Override
    public void downloadStarted(DownloadTask task, int totalTask,
            int progressTask, long totalSize) {
    }

    @Override
    public void downloadTaskDone(int totalTask, int progressTask,
            boolean success) {
    }

    @Override
    public void endConnecting(DownloadTask task, int totalTask,
            int progressTask, DownloadException result) {
    	final Message msg = mHandler.obtainMessage(MSG_UPDATE_SINGLE_ITEM);
    	msg.obj = task;
    	mHandler.sendMessage(msg);
    }
    

    @Override
    public void startConnecting(DownloadTask task, int totalTask,
            int progressTask) {
    	final Message msg = mHandler.obtainMessage(MSG_UPDATE_SINGLE_ITEM);
    	msg.obj = task;
    	mHandler.sendMessage(msg);
    }
    
    @Override
    public void refreshUI() {
    }
    
    private static final int MSG_NOTIFY_DATA_CHANGED = 1;
    private static final int MSG_UPDATE_SINGLE_ITEM = 2;
    private static final int MSG_UPDATE_ALL_ITEM = 3;
    private static final int MSG_CHECK_LOCAL_GAME_SYNCED = 4;
    
    private static final int LOCAL_GAMES_SYNCED_CHECK_DELAY = 1500;
    Handler mHandler = new Handler(){

    	@Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case MSG_NOTIFY_DATA_CHANGED:{
                	updateAfterDateChanged();
                	checkIsEmpty();
                    break;
                }
                case MSG_UPDATE_SINGLE_ITEM:{
                  if(mItems == null){
                	  mItems = new ArrayList<AppItem>();
                  }
                  final DownloadTask task = (DownloadTask)msg.obj;
                  boolean added = false;
                  for(AppItem item : mItems){
                	  if(item.sid == task.mDownloadItem.sid){
                		  added = true;
                		  break;
                	  }
                  }
                  if(!added){
                	  mItems.add(task.mDownloadItem);
                  }
                  updateItem(task.mDownloadItem);
                  checkIsEmpty();
                  break;
                }
                case MSG_UPDATE_ALL_ITEM:{
                	if(mItems != null && mItems.size() > 0){
                		for(AppItem item : mItems){
                			updateItem(item);
                		}
                	}
                	checkIsEmpty();
                    break;
              }
              case MSG_CHECK_LOCAL_GAME_SYNCED:{
//                if(LocalGameDBManager.getInstance().isLocalSynced() || mShowUpdateOnly || mWaitSyncTime > 20){
//            		new MyGameTask().execute(false);
//            		checkIsEmpty();
//            	} else {
//            		mWaitSyncTime ++ ;
//            		mHandler.sendEmptyMessageDelayed(MSG_CHECK_LOCAL_GAME_SYNCED, LOCAL_GAMES_SYNCED_CHECK_DELAY);
//            	}
            	break;
              }
            }
            
        }
        
        
    };
    
    private void updateItem(AppItem item){
    	mProcessManagerView.updateItem(item);
    }
    
    private void updateAfterDateChanged(){
    	final List<AppItem> list = DownloadTaskManager.getInstance().getAllDownloadItem(false);
    	DownloadTaskManager.getInstance().fillAppStates(list);
    	onDateGot(list);
    }

    
    
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		DownloadTaskManager.getInstance().removeListener(this);
        MyApplication.getInstance().mMineViewManager.removeWorkSpaceChildRemovedListener();
		MyApplication.getInstance().mMineViewManager.closeFolderWithOutAnim();
		super.onDestroyView();
		//App.getInstance().recycleAllBitmaps(mProcessManagerView);
		
		if(mImageLoader != null){
			mImageLoader.cancel();
		}
	}
	
	/**
	 * 图标加载器
	 * 
	 * @author qiuximing
	 * 
	 */
	public class ImageLoader implements Runnable {

		private Context mContext;
		private UrlHttpConnection huc;
		private List<AppItem> mListItems;
		private boolean canceled;
		private DisplayMetrics outMetrics = new DisplayMetrics();


		public ImageLoader(Context context, List<AppItem> listItems) {
			this.mContext = context;
			this.mListItems = listItems;
			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(outMetrics);
		}

		/**
		 * 开始加载图标
		 */
		public void loading() {
			if (SharedPrefManager.isDisplayIconScreenshot(mContext)) {
				new Thread(this).start();
			}
		}

		/**
		 * 停止图标加载
		 */
		public void cancel() {
			canceled = true;
			if (huc != null) {
				huc.cancel();
			}
		}

		@Override
		public void run() {
			for (AppItem appitem : mListItems) {
				if (canceled) {
					return;
				}
				if(PackageUtil.isInstalledApk(mContext, appitem.pname, null)){
					continue;
				}
				loadImage(appitem);
			}
		}

		/**
		 * 优先SD卡读取图片，其次网络读取图片
		 * 
		 * @param item
		 */
		private void loadImage(AppItem item) {
			if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
				return;
			}
			String iconName ;
			if(item.iconUrl.startsWith("http://")){
				iconName = StringUtil.md5Encoding(item.iconUrl);
			}else{
				iconName = StringUtil.md5Encoding(Constants.IMG_URL
						+ item.iconUrl);
			}
			if (iconName == null) {
				return;
			}
			byte[] buffer = FileUtil.getBytesFromFile(iconName);
			if (buffer != null) {
				item.icon = BitmapUtil.Bytes2Bimap(buffer);
			} else {
				huc = new UrlHttpConnection(mContext);
				if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
					return;
				}
				if(!item.iconUrl.startsWith("http://")){
					item.iconUrl = Constants.IMG_URL+item.iconUrl;
				}
				Result result = huc.downloadIcon(item.iconUrl);
				buffer = result.getResult();
				if (result.getState() == UrlHttpConnection.OK && buffer != null) {
					item.icon = BitmapUtil.Bytes2Bimap(buffer);
					FileUtil.saveBytesToFile(buffer, iconName);
				}
			}
			if(item.icon == null){
				return;
			}
			item.icon = item.icon.createScaledBitmap(item.icon, Utilities.sIconWidth, Utilities.sIconWidth, false);
			final Message msg = mHandler.obtainMessage(MSG_UPDATE_SINGLE_ITEM);
			final DownloadTask task = new DownloadTask();
			if(item instanceof DownloadItem){
				task.mDownloadItem = (DownloadItem) item;
			} else {
				final DownloadItem downloadItem = new DownloadItem();
				downloadItem.fill(item);
				task.mDownloadItem = downloadItem;
			}
			
	    	msg.obj = task;
	    	mHandler.sendMessage(msg);
		}
	}

	@Override
	public void onChildRemoved(int sid) {
		// TODO Auto-generated method stub
		if(mItems != null && mItems.size() > 0){
			for(AppItem item : mItems){
				if(item.sid == sid){
					mItems.remove(item);
					break;
				}
			}
		}
		checkIsEmpty();
	}
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
