package com.byt.market.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.byt.market.Constants;
import com.byt.market.OnViewChangeListener;
import com.byt.ar.R;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.util.SharedPrefManager;

public class ScreenshotBand extends FrameLayout implements OnViewChangeListener {

	public static final String TAG = "newGridAdvertiseBand";
	private boolean scroll = false;

	private int currentItem = 0;
	private CustomScreenScrollLayout mScrollLayout;
	private List<ImageView> imgs;
	private int count;
	private LinearLayout pointLLayout;
	private Activity activity;

	private List<String> datas;

	public CustomScreenScrollLayout getScrollLayout() {
		return mScrollLayout;
	}

	public ScreenshotBand(Context context) {
		super(context);
		inflate(context, R.layout.screen_ad_band, this);
		initView();
		datas = new ArrayList<String>();
	}

	/**
	 * @param context
	 * @param appadvertisetaskmark
	 */
	public ScreenshotBand(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.screen_ad_band, this);
		initView();
		datas = new ArrayList<String>();
		Index = 0;
	}

	int Index = 0;
	ArrayList<String> AD_ImgList = new ArrayList<String>();
	String bimgs;

	public void flushAdvertiseBand(ImageLoader imageLoader,DisplayImageOptions options,String bimgs, List<String> datas) {
		this.bimgs = bimgs;
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
			
			if (!AD_ImgList.contains(url)) {
				ImageView imageView = (ImageView) mScrollLayout
						.getChildAt(i);
				if(!TextUtils.isEmpty(url)){
					imageLoader.displayImage(url,imageView,options);
				}
				
				AD_ImgList.add(url);
			}
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			setcurrentPoint(msg.arg1);
			mScrollLayout.snapToScreen(msg.arg1);
		};
	};

	private void initView() {
		MadHandler mmHandler = new MadHandler();
		mScrollLayout = (CustomScreenScrollLayout) findViewById(R.id.SScrollLayout);
		mScrollLayout.setHandler(mmHandler);
		pointLLayout = (LinearLayout) findViewById(R.id.Sllayout);
	}

	private void createView(int count) {
		List<ImageView> list_iv = createScrollViewByCount(count);
		List<ImageView> list_dot = createDotsByCount(count);
		for (ImageView imageView : list_iv) {
			mScrollLayout.addView(imageView);
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

	private List<ImageView> createScrollViewByCount(int count) {
		List<ImageView> list = new ArrayList<ImageView>();
		for (int i = 0; i < count; i++) {
			ImageView iv = new ImageView(getContext());
			iv.setTag("iv_" + i);
//			iv.setScaleType(ScaleType.FIT_XY);
			iv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
			list.add(iv);
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
			lParams.leftMargin = 5;
			lParams.rightMargin = 5;
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
			case 1228:
				handleClickEvent(message.arg1);
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

	public void handleClickEvent(int screen) {
		Intent intent = new Intent(Constants.TOBSREEN);
		intent.putExtra("bimgs", bimgs);
		intent.putExtra("screen", screen);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
