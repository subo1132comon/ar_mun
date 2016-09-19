package com.byt.market.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataDatabaseHelper extends SQLiteOpenHelper {

	public DataDatabaseHelper(Context context) {
		super(context, "data.db", null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(LIST.createTable());
		db.execSQL(GAME.createTable());
		db.execSQL(PUSH.createTable());
		db.execSQL(MAPP.createTable());
		db.execSQL(DP.createTable());
		db.execSQL(SK.createTable());
		db.execSQL(USER.createTable());
		db.execSQL(FAVORATE.createTable());
		db.execSQL(MYFAVORATE.createTable());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS list");
		db.execSQL("DROP TABLE IF EXISTS game");
		db.execSQL("DROP TABLE IF EXISTS push");
		db.execSQL("DROP TABLE IF EXISTS mapp");
		db.execSQL("DROP TABLE IF EXISTS dp");
		db.execSQL("DROP TABLE IF EXISTS search_key");
		db.execSQL("DROP TABLE IF EXISTS user");
		db.execSQL("DROP TABLE IF EXISTS faviorate");
		db.execSQL("DROP TABLE IF EXISTS myfaviorate");
		

		db.execSQL(LIST.createTable());
		db.execSQL(GAME.createTable());
		db.execSQL(PUSH.createTable());
		db.execSQL(MAPP.createTable());
		db.execSQL(DP.createTable());
		db.execSQL(SK.createTable());
		db.execSQL(USER.createTable());
		db.execSQL(FAVORATE.createTable());
		db.execSQL(MYFAVORATE.createTable());
	}

	/**
	 * 游戏
	 * 
	 * @author xsl
	 * 
	 */
	public static class GAME {
		public static final String TABLE = "game";

		public static final String APP_ID = "app_id";// 软件id
		public static final String LIST_ID = "list_id";// 所在列表
		public static final String LIST_CATE = "list_c";// 所在列表
		public static final String DOWN_C = "d_c";
		public static final String DOWN_O = "d_o";
		public static final String DOWN_F = "d_f";
		public static final String DOWN_D = "d_d";
		public static final String INSTALL_O = "i_o";
		public static final String INSTALL_F = "i_f";

		public static String createTable() {
			StringBuilder sb = new StringBuilder();
			sb.append("create table game (_id integer primary key autoincrement,");
			sb.append(APP_ID).append(" integer,");
			sb.append(LIST_ID).append(" text,");
			sb.append(LIST_CATE).append(" text,");
			sb.append(DOWN_C).append(" integer,");
			sb.append(DOWN_O).append(" integer,");
			sb.append(DOWN_F).append(" integer,");
			sb.append(DOWN_D).append(" integer,");
			sb.append(INSTALL_O).append(" integer,");
			sb.append(INSTALL_F).append(" integer)");
			return sb.toString();
		}
	}

	/**
	 * 游戏
	 * 
	 * @author xsl
	 * 
	 */
	public static class LIST {
		public static final String TABLE = "list";

		public static final String LIST_ID = "list_id";// 列表标示
		public static final String LIST_C = "list_c";
		public static final String LIST_SC = "list_sc";
		public static final String LIST_CA = "list_ca";

		public static String createTable() {
			StringBuilder sb = new StringBuilder();
			sb.append("create table list (_id integer primary key autoincrement,");
			sb.append(LIST_ID).append(" text,");
			sb.append(LIST_C).append(" integer,");
			sb.append(LIST_SC).append(" integer,");
			sb.append(LIST_CA).append(" text)");
			return sb.toString();
		}
	}

	/**
	 * 游戏
	 * 
	 * @author xsl
	 * 
	 */
	public static class PUSH {
		public static final String TABLE = "push";

		public static final String PUSH_ID = "p_id";// 消息id
		public static final String PUSH_STATE = "p_state";
		public static String PUSH_APPID = "appid";
		public static String PUSH_TYPE = "type";

		public static String createTable() {
			StringBuilder sb = new StringBuilder();
			sb.append("create table push (_id integer primary key autoincrement,");
			sb.append(PUSH_ID).append(" integer,");
			sb.append(PUSH_APPID).append(" integer,");
			sb.append(PUSH_TYPE).append(" integer,");
			sb.append(PUSH_STATE).append(" integer)");
			return sb.toString();
		}
	}

	/**
	 * 游戏
	 * 
	 * @author xsl
	 * 
	 */
	public static class MAPP {
		public static final String TABLE = "mapp";

		public static final String MAPP_SC = "mapp_sc";
		public static final String MAPP_FC = "mapp_fc";
		public static final String MAPP_T = "mapp_t";

		public static String createTable() {
			StringBuilder sb = new StringBuilder();
			sb.append("create table mapp (_id integer primary key autoincrement,");
			sb.append(MAPP_SC).append(" integer,");
			sb.append(MAPP_FC).append(" integer,");
			sb.append(MAPP_T).append(" integer)");
			return sb.toString();
		}
	}

	public static class DP {
		public static final String TABLE = "dp";

		public static final String DP_K = "dp_k";
		public static final String DP_V = "dp_v";

		public static String createTable() {
			StringBuilder sb = new StringBuilder();
			sb.append("create table dp (_id integer primary key autoincrement,");
			sb.append(DP_K).append(" text,");
			sb.append(DP_V).append(" integer)");
			return sb.toString();
		}
	}

	public static class SK {
		public static final String TABLE = "search_key";

		public static final String SK_K = "sk_k";

		public static String createTable() {
			StringBuilder sb = new StringBuilder();
			sb.append("create table search_key (_id integer primary key autoincrement,");
			sb.append(SK_K).append(" text)");
			return sb.toString();
		}
	}

	public static class USER {
		public static final String TABLE = "user";

		public static final String TYPE = "type";
		public static final String NICKNAME = "nickname";
		public static final String ICON = "icon";
		public static final String UID = "uid";

		public static String createTable() {
			StringBuilder sb = new StringBuilder();
			sb.append("create table user (_id integer primary key autoincrement,");
			sb.append(TYPE).append(" integer,");
			sb.append(NICKNAME).append(" text,");
			sb.append(ICON).append(" text,");
			sb.append(UID).append(" text)");
			return sb.toString();
		}
	}
	public static class MYFAVORATE {
		public static final String TABLE = "myfaviorate";
		public static final String sid = "ssid";
		public static String createTable() {
			StringBuilder sb = new StringBuilder();
			sb.append("create table myfaviorate (_id integer primary key autoincrement,");
		
			sb.append(sid).append(" integer)");
			return sb.toString();
		}
	}
	public static class FAVORATE {
		public static final String TABLE = "faviorate";


		// apk属性
		public static final String iconUrl = "iconUrl";
		public static final String name = "name";
		public static final String pname = "pname";
		public static final String vname = "vname";
		public static final String vcode = "vcode";
		public static final String length = "length";
		public static final String hash = "hash";
		public static final String sid = "sid";
		public static final String apk = "apk";
		public static final String downNum = "downNum";
		public static final String adesc = "adesc";
		public static final String stype = "stype";
		public static final String rating = "rating";
		public static final String cateid = "cateid";
		public static final String cateName = "cateName";
		public static final String state = "state";
		public static final String rankcount = "rankcount";
		public static final String commcount = "commcount";
		public static final String updatetime = "updatetime";
		public static final String list_id = "list_id";
		public static final String list_cateid = "list_cateid";
		public static final String app_level = "app_level";
		public static String createTable() {
			StringBuilder sb = new StringBuilder();
			sb.append("create table faviorate (_id integer primary key autoincrement,");
			sb.append(iconUrl).append(" text,");
			sb.append(name).append(" text,");
			sb.append(pname).append(" text,");
			sb.append(vname).append(" text,");
			sb.append(vcode).append(" integer,");
			sb.append(length).append(" integer,");
			sb.append(hash).append(" text,");
			sb.append(sid).append(" integer,");
			sb.append(apk).append(" text,");
			sb.append(downNum).append(" text,");
			sb.append(adesc).append(" text,");
			sb.append(stype).append(" integer,");
			sb.append(rating).append(" integer,");
			sb.append(cateid).append(" integer,");
			sb.append(cateName).append(" text,");
			sb.append(state).append(" integer,");
			sb.append(rankcount).append(" integer,");
			sb.append(commcount).append(" integer,");
			sb.append(updatetime).append(" text,");
			sb.append(list_id).append(" text,");
			sb.append(app_level).append(" integer,");
			sb.append(list_cateid).append(" text)");
			return sb.toString();
		}
	}

	public synchronized SQLiteDatabase getReadableDb() {
		return getReadableDatabase();
	}

	public synchronized SQLiteDatabase getWritableDb() {
		return getWritableDatabase();
	}
}
