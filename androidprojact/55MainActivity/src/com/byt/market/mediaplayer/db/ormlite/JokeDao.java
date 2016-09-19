package com.byt.market.mediaplayer.db.ormlite;

import java.sql.SQLException;
import java.util.List;

import com.byt.market.data.CateItem;

import android.content.Context;

public class JokeDao extends Dao{

	public JokeDao(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void addJoke(CateItem item){
		try {
			getHelper().getDao(CateItem.class).create(item);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<CateItem > getJokeAll(){
		List<CateItem> items = null;
		try {
			 items = getHelper().getDao(CateItem.class).queryForAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return items; 
	}

}
