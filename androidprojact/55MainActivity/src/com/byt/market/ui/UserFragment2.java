package com.byt.market.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.UserActivity;
import com.byt.market.data.UserData;
import com.byt.market.data.UserData.OnUserInfoChangeListener;
import com.byt.market.oauth2.UserKeeper;

public class UserFragment2 extends UserBaseFragment implements OnUserInfoChangeListener, OnClickListener{
	private View mGoBack;
    private View mTitleBarIcon;
    private TextView mTitle;
    private View mSearchBtn;
    private View mRightMenu;
    
    private ImageView mIcon;
    private TextView mName;
    
    private Handler mHander = new Handler(){
        public void handleMessage(android.os.Message msg) {
            UserData user = MyApplication.getInstance().getUser();
            Bitmap bmpIcon = user.getBmpIcon();
            if(bmpIcon != null){
                mIcon.setImageBitmap(bmpIcon);
            } else {
                mIcon.setImageResource(R.drawable.default_user);
            }
        }
    };
	private View mUserInfo;
	private TextView mLogout;

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
    	mLogout = (TextView) view.findViewById(R.id.back_btn);
    	
    	mUserInfo = view.findViewById(R.id.user_center_info);
    	mIcon = (ImageView) view.findViewById(R.id.user_center_icon);
        mName = (TextView) view.findViewById(R.id.user_center_nick_name);
        
        mGoBack.setVisibility(View.VISIBLE);
        mTitleBarIcon.setVisibility(View.GONE);
        mSearchBtn.setVisibility(View.GONE);
        mRightMenu.setVisibility(View.GONE);
        view.findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
        mTitle.setText(R.string.titlebar_title_account);
        mLogout.setVisibility(View.VISIBLE);
        mLogout.setBackgroundColor(Color.TRANSPARENT);
        mLogout.setTextColor(Color.WHITE);
        mLogout.setText(R.string.titlebar_title_logout);
        
        UserData user = MyApplication.getInstance().getUser();
        if(user.isLogin()){
            String nickName = user.getNickname();
            if(nickName != null){
                mName.setText(nickName);
            }
        }
        
        mGoBack.setOnClickListener(this);
    	mTitle.setOnClickListener(this);
        mUserInfo.setOnClickListener(this);
        mLogout.setOnClickListener(this);
		
		MyApplication.getInstance().mUserInfoChangeList.add(this);
        onUserInfoChange();
	}
    
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		MyApplication.getInstance().mUserInfoChangeList.remove(this);
	}

    @Override
    public void onClick(View v) {
    	UserActivity userActivity = getUserActivity();
        switch(v.getId()){
            case R.id.user_center_info:
//            	if(userActivity != null){
//            		userActivity.showUserInfoFragment();
//            	}
                break;
            case R.id.titlebar_back_arrow:
            case R.id.titlebar_title:
            	if(userActivity != null){
            		userActivity.onFragmentGoBack();
            	}
                break;
            case R.id.back_btn:
            	MyApplication.getInstance().clearUser();
            	UserKeeper.clearUser();
            	MyApplication.getInstance().updateUserInfo();
            	if(userActivity != null){
            		userActivity.onFragmentGoBack();
            	}
            	break;
        }
    }

    @Override
    public void onUserInfoChange() {
        UserData user = MyApplication.getInstance().getUser();
        String nickName = user.getNickname();
        if(nickName != null){
            mName.setText(nickName);
        }
        MyApplication.getInstance().loadUserIcon(new Runnable() {
            
            @Override
            public void run() {
                mHander.sendEmptyMessage(0);
            }
        });
    }
}
