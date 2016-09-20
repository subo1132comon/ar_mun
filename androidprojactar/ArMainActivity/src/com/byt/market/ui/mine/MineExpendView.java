package com.byt.market.ui.mine;

import com.byt.ar.R;
import com.byt.market.MyApplication;
import com.byt.market.Constants;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadItem;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.log.LogModel;
import com.byt.market.util.DataUtil;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.StringUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MineExpendView extends LinearLayout implements OnClickListener {

	private TextView mButton1;
	private TextView mButton2;
	private TextView mButton3;
	private TextView mButton4;
	private TextView mButton5;

	private ExpendAppInfo mAppInfo;
	private Context mContext;

	public MineExpendView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public void setAppInfo(ExpendAppInfo appInfo) {
		mAppInfo = appInfo;
		DownloadTaskManager.getInstance().updateByState(getContext(), mButton1,
				mAppInfo.mAppItem, null, null, false, false, false, null, null);
		/*
		 * if(mAppInfo.mAppItem.state == AppItemState.STATE_IDLE){
		 * mButton5.setVisibility(View.INVISIBLE); } else
		 */if (mAppInfo.mAppItem.state < AppItemState.STATE_NEED_UPDATE) {
			mButton5.setVisibility(View.VISIBLE);
			mButton5.setText(R.string.expend_child5_remove);
		} else {
			mButton5.setVisibility(View.VISIBLE);
			mButton5.setText(R.string.expend_child5_text);
		}
		mButton1.setTextColor(Color.BLACK);
		updateByState(mButton1, mAppInfo.mAppItem);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mButton1 = (TextView) findViewById(R.id.expend_child1);
		mButton2 = (TextView) findViewById(R.id.expend_child2);
		mButton3 = (TextView) findViewById(R.id.expend_child3);
		mButton4 = (TextView) findViewById(R.id.expend_child4);
		mButton5 = (TextView) findViewById(R.id.expend_child5);

		mButton1.setOnClickListener(this);
		mButton2.setOnClickListener(this);
		mButton3.setOnClickListener(this);
		mButton4.setOnClickListener(this);
		mButton5.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.expend_child1: {
			if (DataUtil.getInstance(mContext).hasFavor(mAppInfo.mAppItem.sid) && mAppInfo.mAppItem.state == AppItemState.STATE_IDLE) {
				mAppInfo.mAppItem.list_id = LogModel.L_DOWN_MANAGER;
				mAppInfo.mAppItem.list_cateid = LogModel.L_APP_FAVOR;
				DownloadTaskManager.getInstance().updateAppItem(mAppInfo.mAppItem);
				/*new Thread() {
					public void run() {
						DownloadTaskManager.getInstance().updateAppItem(mAppInfo.mAppItem);
						handler.sendEmptyMessage(0);
					}
				}.start();*/
			} 
			DownloadTaskManager.getInstance().onDownloadBtnClick(
					mAppInfo.mAppItem);
			MyApplication.getInstance().mMineViewManager.closeFolder();
			break;
		}
		case R.id.expend_child2: {
			gotoAppDetail(mAppInfo.mAppItem);
			MyApplication.getInstance().mMineViewManager.closeFolder();
			break;
		}
		case R.id.expend_child3: {
			gotoAppComment(mAppInfo.mAppItem);
			MyApplication.getInstance().mMineViewManager.closeFolder();
			break;
		}
		case R.id.expend_child4: {
			gotoJoySkill(mAppInfo.mAppItem);
			MyApplication.getInstance().mMineViewManager.closeFolder();
			break;
		}
		case R.id.expend_child5: {
			MyApplication.getInstance().mMineViewManager.doUninstallOrRemove(
					mAppInfo.mAppItem, mAppInfo.mFolderIcon);
			break;
		}
		}

	}

	private void gotoAppDetail(Object obj) {
		Intent intent = new Intent(Constants.TODETAIL);
		if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			intent.putExtra("app", app);
		}
		intent.putExtra(Constants.LIST_FROM, LogModel.L_DOWN_MANAGER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
		if (mContext instanceof Activity) {
			((Activity) mContext).overridePendingTransition(
					R.anim.push_left_in, R.anim.push_left_out);
		}
		// getActivity().overridePendingTransition(R.anim.push_left_in,
		// R.anim.push_left_out);
	}

	private void gotoAppComment(Object obj) {
		Intent intent = new Intent(Constants.TOCOMMENTS);
		if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			intent.putExtra("app", app);
		}
		intent.putExtra(Constants.LIST_FROM, LogModel.L_DOWN_MANAGER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
		if (mContext instanceof Activity) {
			((Activity) mContext).overridePendingTransition(
					R.anim.push_left_in, R.anim.push_left_out);
		}
	}

	private void gotoJoySkill(Object obj) {
		Intent intent = new Intent(Constants.TOJOYSKILL);
		if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			intent.putExtra("app", app);
		}
		intent.putExtra(Constants.LIST_FROM, LogModel.L_DOWN_MANAGER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
		if (mContext instanceof Activity) {
			((Activity) mContext).overridePendingTransition(
					R.anim.push_left_in, R.anim.push_left_out);
		}
	}

	public void updateByState(TextView textView, AppItem appItem) {
		int drawableRes = R.drawable.mine_folder_child_src_install;
		switch (appItem.state) {
		case AppItemState.STATE_WAIT: {
			drawableRes = R.drawable.mine_folder_child_src_waiting;
			break;
		}
		case AppItemState.STATE_RETRY: {

			break;
		}
		case AppItemState.STATE_ONGOING: {
			/*
			 * if (DownloadTaskManager.getInstance().isInstalled(appItem.pname))
			 * { drawableRes = R.drawable.mine_folder_child_src_pause; } else {
			 * 
			 * }
			 */
			drawableRes = R.drawable.mine_folder_child_src_pause;

			break;
		}
		case AppItemState.STATE_PAUSE: {
			drawableRes = R.drawable.mine_folder_child_src_resume;
			break;
		}
		case AppItemState.STATE_DOWNLOAD_FINISH: {
			if (DownloadTaskManager.getInstance().isInstalled(appItem.pname)) {
				drawableRes = R.drawable.mine_folder_child_src_update;
			} else {
				drawableRes = R.drawable.mine_folder_child_src_install;
			}
			break;
		}
		case AppItemState.STATE_INSTALLING: {
			if (DownloadTaskManager.getInstance().isInstalled(appItem.pname)) {
				drawableRes = R.drawable.mine_folder_child_src_updating;
			} else {
				drawableRes = R.drawable.mine_folder_child_src_installing;
			}
			break;
		}
		case AppItemState.STATE_INSTALL_FAILED: {
			drawableRes = R.drawable.mine_folder_child_src_resume;
			break;
		}
		case AppItemState.STATE_UNINSTALLING: {
			drawableRes = R.drawable.mine_folder_child_src_uninstalling;
			break;
		}
		case AppItemState.STATE_NEED_UPDATE: {
			drawableRes = R.drawable.mine_folder_child_src_update;
			break;
		}
		case AppItemState.STATE_INSTALLED_NEW: {
			drawableRes = R.drawable.mine_folder_child_src_open;
			break;
		}
		case AppItemState.STATE_IDLE: {
			drawableRes = R.drawable.mine_folder_child_src_install;
			break;
		}
		}
		textView.setCompoundDrawablesWithIntrinsicBounds(null, this
				.getResources().getDrawable(drawableRes), null, null);
		// textView.setBackgroundDrawable(this.getResources().getDrawable(drawableRes));
	}

}
