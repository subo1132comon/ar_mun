package com.byt.market.data;

/**
 * 搜索历史词
 * 
 * @author Administrator
 * 
 */
public class SearchHistory {
	public int id;
	public String key;

	public SearchHistory() {
	}
	
	public SearchHistory(int id, String key) {
		this.id = id;
		this.key = key;
	}
}
