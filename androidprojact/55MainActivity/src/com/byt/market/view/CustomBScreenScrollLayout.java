package com.byt.market.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.byt.market.MyApplication;
import com.byt.market.MarketContext;
import com.byt.market.OnViewChangeListener;
import com.byt.market.util.LogUtil;

public class CustomBScreenScrollLayout extends ViewGroup {

	private static final String TAG = "ScrollLayout";

	private VelocityTracker mVelocityTracker; // 鐢ㄤ簬鍒ゆ柇鐢╁姩鎵嬪娍

	private static final int SNAP_VELOCITY = 600;

	private Scroller mScroller; // 婊戝姩鎺у埗鍣?

	private int mCurScreen;

	private int mDefaultScreen = 0;

	private float mLastMotionX;
	private int firstX,firstY;

	// private int mTouchSlop;

	// private static final int TOUCH_STATE_REST = 0;
	// private static final int TOUCH_STATE_SCROLLING = 1;
	// private int mTouchState = TOUCH_STATE_REST;

	private OnViewChangeListener mOnViewChangeListener;

	public CustomBScreenScrollLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public CustomBScreenScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public CustomBScreenScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub

		init(context);
	}

	private void init(Context context) {
		mCurScreen = mDefaultScreen;

		// mTouchSlop =
		// ViewConfiguration.get(getContext()).getScaledTouchSlop();

		mScroller = new Scroller(context);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

		if (changed) {
			int childLeft = 0;
			final int childCount = getChildCount();

			for (int i = 0; i < childCount; i++) {
				final View childView = getChildAt(i);
				if (childView.getVisibility() != View.GONE) {
					final int childWidth = childView.getMeasuredWidth();
					childView.layout(childLeft-2, 0, childLeft + childWidth+2,
							childView.getMeasuredHeight());
					childLeft += childWidth;
				}
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		scrollTo(mCurScreen * width, 0);

	}

	public void snapToDestination() {
		int screenWidth = getWidth();
		int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		snapToScreen(destScreen);
	}

	public void snapToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (getScrollX() != (whichScreen * MyApplication.mScreenWidth)) {
			int delta = whichScreen * getWidth() - getScrollX();

			mScroller.startScroll(getScrollX(), 0, whichScreen * MyApplication.mScreenWidth
					- getScrollX(), 0, Math.abs(delta) * 2);

			mCurScreen = whichScreen;
			invalidate(); // Redraw the layout

			if (mOnViewChangeListener != null) {
				mOnViewChangeListener.OnViewChange(mCurScreen);
			}
		}
	}

	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		boolean flag = false;
		float ox = x;
		// System.out.println(action);
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			MarketContext.getInstance().isGalleryMove = true;
			// System.out.println("ACTION_DOWN");
			ox = event.getX();
			if (mVelocityTracker == null) {
				mVelocityTracker = VelocityTracker.obtain();
				mVelocityTracker.addMovement(event);
			}

			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			firstX = (int)x;
			firstY = (int)y;
			break;

		case MotionEvent.ACTION_MOVE:
			int deltaX = (int) (mLastMotionX - x);
			// System.out.println("ACTION_MOVE");
			if (IsCanMove(deltaX)) {
				if (mVelocityTracker != null) {
					mVelocityTracker.addMovement(event);
				}
				mLastMotionX = x;

				scrollBy(deltaX, 0);
			}

			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			MarketContext.getInstance().isGalleryMove = false;
			// System.out.println("ACTION_UP");
			// System.out.println(event.getX());
			// System.out.println(ox);
			int velocityX = 0;
			if (mVelocityTracker != null) {
				// System.out.println(mVelocityTracker.getYVelocity());
				mVelocityTracker.addMovement(event);
				mVelocityTracker.computeCurrentVelocity(1000);
				velocityX = (int) mVelocityTracker.getXVelocity();
			}

			// System.out.println(velocityX);
			if (Math.abs(velocityX) <= 50 && Math.abs(velocityX) >= 0)
				flag = true;

			if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
				// Fling enough to move left
				LogUtil.i(TAG, "snap left");
				snapToScreen(mCurScreen - 1);
				Message msg = new Message();
				msg.what = 1231;
				mmHandler.sendMessage(msg);
			} else if (velocityX < -SNAP_VELOCITY
					&& mCurScreen < getChildCount() - 1) {
				// Fling enough to move right
				LogUtil.i(TAG, "snap right");
				snapToScreen(mCurScreen + 1);
				Message msg = new Message();
				msg.what = 1231;
				mmHandler.sendMessage(msg);
			} else {
				snapToDestination();
			}

			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}

			// mTouchState = TOUCH_STATE_REST;
			break;
		}
		if (Math.abs(firstX-(int)x)<20 &&  Math.abs(firstY-(int)y)<20 && !MarketContext.getInstance().isGalleryMove) {
			Message msg = new Message();
			msg.what = 1230;
			mmHandler.sendMessage(msg);
		}

		return true;
	}

	//
	// public boolean onInterceptTouchEvent(MotionEvent ev) {
	// // TODO Auto-generated method stub
	// final int action = ev.getAction();
	// if ((action == MotionEvent.ACTION_MOVE)
	// && (mTouchState != TOUCH_STATE_REST)) {
	// Log.i("", "onInterceptTouchEvent  return true");
	// return true;
	// }
	// final float x = ev.getX();
	// final float y = ev.getY();
	// switch (action) {
	// case MotionEvent.ACTION_MOVE:
	// final int xDiff = (int) Math.abs(mLastMotionX - x);
	// if (xDiff > mTouchSlop) {
	// mTouchState = TOUCH_STATE_SCROLLING;
	// }
	// break;
	//
	// case MotionEvent.ACTION_DOWN:
	// mLastMotionX = x;
	//
	// mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
	// : TOUCH_STATE_SCROLLING;
	// break;
	//
	// case MotionEvent.ACTION_CANCEL:
	// case MotionEvent.ACTION_UP:
	// mTouchState = TOUCH_STATE_REST;
	// break;
	// }
	//
	// if (mTouchState != TOUCH_STATE_REST)
	// {
	// Log.i("", "mTouchState != TOUCH_STATE_REST  return true");
	// }
	//
	//
	// return mTouchState != TOUCH_STATE_REST;
	// }

	private boolean IsCanMove(int deltaX) {

		if (getScrollX() <= 0 && deltaX < 0) {
			return false;
		}

		if (getScrollX() >= (getChildCount() - 1) * getWidth() && deltaX > 0) {
			return false;
		}

		return true;
	}

	public void SetOnViewChangeListener(OnViewChangeListener listener) {
		mOnViewChangeListener = listener;
	}

	public Handler mmHandler;
	
	public void setHandler(Handler mmHandler) {
		this.mmHandler = mmHandler;
	}

}
