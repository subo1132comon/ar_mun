package com.byt.market.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byt.ar.R;
import com.byt.market.util.LogUtil;
import com.byt.market.view.CustomListView;

public class H1Fragment extends Fragment {
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		LogUtil.e("TAGFRAGMENT", "onAttach-------------------");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.e("TAGFRAGMENT", "onCreate-------------------");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LogUtil.e("TAGFRAGMENT", "onCreateView-------------------");
		View view = inflater.inflate(R.layout.cate_lv_header, null);
		return view;
	}

	public void setStyle(CustomListView listview2) {

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		LogUtil.e("TAGFRAGMENT", "onViewCreated-------------------");
	}

	@Override
	public void onStart() {
		super.onStart();
		LogUtil.e("TAGFRAGMENT", "onStart-------------------");
	}

	@Override
	public void onStop() {
		super.onStop();
		LogUtil.e("TAGFRAGMENT", "onStop-------------------");
	}

	@Override
	public void onResume() {
		super.onResume();
		LogUtil.e("TAGFRAGMENT", "onResume-------------------");
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		LogUtil.e("TAGFRAGMENT", "onPause-------------------");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LogUtil.e("TAGFRAGMENT", "onDestroyView-------------------");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogUtil.e("TAGFRAGMENT", "onDestroy-------------------");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		LogUtil.e("TAGFRAGMENT", "onDetach-------------------");
	}
}
