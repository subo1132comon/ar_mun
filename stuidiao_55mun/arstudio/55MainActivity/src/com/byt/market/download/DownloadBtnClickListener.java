package com.byt.market.download;

import com.byt.market.data.AppItem;

import android.view.View;
import android.view.View.OnClickListener;

public class DownloadBtnClickListener implements OnClickListener{

	AppItem mItem;
	
	public DownloadBtnClickListener(AppItem item){
		mItem = item;
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		DownloadTaskManager.getInstance().onDownloadBtnClick(mItem);
	}
}
