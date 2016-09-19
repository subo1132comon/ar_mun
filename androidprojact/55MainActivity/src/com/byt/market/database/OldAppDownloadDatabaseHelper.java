package com.byt.market.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class OldAppDownloadDatabaseHelper extends SQLiteOpenHelper {

	public OldAppDownloadDatabaseHelper(Context context) {
		super(context, "uc_market_new.db", null, 10);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	public static final String TABLE_APP_DOWNLOAD = "app_download";
	/**
	 * v1.0 下载实体类
	 * v1.1 升级保留下载列表使用
	 * 
	 * @author Administrator
	 * 
	 */
	public class TAppDownload implements BaseColumns {
		
		public static final String APP_ID = "app_id";
		public static final String DSIZE = "dsize";
		public static final String DURL = "durl";
		public static final String IURL = "iurl";
		public static final String NAME = "name";
		public static final String PNAME = "pname";
		public static final String SIZE = "size";
		public static final String REALSIZE = "realSize";
		public static final String STATE = "state";
		public static final String VERSION = "version";
		public static final String VERSION_CODE = "version_code";
		public static final String FREE = "free";
		public static final String PURCHASE = "purchase";
		public static final String LIST_CATEID = "list_cate_id";
		public static final String LIST_ID = "list_id";
	}
	public synchronized SQLiteDatabase getReadableDb() {
		return getReadableDatabase();
	}

	public synchronized SQLiteDatabase getWritableDb() {
		return getWritableDatabase();
	}
}
