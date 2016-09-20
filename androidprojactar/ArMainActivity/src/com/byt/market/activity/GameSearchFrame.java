package com.byt.market.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.log.LogModel;
import com.byt.market.net.NetworkUtil;
import com.byt.market.ui.HistoryFragment;
import com.byt.market.ui.HotWordFragment;
import com.byt.market.ui.ResultFragment;
import com.byt.market.ui.ThinkFragment;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;

public class GameSearchFrame extends BaseActivity implements OnClickListener {

	public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	public static final int HOTWORD_VIEW = 1;
	public static final int HISTORY_VIEW = 2;
	public static final int THINK_VIEW = 3;
	public static final int LANGUAGE_VIEW = 5;
	public static final int OTHER_VIEW = 6;
	public static final int RESULT_VIEW = 4;
	String searchstring;
	public static int CURRENT_VIEW = HOTWORD_VIEW;

	private EditText mSearchKey;
	private ImageView mVoiceSearch;
	private ImageView mClearSearch;
	private ImageView search_page_icon;
	private ImageView mDoSearch;
	private View mSearchResultBar;
	private AlwsydMarqueeTextView mSearchResultText;
	private ImageButton gotoDownlist;//前往下载页面
	private String key;
	private String oldKey;
	private boolean mIsVoiceEnabled;
	private MarketContext marContext;
	private boolean isOther = true;
	private String from;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.search_frame);
		initData();
		initView();
		addEvent();
		if(searchstring!=null&&!(searchstring.equals("")))
		{
			mSearchKey.setText(searchstring);
			key=searchstring;
			doSearchKeyPre();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			cleanOldKey();
			switch (CURRENT_VIEW) {
			case HOTWORD_VIEW:
				finishWindow();
				return true;
			case HISTORY_VIEW:
				showView(HOTWORD_VIEW);
				return true;
			case THINK_VIEW:
				showView(HOTWORD_VIEW);
				return true;
			case RESULT_VIEW:
				showView(HOTWORD_VIEW);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void initView() {
		mIsVoiceEnabled = true;
		gotoDownlist=(ImageButton) findViewById(R.id.gotodownlist);
		gotoDownlist.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent downloadIntent = new Intent(GameSearchFrame.this,DownLoadManageActivity.class);
				downloadIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
						DownLoadManageActivity.TYPE_FROM_DOWNLOAD);
				startActivity(downloadIntent);
				GameSearchFrame.this.overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
				
			}
		});
		mSearchKey = (EditText) findViewById(R.id.search_page_key);
		mVoiceSearch = (ImageView) findViewById(R.id.search_page_voice); 
		mClearSearch = (ImageView) findViewById(R.id.search_page_clear);
		mDoSearch = (ImageView) findViewById(R.id.search_page_search);
		mSearchKey.setFocusable(true);
		mSearchKey.requestFocus();
		//search_page_icon = (ImageView) findViewById(R.id.search_page_icon);
		showView(HOTWORD_VIEW);
	}

	@Override
	public void initData() {
		from = getIntent().getStringExtra(Constants.LIST_FROM);
		searchstring=getIntent().getStringExtra("searchstring");
		marContext = MarketContext.getInstance();
	}

	@Override
	public void addEvent() {
		mSearchKey.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//					doSearch(mSearchKey.getText().toString().trim());
					doSearchKeyPre();
					// if(LogModel.hasMap.size() == 0 ||
					// !LogModel.hasMap.containsKey(LogModel.L_SEARCH_HIT) ||
					// (LogModel.hasMap.containsKey(LogModel.L_SEARCH_HIT) &&
					// LogModel.hasMap.get(LogModel.L_SEARCH_HIT) == 1))
					// Util.addListData(marketContext, LogModel.L_SEARCH_HIT,
					// LogModel.SEARCH_CATE_OTHER);
				}
				return false;
			}
		});
		// mSearchKey.setOnFocusChangeListener(new OnFocusChangeListener() {
		//
		// public void onFocusChange(View v, boolean hasFocus) {
		// search_page_icon.setVisibility(hasFocus?View.GONE:View.VISIBLE);
		// }
		// });
		mSearchKey.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				
				
				if (NetworkUtil.isNetWorking(GameSearchFrame.this)) {
					if (!TextUtils.isEmpty(mSearchKey.getText().toString().trim())) {
						mVoiceSearch.setVisibility(View.GONE);
						mClearSearch.setVisibility(View.VISIBLE);
						key = s.toString().trim();
						// TODO:联想
						showView(THINK_VIEW);
					} else {
						if (mIsVoiceEnabled) {
							mVoiceSearch.setVisibility(View.GONE);
						}
						mClearSearch.setVisibility(View.INVISIBLE);
						if(CURRENT_VIEW != HOTWORD_VIEW){
							showView(HOTWORD_VIEW);
						}
					}
				}
				if(!TextUtils.isEmpty(mSearchKey.getText().toString())){
					mClearSearch.setVisibility(View.VISIBLE);
				}
			}
		});

		mVoiceSearch.setOnClickListener(this);
		mClearSearch.setOnClickListener(this);
		mDoSearch.setOnClickListener(this);
		mVoiceSearch.setOnClickListener(this);
		//findViewById(R.id.search_page_back_arrow).setOnClickListener(this);
	}

	private OnKeySelectedListener mOnKeySelected = new OnKeySelectedListener() {

		@Override
		public void onKeySelected(String key) {
			isOther = false;
			mIsVoiceEnabled = false;
			mSearchKey.setText(key);
			doSearch(key);
		}

		@Override
		public void onKeyClick(String key) {
			if ("clear_all".equals(key)) {
				new AlertDialog.Builder(GameSearchFrame.this)
						.setTitle(getString(R.string.gs_clearall))
						.setMessage(getString(R.string.gs_isclear))
						.setPositiveButton(getString(R.string.dialog_nowifi_btn_ok),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Util.delData(
												MarketContext.getInstance(),
												null);
										showView(HISTORY_VIEW);
									}
								}).setNegativeButton(getString(R.string.dialog_nowifi_btn_back), null).show();
			} else if ("clear_one".equals(key)) {
				mIsVoiceEnabled = true;
				showView(HISTORY_VIEW);
			} else {
				isOther = false;
				mIsVoiceEnabled = false;
				mSearchKey.setText(key);				
				doSearch(key);
			}
		}
	};

	protected void showView(int i) {
		try {
			FragmentTransaction transaction = this.getSupportFragmentManager()
					.beginTransaction();
			Bundle bundle = new Bundle();
			bundle.putInt(Constants.LIST_FROM, CURRENT_VIEW);
			switch (i) {
			case HOTWORD_VIEW:
				HotWordFragment deFragment = new HotWordFragment();
				deFragment.setOnKeySelectedListener(mOnKeySelected);
				deFragment.setArguments(bundle);
				// transaction.setCustomAnimations(R.anim.push_right_in,
				// R.anim.push_right_out);
				transaction.replace(R.id.contentFrame, deFragment);
				transaction.commitAllowingStateLoss();
				CURRENT_VIEW = HOTWORD_VIEW;
				Util.addListData(maContext, LogModel.L_SEARCH_HIT,
						LogModel.SEARCH_CATE_HOT);
				break;

			case HISTORY_VIEW:
				HistoryFragment hisFragment = new HistoryFragment();
				hisFragment.setArguments(bundle);
				hisFragment.setOnKeySelectedListener(mOnKeySelected);
				transaction.replace(R.id.contentFrame, hisFragment);
				transaction.commitAllowingStateLoss();
				CURRENT_VIEW = HISTORY_VIEW;
				Util.addListData(maContext, LogModel.L_SEARCH_HIT,
						LogModel.SEARCH_CATE_HISTORY);
				break;
			case THINK_VIEW:
				ThinkFragment thinkFragment = new ThinkFragment();
				thinkFragment.setArguments(bundle);
				thinkFragment.setKey(key);
				thinkFragment.setOnKeySelectedListener(mOnKeySelected);
				transaction.replace(R.id.contentFrame, thinkFragment);
				transaction.commitAllowingStateLoss();
				CURRENT_VIEW = THINK_VIEW;
				Util.addListData(maContext, LogModel.L_SEARCH_HIT,
						LogModel.SEARCH_CATE_THINK);
				break;
			case RESULT_VIEW:
				ResultFragment reFragment = new ResultFragment();
				reFragment.setArguments(bundle);
				reFragment.setKey(key);
				// transaction.setCustomAnimations(R.anim.push_left_in,
				// R.anim.push_left_out);
				transaction.replace(R.id.contentFrame, reFragment);
				transaction.commitAllowingStateLoss();
				CURRENT_VIEW = RESULT_VIEW;
				mIsVoiceEnabled = true;
				mSearchKey.clearFocus();
				break;
			}
		
		} catch (Exception e) {
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.search_page_search) {
			doSearchKeyPre();
		}else if (v.getId() == R.id.search_page_voice) {
			voice();
		} else if (v.getId() == R.id.search_page_clear) {
			cleanOldKey();
			mSearchKey.setText("");
			mClearSearch.setVisibility(View.GONE);
		} /*else if (v.getId() == R.id.search_page_back_arrow) {
		cleanOldKey();
		if (TextUtils.isEmpty(mSearchKey.getText().toString())) {
			switch (CURRENT_VIEW) {
			case HOTWORD_VIEW:
				finishWindow();
				break;
			case RESULT_VIEW:
				showView(HOTWORD_VIEW);
				break;
			default:
				break;
			}
		} else {
			mSearchKey.setText("");
		}
	} */
		
	}

	private void cleanOldKey() {
		oldKey = "";
	}

	public void voice() {
		PackageManager pm = getPackageManager();
		try {
			List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
					RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
			if (activities.size() != 0) {
				startVoiceRecognitionActivity();
			} else {
				Toast.makeText(this, R.string.gs_nomodel, 100).show();
			}
		} catch (Exception e) {
		}

	}

	/**
	 * Fire an intent to start the speech recognition activity.
	 */
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.gs_hotword));
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/**
	 * Handle the results from the recognition activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it
			// could have heard
			ArrayList matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			final String[] items = new String[matches.size()];
			matches.toArray(items);
			new AlertDialog.Builder(this)
					.setItems(items, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							key = items[which];
							isOther = false;
							mIsVoiceEnabled = true;
							mSearchKey.setText(key);
							Util.addListData(maContext, LogModel.L_SEARCH_HIT,
									LogModel.SEARCH_CATE_LAUNGE);
							doSearch(key);
						}
					}).setTitle(R.string.gs_whatpoint).show();
		}
	}
	
	public void doSearchKeyPre(){
		if (NetworkUtil.isNetWorking(this)) {
			key = mSearchKey.getText().toString().trim();
			if (key != null && !key.equals("")) {
				if(!key.equals(oldKey)){
					oldKey = key;
					doSearch(key);
				}
			}else{
				mSearchKey.setText("");
				showShortToast(getString(R.string.toast_no_search_key));
			}
		} else {
			showShortToast(getResources().getString(
					R.string.toast_no_network));
		}
	}

	public void doSearch(String key) {
		this.key = key;
		handSerachApp();
		if (mIsVoiceEnabled) {
			CURRENT_VIEW = LANGUAGE_VIEW;
			Util.addListData(maContext, LogModel.L_SEARCH_RS,
					LogModel.SEARCH_CATE_LAUNGE);
		} else if (isOther) {
			CURRENT_VIEW = OTHER_VIEW;
			Util.addListData(maContext, LogModel.L_SEARCH_RS,
					LogModel.SEARCH_CATE_OTHER);
		} else {
			switch (CURRENT_VIEW) {
			case HOTWORD_VIEW:
				Util.addListData(maContext, LogModel.L_SEARCH_RS,
						LogModel.SEARCH_CATE_HOT);
				break;

			case HISTORY_VIEW:
				Util.addListData(maContext, LogModel.L_SEARCH_RS,
						LogModel.SEARCH_CATE_HISTORY);
				break;
			case THINK_VIEW:
				Util.addListData(maContext, LogModel.L_SEARCH_RS,
						LogModel.SEARCH_CATE_THINK);
				break;
			}
		}
		showView(RESULT_VIEW);
	}

	public void handSerachApp() {
		InputMethodManager iMManager = (InputMethodManager) getSystemService("input_method");
		iMManager.hideSoftInputFromWindow(mSearchKey.getWindowToken(), 2);
		Util.addData(marContext, key);
	}

	public interface OnKeySelectedListener {

		/**
		 * 历史关键词、联想关键词被点击
		 * 
		 * @param key
		 */
		public void onKeyClick(String key);

		/**
		 * 历史关键词、联想关键词被选择
		 * 
		 * @param key
		 */
		public void onKeySelected(String key);
	}
	@Override
	public void finish() {
		handSerachApp();
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				GameSearchFrame.super.finish();
			}
		}, 100);
		
	}
}
