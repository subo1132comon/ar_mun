package com.byt.market.data;

import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.graphics.Bitmap;

@DatabaseTable(tableName="AppDetails")
public class AppDetail implements AppItemState {

	/**
	 * 列表项图标
	 */
	public Bitmap icon;

	@DatabaseField
	// 游戏id
	public int sid;
	// 图标地址
	public String iconUrl;
	// 名称
	public String name;
	// 下载量
	public String downNum;
	
	// 应用所属类型
	public String stype;
	// 评分
	public float rating;

	// 截图地址,中间用','分隔
	public String screen;

	// 大截图地址,中间用','分隔
	public String imagePath;

	// 游戏大小格式化后的字符串
	public String strLength;
	// 游戏大小
	public long length;
	// 游戏分类名称
	public String cateName;
	// 游戏版本名称
	public String vname;
	// 游戏上线时间
	public String utime;
	// 游戏语言
	public int lang;
	// 收费类型
	public String feetype;

	// 游戏描述
	public String sdesc;
	// 小编点评
	public String adesc;
	
	// 游戏标签
	public String tag;

	// 两条评论
	public List<AppComment> comments = new ArrayList<AppComment>();

	// apk下载地址
	public String apk;
	public String hash;
	// 评价数量
	public int ccount;

	public AppItem app;
	
	public int state = STATE_IDLE;
	
	//积分应用限制
	public int creditLimit=0;
	//用于判断是否是谷歌应用0不是，1是
	public int googlemarket;
	//谷歌应用的价格
	public double googlePrice;
	public int ulevel=1;
	 public int isshare ;
	 public float ispay ;
	 public String oobpackage;
	 public int gotype=-1;
	 public int pointtype=-1;
	 
	
}
