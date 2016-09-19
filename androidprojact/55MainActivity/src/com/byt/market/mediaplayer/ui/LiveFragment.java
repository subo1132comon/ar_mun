package com.byt.market.mediaplayer.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Video.VideoColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluepay.data.Config;
import com.bluepay.pay.BlueMessage;
import com.bluepay.pay.BluePay;
import com.bluepay.pay.ClientHelper;
import com.bluepay.pay.IPayCallback;
import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.ShareActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.display.ZoomAndRounderBitmapDisplayer;
import com.byt.market.data.BigItem;
import com.byt.market.data.CateItem;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.PlayActivity;
import com.byt.market.mediaplayer.PlayWebVideoActivity;
import com.byt.market.mediaplayer.data.PlayDownloadItem;
import com.byt.market.mediaplayer.data.VideoItem;
import com.byt.market.mediaplayer.service.PlayDownloadService;
import com.byt.market.mediaplayer.tv.NewTVListFragment;
import com.byt.market.mediaplayer.ui.MovieFragment.Ipayback;
import com.byt.market.tools.Dailog;
import com.byt.market.tools.DownLoadVdioapkTools;
import com.byt.market.tools.LogCart;
import com.byt.market.tools.Toolst;
import com.byt.market.ui.CateListViewFragment;
import com.byt.market.ui.HomeFragment;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.BluePayUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.NotifaHttpUtil.NotifaHttpResalout;
import com.byt.market.util.PayUtile;
import com.byt.market.util.RapitUtile;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.Singinstents;
import com.byt.market.util.StartVidioutil;
import com.byt.market.util.ToastUtile;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.MyPayDailog.PayBack;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

/**
 * 分类列表
 * 
 * @author qiuximing
 * 
 */
public class LiveFragment extends ListViewFragment {
	
	//---------add by  bobo
	private static final int BAO_MATH = 123;
	private static final int BAO_DAY = 0124;
	private static final int PAYSing = 0125;
	
	private VideoItem mcateItem;
	
	String netType;
	private TextView musiclandtitle;
	private DisplayImageOptions mOptions;
	private int mid;
	private int type = 1;
	private int price = 0;
	//---------add by  bobo
	private TextView paytext;
	private float mpayMoney;
	private boolean isPay = false;
	//private boolean canpay = false;
	private int price_day = 0;
	private int feeid = 0;
	private int fee_day_id = 0;
	private int priodid = 0;
	private int nCheckResult0 = -20;
	private int mleve = 0;
	private int FEEID_WEEK ;        
	private int FEEID_2MONTHS;
	private int FEEID_YEAR;
	private int PRICE_WEEK;
	private int PRICE_2MONTHS;
	private int PRICE_YEAR;
	public int getFEEID_WEEK() {
		return FEEID_WEEK;
	}

	public void setFEEID_WEEK(int fEEID_WEEK) {
		FEEID_WEEK = fEEID_WEEK;
	}

	public int getFEEID_2MONTHS() {
		return FEEID_2MONTHS;
	}

	public void setFEEID_2MONTHS(int fEEID_2MONTHS) {
		FEEID_2MONTHS = fEEID_2MONTHS;
	}

	public int getFEEID_YEAR() {
		return FEEID_YEAR;
	}

	public void setFEEID_YEAR(int fEEID_YEAR) {
		FEEID_YEAR = fEEID_YEAR;
	}

	public int getPRICE_WEEK() {
		return PRICE_WEEK;
	}

	public void setPRICE_WEEK(int pRICE_WEEK) {
		PRICE_WEEK = pRICE_WEEK;
	}

	public int getPRICE_2MONTHS() {
		return PRICE_2MONTHS;
	}

	public void setPRICE_2MONTHS(int pRICE_2MONTHS) {
		PRICE_2MONTHS = pRICE_2MONTHS;
	}

	public int getPRICE_YEAR() {
		return PRICE_YEAR;
	}

