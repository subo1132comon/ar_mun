package com.byt.market.util;

import java.util.ArrayList;

import com.byt.market.adapter.ImageAdapter.BigHolder;
import com.byt.market.adapter.ImageAdapter.CommonItemHolder;

public class Singinstents {

	private Singinstents(){};
	private static Singinstents sing = new Singinstents();
	private String vdiouri = null;
	private String appPackageName =null;
	private String icUrl = null;
	public String getIcUrl() {
		return icUrl;
	}
	public void setIcUrl(String icUrl) {
		this.icUrl = icUrl;
	}
	private BigHolder holder;
	public BigHolder getHolder() {
		return holder;
	}
	public void setHolder(BigHolder holder) {
		this.holder = holder;
	}
	public static Singinstents getInstents(){
		if(sing == null){
			sing = new Singinstents();
		}
		return sing;
	}
	public  ArrayList<String> marray = new ArrayList<String>();
	public  ArrayList<String> getArry(){
		return marray;
	}
	public void setArray(ArrayList<String> array){
		this.marray = array;
	}
	public void setVdiouri(String url){
		this.vdiouri = url;
	}
	public String getVdiouri(){
		return vdiouri;
	}
	public String getAppPackageName() {
		return appPackageName;
	}
	public void setAppPackageName(String appPackageName) {
		this.appPackageName = appPackageName;
	}
}
