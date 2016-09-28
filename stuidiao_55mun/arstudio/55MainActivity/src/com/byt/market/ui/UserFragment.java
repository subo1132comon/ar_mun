package com.byt.market.ui;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.MyApplication;
import com.byt.market.activity.RetrievePasswordActivity;
import com.byt.market.activity.UserActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.UserData;
import com.byt.market.data.UserData.OnUserInfoChangeListener;
import com.byt.market.net.NetworkUtil;
import com.byt.market.oauth2.UserKeeper;

public class UserFragment extends UserBaseFragment implements
		OnUserInfoChangeListener, OnClickListener {
	private View mGoBack;
	private View mTitleBarIcon;
	private TextView mTitle;
	private View mSearchBtn;
	private View mRightMenu;
	private ImageView mIcon;
	private EditText et_nick_name;
	private RadioGroup gender_radioGroup;
	private View mUserInfo;
	private TextView save;
	private TextView tip;
	private Button btn_swtich_accoun;
	private Button btn_modify_password;
	private UserData userData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.user_center, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mGoBack = view.findViewById(R.id.titlebar_back_arrow);
		mTitleBarIcon = view.findViewById(R.id.titlebar_icon);
		mTitle = (TextView) view.findViewById(R.id.titlebar_title);
		mSearchBtn = view.findViewById(R.id.titlebar_search_button);
		mRightMenu = view.findViewById(R.id.titlebar_applist_button_container);
		save = (TextView) view.findViewById(R.id.title_btn);

		mUserInfo = view.findViewById(R.id.user_center_info);
		mIcon = (ImageView) view.findViewById(R.id.img_user_avatar);
		et_nick_name = (EditText) view.findViewById(R.id.user_center_nick_name);
		tip = (TextView) view.findViewById(R.id.tv_show_user_regist_tip);

		mGoBack.setVisibility(View.VISIBLE);
		mTitleBarIcon.setVisibility(View.GONE);
		mSearchBtn.setVisibility(View.GONE);
		mRightMenu.setVisibility(View.GONE);
		view.findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		mTitle.setText(R.string.text_mine_setting);
		save.setVisibility(View.VISIBLE);
		// save.setBackgroundColor(Color.TRANSPARENT);
		// save.setTextColor(Color.WHITE);
		save.setText(R.string.text_action_save);

		btn_swtich_accoun = (Button) view.findViewById(R.id.btn_swtich_accoun);
		btn_modify_password = (Button) view
				.findViewById(R.id.btn_modify_password);

		UserData user = MyApplication.getInstance().getUser();
		if (user.isLogin()) {
			String nickName = user.getNickname();
			if (nickName != null) {
				et_nick_name.setText(nickName);
			}
		}

		mGoBack.setOnClickListener(this);
		mTitle.setOnClickListener(this);
		mUserInfo.setOnClickListener(this);
		save.setOnClickListener(this);
		btn_swtich_accoun.setOnClickListener(this);
		btn_modify_password.setOnClickListener(this);

		gender_radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
		if (UserData.MALE.equals(user.getGender())) {
			mIcon.setImageResource(R.drawable.icon_user_default);
			gender_radioGroup.check(R.id.radioMale);
		} else {
			gender_radioGroup.check(R.id.radioFemale);
			mIcon.setImageResource(R.drawable.icon_user_default);
		}
		gender_radioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						int radioButtonId = group.getCheckedRadioButtonId();
						if (radioButtonId == R.id.radioMale) {
							mIcon.setImageResource(R.drawable.icon_user_default);
						} else {
							mIcon.setImageResource(R.drawable.icon_user_default);
						}

					}
				});

		MyApplication.getInstance().mUserInfoChangeList.add(this);
		onUserInfoChange();
		
		if(user.getType() != 0 ){
			btn_modify_password.setEnabled(false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		MyApplication.getInstance().mUserInfoChangeList.remove(this);
	}

	@Override
	public void onClick(View v) {
		UserActivity userActivity = getUserActivity();
		switch (v.getId()) {
		case R.id.user_center_info:
//			if (userActivity != null) {
//				userActivity.showUserInfoFragment();
//			}
			break;
		case R.id.titlebar_back_arrow:
		case R.id.titlebar_title:
			if (userActivity != null) {
				userActivity.onFragmentGoBack();
			}
			break;
		case R.id.title_btn:
			if(!NetworkUtil.isNetWorking(getActivity())){
				showShortToast(getString(R.string.toast_no_network));
				return;
			}
			
			// 保存
			if (checkNickName()) {
				tip.setVisibility(View.GONE);
				hideKeyboard(v);
				final String nickName = et_nick_name.getText().toString();
				int sex = 0;
				if (gender_radioGroup.getCheckedRadioButtonId() == R.id.radioMale) {
					userData.setGender(UserData.MALE);
					sex = 1;
				} else {
					userData.setGender(UserData.FEMALE);
					sex = 0 ;
				}
				userData.setNickname(nickName);
				
				ProtocolTask task = new ProtocolTask(getActivity());
				task.setListener(new TaskListener() {

					@Override
					public void onPostExecute(byte[] bytes) {
						JSONObject json;
						try {
							json = new JSONObject(new String(bytes));
							int resultStatus = json.getInt("resultStatus");
							if (resultStatus == 1) {
								showShortToast(getString(R.string.edit_succ));
								MyApplication.getInstance().setUser(userData);
								UserKeeper.saveUser();
								MyApplication.getInstance().updateUserInfo();
								UserActivity userActivity = getUserActivity();
								if (userActivity != null) {
									userActivity.onFragmentGoBack();
								}
							} else {
								showShortToast(getString(R.string.edit_error));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onNoNetworking() {

					}

					@Override
					public void onNetworkingError() {

					}
				});
				try {

					String pwd = MyApplication.getInstance().getUser().getMd5();
					if (pwd == null) {
						pwd = "";
					}
					JSONObject json = new JSONObject();
					json.put("NICKNAME", nickName);
					json.put("SEX", sex);
					task.execute(Constants.LIST_URL + "?qt=" + Constants.UPUSER
							+ "&uid="
							+ MyApplication.getInstance().getUser().getUid()
							+ "&pwd=" + pwd, json.toString(), "TAG", getHeader());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
//				MyApplication.getInstance().setUser(userData);
//				UserKeeper.saveUser();
//				MyApplication.getInstance().updateUserInfo();
//				if (userActivity != null) {
//					userActivity.onFragmentGoBack();
//				}
			} else {
				tip.setText(R.string.text_action_nickname_input_tip);
				tip.setVisibility(View.VISIBLE);
			}
			break;
		// 切换账号
		case R.id.btn_swtich_accoun:

			MyApplication.getInstance().clearUser();
			UserKeeper.clearUser();
			MyApplication.getInstance().updateUserInfo();
//			Intent intent = new Intent(getActivity(), UserActivity.class);
//			intent.putExtra(UserActivity.FRAGMENT_USER_ACTION,
//					UserActivity.TYPE_USER_LOGIN);
//			startActivity(intent);
			getUserActivity().showLoginFragment();
			break;
		// 修改密码
		case R.id.btn_modify_password:
			Intent intent2 = new Intent(getActivity(),
					RetrievePasswordActivity.class);
			startActivity(intent2);
			break;
		}
	}

	// 4到16个字符
	private boolean checkNickName() {
		String nickName = et_nick_name.getText().toString();
		if (TextUtils.isEmpty(nickName)) {
			return false;
		}
		if (nickName.length() < 4 || nickName.length() > 16) {
			return false;
		}
		return true;

	}

	@Override
	public void onUserInfoChange() {
		userData = MyApplication.getInstance().getUser();
		String nickName = userData.getNickname();
		if (nickName != null) {
			et_nick_name.setText(nickName);
		}
		if (TextUtils.isEmpty(userData.getGender())
				|| "null".equals(userData.getGender())) {
			mIcon.setImageResource(R.drawable.icon_user_default);
		} else {
			if (UserData.MALE.equals(userData.getGender())) {
				mIcon.setImageResource(R.drawable.icon_user_default);
			} else {
				mIcon.setImageResource(R.drawable.icon_user_default);
			}
		}
	}
}
