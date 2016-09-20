package com.byt.market.ui.mine;

import com.byt.ar.R;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;


/**
 * Folder which contains applications or shortcuts chosen by the user.
 *
 */
public class ExpendAppFolder extends Folder {
    private static final String TAG = "Launcher.UserFolder";

    public ExpendAppFolder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    /**
     * Creates a new UserFolder, inflated from R.layout.user_folder.
     *
     * @param context The application's context.
     *
     * @return A new UserFolder.
     */
    static ExpendAppFolder fromXml(Context context) {
        return (ExpendAppFolder) LayoutInflater.from(context).inflate(R.layout.expend_app_folder, null);
    }
  
	void bind(FolderInfo info) {
        //super.bind(info);
    	mInfo = info;
    	if(mCustomView instanceof MineExpendView && info instanceof ExpendAppInfo){
    		final MineExpendView view = (MineExpendView)mCustomView;
    		view.setAppInfo(((ExpendAppInfo)info));
    	}
    }

    // When the folder opens, we need to refresh the GridView's selection by
    // forcing a layout
    @Override
    void onOpen(int splitX, int splitY, int topOffsetLimit) {
        super.onOpen(splitX, splitY, topOffsetLimit);
        requestFocus();
    }

}
