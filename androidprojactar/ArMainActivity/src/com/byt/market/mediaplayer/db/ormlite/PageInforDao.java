package com.byt.market.mediaplayer.db.ormlite;

import java.sql.SQLException;

import com.byt.market.data.PageInfo;

import android.content.Context;

public class PageInforDao extends Dao{

	public PageInforDao(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public void addPaginfor(PageInfo infor){
		try {
			getHelper().getDao(PageInfo.class).create(infor);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public PageInfo getPageInfor(){
//		//return get
//	}

}
