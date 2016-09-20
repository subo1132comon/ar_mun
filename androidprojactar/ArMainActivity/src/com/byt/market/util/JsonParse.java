package com.byt.market.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.WebCommonActivity;
import com.byt.market.data.AppComment;
import com.byt.market.data.AppDetail;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.BigItem;
import com.byt.market.data.CateItem;
import com.byt.market.data.HLeaderItem;
import com.byt.market.data.HomeItemBean;
import com.byt.market.data.JoySkillItem;
import com.byt.market.data.PageInfo;
import com.byt.market.data.RingItem;
import com.byt.market.data.SearchHint;
import com.byt.market.data.SearchHistory;
import com.byt.market.data.SearchHotword;
import com.byt.market.data.SubjectItem;
import com.byt.market.data.UserData;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.PlayActivity;
import com.byt.market.mediaplayer.PlayWebVideoActivity;
import com.byt.market.mediaplayer.data.PlayDownloadItem;
import com.byt.market.mediaplayer.data.VideoItem;
import com.byt.market.mediaplayer.service.PlayDownloadService;
import com.byt.market.qiushibaike.JokeActivity;
import com.byt.market.qiushibaike.database.JokeDataDatabaseHelper;
import com.byt.market.qiushibaike.database.JokeDataDatabaseHelper.joke_feedback_list;
import com.byt.market.tools.LogCart;

public class JsonParse {

