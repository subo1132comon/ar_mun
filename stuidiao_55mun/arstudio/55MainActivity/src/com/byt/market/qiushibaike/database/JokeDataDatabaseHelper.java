package com.byt.market.qiushibaike.database;
import com.byt.market.download.GamesProvider.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class JokeDataDatabaseHelper extends SQLiteOpenHelper{

	private static JokeDataDatabaseHelper mIntance = null;
	public static final String DATABASE_NAME = "jokefeedback";
	public static final int DATABASE_VERSION = 1;
	
	public JokeDataDatabaseHelper(Context context) 
	{
		this(context, DATABASE_NAME, DATABASE_VERSION);
	}
	public JokeDataDatabaseHelper(Context context, String name) 
	{
		this(context, name, DATABASE_VERSION);
	}	
	public JokeDataDatabaseHelper(Context context, String name, int ver) 
	{
		this(context, name, null, ver);
	}	

	public JokeDataDatabaseHelper(Context context, String name,	CursorFactory factory, int version) 
	{
		super(context, name, factory, version);
	}
	
	public static synchronized JokeDataDatabaseHelper getInstance(Context context)
	{
		if(mIntance == null)
		{
			mIntance = new JokeDataDatabaseHelper(context);
		}
		return mIntance;
	}
	
	public static class joke_feedback_list{
		public static final String TABLE = "jokefeedback";
		public static final String JOKESID = "sid";
		public static final String JOKECOMMFLAG = "iscommented";
		public static final String JOKEVOTEFLAG = "isvoted";
		
		public static String createTable(){
			StringBuffer sb = new StringBuffer();
			sb.append("create table if not exists jokefeedback (_id integer primary key autoincrement,");
			sb.append(JOKESID).append(" text,");
			sb.append(JOKECOMMFLAG).append(" integer,");
			sb.append(JOKEVOTEFLAG).append(" integer)");
			return sb.toString();
		}
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//db.execSQL(joke_feedback_list.createTable());
		
		db.execSQL("create table jokefeedback (_id integer primary key autoincrement,sid text, iscommented text, isvoted text)");
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS jokefeedback");
		//db.execSQL(joke_feedback_list.createTable());
		db.execSQL("create table jokefeedback (_id integer primary key autoincrement,sid text, iscommented text, isvoted text)");

	}
	
	@Override
	public SQLiteDatabase getReadableDatabase() {
		// TODO Auto-generated method stub
		return super.getReadableDatabase();
	}

	@Override
	public SQLiteDatabase getWritableDatabase() {
		// TODO Auto-generated method stub
		return super.getWritableDatabase();
	}

}
