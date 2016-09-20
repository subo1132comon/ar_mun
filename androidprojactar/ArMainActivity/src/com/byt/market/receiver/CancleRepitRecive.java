package com.byt.market.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

public class CancleRepitRecive extends BroadcastReceiver{

	private View view =null;
	public CancleRepitRecive(View v){
		this.view = v;
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if("com.tyb.mark.jokcanclerepit".equals(intent.getAction())){
			view.setVisibility(View.GONE);
		}else if("com.tyb.mark.newscanclerepit".equals(intent.getAction())){
			view.setVisibility(View.GONE);
		}
		
	}


}
