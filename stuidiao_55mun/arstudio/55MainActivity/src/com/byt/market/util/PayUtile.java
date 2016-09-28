package com.byt.market.util;

import com.byt.market.R;
import com.byt.market.view.MyMoneyDailog;
import com.byt.market.view.MyPayDailog;
import com.byt.market.view.MyPayDailog.PayBack;

import android.app.Activity;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public class PayUtile {
	
	public static void pay(Activity context,String transID,String customerId,String price,
		String propsName,int feeid,PayBack callback){
	
	    MyPayDailog dailog = new MyPayDailog(context,R.style.MyDialog);
	    
	    dailog.initData(context,transID,customerId,
	    price, propsName,feeid,callback);
	    
	    dailog.show();
		Window win = dailog.getWindow();
		//win.setGravity(Gravity.BOTTOM);//对话框底部显示，如果没设置默认是居中显示的  
		win.getDecorView().setPadding(0, 0, 0, 0);//间距为0  
		win.setBackgroundDrawableResource(android.R.color.transparent); //清除黑色边框
		win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);  //清除模糊背景
		win.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,WindowManager.LayoutParams.FLAG_DIM_BEHIND);//设置模糊背景  
		android.view.WindowManager.LayoutParams lp = win.getAttributes();  //获取对话框当前的参数值
		//lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		Display display = win.getWindowManager().getDefaultDisplay();
		lp.height = (int) (0.8*display.getHeight());
		lp.width = display.getWidth();
        //设置窗口高度为包裹内容
       // lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        win.setAttributes(lp);     //设置生效 
		
	}
	
	@SuppressWarnings("deprecation")
	public static void showPay(Activity context,String priceday,String pricemonth,String priceyear,com.byt.market.view.MyMoneyDailog.PayBack callback){
		
		Log.d("nnlog", "beacase");
		MyMoneyDailog dailog = new MyMoneyDailog(context, R.style.MyDialog);
		dailog.initData(callback,priceday,pricemonth,priceyear);
		dailog.show();
		
		Window win = dailog.getWindow();
		//win.setGravity(Gravity.BOTTOM);//对话框底部显示，如果没设置默认是居中显示的  
		//win.getDecorView().setPadding(50, 50, 50, 50);//间距为0  
		win.setBackgroundDrawableResource(android.R.color.transparent); //清除黑色边框
		win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);  //清除模糊背景
		win.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,WindowManager.LayoutParams.FLAG_DIM_BEHIND);//设置模糊背景  
		android.view.WindowManager.LayoutParams lp = win.getAttributes();  //获取对话框当前的参数值
		//lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		Display display = win.getWindowManager().getDefaultDisplay(); 
		lp.height = (int) (0.6*display.getHeight());
		lp.width = (int) (0.9*display.getWidth());
        //设置窗口高度为包裹内容
       // lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        win.setAttributes(lp);  
		
	}

}
