package com.byt.market.oauth2;


public class TencentWeiboOauth2 {/*
	public static final String APP_KEY="";
    public static final String APP_SECRET = "";
    public static final String REDIRECT_URL = "";
    
    public static AccountModel getAccountModel(Context context){
    	AccountModel model = new AccountModel();
    	model.setAccessToken(Util.getSharePersistent(context, "ACCESS_TOKEN"));
    	model.setExpiresIn(Long.parseLong(Util.getSharePersistent(context, "EXPIRES_IN")));
    	model.setOpenID(Util.getSharePersistent(context, "OPEN_ID"));
    	model.setOpenKey(Util.getSharePersistent(context, "OPEN_KEY"));
    	model.setRefreshToken(Util.getSharePersistent(context, "REFRESH_TOKEN"));
    	model.setName(Util.getSharePersistent(context, "NAME"));
    	model.setNike(Util.getSharePersistent(context, "NICK"));
    	return model;
    }
    
    public static void getUserInfo(Context context, final OnTencentWeiboUserReceive callback){
    	final AccountModel accountModel = getAccountModel(context);
		UserAPI uapi = new UserAPI(accountModel);
    	uapi.getUserInfo(context, "json", new HttpCallback() {
			
			@Override
			public void onResult(Object arg0) {
				JSONObject request = (JSONObject) ((ModelResult) arg0).getObj();
				try {
					UserData user = new UserData();
					user.setUid(accountModel.getOpenID());
					user.setType(UserData.TYPE_TENCENT_WEIBO);
					if(request.getInt("ret") >= 0){
						JSONObject info = request.getJSONObject("data");
						user.setNickname(info.getString("nick"));
						String head = info.getString("head");
						if(head.endsWith("/120")){
							user.setIconUrl(head);
						} else {
							user.setIconUrl(head + "/120");
						}
						String gender = info.getString("sex");
						if("1".equals(gender)){
							user.setGender("1");
						} else if("2".equals(gender)){
							user.setGender("2");
						} else {
							user.setGender("0");
						}
					} else {
					}
					if(callback != null){
						callback.onUserReceive(user);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, null, BaseVO.TYPE_JSON);
    }
    
    public interface OnTencentWeiboUserReceive{
    	public void onUserReceive(UserData user);
    }
*/}
