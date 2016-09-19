package com.byt.market.ui;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.UserActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.UserData;
import com.byt.market.net.NetworkUtil;
import com.byt.market.oauth2.SinaAccessToken;
import com.byt.market.oauth2.SinaAccessTokenKeeper;
import com.byt.market.oauth2.SinaOauth2;
import com.byt.market.oauth2.SinaOauth2.OnAccessTokenReceive;
import com.byt.market.oauth2.SinaOauth2.OnUserReceive;
import com.byt.market.oauth2.TencentAccessToken;
import com.byt.market.oauth2.TencentAccessTokenKeeper;
import com.byt.market.oauth2.TencentOauth2;
import com.byt.market.oauth2.TencentWeiboOauth2;
import com.byt.market.util.JsonParse;
import com.byt.market.util.LogUtil;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.StringUtil;
import com.byt.market.util.SystemUtil;
import com.byt.market.util.Util;

/**
 * 用户登录界面
 */
public class UserLoginFragment extends UserBaseFragment implements
		OnClickListener, TaskListener {
	/**
	 * 新浪微博认证回调
	 */
	private static final int SINA_TOKEN_COMPLETE = 3;
	/**
	 * 腾讯qq认证回调
	 */
	private static final int TENCENT_TOKEN_COMPLETE = 2;

	private static final int GETUSER_SUCCESS = 0;
	private static final int GETUSER_FAIL = 1;

	/**
	 * 注册
	 */
	public static final int REQUEST_CODE_REGIST = 10;
	public static final int REQUEST_CODE_TENCENT_WEIBO = 11;
	public static final int RESULT_CODE_FAIL = 0;
	public static final int RESULT_CODE_SUCC = 1;

	// 新浪微博
//	private Weibo mWeibo;
//	private SsoHandler mSsoHandler;
	// QQ
//	private Tencent mTencent;

	private View mGoBack;
	private View mTitleBarIcon;
	private TextView mTitle;
	private View mSearchBtn;
	private View mRightMenu;

	private AutoCompleteTextView mUid;
	private EditText mPwd;
	private View mUserTips;
	private View mPasswordTips;

	

	private View mLogin;
	private View mSinaOauth2;
	private View mTencentWeiboOauth2;
	/** 用来提示用户比如用户名或密码不正确等提示 **/
	private TextView tv_show_user_login_tip;
	/** 忘记密码 **/
	private TextView user_login_forgot_password;
	/** 立即注册 **/
	private TextView user_login_regist;
	private Dialog callDialog;

	private Handler mHander = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GETUSER_SUCCESS:
				MyApplication.getInstance().updateUserInfo();
				Constants.ISSHOW = true;
				UserActivity userActivity = getUserActivity();
				if (userActivity != null) {
					userActivity.onFragmentGoBack();
				}
				showShortToast("登录成功");
				// dismissDialog(mOauthDialog);
				break;
			case GETUSER_FAIL:
				// dismissDialog(mOauthDialog);
				SinaAccessTokenKeeper.clear(getActivity());
				TencentAccessTokenKeeper.clear(getActivity());
				showShortToast(getString(R.string.toast_oauth_get_userinfo_fail));
				break;
			case SINA_TOKEN_COMPLETE:
				onSinaTokenComplete((SinaAccessToken) msg.obj);
				break;
			case TENCENT_TOKEN_COMPLETE:
				onTencentTokenComplete((TencentAccessToken) msg.obj);
				break;
			}
		}
	};

	private String md5Pwd;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.user_login, null);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		mWeibo = Weibo.getInstance(SinaOauth2.APP_KEY, SinaOauth2.REDIRECT_URL,
//				null);
//		mTencent = Tencent.createInstance(TencentOauth2.APP_ID, getActivity());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (callDialog != null && callDialog.isShowing()) {
			callDialog.dismiss();
		}
	}

	

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mGoBack = view.findViewById(R.id.titlebar_back_arrow);
		mTitleBarIcon = view.findViewById(R.id.titlebar_icon);
		mTitle = (TextView) view.findViewById(R.id.titlebar_title);
		mSearchBtn = view.findViewById(R.id.titlebar_search_button);
		mRightMenu = view.findViewById(R.id.titlebar_applist_button_container);

		mUid = (AutoCompleteTextView) view.findViewById(R.id.user_login_name);
		if (!TextUtils.isEmpty(SharedPrefManager
				.getLastLoginUserName(getActivity()))) {
			mUid.setText(SharedPrefManager.getLastLoginUserName(getActivity()));
		}
		mPwd = (EditText) view.findViewById(R.id.user_login_pwd);
		mUserTips = view.findViewById(R.id.login_user_tips_btn);
		mPasswordTips = view.findViewById(R.id.login_password_tips_btn);

		user_login_regist = (TextView) view
				.findViewById(R.id.user_login_regist);
		mLogin = view.findViewById(R.id.user_login_login);
		mSinaOauth2 = view.findViewById(R.id.user_login_sina);
		mTencentWeiboOauth2 = view.findViewById(R.id.user_login_tencentweibo);

		mGoBack.setVisibility(View.VISIBLE);
		mTitleBarIcon.setVisibility(View.GONE);
		mSearchBtn.setVisibility(View.GONE);
		mRightMenu.setVisibility(View.INVISIBLE);
		mTitle.setText(R.string.titlebar_title_login);
		view.findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);

		tv_show_user_login_tip = (TextView) view
				.findViewById(R.id.tv_show_user_login_tip);
		user_login_forgot_password = (TextView) view
				.findViewById(R.id.user_login_forgot_password);
		user_login_forgot_password.getPaint().setFlags(
				Paint.UNDERLINE_TEXT_FLAG);// 下载线
		user_login_regist.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);// 下载线
		mGoBack.setOnClickListener(this);
		mTitle.setOnClickListener(this);
		mUserTips.setOnClickListener(this);
		user_login_forgot_password.setOnClickListener(this);
		mPasswordTips.setOnClickListener(this);
		user_login_regist.setOnClickListener(this);
		mLogin.setOnClickListener(this);
		mSinaOauth2.setOnClickListener(this);
		mTencentWeiboOauth2.setOnClickListener(this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getActivity(), // 定义匹配源的adapter
				android.R.layout.simple_dropdown_item_1line,
				new String[] { "qxmwrr@3g.sina.com" });
		mUid.setAdapter(adapter);
		
		mUid.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!TextUtils.isEmpty(mUid.getText())) {
					mUserTips.setVisibility(View.VISIBLE);
				}else{
					mUserTips.setVisibility(View.GONE);
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
		
		mPwd.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!TextUtils.isEmpty(mPwd.getText())) {
					mPasswordTips.setVisibility(View.VISIBLE);
				}else{
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
		
	}

	@Override
	public void onClick(View v) {
		UserActivity userActivity = getUserActivity();
		switch (v.getId()) {
		case R.id.user_login_sina:
//			mSsoHandler = new SsoHandler(getActivity(), mWeibo);
//			mSsoHandler.authorize(new SinaOauthCallback());
			break;
		// case R.id.user_login_qq:
		// mTencent.login(getActivity(), "all", new TencentOauth2Callback());
		// break;
		case R.id.titlebar_back_arrow:
		case R.id.titlebar_title:
			if (userActivity != null) {
				userActivity.onFragmentGoBack();
			}
			break;
		case R.id.login_user_tips_btn:
			mUid.setText("");
			break;
		case R.id.login_password_tips_btn:
			mPwd.setText("");
			break;
		case R.id.user_login_forgot_password:// 忘记密码
			forgotPassword();
			break;
		case R.id.user_login_regist:
			if (userActivity != null) {
				userActivity.showRegistFragment();
			}
			break;
		case R.id.user_login_login:
			if (NetworkUtil.isNetWorking(getActivity())) {
				doLogin(v);
			} else {
				hideKeyboard(v);
				tv_show_user_login_tip.setText("");
				showShortToast(getString(R.string.toast_no_network));
			}
			break;
		case R.id.user_login_tencentweibo:
			// 解决当顶端的fragment是UserRegistFragment仍然响应TencentWeiboAuth的BUG
			if (NetworkUtil.isNetWorking(getActivity())) {
				if (!UserRegistFragment.isIsUserRegistFragmentTop()) {
					hideKeyboard(v);
					TencentWeiboAuth();
				}
			} else {
				hideKeyboard(v);
				tv_show_user_login_tip.setText(R.string.toast_no_network);
			}

			break;
		}
	}

	private void forgotPassword() {

		callDialog = new Dialog(getActivity(), R.style.DialogTheme);
		// 设置它的ContentView
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_forgot_password, null);
		Button btn_call = (Button) view.findViewById(R.id.btn_call);
		btn_call.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SystemUtil.makeCall(getActivity(), "400-000-0000");
				callDialog.dismiss();
			}
		});
		Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				callDialog.dismiss();
			}
		});
		callDialog.setContentView(view);
		callDialog.show();
	}

	private void doLogin(View v) {
		if (!checkUid()) {
			tv_show_user_login_tip
					.setText(getString(R.string.toast_uid_cannot_null));
			mUid.requestFocus();
			mUid.requestFocusFromTouch();
			return;
		}
		if (!checkPwd()) {
			tv_show_user_login_tip
					.setText(getString(R.string.toast_pwd_cannot_null));
			mPwd.requestFocus();
			mPwd.requestFocusFromTouch();
			return;
		}
		hideKeyboard(v);
		String uid = mUid.getText().toString().trim();
		String pwd = mPwd.getText().toString().trim();
		md5Pwd = StringUtil.md5Encoding(pwd);

		ProtocolTask task = new ProtocolTask(getActivity());
		task.setListener(this);
		task.execute(Constants.LIST_URL + "?qt=" + Constants.USERLOGIN
				+ "&uid=" + uid + "&pwd=" + md5Pwd, null, "UserLoginFragment",
				getHeader());
		LogUtil.i("0419", "wwwwwwww md5Pwd = " + md5Pwd);
		showProgressDialog(MyApplication.getInstance().getString(
				R.string.dialog_msg_logining));

	}

	@Override
	public void onNoNetworking() {
		tv_show_user_login_tip.setText(getString(R.string.toast_no_network));
	}

	@Override
	public void onNetworkingError() {
		tv_show_user_login_tip
				.setText(getString(R.string.toast_network_connect_error));
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		try {
			JSONObject json = new JSONObject(new String(bytes));
			int resultStatus = json.getInt("resultStatus");
			if (resultStatus == 1) {
				int allcount = json.getInt("allCount");
				if (allcount == 0) {
					// 证明用户名或密码有问题
					String errorResult = json.optString("data");
					// showShortToast(errorResult);
					tv_show_user_login_tip.setText(errorResult);
				} else {
					JSONObject result = json.getJSONObject("data");
					UserData user = JsonParse.parseUserInfo(result);
					if (user != null) {
						if (result.isNull("SEX")) {
							user.setGender(UserData.MALE);
						} else {
							int sex = result.getInt("SEX");
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
						user.setUid(result.getString("USID"));
						user.setType(UserData.TYPE_ME);
						user.setMd5(md5Pwd);
						SharedPrefManager.setLastLoginUserName(getActivity(),
								user.getUid());
						setLoginUserInfo(user);
					} else {
						// showShortToast(getString(R.string.toast_uid_or_pwd_error));
						tv_show_user_login_tip
								.setText(getString(R.string.toast_uid_or_pwd_error));
					}
				}
			} else {
				// showShortToast(getString(R.string.toast_uid_or_pwd_error));
				tv_show_user_login_tip
						.setText(getString(R.string.toast_uid_or_pwd_error));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		dismissProgressDialog();
	}

	private boolean checkPwd() {
		if (mPwd.getText().toString().trim().equals("")) {
			return false;
		}
		return true;
	}

	private boolean checkUid() {
		if (mUid.getText().toString().trim().equals("")) {
			return false;
		}
		return true;
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

	/**
	 * 当获取到新浪微博认证
	 * 
	 * @param token
	 */
	private void onSinaTokenComplete(final SinaAccessToken token) {
		// mOauthDialog = createDialog(getString(R.string.dialog_title_hint),
		// getString(R.string.dialog_msg_reading_userinfo));
		// mOauthDialog.show();
		showShortToast(getString(R.string.login_readinfo));
		// tv_show_user_login_tip.setText("正在读取用户资料");
		if (token.getUid() == null) {
			// dismissDialog(mOauthDialog);
			return;
		}
		ProtocolTask task = new ProtocolTask(getActivity());
		task.setListener(new OnGetUserInfoFromServer(UserData.TYPE_SINA, token
				.getToken(), token.getUid()));
		task.execute(Constants.LIST_URL + "?qt=" + Constants.ISES + "&uuid="
				+ token.getUid(), null, "TAG", getHeader());
	}

	/**
	 * 当获取到腾讯QQ认证
	 * 
	 * @param token
	 */
	private void onTencentTokenComplete(TencentAccessToken token) {
		// mOauthDialog = createDialog(getString(R.string.dialog_title_hint),
		// getString(R.string.dialog_msg_reading_userinfo));
		// mOauthDialog.show();
    	showShortToast(getString(R.string.login_readinfo));
		// tv_show_user_login_tip.setText("正在读取用户资料");
		ProtocolTask task = new ProtocolTask(getActivity());
		task.setListener(new OnGetUserInfoFromServer(UserData.TYPE_TENCENT,
				token.getToken(), token.getOpenId()));
		task.execute(Constants.LIST_URL + "?qt=" + Constants.ISES + "&uuid="
				+ token.getOpenId(), null, "TAG", getHeader());
	}

	private void onTencentWeiboTokenComplete() {
		// mOauthDialog = createDialog(getString(R.string.dialog_title_hint),
		// getString(R.string.dialog_msg_reading_userinfo));
		// mOauthDialog.show();
    	showShortToast(getString(R.string.login_readinfo));
		// tv_show_user_login_tip.setText("正在读取用户资料");
		ProtocolTask task = new ProtocolTask(getActivity());
//		String token = com.tencent.weibo.sdk.android.api.util.Util
//				.getSharePersistent(getActivity(), "ACCESS_TOKEN");
//		String openid = com.tencent.weibo.sdk.android.api.util.Util
//				.getSharePersistent(getActivity(), "OPEN_ID");
//		task.setListener(new OnGetUserInfoFromServer(
//				UserData.TYPE_TENCENT_WEIBO, token, openid));
//		task.execute(Constants.LIST_URL + "?qt=" + Constants.ISES + "&uuid="
//				+ openid, null, "TAG", getHeader());
	}

	private class OnGetUserInfoFromServer implements TaskListener {

		String token;
		String uid;
		int type;

		/**
		 * 服务器返回登录结果监听器
		 * 
		 * @param type
		 *            登录类型
		 * @param token
		 *            第三方token
		 * @param uid
		 *            第三方登录唯一标识
		 */
		public OnGetUserInfoFromServer(int type, String token, String uid) {
			this.token = token;
			this.uid = uid;
			this.type = type;
		}

		@Override
		public void onPostExecute(byte[] bytes) {
			// dismissDialog(mOauthDialog);
			try {
				JSONObject json = new JSONObject(new String(bytes));
				int resultStatus = json.getInt("resultStatus");
				int allCount = json.getInt("allCount");
				if (resultStatus == 1 && allCount > 0) {
					haveUserInfoInServer(json);
				} else {
					readUserInfoFromOther();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 从第三方读取用户资料
		 */
		private void readUserInfoFromOther() {/*
			// mOauthDialog =
			// createDialog(getString(R.string.dialog_title_hint),
			// getString(R.string.dialog_msg_reading_userinfo));
			// mOauthDialog.show();
			if (type == UserData.TYPE_TENCENT_WEIBO) {
				TencentWeiboOauth2.getUserInfo(getActivity(),
						new OnTencentWeiboUserReceive() {

							@Override
							public void onUserReceive(UserData user) {
								if (user != null) {
									try {
										JSONObject send = new JSONObject();
										send.put("INTYPE", type);
										send.put("NICKNAME", user.getNickname());
										send.put("SEX", user.getGender());
										send.put("ICON", user.getIconUrl());
										final UserData userInfo = user;
										ProtocolTask task = new ProtocolTask(
												getActivity());
										task.setListener(new TaskListener() {

											@Override
											public void onPostExecute(
													byte[] bytes) {
												// dismissDialog(mOauthDialog);
												try {
													JSONObject json = new JSONObject(
															new String(bytes));
													int resultStatus = json
															.getInt("resultStatus");
													if (resultStatus == 1) {
														setLoginUserInfo(userInfo);
													} else {
														mHander.sendEmptyMessage(GETUSER_FAIL);
													}
													// json.toString();
												} catch (JSONException e) {
													e.printStackTrace();
												}
											}

											@Override
											public void onNoNetworking() {
												// dismissDialog(mOauthDialog);
											}

											@Override
											public void onNetworkingError() {
												// dismissDialog(mOauthDialog);
											}
										});
										task.execute(Constants.LIST_URL
												+ "?qt=" + Constants.NUSER
												+ "&uuid=" + uid,
												send.toString(), "TAG",
												getHeader());
									} catch (JSONException e) {
										e.printStackTrace();
									}
								} else {
									mHander.sendEmptyMessage(GETUSER_FAIL);
								}
							}
						});
			} else if (type == UserData.TYPE_SINA) {
				SinaOauth2.getUserInfo(token, uid, new OnUserReceive() {

					@Override
					public void onReceive(UserData user) {
						if (user != null) {
							try {
								JSONObject send = new JSONObject();
								send.put("INTYPE", type);
								send.put("NICKNAME", user.getNickname());
								send.put("SEX", user.getGender());
								send.put("ICON", user.getIconUrl());
								final UserData userInfo = user;
								ProtocolTask task = new ProtocolTask(
										getActivity());
								task.setListener(new TaskListener() {

									@Override
									public void onPostExecute(byte[] bytes) {
										// dismissDialog(mOauthDialog);
										try {
											JSONObject json = new JSONObject(
													new String(bytes));
											int resultStatus = json
													.getInt("resultStatus");
											if (resultStatus == 1) {
												setLoginUserInfo(userInfo);
											} else {
												mHander.sendEmptyMessage(GETUSER_FAIL);
											}
											// json.toString();
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}

									@Override
									public void onNoNetworking() {
										// dismissDialog(mOauthDialog);
									}

									@Override
									public void onNetworkingError() {
										// dismissDialog(mOauthDialog);
									}
								});
								task.execute(Constants.LIST_URL + "?qt="
										+ Constants.NUSER + "&uuid=" + uid,
										send.toString(), "TAG", getHeader());
							} catch (JSONException e) {
								e.printStackTrace();
							}
						} else {
							mHander.sendEmptyMessage(GETUSER_FAIL);
						}
					}

					@Override
					public void onFail() {

					}
				});
			} else {
				// new Thread() {
				// public void run() {
				// UserData user = null;
				// if(type == UserData.TYPE_SINA){
				// user = SinaOauth2.getUserInfo(token, uid);
				// } else if( type == UserData.TYPE_TENCENT){
				// user = TencentOauth2.getUserInfo(mTencent);
				// }
				// if (user != null) {
				// try {
				// JSONObject send = new JSONObject();
				// send.put("INTYPE", type);
				// send.put("NICKNAME", user.getNickname());
				// send.put("SEX", user.getGender());
				// send.put("ICON", user.getIconUrl());
				// final UserData userInfo = user;
				// ProtocolTask task = new ProtocolTask(getActivity());
				// task.setListener(new TaskListener() {
				//
				// @Override
				// public void onPostExecute(byte[] bytes) {
				// // dismissDialog(mOauthDialog);
				// try {
				// JSONObject json = new JSONObject(new String(bytes));
				// int resultStatus = json.getInt("resultStatus");
				// if(resultStatus == 1){
				// setLoginUserInfo(userInfo);
				// } else {
				// mHander.sendEmptyMessage(GETUSER_FAIL);
				// }
				// // json.toString();
				// } catch (JSONException e) {
				// e.printStackTrace();
				// }
				// }
				//
				// @Override
				// public void onNoNetworking() {
				// // dismissDialog(mOauthDialog);
				// }
				//
				// @Override
				// public void onNetworkingError() {
				// // dismissDialog(mOauthDialog);
				// }
				// });
				// task.execute(Constants.LIST_URL + "?qt=" + Constants.NUSER +
				// "&uuid=" + uid, send.toString(), "TAG", getHeader());
				// } catch (JSONException e) {
				// e.printStackTrace();
				// }
				// } else {
				// mHander.sendEmptyMessage(GETUSER_FAIL);
				// }
				// }
				// }.start();
			}

		*/}

		/**
		 * 服务器有第三方登录用户信息直接解析显示
		 * 
		 * @param json
		 * @throws JSONException
		 */
		private void haveUserInfoInServer(JSONObject json) throws JSONException {
			JSONObject result = json.getJSONObject("data");
			UserData user = new UserData();
			user.setType(type);
			user.setNickname(result.getString("NICKNAME"));
			user.setIconUrl(result.getString("ICON"));
			user.setGender(result.getString("SEX"));
			user.setUid(result.getString("USID"));
			setLoginUserInfo(user);
		}

		@Override
		public void onNoNetworking() {
			// dismissDialog(mOauthDialog);

		}

		@Override
		public void onNetworkingError() {
			// dismissDialog(mOauthDialog);

		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {/*
		super.onActivityResult(requestCode, resultCode, data);
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
		mTencent.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_REGIST) {
			if (resultCode == RESULT_CODE_SUCC) {
				String uid = data.getStringExtra("uid");
				mUid.setText(uid);
			}
		} else if (requestCode == REQUEST_CODE_TENCENT_WEIBO) {
			String token = com.tencent.weibo.sdk.android.api.util.Util
					.getSharePersistent(getActivity(), "ACCESS_TOKEN");
			if (token != null && !token.equals("")) {
				onTencentWeiboTokenComplete();
			}
		}

	*/}

	/**
	 * 设置登录用户，放到缓存中
	 * 
	 * @param user
	 */
	private void setLoginUserInfo(UserData user) {
		MyApplication.getInstance().setUser(user);
		MyApplication.getInstance().getUser().setLogin(true);
		// FileUtil.deleteUserIconFromFile(UserData.ICON_NAME);
		mHander.sendEmptyMessage(GETUSER_SUCCESS);
	}

	/**
	 * 新浪微博登录回调接口
	 * 
	 * @author qiuximing
	 */
	private class SinaOauthCallback 
//	implements WeiboAuthListener 
	{/*

		@Override
		public void onComplete(Bundle values) {
			final String code = values.getString("code");
			if (code == null) {
				// uid=2679767090,
				// userName=邱喜明,
				// expires_in=142085,
				// remind_in=142085,
				// access_token=2.00qpB3vC03b2RKf52362875eUvgc_E
				String token = values.getString("access_token");
				String expires_in = values.getString("expires_in");
				SinaAccessToken accessToken = new SinaAccessToken(token,
						expires_in);
				accessToken.setUid(values.getString("uid"));
				if (accessToken.isSessionValid()) {
					SinaAccessTokenKeeper.keepAccessToken(getActivity(),
							accessToken);
					mHander.sendMessage(mHander.obtainMessage(
							SINA_TOKEN_COMPLETE, accessToken));
				}
			} else {
				SinaOauth2.getAccessToken(code, new OnAccessTokenReceive() {

					@Override
					public void onReceive(SinaAccessToken token) {
						SinaAccessTokenKeeper.keepAccessToken(getActivity(),
								token);
						mHander.sendMessage(mHander.obtainMessage(
								SINA_TOKEN_COMPLETE, token));
					}

					@Override
					public void onFail() {

					}
				});
			}
		}

		@Override
		public void onError(WeiboDialogError e) {
			showShortToast(getString(R.string.toast_oauth_error));
		}

		@Override
		public void onCancel() {
			// showShortToast("取消授权");
		}

		@Override
		public void onWeiboException(WeiboException e) {
			showShortToast(getString(R.string.toast_oauth_error));
		}

	*/}

	private void TencentWeiboAuth() {/*
		long appid = Long.valueOf(com.tencent.weibo.sdk.android.api.util.Util
				.getConfig().getProperty("APP_KEY"));
		String app_secket = com.tencent.weibo.sdk.android.api.util.Util
				.getConfig().getProperty("APP_KEY_SEC");
		auth(appid, app_secket);
	*/}

	private void auth(long appid, String app_secket) {/*

		AuthHelper.register(getActivity(), appid, app_secket,
				new OnAuthListener() {

					@Override
					public void onWeiBoNotInstalled() {
						// Toast.makeText(getActivity(), "onWeiBoNotInstalled",
						// Toast.LENGTH_SHORT)
						// .show();
						Intent i = new Intent(getActivity(), Authorize.class);
						startActivityForResult(i, REQUEST_CODE_TENCENT_WEIBO);
					}

					@Override
					public void onWeiboVersionMisMatch() {
						// Toast.makeText(getActivity(),
						// "onWeiboVersionMisMatch",
						// Toast.LENGTH_SHORT).show();
						Intent i = new Intent(getActivity(), Authorize.class);
						startActivityForResult(i, REQUEST_CODE_TENCENT_WEIBO);
					}

					@Override
					public void onAuthFail(int result, String err) {
						Toast.makeText(getActivity(), "result : " + result,
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onAuthPassed(String name, WeiboToken token) {
						// Toast.makeText(getActivity(), "授权成功",
						// Toast.LENGTH_SHORT).show();
						//
						com.tencent.weibo.sdk.android.api.util.Util
								.saveSharePersistent(getActivity(),
										"ACCESS_TOKEN", token.accessToken);
						com.tencent.weibo.sdk.android.api.util.Util
								.saveSharePersistent(getActivity(),
										"EXPIRES_IN",
										String.valueOf(token.expiresIn));
						com.tencent.weibo.sdk.android.api.util.Util
								.saveSharePersistent(getActivity(), "OPEN_ID",
										token.openID);
						// Util.saveSharePersistent(context, "OPEN_KEY",
						// token.omasKey);
						com.tencent.weibo.sdk.android.api.util.Util
								.saveSharePersistent(getActivity(),
										"REFRESH_TOKEN", "");
						// Util.saveSharePersistent(context, "NAME", name);
						// Util.saveSharePersistent(context, "NICK", name);
						com.tencent.weibo.sdk.android.api.util.Util
								.saveSharePersistent(
										getActivity(),
										"CLIENT_ID",
										com.tencent.weibo.sdk.android.api.util.Util
												.getConfig().getProperty(
														"APP_KEY"));
						com.tencent.weibo.sdk.android.api.util.Util
								.saveSharePersistent(getActivity(),
										"AUTHORIZETIME", String.valueOf(System
												.currentTimeMillis() / 1000l));
						//
						onTencentWeiboTokenComplete();
					}
				});

		AuthHelper.auth(getActivity(), "");
	*/}

	/**
	 * QQ登录回调接口
	 * 
	 * @author qiuximing
	 * 
	 */
	private class TencentOauth2Callback 
//	implements IUiListener
	{/*

		@Override
		public void onComplete(JSONObject response) {
			TencentAccessToken token = new TencentAccessToken();
			try {
				token.setToken(response.getString("access_token"));
				token.setExpiresTime(response.getLong("expires_in") * 1000
						+ System.currentTimeMillis());
				token.setExpiresIn(response.getString("expires_in"));
				token.setOpenId(response.getString("openid"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			TencentAccessTokenKeeper.keepAccessToken(getActivity(), token);
			mHander.sendMessage(mHander.obtainMessage(TENCENT_TOKEN_COMPLETE,
					token));
		}

		@Override
		public void onError(UiError e) {
			showShortToast(getString(R.string.toast_oauth_error));
		}

		@Override
		public void onCancel() {
		}
	*/}
}
