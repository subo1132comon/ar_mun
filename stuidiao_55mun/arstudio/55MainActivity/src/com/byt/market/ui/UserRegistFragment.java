package com.byt.market.ui;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.ProtocolActivity;
import com.byt.market.activity.UserActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.UserData;
import com.byt.market.net.NetworkUtil;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.StringUtil;
import com.byt.market.util.Util;

/**
 * 用户注册界面
 */
public class UserRegistFragment extends UserBaseFragment implements
		OnClickListener, TaskListener {
	// views
	private View goBack;
	private View titleBarIcon;
	private TextView title;
	private View searchBtn;
	private View mRightMenu;
	private View userTips;
	private View mPasswordTips;
	private View mRepeatPwdTips;
	private Dialog loadingDialog;
	private View regist;
	private View checking;
	private TextView protocalTxt;
	/** 用来提示用户比如账号必须是数字或字母，两次密码必须一致等提示 **/
	private TextView tv_show_user_regist_tip;

	private EditText uid;
	private EditText pwd;
	/** 重复密码 **/
	private EditText et_user_login_pwd_repeat;

	private Button btn_save;

	// variables
	private String md5Pwd;

	public static final String TAG = "UserRegisteActivity";
	/** 解决点击用户注册界面立即注册按钮下面空白部分，仍然会响应用户登录fragment用腾讯微博登录的问题 **/
	private static boolean isUserRegistFragmentTop = false;

	public static boolean isIsUserRegistFragmentTop() {
		return isUserRegistFragmentTop;
	}

	public static void setIsUserRegistFragmentTop(
			boolean isUserRegistFragmentTop) {
		UserRegistFragment.isUserRegistFragmentTop = isUserRegistFragmentTop;

	}

	public UserRegistFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setIsUserRegistFragmentTop(true);
		View view = inflater.inflate(R.layout.user_registe, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		goBack = view.findViewById(R.id.titlebar_back_arrow);
		titleBarIcon = view.findViewById(R.id.titlebar_icon);
		title = (TextView) view.findViewById(R.id.titlebar_title);
		title.setText(getActivity().getString(R.string.registernew));
		searchBtn = view.findViewById(R.id.titlebar_search_button);
		mRightMenu = view.findViewById(R.id.titlebar_applist_button_container);
		protocalTxt = (TextView) view.findViewById(R.id.txt_protocol);
		protocalTxt.setOnClickListener(this);
		uid = (EditText) view.findViewById(R.id.user_login_name);
		pwd = (EditText) view.findViewById(R.id.user_login_pwd);
		userTips = view.findViewById(R.id.login_user_tips_btn);
		mRepeatPwdTips = view.findViewById(R.id.login_password_tips_btn_repeat);
		/** 密码清除按钮 **/
		mPasswordTips = view.findViewById(R.id.login_password_tips_btn);
		regist = view.findViewById(R.id.user_registe_btn);
		checking = view.findViewById(R.id.checking);
		goBack.setVisibility(View.VISIBLE);
		titleBarIcon.setVisibility(View.GONE);
		searchBtn.setVisibility(View.GONE);
		mRightMenu.setVisibility(View.INVISIBLE);
		view.findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		tv_show_user_regist_tip = (TextView) view
				.findViewById(R.id.tv_show_user_regist_tip);
		et_user_login_pwd_repeat = (EditText) view
				.findViewById(R.id.et_user_login_pwd_repeat);
		btn_save = (Button) view.findViewById(R.id.title_btn);
		btn_save.setOnClickListener(this);
		regist.setOnClickListener(this);
		goBack.setOnClickListener(this);
		userTips.setOnClickListener(this);
		mPasswordTips.setOnClickListener(this);
		mRepeatPwdTips.setOnClickListener(this);

		uid.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!TextUtils.isEmpty(uid.getText())) {
					userTips.setVisibility(View.VISIBLE);
				} else {
					userTips.setVisibility(View.GONE);
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

		pwd.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!TextUtils.isEmpty(pwd.getText())) {
					mPasswordTips.setVisibility(View.VISIBLE);
				} else {
					mPasswordTips.setVisibility(View.GONE);
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
					mRepeatPwdTips.setVisibility(View.VISIBLE);
				} else {
					mRepeatPwdTips.setVisibility(View.GONE);
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

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
		UserRegistFragment.setIsUserRegistFragmentTop(false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.user_registe_btn:
			if (!checkUid() && isAdded()) {
				// showShortToast(getString(R.string.toast_uid_cannot_null));
				tv_show_user_regist_tip
						.setText(getString(R.string.toast_uid_cannot_null));
				return;
			}

			if (uid.getText().toString().length() < 6) {
				tv_show_user_regist_tip
						.setText(getString(R.string.toast_name_not_less_6_or_more_16));
				return;
			}

			if (!checkUserNameIsRegex(uid.getText().toString()) && isAdded()) {
				// showShortToast(getString(R.string.toast_uid_not_ruler));
				tv_show_user_regist_tip
						.setText(getString(R.string.toast_uid_not_ruler));
				return;
			}

			if (!checkPwd() && isAdded()) {
				// showShortToast(getString(R.string.toast_pwd_cannot_null));
				tv_show_user_regist_tip
						.setText(getString(R.string.toast_pwd_cannot_null));
				return;
			}
			// 密码须6-16位
			if ((pwd.getText().toString().length() < 6 || pwd.getText()
					.toString().length() > 16)
					&& isAdded()) {
				pwd.setText("");
				pwd.requestFocus();
				pwd.requestFocusFromTouch();
				et_user_login_pwd_repeat.setText("");
				tv_show_user_regist_tip
						.setText(getString(R.string.toast_pwd_not_less_6_or_more_16));
				return;
			}

			if (!checkPwd(pwd.getText().toString()) && isAdded()) {
				// showShortToast(getString(R.string.toast_pwd_not_ruler));
				tv_show_user_regist_tip
						.setText(getString(R.string.toast_pwd_not_ruler));
				return;
			}

			if (!checkIsTheSamePwd() && isAdded()) {
				pwd.setText("");
				pwd.requestFocus();
				pwd.requestFocusFromTouch();
				et_user_login_pwd_repeat.setText("");
				tv_show_user_regist_tip
						.setText(getString(R.string.toast_pwd_is_not_same));
				return;
			}
			if (!checkUserNameIsRegex(uid.getText().toString())) {
				if (isAdded()) {
					tv_show_user_regist_tip
							.setText(getString(R.string.toast_uid_not_ruler));
				}
			}
			doRegist(v);
			break;
		case R.id.titlebar_back_arrow:
			UserActivity userActivity = getUserActivity();
			if (userActivity != null) {
				userActivity.onFragmentGoBack();
			}
			break;
		case R.id.login_user_tips_btn:
			uid.setText("");
			break;
		case R.id.login_password_tips_btn:
			pwd.setText("");
			break;
		case R.id.login_password_tips_btn_repeat:
			et_user_login_pwd_repeat.setText("");
			break;
		case R.id.txt_protocol:
			Intent intent = new Intent(getActivity(), ProtocolActivity.class);
			getActivity().startActivity(intent);
			break;
		}
	}

	public HashMap<String, String> getHeader() {
		String model = Util.mobile;
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		if (model == null)
			model = Util.getModel();
		if (imei == null)
			imei = Util.getIMEI(getActivity());
		if (vcode == null)
			vcode = Util.getVcode(getActivity());
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(MyApplication.getInstance());
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		NetworkUtil.isNetWorking(getActivity());
		map.put("net", NetworkUtil.getNetworkType());
		map.put("model", model);
		map.put("channel", channel);
		return map;
	}

	private void doRegist(View v) {
		if (!NetworkUtil.isNetWorking(getActivity())) {
			tv_show_user_regist_tip.setText("");
			showShortToast(getString(R.string.toast_no_network));
			return;
		}
		hideKeyboard(v);
		String uid = this.uid.getText().toString().trim();
		String pwd = this.pwd.getText().toString().trim();
		md5Pwd = StringUtil.md5Encoding(pwd);

		ProtocolTask task = new ProtocolTask(getActivity());
		task.setListener(this);
		task.execute(Constants.LIST_URL + "?qt=reg&uid=" + uid + "&pwd="
				+ md5Pwd, null, TAG, getHeader());
		loadingDialog = createDialog(getString(R.string.dialog_title_hint),
				getString(R.string.dialog_msg_registing));
		loadingDialog.show();
	}

	private boolean checkPwd() {
		if (pwd.getText().toString().trim().equals("")) {
			return false;
		}
		return true;
	}

	/**
	 * 密码重复密码须一致
	 * 
	 * @return
	 */
	private boolean checkIsTheSamePwd() {

		if (!pwd.getText().toString()
				.equals(et_user_login_pwd_repeat.getText().toString())) {
			return false;
		}
		return true;
	}

	private boolean checkUid() {
		if (TextUtils.isEmpty(uid.getText())) {
			return false;
		}
		return true;
	}

	public boolean checkUserNameIsRegex(String userName) {
		return isLetter(userName);
	}
	
	public boolean checkPwd(String userName) {
		return isLetterPwd(userName);
	}

	/**
	 * 正则匹配只能输入字母和数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isLetter(String str) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern
//				.compile("[0-9A-Za-z_]*");
				.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$");
		java.util.regex.Matcher m = pattern.matcher(str);
		return m.matches();
	}
	
	public static boolean isLetterPwd(String str) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern
				.compile("[0-9A-Za-z_]*");
//				.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$");
		java.util.regex.Matcher m = pattern.matcher(str);
		return m.matches();
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
		JSONObject json = null;
		try {
			json = new JSONObject(new String(bytes));
			int resultStatus = json.getInt("resultStatus");
			if (resultStatus == 1) {
				JSONObject data = json.getJSONObject("data");
				showShortToast(getString(R.string.toast_regist_success));
				UserData user = new UserData();
				user.setType(UserData.TYPE_ME);
				user.setNickname(data.getString("NICKNAME"));
				user.setIconUrl(data.getString("ICON"));
				if (data.isNull("SEX")) {
					user.setGender(UserData.MALE);
				} else {
					int sex = data.getInt("SEX");
					switch (sex) {
					case 0:
						user.setGender(UserData.FEMALE);
						break;
					case 1:
						user.setGender(UserData.MALE);
						break;
					default:
						user.setGender(UserData.MALE);
						break;
					}
				}
				
				user.setUid(data.getString("USID"));
				user.setType(UserData.TYPE_ME);
				user.setMd5(md5Pwd);
				SharedPrefManager.setLastLoginUserName(getActivity(),
						user.getUid());
				MyApplication.getInstance().setUser(user);
				MyApplication.getInstance().getUser().setLogin(true);
				MyApplication.getInstance().updateUserInfo();
				Constants.ISSHOW = true;
				// UserActivity userActivity = getUserActivity();
				// if (userActivity != null) {
				// userActivity.showUserInfoFragment();
				// }
				UserActivity userActivity = getUserActivity();
				if (userActivity != null) {
					userActivity.onFragmentGoBack();
				}
				return;
			}
			json.toString();
		} catch (JSONException e) {
			if (json != null) {
				try {
					String msg = json.getString("data");
					if (TextUtils.isEmpty(msg)) {
						if (isAdded()) {
							tv_show_user_regist_tip
									.setText(getString(R.string.toast_regist_fail));
						}
					} else {
						if (getString(R.string.toast_uid_has_used).equals(msg)) {
							// 当用户名已经被注册时,清空用户名和密码
							uid.setText("");
							uid.requestFocus();
							uid.requestFocusFromTouch();
							pwd.setText("");
							et_user_login_pwd_repeat.setText("");
						}
						if (isAdded()) {
							tv_show_user_regist_tip.setText(msg);
						}
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
