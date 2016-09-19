package com.byt.market.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.DetailFrame2;
import com.byt.market.activity.ShareActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.data.AppDetail;
import com.byt.market.data.AppItem;
import com.byt.market.data.BigItem;
import com.byt.market.data.DetailItemHolder;
import com.byt.market.data.UserData;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;

public class DetailFragment1 extends BaseUIFragment implements TaskListener {
	private DetailItemHolder holder;
	private AppItem appitem;
	private boolean issend = false;
	private boolean isshowgooglemarkt = false;// 解决连续点击原价弹出多个提示框
	protected boolean isLoading;
	private ProtocolTask mTask;
	protected View loading, detailArea;
	private boolean isLoaded;
	DisplayImageOptions screenOptions;
	DetailMyListAdapter mydapter;

	public boolean isIssend() {
		return issend;
	}

	public void setIssend(boolean issend) {
		this.issend = issend;
	}

	public boolean isLoading() {
		return isLoading;
	}

	public void setBundle(Bundle bundle) {
		appitem = bundle.getParcelable("app");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.listitem_detail, container, false);
		initView(view);
		initImageLoader();
		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case -1:
			issend = true;
			break;
		}

	}

	private void initView(View view) {
		holder = new DetailItemHolder();	
		screenOptions = new DisplayImageOptions.Builder()
				.cacheOnDisc().delayBeforeLoading(200).build();
		holder.size = (TextView) view.findViewById(R.id.appSizeView);
		holder.catename = (TextView) view.findViewById(R.id.appCateView);
		holder.vname = (TextView) view.findViewById(R.id.appVnameView);
		holder.updatetime = (TextView) view.findViewById(R.id.appUTView);
		holder.tagLayout = (LinearLayout) view.findViewById(R.id.tagLayoutView);
		holder.tag = (TextView) view.findViewById(R.id.appTagView); // 标签
		holder.lang = (TextView) view.findViewById(R.id.appLangView);
		holder.feetype = (TextView) view.findViewById(R.id.appFeeView);
		holder.v_ades = view.findViewById(R.id.listitem_detail_ades);
		holder.v_sdes = view.findViewById(R.id.listitem_detail_sdes);
		holder.ades = (TextView) view.findViewById(R.id.introduceLabel);
		holder.des = (TextView) view.findViewById(R.id.imprintLabel);
		holder.exButton = (ImageButton) view
				.findViewById(R.id.imprintExpandBtn);
		holder.sBand = (GridView) view.findViewById(R.id.screenshotFrame);
		holder.exButton.setFocusable(false);
		loading = view.findViewById(R.id.listview_loading);
		detailArea = view.findViewById(R.id.detail_area);
	}

	private void initData(BigItem item) {
		isLoaded = true;
		for (final AppDetail app : item.details) {
			holder.size.setText(getActivity().getString(R.string.detail_size)+" "
					+ app.strLength);
			holder.catename.setText(app.cateName);
			holder.catename.setVisibility(View.GONE);
			holder.vname.setText(getActivity().getString(
					R.string.detail_version)+" "
					+ app.vname);
			holder.updatetime.setText(getActivity().getString(
					R.string.head_update)
					+ ":"+"  " + app.utime);
			if (!TextUtils.isEmpty(app.tag)) {
				holder.tagLayout.setVisibility(View.VISIBLE);
				if (app.tag.contains(",")) {
					StringBuilder tagTxt = new StringBuilder();
					String[] tags = app.tag.split(",");
					for (String t : tags) {
						tagTxt.append(t + " ");
					}
					holder.tag.setText(getActivity().getString(
							R.string.detail_tag)
							+ tagTxt.toString());

				} else {
					holder.tag.setText(getActivity().getString(
							R.string.detail_tag)
							+ app.tag);
				}
			} else {
				holder.tagLayout.setVisibility(View.GONE);
			}
			if (app.lang == 1)
				holder.lang.setText(getActivity().getString(
						R.string.detail_lang)
						+ getActivity().getString(R.string.detail_zh));
			else if (app.lang == 2)
				holder.lang.setText(getActivity().getString(
						R.string.detail_lang)
						+ getActivity().getString(R.string.detail_en));
			else
				holder.lang.setText(getActivity().getString(
						R.string.detail_lang)
						+ getActivity().getString(R.string.detail_unknow));
			if (app.screen != null) {
				if (app.screen.contains(",")) {
					String[] imgs = app.screen.split(",");
					ArrayList<String> datas = new ArrayList<String>();
					for (String string : imgs) {
						datas.add(string);
					}
					Display mDisplay = DetailFragment1.this.getActivity()
							.getWindowManager().getDefaultDisplay();
					int W = mDisplay.getWidth();
					int H = mDisplay.getHeight();
					LayoutParams params = new LayoutParams(datas.size()
							* ((W / 2-40)+5), LayoutParams.FILL_PARENT);

					holder.sBand.setLayoutParams(params);
					holder.sBand.setColumnWidth(W / 2-40);
					holder.sBand.setHorizontalSpacing(5);
					holder.sBand.setStretchMode(GridView.NO_STRETCH);
					holder.sBand.setNumColumns(datas.size());
					mydapter = new DetailMyListAdapter(
							datas, app.screen);
					holder.sBand.setAdapter(mydapter);
				}
			}
			holder.feetype.setText(getActivity().getString(R.string.detail_exp)
					+ app.feetype);
			if (app.adesc != null && !TextUtils.isEmpty(app.adesc)) {
				holder.v_ades.setVisibility(View.VISIBLE);
				holder.ades.setText(app.adesc);
			} else
				holder.v_ades.setVisibility(View.GONE);
			if (app.sdesc != null) {
				holder.v_sdes.setVisibility(View.VISIBLE);
				String sDescribe = app.sdesc;
				/*if (sDescribe.length() > 130) {
					sDescribe = sDescribe.substring(0, 130) + "...";
					holder.des.setText(sDescribe);
					holder.exButton.setVisibility(View.VISIBLE);
					holder.exButton.setSelected(true);
					OnClickListener desClickListener = new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							ImageButton button = (ImageButton) holder.exButton;
							boolean flag = false;
							String des = app.sdesc;
							if (!button.isSelected()) {
								des = app.sdesc.substring(0, 130) + "...";
								button.setImageResource(R.drawable.detail_zhankai_icon);
							} else {
								button.setImageResource(R.drawable.detail_expand_icon);
							}

							holder.des.setText(des);
							if (button.isSelected()) {
								flag = false;
							} else {
								flag = true;
							}
							button.setSelected(flag);
						}
					};
					holder.exButton.setOnClickListener(desClickListener);
					holder.des.setOnClickListener(desClickListener);
				} else {*/
					holder.des.setText(sDescribe);
					holder.exButton.setVisibility(View.GONE);
				/*}*/
			} else
				holder.v_sdes.setVisibility(View.GONE);
			/*holder.btn_down.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					downApp(app, holder.btn_down);
				}
			});*/
			DetailFrame2 detailFrame2 = (DetailFrame2) getActivity();
			if (app.googlemarket != 1) {
				detailFrame2.comparepri.setEnabled(false);
			} else {
				detailFrame2.textviewprice.setTextColor(Color.WHITE);
				if (app.googlePrice == 0) {

					detailFrame2.textviewprice.setText(getString(R.string.txt_detail_tag_free));							
			
				} else {
					detailFrame2.comparepri.setEnabled(true);
				detailFrame2.textviewprice.setText(MyApplication.getInstance().getResources().getString(R.string.char_alert)+app.googlePrice);
				detailFrame2.freeline.setVisibility(View.VISIBLE);
				
				}
			}
			detailFrame2.appDetail = app;
			

			/*DownloadTaskManager.getInstance().updateByState2(getActivity(),
					detailFrame2.downbutton, appitem, null, null, true, false);*/
		}

	}

	protected String getRequestUrl() {
		return Constants.LIST_URL + "?qt=" + Constants.DETAIL + "&sid="
				+ appitem.sid;
	}

	protected String tag() {
		return this.getClass().getSimpleName();
	}

	public HashMap<String, String> getHeader() {
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		if (imei == null)
			imei = Util.getIMEI(MyApplication.getInstance());
		if (vcode == null)
			vcode = Util.getVcode(MyApplication.getInstance());
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(MyApplication.getInstance());
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("channel", channel);
		return map;
	}

	public void requestData() {
		if (!isLoading && !isLoaded) {
			isLoading = true;
			updateViewShowState();
			mTask = new ProtocolTask(MyApplication.getInstance());
			mTask.setListener(this);
			mTask.execute(getRequestUrl(), null, tag(), getHeader());
		}
	}

	@Override
	public void onNoNetworking() {
		isLoading = false;
		updateViewShowState();
		System.gc();
	}

	@Override
	public void onNetworkingError() {
		isLoading = false;
		updateViewShowState();
		System.gc();
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		try {
			if (bytes != null) {
				JSONObject result = new JSONObject(new String(bytes));
				int status = JsonParse.parseResultStatus(result);
				if (status == 1) {
					List<BigItem> appendList = parseListData(result);
					if (appendList != null && !appendList.isEmpty()) {
						initData(appendList.get(0));
					} else {
					}
				} else {
				}
			} else {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.gc();
		isLoading = false;
		updateViewShowState();
	}

	private void updateViewShowState() {
		try {
			if (!isLoading) {
				loading.setVisibility(View.GONE);
				detailArea.setVisibility(View.VISIBLE);
			} else {
				loading.setVisibility(View.VISIBLE);
				detailArea.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected List<BigItem> parseListData(JSONObject result) {
		try {
			if (!result.isNull("data")) {
				JSONObject jsObject = result.getJSONObject("data");
				List<BigItem> bigItems = JsonParse.parseDetailList(
						getActivity(), jsObject);
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
	public void downAppshare(AppItem appDetail,TextView v,ProgressBar progressbar,TextView textview)
	{
		if (issend||!(v.getText().toString().equals(getString(R.string.sharedowndetail)))) {
			DownloadTaskManager.getInstance().onDownloadBtnClick(appitem);

			DownloadTaskManager.getInstance().updateByState2share(getActivity(),
							v,
							appitem, progressbar,
							textview, true, false,null,null);
		}
		else{
			Intent intent =new Intent();
			intent.setClass(DetailFragment1.this.getActivity(),ShareActivity.class);
			intent.putExtra("sendstring", appitem); 
			DetailFragment1.this.getActivity().startActivityForResult(intent,0);
		}
	}
	public void downApp(AppItem appDetail,TextView v,ProgressBar progressbar,TextView textview){		
			DownloadTaskManager.getInstance().onDownloadBtnClick(appitem);

	DownloadTaskManager.getInstance().updateByState2(getActivity(),
					v,
					appitem, progressbar,
					textview, true, false);
	}

	/* add by "zengxiao" */
	class DetailMyListAdapter extends BaseAdapter {
		String appsc;
		ArrayList<String> mappImage;

		public DetailMyListAdapter(ArrayList<String> appImage, String appscreen) {
			mappImage = appImage;
			appsc = appscreen;
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
		public void clear(){
			if(mappImage!=null){
				mappImage.clear();
			}
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int myposition = position;
			final RecordLayoutHolder mlayout;
			RecordLayoutHolder layoutHolder = null;
			if (convertView == null) {
				layoutHolder = new RecordLayoutHolder();
				convertView = LayoutInflater.from(
						DetailFragment1.this.getActivity()).inflate(
						R.layout.bestone_imageitem, null, false);
				layoutHolder.setas = (ImageView) convertView
						.findViewById(R.id.appImage);
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
					intent.putExtra("screen", myposition);
					DetailFragment1.this.getActivity().startActivity(intent);
					DetailFragment1.this.getActivity()
							.overridePendingTransition(R.anim.push_left_in,
									R.anim.push_left_out);
				}

			});
			flushAdvertiseBand(mappImage.get(myposition), layoutHolder.setas);

			return convertView;

		}

		public class RecordLayoutHolder {
			public ImageView setas;

		}
	}

	protected void initImageLoader() {
		imageLoader = ImageLoader.getInstance();
	}

	public void flushAdvertiseBand(String iconUrl, ImageView imageView) {
		String url;
		/*
		 * ------------The Bestone modifed start-------------- Modified by
		 * "zengxiao" Date:20140523 Modified for:修改详情滚动图路径
		 */

		if (iconUrl.startsWith("http://")) {
			url = iconUrl.substring(0, iconUrl.lastIndexOf("/") + 1) + "/"
					+ iconUrl.substring(iconUrl.lastIndexOf("/") + 1);
		} else {
			url = Constants.IMG_URL
					+ iconUrl.substring(0, iconUrl.lastIndexOf("/") + 1) + "/"
					+ iconUrl.substring(iconUrl.lastIndexOf("/") + 1);
		}
		/*
		 * ------------The Bestone modifed end--------------
		 */
		if (!TextUtils.isEmpty(url)) {			
			imageLoader.displayImage(url, imageView, screenOptions);
		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d("myzx", "ListView onDestroyViewa()");
		try {
			if (mTask != null) {
				isLoading = false;
				mTask.onCancelled();
			}
			if(imageLoader!=null){
				imageLoader.clearMemoryCache();
//				imageLoader.clearDiscCache();
			}
			if(holder!=null&&holder.sBand!=null){
				holder.sBand.setAdapter(null);
				mydapter.clear();
			}
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
