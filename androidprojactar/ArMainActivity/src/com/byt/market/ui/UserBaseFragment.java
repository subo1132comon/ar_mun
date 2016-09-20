package com.byt.market.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.byt.market.activity.UserActivity;
import com.byt.market.ui.base.BaseUIFragment;

public class UserBaseFragment extends BaseUIFragment {
	
	private ProgressDialog progressDialog;
	
	public UserActivity getUserActivity(){
		if(getActivity() != null && getActivity() instanceof UserActivity){
			return (UserActivity) getActivity();
		} else {
			return null;
		}
	}
	
	protected void hideKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	public void showProgressDialog(String msg) {
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setMessage(msg);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	public void dismissProgressDialog() {
		progressDialog.dismiss();
	}
}
