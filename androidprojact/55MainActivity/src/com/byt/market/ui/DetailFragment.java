package com.byt.market.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.DetailFrame;
import com.byt.market.activity.DetailFrame2;
import com.byt.market.activity.ShareActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.adapter.MyListAdapter.RecordLayoutHolder;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.bitmaputil.core.display.RoundedBitmapDisplayer;
import com.byt.market.data.AppDetail;
import com.byt.market.data.AppItem;
import com.byt.market.data.BigItem;
import com.byt.market.data.DetailItemHolder;
import com.byt.market.data.DownloadAppItem;
import com.byt.market.data.DownloadItem;
import com.byt.market.data.UserData;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.util.DataUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.DetailTypeView;
import com.byt.market.view.ScreenshotBand;

/**
 * 游戏详情
 */
public class DetailFragment extends ListViewFragment  implements OnTouchListener{
	private float startx=0;
	private AppItem appitem;
	private ViewPager viewPager;
	private boolean isshowgooglemarkt=false;//解决连续点击原价弹出多个提示框
	private boolean issend=false;
	private boolean headisshow=true;
	public boolean isIssend() {
		return issend;
	}

	public void setIssend(boolean issend) {
		this.issend = issend;
	}

	public void setViewPager(ViewPager viewPager) {
		this.viewPager = viewPager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appitem = getArguments().getParcelable("app");
	}

	@Override
	protected String tag() {
		return this.getClass().getSimpleName();
	}

	@Override
	protected String getRequestUrl() {
		return Constants.LIST_URL + "?qt=" + Constants.DETAIL + "&sid="
				+ appitem.sid;
	}

