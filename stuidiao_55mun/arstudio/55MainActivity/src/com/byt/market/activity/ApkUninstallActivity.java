package com.byt.market.activity;

import com.byt.market.activity.base.BaseActivity;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.util.PackageUtil;

import android.content.Intent;
import android.os.Bundle;

public class ApkUninstallActivity extends BaseActivity {

	public static final int REQUEST_CODE_UNINSTALL = 1;
	public static final String EXT_PNAME = "ext_pname";
	public static final String EXT_REMOVE_FILE = "ext_remove_file";
	public static final String EXT_SID = "ext_sid";
	private String mPname;
	private boolean mRemoveFile;
	private String mSid;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mPname = this.getIntent().getStringExtra(EXT_PNAME);
		mRemoveFile = this.getIntent().getBooleanExtra(EXT_REMOVE_FILE,false);
		mSid = this.getIntent().getStringExtra(EXT_SID);
		PackageUtil.uninstallAppByUser(this, mPname);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		DownloadTaskManager.getInstance().updateAfterApkUninstalled(null,mSid, mRemoveFile,mPname);
		finishWindow();
	}

	@Override
	public void addEvent() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void initData() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void initView() {
		// TODO Auto-generated method stub
		
	}

	

	
	
}
	