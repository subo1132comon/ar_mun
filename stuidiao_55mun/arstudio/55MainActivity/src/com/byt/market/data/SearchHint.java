package com.byt.market.data;

/**
 * 搜索词联想
 * 
 * @author Administrator
 * 
 */
public class SearchHint {
	public int id;
	public String key;
	public AppItem app;

	public SearchHint() {
	}

	public SearchHint(int id, String key) {
		this.id = id;
		this.key = key;
	}
	
	public SearchHint(int id, String key, AppItem app) {
		this.id = id;
		this.key = key;
		this.app = app;
	}
}
