package com.byt.market.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.byt.ar.R;
import com.byt.market.util.StorageUtil;

public class StorageView extends LinearLayout {

	private TextView mStroagePhoneInfo;
	private ProgressBar mStroagePhoneProgress;
	private TextView mStroageSDInfo;
	private ProgressBar mStroageSDProgress;
	private Context mContext;
	
	public StorageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.inflate(context, R.layout.storage_view, this);
		mStroagePhoneInfo = (TextView) findViewById(R.id.storage_phone_info);
		mStroagePhoneProgress = (ProgressBar) findViewById(R.id.storage_phone_progress);
		mStroageSDInfo = (TextView) findViewById(R.id.storage_sd_info);
		mStroageSDProgress = (ProgressBar) findViewById(R.id.storage_sd_progress);
		mContext  = context;
		refresh();
	}
	
	public void refresh() {
		long phoneTotalMemorySize = (long) StorageUtil
				.getTotalInternalMemorySize();
		long sdTotalMemorySize = (long) StorageUtil
				.getTotalExternalMemorySize();
		long phoneUsedMemorySize = phoneTotalMemorySize
				- (long) StorageUtil.getAvailableInternalMemorySize();
		long sdUsedMemorySize = sdTotalMemorySize
				- (long) StorageUtil.getAvailableExternalMemorySize();
		int phTotalKB = (int) (phoneTotalMemorySize / 1024);
		int phUsedKB = (int) (phoneUsedMemorySize / 1024);
		int sdTotalKB = (int) (sdTotalMemorySize / 1024);
		int sdUsedKB = (int) (sdUsedMemorySize / 1024);
		mStroagePhoneProgress.setMax(phTotalKB);
		mStroageSDProgress.setMax(sdTotalKB);

		if (phoneUsedMemorySize > phoneTotalMemorySize / 3 * 2) {
			mStroagePhoneProgress.setSecondaryProgress(phUsedKB);
		} else {
			mStroagePhoneProgress.setProgress(phUsedKB);
		}
		if (sdUsedMemorySize > sdTotalMemorySize / 3 * 2) {
			mStroageSDProgress.setSecondaryProgress(sdUsedKB);
		} else {
			mStroageSDProgress.setProgress(sdUsedKB);
		}

		mStroagePhoneInfo.setText(mContext.getString(R.string.storage_fomat,
				StorageUtil.computePercent(phoneUsedMemorySize, phoneTotalMemorySize),
				StorageUtil.formatSize(phoneTotalMemorySize - phoneUsedMemorySize)));

		if(!existSDCard()){
			mStroageSDInfo.setText(R.string.storage_unaviable);
		} else {
			mStroageSDInfo.setText(mContext.getString(R.string.storage_fomat,
					StorageUtil
							.computePercent(sdUsedMemorySize,
									sdTotalMemorySize), StorageUtil
							.formatSize(sdTotalMemorySize - sdUsedMemorySize)));
		}
		

	}
	
	private boolean existSDCard() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}

}
