package com.byt.market.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.data.CateItem;
import com.byt.market.data.HomeItemBean;
import com.byt.market.log.LogModel;

public class HomeToutils {
	
	public static void getTo(HomeItemBean beana,Context context){
		Intent  intent;
		switch (Integer.parseInt(beana.type)) {
		case 2://音乐	
			intent = new Intent(Constants.TOMusicLIST);
			//SubjectItem app = new SubjectItem();
			CateItem app = new CateItem();
			//app.sid = appItem.musicRid;
			//app.id = appItem.musicRid;
			
			Log.d("test", "-----idid------"+beana.contentId);
			//Log.d("test", "-----idid------"+beana.contentId);
			//Log.d("test", "-----idid------"+beana.contentId);
			//Log.d("test", "-----idid------"+beana.contentId);
			app.id = beana.albumId;
			
			app.ImagePath = beana.ic_url;
			app.cTitle = beana.albumName;
			app.cDesc = beana.abumDes;
			app.cCount = 2;
			intent.putExtra("app", app);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
			((FragmentActivity) context).startActivity(intent);
			((FragmentActivity) context).overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);		
			break;
		case 4://电视
			intent = new Intent(Constants.TOTVLIST);
			//SubjectItem app = new SubjectItem();
			CateItem app3 = new CateItem();
			//app.sid = appItem.musicRid;
			//app.id = appItem.musicRid;
			app3.id = beana.albumId;
			
			app3.ImagePath = beana.ic_url;
			app3.cTitle = beana.albumName;
			app3.cDesc = beana.abumDes;
			app3.cCount = 2;
			intent.putExtra("app", app3);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
			((FragmentActivity) context).startActivity(intent);
			((FragmentActivity) context).overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		case 6://动画
			intent = new Intent(Constants.TOTVLIST);
			//SubjectItem app = new SubjectItem();
			CateItem app4 = new CateItem();
			//app.sid = appItem.musicRid;
			//app.id = appItem.musicRid;
			app4.id = beana.albumId;
			
			app4.ImagePath = beana.ic_url;
			app4.cTitle = beana.albumName;
			app4.cDesc = beana.abumDes;
			app4.cCount = 2;
			intent.putExtra("app", app4);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
			((FragmentActivity) context).startActivity(intent);
			((FragmentActivity) context).overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		case 8://电台
			intent = new Intent(Constants.TOMusicLIST);
			//SubjectItem app = new SubjectItem();
			CateItem app2 = new CateItem();
			//app.sid = appItem.musicRid;
			//app.id = appItem.musicRid;
			app2.id = beana.albumId;
			
			app2.ImagePath = beana.ic_url;
			app2.cTitle = beana.albumName;
			app2.cDesc = beana.abumDes;
			app2.cCount = 2;
			intent.putExtra("app", app2);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
			((FragmentActivity) context).startActivity(intent);
			((FragmentActivity) context).overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		case 10://文本小说
			intent = new Intent(Constants.TONOVELLIST);
			//SubjectItem app = new SubjectItem();
			CateItem app5 = new CateItem();
			//app.sid = appItem.musicRid;
			//app.id = appItem.musicRid;
			app5.id = beana.albumId;
			
			app5.ImagePath = beana.ic_url;
			app5.cTitle = beana.albumName;
			app5.cDesc = beana.abumDes;
			app5.cCount = 2;
			intent.putExtra("app", app5);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
			((FragmentActivity) context).startActivity(intent);
			((FragmentActivity) context).overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		default:
			break;
		}
	}
}
