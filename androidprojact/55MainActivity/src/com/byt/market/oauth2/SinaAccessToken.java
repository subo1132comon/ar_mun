package com.byt.market.oauth2;


public class SinaAccessToken 
//extends Oauth2AccessToken 
{
    
    public SinaAccessToken() {
    }
    
    public SinaAccessToken(String token, String expires_in){
//        super(token, expires_in);
    }
    
    public SinaAccessToken(String token, String expires_in, String uid){
//        super(token, expires_in);
        this.uid = uid;
    }
    
    
    
    private String uid;

    /**
     * 获得新浪微博授权用户id
     * @return
     */
    public String getUid() {
        return uid;
    }

    /**
     * 设置新浪微博授权用户id
     * @param uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getToken(){
    	return null;
    }
    
}
