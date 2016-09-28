/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.byt.market.ui.mine;

import java.util.List;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;

import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadItem;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.download.DownloadUtils;
import com.byt.market.util.DataUtil;
import com.byt.market.util.LogUtil;
import com.byt.market.util.PackageUtil;

public class MineViewManager implements View.OnClickListener{
	Context mContext;
	public LayoutInflater mInflater;
	public boolean mInEdtiMode = false;
	public DragLayer mDragLayer;
	private Workspace mWorkspace;

	private Folder mOpenFolder;
	public SaveModel mSaveModel ;
    public MineViewManager(Context context) {
		// TODO Auto-generated constructor stub
    	mContext = context;
    	mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	init();
	}
    
    public DragLayer getDragLayer(){
    	return mDragLayer;
    }
    public int getDragLayerWidth() {
		return mDragLayer.getWidth();
	}

	public int getDragLayerHeight() {
		return mDragLayer.getHeight();
	}
    
	public boolean onBackDown(){
		if(mInEdtiMode && mWorkspace != null){
			mWorkspace.exitEditMode(true);
			return true;
		} else if(getOpenFolder() != null){
			closeFolder();
			return true;
		}
		return false;
	}
    public void setWorkspace(Workspace workspace){
    	mWorkspace = workspace;
    }
    
    public Workspace getWorkspace(){
    	return mWorkspace;
    }
    public void setDragLayer(DragLayer dragLayer){
    	mDragLayer = dragLayer;
    }
    
    public void setContext(Context context){
    	mContext = context;
    }
    public Context getContext(){
    	if(mContext == null){
    		mContext = MyApplication.getInstance().getApplicationContext();
    	}
    	return mContext;
    }

    private static final String TAG = "RunningTaskDialog";
    private LauncherModel mModel;           
    
    IconCache mIconCache;   

    protected void init() {

        Context context = getContext();

        mIconCache = new IconCache(context);
        mModel = new LauncherModel(context, mIconCache);
        mSaveModel = new SaveModel(context);
        
    }
    
    /**
	 * Launches the intent referred by the clicked shortcut.
	 * 
	 * @param v
	 *            The view representing the clicked shortcut.
	 */
	public void onClick(View v) {
		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "onClick()");

