package com.byt.market.ui.base;

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.Refreshable;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.data.MarketUpdateInfo;
import com.byt.market.service.UpdateDownloadService;
import com.byt.market.util.RefreshHelper;
import com.byt.market.util.Util;

/**
 * 父Fragment
 * 
 * @author Administrator
 * 
 */
public class BaseUIFragment extends BaseFragment implements Refreshable{
//	private static final String KEY_CONTENT = "BaseUIFragment:frame";
    protected ImageLoader imageLoader;
	protected DisplayImageOptions options;
	protected ProgressDialog progressDialog;
	public Toast toast;
	public static BaseUIFragment newInstance(Class<? extends BaseUIFragment> clazz) {
		try {
            return clazz.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        }
		return null;
	}
	/**
     * 注册可刷新
     */
    protected void registRefreshable() {
        RefreshHelper.getInstance().registRefreshable(this);
    }
    
    /**
     * 注销可刷新
     */
    protected void unregistRefreshable(){
        RefreshHelper.getInstance().unregistRefreshable(this);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Constants.Settings.readSettings(getActivity());
        registRefreshable();
    }
    
    @Override
    public void onStop() {
        super.onStop();
       unregistRefreshable();
    }
    
	public void showProgressDialog(String msg) {
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setMessage(msg);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	public void dismissProgressDialog() {
		progressDialog.dismiss();
	}
    
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		MobclickAgent.onPageStart(this.getClass().getSimpleName());
	}
	@Override
		public void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			System.gc();
		}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		//System.gc();
	}


    @Override
    public void refresh() {
        
    }
    
    public HashMap<String, String> getHeader(){
    	return null;
    }
    
	/**
	 * 市场更新提示
	 */
	protected boolean handleMarketUpdateNotify(){
		final MarketUpdateInfo marketUpdateInfo = null;
		if (marketUpdateInfo == null){
			return false;
		} else {
			Builder builder = new Builder(getActivity());
			builder.setTitle(getActivity().getString(R.string.updateversionprom));
			StringBuilder sb = new StringBuilder(getActivity().getString(R.string.updateexpain)+"\n\n");
			sb.append(getActivity().getString(R.string.updateversion) + marketUpdateInfo.vname + "\n");
			// sb.append("更新时间:" + marketUpdateInfo.getTime() + "\n\n");
			sb.append(marketUpdateInfo.describe + "\n\n");
			sb.append(getActivity().getString(R.string.softwaresize)
					+ Util.apkSizeFormat(
							(marketUpdateInfo.length * 1.0) / 1024 / 1024, "M")
					+ "\n");
			sb.append(getActivity().getString(R.string.isupdate));
			builder.setMessage(sb.toString());
			builder.setNegativeButton(getActivity().getString(R.string.state_update_action_text),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							updateNow(marketUpdateInfo);
						}
					});
			builder.setPositiveButton(getString(R.string.later),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			builder.create().show();
			return true;
		}
	}

	public void updateNow(MarketUpdateInfo marketUpdateInfo) {
		if (apkPath.startsWith("http://")) {
			apkPath = marketUpdateInfo.apk;
		}else{
			apkPath = Constants.APK_URL + marketUpdateInfo.apk;
		}
		if (apkPath != null) {
			Intent intent = new Intent(getActivity(), UpdateDownloadService.class);
			getActivity().startService(intent); //
			// 如果先调用startService,则在多个服务绑定对象调用unbindService后服务仍不会被销毁
			if (binder == null)
				getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
			else
				binder.start(apkPath);
		}
	}

	protected UpdateDownloadService.DownloadBinder binder;
	private String apkPath;
	protected ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (UpdateDownloadService.DownloadBinder) service;
			binder.start(apkPath);
		}

		public void onServiceDisconnected(ComponentName name) {
		}
	};
    
	/**
	 * 创建对话框
	 * 
	 * @param title
	 *            标题
	 * @param msg
	 *            内容
	 * @return
	 */
	public Dialog createDialog(String title, String msg) {
		return new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(msg)
				.setIcon(R.drawable.icon).create();
	}
	
	public Dialog createDialog(String title, View view){
		return new AlertDialog.Builder(getActivity()).setTitle(title).setView(view)
				.setIcon(R.drawable.icon).create();
	}

	/**
	 * 关闭对话框
	 * 
	 * @param d
	 */
	public void dismissDialog(Dialog d) {
		if (d != null && d.isShowing()) {
			d.dismiss();
		}
	}

	public void showShortToast(String msg) {
		if(toast==null){
			toast= Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
		}else{
			toast.setText(msg);
			toast.setDuration(Toast.LENGTH_SHORT);
		}
		toast.show();
	}

	public void showLongToast(String msg) {
		if(toast==null){
			toast= Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG);
		}else{
			toast.setText(msg);
			toast.setDuration(Toast.LENGTH_LONG);
		}
		toast.show();
	}
}
