package com.byt.market.data;

/**
 * Created by Administrator on 2014/4/11.
 */
public class DownloadAppItem {
	private AppItem appItem;
	private String text;
	private boolean isShowPopup;

	public AppItem getAppItem() {
		return appItem;
	}

	public void setAppItem(AppItem appItem) {
		this.appItem = appItem;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public DownloadAppItem(AppItem item, String categroyText) {
		appItem = item;
		text = categroyText;

	}

	public boolean isShowPopup() {
		return isShowPopup;
	}

	public void setShowPopup(boolean isShowPopup) {
		this.isShowPopup = isShowPopup;
	}

	public boolean isClickable() {
		if (appItem == null) {
			return false;
		}
		return true;
	}
}
