package com.byt.market.mediaplayer.db;

import java.util.ArrayList;
import java.util.List;

import com.byt.market.mediaplayer.data.PlayDownloadItem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class InfoDao
{
	private DBOpenHelper openHelper;

	public InfoDao(Context context)
	{
		openHelper = new DBOpenHelper(context);
	}

	public void insert(PlayDownloadItem info)
	{
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL(DBConstants.DB_INSERT, new Object[]
		{ info.sid, info.cursize,info.getSavePath(),info.length,info.name,info.strLength,info.hash,info.state,info.getType(),info.musicuri});
	}

	public void delete(int thid,String path)
	{
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL(DBConstants.DB_DELETE, new Object[]
		{ path, thid });
	}

	public void update(PlayDownloadItem info)
	{
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL(DBConstants.DB_UPDATE, new Object[]
		{info.sid});
	}

	public PlayDownloadItem query(String path, int thid)
	{
		SQLiteDatabase db = openHelper.getWritableDatabase();
		Cursor c = db.rawQuery(DBConstants.DB_QUERY, new String[]
		{ path, String.valueOf(thid) });
		PlayDownloadItem info = null;
		if (c.moveToNext())
			info = new PlayDownloadItem(c.getInt(0), c.getLong(1), c.getString(2),c.getLong(3), c.getString(4)
					, c.getString(5), c.getString(6), c.getInt(7), c.getInt(8), c.getString(9));
		c.close();
		return info;
	}

	public void deleteAll(String path, int len)
	{
		SQLiteDatabase db = openHelper.getWritableDatabase();
		Cursor c = db.rawQuery(DBConstants.DB_DELETE_ALL, new String[]
		{ path });
		if (c.moveToNext())
		{
			int result = c.getInt(0);
			if (result == len)
				db.execSQL(DBConstants.DB_2_DELETE, new Object[]
				{ path });
		}
	}

	public List<PlayDownloadItem> queryUndone()
	{
		SQLiteDatabase db = openHelper.getWritableDatabase();
		Cursor c = db.rawQuery(DBConstants.DB_QUERY_UNDONE, null);
		List<PlayDownloadItem> pathList = new ArrayList<PlayDownloadItem>();
		while (c.moveToNext())
		{
			PlayDownloadItem playDown=new PlayDownloadItem(c.getInt(0), c.getLong(1), c.getString(2),c.getLong(3), c.getString(4)
					, c.getString(5), c.getString(6), c.getInt(7), c.getInt(8), c.getString(9));
			pathList.add(playDown);
		}
		c.close();
		return pathList;
	}
}
