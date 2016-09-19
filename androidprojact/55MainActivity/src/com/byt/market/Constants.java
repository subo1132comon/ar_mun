package com.byt.market;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpResponse;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.byt.market.activity.MainActivity;
import com.byt.market.net.NetworkUtil;
import com.byt.market.util.SharedPrefManager;

public class Constants {

	
	public static final String SHARE_ACTION = "com.tyb,share";
	
	public static final boolean LOCAL_PROTOCOL = false;

	public static final String GAME_IN_TOOL = "sumoveso";
	public static final String SU = "su";
	public static final String PROCESS_NAME = "supervisor com.byt.market:kernel";
	public static final String HOME_BG = "shouyejiazaitu.png";

	public static final String PUSH = "2";
	public static final String LOGIN = "1";
	public static final int COMMENT_REQUEST_CODE = 110;
	public static final int COMMENT_RESPONSE_CODE = 120;
	public static final String IS_NOTIF = "isnotif";
	public static final String PACKAGENAME_NAME = "com.byt.market";
	/**
	 * 日志开关
	 */
	public static final String PROGREES_BAR_RECIVE ="com.byt.progroesbar"; 
	public static final boolean ISLOG = false;
	public static final boolean IS_SAVE_PROTOCOL = false;

	public static final String FOLDER_ROOT = "/BYTAPPPlus/";
	public static final String FOLDER_ICONS = FOLDER_ROOT + "icon/";
	public static final String FOLDER_USERICONS = FOLDER_ROOT + "usericon/";
	public static final String FOLDER_PROTOCEL = FOLDER_ROOT + "protocol/";
	public static final String FOLDER_DOWNLOAD = FOLDER_ROOT + "download/";
	public static final String JOKE_FOLDER = "/SYNC/JOKE/";
	public static final String JOKE_GIF_FILENAME = "joke.gif";
	// public static final String SERVER = FOLDER_ROOT + "protocol/";

//	public static final String APK_URL = "http://ksfile.client.gamebean.net/";   	//线上资源正式环境
//	public static final String IMG_URL = "http://ksfile.client.gamebean.net/";		//线上资源正式环境
	public static  String APK_URL = "http://55mun.com:8022/";    //线上资源测试环境
	public static  String IMG_URL = "http://55mun.com:8022/";	//线上资源测试环境
	// public static final String LIST_URL = "http://115.28.144.6/v1.php";
	// public static final String LIST_URL =
	// "http://ksapi.client.gamebean.net/v1.php";//// 测试外网
	// public static final String LIST_URL =
	// "http://api.dev.bestone.cn/v1.php";// 测试内网
	public static  String LIST_URL = "http://55mun.com:8022/v1.php";// / 测试内网
	public static  String JOKE_COMMENT_URL = "http://55mun.com:8022/Joke/v1.php";// / 糗百评论
	// public static final String LIST_URL =
	// "http://api.local.bestone.cn/v1.php";// 测试内网
	// public static final String LIST_URL =
	// "http://api2.client.bestone.cn/v1.php";// 正式测试
	// public static final String LIST_URL =
	// "http://api.dev.bestone.cn/v1.php";
	// public static final String LIST_URL =
	// "http://api.home.gamebean.net/v1.php";
	//public static  String YULE_VER_URL = "http://55mun.com:8022/Joke/v1.php?qt=Category"; // 娱乐版块版本号查询	
	public static  String YULE_VER_URL = "http://55mun.com:8022/Joke/v1.php?qt=Category1"; // 娱乐版块版本号查询	//add  by bobo
	public static  String BAK_LIST_URL = "http://55mun.com:8022/v1.php"; // 备用url
	//http://appstore.szbytcloud.com:8022/index.php?s=/Admin/Index/index.html#ThemeSource
	
	public static String SHREA_PATH = "http://www.jokedeedee.com/?s=/index/m_click/sid/";
	
	//首页TV列表
	public static String TV_LIST_URL = "http://122.155.202.149:8022/";

