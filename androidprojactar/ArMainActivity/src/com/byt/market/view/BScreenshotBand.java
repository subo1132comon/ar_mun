package com.byt.market.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;

import com.byt.ar.R;
import com.byt.market.Constants;
import com.byt.market.OnViewChangeListener;
import com.byt.market.activity.BScreenFrame;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.util.SharedPrefManager;

public class BScreenshotBand extends FrameLayout implements
		OnViewChangeListener {

	public static final String TAG = "BScreenshotBand";
	private int currentItem = 0;
	private CustomBScreenScrollLayout mScrollLayout;
	private List<ImageView> imgs;
	private int count;
	private LinearLayout pointLLayout;

	List<String> datas;

	public BScreenshotBand(Context context) {
		super(context);
		inflate(context, R.layout.bscreen_ad_band, this);
		initView();
		datas = new ArrayList<String>();
	}

	/**
	 * @param context
	 * @param appadvertisetaskmark
	 */
	public BScreenshotBand(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.bscreen_ad_band, this);
		initView();
		datas = new ArrayList<String>();
		Index = 0;
	}

	int Index = 0;
	ArrayList<String> AD_ImgList = new ArrayList<String>();

	public void flushAdvertiseBand(ImageLoader mImageLoader,DisplayImageOptions options,List<String> datas) {
		if (this.datas != null && this.datas.size() > 0)
			return;
		this.datas = datas;
		count = datas.size();
		createView(count);
		String iconUrl = null;
		mScrollLayout.setTag(datas);
		for (int i = 0; i < datas.size(); i++) {
			iconUrl = (String) datas.get(i);
			String url;
			if (iconUrl.startsWith("http://")) {
				url = iconUrl;
			} else {
				url = Constants.IMG_URL + iconUrl;
			}
			if (!AD_ImgList.contains(iconUrl)) {
				ImageView imageView = (ImageView) mScrollLayout
						.getChildAt(i).findViewById(R.id.appImage);
				if(!TextUtils.isEmpty(iconUrl)){
					mImageLoader.displayImage(iconUrl,imageView,options);
				}
					AD_ImgList.add(iconUrl);
				}
			}
	}

	public void setCurrentView(int which) {
		if (mScrollLayout != null && which < mScrollLayout.getChildCount()) {
			setcurrentPoint(which);
			mScrollLayout.snapToScreen(which);
		}
	}

	private void initView() {
		MadHandler mmHandler = new MadHandler();
		mScrollLayout = (CustomBScreenScrollLayout) findViewById(R.id.bSScrollLayout);
		mScrollLayout.setHandler(mmHandler);
		pointLLayout = (LinearLayout) findViewById(R.id.bSllayout);
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
		imgs.get(currentItem).setBackgroundResource(R.drawable.dot_focused);
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
			iv.setBackgroundResource(R.drawable.dot_normal);
			list.add(iv);
		}
		return list;
	}

	class MadHandler extends Handler {

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case 1230:
				BScreenFrame.paFrame.finishWindow();
				break;
			case 1229:
				/*
				 * if (LogModel.hasMap.size() == 0 ||
				 * !LogModel.hasMap.containsKey(LogModel.L_BANNER) ||
				 * (LogModel.hasMap.containsKey(LogModel.L_BANNER) &&
				 * LogModel.hasMap .get(LogModel.L_BANNER) == 1))
				 * Util.addListData(marketContext, LogModel.L_BANNER);
				 */
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
		imgs.get(currentItem).setBackgroundResource(R.drawable.dot_normal);
		imgs.get(position).setBackgroundResource(R.drawable.dot_focused);
		currentItem = position;
	}

	public void handleClickEvent() {/*
									 * List<String> AppItemlist = datas; if
									 * (AppItemlist != null &&
									 * AppItemlist.size() > 0 && currentItem <
									 * AppItemlist.size()) {
									 * 
									 * AppItem appItem = (AppItem)
									 * AppItemlist.get(currentItem); if (appItem
									 * != null) { int cateid = appItem.cateid;
									 * 
									 * if (cateid == -1) { Intent intent = new
									 * Intent(Constants.TOLIST);
									 * intent.putExtra("bean", appItem);
									 * intent.putExtra("from",
									 * LogModel.L_BANNER); //
									 * intent.putExtra("what",
									 * AppSubInfoFrame.TYPE_SPECIAL);
									 * ((Activity)
									 * getContext()).startActivityForResult
									 * (intent, 0); ((Activity)
									 * getContext()).overridePendingTransition(
									 * R.anim.push_left_in,
									 * R.anim.push_left_out); } else { Intent
									 * intent = new Intent(Constants.TODETAIL);
									 * intent.putExtra("obj", appItem);
									 * intent.putExtra("from",
									 * LogModel.L_BANNER); ((Activity)
									 * getContext
									 * ()).startActivityForResult(intent, 0);
									 * ((Activity)
									 * getContext()).overridePendingTransition(
									 * R.anim.push_left_in,
									 * R.anim.push_left_out); }
									 * 
									 * } }
									 */
	}
}
