package com.byt.market.ui;

import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.R;
import com.byt.market.activity.GameSearchFrame.OnKeySelectedListener;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.data.BigItem;
import com.byt.market.data.SearchHistory;
import com.byt.market.log.LogModel;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.CustomListView;

/**
 * 搜索历史
 * 
 * @author Administrator
 * 
 */
public class HistoryFragment extends ListViewFragment {

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
		return Constants.LOCAL_URL_SK;
	}

	public void setOnKeySelectedListener(OnKeySelectedListener listener) {
		this.mOnKeySelected = listener;
	}

	@Override
	protected String getRequestContent() {
		return null;
	}

	@SuppressLint("NewApi")
	protected List<BigItem> parseHisListData(String result) {
		try {
			if (result != null && !result.isEmpty()) {
				return JsonParse.parseHistoryList(result);
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
		return new HistoryAdapter();
	}

	private class HistoryAdapter extends ImageAdapter {

		public HistoryAdapter() {
		}

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_HISTORY:
				view = inflater.inflate(R.layout.listitem_history_item, null);
				holder.layoutType = item.layoutType;
				HistoryHolder itemHolder = new HistoryHolder();
				itemHolder.name = (TextView) view
						.findViewById(R.id.suggestion_item_title);
				itemHolder.ll_clear_all = view.findViewById(R.id.ll_clear_all);
				itemHolder.ll_history = view.findViewById(R.id.ll_history);
				itemHolder.del = (ImageView) view
						.findViewById(R.id.suggestion_item_del);
				itemHolder.layout = view.findViewById(R.id.suggestion_item);
				itemHolder.layout.setFocusable(false);
				itemHolder.del.setFocusable(false);
				itemHolder.ll_clear_all.setFocusable(false);
				holder.historyHolder = itemHolder;
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
			case BigItem.Type.LAYOUT_HISTORY:
				for (final SearchHistory app : item.historys) {
					holder.historyHolder.name.setText(app.key);
					holder.historyHolder.del
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									Util.delData(MarketContext.getInstance(),
											app.key);
									mOnKeySelected.onKeyClick("clear_one");
								}
							});
					if (app.id == 0) {
						holder.historyHolder.ll_clear_all
								.setVisibility(View.VISIBLE);
						holder.historyHolder.ll_history
								.setVisibility(View.GONE);
						holder.historyHolder.ll_clear_all
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View arg0) {
										if (app.id == 0)
											mOnKeySelected
													.onKeyClick("clear_all");
									}
								});
					} else {
						holder.historyHolder.ll_clear_all
								.setVisibility(View.GONE);
						holder.historyHolder.ll_history
								.setVisibility(View.VISIBLE);
					}
					holder.historyHolder.layout
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									if (app.id != 0)
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
				LogModel.SEARCH_CATE_HISTORY, 1);
	}

	@Override
	public void onPostExecute(final byte[] bytes) {
		isRequesting = false;
		try {
			if (bytes != null) {
				String rs = new String(bytes);
				List<BigItem> appendList = parseHisListData(rs);
				if (appendList != null && !appendList.isEmpty()) {
					if (mAdapter.isEmpty()) {
						loading.setVisibility(View.GONE);
						loadfailed.setVisibility(View.GONE);
						mAdapter.add(appendList);
					} else {
						mAdapter.remove(mAdapter.getCount() - 1);
						mAdapter.add(appendList);
					}
				} else {
					showNoResultView();
				}
			} else {
				setLoadfailedView();
			}
		} catch (Exception e) {
			e.printStackTrace();
			setLoadfailedView();
		}
		mAdapter.loadIcon(listview);
		System.gc();
	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		// TODO Auto-generated method stub
		return null;
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
