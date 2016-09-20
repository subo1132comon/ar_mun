package com.byt.market.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.DetailFrame2;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.AppComment;
import com.byt.market.data.BigItem;
import com.byt.market.net.NetworkUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.view.CusPullListView;

/**
 * 评论列表
 * 
 * @author Administrator
 * 
 */
public class CommentFragment extends CommListViewFragment implements OnTouchListener{
	public static final String SUB_IMG_TYPE = "sub_img";
	private ListView listView;
	/* 评分 */
	private RatingBar rb_score;
	/* 提交评分按钮 */
	private Button btn_commit_score;
	private int sid;
	public boolean isRefresh;
	public int cid;
	private ProtocolTask scoreTask;
	private ProtocolTask mTask;
	/*add by zengxiao*/
	Button canUse;
	Button canUseless;
	Button randinPeople;
	Button rankinTime;
	private boolean headisshow=true;
	private float startx=0;
	/*add end */

	@Override
	protected String tag() {
		return this.getClass().getSimpleName();
	}
	public void setBundle(Bundle bundle) {
		sid = bundle.getInt("sid");
	}
	@Override
	protected String getRequestUrl() {
		getPageInfo().setPageSize(20);
		return Constants.LIST_URL + "?qt=" + Constants.COMM + "&sid=" + sid+"&uid="+MyApplication.getInstance().getUser().getUid()
				+ "&pi=" + getPageInfo().getNextPageIndex() + "&ps="
				+ getPageInfo().getPageSize();
	}

	protected String getNewRequestUrl() {
		return Constants.LIST_URL + "?qt=" + Constants.NCOMM + "&sid=" + sid+"&uid="+MyApplication.getInstance().getUser().getUid()
				+ "&cid=" + cid;
	}

	@Override
	protected String getRequestContent() {
		return null;
	}