	public static final String HOTWORD = "hotword";
	public static final String CATE = "category";
	public static final String RANK = "ranklist";
	public static final String GAME_REC = "gamereslist";// 游戏推荐
	public static final String APP_REC = "appreslist";// 应用推荐
	public static final String GAME_RANK = "gameranklist";// 游戏排行
	public static final String APP_RANK = "appranklist";//应用排行
	public static final String WEEK_RANK = "weekranklist";// 周排行
	public static final String MONTH_RANK = "monthranklist";// 月排行
	public static final String TOTAL_RANK = "totalranklist";// 总排行
	public static final String SAVE = "loadlist";
	public static final String HOME = "mainlist";
	//public static final String HEAD = "headlist";
	public static final String HEAD = "headlist1";
	public static final String DETAIL = "info";
	public static final String CATE_NEW = "categorynew";
	public static final String CATE_HOT = "categoryhot";
	public static final String SEARCH_HINT = "hotrelation";
	public static final String SUBJECT = "speciallist";
	public static final String PLINFO = "plinfo";
	public static final String SUBJECT_LIST = "special";
	public static final String GUIDE = "guide";
	public static final String UPDATE = "update";
	public static final String LOCAL_GAME_UPDATE = "guidelocal&mdate=";
	public static final String DEFAULT_LOCAL_UPDATE = "2014/3/29 21:23:14";
	public static final String STARTRECREPEATTIME = "startrecommendtime";

	public static String COMM = "comm";
	public static String NCOMM = "ncomm";
	public static String UCOMM = "ucomm";
	public static String ISES = "ises";// 认证登录
	public static String NUSER = "nuser";// 上传第三方登录用户资料
	public static String UPUSER = "upuser";// 修改昵称
	public static String UPUSERHEAD = "upuserhead";// 修改头像
	public static String USERLOGIN = "login";// 登录
	public static String ISREG = "isreg";// 注册
	public static String RATING = "rating"; // 评星
	public static String UPDATE_CLIENT = "uclient"; // 检测客户端更新

	public static String APKNAME = "2D4Eg3S5aBme2DC3BEnter.apk";

	public static boolean ISSHOW;
	public static boolean ISGOOGLE=false;
	public static boolean ISShowToast=false;
	public static final String DOWNLOAD_DIR = ".dkbestone_store";

	// public static boolean loadIcon = true;
	public static final String TOMusicLIST = "com.byt.market.musiclist";
	public static final String TOAVLIST = "com.byt.market.avlist";
	public static final String TOridiaoLLIST = "com.byt.market.ridiaollist";
	public static final String TONOVELLIST = "com.byt.market.novellist";
	public static final String TOTVLIST = "com.byt.market.tvlist";
	public static final String TOLIST = "com.byt.market.list";
	public static final String TODETAIL = "com.byt.market.detail";
	public static final String TOCOMMENTS = "com.byt.market.comm";
	public static final String TOBSREEN = "com.byt.market.bscreen";
	public static final String TOSEARCH = "com.byt.market.search";
	public static final String TOMAIN = "com.byt.market.main";
	public static final String TOJOYSKILL = "com.byt.market.joyskill";

	public static final String LIST_FROM = "from";
	public static final String LOCAL_URL_SK = "localService_sk";

	public static final String PREF_USERINFO = "userinfo";
	public static final String PREF_OAUTH_SINA = "pref_oauth_sina";
	public static final String PREF_OAUTH_TENCENT = "pref_oauth_tencent";

	public static final String SETTINGS_PREF = "settings";

	public static final String PKG = "com.byt.market";
	public static final String TOJOKETETAILS = "com.byt.market.jokedetail";
	public static final String TOJOKETETAILS2 = "com.byt.market.jokedetail2";
	
	public static final String stopMusicBrodcast = "com.tyb.stopmusic";
	
	public static String RAPIT_PLAYSP = "rapitplaysp"; // 检查音乐 tv..下载红点 sp
	public static String RAPIT_KEY = "rapitv";//检查音乐 tv..下载红点value
	public static String UMSTAT_KEY = "umstatistics";//友盟统计
	public static String TV_TOASTSP = "tvtoastsp"; 
	public static String TV_TOASTKEY = "tvtoastkey";
	
