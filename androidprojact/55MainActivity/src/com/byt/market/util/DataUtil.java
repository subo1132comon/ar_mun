package com.byt.market.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.byt.market.MyApplication;
import com.byt.market.data.APP;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadItem;
import com.byt.market.data.GAME;
import com.byt.market.data.ListBean;
import com.byt.market.data.PUSH;
import com.byt.market.database.DataDatabaseHelper;
import com.byt.market.database.OldAppDownloadDatabaseHelper;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.log.LogModel;

public class DataUtil {

	private static DataUtil mHelper;
	private static DataDatabaseHelper mDBHelper2;
	private static DataDatabaseHelper mDBHelper;
	private ExecutorService mSingleThreadExecutor;

	private DataUtil(Context context) {
		mDBHelper2 = new DataDatabaseHelper(context);
		mDBHelper = new DataDatabaseHelper(context);
		mSingleThreadExecutor = Executors.newSingleThreadExecutor();
	}

	public static synchronized DataUtil getInstance(Context context) {
		if (mHelper == null) {
			mHelper = new DataUtil(context);
		}
		return mHelper;
	}

	public synchronized void handleDatabase(int type, Object... objs) {
		DataTask run = new DataTask(type, objs);
		mSingleThreadExecutor.execute(run);
	}

	private class DataTask implements Runnable {

		private int type;
		private Object[] objs;

		public DataTask(int type, Object... objs) {
			this.type = type;
			this.objs = objs;
		}

		@Override
		public void run() {
			// marketContext.opData(LogModel.DATA_INSERT,
			// LogModel.DATA_FAVORATE, app)
			Object tab = objs[0];
			Object values = objs[1];
			switch (type) {
			case LogModel.DATA_INSERT:
				if (tab != null && tab instanceof Integer && values != null) {
					int mTab = (Integer) tab;
					switch (mTab) {
					case LogModel.DATA_LIST:
						updateList((ListBean) values);
						break;
					case LogModel.DATA_GAME:
						updateGame((GAME) values);
						break;
					case LogModel.DATA_PUSH:
						updatePush((PUSH) values);
						break;
					case LogModel.DATA_APP:
						updateAPP((APP) values);
						break;
					case LogModel.DATA_KEY:
						addKey((String) values);
						break;
					case LogModel.DATA_FAVORATE:
						updateFavor((AppItem) values);
						// 这段代码的会将收藏app自动加入下载任务列表
						// DownloadTaskManager.getInstance().addFav(
						// (AppItem) values);
						break;
					case LogModel.DATA_MYFAVORATE:
						updateMyFavor((Integer)values);
						break;
					}
				}

				break;
			case LogModel.DATA_DEL:
				if (tab != null && tab instanceof Integer) {
					int mTab = (Integer) tab;
					switch (mTab) {
					case LogModel.DATA_LIST:
						if (values != null)
							delListBean((ListBean) values);
						break;
					case LogModel.DATA_GAME:
						if (values != null)
							delGame((GAME) values);
						break;
					case LogModel.DATA_PUSH:
						delPush();
						break;
					case LogModel.DATA_APP:
						delAPP();
						break;
					case LogModel.DATA_FAVORATE:
						delFavor((Integer) values);
						final DownloadTask task = DownloadTaskManager
								.getInstance().getDownloadTask(
										MyApplication.getInstance()
												.getApplicationContext(),
										String.valueOf((Integer) values), "");
						if (task != null
								&& task.mDownloadItem != null
								&& task.mDownloadItem.state == AppItemState.STATE_IDLE) {
							DownloadTaskManager.getInstance()
									.deleteDownloadTask(task, false, false);
						}
						break;
					case LogModel.DATA_MYFAVORATE:
						delMYFavor((Integer) values);
						break;
					case LogModel.DATA_ALL:
						clearData();
						break;
					case LogModel.DATA_MYFAV:
						delMyFav();;
						break;
					case LogModel.DATA_KEY:
						if (values != null)
							clearOneKey((String) values);
						break;
					case LogModel.DATA_ALL_KEY:
						clearKeys();
						break;
					}
				}

				break;

			default:
				break;
			}
		}

	}