	/**
	 * 解析页信息
	 * 
	 * @param jsonPageInfo
	 * @return
	 */
	public static PageInfo parsePageInfo(JSONObject result) {
		PageInfo pageInfo = new PageInfo();
		try {
			if (result != null && !result.isNull("pinfo")) {
				JSONObject jsonPageInfo = result.getJSONObject("pinfo");
				if (!jsonPageInfo.isNull("allCount"))
					pageInfo.setRecordNum(jsonPageInfo.getInt("allCount"));
				if (!jsonPageInfo.isNull("pageSize"))
					pageInfo.setPageSize(jsonPageInfo.getInt("pageSize"));
				if (!jsonPageInfo.isNull("pageIndex"))
					pageInfo.setPageIndex(jsonPageInfo.getInt("pageIndex"));
				if (!jsonPageInfo.isNull("pageCount"))
					pageInfo.setPageNum(jsonPageInfo.getInt("pageCount"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return pageInfo;
	}

	/**
	 * 解析返回结果状态
	 * 
	 * @param jsonPageInfo
	 * @return
	 */
	public static int parseResultStatus(JSONObject jsonPageInfo) {
		try {
			if (!jsonPageInfo.isNull("resultStatus")) {
				return jsonPageInfo.getInt("resultStatus");
			}
		} catch (Exception e) {
		}

		return -1;
	}

	/**
	 * 解析返回数据总条数
	 * 
	 * @param jsonPageInfo
	 * @return
	 */
	public static int parseDataAllCount(JSONObject jsonPageInfo) {
		try {
			if (!jsonPageInfo.isNull("allCount")) {
				return jsonPageInfo.getInt("allCount");
			}
		} catch (Exception e) {
		}

		return -1;
	}

	/**
	 * 排行列表
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseRankList(JSONArray jsonArray) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();

		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				BigItem bigItem = new BigItem();
				bigItem.layoutType = BigItem.Type.LAYOUT_RANKLIST;
				AppItem app = parseAppItem(jsObject);
				app.list_id = LogModel.L_RANK;
				app.list_cateid = LogModel.P_LIST;
				bigItem.rankItems.add(app);
				bigList.add(bigItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bigList;
	}

	/**
	 * 排行列表
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseSearchList(Context context,JSONArray jsonArray,
			int pageIndex, int ps) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();
		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				BigItem bigItem = new BigItem();
				AppItem appItem = parseAppItem(jsObject);
				appItem.list_id = LogModel.L_SEARCH_RS;
				if (appItem.stype != null) {
					if (appItem.stype.contains("7")) {
						bigItem.layoutType = BigItem.Type.LAYOUT_SEARCH_SAFE;
						appItem.list_cateid = "s1";
					} else if (appItem.stype.contains("8")) {
						bigItem.layoutType = BigItem.Type.LAYOUT_SEARCH_NO_RESULT;
						appItem.list_cateid = "s3";
					} else {
						bigItem.layoutType = BigItem.Type.LAYOUT_SEARCH_RELA;
						appItem.list_cateid = "s2";
					}
				} else {
					bigItem.layoutType = BigItem.Type.LAYOUT_SEARCH_RELA;
					appItem.list_cateid = "s2";
				}
				bigItem.resultItems.add(appItem);
				bigList.add(bigItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		BigItem safeItem = new BigItem();
		BigItem relaItem = new BigItem();
		BigItem noItem = new BigItem();

		List<BigItem> saList = new ArrayList<BigItem>();
		List<BigItem> reList = new ArrayList<BigItem>();
		List<BigItem> noList = new ArrayList<BigItem>();

		for (BigItem bItem : bigList) {
			if (bItem.layoutType == BigItem.Type.LAYOUT_SEARCH_SAFE) {
				saList.add(bItem);
			} else if (bItem.layoutType == BigItem.Type.LAYOUT_SEARCH_RELA) {
				reList.add(bItem);
			} else if (bItem.layoutType == BigItem.Type.LAYOUT_SEARCH_NO_RESULT) {
				noList.add(bItem);
			}
		}

		bigList.clear();
		if (saList.size() > 0) {
			if (pageIndex == 1) {
				BigItem safetItem = new BigItem();
				safetItem.layoutType = BigItem.Type.LAYOUT_SEARCH_TITLE;
				AppItem app = new AppItem();
				app.sid = -1;
				app.name = context.getString(R.string.json_16) + saList.size() + context.getString(R.string.kuohao);
				app.rankcount = safeItem.resultItems.size();
				safetItem.resultItems.add(app);
				saList.add(0, safetItem);
			}
			bigList.addAll(saList);
		}
		if (reList.size() > 0) {
			if (pageIndex == 1) {
				BigItem relatItem = new BigItem();
				relatItem.layoutType = BigItem.Type.LAYOUT_SEARCH_TITLE;
				AppItem app = new AppItem();
				app.sid = -2;
				app.name = context.getString(R.string.json_15) + ps + context.getString(R.string.kuohao);
				app.rankcount = relaItem.resultItems.size();
				relatItem.resultItems.add(app);
				reList.add(0, relatItem);
			}
			bigList.addAll(reList);
		}
		if (noList.size() > 0) {
			if (pageIndex == 1) {
				BigItem notItem = new BigItem();
				notItem.layoutType = BigItem.Type.LAYOUT_SEARCH_TITLE;
				AppItem app = new AppItem();
				app.sid = -3;
				app.name = context.getString(R.string.json_14);
				app.rankcount = noItem.resultItems.size();
				notItem.resultItems.add(app);
				noList.add(0, notItem);
			}
			bigList.addAll(noList);
		}
		return bigList;
	}

	/**
	 * 专题列表
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseSubjectList(JSONArray jsonArray) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();

		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				BigItem bigItem = new BigItem();
				bigItem.layoutType = BigItem.Type.LAYOUT_SUBJECTLIST;
				SubjectItem parseSubjectItem = parseSubjectItem(jsObject);
//				if(parseSubjectItem != null && (parseSubjectItem.sid == 56 || parseSubjectItem.sid == 57)){
//					continue;
//				}
				bigItem.subjectItems.add(parseSubjectItem);
				bigList.add(bigItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bigList;
	}

	/**
	 * 评论列表
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseCommentList(JSONArray jsonArray) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();

		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				BigItem bigItem = new BigItem();
				bigItem.layoutType = BigItem.Type.LAYOUT_COMMENTS;
				bigItem.comments.add(parseCommentItem(jsObject));
				bigList.add(bigItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bigList;
	}
	
	public static long pareseRecRepeatTime(JSONObject result) {
		try {
			if (!result.isNull("REPEAT_TIME")) {
				return result.getLong("REPEAT_TIME");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 攻略列表
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseJoySkillList(Context context,JSONArray jsonArray) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();

		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				BigItem bigItem = new BigItem();
				bigItem.layoutType = BigItem.Type.LAYOUT_JOY_SKILL;
				bigItem.joySkillItems.add(parseJoySkillItem(jsObject));
				bigList.add(bigItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (bigList.size() > 0) {
			BigItem relatItem = new BigItem();
			relatItem.layoutType = BigItem.Type.LAYOUT_SEARCH_TITLE;
			AppItem app = new AppItem();
			app.sid = -2;
			app.name = context.getString(R.string.json_11) + bigList.size() + context.getString(R.string.kuohao);
			relatItem.resultItems.add(app);
			bigList.add(0, relatItem);
		}
		return bigList;
	}

	/**
	 * 搜索热词
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseHotwordList(Context context,JSONArray jsonArray) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();

		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				BigItem bigItem = new BigItem();
				bigItem.layoutType = BigItem.Type.LAYOUT_HOTWORDS;
				SearchHotword hotword = new SearchHotword();
				if (!jsObject.isNull("ID"))
					hotword.id = jsObject.getInt("ID");
				if (!jsObject.isNull("NAME"))
					hotword.key = jsObject.getString("NAME");
				if (!jsObject.isNull("SCOUNT")) {
					int count = jsObject.getInt("SCOUNT");
					String counts = null;
					if (count > 0 && count < 10000) {
						counts =context.getString(R.string.json_10);
					} else if (count > 10000 && count < 100000) {
						counts = context.getString(R.string.json_09);
					} else if (count > 100000 && count < 1000000) {
						counts = context.getString(R.string.json_08);
					} else if (count > 1000000 && count < 10000000) {
						counts = context.getString(R.string.json_07);
					} else if (count > 10000000 && count < 100000000) {
						counts =  context.getString(R.string.json_06);
					}
					hotword.count = counts;
				}
				bigItem.hotwords.add(hotword);
				bigList.add(bigItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bigList;
	}

	/**
	 * 搜索热词
	 * 
	 * @return
	 */
	public static List<BigItem> parseHistoryList(String rs) {
		List<BigItem> bigList = new ArrayList<BigItem>();
		String[] hislist = rs.split("_|_");
		for (String string : hislist) {
			if (string.contains("@")) {
				String[] hisitems = string.split("@");
				if (hisitems.length < 2)
					continue;
				BigItem bigItem = new BigItem();
				bigItem.layoutType = BigItem.Type.LAYOUT_HISTORY;
				SearchHistory history = new SearchHistory();
				history.id = Integer.parseInt(hisitems[0]);
				history.key = hisitems[1];
				bigItem.historys.add(history);
				bigList.add(bigItem);
			}
		}
		if (bigList.size() == 1)
			bigList.clear();
		return bigList;
	}

	/**
	 * 搜索联想词
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseThinkList(JSONArray jsonArray) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();

		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				BigItem bigItem = new BigItem();
				bigItem.layoutType = BigItem.Type.LAYOUT_THINK;
				SearchHint tHint = new SearchHint();
				if (!jsObject.isNull("CDATA"))
					tHint.app = parseAppItem(jsObject.getJSONObject("CDATA"));
				if (!jsObject.isNull("ID"))
					tHint.id = jsObject.getInt("ID");
				if (!jsObject.isNull("SNAME"))
					tHint.key = jsObject.getString("SNAME");
				bigItem.thinks.add(tHint);
				bigList.add(bigItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bigList;
	}

	/**
	 * 子列表
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseSubList(JSONArray jsonArray, String from,
			String catelist) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();

		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				BigItem bigItem = new BigItem();
				bigItem.layoutType = BigItem.Type.LAYOUT_SUBLIST;
				AppItem app = parseAppItem(jsObject);
				app.list_id = from;
				app.list_cateid = catelist;
				bigItem.subListItems.add(app);
				bigList.add(bigItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bigList;
	}
	/**
	 * 音乐列表
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseMusicSubList(JSONArray jsonArray, String from,
			String catelist) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();

		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				BigItem bigItem = new BigItem();
				bigItem.layoutType = BigItem.Type.LAYOUT_MUSICSUBLIST;
				RingItem app = parseMusicItem(jsObject);
				app.list_id = from;
				app.list_cateid = catelist;
				bigItem.ringhomeItems.add(app);
				bigList.add(bigItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bigList;
	}
	/**
	 * 应用的详情
	 * 
	 * @param jsObject
	 * @return
	 */     
	public static List<BigItem> parseDetailList(Context context,JSONObject jsObject) {
		if (jsObject == null)
			return null;
		List<BigItem> bigList = new ArrayList<BigItem>();
		try {
			BigItem bigItem = new BigItem();
			bigItem.layoutType = BigItem.Type.LAYOUT_DETAILS;
			AppDetail appItem = new AppDetail();
			if (!jsObject.isNull("SID"))
				appItem.sid = jsObject.getInt("SID");
			if (!jsObject.isNull("NAME"))
				appItem.name = jsObject.getString("NAME");
			if (!jsObject.isNull("VNAME"))
				appItem.vname = jsObject.getString("VNAME");
			if (!jsObject.isNull("STYPE"))
				appItem.stype = jsObject.getString("STYPE");
			if (!jsObject.isNull("SIZE")) {
				appItem.length = jsObject.getInt("SIZE");
				appItem.strLength = StringUtil
						.resultBitTranslate(appItem.length);
			}
			if (!jsObject.isNull("RATING"))
				appItem.rating = Util.getDrawRateVaue(jsObject
						.getDouble("RATING"));
			if (!jsObject.isNull("LOGO"))
				appItem.iconUrl = jsObject.getString("LOGO");
			if (!jsObject.isNull("CCOUNT"))
				appItem.ccount = jsObject.getInt("CCOUNT");
			if (!jsObject.isNull("APK"))
				appItem.apk = jsObject.getString("APK");
			if (!jsObject.isNull("LANG"))
				appItem.lang = jsObject.getInt("LANG");
			if (!jsObject.isNull("SCREEN"))
				appItem.screen = jsObject.getString("SCREEN");
			if (!jsObject.isNull("IMAGEPATH"))
				appItem.imagePath = jsObject.getString("IMAGEPATH");
			if (!jsObject.isNull("SDESC"))
				appItem.sdesc = jsObject.getString("SDESC");
			if (!jsObject.isNull("ADESC"))
				appItem.adesc = jsObject.getString("ADESC");
			if (!jsObject.isNull("DOWN"))
			{
				appItem.downNum = Util.formatDnum(jsObject.getInt("DOWN"), context.getString(R.string.json_12));
			}			
			if (!jsObject.isNull("HASH"))
				appItem.hash = jsObject.getString("HASH");
			if (!jsObject.isNull("CNAME"))
				appItem.cateName = jsObject.getString("CNAME");
			if (!jsObject.isNull("SDATE")) // 上线时间
				appItem.utime = Util.format2ShortDate(jsObject
						.getString("SDATE"));
			if(!jsObject.isNull("TAG")){
				appItem.tag = jsObject.getString("TAG");
			}
			if (!jsObject.isNull("CREDIT_LIMIT")) // 访问应用需要的积分
				appItem.creditLimit =jsObject.getInt("CREDIT_LIMIT");
			
			if (!jsObject.isNull("GOOGLEMARKET")) // 判断该应用是否存在谷歌商店
				appItem.googlemarket =jsObject.getInt("GOOGLEMARKET");
			
			if (!jsObject.isNull("GOOGLE_PRICE")) // 该应用在谷歌商店的价格 
				appItem.googlePrice =jsObject.getDouble("GOOGLE_PRICE");
			if (!jsObject.isNull("LEVEL")) // 该所需用户级别
				appItem.ulevel =jsObject.getInt("LEVEL");
			
			if (!jsObject.isNull("ISPAY")) 
				appItem.ispay =(float)jsObject.getDouble("ISPAY");
			if (!jsObject.isNull("ISSHARE")) 
				appItem.isshare =jsObject.getInt("ISSHARE");
			if (!jsObject.isNull("OBB_PACKAGE")) 
				appItem.oobpackage =jsObject.getString("OBB_PACKAGE");
			
			if (!jsObject.isNull("FEETYPE")) {
				String ft = jsObject.getString("FEETYPE");
				if (ft.equals("1"))
					appItem.feetype = context.getString(R.string.json_05);
				else if (ft.equals("2"))
					appItem.feetype = context.getString(R.string.json_04);
				else if (ft.equals("3"))
					appItem.feetype = context.getString(R.string.json_03);
				else if (ft.equals("4"))
					appItem.feetype = context.getString(R.string.json_02);
				else if (ft.equals("5"))
					appItem.feetype =context.getString(R.string.json_ad);
				else
					appItem.feetype = context.getString(R.string.json_01);
			}
			if (!jsObject.isNull("COMM")) {
				JSONArray jsonArray = jsObject.getJSONArray("COMM");
				if (jsonArray != null && jsonArray.length() > 0) {
					List<AppComment> listComments = new ArrayList<AppComment>();
					int size = jsonArray.length();
					for (int i = 0; i < size; i++) {
						JSONObject jsonObject = (JSONObject) jsonArray.get(i);
						listComments.add(parseCommentItem(jsonObject));
					}
					appItem.comments = listComments;
				}
			}
			if (!jsObject.isNull("app")) // 上线时间
				appItem.app = parseAppItem(jsObject.getJSONObject("app"));
			
			
			bigItem.details.add(appItem);
			bigList.add(bigItem);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bigList;
	}

	/**
	 * 解析首页数据
	 * 
	 * @param result
	 * @param homeperoid
	 * @return
	 * @throws Exception
	 */
	public static List<BigItem> parseHomeList(JSONObject result,
			int homeperoid, int pi) throws Exception {
		List<BigItem> lItems = new ArrayList<BigItem>();
		if (pi == 1 && !result.isNull("hlist")) {
			JSONArray baArray = result.getJSONArray("hlist");
			lItems.addAll(parseList(baArray, 5, 0));
		}
		if (pi == 1 && !result.isNull("hload")) {
			JSONArray leArray = result.getJSONArray("hload");
			lItems.addAll(parseList(leArray, 3, 2));
		}
//		if (pi == 1 && !result.isNull("rsoft")) {
//			JSONArray trArray = result.getJSONArray("rsoft");
//			lItems.addAll(parseList(trArray, 1, 0));
//		}
		if (!result.isNull("mlist")) {
			JSONArray hoArray = result.getJSONArray("mlist");
//			//ArrayList<BigItem> b = (ArrayList<BigItem>) parseList(hoArray, 1, 1);
			ArrayList<BigItem> b = (ArrayList<BigItem>) parseList(hoArray);
			Log.d("test", "length---"+b.size());
			lItems.addAll(b);
//			Log.d("test", "susu--"+b.get(0).homeItembeas.get(0).abumDes);
//			lItems.addAll(b);
		}
		return lItems;
	}
	/**
	 * 解析弹出推荐页数据
	 * @param result
	 * @param homeperoid
	 * @param pi
	 * @return
	 * @throws Exception
	 */
	public static List<BigItem> parseStartHomeList(JSONObject result,int homeperoid, int pi) throws Exception {
		List<BigItem> lItems = new ArrayList<BigItem>();
		if (!result.isNull("mlist")) {
			JSONArray hoArray = result.getJSONArray("mlist");
			lItems.addAll(parseList(hoArray, homeperoid, 1));
		}
		return lItems;
	}
	
	public static List<BigItem> parseHomeList(JSONArray result,
			int homeperoid,int pi)throws Exception{
		List<BigItem> lItems = new ArrayList<BigItem>();
		lItems.addAll(parseList(result, homeperoid, 1));
		return lItems;
	}

	
	/**
	 * 解析
	 * 首页 三行列表数据
	 * 3 ： 每个item3行
	 */
	public static List<BigItem> parseList(JSONArray jsonArray){
		int size = jsonArray.length();
		LogCart.Log("size--head---"+size);
		List<BigItem> bigList = new ArrayList<BigItem>();
		int mCount = size % 3 == 0 ? size / 3 : size / 3 + 1;
		for(int i=1;i<mCount+1;i++){
			try {
				BigItem bigItem = new BigItem();
				bigItem.layoutType = BigItem.Type.LAYOUT_APPLIST_THREE;
				
				int jcunt = 3*i;
				if(jcunt>size){
					jcunt = size;
				}
					for(int j=i*3-3;j<jcunt;j++){
						JSONObject obj = jsonArray.getJSONObject(j);
						bigItem.homeItembeas.add(getjsonItembean(obj));
					}
					bigList.add(bigItem);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return bigList;
	}
	/**
	 * 解析item
	 * 
	 * @throws JSONException 
	 * 
	 */
	public static HomeItemBean  getjsonItembean(JSONObject jsObject) throws JSONException{
		HomeItemBean appItem = new HomeItemBean();
		
		if (!jsObject.isNull("ALBUMID"))
			appItem.albumId = jsObject.getInt("ALBUMID");
		if (!jsObject.isNull("ALBUMICON"))
			appItem.ic_url = jsObject.getString("ALBUMICON");
		if (!jsObject.isNull("ALBUMNAME"))
			appItem.albumName = jsObject.getString("ALBUMNAME");
		if (!jsObject.isNull("ALBUMCDESC"))
			appItem.abumDes = jsObject.getString("ALBUMCDESC");
		if (!jsObject.isNull("TYPE"))
			appItem.type = jsObject.getString("TYPE");
		if (!jsObject.isNull("MUSICID"))
			appItem.contentId = jsObject.getString("MUSICID");
		if (!jsObject.isNull("MUSICNAME"))
			appItem.contentName = jsObject.getString("MUSICNAME");
		return appItem;
	}
	/**
	 * 根据列表布局不同解析数据列表
	 * 
	 * @param jsonArray
	 * @param peroid
	 * @param style
	 * @return
	 */
	public static List<BigItem> parseList(JSONArray jsonArray, int peroid,
			int style) {
		int size = jsonArray.length();
		LogCart.Log("size--head---"+size);
		List<BigItem> bigList = new ArrayList<BigItem>();
		int mCount = size % peroid == 0 ? size / peroid : size / peroid + 1;
		for (int i = 0; i < mCount; i++) {
			BigItem bigItem = new BigItem();
			switch (peroid) {
			case 1:
				if (style == 1) {
					bigItem.layoutType = BigItem.Type.LAYOUT_APPLIST_THREE;
					int sc=0;
					for (int j = 0; j < peroid; j++) {
						int w = i * peroid + j;
						if (w < size) {
							try {
								//------add  by   bobo
//								if(w == 8){
//									bigItem.layoutType = 33;
//								}
								//-end
								JSONObject jsObject = (JSONObject) jsonArray
										.get(w);
								AppItem app = parseAppItem(jsObject);
								//HomeItemBean app = getjsonItembean(jsObject);
								app.list_id = LogModel.L_HOME;
								app.list_cateid = LogModel.P_LIST;
								bigItem.homeItems.add(app);
								//bigItem.homeItembeas.add(app);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						sc++;
					}
				}else{
					bigItem.layoutType = BigItem.Type.LAYOUT_APPLIST_ONE;
					for (int j = 0; j < peroid; j++) {
						int w = i * peroid + j;
						if (w < size) {
							try {
								JSONObject jsObject = (JSONObject) jsonArray.get(w);
								AppItem app = parseAppItem(jsObject);
								app.list_id = LogModel.L_HOME_REC;
								app.list_cateid = LogModel.P_LIST;
								bigItem.recItems.add(app);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				break;
			case 2:
				bigItem.layoutType = BigItem.Type.LAYOUT_APPLIST_TWO;
				for (int j = 0; j < peroid; j++) {
					int w = i * peroid + j;
					if (w < size) {
						try {
							JSONObject jsObject = (JSONObject) jsonArray.get(w);
							AppItem app = parseAppItem(jsObject);
							app.list_id = LogModel.L_HOME;
							app.list_cateid = LogModel.P_LIST;
							bigItem.homeItems.add(app);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				break;
			case 3:
				if (style == 1) {
//					bigItem.layoutType = BigItem.Type.LAYOUT_APPLIST_THREE;
//					for (int j = 0; j < 1; j++) {
//						int w = i * 1 + j;
//						if (w < size) {
//							try {
//								JSONObject jsObject = (JSONObject) jsonArray
//										.get(w);
//								AppItem app = parseAppItem(jsObject);
//								app.list_id = LogModel.L_HOME;
//								app.list_cateid = LogModel.P_LIST;
//								bigItem.homeItems.add(app);
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					}
					
//					for(int j = 0 ; j < size; j++){
//						try {
//							JSONObject jsObject = (JSONObject) jsonArray
//									.get(j);
//							AppItem app = parseAppItem(jsObject);
//							app.list_id = LogModel.L_HOME;
//							app.list_cateid = LogModel.P_LIST;
//							bigItem.homeItems.add(app);
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
					
				} else if (style == 2) {
					bigItem.layoutType = BigItem.Type.LAYOUT_LEADER_THREE;
					for (int j = 0; j < 4; j++) {
						int w = i * 4 + j;
						if (w < size) {
							try {
								JSONObject jsObject = (JSONObject) jsonArray
										.get(w);
								bigItem.leaderItems
										.add(parseLeaderItem(jsObject));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				break;
			case 5:
				bigItem.layoutType = BigItem.Type.LAYOUT_APPLIST_MUTILE;
				for (int j = 0; j <10; j++) {
					int w = i * peroid + j;
					if (w < size) {
						try {
							JSONObject jsObject = (JSONObject) jsonArray.get(w);
							AppItem app = parseAppItemHome(jsObject);
							app.list_id = LogModel.L_BANNER;
							app.list_cateid = LogModel.P_LIST;
							if(app.gotype==1)
							{
							bigItem.homeItems.add(app);
							}else if(app.gotype==2)
							{
								bigItem.homeADItems.add(app);
							}
						} catch (Exception e) {
							e.printStackTrace();
							LogCart.Log("eeeeee-----"+e);
						}
					}
				}
				break;
			}
			bigList.add(bigItem);
			if (peroid == 5 ||(peroid == 3 && style == 2))
				break;
		}
		return bigList;
	}

	/**
	 * 解析游戏对象
	 * 
	 * @param jsObject
	 * @return
	 * @throws JSONException
	 */
	public static AppItem parseAppItemHome(JSONObject jsObject)
			throws JSONException {
		AppItem appItem = new AppItem();
		if (!jsObject.isNull("ADTYPE")){
			appItem.pointtype= jsObject.getInt("ADTYPE");
			  String sr = jsObject.getString("ADTYPE");
			LogCart.Log("解析 types--"+sr);
		}
		if (!jsObject.isNull("PTYPE")){
			appItem.gotype= jsObject.getInt("PTYPE");
			LogCart.Log("Json--ADTYPE");
		}
//		if(!jsObject.isNull("MARK")){
//			appItem.mark = jsObject.getInt("MARK");
//			int i = appItem.mark;
//			LogCart.Log("Json--5--mark"+i);
//		}
		switch(appItem.pointtype){
		case 1://糗百		
			if (!jsObject.isNull("CONTENT"))
				appItem.adesc = jsObject.getString("CONTENT");
			if (!jsObject.isNull("LOGO"))
				appItem.iconUrl = jsObject.getString("LOGO");
			break;
		case 2://视频
			if (!jsObject.isNull("ID"))
				appItem.cateid = jsObject.getInt("ID");
			if (!jsObject.isNull("NAME"))
				appItem.name = jsObject.getString("NAME");
			if (!jsObject.isNull("LOGO"))
				appItem.iconUrl= jsObject.getString("LOGO");
				if (!jsObject.isNull("CDESC"))
					appItem.adesc = jsObject.getString("CDESC");
			if (!jsObject.isNull("SID"))
				appItem.sid = jsObject.getInt("SID");
			if (!jsObject.isNull("HASH"))
				appItem.hash = jsObject.getString("HASH");
			if (!jsObject.isNull("VIDEURI"))
				appItem.cateName = jsObject.getString("VIDEURI")==null?"":jsObject.getString("VIDEURI");
			if (!jsObject.isNull("SIZE"))//文件大小
				{
				appItem.length = jsObject.getInt("SIZE");
				appItem.strLength = StringUtil.resultBitTranslate(appItem.length);
			}
			if (!jsObject.isNull("ACTORS"))
				appItem.list_cateid = jsObject.getString("ACTORS");
			if (!jsObject.isNull("AREA"))
				appItem.oobpackage = jsObject.getString("AREA");
			if (!jsObject.isNull("DIRECTORS"))
				appItem.pname = jsObject.getString("DIRECTORS");
			if (!jsObject.isNull("PUBLISH_TIME"))
			{
				String tmp=jsObject.getString("PUBLISH_TIME");
				 appItem.stype = tmp;
				
			}
			if (!jsObject.isNull("DOWN"))
			{
				int down=jsObject.getInt("DOWN");
				
				appItem.downNum =Util.formatVideoDnum(down);
			}
			if (!jsObject.isNull("ISPAY")) 
				appItem.ispay =(float)jsObject.getDouble("ISPAY");
			if (!jsObject.isNull("ISSHARE")) 
				appItem.isshare=jsObject.getInt("ISSHARE");
			if (!jsObject.isNull("URL"))
				appItem.updatetime= jsObject.getString("URL");

			break;
		case 3://专题
			if (!jsObject.isNull("ID"))
				appItem.sid = jsObject.getInt("ID");
			if (!jsObject.isNull("CTITLE"))
				appItem.name = jsObject.getString("CTITLE");
			if (!jsObject.isNull("SDATE"))
				appItem.updatetime = Util.format2ShortDate(jsObject
						.getString("SDATE"));
			if (!jsObject.isNull("CDESC"))
				appItem.adesc = jsObject.getString("CDESC");
			if (!jsObject.isNull("SOFTCOUNT"))
				appItem.commcount = jsObject.getInt("SOFTCOUNT");
			if (!jsObject.isNull("CCOUNT"))
				appItem.rankcount = jsObject.getInt("CCOUNT");
			if (!jsObject.isNull("LOGO"))
				appItem.iconUrl = jsObject.getString("LOGO");
			if (!jsObject.isNull("CREDIT_LIMIT"))
				appItem.creditLimit = jsObject.getInt("CREDIT_LIMIT");
			if (!jsObject.isNull("LEVEL"))
			{
				appItem.ulevel = jsObject.getInt("LEVEL");//用户级别
			}
			if (!jsObject.isNull("ISSHARE"))
				appItem.isshare =jsObject.getInt("ISSHARE");
			if (!jsObject.isNull("ISPAY")){
				appItem.ispay =(float)jsObject.getDouble("ISPAY");
			}
			if (!jsObject.isNull("FEEID")){
				appItem.vcode =jsObject.getInt("FEEID");
			}	
			break;
		case 4://url
			if (!jsObject.isNull("LOGO"))
				appItem.iconUrl = jsObject.getString("LOGO");
			if (!jsObject.isNull("URL")) 
				appItem.adesc=jsObject.getString("URL");
			//intent.putExtra("videoUrl", appItem.adesc);
//			if(!jsObject.isNull("MARK")){
//				appItem.mark = jsObject.getInt("MARK");
//				int i = appItem.mark;
//				LogCart.Log("Json--4--mark"+i);
//			}
			break;
		case 5://app
			if(!jsObject.isNull("MARK")){
				//appItem.mark = jsObject.getString("MARK");
				appItem.mark = jsObject.getInt("MARK");
				//String sr = jsObject.getString("MARK");
				//int in = Integer.parseInt(sr);
				//appItem.mark = in;
				//LogCart.Log("Json--5--mark"+in);
			}
			
			
			if (!jsObject.isNull("SID"))
				appItem.sid = jsObject.getInt("SID");
			if (!jsObject.isNull("VNAME"))
				appItem.vname = jsObject.getString("VNAME");
			if (!jsObject.isNull("VCODE"))
				appItem.vcode = jsObject.getInt("VCODE");
			if (!jsObject.isNull("STYPE"))
				appItem.stype = jsObject.getString("STYPE");
			if (!jsObject.isNull("NAME"))
				appItem.name = jsObject.getString("NAME");
			if (!jsObject.isNull("CATEID"))
				appItem.cateid = jsObject.getInt("CATEID");
			if (!jsObject.isNull("PNAME"))
				appItem.pname = jsObject.getString("PNAME");
			if (!jsObject.isNull("SIZE")) {
				appItem.length = jsObject.getInt("SIZE");
				appItem.strLength = StringUtil.resultBitTranslate(appItem.length);
			}
			if (!jsObject.isNull("RATING"))
				appItem.rating = Util
						.getFloatRateVaue(jsObject.getDouble("RATING"));
			if (!jsObject.isNull("LOGO"))
				appItem.iconUrl = jsObject.getString("LOGO");
			if (!jsObject.isNull("APK"))
				appItem.apk = jsObject.getString("APK");
			if (!jsObject.isNull("ADESC"))
				appItem.adesc = jsObject.getString("ADESC");
			if (!jsObject.isNull("DOWN"))
			{
				appItem.downNum = Util.formatDnum(jsObject.getInt("DOWN"), MyApplication.getInstance().getResources().getString(R.string.downhistory));
			}	
			if (!jsObject.isNull("HASH"))
				appItem.hash = jsObject.getString("HASH");
			if (!jsObject.isNull("CNAME"))
				appItem.cateName = jsObject.getString("CNAME");
			if (!jsObject.isNull("RSTATUS")) // 排行
				appItem.rankcount = jsObject.getInt("RSTATUS");
			if (!jsObject.isNull("CCOUNT")) // 评论条数
				appItem.commcount = jsObject.getInt("CCOUNT");
			if (!jsObject.isNull("SDATE")) // 上线时间
				appItem.updatetime = Util.format2ShortDate(jsObject
						.getString("SDATE"));
			
			if (!jsObject.isNull("CREDIT_LIMIT")) // 访问应用需要的积分
				appItem.creditLimit =jsObject.getInt("CREDIT_LIMIT");
			
			if (!jsObject.isNull("GOOGLEMARKET")) // 判断该应用是否存在谷歌商店
				appItem.googlemarket =jsObject.getInt("GOOGLEMARKET");
			
			if (!jsObject.isNull("GOOGLE_PRICE")) // 该应用在谷歌商店的价格 
				appItem.googlePrice =jsObject.getDouble("GOOGLE_PRICE");
			if (!jsObject.isNull("LEVEL")) // 该应用在谷歌商店的价格 
				appItem.ulevel =jsObject.getInt("LEVEL");
			
			if (!jsObject.isNull("ISSHARE"))
				appItem.isshare =jsObject.getInt("ISSHARE");
			if (!jsObject.isNull("ISPAY"))
				appItem.ispay =(float)jsObject.getDouble("ISPAY");
			if (!jsObject.isNull("OBB_PACKAGE")) 
				appItem.oobpackage =jsObject.getString("OBB_PACKAGE");
			break;
		case 6://url
			if (!jsObject.isNull("LOGO"))
				appItem.iconUrl = jsObject.getString("LOGO");
			if (!jsObject.isNull("ID")) 
				appItem.musicRid=jsObject.getInt("ID");
			if(!jsObject.isNull("NAME")){
				appItem.cTitle=jsObject.getString("NAME");
			}
			if(!jsObject.isNull("CDESC")){
				appItem.cDesc=jsObject.getString("CDESC");
			}
			if(!jsObject.isNull("MARK")){
				appItem.markiconURL = jsObject.getString("MARK");
			}
			break;
		case 7://url
			if (!jsObject.isNull("LOGO"))
				appItem.iconUrl = jsObject.getString("LOGO");
			if (!jsObject.isNull("ID")) 
				appItem.musicRid=jsObject.getInt("ID");
			if(!jsObject.isNull("NAME")){
				appItem.cTitle=jsObject.getString("NAME");
			}
			if(!jsObject.isNull("CDESC")){
				appItem.cDesc=jsObject.getString("CDESC");
			}
			if(!jsObject.isNull("MARK")){
				appItem.markiconURL = jsObject.getString("MARK");
			}
			break;
		case 8://url
			if (!jsObject.isNull("LOGO"))
				appItem.iconUrl = jsObject.getString("LOGO");
			if (!jsObject.isNull("ID")) 
				appItem.musicRid=jsObject.getInt("ID");
			if(!jsObject.isNull("NAME")){
				appItem.cTitle=jsObject.getString("NAME");
			}
			if(!jsObject.isNull("CDESC")){
				appItem.cDesc=jsObject.getString("CDESC");
			}
			if(!jsObject.isNull("MARK")){
				appItem.markiconURL = jsObject.getString("MARK");
			}
			break;
		default:
			break;
		
		}		
		return appItem;
	}
	public static AppItem parseAppItem(JSONObject jsObject)
			throws JSONException {
		AppItem appItem = new AppItem();
		if (!jsObject.isNull("SID"))
			appItem.sid = jsObject.getInt("SID");
		if (!jsObject.isNull("VNAME"))
			appItem.vname = jsObject.getString("VNAME");
		if (!jsObject.isNull("VCODE"))
			appItem.vcode = jsObject.getInt("VCODE");
		if (!jsObject.isNull("STYPE"))
			appItem.stype = jsObject.getString("STYPE");
		if (!jsObject.isNull("NAME"))
			appItem.name = jsObject.getString("NAME");
		if (!jsObject.isNull("CATEID"))
			appItem.cateid = jsObject.getInt("CATEID");
		if (!jsObject.isNull("PNAME"))
			appItem.pname = jsObject.getString("PNAME");
		if (!jsObject.isNull("SIZE")) {
			appItem.length = jsObject.getInt("SIZE");
			appItem.strLength = StringUtil.resultBitTranslate(appItem.length);
		}
		if (!jsObject.isNull("RATING"))
			appItem.rating = Util
					.getFloatRateVaue(jsObject.getDouble("RATING"));
		if (!jsObject.isNull("LOGO"))
			appItem.iconUrl = jsObject.getString("LOGO");
		if (!jsObject.isNull("APK"))
			appItem.apk = jsObject.getString("APK");
		if (!jsObject.isNull("ADESC"))
			appItem.adesc = jsObject.getString("ADESC");
		if (!jsObject.isNull("DOWN"))
		{
			appItem.downNum = Util.formatDnum(jsObject.getInt("DOWN"), MyApplication.getInstance().getResources().getString(R.string.downhistory));
		}	
		if (!jsObject.isNull("HASH"))
			appItem.hash = jsObject.getString("HASH");
		if (!jsObject.isNull("CNAME"))
			appItem.cateName = jsObject.getString("CNAME");
		if (!jsObject.isNull("RSTATUS")) // 排行
			appItem.rankcount = jsObject.getInt("RSTATUS");
		if (!jsObject.isNull("CCOUNT")) // 评论条数
			appItem.commcount = jsObject.getInt("CCOUNT");
		if (!jsObject.isNull("SDATE")) // 上线时间
			appItem.updatetime = Util.format2ShortDate(jsObject
					.getString("SDATE"));
		
		if (!jsObject.isNull("CREDIT_LIMIT")) // 访问应用需要的积分
			appItem.creditLimit =jsObject.getInt("CREDIT_LIMIT");
		
		if (!jsObject.isNull("GOOGLEMARKET")) // 判断该应用是否存在谷歌商店
			appItem.googlemarket =jsObject.getInt("GOOGLEMARKET");
		
		if (!jsObject.isNull("GOOGLE_PRICE")) // 该应用在谷歌商店的价格 
			appItem.googlePrice =jsObject.getDouble("GOOGLE_PRICE");
		if (!jsObject.isNull("LEVEL")) // 该应用在谷歌商店的价格 
			appItem.ulevel =jsObject.getInt("LEVEL");
		
		if (!jsObject.isNull("ISSHARE"))
			appItem.isshare =jsObject.getInt("ISSHARE");
		if (!jsObject.isNull("ISPAY"))
			appItem.ispay =(float)jsObject.getDouble("ISPAY");
		if (!jsObject.isNull("OBB_PACKAGE")) 
			appItem.oobpackage =jsObject.getString("OBB_PACKAGE");
		return appItem;
	}
	public static RingItem parseMusicItem(JSONObject jsObject)
			throws JSONException {
		RingItem appItem = new RingItem();
		if (!jsObject.isNull("SID"))
			appItem.sid = jsObject.getInt("SID");
		if (!jsObject.isNull("AUTHOR"))//作者
			appItem.user = jsObject.getString("AUTHOR");
		if (!jsObject.isNull("SPENAME"))//专辑名
			appItem.spename = jsObject.getString("SPENAME");
		if (!jsObject.isNull("NAME"))//歌名
			appItem.name = jsObject.getString("NAME");
		if(!jsObject.isNull("ISSHARE"))
			appItem.share = jsObject.getString("ISSHARE");
		if (!jsObject.isNull("MUSICURI"))//音乐文件下载地址
			appItem.musicuri = jsObject.getString("MUSICURI");
		if (!jsObject.isNull("SIZE"))//文件大小
			{
			appItem.length = jsObject.getInt("SIZE");
			appItem.strLength = StringUtil.resultBitTranslate(appItem.length);
		}
		if (!jsObject.isNull("LOGO"))
			appItem.iconUrl = jsObject.getString("LOGO");
		if (!jsObject.isNull("ADESC"))
			appItem.adesc = jsObject.getString("ADESC");
		if (!jsObject.isNull("DOWN"))
		{
			appItem.downNum = Util.formatDnum(jsObject.getInt("DOWN"), MyApplication.getInstance().getResources().getString(R.string.downhistory));
		}	
		if (!jsObject.isNull("HASH"))
			appItem.hash = jsObject.getString("HASH");
		if (!jsObject.isNull("RSTATUS")) // 排行
			appItem.rankcount = jsObject.getInt("RSTATUS");	
		if (!jsObject.isNull("LOGO")) // 排行
			appItem.logo = jsObject.getString("LOGO");			
		return appItem;
	}
	/**
	 * 解析banner对象
	 * 
	 * @param jsObject
	 * @return
	 * @throws JSONException
	 */
	/*
	 * private static HBannerItem parseBannerItem(JSONObject jsObject) throws
	 * JSONException { HBannerItem appItem = new HBannerItem(); if
	 * (!jsObject.isNull("SID")) appItem.sid = jsObject.getInt("SID"); if
	 * (!jsObject.isNull("NAME")) appItem.name = jsObject.getString("NAME"); if
	 * (!jsObject.isNull("CATEID")) appItem.cid = jsObject.getInt("CATEID"); if
	 * (!jsObject.isNull("LOGO")) appItem.iconUrl = jsObject.getString("LOGO");
	 * return appItem; }
	 */

	/**
	 * 解析leader对象
	 * 
	 * @param jsObject
	 * @return
	 * @throws JSONException
	 */
	private static HLeaderItem parseLeaderItem(JSONObject jsObject)
			throws JSONException {
		HLeaderItem appItem = new HLeaderItem();
		if (!jsObject.isNull("ID"))
			appItem.sid = jsObject.getInt("ID");
		if (!jsObject.isNull("NAME"))
			appItem.name = jsObject.getString("NAME");
		if (!jsObject.isNull("STYPE"))
			appItem.stype = jsObject.getInt("STYPE");
		if (!jsObject.isNull("ICON"))
			appItem.iconUrl = jsObject.getString("ICON");
		return appItem;
	}

	/**
	 * 解析评论对象
	 * 
	 * @param jsObject
	 * @return
	 * @throws JSONException
	 */
	private static AppComment parseCommentItem(JSONObject jsObject)
			throws JSONException {
		AppComment cateItem = new AppComment();
		if (!jsObject.isNull("ID"))
			cateItem.sid = jsObject.getInt("ID");
		if (!jsObject.isNull("NICKNAME"))
			cateItem.name = jsObject.getString("NICKNAME");
		if (!jsObject.isNull("PMODEL"))
			cateItem.modle = jsObject.getString("PMODEL");
		if (!jsObject.isNull("RATING"))
			cateItem.rating = Util
					.getDrawRateVaue(jsObject.getDouble("RATING"));
		if (!jsObject.isNull("VNAME"))
			cateItem.vname = jsObject.getString("VNAME");
		if (!jsObject.isNull("SDATE"))
			cateItem.comtime = Util.dateFormat(jsObject.getString("SDATE"));
		if (!jsObject.isNull("CONTENT"))
			cateItem.sdesc = jsObject.getString("CONTENT");
		if (!jsObject.isNull("ICON"))
			cateItem.iconUrl = jsObject.getString("ICON");
		
//		if (!jsObject.isNull("id"))
//			cateItem.sid = jsObject.getInt("id");
//		if (!jsObject.isNull("nickname"))
//			cateItem.name = jsObject.getString("nickname");
//		if (!jsObject.isNull("pmodel"))
//			cateItem.modle = jsObject.getString("pmodel");
//		if (!jsObject.isNull("rating"))
//			cateItem.rating = Util
//					.getDrawRateVaue(jsObject.getDouble("rating"));
//		if (!jsObject.isNull("vname"))
//			cateItem.vname = jsObject.getString("vname");
//		if (!jsObject.isNull("sdate"))
//			cateItem.comtime = Util.dateFormat(jsObject.getString("sdate"));
//		if (!jsObject.isNull("content"))
//			cateItem.sdesc = jsObject.getString("content");
//		if (!jsObject.isNull("icon"))
//			cateItem.iconUrl = jsObject.getString("icon");
		return cateItem;
	}

	/**
	 * 解析专题对象
	 * 
	 * @param jsObject
	 * @return
	 * @throws JSONException
	 */
	private static SubjectItem parseSubjectItem(JSONObject jsObject)
			throws JSONException {
		SubjectItem cateItem = new SubjectItem();
		if (!jsObject.isNull("ID"))
			cateItem.sid = jsObject.getInt("ID");
		if (!jsObject.isNull("CTITLE"))
			cateItem.name = jsObject.getString("CTITLE");
		if (!jsObject.isNull("SDATE"))
			cateItem.updatetTime = Util.format2ShortDate(jsObject
					.getString("SDATE"));
		if (!jsObject.isNull("CDESC"))
			cateItem.adesc = jsObject.getString("CDESC");
		if (!jsObject.isNull("SOFTCOUNT"))
			cateItem.count = jsObject.getInt("SOFTCOUNT");
		if (!jsObject.isNull("CCOUNT"))
			cateItem.visitCount = jsObject.getInt("CCOUNT");
		if (!jsObject.isNull("IMAGEPATH"))
			cateItem.iconUrl = jsObject.getString("IMAGEPATH");
		if (!jsObject.isNull("CREDIT_LIMIT"))
			cateItem.creditLimit = jsObject.getInt("CREDIT_LIMIT");
		if (!jsObject.isNull("LEVEL"))
		{
			cateItem.ulevel = jsObject.getInt("LEVEL");//用户级别
		}
		if (!jsObject.isNull("ISSHARE"))
			cateItem.isshare =jsObject.getInt("ISSHARE");
		if (!jsObject.isNull("ISPAY")){
			cateItem.ispay =(float)jsObject.getDouble("ISPAY");
		//	LogCart.Log("ispaly------------"+(float)jsObject.getDouble("ISPAY"));
		}
		if(!jsObject.isNull("ISPAY_DAY")){
			cateItem.ispayday =(float)jsObject.getDouble("ISPAY_DAY");
			LogCart.Log("ispaly-----n-------"+(float)jsObject.getDouble("ISPAY_DAY"));
		}
		if (!jsObject.isNull("FEEID")){
			cateItem.feeID =jsObject.getInt("FEEID");
		}
		if(!jsObject.isNull("FEEID_DAY")){
			cateItem.feeid_day = jsObject.getInt("FEEID_DAY");
		}
		if(!jsObject.isNull("PRODID")){
			cateItem.priodid = jsObject.getInt("PRODID");
		}
//		if(jsObject.isNull("PRICE_DAY")){
//			cateItem.ispayday = jsObject.getInt("PRICE_DAY");
//		}
		return cateItem;
	}

	/**
	 * 解析攻略对象
	 * 
	 * @param jsObject
	 * @return
	 * @throws JSONException
	 */
	private static JoySkillItem parseJoySkillItem(JSONObject jsObject)
			throws JSONException {
		JoySkillItem cateItem = new JoySkillItem();
		if (!jsObject.isNull("PNAME"))
			cateItem.pname = jsObject.getString("PNAME");
		if (!jsObject.isNull("NAME"))
			cateItem.name = jsObject.getString("NAME");
		if (!jsObject.isNull("STATE"))
			cateItem.state = Util.format2ShortDate(jsObject.getString("STATE"));
		if (!jsObject.isNull("ICON"))
			cateItem.iconUrl = jsObject.getString("ICON");
		if (!jsObject.isNull("WURL"))
			cateItem.wUrl = jsObject.getString("WURL");
		if (!jsObject.isNull("ADESC"))
			cateItem.adEsc = jsObject.getString("ADESC");
		if (!jsObject.isNull("VCOUNT"))
			cateItem.vCount = jsObject.getString("VCOUNT");
		return cateItem;
	}
	public static List<BigItem> parseCateMusicRankList(JSONArray jsonArray) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();
		List<CateItem> apps = new ArrayList<CateItem>();
		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				CateItem cateItem = new CateItem();
				if (!jsObject.isNull("ID"))
					cateItem.id = jsObject.getInt("ID");
				if (!jsObject.isNull("CTITLE"))
					cateItem.cTitle = jsObject.getString("CTITLE");
				if (!jsObject.isNull("IMAGEPATH"))
					cateItem.ImagePath = jsObject.getString("IMAGEPATH");
				if (!jsObject.isNull("SOFTCOUNT"))
					cateItem.softCount = jsObject.getInt("SOFTCOUNT");
				if (!jsObject.isNull("CDESC"))
					cateItem.cDesc = jsObject.getString("CDESC");
				if (!jsObject.isNull("SID"))
					cateItem.sid = jsObject.getInt("SID");
				if (!jsObject.isNull("SNAME"))
					cateItem.sName = jsObject.getString("SNAME");
				if (!jsObject.isNull("ISSHARE")) 
					cateItem.isshare =jsObject.getInt("ISSHARE");
				apps.add(cateItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (int j = 0; j < apps.size(); j++) {
			BigItem item = new BigItem();
			item.layoutType = BigItem.Type.LAYOUT_CAEGORYLIST;
			item.cateItems.add(apps.get(j));
			bigList.add(item);
		}
		
		return bigList;
	}
	
	/**
	 * 解析分类对象
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseCateList(JSONArray jsonArray) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();
		List<CateItem> apps = new ArrayList<CateItem>();
		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				CateItem cateItem = new CateItem();
				if (!jsObject.isNull("ID"))
					cateItem.id = jsObject.getInt("ID");
				if (!jsObject.isNull("CTITLE"))
					cateItem.cTitle = jsObject.getString("CTITLE");
				if (!jsObject.isNull("IMAGEPATH"))
					cateItem.ImagePath = jsObject.getString("IMAGEPATH");
				if (!jsObject.isNull("SOFTCOUNT"))
					cateItem.softCount = jsObject.getInt("SOFTCOUNT");
				if (!jsObject.isNull("CDESC"))
					cateItem.cDesc = jsObject.getString("CDESC");
				if (!jsObject.isNull("SID"))
					cateItem.sid = jsObject.getInt("SID");
				if (!jsObject.isNull("SNAME"))
					cateItem.sName = jsObject.getString("SNAME");
				if (!jsObject.isNull("ISSHARE")) 
					cateItem.isshare =jsObject.getInt("ISSHARE");
				apps.add(cateItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (int j = 0; j < apps.size(); j++) {
			BigItem item = new BigItem();
			item.layoutType = BigItem.Type.LAYOUT_CAEGORYLIST;
			item.cateItems.add(apps.get(j++));
			if (j < apps.size()) {
				item.cateItems.add(apps.get(j));
			}
			bigList.add(item);
		}
		
		return bigList;
	}
	/**
	 * 解析分类对象
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseCateMusicLandList(JSONArray jsonArray) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();
		List<CateItem> apps = new ArrayList<CateItem>();
		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				CateItem cateItem = new CateItem();
				if (!jsObject.isNull("ID"))
					cateItem.id = jsObject.getInt("ID");
				if (!jsObject.isNull("CTITLE"))
					cateItem.cTitle = jsObject.getString("CTITLE");
				if (!jsObject.isNull("IMAGEPATH"))
					cateItem.ImagePath = jsObject.getString("IMAGEPATH");
				if (!jsObject.isNull("SOFTCOUNT"))
					cateItem.softCount = jsObject.getInt("SOFTCOUNT");
				if (!jsObject.isNull("CDESC"))
					cateItem.cDesc = jsObject.getString("CDESC");
				if (!jsObject.isNull("SID"))
					cateItem.sid = jsObject.getInt("SID");
				if (!jsObject.isNull("SNAME"))
					cateItem.sName = jsObject.getString("SNAME");
				if (!jsObject.isNull("ISSHARE")) 
					cateItem.isshare =jsObject.getInt("ISSHARE");
				apps.add(cateItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (int j = 0; j < apps.size(); j++) {	
			BigItem item = new BigItem();
			item.layoutType = BigItem.Type.LAYOUT_CAEGORYLIST;
			item.cateItems.add(apps.get(j++));
			if (j < apps.size()) {
				item.cateItems.add(apps.get(j));
			}
			bigList.add(item);
		}
		
		return bigList;
	}
	public static List<BigItem> parseCateVideoList(JSONObject jsonobj,BigItem lastbigitem) {
		List<BigItem> bigList = new ArrayList<BigItem>();
		try {			
			JSONArray headjsa=null;
			if(!jsonobj.isNull("hlist"))
			headjsa=jsonobj.getJSONArray("hlist");
		JSONArray infojsa=jsonobj.getJSONArray("mlist");
		
		if(headjsa!=null)
		{

			List<VideoItem> headapps = new ArrayList<VideoItem>();
			for (int i = 0; i < headjsa.length(); i++) {
				try {
					JSONObject jsObject = (JSONObject) headjsa.get(i);
					VideoItem cateItem = new VideoItem();
					if (!jsObject.isNull("ID"))
						cateItem.id = jsObject.getInt("ID");
					if (!jsObject.isNull("NAME"))
						cateItem.cTitle = jsObject.getString("NAME");
					if (!jsObject.isNull("IMAGEPATH"))
						cateItem.ImagePath = jsObject.getString("IMAGEPATH");
						if (!jsObject.isNull("CDESC"))
						cateItem.cDesc = jsObject.getString("CDESC");
					if (!jsObject.isNull("SID"))
						cateItem.sid = jsObject.getInt("SID");
					if (!jsObject.isNull("HASH"))
						cateItem.hash = jsObject.getString("HASH");
					if (!jsObject.isNull("VIDEURI"))
						cateItem.videuri = jsObject.getString("VIDEURI")==null?"":jsObject.getString("VIDEURI");
					if (!jsObject.isNull("SIZE"))//文件大小
						{
						cateItem.length = jsObject.getInt("SIZE");
						cateItem.strLength = StringUtil.resultBitTranslate(cateItem.length);
					}
					if (!jsObject.isNull("ACTORS"))
						cateItem.actors = jsObject.getString("ACTORS");
					if (!jsObject.isNull("AREA"))
						cateItem.area = jsObject.getString("AREA");
					if (!jsObject.isNull("DIRECTORS"))
						cateItem.directors = jsObject.getString("DIRECTORS");
					if (!jsObject.isNull("PUBLISH_TIME"))
					{
						String tmp=jsObject.getString("PUBLISH_TIME");
						cateItem.year = tmp;
						
					}
					if (!jsObject.isNull("DOWN"))
					{
						int down=jsObject.getInt("DOWN");
						
						cateItem.playsum =Util.formatVideoDnum(down);
					}
					if (!jsObject.isNull("ISPAY_DAY")) 
						cateItem.ispayday =(float)jsObject.getDouble("ISPAY_DAY");
					if(!jsObject.isNull("ISPAY")){
						cateItem.ispay = (float) jsonobj.getDouble("ISPAY");
					}
					if (!jsObject.isNull("ISSHARE")) 
						cateItem.isshare =jsObject.getInt("ISSHARE");
					if (!jsObject.isNull("URL"))
						cateItem.webURL = jsObject.getString("URL");
					
					
					headapps.add(cateItem);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for (int j = 0; j < headapps.size(); j++) {	
				BigItem item2 = new BigItem();
				item2.layoutType = BigItem.Type.LAYOUT_VIDEOHEADLIST;
				item2.videoHeadItems.add(headapps.get(j++));
				if (j < headapps.size()) {
					item2.videoHeadItems.add(headapps.get(j));		
				}
				bigList.add(item2);
			}
		}
		List<VideoItem> apps = new ArrayList<VideoItem>();
		for (int i = 0; i < infojsa.length(); i++) {
			try {
				JSONObject jsObject = (JSONObject) infojsa.get(i);
				VideoItem cateItem = new VideoItem();
				if (!jsObject.isNull("ID"))
					cateItem.id = jsObject.getInt("ID");
				if (!jsObject.isNull("NAME"))
					cateItem.cTitle = jsObject.getString("NAME");
				if (!jsObject.isNull("IMAGEPATH"))
					cateItem.ImagePath = jsObject.getString("IMAGEPATH");
					if (!jsObject.isNull("CDESC"))
					cateItem.cDesc = jsObject.getString("CDESC");
				if (!jsObject.isNull("SID"))
					cateItem.sid = jsObject.getInt("SID");
				if (!jsObject.isNull("HASH"))
					cateItem.hash = jsObject.getString("HASH");
				if (!jsObject.isNull("VIDEURI"))//音乐文件下载地址
					cateItem.videuri = jsObject.getString("VIDEURI");
				if (!jsObject.isNull("SIZE"))//文件大小
					{
					cateItem.length = jsObject.getInt("SIZE");
					cateItem.strLength = StringUtil.resultBitTranslate(cateItem.length);
				}
				if (!jsObject.isNull("ACTORS"))
					cateItem.actors = jsObject.getString("ACTORS");
				if (!jsObject.isNull("AREA"))
					cateItem.area = jsObject.getString("AREA");
				if (!jsObject.isNull("DIRECTORS"))
					cateItem.directors = jsObject.getString("DIRECTORS");
				if (!jsObject.isNull("PUBLISH_TIME"))
				{
					String tmp=jsObject.getString("PUBLISH_TIME");
					cateItem.year = tmp;
					
				}
				if (!jsObject.isNull("DOWN"))
				{
					int down=jsObject.getInt("DOWN");
					
					cateItem.playsum =Util.formatVideoDnum(down);
				}
				if (!jsObject.isNull("ISPAY")) 
					cateItem.ispay =(float)jsObject.getDouble("ISPAY");
				if (!jsObject.isNull("ISSHARE")) 
					cateItem.isshare =jsObject.getInt("ISSHARE");
				
				if (!jsObject.isNull("URL"))
					cateItem.webURL = jsObject.getString("URL");
				apps.add(cateItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (lastbigitem != null) {
			for (int k = lastbigitem.videoItems.size(); k < 4; k++) {
				if (apps.size() > 0) {
					lastbigitem.videoItems.add(apps.get(0));
					apps.remove(0);
				}
			}
			bigList.add(lastbigitem);
		}
		for (int j = 0; j < apps.size(); j++) {	
			BigItem item = new BigItem();
			item.layoutType = BigItem.Type.LAYOUT_VIDEOCAEGORYLIST;
			item.videoItems.add(apps.get(j++));
			if (j < apps.size()) {
				item.videoItems.add(apps.get(j));
				j++;
				if((j < apps.size()))
				{
					item.videoItems.add(apps.get(j));
				}
			}
			bigList.add(item);
		}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return bigList;
	}
	public static List<BigItem> parseCateVideotwoList(JSONObject jsonobj,BigItem lastbigitem) {
		List<BigItem> bigList = new ArrayList<BigItem>();
		try {			
			
		JSONArray infojsa=jsonobj.getJSONArray("data");
	
		List<VideoItem> apps = new ArrayList<VideoItem>();
		for (int i = 0; i < infojsa.length(); i++) {
			try {
				JSONObject jsObject = (JSONObject) infojsa.get(i);
				VideoItem cateItem = new VideoItem();
				if (!jsObject.isNull("ID"))
					cateItem.id = jsObject.getInt("ID");
				if (!jsObject.isNull("TITLE"))
					cateItem.cTitle= jsObject.getString("TITLE");
				if (!jsObject.isNull("NAME"))
					cateItem.name = jsObject.getString("NAME");
				if (!jsObject.isNull("IMAGEPATH"))
					cateItem.ImagePath = jsObject.getString("IMAGEPATH");
					if (!jsObject.isNull("CDESC"))
					cateItem.cDesc = jsObject.getString("CDESC");
				if (!jsObject.isNull("SID"))
					cateItem.sid = jsObject.getInt("SID");
				if (!jsObject.isNull("HASH"))
					cateItem.hash = jsObject.getString("HASH");
				if (!jsObject.isNull("VIDEURI"))//音乐文件下载地址
					cateItem.videuri = jsObject.getString("VIDEURI")==null?"":jsObject.getString("VIDEURI");
				if (!jsObject.isNull("SIZE"))//文件大小
					{
					cateItem.length = jsObject.getInt("SIZE");
					cateItem.strLength = StringUtil.resultBitTranslate(cateItem.length);
				}
				if (!jsObject.isNull("ACTORS"))
					cateItem.actors = jsObject.getString("ACTORS");
				if (!jsObject.isNull("AREA"))
					cateItem.area = jsObject.getString("AREA");
				if (!jsObject.isNull("DIRECTORS"))
					cateItem.directors = jsObject.getString("DIRECTORS");
				if (!jsObject.isNull("PUBLISH_TIME"))
				{
					String tmp=jsObject.getString("PUBLISH_TIME");
//					cateItem.year = tmp.substring(0, tmp.indexOf("-"));
					cateItem.year = tmp;
				}
				if (!jsObject.isNull("DOWN"))
				{
					int down=jsObject.getInt("DOWN");
					
					cateItem.playsum =Util.formatVideoDnum(down);
				}
				if (!jsObject.isNull("ISPAY")) 
					cateItem.ispay =(float)jsObject.getDouble("ISPAY");
				if (!jsObject.isNull("ISSHARE")) 
					cateItem.isshare =jsObject.getInt("ISSHARE");
				if (!jsObject.isNull("URL"))
					cateItem.webURL = jsObject.getString("URL");
				
				apps.add(cateItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (lastbigitem != null) {
			for (int k = lastbigitem.videoItems.size(); k < 4; k++) {
				if (apps.size() > 0) {
					lastbigitem.videoItems.add(apps.get(0));
					apps.remove(0);
				}
			}
			bigList.add(lastbigitem);
		}
		for (int j = 0; j < apps.size(); j++) {	
			BigItem item = new BigItem();
			item.layoutType = BigItem.Type.LAYOUT_VIDEOCAEGORYLIST;
			item.videoItems.add(apps.get(j++));
			if (j < apps.size()) {
				item.videoItems.add(apps.get(j));
				j++;
				if((j < apps.size()))
				{
					item.videoItems.add(apps.get(j));
				}
			}
			bigList.add(item);
		}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return bigList;
	}
	
	/**
	 * 解析TV
	 * 
	 * @param jsonobj
	 * @param lastbigitem
	 * @return
	 */
	public static List<BigItem> parseLiveVideotwoList(JSONObject jsonobj, BigItem lastbigitem) {

		List<BigItem> bigList = new ArrayList<BigItem>();
		try {

			JSONArray infojsa = jsonobj.getJSONArray("data");

			List<VideoItem> apps = new ArrayList<VideoItem>();
			for (int i = 0; i < infojsa.length(); i++) {
				try {
					JSONObject jsObject = (JSONObject) infojsa.get(i);
					VideoItem cateItem = new VideoItem();
					if (!jsObject.isNull("ID"))
						cateItem.id = jsObject.getInt("ID");
					if (!jsObject.isNull("NAME"))
						cateItem.cTitle = jsObject.getString("NAME");
					if (!jsObject.isNull("IMAGEPATH"))
						cateItem.ImagePath = jsObject.getString("IMAGEPATH");
					if (!jsObject.isNull("CDESC"))
						cateItem.cDesc = jsObject.getString("CDESC");
					if (!jsObject.isNull("SID"))
						cateItem.sid = jsObject.getInt("SID");
					if (!jsObject.isNull("HASH"))
						cateItem.hash = jsObject.getString("HASH");
					if (!jsObject.isNull("VIDEURI"))// 音乐文件下载地址
						cateItem.videuri = jsObject.getString("VIDEURI") == null ? "" : jsObject.getString("VIDEURI");
					if (!jsObject.isNull("SIZE"))// 文件大小
					{
						cateItem.length = jsObject.getInt("SIZE");
						cateItem.strLength = StringUtil.resultBitTranslate(cateItem.length);
					}
					if (!jsObject.isNull("ACTORS"))
						cateItem.actors = jsObject.getString("ACTORS");
					if (!jsObject.isNull("AREA"))
						cateItem.area = jsObject.getString("AREA");
					if (!jsObject.isNull("DIRECTORS"))
						cateItem.directors = jsObject.getString("DIRECTORS");
					if (!jsObject.isNull("PUBLISH_TIME")) {
						String tmp = jsObject.getString("PUBLISH_TIME");
						// cateItem.year = tmp.substring(0, tmp.indexOf("-"));
						cateItem.year = tmp;
					}
					if (!jsObject.isNull("DOWN")) {
						int down = jsObject.getInt("DOWN");

						cateItem.playsum = Util.formatVideoDnum(down);
					}
					if (!jsObject.isNull("ISPAY"))
						cateItem.ispay = (float) jsObject.getDouble("ISPAY");
					if (!jsObject.isNull("ISSHARE"))
						cateItem.isshare = jsObject.getInt("ISSHARE");
					if (!jsObject.isNull("URL"))
						cateItem.webURL = jsObject.getString("URL");

					apps.add(cateItem);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (lastbigitem != null) {
				for (int k = lastbigitem.videoItems.size(); k < 4; k++) {
					if (apps.size() > 0) {
						lastbigitem.videoItems.add(apps.get(0));
						apps.remove(0);
					}
				}
				bigList.add(lastbigitem);
			}

			for (int j = 0; j < apps.size(); j++) {
				BigItem item2 = new BigItem();
				item2.layoutType = BigItem.Type.LAYOUT_VIDEOHEADLIST;
				//item2.layoutType = BigItem.Type.LAYOUT_VIDEOCAEGORYLIST;
				item2.videoHeadItems.add(apps.get(j++));
				if (j < apps.size()) {
					item2.videoHeadItems.add(apps.get(j));
				}
				bigList.add(item2);
			}
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return bigList;

	}
	
	/**
	 * 解析大家都在玩对象
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseStartRecList(JSONArray jsonArray) {
		List<AppItem> appItemList = parseAppItemList(jsonArray);
		int size = appItemList.size() > 4 ? 4 : appItemList.size();
		List<BigItem> bigList = new ArrayList<BigItem>();
		List<AppItem> apps = new ArrayList<AppItem>();
		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				AppItem appItem = parseAppItem(jsObject);
				apps.add(appItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (int j = 0; j < apps.size(); j++) {
			BigItem item = new BigItem();
			item.layoutType = BigItem.Type.LAYOUT_APPLIST_REC_TWO;
			item.splashRecItems.add(apps.get(j++));
			if (j < apps.size()) {
				item.splashRecItems.add(apps.get(j));
			}
			bigList.add(item);
		}
		return bigList;
	}
	
	/**
	 * 将jsonArray解析为list
	 * @param jsonArray
	 * @return 封装Appitm的list
	 */
	public static List<AppItem> parseAppItemList(JSONArray jsonArray) {
		ArrayList<AppItem> items = new ArrayList<AppItem>();
		for (int i = 0;i<jsonArray.length();i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				AppItem appItem = parseAppItem(jsObject);
				items.add(appItem);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return items;
	}

	/**
	 * 解析用户信息
	 * 
	 * @param result
	 * @return
	 */
	public static UserData parseUserInfo(JSONObject result) {
		try {
			UserData user = new UserData();
			user.setType(UserData.TYPE_ME);
			user.setNickname(result.getString("NICKNAME"));
			user.setIconUrl(result.getString("ICON"));
			user.setGender(result.getString("SEX"));
			user.setUid(result.getString("USID"));
			user.setCredit(result.getInt("CREDIT"));
			user.setUlevel(result.getInt("LEVEL"));
			user.setVIP(result.getInt("VIP"));
			return user;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解析需要更新的游戏
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<AppItem> parseUpdateGames(JSONArray jsonArray) {
		int size = jsonArray.length();
		List<AppItem> items = new ArrayList<AppItem>();

		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				final AppItem app = parseAppItem(jsObject);
				app.state = AppItemState.STATE_NEED_UPDATE;
				app.list_id = LogModel.L_DOWN_MANAGER;
				app.list_cateid = LogModel.L_APP_UPDATE;
				items.add(app);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return items;
	}

	/**
	 * 解析本地游戏列表
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static Map<String, String> parseLocalGames(JSONObject jsonObject) {
		Map<String, String> items = new HashMap<String, String>();
		try {
			JSONArray jsonArray = jsonObject.getJSONArray("DLIST");
			int size = jsonArray.length();
			for (int i = 0; i < size; i++) {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				items.put(jsObject.getString("ID"), jsObject.getString("NAME"));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return items;
	}

	/**
	 * 解析本地游戏更新时间
	 * 
	 * @param jsonPageInfo
	 * @return
	 */
	public static String parseLocalGameMDate(JSONObject jsonObject) {
		try {
			if (!jsonObject.isNull("MDATE")) {
				return jsonObject.getString("MDATE");
			}
		} catch (Exception e) {
		}

		return null;
	}
	
	//////////////------------------
	
	/**
	 * 解析糗事百科对象
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseJokeList(Context context,JSONArray jsonArray) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();
		List<CateItem> apps = new ArrayList<CateItem>();
		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				CateItem cateItem = new CateItem();
				if (!jsObject.isNull("IMAGEPATH"))
					cateItem.ImagePath = jsObject.getString("IMAGEPATH");
				if (!jsObject.isNull("CONTENT"))
					cateItem.content = jsObject.getString("CONTENT");
				if (!jsObject.isNull("NAME"))
					cateItem.cTitle = jsObject.getString("NAME");
				if (!jsObject.isNull("COMMENT_COUNT"))
					cateItem.comment_count= jsObject.getInt("COMMENT_COUNT");
				if (!jsObject.isNull("VOTE_COUNT"))
					cateItem.vote_count = jsObject.getInt("VOTE_COUNT");
				if (!jsObject.isNull("ISSHARE")) 
					cateItem.isshare =jsObject.getInt("ISSHARE");
				if(!jsObject.isNull("PUBLISHTIME"))
					cateItem.publish_time =jsObject.getString("PUBLISHTIME");
				if(!jsObject.isNull("ICON"))
					cateItem.ImagePath = jsObject.getString("ICON");
				if (!jsObject.isNull("SID"))
				{
					String currSid = null;
					int iscommflag = 0;
					int voteflag = 0;					
					cateItem.sid = jsObject.getInt("SID");
					
					cateItem.comment_img_resid = R.drawable.joke_btn_comment_selector;
	                cateItem.vote_img_resid = R.drawable.joke_btn_collect_selector; 
					JokeDataDatabaseHelper dbHelper = JokeDataDatabaseHelper.getInstance(context);
					SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
					String sql = "select * from jokefeedback where sid=" + Integer.toString(cateItem.sid);
					Cursor cursor = sqliteDatabase.rawQuery(sql, new String[0]);
					
					while(cursor.moveToNext())
					{
						currSid = cursor.getString(cursor.getColumnIndex(joke_feedback_list.JOKESID));
						
						if(currSid != null)
						{
							iscommflag = cursor.getInt(cursor.getColumnIndex(joke_feedback_list.JOKECOMMFLAG));
							voteflag = cursor.getInt(cursor.getColumnIndex(joke_feedback_list.JOKEVOTEFLAG));
							
							if(iscommflag != 0)
							{
								cateItem.comment_img_resid = R.drawable.joke_btn_commented;				                
							}
							if(voteflag != 0)
							{								
				                cateItem.vote_img_resid = R.drawable.joke_btn_collected; 
							}
						}
						
					}
					cursor.close();					
				}
				if (!jsObject.isNull("NICKNAME"))
					cateItem.sName = jsObject.getString("NICKNAME");	
				if (!jsObject.isNull("VCOUNT"))
					cateItem.cCount= jsObject.getInt("VCOUNT");
				
				apps.add(cateItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (int j = 0; j < apps.size(); j++) {
			BigItem item = new BigItem();
			item.layoutType = BigItem.Type.LAYOUT_CAEGORYLIST;
			item.cateItems.add(apps.get(j));			
			bigList.add(item);
		}
		
		return bigList;
	}
	
	//////////end
	/**
	 * 解析糗事百科对象2
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseJokeList2(Context context,JSONArray jsonArray) {
		int size = jsonArray.length();
		int mysize = size+1;
		List<BigItem> bigList = new ArrayList<BigItem>();
		List<CateItem> apps = new ArrayList<CateItem>();
		for (int i = 0; i < mysize; i++) {
			if(i!=size){
				try {
					JSONObject jsObject = (JSONObject) jsonArray.get(i);
					CateItem cateItem = new CateItem();
					if (!jsObject.isNull("IMAGEPATH"))
						cateItem.ImagePath = jsObject.getString("IMAGEPATH");
					if (!jsObject.isNull("CONTENT"))
						cateItem.content = jsObject.getString("CONTENT");
					if (!jsObject.isNull("NAME"))
						cateItem.cTitle = jsObject.getString("NAME");
					if (!jsObject.isNull("COMMENT_COUNT"))
						cateItem.comment_count= jsObject.getInt("COMMENT_COUNT");
					if (!jsObject.isNull("VOTE_COUNT"))
						cateItem.vote_count = jsObject.getInt("VOTE_COUNT");
					if (!jsObject.isNull("ISSHARE")) 
						cateItem.isshare =jsObject.getInt("ISSHARE");
					if(!jsObject.isNull("PUBLISHTIME"))
						cateItem.publish_time =jsObject.getString("PUBLISHTIME");
					if (!jsObject.isNull("SID"))
					{
						String currSid = null;
						int iscommflag = 0;
						int voteflag = 0;					
						cateItem.sid = jsObject.getInt("SID");
						
						cateItem.comment_img_resid = R.drawable.joke_btn_comment_selector;
		                cateItem.vote_img_resid = R.drawable.joke_btn_collect_selector; 
						JokeDataDatabaseHelper dbHelper = JokeDataDatabaseHelper.getInstance(context);
						SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
						String sql = "select * from jokefeedback where sid=" + Integer.toString(cateItem.sid);
						Cursor cursor = sqliteDatabase.rawQuery(sql, new String[0]);
						
						while(cursor.moveToNext())
						{
							currSid = cursor.getString(cursor.getColumnIndex(joke_feedback_list.JOKESID));
							
							if(currSid != null)
							{
								iscommflag = cursor.getInt(cursor.getColumnIndex(joke_feedback_list.JOKECOMMFLAG));
								voteflag = cursor.getInt(cursor.getColumnIndex(joke_feedback_list.JOKEVOTEFLAG));
								
								if(iscommflag != 0)
								{
									cateItem.comment_img_resid = R.drawable.joke_btn_commented;				                
								}
								if(voteflag != 0)
								{								
					                cateItem.vote_img_resid = R.drawable.joke_btn_collected; 
								}
							}
							
						}
						cursor.close();					
					}
					if (!jsObject.isNull("NICKNAME"))
						cateItem.sName = jsObject.getString("NICKNAME");	
					if (!jsObject.isNull("VCOUNT"))
						cateItem.cCount= jsObject.getInt("VCOUNT");
					//add by bobo
					if(i==size){
						}
					apps.add(cateItem);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				CateItem cateItem = new CateItem();
				cateItem.changjoke = true;
				apps.add(cateItem);
			}
		}
		
		for (int j = 0; j < apps.size(); j++) {
			BigItem item = new BigItem();
			if(apps.get(j).changjoke){
				item.layoutType = BigItem.Type.LAYOUT_CHANGE;
			}else{
				item.layoutType = BigItem.Type.LAYOUT_CAEGORYLIST;
			}
			item.cateItems.add(apps.get(j));			
			bigList.add(item);
		}
		
		return bigList;
	}	
	
	/**
	 * 新闻
	 * 
	 * @param jsObject
	 * @return
	 * @throws JSONException
	 */
	/**
	 * 解析糗事百科对象
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static List<BigItem> parseJokeList3(Context context,JSONArray jsonArray) {
		int size = jsonArray.length();
		List<BigItem> bigList = new ArrayList<BigItem>();
		List<CateItem> apps = new ArrayList<CateItem>();
		for (int i = 0; i < size; i++) {
			try {
				JSONObject jsObject = (JSONObject) jsonArray.get(i);
				CateItem cateItem = new CateItem();
				if (!jsObject.isNull("IMAGEPATH"))
					cateItem.ImagePath = jsObject.getString("IMAGEPATH");
				if (!jsObject.isNull("CONTENT"))
					cateItem.content = jsObject.getString("CONTENT");
				if (!jsObject.isNull("NAME"))
					cateItem.cTitle = jsObject.getString("NAME");
				if (!jsObject.isNull("COMMENT_COUNT"))
					cateItem.comment_count= jsObject.getInt("COMMENT_COUNT");
				if (!jsObject.isNull("VOTE_COUNT"))
					cateItem.vote_count = jsObject.getInt("VOTE_COUNT");
				if (!jsObject.isNull("ISSHARE")) 
					cateItem.isshare =jsObject.getInt("ISSHARE");
				if(!jsObject.isNull("PUBLISHTIME"))
					cateItem.publish_time =jsObject.getString("PUBLISHTIME");
				if (!jsObject.isNull("H")){
					cateItem.myH =jsObject.getInt("H");
					Log.d("mylog", "新闻---"+cateItem.myH);
				} 
				if (!jsObject.isNull("SID"))
				{
					String currSid = null;
					int iscommflag = 0;
					int voteflag = 0;					
					cateItem.sid = jsObject.getInt("SID");
					
					cateItem.comment_img_resid = R.drawable.joke_btn_comment_selector;
	                cateItem.vote_img_resid = R.drawable.joke_btn_collect_selector; 
					JokeDataDatabaseHelper dbHelper = JokeDataDatabaseHelper.getInstance(context);
					SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
					String sql = "select * from jokefeedback where sid=" + Integer.toString(cateItem.sid);
					Cursor cursor = sqliteDatabase.rawQuery(sql, new String[0]);
					
					while(cursor.moveToNext())
					{
						currSid = cursor.getString(cursor.getColumnIndex(joke_feedback_list.JOKESID));
						
						if(currSid != null)
						{
							iscommflag = cursor.getInt(cursor.getColumnIndex(joke_feedback_list.JOKECOMMFLAG));
							voteflag = cursor.getInt(cursor.getColumnIndex(joke_feedback_list.JOKEVOTEFLAG));
							
							if(iscommflag != 0)
							{
								cateItem.comment_img_resid = R.drawable.joke_btn_commented;				                
							}
							if(voteflag != 0)
							{								
				                cateItem.vote_img_resid = R.drawable.joke_btn_collected; 
							}
						}
						
					}
					cursor.close();					
				}
				if (!jsObject.isNull("NICKNAME"))
					cateItem.sName = jsObject.getString("NICKNAME");	
				if (!jsObject.isNull("VCOUNT"))
					cateItem.cCount= jsObject.getInt("VCOUNT");
				
				apps.add(cateItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (int j = 0; j < apps.size(); j++) {
			BigItem item = new BigItem();
			if(apps.get(j).myH==5){
				Log.d("mylog", "-----大图");
				item.layoutType = BigItem.Type.LAYOUT_CAEGORYBIG_IC;
			}
			else 
				if(apps.get(j).myH==0){
				Log.d("mylog", "-----换行");
				item.layoutType = BigItem.Type.LAYOUT_CAEGORYSPES;
			}
			else
			{
				item.layoutType = BigItem.Type.LAYOUT_CAEGORYLIST;
			}
			item.cateItems.add(apps.get(j));			
			bigList.add(item);
		}
		
		return bigList;
	}
	public static List<CateItem> parseVideoCateItem(JSONObject jsObject)
			throws JSONException {
		JSONArray jsonArray = jsObject.getJSONArray("data");
		int size = jsonArray.length();
		List<CateItem> apps = new ArrayList<CateItem>();
		for (int i = 0; i < size; i++) {
			try {
				JSONObject myjsObject = (JSONObject) jsonArray.get(i);
				CateItem videoItem = new CateItem();
				if (!myjsObject.isNull("CTITLE"))
					videoItem.cTitle = myjsObject.getString("CTITLE");
				if (!myjsObject.isNull("ID"))
					videoItem.id = myjsObject.getInt("ID");	
				if (!myjsObject.isNull("ISSHARE")) 
					videoItem.isshare =myjsObject.getInt("ISSHARE");
				if (!myjsObject.isNull("PRICE")) 
					videoItem.cCount =myjsObject.getInt("PRICE");
				//add   by  ----bobo
				if(!myjsObject.isNull("PRICE_DAY")){
					videoItem.price_day = myjsObject.getInt("PRICE_DAY");
				}
				if(!myjsObject.isNull("FEEID_DAY")){
					videoItem.feeid_day = myjsObject.getInt("FEEID_DAY");
				}
				if(!myjsObject.isNull("FEEID")){
					videoItem.feeid = myjsObject.getInt("FEEID");
				}
				if(!myjsObject.isNull("FEEID_WEEK")){
					videoItem.FEEID_WEEK = myjsObject.getInt("FEEID_WEEK");
				}
				if(!myjsObject.isNull("FEEID_2MONTHS")){
					videoItem.FEEID_2MONTHS = myjsObject.getInt("FEEID_2MONTHS");
				}
				if(!myjsObject.isNull("FEEID_YEAR")){
					videoItem.FEEID_YEAR = myjsObject.getInt("FEEID_YEAR");
				}
				if(!myjsObject.isNull("PRICE_WEEK")){
					videoItem.PRICE_WEEK = myjsObject.getInt("PRICE_WEEK");
				}
				if(!myjsObject.isNull("PRICE_2MONTHS")){
					videoItem.PRICE_2MONTHS = myjsObject.getInt("PRICE_2MONTHS");
				}
				if(!myjsObject.isNull("PRICE_YEAR")){
					videoItem.PRICE_YEAR = myjsObject.getInt("PRICE_YEAR");
				}
				if(!myjsObject.isNull("PRODID")){
					videoItem.priodid = myjsObject.getInt("PRODID");
				}
				apps.add(videoItem);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return apps;
	}
	public static VideoItem parseCateVideo(JSONObject jsonobj) {
			
			VideoItem cateItem = null ;
			try {				
				JSONArray infojsa=jsonobj.getJSONArray("data");
				JSONObject jsObject = (JSONObject) infojsa.get(0);
				 cateItem = new VideoItem();
				if (!jsObject.isNull("ID"))
					cateItem.id = jsObject.getInt("ID");
				if (!jsObject.isNull("NAME"))
					cateItem.cTitle = jsObject.getString("NAME");
				if (!jsObject.isNull("IMAGEPATH"))
					cateItem.ImagePath = jsObject.getString("IMAGEPATH");
					if (!jsObject.isNull("CDESC"))
					cateItem.cDesc = jsObject.getString("CDESC");
				if (!jsObject.isNull("SID"))
					cateItem.sid = jsObject.getInt("SID");
				if (!jsObject.isNull("HASH"))
					cateItem.hash = jsObject.getString("HASH");
				if (!jsObject.isNull("VIDEURI"))//音乐文件下载地址
					cateItem.videuri = jsObject.getString("VIDEURI")==null?"":jsObject.getString("VIDEURI");
				if (!jsObject.isNull("SIZE"))//文件大小
					{
					cateItem.length = jsObject.getInt("SIZE");
					cateItem.strLength = StringUtil.resultBitTranslate(cateItem.length);
				}
				if (!jsObject.isNull("ACTORS"))
					cateItem.actors = jsObject.getString("ACTORS");
				if (!jsObject.isNull("AREA"))
					cateItem.area = jsObject.getString("AREA");
				if (!jsObject.isNull("DIRECTORS"))
					cateItem.directors = jsObject.getString("DIRECTORS");
				if (!jsObject.isNull("PUBLISH_TIME"))
				{
					String tmp=jsObject.getString("PUBLISH_TIME");
					cateItem.year = tmp;
					
				}
				if (!jsObject.isNull("DOWN"))
				{
					int down=jsObject.getInt("DOWN");
					
					cateItem.playsum =Util.formatVideoDnum(down);
				}
				if (!jsObject.isNull("ISPAY")) 
					cateItem.ispay =(float)jsObject.getDouble("ISPAY");
				if (!jsObject.isNull("ISSHARE")) 
					cateItem.isshare =jsObject.getInt("ISSHARE");
				
				if (!jsObject.isNull("URL"))
					cateItem.webURL = jsObject.getString("URL");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	
		return cateItem;
	}
}
