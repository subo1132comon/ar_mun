package com.byt.market.view;

import com.byt.ar.R;
import com.byt.market.activity.MainActivity.OnMenuAnimation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 
 * @author Administrator
 * 
 */
public class TabBarView extends LinearLayout {
	public TextView mTextView = null;
	private TextView mImageView = null;
	private boolean interruptFlag;
	private Context mContext;
	private Animation mAnim_txt;
	private Animation mAnim_img;
	private OnMenuAnimation onAnimation;
	private boolean isAnimationEnd;

	public TabBarView(Context context) {
		super(context);
		mContext = context;
		isAnimationEnd = true;
	}

	public TabBarView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.m_tabbar_view, this);
		mTextView = (TextView) findViewById(R.id.wjtabmenu_tv);
		mImageView = (TextView) findViewById(R.id.wjtabmenu_iv);
		mImageView.setSelected(true);
		mTextView.setSelected(false);
		mContext = context;
		mAnim_txt = AnimationUtils.loadAnimation(mContext,
				R.anim.bottommenu_txt_anim);
		isAnimationEnd = true;
		mAnim_txt.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				isAnimationEnd = false;
				onAnimation.onAnimationStart();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				unSelected();
				onAnimation.onAnimationEnd();
				isAnimationEnd = true;
				if (!interruptFlag) {
					mTextView.setVisibility(View.GONE);
				}
			}
		});

		mAnim_img = AnimationUtils.loadAnimation(mContext,
				R.anim.bottommenu_img_anim);
	}

	public void setText(String title) {
		mTextView.setText(title);
		mImageView.setText(title);
		
	}

	public void setIcon(int resid) {
		//mImageView.setImageResource(resid);
	}

	public void setItemSelected() {
		mTextView.setVisibility(View.GONE);
	}

	/**
	 * ???????
	 */
	public void unSelected() {
		mTextView.clearAnimation();
		mImageView.clearAnimation();
		clearAnimation();
	
		
		interruptFlag = true;
		mTextView.setVisibility(View.VISIBLE);
	}

	/**
	 * ???
	 */
	public void selected() {
		interruptFlag = false;
		
		
		mTextView.startAnimation(mAnim_txt);
		mImageView.startAnimation(mAnim_img);
	}

	public void setMenuAnimation(OnMenuAnimation onAnimation) {
		this.onAnimation = onAnimation;
	};

	public boolean isAnimationEnd() {
		return isAnimationEnd;
	}
}
