package com.byt.market;

import com.byt.market.service.PushService;
import com.byt.market.util.GameUpdateManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class BReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (intent == null) {
			return;
		}
		String ActionName = intent.getAction();
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(ActionName)) {
//			ConnectivityManager connManager = (ConnectivityManager) context
//					.getSystemService(Context.CONNECTIVITY_SERVICE);
//			NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
//			if (networkInfo != null && networkInfo.isAvailable()) {
//				startPushService(context);
//				GameUpdateManager.getInstance().tryCheckGameUpdate(App.getInstance(),true);
//			}
		} else if(ActionName.equals(Constants.ACTION_REQUEST_GAME_UPDATE_CHECK)){
			GameUpdateManager.getInstance().tryCheckGameUpdate(MyApplication.getInstance(),true);
		}
	}

	/**
	 * @param context
	 */
	private void startPushService(Context context) {
		Intent intent = new Intent(context, PushService.class);
		context.startService(intent);
	}
}
