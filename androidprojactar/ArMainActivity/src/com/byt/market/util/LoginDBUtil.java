package com.byt.market.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.byt.market.data.LoadPage;
import com.byt.market.data.LoginInfo;
import com.byt.market.database.MDBHelper;

public class LoginDBUtil {
	public static void add(Context context, LoadPage lPage) {
		if (lPage == null)
			return;
		SQLiteDatabase db = null;
		try {
			db = new MDBHelper(context).getWritableDb();
			if (lPage != null) {
				ContentValues values = new ContentValues();
				values.put(MDBHelper.LoadPage.URL, lPage.url);
				values.put(MDBHelper.LoadPage.CODE, lPage.code);
				Cursor c = db.query(MDBHelper.LoadPage.LoadPage, null, null, null, null,
						null, null);
				while (c.moveToNext()) {
					int rowid = c.getInt(c
							.getColumnIndex(MDBHelper.LoadPage._ID));
					db.delete(MDBHelper.LoadPage.LoadPage,
							MDBHelper.LoadPage._ID + "=?",
							new String[] { String.valueOf(rowid) });
				}
				if (c != null)
					c.close();
				db.insert(MDBHelper.LoadPage.LoadPage, null, values);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public static LoginInfo getLoginInfo(Context context) {
		LoginInfo loginInfo = new LoginInfo();
		loginInfo.page = getLoadPage(context);
		return loginInfo;
	}

	public static LoadPage getLoadPage(Context context) {
		SQLiteDatabase db = null;
		LoadPage loadPage = null;
		Cursor c = null;
		try {
			db = new MDBHelper(context).getWritableDb();
			c = db.query(MDBHelper.LoadPage.LoadPage, null, null, null, null, null, null);
			while (c.moveToNext()) {
				loadPage = new LoadPage();
				loadPage.url = c.getString(c
						.getColumnIndex(MDBHelper.LoadPage.URL));
				loadPage.code = c.getInt(c
						.getColumnIndex(MDBHelper.LoadPage.CODE));
			}
		} catch (Exception e) {

		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return loadPage;
	}

}
