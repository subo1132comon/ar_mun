package com.byt.market.view;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byt.ar.R;
import com.byt.market.Constants;
import com.byt.market.data.AppComment;

public class CustomDetailCommBand extends LinearLayout {

	public static final String TAG = "CustomDetailCommBand";
	private LinearLayout ll_detail_comm;
	private TextView tv_u_ccount;
	private View view;
	private List<AppComment> datas;

	public CustomDetailCommBand(Context context) {
		super(context);
		view = inflate(context, R.layout.listitem_detail_comm, this);
		initView();
	}

	/**
	 * @param context
	 * @param appadvertisetaskmark
	 */
	public CustomDetailCommBand(Context context, AttributeSet attrs) {
		super(context, attrs);
		view = inflate(context, R.layout.listitem_detail_comm, this);
		initView();
	}

	public void flushAdvertiseBand(List<AppComment> datas, int ccount) {
		if (this.datas != null)
			return;
		this.datas = datas;
		if (this.datas == null || this.datas.size() == 0) {
			view.setVisibility(View.GONE);
			return;
		}
		tv_u_ccount.setText(getContext().getString(R.string.app_usereva) + ccount + getContext().getString(R.string.kuohao));
		createView(this.datas);
	}

	private void initView() {
		ll_detail_comm = (LinearLayout) findViewById(R.id.ll_detail_comm);
		tv_u_ccount = (TextView) findViewById(R.id.tv_u_ccount);
		datas = null;
	}

	private void createView(List<AppComment> comments) {
		for (AppComment appComment : comments) {
			View view = LayoutInflater.from(getContext()).inflate(
					R.layout.listitem_detail_comment_item, null);
			TextView tv_name = (TextView) view
					.findViewById(R.id.appcommAuthorName);
			RatingBar rBar = (RatingBar) view
					.findViewById(R.id.appcommRatingView);
			TextView appcommTime = (TextView) view
					.findViewById(R.id.appcommTime);
			TextView listitem_comm_des = (TextView) view
					.findViewById(R.id.listitem_comm_des);
			TextView listitem_comm_moble = (TextView) view
					.findViewById(R.id.listitem_comm_moble);
			TextView listitem_comm_vname = (TextView) view
					.findViewById(R.id.listitem_comm_vname);

			view.setId(appComment.sid);
//			imageView.setDefaultImageResource(R.drawable.default_user);
//			imageView.setUrl(Constants.IMG_URL + appComment.iconUrl);
			tv_name.setText(appComment.name);
			appcommTime.setText(appComment.comtime);
//			rBar.setRating((float) appComment.rating);
			listitem_comm_des.setText(appComment.sdesc);
			listitem_comm_moble.setText(getContext().getString(R.string.app_from) + appComment.modle);
			listitem_comm_vname.setText(getContext().getString(R.string.app_version) + appComment.vname);

			ll_detail_comm.addView(view);
		}
	}
}
