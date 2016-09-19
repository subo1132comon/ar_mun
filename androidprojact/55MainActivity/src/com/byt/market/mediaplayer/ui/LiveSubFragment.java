package com.byt.market.mediaplayer.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
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
import com.byt.market.view.MyMoneyDailog.PayBack;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

/**
 * 分类列表
 * 
 * @author qiuximing
 * 
 */
public class LiveSubFragment extends ListViewFragment {
	
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
	//private boolean canpay = false;
	private int price_day = 0;
	private int feeid = 0;
	private int fee_day_id = 0;
	private int priodid = 0;
	private int nCheckResult0 = -20;
	private int mleve = 0;
	//private String mname = null;
	//public static jsxltjsrv jsxltj = new jsxltjsrv();
	private boolean isPay = false; 
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
		String keyword = "";
		if(mcateItem.getName()!=null){
			try {
				keyword = URLEncoder.encode(mcateItem.getName(), "utf8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Constants.APK_URL+"Video/v1.php?qt=videolist3&cid=1&pi="
		+getPageInfo().getNextPageIndex()+"&ps=20&cat="+keyword+"&stype="+MainActivity.AV_KEY;
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
			//if (type == 0) {
//				getbigs = JsonParse.parseCateVideoList(result.getJSONObject("data"), bigitem);
			//	getbigs = JsonParse.parseLiveVideotwoList(result, bigitem);
			//} else {
				getbigs = JsonParse.parseCateVideotwoList(result, bigitem);
			//}
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
		LiveSubFragment.this.getActivity().registerReceiver(new LinePayBrodcast(), new IntentFilter("com.linepay"));
		//mname = getArguments().getString("avname");
		mcateItem = getArguments().getParcelable("avapp");
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
	//------------------bobo
	  private Handler myhandler = new Handler(){
	    	public void handleMessage(Message msg) {
	    		if(msg.what == 1){
	    			isPay = true;
	    			//RapitUtile.setTpay(true);
	    			mAdapter.notifyDataSetChanged();
	    		}
	    	};
	    };
	    //LINEPay 支付完逻辑
	    private class LinePayBrodcast extends BroadcastReceiver{

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				if("com.linepay".equals(arg1.getAction())){
	    			//RapitUtile.setTpay(true);
	    			isPay = true;
	    			mAdapter.notifyDataSetChanged();
	    			MobclickAgent.onEvent(getActivity(), "AVpaySucceed");
				}
			}
	    	
	    }
	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);
		//----add  by  bobo
		//usname = MyApplication.getInstance().getUser().getNickname();
		//LogCart.Log("用户名-----"+usname);
		
		
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
										Toast.makeText(LiveSubFragment.this.getActivity(), getActivity().getString(R.string.network_errors), Toast.LENGTH_LONG).show();
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
							
						}
					});
			}
		return view;
	}
	
	public class Ipayback implements com.byt.market.view.MyPayDailog.PayBack{

		@Override
		public void Resout(BlueMessage bluemsg) {
			// TODO Auto-generated method stub
				if(!bluemsg.getPublisher().equals("line")){
					if(bluemsg.getCode() == 200){
//						sellBottom.setVisibility(View.GONE);
//		    			isPayTemp=false;
						isPay = true;
		    			mAdapter.notifyDataSetChanged();
		    			MobclickAgent.onEvent(getActivity(), "AVpaySucceed");
					}else{
						BluePayUtil.showErroe(LiveSubFragment.this.getActivity(), bluemsg.getCode());
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
					
					if (cateItem.cDesc != null) {
						cateHolders.get(i).listitemvideosdc.setText(cateItem.playsum);
						cateHolders.get(i).content.setText(cateItem.cTitle);
					}
					if (TextUtils.isEmpty(cateItem.ImagePath)) {
						cateHolders.get(i).icon.setImageResource(R.drawable.videodefaultbg);
					} else if (!imageLoader.getPause().get()) {
						imageLoader.displayImage(cateItem.ImagePath, cateHolders.get(i).icon, mOptions);
					}

					cateHolders.get(i).layout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							//vip区如果支付了 或者下载过的 直接可看
							if(isPay||PlayDownloadService.isVideodownedFile(cateItem)){
								player(cateItem);
							}else{
								//String price_days=String.format(MyApplication.getInstance().getResources().getString(R.string.money),"10");
								//String price_math = String.format(MyApplication.getInstance().getResources().getString(R.string.money), "100");
								//String price_year = String.format(MyApplication.getInstance().getResources().getString(R.string.money), "500");
								PayUtile.showPay(LiveSubFragment.this.getActivity(),
										String.valueOf(mcateItem.getPRICE_WEEK()),String.valueOf(mcateItem.getPRICE_2MONTHS()),
										String.valueOf(mcateItem.getPRICE_YEAR()),new PayBack() {

									@Override
									public void Resout(int arg0, String propname) {
										// TODO Auto-generated method stub
										// TODO Auto-generated method stub
										//Toast.makeText(LiveSubFragment.this.getActivity(), "jjjjjjjj"+arg0, Toast.LENGTH_LONG).show();
										String type = "";
										if(mcateItem.PRICE_WEEK==arg0){
											type = "week";
										}else if(mcateItem.PRICE_2MONTHS==arg0){
											type = "month";
										}else if(mcateItem.PRICE_YEAR==arg0){
											type = "year";
										}
										pay(type, arg0, propname);
									}
								});
							}
						}
					});
					
					//下载-------
					cateHolders.get(i).mimagedowload.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							if(!PlayDownloadService.isVideodownedFile(cateItem)){
								if(isPay){
									Intent intent = new Intent(
											LiveSubFragment.this
													.getActivity(),
											PlayDownloadService.class);
									intent.putExtra(
											PlayDownloadItem.DOWN_ITEM,
											cateItem);
									mhandHandle.removeMessages(0);
									mhandHandle
											.sendEmptyMessageDelayed(0,
													1000);
									LiveSubFragment.this
											.getActivity()
											.startService(intent);
									}else{
										
										PayUtile.showPay(LiveSubFragment.this.getActivity(),
												String.valueOf(mcateItem.getPRICE_WEEK()),String.valueOf(mcateItem.getPRICE_2MONTHS()),
												String.valueOf(mcateItem.getPRICE_YEAR()),new PayBack() {

											@Override
											public void Resout(int arg0, String propname) {
												// TODO Auto-generated method stub
												// TODO Auto-generated method stub
												//Toast.makeText(LiveSubFragment.this.getActivity(), "jjjjjjjj"+arg0, Toast.LENGTH_LONG).show();
												String type = "";
												if(mcateItem.PRICE_WEEK==arg0){
													type = "week";
												}else if(mcateItem.PRICE_2MONTHS==arg0){
													type = "month";
												}else if(mcateItem.PRICE_YEAR==arg0){
													type = "year";
												}
												pay(type, arg0, propname);
											}
										});
									}
							}else{
								Toast.makeText(
										LiveSubFragment.this
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
					intent.setClass(LiveSubFragment.this.getActivity(),PlayWebVideoActivity.class);
				}
				else
				{
					String Stype = "AV";
					//判断是否已经下载
					if(PlayDownloadService.isVideodownedFile(cateItem)){
						cateItem.webURL = PlayDownloadService.mVdiaoPath;
						Stype = "TV";
					}
					StartVidioutil.startVidiao(LiveSubFragment.this.getActivity(),cateItem.webURL ,Stype);
//					DownLoadVdioapkTools dt = new DownLoadVdioapkTools(LiveFragment.this.getActivity());
//					if(dt.checkApkExist(LiveFragment.this.getActivity(), "com.tyb.fun.palyer")){
//						dt.startAPP(cateItem.webURL);
//					}else{
//						Singinstents.getInstents().setVdiouri(cateItem.webURL);
//						Singinstents.getInstents().setAppPackageName("com.tyb.fun.palyer");
//						dt.showNoticeDialog();
//					}
					return;
				}
				intent.putExtra("videoUrl", cateItem.webURL);
				LiveSubFragment.this.getActivity().startActivity(intent);
			} else {
				Intent intent = new Intent();
				
				if(cateItem.webURL.contains("youtube"))
				{
					intent.setClass(LiveSubFragment.this.getActivity(),PlayActivity.class);
				}
				else
				{
					String Stype = "AV";
					//判断是否已经下载
					if(PlayDownloadService.isVideodownedFile(cateItem)){
						cateItem.webURL = PlayDownloadService.mVdiaoPath;
						Stype = "TV";
					}
					StartVidioutil.startVidiao(LiveSubFragment.this.getActivity(),cateItem.webURL ,Stype);
//					DownLoadVdioapkTools dt = new DownLoadVdioapkTools(LiveFragment.this.getActivity());
//					if(dt.checkApkExist(LiveFragment.this.getActivity(), "com.tyb.fun.palyer")){
//						dt.startAPP(cateItem.webURL);
//					}else{
//						Singinstents.getInstents().setVdiouri(cateItem.webURL);
//						Singinstents.getInstents().setAppPackageName("com.tyb.fun.palyer");
//						dt.showNoticeDialog();
//					}
					return;
				}
				
//				if (PlayDownloadService.isVideodownedFile(cateItem)) {
//					intent.putExtra("sendlocalstring",
//							PlayDownloadItem.VIDEO_DIR + cateItem.cTitle + cateItem.videuri.substring(cateItem.videuri.lastIndexOf(".")));
//				} else {
//					intent.putExtra("sendstring", cateItem);
//				}
//				if (cateItem.webURL != null && cateItem.webURL.trim().length() > 0) {
//					intent.putExtra("videoUrl", cateItem.webURL);
//				}
				LiveSubFragment.this.getActivity().startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
//---------------------bobo-------------
	public void pay(String type,int price,String productName){
		StatService.trackCustomBeginEvent(LiveSubFragment.this.getActivity(), "payment_success", "");
		StatService.trackCustomEvent(LiveSubFragment.this.getActivity(),"payment", "");
		int feid = 0 ;
		if(!MyApplication.getInstance().getBulepayInit()){
			Toast.makeText(getActivity(), getActivity().getString(R.string.initerroe), Toast.LENGTH_LONG).show();
		}else{
			try {
				//Log.d("nnlog", "mprice--"+mprice);
				if(type.equals("week")){
					MobclickAgent.onEvent(getActivity(), "AVweekpay");
					StatService.trackCustomEvent(getActivity(), "AVweekpay", "");
					feid = mcateItem.getFEEID_WEEK();
				}else if(type.equals("month")){
					MobclickAgent.onEvent(getActivity(), "AV2Mothpay");
					StatService.trackCustomEvent(getActivity(), "AV2Mothpay", "");
					feid = mcateItem.getFEEID_2MONTHS();
				}else{
					MobclickAgent.onEvent(getActivity(), "AVYEAR");
					StatService.trackCustomEvent(getActivity(), "AVYEAR", "");
					feid = mcateItem.getFEEID_YEAR();
				}
//				BluePay.getInstance().payBySMS(LiveFragment.this.getActivity(),
//						BluePayUtil.getTransationId(mfeeid,MyApplication.getInstance().getUser().getUid()), 
//						Config.K_CURRENCY_THB,String.valueOf(mprice*100), 0, "propsName", true, new Ipayback());
				
				String transID = BluePayUtil.getTransationId(feid, MyApplication.getInstance().getUser().getUid());
				RapitUtile.setTransID(transID);
//				BluePay.getInstance().payByUI(LiveFragment.this.getActivity(), transID,
//						Config.K_CURRENCY_TRF, MyApplication.getInstance().getUser().getUid(), 
//						String.valueOf(mprice*100), mproductName, 0, "mun://com.mun.pay", new Ipayback());
//				
				//
				PayUtile.pay(LiveSubFragment.this.getActivity(), transID, 
						MyApplication.getInstance().getUser().getUid(), 
						String.valueOf(price),productName,feid,new Ipayback());
			} catch (Exception e) {
				Toast.makeText(LiveSubFragment.this.getActivity(), getActivity().getString(R.string.payerroe),Toast.LENGTH_LONG).show();
			}
		}
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
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
