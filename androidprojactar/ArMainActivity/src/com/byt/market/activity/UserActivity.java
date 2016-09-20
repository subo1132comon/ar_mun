package com.byt.market.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.ui.UserFragment;
import com.byt.market.ui.UserInfoFragment;
import com.byt.market.ui.UserLoginFragment;
import com.byt.market.ui.UserRegistFragment;
import com.byt.market.util.LogUtil;
import com.byt.market.util.PackageUtil;

public class UserActivity extends BaseActivity {

	public static final int TYPE_USER_NONE = 0;
	public static final int TYPE_USER_CENTER = 1;
	public static final int TYPE_USER_LOGIN = 2;
	public static final int TYPE_USER_INFO = 3;
	public static final int TYPE_USER_REGIST = 4;

	public static final String FRAGMENT_USER_CENTER = "userCenter";
	public static final String FRAGMENT_LOGIN = "login";
	public static final String FRAGMENT_USER_INOF = "userInfo";
	public static final String FRAGMENT_USER_REGIST = "userRegist";
	public static final String FRAGMENT_USER_ACTION = "user_action_register_or_login";

	private int mLastType = TYPE_USER_NONE;
	private int mType;
	private FrameLayout user_fragment;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.user_layout);
		LogUtil.i("0419",
				"data = " + getIntent().getIntExtra(FRAGMENT_USER_ACTION, -1));
		if (getIntent().getIntExtra(FRAGMENT_USER_ACTION, -1) == TYPE_USER_REGIST) {
			showRegistFragment();
		} else {
			if (MyApplication.getInstance().getUser().isLogin()) {
				showUserCenterFragment();
			} else {
				showLoginFragment();
			}
		}

		initView();
		initData();
		addEvent();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onFragmentGoBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 返回
	 */
	public void onFragmentGoBack() {
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.user_fragment);
		if (fragment instanceof UserLoginFragment
				|| fragment instanceof UserFragment) {
			finish();
		} else if (fragment instanceof UserRegistFragment) {
			mLastType = TYPE_USER_REGIST;
			mType = TYPE_USER_LOGIN;
			// getSupportFragmentManager().popBackStack(FRAGMENT_LOGIN, 0);
			finish();
		} else if (fragment instanceof UserInfoFragment) {
			if (mLastType == TYPE_USER_CENTER) {
				mLastType = TYPE_USER_INFO;
				mType = TYPE_USER_CENTER;
				getSupportFragmentManager().popBackStack(FRAGMENT_USER_CENTER,
						0);
			} else {
				PackageUtil.hideKeyboard(user_fragment);
				finish();
			}
		}
		PackageUtil.hideKeyboard(user_fragment);
	}

	/**
	 * 显示用户中心界面
	 */
	public void showUserCenterFragment() {
		mLastType = mType;
		mType = TYPE_USER_CENTER;
		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		UserFragment userFragment = new UserFragment();
		transaction.setTransitionStyle(0);
		// transaction.setCustomAnimations(R.anim.push_right_in,
		// R.anim.push_right_out);
		transaction.add(R.id.user_fragment, userFragment);
		transaction.addToBackStack(FRAGMENT_USER_CENTER);
		transaction.commit();
	}

	public void showLoginFragment() {
		mLastType = mType;
		mType = TYPE_USER_LOGIN;
		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		// transaction.setCustomAnimations(R.anim.push_right_in,
		// R.anim.push_right_out);
		transaction.addToBackStack(FRAGMENT_LOGIN);
		transaction.add(R.id.user_fragment, new UserLoginFragment());
		transaction.commit();
	}

	public void showUserInfoFragment() {
		mLastType = mType;
		mType = TYPE_USER_INFO;
		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		// transaction.setCustomAnimations(R.anim.push_right_in,
		// R.anim.push_right_out);
		transaction.addToBackStack(FRAGMENT_USER_INOF);
		transaction.add(R.id.user_fragment, new UserInfoFragment());
		transaction.commit();
	}

	public void showRegistFragment() {
		mLastType = mType;
		mType = TYPE_USER_REGIST;
		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		// transaction.setCustomAnimations(R.anim.push_right_in,
		// R.anim.push_right_out);
		transaction.addToBackStack(FRAGMENT_USER_REGIST);
		transaction.add(R.id.user_fragment, new UserRegistFragment());
		transaction.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.user_fragment);
		fragment.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void initView() {
		user_fragment = (FrameLayout) findViewById(R.id.user_fragment);
	}

	@Override
	public void initData() {

	}

	@Override
	public void addEvent() {

	}

}
