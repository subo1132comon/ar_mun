package com.byt.market.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.byt.market.R;
import com.byt.market.Constants;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.data.AppItem;
import com.byt.market.data.JoySkillItem;
import com.byt.market.ui.JoySkillDetail;
import com.byt.market.ui.JoySkillFragment;


/**
 * 
 * @author Administrator
 * 
 */
public class JoySkillListFrame extends BaseActivity implements OnClickListener,JoySkillFragment.OnJoySkillClickListener{

	private static final int VIEW_LIST = 1;
	private static final int VIEW_DETAIL = 2;
	private int mCurrentView = VIEW_LIST;
	public JoySkillFragment mJoySkillFragment;
	public JoySkillDetail mJoySkillDetail;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_info_frame);
		initView();
		initData();
		addEvent();
	}

	@Override
	public void initView() {
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		mJoySkillFragment = new JoySkillFragment();
		mJoySkillFragment.setOnJoySkillClickListener(this);
		findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_search_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.list_info_contentframe, mJoySkillFragment);
		transaction.commit();
		((TextView)this.findViewById(R.id.titlebar_title)).setText(R.string.expend_child4_text);
		mJoySkillDetail = new JoySkillDetail();
	}

	@Override
	public void initData() {

	}
	
	private void showView(Fragment fragment){
		FragmentTransaction transaction = this.getSupportFragmentManager()
		.beginTransaction();
		if(fragment instanceof JoySkillDetail){
			transaction.setCustomAnimations(R.anim.push_left_in,
					R.anim.push_left_out);
			mCurrentView = VIEW_DETAIL;
			final AppItem appItem = this.getIntent().getParcelableExtra("app");
			if(appItem != null){
				((TextView)this.findViewById(R.id.titlebar_title)).setText(appItem.name+"-"+this.getString(R.string.expend_child4_text));
			}
			
		} else if(fragment instanceof JoySkillFragment){
			transaction.setCustomAnimations(R.anim.push_right_in,
					R.anim.push_right_out);
			mCurrentView = VIEW_LIST;
			((TextView)this.findViewById(R.id.titlebar_title)).setText(R.string.expend_child4_text);
		}
		
		transaction.replace(R.id.list_info_contentframe, fragment);
		transaction.commit();
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
		boolean flag = false;
		// 后退按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(mCurrentView == VIEW_DETAIL){
				if(!mJoySkillDetail.goBack()){
					showView(mJoySkillFragment);
				}
				return true;
			}
			finishWindow();
		}
		return flag;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mJoySkillFragment.removeOnJoySkillClickListener();
	}

	@Override
	public void addEvent() {
		// TODO Auto-generated method stub
		findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
		//findViewById(R.id.titlebar_search_button).setOnClickListener(this);
	}
	
	/**
	 * 点击事件处理
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.titlebar_back_arrow) {
			if(mCurrentView == VIEW_DETAIL){
				showView(mJoySkillFragment);
				return ;
			}
			finishWindow();
		} else if (v.getId() == R.id.titlebar_search_button) {
			startActivity(new Intent(Constants.TOSEARCH));
		}
	}

	@Override
	public void onJoySkillClicked(JoySkillItem item) {
		// TODO Auto-generated method stub
		mJoySkillDetail.setUrl(Constants.LIST_URL+"?"+item.wUrl);
		showView(mJoySkillDetail);
	}
}
