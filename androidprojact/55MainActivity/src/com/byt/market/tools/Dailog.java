package com.byt.market.tools;

import com.byt.market.R;

import android.app.ProgressDialog;
import android.content.Context;

public class Dailog {
	
	private static ProgressDialog progressDialog = null;
	public static void show(Context context){
		ProgressDialog MyDialog = ProgressDialog.show( context, " " , " Loading. Please wait ... ", true);
		MyDialog.show();
	}
	
	public static void show2(Context context){
		 progressDialog = new ProgressDialog(    
                context);    
            //设置进度条风格，风格为圆形，旋转的    
            progressDialog.setProgressStyle(    
                ProgressDialog.STYLE_SPINNER);    
            //设置ProgressDialog 标题    
           // progressDialog.setTitle("下载");    
            //设置ProgressDialog 提示信息    
            String msg = context.getResources().getString(R.string.state_wait_text);
            progressDialog.setMessage(msg);    
            //设置ProgressDialog 标题图标    
          //  progressDialog.setIcon(android.R.drawable.btn_star);    
            //设置ProgressDialog 的进度条是否不明确    
            progressDialog.setIndeterminate(false);    
            //设置ProgressDialog 是否可以按退回按键取消    
            progressDialog.setCancelable(false);    
//            //设置取消按钮    
//            progressDialog.setButton("取消",     
//                new ProgressDialogButtonListener());    
            // 让ProgressDialog显示    
            progressDialog.show();    
	}
	public static void dismis(){
		progressDialog.dismiss();
	}

}
