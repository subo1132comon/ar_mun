package com.byt.market.ui;
import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.activity.GuideActivity;
import com.byt.market.activity.MainActivity;
import com.byt.market.util.RapitUtile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

@SuppressLint("ValidFragment")
public class GuideFragmentthree extends Fragment {

	private Context ctx;

	public GuideFragmentthree(Context ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = null;
		view = View.inflate(ctx, R.layout.fragment_guide03, null);
		return view;
	}

}
