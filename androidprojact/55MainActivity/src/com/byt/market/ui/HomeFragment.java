package com.byt.market.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.app.SearchManager.OnCancelListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.WebCommonActivity;
import com.byt.market.activity.FeedBackActivity;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.ShareActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.data.AppItem;
import com.byt.market.data.BigItem;
import com.byt.market.data.HLeaderItem;
import com.byt.market.data.HomeItemBean;
import com.byt.market.data.SubjectItem;
import com.byt.market.download.DownloadBtnClickListener;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.LiveActivity;
import com.byt.market.mediaplayer.PlayActivity;
import com.byt.market.mediaplayer.PlayWebVideoActivity;
import com.byt.market.mediaplayer.VideoActivity;
import com.byt.market.mediaplayer.MV.MVActivity;
import com.byt.market.mediaplayer.animtion.AnimtionActivity;
import com.byt.market.mediaplayer.data.PlayDownloadItem;
import com.byt.market.mediaplayer.data.VideoItem;
import com.byt.market.mediaplayer.music.NewRingActivity;
import com.byt.market.mediaplayer.nover.NovelActivity;
import com.byt.market.mediaplayer.service.PlayDownloadService;
import com.byt.market.mediaplayer.show.ShowActivity;
import com.byt.market.mediaplayer.tv.TVActivity;
import com.byt.market.mediaplayer.voiced.VoicedActivity;
import com.byt.market.qiushibaike.JokeActivity;
import com.byt.market.qiushibaike.news.NewsActivity;
import com.byt.market.tools.LogCart;
import com.byt.market.util.HomeToutils;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LogUtil;
import com.byt.market.util.Singinstents;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.CustomPagerAdapter;
import com.byt.market.view.HomeAdvertiseBand;
import com.byt.market.view.HomeViewPaper;
import com.byt.market.view.rapit.CoverManager;
import com.byt.market.view.rapit.WaterDrop;
import com.byt.market.view.zhedie.CollapsableLinearLayout;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

public class HomeFragment extends ListViewFragment implements OnClickListener, OnPageChangeListener  {
	private boolean isshowAd=false;
	private HomeAdvertiseBand mHomeAdvertiseBand;
	private boolean isFlushHomeBand;
	private View homeAdLayout;
	private final int RFRESHUI=1;
	private final int REFRESHAD=2;
	private final int OVEERE=3;
	private boolean isoverre ;
	private boolean isSend;
	private boolean funrapit;
	private DisplayImageOptions bannerOptions;
	/*bestone add by zengxiao */
	private HomeViewPaper mViewPager;//滚动 log  
	private ViewGroup mPointViewGroup;
	private ArrayList<View> mViewPagerList;
	private boolean mIsChanged = false;
	//默认ViewPage显示数组里的第二个数据,用来控制ViewPage显示数组中的哪个，序号从0开始
	private int mCurrentPagePosition =1;

