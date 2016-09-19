package com.byt.market.activity;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.R;
import com.byt.market.MyApplication;
import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.AppComment;
import com.byt.market.data.AppItem;
import com.byt.market.net.NetworkUtil;
import com.byt.market.net.UrlHttpConnection.Result;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;

/**
 * 主页面，控制其他五个页面
 * 
 * @author Administrator
 * 
 */
public class SubCommentFrame extends BaseActivity implements OnClickListener,
		TaskListener {

	private AppItem appitem;
	private String userid;

	// private RatingBar ratingBar;
	private EditText et_comment;
	private Button btn_sub_comm;
	private Button btn_cancle_comm;
	private TextView tv_text_number;

	// private TextView comm_rat_tip;

	// private ImageView comm_user_icon;
	// private TextView comm_user_name;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sub_comm_frame);
		initView();
		initData();
		addEvent();
	}

	@Override
	public void initView() {
		appitem = getIntent().getParcelableExtra("app");
		// ratingBar = (RatingBar) findViewById(R.id.appRatingView);
		et_comment = (EditText) findViewById(R.id.et_comment);
		btn_sub_comm = (Button) findViewById(R.id.btn_sub_comm);
		// comm_rat_tip = (TextView) findViewById(R.id.comm_rat_tip);
		btn_cancle_comm = (Button) findViewById(R.id.titlebar_back_arrow);
		tv_text_number = (TextView) findViewById(R.id.tv_text_number);
		// comm_user_icon = (ImageView) findViewById(R.id.comm_user_icon);
		// comm_user_name = (TextView) findViewById(R.id.comm_user_name);
	}

	@Override
	public void initData() {
		// Bitmap uBitmap = MyApplication.getInstance().getUser().getBmpIcon();
		// MyApplication.getInstance().loadUserIcon(new Runnable() {
		//
		// @Override
		// public void run() {
		// runOnUiThread(new Runnable() {
		// public void run() {
		// comm_user_icon.setImageBitmap(MyApplication.getInstance()
		// .getUser().getBmpIcon());
		// }
		// });
		// }
		// });
		// comm_user_icon.setImageBitmap(uBitmap);
		// comm_user_name.setText(MyApplication.getInstance().getUser().getNickname());
	}

	@Override
	public void addEvent() {
		btn_sub_comm.setOnClickListener(this);
		btn_cancle_comm.setOnClickListener(this);
		et_comment.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.toString().trim().length() < 5) {
					tv_text_number.setTextColor(Color.RED);
					tv_text_number.setText(5 + "/140");
				} else {
					tv_text_number.setTextColor(0x99000000);
					tv_text_number.setText(s.toString().trim().length() + "/140");
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
	public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			overridePendingTransition(0, R.anim.push_bottom_out);
		}
		return super.onKeyDown(keyCode, keyevent);
	}

	/**
	 * 点击事件处理
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.titlebar_back_arrow) {
			finish();
			overridePendingTransition(0, R.anim.push_bottom_out);
		} else if (v.getId() == R.id.btn_sub_comm) {
			if (NetworkUtil.isNetWorking(this)) {
				String content = et_comment.getText().toString().trim();
				if (TextUtils.isEmpty(content)) {
					et_comment.setHintTextColor(Color.RED);
					return;
				}
				// if (ratingBar.getRating() == 0) {
				// Toast.makeText(SubCommentFrame.this, "请先给游戏评分", 150).show();
				// return;
				// }
				if (content.length() < 5 || content.length() > 140) {
					Toast.makeText(SubCommentFrame.this, R.string.commenttextmax, 150)
							.show();
					return;
				}
				doComments(content, appitem.sid, appitem.vname);
			} else {
				showShortToast(getString(R.string.toast_no_network));
				return;
			}
		}
	}

	protected void doComments(String content, int sid, String vname) {
		AppComment appComment = new AppComment();
		appComment.modle = Build.MODEL;
		appComment.name = "uname";
		appComment.sdesc = content;
		appComment.vname = vname;
		appComment.sid = sid;
		requestData(appComment);
	}

	private boolean isRequesting;

	/**
	 * 请求数据
	 * 
	 * @param url
	 * @param content
	 */
	public void requestData(AppComment comment) {
		if (isRequesting) {
			return;
		}
		String comm = comment.sdesc;
		String vname = comment.vname;
		userid = MyApplication.getInstance().getUser().getUid();
		JSONObject jObject = null;
		try {
			jObject = new JSONObject();
			jObject.put("USID", userid);
			jObject.put("TYPE", MyApplication.getInstance().getUser().getType());
			jObject.put("CONTENT", comm);
			jObject.put("VNAME", vname);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String comments = jObject.toString();
		isRequesting = true;
		ProtocolTask mTask = new ProtocolTask(this);
		mTask.setListener(this);
		HashMap<String, String> map = getHeader();
		if (map != null
				&& (map.get("vcode") == null || map.get("model") == null)) {
			Toast.makeText(this, getString(R.string.commentfail), 150).show();
			return;
		}
		mTask.execute(getRequestUrl(comment.sid), comments, tag(), map);
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
			channel = Util.getChannelName(this);
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("model", model);
		map.put("channel", channel);
		return map;
	}

	private String tag() {
		return this.getClass().getSimpleName();
	}

	private String getRequestUrl(int sid) {
		return Constants.LIST_URL + "?qt=" + Constants.UCOMM + "&sid=" + sid;
	}

	@Override
	public void onNoNetworking() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNetworkingError() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostExecute(byte[] bytes) {
		isRequesting = false;
		try {
			if (bytes != null) {
				JSONObject result = new JSONObject(new String(bytes));
				int status = JsonParse.parseResultStatus(result);
				if (status == 1) {
					Toast.makeText(SubCommentFrame.this, getString(R.string.commentsuccess), 150).show();
					setResult(Constants.COMMENT_RESPONSE_CODE);
					finish();
					overridePendingTransition(0, R.anim.push_bottom_out);
				} else if (status == 0) {
					Toast.makeText(SubCommentFrame.this, getString(R.string.submiterror), 150).show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(SubCommentFrame.this, getString(R.string.submiterror), 150).show();
		}
	}
}
