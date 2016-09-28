package com.byt.market.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class MDBHelper extends SQLiteOpenHelper {

	public static final String TABLE_FAVORITES = "favorites";
	private static final String CREATE_APP_DOWNLOAD_TABLE = "create table app_download (_id integer primary key autoincrement,app_id int,pname text,name text,version text,version_code int,durl text,iurl text,state integer, dsize real,size real,realSize real,free text,purchase text,list_cate_id text,list_id text)";
	private static final String Create_App_Install_Table = "create table software_installed (_id integer primary key autoincrement,pname text,version_code int,update_state int,lus_time real)";
//	private static final String CREATE_FAVORITES_TABLE = "create table "
//			+ TABLE_FAVORITES
//			+ "(_id integer primary key autoincrement,app_id integer,name text,iurl text, size real, apkPath text, free text, packageName text, price real, purchase text, state integer,updatable integer,realsize real, version text, versioncode integer, hostname text, dunm integer)";
	
	private static final String CREATELOADPAGE = "create table "
			+ LoadPage.LoadPage
			+ "(_id integer primary key autoincrement,url text, code integer)";
	private static final String CREATEMARKETUPDATE = "create table "
			+ MarketUpdate.MarketUpdate
			+ "(_id integer primary key autoincrement,url text, code integer)";
	
	private static final String CREATE_LOG = "create table IF NOT EXISTS log_info (_id integer primary key autoincrement,type text,ident text,time text)";

	public static final String DB_NAME = "dkgames.db";
	public static final int DB_VERSION = 10;
	public static final String TAG = "MDBHelper";

	public MDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqldb) {
		// TODO Auto-generated method stub
//		sqldb.execSQL(getCreateNewAppDownloadTable());
		sqldb.execSQL(CREATE_APP_DOWNLOAD_TABLE);
		sqldb.execSQL(Create_App_Install_Table);
//		sqldb.execSQL(CREATE_FAVORITES_TABLE);
		sqldb.execSQL(CREATE_LOG);
		sqldb.execSQL(CREATELOADPAGE);
		sqldb.execSQL(CREATEMARKETUPDATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqldb, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		sqldb.execSQL("DROP TABLE IF EXISTS app_download");
		sqldb.execSQL("DROP TABLE IF EXISTS software_installed");
//		sqldb.execSQL("DROP TABLE IF EXISTS favorites");
		sqldb.execSQL("DROP TABLE IF EXISTS log_info");
		sqldb.execSQL("DROP TABLE IF EXISTS LoadPage");
		sqldb.execSQL("DROP TABLE IF EXISTS MarketUpdate");
		
		sqldb.execSQL(CREATE_APP_DOWNLOAD_TABLE);
		sqldb.execSQL(Create_App_Install_Table);
//		sqldb.execSQL(CREATE_FAVORITES_TABLE);
		sqldb.execSQL(CREATE_LOG);
		sqldb.execSQL(CREATELOADPAGE);
		sqldb.execSQL(CREATEMARKETUPDATE);
	}

	private static final String Create_App_Install_TableR = "create table IF NOT EXISTS  software_installed (_id integer primary key autoincrement,pname text,version_code int,update_state int,lus_time real)";

	public void checkLOGtable(SQLiteDatabase sqldb) {
		sqldb.execSQL(CREATE_LOG);
	}

	public void checkInstallTableExist(SQLiteDatabase sqldb) {
		sqldb.execSQL(Create_App_Install_TableR);
	}
	
	/**
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
		public static final String REALSIZE = "realsize";
		public static final String STATE = "state";
		public static final String TABLE_NAME = "app_download";
		public static final String VERSION = "version";
		public static final String VERSION_CODE = "version_code";
		public static final String FREE = "free";
		public static final String PURCHASE = "purchase";
		public static final String LIST_CATE_ID = "list_cate_id";
		public static final String LIST_ID = "list_id";
	}
	
	/**
	 * 
	 * @author Administrator
	 * 
	 */
	public class NewAppDownload implements BaseColumns {
		public static final String NEW_DOWNLOAD_TABLE = "new_app_download";
		public static final String _ID = "_id";
		public static final String APP_ID = "app_id";
		public static final String DURL = "durl";
		public static final String IURL = "iurl";
		public static final String NAME = "name";
		public static final String SIZE = "size";
		public static final String PNAME = "pname";
		public static final String VERSION = "version";
		public static final String VERSION_CODE = "version_code";
		public static final String DSIZE = "dsize";
		public static final String REALSIZE = "realsize";
		public static final String STATE = "state";
	}
	
	/**
	 * loadpage
	 * 
	 * @author Administrator
	 * 
	 */
	public class LoadPage implements BaseColumns {
		public static final String LoadPage = "LoadPage";
		public static final String _ID = "_id";
		public static final String URL = "url";
		public static final String CODE = "code";
	}
	
	/**
	 * update
	 * 
	 * @author Administrator
	 * 
	 */
	public class MarketUpdate implements BaseColumns {
		public static final String MarketUpdate = "MarketUpdate";
		public static final String _ID = "_id";
		public static final String URL = "url";
		public static final String CODE = "code";
	}
	
	public synchronized SQLiteDatabase getWritableDb(){
		return getWritableDatabase();
	}

}
