package com.byt.market.util;

import java.util.Random;

import com.byt.market.util.NotifaHttpUtil.NotifaHttpResalout;
import com.byt.market.Constants;
import com.byt.market.R;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class BluePayUtil {
	
	public static String getTransationId(int feeid, String userID){
		String TransationId = "";
		Random dom = new Random();
		int math = getRondom(dom);
		TransationId = String.valueOf(feeid)+math+userID;
		return TransationId;
	}

	public static void qureyAVResoult(String userID,NotifaHttpResalout notif){
		String url = Constants.APK_URL+"v1.php?qt=bluepay&usid="+userID;
		NotifaHttpUtil.getJson(url, notif);
	}
//	public static void qureyResoult(int feeid,String userID,NotifaHttpResalout notif){
//		String url = "http://55mun.com:8022/v1.php?qt=bluepay&"+"usid="+userID+"&feeid="+feeid;
//		NotifaHttpUtil.getJson(url, notif);
//	}
	
	public static void showErroe(Context context,int code){
		String sErorre = "";
		if(code>=400 && code<500){
			sErorre = context.getString(R.string.payerroe);
		}else if(code>=500 && code<600){
			if(code==507){
				sErorre = context.getString(R.string.timeout);
			}else{
				sErorre = context.getString(R.string.payerroe);
			}
			
		}else{
			if(code == 601){
				sErorre = context.getString(R.string.not_sufficient_funds);
			}
			sErorre = context.getString(R.string.payerroe);
		}
		Toast.makeText(context, sErorre+"---erorreCode:"+code, Toast.LENGTH_LONG).show();
	}
	
	public static int getRondom(Random dom){
		//解决有时候生成4位
		int n = dom.nextInt(99999);
		if(n<10000){
			n = getRondom(dom);
		}
		return n;
	}
}
