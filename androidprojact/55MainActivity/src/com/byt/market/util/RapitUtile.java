package com.byt.market.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;

import com.byt.market.Constants;
import com.byt.market.MyApplication;

public class RapitUtile {

	public static void deletRapit(){
		//取消红点
		SharedPreferences rapit_p = MyApplication.getInstance()
				.getSharedPreferences(Constants.RAPIT_PLAYSP, 0);
				SharedPreferences.Editor editor = rapit_p.edit();
				editor.putBoolean(Constants.RAPIT_KEY, false);
				editor.commit();
	}
	
	public static boolean isShowRapit(){
		//如果不是第一次启动 就把红点取消
		SharedPreferences sp_rapit = MyApplication.getInstance().getSharedPreferences(Constants.RAPIT_PLAYSP, 0);
		boolean b = sp_rapit.getBoolean(Constants.RAPIT_KEY, false);
		return b;
	}
	
	public static boolean tvToastShow(String type){
		//如果不是第一次启动 就把红点取消
		SharedPreferences sp_rapit = MyApplication.getInstance().getSharedPreferences(type, 0);
		boolean b = sp_rapit.getBoolean(type+"v", false);
		return b;
	}
	
	public static void deletTVtoastShow(String type){
		SharedPreferences rapit_p = MyApplication.getInstance()
				.getSharedPreferences(type, 0);
				SharedPreferences.Editor editor = rapit_p.edit();
				editor.putBoolean(type+"v", true);
				editor.commit();
	}
	
	public static boolean showTVbutton(){
		
		SharedPreferences sp_rapit = MyApplication.getInstance().getSharedPreferences(Constants.TV_SHOW_DELET_SP, 0);
		boolean b = sp_rapit.getBoolean(Constants.TV_SHOW_DELET, false);
		return b;
	}
	
	public static void cancenButton(){
		SharedPreferences rapit_p = MyApplication.getInstance()
				.getSharedPreferences(Constants.TV_SHOW_DELET_SP, 0);
				SharedPreferences.Editor editor = rapit_p.edit();
				editor.putBoolean(Constants.TV_SHOW_DELET, true);
				editor.commit();
	}
	/**
	 * 
	 * @return
	 * 查看底部av是否支付
	 */
	public static boolean queayIspay(){
		SharedPreferences sp_rapit = MyApplication.getInstance().getSharedPreferences(Constants.UM_AIDAO_SP, 0);
		boolean b = sp_rapit.getBoolean(Constants.UM_AIDAO, false);
		return b;
	}
	/**
	 * 底部av是否支付
	 */
	public static void setTpay(boolean b){
		SharedPreferences rapit_p = MyApplication.getInstance()
		.getSharedPreferences(Constants.UM_AIDAO_SP, 0);
		SharedPreferences.Editor editor = rapit_p.edit();
		editor.putBoolean(Constants.UM_AIDAO, b);
		editor.commit();
	}
	
	public static void setisBro(boolean b){
		SharedPreferences rapit_p = MyApplication.getInstance()
				.getSharedPreferences(Constants.AV_ISbro_SP, 0);
				SharedPreferences.Editor editor = rapit_p.edit();
				editor.putBoolean(Constants.AV_ISbro, b);
				editor.commit();
	}
	public static boolean getIsBro(){
		SharedPreferences sp_rapit = MyApplication.getInstance().getSharedPreferences(Constants.AV_ISbro_SP, 0);
		boolean b = sp_rapit.getBoolean(Constants.AV_ISbro, false);
		return b;
	}
	public static long getEnterApptime(){
		SharedPreferences sharepreference =MyApplication.getInstance().getSharedPreferences("myupdate", 0);
		long unpdataValue = sharepreference.getLong("unpdata", 0);
		return unpdataValue;
	}
	
	/**
	 * 交易ID
	 * @param id
	 */
	public static void setTransID(String id){
		SharedPreferences rapit_p = MyApplication.getInstance()
				.getSharedPreferences(Constants.Trans_sp, 0);
				SharedPreferences.Editor editor = rapit_p.edit();
				editor.putString(Constants.TRANNS_KEY, id);
				editor.commit();
	}
	public static String getTrannsID(){
		SharedPreferences rapit_p = MyApplication.getInstance()
				.getSharedPreferences(Constants.Trans_sp, 0);
		return rapit_p.getString(Constants.TRANNS_KEY, "");
	}
	
	/**
	 * 极光推送 别名是否设置
	 * @param id
	 */
	public static void setIsAlias(boolean b){
		SharedPreferences rapit_p = MyApplication.getInstance()
				.getSharedPreferences("BIEMING", 0);
				SharedPreferences.Editor editor = rapit_p.edit();
				editor.putBoolean("bvluae", b);
				editor.commit();
	}
	public static boolean getisAlias(){
		SharedPreferences rapit_p = MyApplication.getInstance()
				.getSharedPreferences("BIEMING", 0);
		return rapit_p.getBoolean("bvluae", false);
	}
	
}
