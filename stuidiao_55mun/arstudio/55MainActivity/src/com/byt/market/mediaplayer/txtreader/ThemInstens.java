package com.byt.market.mediaplayer.txtreader;

public class ThemInstens {
	
	private String mposition = "0";
	private static ThemInstens them = new ThemInstens();
	public static ThemInstens getTemInstens(){
		if(them!=null){
			return them;
		}
		return them;
	}
	public void setPosition(String position){
		this.mposition = position;
	}
	public String getPosition(){
		return mposition;
	}
}
