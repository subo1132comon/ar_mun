package com.byt.market.view;

import com.byt.ar.R;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailTipView extends FrameLayout {

	private Activity activity;
	private TextView tv_tip_info;
	private ImageButton d_tip_lock;
	
	private TextView tv_tip_info_ad;
	private TextView tv_tip_info_wifi;
	private TextView tv_tip_info_gp;
	private ImageView iv_tip_info_ad;
	private ImageView iv_tip_info_wifi;
	private ImageView iv_tip_info_gp;
	
	private View tip_layout;
	private View l_d_tip_info_des;

	public DetailTipView(Context context) {
		super(context);
		inflate(context, R.layout.detail_tip, this);
		initView();
		d_tip_lock.setFocusable(false);
		d_tip_lock.setSelected(false);
		d_tip_lock.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				boolean flag = d_tip_lock.isSelected();
				if (!flag) {
					l_d_tip_info_des.setVisibility(View.VISIBLE);
					d_tip_lock.setImageResource(R.drawable.detail_expand_icon);
				} else {
					l_d_tip_info_des.setVisibility(View.GONE);
					d_tip_lock.setImageResource(R.drawable.detail_zhankai_icon);
				}
				d_tip_lock.setSelected(!flag);
			}
		});
	}

	/**
	 * @param context
	 * @param appadvertisetaskmark
	 */
	public DetailTipView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.detail_tip, this);
		initView();
		d_tip_lock.setFocusable(false);
		d_tip_lock.setSelected(false);
		OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				boolean flag = d_tip_lock.isSelected();
				if (!flag) {
					l_d_tip_info_des.setVisibility(View.VISIBLE);
					d_tip_lock.setImageResource(R.drawable.detail_expand_icon);
				} else {
					l_d_tip_info_des.setVisibility(View.GONE);
					d_tip_lock.setImageResource(R.drawable.detail_zhankai_icon);
				}
				d_tip_lock.setSelected(!flag);
			}
		};
		d_tip_lock.setOnClickListener(onClickListener);
		tip_layout.setOnClickListener(onClickListener);
	}

	public void flushAdvertiseBand(String stype) {
		StringBuffer sb = new StringBuffer();
		if (stype.contains("a")) {
			tv_tip_info_ad.setText(getContext().getString(R.string.app_ad));
			iv_tip_info_ad.setImageResource(R.drawable.ad_s);
			sb.append(getContext().getString(R.string.app_addetail));
		} else {
			iv_tip_info_ad.setImageResource(R.drawable.ad_n);
			tv_tip_info_ad.setText(getContext().getString(R.string.app_noad));
			sb.append(getContext().getString(R.string.app_noaddetail));
		}
		if (stype.contains("b")) {
			iv_tip_info_wifi.setImageResource(R.drawable.wifi_s);
			tv_tip_info_wifi.setText(getContext().getString(R.string.app_net));
			sb.append(getContext().getString(R.string.app_netdetail));
		} else {
			iv_tip_info_wifi.setImageResource(R.drawable.wifi_n);
			tv_tip_info_wifi.setText(getContext().getString(R.string.app_nonet));
			sb.append(getContext().getString(R.string.app_nonetdetail));
		}
		if (stype.contains("c")) {
			iv_tip_info_gp.setImageResource(R.drawable.googel_s);
			tv_tip_info_gp.setText(getContext().getString(R.string.app_google));
			sb.append(getContext().getString(R.string.app_googledetail));
		} else {
			iv_tip_info_gp.setImageResource(R.drawable.googel_n);
			tv_tip_info_gp.setText(getContext().getString(R.string.app_nogoogle));
			sb.append(getContext().getString(R.string.app_nogoogledetail));
		}
		tv_tip_info.setText(sb.toString());
	}

	private void initView() {
		tv_tip_info = (TextView) findViewById(R.id.tv_tip_info);
		d_tip_lock = (ImageButton) findViewById(R.id.d_tip_lock);
		tv_tip_info_ad = (TextView) findViewById(R.id.tv_tip_info_ad);
		tv_tip_info_wifi = (TextView) findViewById(R.id.tv_tip_info_wifi);
		tv_tip_info_gp = (TextView) findViewById(R.id.tv_tip_info_gp);
		iv_tip_info_ad = (ImageView) findViewById(R.id.iv_tip_info_ad);
		iv_tip_info_wifi = (ImageView) findViewById(R.id.iv_tip_info_wifi);
		iv_tip_info_gp = (ImageView) findViewById(R.id.iv_tip_info_gp);
		tip_layout = findViewById(R.id.l_tip_info);
		l_d_tip_info_des = findViewById(R.id.l_d_tip_info_des);
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
