package com.byt.market.oauth2;

import com.byt.market.data.UserData;

public class SinaOauth2 {
    
    public static final String APP_KEY="734241126";
    public static final String APP_SECRET = "f6360ae5aee9ea81e0432158e8ccd15d";
    public static final String REDIRECT_URL = "http://www.sina.com";
    public static final String SCOPE = "email,direct_messages_read,direct_messages_write," +
            "friendships_groups_read,friendships_groups_write,statuses_to_me_read," +
                "follow_app_official_microblog";
//    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
 
    public static void getUserInfo(String token, final String uid, final OnUserReceive l){/*
    	WeiboParameters params = new WeiboParameters();
		params.add("uid", uid);
		params.add("access_token", token);
		
		AsyncWeiboRunner.request("https://api.weibo.com/2/users/show.json", params, "GET", new RequestListener() {
			
			@Override
			public void onIOException(IOException arg0) {
				l.onFail();
			}
			
			@Override
			public void onError(WeiboException arg0) {
				l.onFail();
				
			}
			
			@Override
			public void onComplete4binary(ByteArrayOutputStream arg0) {
				
			}
			
			@Override
			public void onComplete(String arg0) {
				try {
					JSONObject o = new JSONObject(arg0);
		            UserData user = new UserData();
		            user.setUid(uid);
		            user.setNickname(o.getString("screen_name"));
		            if(!o.isNull("avatar_large") && o.getString("avatar_large") != null){
		            	user.setIconUrl(o.getString("avatar_large"));
		            } else {
		            	user.setIconUrl(o.getString("profile_image_url"));
		            }
		            String gender = o.getString("gender");
		            if("m".equals(gender)){
		            	user.setGender("1");
		            } else if("f".equals(gender)){
		            	user.setGender("2");
		            } else {
		            	user.setGender("0");
		            }
		            user.setType(UserData.TYPE_SINA);
		            l.onReceive(user);
				} catch (Exception e) {
					l.onFail();
					
				}
			}
		});
    */}
    
    public static void getAccessToken(String code, final OnAccessTokenReceive l){/*
    	WeiboParameters params = new WeiboParameters();
		params.add("client_id", SinaOauth2.APP_KEY);
		params.add("client_secret", SinaOauth2.APP_SECRET);
		params.add("grant_type", "authorization_code");
		params.add("code", code);
		params.add("redirect_uri", SinaOauth2.REDIRECT_URL);
		
		AsyncWeiboRunner.request("https://api.weibo.com/oauth2/access_token", params, "POST", new RequestListener() {
			
			@Override
			public void onIOException(IOException arg0) {
				l.onFail();
			}
			
			@Override
			public void onError(WeiboException arg0) {
				l.onFail();
				
			}
			
			@Override
			public void onComplete4binary(ByteArrayOutputStream arg0) {
				
			}
			
			@Override
			public void onComplete(String arg0) {
				try {
					JSONObject o = new JSONObject(arg0);
					String access_token = o.getString("access_token");
					String express_in = o.getString("expires_in");
					String uid = o.getString("uid");
					// 这里按照 毫秒计算
					long time = Long.parseLong(express_in) * 1000 + System.currentTimeMillis();
					SinaAccessToken oauth2AccessToken = new SinaAccessToken();
					oauth2AccessToken.setExpiresTime(time);
					oauth2AccessToken.setToken(access_token);
					oauth2AccessToken.setUid(uid);
					l.onReceive(oauth2AccessToken);
				} catch (Exception e) {
					l.onFail();
					
				}
			}
		});
    */}
    
    public interface OnAccessTokenReceive{
    	public void onReceive(SinaAccessToken token);
    	
    	public void onFail();
    }
    
    public interface OnUserReceive{
    	public void onReceive(UserData user);
    	
    	public void onFail();
    }
}
