package com.byt.market.mediaplayer.nover;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.data.BigItem;
import com.byt.market.data.CateItem;
import com.byt.market.log.LogModel;
import com.byt.market.tools.LogCart;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.umeng.analytics.MobclickAgent;

/**
 * 音乐歌单列表
 * 
 * @author qiuximing
 * 
 */
public class NovelSubFragment extends ListViewFragment {

	String netType;
	private TextView musiclandtitle;
	private DisplayImageOptions mOptions;

	@Override
	protected String getRequestUrl() {
		// return Constants.LIST_URL + "?qt=" + Constants.CATE+"&type=wifi";
		// if(!TextUtils.isEmpty(netType) && "wifi".equals(netType)){
		// }else{
		return Constants.APK_URL + "Music/v1.php?qt=Ranklist&type=8" + "&pi="
				+ getPageInfo().getNextPageIndex() + "&ps="
				+ getPageInfo().getPageSize()+"&stype="+MainActivity.RIDIAO_KEY;
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
			return JsonParse
					.parseCateMusicLandList(result.getJSONArray("data"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void canRequestGet() {
		//request();
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
		mOptions = new DisplayImageOptions.Builder().cacheOnDisc().build();
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
				view = inflater.inflate(R.layout.listitem_musiclandcate, null);
				holder.layoutType = item.layoutType;
				CategoryItemHolder itemHolder1 = new CategoryItemHolder();
				itemHolder1.content = (TextView) view
						.findViewById(R.id.listitem_cate_name_left);
				itemHolder1.icon = (ImageView) view
						.findViewById(R.id.musicrankpic);
				itemHolder1.layout = view
						.findViewById(R.id.musicranklandlayout);
				itemHolder1.mycontent = (FrameLayout) view
						.findViewById(R.id.istitem_music_content_1);
				CategoryItemHolder itemHolder2 = new CategoryItemHolder();
				itemHolder2.icon = (ImageView) view
						.findViewById(R.id.musicrankpic2);
				itemHolder2.layout = view
						.findViewById(R.id.musicranklandlayout2);
				itemHolder2.content = (TextView) view
						.findViewById(R.id.listitem_cate_name_left2);
				itemHolder2.mycontent = (FrameLayout) view
						.findViewById(R.id.istitem_music_content_2);
				holder.cateHolders.add(itemHolder1);
				holder.cateHolders.add(itemHolder2);
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
					if (item.cateItems.size() < 2) {
						cateHolders.get(1).icon.setVisibility(View.INVISIBLE);
						cateHolders.get(1).layout.setVisibility(View.INVISIBLE);
						cateHolders.get(1).content
								.setVisibility(View.INVISIBLE);
						cateHolders.get(1).mycontent
								.setVisibility(View.INVISIBLE);
					} else {
						cateHolders.get(1).icon.setVisibility(View.VISIBLE);
						cateHolders.get(1).layout.setVisibility(View.VISIBLE);
						cateHolders.get(1).content.setVisibility(View.VISIBLE);
						cateHolders.get(1).mycontent
								.setVisibility(View.VISIBLE);
					}
					for (int i = 0; i < item.cateItems.size()
							&& i < cateHolders.size(); i++) {

						final CateItem cateItem = item.cateItems.get(i);
						if (cateItem.cTitle != null) {
							cateHolders.get(i).content.setText(cateItem.cTitle);
						}
						if (TextUtils.isEmpty(cateItem.ImagePath)) {
							cateHolders.get(i).icon
									.setImageResource(R.drawable.default_disc_360);
						} else if (!imageLoader.getPause().get()) {
							imageLoader.displayImage(cateItem.ImagePath,
									cateHolders.get(i).icon, mOptions);
						}

						cateHolders.get(i).layout
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										LogCart.Log("音乐------------");
										onAppClick(cateItem,
												Constants.TOridiaoLLIST);

									}
								});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				/* } */
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
			caItem.cCount = 2;
			intent.putExtra("app", caItem);
			// RingActivity.mRingActivity.cateItem = caItem;
			// ((MyApplication) getActivity().getApplication()
			// .getApplicationContext()).saveCurrCateItem(caItem);
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

	@Override
	public void setStyle(CusPullListView listview2) {
		// TODO Auto-generated method stub
		super.setStyle(listview2);
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.musicland_header, null);
		musiclandtitle = (TextView) view.findViewById(R.id.musiclandtitle);
		musiclandtitle.setText(R.string.popular);
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
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("电台");
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("电台");
	}
	
	@Override
	protected List<BigItem> dblistData() {
		// TODO Auto-generated method stub
		return null;
	}

}
