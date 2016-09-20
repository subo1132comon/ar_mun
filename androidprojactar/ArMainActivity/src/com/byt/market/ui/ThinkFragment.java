package com.byt.market.ui;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.activity.GameSearchFrame.OnKeySelectedListener;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.data.AppItem;
import com.byt.market.data.BigItem;
import com.byt.market.data.SearchHint;
import com.byt.market.log.LogModel;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.CustomListView;

/**
 * 专题列表
 * 
 * @author Administrator
 * 
 */
public class ThinkFragment extends ListViewFragment {

	private int from;
	private OnKeySelectedListener mOnKeySelected;
	private String key;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		from = getArguments().getInt(Constants.LIST_FROM);
	}

	@Override
	protected String tag() {
		return this.getClass().getSimpleName();
	}

	@Override
	protected String getRequestUrl() {
		return Constants.LIST_URL + "?qt=" + Constants.SEARCH_HINT + "&word="
				+ Util.encodeContentForUrl(key);
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setOnKeySelectedListener(OnKeySelectedListener listener) {
		this.mOnKeySelected = listener;
	}

	@Override
	protected String getRequestContent() {
		return null;
	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		try {
			if (!result.isNull("data")) {
				JSONArray jsonArray = result.getJSONArray("data");
				return JsonParse.parseThinkList(jsonArray);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.listview;
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);

		return view;
	}

	@Override
	protected ImageAdapter createAdapter() {
		return new ThinkAdapter();
	}

	private class ThinkAdapter extends ImageAdapter {

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_THINK:
				view = inflater.inflate(R.layout.listitem_think_item, null);
				holder.layoutType = item.layoutType;
				ThinkHolder itemHolder = new ThinkHolder();
				itemHolder.img =  (ImageView) view
						.findViewById(R.id.suggestion_item_icon);
				itemHolder.name = (TextView) view
						.findViewById(R.id.suggestion_item_title);
				itemHolder.des = (TextView) view
						.findViewById(R.id.suggestion_item_des);
				itemHolder.layout = view.findViewById(R.id.suggestion_item);
				itemHolder.layout.setFocusable(false);
				holder.thinkHolder = itemHolder;
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
		public void bindView(int position, BigItem item, final BigHolder holder) {
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_THINK:
				for (final SearchHint app : item.thinks) {
					if (app.app != null) {
						holder.thinkHolder.name.setText(app.app.name);
						StringBuilder sb = new StringBuilder();
						if(!TextUtils.isEmpty(app.app.cateName)){
							sb.append(app.app.cateName).append(" | ");
						}
						if(!TextUtils.isEmpty(app.app.strLength)){
							sb.append(app.app.strLength).append(" | ");
						}
						if(!TextUtils.isEmpty(app.app.downNum)){
							sb.append(app.app.downNum);
						}
						holder.thinkHolder.des.setVisibility(View.VISIBLE);
						holder.thinkHolder.des.setText(sb.toString());
						if (app.app.iconUrl != null){
							if(app.app.iconUrl.startsWith("http://")){
//								holder.thinkHolder.img.setUrl(app.app.iconUrl);
								imageLoader.displayImage(app.app.iconUrl, holder.thinkHolder.img, options);
							}else{
								imageLoader.displayImage(Constants.IMG_URL
										+ app.app.iconUrl, holder.thinkHolder.img, options);
//							holder.thinkHolder.img.setUrl(Constants.IMG_URL
//									+ app.app.iconUrl);
							}
						}else
							holder.thinkHolder.img
									.setImageResource(R.drawable.search_btn_suggestion_icon);
					} else {
						holder.thinkHolder.name.setText(app.key);
						holder.thinkHolder.des.setVisibility(View.GONE);
						holder.thinkHolder.img
								.setImageResource(R.drawable.search_btn_suggestion_icon);
					}
					holder.thinkHolder.layout
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									if (app.app != null)
										onAppClick(app.app, Constants.TODETAIL);
									else
										mOnKeySelected.onKeySelected(app.key);
								}
							});
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
		if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			intent.putExtra("app", app);
		}
		intent.putExtra(Constants.LIST_FROM, LogModel.SEARCH_CATE_THINK);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	@Override
	public void setStyle(CusPullListView listview2) {
		listview2.setCacheColorHint(0);
	}

	@Override
	public void showNoResultView() {
		loading.setVisibility(View.GONE);
		loadfailed.setVisibility(View.GONE);
	}

	@Override
	public void addNewDataOnce() {
		Util.addListData(maContext, LogModel.L_SEARCH_HIT,
				LogModel.SEARCH_CATE_THINK, 1);
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub
		
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
}