	/*-------------------list---------------------*/
	public synchronized void updateList(ListBean lb) {
		if (lb == null)
			return;
		SQLiteDatabase db = null;
		try {
			ListBean newlb = hasListBean(lb);
			db = mDBHelper.getWritableDb();
			ContentValues values = new ContentValues();
			if (newlb != null) {
				values.put(DataDatabaseHelper.LIST.LIST_C, newlb.c + lb.c);
				values.put(DataDatabaseHelper.LIST.LIST_SC, newlb.sc + lb.sc);
				db.update(DataDatabaseHelper.LIST.TABLE, values,
						DataDatabaseHelper.LIST.LIST_ID + "=? and "
								+ DataDatabaseHelper.LIST.LIST_CA + "=?",
						new String[] { String.valueOf(lb.id), lb.ca });
			} else {
				values.put(DataDatabaseHelper.LIST.LIST_ID, lb.id);
				values.put(DataDatabaseHelper.LIST.LIST_C, lb.c);
				values.put(DataDatabaseHelper.LIST.LIST_SC, lb.sc);
				values.put(DataDatabaseHelper.LIST.LIST_CA, lb.ca);
				db.insert(DataDatabaseHelper.LIST.TABLE, null, values);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public synchronized boolean delListBean(ListBean lb) {
		SQLiteDatabase db = mDBHelper.getWritableDb();
		int delete = db.delete(DataDatabaseHelper.LIST.TABLE,
				DataDatabaseHelper.LIST.LIST_ID + "=? and "
						+ DataDatabaseHelper.LIST.LIST_CA + "=?", new String[] {
						String.valueOf(lb.id), lb.ca });
		return delete != -1;
	}

	public synchronized void delListBean() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = mDBHelper.getWritableDb();
			cursor = db.query(DataDatabaseHelper.LIST.TABLE, null, null, null,
					null, null, null);
			if (cursor == null)
				return;
			while (cursor.moveToNext()) {
				String id = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.LIST.LIST_ID));
				String ca = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.LIST.LIST_CA));
				db.delete(DataDatabaseHelper.LIST.TABLE,
						DataDatabaseHelper.LIST.LIST_ID + "=? and "
								+ DataDatabaseHelper.LIST.LIST_CA + "=?",
						new String[] { id, ca });
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}

	public synchronized List<ListBean> getListBeanList() {
		List<ListBean> list = new ArrayList<ListBean>();
		try {
			SQLiteDatabase db = mDBHelper.getWritableDb();
			Cursor c = db.query(DataDatabaseHelper.LIST.TABLE, null, null,
					null, null, null, null);
			if (c != null)
				while (c.moveToNext()) {
					ListBean item = new ListBean();
					item.id = c.getString(c
							.getColumnIndex(DataDatabaseHelper.LIST.LIST_ID));
					item.c = c.getInt(c
							.getColumnIndex(DataDatabaseHelper.LIST.LIST_C));
					item.sc = c.getInt(c
							.getColumnIndex(DataDatabaseHelper.LIST.LIST_SC));
					item.ca = c.getString(c
							.getColumnIndex(DataDatabaseHelper.LIST.LIST_CA));
					list.add(item);
				}
		} catch (Exception e) {
		}
		return list;
	}

	public ListBean hasListBean(ListBean lb) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		ListBean newlb = null;
		try {
			db = mDBHelper.getWritableDb();
			cursor = db.query(DataDatabaseHelper.LIST.TABLE, null,
					DataDatabaseHelper.LIST.LIST_ID + "=? and "
							+ DataDatabaseHelper.LIST.LIST_CA + "=?",
					new String[] { String.valueOf(lb.id), lb.ca }, null, null,
					null);
			if (cursor == null)
				return null;
			if (cursor.moveToFirst()) {
				newlb = new ListBean();
				newlb.id = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.LIST.LIST_ID));
				newlb.c = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.LIST.LIST_C));
				newlb.sc = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.LIST.LIST_SC));
				newlb.ca = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.LIST.LIST_CA));
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return newlb;
	}

	/*-------------------game---------------------*/
	public synchronized void updateGame(GAME game) {
		if (game == null)
			return;
		SQLiteDatabase db = null;
		try {
			ContentValues values = new ContentValues();
			GAME oldGame = hasGame(game);
			db = mDBHelper.getWritableDb();
			if (oldGame != null) {
				values.put(DataDatabaseHelper.GAME.DOWN_C, oldGame.d_c
						+ game.d_c);
				values.put(DataDatabaseHelper.GAME.DOWN_D, oldGame.d_d
						+ game.d_d);
				values.put(DataDatabaseHelper.GAME.DOWN_F, oldGame.d_f
						+ game.d_f);
				values.put(DataDatabaseHelper.GAME.DOWN_O, oldGame.d_o
						+ game.d_o);
				values.put(DataDatabaseHelper.GAME.INSTALL_F, oldGame.i_f
						+ game.i_f);
				values.put(DataDatabaseHelper.GAME.INSTALL_O, oldGame.i_o
						+ game.i_o);
				db.update(
						DataDatabaseHelper.GAME.TABLE,
						values,
						DataDatabaseHelper.GAME.LIST_ID + "=? and "
								+ DataDatabaseHelper.GAME.APP_ID + "=? and "
								+ DataDatabaseHelper.GAME.LIST_CATE + "=?",
						new String[] { game.list_id,
								String.valueOf(game.app_id), game.list_c });
			} else {
				values.put(DataDatabaseHelper.GAME.APP_ID, game.app_id);
				values.put(DataDatabaseHelper.GAME.LIST_ID, game.list_id);
				values.put(DataDatabaseHelper.GAME.LIST_CATE, game.list_c);
				values.put(DataDatabaseHelper.GAME.DOWN_C, game.d_c);
				values.put(DataDatabaseHelper.GAME.DOWN_D, game.d_d);
				values.put(DataDatabaseHelper.GAME.DOWN_F, game.d_f);
				values.put(DataDatabaseHelper.GAME.DOWN_O, game.d_o);
				values.put(DataDatabaseHelper.GAME.INSTALL_F, game.i_f);
				values.put(DataDatabaseHelper.GAME.INSTALL_O, game.i_o);
				db.insert(DataDatabaseHelper.GAME.TABLE, null, values);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null && db.isOpen()) {
				try {
					db.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized boolean delGame(GAME game) {
		SQLiteDatabase db = mDBHelper.getWritableDb();
		int delete = db.delete(DataDatabaseHelper.GAME.TABLE,
				DataDatabaseHelper.GAME.LIST_ID + "=? and "
						+ DataDatabaseHelper.GAME.APP_ID + "=? and "
						+ DataDatabaseHelper.GAME.LIST_CATE + "=?",
				new String[] { game.list_id, String.valueOf(game.app_id),
						game.list_c });
		return delete != -1;
	}

	public synchronized void delGame() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = mDBHelper.getWritableDb();
			cursor = db.query(DataDatabaseHelper.GAME.TABLE, null, null, null,
					null, null, null);
			if (cursor == null)
				return;
			while (cursor.moveToNext()) {
				int app_id = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.APP_ID));
				db.delete(DataDatabaseHelper.GAME.TABLE,
						DataDatabaseHelper.GAME.APP_ID + "=?",
						new String[] { String.valueOf(app_id) });
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}

	public synchronized List<GAME> getGameList() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		List<GAME> list = null;
		try {
			db = mDBHelper.getWritableDb();
			cursor = db.query(DataDatabaseHelper.GAME.TABLE, null, null, null,
					null, null, null);
			list = new ArrayList<GAME>();
			if (cursor == null)
				return list;
			while (cursor.moveToNext()) {
				GAME newgame = new GAME();
				newgame.app_id = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.APP_ID));
				newgame.list_id = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.LIST_ID));
				newgame.list_c = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.LIST_CATE));
				newgame.d_c = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.DOWN_C));
				newgame.d_d = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.DOWN_D));
				newgame.d_f = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.DOWN_F));
				newgame.d_o = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.DOWN_O));
				newgame.i_f = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.INSTALL_F));
				newgame.i_o = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.INSTALL_O));
				list.add(newgame);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return list;
	}

	public GAME hasGame(GAME game) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		GAME newgame = null;
		try {
			db = mDBHelper.getWritableDb();
			cursor = db.query(DataDatabaseHelper.GAME.TABLE, null,
					DataDatabaseHelper.GAME.APP_ID + "=? and "
							+ DataDatabaseHelper.GAME.LIST_ID + "=? and "
							+ DataDatabaseHelper.GAME.LIST_CATE + "=?",
					new String[] { String.valueOf(game.app_id), game.list_id,
							game.list_c }, null, null, null);
			if (cursor == null)
				return null;
			if (cursor.moveToFirst()) {
				newgame = new GAME();
				newgame.app_id = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.APP_ID));
				newgame.list_id = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.LIST_ID));
				newgame.list_c = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.LIST_CATE));
				newgame.d_c = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.DOWN_C));
				newgame.d_d = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.DOWN_D));
				newgame.d_f = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.DOWN_F));
				newgame.d_o = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.DOWN_O));
				newgame.i_f = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.INSTALL_F));
				newgame.i_o = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.GAME.INSTALL_O));
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return newgame;
	}

	/*-------------------push---------------------*/
	public synchronized void updatePush(PUSH lb) {
		if (lb == null)
			return;
		SQLiteDatabase db = null;
		try {
			db = mDBHelper.getWritableDb();
			ContentValues values = new ContentValues();
			values.put(DataDatabaseHelper.PUSH.PUSH_ID, lb.id);
			values.put(DataDatabaseHelper.PUSH.PUSH_STATE, lb.state);
			values.put(DataDatabaseHelper.PUSH.PUSH_TYPE, lb.type);
			values.put(DataDatabaseHelper.PUSH.PUSH_APPID, lb.appid);
			int update = db.update(DataDatabaseHelper.PUSH.TABLE, values,
					DataDatabaseHelper.PUSH.PUSH_ID + "=?",
					new String[] { String.valueOf(lb.id) });
			if (update == 0)
				db.insert(DataDatabaseHelper.PUSH.TABLE, null, values);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public synchronized boolean delPush(PUSH lb) {
		SQLiteDatabase db = null;
		try {
			db = mDBHelper.getWritableDb();
			int delete = db.delete(DataDatabaseHelper.PUSH.TABLE,
					DataDatabaseHelper.PUSH.PUSH_ID + "=?",
					new String[] { String.valueOf(lb.id) });
			return delete != -1;

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return false;
	}

	public synchronized void delPush() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = mDBHelper.getWritableDb();
			cursor = db.query(DataDatabaseHelper.PUSH.TABLE, null, null, null,
					null, null, null);
			if (cursor == null)
				return;
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.PUSH.PUSH_ID));
				db.delete(DataDatabaseHelper.PUSH.TABLE,
						DataDatabaseHelper.PUSH.PUSH_ID + "=?",
						new String[] { String.valueOf(id) });
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}

	public synchronized PUSH getPush() {
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = mDBHelper.getWritableDb();
			c = db.query(DataDatabaseHelper.PUSH.TABLE, null, null, null, null,
					null, null);
			if (c == null)
				return null;
			while (c.moveToNext()) {
				PUSH item = new PUSH();
				item.id = c.getInt(c
						.getColumnIndex(DataDatabaseHelper.PUSH.PUSH_ID));
				item.state = c.getInt(c
						.getColumnIndex(DataDatabaseHelper.PUSH.PUSH_STATE));
				item.type = c.getInt(c
						.getColumnIndex(DataDatabaseHelper.PUSH.PUSH_TYPE));
				item.appid = c.getInt(c
						.getColumnIndex(DataDatabaseHelper.PUSH.PUSH_APPID));
				return item;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return null;
	}

	/*-------------------app---------------------*/
	public synchronized void updateAPP(APP lb) {
		if (lb == null)
			return;
		SQLiteDatabase db = null;
		try {
			APP newapp = hasAPP();
			delAPP();
			db = mDBHelper.getWritableDb();
			ContentValues values = new ContentValues();
			if (newapp != null) {
				values.put(DataDatabaseHelper.MAPP.MAPP_SC, newapp.sc + lb.sc);
				values.put(DataDatabaseHelper.MAPP.MAPP_FC, newapp.fc + lb.fc);
				values.put(DataDatabaseHelper.MAPP.MAPP_T, newapp.t + lb.t);
			} else {
				values.put(DataDatabaseHelper.MAPP.MAPP_SC, lb.sc);
				values.put(DataDatabaseHelper.MAPP.MAPP_FC, lb.fc);
				values.put(DataDatabaseHelper.MAPP.MAPP_T, lb.t);
			}
			db.insert(DataDatabaseHelper.MAPP.TABLE, null, values);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public synchronized void delAPP() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = mDBHelper.getWritableDb();
			cursor = db.query(DataDatabaseHelper.MAPP.TABLE, null, null, null,
					null, null, null);
			if (cursor != null)
				while (cursor.moveToNext()) {
					int _id = cursor.getInt(cursor.getColumnIndex("_id"));
					db.delete(DataDatabaseHelper.MAPP.TABLE, "_id=?",
							new String[] { String.valueOf(_id) });
				}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}

	public synchronized APP getAPP() {
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = mDBHelper.getWritableDb();
			c = db.query(DataDatabaseHelper.MAPP.TABLE, null, null, null, null,
					null, null);
			if (c == null)
				return null;
			while (c.moveToNext()) {
				APP item = new APP();
				item.sc = c.getInt(c
						.getColumnIndex(DataDatabaseHelper.MAPP.MAPP_SC));
				item.fc = c.getInt(c
						.getColumnIndex(DataDatabaseHelper.MAPP.MAPP_FC));
				item.t = c.getInt(c
						.getColumnIndex(DataDatabaseHelper.MAPP.MAPP_T));
				return item;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return null;
	}

	public APP hasAPP() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		APP newlb = null;
		try {
			db = mDBHelper.getReadableDb();
			cursor = db.query(DataDatabaseHelper.MAPP.TABLE, null, null, null,
					null, null, null);
			if (cursor == null)
				return null;
			while (cursor.moveToNext()) {
				newlb = new APP();
				newlb.sc = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.MAPP.MAPP_SC));
				newlb.fc = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.MAPP.MAPP_FC));
				newlb.t = cursor.getLong(cursor
						.getColumnIndex(DataDatabaseHelper.MAPP.MAPP_T));
				return newlb;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return null;
	}

	public void clearData() {
		delListBean();
		delGame();
		delAPP();
		delPush();
	}

	public void updateDataPoint(HashMap<String, Integer> hasMap) {
		delDP();
		Iterator<String> iterator = hasMap.keySet().iterator();
		SQLiteDatabase db = null;
		try {
			db = mDBHelper.getWritableDb();
			while (iterator.hasNext()) {
				String key = iterator.next();
				ContentValues values = new ContentValues();
				values.put(DataDatabaseHelper.DP.DP_K, key);
				values.put(DataDatabaseHelper.DP.DP_V, hasMap.get(key));
				db.insert(DataDatabaseHelper.DP.TABLE, null, values);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}

	}

	public synchronized void delDP() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = mDBHelper.getWritableDb();
			cursor = db.query(DataDatabaseHelper.DP.TABLE, null, null, null,
					null, null, null);
			if (cursor != null)
				while (cursor.moveToNext()) {
					int _id = cursor.getInt(cursor.getColumnIndex("_id"));
					db.delete(DataDatabaseHelper.DP.TABLE, "_id=?",
							new String[] { String.valueOf(_id) });
				}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}

	public synchronized HashMap<String, Integer> getDP() {
		HashMap<String, Integer> hasMap = null;
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = mDBHelper.getWritableDb();
			c = db.query(DataDatabaseHelper.DP.TABLE, null, null, null, null,
					null, null);
			if (c == null)
				return null;
			hasMap = new HashMap<String, Integer>();
			while (c.moveToNext()) {
				String key = c.getString(c
						.getColumnIndex(DataDatabaseHelper.DP.DP_K));
				int value = c.getInt(c
						.getColumnIndex(DataDatabaseHelper.DP.DP_V));
				hasMap.put(key, value);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return hasMap;
	}

	public synchronized void addKey(String key) {
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = mDBHelper.getWritableDb();
			c = db.query(DataDatabaseHelper.SK.TABLE, null, null, null, null,
					null, null);
			List<Integer> ids = new ArrayList<Integer>();
			while (c.moveToNext()) {
				String sk_k = c.getString(c
						.getColumnIndex(DataDatabaseHelper.SK.SK_K));
				if (sk_k.equals(key))
					return;
				ids.add(c.getInt(c.getColumnIndex("_id")));
			}
			if (ids.size() >= 10) {
				Collections.sort(ids);
				Integer integer = ids.get(0);
				db.delete(DataDatabaseHelper.SK.TABLE, "_id=?",
						new String[] { String.valueOf(integer) });
			}
			ContentValues values = new ContentValues();
			values.put(DataDatabaseHelper.SK.SK_K, key);
			db.insert(DataDatabaseHelper.SK.TABLE, null, values);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}

	public synchronized byte[] getKeys() {
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = mDBHelper.getReadableDb();
			c = db.query(DataDatabaseHelper.SK.TABLE, null, null, null, null,
					null, "_id desc");
			List<String> keys = new ArrayList<String>();
			while (c.moveToNext()) {
				keys.add(c.getString(c
						.getColumnIndex(DataDatabaseHelper.SK.SK_K)));
			}
			int _id = 0;
			StringBuffer sb = new StringBuffer();
			for (String string : keys) {
				_id++;
				String obj = _id + "@" + string;
				sb.append(obj).append("_|_");
			}
			if (keys.size() > 0) {
				String obj = 0 + "@" + "清空历史记录";
				sb.append(obj);
			}
			return sb.toString().getBytes("utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return null;
	}

	public synchronized void clearKeys() {
		SQLiteDatabase db = mDBHelper.getWritableDb();
		db.delete(DataDatabaseHelper.SK.TABLE, null, null);
	}

	public synchronized void clearOneKey(String key) {
		SQLiteDatabase db = mDBHelper.getWritableDb();
		db.delete(DataDatabaseHelper.SK.TABLE, DataDatabaseHelper.SK.SK_K
				+ "=?", new String[] { key });
	}
	/*add by zengxiao for :添加收藏显示*/
	public synchronized void delMyFav() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = mDBHelper.getWritableDb();
			cursor = db.query(DataDatabaseHelper.MYFAVORATE.TABLE, null, null, null,
					null, null, null);
			if (cursor != null)
				while (cursor.moveToNext()) {
					int _id = cursor.getInt(cursor.getColumnIndex("_id"));
					db.delete(DataDatabaseHelper.MYFAVORATE.TABLE, "_id=?",
							new String[] { String.valueOf(_id) });
				}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
	}
	public synchronized void updateMyFavor(Integer sid) {
		if (sid == null)
			return;
		SQLiteDatabase db = null;
		try {
			db = mDBHelper.getWritableDb();
			ContentValues values = new ContentValues();
			values.put(DataDatabaseHelper.MYFAVORATE.sid, sid);			
			db.insert(DataDatabaseHelper.MYFAVORATE.TABLE, null, values);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}
	
	public synchronized void delMYFavor(int sid) {
		try{
		SQLiteDatabase db = mDBHelper2.getWritableDb();
		db.delete(DataDatabaseHelper.MYFAVORATE.TABLE,
				DataDatabaseHelper.MYFAVORATE.sid + "=?",
				new String[] { String.valueOf(sid) });
		}catch(Exception e){}
	}
	public boolean hasMYFavor(int sid) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = mDBHelper2.getReadableDb();
			cursor = db.query(DataDatabaseHelper.MYFAVORATE.TABLE, null,
					DataDatabaseHelper.MYFAVORATE.sid + "=?",
					new String[] { String.valueOf(sid) }, null, null, null);
			if (cursor != null && cursor.getCount() > 0)
				return true;
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("zxnew", e.toString());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public synchronized List<Integer> getMYFaviorateList() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		List<Integer> list = null;
		try {
			db = mDBHelper2.getWritableDb();
			// cursor = db.query(DataDatabaseHelper.GAME.TABLE, null, null,
			// null,
			// null, null, null);
			cursor = db.query(DataDatabaseHelper.MYFAVORATE.TABLE, null, null,
					null, null, null, null);
			list = new ArrayList<Integer>();
			if (cursor == null)
				return list;
			while (cursor.moveToNext()) {
				int sid=cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.sid));
				
				list.add(sid);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return list;
	}

	/*add by zengxiao for :添加收藏显示*/
	public synchronized void updateFavor(AppItem app) {
		if (app == null)
			return;
		SQLiteDatabase db = null;
		try {
			db = mDBHelper.getWritableDb();
			ContentValues values = new ContentValues();
			values.put(DataDatabaseHelper.FAVORATE.iconUrl, app.iconUrl);
			values.put(DataDatabaseHelper.FAVORATE.name, app.name);
			values.put(DataDatabaseHelper.FAVORATE.pname, app.pname);
			values.put(DataDatabaseHelper.FAVORATE.vname, app.vname);
			values.put(DataDatabaseHelper.FAVORATE.vcode, app.vcode);
			values.put(DataDatabaseHelper.FAVORATE.length, app.length);
			values.put(DataDatabaseHelper.FAVORATE.hash, app.hash);
			values.put(DataDatabaseHelper.FAVORATE.sid, app.sid);
			values.put(DataDatabaseHelper.FAVORATE.apk, app.apk);
			values.put(DataDatabaseHelper.FAVORATE.downNum, app.downNum);
			values.put(DataDatabaseHelper.FAVORATE.adesc, app.adesc);
			values.put(DataDatabaseHelper.FAVORATE.stype, app.stype);
			values.put(DataDatabaseHelper.FAVORATE.rating, app.rating);
			values.put(DataDatabaseHelper.FAVORATE.cateid, app.cateid);
			values.put(DataDatabaseHelper.FAVORATE.cateName, app.cateName);
			values.put(DataDatabaseHelper.FAVORATE.state, app.state);
			values.put(DataDatabaseHelper.FAVORATE.rankcount, app.rankcount);
			values.put(DataDatabaseHelper.FAVORATE.commcount, app.commcount);
			values.put(DataDatabaseHelper.FAVORATE.updatetime, app.updatetime);
			values.put(DataDatabaseHelper.FAVORATE.list_id,
					LogModel.L_DOWN_MANAGER);
			values.put(DataDatabaseHelper.FAVORATE.list_cateid,
					LogModel.L_APP_FAVOR);
			values.put(DataDatabaseHelper.FAVORATE.app_level,
					app.ulevel);
			db.insert(DataDatabaseHelper.FAVORATE.TABLE, null, values);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public synchronized void delFavor(int sid) {
		SQLiteDatabase db = mDBHelper.getWritableDb();
		db.delete(DataDatabaseHelper.FAVORATE.TABLE,
				DataDatabaseHelper.FAVORATE.sid + "=?",
				new String[] { String.valueOf(sid) });
	}

	public boolean hasFavor(int sid) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = mDBHelper.getReadableDb();
			cursor = db.query(DataDatabaseHelper.FAVORATE.TABLE, null,
					DataDatabaseHelper.FAVORATE.sid + "=?",
					new String[] { String.valueOf(sid) }, null, null, null);
			if (cursor != null && cursor.getCount() > 0)
				return true;
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public synchronized List<AppItem> getFaviorateList() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		List<AppItem> list = null;
		try {
			db = mDBHelper.getWritableDb();
			// cursor = db.query(DataDatabaseHelper.GAME.TABLE, null, null,
			// null,
			// null, null, null);
			cursor = db.query(DataDatabaseHelper.FAVORATE.TABLE, null, null,
					null, null, null, null);
			list = new ArrayList<AppItem>();
			if (cursor == null)
				return list;
			while (cursor.moveToNext()) {
				AppItem app = new AppItem();
				app.iconUrl = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.iconUrl));
				app.name = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.name));
				app.pname = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.pname));
				app.vname = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.vname));
				app.vcode = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.vcode));
				app.length = cursor.getLong(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.length));
				app.hash = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.hash));
				app.sid = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.sid));
				app.apk = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.apk));
				app.downNum = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.downNum));
				app.adesc = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.adesc));
				app.stype = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.stype));
				app.rating = cursor.getFloat(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.rating));
				app.cateid = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.cateid));
				app.cateName = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.cateName));
				app.state = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.state));
				app.rankcount = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.rankcount));
				app.commcount = cursor.getInt(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.commcount));
				app.updatetime = cursor
						.getString(cursor
								.getColumnIndex(DataDatabaseHelper.FAVORATE.updatetime));
				app.list_id = cursor.getString(cursor
						.getColumnIndex(DataDatabaseHelper.FAVORATE.list_id));
				app.list_cateid = cursor
						.getString(cursor
								.getColumnIndex(DataDatabaseHelper.FAVORATE.list_cateid));
				app.ulevel = cursor
						.getInt(cursor
								.getColumnIndex(DataDatabaseHelper.FAVORATE.app_level));
				list.add(app);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return list;
	}

	private OldAppDownloadDatabaseHelper mOldAppDownloadDatabaseHelper;

	/**
	 * 获取v1.0的下载列�? v1.1更新使用
	 * */
	public synchronized List<DownloadItem> getOldDownloadList(Context context) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		List<DownloadItem> list = null;
		try {
			if (mOldAppDownloadDatabaseHelper == null) {
				mOldAppDownloadDatabaseHelper = new OldAppDownloadDatabaseHelper(
						context);
			}
			db = mOldAppDownloadDatabaseHelper.getWritableDb();
			cursor = db.query(OldAppDownloadDatabaseHelper.TABLE_APP_DOWNLOAD,
					null, null, null, null, null, null);
			list = new ArrayList<DownloadItem>();
			if (cursor == null)
				return list;
			android.util.Log.d("cx", "cursor is " + cursor.getCount());
			while (cursor.moveToNext()) {
				DownloadItem app = new DownloadItem();
				app.sid = cursor
						.getInt(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.APP_ID));
				app.lastDSize = cursor
						.getInt(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.DSIZE));
				app.apk = cursor
						.getString(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.DURL));
				app.iconUrl = cursor
						.getString(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.IURL));
				app.name = cursor
						.getString(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.NAME));
				app.pname = cursor
						.getString(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.PNAME));
				app.length = cursor
						.getLong(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.SIZE));
				app.dSize = cursor
						.getLong(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.REALSIZE));
				app.state = cursor
						.getInt(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.STATE));// old
																									// state
																									// to
																									// new
				app.vname = cursor
						.getString(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.VERSION));
				app.vcode = cursor
						.getInt(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.VERSION_CODE));
				/*
				 * cursor.getString(cursor
				 * .getColumnIndex(OldAppDownloadDatabaseHelper
				 * .TAppDownload.FREE));
				 */
				app.list_cateid = cursor
						.getString(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.LIST_CATEID));
				app.list_id = cursor
						.getString(cursor
								.getColumnIndex(OldAppDownloadDatabaseHelper.TAppDownload.LIST_ID));

				list.add(app);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return list;
	}

	/**
	 * v1.1升级下载列表数据库完成后删除原来数据
	 * */
	public synchronized void removeOldDownloadTable(Context context) {
		SQLiteDatabase db = null;
		try {
			if (mOldAppDownloadDatabaseHelper == null) {
				mOldAppDownloadDatabaseHelper = new OldAppDownloadDatabaseHelper(
						context);
			}
			db = mOldAppDownloadDatabaseHelper.getWritableDb();
			int deleteCount = db
					.delete(OldAppDownloadDatabaseHelper.TABLE_APP_DOWNLOAD,
							null, null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {

			if (db != null) {
				db.close();
			}
		}
	}

}