	public void setPRICE_YEAR(int pRICE_YEAR) {
		PRICE_YEAR = pRICE_YEAR;
	}
	//public static jsxltjsrv jsxltj = new jsxltjsrv();
	public int getPriodid() {
		return priodid;
	}

	public void setPriodid(int priodid) {
		this.priodid = priodid;
	}

	String usname = "";
	public int getPrice_day() {
		return price_day;
	}

	public void setPrice_day(int price_day) {
		this.price_day = price_day;
	}

	public int getFeeid() {
		return feeid;
	}

	public void setFeeid(int feeid) {
		this.feeid = feeid;
	}

	public int getFee_day_id() {
		return fee_day_id;
	}

	public void setFee_day_id(int fee_day_id) {
		this.fee_day_id = fee_day_id;
	}

	private boolean isfirst = true;
	
	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public void canRequestGet() {
		request();
	}


	@Override
	protected String getRequestUrl() {
				String uri = "";
				if (type == 0) {
					String count = "8";
					if(mleve>1){
						count = "16";
					}
					uri = Constants.APK_URL+"Video/v1.php?qt=Videolist2&cid=53&pi="
					+getPageInfo().getNextPageIndex()+"&ps="+count+ "&uid=" 
							+ SharedPrefManager.getLastLoginUserName(getActivity())+"&stype="+"move";
				} else if(type==1){
					
					uri = Constants.APK_URL+"Video/v1.php?qt=videolist3&cid=1&pi=1&ps=20&cat=1";
					
				}else {
					uri = Constants.APK_URL+"Video/v1.php?qt=Videolist2&cid=70" +
							"&pi=" + getPageInfo().getNextPageIndex() + "&ps=" + getPageInfo().getPageSize()+"&stype="+MainActivity.AV_KEY;
				}
				return uri;
				// }
	}

	@Override
	protected void deletelaseDate(List<BigItem> appendList) {
		mAdapter.remove(mAdapter.getCount() - 1);
		mAdapter.remove(mAdapter.getCount() - 1);
		mAdapter.add(appendList);
	}

	public int getMid() {
		return mid;
	}

	public void setMid(int mid) {
		this.mid = mid;
	}

