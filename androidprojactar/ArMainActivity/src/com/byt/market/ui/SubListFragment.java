package com.byt.market.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.ShareActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.adapter.MyListAdapter;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.data.BigItem;
import com.byt.market.data.HLeaderItem;
import com.byt.market.data.SubjectItem;
import com.byt.market.data.UserData;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.ui.LiveFragment;
import com.byt.market.mediaplayer.ui.MovieFragment;
import com.byt.market.tools.Dailog;
import com.byt.market.tools.LogCart;
import com.byt.market.tools.Toolst;
import com.byt.market.util.BitmapUtil;
import com.byt.market.util.DataUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.TextUtil;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.CustomListView;
import com.tencent.stat.StatService;

public class SubListFragment extends ListViewFragment {

	private static final int BAO_MATH = 123;
	private static final int BAO_DAY = 0124;
	private String from;
	private Object object;
	private int which;
	/* add by zengxiao */
	LayoutInflater minflater;
	PopupWindow moptionmenu;
	private int nCheckResult3 = -20;
	private String usename = "";
	protected int[] optiontextlist = { R.string.expend_child1_text,
			R.string.state_idle_text, R.string.dialog_nowifi_btn_fav };
//	private boolean isPay;
	private int feeID;
	private int feeid_day;
	private int priodid;
	/* add end */
	@Override
	protected String tag() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		object = getArguments().getParcelable("app");
		from = getArguments().getString(Constants.LIST_FROM);
	}
	//---------by----------bobo---------
	private Handler myhandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what==0){
    			sellBottom.setVisibility(View.GONE);
    			isPayTemp=false;
    			mAdapter.notifyDataSetChanged();
    			if(msg.arg1==2){
    				StatService.trackCustomEvent(SubListFragment.this.getActivity(), "payment_success", "");
        			Dailog.dismis();
        		}
    		}else if(msg.what==2){
        			Dailog.dismis();
        			//Toast.makeText(getActivity(), "支付失败", Toast.LENGTH_LONG).show();
        			Toolst.show(getActivity(),R.string.play_fiald);
    		}else if(msg.what==3){
    			new Thread(){@Override
					public void run() {
//						// TODO Auto-generated method stub
//						super.run();
//						LogCart.Log("看看 是多少-天--usename----"+usename);
//						//int nCheckResult3 = jsxltjsrv.CheckFeeValid(usename,1000, 1000, priodid,feeid_day);
//						final int nCheckResult0 = MainActivity.jsxltj.JsXltj_CheckFeeState(usename, 1000, 1000, priodid, feeid_day);
//						LogCart.Log("看看 是多少--天-----"+nCheckResult0+"------feeid"+feeid_day+"--p--"+priodid);
//						Message msg = myhandler.obtainMessage();
//						msg.what = nCheckResult0;
//						myhandler.sendMessage(msg);
					}}.start();
    		}else{
    			if(msg.arg1==2){
        			Dailog.dismis();
        		}
    			isPayTemp = true;
    			mAdapter.notifyDataSetChanged();
    			sellBottom.setVisibility(View.VISIBLE);
    		}
		};
	};
	//----------end
	@Override
	protected String getRequestUrl() {
		if (object instanceof HLeaderItem) {
			HLeaderItem hItem = (HLeaderItem) object;
			which = hItem.sid;
			return Constants.LIST_URL + "?qt=" + Constants.SAVE + "&id="
					+ hItem.sid + "&pi=" + getPageInfo().getNextPageIndex()
					+ "&ps=" + getPageInfo().getPageSize();
		} else if (object instanceof SubjectItem) {
			
			SubjectItem item = (SubjectItem) object;
			int cid = item.sid;
			isPayTemp=item.ispay>0;	
			//Log.d("logcart", "tttttttt"+isPayTemp);
			Log.w("xxx", "item.ispay>0==>"+(item.ispay>0));
			String price=String.format(MyApplication.getInstance().getResources().getString(R.string.sell_alert), item.ispay);
			
			String price_days=String.format(MyApplication.getInstance().getResources().getString(R.string.money),item.ispayday);
			String price_math = String.format(MyApplication.getInstance().getResources().getString(R.string.money), item.ispay);
			dayprice.setText(price_days);
			mathpric.setText(price_math);
			
			alert_sell.setText(price);
			payArgs.put("feeId", item.feeID);
			feeID=item.feeID;
			feeid_day = item.feeid_day;
			priodid = item.priodid;
			sellbutton.setOnClickListener(new ShellClicklisenler(feeID,BAO_MATH));
	        sell_daybutton.setOnClickListener(new ShellClicklisenler(feeid_day,BAO_DAY));
			
			//这里 因该 就是检查 有没有付费 了 --------------------------------------------
			//int nCheckResult3 = jsxltjsrv.CheckFeeValid(1000, 1000, 1000,feeID);
			
			//-----------------------bobo
			if(item.ispay>0&&MyApplication.getInstance().getUser().getUlevel()<2){
				new Thread(){@Override
					public void run() {
//						// TODO Auto-generated method stub
//						super.run();
//					//	nCheckResult3 = jsxltjsrv.CheckFeeValid(usename,1000, 1000, priodid,feeID);
//						nCheckResult3 = MainActivity.jsxltj.JsXltj_CheckFeeState(usename,1000, 1000, priodid,feeID);
//						LogCart.Log("看看 是多少-月------"+nCheckResult3+"-priodid"+priodid+"--f-"+feeID+"---"+usename);
//						Message msg = myhandler.obtainMessage();
//						if(nCheckResult3==0){
//							msg.what = nCheckResult3;
//						}else{
//							msg.what = 3;
//						}
//						myhandler.sendMessage(msg);
						
					}}.start();
//					if(nCheckResult3!=0){
//						Message msg = myhandler.obtainMessage();
//						msg.what = nCheckResult3;
//						myhandler.sendMessage(msg);
//					}
			}
			//-------------
			
			 Log.i("xxx", "CheckMonthFee(0, 0, 1000,"+feeID+")");
			// Log.i("xxx", "nCheckResult==>"+nCheckResult3);
			//if (nCheckResult3==0) {
				//isPayTemp=false;
				//sellBottom.setVisibility(View.GONE);
			//}
			//这里 因该 就是检查 有没有付费 了 --------------------------------------------
			return Constants.LIST_URL + "?qt=" + Constants.SUBJECT_LIST
					+ "&cid=" + cid + "&pi=" + getPageInfo().getNextPageIndex()
					+ "&ps=" + getPageInfo().getPageSize();
			

		} else if (object instanceof AppItem) {
			AppItem item = (AppItem) object;
			int cid = item.sid;
			return Constants.LIST_URL + "?qt=" + Constants.SUBJECT_LIST
					+ "&cid=" + cid + "&pi=" + getPageInfo().getNextPageIndex()
					+ "&ps=" + getPageInfo().getPageSize();
		}
		return null;
	}

	@Override
	protected String getRequestContent() {
		return null;
	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		try {
			String fromstr = LogModel.P_LIST;
			String catlist = LogModel.P_LIST;
			if (object instanceof HLeaderItem) {
				HLeaderItem item = (HLeaderItem) object;
				fromstr = LogModel.L_HOME_LEADER;
				catlist = item.sid + "," + item.stype;
			} else if (object instanceof SubjectItem) {
				SubjectItem item = (SubjectItem) object;
				fromstr = LogModel.L_SUBJECT_LIST;
				catlist = item.sid + "";
			} else if (object instanceof AppItem) {
				AppItem item = (AppItem) object;
				fromstr = LogModel.L_BANNER_LIST;
				catlist = item.sid + "";
			}
			List<BigItem> list = JsonParse.parseSubList(
					result.getJSONArray("data"), fromstr, catlist);
			DownloadTaskManager.getInstance().fillBigItemStates(list,
					new int[] { DownloadTaskManager.FILL_TYPE_SUBLIST_ITEMS });
			return list;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public void onPostExecute(byte[] bytes) {
		if(isPayTemp){
			sellBottom.setVisibility(View.VISIBLE);
		}else{
			sellBottom.setVisibility(View.GONE);
		}
		super.onPostExecute(bytes);
		
	}
	@Override
	protected int getLayoutResId() {
		return R.layout.listview;
	}
	TextView mathpric;
	TextView dayprice;
	private View sellBottom;
	private TextView alert_sell,sell,sellbutton,sellbuttontmp,sell_daybutton,sellday_buttontmp;
	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);
		sellBottom=view.findViewById(R.id.sell_bottom);
		alert_sell=(TextView)view.findViewById(R.id.alert_sell);
		sell=(TextView)view.findViewById(R.id.sell);
		sellbuttontmp=(TextView)view.findViewById(R.id.selltmp);
		
		sellbutton=(TextView)view.findViewById(R.id.sell);
		sell_daybutton = (TextView) view.findViewById(R.id.sell_day);
		sellday_buttontmp = (TextView) view.findViewById(R.id.selltmp_day);
		mathpric = (TextView) view.findViewById(R.id.bao_math_price);
		dayprice = (TextView) view.findViewById(R.id.bao_day_price);
		usename = MyApplication.getInstance().getUser().getNickname();
		
//		sellBottom.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(final View vw) {
//				vw.setEnabled(false);
//				new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
//						try {
//							Log.i("xxx", "jsxltjsrv.FeeRequ feeID==>"+feeID);
////							final int  nFeeOrder = jsxltjsrv.FeeRequ(Util.getIMSI(MyApplication.getInstance()), 1, 2, 3, feeID);
//							final String strTID =MainActivity.jsxltj.BluePay_Pay(usename,1000, 1000, 1000, feeID);
//							Log.i("xxx", "nFeeOrder=BluePay_Pay(1000, 1000, 1000,"+feeID+")");
//							Log.i("xxx", "nFeeOrder==>"+strTID);
//							final int nCheckResult = jsxltjsrv.CheckFeeValid(usename,1000, 1000, 1000,feeID);
//							Log.i("xxx", "CheckMonthFee(0, 0, 1000,"+feeID+")");
//							Log.i("xxx", "nFeeOrder  nCheckResult==>"+nCheckResult);
//							Log.d("newzx", "Thread nCheckResult="+nCheckResult);
//							handler.post(new Runnable() {
//								
//								@Override
//								public void run() {
//									try {
//										vw.setEnabled(true);
//										Log.d("newzx", "Thread 2nCheckResult="+nCheckResult);
//										Log.i("xxx", "display---  nCheckResult==>"+nCheckResult);
//										if (nCheckResult==0) {
//											isPayTemp=false;
//											sellBottom.setVisibility(View.GONE);
//											if(mAdapter!=null){
//												mAdapter.notifyDataSetChanged();
//											}
//											Log.d("newzx", "Thread notify");
//										}else{
//											
//												handler.removeMessages(1001);
//												handler.sendEmptyMessageDelayed(1001, 1500);
//												Log.d("newzx", "send thread");
//											
//										}
//									} catch (Exception e) {
//										e.printStackTrace();
//									}
//								}
//							});
//							
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}).start();
//				
//			}
//		});
		return view;
	}
