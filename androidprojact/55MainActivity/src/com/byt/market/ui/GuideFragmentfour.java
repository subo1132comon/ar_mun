package com.byt.market.ui;

import com.byt.market.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ValidFragment")
public class GuideFragmentfour extends Fragment {

	private Context ctx;// ��activity���еõ���������

	@SuppressLint("ValidFragment")
	public GuideFragmentfour(Context ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		// ��ʼ��fragmentʱʹ��
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// fragment����ʱʹ��
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// fragment����viewʱʹ��
		// ���ص���һ��view
		/**
		 * LayoutInflater inflater �����ǵ�fragmentxmlʱʵ�õ� ViewGroup
		 * ʹ��inflaterʱ���������� bundler ����ͨ�����ǵ�bundle��fragment����viewʱ���ݲ���
		 * */
		View view = null;
		view = View.inflate(ctx, R.layout.fragment_guide04, null);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		// activity����ʱʹ��

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// ��ͣ
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// activity onresume ʱʹ��
	}

	@Override
	public void onStop() {
		// activity onStop()ʹ��
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		// fragment����viewʱ����
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		// activity����
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		// fragment ��ɾ��ʱ����
		super.onDetach();
	}
}