	public void pullRefresh() {
		requestNewCommData();
	}
	
//	@Override
//	public void onResume() {
//		// TODO Auto-generated method stub
//		super.onResume();
//		requestNewCommData();
//	}
	
	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		try {
			if (!result.isNull("data")) {
				JSONArray jsonArray = result.getJSONArray("data");
				return JsonParse.parseCommentList(jsonArray);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.comm_listview;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		sid = getArguments().getInt("sid");
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);
		view.findViewById(R.id.lay_headview).setVisibility(View.VISIBLE);
		rb_score = (RatingBar) view.findViewById(R.id.rb_score);
		btn_commit_score = (Button) view.findViewById(R.id.btn_commit_score);
		/*add by zengxiao */		
		canUse=(Button)view.findViewById(R.id.CanUse);
		canUseless=(Button)view.findViewById(R.id.Useless);
		rankinTime=(Button)view.findViewById(R.id.sortintime);
		randinPeople=(Button)view.findViewById(R.id.sortinpeople);
		canUse.setVisibility(View.GONE);
		canUseless.setVisibility(View.GONE);
		rankinTime.setVisibility(View.GONE);
		randinPeople.setVisibility(View.GONE);
		randinPeople.setSelected(true);
		canUse.setOnClickListener(this);
		canUseless.setOnClickListener(this);
		rankinTime.setOnClickListener(this);
		randinPeople.setOnClickListener(this);
		/*and end*/
		listView = (ListView) view.findViewById(R.id.listview);
		//listView.setDividerHeight(5);
		listView.setPadding(10, 10, 10, 20);
		btn_commit_score.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				commitScore();
			}
		});
		
		return view;
	}
	/**
	 * 为游戏评星
	 */
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.CanUse :
			canUse.setSelected(true);
			canUseless.setSelected(false);
			break;
		case R.id.Useless :
			canUse.setSelected(false);
			canUseless.setSelected(true);
			break;
		case R.id.sortintime :
			rankinTime.setSelected(true);
			randinPeople.setSelected(false);
			break;
		case R.id.sortinpeople :
			rankinTime.setSelected(false);
			randinPeople.setSelected(true);
			break;
		}
	}
	private void commitScore(){
		if(!NetworkUtil.isNetWorking(getActivity())){
			showShortToast(MyApplication.getInstance().getResources().getString(R.string.toast_no_network));
			return;
		}
		
		if(rb_score.getRating() == 0) {
			showShortToast(MyApplication.getInstance().getResources().getString(R.string.rastar_forgame));
			return;
		}
		
		
		scoreTask = new ProtocolTask(getActivity());
		scoreTask.setListener(new CommitScoreTaskListener());
		scoreTask.execute(getCommitScoreUrl(), null, tag(),
				getHeader());
	}
	private String getCommitScoreUrl(){
		return Constants.LIST_URL + "?qt=" + Constants.RATING + "&sid=" + sid+"&uid="+MyApplication.getInstance().getUser().getUid()
				+ "&rating=" + rb_score.getRating();
	}
	/**
	 * 请求数据
	 * 
	 * @param url
	 * @param content
	 */
	public void requestNewCommData() {
		if (isRequesting) {
			return;
		}
		isRefresh = true;
		isRequesting = true;
		loadfailed.setVisibility(View.GONE);
		mTask = new ProtocolTask(getActivity());
		mTask.setListener(this);
		mTask.execute(getNewRequestUrl(), getRequestContent(), tag(),
				getHeader());
	}

	@Override
	protected ImageAdapter createAdapter() {
		return new CommentAdapter();
	}

	@Override
	protected void onDownloadStateChanged() {
	}

	@Override
	public void onAppClick(Object obj, String action) {
	}

	@Override
	public void onPostExecute(byte[] bytes) {
		if (!isRefresh) {
			super.onPostExecute(bytes);
			initRating();
			return;
		}
		isRequesting = false;
		try {
			if (bytes != null) {
				JSONObject result = new JSONObject(new String(bytes));
				int status = JsonParse.parseResultStatus(result);
				if (status == 1) {
					List<BigItem> appendList = parseListData(result);
					if (isRefresh) {
						if (appendList != null && !appendList.isEmpty()) {
							cid = appendList.get(0).comments.get(0).sid;
							if (mAdapter.isEmpty()) {
								loading.setVisibility(View.GONE);
								loadfailed.setVisibility(View.GONE);
							}
							mAdapter.addFirst(appendList);
							if (listview.getAdapter() == null) {
								listview.setAdapter(mAdapter);
							}
						} else {
//							showNoResultView();
						}
						isRefresh = false;
					} else {
						if (appendList != null && !appendList.isEmpty()) {
							if (getPageInfo().getPageIndex() == 1) {
								cid = appendList.get(0).comments.get(0).sid;
							}
							if (mAdapter.isEmpty()) {
								loading.setVisibility(View.GONE);
								loadfailed.setVisibility(View.GONE);
								// listview.setVisibility(View.VISIBLE);
								mAdapter.add(appendList);
							} else {
								mAdapter.remove(mAdapter.getCount() - 1);
								mAdapter.add(appendList);
							}

						} else {
							showNoResultView();
						}
					}
				} else {
					setLoadfailedView();
				}
			} else {
				setLoadfailedView();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mAdapter.loadIcon(listview);
		System.gc();
	}
	
	@Override
	protected void setLoadfailedView() {
		// TODO Auto-generated method stub
		super.setLoadfailedView();
		loadfailed.setOnClickListener(this);
	}

	public void showNoResultView() {
		if (mAdapter.isEmpty()) {
			loading.setVisibility(View.GONE);
			loadfailed.setVisibility(View.VISIBLE);
			loadfailed.setImageIcon(R.drawable.nothing_cute);
			loadfailed.setText(MyApplication.getInstance().getResources().getString(R.string.rastar_first));
			loadfailed.setButtonVisible(View.GONE);
			loadfailed.setBackgroundColor(0xffffff);
			loadfailed.setOnClickListener(null);
		} else {
			mAdapter.getItem(mAdapter.getCount() - 1).layoutType = BigItem.Type.LAYOUT_LOADFAILED;
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		int tempCid = appendList.get(0).comments.get(0).sid;
		if (!(tempCid < cid)) {
			cid = tempCid;
		}
	}

	private void initRating() {
		if (rating > 0) {
			rb_score.setRating(rating);
			rb_score.setIsIndicator(true);
			btn_commit_score.setText(MyApplication.getInstance().getResources().getString(R.string.rastar_cando));
			btn_commit_score.setEnabled(false);
		}
	}
	private class CommitScoreTaskListener implements TaskListener {

		@Override
		public void onNoNetworking() {
		}

		@Override
		public void onNetworkingError() {
			showShortToast(CommentFragment.this.getActivity().getString(R.string.rastar_neterror));
		}

		@Override
		public void onPostExecute(byte[] bytes) {
			try {
				JSONObject result = new JSONObject(new String(bytes));
				int status = JsonParse.parseResultStatus(result);
				if(status == 1) {
					if(!result.isNull("allCount")) {
						if(result.getInt("allCount") == 1) {
							btn_commit_score.setEnabled(false);
							btn_commit_score.setText(MyApplication.getInstance().getResources().getString(R.string.rastar_cando));
							rb_score.setIsIndicator(true);
							showShortToast(MyApplication.getInstance().getResources().getString(R.string.rastar_success));
						} else {
							showShortToast(MyApplication.getInstance().getResources().getString(R.string.rastar_fail));
						}
					}
				} else {
					showShortToast(MyApplication.getInstance().getResources().getString(R.string.rastar_netcoll));
				}
			} catch (JSONException e) {
				e.printStackTrace(); 
			}
		}

	} 
	private class CommentAdapter extends ImageAdapter {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

		public CommentAdapter() {
			df.setMaximumFractionDigits(1);
		}

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_COMMENTS:
				view = inflater.inflate(R.layout.listitem_detail_comment_item,
						null);
				holder.layoutType = item.layoutType;
				CommentItemHolder itemHolder = new CommentItemHolder();
				itemHolder.icon = (ImageView) view
						.findViewById(R.id.listitem_comm_icon);
				itemHolder.cname = (TextView) view
						.findViewById(R.id.appcommAuthorName);
				itemHolder.cranting = (RatingBar) view
						.findViewById(R.id.appcommRatingView);
				itemHolder.cupdatetime = (TextView) view
						.findViewById(R.id.appcommTime);
				itemHolder.cdes = (TextView) view
						.findViewById(R.id.listitem_comm_des);
				itemHolder.cmodle = (TextView) view
						.findViewById(R.id.listitem_comm_moble);
				itemHolder.cvname = (TextView) view
						.findViewById(R.id.listitem_comm_vname);
				holder.commentHolder = itemHolder;
				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_LOADING:
				view = inflater.inflate(R.layout.listitem_loading, null);
				holder.layoutType = item.layoutType;

				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_LOADFAILED:
				view = inflater.inflate(R.layout.listitem_loadfailed2, null);
				holder.layoutType = item.layoutType;
				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						retry();
					}
				});
				view.setTag(holder);
				break;
			}

			return view;
		}

		@Override
		public void bindView(int position, BigItem item, BigHolder holder) {
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_COMMENTS:
				for (final AppComment app : item.comments) {
					holder.commentHolder.cname.setText(app.name);
					// holder.commentHolder.cranting.setRating(app.rating);
					holder.commentHolder.cmodle.setText(MyApplication.getInstance().getResources().getString(R.string.rastar_model) + app.modle);
					holder.commentHolder.cvname.setText(MyApplication.getInstance().getResources().getString(R.string.rastar_version) + app.vname);
					holder.commentHolder.cupdatetime.setText(app.comtime);
					holder.commentHolder.cdes.setText(app.sdesc);
				}
				break;
			case BigItem.Type.LAYOUT_LOADING:

				break;
			case BigItem.Type.LAYOUT_LOADFAILED:

				break;
			}
		}

	}
	
	/*add end*/
	/*add for 修改提示语*/
	@Override
	public void setButtonInvi() {
		// TODO Auto-generated method stub
		if(loadfailed!=null)
		{
			loadfailed.setButtonVisible(View.INVISIBLE);
		}
		
	}

	/*add end*/
	/*add by zengxiao for:滑动到顶时隐藏详情头*/
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		if(firstVisibleItem==0&&totalItemCount!=0){
			
			headisshow=true;
			Log.d("zxnew", "headisshow="+headisshow);
        } 
		else{
			
			headisshow=false;  
			Log.d("zxnew", "headisshow="+headisshow);
		}
	}
	/*add end*/

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		switch(arg1.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			 startx=arg1.getRawY();
			break;
		case MotionEvent.ACTION_UP:
			 
			break;
		case MotionEvent.ACTION_MOVE:
			
			 if(headisshow)
			 {
				 Log.d("zxnew", "arg1.get-startx="+(arg1.getY()-startx));
				 if((arg1.getRawY()-startx)>0){
					 ((DetailFrame2)CommentFragment.this.getActivity()).showheaddetail();
				 }
				
					
					
			 }
			 else{
				 if((arg1.getRawY()-startx)<0)
				 ((DetailFrame2)CommentFragment.this.getActivity()).hideheaddetail();
			 }
			break;
		}
		return false;
	}
	@Override
	public void setStyle(CusPullListView listview2) {
			listview.setOnTouchListener(this);
		
		}
}
