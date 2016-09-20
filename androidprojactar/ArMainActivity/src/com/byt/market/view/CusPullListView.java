package com.byt.market.view;

import java.util.Date;

import com.byt.market.MarketContext;
import com.byt.ar.R;
import com.byt.market.tools.LogCart;
import com.byt.market.util.LogUtil;
import com.byt.market.util.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CusPullListView extends ListView{

	private static final String TAG = "listview";

	private final static int RELEASE_To_REFRESH = 0;
	private final static int PULL_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;
	SharedPreferences mydate;
	// 实际的padding的距离与界面上偏移距离的比例
	private final static int RATIO = 2;
	private TextView refreshtext;
	LinearLayout head_contentLayout;
	private LayoutInflater inflater;
	private ImageView refresh_ok;
	private LinearLayout headView;

	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;


	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean isRecored;
	private boolean canrefresh;

	private int headContentWidth;
	private int headContentHeight;

	private int startY;
	private int firstItemIndex;

	private int state;

	private boolean isBack;

	public boolean isCanrefresh() {
		return canrefresh;
	}

	public void setCanrefresh(boolean canrefresh) {
		this.canrefresh = canrefresh;
	}

	private OnRefreshListener refreshListener;

	public boolean isRefreshable() {
		return isRefreshable;
	}

	public void setRefreshable(boolean isRefreshable) {
		this.isRefreshable = isRefreshable;
	}

	private boolean isRefreshable;

	public CusPullListView(Context context) {
		super(context);
		init(context);
	}

	public CusPullListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	private void init(Context context) {
		mydate=context.getSharedPreferences("MYREFRESHDATA", 0);
		setCacheColorHint(context.getResources().getColor(R.color.transparent));
		inflater = LayoutInflater.from(context);
		
		headView = (LinearLayout) inflater.inflate(R.layout.head, null);
		refreshtext=(TextView) headView.findViewById(R.id.refreshtext);
		head_contentLayout=(LinearLayout) headView.findViewById(R.id.head_contentLayout);
		arrowImageView = (ImageView) headView
				.findViewById(R.id.head_arrowImageView);
		arrowImageView.setMinimumWidth(70);
		arrowImageView.setMinimumHeight(50);
		progressBar = (ProgressBar) headView
				.findViewById(R.id.head_progressBar);
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView = (TextView) headView
				.findViewById(R.id.head_lastUpdatedTextView);
		
		refresh_ok=(ImageView) headView
				.findViewById(R.id.refresh_ok);
		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		headContentWidth = headView.getMeasuredWidth();

		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();

		LogUtil.v("size", "width:" + headContentWidth + " height:"
				+ headContentHeight);

		addHeaderView(headView, null, false);

		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);
		state = DONE;
		isRefreshable = false;
		canrefresh=false;
		mMaximumVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
	}
	int mMaximumVelocity;
	public void setFirstIndex(int firstVisiableItem){
		firstItemIndex = firstVisiableItem;
	}
	private void releaseVelocityTracker() {  
        if(null != mVelocityTracker) {  
            mVelocityTracker.clear();  
            mVelocityTracker.recycle();  
            mVelocityTracker = null;  
        }  
    }  
    private void acquireVelocityTracker(final MotionEvent event) {  
        if(null == mVelocityTracker) {  
            mVelocityTracker = VelocityTracker.obtain();  
        }  
        mVelocityTracker.addMovement(event);  
    }  
    private int  getTime(float vy){
    	int time=50;
//    	if(vy<=500){
//    		time=50;
//    	}else if (vy<1000) {
//    		time=50;
//		}else if (vy<=1500) {
//    		time=50;
//		}else if (vy<=2000) {
//    		time=50;
//		}else if (vy<=3000) {
//    		time=100;
//		}else if (vy<=4000) {
//    		time=150;
//		}else if (vy<=5000) {
//    		time=200;
//		}else if (vy<=6000) {
//    		time=200;
//		}else if (vy>=7000) {
//    		time=200;
//		}
    	return time;
    }

	VelocityTracker mVelocityTracker;
	public boolean onTouchEvent(MotionEvent event) {
		 acquireVelocityTracker(event);  
	     final VelocityTracker verTracker = mVelocityTracker;  
		if(event.getAction()==MotionEvent.ACTION_UP||event.getAction()==MotionEvent.ACTION_CANCEL){
			
		        verTracker.computeCurrentVelocity(1000, mMaximumVelocity);  
                final float velocityY = verTracker.getYVelocity(); 
                if(scrollRefreshListener!=null){
                	scrollRefreshListener.scrollRefreshListener(getTime(Math.abs(velocityY)));
                }
		        releaseVelocityTracker();
		}
		
		if (isRefreshable) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//Log.d("rmyzx","CusPullListView MotionEvent.ACTION_DOWN");
				if (firstItemIndex == 0 && !isRecored) {
					isRecored = true;
					startY = (int) event.getY();
					LogUtil.v(TAG, "在down时候记录当前位置‘");
				}
				break;

			case MotionEvent.ACTION_UP:
			//	Log.d("rmyzx","CusPullListView MotionEvent.ACTION_UP");
				if (state != REFRESHING && state != LOADING) {
					if (state == DONE) {
						// 什么都不做
						changeHeaderViewByState();
					}
					if (state == PULL_To_REFRESH) {
						state = DONE;
						changeHeaderViewByState();

						LogUtil.v(TAG, "由下拉刷新状态，到done状态");
					}
					if (state == RELEASE_To_REFRESH) {
						state = REFRESHING;
						changeHeaderViewByState();
						onRefresh();

						LogUtil.v(TAG, "由松开刷新状态，到done状态");
					}
				}

				isRecored = false;
				isBack = false;

				break;

			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();

				if (!isRecored && firstItemIndex == 0) {
					LogUtil.v(TAG, "在move时候记录下位置");
					isRecored = true;
					startY = tempY;
				}

				if (state != REFRESHING && isRecored && state != LOADING) {

					// 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动

					// 可以松手去刷新了
					if (state == RELEASE_To_REFRESH) {

						setSelection(0);

						// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
						if (((tempY - startY) / RATIO < headContentHeight)
								&& (tempY - startY) > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();

							LogUtil.v(TAG, "由松开刷新状态转变到下拉刷新状态");
						}
						// 一下子推到顶了
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();

							LogUtil.v(TAG, "由松开刷新状态转变到done状态");
						}
						// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
						else {
							// 不用进行特别的操作，只用更新paddingTop的值就行了
						}
					}
					// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
					if (state == PULL_To_REFRESH) {

						setSelection(0);

						// 下拉到可以进入RELEASE_TO_REFRESH的状态
						if ((tempY - startY) / RATIO >= headContentHeight) {
							state = RELEASE_To_REFRESH;
							isBack = true;
							changeHeaderViewByState();

							LogUtil.v(TAG, "由done或者下拉刷新状态转变到松开刷新");
						}
						// 上推到顶了
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();

							LogUtil.v(TAG, "由DOne或者下拉刷新状态转变到done状态");
						}
					}

					// done状态下
					if (state == DONE) {
						if (tempY - startY > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
						}
					}

					// 更新headView的size
					if (state == PULL_To_REFRESH) {
						headView.setPadding(0, -1 * headContentHeight
								+ (tempY - startY) / RATIO, 0, 0);
					//	Log.d("rmyzx","CusPullListView MotionEvent.Move 1="+(headContentHeight
					//			+ (tempY - startY) / RATIO));
					}

					// 更新headView的paddingTop
					if (state == RELEASE_To_REFRESH) {
						headView.setPadding(0, (tempY - startY) / RATIO
								- headContentHeight, 0, 0);
					//	Log.d("rmyzx","CusPullListView MotionEvent.Move 2="+((tempY - startY) / RATIO
					//			- headContentHeight));
					}

				}

				break;
			}
		}

		return super.onTouchEvent(event);
	}

	// 当状态改变时候，调用该方法，以更新界面
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_To_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			refreshtext.setVisibility(View.GONE);
			head_contentLayout.setVisibility(View.VISIBLE);
			
			tipsTextview.setVisibility(View.VISIBLE);
			refresh_ok.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);
			
			
			tipsTextview.setText(getContext().getString(R.string.cus_refreshref));

			LogUtil.v(TAG, "当前状态，松开刷新");
			break;
		case PULL_To_REFRESH:
			long data=mydate.getLong("REFRESHDATA", 0);
			if(data!=0)
			{
				lastUpdatedTextView.setVisibility(View.VISIBLE);
				lastUpdatedTextView.setText(getContext().getString(R.string.cus_update) +  Util.dateFormat2(data));
			}
			else{
				lastUpdatedTextView.setVisibility(View.GONE);
			}
			refresh_ok.setVisibility(View.GONE);

			progressBar.setVisibility(View.GONE);
			refreshtext.setVisibility(View.GONE);
			head_contentLayout.setVisibility(View.VISIBLE);
			//lastUpdatedTextView.setVisibility(View.VISIBLE);
			tipsTextview.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText(getContext().getString(R.string.cus_refreshpull));
			} else {
				tipsTextview.setText(getContext().getString(R.string.cus_refreshpull));
			}
			LogUtil.v(TAG, "当前状态，下拉刷新");
			break;

		case REFRESHING:
			refresh_ok.setVisibility(View.GONE);

			headView.setPadding(0, 0, 0, 0);
			refreshtext.setVisibility(View.VISIBLE);
			head_contentLayout.setVisibility(View.GONE);
			refreshtext.setText(getContext().getString(R.string.cus_refreshnow));
			progressBar.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText(getContext().getString(R.string.cus_refreshnow));

			LogUtil.v(TAG, "当前状态,正在刷新...");
			break;
		case DONE:			
					refresh_ok.setVisibility(View.GONE);
					// TODO Auto-generated method stub
					headView.setPadding(0, -1 * headContentHeight, 0, 0);
					refreshtext.setVisibility(View.GONE);
					head_contentLayout.setVisibility(View.VISIBLE);
					progressBar.setVisibility(View.GONE);
					lastUpdatedTextView.setVisibility(View.GONE);
					arrowImageView.clearAnimation();
					arrowImageView.setImageResource(R.drawable.arrow);
					tipsTextview.setText(getContext().getString(R.string.cus_refreshpull));						
			LogUtil.v(TAG, "当前状态，done");
			break;
		}
	}

	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		canrefresh=true;
		isRefreshable = true;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void onRefreshComplete() {
		state = DONE;
		SharedPreferences.Editor edit=mydate.edit();
		edit.putLong("REFRESHDATA", System.currentTimeMillis());
		edit.commit();
		changeHeaderViewByState();
	}

	private void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}

	// 此方法直接照搬自网络上的一个下拉刷新的demo，此处是“估计”headView的width以及height
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setAdapter(BaseAdapter adapter) {
		lastUpdatedTextView.setText(getContext().getString(R.string.cus_update) + Util.dateFormat(new Date().toLocaleString()));
		super.setAdapter(adapter);
	}
	public void setrefreshok()
	{
		refresh_ok.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		refreshtext.setText(R.string.refreshok);	
	new Handler().postDelayed(new Runnable() {
		
		@Override
		public void run() {
		onRefreshComplete();
			
		}
	}, 700);
	
	}
	private ScrollRefreshListener scrollRefreshListener;
	
	public void setScrollRefreshListener(ScrollRefreshListener scrollRefreshListener) {
		this.scrollRefreshListener = scrollRefreshListener;
	}

	public interface ScrollRefreshListener{
		public void scrollRefreshListener(long time);
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (MarketContext.getInstance().isGalleryMove) {
			
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}
	public void refreshlistview() {
		if(PULL_To_REFRESH==state)
		{
			state=DONE;
			changeHeaderViewByState();
		}
	}
}
