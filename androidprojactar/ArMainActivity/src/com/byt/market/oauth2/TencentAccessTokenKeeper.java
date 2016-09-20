package com.byt.market.oauth2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.byt.market.Constants;

/**
 * 该类用于保存SinaAccessToken到sharepreference，并提供读取功能
 * @author xiaowei6@staff.sina.com.cn
 *
 */
public class TencentAccessTokenKeeper {
	/**
	 * 保存accesstoken到SharedPreferences
	 * @param context Activity 上下文环境
	 * @param token Oauth2AccessToken
	 */
	public static void keepAccessToken(Context context, TencentAccessToken token) {
//		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_OAUTH_TENCENT, Context.MODE_APPEND);
//		Editor editor = pref.edit();
//		editor.putString("token", token.getToken());
//		editor.putLong("expiresTime", token.getExpiresTime());
//		editor.putString("expiresIn", token.getExpiresIn());
//	    editor.putString("openId", token.getOpenId());
//		editor.commit();
	}
	
	/**
	 * 清空sharepreference
	 * @param context
	 */
	public static void clear(Context context){
	    SharedPreferences pref = context.getSharedPreferences(Constants.PREF_OAUTH_TENCENT, Context.MODE_APPEND);
	    Editor editor = pref.edit();
	    editor.clear();
	    editor.commit();
	}

	/**
	 * 从SharedPreferences读取accessstoken
	 * @param context
	 * @return Oauth2AccessToken
	 */
	public static TencentAccessToken readAccessToken(Context context){
	    TencentAccessToken token = new TencentAccessToken();
//		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_OAUTH_TENCENT, Context.MODE_APPEND);
//		token.setToken(pref.getString("token", ""));
//		token.setExpiresTime(pref.getLong("expiresTime", 0));
//		token.setExpiresIn(pref.getString("expiresIn", "0"));
//        token.setOpenId(pref.getString("openId", null));
		
		return token;
	}
}
