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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import com.byt.market.R;
import com.byt.market.activity.GameSearchFrame;
import com.byt.market.activity.ShareActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.adapter.MyListAdapter;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.BigItem;
import com.byt.market.data.UserData;
import com.byt.market.download.DownloadBtnClickListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.util.BitmapUtil;
import com.byt.market.util.DataUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;

public class ResultFragment extends ListViewFragment {

	private String key;
	private String currentView;
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
	public void onResume() {
		// TODO Auto-generated method stub
		if(moptionmenu!=null&&moptionmenu.isShowing())
		{
			moptionmenu.dismiss();
		}
		super.onResume();
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int from = getArguments().getInt(Constants.LIST_FROM);
		switch (from) {
		case GameSearchFrame.THINK_VIEW:
			currentView = LogModel.SEARCH_CATE_THINK;
			break;
		case GameSearchFrame.HISTORY_VIEW:
			currentView = LogModel.SEARCH_CATE_HISTORY;
			break;
		case GameSearchFrame.HOTWORD_VIEW:
			currentView = LogModel.SEARCH_CATE_HOT;
			break;
		case GameSearchFrame.LANGUAGE_VIEW:
			currentView = LogModel.SEARCH_CATE_LAUNGE;
			break;
		case GameSearchFrame.OTHER_VIEW:
			currentView = LogModel.SEARCH_CATE_OTHER;
			break;
		}
	}

	@Override
	protected String getRequestUrl() {
		return Constants.LIST_URL + "?qt=search&word="
				+ Util.encodeContentForUrl(key) + "&pi="
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
			
			final List<BigItem> list = JsonParse.parseSearchList(getActivity(),
					result.getJSONArray("data"), getPageInfo().getPageIndex(),
					getPageInfo().getRecordNum());
			DownloadTaskManager.getInstance().fillBigItemStates(list,
					new int[] { DownloadTaskManager.FILL_TYPE_RESULTS });
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
		return new ResutlAdapter();
	}

	private class ResutlAdapter extends ImageAdapter {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