	@Override
	protected String getRequestContent() {
		return null;
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("AV");
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("AV");
	}
	
	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		try {
			List<BigItem> getbigs = null;
			BigItem bigitem = null;
			if (mAdapter != null && mAdapter.getCount() > 0) {
				bigitem = mAdapter.getItem(mAdapter.getCount() - 2);
			}
				getbigs = JsonParse.parseCateVideotwoList(result, bigitem);
				if (getbigs != null) {
					PlayDownloadService.isVideoStatdownedFile(getbigs);//初始化 下载状态
				}
			return getbigs;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.listview;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		netType = Util.getNetAvialbleType(MyApplication.getInstance());
		if (!TextUtils.isEmpty(netType) && "wifi".equals(netType)) {
			// mImageFetcher.setLoadingImage(R.drawable.category_loading);
		} else {
			// mImageFetcher.setLoadingImage(R.drawable.app_empty_icon);
		}
		mOptions = new DisplayImageOptions.Builder().cacheOnDisc().build();
		getActivity().registerReceiver(downreBroadcastReceiver,
				new IntentFilter("com.byt.music.downcomplet"));
		/*
		 * mOptions = new DisplayImageOptions.Builder()
		 * .showStubImage(R.drawable.videodefaultbg)
		 * .showImageForEmptyUri(R.drawable.videodefaultbg) .cacheOnDisc()
		 * .displayer(new ZoomAndRounderBitmapDisplayer((int)
		 * ((outMetrics.widthPixels - 15 * outMetrics.density) / 2))).build();
		 */
	}
	//下载状态
    Handler mhandHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				PlayDownloadService.isVideoStatdownedFile(getAdapter().mListItems);
				getAdapter().notifyDataSetChanged();
				break;
			}
		}
	};
	BroadcastReceiver downreBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
				mhandHandle.removeMessages(0);
				mhandHandle.sendEmptyMessage(0);
		}
	};
	private Handler myhandler = new Handler(){
    	public void handleMessage(Message msg) {
    		if(msg.what == 1){
    			isPay = true;
    			//RapitUtile.setTpay(true);
    			mAdapter.notifyDataSetChanged();
    		}
    	};
    };
	
	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);
		
		mleve = MyApplication.getInstance().getUser().getUlevel();
		if(mleve>=4){
			Message msg = myhandler.obtainMessage();
			msg.what = 1;
			myhandler.sendMessage(msg);
		}else{
				BluePayUtil.qureyAVResoult(MyApplication.getInstance().getUser().getUid(),
						new NotifaHttpResalout() {
					@Override
					public void reaslout(String json) {
						// TODO Auto-generated method stub
						try {
							JSONObject resoutjson = new JSONObject(json);
							if(!resoutjson.isNull("resultStatus")){
								if(resoutjson.getInt("resultStatus") == 1){
									if(!resoutjson.isNull("data")){
										Message msg = myhandler.obtainMessage();
										if(resoutjson.getInt("data")==1){
											msg.what = 1;
										}
										myhandler.sendMessage(msg);
									}
								}else{
									Toast.makeText(LiveFragment.this.getActivity(), getActivity().getString(R.string.network_errors), Toast.LENGTH_LONG).show();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
					}
				});
		}
		//----
		return view;
	}
	
	
	// edit by wangxin
	@Override
	protected ImageAdapter createAdapter() {
		String netType = Util.getNetAvialbleType(MyApplication.getInstance());
		if (!TextUtils.isEmpty(netType)) {
			// if("wifi".equals(netType)){
			// return new WifiCateAdapter();
			// }
		}
		return new WifiCateAdapter();
	}

	private class WifiCateAdapter extends ImageAdapter {

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_VIDEOCAEGORYLIST:
				view = inflater.inflate(R.layout.listitem_videolandthreecate, null);
				holder.layoutType = item.layoutType;
				CategoryItemHolder itemHolder1 = new CategoryItemHolder();
				itemHolder1.content = (TextView) view.findViewById(R.id.listitem_cate_name_Start);
				itemHolder1.icon = (ImageView) view.findViewById(R.id.musicrankpic);
				itemHolder1.layout = view.findViewById(R.id.musicranklandlayout);
				itemHolder1.mimagedowload = (ImageView) view.findViewById(R.id.dowload_image1);
				itemHolder1.mprobar = (ProgressBar) view.findViewById(R.id.musicisdowning1);
				itemHolder1.mdownloaded = (ImageView) view.findViewById(R.id.dowloaded1);
				itemHolder1.videolayout = (RelativeLayout) view.findViewById(R.id.videolandthreelayout1);
				itemHolder1.listitemvideosdc = (TextView) view.findViewById(R.id.listitemvideosdc);
				CategoryItemHolder itemHolder2 = new CategoryItemHolder();
				itemHolder2.icon = (ImageView) view.findViewById(R.id.musicrankpic2);
				itemHolder2.layout = view.findViewById(R.id.musicranklandlayout2);
				itemHolder2.mimagedowload = (ImageView) view.findViewById(R.id.dowload_image2);
				itemHolder2.mprobar = (ProgressBar) view.findViewById(R.id.musicisdowning2);
				itemHolder2.mdownloaded = (ImageView) view.findViewById(R.id.dowloaded2);
				itemHolder2.content = (TextView) view.findViewById(R.id.listitem_cate_name_Start2);
				itemHolder2.listitemvideosdc = (TextView) view.findViewById(R.id.listitemvideosdc2);
				itemHolder2.videolayout = (RelativeLayout) view.findViewById(R.id.videolandthreelayout2);
				CategoryItemHolder itemHolder3 = new CategoryItemHolder();
				itemHolder3.icon = (ImageView) view.findViewById(R.id.musicrankpic3);
				itemHolder3.layout = view.findViewById(R.id.musicranklandlayout3);
				itemHolder3.mimagedowload = (ImageView) view.findViewById(R.id.dowload_image3);
				itemHolder3.mprobar = (ProgressBar) view.findViewById(R.id.musicisdowning3);
				itemHolder3.mdownloaded = (ImageView) view.findViewById(R.id.dowloaded3);
				itemHolder3.content = (TextView) view.findViewById(R.id.listitem_cate_name_Start3);
				itemHolder3.listitemvideosdc = (TextView) view.findViewById(R.id.listitemvideosdc3);
				itemHolder3.videolayout = (RelativeLayout) view.findViewById(R.id.videolandthreelayout3);
				holder.cateHolders.add(itemHolder1);
				holder.cateHolders.add(itemHolder2);
				holder.cateHolders.add(itemHolder3);
				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_VIDEOHEADLIST:
				view = inflater.inflate(R.layout.listitem_videolandcate, null);
				holder.layoutType = item.layoutType;
				CategoryItemHolder itemHolder4 = new CategoryItemHolder();
				itemHolder4.content = (TextView) view.findViewById(R.id.mylistitem_cate_name_left);
				itemHolder4.icon = (ImageView) view.findViewById(R.id.mymusicrankpic);
				itemHolder4.layout = view.findViewById(R.id.mymusicranklandlayout);
				itemHolder4.text_pay = (TextView) view.findViewById(R.id.money_text);
				itemHolder4.listitemvideosdc = (TextView) view.findViewById(R.id.mylistitemvideosdc);
				CategoryItemHolder itemHolder5 = new CategoryItemHolder();
				itemHolder5.icon = (ImageView) view.findViewById(R.id.mymusicrankpic2);
				itemHolder5.layout = view.findViewById(R.id.mymusicranklandlayout2);
				itemHolder5.content = (TextView) view.findViewById(R.id.mylistitem_cate_name_left2);
				itemHolder5.listitemvideosdc = (TextView) view.findViewById(R.id.mylistitemvideosdc2);
				itemHolder5.text_pay = (TextView) view.findViewById(R.id.money_text2);
				holder.cateHoldershead.add(itemHolder4);
				holder.cateHoldershead.add(itemHolder5);
				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_LOADING:
				view = inflater.inflate(R.layout.listitem_videoloading, null);
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
			case BigItem.Type.LAYOUT_VIDEOCAEGORYLIST:
				Log.d("rmyzx", "BigItem.Type.LAYOUT_VIDEOCAEGORYLIST");
				ArrayList<CategoryItemHolder> cateHolders = holder.cateHolders;
				if (item.videoItems.size() < 2) {
					cateHolders.get(1).videolayout.setVisibility(View.INVISIBLE);
					cateHolders.get(2).videolayout.setVisibility(View.INVISIBLE);
				} else if (item.videoItems.size() < 3) {
					cateHolders.get(1).videolayout.setVisibility(View.VISIBLE);
					cateHolders.get(2).videolayout.setVisibility(View.INVISIBLE);
				} else {
					cateHolders.get(1).videolayout.setVisibility(View.VISIBLE);
					cateHolders.get(2).videolayout.setVisibility(View.VISIBLE);
				}
				for (int i = 0; i < item.videoItems.size() && i < cateHolders.size(); i++) {

					final VideoItem cateItem = item.videoItems.get(i);

					
					if(cateItem.state==2){
	                 	  cateHolders.get(i).mprobar.setVisibility(View.GONE);
	                 	  cateHolders.get(i).mimagedowload.setVisibility(View.GONE);
	                 	 cateHolders.get(i).mdownloaded.setVisibility(View.VISIBLE);
	                 	 cateHolders.get(i).mdownloaded.setImageResource(R.drawable.list_icn_dld_ok);
	                    }else if(cateItem.state==0){
	                 	   cateHolders.get(i).mimagedowload.setVisibility(View.VISIBLE);
	                 	   cateHolders.get(i).mprobar.setVisibility(View.GONE);
	                 	  cateHolders.get(i).mdownloaded.setVisibility(View.GONE);
	                    }else{
	                 	   cateHolders.get(i).mimagedowload.setVisibility(View.GONE);
	                 	   cateHolders.get(i).mprobar.setVisibility(View.VISIBLE);
	                 	  cateHolders.get(i).mdownloaded.setVisibility(View.GONE);
	                    } 
					
					//if (cateItem.cDesc != null) {
						cateHolders.get(i).listitemvideosdc.setText(cateItem.playsum);
						cateHolders.get(i).content.setText(cateItem.name);
					//}
					if (TextUtils.isEmpty(cateItem.ImagePath)) {
						cateHolders.get(i).icon.setImageResource(R.drawable.videodefaultbg);
					} else if (!imageLoader.getPause().get()) {
						imageLoader.displayImage(cateItem.ImagePath, cateHolders.get(i).icon, mOptions);
					}
					if(type == 1){
						cateHolders.get(i).mimagedowload.setVisibility(View.GONE);
						 cateHolders.get(i).mprobar.setVisibility(View.GONE);
					}
					cateHolders.get(i).layout.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							if(type == 1){
								//跳转 详情页
								Intent intent = new Intent(Constants.TOAVLIST);
								intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
								
								cateItem.setFEEID_WEEK(FEEID_WEEK);
								cateItem.setFEEID_2MONTHS(FEEID_2MONTHS);
								cateItem.setFEEID_YEAR(FEEID_YEAR);
								cateItem.setPRICE_WEEK(PRICE_WEEK);
								cateItem.setPRICE_2MONTHS(PRICE_2MONTHS);
								cateItem.setPRICE_YEAR(PRICE_YEAR);
								intent.putExtra("app", cateItem);
								startActivity(intent);
								getActivity().overridePendingTransition(R.anim.push_left_in,
										R.anim.push_left_out);
								//-----------
							}else{
								player(cateItem);
							}
						}
					});
					//下载
					cateHolders.get(i).mimagedowload.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							if(!PlayDownloadService.isVideodownedFile(cateItem)){
								if(isPay){
									Intent intent = new Intent(
											LiveFragment.this
													.getActivity(),
											PlayDownloadService.class);
									intent.putExtra(
											PlayDownloadItem.DOWN_ITEM,
											cateItem);
									mhandHandle.removeMessages(0);
									mhandHandle
											.sendEmptyMessageDelayed(0,
													1000);
									LiveFragment.this
											.getActivity()
											.startService(intent);
									}else{
										PayUtile.showPay(LiveFragment.this.getActivity(),
												String.valueOf(PRICE_WEEK),String.valueOf(PRICE_2MONTHS),
												String.valueOf(PRICE_YEAR),new com.byt.market.view.MyMoneyDailog.PayBack() {

											@Override
											public void Resout(int arg0, String propname) {
												String type = "";
												if(PRICE_WEEK==arg0){
													type = "week";
												}else if(PRICE_2MONTHS==arg0){
													type = "month";
												}else if(PRICE_YEAR==arg0){
													type = "year";
												}
												pay(type, arg0, propname);
											}
										});
									}
							}else{
								Toast.makeText(
										LiveFragment.this
												.getActivity(),
										R.string.download_allready,
										Toast.LENGTH_SHORT)
										.show();
								return;
							}
						}
					});
				}
				/* } */
				break;
			case BigItem.Type.LAYOUT_VIDEOHEADLIST:
				Log.d("rmyzx", "BigItem.Type.LAYOUT_VIDEOHEADLIST");
				final ArrayList<CategoryItemHolder> cateHoldershead = holder.cateHoldershead;
				if (item.videoHeadItems.size() < 2) {
					cateHoldershead.get(1).icon.setVisibility(View.INVISIBLE);
					cateHoldershead.get(1).layout.setVisibility(View.INVISIBLE);
					cateHoldershead.get(1).content.setVisibility(View.INVISIBLE);
					cateHoldershead.get(1).listitemvideosdc.setVisibility(View.INVISIBLE);
				} else {
					cateHoldershead.get(1).icon.setVisibility(View.VISIBLE);
					cateHoldershead.get(1).layout.setVisibility(View.VISIBLE);
					cateHoldershead.get(1).content.setVisibility(View.VISIBLE);
					cateHoldershead.get(1).listitemvideosdc.setVisibility(View.VISIBLE);
				}
				for (int i = 0; i < item.videoHeadItems.size() && i < cateHoldershead.size(); i++) {

					final VideoItem cateItem = item.videoHeadItems.get(i);
					
					//--------------------by bobo
                    if(cateItem.ispay>0){
    //                	paytext = cateHoldershead.get(i).text_pay;
//                    	mpayMoney = cateItem.ispay;
//                    	LogCart.Log("价格--v---"+cateItem.ispay);
                    	String price=String.format(MyApplication.getInstance().getResources().getString(R.string.money), cateItem.ispay);
                    	cateHoldershead.get(i).text_pay.setText( price);
                    	paytext = cateHoldershead.get(i).text_pay;
                    	new Thread(){
                    		@Override
                    		public void run() {
//                    			// TODO Auto-generated method stub
//                    			super.run();
//                    			nCheckResult0 = MainActivity.jsxltj.JsXltj_CheckFeeState(usname, 3000, 0, priodid, feeid);
//                    			Message msg = songleHandler.obtainMessage();
//                    			msg.what = 3;
//                    			if(nCheckResult0 == 0){
//                    				msg.arg1 = 1;
//                    			}else{
//                    				msg.arg1 = 2;
//                    			}
//                    			songleHandler.sendMessage(msg);
                    		}
                    	}.start();
//                    	if(nCheckResult0!=0){
//                    		cateHoldershead.get(i).text_pay.setVisibility(View.VISIBLE);
//                        	String price=String.format(MyApplication.getInstance().getResources().getString(R.string.money), cateItem.ispay);
//                        	cateHoldershead.get(i).text_pay.setText( price);
//                    	}
					}else{
						cateHoldershead.get(i).text_pay.setVisibility(View.GONE);
					}
                    //----------bybobo-----------
					
					if (cateItem.cDesc != null) {
						cateHoldershead.get(i).listitemvideosdc.setText(cateItem.cDesc);
						cateHoldershead.get(i).content.setText(cateItem.cTitle);
					}
					if (TextUtils.isEmpty(cateItem.ImagePath)) {
						cateHoldershead.get(i).icon.setImageResource(R.drawable.videodefaultbg);
					} else if (!imageLoader.getPause().get()) {
						imageLoader.displayImage(cateItem.ImagePath, cateHoldershead.get(i).icon, mOptions);
					}
					cateHoldershead.get(i).layout.setId(i);
					cateHoldershead.get(i).layout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
//							int id = v.getId();
//							if(cateHoldershead.get(id).text_pay.isShown()){
//								StatService.trackCustomBeginEvent(LiveFragment.this.getActivity(), "payment_success", "");
//								StatService.trackCustomEvent(LiveFragment.this.getActivity(),"payment", "");
//								mcateItem = cateItem;
//								nCheckResult0 = MainActivity.jsxltj.JsXltj_CheckFeeState(usname, 3000, 0, cateItem.sid, cateItem.sid);
//								//LogCart.Log("-----单项检测结果-----"+nCheckResult0);
//								if(nCheckResult0!=0){
//								//	LogCart.Log("-----sid------sid------"+cateItem.sid);
//									pay(cateItem.sid,PAYSing);
//								}else{
//									player(cateItem);
//								}
//							}else{
//								if (type == 1 && isPayTemp) {
//									return;
//								}
//								player(cateItem);
//							}
							
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
	
	/**
	 * 
	 * bobo
	 * 播放 
	 */
	
	private void player(VideoItem cateItem){
		
		if (type == 1 && isPayTemp) {
			return;
		}
		try {
			Log.i("xxx", "cccccccccccc==>" + cateItem.webURL + " title==>" + cateItem.isshare);
			if (cateItem.webURL != null && cateItem.webURL.trim().length() > 0 & cateItem.isshare != 1) {
				Intent intent = new Intent();
				
				if(cateItem.webURL.contains("youtube"))
				{
					intent.setClass(LiveFragment.this.getActivity(),PlayWebVideoActivity.class);
				}
				else
				{
					String Stype = "AV";
					//判断是否已经下载
					if(PlayDownloadService.isVideodownedFile(cateItem)){
						cateItem.webURL = PlayDownloadService.mVdiaoPath;
						Stype = "TV";
					}
					StartVidioutil.startVidiao(LiveFragment.this.getActivity(),cateItem.webURL ,Stype);
					return;
				}
				intent.putExtra("videoUrl", cateItem.webURL);
				LiveFragment.this.getActivity().startActivity(intent);
			} else {
				Intent intent = new Intent();
				
				if(cateItem.webURL.contains("youtube"))
				{
					intent.setClass(LiveFragment.this.getActivity(),PlayActivity.class);
				}
				else
				{
					String Stype = "AV";
					//判断是否已经下载
					if(PlayDownloadService.isVideodownedFile(cateItem)){
						cateItem.webURL = PlayDownloadService.mVdiaoPath;
						Stype = "TV";
					}
					StartVidioutil.startVidiao(LiveFragment.this.getActivity(),cateItem.webURL ,Stype);
					return;
				}
				
				LiveFragment.this.getActivity().startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	/**
	 * 2g 网络 使用此Adapter,主要是List样式
	 * 
	 * @author z
	 * 
	 */
	private class CateAdapter extends ImageAdapter {

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_CAEGORYLIST:
				view = inflater.inflate(R.layout.listitem_cate_list, null);
				holder.layoutType = item.layoutType;
				CategoryItemHolder itemHolder1 = new CategoryItemHolder();
				itemHolder1.icon = (ImageView) view.findViewById(R.id.cate_icon);
				itemHolder1.name = (TextView) view.findViewById(R.id.cate_title);
				itemHolder1.layout = view.findViewById(R.id.cate_item_layout);
				itemHolder1.content = (TextView) view.findViewById(R.id.cate_content);
				CategoryItemHolder itemHolder2 = new CategoryItemHolder();
				itemHolder2.icon = (ImageView) view.findViewById(R.id.cate_icon1);
				itemHolder2.name = (TextView) view.findViewById(R.id.cate_title1);
				itemHolder2.layout = view.findViewById(R.id.cate_item_layout1);
				itemHolder2.line = view.findViewById(R.id.listitem_rank_line2);
				itemHolder2.content = (TextView) view.findViewById(R.id.cate_content1);
				holder.cateHolders.add(itemHolder1);
				holder.cateHolders.add(itemHolder2);
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
			case BigItem.Type.LAYOUT_CAEGORYLIST:
				ArrayList<CategoryItemHolder> cateHolders = holder.cateHolders;
				if (item.cateItems.size() < 2) {
					cateHolders.get(1).layout.setVisibility(View.INVISIBLE);
					cateHolders.get(1).line.setVisibility(View.INVISIBLE);
				}
				for (int i = 0; i < item.cateItems.size() && i < cateHolders.size(); i++) {
					final CateItem cateItem = item.cateItems.get(i);
					cateHolders.get(i).name.setText(cateItem.cTitle);
					if (TextUtils.isEmpty(cateItem.ImagePath)) {
						cateHolders.get(i).icon.setImageResource(R.drawable.app_empty_icon);
					} else {
						if (!cateItem.ImagePath.startsWith("http://")) {
							// mImageFetcher.loadImage(cateItem.ImagePath,
							// cateHolders.get(i).icon);
							imageLoader.displayImage(cateItem.ImagePath, cateHolders.get(i).icon, mOptions);
						} else {
							// mImageFetcher.loadImage(cateItem.ImagePath,
							// cateHolders.get(i).icon);
							imageLoader.displayImage(cateItem.ImagePath, cateHolders.get(i).icon, mOptions);
						}
					}
					/* bestone add by zengxiao for :添加分类详情介绍 */
					cateHolders.get(i).content.setText(cateItem.cDesc);
					/* add end */
					cateHolders.get(i).layout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							onAppClick(cateItem, Constants.TOLIST);
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
	//---------------------bobo-------------
		public void pay(String type,int price,String productName){
			StatService.trackCustomBeginEvent(LiveFragment.this.getActivity(), "payment_success", "");
			StatService.trackCustomEvent(LiveFragment.this.getActivity(),"payment", "");
			int feid = 0 ;
			if(!MyApplication.getInstance().getBulepayInit()){
				Toast.makeText(getActivity(), getActivity().getString(R.string.initerroe), Toast.LENGTH_LONG).show();
			}else{
				try {
					if(type.equals("week")){
						MobclickAgent.onEvent(getActivity(), "AVweekpay");
						StatService.trackCustomEvent(getActivity(), "AVweekpay", "");
						feid = FEEID_WEEK;
					}else if(type.equals("month")){
						MobclickAgent.onEvent(getActivity(), "AV2Mothpay");
						StatService.trackCustomEvent(getActivity(), "AV2Mothpay", "");
						feid = FEEID_2MONTHS;
					}else{
						MobclickAgent.onEvent(getActivity(), "AVYEAR");
						StatService.trackCustomEvent(getActivity(), "AVYEAR", "");
						feid = FEEID_YEAR;
					}
					
					String transID = BluePayUtil.getTransationId(feid, MyApplication.getInstance().getUser().getUid());
					RapitUtile.setTransID(transID);
					
					PayUtile.pay(LiveFragment.this.getActivity(), transID, 
							MyApplication.getInstance().getUser().getUid(), 
							String.valueOf(price),productName,feid,new Ipayback());
				} catch (Exception e) {
					Toast.makeText(LiveFragment.this.getActivity(), getActivity().getString(R.string.payerroe),Toast.LENGTH_LONG).show();
				}
			}
		}
		public class Ipayback implements com.byt.market.view.MyPayDailog.PayBack{

			@Override
			public void Resout(BlueMessage bluemsg) {
				// TODO Auto-generated method stub
					if(!bluemsg.getPublisher().equals("line")){
						if(bluemsg.getCode() == 200){
//							sellBottom.setVisibility(View.GONE);
//			    			isPayTemp=false;
							isPay = true;
			    			mAdapter.notifyDataSetChanged();
			    			MobclickAgent.onEvent(getActivity(), "AVpaySucceed");
						}else{
							BluePayUtil.showErroe(LiveFragment.this.getActivity(), bluemsg.getCode());
						}
					}else{
						
					}
			}

			@Override
			public void bankRsout(int re) {
				// TODO Auto-generated method stub
				if(re==1){
					isPay = true;
	    			mAdapter.notifyDataSetChanged();
	    			MobclickAgent.onEvent(getActivity(), "AVpaySucceed");
				}
			}
			
		}
	@Override
	protected void onDownloadStateChanged() {

	}

	@Override
	public void onAppClick(Object obj, String action) {
		Intent intent = new Intent(action);
		Log.i("xxx", "obj==>" + obj);
		if (obj instanceof CateItem) {
			CateItem caItem = (CateItem) obj;
			intent.putExtra("app", caItem);
		}
		intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStyle(CusPullListView listview2) {
		// TODO Auto-generated method stub
		super.setStyle(listview2);
		/*
		 * View
		 * view=LayoutInflater.from(getActivity()).inflate(R.layout.videoland_header
		 * , null); listview2.addHeaderView(view);
		 */}

	// 修改提示语
	@Override
	public void setButtonInvi() {
		super.setButtonInvi();
		loadfailed.setText(MyApplication.getInstance().getString(R.string.nodata));
		loadfailed.setButtonVisible(View.GONE);
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		super.onPostExecute(bytes);
	}
	
	
	@Override
	public void onDestroy() {
		handler.removeMessages(1003);
		handler.removeMessages(1002);
		handler.removeMessages(1001);
		super.onDestroy();
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