	public static String TV_SHOW_DELET_SP = "tvshowdeletsp";
	public static String UM_AIDAO_SP = "umvidaotimesp";
	public static String UM_AIDAO = "umvidaotime";
	public static String TV_SHOW_DELET = "tvshowdelet"; 
	public static String AV_ISbro_SP = "avisbrosp";
	public static String AV_ISbro = "avisbrosp";
	public static String Trans_sp = "tranns_sp";
	public static String TRANNS_KEY = "tranns_key";
	
	public static String TV_SUBKEY = "tv_subkey";
	public static String TV_ANIME_SUBKEY = "tv_anim_subkey";
	public static String TV_SHOWKEY = "tv_showkey";
	public static String JOKE_COMMED_BRODCART = "com.tyb.mark.jokecommed";
	
	/**
	 * 定时检测安装的游戏更新
	 * */
	public static final String ACTION_REQUEST_GAME_UPDATE_CHECK = "com.byt.market.REQUEST_GAME_UPDATE_CHECK";

	private static boolean mGameInToolInited = false;
	public static String SU_COMMAND_NAME = GAME_IN_TOOL;

	public static String getGameInTool() {
		if (!mGameInToolInited) {
			if (isCommandExist(GAME_IN_TOOL)) {
				SU_COMMAND_NAME = GAME_IN_TOOL;
			} else if (isCommandExist(SU)) {
				SU_COMMAND_NAME = SU;
			}
			mGameInToolInited = true;
		}
		return SU_COMMAND_NAME;
	}
	public static boolean checkDomain(Context context) {
			android.util.Log.i("test","checkDomain");
			HttpURLConnection conn=null;
		try {
			android.util.Log.i("test","checkDomain1");
			 conn=(HttpURLConnection)new URL(LIST_URL).openConnection();
			 String host = null;
				int hostIndex = "http://".length();
				int pathIndex = LIST_URL.indexOf('/', hostIndex);// url为原始的请求链接
				if (pathIndex < 0) {
					host = LIST_URL.substring(hostIndex);
				} else {
					host = LIST_URL.substring(hostIndex, pathIndex);
				}
			 conn.setRequestProperty("X-Online-Host", host);

			int code=conn.getResponseCode();
			android.util.Log.i("test","code==>"+code);
			if(code!=200){
				android.util.Log.i("test","error http");
				 switchIP();
			}else{
				android.util.Log.i("test","true http");
			}
		} catch (MalformedURLException e) {
			android.util.Log.i("test","checkDomain3");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			android.util.Log.i("test","error http IOException");
			// TODO Auto-generated catch block
			 switchIP();
		}catch(Exception e) {
			android.util.Log.i("test","error http Exception");
			 switchIP();
		}
		try {
			if(conn!=null){
				conn.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;

	}
	
	public static boolean isHasEffectiveURL(Handler handler) {
		initIP();
		HttpClient client = new HttpClient();
		PostMethod httpPost = new PostMethod(LIST_URL+"?qt=update");
		try {
			Log.i("test", "start......==>");
	        client.setTimeout(3000);
	        httpPost.addRequestHeader("Content-Type", "text/xml");
	        httpPost.setRequestHeader("Connection", "close");
			client.executeMethod(httpPost);
			int resStatusCode = httpPost.getStatusCode();
			Log.i("test", "resStatusCode==>" + resStatusCode);
			if (resStatusCode != 200) {
				 switchIP();
			} 
			handler.removeMessages(MainActivity.INIT_IP_FLAG);
			handler.sendEmptyMessage(MainActivity.INIT_IP_FLAG);
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.i("test", "exception.==>");
			 switchIP();
			 handler.removeMessages(MainActivity.INIT_IP_FLAG);
			 handler.sendEmptyMessage(MainActivity.INIT_IP_FLAG);
		} finally {
			Log.w("test", "resStatusCode==>");
			try {
				httpPost.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.w("test", "1111resStatusCode==>");
		}
		return true;
	}
	public static void switchIP(){
		APK_URL="http://122.155.202.149:8022/";
		IMG_URL ="http://122.155.202.149:8022/";
		LIST_URL = "http://122.155.202.149:8022/v1.php";
		YULE_VER_URL = "http://122.155.202.149:8022/Joke/v1.php?qt=Category"; // 娱乐版块版本号查询	
		JOKE_COMMENT_URL = "http://122.155.202.149:8022/Joke/v1.php";// / 糗百评论
	}
	public static void initIP(){
		APK_URL="http://55mun.com:8022/";
		IMG_URL ="http://55mun.com:8022/";
		LIST_URL = "http://55mun.com:8022/v1.php";
		YULE_VER_URL = "http://55mun.com:8022/Joke/v1.php?qt=Category"; // 娱乐版块版本号查询	
		JOKE_COMMENT_URL = "http://55mun.com:8022/Joke/v1.php";// / 糗百评论
	}
	public static boolean isCommandExist(String command) {
		boolean rooted = false;
		try {
			File su = new File("/system/bin/" + command);
			if (su.exists()) {
				rooted = true;
			} else {
				su = new File("/system/xbin/" + command);
				if (su.exists()) {
					rooted = true;
				}
			}
		} catch (Exception ee) {
			rooted = false;
		}
		return rooted;
	}

	/**
	 * 设置属性
	 * 
	 * @author qiuximing
	 * 
	 */
	public static class Settings {

		// 省流量模式
		public static boolean wifiDownload;
		// 通知提示
		public static boolean userUpdateNotify;
		public static boolean userGameRecommendNotify;
		// 下载位置
		public static boolean downloadSD;
		// 同时下载个数
		public static int downloadNum;
		// 静默安装卸载
		public static boolean quickInstall;

		public static final String DOWNLOAD_NUM = "downloadNum";
		public static final String DOWNLOAD_SD = "downloadSD";
		public static final String USER_GAME_RECOMMEND_NOTIFY = "userGameRecommendNotify";
		public static final String USER_UPDATE_NOTIFY = "userUpdateNotify";
		public static final String WIFI_DOWNLOAD = "wifiDownload";
		public static final String QUICK_INSTALL = "quickInstall";

		public static void saveSettings(Context context, String key,
				boolean value) {
			SharedPreferences sp = context.getSharedPreferences("settings",
					Context.MODE_PRIVATE);
			sp.edit().putBoolean(key, value).commit();
		}

		public static void saveSettings(Context context, String key, int value) {
			SharedPreferences sp = context.getSharedPreferences("settings",
					Context.MODE_PRIVATE);
			sp.edit().putInt(key, value).commit();
		}

		public static void readSettings(Context context) {
			SharedPreferences sp = context.getSharedPreferences("settings",
					Context.MODE_PRIVATE);
			wifiDownload = sp.getBoolean(WIFI_DOWNLOAD, true);
			userUpdateNotify = sp.getBoolean(USER_UPDATE_NOTIFY, true);
			userGameRecommendNotify = sp.getBoolean(USER_GAME_RECOMMEND_NOTIFY,
					true);
			downloadSD = sp.getBoolean(DOWNLOAD_SD, true);
			downloadNum = SharedPrefManager.getCurrentDownloadSum(context);
			quickInstall = SharedPrefManager
					.isQuickInstallationInRootUser(context);
		}
	}

	public static final class ExtraKey {
		public static final String APP_ITEM = "app_item";
		public static final String SID = "sid";
	}

	/**
	 * 跳转的Action
	 */
	public static final class Action {
		public static final String REFRESH_LOCAL_GAME = "com.bestone.gamebox.action.refreshLocalGame";
		public static final String CHANGE_TO_HOME = "com.bestone.gamebox.action.changeToHome";
		public static final String TODETAIL = "com.bestone.gamebox.detail";
	}

	public static final class GOTYPE {

		/** 跳转至首界面 **/
		public static final int GO_HOME = 1;
		/** 跳转至游戏详情界面 **/
		public static final int GO_GAME_DETAIL = 2;
		/** 跳转至我的游戏界面 **/
		public static final int GO_MY_GAME = 3;
		/** 跳转至系统浏览器 **/
		public static final int GO_BROWER = 4;
	}

}