	//控制显示哪个标签，序号从0开始
	private int mCurrentIndex;
	//数组的大小
	private int POINT_LENGTH;
	private static final String TAG = "Lihao";
	private int pageindex=1;
	Timer timer;
	private DisplayImageOptions mOptions;
	/*add end*/
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
	private Handler mHander=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what)
			{
			case RFRESHUI :
				DownloadTaskManager.getInstance().fillBigItemStates(
						HomeFragment.this.getAdapter().mListItems,
						new int[] { DownloadTaskManager.FILL_TYPE_HOMEITEMS,
								DownloadTaskManager.FILL_TYPE_RECITEMS });
				HomeFragment.this.getAdapter().notifyDataSetChanged();
				break;
			case REFRESHAD:
					if(MarketContext.getInstance().isGalleryMove)
					{
						return;
					}
					if(mViewPager.getChildCount()>0)
					{
						pageindex++;
						if(pageindex==POINT_LENGTH+1)
							pageindex=1;
						mViewPager.setCurrentItem(pageindex);
					}
						break;
			case OVEERE:
				isoverre = true;
				HomeFragment.this.getAdapter().notifyDataSetChanged();
				break;
			}
		}
	};
	@Override
	protected String getRequestUrl() {
		
		String u = Constants.LIST_URL + "?qt=" + Constants.HEAD + "&pi="
				+ getPageInfo().getNextPageIndex() + "&ps="
				+ getPageInfo().getPageSize()+"&stype="+MainActivity.HOME_KEY;
		Log.d("test", u);
		return u;
	}
	@Override
	public void initViewBYT(View view) {
/*		bestone add by "zengxiao" for:添加标题头
		view.findViewById(R.id.titlebar_title).setVisibility(View.GONE);
		view.findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
		view.findViewById(R.id.iv_settings).setVisibility(View.GONE);
		view.findViewById(R.id.search_page_view).setVisibility(View.VISIBLE);
		view.findViewById(R.id.search_page_view).setOnClickListener(this);
		view.findViewById(R.id.top_downbutton).setVisibility(View.VISIBLE);
		view.findViewById(R.id.top_downbutton).setOnClickListener(this);//添加下载界面按钮
		view.findViewById(R.id.titlebar_applist_button_container).setVisibility(View.GONE);
		bestone add end*/
	}
	@Override
	public void setStyle(CusPullListView listview2) {
		// TODO Auto-generated method stub
		listview.setonRefreshListener(refreshListen);
		loadfailed.setButtonVisible(View.GONE);
	}
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
			if (!result.isNull("hlist")) {
				isonepagere=true;
			}
			if (!result.isNull("hload")) {
				isonepagere=true;
			}
			final List<BigItem> items = JsonParse.parseHomeList(result
					.getJSONObject("data"), 3, getPageInfo().getPageIndex());
			DownloadTaskManager.getInstance().fillBigItemStates(
					items,
					new int[] { DownloadTaskManager.FILL_TYPE_HOMEITEMS,
							DownloadTaskManager.FILL_TYPE_RECITEMS });
			return items;
		} catch (Exception e) {
		
			}

		/*}*/
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
		mOptions = new DisplayImageOptions.Builder().cacheOnDisc().build();
		//红点初始
		CoverManager.getInstance().init(getActivity());
		CoverManager.getInstance().setMaxDragDistance(150);
		CoverManager.getInstance().setExplosionTime(150);
		getActivity().registerReceiver(new Myfunrecevie(), new IntentFilter("com.market.fun"));
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
			/* add by zengxiao */
			minflater = inflater;
			/* add end */
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_APPLIST_ONE:
				HomeRecHolder recHolder = new HomeRecHolder();
				view = inflater.inflate(R.layout.listitem_homerec_item, null);
				holder.layoutType = item.layoutType;
				recHolder.icon = (ImageView) view
						.findViewById(R.id.listitem_rec_icon);
				recHolder.icon_type = (ImageView) view
						.findViewById(R.id.listitem_rec_type);
				recHolder.name = (TextView) view
						.findViewById(R.id.listitem_rec_name);
				recHolder.subLine = (TextView) view
						.findViewById(R.id.listitem_rec_category);
				recHolder.listitem_rec_atip = (TextView) view
						.findViewById(R.id.listitem_rec_atip);
				recHolder.rec_rating = (RatingBar) view
						.findViewById(R.id.appRatingView);
				recHolder.rec_comm = (TextView) view
						.findViewById(R.id.appComm_count);
				recHolder.progress = (ProgressBar) view
						.findViewById(R.id.homedownloadProgressBar);
				recHolder.layout = view.findViewById(R.id.ll_rec_today);
				recHolder.btn = (Button) view
						.findViewById(R.id.listitem_rec_btn);
				holder.recHolder = recHolder;
				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_LEADER_THREE:
				view = inflater.inflate(R.layout.listitem_leader_three, null);
				holder.layoutType = item.layoutType;
				HomeLeaderHolder homeLeaderHolder = new HomeLeaderHolder();
				homeLeaderHolder.title_rela = (RelativeLayout) view.findViewById(R.id.title_click);
				homeLeaderHolder.collaslinerlayout = (CollapsableLinearLayout) view.findViewById(R.id.collapsable);
				homeLeaderHolder.zhedie_imag = (ImageView) view.findViewById(R.id.zhedie_imageView);
				
				//糗百
				homeLeaderHolder.leadericons[4] = (WaterDrop) view
						.findViewById(R.id.fun_redpoint_tv);
				homeLeaderHolder.leadernames[4] = (TextView) view
						.findViewById(R.id.listitem_leader_three_name_1);
				homeLeaderHolder.leaderviews[4] = view
						.findViewById(R.id.listitem_leader_three_view_1);
				//viduTvRestupdata(homeLeaderHolder.leadericons[0]);
				resUpdate(homeLeaderHolder.leadericons[4], MainActivity.JOKE_KEY);
				//txtResUpdate(homeLeaderHolder.leadericons[0]);
				//tv
				homeLeaderHolder.leadericons[0] = (WaterDrop) view
						.findViewById(R.id.tv_redpoint_tv);
				homeLeaderHolder.leadernames[0] = (TextView) view
						.findViewById(R.id.listitem_leader_three_name_2);
				homeLeaderHolder.leaderviews[0] = view
						.findViewById(R.id.listitem_leader_three_view_2);
				//viduTvRestupdata(homeLeaderHolder.leadericons[1]);
				//resUpdate(homeLeaderHolder.leadericons[4], MainActivity.TV_KEY);

				//music
				homeLeaderHolder.leadericons[3] = (WaterDrop) view
						.findViewById(R.id.music_redpoint_tv);
				homeLeaderHolder.leadernames[3] = (TextView) view
						.findViewById(R.id.listitem_leader_three_name_3);
				homeLeaderHolder.leaderviews[3] = view
						.findViewById(R.id.listitem_leader_three_view_3);
				//resUpdate(homeLeaderHolder.leadericons[3], MainActivity.MUSIC_KEY);
				//av
