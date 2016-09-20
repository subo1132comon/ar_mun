package com.byt.market.ui;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.util.RapitUtile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

@SuppressLint("ValidFragment")
public class GuideFragmentFive extends Fragment {

	private Context ctx;

	public GuideFragmentFive(Context ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = null;
		view = View.inflate(ctx, R.layout.fragment_guide05, null);
//		LinearLayout mLinear = (LinearLayout) view
//				.findViewById(R.id.Fragment01Linear);
//		mLinear.setBackgroundResource(R.drawable.guidance_new2);
		
		ImageView mBtn = (ImageView) view.findViewById(R.id.MyLoginBtn);
		mBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				RapitUtile.deletTVtoastShow("GuideActivity");
//				Intent intent = new Intent(ctx, MainActivity.class);
//				ctx.startActivity(intent);
				//GuideActivity.this.finish();
				Intent intent = new Intent();
				intent.setAction(Constants.PROGREES_BAR_RECIVE);
				getActivity().sendBroadcast(intent);
				getActivity().finish();
			}
		});
		
		return view;
	}

}
