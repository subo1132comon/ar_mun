/**
 * @author jiangxiaoliang
 * 用来操作ContentProvider
 */

package com.byt.market.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.byt.market.MyApplication;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadItem;
import com.byt.market.util.LogUtil;
import com.byt.market.util.PackageUtil;

public abstract class DownloadContent {

	public static final String AUTHORITY = GamesProvider.DATABASE_AUTHORITY;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final String RECORD_ID = "_id";
	private static final int NOT_SAVED = -1;

	public long mId = NOT_SAVED;

	public int downloadType;

	public abstract ContentValues toContentValues();

	public abstract <T extends DownloadContent> T restore(Cursor cursor);

	public abstract String[] getContentProjection();

	@SuppressWarnings("unchecked")
	static public <T extends DownloadContent> T getContent(Cursor cursor,
			Class<T> klass) {
		try {
			T content = klass.newInstance();
			content.mId = cursor.getLong(0);
			return (T) content.restore(cursor);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Uri save(Context context) {
		if (isSaved()) {
			throw new UnsupportedOperationException();
		}
		Uri res = context.getContentResolver().insert(CONTENT_URI,
				toContentValues());
		mId = ContentUris.parseId(res);
		return res;
	}

	public boolean isSaved() {
		return mId != NOT_SAVED;
	}

	public static Cursor query(Context context, Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		ContentResolver sContentResolver = context.getContentResolver();
		Cursor sCursor = sContentResolver.query(uri, projection, selection,
				selectionArgs, sortOrder);
		return sCursor;
	}

	public static int update(Context context, Uri baseUri, long id,
			ContentValues contentValues) {
		return context.getContentResolver().update(
				ContentUris.withAppendedId(baseUri, id), contentValues, null,
				null);
	}

	public static int update(Context context, Uri baseUri,
			ContentValues contentValues, String selection,
			String[] selectionArgs) {
		return context.getContentResolver().update(baseUri, contentValues,
				selection, selectionArgs);
	}

	public static int delete(Context context, Uri uri, String where,
			String[] selection) {
		return context.getContentResolver().delete(uri, where, selection);
	}

	/**
	 * Generic count method that can be used for any ContentProvider
	 * 
	 * @param context
	 *            the calling Context
	 * @param uri
	 *            the Uri for the provider query
	 * @param selection
	 *            as with a query call
	 * @param selectionArgs
	 *            as with a query call
	 * @return the number of items matching the query (or zero)
	 */
	static public int count(Context context, Uri uri, String selection,
			String[] selectionArgs) {
		String[] countColumns = new String[] { "count(*)" };

		Cursor cursor = context.getContentResolver().query(uri, countColumns,
				selection, selectionArgs, null);
		try {
			if (!cursor.moveToFirst()) {
				return 0;
			}
			return cursor.getInt(0);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			cursor.close();
		}
	}

	public static final int POSITION_NULL = 10000;

	public interface DownloadTaskColumn {
		// apk属性
		public static final String COLUMN_ICON_URL = "iconUrl";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_PNAME = "pname";
		public static final String COLUMN_VNAME = "vname";
		public static final String COLUMN_VCODE = "vcode";
		public static final String COLUMN_LENGTH = "length";
		public static final String COLUMN_HASH = "hash";
		public static final String COLUMN_SID = "sid";
		public static final String COLUMN_APK = "apk";
		public static final String COLUMN_DOWN_NUM = "downNum";		
		public static final String COLUMN_ADESC = "adesc";
		public static final String COLUMN_STYPE = "stype";
		public static final String COLUMN_RATING = "rating";
		public static final String COLUMN_CATEID = "cateid";
		public static final String COLUMN_CATE_NAME = "cateName";
		public static final String COLUMN_STATE = "state";
		public static final String COLUMN_RANK_COUNT = "rankcount";
		public static final String COLUMN_COMM_COUNT = "commcount";
		public static final String COLUMN_UPDATE_TIME = "updatetime";
		public static final String COLUMN_LIST_ID = "list_id";
		public static final String COLUMN_CATE_ID = "list_cateid";		
		/**
		 * 我的游戏，存储应用所在屏幕编号
		 * */
		public static final String COLUMN_SCREEN = "scree";
		/**
		 * 我的游戏，存储应用在当该屏的位置
		 * */
		public static final String COLUMN_POSITION = "position";

		public static final String COLUMN_DOWNLOAD_TYPE = "downloadType"; // 下载文件类型

		public static final String COLUMN_DOWNLOADED_SIZE = "download_size";
		public static final String COLUMN_REAL_SIZE = "real_size";
		/**
		 * 是否是在游戏盒子内的下载的游戏:1表示在,0表示不在
		 */
		public static final String COLUMN_IS_INNER_GAME = "is_inner_game";
		/**
		 * 是否已经打开过该游戏,如果已安装未打开过的游戏,则会显示new标识;0表示没打开过,1表示打开过
		 */
		public static final String COLUMN_IS_OPENNED = "is_openned";
		
	}

	public static class DownloadTask extends DownloadContent implements
			DownloadTaskColumn {
		public static final Uri CONTENT_URI = Uri
				.parse(DownloadContent.CONTENT_URI + "/"
						+ GamesProvider.DATABASE_TABLE_DOWNLOAD_TASK);

		private static final String[] CONTENT_PROJECTION = { RECORD_ID,
				COLUMN_ICON_URL, COLUMN_NAME, COLUMN_PNAME, COLUMN_VNAME,
				COLUMN_VCODE, COLUMN_LENGTH, COLUMN_HASH, COLUMN_SID,
				COLUMN_APK, COLUMN_DOWN_NUM, COLUMN_ADESC, COLUMN_STYPE,
				COLUMN_RATING, COLUMN_CATEID, COLUMN_CATE_NAME, COLUMN_STATE,
				COLUMN_RANK_COUNT, COLUMN_COMM_COUNT, COLUMN_UPDATE_TIME,
				COLUMN_LIST_ID, COLUMN_CATE_ID, COLUMN_SCREEN, COLUMN_POSITION,
				COLUMN_DOWNLOAD_TYPE, COLUMN_DOWNLOADED_SIZE, COLUMN_REAL_SIZE,
				COLUMN_IS_INNER_GAME, COLUMN_IS_OPENNED };

		public DownloadItem mDownloadItem = new DownloadItem();

		@Override
		public ContentValues toContentValues() {
			ContentValues sContentValues = new ContentValues();
			sContentValues.put(COLUMN_ICON_URL, mDownloadItem.iconUrl);
			sContentValues.put(COLUMN_NAME, mDownloadItem.name);
			sContentValues.put(COLUMN_PNAME, mDownloadItem.pname);
			sContentValues.put(COLUMN_VNAME, mDownloadItem.vname);
			sContentValues.put(COLUMN_VCODE, mDownloadItem.vcode);
			sContentValues.put(COLUMN_LENGTH, mDownloadItem.length);
			sContentValues.put(COLUMN_HASH, mDownloadItem.hash);
			sContentValues.put(COLUMN_SID, mDownloadItem.sid);
			sContentValues.put(COLUMN_APK, mDownloadItem.apk);
			sContentValues.put(COLUMN_DOWN_NUM, mDownloadItem.downNum);			
			sContentValues.put(COLUMN_ADESC, mDownloadItem.adesc);
			sContentValues.put(COLUMN_STYPE, mDownloadItem.stype);
			sContentValues.put(COLUMN_RATING, mDownloadItem.rating);
			sContentValues.put(COLUMN_CATEID, mDownloadItem.cateid);
			sContentValues.put(COLUMN_CATE_NAME, mDownloadItem.cateName);
			sContentValues.put(COLUMN_STATE, mDownloadItem.state);
			sContentValues.put(COLUMN_RANK_COUNT, mDownloadItem.rankcount);
			sContentValues.put(COLUMN_COMM_COUNT, mDownloadItem.commcount);
			sContentValues.put(COLUMN_UPDATE_TIME, mDownloadItem.updatetime);
			sContentValues.put(COLUMN_LIST_ID, mDownloadItem.list_id);
			sContentValues.put(COLUMN_CATE_ID, mDownloadItem.list_cateid);
			sContentValues.put(COLUMN_SCREEN, mDownloadItem.screen);
			sContentValues.put(COLUMN_POSITION, mDownloadItem.position);
			// sContentValues.put(COLUMN_DOWNLOAD_TYPE,mDownloadItem.dow);
			sContentValues.put(COLUMN_DOWNLOADED_SIZE, mDownloadItem.lastDSize);
			sContentValues.put(COLUMN_REAL_SIZE, mDownloadItem.dSize);
			sContentValues.put(COLUMN_IS_INNER_GAME, mDownloadItem.isInnerGame);
			sContentValues.put(COLUMN_IS_OPENNED, mDownloadItem.isOpenned);
			
			return sContentValues;
		}

		@SuppressWarnings("unchecked")
		@Override
		public DownloadTask restore(Cursor cursor) {
			mId = cursor.getInt(0);
			DownloadItem downloadItem = new DownloadItem();
			downloadItem.iconUrl = cursor.getString(1);
			downloadItem.name = cursor.getString(2);
			downloadItem.pname = cursor.getString(3);
			downloadItem.vname = cursor.getString(4);
			downloadItem.vcode = cursor.getInt(5);
			downloadItem.length = cursor.getLong(6);
			downloadItem.hash = cursor.getString(7);
			downloadItem.sid = cursor.getInt(8);
			downloadItem.apk = cursor.getString(9);
			downloadItem.downNum = cursor.getString(10);
			downloadItem.adesc = cursor.getString(11);
			downloadItem.stype = cursor.getString(12);
			downloadItem.rating = cursor.getInt(13);
			downloadItem.cateid = cursor.getInt(14);
			downloadItem.cateName = cursor.getString(15);
			downloadItem.state = cursor.getInt(16);
			downloadItem.rankcount = cursor.getInt(17);
			downloadItem.commcount = cursor.getInt(18);
			downloadItem.updatetime = cursor.getString(19);
			downloadItem.list_id = cursor.getString(20);
			downloadItem.list_cateid = cursor.getString(21);
			downloadItem.screen = cursor.getInt(22);
			downloadItem.position = cursor.getInt(23);
			// downloadType
			downloadItem.lastDSize = cursor.getLong(25);
			downloadItem.dSize = cursor.getLong(26);
			downloadItem.isInnerGame = cursor.getInt(27);
			downloadItem.isOpenned = cursor.getInt(28);
			
			this.mDownloadItem = downloadItem;
			return this;
		}

		@Override
		public String[] getContentProjection() {
			return CONTENT_PROJECTION;
		}

		/**
		 * 添加下载任务
		 * 
		 * @param context
		 * @param task
		 */
		public static void addDownloadTask(Context context, DownloadTask task,
				boolean isFav) {
			String selection;
			String[] selectionArgs;

			switch (task.downloadType) {
			case DownloadTaskManager.FILE_TYPE_BACKGROUND:
			case DownloadTaskManager.FILE_TYPE_APK:
				selection = COLUMN_SID + " = ? ";
				selectionArgs = new String[] { String
						.valueOf(task.mDownloadItem.sid) };
				break;
			default:
				/*
				 * selection = COLUMN_DOWNLOAD_TYPE + " = ? and " +
				 * COLUMN_SERVER_ID + " = ? "; selectionArgs = new String[] {
				 * String.valueOf(task.downloadType), String.valueOf(task.sId)
				 * };
				 */
				selection = COLUMN_SID + " = ? ";
				selectionArgs = new String[] { String
						.valueOf(task.mDownloadItem.sid) };
				break;
			}

			Cursor cursor = null;

			try {
				cursor = query(context, CONTENT_URI, CONTENT_PROJECTION,
						selection, selectionArgs, null);
				if (cursor.moveToFirst()) {
					if (!isFav) {
						DownloadTask currTask = getContent(cursor,
								DownloadTask.class);
						if (currTask.mDownloadItem.state == AppItemState.STATE_PAUSE) {
							task.mDownloadItem.state = AppItemState.STATE_WAIT;
							update(context, CONTENT_URI,
									task.toContentValues(), selection,
									selectionArgs);
						} else {
							if (task.downloadType == DownloadTaskManager.FILE_TYPE_BACKGROUND
									|| task.downloadType == DownloadTaskManager.FILE_TYPE_APK) {
								if (!task.mDownloadItem.vname
										.equals(currTask.mDownloadItem.vname)) {
									task.mDownloadItem.state = AppItemState.STATE_WAIT;
									update(context, CONTENT_URI,
											task.toContentValues(), selection,
											selectionArgs);
								}
							}
						}
					}
				} else {
					context.getContentResolver().insert(CONTENT_URI,
							task.toContentValues());
				}
			} catch (Exception e) {
				try {
					context.getContentResolver().insert(CONTENT_URI,
							task.toContentValues());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		/**
		 * 更新下载状态
		 * 
		 * @param context
		 * @param state
		 */
		public void updateState(Context context, int state, long downloadedSize) {
			this.mDownloadItem.state = state;
			ContentValues sContentValues = new ContentValues();
			sContentValues.put(COLUMN_STATE, state);
			if (downloadedSize >= 0) {
				sContentValues.put(COLUMN_DOWNLOADED_SIZE, downloadedSize);
			}

			String selection;
			String[] selectionArgs;

			switch (downloadType) {
			case DownloadTaskManager.FILE_TYPE_BACKGROUND:
			case DownloadTaskManager.FILE_TYPE_APK:
				selection = DownloadTask.COLUMN_SID + " = ? and "
						+ DownloadTask.COLUMN_VCODE + " = ?";
				selectionArgs = new String[] {
						String.valueOf(mDownloadItem.sid),
						String.valueOf(mDownloadItem.vcode) };
				break;
			default:
				/*
				 * selection = DownloadTask.COLUMN_DOWNLOAD_TYPE + " = ? and " +
				 * DownloadTask.COLUMN_SERVER_ID + " = ?"; selectionArgs = new
				 * String[]{ String.valueOf(downloadType), String.valueOf(sId)
				 * };
				 */
				selection = DownloadTask.COLUMN_SID + " = ? and "
						+ DownloadTask.COLUMN_VCODE + " = ?";
				selectionArgs = new String[] {
						String.valueOf(mDownloadItem.sid),
						String.valueOf(mDownloadItem.vcode) };
				break;
			}

			int count = update(context, CONTENT_URI, sContentValues, selection,
					selectionArgs);
            LogUtil.i("appupdate","count = "+count);
		}

		/**
		 * 更新实际大小
		 * 
		 * @param context
		 * @param state
		 */
		public void updateRealState(Context context, long realSize) {
			this.mDownloadItem.dSize = realSize;
			ContentValues sContentValues = new ContentValues();
			sContentValues.put(COLUMN_REAL_SIZE, realSize);

			String selection;
			String[] selectionArgs;

			switch (downloadType) {
			case DownloadTaskManager.FILE_TYPE_BACKGROUND:
			case DownloadTaskManager.FILE_TYPE_APK:
				selection = DownloadTask.COLUMN_SID + " = ? and "
						+ DownloadTask.COLUMN_VCODE + " = ?";
				selectionArgs = new String[] {
						String.valueOf(mDownloadItem.sid),
						String.valueOf(mDownloadItem.vcode) };
				break;
			default:
				/*
				 * selection = DownloadTask.COLUMN_DOWNLOAD_TYPE + " = ? and " +
				 * DownloadTask.COLUMN_SERVER_ID + " = ?"; selectionArgs = new
				 * String[]{ String.valueOf(downloadType), String.valueOf(sId)
				 * };
				 */
				selection = DownloadTask.COLUMN_SID + " = ? and "
						+ DownloadTask.COLUMN_VCODE + " = ?";
				selectionArgs = new String[] {
						String.valueOf(mDownloadItem.sid),
						String.valueOf(mDownloadItem.vcode) };
				break;
			}
			int count = update(context, CONTENT_URI, sContentValues, selection,
					selectionArgs);
		}

		/**
		 * 删除下载任务
		 * 
		 * @param context
		 * @param task
		 */
		public static void deleteDownloadTask(Context context, DownloadTask task) {
			String selection;
			String[] selectionArgs;

			switch (task.downloadType) {
			case DownloadTaskManager.FILE_TYPE_BACKGROUND:
			case DownloadTaskManager.FILE_TYPE_APK:
				selection = COLUMN_SID + " = ? ";
				selectionArgs = new String[] { String
						.valueOf(task.mDownloadItem.sid) };
				break;
			default:
				selection = COLUMN_SID + " = ? ";
				selectionArgs = new String[] { String
						.valueOf(task.mDownloadItem.sid) };
				break;
			}

			delete(context, CONTENT_URI, selection, selectionArgs);
		}

		/**
		 * 获取正在下载文件的个数(开始下载并且没有安装成功)
		 * 
		 * @param context
		 * @param downloadType
		 * @return
		 */
		public static int getDownloaddingCount(int downloadType) {
			try {
				Context context = MyApplication.getInstance()
						.getApplicationContext();
				if (downloadType == -1) {
					return count(context, CONTENT_URI, null, null);
				} else {
					String selection = COLUMN_STATE + " < "
							+ AppItemState.STATE_INSTALL_FAILED + " AND "
							+ COLUMN_STATE + " > " + AppItemState.STATE_IDLE;// +
																				// " and "
																				// +
																				// COLUMN_DOWNLOAD_TYPE
																				// +
																				// " IS "+downloadType;
					return count(context, CONTENT_URI, selection, null);
				}
			} catch (Exception e) {
				DownloadLog
						.error(DownloadTask.class, "getDownloaddingCount", e);
				return 0;
			}
		}

		/**
		 * 获取正在下载文件的个数(开始下载并且没有安装成功)
		 * 
		 * @param context
		 * @param downloadType
		 * @return
		 */
		public static int getRealDownloaddingCount(int downloadType) {
			try {
				Context context = MyApplication.getInstance()
						.getApplicationContext();
				if (downloadType == -1) {
					return count(context, CONTENT_URI, null, null);
				} else {
					String selection = COLUMN_STATE + " = "
							+ AppItemState.STATE_ONGOING + " OR "
							+ COLUMN_STATE + " = " + AppItemState.STATE_WAIT;
					return count(context, CONTENT_URI, selection, null);
				}
			} catch (Exception e) {
				DownloadLog
						.error(DownloadTask.class, "getDownloaddingCount", e);
				return 0;
			}
		}

		/**
		 * 获取未下载的文件个数(IDLE或者PAUSE)
		 * 
		 * @param context
		 * @param downloadType
		 * @return
		 */
		public static int getIdleOrPauseCount(int downloadType) {
			try {
				Context context = MyApplication.getInstance()
						.getApplicationContext();
				if (downloadType == -1) {
					return count(context, CONTENT_URI, null, null);
				} else {
					String selection = COLUMN_STATE + " = "
							+ AppItemState.STATE_PAUSE /*+ " OR " + COLUMN_STATE
							+ " = " + AppItemState.STATE_IDLE*/;
					return count(context, CONTENT_URI, selection, null);
				}
			} catch (Exception e) {
				DownloadLog
						.error(DownloadTask.class, "getDownloaddingCount", e);
				return 0;
			}
		}

		/**
		 * 获取需要更新的游戏个数
		 * 
		 * @param context
		 * @param downloadType
		 * @return
		 */
		public static int getNeedUpdateCount() {
			try {
				Context context = MyApplication.getInstance()
						.getApplicationContext();

				String selection = COLUMN_STATE + " = "
						+ AppItemState.STATE_NEED_UPDATE;// + " and " +
															// COLUMN_DOWNLOAD_TYPE
															// +
															// " IS "+downloadType;
				return count(context, CONTENT_URI, selection, null);
			} catch (Exception e) {
				DownloadLog
						.error(DownloadTask.class, "getDownloaddingCount", e);
				return 0;
			}
		}

		/**
		 * 获取一个未下载完成的任务
		 * 
		 * @param context
		 * @param offset
		 * @return
		 */
		public static DownloadTask loadOneDownloaddingTask(Context context,
				int offset) {
			String sortOrder = COLUMN_STATE + " asc ";
			String selection = COLUMN_STATE + " = ?";
			String[] selectionArgs = new String[] { String
					.valueOf(AppItemState.STATE_WAIT) };
			Cursor c = null;
			try {
				c = context.getContentResolver()
						.query(CONTENT_URI, CONTENT_PROJECTION, selection,
								selectionArgs, sortOrder);
				if (c.move(offset + 1)) {
                    LogUtil.i("appupdate","loadOneDownloaddingTask");
					return getContent(c, DownloadTask.class);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (c != null) {
					c.close();
				}
			}
			return null;
		}

		/**
		 * 获取指定下载任务
		 * 
		 * @param ctx
		 * @param value1
		 * @param version
		 * @return
		 */
		public static DownloadTask getDownloadTask(Context ctx, String value1,
				String version) {
			String selection = COLUMN_SID + " = ? and " + COLUMN_VCODE + " = ?";
			String[] selectionArgs = { value1, version };
			return getDownloadTask(ctx, selection, selectionArgs);
		}

		/**
		 * 获取指定下载任务
		 * 
		 * @param ctx
		 * @param value1
		 * @param version
		 * @return
		 */
		public static DownloadTask getDownloadTaskByPname(Context ctx,
				String pname) {
			String selection = COLUMN_PNAME + " = ?  ";
			String[] selectionArgs = { pname };
			return getDownloadTask(ctx, selection, selectionArgs);
		}

		/**
		 * 获取指定下载任务
		 * 
		 * @param ctx
		 * @param type
		 * @param id
		 * @return
		 */
		/*
		 * public static DownloadTask getDownloadTask(Context ctx, int type, int
		 * id) { String selection = COLUMN_DOWNLOAD_TYPE + " = ? and " +
		 * COLUMN_SERVER_ID + " = ?"; String[] selectionArgs = {
		 * String.valueOf(type), String.valueOf(id) }; return
		 * getDownloadTask(ctx, selection, selectionArgs); }
		 */

		/**
		 * 获取指定下载任务
		 * 
		 * @param ctx
		 * @param value1
		 * @param state
		 * @return
		 */
		/*
		 * public static DownloadTask getDownloadTask(Context ctx, String
		 * value1, int state) { String selection = COLUMN_APP_ID + " = ? and " +
		 * COLUMN_STATE + " = ?"; String[] selectionArgs = { value1,
		 * String.valueOf(state) }; return getDownloadTask(ctx, selection,
		 * selectionArgs); }
		 */

		/**
		 * 获取指定下载任务
		 * 
		 * @param ctx
		 * @param selection
		 * @param selectionArgs
		 * @return
		 */
		public static DownloadTask getDownloadTask(Context ctx,
				String selection, String[] selectionArgs) {
			Cursor c = null;
			try {
				c = ctx.getContentResolver().query(CONTENT_URI,
						CONTENT_PROJECTION, selection, selectionArgs, null);
				if (c.moveToFirst()) {
					return getContent(c, DownloadTask.class);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (c != null) {
					c.close();
				}
			}
			return null;
		}

		public static HashMap<String, DownloadTask> getAllDownloadTasks(
				Context ctx, final HashMap<String, Integer> installedGames) {
			HashMap<String, DownloadTask> downloadTasks = new LinkedHashMap<String, DownloadTask>();
			Cursor c = null;
			DownloadTask downloadTask;
			installedGames.clear();
			try {
				c = ctx.getContentResolver().query(CONTENT_URI,
						CONTENT_PROJECTION, null, null, null);
				if (c != null) {
					while (c.moveToNext()) {
						downloadTask = getContent(c, DownloadTask.class);
						downloadTasks.put(
								String.valueOf(downloadTask.mDownloadItem.sid),
								downloadTask);
						if (PackageUtil.isInstalledApk(ctx,
								downloadTask.mDownloadItem.pname, null)) {
							installedGames.put(
									downloadTask.mDownloadItem.pname, 0);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (c != null) {
					c.close();
				}
			}
			return downloadTasks;
		}

		/**
		 * 获取所有已下载 或者正在下载的任务
		 * 
		 * @param ctx
		 * @param state
		 * @return
		 */
		public static ArrayList<DownloadTask> getAllDownloadTasks(Context ctx,
				int state) {
			ArrayList<DownloadTask> downloadTasks = new ArrayList<DownloadTask>();
			DownloadTask downloadTask;
			Cursor c = null;
			try {
				String selection;
				String[] selectionArgs;

				selection = COLUMN_STATE + " = ? ";
				selectionArgs = new String[] { String.valueOf(state) };

				c = ctx.getContentResolver().query(CONTENT_URI,
						CONTENT_PROJECTION, selection, selectionArgs, null);
				while (c.moveToNext()) {
					downloadTask = getContent(c, DownloadTask.class);
					downloadTasks.add(downloadTask);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (c != null) {
					c.close();
				}
			}
			return downloadTasks;
		}

		/**
		 * 获得所有正在下载的任务
		 * 
		 * public static ArrayList<DownloadTask> getAllDownningTasks(Context
		 * ctx) { String selection = COLUMN_STATE + " = " +
		 * AppItemState.STATE_PAUSE + " OR " + COLUMN_STATE + " = " +
		 * AppItemState.STATE_IDLE; Cursor c =
		 * ctx.getContentResolver().query(CONTENT_URI, CONTENT_PROJECTION,
		 * selection, null, null); ArrayList<DownloadTask> downloadTasks = new
		 * ArrayList<DownloadTask>(); DownloadTask downloadTask;
		 * 
		 * try { while (c.moveToNext()) { downloadTask = getContent(c,
		 * DownloadTask.class); downloadTasks.add(downloadTask); } } catch
		 * (Exception e) { e.printStackTrace(); } finally { if (c != null) {
		 * c.close(); } } return downloadTasks; }
		 */

		/**
		 * 获得所有正在下载的任务
		 */
		public static ArrayList<DownloadTask> getAllDownningTasks(Context ctx) {
			ArrayList<DownloadTask> downloadTasks = new ArrayList<DownloadTask>();
			DownloadTask downloadTask;
			Cursor c = null;
			try {
				String selection = COLUMN_STATE + " = "
						+ AppItemState.STATE_ONGOING + " OR " + COLUMN_STATE
						+ " = " + AppItemState.STATE_WAIT + " OR "
						+ COLUMN_STATE + "=" + AppItemState.STATE_PAUSE;
				c = ctx.getContentResolver().query(CONTENT_URI,
						CONTENT_PROJECTION, selection, null, null);
				while (c.moveToNext()) {
					downloadTask = getContent(c, DownloadTask.class);
					downloadTasks.add(downloadTask);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (c != null) {
					c.close();
				}
			}
			return downloadTasks;
		}
		/**
		 * 获得所有正在下载的任务
		 */
		public static ArrayList<DownloadTask> getAllDownningTasks2(Context ctx) {
			ArrayList<DownloadTask> downloadTasks = new ArrayList<DownloadTask>();
			DownloadTask downloadTask;
			Cursor c = null;
			try {
				String selection = COLUMN_STATE + " = "
						+ AppItemState.STATE_ONGOING + " OR " + COLUMN_STATE
						+ " = " + AppItemState.STATE_WAIT + " OR "
						+ COLUMN_STATE + "=" + AppItemState.STATE_PAUSE+ " OR "+COLUMN_STATE + "=" + AppItemState.STATE_RETRY;
				c = ctx.getContentResolver().query(CONTENT_URI,
						CONTENT_PROJECTION, selection, null, null);
				while (c.moveToNext()) {
					downloadTask = getContent(c, DownloadTask.class);
					downloadTasks.add(downloadTask);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (c != null) {
					c.close();
				}
			}
			return downloadTasks;
		}

	}
	
	
}
