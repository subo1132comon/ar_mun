package com.byt.market.tools;

import android.content.Context;
import android.widget.Toast;

public class Toolst {
	public static void show(Context context,int src){
		Toast.makeText(context, context.getResources().getString(src), Toast.LENGTH_LONG).show();
	}
}
