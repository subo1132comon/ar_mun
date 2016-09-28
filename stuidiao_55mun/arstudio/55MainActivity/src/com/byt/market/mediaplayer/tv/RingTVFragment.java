package com.byt.market.mediaplayer.tv;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.data.BigItem;
import com.byt.market.data.CateItem;
import com.byt.market.log.LogModel;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.umeng.analytics.MobclickAgent;

/**
 * 音乐排行榜列表
 * 
 * @author qiuximing
 * 
 */
public class RingTVFragment extends ListViewFragment {

	String netType;

	private DisplayImageOptions mOptions;

	@Override
	protected String getRequestUrl() {
		// return Constants.LIST_URL + "?qt=" + Constants.CATE+"&type=wifi";
		// if(!TextUtils.isEmpty(netType) && "wifi".equals(netType)){
		// }else{
		Log.d("myzx", Constants.LIST_URL + "?qt=" + Constants.CATE
				+ "&ctype=app" + "&pi=" + getPageInfo().getNextPageIndex()
				+ "&ps=" + getPageInfo().getPageSize());
		return Constants.APK_URL + "Music/v1.php?qt=Ranklist&type=3" + "&pi="
				+ getPageInfo().getNextPageIndex() + "&ps="
				+ getPageInfo().getPageSize()+"&stype="+MainActivity.TV_KEY;

		/*
		 * Constants.LIST_URL + "?qt=" + Constants.CATE+"&ctype=app"+ "&pi=" +
		 * getPageInfo().getNextPageIndex() + "&ps=" +
		 * getPageInfo().getPageSize();
		 */
		// }
	}

	@Override
	protected String getRequestContent() {
		return null;
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		try {
			return JsonParse.parseCateMusicRankList(result.getJSONArray("data"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.listview;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		netType = Util.getNetAvialbleType(MyApplication.getInstance());
		if (!TextUtils.isEmpty(netType) && "wifi".equals(netType)) {
			// mImageFetcher.setLoadingImage(R.drawable.category_loading);
		} else {
			// mImageFetcher.setLoadingImage(R.drawable.app_empty_icon);
		}
		mOptions = new DisplayImageOptions.Builder().cacheOnDisc()
				.delayBeforeLoading(200).build();
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);
		// if(this.getActivity() != null && this.getActivity() instanceof
		// MainActivity){
		// final MainActivity activity = (MainActivity)this.getActivity();
		// if(activity.mAdapter != null && activity.mAdapter.list != null&&
		// activity.mAdapter.list.size() > MainActivity.MAIN_CATE ){
		// activity.mAdapter.list.set(MainActivity.MAIN_CATE, this);
		// }
		// }
		return view;
	}

	// edit by wangxin
	@Override
	protected ImageAdapter createAdapter() {
		String netType = Util.getNetAvialbleType(MyApplication.getInstance());
		if (!TextUtils.isEmpty(netType)) {
			// if("wifi".equals(netType)){
			// return new WifiCateAdapter();
			// }
		}
		return new WifiCateAdapter();
	}

	private class WifiCateAdapter extends ImageAdapter {

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_CAEGORYLIST:
				view = inflater.inflate(R.layout.listitem_musiccate, null);
				holder.layoutType = item.layoutType;
				CategoryItemHolder itemHolder1 = new CategoryItemHolder();
				itemHolder1.icon = (ImageView) view
						.findViewById(R.id.musicrankicon);
				itemHolder1.content = (TextView) view
						.findViewById(R.id.cate_content);
				itemHolder1.name = (TextView) view
						.findViewById(R.id.listitem_cate_name_left);
				itemHolder1.layout = view.findViewById(R.id.listitem_cate_left);
				itemHolder1.musicHolder = (TextView) view
						.findViewById(R.id.musiclayout);
				holder.cateHolders.add(itemHolder1);
				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_LOADING:
				view = inflater.inflate(R.layout.listitem_loading, null);
				holder.layoutType = item.layoutType;

				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_LOADFAILED:
				view = inflater.inflate(R.layout.listitem_loadfailed2, null);
				holder.layoutType = item.layoutType;
				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						retry();
					}
				});
				view.setTag(holder);
				break;
			}
			return view;
		}

		@Override
		public void bindView(int position, BigItem item, BigHolder holder) {
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_CAEGORYLIST:

				try {
					ArrayList<CategoryItemHolder> cateHolders = holder.cateHolders;
					final CateItem cateItem = item.cateItems.get(0);
					cateHolders.get(0).name.setText(cateItem.cTitle);
					// edit by wangxin.
					if (cateItem.cDesc != null) {
						/* bestone add by zengxiao for :添加分类详情介绍 */
						cateHolders.get(0).content.setText(cateItem.cDesc);
						/* add end */
					}
					if (TextUtils.isEmpty(cateItem.ImagePath)) {
						cateHolders.get(0).icon
								.setImageResource(R.drawable.default_disc_180);
					} else if (!imageLoader.getPause().get()) {
						imageLoader.displayImage(cateItem.ImagePath,
								cateHolders.get(0).icon, mOptions);
					}

					cateHolders.get(0).musicHolder
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									onAppClick(cateItem, Constants.TOTVLIST);

								}
							});
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case BigItem.Type.LAYOUT_LOADING:

				break;
			case BigItem.Type.LAYOUT_LOADFAILED:

				break;
			}
		}

	}

	@Override
	protected void onDownloadStateChanged() {

	}

	@Override
	public void onAppClick(Object obj, String action) {
		Intent intent = new Intent(action);
		if (obj instanceof CateItem) {
			CateItem caItem = (CateItem) obj;
			caItem.cCount = 1;
			intent.putExtra("app", caItem);
			// RingActivity.mRingActivity.cateItem = caItem;
			// ((MyApplication)getActivity().getApplication().getApplicationContext()).saveCurrCateItem(caItem);

		}
		intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub

	}
	private TextView musiclandtitle;
	@Override
	public void setStyle(CusPullListView listview2) {
		// TODO Auto-generated method stub
		super.setStyle(listview2);
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.musichead, null);
		musiclandtitle = (TextView) view.findViewById(R.id.runk);
		musiclandtitle.setText(R.string.classic_rank);
		listview2.addHeaderView(view);
	}

	// 修改提示语
	@Override
	public void setButtonInvi() {
		super.setButtonInvi();
		loadfailed.setText(getString(R.string.nodata));
		loadfailed.setButtonVisible(View.GONE);
	}

	@Override
	protected String getRefoushtUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<BigItem> dblistData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("TV排行");
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("TV排行");
	}
}
