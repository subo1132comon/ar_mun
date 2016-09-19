package com.byt.market.oauth2;

import com.byt.market.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 该类用于保存SinaAccessToken到sharepreference，并提供读取功能
 * @author xiaowei6@staff.sina.com.cn
 *
 */
public class SinaAccessTokenKeeper {
	/**
	 * 保存accesstoken到SharedPreferences
	 * @param context Activity 上下文环境
	 * @param token Oauth2AccessToken
	 */
	public static void keepAccessToken(Context context, SinaAccessToken token) {
//		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_OAUTH_SINA, Context.MODE_APPEND);
//		Editor editor = pref.edit();
//		editor.putString("token", token.getToken());
//		editor.putLong("expiresTime", token.getExpiresTime());
//		editor.putString("uid", token.getUid());
//		editor.commit();
	}
	
	/**
	 * 清空sharepreference
	 * @param context
	 */
	public static void clear(Context context){
	    SharedPreferences pref = context.getSharedPreferences(Constants.PREF_OAUTH_SINA, Context.MODE_APPEND);
	    Editor editor = pref.edit();
	    editor.clear();
	    editor.commit();
	}

	/**
	 * 从SharedPreferences读取accessstoken
	 * @param context
	 * @return Oauth2AccessToken
	 */
	public static SinaAccessToken readAccessToken(Context context){
	    SinaAccessToken token = new SinaAccessToken();
//		SharedPreferences pref = context.getSharedPreferences(Constants.PREF_OAUTH_SINA, Context.MODE_APPEND);
//		token.setToken(pref.getString("token", ""));
//		token.setExpiresTime(pref.getLong("expiresTime", 0));
//		token.setUid(pref.getString("uid", ""));
		return token;
	}
}