		Object tag = v.getTag();
		if (tag instanceof ShortcutInfo) {
			// Open shortcut
			final Intent intent = ((ShortcutInfo) tag).intent;
			int[] pos = new int[2];
			v.getLocationOnScreen(pos);
			intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0]
					+ v.getWidth(), pos[1] + v.getHeight()));
			//startActivitySafely(intent, tag);

		} else if (tag instanceof FolderInfo) {
			handleFolderClick((FolderInfo) tag);
		}
	}
	
	private void handleFolderClick(FolderInfo folderInfo) {
		LogUtil.d(TAG,"_---------handleFolderClick-----------"+folderInfo.opened);
		if (!folderInfo.opened) {
			// Close any open folder
			closeFolder();
			// Open the requested folder
			openFolder(folderInfo);
		} else {
			// Find the open folder...
			Folder openFolder = mWorkspace.getFolderForTag(folderInfo);
			int folderScreen;
			if (openFolder != null) {
				folderScreen = mWorkspace.getScreenForView(openFolder);
				// .. and close it
				closeFolder(openFolder);
				if (folderScreen != mWorkspace.getCurrentScreen()) {
					// Close any folder open on the current screen
					closeFolder();
					// Pull the folder onto this screen
					openFolder(folderInfo);
				}
			}
		}
	}
    
    public List<ShortcutInfo> getShorcatInfos(Context context,List<ActivityManager.RecentTaskInfo> recentTasks){
    	return mModel.syncAllAppsBatch(context,recentTasks);
    }
    
    
    public View createTaskIcon(Context context, ViewGroup parent, Intent intent,AppItem appItem) {
        ShortcutInfo info = mModel.getShortcutInfo(context.getPackageManager(), intent, this.getContext(),appItem);
        if (info == null) return null;
        //return createShortcut(R.layout.mine_application, parent, info);
        //return createShortcut(R.layout.folder_icon, parent, info);
        return addFolder(parent,info,appItem);
    }
  
    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent The group the shortcut belongs to.
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from layoutResId.
     */
    View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        BubbleTextView favorite = (BubbleTextView) mInflater.inflate(layoutResId, parent, false);
    	//FolderIcon favorite = (FolderIcon) mInflater.inflate(layoutResId, parent, false);

        favorite.setCompoundDrawablesWithIntrinsicBounds(null,
                new FastBitmapDrawable(info.getIcon(mIconCache)),
                null, null);
        favorite.setText(info.title);
        favorite.setTextSize(13);
        AppIcon appIcon = new AppIcon(this.getContext());
        
        appIcon.setIconView(favorite);
        appIcon.setTag(appIcon);
        appIcon.setOnClickListener(this);
        appIcon.setLauncher(this);
        
        /*if ((info.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
            appIcon.setRemovable(true);
        }*/
        //all taskIcon can be removed
        appIcon.setRemovable(true);
        return appIcon;
    }
    
    private CellLayout.CellInfo mAddItemCellInfo;
    
    private View addFolder(ViewGroup parent, ShortcutInfo info , AppItem appItem) {
    	ExpendAppInfo folderInfo = new ExpendAppInfo();
		folderInfo.title = appItem.name;
		folderInfo.mAppItem = appItem;
		folderInfo.id = appItem.sid;
		folderInfo.screen = appItem.screen;
		mAddItemCellInfo = new CellLayout.CellInfo();
		
		// Create the view
		BubbleTextView favorite = (BubbleTextView) mInflater.inflate(R.layout.mine_application, parent, false);
    	//FolderIcon favorite = (FolderIcon) mInflater.inflate(layoutResId, parent, false);
		if(info != null){
			favorite.setCompoundDrawablesWithIntrinsicBounds(null,
	                new FastBitmapDrawable(info.getIcon(mIconCache)),
	                null, null);
		}
		favorite.setText(appItem.name);
        favorite.setTextSize(13);

        AppIcon appIcon = null;
        if(appItem != null && appItem instanceof DownloadItem){
        	appIcon = new DownloadAppIcon(this.getContext());
        	appIcon.setLauncher(this);
        } else {
        	appIcon = new AppIcon(this.getContext());
        	appIcon.setLauncher(this);
        }
        
		appIcon.setIconView(favorite);
		if(appItem != null && appItem instanceof DownloadItem){
        	((DownloadAppIcon)appIcon).updateAppItem((DownloadItem)appItem);
        } 
		appIcon.setTag(folderInfo);
		appIcon.setOnClickListener(this);
		
		folderInfo.mFolderIcon = appIcon;
		return appIcon;
	}
    
    /**
	 * Creates a view representing a shortcut.
	 * 
	 * @param info
	 *            The data structure describing the shortcut.
	 * 
	 * @return A View inflated from R.layout.application.
	 */
	View createShortcut(ShortcutInfo info) {
		/*return createShortcut(R.layout.mine_application, (ViewGroup) mWorkspace
				.getChildAt(mWorkspace.getCurrentScreen()), info);*/
		return addFolder((ViewGroup) mWorkspace
				.getChildAt(mWorkspace.getCurrentScreen()),info,null);
	}
    
    public void removeTask(AppIcon appicon){
    	
    }
    
    
    public void closeFolder() {
		closeFolder(true);
	}
    
    public void closeFolderWithOutAnim() {
		closeFolder(false);
	}

	public Folder getOpenFolder() {
		return mOpenFolder;
	}

	public void closeFolder(boolean animation) {
		Folder folder = mOpenFolder;
		if (folder != null) {
			closeFolder(folder, animation);
		}
	}

	public void onClosed(Folder folder) {
		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "onClosed()...");
		FolderIcon folderIcon = null;
		if(folder.getInfo().mFolderIcon
				.getIconView() instanceof FolderIcon){
			folderIcon = (FolderIcon) (folder.getInfo().mFolderIcon
					.getIconView());
		}

		Animation animation = AnimationUtils.loadAnimation(this.getContext(),
				R.anim.fade_in_fast);
		animation.setFillBefore(true);

		//mIndicator.setVisibility(View.VISIBLE);
		//mIndicator.startAnimation(animation);

		
		if(folderIcon != null){
			folder.postProcess();
			folderIcon.reGenerateFolderIcon();
		}

		mDragLayer.removeView(folder);
		mOpenFolder = null;

		// mBackground.setVisibility(View.VISIBLE);
		mWorkspace.setVisibility(View.VISIBLE);
	}

	void closeFolder(Folder folder) {
		closeFolder(folder, true);
	}

	void closeFolder(Folder folder, boolean animation) {
		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "closeFolder...");
		if (folder.getInfo().opened == false) {
			return;
		}

		folder.getInfo().opened = false;

		Workspace w = folder.getContent();
		MyApplication.getInstance().mDragController.removeDropTarget(w);

		/* added for move drag info from closed folder to workspace. */
		if (w.getDragInfo() != null) {
			w.removeDragView();
			MyApplication.getInstance().mDragController.updateDragSource(mWorkspace);
			mWorkspace.updateDrag(w.getDragInfo());
		}

		folder.getInfo().mFolderIcon.setVisibility(View.VISIBLE);

		/* to stop edit rotation of workspace and dock */
		if (mWorkspace.isInEditMode()) {
			mWorkspace.enterEditMode();
			//mDock.enterEditMode();
		}

		folder.onClose(animation);
	}
	
	public void onDrawIconsBitmap(Canvas canvas) {
		/* disable mask first */
		mDragLayer.destroyDrawingCache();
		mDragLayer.draw(canvas);
		return;
	}
	
	
	/**
	 * Opens the user fodler described by the specified tag. The opening of the
	 * folder is animated relative to the specified View. If the View is null,
	 * no animation is played.
	 * 
	 * @param folderInfo
	 *            The FolderInfo describing the folder to open.
	 */
	public void openFolder(FolderInfo folderInfo) {
		Folder openFolder;
		if (folderInfo instanceof UserFolderInfo) {
			openFolder = UserFolder.fromXml(this.getContext());
		} else if (folderInfo instanceof ExpendAppInfo) {
			openFolder = ExpendAppFolder.fromXml(this.getContext());		
		} /*else if (folderInfo instanceof LiveFolderInfo) {
			openFolder = com.ezui.launcher.LiveFolder.fromXml(this, folderInfo);
		} */else {
			return;
		}
		Workspace workspace = mWorkspace;
		//Dock dock = mDock;

		/* to stop edit rotation of workspace and dock */
		workspace.exitEditMode(false);
		//dock.exitEditMode(false);

		MyApplication.getInstance().mDragController.addDropTarget(openFolder.getContent());

		openFolder.setDragController(MyApplication.getInstance().mDragController);
		openFolder.setLauncher(this);

		//openFolder.setBackgroundResource(R.drawable.folder_bg);
		openFolder.bind(folderInfo);

		AppIcon folderIcon = folderInfo.mFolderIcon;

		folderIcon.setVisibility(View.INVISIBLE);
		//mIndicator.clearAnimation();
		//mIndicator.setVisibility(View.INVISIBLE);

		int[] location = new int[2];

		folderIcon.getIconView().getLocationInWindow(location);
		int w = folderIcon.getIconView().getWidth();
		int h = folderIcon.getIconView().getHeight();
		openFolder.setFolderIconLocation(location[0], location[1], w, h);

		openFolder.setLabelString(folderInfo.title.toString());

		folderInfo.opened = true;

		mDragLayer.addView(openFolder);

		boolean inWorkspace = (folderIcon.getParent().getParent() instanceof Workspace);

		BubbleTextView icon = (BubbleTextView) folderIcon.getIconView();
		int INDICATOR_HEIGHT = (int) this.getContext().getResources().getDimension(
				R.dimen.folder_indicator_height);

		/* 5pixels padding */
		if (inWorkspace) {
			location[1] += (INDICATOR_HEIGHT / 2 + icon.getCompoundPaddingTop() + 5);
		} else {
			location[1] -= (INDICATOR_HEIGHT / 2 - icon.getPaddingTop()
					- icon.getCompoundDrawablePadding() + 5);
		}

		location[0] += (w / 2);

		openFolder.onOpen(location[0], location[1], workspace.getCellHeight());

		mOpenFolder = openFolder;
		if (mWorkspace.isInEditMode())
			openFolder.enterEditMode();

		workspace.setVisibility(View.GONE);
		//dock.setVisibility(View.GONE);
	}
	
	private BitmapDrawable mWallpaperDrawable = null;

	private BitmapDrawable getWallpaperDrawable() {
		/*BitmapDrawable d = mWallpaperDrawable;
		if (d != null)
			return d;

		WallpaperManager wpm = (WallpaperManager) this.getContext().getSystemService(Context.WALLPAPER_SERVICE);

		d = (BitmapDrawable) (wpm.getDrawable());
		mWallpaperDrawable = d;

		return d;*/
		BitmapDrawable d = mWallpaperDrawable;
		if (d != null)
			return d;
		Drawable drawable = this.getContext().getResources().getDrawable(R.drawable.folder_wp);
		d = (BitmapDrawable)drawable;
		mWallpaperDrawable = d;

		return d;
	}
	
	private final Paint mPaint = new Paint();
	public void onDrawWallpaper(Canvas canvas, int width, int height,
			int drawHeight, boolean top, Paint p) {
		BitmapDrawable d = getWallpaperDrawable();

		if (d == null) {
			if (LauncherApplication.DBG)
				LogUtil.w(TAG, "onDrawWallpaper got wallpaper null");
			return;
		}

		int w = d.getIntrinsicWidth();
		int h = d.getIntrinsicHeight();
		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "onDrawWallpaper() w:" + w + " h:" + h);

		int x = 0;/*(int) ((w - width) * mWorkspace.getWallpaperOffsetX() - .5f);*/ // .5f
																				// is
																				// according
																				// to
																				// window
																				// wallpaper
																				// logic

		Rect srcRect = new Rect(x, /*h - drawHeight*/0, /*x + width*/1, 1/*h*/); // temporary:
																			// work
																			// around
																			// for
																			// status
																			// bar
																			// height
		/*Rect srcRect = top ? new Rect(x, h - height, x + width, h - height
				+ drawHeight) : new Rect(x, h - drawHeight, x + width, h);*/
		Rect dstRect = new Rect(0, 0, width + 50, drawHeight);
		LogUtil.d(TAG, "onDrawWallpaper() width :" + width + " drawHeight:" + drawHeight);
		canvas.drawBitmap(d.getBitmap(), srcRect, dstRect, p == null ? mPaint
				: p);

		/* Draw mask 
		d = ((BitmapDrawable) (mMask.getDrawable()));
		w = d.getIntrinsicWidth();
		h = d.getIntrinsicHeight();

		int bitmapDrawHeight = (int) (drawHeight * h / height);
		srcRect = top ? new Rect(0, 0, width, bitmapDrawHeight) : new Rect(0, h
				- bitmapDrawHeight, width, h); // temporary: work around for
												// status bar height
		dstRect = new Rect(0, 0, width, drawHeight);

		canvas.drawBitmap(d.getBitmap(), srcRect, dstRect, p == null ? mPaint
				: p);*/

		return;
	}
	
	
	public void doUninstallOrRemove(final AppItem appItem,final View removeView){
		final boolean installed = DownloadTaskManager.getInstance().isInstalled(appItem.pname);
		if(appItem.state == AppItem.STATE_INSTALLING || appItem.state == AppItem.STATE_UNINSTALLING){
			//installing or uninstalling ,can not be removed
			MyApplication.getInstance().showToast(R.string.taost_pkg_mgr_busy);
			return;
		}
		final boolean isDownloadStarted = appItem.state < AppItemState.STATE_NEED_UPDATE;
		final boolean isFav = appItem.state == AppItemState.STATE_IDLE && DataUtil.getInstance(getContext()).hasFavor(appItem.sid);
		final int title = isFav ? R.string.expend_child5_del_fav :  isDownloadStarted ? R.string.expend_child5_remove : R.string.expend_child5_text ;
		String msg = null;
		if(R.string.expend_child5_text == title){
			if(PackageUtil.isInSystemState(mContext, appItem.pname) == PackageUtil.SYSTEM_STATE_SYSTEM_UPDATED){
				msg = mContext.getString(R.string.expend_child5_text_uninstall_system_updated_msg, appItem.name);
			}  else if(PackageUtil.isInSystemState(mContext, appItem.pname) == PackageUtil.SYSTEM_STATE_SYSTEM){
				MyApplication.getInstance().showToast(mContext.getString(R.string.expend_child5_text_uninstall_system_msg, appItem.name));
				return;
			} else {
				msg = mContext.getString(R.string.expend_child5_text_msg, appItem.name);
			}
			
		} else  if(R.string.expend_child5_remove == title){
			msg = mContext.getString(R.string.expend_child5_remove_msg, appItem.name);
		} else  if(R.string.expend_child5_del_fav == title){
			msg = mContext.getString(R.string.expend_child5_del_fav_msg, appItem.name);
		}
		final CheckBox checkBox = new CheckBox(mContext);
		checkBox.setText(R.string.expend_child5_text_check);
		final boolean hasDownloadFile = DownloadUtils.hasDownloadFile(appItem);
		if(hasDownloadFile){
			checkBox.setChecked(true);
		}
		Dialog mDialog = hasDownloadFile ? new AlertDialog.Builder(mContext)
		.setView(checkBox)
		.setTitle(title)
		.setMessage(msg)
		.setPositiveButton(title, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MyApplication.getInstance().mMineViewManager.closeFolder();
				
				// TODO Auto-generated method stub
				if(R.string.expend_child5_text ==title){
					//卸载
					DownloadTaskManager.getInstance().uninstallApp(mContext, appItem.pname,String.valueOf(appItem.sid),checkBox.isChecked() );
				} else {
					//删除下载任务
					DownloadTask task = new DownloadTask();
					task.downloadType = DownloadTaskManager.FILE_TYPE_APK;
					task.mDownloadItem = (DownloadItem) appItem;
					if(installed){
						DownloadTaskManager.getInstance().pauseDownloadTask(task);
						final Message msg = mHandler.obtainMessage(MSG_CANCEL_TASK);
						msg.obj = task;
						mHandler.sendMessageDelayed(msg, 100);
					} else {
						DownloadTaskManager.getInstance().deleteDownloadTask(task,true,false);
						MyApplication.getInstance().mMineViewManager.getWorkspace().removeItem(appItem,removeView);
					}
					if(checkBox.isChecked()){
						//删除安装包
						DownloadTaskManager.getInstance().deleteDownlaodFile((DownloadItem) appItem);
					}
				}
				
				
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.create()
		:
		new AlertDialog.Builder(mContext)
		.setTitle(title)
		.setMessage(msg)
		.setPositiveButton(title, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MyApplication.getInstance().mMineViewManager.closeFolder();
				// TODO Auto-generated method stub
				if(R.string.expend_child5_text ==title){
					//卸载
					DownloadTaskManager.getInstance().uninstallApp(mContext, appItem.pname,String.valueOf(appItem.sid),checkBox.isChecked() );
				} else {
					//删除下载任务
					DownloadTask task = new DownloadTask();
					task.downloadType = DownloadTaskManager.FILE_TYPE_APK;
					task.mDownloadItem = (DownloadItem) appItem;
					if(installed){
						DownloadTaskManager.getInstance().pauseDownloadTask(task);
						final Message msg = mHandler.obtainMessage(MSG_CANCEL_TASK);
						msg.obj = task;
						mHandler.sendMessageDelayed(msg, 100);
					} else {
						DownloadTaskManager.getInstance().deleteDownloadTask(task,true,false);
						MyApplication.getInstance().mMineViewManager.getWorkspace().removeItem(appItem,removeView);
					}
					if(checkBox.isChecked()){
						//删除安装包
						DownloadTaskManager.getInstance().deleteDownlaodFile((DownloadItem) appItem);
					}
				}
				
				
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.create();
		
		mDialog.show();
	}
    
	private static final int MSG_CANCEL_TASK = 1;
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
				case MSG_CANCEL_TASK:{
					DownloadTaskManager.getInstance().cancelDownloadTask((DownloadTask)msg.obj);
					break;
				}
			}
		}
		
	};
    
    public interface  WorkSpaceChildRemovedListener{
    	
    	public void onChildRemoved(int sid);
    }
    
    WorkSpaceChildRemovedListener mWorkSpaceChildRemovedListener;
    
    public void setWorkSpaceChildRemovedListener(WorkSpaceChildRemovedListener listener){
    	mWorkSpaceChildRemovedListener = listener;
    }
    
    public void removeWorkSpaceChildRemovedListener(){
    	mWorkSpaceChildRemovedListener = null;
    }
}