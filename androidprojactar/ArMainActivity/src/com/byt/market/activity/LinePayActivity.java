package com.byt.market.activity;

import com.bluepay.pay.BlueMessage;
import com.bluepay.pay.BluePay;
import com.bluepay.pay.IPayCallback;
import com.bluepay.pay.PublisherCode;
import com.byt.market.util.RapitUtile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class LinePayActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		TextView tv = new TextView(this);
//		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		tv.setLayoutParams(params);
//		tv.setText("this page is the one which send the payment request.");
//		setContentView(tv);
		String t_id =RapitUtile.getTrannsID();
		
		BluePay.getInstance().queryTrans(LinePayActivity.this, new IPayCallback() {
			
			@Override
			public void onFinished(int code, BlueMessage msg) {
				AlertDialog dialog = new AlertDialog.Builder(LinePayActivity.this).create();
				String title;
				if (msg.getCode() == 200) {
					title = "Success";
					LinePayActivity.this.sendBroadcast(new Intent("com.linepay"));
				}else {
					title = "Fail";
				}
				String msgStr = "Code:"+ msg.getCode()+ " price:"+msg.getPrice() ;
				dialog.setTitle(title);
				dialog.setMessage(msgStr);
				dialog.show();
				LinePayActivity.this.finish();
			}
		}, t_id, PublisherCode.PUBLISHER_LINE, 1);
	}

}
