package com.byt.market.util;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonCreater {
	public static String getUserRegist(String uid, String md5Pwd){
		JSONObject json = new JSONObject();
		try {
			json.put("UID", uid);
			json.put("PWD", md5Pwd);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
}
