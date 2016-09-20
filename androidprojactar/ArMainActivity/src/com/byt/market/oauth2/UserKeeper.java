package com.byt.market.oauth2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.byt.market.MyApplication;
import com.byt.market.Constants;
import com.byt.market.data.UserData;

public class UserKeeper {

	public static void saveUser() {
		SharedPreferences sp = MyApplication.getInstance()
				.getSharedPreferences(Constants.PREF_USERINFO,
						Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		UserData mUser = MyApplication.getInstance().getUser();
		if (mUser.isLogin()) {
			edit.putInt("type", mUser.getType());
			edit.putString("nickname", mUser.getNickname());
			edit.putString("iconurl", mUser.getIconUrl());
			edit.putString("pwd", mUser.getPwd());
			edit.putString("uid", mUser.getUid());
			edit.putString("gender", mUser.getGender());
			edit.putBoolean("islogin", mUser.isLogin());
			edit.putString("uuid", mUser.getMd5());
		}
		edit.commit();
	}

	public static void clearUser() {
		SharedPreferences sp = MyApplication.getInstance()
				.getSharedPreferences(Constants.PREF_USERINFO,
						Context.MODE_PRIVATE);
		sp.edit().clear().commit();
	}

	public static void loadUser() {
		SharedPreferences sp = MyApplication.getInstance()
				.getSharedPreferences(Constants.PREF_USERINFO,
						Context.MODE_PRIVATE);
		int type = sp.getInt("type", -1);
		UserData mUser = MyApplication.getInstance().getUser();
		mUser.setType(type);
		mUser.setNickname(sp.getString("nickname", null));
		mUser.setIconUrl(sp.getString("iconurl", null));
		mUser.setPwd(sp.getString("pwd", null));
		mUser.setUid(sp.getString("uid", null));
		mUser.setGender(sp.getString("gender", UserData.MALE));
		mUser.setMd5(sp.getString("uuid", null));
		mUser.setLogin(sp.getBoolean("islogin", false));
	}
}
