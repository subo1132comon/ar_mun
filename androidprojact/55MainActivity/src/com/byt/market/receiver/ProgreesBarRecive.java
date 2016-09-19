package com.byt.market.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class ProgreesBarRecive extends BroadcastReceiver{

	public RelativeLayout mbar;
	public ProgreesBarRecive(RelativeLayout bar){
		this.mbar = bar;
	}
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		if("".equals(arg1.getAction())){
			mbar.setVisibility(View.VISIBLE);
		}
		
	}
	
}
