package com.byt.market.qiushibaike;


import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.SubCommentFrame;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.AppComment;
import com.byt.market.data.CateItem;
import com.byt.market.net.NetworkUtil;
import com.byt.market.qiushibaike.ui.JokeCommentFragment;
import com.byt.market.qiushibaike.ui.JokeImageFragment;
import com.byt.market.qiushibaike.ui.JokeTextFragment;
import com.byt.market.qiushibaike.ui.JokeTextImageFragment;
import com.byt.market.ui.CommentFragment;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.view.AlwsydMarqueeTextView;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class JokeCommentActivity extends FragmentActivity implements OnPageChangeListener, OnClickListener {
	private static final String TAG = "JokeCommentActivity";
	private AlwsydMarqueeTextView tv_title;
    private EditText joke_comment;
    private Button comment_submit;
    private ViewPager mPager;
    private ArrayList<BaseUIFragment> fragmentList; 
    private JokeCommentFragment jokecommentFragment;
    private ProtocolTask commentTask;
    private String userid;
    private CateItem cateitem;
   

	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();		
		cateitem = bundle.getParcelable("jokecate");
		
		super.onCreate(arg0);
		setContentView(R.layout.joke_comment);
		initView();
		initPager();

	}
	
	private void initView()
	{
		try {
			findViewById(R.id.topline4).setVisibility(View.GONE);
			findViewById(R.id.top_downbutton).setVisibility(View.GONE);
			findViewById(R.id.titlebar_applist_button_container).setVisibility(View.GONE);
			findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
			findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
			findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);			
			tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
			tv_title.setText(getString(R.string.back));
			findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
			findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
			findViewById(R.id.top_downbutton).setOnClickListener(this);	
			
			joke_comment = (EditText)findViewById(R.id.joke_comment);
			joke_comment.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {					
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});			
			comment_submit = (Button)findViewById(R.id.comment_submit);
			comment_submit.setOnClickListener(this);

			
		}catch (Exception e) {
			Log.d(TAG,"initViewerror");
			e.printStackTrace();
		}

	}
	private void initPager()
	{
		mPager = (ViewPager)findViewById(R.id.jokeviewpager);
		fragmentList = new ArrayList<BaseUIFragment>();		
		jokecommentFragment = new JokeCommentFragment();
		jokecommentFragment.setSid(cateitem.sid);
		fragmentList.clear();
		fragmentList.add(jokecommentFragment);		
		mPager.setAdapter(new MyFragmentPagerAdapter(this.getSupportFragmentManager(), fragmentList)); 
        mPager.setCurrentItem(0); 
        mPager.setOnPageChangeListener(this);	
	}

	public class MyFragmentPagerAdapter extends FragmentPagerAdapter
	{
		ArrayList<BaseUIFragment> list;  
        public MyFragmentPagerAdapter(FragmentManager fm,ArrayList<BaseUIFragment> list) {  
            super(getSupportFragmentManager());  
            this.list = list;  
        } 

		@Override
		public Fragment getItem(int arg0) {			
			return list.get(arg0);  
		}

		@Override
		public int getCount() {			
			 return list.size();  
		}
		
	}
	
	public void finishWindow() {
		finish();
	}
	public Toast toast;
	private void showShortToast(String msg) {
		if(toast==null){
			toast= Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		}else{
			toast.setText(msg);
			toast.setDuration(Toast.LENGTH_SHORT);
		}
		toast.show();
	}	
	private class CommitCommentTaskListener implements TaskListener {

		@Override
		public void onNoNetworking() {
		}

		@Override
		public void onNetworkingError() {	
			showShortToast(getString(R.string.toast_no_network));
		}
		
		@Override
		public void onPostExecute(byte[] bytes) {
			isRequesting = false;
			try {
				if (bytes != null) {
					JSONObject result = new JSONObject(new String(bytes));
					int status = JsonParse.parseResultStatus(result);
					if (status == 1) {
						showShortToast(getString(R.string.commentsuccess));						
						//setResult(Constants.COMMENT_RESPONSE_CODE);
						int counts = 0;
						if(!result.isNull("allCount")) 
						{
							counts = result.getInt("allCount");							
						}
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putInt("jokecomm_count", counts);
						bundle.putParcelable("jokecommcate", cateitem);
						intent.putExtras(bundle);
						setResult(Constants.COMMENT_RESPONSE_CODE, intent);
						//---------add by bobo
						intent.setAction(Constants.JOKE_COMMED_BRODCART);
						sendBroadcast(intent);
						//-end
						finish();
						overridePendingTransition(0, R.anim.push_bottom_out);
					} else if (status == 0) {
						Log.e(TAG, "status = "+status+getString(R.string.submiterror));
						showShortToast(getString(R.string.submiterror));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "Exception "+getString(R.string.submiterror));
				showShortToast(getString(R.string.submiterror));
			}
		}	

	} 	
	private boolean isRequesting;
	private HashMap<String, String> getHeader() {
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
	public void requestData(AppComment comment) {
		if (isRequesting) {
			return;
		}
		Log.e(TAG, ""+comment.sid);
		String url = Constants.JOKE_COMMENT_URL + "?qt=" + Constants.UCOMM + "&sid=" + comment.sid;
		Log.e(TAG, ""+url);
		String comm = comment.sdesc;
		String vname = comment.vname;
		userid = MyApplication.getInstance().getUser().getUid();
		JSONObject jObject = null;
		try {
			jObject = new JSONObject();
			jObject.put("USID", userid);
			jObject.put("TYPE", MyApplication.getInstance().getUser().getType());
			jObject.put("CONTENT", comm);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String comments = jObject.toString();
		isRequesting = true;
		ProtocolTask mTask = new ProtocolTask(this);
		mTask.setListener(new CommitCommentTaskListener());
		HashMap<String, String> map = getHeader();
		if (map != null
				&& (map.get("vcode") == null || map.get("model") == null)) {
			Log.e(TAG, "map "+getString(R.string.submiterror));
			Toast.makeText(this, getString(R.string.commentfail), 150).show();
			return;
		}
		mTask.execute(url, comments, tag(), map);
	}

	private void commit_comment(String content, int sid)
	{
		
		/*if(!NetworkUtil.isNetWorking(this))
		{			
			Toast.makeText(this, getString(R.string.toast_no_network), Toast.LENGTH_SHORT).show();
			return;
		}
		
		scoreTask = new ProtocolTask(this);
		scoreTask.setListener(new CommitCommentTaskListener());
		scoreTask.execute(getCommitScoreUrl(), null, tag(),	getHeader());
		
		**/
		AppComment appComment = new AppComment();
		appComment.modle = Build.MODEL;
		appComment.name = "uname";
		appComment.sdesc = content;		
		appComment.sid = sid;
		requestData(appComment);
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch(view.getId())
		{
		    case R.id.comment_submit:
				if (NetworkUtil.isNetWorking(this)) 
				{
					String content = joke_comment.getText().toString().trim();
					if (TextUtils.isEmpty(content)) 
					{
						joke_comment.setHintTextColor(Color.RED);
						return;
					}
					/*if (content.length() < 5 || content.length() > 140) 
					{

						showShortToast(getString(R.string.commenttextmax));
						return;
					}//*/
					commit_comment(content, 
							cateitem.sid);
				} 
				else 
				{
					showShortToast(getString(R.string.toast_no_network));
					return;
				}
			
			    break;
		    case R.id.titlebar_back_arrow:
				finishWindow();
				break;			    
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int page) {
		
	}
}
