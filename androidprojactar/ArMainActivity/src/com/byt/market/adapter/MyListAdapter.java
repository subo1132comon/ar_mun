package com.byt.market.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.PopupWindow;

import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.ShareActivity;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.DownloadAppItem;
import com.byt.market.data.DownloadItem;
import com.byt.market.data.UserData;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.ui.DetailFragment;
import com.byt.market.ui.HomeFragment;
import com.byt.market.util.DataUtil;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.Util;

public class MyListAdapter extends BaseAdapter {

	private List<String> myArrList = null;

	private AppItem mapp = null;
	private PopupWindow moptionmenu;
	private int selectedPosition = -1;//

	private Context context;

	public MyListAdapter(Context context, PopupWindow moptionmenu,
			List<String> buttonListView, AppItem app) {
		myArrList = buttonListView;
		mapp = app;
		this.context = context;
		this.moptionmenu = moptionmenu;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return myArrList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return myArrList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void setSelectedPosition(int position) {
		selectedPosition = position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int myposition = position;
		final RecordLayoutHolder mlayout;
		RecordLayoutHolder layoutHolder = null;
		if (convertView == null) {
			layoutHolder = new RecordLayoutHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.bestone_choicebutton, null, false);
			layoutHolder.setas = (Button) convertView.findViewById(R.id.setas);
			convertView.setTag(layoutHolder);
		} else {
			layoutHolder = (RecordLayoutHolder) convertView.getTag();
		}
		mlayout = layoutHolder;
		layoutHolder.setas.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (moptionmenu != null && moptionmenu.isShowing()) {
					moptionmenu.dismiss();
				}
				switch (myposition) {
				case 0:
					Log.d("myzx", "downonclick");
					UserData user = MyApplication.getInstance()
							.getUser();
					if(mapp.isshare==1&&mlayout.setas.getText().toString().equals(context.getString(R.string.expend_child1_text)))
					{
						Intent intent =new Intent();
						intent.setClass(context,ShareActivity.class);						
						intent.putExtra("sendstring", mapp); 
						context.startActivity(intent);
					}
					else
					{
					DownloadTaskManager.getInstance().onDownloadBtnClick(mapp);
					}
					break;
				case 1:
					if(!(DataUtil.getInstance(context).hasFavor(mapp.sid))){
					Log.d("myzx", "collect" + mapp.name);
					Util.addData(MarketContext.getInstance(), mapp);
					context.sendBroadcast(new Intent("com.byt.market.updatemycellet"));
					}
					break;
				}

			}
		});

		layoutHolder.setas.setText(myArrList.get(myposition));
		if (myposition == 0) {
			/*mapp=DownloadTaskManager.getInstance().getAppItemStates(mapp);
			DownloadTaskManager.getInstance().updateItemBtnByState(context,
					layoutHolder.setas, mapp);*/
		}
		return convertView;

	}
	public class RecordLayoutHolder {
		public Button setas;

	}
}
