package com.byt.market.data;

import android.graphics.Bitmap;


public class UserData {
    public static final int TYPE_ME = 0;
    public static final int TYPE_SINA = 1;
    public static final int TYPE_TENCENT = 2;
    public static final int TYPE_TENCENT_WEIBO = 2;
    //之后可改成枚举
    public static final String MALE = "male";
    public static final String FEMALE = "female";
//    public static final String ICON_NAME = "usericon";
    
    private boolean isLogin;
    private Bitmap bmpIcon;

    //基本属性
    private int type;
    private String uid;
    private String pwd;
    private String nickname;
    private String iconUrl;
    private String gender;
    private String md5;//密码
    private int credit=0;
    private int ulevel=1;//用户级别
    private int VIP = 0;
    public int getVIP() {
		return VIP;
	}
	public void setVIP(int vIP) {
		VIP = vIP;
	}
	private boolean ispay;
    private boolean isCanUseSim;
    
    public boolean isCanUseSim() {
		return isCanUseSim;
	}
	public void setCanUseSim(boolean isCanUseSim) {
		this.isCanUseSim = isCanUseSim;
	}
	public boolean isIspay() {
		return ispay;
	}
	public void setIspay(boolean ispay) {
		this.ispay = ispay;
	}
	public int getUlevel() {
		return ulevel;
	}
	public void setUlevel(int ulevel) {
		this.ulevel = ulevel;
	}
	public interface OnUserInfoChangeListener{
        
        public void onUserInfoChange();
    }
    
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public boolean isLogin() {
        return isLogin;
    }
    public void setLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getPwd() {
        return pwd;
    }
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    public Bitmap getBmpIcon() {
        return bmpIcon;
    }
    public void setBmpIcon(Bitmap bmpIcon) {
        this.bmpIcon = bmpIcon;
    }
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public int getCredit() {
		return credit;
	}
	public void setCredit(int credit) {
		this.credit = credit;
	}
	
}
