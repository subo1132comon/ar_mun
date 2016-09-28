package com.byt.market.mediaplayer.db.ormlite;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.content.Context;

public class Dao {

	Context mcontext;
	static DatabaseHelper mhelper;

	public Dao(Context context) {
		super();
		this.mcontext = context;
		if (null == mhelper)
			mhelper = new DatabaseHelper(context);
	}

	public Context getMcontext() {
		return mcontext;
	}

	public void setMcontext(Context mcontext) {
		this.mcontext = mcontext;
	}

	public DatabaseHelper getHelper() {
		return this.mhelper;
	}

	public void setHelper(DatabaseHelper helper) {
		this.mhelper = helper;
	}
}
