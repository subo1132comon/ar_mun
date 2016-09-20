package com.byt.market.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.ShareActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.adapter.MyListAdapter;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.BigItem;
import com.byt.market.data.UserData;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.util.BitmapUtil;
import com.byt.market.util.DataUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LogUtil;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;

/**
 * 月排行
 */
public class RecAppFragment extends ListViewFragment {

	/* add by zengxiao */
	LayoutInflater minflater;
	PopupWindow moptionmenu;
	protected int[] optiontextlist = { R.string.expend_child1_text,
			R.string.state_idle_text, R.string.dialog_nowifi_btn_fav };

	/* add end */
	@Override
	protected String tag() {
		return this.getClass().getSimpleName();
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    	request();
	}
	@Override
	protected String getRequestUrl() {
		return Constants.LIST_URL + "?qt=" + Constants.APP_REC + "&pi="
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
		return new RankAdapter();
	}

	@Override
	public void onResume() {
		LogUtil.i("appupdate", "touch lf week onResume if");
		DownloadTaskManager.getInstance().fillBigItemStates(
				this.getAdapter().mListItems,
				new int[] { DownloadTaskManager.FILL_TYPE_RANKITEMS });
		super.onResume();
	}

	private class RankAdapter extends ImageAdapter {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

		public RankAdapter() {
			df.setMaximumFractionDigits(1);
		}

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			/* add by zengxiao */
			minflater = inflater;
			/* add end */
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_RANKLIST:
				view = inflater.inflate(R.layout.listitem_rank_layout, null);
				holder.layoutType = item.layoutType;
				RankItemHolder commonItemHolder = new RankItemHolder();
				commonItemHolder.sdesc = (TextView) view
						.findViewById(R.id.tv_desc_view);
				commonItemHolder.tv_rank_number = (TextView) view
						.findViewById(R.id.tv_rank_number);
				commonItemHolder.tv_rank_other_number = (TextView) view
						.findViewById(R.id.tv_rank_other_number);
				commonItemHolder.appIcon = (ImageView) view
						.findViewById(R.id.img_icon_View);	
				/*ImageView temp = (ImageView) view
						.findViewById(R.id.default_icon);
				commonItemHolder.appIcon.setTag(temp);*/
				commonItemHolder.name = (TextView) view
						.findViewById(R.id.tv_name_lable);
				commonItemHolder.rating = (RatingBar) view
						.findViewById(R.id.rb_rating_view);
				commonItemHolder.downum = (TextView) view
						.findViewById(R.id.tv_downum_view);
				commonItemHolder.size = (TextView) view
						.findViewById(R.id.tv_size_view);
				commonItemHolder.downBtn = (TextView) view
						.findViewById(R.id.bt_down_btn);
				commonItemHolder.itemLayout = (RelativeLayout) view
						.findViewById(R.id.more_item_layout);
				/*modify bu znegxiao for:修改item样式*/
				commonItemHolder.bt_free_btn=(TextView) view
						.findViewById(R.id.bt_free_btn);
				
				commonItemHolder.googleicon=(ImageView) view.findViewById(R.id.googleicon);

