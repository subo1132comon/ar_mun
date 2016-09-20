package com.byt.market.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.data.AppItem;
import com.byt.market.data.BigItem;
import com.byt.market.data.HLeaderItem;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LogUtil;
import com.byt.market.util.TextUtil;
import com.byt.market.util.Util;

public class SplashRecommendFragment extends ListViewFragment {

	Button btnInstallSelected;
	Button btnInstallAll;

	private List<AppItem> checkItems = new ArrayList<AppItem>();
	private List<AppItem> AllItems = new ArrayList<AppItem>();

	@Override
	protected String tag() {
		return this.getClass().getSimpleName();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = super.onCreateView(inflater, container, savedInstanceState);
		btnInstallSelected = (Button) view
				.findViewById(R.id.btn_install_select);
		btnInstallSelected.setSelected(true);
		btnInstallAll = (Button) view.findViewById(R.id.btn_install_all);
		btnInstallAll.setSelected(true);
		btnInstallSelected.setOnClickListener(this);
		btnInstallAll.setOnClickListener(this);
		initInstallSelectBtn(0);
		return view;
	}

	@Override
	protected String getRequestUrl() {
		return Constants.LIST_URL + "?qt=startrecommend" + "&pi=1" + "&ps=4";
	}

	public void initInstallSelectBtn(int count) {
		
		if(count==0){
			btnInstallSelected.setEnabled(false);
			btnInstallSelected.setText(getActivity().getString(R.string.sp_install));
		}else{
		btnInstallSelected.setEnabled(true);
		btnInstallSelected
				.setText(String
						.format(getResources().getString(
								R.string.btn_rec_select_count), count));
		}
	}

