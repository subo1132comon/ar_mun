package com.byt.market.mediaplayer.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
import com.bluepay.pay.PublisherCode;
import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
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
import com.byt.market.mediaplayer.ui.LiveSubFragment.Ipayback;
import com.byt.market.tools.Dailog;
import com.byt.market.tools.DownLoadVdioapkTools;
import com.byt.market.tools.LogCart;
import com.byt.market.tools.Toolst;
import com.byt.market.ui.CateListViewFragment;
import com.byt.market.ui.HomeFragment;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.BluePayUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.PayUtile;
import com.byt.market.util.RapitUtile;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.Singinstents;
import com.byt.market.util.StartVidioutil;
import com.byt.market.util.Util;
import com.byt.market.util.NotifaHttpUtil.NotifaHttpResalout;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.MyMoneyDailog.PayBack;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

/**
 * 分类列表
 * @author qiuximing
 *
 */
public class MovieFragment extends ListViewFragment {
	
	private static final int BAO_MATH = 123;
	private static final int BAO_DAY = 0124;
	private int mleve = 0;
	String netType;
	private TextView musiclandtitle;
	private DisplayImageOptions mOptions;
	private int mid;
	private int type=1;
	private int price =0;
	private int price_day = 0;
	private int feeid = 0;
	private int fee_day_id = 0;
	private String usname = "";
	public boolean ispay = false;
	private int priodid = 0;
	private int FEEID_WEEK ;        
	private int FEEID_2MONTHS;
	private int FEEID_YEAR;
	private int PRICE_WEEK;
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
	private int PRICE_2MONTHS;
	private int PRICE_YEAR;
	private boolean canpay = false;
	
