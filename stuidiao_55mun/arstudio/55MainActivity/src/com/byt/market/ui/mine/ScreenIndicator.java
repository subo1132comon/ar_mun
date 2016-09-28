/*
 * Copyright
 */

package com.byt.market.ui.mine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.byt.market.R;
import com.byt.market.util.LogUtil;

class ScreenIndicator extends View {
	private final static String TAG = "Launcher.ScreenIndicator";
	private final static boolean DBG = false;
	private final static int SEARCH_INDICATOR_POS = 0;

	private int mMaxLevel = 0;
	private int mLevel = 0;

	private float mStartLeft = 0;
	private float mStartTop = 0;

	private static float INDICATOR_WIDTH;
	private static float INDICATOR_HEIGHT;
	private static float INDICATOR_GAP;

	private Drawable mFocusIndicator = null;
	private Drawable mUnfocusIndicator = null;
	private Drawable mSearchFocusIndicator = null;
	private Drawable mSearchUnfocusIndicator = null;



	public void setLevel(int level) {
		if (DBG)
			LogUtil.d(TAG, "setLevel:" + level);
		mLevel = Math.min(mMaxLevel, Math.max(0, level));

		postInvalidate();
	}

	public void setMaxLevel(int max) {
		if (max < 0)
			return;

		if (DBG)
			LogUtil.d(TAG, "setMaxLevel() max:" + max + " origin:" + mMaxLevel);

		mMaxLevel = max;
		
		postInvalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();

		boolean handled = false;

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			float x = ev.getX();
			if (DBG)
				LogUtil.d(TAG, "onTouchEvent down x:" + x);
			float w = (INDICATOR_WIDTH + INDICATOR_GAP) * mMaxLevel
					- INDICATOR_GAP;
			float offset = x - mStartLeft + INDICATOR_GAP;
			int level = 0;

			if (offset < 0) {
				level = mLevel - 1;
			} else if (offset > (w + INDICATOR_GAP)) {
				level = mLevel + 1;
			} else {
				if (offset < INDICATOR_GAP / 2) {
					level = 0;
				} else if (offset > w + INDICATOR_GAP / 2) {
					level = mMaxLevel;
				} else {
					level = (int) ((offset - INDICATOR_GAP / 2) / (INDICATOR_WIDTH + INDICATOR_GAP));
				}

				if (DBG)
					LogUtil.d(TAG, "onTouchEvent down to level:" + level);
			}

			level = (level < 0) ? 0 : (level > mMaxLevel ? mMaxLevel : level);
			//mLauncher.snapToScreen(level);

			handled = true;
			break;
		case MotionEvent.ACTION_UP:
		default:
			break;
		}

		return handled;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable indicator = null;
		int max = mMaxLevel;

		/* Layout */
		float w = (INDICATOR_WIDTH + INDICATOR_GAP) * max - INDICATOR_GAP;
		mStartLeft = (getWidth() - w) / 2;
		mStartTop = (getHeight() - INDICATOR_HEIGHT) / 2;

		if (DBG)
			LogUtil.d(TAG, "ScreenIndicator.onDraw() max:" + max + " level:"
					+ mLevel + " startLeft:" + mStartLeft);

		/* work around */
		if (max < 0 || mStartLeft <= 0)
			return;

		int enlarge = 0;
		int top = 0;
		int bottom = 0;
		int right = 0;
		int left = 0;
		for (int i = 0; i < max; i++) {
			if (i == mLevel) {
				/* Current screen indicators */
				if (i == SEARCH_INDICATOR_POS) {
					/* Search screen */
					indicator = mSearchFocusIndicator;
					enlarge = 2;
				} else {
					indicator = mFocusIndicator;
					enlarge = 0;
				}
			} else {
				/* Unforcus screen indicators */
				if (i == SEARCH_INDICATOR_POS) {
					/* Search screen */
					indicator = mSearchUnfocusIndicator;
					enlarge = 2;
				} else {
					indicator = mUnfocusIndicator;
					enlarge = 0;
				}
			}

			if (indicator != null) {
				left = (int) (mStartLeft + (INDICATOR_WIDTH + INDICATOR_GAP)
						* i);
				top = (int) mStartTop;
				right = (int) (left + INDICATOR_WIDTH + enlarge);
				bottom = (int) (mStartTop + INDICATOR_HEIGHT + enlarge);
				if (DBG)
					LogUtil.d(TAG, "ScreenIndicator.onDraw() left:" + mStartLeft
							+ " top:" + mStartTop);

				indicator.setBounds(left, top, right, bottom);
				indicator.draw(canvas);
				indicator = null;
			}
		}
	}

	public ScreenIndicator(Context context) {
		this(context, null, 0);
	}

	public ScreenIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScreenIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		Resources res = context.getResources();
		mFocusIndicator = res.getDrawable(R.drawable.indicator_focus);
		mUnfocusIndicator = res.getDrawable(R.drawable.indicator_unfocus);
		mSearchFocusIndicator = res
				.getDrawable(R.drawable.indicator_search_focus);
		mSearchUnfocusIndicator = res
				.getDrawable(R.drawable.indicator_search_unfocus);

		INDICATOR_WIDTH = res.getDimension(R.dimen.indicator_width);
		INDICATOR_HEIGHT = res.getDimension(R.dimen.indicator_height);
		INDICATOR_GAP = res.getDimension(R.dimen.indicator_gap);
	}
}
