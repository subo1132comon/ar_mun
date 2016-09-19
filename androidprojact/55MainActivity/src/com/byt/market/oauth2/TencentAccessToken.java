package com.byt.market.oauth2;


public class TencentAccessToken 
//extends Oauth2AccessToken 
{
    private String expiresIn;
    private String openId;
    
    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expirseIn) {
        this.expiresIn = expirseIn;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }
    public String getToken(){
    	return null;
    }
}
