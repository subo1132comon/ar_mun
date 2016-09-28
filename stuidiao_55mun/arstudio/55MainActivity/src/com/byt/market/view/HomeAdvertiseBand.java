package com.byt.market.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.byt.market.R;
import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.OnViewChangeListener;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.bitmaputil.core.display.BannerBitmapDisplayer;
import com.byt.market.bitmaputil.core.display.RoundedBitmapDisplayer;
import com.byt.market.data.AppItem;
import com.byt.market.log.LogModel;
import com.byt.market.util.Util;

/**
 * 
 * @author Administrator
 * 
 */
public class HomeAdvertiseBand extends LinearLayout implements
		OnViewChangeListener {

	public static final String TAG = "newGridAdvertiseBand";
	private boolean scroll = false;

	private List<View> dots;
	private int currentItem = 0;
	private CustomScrollLayout mScrollLayout;
	private List<ImageView> imgs;
	private int count;
	private LinearLayout pointLLayout;
	private Activity activity;
	private static HomeAdvertiseBand Instance;
	List<AppItem> datas;
	private MarketContext marketContext;
	private Context mContext;

	public HomeAdvertiseBand(Context context) {
		super(context);
		inflate(context, R.layout.home_ad_band, this);
		marketContext = MarketContext.getInstance();
		mContext = context;
		initView();
		datas = new ArrayList<AppItem>();
		
	}

	public static HomeAdvertiseBand getInstance(Context context) {
		if (Instance == null) {
			Instance = new HomeAdvertiseBand(context);
		}
		return Instance;
	}

	/**
	 * @param context
	 * @param appadvertisetaskmark
	 */
	public HomeAdvertiseBand(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.home_ad_band, this);
		marketContext = MarketContext.getInstance();
		initView();
		datas = new ArrayList<AppItem>();
		Index = 0;
	}

	int Index = 0;
	ArrayList<Integer> AD_ImgList = new ArrayList<Integer>();

	public void flushAdvertiseBand(ImageLoader mImageLoader,DisplayImageOptions options,List<AppItem> datas) {
		if (this.datas != null && this.datas.size() > 0)
			return;
		/******/
		this.datas = datas;
		count = datas.size();
		if(count>1){
			pointLLayout.setVisibility(View.VISIBLE);
		}else{
			pointLLayout.setVisibility(View.GONE);
		}
		createView(count);
		AppItem appitem = null;
		mScrollLayout.setTag(datas);
		for (int i = 0; i < datas.size(); i++) {
			appitem = (AppItem) datas.get(i);
			final int app_id_this = appitem.sid;
			if (!AD_ImgList.contains(app_id_this)) {
				ImageView imageView = (ImageView) mScrollLayout
						.getChildAt(i).findViewById(R.id.appImage);
				if(!TextUtils.isEmpty(appitem.iconUrl)){
					mImageLoader.displayImage(appitem.iconUrl,imageView,options);
				}
				
				AD_ImgList.add(app_id_this);
			}
		}
		startScroll();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			setcurrentPoint(msg.arg1);
			mScrollLayout.snapToScreen(msg.arg1);
		};
	};
	Timer timer;
	boolean isNormal = true;

	public void startScroll() {
		if (!scroll) {
			scroll = true;
			timer = new Timer();

			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					Message msg = handler.obtainMessage();
					int current = 0;
					if (isNormal) {
						current = (currentItem + 1) % count;
						if (current == 0) {
							isNormal = false;
							current = count - 2;
						}
					} else {
						current = (currentItem - 1) % count;
						if (current == -1) {
							isNormal = true;
							current = 1;
						}
					}
					msg.arg1 = current;
					handler.sendMessage(msg);
				}
			}, 5000, 5000);
		}
	}

	public void stopScroll() {
		// scroll = false;
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void initView() {
		MadHandler mmHandler = new MadHandler();
		mScrollLayout = (CustomScrollLayout) findViewById(R.id.ScrollLayout);
		mScrollLayout.setHandler(mmHandler);
		pointLLayout = (LinearLayout) findViewById(R.id.llayout);
	}

	private void createView(int count) {
		List<View> list_iv = createScrollViewByCount(count);
		List<ImageView> list_dot = createDotsByCount(count);
		for (View view : list_iv) {
			mScrollLayout.addView(view);
		}

		for (ImageView imageView : list_dot) {
			pointLLayout.addView(imageView);
		}
		this.count = count;
		imgs = list_dot;
		currentItem = 0;
		imgs.get(currentItem).setBackgroundResource(
				R.drawable.icon_nav_selected);
		mScrollLayout.SetOnViewChangeListener(this);
	}

	private List<View> createScrollViewByCount(int count) {
		List<View> list = new ArrayList<View>();
		LayoutInflater inflater=LayoutInflater.from(getContext());
		for (int i = 0; i < count; i++) {
//			ImageView iv = new ImageView(getContext());
			View view=inflater.inflate(R.layout.bestone_rec_ada_item, null);
//			iv.setTag("iv_" + i);
//			iv.setScaleType(ScaleType.FIT_XY);
//			iv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
//					LayoutParams.FILL_PARENT));
			list.add(view);
		}
		return list;
	}

	private List<ImageView> createDotsByCount(int count) {
		List<ImageView> list = new ArrayList<ImageView>();
		for (int i = 0; i < count; i++) {
			ImageView iv = new ImageView(getContext());
			iv.setTag(i);
			LayoutParams lParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			lParams.leftMargin = 3;
			lParams.rightMargin = 3;
			iv.setLayoutParams(lParams);
			iv.setBackgroundResource(R.drawable.icon_nav_normal);
			list.add(iv);
		}
		return list;
	}

	class MadHandler extends Handler {

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case 1226:
				handleClickEvent();
				break;
			case 1227:
				if (LogModel.hasMap.size() == 0
						|| !LogModel.hasMap.containsKey(LogModel.L_BANNER)
						|| (LogModel.hasMap.containsKey(LogModel.L_BANNER) && LogModel.hasMap
								.get(LogModel.L_BANNER) == 1))
					Util.addListData(marketContext, LogModel.L_BANNER);

				break;
			}
		}
	}

	@Override
	public void OnViewChange(int position) {
		setcurrentPoint(position);
	}

	private void setcurrentPoint(int position) {
		if (position < 0 || position > count - 1 || currentItem == position) {
			return;
		}
		imgs.get(currentItem).setBackgroundResource(R.drawable.icon_nav_normal);
		imgs.get(position).setBackgroundResource(R.drawable.icon_nav_selected);
		currentItem = position;
	}

	public void handleClickEvent() {
		List<AppItem> AppItemlist = datas;
		if (AppItemlist != null && AppItemlist.size() > 0
				&& currentItem < AppItemlist.size()) {
			AppItem appItem = (AppItem) AppItemlist.get(currentItem);
			if (appItem != null) {
				int cateid = appItem.cateid;
				if (cateid == -1 || cateid == -2) {
					Intent intent = new Intent(Constants.TOLIST);
					intent.putExtra("app", appItem);
					intent.putExtra("from", LogModel.L_BANNER);
					activity.startActivityForResult(intent, 0);
					activity.overridePendingTransition(R.anim.push_left_in,
							R.anim.push_left_out);
				} else {
					Intent intent = new Intent(Constants.TODETAIL);
					intent.putExtra("app", appItem);
					intent.putExtra("from", LogModel.L_BANNER);
					activity.startActivityForResult(intent, 0);
					activity.overridePendingTransition(R.anim.push_left_in,
							R.anim.push_left_out);
				}
			}
		}
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
