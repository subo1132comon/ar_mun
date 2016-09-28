package com.byt.market.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.ui.MineFragment2;

/**
 * 
 * @author Administrator
 * 
 */
public class GameListUpdateFrame extends BaseActivity implements
		OnClickListener {

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
		MineFragment2 fragment = new MineFragment2(true);
		findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_search_button).setVisibility(View.INVISIBLE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		FragmentTransaction transaction = this.getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.list_info_contentframe, fragment);
		transaction.commit();
		((TextView) this.findViewById(R.id.titlebar_title))
				.setText(R.string.game_update_notify_title);
	}

	@Override
	public void initData() {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
		boolean flag = false;
		// 后退按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finishWindow();
		}
		return flag;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void addEvent() {
		// TODO Auto-generated method stub
		findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
		// findViewById(R.id.titlebar_search_button).setOnClickListener(this);
	}

	/**
	 * 点击事件处理
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.titlebar_back_arrow) {
			finishWindow();
		} else if (v.getId() == R.id.titlebar_search_button) {
			startActivity(new Intent(Constants.TOSEARCH));
		}
	}
}
