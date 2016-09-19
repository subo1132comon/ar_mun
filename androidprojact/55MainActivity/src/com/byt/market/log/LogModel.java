package com.byt.market.log;

import java.io.Serializable;
import java.util.HashMap;

public class LogModel {

	/**
	 * 数据管理
	 */
	public static final int DATA_LIST = 10;
	public static final int DATA_GAME = 11;
	public static final int DATA_PUSH = 12;
	public static final int DATA_APP = 13;
	public static final int DATA_KEY = 14;
	public static final int DATA_ALL_KEY = 15;
	public static final int DATA_FAVORATE = 16;
	public static final int DATA_MYFAVORATE = 17;
	public static final int DATA_MYFAV = 18;
	
	public static final int DATA_INSERT = 1;
	public static final int DATA_DEL = 2;

	/**
	 * 所有列表标示
	 */
	public static final String P_LIST = "0";
	// banner
	public static final String L_BANNER = "a";
	// banner专题列表
	public static final String L_BANNER_LIST = "a1";
	// 首页今日推荐
	public static final String L_HOME_REC = "b";
	// 首页导航
	public static final String L_HOME_LEADER = "c";
	// 首页导航
		public static final String L_FAV_USER = "z";
	// 首页导航--最新收录
	public static final String L_HOME_LEADER_LATEST_COLLECTION = "clc";
	// 首页导航--经典必玩
	public static final String L_HOME_LEADER_CLASSIC_PLAY = "ccp";
	// 首页游戏
	public static final String L_HOME = "d";
	// 排行
	public static final String L_RANK = "e";
	// 分类
	public static final String L_CATE_HOME = "f";
	// 分类最新
	public static final String L_CATE_NEW = "g";
	// 分类最热
	public static final String L_CATE_HOT = "h";
	// 更新
	public static final String L_APP_UPDATE = "i";
	// 收藏
	public static final String L_APP_FAVOR = "l";
	// 设置
	public static final String L_SETTINGS = "m";
	// 意见反馈
	public static final String L_SUGGEST = "n";
	//
	public static final String L_SEARCH_HIT = "o";
	// 搜索结果
	public static final String L_SEARCH_RS = "p";
	// 详情
	public static final String L_DETAIL = "q";
	// push
	public static final String L_PUSH = "r";
	// 我的游戏
	public static final String L_DOWN_MANAGER = "s";
	// 专题
	public static final String L_SUBJECT_HOME = "t";
	public static final String L_MUSIC_CATE_HOME = "mcf";
	// 专题列表
	public static final String L_SUBJECT_LIST = "t1";
	public static final String SEARCH_CATE_HISTORY = "1";
	public static final String SEARCH_CATE_THINK = "2";
	public static final String SEARCH_CATE_LAUNGE = "3";
	public static final String SEARCH_CATE_OTHER = "4";
	public static final String SEARCH_CATE_HOT = "5";
	public static final int DATA_ALL = 99999;

	public static HashMap<String, Integer> hasMap = new HashMap<String, Integer>();

	public static void all0Point() {
		hasMap.clear();
		hasMap.put(L_BANNER, 0);
		hasMap.put(L_BANNER_LIST, 0);
		hasMap.put(L_HOME_REC, 0);
		hasMap.put(L_HOME_LEADER, 0);
		hasMap.put(L_HOME, 0);
		hasMap.put(L_RANK, 0);
		hasMap.put(L_CATE_HOME, 0);
		hasMap.put(L_CATE_NEW, 0);
		hasMap.put(L_CATE_HOT, 0);
		hasMap.put(L_APP_UPDATE, 0);
		hasMap.put(L_APP_FAVOR, 0);
		hasMap.put(L_SETTINGS, 0);
		hasMap.put(L_SUGGEST, 0);
		hasMap.put(L_SEARCH_HIT, 0);
		hasMap.put(L_SEARCH_RS, 0);
		hasMap.put(L_DETAIL, 0);
		hasMap.put(L_SUBJECT_HOME, 0);
		hasMap.put(L_SUBJECT_LIST, 0);
		hasMap.put(L_PUSH, 0);
		hasMap.put(L_DOWN_MANAGER, 0);
	}

	public static void all1Point() {
		hasMap.clear();
	}

	public static void all0Point(String... strings) {
		hasMap.clear();
		for (int i = 1; i < strings.length; i++) {
			String str = strings[i];
			if (str != null && !hasMap.containsKey(str))
				hasMap.put(str, 0);
		}
	}

}
