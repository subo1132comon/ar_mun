package com.byt.market.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.byt.market.Constants.Settings;
import com.byt.ar.R;
import com.byt.market.util.SharedPrefManager;

public class DownloadSumSetView extends LinearLayout implements OnClickListener {

	// views
	private Button btn_set_down_sum_decrease;
	private Button btn_set_down_sum_input;
	private Button btn_set_down_sum_increase;

	public DownloadSumSetView(Context context) {
		super(context);
	}

	public DownloadSumSetView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		btn_set_down_sum_decrease = (Button) findViewById(R.id.btn_set_down_sum_decrease);
		btn_set_down_sum_input = (Button) findViewById(R.id.btn_set_down_sum_input);
		btn_set_down_sum_increase = (Button) findViewById(R.id.btn_set_down_sum_increase);
		btn_set_down_sum_decrease.setOnClickListener(this);
		btn_set_down_sum_increase.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_set_down_sum_decrease:
			if (SharedPrefManager.getCurrentDownloadSum(getContext()) != 1) {
				SharedPrefManager
						.setCurrentDownloadSum(getContext(), SharedPrefManager
								.getCurrentDownloadSum(getContext()) - 1);
			}
			break;
		case R.id.btn_set_down_sum_increase:
			if (SharedPrefManager.getCurrentDownloadSum(getContext()) != 3) {
				SharedPrefManager
						.setCurrentDownloadSum(getContext(), SharedPrefManager
								.getCurrentDownloadSum(getContext()) + 1);
			}
			break;
		}
		refreshNumToView();
	}

	private void refreshNumToView() {
		Settings.downloadNum = SharedPrefManager
				.getCurrentDownloadSum(getContext());
		btn_set_down_sum_input.setText(Settings.downloadNum + "");
	}

}
