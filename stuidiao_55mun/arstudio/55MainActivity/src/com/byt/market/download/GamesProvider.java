/**
 * @author jiangxiaoliang
 * 实现ContentProvider 用来访问数据库
 */

package com.byt.market.download;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.byt.market.MyApplication;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadItem;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.util.DataUtil;
import com.byt.market.util.FileUtil;
import com.byt.market.util.LogUtil;
import com.byt.market.util.SecurityUtil;

public class GamesProvider extends ContentProvider {
	public static final int DATEBASE_VERSION = 2;

	public static final String DATABASE_NAME = "games.db";
	public static final String DATABASE_AUTHORITY = "com.byt.market.database";

	public static final String DATABASE_TABLE_DOWNLOAD_TASK = "app_download"; // 下载列表

	public static final String ALL_GAMES_NAME = "all_games.db";
	public static final String ALL_GAMES_TABLE = "all_games";
	private static final String[] TABLE_NAMES = { DATABASE_TABLE_DOWNLOAD_TASK, };

	private static final UriMatcher mUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int BASE_SHIFT = 8; // 8 bits to the base type: 0,
												// 0x100, 0x200, etc.

	private static final int DOWNLOAD_TASK_BASE = 0x000;
	private static final int DOWNLOAD_TASK = DOWNLOAD_TASK_BASE;
	private static final int DOWNLOAD_TASK_ID = DOWNLOAD_TASK + 1;
	private static final int ALL_GAMES = DOWNLOAD_TASK + 2;
	private static SQLiteDatabase mDatabase;

	static {
		mUriMatcher.addURI(DATABASE_AUTHORITY, DATABASE_TABLE_DOWNLOAD_TASK,
				DOWNLOAD_TASK);
		mUriMatcher.addURI(DATABASE_AUTHORITY, DATABASE_TABLE_DOWNLOAD_TASK
				+ "/#", DOWNLOAD_TASK_ID);
		mUriMatcher.addURI(DATABASE_AUTHORITY, ALL_GAMES_TABLE, ALL_GAMES);
	}

	public static synchronized SQLiteDatabase getDatabase(Context context) {
		if (mDatabase != null) {
			return mDatabase;
		}
		DatabaseHelper helper = new DatabaseHelper(context);
		mDatabase = helper.getWritableDatabase();
		return mDatabase;
	}

