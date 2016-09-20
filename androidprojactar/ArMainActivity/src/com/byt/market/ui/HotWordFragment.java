package com.byt.market.ui;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.activity.GameSearchFrame.OnKeySelectedListener;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.data.BigItem;
import com.byt.market.data.SearchHotword;
import com.byt.market.log.LogModel;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.view.CustomListView;

/**
 * 搜索热词列表
 * 
 * @author Administrator
 * 
 */
public class HotWordFragment extends HotWordListViewFragment {

	private int from;
	private OnKeySelectedListener mOnKeySelected;

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
		return Constants.LIST_URL + "?qt=" + Constants.HOTWORD + "&pi="
				+ getPageInfo().getNextPageIndex() + "&ps="
				+ 9;
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
				return JsonParse.parseHotwordList(getActivity(),jsonArray);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.hotworklistview;
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);

		return view;
	}

	@Override
	protected ImageAdapter createAdapter() {
		return new HotWordAdapter();
	}

	private class HotWordAdapter extends ImageAdapter {

		public HotWordAdapter() {
		}

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_HOTWORDS:
				view = inflater.inflate(R.layout.listitem_hotword_item, null);
				holder.layoutType = item.layoutType;
				HotwordHolder itemHolder = new HotwordHolder();
				itemHolder.rank = (TextView) view
						.findViewById(R.id.listitem_hw_rank);
				itemHolder.name = (TextView) view
						.findViewById(R.id.listitem_hw_name);
				itemHolder.layout = view.findViewById(R.id.ll_hotword);
				itemHolder.count = (TextView) view
						.findViewById(R.id.listitem_hw_count);
				holder.hotwordHolder = itemHolder;
				itemHolder.layout.setFocusable(false);
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
			case BigItem.Type.LAYOUT_HOTWORDS:
				for (final SearchHotword app : item.hotwords) {
					/*if (position == 0) {
						holder.hotwordHolder.rank
								.setBackgroundResource(R.drawable.icon_first_rank_number);
						holder.hotwordHolder.rank.setText(String
								.valueOf(position + 1));
						holder.hotwordHolder.name.setText(app.key);
						holder.hotwordHolder.rank.setVisibility(View.VISIBLE);
						((LinearLayout.LayoutParams) holder.hotwordHolder.name
								.getLayoutParams()).leftMargin = 0;
					} else if (position == 1) {
						holder.hotwordHolder.rank
								.setBackgroundResource(R.drawable.icon_second_rank_number);
						holder.hotwordHolder.rank.setText(String
								.valueOf(position + 1));
						holder.hotwordHolder.name.setText(app.key);
						holder.hotwordHolder.rank.setVisibility(View.VISIBLE);
						((LinearLayout.LayoutParams) holder.hotwordHolder.name
								.getLayoutParams()).leftMargin = 0;
					} else if (position == 2) {
						holder.hotwordHolder.rank
								.setBackgroundResource(R.drawable.icon_third_rank_number);
						holder.hotwordHolder.rank.setText(String
								.valueOf(position + 1));
						holder.hotwordHolder.name.setText(app.key);
						holder.hotwordHolder.rank.setVisibility(View.VISIBLE);
						((LinearLayout.LayoutParams) holder.hotwordHolder.name
								.getLayoutParams()).leftMargin = 0;
					} else {
						holder.hotwordHolder.rank
						.setBackgroundResource(R.drawable.icon_byt_rank_number);
						holder.hotwordHolder.rank.setVisibility(View.VISIBLE);
						// holder.hotwordHolder.rank
						// .setImageResource(android.R.color.transparent);
						holder.hotwordHolder.rank.setText(String
								.valueOf(position + 1));
						holder.hotwordHolder.name.setText(
								app.key);
						((LinearLayout.LayoutParams) holder.hotwordHolder.name
								.getLayoutParams()).leftMargin = 0;
					}*/
					//holder.hotwordHolder.count.setText(app.count);
					holder.hotwordHolder.name.setText(app.key);
					holder.hotwordHolder.layout
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
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

	}

	@Override
	public void setStyle(GridView listview2) {
/*		View v = LayoutInflater.from(getActivity()).inflate(
				R.layout.listitem_hotword_header, null);
		listview2.addHeaderView(v);
		listview2.setCacheColorHint(0);*/
	}

	@Override
	public void addNewDataOnce() {
		Util.addListData(maContext, LogModel.L_SEARCH_HIT,
				LogModel.SEARCH_CATE_HOT, 1);
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub

	}
}
