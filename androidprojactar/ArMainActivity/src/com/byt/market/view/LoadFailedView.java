package com.byt.market.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.activity.MainActivity;

public class LoadFailedView extends LinearLayout {
	private ImageView iv_icon;
	private TextView tv_msg;
	private Button btn_find_game;
	private Context mContext;

	public LoadFailedView(Context context) {
		super(context);
		this.mContext = context;
		inflate(context, R.layout.listitem_loadfailed, this);
		initView();
		addEvent();
	}

	public LoadFailedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		inflate(context, R.layout.listitem_loadfailed, this);
		initView();
		addEvent();
	}

	private void initView() {
		iv_icon = (ImageView) findViewById(R.id.iv_icon);
		tv_msg = (TextView) findViewById(R.id.tv_msg);
		btn_find_game = (Button) findViewById(R.id.btn_find_game);
	}

	private void addEvent() {
		btn_find_game.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Constants.Action.CHANGE_TO_HOME);
				mContext.sendBroadcast(intent);
				intent = new Intent(mContext, MainActivity.class);
				intent.putExtra("from", 1);
				/**/intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//设置不要刷新将要跳到的界面  
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mContext.startActivity(intent);
			}
		});
	}

	/**
	 * 设置加载失败显示的icon
	 * 
	 * @param resID
	 */
	public void setImageIcon(int resID) {
		iv_icon.setImageResource(resID);
	}

	/**
	 * 设置加载失败显示的消息
	 * 
	 * @param msg
	 */
	@SuppressLint("ResourceAsColor")
	public void setText(String msg) {
		if (msg == null) {
			return;
		}
		tv_msg.setText(msg);
		tv_msg.setTextColor(mContext.getResources().getColor(R.color.text_gray));
	}

	/**
	 * 设置加载失败是否显示找游戏去按钮
	 * 
	 * @param visible
	 */
	public void setButtonVisible(int visible) {
		btn_find_game.setVisibility(visible);
	}
}
