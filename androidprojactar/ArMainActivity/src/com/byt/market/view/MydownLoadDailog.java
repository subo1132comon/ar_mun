package com.byt.market.view;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.byt.ar.R;

public class MydownLoadDailog {
	private Context mcontext;
	private Dialog mlog;
	public MydownLoadDailog(Context context){
		this.mcontext = context;
		initView();
	}
	private void initView(){
		View view = LayoutInflater.from(mcontext).inflate(R.layout.dialog_download, null);
		mlog = new Dialog(mcontext);
		mlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mlog.setContentView(view);
		mlog.setCancelable(true);
	}
	
	public void show(){
		mlog.show();
	}
}