	/** 将已安装应用post过去 **/
	@Override
	protected String getRequestContent() {
		List<AppItem> packagesList = DownloadTaskManager.getInstance()
				.getAllInstalledApps();
		StringBuilder builder = new StringBuilder();
		int size = packagesList.size();
		for (int i = 0; i < size; i++) {
			builder.append(packagesList.get(i).pname + "|");
		}
		if (TextUtils.isEmpty(builder.toString())) {
			return null;
		}
		return builder.toString().substring(0, builder.length() - 1);
	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		try {
			return JsonParse.parseStartRecList(result.getJSONArray("data"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.layout_rec_dialog;
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);
		return view;
	}

	@Override
	protected ImageAdapter createAdapter() {
		return new HomeAdapter();
	}

	private class HomeAdapter extends ImageAdapter {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

		public HomeAdapter() {
			df.setMaximumFractionDigits(1);
		}

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_APPLIST_REC_TWO:
				view = inflater.inflate(R.layout.listitem_rec_two, null);
				holder.layoutType = item.layoutType;
				RecommendHolder recommendHolder1 = new RecommendHolder();
				recommendHolder1.icons = (ImageView) view
						.findViewById(R.id.listitem_rec_icon_1);
				recommendHolder1.imgCheck = (ImageView) view
						.findViewById(R.id.listitem_rec_img_check_1);
				recommendHolder1.names = (TextView) view
						.findViewById(R.id.listitem_rec_name_1);
				recommendHolder1.ratingbar = (RatingBar) view
						.findViewById(R.id.listitem_rec_appRatingView_1);
				recommendHolder1.views = view
						.findViewById(R.id.listitem_rec_two_view_1);

				RecommendHolder recommendHolder2 = new RecommendHolder();
				recommendHolder2.icons = (ImageView) view
						.findViewById(R.id.listitem_rec_icon_2);
				recommendHolder2.names = (TextView) view
						.findViewById(R.id.listitem_rec_name_2);
				recommendHolder2.ratingbar = (RatingBar) view
						.findViewById(R.id.listitem_rec_appRatingView_2);
				recommendHolder2.views = view
						.findViewById(R.id.listitem_rec_two_view_2);
				recommendHolder2.imgCheck = (ImageView) view
						.findViewById(R.id.listitem_rec_img_check_2);
				holder.recommendHolders.add(recommendHolder1);
				holder.recommendHolders.add(recommendHolder2);
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
			case BigItem.Type.LAYOUT_APPLIST_REC_TWO:
				ArrayList<RecommendHolder> recommendHolders = holder.recommendHolders;
				for (int i = 0; i < item.splashRecItems.size()
						&& i < recommendHolders.size(); i++) {
					final AppItem app = item.splashRecItems.get(i);

					recommendHolders.get(i).names.setText(TextUtil
							.toDBC(app.name));
					recommendHolders.get(i).ratingbar.setRating(app.rating);

					if (TextUtils.isEmpty(app.iconUrl)) {
						recommendHolders.get(i).icons
								.setImageResource(R.drawable.app_empty_icon);
					} else {
						imageLoader.displayImage(app.iconUrl,
								recommendHolders.get(i).icons, options);
					}
					recommendHolders.get(i).imgCheck
							.setSelected(app.isSelected);

					recommendHolders.get(i).views
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View view) {
									if (app.isSelected) {
										checkItems.remove(app);
									} else {
										checkItems.add(app);
									}
									;
									app.isSelected = !app.isSelected;
									initInstallSelectBtn(checkItems.size());
									notifyDataSetChanged();
								}
							});

				}
				break;
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		LogUtil.d("TAGTAG", "onStart() ---> " + tag());
		if (flag) {
			requestDelay();
			flag = false;
		}
	}

	public boolean flag = true;

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LogUtil.d("TAGTAG", "onDestroyView() ---> " + tag());
	}

	@Override
	protected void onDownloadStateChanged() {
		// TODO Auto-generated method stub
		DownloadTaskManager.getInstance().fillBigItemStates(
				SplashRecommendFragment.this.getAdapter().mListItems,
				new int[] { DownloadTaskManager.FILL_TYPE_HOMEITEMS,
						DownloadTaskManager.FILL_TYPE_RECITEMS });
		SplashRecommendFragment.this.getAdapter().notifyDataSetChanged();
	}

	@Override
	public void onAppClick(Object obj, String action) {
		Intent intent = new Intent(action);
		if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			intent.putExtra("app", app);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_HOME);
		} else if (obj instanceof HLeaderItem) {
			HLeaderItem app = (HLeaderItem) obj;
			intent.putExtra("app", app);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_HOME_LEADER);
		}
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	@Override
	public void onAppClick(Object obj, int what, String action) {
	}

	@Override
	public void addNewDataOnce() {
		Util.addListData(maContext, LogModel.L_HOME, LogModel.P_LIST, 1);
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		for (BigItem bigItem : appendList) {
			if (bigItem.layoutType == BigItem.Type.LAYOUT_APPLIST_REC_TWO) {
				AllItems.addAll(bigItem.splashRecItems);
				for (AppItem appItem : AllItems) {
					appItem.isSelected = true;
				}
			}
		}
		if (AllItems.size() > 0) {
			checkItems.addAll(AllItems);
			initInstallSelectBtn(checkItems.size());
		}
	}
	
	@Override
	protected void setLoadfailedView() {
		// TODO Auto-generated method stub
		super.setLoadfailedView();
		loadfailed.setButtonVisible(View.GONE);
	}
	
	@Override
	public void showNoResultView() {
		// TODO Auto-generated method stub
		super.showNoResultView();
		loadfailed.setButtonVisible(View.GONE);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		int id = v.getId();
		switch (id) {
		case R.id.btn_install_select:
			if (checkItems.size() > 0) {
				for (AppItem item : checkItems) {
					DownloadTaskManager.getInstance().onDownloadBtnClick(item);
				}
				getActivity().finish();
			}
			break;
		case R.id.btn_install_all:
			if (AllItems.size() > 0) {
				for (AppItem item : AllItems) {
					DownloadTaskManager.getInstance().onDownloadBtnClick(item);
				}
				getActivity().finish();
			}
			break;
		}
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