		public ResutlAdapter() {
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
			case BigItem.Type.LAYOUT_SEARCH_TITLE:
				ResultTitleHolder resultTitleHolder = new ResultTitleHolder();
				view = inflater.inflate(R.layout.listitem_result_header, null);
				holder.layoutType = item.layoutType;
				resultTitleHolder.name = (TextView) view
						.findViewById(R.id.list_result_header_title);
				holder.reTitleHolder = resultTitleHolder;
				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_SEARCH_SAFE:
				ResultSafeHolder resultSafeHolder = new ResultSafeHolder();
				view = inflater.inflate(R.layout.listitem_resultrec_item, null);
				holder.layoutType = item.layoutType;
				resultSafeHolder.icon = (ImageView) view
						.findViewById(R.id.listitem_rec_icon);
				/*ImageView temp = (ImageView) view
						.findViewById(R.id.default_icon);
				resultSafeHolder.icon.setTag(temp);*/
				resultSafeHolder.name = (TextView) view
						.findViewById(R.id.listitem_rec_name);
				resultSafeHolder.subLine = (TextView) view
						.findViewById(R.id.listitem_rec_category);
				resultSafeHolder.listitem_rec_atip = (TextView) view
						.findViewById(R.id.listitem_rec_atip);
				resultSafeHolder.rec_rating = (RatingBar) view
						.findViewById(R.id.appRatingView);
				resultSafeHolder.rec_comm = (TextView) view
						.findViewById(R.id.appComm_count);
				resultSafeHolder.btn = (Button) view
						.findViewById(R.id.listitem_rec_btn);
				resultSafeHolder.progress = (ProgressBar) view
						.findViewById(R.id.downloadProgressBar);
				resultSafeHolder.layout = view.findViewById(R.id.ll_rec_today);
				resultSafeHolder.icon_type = (ImageView) view
						.findViewById(R.id.listitem_type);
				holder.reSafeHolder = resultSafeHolder;
				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_SEARCH_RELA:
			case BigItem.Type.LAYOUT_SEARCH_NO_RESULT:
				view = inflater.inflate(R.layout.listitem_common_layout_all, null);
				holder.layoutType = item.layoutType;
				CommonItemHolder commonItemHolder = new CommonItemHolder();
				commonItemHolder.appIcon = (ImageView) view
						.findViewById(R.id.img_icon_View);
				 /*temp = (ImageView) view
						.findViewById(R.id.default_icon);
				commonItemHolder.appIcon.setTag(temp);*/
				/*commonItemHolder.appType = (ImageView) view
						.findViewById(R.id.img_icon_type);*/
				commonItemHolder.name = (TextView) view
						.findViewById(R.id.tv_name_lable);
				commonItemHolder.rating = (RatingBar) view
						.findViewById(R.id.rb_rating_view);
				commonItemHolder.downum = (TextView) view
						.findViewById(R.id.tv_downum_view);
				commonItemHolder.size = (TextView) view
						.findViewById(R.id.tv_size_view);
				commonItemHolder.sdesc = (TextView) view
						.findViewById(R.id.tv_desc_view);
				commonItemHolder.downBtn = (TextView) view
						.findViewById(R.id.bt_down_btn);
				commonItemHolder.descLayout = (LinearLayout) view
						.findViewById(R.id.txt_desc_layout);
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
				commonItemHolder.DownloadProgressBar= (ProgressBar) view
						.findViewById(R.id.DownloadProgressBar);
				commonItemHolder.item_up_bg=(TextView) view
						.findViewById(R.id.item_up_bg);
				commonItemHolder.progressnumtext=(TextView) view
						.findViewById(R.id.progressnumtext);
				commonItemHolder.downBtn2=(TextView) view.findViewById(R.id.bt_down_btn2);
			//	commonItemHolder.share_icon=(ImageView) view.findViewById(R.id.share_icon);
				commonItemHolder.sizedivider=(TextView) view.findViewById(R.id.sizedivider);
				/*modify end*/
				holder.commonItemHolder = commonItemHolder;
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
			case BigItem.Type.LAYOUT_SEARCH_TITLE:
				ResultTitleHolder resultTitleHolder = holder.reTitleHolder;
				for (final AppItem app : item.resultItems) {
					if (app.name != null)
						resultTitleHolder.name.setText(app.name);
				}
				break;
			case BigItem.Type.LAYOUT_SEARCH_SAFE:
				ResultSafeHolder resultSafeHolder = holder.reSafeHolder;
				for (final AppItem app : item.resultItems) {
					try{
					if(app.name.equals(String.valueOf(resultSafeHolder.name.getText()))){
						if((app.state!=AppItemState.STATE_IDLE&&mScrollState==OnScrollListener.SCROLL_STATE_IDLE)
								||(app.state==AppItemState.STATE_IDLE&&app.cacheState!=AppItemState.STATE_IDLE)){
							DownloadTaskManager.getInstance().updateByState(
								getActivity(), resultSafeHolder.btn, app,
								resultSafeHolder.progress,
								resultSafeHolder.subLine, true, false);
							if(app.state==AppItemState.STATE_IDLE&&app.cacheState!=AppItemState.STATE_IDLE){
								app.cacheState=AppItemState.STATE_IDLE;
							}}
						break;
					}
						imageLoader.cancelDisplayTask(resultSafeHolder.icon);
						resultSafeHolder.icon.setImageBitmap(null);
					} catch (Exception e) {
						e.printStackTrace();
					}
					resultSafeHolder.name.setText(app.name);
					StringBuilder sb = new StringBuilder();

					sb.append(app.cateName).append(" | ");
					//sb.append(app.strLength).append(" | ");
					sb.append(app.downNum+" | "+app.strLength);
					resultSafeHolder.subLine.setText(sb.toString());
					String atip = getString(R.string.editor_word) + app.adesc;
					if (atip != null) {
						SpannableStringBuilder style = new SpannableStringBuilder(
								atip);
						style.setSpan(new ForegroundColorSpan(getResources()
								.getColor(R.color.home_rec_atip_color)), 0, 5,
								Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
						resultSafeHolder.listitem_rec_atip.setText(style);
					} else
						resultSafeHolder.listitem_rec_atip.setText(null);
					// resultSafeHolder.rating.setText(df.format(app.rating) +
					// "分");
					resultSafeHolder.rec_rating.setRating(app.rating);
					if (app.commcount != 0) {
						resultSafeHolder.rec_comm.setVisibility(View.VISIBLE);
						resultSafeHolder.rec_comm.setText("(" + app.commcount
								+ ")");
					} else {
						resultSafeHolder.rec_comm.setVisibility(View.GONE);
					}

					if (TextUtils.isEmpty(app.iconUrl)) {
						//resultSafeHolder.icon
						//		.setImageResource(R.drawable.app_empty_icon);
					}  else  if(!imageLoader.getPause().get()){
						// mImageFetcher.loadImage(app.iconUrl,
						// resultSafeHolder.icon);
						imageLoader.displayImage(app.iconUrl,
								resultSafeHolder.icon, options);
					}

					// if (app.icon == null) {
					// resultSafeHolder.icon
					// .setImageResource(R.drawable.app_empty_icon);
					// } else {
					// resultSafeHolder.icon.setImageBitmap(app.icon);
					// }
					showStype(resultSafeHolder, app);
					resultSafeHolder.layout
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									onAppClick(app, Constants.TODETAIL);
								}
							});
					resultSafeHolder.btn
							.setOnClickListener(new DownloadBtnClickListener(
									app));
					DownloadTaskManager.getInstance().updateByState(
							getActivity(), resultSafeHolder.btn, app,
							resultSafeHolder.progress,
							resultSafeHolder.subLine, true, false);
				}
				break;
			case BigItem.Type.LAYOUT_SEARCH_RELA:
			case BigItem.Type.LAYOUT_SEARCH_NO_RESULT:
				final CommonItemHolder itemHolder = holder.commonItemHolder;
				for (final AppItem appItem : item.resultItems) {
					itemHolder.name.setText(appItem.name);
					itemHolder.rating.setRating(appItem.rating);
					itemHolder.size.setText(appItem.strLength);
					itemHolder.sizedivider.setText(" | ");

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
					itemHolder.downum.setText(appItem.downNum);
					itemHolder.descLayout.setVisibility(View.GONE);
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
					if(appItem.isshare==1){
						itemHolder.downBtn.setVisibility(View.GONE);
						//itemHolder.share_icon.setVisibility(View.VISIBLE);
						itemHolder.downBtn2.setVisibility(View.VISIBLE);
						itemHolder.downBtn2.setOnClickListener(new OnClickListener() {		
							@Override
							public void onClick(View arg0) {
								
									if(itemHolder.downBtn2.getText().toString().equals(ResultFragment.this.getActivity().getString(R.string.sharedown)))
									{
										Intent intent =new Intent();
										intent.setClass(ResultFragment.this.getActivity(),ShareActivity.class);						
										intent.putExtra("sendstring", appItem); 
										ResultFragment.this.getActivity().startActivity(intent);
									}
									else
									{
									DownloadTaskManager.getInstance().onDownloadBtnClick(appItem);
									}
								
							}
						});
						if(!imageLoader.getPause().get()){
							Drawable drawable=ResultFragment.this.getActivity().getResources().getDrawable(R.drawable.progress_listitem);
							itemHolder.DownloadProgressBar.setProgressDrawable(drawable);
							itemHolder.item_up_bg.setBackgroundResource(R.drawable.bg_press_draw);
							DownloadTaskManager.getInstance().updateItemBtnByStateshare(
									getActivity(), itemHolder.downBtn2, appItem,itemHolder.DownloadProgressBar2,itemHolder.progressnumtext2,null,itemHolder.sharelayout);

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
							Drawable drawable=ResultFragment.this.getActivity().getResources().getDrawable(R.drawable.progress_listitem);
							itemHolder.DownloadProgressBar.setProgressDrawable(drawable);
							itemHolder.item_up_bg.setBackgroundResource(R.drawable.bg_listitem_all_drawable);
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
		// TODO Auto-generated method stub
		DownloadTaskManager.getInstance().fillBigItemStates(
				ResultFragment.this.getAdapter().mListItems,
				new int[] { DownloadTaskManager.FILL_TYPE_RESULTS });
			ResultFragment.this.getAdapter().notifyDataSetChanged();
	}

	@Override
	public void onAppClick(Object obj, String action) {
		Intent intent = new Intent(action);
		if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			intent.putExtra("app", app);
		}
		intent.putExtra(Constants.LIST_FROM, LogModel.L_SEARCH_RS);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public void showNoResultView() {
		super.showNoResultView();
		loadfailed.setButtonVisible(View.GONE);
		loadfailed.setText(MyApplication.getInstance().getString(R.string.nofindgame));
	}

	@Override
	public void addNewDataOnce() {
		Util.addListData(maContext, LogModel.L_SEARCH_RS, currentView, 1);
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub

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