	private static synchronized SQLiteDatabase getDatabase(Context context,
			int matchID) {
		if (matchID == ALL_GAMES) {
			return getAllGamesDB(context);
		} else {
			return getDatabase(context);
		}
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	protected void notifyChange(Uri uri, ContentObserver observer) {
		getContext().getContentResolver().notifyChange(uri, observer);
	}

	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		Context sContext = getContext();
		SQLiteDatabase sDatabase = getDatabase(sContext);
		sDatabase.beginTransaction();// 开启事务
		try {
			ContentProviderResult[] results = super.applyBatch(operations);
			sDatabase.setTransactionSuccessful();// 设置事务标记为successful
			notifyChange(DownloadContent.CONTENT_URI, null);
			return results;
		} finally {
			sDatabase.endTransaction();
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case DOWNLOAD_TASK:
			return "vnd.android.cursor.dir/" + DATABASE_TABLE_DOWNLOAD_TASK;
		case DOWNLOAD_TASK_ID:
			return "vnd.android.cursor.item/" + DATABASE_TABLE_DOWNLOAD_TASK;
		case ALL_GAMES:
			return "vnd.android.cursor.item/" + ALL_GAMES_TABLE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		final int match = mUriMatcher.match(uri);
		int tableIndex = match >> BASE_SHIFT;
		Context sContext = getContext();
		SQLiteDatabase sDatabase = getDatabase(sContext, match);
		Uri notificationUri = DownloadContent.CONTENT_URI;
		Cursor sCursor = null;

		switch (match) {
		case DOWNLOAD_TASK:
			sCursor = sDatabase.query(TABLE_NAMES[tableIndex], projection,
					selection, selectionArgs, null, null, sortOrder);
			break;

		case DOWNLOAD_TASK_ID:
			long id = ContentUris.parseId(uri);
			sCursor = sDatabase.query(TABLE_NAMES[tableIndex], projection,
					whereWithId(id, selection), selectionArgs, null, null,
					sortOrder);
			break;
		case ALL_GAMES:
			sCursor = sDatabase.query(ALL_GAMES_TABLE, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if ((sCursor != null) && isTemporary()) {
			sCursor.setNotificationUri(sContext.getContentResolver(),
					notificationUri);
		}
		return sCursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int rows = -1;
		try{
		final int match = mUriMatcher.match(uri);
		int tableIndex = match >> BASE_SHIFT;
		Context sContext = getContext();
		SQLiteDatabase sDatabase = getDatabase(sContext, match);
		

		switch (match) {
		case DOWNLOAD_TASK:
			rows = sDatabase.update(TABLE_NAMES[tableIndex], values, selection,
					selectionArgs);
			break;

		case DOWNLOAD_TASK_ID:
			long id = ContentUris.parseId(uri);
			rows = sDatabase.update(TABLE_NAMES[tableIndex], values,
					whereWithId(id, selection), selectionArgs);
			break;

		case ALL_GAMES:
			rows = sDatabase.update(ALL_GAMES_TABLE, values, selection,
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (rows > 0)
			notifyChange(uri, null);
		}catch(Exception e){
			
		}
		return rows;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final int match = mUriMatcher.match(uri);
		int tableIndex = match >> BASE_SHIFT;
		Context sContext = getContext();
		SQLiteDatabase sDatabase = getDatabase(sContext, match);
		int rows = -1;

		switch (match) {
		case DOWNLOAD_TASK:
			rows = sDatabase.delete(TABLE_NAMES[tableIndex], selection,
					selectionArgs);
			break;

		case DOWNLOAD_TASK_ID:
			long id = ContentUris.parseId(uri);
			rows = sDatabase.delete(TABLE_NAMES[tableIndex],
					whereWithId(id, selection), selectionArgs);
			break;
		case ALL_GAMES:
			rows = sDatabase.delete(ALL_GAMES_TABLE, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (rows > 0 && !sDatabase.inTransaction()) {
			String volume = uri.getPathSegments().get(0);
			Uri suri = Uri.parse(DownloadContent.CONTENT_URI + "/" + volume);
			notifyChange(suri, null);
		}
		return rows;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final int match = mUriMatcher.match(uri);
		int tableIndex = match >> BASE_SHIFT;
		Context sContext = getContext();
		SQLiteDatabase sDatabase = getDatabase(sContext, match);
		long id;
		Uri resultUri;

		switch (match) {
		case DOWNLOAD_TASK:
			id = sDatabase.insert(TABLE_NAMES[tableIndex], null, values);
			resultUri = ContentUris.withAppendedId(uri, id);
			break;
		case ALL_GAMES:
			id = sDatabase.insert(ALL_GAMES_TABLE, null, values);
			resultUri = ContentUris.withAppendedId(uri, id);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		notifyChange(uri, null);
		return resultUri;
	}

	/**
	 * 清空指定表的内容 并重置自增字段
	 * 
	 * @param ctx
	 * @param tableName
	 */
	public static void clearTable(Context ctx, String tableName) {
		SQLiteDatabase sDatabase = getDatabase(ctx);
		sDatabase.execSQL("delete from " + tableName);// 清空表内容
		sDatabase.execSQL("update sqlite_sequence set seq=0 where name='"
				+ tableName + "'");// 清空指定表的自增字段
	}

	static class OldDownloadState {
		/** 可下载资源，或可更新资源 */
		public static final int APP_NEW = 0;
		/** 正在下载资源 */
		public static final int APP_DOWNLOADING = 1;// 5
		/** 等待下载资源 */
		public static final int APP_DOWNLOAD_WAIT = 2;// 8
		/** 暂停中资源 */
		public static final int APP_DOWNLOAD_PAUSE = 3;// 6
		/** 下载完成资源 */
		public static final int APP_DOWNLOADED = 4;// 4
		/** 下载失败资源 */
		public static final int APP_DOWNLOAD_FAIL = 5;// 7
		/** 正在安装资源 */
		public static final int APP_INSTALLING = 6;// 2
		/** 已安装资源 */
		public static final int APP_INSTALLED = 7;// 1

		public static int oldStateToNew(int state, DownloadItem item) {
			int newState = AppItemState.STATE_IDLE;
			boolean mRename = false;
			switch (state) {
			case APP_NEW: {
				newState = AppItemState.STATE_IDLE;
				break;
			}
			case APP_DOWNLOADING: {
				// 刚升级，不可能正在下载，异常状态，修改为暂停
				newState = AppItemState.STATE_PAUSE;// AppItemState.STATE_ONGOING;
				mRename = true;
				break;
			}
			case APP_DOWNLOAD_WAIT: {
				// newState = AppItemState.STATE_WAIT;
				// 刚升级，不应为为等待，异常状态，修改为暂停
				newState = AppItemState.STATE_PAUSE;
				mRename = true;
				break;
			}
			case APP_DOWNLOAD_PAUSE: {
				newState = AppItemState.STATE_PAUSE;
				mRename = true;
				break;
			}
			case APP_DOWNLOADED: {
				newState = AppItemState.STATE_DOWNLOAD_FINISH;
				break;
			}
			case APP_DOWNLOAD_FAIL: {
				newState = AppItemState.STATE_RETRY;
				break;
			}
			case APP_INSTALLING: {
				newState = AppItemState.STATE_INSTALLING;
				break;
			}
			case APP_INSTALLED: {
				newState = AppItemState.STATE_INSTALLED_NEW;
				break;
			}
			}
			if (mRename) {
				final File src = new File(
						DownloadUtils.getFileDownloadPath(item));
				final File dest = new File(
						DownloadUtils.getTempFileDownloadPath(item));
				if (src.exists() && !dest.exists()) {
					src.renameTo(dest);
				}
			}

			return newState;
		}
	}

	public static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATEBASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createDatabase(db);
			final List<DownloadItem> mOldItems = DataUtil.getInstance(
					MyApplication.getInstance()).getOldDownloadList(
					MyApplication.getInstance());
			final DownloadTask mTempTask = new DownloadTask();
			ContentValues values;
			if (mOldItems != null && mOldItems.size() > 0) {
				for (DownloadItem item : mOldItems) {
					item.state = OldDownloadState.oldStateToNew(item.state,
							item);
					item.apk = SecurityUtil.decodeBase64(item.apk);
					item.iconUrl = SecurityUtil.decodeBase64(item.iconUrl);
					mTempTask.mDownloadItem = item;
					values = mTempTask.toContentValues();
					db.insert(DATABASE_TABLE_DOWNLOAD_TASK, null, values);
				}
				mOldItems.clear();
				DataUtil.getInstance(MyApplication.getInstance())
						.removeOldDownloadTable(MyApplication.getInstance());
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_DOWNLOAD_TASK);
			onCreate(db);
			LogUtil.e("cexo", "DatabaseHelper.onUpgrade():oldVersion:"
					+ oldVersion + ";newVersion:" + newVersion);
		}

		private void createDatabase(SQLiteDatabase db) {
			createDownloadTaskTable(db);
		}

		private void createDownloadTaskTable(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("create table ");
			sb.append(DATABASE_TABLE_DOWNLOAD_TASK);
			sb.append(" (" + DownloadContent.RECORD_ID
					+ " INTEGER PRIMARY KEY autoincrement, ");
			sb.append(DownloadTask.COLUMN_ICON_URL).append(" text,");
			sb.append(DownloadTask.COLUMN_NAME).append(" text,");
			sb.append(DownloadTask.COLUMN_PNAME).append(" text,");
			sb.append(DownloadTask.COLUMN_VNAME).append(" text,");
			sb.append(DownloadTask.COLUMN_VCODE).append(" integer,");
			sb.append(DownloadTask.COLUMN_LENGTH).append(" integer,");
			sb.append(DownloadTask.COLUMN_HASH).append(" text,");
			sb.append(DownloadTask.COLUMN_SID).append(" integer,");
			sb.append(DownloadTask.COLUMN_APK).append(" text,");
			sb.append(DownloadTask.COLUMN_DOWN_NUM).append(" text,");
			sb.append(DownloadTask.COLUMN_ADESC).append(" text,");
			sb.append(DownloadTask.COLUMN_STYPE).append(" integer,");
			sb.append(DownloadTask.COLUMN_RATING).append(" integer,");
			sb.append(DownloadTask.COLUMN_CATEID).append(" integer,");
			sb.append(DownloadTask.COLUMN_CATE_NAME).append(" text,");
			sb.append(DownloadTask.COLUMN_STATE).append(" integer,");
			sb.append(DownloadTask.COLUMN_RANK_COUNT).append(" integer,");
			sb.append(DownloadTask.COLUMN_COMM_COUNT).append(" integer,");
			sb.append(DownloadTask.COLUMN_UPDATE_TIME).append(" text,");
			sb.append(DownloadTask.COLUMN_LIST_ID).append(" text,");
			sb.append(DownloadTask.COLUMN_CATE_ID).append(" text,");
			sb.append(DownloadTask.COLUMN_SCREEN).append(" integer,");
			sb.append(DownloadTask.COLUMN_POSITION).append(" integer,");
			sb.append(DownloadTask.COLUMN_DOWNLOAD_TYPE).append(" integer,");
			sb.append(DownloadTask.COLUMN_DOWNLOADED_SIZE).append(" integer,");
			sb.append(DownloadTask.COLUMN_REAL_SIZE).append(" integer,");
			sb.append(DownloadTask.COLUMN_IS_INNER_GAME).append(" integer,");
			sb.append(DownloadTask.COLUMN_IS_OPENNED).append(" integer)");			
			db.execSQL(sb.toString());
			createIndex(db, DATABASE_TABLE_DOWNLOAD_TASK,
					DownloadContent.DownloadTaskColumn.COLUMN_SID);
		}

		/**
		 * 创建索引
		 * 
		 * @param db
		 * @param tableName
		 * @param columnName
		 */
		private void createIndex(SQLiteDatabase db, String tableName,
				String columnName) {
			String indexSql = "create index " + tableName + '_' + columnName
					+ " on " + tableName + " (" + columnName + ");";

			db.execSQL(indexSql);
		}
	}

	private String whereWithId(long id, String selection) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("_id=");
		sb.append(id);
		if (selection != null) {
			sb.append(" AND (");
			sb.append(selection);
			sb.append(')');
		}
		return sb.toString();
	}

	private static SQLiteDatabase mAllGamesDB;

	private static synchronized SQLiteDatabase getAllGamesDB(Context context) {
		if (mAllGamesDB != null) {
			return mAllGamesDB;
		}
		AllGamesHelper helper = new AllGamesHelper(context);
		mAllGamesDB = helper.getWritableDatabase();
		return mAllGamesDB;
	}

	private static class AllGamesHelper extends SQLiteOpenHelper {

		private static final int ALL_GAMES_VERSION = 1;

		private Context mContext;

		public AllGamesHelper(Context context) {
			super(context, ALL_GAMES_NAME, null, ALL_GAMES_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createDatabase(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

		private void createDatabase(SQLiteDatabase db) {
			initAllGamesDB();
		}

		private void initAllGamesDB() {
			try {
				FileUtil.copyFile(mContext.getAssets().open(ALL_GAMES_NAME),
						mContext.getDatabasePath(ALL_GAMES_NAME));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
