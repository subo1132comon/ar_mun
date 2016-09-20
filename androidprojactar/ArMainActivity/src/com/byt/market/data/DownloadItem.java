package com.byt.market.data;

import com.byt.market.MyApplication;
import com.byt.ar.R;



/**
 * 下载详细
 * */
public class DownloadItem extends AppItem {

	public long dSize; // 大小Size
	public String savePath; // 保存位置
	public long lastDSize;

	private boolean isInstalledByMarket;
	
	/**
	 * 取得保存路径
	 * 
	 * @return
	 */
	public String getSavePath() {
		return this.savePath;
	}

	public void setSavePath(String path) {
		this.savePath = path;
	}

	/**
	 * 取得已完成下载大小
	 * 
	 * @return
	 */
	public long getdSize() {
		return this.dSize;
	}

	/**
	 *设置已下载文件大小
	 * 
	 * @param d
	 */
	public void setdSize(long d) {
		this.dSize = d;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (o.getClass() != getClass()) {
			return false;
		}
		DownloadItem i = (DownloadItem) o;
		if (i.sid == sid) {
			return true;
		}
		return false;
	}

	public String showDownloadInfo() {
		return MyApplication.getInstance().getResources().getString(R.string.resourdce_id) + sid + ","+MyApplication.getInstance().getResources().getString(R.string.res_idnow) + name
				+ ","+MyApplication.getInstance().getResources().getString(R.string.alltotal)+"：" + length + ","+MyApplication.getInstance().getResources().getString(R.string.downloaded_title_desc)+"：" + getdSize() +  MyApplication.getInstance().getResources().getString(R.string.dangqzt);
	}

	public boolean isInstalledByMarket() {
		return isInstalledByMarket;
	}

	public void setInstalledByMarket(boolean isInstalledByMarket) {
		this.isInstalledByMarket = isInstalledByMarket;
	}

	public long getLastDSize() {
		return lastDSize;
	}

	public void setLastDSize(long lastDSize) {
		this.lastDSize = lastDSize;
	}
	
	public void fill(AppItem appItem){
		fillWithOutScreenAndListInfo(appItem);
		this.screen = appItem.screen;
		this.position = appItem.position;
	}
	public void fillWithOutScreenAndListInfo(AppItem appItem){
		this.adesc = appItem.adesc;
		this.apk = appItem.apk;
		this.cateid = appItem.cateid;
		this.cateName = appItem.cateName;
		this.downNum = appItem.downNum;
		
//		this.feetype = appItem.feetype;
		this.hash = appItem.hash;
		this.icon = appItem.icon;
		this.iconUrl = appItem.iconUrl;
//		this.imagePath = appItem.imagePath;
//		this.lang = appItem.lang;
		this.length = appItem.length;
		this.name = appItem.name;
		this.pname = appItem.pname;
//		this.price = appItem.price;
		this.rating = appItem.rating;
		this.adesc = appItem.adesc;
//		this.sdk = appItem.sdk;
		this.sid = appItem.sid;
		this.state = appItem.state;
		this.strLength = appItem.strLength;
		this.stype = appItem.stype;
		this.vcode = appItem.vcode;
		this.vname = appItem.vname;
		this.list_id = appItem.list_id;
		this.list_cateid = appItem.list_cateid;
		this.updatetime = appItem.updatetime;
		this.isInnerGame = appItem.isInnerGame;
		this.isOpenned = appItem.isOpenned;
		
		this.creditLimit=appItem.creditLimit;
		this.googlemarket=appItem.googlemarket;
		this.googlePrice=appItem.googlePrice;
		this.ulevel=appItem.ulevel;
		this.isshare=appItem.isshare;
		this.ispay=appItem.ispay;
		this.oobpackage=appItem.oobpackage;
		this.gotype=appItem.gotype;
		this.pointtype=appItem.pointtype;
	}
}
