package com.byt.market.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.display.SimpleBitmapDisplayer;
import com.byt.market.data.AppItem;
import com.byt.market.data.BigItem;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;

/**
 * 排行
 */
public class RankFragment2 extends ListViewFragment {
	private DisplayImageOptions mOptions;

	@Override
	protected String tag() {
		return this.getClass().getSimpleName();
	}

	@Override
	protected String getRequestUrl() {
		return Constants.LIST_URL + "?qt=" + Constants.RANK + "&pi="
				+ getPageInfo().getNextPageIndex() + "&ps="
				+ getPageInfo().getPageSize();
	}

	@Override
	protected String getRequestContent() {
		return null;
	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		try {
			final List<BigItem> list = JsonParse.parseRankList(result
					.getJSONArray("data"));
			DownloadTaskManager.getInstance().fillBigItemStates(list,
					new int[] { DownloadTaskManager.FILL_TYPE_RANKITEMS });
			return list;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mOptions = new DisplayImageOptions.Builder()
				.cacheOnDisc().delayBeforeLoading(200)
				.displayer(new SimpleBitmapDisplayer()).build();
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.listview;
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);
		if (this.getActivity() != null
				&& this.getActivity() instanceof MainActivity) {
			final MainActivity activity = (MainActivity) this.getActivity();
			if (activity.mAdapter != null && activity.mAdapter.list != null
					&& activity.mAdapter.list.size() > MainActivity.MAIN_RANK) {
				activity.mAdapter.list.set(MainActivity.MAIN_RANK, this);
			}
		}

		return view;
	}

	@Override
	protected ImageAdapter createAdapter() {
		return new RankAdapter();
	}

	private class RankAdapter extends ImageAdapter {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

		public RankAdapter() {
			df.setMaximumFractionDigits(1);
		}

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_RANKLIST:
				view = inflater.inflate(R.layout.listitem_rank, null);
				//holder.layoutType = item.layoutType;
				RankItemHolder itemHolder = new RankItemHolder();
				// itemHolder.icon = (ImageView) view
				// .findViewById(R.id.listitem_rank_icon);
				// itemHolder.rankIcon = (ImageView) view
				// .findViewById(R.id.listitem_rank_rankicon);
				// itemHolder.rankcount = (TextView) view
				// .findViewById(R.id.listitem_rank_rankcount);
				// itemHolder.name = (TextView) view
				// .findViewById(R.id.listitem_rank_name);
				// itemHolder.subLine = (TextView) view
				// .findViewById(R.id.listitem_rank_category);
				// itemHolder.rank = (TextView) view
				// .findViewById(R.id.listitem_rank_ranknum);
				// itemHolder.ratingIcon = view
				// .findViewById(R.id.listitem_rank_rating_icon);
				// itemHolder.rating = (TextView) view
				// .findViewById(R.id.listitem_rank_rating);
				// itemHolder.btn = (Button) view
				// .findViewById(R.id.listitem_rank_btn);
				itemHolder.layout = view.findViewById(R.id.ll_rank);
				itemHolder.progress = (ProgressBar) view
						.findViewById(R.id.downloadProgressBar);
				itemHolder.icon_type = (ImageView) view
						.findViewById(R.id.listitem_type);
				holder.rankHolder = itemHolder;
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
			case BigItem.Type.LAYOUT_RANKLIST:
				RankItemHolder rankHolder = holder.rankHolder;
				// for (final AppItem app : item.rankItems) {
				// rankHolder.name.setText(app.name);
				// StringBuilder sb = new StringBuilder();
				//
				// sb.append(app.cateName).append(" | ");
				// sb.append(app.strLength).append(" | ");
				// sb.append(app.downNum);
				// rankHolder.subLine.setText(sb.toString());
				// rankHolder.rank.setText(String.valueOf(position + 1));
				// if (app.rating != 0) {
				// rankHolder.ratingIcon.setVisibility(View.VISIBLE);
				// rankHolder.rating.setVisibility(View.VISIBLE);
				// rankHolder.rating.setText(df.format(app.rating) + "分");
				// } else {
				// rankHolder.ratingIcon.setVisibility(View.GONE);
				// rankHolder.rating.setVisibility(View.GONE);
				// }
				// // edit by wangxin,load image by Bitmapfun
				// if (TextUtils.isEmpty(app.iconUrl)) {
				// rankHolder.icon
				// .setImageResource(R.drawable.app_empty_icon);
				// } else {
				// // mImageFetcher.loadImage(app.iconUrl,
				// // rankHolder.icon);
				// imageLoader.displayImage(app.iconUrl, rankHolder.icon,
				// mOptions);
				// }
				//
				// // if (app.icon == null) {
				// // rankHolder.icon
				// // .setImageResource(R.drawable.app_empty_icon);
				// // } else {
				// // rankHolder.icon.setImageBitmap(app.icon);
				// // }
				//
				// if (app.rankcount == 0) {
				// rankHolder.rankIcon.setVisibility(View.GONE);
				// rankHolder.rankcount.setVisibility(View.GONE);
				// } else if (app.rankcount > 0) {
				// rankHolder.rankIcon.setVisibility(View.VISIBLE);
				// rankHolder.rankcount.setVisibility(View.VISIBLE);
				// rankHolder.rankIcon
				// .setBackgroundDrawable(getResources()
				// .getDrawable(R.drawable.rank_up));
				// rankHolder.rankcount.setText(Math.abs(app.rankcount)
				// + "");
				// } else if (app.rankcount < 0) {
				// rankHolder.rankIcon.setVisibility(View.VISIBLE);
				// rankHolder.rankcount.setVisibility(View.VISIBLE);
				// rankHolder.rankcount.setText(Math.abs(app.rankcount)
				// + "");
				// rankHolder.rankIcon
				// .setBackgroundDrawable(getResources()
				// .getDrawable(R.drawable.rank_down));
				// }
				// showStype(rankHolder, app);
				// rankHolder.layout.setOnClickListener(new OnClickListener() {
				//
				// @Override
				// public void onClick(View arg0) {
				// onAppClick(app, Constants.TODETAIL);
				// }
				// });
				// rankHolder.btn
				// .setOnClickListener(new DownloadBtnClickListener(
				// app));
				// DownloadTaskManager.getInstance().updateByState(
				// getActivity(), rankHolder.btn, app,
				// rankHolder.progress, rankHolder.subLine, true,
				// false);
				// }
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
		// TODO Auto-generated method stub
		DownloadTaskManager.getInstance().fillBigItemStates(
				RankFragment2.this.getAdapter().mListItems,
				new int[] { DownloadTaskManager.FILL_TYPE_RANKITEMS });
		/*RankFragment2.this.getAdapter().notifyDataSetChanged();*/
	}

	@Override
	public void onAppClick(Object obj, String action) {
		Intent intent = new Intent(action);
		if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			intent.putExtra("app", app);
		}
		intent.putExtra(Constants.LIST_FROM, LogModel.L_RANK);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	@Override
	public void addNewDataOnce() {
		Util.addListData(maContext, LogModel.L_RANK, LogModel.P_LIST, 1);
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
