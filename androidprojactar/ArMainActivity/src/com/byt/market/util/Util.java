package com.byt.market.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.widget.Toast;

import cn.jpush.android.api.JPushInterface;

import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.data.APP;
import com.byt.market.data.AppItem;
import com.byt.market.data.GAME;
import com.byt.market.data.ListBean;
import com.byt.market.data.LoadPage;
import com.byt.market.data.MarketUpdateInfo;
import com.byt.market.data.PUSH;
import com.byt.market.data.PushInfo;
import com.byt.market.download.DownloadUtils;
import com.byt.market.log.LogModel;
import com.byt.market.tools.RootTools;
import com.byt.market.tools.RootTools.Result;

public class Util {

	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat shortFormatter = new SimpleDateFormat("yyyy-MM-dd");

	/** 打开应用的时间 **/
	public static long OPENAPPTIME = 0;

	public static LoadPage loadInfos;
	public static MarketUpdateInfo update;
	public static PushInfo pInfo;
	public static APP app;
	public static final long MIN = 60 * 1000;
	public static long UPDATERULETIME = 30 * MIN;
	public static String SOUND;
	public static String POSTDATA;
	public static String vcode;
	public static String screeSize;
	public static String channel;
	public static String imie;
	public static String mobile;

	/**
	 * 转为日期
	 * 
	 * @param date
	 * @return
	 */
	public static Date format2Date(String date) {
		if(date.contains("-")){
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}else if(date.contains("/"))
			formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Date();
	}

	
	/**
	 * 转为日期字符串
	 * 
	 * @param date
	 * @return
	 */
	public static String format2ShortDate(String date) {
		try {
			if (date != null)
				return date.substring(0, date.lastIndexOf("-") + 3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}
	public static String ToDBC(String input) {  
		   char[] c = input.toCharArray();  
		   for (int i = 0; i< c.length; i++) {  
		       if (c[i] == 12288) {  
		         c[i] = (char) 32;  
		         continue;  
		       }if (c[i]> 65280&& c[i]< 65375)  
		          c[i] = (char) (c[i] - 65248);  
		       }  
		   return new String(c);  
		}  

	/** 将时间戳转换为日期格式**/
	public static String getStrTime(long time)
	{
		String mStrTime = null;
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		mStrTime = formatter.format(new Date(time));
		return mStrTime;		
	}
	
	/** long time 转换成时间格式 00:00**/
	public static String intToTime(int time)
	{
		String minStr, secStr;
		StringBuffer timeStr = new StringBuffer();
		
		int min = time / 1000 / 60;
		int sec = time / 1000 % 60;
		if(min / 10 == 0)
		{
			minStr = "0" + min;
		}
		else
		{
			minStr = min + "";
		}
		if(sec / 10 == 0)
		{
			secStr = "0" + sec;
		}
		else
		{
			secStr = sec + "";
		}	
		timeStr.append(minStr);
		timeStr.append(":");
		timeStr.append(secStr);
		return timeStr.toString();
	}	
	/**
	 * 格式化日期
	 * 
	 * @param date
	 * @return
	 */
	public static String dateFormat(String date) {
		if (date == null)
			return null;
		long time = format2Date(date).getTime();
		long current = System.currentTimeMillis();
		long lastTime = current - time;
		if (lastTime <= 5 * 60 * 1000) {
			return MyApplication.getInstance().getResources().getString(R.string.just);
		} else if (lastTime < 60 * 60 * 1000) {
			return (int) (lastTime / (60 * 1000)) + MyApplication.getInstance().getResources().getString(R.string.before_min);
		} else if (lastTime < 24 * 60 * 60 * 1000 && lastTime >= 60 * 60 * 1000) {
			return (int) (lastTime / (60 * 60 * 1000)) + MyApplication.getInstance().getResources().getString(R.string.before_hour);
		} else
			return formatter.format(new Date(time));
	}
	public static String dateFormat2(long date) {
		long time =date;
		long current = System.currentTimeMillis();
		long lastTime = current - time;
		if (lastTime <= 5 * 60 * 1000) {
			return MyApplication.getInstance().getResources().getString(R.string.just);
		} else if (lastTime < 60 * 60 * 1000) {
			return (int) (lastTime / (60 * 1000)) + MyApplication.getInstance().getResources().getString(R.string.before_min);
		} else if (lastTime < 24 * 60 * 60 * 1000 && lastTime >= 60 * 60 * 1000) {
			return (int) (lastTime / (60 * 60 * 1000)) + MyApplication.getInstance().getResources().getString(R.string.before_hour);
		} else
			return formatter.format(new Date(time));
	}

	public static String apkSizeFormat(double dSize, String ext) {

		return String.valueOf(apkSizeFormat.format(dSize)) + ext;
	}

	private static NumberFormat apkSizeFormat = new DecimalFormat("0.##");

	/**
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public static byte[] decompress(byte[] source) throws IOException {
		ByteArrayInputStream bin = new ByteArrayInputStream(source);
		GZIPInputStream gis = new GZIPInputStream(bin);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int length = 0;
		while ((length = gis.read(buf)) > 0) {
			bos.write(buf, 0, length);
		}
		return bos.toByteArray();
	}

	public static byte[] compress(byte[] source) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(source);
		gzip.close();
		return out.toByteArray();
	}

	public static synchronized void addListData(MarketContext marketContext, String id, String ca) {
		ListBean lisBean = new ListBean();
		lisBean.id = id;
		lisBean.c = 1;
		lisBean.ca = ca;
		marketContext.opData(LogModel.DATA_INSERT, LogModel.DATA_LIST, lisBean);
	}

	public static synchronized void addListData(MarketContext marketContext, String id, String ca, int sc) {
		ListBean lisBean = new ListBean();
		lisBean.id = id;
		lisBean.sc = sc;
		lisBean.ca = ca;
		marketContext.opData(LogModel.DATA_INSERT, LogModel.DATA_LIST, lisBean);
	}

	public static synchronized void addListData(MarketContext marketContext, String id) {
		ListBean lisBean = new ListBean();
		lisBean.id = id;
		lisBean.c = 1;
		lisBean.ca = "0";
		marketContext.opData(LogModel.DATA_INSERT, LogModel.DATA_LIST, lisBean);
	}

	public static synchronized void addData(MarketContext marketContext, GAME game) {
		marketContext.opData(LogModel.DATA_INSERT, LogModel.DATA_GAME, game);
	}

	public static synchronized void addData(MarketContext marketContext, AppItem app) {
		marketContext.opData(LogModel.DATA_INSERT, LogModel.DATA_FAVORATE, app);
	}
	public static synchronized void addMyFavData(MarketContext marketContext, int sid) {
		marketContext.opData(LogModel.DATA_INSERT, LogModel.DATA_MYFAVORATE, sid);
	}
	public static synchronized void delMyData(MarketContext marketContext, int sid) {
		marketContext.opData(LogModel.DATA_DEL, LogModel.DATA_MYFAVORATE, sid);
	}
	public static synchronized void delAllMyData(MarketContext marketContext) {
		marketContext.opData(LogModel.DATA_DEL, LogModel.DATA_MYFAV, 0);
	}

	public static synchronized void delData(MarketContext marketContext, int sid) {
		marketContext.opData(LogModel.DATA_DEL, LogModel.DATA_FAVORATE, sid);
	}

	public static synchronized void addData(MarketContext marketContext, String key) {
		marketContext.opData(LogModel.DATA_INSERT, LogModel.DATA_KEY, key);
	}

	public static synchronized void delData(MarketContext marketContext, String key) {
		if (key != null)
			marketContext.opData(LogModel.DATA_DEL, LogModel.DATA_KEY, key);
		else
			marketContext.opData(LogModel.DATA_DEL, LogModel.DATA_ALL_KEY, null);
	}

	public static synchronized void addData(MarketContext marketContext, PUSH push) {
		marketContext.opData(LogModel.DATA_INSERT, LogModel.DATA_PUSH, push);
	}

	public static synchronized void addData(MarketContext marketContext, long t) {
		APP app = new APP();
		app.sc = 1;
		app.t = t;
		marketContext.opData(LogModel.DATA_INSERT, LogModel.DATA_APP, app);
	}

	public static synchronized void addData(MarketContext marketContext) {
		APP app = new APP();
		app.fc = 1;
		marketContext.opData(LogModel.DATA_INSERT, LogModel.DATA_APP, app);
	}

	public static String getLogPostData(Context context) {
		DataUtil dHelper = DataUtil.getInstance(context);
		APP app = dHelper.getAPP();
		List<GAME> games = dHelper.getGameList();
		List<ListBean> listbeans = dHelper.getListBeanList();
		PUSH push = dHelper.getPush();
		StringBuilder sb = new StringBuilder();
		String appData = "";
		if (app != null)
			appData = app.sc + "_" + app.fc + "_" + app.t;
		StringBuilder sbGData = new StringBuilder();
		if (games != null && games.size() > 0) {
			for (GAME game : games) {
				sbGData.append(game.app_id).append("_").append(game.list_id).append("_").append(game.list_c).append("_")
						.append(game.d_c).append("_").append(game.d_o).append("_").append(game.d_f).append("_").append(game.d_d)
						.append("_").append(game.i_o).append("_").append(game.i_f).append("|");
			}
		}
		String sGData = sbGData.toString();
		if (sGData.length() == 0)
			sGData = "";
		StringBuilder sbLData = new StringBuilder();
		if (listbeans != null && listbeans.size() > 0) {
			for (ListBean listBean : listbeans) {
				sbLData.append(listBean.id).append("_").append(listBean.ca).append("_").append(listBean.c).append("_")
						.append(listBean.sc).append("|");
			}
		}

		String sLData = sbLData.toString();
		if (sLData.length() == 0)
			sLData = "";

		String pushData = "";

		if (push != null) {
			int id = push.id;
			int state = push.state;
			pushData = id + "_" + state;
			if (push.type == 2) {
				if (listbeans != null) {
					StringBuilder sbPush = new StringBuilder();
					for (GAME game : games) {
						if (game.list_id != null && game.list_id.equals(LogModel.L_PUSH) && game.app_id == push.appid) {
							sbPush.append(game.app_id).append("_").append(game.d_c).append("_").append(game.d_o).append("_")
									.append(game.d_f).append("_").append(game.d_d).append("_").append(game.i_o).append("_")
									.append(game.i_f);
							break;
						}
					}
					String pushApkData = sbPush.toString();
					if (pushApkData.length() > 0)
						pushData = pushData + "_" + pushApkData;
				}
			}
		}
		sb.append(appData).append("/").append(sGData).append("/").append(sLData).append("/").append(pushData);

		String rs = sb.toString();
		if (rs != null && rs.length() > 0 && rs.contains("_"))
			return rs;
		return null;

	}

	public static void getDataPointMap(Context context) {
		DataUtil dHelper = DataUtil.getInstance(context);
		HashMap<String, Integer> map = dHelper.getDP();
		if (map != null) {
			if (LogModel.hasMap != null)
				LogModel.hasMap.clear();
			LogModel.hasMap = map;
		}
	}

	/**
	 * 给Url加密
	 * 
	 * @param content
	 * @return
	 */
	public static String encodeContentForUrl(String content) {
		String ReStr = "";
		if (content != null) {
			try {
				ReStr = URLEncoder.encode(content, "UTF-8");

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return ReStr;

	}

	/**
	 * 取得评论图形
	 * 
	 * @param d
	 * @return
	 */
	public static float getFloatRateVaue(double d) {
		return (float) d;// 0x4000000000000000L
	}

	/**
	 * 取得详情评分
	 * 
	 * @param d
	 * @return
	 */
	public static float getDrawRateVaue(double d) {
		return ((int) Math.ceil(d * 2)) * 1.0f / 2;// 0x4000000000000000L
	}

	/**
	 * 根据类型来获得通用请求信息
	 */
	public static HashMap<String, String> getUplodMap(Context context, String type) {
		HashMap<String, String> map = new HashMap<String, String>();
		String imei = Util.getIMEI(context);
		String model = Util.getModel();
		String api = getApi(context);
		String code = Util.getVcode(context);
		imie = imei;
		mobile = model;
		vcode = code;

		String net = Util.getNetAvialbleType(context);
		screeSize = Util.getScreenSize((Activity) context);
		channel = Util.getChannelName(context);
		map.put("type", type);
		map.put("imei", imei);
		map.put("model", model);
		map.put("net", net);
		map.put("api", api);
		map.put("vcode", vcode);
		map.put("reso", screeSize); // 获取 屏幕大小
		map.put("channel", channel); // 获取 渠道名称
		return map;
	}

	public static String getApi(Context context) {
		String wel = getWelVer(context);
		String vcode = getVcode(context);
		int pcode = getPcode(context);
		if (wel == null)
			wel = "-1";
		StringBuilder sb = new StringBuilder();
		sb.append(8).append(":").append(wel).append("|");
		sb.append(9).append(":").append(vcode).append("|");
		sb.append(10).append(":").append(pcode);
		String api = sb.toString();
		return api;
	}

	public static String getIMEI(Context context) {
		final TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId();
		return imei;
	}

	public static String getIMSI(Context context) {
		final TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mTelephonyMgr.getSubscriberId();
		return imsi;
	}
	//sim卡是否可读
	public static boolean isCanUseSim(Context context) {
		try {
			TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			return TelephonyManager.SIM_STATE_READY == mgr
					.getSimState();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 获取手机型号，如 "HTC Desire"等
	 * **/
	public static String getModel() {
		return Build.MODEL;
	}

	public static String getNetAvialbleType(Context context) {
		String net_type = "null";
		try {
			ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			// 获取代表联网状态的NetWorkInfo对象
			NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
			if (networkInfo == null) {
				return "";
			}
			// 获取当前的网络连接是否可用
			boolean available = networkInfo.isAvailable();
			if (available) {
			} else {
				net_type = "";
			}

			android.net.NetworkInfo.State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
			if (android.net.NetworkInfo.State.CONNECTED == state) {
				net_type = "gprs";
			}

			state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
			if (android.net.NetworkInfo.State.CONNECTED == state) {
				net_type = "wifi";
			}
		} catch (Exception ee) {

		}
		return net_type;
	}

	public static String getScreenSize(Activity context) {
		String size = "";
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		int w = mDisplayMetrics.widthPixels;
		int h = mDisplayMetrics.heightPixels;
		size = w + "x" + h;
		return size;
	}
	
	public static String getJPushID(Context context){
		String jpushID = JPushInterface.getRegistrationID(context);
		if(jpushID!=null){
			return JPushInterface.getRegistrationID(context);
		}else{
			return "";
		}
	}
	public static String getChannelName(Context context){
		 ApplicationInfo appInfo;
		 String msg="";
		try {
			appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
			 PackageManager.GET_META_DATA);
			//msg=appInfo.metaData.getInt("UMENG_CHANNEL");
			msg=appInfo.metaData.getString("UMENG_CHANNEL");
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(msg.startsWith("um")){
			msg = msg.substring(2, msg.length());
		}
		return msg;
	}

	public static String getVcode(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return String.valueOf(packageInfo.versionCode);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String getWelVer(Context context) {
		LoadPage loadPage = LoginDBUtil.getLoadPage(context);
		if (loadPage == null)
			return null;
		return String.valueOf(loadPage.code);
	}

	private static int getPcode(Context context) {
		SharedPreferences sp = context.getSharedPreferences("push", Context.MODE_PRIVATE);
		return sp.getInt("pid", 0);
	}

	public static boolean isInstalled(Context context, String pkg) {
		List<PackageInfo> pis = context.getPackageManager().getInstalledPackages(0);
		for (PackageInfo packageInfo : pis) {
			if (packageInfo.packageName.equals(pkg))
				return true;
		}
		return false;
	}

	public static boolean canSysMount() {
		String[] commands = new String[] { "busybox mount -o remount,rw /system" + "\n" };
		boolean res = false;
		final List<Boolean> result = new ArrayList<Boolean>();
		try {
			RootTools.sendShell(commands, 50, new Result() {

				@Override
				public void processError(String line) throws Exception {
					// write: No space left on device
					if (line != null && line.contains("Read-only file system")) {
						result.add(false);
					}
				}

				@Override
				public void process(String line) throws Exception {
				}

				@Override
				public void onFailure(Exception ex) {
				}

				@Override
				public void onComplete(int diag) {
				}
			}, true, true);
		} catch (InterruptedException e) {
			result.add(false);
		}
		if (!result.contains(Boolean.valueOf(false))) {
			res = true;
		}
		return res;
	}

	public static boolean installSlefSystem(final Context context, String path, String pname) {
		if (!Constants.isCommandExist(Constants.SU_COMMAND_NAME))
			return false;
		if (!canSysMount())
			return false;
		String[] commands = new String[] { "chmod 644 " + path + File.separator + pname + "\n",
				"chown root.root " + path + File.separator + pname + "\n", "busybox mount -o remount,rw /system" + "\n",
				"rm /system/app/" + pname + "\n", "rm /data/dalvik-cache/system@app@" + pname + "@classes.dex\n",
				"busybox cp -p " + path + File.separator + pname + " /system/app/" + pname + "\n" };
		boolean res = false;
		final List<Boolean> result = new ArrayList<Boolean>();
		try {
			RootTools.sendShell(commands, 50, new Result() {

				@Override
				public void processError(String line) throws Exception {
					// write: No space left on device
					if ("write: No space left on device".equals(line)) {
						result.add(false);
					} else if (line != null && line.contains("No such file or directory")) {
						result.add(false);
					} else if (line != null && line.contains("Read-only file system")) {
						result.add(false);
					}
				}

				@Override
				public void process(String line) throws Exception {
				}

				@Override
				public void onFailure(Exception ex) {
				}

				@Override
				public void onComplete(int diag) {
				}
			}, true, true);
		} catch (InterruptedException e) {
			result.add(false);
		}
		if (!result.contains(Boolean.valueOf(false))) {
			res = true;
		}
		return res;
	}

	public static void saveFile(OutputStream os, String content) {
		try {
			os.write(content.getBytes());
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String readFile(InputStream is) {
		byte[] buff = new byte[512];
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int tag = -1;
		try {
			while ((tag = is.read(buff)) != -1) {
				os.write(buff, 0, tag);
				os.flush();
			}
			return new String(os.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	public static int gamesInWhere(Context context) {
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		for (PackageInfo package1 : packages) {
			if (package1.packageName.equals(Constants.PKG)) {
				if ((package1.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1)
					return 1;
				else
					return 0;
			}
		}
		return 0;
	}

	/**
	 * 下载 量格式化
	 * 
	 * @param dNum
	 * @param ext
	 * @return
	 */
	public static String formatDnum(int dNum, String ext) {
		StringBuilder formatData = new StringBuilder();
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		df.setMaximumFractionDigits(1);
		if (dNum >= 0 && dNum < 100000) {
			formatData.append(dNum);
		} else if (dNum >= 100000 ) {
			formatData.append(100000+"+");
			/*formatData.append(df.format((int)(dNum * 1.0 / 10000)) + MyApplication.getInstance().getResources().getString(R.string.myriad)).append(ext);*/
		}/* else if (dNum >= 100000 && dNum < ) {
			formatData.append(df.format(dNum * 1.0 / 100000) + MyApplication.getInstance().getResources().getString(R.string.myriad)).append(ext);
		} else if (dNum >= 1000000 && dNum < 10000000) {
			formatData.append(df.format(dNum * 1.0 / 1000000) + MyApplication.getInstance().getResources().getString(R.string.myriad)).append(ext);
		} else if (dNum >= 10000000 && dNum < 100000000) {
			formatData.append(df.format(dNum * 1.0 / 10000000) + MyApplication.getInstance().getResources().getString(R.string.ten_million)).append(ext);
		} else if (dNum >= 100000000) {
			formatData.append(df.format(dNum * 1.0 / 100000000) + MyApplication.getInstance().getResources().getString(R.string.calculate)).append(ext);
		}*/
		return formatData.toString();
	}
	public static String formatVideoDnum(int dNum) {
		String down = null;
		if (dNum >= 0 && dNum < 100000) {
			down=""+dNum;
		} else if (dNum >= 100000 ) {
			down=(dNum+"+");
		}
		return down;
	}
	public static void killApps(Context context) {

		ActivityManager activityManger = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi1 = new MemoryInfo();
		activityManger.getMemoryInfo(mi1);// 201613312
		List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				ActivityManager.RunningAppProcessInfo apinfo = list.get(i);

				System.out.println("pid            " + apinfo.pid);
				System.out.println("processName              " + apinfo.processName);
				System.out.println("importance            " + apinfo.importance);
				String[] pkgList = apinfo.pkgList;

				if (apinfo.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
					for (int j = 0; j < pkgList.length; j++) {
						// 2.2以上是过时的,请用killBackgroundProcesses代替
						// activityManger.restartPackage(pkgList[j]);
						activityManger.killBackgroundProcesses(pkgList[j]);
					}
				}
			}
		}
		MemoryInfo mi2 = new MemoryInfo();
		activityManger.getMemoryInfo(mi2);
		long availMem = mi2.availMem - mi1.availMem;
		if (availMem < 0) {
			availMem = 0;
		}
	}

	public static File startDownload(Context context, String dowUrl) throws Exception {
		int fileSize = 0;
		int readSize = 0;
		int downSize = 0;
		boolean cancelled = false;
		File downFile = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			URL myURL = new URL(dowUrl);
			URLConnection conn = myURL.openConnection();
			conn.connect();
			fileSize = conn.getContentLength();
			String apkFilename = Constants.APKNAME;
			// 建立临时文件
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				if (DownloadUtils.getSDCardAvailableSpace() <= fileSize) {
					downFile = new File(context.getFilesDir() + File.separator + apkFilename);
				} else {
					String downloadDir = MyApplication.DATA_DIR;
					File file = new File(downloadDir);
					if (!file.exists()) {
						file.mkdirs();
					}
					downFile = new File(file.getAbsolutePath() + File.separator + apkFilename);
				}
			} else {
				downFile = new File(context.getFilesDir() + File.separator + apkFilename);
			}
			is = conn.getInputStream();

			if (fileSize <= 0)
				throw new RuntimeException("filesize is 0");
			if (is == null) {
				throw new RuntimeException("stream is null");
			}
			if (downFile.exists())
				downFile.delete();
			fos = new FileOutputStream(downFile);
			byte buf[] = new byte[1024 * 1024];
			while (!cancelled && (readSize = is.read(buf)) != -1) {
				fos.write(buf, 0, readSize);
				downSize += readSize;
				if (downSize == fileSize) {
					cancelled = true;
					return downFile;
				}
			}
		} catch (MalformedURLException e) {
			if (downFile != null && downFile.exists())
				downFile.delete();
			e.printStackTrace();
		} finally {
			try {
				if (null != fos)
					fos.close();
				if (null != is)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return downFile;
	}
	
	public   static  String bytes2kb( long  bytes)  {
        BigDecimal filesize  =   new  BigDecimal(bytes);
        BigDecimal megabyte  =   new  BigDecimal( 1024 * 1024 );
         float  returnValue  =  filesize.divide(megabyte,  2 , BigDecimal.ROUND_UP).floatValue();
         if  (returnValue  >   1 )
             return (returnValue  +   "  MB " );
        BigDecimal kilobyte  =   new  BigDecimal( 1024 );
        returnValue  =  filesize.divide(kilobyte,  2 , BigDecimal.ROUND_UP).floatValue();
         return (returnValue  +   "  KB " );
	}
	
	/**防暴力点击**/
	private static long lastClickTime = 0;
	public static boolean isFastDoubleClick() 
	{
	    long time = System.currentTimeMillis();
	    long timeD = time - lastClickTime;
	    if ( 0 < timeD && timeD < 1000) 
	    {  
	        return true;  
	    }  
	    lastClickTime = time;  
	    return false;  
	}
	
	 //获取 wifMAC地址
	public static String getMAC(Context context){
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
        WifiInfo info = wifi.getConnectionInfo();  
        String mac = info.getMacAddress();
        if(mac == null){
        	mac = "";
        }
        return mac; 
    }
	
	public static boolean isAppOnForeground() { 
	    // Returns a list of application processes that are running on the 
	    // device 
	  
	    ActivityManager activityManager = (ActivityManager) MyApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE); 
	    String packageName = MyApplication.getInstance().getPackageName(); 
	  
	    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager 
	        .getRunningAppProcesses(); 
	    if (appProcesses == null) 
	      return false; 
	  
	    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) { 
	      // The name of the process that this object is associated with. 
	      if (appProcess.processName.equals(packageName) 
	          && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) { 
	        return true; 
	      } 
	    } 
	    return false; 
	  } 
	/**
	 * 
	 */
	
	public static boolean isApplicationBroughtToBackground(Context context) { 
	    ActivityManager am = (ActivityManager) context 
	            .getSystemService(Context.ACTIVITY_SERVICE); 
	    List<RunningTaskInfo> tasks = am.getRunningTasks(1); 
	    if (tasks != null && !tasks.isEmpty()) { 
	      ComponentName topActivity = tasks.get(0).topActivity; 
	    //  Debug.i(TAG, "topActivity:" + topActivity.flattenToString()); 
	    //  Debug.f(TAG, "topActivity:" + topActivity.flattenToString()); 
	      if (!topActivity.getPackageName().equals(context.getPackageName())) { 
	        return true; 
	      } 
	    } 
	    return false; 
	  } 
	
	@SuppressWarnings("null")
	public static boolean isBackgroundRunning(Context context) {
	   // mContext = context.getApplicationContext();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    String packageName = context.getPackageName();
	    List<RunningTaskInfo> list = activityManager.getRunningTasks(100);
	    if (list == null && list.size() == 0) return false;
	    for (RunningTaskInfo info : list) {
	        if (info.topActivity.getPackageName().equals(packageName) || info.baseActivity.getPackageName().equals(packageName)) {
	            return true;
	        }
	    }
	    return false;
	}
}
