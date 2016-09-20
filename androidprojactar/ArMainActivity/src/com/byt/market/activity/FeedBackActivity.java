package com.byt.market.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.android.api.JPushInterface;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.net.NetworkUtil;
import com.byt.market.util.SharedPrefManager;
import com.byt.market.util.SystemUtil;
import com.byt.market.util.Util;
import com.umeng.analytics.MobclickAgent;

/**
 * 意见反馈
 */
public class FeedBackActivity extends Activity implements OnClickListener,
		TaskListener {

	// constants
	private static final int MSG_DISS_POPUP_WINDOW = 1;

	private static final int FEED_BACK_TYPE_PRODUCT_USE = 1;
	private static final int FEED_BACK_TYPE_FUCTION_SUGGESTION = 2;
	private static final int FEED_BACK_TYPE_EXCEPTION_FEEDBACK = 3;

	// views
	/** 反馈类型 **/
	private Button btn_feed_back_type;
	/** 反馈内容填写区域 **/
	private EditText et_feed_content;
	/** 反馈内容填写区域字数 **/
	private TextView tv_feed_content_count;
	/** 联系方式填写区域 **/
	private EditText et_contact_way;
	/** 联系方式填写区域字数 **/
	private TextView tv_contact_way_count;
	/** 重新填写 **/
	private Button btn_reset;
	/** 提交 **/
	private Button btn_submit;
	public static long umtime; 
	// variables
	private ArrayList<Map<String, String>> feedBackTypeList;
	private ProtocolTask task = new ProtocolTask(this);
	private PopupWindow popupWindow;
	private HashMap<String, Integer> feedBackMaps = new HashMap<String, Integer>();
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_DISS_POPUP_WINDOW:
				try {
					popupWindow.dismiss();
				} catch (Exception e) {
				}
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_feed_back);
		initVariables();
		initViews();
		initListeners();
	}

	private void initVariables() {
		feedBackMaps.put(getString(R.string.txt_feed_back_type_product_use),
				FEED_BACK_TYPE_PRODUCT_USE);
		feedBackMaps.put(
				getString(R.string.txt_feed_back_type_fuction_suggestion),
				FEED_BACK_TYPE_FUCTION_SUGGESTION);
		feedBackMaps.put(
				getString(R.string.txt_feed_back_type_exception_feedback),
				FEED_BACK_TYPE_EXCEPTION_FEEDBACK);
	}

	private void initViews() {
		initTtileBar();
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		btn_feed_back_type = (Button) findViewById(R.id.btn_feed_back_type);
		et_feed_content = (EditText) findViewById(R.id.et_feed_content);
		tv_feed_content_count = (TextView) findViewById(R.id.tv_feed_content_count);
		et_contact_way = (EditText) findViewById(R.id.et_contact_way);
		tv_contact_way_count = (TextView) findViewById(R.id.tv_contact_way_count);
		btn_reset = (Button) findViewById(R.id.btn_reset);
		btn_reset.setEnabled(false);
		btn_submit = (Button) findViewById(R.id.btn_submit);
	}

	private void initTtileBar() {
		View titlebar_back_arrow = findViewById(R.id.titlebar_back_arrow);
		titlebar_back_arrow.setOnClickListener(this);
		View titlebar_icon = findViewById(R.id.titlebar_icon);
		TextView titlebar_title = (TextView) findViewById(R.id.titlebar_title);
		View titlebar_search_button = findViewById(R.id.titlebar_search_button);
		View titlebar_applist_button_container = findViewById(R.id.titlebar_applist_button_container);

		titlebar_back_arrow.setVisibility(View.VISIBLE);
		titlebar_icon.setVisibility(View.GONE);
		titlebar_search_button.setVisibility(View.GONE);
		titlebar_applist_button_container.setVisibility(View.INVISIBLE);
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		titlebar_title.setText(R.string.titlebar_title_suggest);
	}

	private void initListeners() {
		btn_feed_back_type.setOnClickListener(this);
		et_feed_content.addTextChangedListener(new EditTextWatcher(
				et_feed_content, 140, tv_feed_content_count));
		et_contact_way.addTextChangedListener(new EditTextWatcher(
				et_contact_way, 50, tv_contact_way_count));
		btn_reset.setOnClickListener(this);
		btn_submit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_feed_back_type:// 反馈类型
			showTitlePp(btn_feed_back_type, R.array.feed_back_type);
			break;
		case R.id.btn_reset:// 重新填写
			resetPageStatus();
			break;
		case R.id.btn_submit:// 提交
			submitFeedBack();
			break;
		case R.id.titlebar_back_arrow:// 返回
			finish();
			break;
		}
	}

	/**
	 * 重新填写
	 */
	private void resetPageStatus() {
		et_feed_content.setText(null);
		et_contact_way.setText(null);
		btn_feed_back_type.setText(R.string.txt_feed_back_type_product_use);
	}

	/**
	 * 提交反馈
	 */
	private void submitFeedBack() {
		String feedContentStr = et_feed_content.getText().toString();
		String contactWayStr = et_contact_way.getText().toString();
		if (TextUtils.isEmpty(feedContentStr.trim())) {
			SystemUtil.showToast(this, getString(R.string.feedback_inputnull));
			et_feed_content.setFocusable(true);
			et_feed_content.setFocusableInTouchMode(true);
			return;
		}
		btn_submit.setClickable(false);
		
		//Log.d("mylog", "-------反馈-----"+SharedPrefManager.getLastLoginUserName(this)+"------jj--"+JPushInterface.getRegistrationID(getApplicationContext()));
		StringBuilder suggestContent = new StringBuilder();
		suggestContent.append("content=").append(feedContentStr).append("&")
				.append("conn=").append(contactWayStr).append("&")
				.append("usid=").append(SharedPrefManager.getLastLoginUserName(this)).append("&")
				.append("jpRegID=").append(JPushInterface.getRegistrationID(getApplicationContext()));
		String url = Constants.LIST_URL + "?qt=feedback" + "&categ="
				+ feedBackMaps.get(btn_feed_back_type.getText().toString());
		if (task != null) {
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
		if (TextUtils.isEmpty(channel)) {
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

	/**
	 * 弹出反馈下拉框,采用PopUpWindows下拉,而不用Spinner,提升用户体验
	 */
	private void showTitlePp(final TextView show_view, final int array) {
		feedBackTypeList = new ArrayList<Map<String, String>>();
		String[] type_array = getResources().getStringArray(array);
		for (int i = 0; i < type_array.length; i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("title", type_array[i]);
			feedBackTypeList.add(map);
		}
		if (feedBackTypeList == null || feedBackTypeList.size() <= 0) {
			return;
		}
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuViews = (ViewGroup) mLayoutInflater.inflate(
				R.layout.single_select_text, null, true);

		ListView text_list = (ListView) menuViews
				.findViewById(R.id.single_choose_list_text);
		text_list.setAdapter(new SimpleAdapter(this, feedBackTypeList,
				R.layout.pp_list_item, new String[] { "title" },
				new int[] { R.id.text1 }));
		text_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				handler.sendEmptyMessageDelayed(MSG_DISS_POPUP_WINDOW, 100);
				int row_id = position;
				show_view.setText(feedBackTypeList.get(position).get("title"));

			}
		});

		LinearLayout pp_ll_top = (LinearLayout) menuViews
				.findViewById(R.id.pp_ll_top);
		pp_ll_top.setFocusable(true);
		pp_ll_top.setFocusableInTouchMode(true);
		pp_ll_top.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_MENU:
					try {
						if (popupWindow != null && popupWindow.isShowing()) {
							popupWindow.dismiss();
							popupWindow = null;
						}
					} catch (Exception e) {

					}
					break;

				default:
					break;
				}
				return false;
			}
		});
		menuViews.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_MENU:
					try {
						if (popupWindow != null && popupWindow.isShowing()) {
							popupWindow.dismiss();
							popupWindow = null;
						}
					} catch (Exception e) {

					}
					break;

				default:
					break;
				}
				return true;
			}
		});

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int factor = 50;
		if (dm.widthPixels == 800) {
			// note
			factor = 87;
		} else {
			if (dm.widthPixels > 485) {
				// 特大
				factor = 66;
				// y = 130;
			} else if (dm.widthPixels > 330) {
				// 大屏
				factor = 65;
			} else if (dm.widthPixels > 250) {
				// 中
				factor = 44;

			} else {
				// 小
				factor = 33;
			}
		}
		//
		menuViews.getWidth();

		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;

		int location[] = new int[2];
		int x, y;
		show_view.getLocationOnScreen(location);
		x = location[0];
		y = location[1];
		int h = show_view.getHeight();
		int w = show_view.getWidth();
		boolean flag = true;
		int screen_height = dm.heightPixels - statusBarHeight;
		int popupHeight = LayoutParams.WRAP_CONTENT;
		// if ((screen_height / 2) > (y - statusBarHeight + h / 2)) {
		// // 下面空间够
		// flag = true;
		// } else {
		// flag = false;
		// int height = factor * feedBackTypeList.size()
		// + SystemUtil.dip2px(this, 3)
		// * (feedBackTypeList.size() - 1);
		// if (height > Math.abs(y - statusBarHeight)) {
		// popupHeight = Math.abs(y - statusBarHeight)
		// + SystemUtil.dip2px(this, 9);
		// }
		// }
		// 现在只在上面显示
		int height = factor * feedBackTypeList.size()
				+ SystemUtil.dip2px(this, 3) * (feedBackTypeList.size() - 1);
		if (height > Math.abs(y - statusBarHeight)) {
			popupHeight = Math.abs(y - statusBarHeight)
					+ SystemUtil.dip2px(this, 9);
		}

		popupWindow = new PopupWindow(menuViews, w
				+ SystemUtil.dip2px(this, 20), popupHeight, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		// if (flag) {
		// popupWindow.setAnimationStyle(R.style.PopupAnimationDown);
		// popupWindow.showAtLocation(show_view, Gravity.NO_GRAVITY, x
		// - SystemUtil.dip2px(this, 11),
		// y + h - SystemUtil.dip2px(this, 4));
		// } else {
		popupWindow.setAnimationStyle(R.style.PopupAnimationUp);
		popupWindow.showAtLocation(
				show_view,
				Gravity.NO_GRAVITY,
				x - SystemUtil.dip2px(this, 18),
				y - factor * feedBackTypeList.size()
						- SystemUtil.dip2px(this, 3)
						* (feedBackTypeList.size() - 1));
		// }

		popupWindow.update();
	}

	/**
	 * 文本字数监听
	 */
	private final class EditTextWatcher implements TextWatcher {

		private int maxContentNumLimit;
		private EditText watchedEditText;
		private TextView tv_count;
		String tmp;

		public EditTextWatcher(EditText watchedEditText,
				int maxContentNumLimit, TextView tv_count) {
			this.maxContentNumLimit = maxContentNumLimit;
			this.watchedEditText = watchedEditText;
			this.tv_count = tv_count;
		}

		@Override
		public void afterTextChanged(Editable s) {
		
		
			int currentCount = TextUtils.isEmpty(watchedEditText.getText()) ? 0
					: watchedEditText.getText().length();
			
			if(currentCount > 0)
			{
				btn_reset.setEnabled(true);
			}
			else
			{
				btn_reset.setEnabled(false);
			}
			
			if (currentCount > maxContentNumLimit) {
				// TODO 当已超过字数限制时，得进行处理
				int selectionStart = watchedEditText.getSelectionStart();
				int selectionEnd = watchedEditText.getSelectionEnd();
				/*s.delete(selectionStart - 1, selectionEnd);*/
				int tempSelection = selectionStart;
				s=(Editable) s.subSequence(0, maxContentNumLimit);
				watchedEditText.setText(s);		
				Log.d("zxnew","tmp.length()="+tmp.length()+"watchedEditText.getend="+watchedEditText.getText().toString());
				watchedEditText.setSelection(maxContentNumLimit); //设置光标在最后
			}
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			if (count >= maxContentNumLimit)
			{
				tmp=s.toString().substring(0, maxContentNumLimit);
			}

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			int currentCount = TextUtils.isEmpty(watchedEditText.getText()) ? 0
					: watchedEditText.getText().length();
			tv_count.setText(currentCount + "/" + maxContentNumLimit);
		}
	}

	@Override
	public void onNoNetworking() {
		showShortToast(getString(R.string.submit_suggest_fail));
		btn_submit.setClickable(true);
	}

	@Override
	public void onNetworkingError() {
		showShortToast(getString(R.string.submit_suggest_fail));
		btn_submit.setClickable(true);
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		if (bytes != null) {
			try {
				JSONObject json = new JSONObject(new String(bytes));
				if (!json.isNull("data")) {
					int result = json.getInt("data");
					if (result == 1) {
						showShortToast(getString(R.string.submit_suggest_success));
						finish();
						return; 
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		btn_submit.setClickable(true);
		showShortToast(getString(R.string.submit_suggest_fail));
	}

	private void showShortToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	public  void hideSoftInput( View view) {
		final InputMethodManager imm = (InputMethodManager)FeedBackActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(et_contact_way.getWindowToken(), 0);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		MobclickAgent.onPageStart("反馈");
		umtime = System.currentTimeMillis();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
		MobclickAgent.onPageEnd("反馈");
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		int endtime = (int) (System.currentTimeMillis()-umtime);
		MobclickAgent.onEventValue(this, "FB", null,endtime/1000);
	}
	@Override
	public void finish() {
		
		hideSoftInput(et_feed_content);
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				FeedBackActivity.super.finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}
		}, 100);
		
	
	}
}
