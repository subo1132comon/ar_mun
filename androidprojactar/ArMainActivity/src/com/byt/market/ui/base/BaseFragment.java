package com.byt.market.ui.base;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.byt.ar.R;

public abstract class BaseFragment extends Fragment {
	private CharSequence title = "";
	/** 标识空的fragment，适用于软件详情的选项卡 **/
	boolean isEmptyFragment = false;

	public void setTabTitle(String str) {
		title = str;
	}

	public CharSequence getTabTittle() {
		return title;
	}

	public boolean respondBackKey() {
		return false;
	}

	public void respondActionMenu(int menuId) {
	}

	public void deleteAllBlackData() {

	}

	public void onPageChange(int index) {

	}


	public boolean isEmptyFragment() {
		return isEmptyFragment;
	}

	public void setEmptyFragment(boolean isEmptyFragment) {
		this.isEmptyFragment = isEmptyFragment;
	}

}