//				homeLeaderHolder.leadericons[6] = (WaterDrop) view
//						.findViewById(R.id.vdo_redpoint_tv);
				homeLeaderHolder.leadericons[6] = (WaterDrop) view
				.findViewById(R.id.av_redpoint_tv);
				homeLeaderHolder.leadernames[6] = (TextView) view
						.findViewById(R.id.listitem_leader_three_name_4);
				homeLeaderHolder.leaderviews[6] = view
						.findViewById(R.id.listitem_leader_three_view_4);
				resUpdate(homeLeaderHolder.leadericons[6], MainActivity.AV_KEY);
				
				//4--5  movie
				homeLeaderHolder.leadericons[1] = (WaterDrop) view
						.findViewById(R.id.move_redpoint_tv);
				//homeLeaderHolder.leadericons[1] = (WaterDrop) view.findViewById(R.id.vdo_redpoint);
				homeLeaderHolder.leaderviews[1] = view.findViewById(R.id.videotitle);
				resUpdate(homeLeaderHolder.leadericons[1], MainActivity.MVOE_KEY);
				//电台
				homeLeaderHolder.leadericons[9] = (WaterDrop) view.findViewById(R.id.vdo_redpoint_tv);
				homeLeaderHolder.leaderviews[9] = view.findViewById(R.id.listitem_leader_three_Voiced_icon_1);
				//resUpdate(homeLeaderHolder.leadericons[9], MainActivity.RIDIAO_KEY);
				
				//6---2动画
				homeLeaderHolder.leadericons[8] = (WaterDrop) view.findViewById(R.id.anim_redpoint_tv);
				homeLeaderHolder.leaderviews[8] = view.findViewById(R.id.listitem_leader_three_anime);
				//resUpdate(homeLeaderHolder.leadericons[8], MainActivity.ANIME_KEY);
				
				//8--3小说
				homeLeaderHolder.leadericons[2] = (WaterDrop) view.findViewById(R.id.novel_redpoint_tv);
				homeLeaderHolder.leaderviews[2] = view.findViewById(R.id.listitem_leader_three_book_view_3);
				//resUpdate(homeLeaderHolder.leadericons[2], MainActivity.NOVEL_KEY);
				
				//8综艺
				homeLeaderHolder.leadericons[7] = (WaterDrop) view
						.findViewById(R.id.zongyi_redpoint_tv);