//	private Handler handler=new Handler(){
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
//			if(msg.what==1001)
//			{
//				sell.setVisibility(View.GONE);
//				sellbuttontmp.setVisibility(View.VISIBLE);
//				int nCheckResult2 = jsxltjsrv.CheckFeeValid(usename,1000, 1000, 1000,feeID);
//							if (nCheckResult2==0) {
//								isPayTemp=false;
//								sellBottom.setVisibility(View.GONE);
//								if(mAdapter!=null){
//									mAdapter.notifyDataSetChanged();
//								}	
//								Log.d("newzx", "handler notigyDataSetChangeed");
//			}else{
//				handler.removeMessages(1001);
//				handler.sendEmptyMessageDelayed(1001, 1500);
//				Log.d("newzx", "handler sendEmptyMessageDelayed");
//			}
//			}
//			super.handleMessage(msg);
//		}
//	};
	@Override
	protected ImageAdapter createAdapter() {
		return new SubListAdapter();
	}

	private class SubListAdapter extends ImageAdapter {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

		public SubListAdapter() {
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
				view = inflater.inflate(R.layout.listitem_common_layout2, null);

				holder.layoutType = item.layoutType;
				CommonItemHolder commonItemHolder = new CommonItemHolder();
				commonItemHolder.appIcon = (ImageView) view
						.findViewById(R.id.img_icon_View);
				/*ImageView temp = (ImageView) view
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
			case BigItem.Type.LAYOUT_SUBLIST:

				final CommonItemHolder itemHolder = holder.commonItemHolder;
				for (final AppItem appItem : item.subListItems) {
					
					itemHolder.name.setText(appItem.name);
					itemHolder.rating.setRating(appItem.rating);
					itemHolder.sizedivider.setText(" | ");
					itemHolder.size.setText(appItem.strLength);
					if (LogModel.L_HOME_LEADER_LATEST_COLLECTION.equals(from)) {
						// 如果是最新收录则显示更新时间
						itemHolder.downum.setText(appItem.downNum);
					} else {
						itemHolder.downum.setText(appItem.downNum);
					}
					if (TextUtils.isEmpty(appItem.adesc)) {
						itemHolder.descLayout.setVisibility(View.GONE);
					} else {
						itemHolder.descLayout.setVisibility(View.VISIBLE);
						itemHolder.sdesc.setText(appItem.adesc);
					}
					if (TextUtils.isEmpty(appItem.iconUrl)) {
					//	itemHolder.appIcon
					//			.setImageResource(R.drawable.app_empty_icon);
					} else if(!imageLoader.getPause().get()){
						imageLoader.displayImage(appItem.iconUrl,
								itemHolder.appIcon, options);
					}
					/*add by zengxiao for:修改item样式*/
					if(!Constants.ISGOOGLE)
					{
						if(appItem.googlePrice>0&&appItem.googlemarket>0&&!isPayTemp)
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

					itemHolder.itemLayout
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									
									Log.d("logcart", "ititit------");
									onAppClick(appItem, Constants.TODETAIL);
								}
							});

					/* add by zengxiao for change UI */
					if(appItem.isshare==1&&!isPayTemp){
						itemHolder.downBtn.setVisibility(View.GONE);
						//itemHolder.share_icon.setVisibility(View.VISIBLE);
						itemHolder.downBtn2.setVisibility(View.VISIBLE);
						itemHolder.downBtn2.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								
								if(itemHolder.downBtn2.getText().toString().equals(SubListFragment.this.getActivity().getString(R.string.sharedown)))
								{
									Intent intent =new Intent();
									intent.setClass(SubListFragment.this.getActivity(),ShareActivity.class);						
									intent.putExtra("sendstring", appItem); 
									SubListFragment.this.getActivity().startActivity(intent);
								}
								else
								{
								DownloadTaskManager.getInstance().onDownloadBtnClick(appItem);
								}
								
							}
						});
						if(!imageLoader.getPause().get()){
							Drawable drawable=SubListFragment.this.getActivity().getResources().getDrawable(R.drawable.progress_listitem);
							itemHolder.DownloadProgressBar.setProgressDrawable(drawable);
							itemHolder.item_up_bg.setBackgroundResource(R.drawable.bg_press_draw);
							DownloadTaskManager.getInstance().updateItemBtnByStateshare(
									getActivity(), itemHolder.downBtn2, appItem,itemHolder.DownloadProgressBar,itemHolder.progressnumtext,null,itemHolder.sharelayout);

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
						Drawable drawable=SubListFragment.this.getActivity().getResources().getDrawable(R.drawable.progress_listitem);
						itemHolder.DownloadProgressBar.setProgressDrawable(drawable);
						itemHolder.item_up_bg.setBackgroundResource(R.drawable.bg_press_draw);
						DownloadTaskManager.getInstance().updateItemBtnByState(
								getActivity(), itemHolder.downBtn, appItem,itemHolder.DownloadProgressBar,itemHolder.progressnumtext);
						if(AppItemState.STATE_IDLE==appItem.state&&isPayTemp){
							itemHolder.downBtn.setEnabled(false);
							itemHolder.downBtn.setBackgroundColor(Color.GRAY);
						}
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
	public void showNoResultView() {
		super.showNoResultView();
		loadfailed.setButtonVisible(View.GONE);
	}

	@Override
	public void setStyle(CusPullListView listview) {
		listview.setCacheColorHint(0);
		String tip = null;
		String date = null;
		if (object instanceof SubjectItem) {
			SubjectItem item = (SubjectItem) object;
		} else if (object instanceof AppItem) {
			AppItem item = (AppItem) object;
			tip = item.adesc;
		} else {
			return;
		}
		if (tip != null) {
			View view = LayoutInflater.from(getActivity()).inflate(
					R.layout.listitem_subject_header, null);
			TextView list_subject_header_title = (TextView) view
					.findViewById(R.id.list_subject_header_title);
			list_subject_header_title.setText(TextUtil.toDBC(tip));
			listview.addHeaderView(view);
		}
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
	protected void onDownloadStateChanged() {
		// TODO Auto-generated method stub
		DownloadTaskManager.getInstance().fillBigItemStates(
				SubListFragment.this.getAdapter().mListItems,
				new int[] { DownloadTaskManager.FILL_TYPE_SUBLIST_ITEMS });
		SubListFragment.this.getAdapter().notifyDataSetChanged();
	}

	@Override
	public void onAppClick(Object obj, String action) {
		Intent intent = new Intent(action);
		if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			intent.putExtra("app", app);
		}
		String fromstr = LogModel.L_SUBJECT_LIST;
		if (object instanceof HLeaderItem) {
			HLeaderItem item = (HLeaderItem) object;
			fromstr = LogModel.L_HOME_LEADER + "," + item.sid + ","
					+ item.stype;
		} else if (object instanceof SubjectItem) {
			SubjectItem item = (SubjectItem) object;
			fromstr = LogModel.L_SUBJECT_LIST + "," + item.sid;
		} else if (object instanceof AppItem) {
			AppItem item = (AppItem) object;
			fromstr = LogModel.L_BANNER_LIST + "," + item.sid;
		}
		intent.putExtra(Constants.LIST_FROM, fromstr);
		if(!isPayTemp)
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	@Override
	public void addNewDataOnce() {
		if (object instanceof HLeaderItem) {
			String fromstr = LogModel.L_HOME_LEADER;
			HLeaderItem item = (HLeaderItem) object;
			fromstr = item.sid + "," + item.stype;
			Util.addListData(maContext, LogModel.L_HOME_LEADER,
					String.valueOf(fromstr), 1);
		} else if (object instanceof SubjectItem) {
			SubjectItem item = (SubjectItem) object;
			Util.addListData(maContext, LogModel.L_SUBJECT_LIST,
					String.valueOf(item.sid), 1);
		} else if (object instanceof AppItem) {
			AppItem item = (AppItem) object;
			Util.addListData(maContext, LogModel.L_BANNER_LIST,
					String.valueOf(item.sid), 1);
		}
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
	public void onDestroy() {
		// TODO Auto-generated method stub
		handler.removeMessages(1001);
		super.onDestroy();
	}
	private class ShellClicklisenler implements OnClickListener{

		private  int mfeeid = 0;
		private int mtype = 0;
		public ShellClicklisenler (int feeid,int type){
			this.mfeeid = feeid;
			this.mtype = type;
		}
		@Override
		public void onClick(final View arg0) {
			// TODO Auto-generated method stub
			//arg0.setEnabled(false);
			StatService.trackCustomEvent(SubListFragment.this.getActivity(),"payment", "");
			StatService.trackCustomBeginEvent(SubListFragment.this.getActivity(), "payment_success", "");
			Dailog.show2(SubListFragment.this.getActivity());
			new Thread(new Runnable() {
				
				@Override
				public void run() {
//					try {
//						LogCart.Log("-付费接口-----nCheckResult----");
////						final int  nFeeOrder = jsxltjsrv.FeeRequ(Util.getIMSI(MyApplication.getInstance()), 1, 2, 3, feeID);
//				//		final String strTID =MainActivity.jsxltj.BluePay_Pay(usename,1000, 1000, priodid, mfeeid);
//						int r = MainActivity.jsxltj.JsXltj_GetInitResult();
//						LogCart.Log("rrrrrrrrrrrrr-------"+r);
//						//final String strTID =MainActivity.jsxltj.JsXltj_Pay(usename, 1000, 1000, priodid, mfeeid);
//						MainActivity.jsxltj.JsXltj_Pay(usename, 1000, 1000, priodid, mfeeid);
//																					//"usename, 1000, 1000, priodid, mfeeid"
//						LogCart.Log("-付费-22接口--------priodid------mfeeid-"+priodid+"-----"+mfeeid);
//						//add  by---------bobo
//						 int nCheckResult = 2;
//						 int n = 0;
//						 Message msg = myhandler.obtainMessage();
//						try {
//							while(n<10){
//								LogCart.Log("-付费-----nCheckResult----"+nCheckResult);
//						//		 nCheckResult = jsxltjsrv.CheckFeeValid(usename,1000, 1000, priodid,mfeeid);
//								nCheckResult = MainActivity.jsxltj.JsXltj_CheckFeeState(usename,1000, 1000, priodid,mfeeid);
//								 LogCart.Log("result--"+nCheckResult+"-u-"+usename+"-p-"+priodid+"-f-"+mfeeid);
//								 n++;
//								 if( nCheckResult==0){
//									// Message msg = myhandler.obtainMessage();
//									 msg.what = nCheckResult;
//									 msg.arg1 = 2;
//									 myhandler.sendMessage(msg);
//									 return;
//										//n = 8;
//									}else{
//										Thread.sleep(1000*6);
//										LogCart.Log(mfeeid+"--feeid--------"+n);
//									}
//							}
//							if(n==10){
//								msg.what=2;
//									nCheckResult = -1990;
//									LogCart.Log("-----bbb-----"+n);
//									msg.arg1=nCheckResult;
//									myhandler.sendMessage(msg);
//							}
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						
//						
//						LogCart.Log("nCheckResult==>"+nCheckResult);
//						
//						//handler.post(new HandlerRunlber(nCheckResult, arg0, mtype));
//						
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
				}
			}).start();
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
