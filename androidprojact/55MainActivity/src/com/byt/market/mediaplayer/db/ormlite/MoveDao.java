package com.byt.market.mediaplayer.db.ormlite;

import java.sql.SQLException;
import java.util.List;

import com.byt.market.mediaplayer.data.VideoItem;

import android.content.Context;

public class MoveDao extends Dao{

	public MoveDao(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public void addAllmove(VideoItem video){
		try {
			getHelper().getDao(VideoItem.class).create(video);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<VideoItem> queryAll(){
		try {
			return getHelper().getDao(VideoItem.class).queryForAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
