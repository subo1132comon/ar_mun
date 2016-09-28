package com.byt.market.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.data.UserData;
import com.byt.market.net.NetworkUtil;
import com.byt.market.util.LogUtil;
import com.byt.market.util.StringUtil;
import com.byt.market.util.Util;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

/**
 * 修改密码
 */
public class RetrievePasswordActivity extends BaseActivity implements
		View.OnClickListener, ProtocolTask.TaskListener {
	// views
	private EditText et_user_old_pwd;
	private EditText et_user_new_pwd;
	private EditText et_user_login_pwd_repeat;
	private Button btn_save;
	private ImageView iv_back;
	private TextView tv_show_user_regist_tip;
	private String md5Pwd;
	private Dialog loadingDialog;

	private ImageView img_old_pwd_clear;
	private ImageView img_pwd_clear;
	private ImageView img_repeat_pwd_clear;
	
	private TextView titleText;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.retrieve_password);
		initView();
		initData();
		addEvent();
	}

	@Override
	public void initView() {
		initTitleBar();
		et_user_old_pwd = (EditText) findViewById(R.id.et_user_old_pwd);
		et_user_new_pwd = (EditText) findViewById(R.id.et_user_new_pwd);
		et_user_login_pwd_repeat = (EditText) findViewById(R.id.et_user_login_pwd_repeat);

		et_user_old_pwd.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!TextUtils.isEmpty(et_user_old_pwd.getText())) {
					img_old_pwd_clear.setVisibility(View.VISIBLE);
				} else {
					img_old_pwd_clear.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		et_user_new_pwd.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!TextUtils.isEmpty(et_user_new_pwd.getText())) {
					img_pwd_clear.setVisibility(View.VISIBLE);
				} else {
					img_pwd_clear.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		et_user_login_pwd_repeat.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!TextUtils.isEmpty(et_user_login_pwd_repeat.getText())) {
					img_repeat_pwd_clear.setVisibility(View.VISIBLE);
				} else {
					img_repeat_pwd_clear.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		tv_show_user_regist_tip = (TextView) findViewById(R.id.tv_show_user_regist_tip);

		img_old_pwd_clear = (ImageView) findViewById(R.id.img_old_pwd_clear);
		img_pwd_clear = (ImageView) findViewById(R.id.img_pwd_clear);
		img_repeat_pwd_clear = (ImageView) findViewById(R.id.img_repeat_clear);

		img_old_pwd_clear.setOnClickListener(this);
		img_pwd_clear.setOnClickListener(this);
		img_repeat_pwd_clear.setOnClickListener(this);

	}

	private void initTitleBar() {
		
		titleText = (TextView) findViewById(R.id.titlebar_title);
		titleText.setText(getString(R.string.button_text_action_modify_password));
		
		iv_back = (ImageView) findViewById(R.id.titlebar_back_arrow);
		iv_back.setVisibility(View.VISIBLE);
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
		findViewById(R.id.titlebar_applist_button_container).setVisibility(
				View.GONE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		btn_save = (Button) findViewById(R.id.title_btn);
		btn_save.setText(R.string.text_action_save);
		btn_save.setVisibility(View.VISIBLE);
	}

	@Override
	public void initData() {

	}

	@Override
	public void addEvent() {
		et_user_old_pwd.setOnClickListener(this);
		et_user_new_pwd.setOnClickListener(this);
		et_user_login_pwd_repeat.setOnClickListener(this);
		btn_save.setOnClickListener(this);
		iv_back.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_btn:
			if (checkRetrievePassword()) {
				if(NetworkUtil.isNetWorking(this)){
				doRetrievePassword();
				}else{
					tv_show_user_regist_tip.setText(getString(R.string.toast_no_network));
				}
			}
			break;
		case R.id.titlebar_back_arrow:
			finish();
			break;
		case R.id.img_old_pwd_clear:
			et_user_old_pwd.setText("");
			break;
		case R.id.img_pwd_clear:
			et_user_new_pwd.setText("");
			break;
		case R.id.img_repeat_clear:
			et_user_login_pwd_repeat.setText("");
			break;
		}
	}

	// 修改密码点保存时，做检查
	private boolean checkRetrievePassword() {
		String oldPwdMd5 = MyApplication.getInstance().getUser().getMd5();
		if (!oldPwdMd5.equals(StringUtil.md5Encoding(et_user_old_pwd.getText()
				.toString().trim()))) {
			tv_show_user_regist_tip.setText(R.string.text_old_pwd_error_desc);
			return false;
		} else if (et_user_new_pwd.toString().trim().equals("")) {
			tv_show_user_regist_tip.setText(R.string.text_new_pwd_null_desc);
			return false;
		} else if (et_user_new_pwd.getText().toString().length() < 6
				|| et_user_new_pwd.getText().toString().length() > 16) {
			tv_show_user_regist_tip
					.setText(R.string.toast_pwd_not_less_6_or_more_16);
			return false;
		} else if (!et_user_login_pwd_repeat.getText().toString()
				.equals(et_user_new_pwd.getText().toString())) {
			tv_show_user_regist_tip.setText(R.string.toast_pwd_is_not_same);
			return false;
		}
		return true;
	}

	public Dialog createDialog(String title, String msg) {
		return new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
				.setIcon(R.drawable.icon).create();
	}

	private void doRetrievePassword() {
		tv_show_user_regist_tip.setText("");
		String uid = MyApplication.getInstance().getUser().getUid();
		String pwd = et_user_new_pwd.getText().toString().trim();
		md5Pwd = StringUtil.md5Encoding(pwd);
		LogUtil.i("0419", "new md5Pwd = " + md5Pwd + ",old md5pwd = "
				+ StringUtil.md5Encoding(et_user_old_pwd.getText().toString()));
		ProtocolTask task = new ProtocolTask(this);
		task.setListener(this);
		task.execute(Constants.LIST_URL + "?qt=modifypwd&uid=" + uid + "&pwd="
				+ md5Pwd, null, TAG, getHeader());
		loadingDialog = createDialog(getString(R.string.dialog_title_hint),
				getString(R.string.dialog_msg_registing));
		loadingDialog.show();
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
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(MyApplication.getInstance());
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
		dismissDialog(loadingDialog);
		showShortToast(getString(R.string.toast_no_network));
	}

	@Override
	public void onNetworkingError() {
		dismissDialog(loadingDialog);
		showShortToast(getString(R.string.toast_network_connect_error));
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		dismissDialog(loadingDialog);
		try {
			JSONObject json = new JSONObject(new String(bytes));
			int resultStatus = json.getInt("resultStatus");
			if (resultStatus == 1) {
				showShortToast(getString(R.string.toast_modify_pwd_success));
				UserData user = MyApplication.getInstance().getUser();
				user.setMd5(md5Pwd);
				MyApplication.getInstance().getUser().setLogin(true);
				finish();
				return;
			}
			json.toString();
		} catch (JSONException e) {
			LogUtil.i("0419", "catch JSONException e = " + e.getMessage());
			e.printStackTrace();
		}
	}
}