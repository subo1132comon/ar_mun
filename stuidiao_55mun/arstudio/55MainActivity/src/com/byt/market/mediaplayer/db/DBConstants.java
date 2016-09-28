package com.byt.market.mediaplayer.db;

public class DBConstants
{
	public static String DB_CREATE = "CREATE TABLE downdb(thid INTEGER, done INTEGER,"
			+ "savepath text,length real,name text,strlength text,hash text,state INTEGER,type INTEGER,uri VARCHAR(1024),PRIMARY KEY(thid));";
	public static String DB_INSERT = "INSERT INTO downdb(thid,done,savepath,length,name,strlength,hash,state,type,uri) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	public static String DB_DELETE = "DELETE FROM downdb WHERE savepath=? AND thid=?";
	public static String DB_UPDATE = "UPDATE downdb SET done=? WHERE thid=?";
	public static String DB_QUERY = "SELECT * FROM downdb WHERE savepath=? AND thid=?";
	public static String DB_DELETE_ALL = "SELECT SUM(done) FROM downdb WHERE savepath=?";
	public static String DB_2_DELETE = "DELETE FROM downdb WHERE savepath=?";
	public static String DB_QUERY_UNDONE = "SELECT * FROM downdb";
}