	@Override
	protected String getRequestContent() {
		return null;
	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		try {
			if (!result.isNull("data")) {
				JSONObject jsObject = result.getJSONObject("data");
				List<BigItem> bigItems = JsonParse.parseDetailList(getActivity(),jsObject);
				if (bigItems != null && bigItems.size() > 0) {
					// 下面是解决当从我的游戏中点击进详情时,apk属性为null时,点击分享会报错
					if (getActivity() instanceof DetailFrame2) {
						DetailFrame2 parentActivity = (DetailFrame2) (getActivity());
						if (parentActivity != null) {
							if (parentActivity.app != null
									&& TextUtils
											.isEmpty(parentActivity.app.apk)) {
								parentActivity.app.apk = jsObject
										.optString("APK");
							}
						}
					}
				}
				return bigItems;
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
		return new DetailAdapter();
	}

	private class DetailAdapter extends ImageAdapter {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		DisplayImageOptions screenOptions ; 

		public DetailAdapter() {
			df.setMaximumFractionDigits(1);
			screenOptions =  new DisplayImageOptions.Builder()
			.cacheOnDisc().delayBeforeLoading(200).build();
		}

		@SuppressLint("WrongViewCast")
		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_DETAILS:
				view = inflater.inflate(R.layout.listitem_detail, null);
				holder.layoutType = item.layoutType;
				DetailItemHolder itemHolder = new DetailItemHolder();
				/*itemHolder.icon = (ImageView) view
						.findViewById(R.id.appIconView);
				itemHolder.name = (TextView) view
						.findViewById(R.id.appNameLabel);
				itemHolder.rating = (RatingBar) view
						.findViewById(R.id.appRatingView);
				itemHolder.downnum = (TextView) view
						.findViewById(R.id.appDownloadnumLabel);*/

				itemHolder.size = (TextView) view
						.findViewById(R.id.appSizeView);
				itemHolder.catename = (TextView) view
						.findViewById(R.id.appCateView);
				itemHolder.vname = (TextView) view
						.findViewById(R.id.appVnameView);
				itemHolder.updatetime = (TextView) view
						.findViewById(R.id.appUTView);
				itemHolder.tagLayout = (LinearLayout) view
						.findViewById(R.id.tagLayoutView);
				itemHolder.tag = (TextView) view.findViewById(R.id.appTagView); // 标签
				itemHolder.lang = (TextView) view
						.findViewById(R.id.appLangView);
				itemHolder.feetype = (TextView) view
						.findViewById(R.id.appFeeView);
				itemHolder.v_ades = view
						.findViewById(R.id.listitem_detail_ades);
				itemHolder.v_sdes = view
						.findViewById(R.id.listitem_detail_sdes);
				itemHolder.ades = (TextView) view
						.findViewById(R.id.introduceLabel);
				itemHolder.des = (TextView) view
						.findViewById(R.id.imprintLabel);
				itemHolder.exButton = (ImageButton) view
						.findViewById(R.id.imprintExpandBtn);
				itemHolder.sBand = (GridView) view
						.findViewById(R.id.screenshotFrame);
				
				/*add by zengxiao*/
				itemHolder.btn_down=(Button)view.findViewById(R.id.downbutton);
				itemHolder.btn_copareprice=(LinearLayout)view.findViewById(R.id.comparepri);
				itemHolder.textviewprice=(TextView) view.findViewById(R.id.bt_free_btn);
				itemHolder.freeline=(ImageView) view.findViewById(R.id.freeline);
				/*add end*/
				// itemHolder.listitem_d_tip = (DetailTipView) view
				// .findViewById(R.id.listitem_d_tip);
				
				
				itemHolder.exButton.setFocusable(false);
				holder.detailHolder = itemHolder;
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
			case BigItem.Type.LAYOUT_DETAILS:
				for (final AppDetail app : item.details) {
				/*	holder.detailHolder.name.setText(app.name);
					holder.detailHolder.downnum.setText(getActivity().getString(R.string.detail_down) + app.downNum
							+ "");
					holder.detailHolder.rating.setRating(app.rating);
				*/	holder.detailHolder.size.setText(getActivity().getString(R.string.detail_size) + app.strLength);
					holder.detailHolder.catename.setText(app.cateName);
					holder.detailHolder.catename.setVisibility(View.GONE);
					holder.detailHolder.vname.setText(getActivity().getString(R.string.detail_version) + app.vname);
					holder.detailHolder.updatetime.setText(getActivity().getString(R.string.head_update)+":"+app.utime);
					if (!TextUtils.isEmpty(app.tag)) {
						holder.detailHolder.tagLayout
								.setVisibility(View.VISIBLE);
						if (app.tag.contains(",")) {
							StringBuilder tagTxt = new StringBuilder();
							String[] tags = app.tag.split(",");
							for (String t : tags) {
								tagTxt.append(t + " ");
							}
							holder.detailHolder.tag.setText(getActivity().getString(R.string.detail_tag)+tagTxt.toString());
									
						} else {
							holder.detailHolder.tag.setText(getActivity().getString(R.string.detail_tag)+app.tag);
						}
					} else {
						holder.detailHolder.tagLayout.setVisibility(View.GONE);
					}
					if (app.lang == 1)
						holder.detailHolder.lang.setText(getActivity().getString(R.string.detail_lang) + getActivity().getString(R.string.detail_zh));
					else if (app.lang == 2)
						holder.detailHolder.lang.setText(getActivity().getString(R.string.detail_lang) + getActivity().getString(R.string.detail_en));
					else
						holder.detailHolder.lang.setText(getActivity().getString(R.string.detail_lang) + getActivity().getString(R.string.detail_unknow));
					// if (app.imagePath != null) {
					// if (app.imagePath.contains(",")) {
					// String[] imgs = app.imagePath.split(",");
					if (app.screen != null) {
						if (app.screen.contains(",")) {
							String[] imgs = app.screen.split(",");
							ArrayList<String> datas = new ArrayList<String>();
							for (String string : imgs) {
								datas.add(string);
							}
							/*holder.detailHolder.sBand.getScrollLayout().setViewPager(viewPager);
							holder.detailHolder.sBand.flushAdvertiseBand(imageLoader,screenOptions,
									app.screen, datas);*/
							Display mDisplay = DetailFragment.this.getActivity().getWindowManager().getDefaultDisplay();
							int W = mDisplay.getWidth();
							int H = mDisplay.getHeight();
						    LayoutParams params = new LayoutParams(datas.size() * (W/2), LayoutParams.FILL_PARENT);  
						    
						    holder.detailHolder.sBand.setLayoutParams(params);  
						    holder.detailHolder.sBand.setColumnWidth(W/2);  
						    holder.detailHolder.sBand.setHorizontalSpacing(5);  
						    holder.detailHolder.sBand.setStretchMode(GridView.NO_STRETCH);  
						    holder.detailHolder.sBand.setNumColumns(datas.size()); 
							DetailMyListAdapter mydapter=new DetailMyListAdapter(datas,app.screen);
							holder.detailHolder.sBand.setAdapter(mydapter);
										}
					}
				/*	if (app.stype != null && !app.stype.equals("")) {
						holder.detailHolder.tv_type.setVisibility(View.VISIBLE);
						holder.detailHolder.tv_type
								.flushAdvertiseBand(app.stype);
					} else {
						holder.detailHolder.tv_type.setVisibility(View.GONE);
					}*/
					holder.detailHolder.feetype.setText(getActivity().getString(R.string.detail_exp) + app.feetype);
					if (app.adesc != null && !TextUtils.isEmpty(app.adesc)) {
						holder.detailHolder.v_ades.setVisibility(View.VISIBLE);
						holder.detailHolder.ades.setText(app.adesc);
					} else
						holder.detailHolder.v_ades.setVisibility(View.GONE);
					if (app.sdesc != null) {
						holder.detailHolder.v_sdes.setVisibility(View.VISIBLE);
						String sDescribe = app.sdesc;
						/*if (sDescribe.length() > 130) {
							sDescribe = sDescribe.substring(0, 130) + "...";
							holder.detailHolder.des.setText(sDescribe);
							holder.detailHolder.exButton
									.setVisibility(View.VISIBLE);
							holder.detailHolder.exButton.setSelected(true);
							OnClickListener desClickListener = new OnClickListener() {
								@Override
								public void onClick(View arg0) {
									ImageButton button = (ImageButton) holder.detailHolder.exButton;
									boolean flag = false;
									String des = app.sdesc;
									if (!button.isSelected()) {
										des = app.sdesc.substring(0, 130)
												+ "...";
										button.setImageResource(R.drawable.detail_zhankai_icon);
									} else {
										button.setImageResource(R.drawable.detail_expand_icon);
									}

									holder.detailHolder.des.setText( app.sdesc);
									if (button.isSelected()) {
										flag = false;
									} else {
										flag = true;
									}
									button.setSelected(flag);
								}
							};
							holder.detailHolder.exButton
									.setOnClickListener(desClickListener);
							holder.detailHolder.des
									.setOnClickListener(desClickListener);
						} else {*/
							holder.detailHolder.des.setText(sDescribe);
							holder.detailHolder.exButton
									.setVisibility(View.GONE);
						/*}*/
					} else
						holder.detailHolder.v_sdes.setVisibility(View.GONE);
					// edit by wangxin.
				/*	if (TextUtils.isEmpty(app.iconUrl)) {
						holder.detailHolder.icon
								.setImageResource(R.drawable.app_empty_icon);
					} else {
						// mImageFetcher.loadImage(app.iconUrl,
						// holder.detailHolder.icon);
						imageLoader.displayImage(app.iconUrl,
								holder.detailHolder.icon, options);
					}*/
					//下载按钮操作
						/*holder.detailHolder.btn_down.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							downApp(app,holder.detailHolder.btn_down);
						}
					});*/
						/*
						 *  ------------The Bestone modifed start--------------
						 *   Modified by "zengxiao"  Date:20140513
						 *   Modified for:增加价格对比
						 */
						DetailFrame2 detailFrame2=(DetailFrame2)getActivity();
						if(app.googlemarket!=1){
							holder.detailHolder.btn_copareprice.setEnabled(false);
							detailFrame2.comparepri.setEnabled(false);
						}else{
							detailFrame2.textviewprice.setTextColor(Color.WHITE);
							if(app.googlePrice==0)
							{
								
								holder.detailHolder.textviewprice.setText(getString(R.string.txt_detail_tag_free));
								detailFrame2.textviewprice.setText(getString(R.string.txt_detail_tag_free));							
							}
							else{
								holder.detailHolder.freeline.setVisibility(View.VISIBLE);
								holder.detailHolder.btn_copareprice.setEnabled(true);
								detailFrame2.comparepri.setEnabled(true);
							holder.detailHolder.textviewprice.setText(""+app.googlePrice);
							detailFrame2.textviewprice.setText(""+app.googlePrice);
							detailFrame2.freeline.setVisibility(View.VISIBLE);
							}
						}
						detailFrame2.appDetail=app;
						holder.detailHolder.btn_copareprice.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								try{
								 Uri uri = Uri.parse("market://details?id="+appitem.pname);
								 Intent it   = new Intent(Intent.ACTION_VIEW,uri); 
								 it.setClassName("com.android.vending","com.android.vending.AssetBrowserActivity");
							      startActivity(it);
								  }
								  catch(ActivityNotFoundException e){
									  if(isshowgooglemarkt)
									  {
										  
									  }else
									  {
										  isshowgooglemarkt=true;
										  Toast.makeText(DetailFragment.this.getActivity(), R.string.notinstallgoogle, Toast.LENGTH_SHORT).show();
										  new Handler().postDelayed(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												isshowgooglemarkt=false;
											}
										}, 2000);
									  }
									  }
			
							}
						});
												
						 /*
						------------The Bestone modifed end--------------
						*/
						/*DownloadTaskManager.getInstance().updateByState2(
								getActivity(), detailFrame2.downbutton, appitem,
								null, null, true,
								false);*/
					// if (app.icon == null) {
					// holder.detailHolder.icon
					// .setImageResource(R.drawable.app_empty_icon);
					// } else {
					// holder.detailHolder.icon.setImageBitmap(app.icon);
					// }
				}
				break;
			case BigItem.Type.LAYOUT_LOADING:

				break;
			case BigItem.Type.LAYOUT_LOADFAILED:

				break;
			}
		}

	}
	public void downApp(AppItem appDetail,TextView v,ProgressBar progressbar,TextView textview){		
		// TODO Auto-generated method stub
		UserData user = MyApplication.getInstance()
				.getUser();
		if (appDetail.isshare==0||issend||!(v.getText().toString().equals(getString(R.string.detailfree)))) {
			DownloadTaskManager.getInstance().onDownloadBtnClick(appitem);

	DownloadTaskManager.getInstance().updateByState2(getActivity(),
					v,
					appitem, progressbar,
					textview, true, false);

		} else {
			if (user != null
					&& user.getUlevel() < appDetail.ulevel) {
					Intent intent =new Intent();
						intent.setClass(DetailFragment.this.getActivity(),ShareActivity.class);
						Bundle bundle=new Bundle();
						bundle.putString("sendstring", ""); 
						intent.putExtra("sendbundle",bundle);
						DetailFragment.this.getActivity().startActivityForResult(intent,0);
						
			} else {
				DownloadTaskManager
						.getInstance()
						.onDownloadBtnClick(appitem);

				DownloadTaskManager
						.getInstance()
						.updateByState2(
								getActivity(),
								v,
								appitem, progressbar,
								textview, true, false);
			}
		}
	
	}

	@Override
	protected void onDownloadStateChanged() {
		DetailFragment.this.getAdapter().notifyDataSetChanged();
	}

	@Override
	public void onAppClick(Object obj, String action) {

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		switch(resultCode){
		case -1:
			issend=true;
			break;
		}
		
	}

	public boolean isLoading() {
		return isLoading;
	}
	/*add  by "zengxiao"*/
	class DetailMyListAdapter extends BaseAdapter {
		String appsc;
		ArrayList<String> mappImage;
		public DetailMyListAdapter(ArrayList<String> appImage,String appscreen) {
			mappImage=appImage;
			appsc=appscreen;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mappImage.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mappImage.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int myposition = position;
			final RecordLayoutHolder mlayout;
			RecordLayoutHolder layoutHolder = null;
			if (convertView == null) {
				layoutHolder = new RecordLayoutHolder();
				convertView = LayoutInflater.from(DetailFragment.this.getActivity()).inflate(
						R.layout.bestone_imageitem, null, false);
				layoutHolder.setas = (ImageView)convertView.findViewById(R.id.appImage);
				convertView.setTag(layoutHolder);
			} else {
				layoutHolder = (RecordLayoutHolder) convertView.getTag();
			}
			mlayout = layoutHolder;
			layoutHolder.setas.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Constants.TOBSREEN);
					intent.putExtra("bimgs", appsc);
					intent.putExtra("screen",myposition );
					DetailFragment.this.getActivity().startActivity(intent);
					DetailFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
							R.anim.push_left_out);
			}
				
			});
			flushAdvertiseBand(mappImage.get(myposition),layoutHolder.setas);

		
			
			return convertView;

		}
		public class RecordLayoutHolder {
			public ImageView setas;

		}
	}
	public void flushAdvertiseBand(String iconUrl,ImageView imageView) {			
			String url;
			/*
			 *  ------------The Bestone modifed start--------------
			 *   Modified by "zengxiao"  Date:20140523
			 *   Modified for:修改详情滚动图路径
			 */
			
			if (iconUrl.startsWith("http://")) {
				url = iconUrl.substring(0, iconUrl.lastIndexOf("/") + 1)
						+ "/"
						+ iconUrl.substring(iconUrl.lastIndexOf("/") + 1);
			} else {
				url = Constants.IMG_URL
						+ iconUrl.substring(0, iconUrl.lastIndexOf("/") + 1)
						+ "/"
						+ iconUrl.substring(iconUrl.lastIndexOf("/") + 1);
			}						
			 /*
			------------The Bestone modifed end--------------
			*/
				if(!TextUtils.isEmpty(url)){
					DisplayImageOptions screenOptions ; 
					screenOptions =  new DisplayImageOptions.Builder()
					.cacheOnDisc().delayBeforeLoading(200).build();
					imageLoader.displayImage(url,imageView,screenOptions);
				}
				
	}
	
	/*add end*/
	/*add by zengxiao for:滑动到顶时隐藏详情头*/
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		if(firstVisibleItem==0&&totalItemCount!=0){
			
			headisshow=true;
			Log.d("zxnew", "headisshow="+headisshow);
        } 
		else{
			
			headisshow=false;  
			Log.d("zxnew", "headisshow="+headisshow);
		}
	}
	/*add end*/

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		switch(arg1.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			 startx=arg1.getRawY();
			break;
		case MotionEvent.ACTION_UP:
			 
			break;
		case MotionEvent.ACTION_MOVE:
			
			 if(headisshow)
			 {
				 Log.d("zxnew", "arg1.get-startx="+(arg1.getY()-startx));
				 if((arg1.getRawY()-startx)>0){
					 ((DetailFrame2)DetailFragment.this.getActivity()).showheaddetail();
				 }
				
					
					
			 }
			 else{
				 if((arg1.getRawY()-startx)<0)
				 ((DetailFrame2)DetailFragment.this.getActivity()).hideheaddetail();
			 }
			break;
		}
		return false;
	}
	@Override
	public void setStyle(CusPullListView listview2) {
			listview.setOnTouchListener(this);
		
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
