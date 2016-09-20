package com.byt.market.view;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.data.AppItem;
import com.byt.market.util.PackageUtil;

public class CustomToast extends LinearLayout implements OnClickListener {

	// constants
	private static final int DISSMISS_DELAY_TIME = 5 * 1000;
	private static final int MSG_DISSMISS = 1;

	// views
	private ImageView img_game_icon;
	private TextView tv_name;
	private Button btn_launch;

	// varibales
	private AppItem appItem;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_DISSMISS:
				CustomToast.this.setVisibility(View.GONE);
				break;
			}
		};
	};

	public CustomToast(Context context) {
		super(context);
	}

	public CustomToast(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void showToast(AppItem appItem) {
		if (appItem == null || TextUtils.isEmpty(appItem.pname)) {
			return;
		}
		handler.removeMessages(MSG_DISSMISS);
		this.appItem = appItem;
		this.setVisibility(View.VISIBLE);
		img_game_icon.setImageDrawable(PackageUtil.getAppIcon(getContext(),
				appItem.pname));
		if (!TextUtils.isEmpty(appItem.name)) {
			tv_name.setText(appItem.name);
		}
		handler.sendEmptyMessageDelayed(MSG_DISSMISS, DISSMISS_DELAY_TIME);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		img_game_icon = (ImageView) findViewById(R.id.img_game_icon);
		tv_name = (TextView) findViewById(R.id.tv_name);
		btn_launch = (Button) findViewById(R.id.btn_launch);
		btn_launch.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (appItem == null || TextUtils.isEmpty(appItem.pname)) {
			return;
		}
		PackageUtil.startApp(MyApplication.getInstance()
				.getApplicationContext(), appItem.pname);
	}

}
