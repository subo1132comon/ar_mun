
package com.byt.market.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.Constants.Settings;
import com.byt.market.R;
import com.byt.market.ui.base.BaseUIFragment;

public class SettingFragment2 extends BaseUIFragment implements OnClickListener {

    private TextView mTxtNoIcon;
    private TextView mTxtToast;
    private TextView mTxtDPath;
    private TextView mTxtDSize;
    private TextView mTxtQInstall;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment2, container, false);
//        view.findViewById(R.id.settings_fragment_noicon).setOnClickListener(this);
//        view.findViewById(R.id.settings_fragment_toast).setOnClickListener(this);
//        view.findViewById(R.id.settings_fragment_dpath).setOnClickListener(this);
//        view.findViewById(R.id.settings_fragment_dpath).setVisibility(View.GONE);
//        view.findViewById(R.id.settings_fragment_dsize).setOnClickListener(this);
//        view.findViewById(R.id.settings_fragment_quick_install).setOnClickListener(this);
//        mTxtNoIcon = (TextView) view.findViewById(R.id.settings_fragment_smalltitle1);
//        mTxtToast = (TextView) view.findViewById(R.id.settings_fragment_smalltitle2);
//        mTxtDPath = (TextView) view.findViewById(R.id.settings_fragment_smalltitle3);
//        mTxtDSize = (TextView) view.findViewById(R.id.settings_fragment_smalltitle4);
//        mTxtQInstall = (TextView) view.findViewById(R.id.settings_fragment_smalltitle5);
//        StringBuilder sb = new StringBuilder();
//        sb.append(Settings.loadIcon ? "显示图片  " : "");
//        sb.append(Settings.loadBigIcon ? "显示截图  " : "");
//        //sb.append(Settings.wifiDownload ? "仅在WIFI环境下载" : "");
//        mTxtNoIcon.setText(sb.toString());
//        StringBuilder sb2 = new StringBuilder();
//        sb2.append(Settings.userUpdateNotify ? "显示通知栏更新  " : "");
//        sb2.append(Settings.userGameRecommendNotify ? "显示推荐游戏提示" : "");
//        mTxtToast.setText(sb2.toString());
//        mTxtDPath.setText(Settings.downloadSD ? "SD卡" : "手机");
//        mTxtDSize.setText("" + Settings.downloadNum);
//        mTxtQInstall.setText(this.getActivity().getString(R.string.settings_qinstall_title_s, Settings.quickInstall ? "已开启" : "已关闭"));
        return view;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Constants.Settings.readSettings(getActivity());
        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_fragment_noicon:
                showNoIconDialog();
                break;
            case R.id.settings_fragment_toast:
                showToastDialog();
                break;
            case R.id.settings_fragment_dpath:
                showDPathDialog();
                break;
            case R.id.settings_fragment_dsize:
                showDSizeDialog();
                break;
            case R.id.settings_fragment_quick_install:
            	showQInstallDialog();
            	break;
        }
    }

    private void showNoIconDialog() {
		// new AlertDialog.Builder(getActivity()).setMultiChoiceItems(new
		// String[] {
		// "显示图标", "显示截图"/*, "仅在WIFI环境下载"*/
		// }, new boolean[] {
		// Settings.loadIcon, Settings.loadBigIcon/*, Settings.wifiDownload*/
		// }, new DialogInterface.OnMultiChoiceClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which, boolean
		// isChecked) {
		// if(which == 0){
		// Settings.loadIcon = isChecked;
		// Settings.saveSettings(getActivity(), Settings.LOAD_ICON,
		// Settings.loadIcon);
		// } else if (which == 1){
		// Settings.loadBigIcon = isChecked;
		// Settings.saveSettings(getActivity(), Settings.LOAD_BIG_ICON,
		// Settings.loadBigIcon);
		// } else if (which == 2){
		// Settings.wifiDownload = isChecked;
		// Settings.saveSettings(getActivity(), Settings.WIFI_DOWNLOAD,
		// Settings.wifiDownload);
		// }
		// StringBuilder sb = new StringBuilder();
		// sb.append(Settings.loadIcon ? "显示图片  " : "");
		// sb.append(Settings.loadBigIcon ? "显示截图  " : "");
		// // sb.append(Settings.wifiDownload ? "仅在WIFI环境下载" : "");
		// mTxtNoIcon.setText(sb.toString());
		// }
		// }).setTitle("省流量模式").setPositiveButton("确定", null).show();
    }
    
    private void showToastDialog() {
        new AlertDialog.Builder(getActivity()).setMultiChoiceItems(new String[] {
                getString(R.string.setting_updatenoti),  getString(R.string.setting_gamenoti)
        }, new boolean[] {
                Settings.userUpdateNotify, Settings.userGameRecommendNotify
        }, new DialogInterface.OnMultiChoiceClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(which == 0){
                    Settings.userUpdateNotify = isChecked;
                    Settings.saveSettings(getActivity(), Settings.USER_UPDATE_NOTIFY, Settings.userUpdateNotify);
                } else if(which == 1){
                    Settings.userGameRecommendNotify = isChecked;
                    Settings.saveSettings(getActivity(), Settings.USER_GAME_RECOMMEND_NOTIFY, Settings.userGameRecommendNotify);
                }
                StringBuilder sb = new StringBuilder();
                sb.append(Settings.userUpdateNotify ?getString(R.string.setting_disnoupdate): "");
                sb.append(Settings.userGameRecommendNotify ?getString(R.string.setting_dispgame): "");
                mTxtToast.setText(sb.toString());
            }
        }).setTitle(getString(R.string.settings_toast_title_b)).setPositiveButton(getString(R.string.ok), null).show();
    }
    
    private void showDPathDialog(){
        new AlertDialog.Builder(getActivity()).setSingleChoiceItems(new String[]{
        		getString(R.string.settings_dpath_title_s),getString(R.string.menu_control_storage_phone) 
        }, Settings.downloadSD ? 0 : 1, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	dialog.dismiss();
                Settings.downloadSD = which == 0;
                if(Settings.downloadSD){
                	mTxtDPath.setText(getString(R.string.settings_dpath_title_s));
                } else {
                	mTxtDPath.setText(getString(R.string.menu_control_storage_phone));
                }
                Settings.saveSettings(getActivity(), Settings.DOWNLOAD_SD, Settings.downloadSD);
            }
        }).setTitle(getString(R.string.settings_dpath_title_b)).show();
    }
    
    private void showDSizeDialog(){
        new AlertDialog.Builder(getActivity()).setSingleChoiceItems(new String[]{
                "1", "2", "3"
        }, Settings.downloadNum - 1, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	dialog.dismiss();
                Settings.downloadNum = which + 1;
                mTxtDSize.setText("" + Settings.downloadNum);
                Settings.saveSettings(getActivity(), Settings.DOWNLOAD_NUM, Settings.downloadNum);
            }
        }).setTitle(getString(R.string.settings_dsize_title_b)).show();
    }
    
    private void showQInstallDialog() {
        new AlertDialog.Builder(getActivity()).setSingleChoiceItems(new String[] {
        		getString(R.string.settting_open), getString(R.string.setting_close)
        }, 
        Settings.quickInstall ? 0 : 1
        ,new android.content.DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				android.util.Log.d("cx","which is "+which);
				Settings.quickInstall = which == 0 ? true : false;
				Settings.saveSettings(getActivity(), Settings.QUICK_INSTALL, Settings.quickInstall);
				mTxtQInstall.setText(SettingFragment2.this.getActivity().getString(R.string.settings_qinstall_title_s, Settings.quickInstall ? SettingFragment2.this.getActivity().getString(R.string.setting_opened): SettingFragment2.this.getActivity().getString(R.string.setting_closeed)));
			}
        	
        }).setTitle(R.string.settings_qinstall_title_b).setPositiveButton(getString(R.string.ok), null)
        .setCancelable(false).show();
    }
    
}
