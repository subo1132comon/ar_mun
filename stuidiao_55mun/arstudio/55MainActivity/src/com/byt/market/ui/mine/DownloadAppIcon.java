package com.byt.market.ui.mine;


import com.byt.market.R;
import com.byt.market.MyApplication;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadItem;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.util.DataUtil;
import com.byt.market.util.PackageUtil;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadAppIcon extends AppIcon{

	MineDownlodProgressView mMineDownlodProgressView;
	TextView mProgressText;
	private AppItem mItem;
	public DownloadAppIcon(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
	}

	
	public void setIconView(BubbleTextView iconView) {
		super.setIconView(iconView);
		addViews();
	}
	
	private void addViews(){
		this.setRemovable(true);
		mMineDownlodProgressView = new MineDownlodProgressView(this.getContext());

		final int size = Utilities.sIconTextureWidth;//this.getContext().getResources().getDimensionPixelSize(R.dimen.recent_applications_app_icon_size);
		LayoutParams lp = new LayoutParams(size, size);
		lp.addRule(CENTER_HORIZONTAL);
        lp.topMargin = 7;
		this.addView(mMineDownlodProgressView,lp);
		mMineDownlodProgressView.setVisibility(View.INVISIBLE);
		
		mProgressText = new TextView(this.getContext());
		mProgressText.setBackgroundResource(R.drawable.download_app_progress_text_bg);
		lp = new LayoutParams(size/2 + 6, size/2+ 6);
		lp.addRule(CENTER_HORIZONTAL);
        lp.topMargin =  size/4 + 7 - 3;
        lp.leftMargin = size/2 - 3;
        mProgressText.setTextSize(10);
        mProgressText.setGravity(Gravity.CENTER);
        mProgressText.setTextColor(Color.BLACK);
		this.addView(mProgressText,lp);
		mProgressText.setVisibility(View.INVISIBLE);
		mProgressText.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.toString().length() == 0){
					mProgressText.setVisibility(View.INVISIBLE);
					mProgressText.invalidate();
				} else {
					mProgressText.setVisibility(View.VISIBLE);
				}
			}	

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}

	private static final String STR_NULL = "";
	public void updateAppItem(AppItem item){
		mItem = item;
		boolean hasVavor = DataUtil.getInstance(this.getContext()).hasFavor(item.sid);
		if(item.state >= AppItemState.STATE_NEED_UPDATE){
			this.getIconView().setText(item.name);
			mProgressText.setText(STR_NULL);
			mMineDownlodProgressView.setVisibility(View.INVISIBLE);
			/*if(this.getIconView() != null){
				this.getIconView().setShowDownloadMask(false);
			}*/
		} else {
			DownloadTaskManager.getInstance().updateByState(getContext(), null, item, null, mProgressText, false,false,false,this.getIconView(),null);
			if(mProgressText.getTag() == null){
				if(hasVavor && item.state == AppItemState.STATE_IDLE){
					updateDownloadProgressState(item.state);
					mMineDownlodProgressView.setVisibility(View.VISIBLE);
					mMineDownlodProgressView.setProgress(0);
				} else {
					mMineDownlodProgressView.setVisibility(View.INVISIBLE);
				}
				
				//mMineDownlodProgressView.setProgress(0);
			} else {
				updateDownloadProgressState(item.state);
				mMineDownlodProgressView.setVisibility(View.VISIBLE);
				final int percent = (Integer) mProgressText.getTag();
				mMineDownlodProgressView.setProgress(percent);
			}
			if(mProgressText.getText().toString().length() == 0){
				mProgressText.setVisibility(INVISIBLE);
			}
			/*if(mMineDownlodProgressView != null && mMineDownlodProgressView.getVisibility() == View.VISIBLE &&  this.getIconView() != null){
				this.getIconView().setShowDownloadMask(true);
			} else {
				if(this.getIconView() != null){
					this.getIconView().setShowDownloadMask(false);
				}
			}*/
		}
		if(item.state == AppItemState.STATE_NEED_UPDATE){
			this.setNotification(NOTIFY_UPDATE, true);
		} else if(item.state == AppItemState.STATE_IDLE &&  hasVavor){
			this.setNotification(NOTIFY_FAV, true);
		} else {
			this.setNotification(NOTIFY_NULL, true);
		}
		
	}
	
	private void updateDownloadProgressState(int state){
		switch (state){
			case AppItemState.STATE_IDLE:
			case AppItemState.STATE_WAIT:
			case AppItemState.STATE_PAUSE:
			case AppItemState.STATE_RETRY:
			case AppItemState.STATE_DOWNLOAD_FINISH:
			case AppItemState.STATE_INSTALL_FAILED:
			case AppItemState.STATE_NEED_UPDATE:
			case AppItemState.STATE_INSTALLED_NEW:
				mMineDownlodProgressView.pause();
				break;
			case AppItemState.STATE_ONGOING:
				mMineDownlodProgressView.onGoing();
				break;
			
			case AppItemState.STATE_INSTALLING:
			case AppItemState.STATE_UNINSTALLING:
				mMineDownlodProgressView.running();
				break;
		}
		
	}
	
	
	
	
	public AppItem getAppItem(){
		return mItem;
	}


	@Override
	public void onRemoveClicked(View view) {
		// TODO Auto-generated method stub
		super.onRemoveClicked(view);
		MyApplication.getInstance().mMineViewManager.doUninstallOrRemove(mItem,this);
	}
	
	
	

}