	public int getPriodid() {
		return priodid;
	}
	public void setPriodid(int priodid) {
		this.priodid = priodid;
	}
	int nCheckResult0 = -20;
	public int getFee_day_id() {
		return fee_day_id;
	}
	public void setFee_day_id(int fee_day_id) {
		this.fee_day_id = fee_day_id;
	}
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
	private boolean isfirst=true;
	
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
		String uri="";
		if(type==2)
		{
			String count = "9";
			if(mleve>1){
				count = "18";
			}
			uri = Constants.APK_URL+"Video/v1.php?qt=Videolist2&cid=52&pi="
		+getPageInfo().getNextPageIndex()+"&ps="+count+ "&uid=" 
					+ SharedPrefManager.getLastLoginUserName(getActivity())+"&stype="+MainActivity.MVOE_KEY;
		}else{
			uri=Constants.APK_URL+"Video/v1.php?qt=videolist2&cid=60"+"&pi="
					+ getPageInfo().getNextPageIndex() + "&ps=9"+"&stype="+MainActivity.MVOE_KEY;
		}
    	return uri;
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
    public void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	MobclickAgent.onPageStart("电影");
    }
    
    @Override
    public void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	MobclickAgent.onPageEnd("电影");
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    }
    @Override
    protected int getLayoutResId() {
        return R.layout.listview;
    }
    @Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	netType = Util.getNetAvialbleType(MyApplication.getInstance());
    	if(!TextUtils.isEmpty(netType)&&"wifi".equals(netType)){
//    		mImageFetcher.setLoadingImage(R.drawable.category_loading);
    	}else{
//    		mImageFetcher.setLoadingImage(R.drawable.app_empty_icon);
    	}
    	IntentFilter inf = new IntentFilter();
    	inf.addAction("COM.AR.PAY");
    	inf.addAction("com.byt.music.downcomplet");
    	getActivity().registerReceiver(downreBroadcastReceiver,
				inf);
    	mOptions = new DisplayImageOptions.Builder()
		.cacheOnDisc().build();
    /*	mOptions = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.videodefaultbg)
		.showImageForEmptyUri(R.drawable.videodefaultbg)
		.cacheOnDisc()
		.displayer(new ZoomAndRounderBitmapDisplayer((int) ((outMetrics.widthPixels - 15 * outMetrics.density) / 2))).build();*/
    }
    private View sellBottom;
    
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
				if(arg1.getAction().equals("COM.AR.PAY")){
					ispay = true;
	    			mAdapter.notifyDataSetChanged();
				}else if("com.byt.music.downcomplet".equals(arg1.getAction())){
					mhandHandle.removeMessages(0);
					mhandHandle.sendEmptyMessage(0);
				}
		}
	};
    
    @Override
	protected List<BigItem> parseListData(JSONObject result) { 
		
		 try {
			 List<BigItem> getbigs=null;
	    	BigItem bigitem = null;
	    	if(mAdapter!=null&&mAdapter.getCount()>0)
	    	{
	    		 bigitem=mAdapter.getItem(mAdapter.getCount()-2);
	    	}
	    	if(type==2)
			{
	    		//getbigs=JsonParse.parseCateVideoList(result.getJSONObject("data"),bigitem);
	    		//getbigs =JsonParse.parseLiveVideotwoList(result, bigitem);
	    		getbigs=JsonParse.parseCateVideotwoList(result,bigitem);
			}else{
				getbigs=JsonParse.parseCateVideotwoList(result,bigitem);
			}
	    	if (getbigs != null) {
				PlayDownloadService.isVideoStatdownedFile(getbigs);//初始化 下载状态
			}
				return getbigs;
			} catch (Exception e) {
				e.printStackTrace();
			}
		 return null;
	}
    
	
	
	//------------------bobo
	  private Handler myhandler = new Handler(){
	    	public void handleMessage(Message msg) {
	    		if(msg.what == 1){
	    			ispay = true;
	    			mAdapter.notifyDataSetChanged();
	    		}
	    	};
	    };
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState, int res) {
        View view = inflater.inflate(res, container, false);
		usname = MyApplication.getInstance().getUser().getNickname();
		mleve = MyApplication.getInstance().getUser().getUlevel();
	//	if (type == 1) {
			if(mleve>=3){
				Message msg = myhandler.obtainMessage();
				msg.what = 1;
				myhandler.sendMessage(msg);
			}else{
				BluePayUtil.qureyAVResoult(MyApplication.getInstance().getUser().getUid(), new NotifaHttpResalout() {
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
											myhandler.sendMessage(msg);
										}
									}
								}else{
									Toast.makeText(MovieFragment.this.getActivity(), getActivity().getString(R.string.network_errors), Toast.LENGTH_LONG).show();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
					}
				});
			}
		//}
        return view;
    }

    // edit by wangxin
    @Override
    protected ImageAdapter createAdapter() {
    	String netType = Util.getNetAvialbleType(MyApplication.getInstance());
    	if(!TextUtils.isEmpty(netType)){
//    		if("wifi".equals(netType)){
//    			return new WifiCateAdapter();
//    		}
    	}
        return new WifiCateAdapter();
    }
    
    
    private class WifiCateAdapter extends ImageAdapter{


		@Override
        public View newView(LayoutInflater inflater, BigItem item) {
            View view = null;
            BigHolder holder = new BigHolder();
            switch(item.layoutType){
                case BigItem.Type.LAYOUT_VIDEOCAEGORYLIST:
                    view = inflater.inflate(R.layout.listitem_videolandthreecate, null);
                    holder.layoutType = item.layoutType;
                    CategoryItemHolder itemHolder1 = new CategoryItemHolder();
                    itemHolder1.content=(TextView) view.findViewById(R.id.listitem_cate_name_Start);
                    itemHolder1.icon = (ImageView) view.findViewById(R.id.musicrankpic);  
                  //  itemHolder1.text_pay = (TextView) view.findViewById(R.id.price_text1);
                    itemHolder1.layout = view.findViewById(R.id.musicranklandlayout);
                    itemHolder1.videolayout=(RelativeLayout) view.findViewById(R.id.videolandthreelayout1);
                    itemHolder1.listitemvideosdc=(TextView) view.findViewById(R.id.listitemvideosdc);
                    itemHolder1.mimagedowload = (ImageView) view.findViewById(R.id.dowload_image1);
                    itemHolder1.mprobar = (ProgressBar) view.findViewById(R.id.musicisdowning1);
                    itemHolder1.mdownloaded = (ImageView) view.findViewById(R.id.dowloaded1);
                    CategoryItemHolder itemHolder2 = new CategoryItemHolder();
                    itemHolder2.icon = (ImageView) view.findViewById(R.id.musicrankpic2);
                    itemHolder2.layout = view.findViewById(R.id.musicranklandlayout2);
                    itemHolder2.mimagedowload = (ImageView) view.findViewById(R.id.dowload_image2);
                    itemHolder2.mprobar = (ProgressBar) view.findViewById(R.id.musicisdowning2);
                    itemHolder2.mdownloaded = (ImageView) view.findViewById(R.id.dowloaded2);
                 //   itemHolder2.text_pay = (TextView) view.findViewById(R.id.price_text2);
                    itemHolder2.content=(TextView) view.findViewById(R.id.listitem_cate_name_Start2); 
                    itemHolder2.listitemvideosdc=(TextView) view.findViewById(R.id.listitemvideosdc2);
                    itemHolder2.videolayout=(RelativeLayout) view.findViewById(R.id.videolandthreelayout2);
                    CategoryItemHolder itemHolder3 = new CategoryItemHolder();
                    itemHolder3.icon = (ImageView) view.findViewById(R.id.musicrankpic3);
                    itemHolder3.layout = view.findViewById(R.id.musicranklandlayout3);
                    itemHolder3.mimagedowload = (ImageView) view.findViewById(R.id.dowload_image3);
                    itemHolder3.mprobar = (ProgressBar) view.findViewById(R.id.musicisdowning3);
                    itemHolder3.mdownloaded = (ImageView) view.findViewById(R.id.dowloaded3);
               //     itemHolder3.text_pay = (TextView) view.findViewById(R.id.price_text3);
                    itemHolder3.content=(TextView) view.findViewById(R.id.listitem_cate_name_Start3); 
                    itemHolder3.listitemvideosdc=(TextView) view.findViewById(R.id.listitemvideosdc3);
                    itemHolder3.videolayout=(RelativeLayout) view.findViewById(R.id.videolandthreelayout3);
                    holder.cateHolders.add(itemHolder1);
                    holder.cateHolders.add(itemHolder2);
                    holder.cateHolders.add(itemHolder3);
                        view.setTag(holder);
                    break;
                case BigItem.Type.LAYOUT_VIDEOHEADLIST:
                	   view= inflater.inflate(R.layout.listitem_videolandcate, null);
                      holder.layoutType = item.layoutType;
                      CategoryItemHolder itemHolder4 = new CategoryItemHolder();
                      itemHolder4.content=(TextView) view.findViewById(R.id.mylistitem_cate_name_left);
                      itemHolder4.icon = (ImageView) view.findViewById(R.id.mymusicrankpic);    
                      itemHolder4.layout = view.findViewById(R.id.mymusicranklandlayout);
                      itemHolder4.listitemvideosdc=(TextView) view.findViewById(R.id.mylistitemvideosdc);
                      CategoryItemHolder itemHolder5 = new CategoryItemHolder();
                      itemHolder5.icon = (ImageView) view.findViewById(R.id.mymusicrankpic2);
                      itemHolder5.layout = view.findViewById(R.id.mymusicranklandlayout2);
                      itemHolder5.content=(TextView) view.findViewById(R.id.mylistitem_cate_name_left2); 
                      itemHolder5.listitemvideosdc=(TextView) view.findViewById(R.id.mylistitemvideosdc2);
                      view.findViewById(R.id.money_text2).setVisibility(View.GONE);
                      view.findViewById(R.id.money_text).setVisibility(View.GONE);
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
            switch (item.layoutType){
                case BigItem.Type.LAYOUT_VIDEOCAEGORYLIST:
                	Log.d("rmyzx", "BigItem.Type.LAYOUT_VIDEOCAEGORYLIST");
                    final ArrayList<CategoryItemHolder> cateHolders = holder.cateHolders;            
                    if(item.videoItems.size()<2)
                    {
                    	cateHolders.get(1).videolayout.setVisibility(View.INVISIBLE); 
                    	cateHolders.get(2).videolayout.setVisibility(View.INVISIBLE);
                    }else if(item.videoItems.size()<3){
                    	cateHolders.get(1).videolayout.setVisibility(View.VISIBLE); 
                    	cateHolders.get(2).videolayout.setVisibility(View.INVISIBLE); 
                    }else{
                    	cateHolders.get(1).videolayout.setVisibility(View.VISIBLE); 
                    	cateHolders.get(2).videolayout.setVisibility(View.VISIBLE); 
                    }
                    for (int i = 0; i < item.videoItems.size() && i < cateHolders.size(); i++) {
               
                        final VideoItem cateItem = (VideoItem) item.videoItems.get(i);
                        
                        //----------bybobo-----------
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
                       if(cateItem.cDesc!=null)
                       {   
                    	   cateHolders.get(i).listitemvideosdc.setText(cateItem.playsum);
                           cateHolders.get(i).content.setText(cateItem.cTitle);
                       }
                       if (TextUtils.isEmpty(cateItem.ImagePath)) {
                    	   cateHolders.get(i).icon.setImageResource(R.drawable.videodefaultbg);
                       }  else if(!imageLoader.getPause().get()){
                    	   imageLoader.displayImage(cateItem.ImagePath, cateHolders.get(i).icon, mOptions);   
                    	   }   
                       cateHolders.get(i).layout.setId(i);
                        cateHolders.get(i).layout.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if(type==1){
									//vip区如果支付了 或者下载过的 直接可看
									if(ispay||PlayDownloadService.isVideodownedFile(cateItem)){
	  									payMovie(cateItem);
	  								}else{
	  									PayUtile.showPay(MovieFragment.this.getActivity(), 
	  											String.valueOf(PRICE_WEEK), String.valueOf(PRICE_2MONTHS), String.valueOf(PRICE_YEAR), new PayBack(){

											@Override
											public void Resout(int arg0,
													String propname) {
												String type = "";
												if(PRICE_WEEK==arg0){
													type = "week";
												}else if(PRICE_2MONTHS==arg0){
													type = "month";
												}else if(PRICE_YEAR==arg0){
													type = "year";
												}
												pay(type, arg0, propname);
											}});
	  								}
								}else{
									payMovie(cateItem);
								}
							}
						});
                      //下载----------
                        cateHolders.get(i).mimagedowload.setId(i);
                        cateHolders.get(i).mimagedowload.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								//如果没下载过
								if(!PlayDownloadService.isVideodownedFile(cateItem)){
									if(ispay){
										Intent intent = new Intent(
												MovieFragment.this
														.getActivity(),
												PlayDownloadService.class);
										intent.putExtra(
												PlayDownloadItem.DOWN_ITEM,
												cateItem);
										mhandHandle.removeMessages(0);
										mhandHandle
												.sendEmptyMessageDelayed(0,
														1000);
										MovieFragment.this
												.getActivity()
												.startService(intent);
	  								}else{
	  									PayUtile.showPay(MovieFragment.this.getActivity(), 
	  											String.valueOf(PRICE_WEEK), String.valueOf(PRICE_2MONTHS), String.valueOf(PRICE_YEAR), new PayBack(){

											@Override
											public void Resout(int arg0,
													String propname) {
												String type = "";
												if(PRICE_WEEK==arg0){
													type = "week";
												}else if(PRICE_2MONTHS==arg0){
													type = "month";
												}else if(PRICE_YEAR==arg0){
													type = "year";
												}
												pay(type, arg0, propname);
											}});
	  								}
								}else{
									Toast.makeText(
											MovieFragment.this
													.getActivity(),
											R.string.download_allready,
											Toast.LENGTH_SHORT)
											.show();
									return;
								}
					
							}
						});
                    }
                    /*}*/
                    break;
                case BigItem.Type.LAYOUT_VIDEOHEADLIST:
                	Log.d("rmyzx", "BigItem.Type.LAYOUT_VIDEOHEADLIST");
                	 ArrayList<CategoryItemHolder> cateHoldershead = holder.cateHoldershead;            
                      if(item.videoHeadItems.size()<2)
                      {
                    	  cateHoldershead.get(1).icon.setVisibility(View.INVISIBLE);
                    	  cateHoldershead.get(1).layout.setVisibility(View.INVISIBLE);
                    	  cateHoldershead.get(1).content.setVisibility(View.INVISIBLE);
                    	  cateHoldershead.get(1).listitemvideosdc.setVisibility(View.INVISIBLE);
                      }else{
                    	  cateHoldershead.get(1).icon.setVisibility(View.VISIBLE);
                    	  cateHoldershead.get(1).layout.setVisibility(View.VISIBLE);
                    	  cateHoldershead.get(1).content.setVisibility(View.VISIBLE);  
                    	  cateHoldershead.get(1).listitemvideosdc.setVisibility(View.VISIBLE);
                      }
                      for (int i = 0; i < item.videoHeadItems.size() && i < cateHoldershead.size(); i++) {
                 
                          final VideoItem cateItem = (VideoItem) item.videoHeadItems.get(i);

                         if(cateItem.cDesc!=null)
                         {   
                        	 cateHoldershead.get(i).listitemvideosdc.setText(cateItem.cDesc);
                        	 cateHoldershead.get(i).content.setText(cateItem.cTitle);
                         }
                         if (TextUtils.isEmpty(cateItem.ImagePath)) {
                        	 cateHoldershead.get(i).icon.setImageResource(R.drawable.videodefaultbg);
     					}  else if(!imageLoader.getPause().get()){
     						imageLoader.displayImage(cateItem.ImagePath, cateHoldershead.get(i).icon, mOptions);
     					}                         
                          cateHoldershead.get(i).layout.setOnClickListener(new OnClickListener() {
  							
  							@Override
  							public void onClick(View v) {
  								if(type==1){
									if(ispay){
	  									payMovie(cateItem);
	  								}else{
	  									//Log.d("nnlog", "fffffff---"+FEEID_WEEK+"w"+FEEID_2MONTHS+"2w"+FEEID_YEAR);
	  									PayUtile.showPay(MovieFragment.this.getActivity(), 
	  											String.valueOf(PRICE_WEEK), String.valueOf(PRICE_2MONTHS), String.valueOf(PRICE_YEAR), new PayBack(){

											@Override
											public void Resout(int arg0,
													String propname) {
												String type = "";
												if(PRICE_WEEK==arg0){
													type = "week";
												}else if(PRICE_2MONTHS==arg0){
													type = "month";
												}else if(PRICE_YEAR==arg0){
													type = "year";
												}
												pay(type, arg0, propname);
											}});
	  								}
								}else{
									payMovie(cateItem);
								}
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
  		StatService.trackCustomBeginEvent(MovieFragment.this.getActivity(), "payment_success", "");
  		StatService.trackCustomEvent(MovieFragment.this.getActivity(),"payment", "");
  		int feid = 0 ;
  		if(!MyApplication.getInstance().getBulepayInit()){
  			Toast.makeText(getActivity(), getActivity().getString(R.string.initerroe), Toast.LENGTH_LONG).show();
  		}else{
  			try {
  				if(type.equals("week")){
  					MobclickAgent.onEvent(getActivity(), "moveWeek");
  					StatService.trackCustomEvent(getActivity(), "moveWeek", "");
  					feid = FEEID_WEEK;
  				}else if(type.equals("month")){
  					MobclickAgent.onEvent(getActivity(), "move2Month");
  					StatService.trackCustomEvent(getActivity(), "move2Month", "");
  					feid = FEEID_2MONTHS;
  				}else{
  					MobclickAgent.onEvent(getActivity(), "moveYEAR");
  					StatService.trackCustomEvent(getActivity(), "moveYEAR", "");
  					feid = FEEID_YEAR;
  				}
  				
  				String transID = BluePayUtil.getTransationId(feid, MyApplication.getInstance().getUser().getUid());
  				RapitUtile.setTransID(transID);
//  				
  				//
//  				PayUtile.pay(MovieFragment.this.getActivity(), transID, 
//  						MyApplication.getInstance().getUser().getUid(), 
//  						String.valueOf(price),productName,feid,new Ipayback());
  				PayUtile.payAr(MovieFragment.this.getActivity(), transID, "USD", String.valueOf(price));
  			} catch (Exception e) {
  				Toast.makeText(MovieFragment.this.getActivity(), getActivity().getString(R.string.payerroe),Toast.LENGTH_LONG).show();
  			}
  		}
  	}
    
    private void payMovie(VideoItem cateItem){
    	if(type==1&&isPayTemp)
			{
				return;
			}
		try {
			if(cateItem.webURL!=null&&cateItem.webURL.trim().length()>0&cateItem.isshare!=1){
				Intent intent =new Intent();
				
				if(cateItem.webURL.contains("youtube"))
				{
					intent.setClass(MovieFragment.this.getActivity(),PlayWebVideoActivity.class);
					intent.putExtra("videoUrl", cateItem.webURL);
				}
				else
				{
					//判断是否已经下载
					String stapye = "Move";
					if(PlayDownloadService.isVideodownedFile(cateItem)){
						cateItem.webURL = PlayDownloadService.mVdiaoPath;
						stapye = "TV";
					}
					StartVidioutil.startVidiao(MovieFragment.this.getActivity(),cateItem.webURL ,stapye);
					return;
				}
				MovieFragment.this.getActivity().startActivity(intent);
			}else {
				Intent intent =new Intent();
				
				if(cateItem.webURL.contains("youtube"))
				{
					intent.setClass(MovieFragment.this.getActivity(),PlayActivity.class);
				}
				else
				{
					//判断是否已经下载
					String stapye = "Move";
					if(PlayDownloadService.isVideodownedFile(cateItem)){
						cateItem.webURL = PlayDownloadService.mVdiaoPath;
						stapye = "TV";
					}
					StartVidioutil.startVidiao(MovieFragment.this.getActivity(),cateItem.webURL ,stapye);
					return;
				}
				
					MovieFragment.this.getActivity().startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//	}
    }
    
//    public class Ipayback implements com.byt.market.view.MyPayDailog.PayBack{
//
//		@Override
//		public void Resout(int arg0, BlueMessage bluemsg) {
//			// TODO Auto-generated method stub
//			Log.d("nnlog", "arg0---"+arg0+"----BlueMessage---"+bluemsg.getCode());
//			Log.d("nnlog", "arg0---"+arg0+"----BlueMessage类型---"+bluemsg.getPublisher());
//			String ll = bluemsg.getPublisher();
//				if(!bluemsg.getPublisher().equals("line")){
//					if(bluemsg.getCode() == 200){
////						sellBottom.setVisibility(View.GONE);
////		    			isPayTemp=false;
//						ispay = true;
//		    			mAdapter.notifyDataSetChanged();
//		    			//MobclickAgent.onEvent(getActivity(), "AVpaySucceed");
//					}else{
//						BluePayUtil.showErroe(MovieFragment.this.getActivity(), bluemsg.getCode());
//					}
//				}else{
//					
//				}
//		}
//
//		@Override
//		public void bankRsout(int re) {
//			// TODO Auto-generated method stub
//			if(re==1){
//				ispay = true;
//				mAdapter.notifyDataSetChanged();
//			}
//			//MobclickAgent.onEvent(getActivity(), "AVpaySucceed");
//		}
//		
//	}
    
    /**
     * 2g 网络 使用此Adapter,主要是List样式 
     * @author z
     *
     */
    private class CateAdapter extends ImageAdapter{


		@Override
        public View newView(LayoutInflater inflater, BigItem item) {
            View view = null;
            BigHolder holder = new BigHolder();
            switch(item.layoutType){
                case BigItem.Type.LAYOUT_CAEGORYLIST:
                    view = inflater.inflate(R.layout.listitem_cate_list, null);
                    holder.layoutType = item.layoutType;
                    CategoryItemHolder itemHolder1 = new CategoryItemHolder();
                    itemHolder1.icon = (ImageView) view.findViewById(R.id.cate_icon);
                    itemHolder1.name = (TextView) view.findViewById(R.id.cate_title);
                    itemHolder1.layout = view.findViewById(R.id.cate_item_layout);
                    itemHolder1.content=(TextView) view.findViewById(R.id.cate_content);
                    CategoryItemHolder itemHolder2 = new CategoryItemHolder();
                    itemHolder2.icon = (ImageView) view.findViewById(R.id.cate_icon1);
                    itemHolder2.name = (TextView) view.findViewById(R.id.cate_title1);
                    itemHolder2.layout = view.findViewById(R.id.cate_item_layout1);
                    itemHolder2.line = view.findViewById(R.id.listitem_rank_line2);
                    itemHolder2.content=(TextView) view.findViewById(R.id.cate_content1);                                     
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
            switch (item.layoutType){
                case BigItem.Type.LAYOUT_CAEGORYLIST:
                    ArrayList<CategoryItemHolder> cateHolders = holder.cateHolders;
                    if(item.cateItems.size()<2)
                    {
                    	cateHolders.get(1).layout.setVisibility(View.INVISIBLE);	
                    	cateHolders.get(1).line.setVisibility(View.INVISIBLE);
                    }
                    for (int i = 0; i < item.cateItems.size() && i < cateHolders.size(); i++) {
                        final CateItem cateItem = item.cateItems.get(i);
                        cateHolders.get(i).name.setText(cateItem.cTitle);
                      if(TextUtils.isEmpty(cateItem.ImagePath)){
                        	cateHolders.get(i).icon.setImageResource(R.drawable.app_empty_icon);
                       }else{
                        	if (!cateItem.ImagePath.startsWith("http://")) {
//                        	mImageFetcher.loadImage(cateItem.ImagePath, cateHolders.get(i).icon);
                        	imageLoader.displayImage(cateItem.ImagePath, cateHolders.get(i).icon, mOptions);
                        	}else{
//                        	mImageFetcher.loadImage(cateItem.ImagePath, cateHolders.get(i).icon);
                        	imageLoader.displayImage(cateItem.ImagePath, cateHolders.get(i).icon, mOptions);
                        	}
                        }
                      /*bestone add by zengxiao for :添加分类详情介绍*/
                      cateHolders.get(i).content.setText(cateItem.cDesc);
                      /*add end*/
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
    
	@Override
	protected void onDownloadStateChanged() {
		
	}

	@Override
	public void onAppClick(Object obj, String action) {
		Intent intent = new Intent(action);
		Log.i("xxx", "obj==>"+obj);
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
	public void setStyle(CusPullListView listview2){
		// TODO Auto-generated method stub
		super.setStyle(listview2);
/*		View view=LayoutInflater.from(getActivity()).inflate(R.layout.videoland_header, null);
		listview2.addHeaderView(view);
*/	}
	//修改提示语
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
