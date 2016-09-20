package com.byt.market.oauth2;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.byt.market.Constants;
import com.byt.market.data.UserData;

public class TencentOauth2 {/*
    public static final String APP_ID = "";
    
    public static UserData getUserInfo(Tencent tencent){
        Bundle values = new Bundle();
        values.putString("access_token", tencent.getAccessToken());
        values.putString("openid", tencent.getOpenId());
        JSONObject request = tencent.request(Constants.GRAPH_USER_INFO, values , Constants.HTTP_GET);
        try {
        	UserData user = new UserData();
            user.setUid(tencent.getOpenId());
            user.setType(UserData.TYPE_TENCENT);
            if(request.getInt("ret") >= 0){
                user.setNickname(request.getString("nickname"));
                if(!request.isNull("figureurl_qq_2") && request.getString("figureurl_qq_2") != null){
                	user.setIconUrl(request.getString("figureurl_qq_2"));
                } else {
                	user.setIconUrl(request.getString("figureurl_qq_1"));
                }
                String gender = request.getString("gender");
                if("男".equals(gender)){
                	user.setGender("1");
                } else if("女".equals(gender)){
                	user.setGender("2");
                } else {
                	user.setGender("0");
                }
            } else {
//                user.setRet(request.getInt("ret"));
//                user.setMsg(request.getString("msg"));
            }
            return user;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static OpenId getOpenId(Tencent tencent){
        JSONObject request = tencent.request(Constants.GRAPH_OPEN_ID, null, Constants.HTTP_GET);
        try {
            return new OpenId(request.getString("openid"), request.getString("clientid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
*/}
