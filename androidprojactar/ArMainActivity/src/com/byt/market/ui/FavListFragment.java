package com.byt.market.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
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
import com.byt.market.download.DownloadBtnClickListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.util.BitmapUtil;
import com.byt.market.util.DataUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
//import com.tencent.stat.common.p;

/**
 * 游戏分类详情列表
 */
public class FavListFragment extends ListViewFragment {

	private int cateId;
	private String what;
	private String from;
	private String list_cate;
	private boolean isHot;
	/* add by zengxiao */
	LayoutInflater minflater;
	private Button getanather;
	PopupWindow moptionmenu;
	protected int[] optiontextlist = { R.string.expend_child1_text,
			R.string.state_idle_text, R.string.dialog_nowifi_btn_fav };

	/* add end */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cateId = 1;
		list_cate = "";
		what = "qwe";
			isHot = true;
			from = LogModel.L_CATE_HOT;
		
	}

	@Override
	protected String tag() {
		return this.getClass().getSimpleName();
	}
	@Override
	public void initViewBYT(View view) {
		// TODO Auto-generated method stub
		getPageInfo().setPageSize(5);
		getanather=(Button) view.findViewById(R.id.getdataanother);
		getanather.setOnClickListener(this);
	}

	@Override
	protected String getRequestUrl() {
		/*String url = Constants.LIST_URL + "?qt=categoryhot&ctype=101" +
	"&pi=" + getPageInfo().getNextPageIndex() + "&ps="
				+ getPageInfo().getPageSize();*/
/*		http://210.21.246.61:8023/v1.php?qt=recomm&ps=9&pi=1
*/		String url = Constants.LIST_URL + "?qt=" + "recomm"
				 + "&ps="+ getPageInfo().getPageSize()+ "&pi=" + getPageInfo().getNextPageIndex();
		Log.d("muzx", "url="+url);
		return url;
	}

	@Override
	protected String getRequestContent() {
		List<AppItem> items = DownloadTaskManager.getInstance().getAllDownloadItem(false);
		StringBuilder sb = new StringBuilder();
		for (AppItem item : items) {
			if(item.state == AppItem.STATE_INSTALLED_NEW){
				sb.append(item.pname).append(":")
				.append(item.vcode).append("|");
			}
		}
        Log.i("appupdate","GameUpdateManager getPostData sb = "+sb.toString());
		return sb.toString();
	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		try {
			List<BigItem> list = JsonParse.parseSubList(
					result.getJSONArray("data"), from, String.valueOf(cateId));
			DownloadTaskManager.getInstance().fillBigItemStates(list,
					new int[] { DownloadTaskManager.FILL_TYPE_SUBLIST_ITEMS });

			return list;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onResume() {
		if(moptionmenu!=null&&moptionmenu.isShowing())
		{
			moptionmenu.dismiss();
		}
		/*DownloadTaskManager.getInstance().fillBigItemStates(
				this.getAdapter().mListItems,
				new int[] { DownloadTaskManager.FILL_TYPE_SUBLIST_ITEMS });*/
		super.onResume();
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.favlistview;
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);
		view.findViewById(R.id.have_header).setVisibility(View.INVISIBLE);
		return view;
	}

	@Override
	public void showNoResultView() {
		super.showNoResultView();
		loadfailed.setButtonVisible(View.VISIBLE);
	}

	@Override
	protected ImageAdapter createAdapter() {
		return new CateListAdapter();
	}

	private class CateListAdapter extends ImageAdapter {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

		public CateListAdapter() {
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
			case BigItem.Type.LAYOUT_SUBLIST:
				view = inflater.inflate(R.layout.listitem_fav_layout, null);
				holder.layoutType = item.layoutType;
				FavItemHolder commonItemHolder = new FavItemHolder();
				commonItemHolder.bommline1=(View)view
						.findViewById(R.id.bommline1);
				commonItemHolder.appIcon = (ImageView) view
						.findViewById(R.id.img_icon_View);
				/*ImageView temp = (ImageView) view
						.findViewById(R.id.default_icon);
				commonItemHolder.appIcon.setTag(temp);*/
				commonItemHolder.appType = (ImageView) view
						.findViewById(R.id.img_icon_type);
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
				commonItemHolder.descLayout = (LinearLayout) view
						.findViewById(R.id.txt_desc_layout);
				commonItemHolder.itemLayout = (LinearLayout) view
						.findViewById(R.id.more_item_layout);
				/*modify bu znegxiao for:修改item样式*/
				commonItemHolder.appprice=(TextView) view
						.findViewById(R.id.appprice);
						commonItemHolder.pricelayout=(FrameLayout) view
						.findViewById(R.id.ishaveprice);
					
				commonItemHolder.bt_free_btn=(TextView) view
						.findViewById(R.id.bt_free_btn);
				commonItemHolder.googlepriceFram=(FrameLayout) view
						.findViewById(R.id.googlepriceFramlayout);
						
				commonItemHolder.appvip=(TextView) view
						.findViewById(R.id.appviptype);
				
				commonItemHolder.freestype=(TextView) view
						.findViewById(R.id.appfreetype);
			
				commonItemHolder.DownloadProgressBar=(ProgressBar) view
						.findViewById(R.id.DownloadProgressBar);
				commonItemHolder.progressnumtext=(TextView) view
						.findViewById(R.id.progressnumtext);
				commonItemHolder.downBtn2 = (TextView) view
						.findViewById(R.id.bt_down_btn2);
				/*commonItemHolder.share_icon =(ImageView) view
				.findViewById(R.id.share_icon);*/
				
				/*modify end*/
				
				holder.favItemHolder =commonItemHolder;
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
			case BigItem.Type.LAYOUT_SUBLIST:
				
				final FavItemHolder itemHolder = holder.favItemHolder;
				int size=0;
				for (final AppItem appItem : item.subListItems) {
					if(position==0)
					{
						itemHolder.itemLayout.setBackgroundResource(R.drawable.beston_favtop_onclick);
						itemHolder.bommline1.setVisibility(View.VISIBLE);
					}
					else if(position==(mAdapter.getCount()-1))
					{
						itemHolder.bommline1.setVisibility(View.GONE);
						itemHolder.itemLayout.setBackgroundResource(R.drawable.beston_favbom_onclick);
						
					}
					else{
						itemHolder.bommline1.setVisibility(View.VISIBLE);
						itemHolder.itemLayout.setBackgroundResource(R.drawable.beston_favmiddle_onclick);
					}
					
					size++;
					itemHolder.name.setText(appItem.name);
					itemHolder.rating.setRating(appItem.rating);
					//itemHolder.size.setText(" | "+appItem.strLength);

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
					itemHolder.downum.setText(appItem.downNum+" | "+appItem.strLength);

					if (isHot) {
						itemHolder.downum.setText(appItem.downNum+" | "+appItem.strLength);
					} else {
						itemHolder.downum.setText(appItem.downNum+" | "+appItem.strLength);
					}

					if (TextUtils.isEmpty(appItem.iconUrl)) {
						itemHolder.appIcon
								.setBackgroundResource(R.drawable.app_empty_icon);
					} else if(!imageLoader.getPause().get()){
						imageLoader.displayImage(appItem.iconUrl,
								itemHolder.appIcon, options);
					}
					/*add by zengxiao for:修改item样式*/
					if(!Constants.ISGOOGLE)
					{
						if(appItem.googlePrice>0&&appItem.googlemarket>0)
						{
							itemHolder.googlepriceFram.setVisibility(View.VISIBLE);
							itemHolder.bt_free_btn.setVisibility(View.VISIBLE);
							itemHolder.bt_free_btn.setText(MyApplication.getInstance().getResources().getString(R.string.char_alert)+
									appItem.googlePrice);

						}else{
							itemHolder.googlepriceFram.setVisibility(View.GONE);
							itemHolder.bt_free_btn.setVisibility(View.GONE);

						}
					}
					/*else{
						itemHolder.freestype.setText(R.string.txt_detail_tag_free);
					}
					if(appItem.ulevel>0)
					{
						itemHolder.appvip.setVisibility(View.VISIBLE);
						itemHolder.appvip.setText("VIP"+appItem.ulevel);
					}*/
					/*add end*/

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
								
								if(itemHolder.downBtn2.getText().toString().equals(FavListFragment.this.getActivity().getString(R.string.sharedown)))
								{
									Intent intent =new Intent();
									intent.setClass(FavListFragment.this.getActivity(),ShareActivity.class);						
									intent.putExtra("sendstring", appItem); 
									FavListFragment.this.getActivity().startActivity(intent);
								}
								else
								{
								DownloadTaskManager.getInstance().onDownloadBtnClick(appItem);
								}
								
							}
						});
						if(!imageLoader.getPause().get()){
								DownloadTaskManager.getInstance().updateItemBtnByStateshare(
									getActivity(), itemHolder.downBtn2, appItem,itemHolder.DownloadProgressBar,itemHolder.progressnumtext,itemHolder.share_icon,itemHolder.sharelayout);
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
				FavListFragment.this.getAdapter().mListItems,
				new int[] { DownloadTaskManager.FILL_TYPE_SUBLIST_ITEMS });
		FavListFragment.this.getAdapter().notifyDataSetChanged();
	}

	@Override
	public void onAppClick(Object obj, String action) {
		Intent intent = new Intent(action);
		if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			intent.putExtra("app", app);
		}
		String fromstr = LogModel.L_CATE_HOME;
		if (list_cate != null)
			fromstr = from + "," + list_cate;
		else
			fromstr = from + "," + cateId;
		intent.putExtra(Constants.LIST_FROM, fromstr);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	public interface OnTitleBtnClick {

		void onTitleBtnClick(String type);
	}

	@Override
	public void addNewDataOnce() {
		if (list_cate != null)
			Util.addListData(maContext, from, list_cate, 1);
		else
			Util.addListData(maContext, from, String.valueOf(cateId), 1);
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub
		listview.setVisibility(View.VISIBLE);
		if(getPageInfo().getPageNum()<2)
		getanather.setVisibility(View.GONE);
		else{
			getanather.setVisibility(View.VISIBLE);
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.getdataanother:
			if (mAdapter.getPageInfo().getPageIndex() >= mAdapter.getPageInfo()
			.getPageNum()) {
		// if(isBottom){
		// }
		mAdapter.getPageInfo().setPageIndex(0);
		
	}
	requestData();
			
			break;

		default:
			break;
		}
	}
	@Override
	public void requestData() {
		// TODO Auto-generated method stub
		super.requestData();
		mAdapter.clear();
		listview.setVisibility(View.GONE);
		getanather.setVisibility(View.GONE);
		loading.setVisibility(View.VISIBLE);
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