//				homeLeaderHolder.leadernames[8] = (TextView) view
//						.findViewById(R.id.listitem_leader_three_name_3);
				homeLeaderHolder.leaderviews[7] = view
						.findViewById(R.id.listitem_leader_three_zongyi);
				//resUpdate(homeLeaderHolder.leadericons[7], MainActivity.SHOW_KEY);
				//9新闻
				homeLeaderHolder.leadericons[5] = (WaterDrop) view
						.findViewById(R.id.news_redpoint_tv);
				homeLeaderHolder.leaderviews[5] = view
						.findViewById(R.id.listitem_leader_three_news);
				//resUpdate(homeLeaderHolder.leadericons[1], MainActivity.NEWS_KEY);
				//10意见
				homeLeaderHolder.leaderviews[11] = view
						.findViewById(R.id.listitem_leader_three_view_5);
				//11mv
				homeLeaderHolder.leadericons[10] = (WaterDrop) view
						.findViewById(R.id.other_redpoint_tv);
				homeLeaderHolder.leaderviews[10] = view
						.findViewById(R.id.listitem_leader_three_view_other);
				//resUpdate(homeLeaderHolder.leadericons[10], MainActivity.MV_KEY);
				holder.leaderHolder = homeLeaderHolder;
				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_APPLIST_THREE:
				view = inflater.inflate(R.layout.item_home, null);
				holder.layoutType = item.layoutType;
				HomeItemHolder homeItemhodle = new HomeItemHolder();
				homeItemhodle.title_text = (TextView) view.findViewById(R.id.top_titile_textView);
				homeItemhodle.more_text = (TextView) view.findViewById(R.id.more_textView);
				HomecildItemHolder childhodle1 = new HomecildItemHolder();
				childhodle1.bgrelativelayout = (RelativeLayout) view.findViewById(R.id.relativeLayout_2);
				childhodle1.image_head_one = (ImageView) view.findViewById(R.id.head_imageView1);
				childhodle1.one_text1 = (TextView) view.findViewById(R.id.title_one_textView1);
				childhodle1.one_text2 = (TextView) view.findViewById(R.id.title_tow_textView1);
				childhodle1.one_text3 = (TextView) view.findViewById(R.id.title_tree_textView1);
				
				HomecildItemHolder childhodle2 = new HomecildItemHolder();
				childhodle2.bgrelativelayout = (RelativeLayout) view.findViewById(R.id.relativeLayout_3);
				childhodle2.image_head_one = (ImageView) view.findViewById(R.id.head_imageView2);
				childhodle2.one_text1 = (TextView) view.findViewById(R.id.title_one_textView2);
				childhodle2.one_text2 = (TextView) view.findViewById(R.id.title_tow_textView2);
				childhodle2.one_text3 = (TextView) view.findViewById(R.id.title_tree_textView2);
				
				HomecildItemHolder childhodle3 = new HomecildItemHolder();
				childhodle3.bgrelativelayout = (RelativeLayout) view.findViewById(R.id.relativeLayout_4);
				childhodle3.image_head_one = (ImageView) view.findViewById(R.id.head_imageView3);
				childhodle3.one_text1 = (TextView) view.findViewById(R.id.title_one_textView3);
				childhodle3.one_text2 = (TextView) view.findViewById(R.id.title_tow_textView3);
				childhodle3.one_text3 = (TextView) view.findViewById(R.id.title_tree_textView3);
				
				homeItemhodle.homeItemHolders.add(childhodle1);
				homeItemhodle.homeItemHolders.add(childhodle2);
				homeItemhodle.homeItemHolders.add(childhodle3);
				holder.homeItemhodler = homeItemhodle;
				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_APPLIST_MUTILE:
				/*if(!isshowAd){
				view = inflater.inflate(R.layout.listitem_ad, null);
				holder.layoutType = item.layoutType;
				HomeBannerHolder bannerHolder = new HomeBannerHolder();
				bannerHolder.hBand = (HomeAdvertiseBand) view
						.findViewById(R.id.home_ad);
				bannerHolder.hBand.setActivity(getActivity());
				holder.bannerHolder = bannerHolder;
				mHomeAdvertiseBand = bannerHolder.hBand;
				view.setTag(holder);
				isshowAd=true;
				}*/
				break;
			case BigItem.Type.LAYOUT_LOADING:
				LogUtil.e("cexo",
						"HomeFragment newView BigItem.Type.LAYOUT_LOADING");
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
			case BigItem.Type.LAYOUT_APPLIST_ONE:
				HomeRecHolder recHolder = holder.recHolder;
				for (final AppItem app : item.recItems) {
					recHolder.name.setText(app.name);
					StringBuilder sb = new StringBuilder();
					sb.append(app.cateName).append(" | ");
					sb.append(app.strLength).append(" | ");
					sb.append(app.downNum);
					recHolder.subLine.setText(sb.toString());
					String atip = getString(R.string.editor_word) + app.adesc;
					SpannableStringBuilder style = new SpannableStringBuilder(
							atip);
					style.setSpan(new ForegroundColorSpan(getResources()
							.getColor(R.color.home_rec_atip_color)), 0, 5,
							Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					if (TextUtils.isEmpty(app.adesc)) {
						recHolder.listitem_rec_atip.setVisibility(View.GONE);
					} else {
						recHolder.listitem_rec_atip.setText(style);
					}
					recHolder.rec_rating.setRating(app.rating);
					if (app.commcount != 0) {
						recHolder.rec_comm.setVisibility(View.VISIBLE);
						recHolder.rec_comm.setText("(" + app.commcount + ")");
					} else {
						recHolder.rec_comm.setVisibility(View.GONE);
					}
					recHolder.layout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							onAppClick(app, 0, Constants.TODETAIL);
						}
					});
					showStype(recHolder, app);
					if (TextUtils.isEmpty(app.iconUrl)) {
//						recHolder.icon
//								.setImageResource(R.drawable.app_empty_icon);
					}  else  {
						imageLoader.displayImage(app.iconUrl, recHolder.icon,
								options);
					}
					recHolder.btn
							.setOnClickListener(new DownloadBtnClickListener(
									app));
					DownloadTaskManager.getInstance().updateByState(
							getActivity(), recHolder.btn, app,
							recHolder.progress, recHolder.subLine, true, false,
							true, null, LogModel.L_HOME_REC);

				}
				break;
			case BigItem.Type.LAYOUT_LEADER_THREE:
					final HomeLeaderHolder leaderHolder = holder.leaderHolder;
					/*for (int i = 0; i < item.leaderItems.size()
							&& i < leaderHolder.leadernames.length; i++) {*/
					//leaderHolder.collaslinerlayout.collapse();
					
					leaderHolder.collaslinerlayout.setToggleView(leaderHolder.zhedie_imag);
					if(isoverre){
						
						leaderHolder.collaslinerlayout.collapse();
						isoverre = false;
					}
					leaderHolder.title_rela.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							//leaderHolder.collaslinerlayout.toggle();
							if(leaderHolder.collaslinerlayout.expanded){
								leaderHolder.collaslinerlayout.collapse();
							}else{
								leaderHolder.collaslinerlayout.expand();
							}
							//isoverre = true;
						}
					});
					for (int i = 0; i < 12
							&& i < leaderHolder.leaderviews.length; i++) {
						final int index = i;
							
							//final HLeaderItem app = item.leaderItems.get(i);
						if(funrapit){
							leaderHolder.leadericons[0].setVisibility(View.GONE);
							SharedPreferences txtNetResVer = MyApplication.getInstance().getSharedPreferences("yulever", 0);
							//int net_ver3 = txtNetResVer.getInt(skey, 0);
							//SharedPreferences txtResVer = MyApplication.getInstance().getSharedPreferences(tkey, 0);
							SharedPreferences.Editor editor3 = txtNetResVer.edit();
							editor3.putInt(MainActivity.JOKE_KEY, 1);
							editor3.commit();
						}
							leaderHolder.leaderviews[i]
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											
											if (index == 3) {
												//音乐
												//leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												startActivity(new Intent(MyApplication.getInstance(), NewRingActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
											
											} else if (index == 2) {
												//小说
												//leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												startActivity(new Intent(MyApplication.getInstance(), VoicedActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
												
											} else if(index == 1){
												//move
												//leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												startActivity(new Intent(MyApplication.getInstance(), VideoActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
												leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
											}else if(index == 0){
												//tv
												//leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												startActivity(new Intent(MyApplication.getInstance(), TVActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
											}else
											if (index == 4) {
												//糗百
												//如果有更新 通知底部取消红点儿
												leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												if(hasRefsh(MainActivity.JOKE_KEY)){
													Intent intent = new Intent("com.tyb.mark.jokcanclerepit");
													MyApplication.getInstance().sendBroadcast(intent);
												}
												leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												startActivity(new Intent(MyApplication.getInstance(), JokeActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
											} else if (index == 5) {
												//新闻
												//leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												if(hasRefsh(MainActivity.NEWS_KEY)){
													Intent intent = new Intent("com.tyb.mark.newscanclerepit");
													MyApplication.getInstance().sendBroadcast(intent);
												}
												startActivity(new Intent(MyApplication.getInstance(), NewsActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
											}else if(index == 6){
												//av
												leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												startActivity(new Intent(MyApplication.getInstance(), LiveActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
											}else if(index == 7){
												//综艺
												//leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												startActivity(new Intent(MyApplication.getInstance(), ShowActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
											}else if(index == 8){
												//动画
												//leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												startActivity(new Intent(MyApplication.getInstance(), AnimtionActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
											}else if(index == 9){
												//电台
												//leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												startActivity(new Intent(MyApplication.getInstance(), NovelActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
											}else if(index == 10){
												//MV
												//leaderHolder.leadericons[index].setVisibility(View.INVISIBLE);
												startActivity(new Intent(MyApplication.getInstance(), MVActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
														R.anim.push_left_out);
											}else if(index == 11){
												//意见
												StatService.trackCustomEvent(getActivity(), "FB", "");
												startActivity(new Intent(MyApplication.getInstance(), FeedBackActivity.class));
												HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
														R.anim.push_left_out);
											}
										
										}
									});
						
					}
					if(!isSend){
						Log.d("nnlog", "折叠--isSend----"+isSend);
						mHander.sendEmptyMessageDelayed(OVEERE, 2000);
						isSend = true;
					}
				break;
			case BigItem.Type.LAYOUT_APPLIST_THREE:
				//Log.d("test", "item--------"+item.homeItembeas.get(position).abumDes);
				HomeItemHolder itemHodler = holder.homeItemhodler;
				HomeItemBean homebean = null;
				for(int i=0;i<3;i++){
					homebean = item.homeItembeas.get(i);
					itemHodler.homeItemHolders.get(i).one_text1.setText(homebean.albumName);
					itemHodler.homeItemHolders.get(i).one_text2.setText(homebean.contentName);
					itemHodler.homeItemHolders.get(i).one_text3.setText(homebean.abumDes);
					
					
						String tags = (String) itemHodler.homeItemHolders.get(i).image_head_one.getTag();
						if(tags!=null){
						//	Log.d("nnlog", "不为空---"+homebean.albumName+"******url--"+homebean.ic_url+"--------tag-"+tags);
							if(homebean.ic_url.equals(tags)){
							//  if(itemHodler.homeItemHolders.get(i).image_head_one.getDrawable()==null){
					if(TextUtils.isEmpty(homebean.ic_url)){
						Log.d("mmmmm", "------");
						itemHodler.homeItemHolders.get(i).image_head_one.setImageResource(R.drawable.app_empty_icon);
						
					}else if(!imageLoader.getPause().get()){
							imageLoader.displayImage(Constants.IMG_URL+homebean.ic_url, itemHodler.homeItemHolders.get(i).image_head_one,mOptions);
						}
					
							//  }
							}else{
//								itemHodler.homeItemHolders.get(i).image_head_one.setImageResource(R.drawable.noti_icon);
//								imageLoader.displayImage(Constants.IMG_URL+homebean.ic_url, itemHodler.homeItemHolders.get(i).image_head_one,mOptions);
//								itemHodler.homeItemHolders.get(i).image_head_one.setTag(homebean.ic_url);
							}
						}else{
							if(TextUtils.isEmpty(homebean.ic_url)){
								Log.d("mmmmm", "------");
								itemHodler.homeItemHolders.get(i).image_head_one.setImageResource(R.drawable.app_empty_icon);
								
							}else if(!imageLoader.getPause().get()){
									imageLoader.displayImage(Constants.IMG_URL+homebean.ic_url, itemHodler.homeItemHolders.get(i).image_head_one,mOptions);
								}
						}
					
					//Log.d("test", "img---url"+Constants.IMG_URL+homebean.ic_url);
					itemHodler.homeItemHolders.get(i).bgrelativelayout
					.setOnClickListener(new MyhomeItemOnclickLisenl(homebean));
				}
				setTextview(itemHodler.title_text, homebean.type);
				itemHodler.more_text.setOnClickListener(new MymoreLisenler(homebean.type));
				break;
			case BigItem.Type.LAYOUT_APPLIST_MUTILE:
				// HomeBannerHolder bannerHolder = holder.bannerHolder;
				// bannerHolder.hBand.flushAdvertiseBand(item.homeItems);
				break;
			case BigItem.Type.LAYOUT_LOADING:
				break;
			case BigItem.Type.LAYOUT_LOADFAILED:
				break;
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		LogUtil.d("TAGTAG", "onAttach() ---> " + tag());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		homeAdLayout = LayoutInflater.from(getActivity()).inflate(
				R.layout.scrolladbyt, null);
		mViewPager = (HomeViewPaper)homeAdLayout.findViewById(R.id.viewpager);
		mViewPager.setmContext(HomeFragment.this.getActivity());
		mPointViewGroup = (ViewGroup) homeAdLayout.findViewById(R.id.point_layout);
	/*	mHomeAdvertiseBand = (HomeAdvertiseBand) homeAdLayout
				.findViewById(R.id.home_ad);
		mHomeAdvertiseBand.setActivity(getActivity());*/
	/*	isFlushHomeBand = true;*/
		// mImageFetcher.setImageSize(getDimenByResId(R.dimen.listitem_app_icon));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.e("+++++++++++++++++", flag + "===flag");
		if (flag) {
			requestDelay();
			flag = false;
		}
	}

	public boolean flag = true;

	@Override
	public void onResume() {
		if(moptionmenu!=null&&moptionmenu.isShowing())
		{
			moptionmenu.dismiss();
		}
		MobclickAgent.onPageStart("主页"); //统计页面
	    MobclickAgent.onResume(HomeFragment.this.getActivity());//时长  
		
		/*DownloadTaskManager.getInstance().fillBigItemStates(
				HomeFragment.this.getAdapter().mListItems,
				new int[] { DownloadTaskManager.FILL_TYPE_HOMEITEMS,
						DownloadTaskManager.FILL_TYPE_RECITEMS });*/
		super.onResume();
	}
	
	@Override
	public void onPause() {
		if(moptionmenu!=null&&moptionmenu.isShowing())
		{
			moptionmenu.dismiss();
		}
		MobclickAgent.onPageEnd("主页"); 
		MobclickAgent.onPause(HomeFragment.this.getActivity());//时长  
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		isFlushHomeBand = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	protected void onDownloadStateChanged() {
		// TODO Auto-generated method stub
		DownloadTaskManager.getInstance().fillBigItemStates(
				HomeFragment.this.getAdapter().mListItems,
				new int[] { DownloadTaskManager.FILL_TYPE_HOMEITEMS,
						DownloadTaskManager.FILL_TYPE_RECITEMS });
		HomeFragment.this.getAdapter().notifyDataSetChanged();
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
			/**
			 * the bestone modified start by huangxianjian date:2014-05-13
			 * for modified by name Judge 
			 */
			// 由于手机网游,最新收录,经典必玩的首页导航点击呈现的不一样,所以需作判断
			/*if (getString(R.string.txt_home_leader_latest_collection).equals(
					app.name)) {// 最新收录
				intent.putExtra(Constants.LIST_FROM,
						LogModel.L_HOME_LEADER_LATEST_COLLECTION);
			} else if (getString(R.string.txt_home_leader_classic_play).equals(
					app.name)) {// 经典必玩
				intent.putExtra(Constants.LIST_FROM,
						LogModel.L_HOME_LEADER_CLASSIC_PLAY);
			} */
			if(app.stype==1){
				intent.putExtra(Constants.LIST_FROM,
						LogModel.L_HOME_LEADER_LATEST_COLLECTION);
			}else {
				// 手机网游
				intent.putExtra(Constants.LIST_FROM, LogModel.L_HOME_LEADER);
			}
			/**
			 * the bestone modified end 
			 */
		}
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	@Override
	public void onAppClick(Object obj, int what, String action) {
		Intent intent = new Intent(action);
		if (obj instanceof AppItem) {
			AppItem app = (AppItem) obj;
			intent.putExtra("app", app);
			intent.putExtra(Constants.LIST_FROM, LogModel.L_HOME_REC);
		}
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	@Override
	public void addNewDataOnce() {
		Util.addListData(maContext, LogModel.L_HOME, LogModel.P_LIST, 1);
		listview.refreshlistview();
		MarketContext.getInstance().isGalleryMove =false;
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		for (BigItem bigItem : appendList) {
			if (bigItem.layoutType == BigItem.Type.LAYOUT_APPLIST_MUTILE) {
				/*add by zengxiao for:解决出现两个head头*/
				try{
				listview.removeHeaderView(homeAdLayout);
				}catch(Exception e){}
				listview.addHeaderView(homeAdLayout);
				/*add end*/
				initBannerView(bigItem.homeItems);
				appendList.remove(bigItem);
				homeADItems.clear();
				homeADItems.addAll(bigItem.homeADItems);
			}
			break;
		}
			
	}
	@Override
	protected void setHome(int pageIndex, List<BigItem> appendList) {
		for (BigItem bigItem : appendList) {
			if (bigItem.layoutType == BigItem.Type.LAYOUT_APPLIST_THREE) {	
				if(homeADItems.size()>0&&appendList.size()>2)
				{	int index=(int)(Math.random() *2+appendList.size()-2);
					BigItem bigItemtmp = new BigItem();
					bigItemtmp.layoutType = BigItem.Type.LAYOUT_APPLIST_THREE;
					bigItemtmp.homeItems.add(homeADItems.get(0));
					appendList.add(index,bigItemtmp);
					homeADItems.remove(0);
					Log.d("newzx","pageIndex="+pageIndex+"hoemADItemssize="+homeADItems.size()+"size="+bigItem.homeItems.size());
				}
				break;

			}

		}
	}
	public void initBannerView(List<AppItem> datas) {
		try{
		if(!isshowAd)
		{
		bannerOptions = new DisplayImageOptions.Builder()
				.cacheOnDisc()
				.delayBeforeLoading(200)
				.build();

		
		mViewPagerList = new ArrayList<View>();
		//资源的尾作为数组的头
		mViewPager.setMdatas(datas);
		addImageView(datas.get(datas.size()-1));
		for (int i = 0; i < datas.size(); i++) {
			addImageView(datas.get(i));
			addPoint(i);
			AppItem appitem = (AppItem) datas.get(i);

		}
		POINT_LENGTH=datas.size();
		//资源的头作为数组的尾，首尾相连
		addImageView(datas.get(0));
		
		PagerAdapter pagerAdapter = new CustomPagerAdapter(mViewPagerList);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setCurrentItem(mCurrentPagePosition);
		
		timer = new Timer();

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Message msg = handler.obtainMessage();				
				mHander.sendEmptyMessage(REFRESHAD);
			}
		}, 5000, 5000);
		isshowAd=true;
		}
		}catch(Exception e){}
		
		
	}

	@Override
	public void installedSucess(DownloadTask downloadTask) {
		LogUtil.i("appupdate", "touch installedSucess");

	}

	@Override
	public void downloadEnded(DownloadTask task, int totalTask, int progressTask) {
		// TODO Auto-generated method stub
		super.downloadEnded(task, totalTask, progressTask);
		LogUtil.i("appupdate", "touch downloadEnded");
	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {
		// TODO Auto-generated method stub
		super.downloadTaskDone(totalTask, progressTask, success);
		/*modify by zengxiao for:网络不好中断是刷新界面*/
		mHander.sendEmptyMessage(RFRESHUI);
	
		/*modiyf end*/
		LogUtil.i("appupdate", "touch downloadTaskDone");
	}
	/*@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.search_page_view:
			this.getActivity().startActivity(new Intent(Constants.TOSEARCH));
			this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		case R.id.top_downbutton:
			Intent downloadIntent = new Intent(this.getActivity(),
					DownLoadManageActivity.class);
			downloadIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
					DownLoadManageActivity.TYPE_FROM_DOWNLOAD);
			this.getActivity().startActivity(downloadIntent);
			this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
			
		}
		commonItemHolder.DownloadProgressBar2=(ProgressBar) view.findViewById(R.id.DownloadProgressBar2);
				commonItemHolder.progressnumtext2=(TextView) view.findViewById(R.id.progressnumtext2);
				commonItemHolder.downBtn2=(TextView) view.findViewById(R.id.bt_down_btn2);
		
	}*/
	/*add by zengxiao for 循环图片*/
	private void addImageView(final AppItem appItem) {
		try{
		View view=LayoutInflater.from(HomeFragment.this.getActivity()).inflate(R.layout.bestone_rec_ada_item, null);
		ImageView imageView=(ImageView) view.findViewById(R.id.appImage);
		imageLoader.displayImage(appItem.iconUrl, imageView,options);
		mViewPagerList.add(view);
		}catch(Exception e){}
	}

	private void addPoint(final int pIndex) {
		try {
			ImageView pointImageView = new ImageView(this.getActivity());
			LayoutParams layoutParams2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParams2.setMargins(3, 0, 3, 0);
			pointImageView.setLayoutParams(layoutParams2);
			pointImageView.setBackgroundResource(R.drawable.point_style);
			if (0 == pIndex) {
				pointImageView.setEnabled(false);
			}
			mPointViewGroup.addView(pointImageView);
		} catch (Exception e) {
			// TODO: handle exception
		}
		/*pointImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			mPointViewGroup.getChildAt(mCurrentIndex).setEnabled(true);
			mPointViewGroup.getChildAt(pIndex).setEnabled(false);
			mCurrentIndex=pIndex;
			mViewPager.setCurrentItem(mCurrentIndex+1, false);
			mCurrentPagePosition=mCurrentIndex+1;
			}
		});*/
	}
	/* 此方法是在状态改变的时候调用，其中arg0这个参数
	 * 有三种状态（0，1，2）。arg0 ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了，arg0==0的时辰默示什么都没做。
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
	 */
	@Override
	public void onPageScrollStateChanged(int pState) {
		if (ViewPager.SCROLL_STATE_IDLE == pState) {
			if (mIsChanged) {
				mIsChanged = false;
				mViewPager.setCurrentItem(mCurrentPagePosition, false);
			}
		}
	}

	/* 当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到调用
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled(int, float, int)
	 * arg0 :当前页面，及你点击滑动的页面
	 * arg1:当前页面偏移的百分比
	 * arg2:当前页面偏移的像素位置   
	 */
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	/* 此方法是页面跳转完后得到调用，pPosition是你当前选中的页面的Position（位置编号）。
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
	 */
	@Override
	public void onPageSelected(int pPosition) {
		
		mIsChanged = true;
		if (pPosition > POINT_LENGTH) {
			mCurrentPagePosition = 1;
		} else if (pPosition < 1) {
			mCurrentPagePosition = POINT_LENGTH;
		} else {
			mCurrentPagePosition = pPosition;
		}
		Log.i(TAG,"当前的位置是"+mCurrentPagePosition);
		setCurrentDot(mCurrentPagePosition);
	}
	private void setCurrentDot(int positon) {
		// 在界面上实际显示的序号是第1,2,3,4,而0，和5分别填充尾与首。而点的序号应该是0,1,2,3所以减1.
		positon = positon - 1;
		if (positon < 0 || positon > mViewPagerList.size() - 1 || mCurrentIndex == positon) {
			return;
		}
		mPointViewGroup.getChildAt(positon).setEnabled(false);
		mPointViewGroup.getChildAt(mCurrentIndex).setEnabled(true);
		mCurrentIndex = positon;
		pageindex=positon;
		mViewPager.setPageindex(positon);
	}
	
	/*add end*/
	/*add for 修改提示语*/
	@Override
	public void setButtonInvi() {
		// TODO Auto-generated method stub
		loadfailed.setText(MyApplication.getInstance().getString(R.string.nodata));
	}
	
	//-------------------------
	//add  bey  subo  增加 TV红点 提示 
	//------------------

	
	
	//是否有更新
	public boolean hasRefsh(String skey){
		SharedPreferences txtNetResVer = MyApplication.getInstance().getSharedPreferences("yulever", 0);
		//int net_ver = txtNetResVer.getInt(skey, 0);
		int net = txtNetResVer.getInt(skey, 0);
		if(net==1){
			return true;
		}
		return false;
	}
	public void resUpdate(WaterDrop view,String skey){
		
		SharedPreferences txtNetResVer = MyApplication.getInstance().getSharedPreferences("yulever", 0);
		//int net_ver = txtNetResVer.getInt(skey, 0);
		int net = txtNetResVer.getInt(skey, 0);
		//int net_ver = txtNetResVer.getInt("txt_net_ver_update", 0);
		//SharedPreferences txtResVer = MyApplication.getInstance().getSharedPreferences(tkey, 0);
		//int ver = txtResVer.getInt("txtresver", 0);
		if (net>1) {
			view.setVisibility(View.VISIBLE);
			if(net>=100){
				view.setText("99");
			}else{
				view.setText(String.valueOf(net));
			}
			return;
		}
		view.setVisibility(View.GONE);
		
	}
	
	private void setTextview(TextView tv,String type){
		switch (Integer.parseInt(type)) {
		case 2://音乐
			tv.setText(getString(R.string.music_string));
			break;
		case 4://tv
			tv.setText(getString(R.string.tv_string));
			break;
		case 6://动画
			tv.setText(getString(R.string.anime_string));
			break;
		case 8://电台
			tv.setText(getString(R.string.voiced_string));
			break;
		case 10://小说
			tv.setText(getString(R.string.book_string));
			break;
		default:
			break;
		}
	}
	
	class MymoreLisenler implements OnClickListener{

		public String mytype;
		public MymoreLisenler(String type){
			this.mytype = type;
		}
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch (Integer.parseInt(mytype)) {
			case 2://音乐
				//setIsshow(MainActivity.MUSIC_KEY, MainActivity.SMUSIC_KEY);
				startActivity(new Intent(MyApplication.getInstance(), NewRingActivity.class));
				HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				break;
			case 4://tv
				startActivity(new Intent(MyApplication.getInstance(), TVActivity.class));
				HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				break;
			case 6://动画
				startActivity(new Intent(MyApplication.getInstance(), AnimtionActivity.class));
				HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				break;
			case 8://电台
				startActivity(new Intent(MyApplication.getInstance(), NovelActivity.class));
				HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				break;
			case 10://小说
				startActivity(new Intent(MyApplication.getInstance(), VoicedActivity.class));
				HomeFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				break;
			default:
				break;
			}
		}
		
	}
//	class MyTestImageloadingback implements ImageBack{
//		
//		private ImageView mimg =null;
//		public MyTestImageloadingback(ImageView img){
//			this.mimg = img;
//		}
//		@Override
//		public void getBitMap(Bitmap bitmap) {
//			// TODO Auto-generated method stub
//			mimg.setImageBitmap(bitmap);
//		}
//		
//	}
	class Myfunrecevie extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			funrapit = true;
			HomeFragment.this.getAdapter().notifyDataSetChanged();
		}
		
	}
	
	class MyhomeItemOnclickLisenl implements OnClickListener{
		private HomeItemBean mbeana;
		public MyhomeItemOnclickLisenl(HomeItemBean beana){
			this.mbeana  = beana;
		}
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//Toast.makeText(getActivity(), "--"+mbeana.albumId, Toast.LENGTH_LONG).show();
			HomeToutils.getTo(mbeana, HomeFragment.this.getActivity());
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
