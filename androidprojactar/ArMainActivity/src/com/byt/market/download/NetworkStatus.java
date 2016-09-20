/**
 * @author jiangxiaoliang
 * ��ȡ��ǰ����״̬������
 */

package com.byt.market.download;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

public class NetworkStatus {
	    
	private Context mcontext;
	private ConnectivityManager connectivityManager;
	private TelephonyManager telephonyManager;
	
	private static NetworkStatus mInstance;
	
	public static NetworkStatus getInstance(Context context){
		if(mInstance == null){
			mInstance = new NetworkStatus(context);
		}
		return mInstance;
	}
	
	public NetworkStatus(Context context) {
		mcontext = context.getApplicationContext();
        connectivityManager = (ConnectivityManager)mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager)mcontext.getSystemService(Context.TELEPHONY_SERVICE);
	}
	

    /**
     * 是否已连接wifi网络
     * @return
     */
    public boolean isWiFiConnected() {
        NetworkInfo netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (netInfo != null) {
            return netInfo.getState() == NetworkInfo.State.CONNECTED;
        } else {
            return false;
        }
    }

    /**
     * 是否连接手机网络（2G、3G等）
     * @return
     */
    public boolean isMobileConnected() {
        NetworkInfo netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (netInfo != null) {
            return netInfo.getState() == NetworkInfo.State.CONNECTED;
        } else {
            return false;
        }
    }

    /**
     * 获取当前手机网络的类型（2G、3G等）
     * @return
     */
    private int getMobileNetworkType(){
    	int state;
    	switch(telephonyManager.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			state = ConnectionListener.CONN_UNKNOWN;
			break;
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_EDGE:
			state = ConnectionListener.CONN_GPRS;
			break;
		default:
			state = ConnectionListener.CONN_3G;
			break;
		}
    	return state;
    }

    /**
     * 是否已连接网络（未连接、wifi、2G、3G等）
     * @return
     */
    public boolean isConnected() {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null) {
            return netInfo.isConnected();
        } else {
            return false;
        }
    }

    /**
     * 获取当前所连接网络类型（未连接、wifi、2G、3G等）
     * @return
     */
    public int getNetWorkState(){
    	if(isConnected()){
    		if(isMobileConnected()){
    			return getMobileNetworkType();
    		}
    		
    		if(isWiFiConnected()){
    			return ConnectionListener.CONN_WIFI;
    		}
    	}
    	return ConnectionListener.CONN_NONE;
    }

    public boolean isRadioOff() {
        ServiceState serviceState = new ServiceState();
        return serviceState.getState() == ServiceState.STATE_POWER_OFF;
    }

}
