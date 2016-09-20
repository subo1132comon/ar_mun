package com.byt.market.activity;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.byt.ar.R;
import com.byt.market.Constants;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.net.NetworkUtil;
import com.byt.market.util.Util;

public class SuggestActivity extends BaseActivity implements OnClickListener, TaskListener {
	
	private View mGoBack;
    private View mTitleBarIcon;
    private TextView mTitle;
    private View mSearchBtn;
    private View mRightMenu;
	private EditText mSuggestInfo;
	private EditText mPhone;
	private Spinner mSpinner;
	private Button mCancel;
	private Button mSubmit;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.suggest_layout);
		initView();
		initData();
		addEvent();
	}

	@Override
	public void initView() {
		mGoBack = findViewById(R.id.titlebar_back_arrow);
    	mTitleBarIcon = findViewById(R.id.titlebar_icon);
    	mTitle = (TextView) findViewById(R.id.titlebar_title);
    	mSearchBtn = findViewById(R.id.titlebar_search_button);
    	mRightMenu = findViewById(R.id.titlebar_applist_button_container);
    	
    	mSuggestInfo = (EditText) findViewById(R.id.et_suggest_content);
    	mPhone = (EditText) findViewById(R.id.et_suggest_conn);
    	mSpinner = (Spinner) findViewById(R.id.sp_question);
    	mCancel = (Button) findViewById(R.id.btn_cancel_suggest);
    	mSubmit = (Button) findViewById(R.id.btn_submit_suggest);
	}

	@Override
	public void initData() {
		mGoBack.setVisibility(View.VISIBLE);
        mTitleBarIcon.setVisibility(View.GONE);
        mSearchBtn.setVisibility(View.GONE);
        mRightMenu.setVisibility(View.INVISIBLE);
        findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
        mTitle.setText(R.string.titlebar_title_suggest);
	}

	@Override
	public void addEvent() {
		mGoBack.setOnClickListener(this);
    	mTitle.setOnClickListener(this);
    	mSubmit.setOnClickListener(this);
    	mCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.titlebar_back_arrow:
        case R.id.titlebar_title:
        case R.id.btn_cancel_suggest:
            finishWindow();
            break;
        case R.id.btn_submit_suggest:
        	doSuggest();
        	break;
//        	mSuggestInfo.setText("");
//        	mPhone.setText("");
//        	mSpinner.setSelection(0);
//        	mSuggestInfo.requestFocus();
//        	break;
		}
	}

	ProtocolTask task = new ProtocolTask(this);
	
	private void doSuggest() {
		StringBuilder suggestContent = new StringBuilder();
		String content = mSuggestInfo.getText().toString().trim();
		String conn = mPhone.getText().toString();
		String categ = mSpinner.getSelectedItem().toString();
		
		suggestContent.append("content=").append(content).append("&").append("conn=").append(conn);
		
		if(TextUtils.isEmpty(content)){
			showShortToast(getString(R.string.textnull));
			return;
		}
		String url = Constants.LIST_URL + "?qt=feedback"+ "&categ=" + categ;
		//&content=" + content
		if(task != null){
			task.onCancelled();
		}
		task = new ProtocolTask(this);
		task.setListener(this);
		task.execute(url, suggestContent.toString(), "TAG", getHeader());
		showShortToast(getString(R.string.submit_suggest));
	}
	
    public HashMap<String, String> getHeader() {
    	String model = Util.mobile;
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		if (model == null)
			model = Util.getModel();
		if (imei == null)
			imei = Util.getIMEI(this);
		if (vcode == null)
			vcode = Util.getVcode(this);
		if (TextUtils.isEmpty(channel)){
			channel = Util.getChannelName(this);
		}
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("imei", imei);
    	map.put("vcode", vcode);
    	NetworkUtil.isNetWorking(this);
    	map.put("net", NetworkUtil.getNetworkType());
    	map.put("model", model);
    	map.put("channel", channel);
    	return map;
    }
    
	@Override
	public void onNoNetworking() {
		showShortToast(getString(R.string.submit_suggest_fail));
	}

	@Override
	public void onNetworkingError() {
		showShortToast(getString(R.string.submit_suggest_fail));
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		if (bytes != null) {
			try {
				JSONObject json = new JSONObject(new String(bytes));
				if (!json.isNull("data")){
					int result = json.getInt("data");
					if(result == 1){
						showShortToast(getString(R.string.submit_suggest_success));
						finish();
						return;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		showShortToast(getString(R.string.submit_suggest_fail));
	}
}