				commonItemHolder.googlefreebg=(TextView) view
						.findViewById(R.id.googlefreebg);
				commonItemHolder.googlepriceline=(ImageView) view
						.findViewById(R.id.googlepriceline);
				commonItemHolder.item_up_bg=(TextView) view
						.findViewById(R.id.item_up_bg);
				commonItemHolder.DownloadProgressBar= (ProgressBar) view
						.findViewById(R.id.DownloadProgressBar);
				commonItemHolder.progressnumtext=(TextView) view
						.findViewById(R.id.progressnumtext);
				commonItemHolder.downBtn2=(TextView) view.findViewById(R.id.bt_down_btn2);
				//commonItemHolder.share_icon=(ImageView) view.findViewById(R.id.share_icon);
				commonItemHolder.sizedivider=(TextView) view.findViewById(R.id.sizedivider);
				/*modify end*/
				holder.rankHolder = commonItemHolder;
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
				final RankItemHolder itemHolder = holder.rankHolder;
				for (final AppItem appItem : item.rankItems) {
					itemHolder.name.setText(appItem.name);
					itemHolder.rating.setRating(appItem.rating);
					/*int rank = position + 1;
					if (rank == 1) {
						itemHolder.tv_rank_number.setVisibility(View.VISIBLE);
						itemHolder.tv_rank_other_number
								.setVisibility(View.GONE);
						itemHolder.tv_rank_number.setText(String.valueOf(rank));
						itemHolder.tv_rank_number
								.setBackgroundResource(R.drawable.icon_first_rank_number);
					} else if (rank == 2) {
						itemHolder.tv_rank_number.setVisibility(View.VISIBLE);
						itemHolder.tv_rank_other_number
								.setVisibility(View.GONE);
						itemHolder.tv_rank_number.setText(String.valueOf(rank));
						itemHolder.tv_rank_number
								.setBackgroundResource(R.drawable.icon_second_rank_number);
					} else if (rank == 3) {
						itemHolder.tv_rank_number.setVisibility(View.VISIBLE);
						itemHolder.tv_rank_other_number
								.setVisibility(View.GONE);
						itemHolder.tv_rank_number.setText(String.valueOf(rank));
						itemHolder.tv_rank_number
								.setBackgroundResource(R.drawable.icon_third_rank_number);
					} else if (rank > 3) {
						itemHolder.tv_rank_number.setVisibility(View.GONE);
						itemHolder.tv_rank_other_number
								.setVisibility(View.VISIBLE);
						itemHolder.tv_rank_other_number.setText(String
								.valueOf(rank));
					} else {
						itemHolder.tv_rank_number.setVisibility(View.GONE);
						itemHolder.tv_rank_other_number
								.setVisibility(View.GONE);
					}*/
					itemHolder.tv_rank_number.setVisibility(View.GONE);
					itemHolder.tv_rank_other_number
							.setVisibility(View.GONE);
					itemHolder.size.setText(appItem.strLength);
					itemHolder.sdesc.setText(appItem.adesc);
					/*if (appItem.rankcount == 0) {
						itemHolder.img_rank.setVisibility(View.GONE);
					} else if (appItem.rankcount > 0) {
						itemHolder.img_rank.setVisibility(View.VISIBLE);
						itemHolder.img_rank
								.setBackgroundDrawable(getResources()
										.getDrawable(R.drawable.rank_up));
					} else if (appItem.rankcount < 0) {
						itemHolder.img_rank.setVisibility(View.VISIBLE);
						itemHolder.img_rank
								.setBackgroundDrawable(getResources()
										.getDrawable(R.drawable.rank_down));
					}*/
					/*add by zengxiao for:修改item样式*/
					if(!Constants.ISGOOGLE)
					{
						if(appItem.googlePrice>0&&appItem.googlemarket>0)
						{
							itemHolder.googlepriceline.setVisibility(View.VISIBLE);
							itemHolder.googlefreebg.setVisibility(View.VISIBLE);
							itemHolder.bt_free_btn.setVisibility(View.VISIBLE);
							itemHolder.googleicon.setVisibility(View.VISIBLE);
							itemHolder.bt_free_btn.setText(MyApplication.getInstance().getResources().getString(R.string.char_alert)+
									appItem.googlePrice);																		
						}
						else{
							itemHolder.googlepriceline.setVisibility(View.GONE);
							itemHolder.googlefreebg.setVisibility(View.GONE);
							itemHolder.bt_free_btn.setVisibility(View.GONE);
							itemHolder.googleicon.setVisibility(View.GONE);

						}	
					}
					
					/*add end*/
					/*if (appItem.stype != null) {
						if (appItem.stype.contains("2"))
							itemHolder.appType.setImageBitmap(BitmapUtil
									.createTextImage(
											MyApplication.getInstance(),
											R.drawable.l_home_type_new,
											R.string.first_product));
						else if (appItem.stype.contains("1"))
							itemHolder.appType.setImageBitmap(BitmapUtil
									.createTextImage(
											MyApplication.getInstance(),
											R.drawable.l_home_type_new,
											R.string.new_product));
						else if (appItem.stype.contains("3"))
							itemHolder.appType.setImageBitmap(BitmapUtil
									.createTextImage(
											MyApplication.getInstance(),
											R.drawable.l_home_type_new,
											R.string.act_product));
						else if (appItem.stype.contains("4"))
							itemHolder.appType.setImageBitmap(BitmapUtil
									.createTextImage(
											MyApplication.getInstance(),
											R.drawable.l_home_type_new,
											R.string.hot_product));
						else if (appItem.stype.contains("5"))
							itemHolder.appType.setImageBitmap(BitmapUtil
									.createTextImage(
											MyApplication.getInstance(),
											R.drawable.l_home_type_new,
											R.string.rec_product));
						else
							itemHolder.appType
									.setBackgroundColor(getResources()
											.getColor(
													android.R.color.transparent));
					} else {
						itemHolder.appType.setBackgroundColor(getResources()
								.getColor(android.R.color.transparent));
					}*/

					itemHolder.downum.setText(appItem.downNum);
					itemHolder.sizedivider.setText(" | ");
					if (TextUtils.isEmpty(appItem.iconUrl)) {
					//	itemHolder.appIcon
					//			.setImageResource(R.drawable.app_empty_icon);
					}  else if(!imageLoader.getPause().get()){
						imageLoader.displayImage(appItem.iconUrl,
								itemHolder.appIcon, options);
					}

					itemHolder.itemLayout
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									onAppClick(appItem, Constants.TODETAIL);
								}
							});

					/* add by zengxiao for change UI */
					Log.d("rmyzx","ulevel="+appItem.ulevel+"--name="+appItem.name);
					if(appItem.isshare==1){
						itemHolder.downBtn.setVisibility(View.GONE);
						//itemHolder.share_icon.setVisibility(View.VISIBLE);
						itemHolder.downBtn2.setVisibility(View.VISIBLE);
						itemHolder.downBtn2.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								
								if(itemHolder.downBtn2.getText().toString().equals(RecAppFragment.this.getActivity().getString(R.string.sharedown)))
								{
									Intent intent =new Intent();
									intent.setClass(RecAppFragment.this.getActivity(),ShareActivity.class);						
									intent.putExtra("sendstring", appItem); 
									RecAppFragment.this.getActivity().startActivity(intent);
								}
								else
								{
								DownloadTaskManager.getInstance().onDownloadBtnClick(appItem);
								}
								
							}
						});
						if(!imageLoader.getPause().get()){
							Drawable drawable=RecAppFragment.this.getActivity().getResources().getDrawable(R.drawable.progress_listitem);
							itemHolder.DownloadProgressBar.setProgressDrawable(drawable);
							itemHolder.item_up_bg.setBackgroundResource(R.drawable.bg_press_draw);
							DownloadTaskManager.getInstance().updateItemBtnByStateshare(
									getActivity(), itemHolder.downBtn2, appItem,itemHolder.DownloadProgressBar,itemHolder.progressnumtext,null,itemHolder.sharelayout
									);
						}
						
					}
					else
					{
						itemHolder.downBtn.setVisibility(View.VISIBLE);
						//itemHolder.share_icon.setVisibility(View.GONE);
						itemHolder.downBtn2.setVisibility(View.GONE);
					
					itemHolder.downBtn
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {	
								DownloadTaskManager.getInstance().onDownloadBtnClick(appItem);
							}
						});
				/* add by "zengxiao" */
					if(!imageLoader.getPause().get()){
						Drawable drawable=RecAppFragment.this.getActivity().getResources().getDrawable(R.drawable.progress_listitem);
						itemHolder.DownloadProgressBar.setProgressDrawable(drawable);
						itemHolder.item_up_bg.setBackgroundResource(R.drawable.bg_press_draw);
						DownloadTaskManager.getInstance().updateItemBtnByState(
							getActivity(), itemHolder.downBtn, appItem,itemHolder.DownloadProgressBar,itemHolder.progressnumtext);
					}
					}
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
		DownloadTaskManager.getInstance().fillBigItemStates(
				this.getAdapter().mListItems,
				new int[] { DownloadTaskManager.FILL_TYPE_RANKITEMS });
		this.getAdapter().notifyDataSetChanged();
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

	}
	@Override
	public void setStyle(CusPullListView listview2) {
		// TODO Auto-generated method stub
		super.setStyle(listview2);
	}
	/*add for 修改提示语*/
	@Override
	public void setButtonInvi() {
		// TODO Auto-generated method stub
		loadfailed.setText(MyApplication.getInstance().getString(R.string.nodata));
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
