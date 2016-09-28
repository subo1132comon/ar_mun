/*
 * Copyright
 */

package com.byt.market.ui.mine;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import com.byt.market.util.LogUtil;

class SaveModel {
    private static final String TAG = "Launcher.SaveModel";

    private Context mContext;

    private HandlerThread mSaveThread = null;
    private SaveHandler mSaveHandler = null;

    Set<View> mNeedSaveCells = new HashSet<View>();

    public SaveModel(Context context) {
        mContext = context;
        
        createSaveHandlerThread();
    }

    private void createSaveHandlerThread() {
        if (mSaveThread == null) {
            mSaveThread = new HandlerThread("SaveThread", Thread.MIN_PRIORITY);
            mSaveThread.start();
            mSaveHandler = new SaveHandler(mSaveThread.getLooper());
        }
    }

    public void clear() {
        synchronized (mNeedSaveCells) {
            mNeedSaveCells.clear();
        }
    }

    public void addOrUpdate(View view) {
        synchronized (mNeedSaveCells) {
            mNeedSaveCells.add(view);
        }
    }
    
    private static final int MSG_SAVE = 0;
    
    public void save() {
        if (!mNeedSaveCells.isEmpty()) {
            Message msg = mSaveHandler.obtainMessage(MSG_SAVE);
            msg.sendToTarget();
        }
    }

    class SaveHandler extends Handler {
        public SaveHandler() {
        }
        
        public SaveHandler(Looper looper) {
            super(looper);
        }
        
        @Override  
        public void handleMessage(Message msg) {
        	switch (msg.what) {
        	case MSG_SAVE:
        		LogUtil.d(TAG, "To save");
        		try{
        			saveToDatabase();
        		} catch (Exception e){
        			LogUtil.d(TAG, "e "+e);
        		}
        		
        	default:
        		return;
        	}        	
        }
    }

    private void saveToDatabase() {
        if (LauncherApplication.DBG) LogUtil.d(TAG, "saveToDataBase() save "+ mNeedSaveCells.size() + " cells");
        
		while (!mNeedSaveCells.isEmpty()) {
			View cell = mNeedSaveCells.iterator().next();
			final ItemInfo cellInfo = (ItemInfo) cell.getTag();
			final CellLayout.LayoutParams clp = (CellLayout.LayoutParams) cell
					.getLayoutParams();

			if (LauncherApplication.DBG) {
				LogUtil.d(TAG, "saveToDataBase() newadded:" + cellInfo.isNewAdded);
                LogUtil.d(TAG, "addOrUpdate() id:"+cellInfo.id
                		+" container:"+cellInfo.container
                		+" screen: "+cellInfo.screen
                		+" cellX:"+clp.cellX+" cellY:"+clp.cellY);
			}

			if (cellInfo.isNewAdded) {
				LauncherModel.addItemToDatabase(mContext, cellInfo,
						cellInfo.container, cellInfo.screen, clp.cellX,
						clp.cellY, false);

				cellInfo.isNewAdded = false;
			} else {
				LauncherModel.moveItemInDatabase(mContext, cellInfo,
						cellInfo.container, cellInfo.screen, clp.cellX,
						clp.cellY);
			}

			mNeedSaveCells.remove(cell);
		}
        
        if (LauncherApplication.DBG) LogUtil.d(TAG, "saveToDataBase() save done");
    }
}
