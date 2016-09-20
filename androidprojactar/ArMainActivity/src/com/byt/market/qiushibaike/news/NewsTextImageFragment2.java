package com.byt.market.qiushibaike.news;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.HtmlTVpalyActivity;
import com.byt.market.activity.JokeDetailsWebViewActivity;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.ShareActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.adapter.ImageAdapter.NewsItemspeesTime;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.display.ZoomAndRounderBitmapDisplayer;
import com.byt.market.data.BigItem;
import com.byt.market.data.CateItem;
import com.byt.market.log.LogModel;
import com.byt.market.net.NetworkUtil;
import com.byt.market.qiushibaike.JokeActivity;
import com.byt.market.qiushibaike.JokeCommentActivity;
import com.byt.market.qiushibaike.JokeDetailsActivity;
import com.byt.market.qiushibaike.PlayGifActivity;
import com.byt.market.tools.LogCart;
import com.byt.market.ui.CommentFragment;
import com.byt.market.ui.HomeFragment;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.DateUtil;
import com.byt.market.util.JsonParse;
import com.byt.market.util.SystemUtil;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.CusPullListView.OnRefreshListener;
import com.byt.market.view.gifview.GifDecoderView;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

public class NewsTextImageFragment2 extends ListViewFragment{
	private static final String TAG = "JokeTextImageFragment";
	String netType;
	private DisplayImageOptions mOptions;
	HashMap<Integer, String> mlist=new HashMap<Integer, String>();
	HashMap<Integer, String> murllist=new HashMap<Integer, String>();
	private TextView joke_comment_count;
	private TextView joke_collect_count;
	private TextView joke_share_count;
	private ImageAdapter  adapter;
	private int REQUEST_REFRESH = 0;
	private int comm_sid;
	private int refreshCount;
	private RelativeLayout qiushilayout;
	private int urlIndex = 0;
	public boolean isjoke ;
	
//	public JokeTextImageFragment(Boolean b){
//		this.isjoke = b;
//	}
	public void setISjoke(Boolean b){
		this.isjoke = b;
	}
	@Override
	protected String getRequestUrl() {
		String u = "";
		
		//if(urlIndex == 0){
			 u = Constants.APK_URL+"Joke/v1.php?qt=Jokelist&cid=4"+"&pi="
					    + getPageInfo().getNextPageIndex() + "&ps="
					    + getPageInfo().getPageSize()+"&stype="+MainActivity.NEWS_KEY2;
		//}
//		if(urlIndex == 1){
//			u = Constants.APK_URL+"Joke/v1.php?qt=Jokelist&cid=4"+"&pi="
//				    + getPageInfo().getNextPageIndex() + "&ps="
//				    + getPageInfo().getPageSize()+"&stype="+MainActivity.JOKE_KEY;
//		}
		Log.d("mylog", "jok-----url----"+u);
    	return u;
	}
	@Override
    public void canRequestGet() {
		request();
    }
	@Override
	protected String getRequestContent() {		
		return null;
	}
	//---add  by-bobo
//	public void setScoll(ScollList scoll){
//		setScoll(scoll);
//	}
	@Override
	public void onAppClick(Object obj, String action) {
		//Intent intent = new Intent(action);
		//JokeDetailsActivity
		Intent intent = new Intent(getActivity(),JokeDetailsWebViewActivity.class);
		if (obj instanceof CateItem) {
			CateItem caItem = (CateItem) obj;	
			
			Bundle bundle = new Bundle();
		//	bundle.putString("joke_content", caItem.content);
		//	bundle.putInt("isshare", caItem.isshare);
		//	bundle.putString("joke_image_path", caItem.ImagePath);
			bundle.putString("type", "new");
			bundle.putInt(("msid"), caItem.sid);
			intent.putExtras(bundle);			
		}			
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);		
	}



	@Override
	protected List<BigItem> parseListData(JSONObject result) {
        try {       	
            return JsonParse.parseJokeList3(getActivity(),result.getJSONArray("data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.listview_joke;
	}

    @Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	netType = Util.getNetAvialbleType(MyApplication.getInstance());
    	if(!TextUtils.isEmpty(netType)&&"wifi".equals(netType)){
    		//mImageFetcher.setLoadingImage(R.drawable.category_loading);
    	}
    	else
    	{
    		//mImageFetcher.setLoadingImage(R.drawable.joke_empty_icon);
    	}
    	mOptions = new DisplayImageOptions.Builder()
		.cacheOnDisc().build();
    }
    
	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) 
	{
        View view = inflater.inflate(res, container, false);
        qiushilayout=(RelativeLayout) view.findViewById(R.id.qiushiheadlayout);
		TextView switch_date=(TextView)view.findViewById(R.id.switch_date);
		switch_date.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View vv) {
				try {
					Log.d("xxx", "vvvvv");
					//jokeTextImageFragment.goTOP();	
					changTheday();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
//		view.findViewById(R.id.top_textView).setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub.set
//				goTOP();
//			}
//		});
        return view;
	}
	
	@Override
	protected ImageAdapter createAdapter() 
	{
        return adapter = new JokeTextImageAdapter();
	}    

    private class JokeTextImageAdapter extends ImageAdapter{
		@Override
        public View newView(LayoutInflater inflater, BigItem item) {
            View view = null;
            BigHolder holder = new BigHolder();
            switch(item.layoutType){
            	case BigItem.Type.LAYOUT_CAEGORYBIG_IC:
            		view = inflater.inflate(R.layout.item_news_big_ic, null);
            		holder.layoutType = item.layoutType;
            		NewsItemHolder newsitemhodler = new NewsItemHolder();
            		newsitemhodler.big_image = (ImageView) view.findViewById(R.id.big_imageView);
            		newsitemhodler.big_text = (TextView) view.findViewById(R.id.big_text);
            		newsitemhodler.big_r_layout = (RelativeLayout) view.findViewById(R.id.item_bigic_ralayout);
            		newsitemhodler.big_text_layout = (RelativeLayout) view.findViewById(R.id.bg_text_layout);
            		holder.newsitemhodler = newsitemhodler;
            		view.setTag(holder);
            		break;
            	case BigItem.Type.LAYOUT_CAEGORYSPES:
            		view = inflater.inflate(R.layout.item_news_speas, null);
            		NewsItemspeesTime spitem = new NewsItemspeesTime();
            		spitem.speas = (TextView) view.findViewById(R.id.textView_sp);
            		holder.spitem = spitem;
            		view.setTag(holder);
            	break;
                case BigItem.Type.LAYOUT_CAEGORYLIST:
                    view = inflater.inflate(R.layout.listitem_news_image, null);
                    holder.layoutType = item.layoutType;
                    JokeItemHolder itemHolder = new JokeItemHolder();
                  //  itemHolder.joker = (TextView) view.findViewById(R.id.joker);
                    itemHolder.content = (TextView) view.findViewById(R.id.jokecontent);    
                    itemHolder.image =   (ImageView) view.findViewById(R.id.jokeimage); 
                  //  itemHolder.isPaly_img = (ImageView) view.findViewById(R.id.is_paly_imageView);
                  //  itemHolder.gif_text = (TextView) view.findViewById(R.id.gif_textV);
                    itemHolder.layout = (LinearLayout) view.findViewById(R.id.jokelistitem); 
                    holder.jokeTextImageHolders.add(itemHolder);
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
//                case BigItem.Type.LAYOUT_CHANGE:
//                	 view = inflater.inflate(R.layout.bestone_qiushi_head, null);
//                     holder.layoutType = item.layoutType;
//                     //JokeItemHolder citemHolder = new JokeItemHolder();
//                     setIschang(true);//设置 不能加载了
//                     ChangHolder chang = new ChangHolder();
//                     chang.changText = (TextView) view.findViewById(R.id.switch_date);
//                     chang.perviousText = (TextView) view.findViewById(R.id.date);
//                     holder.changer = chang;
//                     view.setTag(holder);
//                    break;
                default:
                	break;
            }
            return view;
        }
        @Override
        public void bindView(int position, BigItem item, BigHolder holder) {
            switch (item.layoutType){
                case BigItem.Type.LAYOUT_CAEGORYLIST:
                    ArrayList<JokeItemHolder> jokeTextImageHolders = holder.jokeTextImageHolders;  
                    for (int i = 0; i < item.cateItems.size() && i < jokeTextImageHolders.size(); i++) 
                    {	
                        final CateItem cateItem = item.cateItems.get(i);
                        jokeTextImageHolders.get(i).content.setText(cateItem.cTitle);
                        	Log.d("mylog", "******!imageLoader.getPause()");
                        	jokeTextImageHolders.get(i).image.setVisibility(View.VISIBLE);
                        	imageLoader.displayImage(cateItem.ImagePath, jokeTextImageHolders.get(i).image, mOptions);
                        //////--------------------------
                        jokeTextImageHolders.get(i).layout.setOnClickListener(new MybigicLisenler(cateItem));       
                        try{
                        }catch(Exception e){
                        	 jokeTextImageHolders.get(i).readview.setText("0");
                        }
                    }  
                   
                    break;
                case BigItem.Type.LAYOUT_LOADING:
                    
                    break;
                case BigItem.Type.LAYOUT_LOADFAILED:
                    
                    break;
                case BigItem.Type.LAYOUT_CAEGORYBIG_IC:
                   Log.d("mylog", "设置----"+item.cateItems.size());
                   holder.newsitemhodler.big_text.setText(item.cateItems.get(0).cTitle);
                   holder.newsitemhodler.big_text_layout.getBackground().setAlpha(200);
                  // holder.newsitemhodler.big_image;
                   imageLoader.displayImage(item.cateItems.get(0).ImagePath, holder.newsitemhodler.big_image, mOptions);
                   holder.newsitemhodler.big_r_layout.setOnClickListener(new MybigicLisenler(item.cateItems.get(0)));
                    break;
                case BigItem.Type.LAYOUT_CAEGORYSPES:
                	Log.d("mylog", "&&&&&&&&&&&");
                	String s = item.cateItems.get(0).publish_time;
                	if(s!=null){
                		long time = Long.parseLong(s);
                    	Log.d("mylog", "&&&&&&&&&&&"+time);
                    	holder.spitem.speas.setText(DateUtil.getFormatShortTime(time*1000));
                	}
                	break;
                    default:
                    	break;
            }
        }
        
    }
    
	@Override
	protected void onDownloadStateChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub
		
	}
	/*@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if(requestCode == REQUEST_REFRESH)
		{
			if(resultCode == Constants.COMMENT_RESPONSE_CODE)
			{
				Bundle extras = intent.getExtras();
				CateItem cate = extras.getParcelable("jokecommcate");
				cate.comment_count = extras.getInt("jokecomm_count");
				adapter.notifyDataSetChanged();
			}
		}
	}//*/
	public void refreshItem(CateItem cate, int count)
	{
		cate.comment_count = count;
		cate.comment_img_resid = R.drawable.joke_btn_commented;
		comm_sid = cate.sid;
		refreshCount = count;
		adapter.notifyDataSetChanged();
	}	
	private void joke_comment(CateItem cate)
	{
		Intent intent =new Intent();
		Bundle bundle = new Bundle();
		bundle.putParcelable("jokecate", cate);
		intent.putExtras(bundle);
		intent.setClass(getActivity(),JokeCommentActivity.class);		
		getActivity().startActivityForResult(intent, REQUEST_REFRESH);
	}
	
	private ProtocolTask task = new ProtocolTask(getActivity());
	
	private class CollectScoreTaskListener implements TaskListener {
		CateItem cateItem;
		public CollectScoreTaskListener(CateItem cateItem) {
			this.cateItem=cateItem;
		}
		@Override
		public void onNoNetworking() {
		}

		@Override
		public void onNetworkingError() {
			showShortToast(NewsTextImageFragment2.this.getActivity().getString(R.string.rastar_neterror));
		}

		@Override
		public void onPostExecute(byte[] bytes) {
			try {
				JSONObject result = new JSONObject(new String(bytes));
				int status = JsonParse.parseResultStatus(result);
				if(status == 1) 
				{
					if(!result.isNull("sid")) 
					{
						Log.d(TAG, "sid = " + result.getInt("sid"));
					}
					if(!result.isNull("allCount")) 
					{
						if(result.getInt("allCount") != 0) 
						{
							if(cateItem!=null&&cateItem.sid==result.getInt("sid")){
								cateItem.vote_count=result.getInt("allCount");
								cateItem.vote_img_resid = R.drawable.joke_btn_collected;
								Log.d(TAG, "allCount = " + result.getInt("allCount"));
								adapter.notifyDataSetChanged();
								
								if(isjoke){
									if(((MainActivity)getActivity()).queryDataIsExists(Integer.toString(result.getInt("sid"))))
									{
										((MainActivity)getActivity()).updateData(Integer.toString(result.getInt("sid")),0, 1);
									}
									else
									{
										((MainActivity)getActivity()).insertData(Integer.toString(result.getInt("sid")),0, 1);
									}
								}else{
									if(((JokeActivity)getActivity()).queryDataIsExists(Integer.toString(result.getInt("sid"))))
									{
										((JokeActivity)getActivity()).updateData(Integer.toString(result.getInt("sid")),0, 1);
									}
									else
									{
										((JokeActivity)getActivity()).insertData(Integer.toString(result.getInt("sid")),0, 1);
									}
								}
								
								
								showShortToast(MyApplication.getInstance().getResources().getString(R.string.vote_success));
							}
						} 
						else 
						{
							showShortToast(MyApplication.getInstance().getResources().getString(R.string.vote_fail));
						}
					}
				} 
				else 
				{
					showShortToast(MyApplication.getInstance().getResources().getString(R.string.rastar_netcoll));
				}
			} catch (JSONException e) {
				e.printStackTrace(); 
			}
		}

	} 
	private void joke_collect(CateItem cateItem)
	{		
		String url = Constants.JOKE_COMMENT_URL + "?qt=" + Constants.RATING + "&sid=" + cateItem.sid+"&uid="+MyApplication.getInstance().getUser().getUid()
				+ "&rating=" + 1;

		Log.d(TAG, "send sid = " + cateItem.sid);
		
		if(!NetworkUtil.isNetWorking(getActivity())){
			showShortToast(MyApplication.getInstance().getResources().getString(R.string.toast_no_network));
			return;
		}
		
		if (task != null) {
			task.onCancelled();
		}
		
		task = new ProtocolTask(getActivity());
		task.setListener(new CollectScoreTaskListener(cateItem));
		task.execute(url, null, tag(),getHeader());
		
		

		//showShortToast(getString(R.string.submit_suggest));
	}
	private void joke_share()
	{		
		//分享商店功能
		Intent intent =new Intent();
		intent.setClass(getActivity(),ShareActivity.class);		
		getActivity().startActivity(intent);
	}
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("底部新闻"); //统计页面
	    MobclickAgent.onResume(NewsTextImageFragment2.this.getActivity());//时长  
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("底部新闻"); 
		MobclickAgent.onPause(NewsTextImageFragment2.this.getActivity());//时长  
	}
	/**
	 * 更换天数
	 * bobo
	 */
	
	public void changTheday(){
		try {
			Log.d("xxx", "vvvvv");
			adapter.clear();
			request();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 置顶 
	 * add  by bobo
	 */
	public void goTOP(){
		getListView().setSelection(0);
	}
	
	/*add by zengxiao */
	OnRefreshListener refreshListen=new OnRefreshListener() {
		
		@Override
		public void onRefresh() {
			if(!NetworkUtil.isNetWorking(MyApplication.getInstance()))
			{
				listview.onRefreshComplete();
				Toast.makeText(MyApplication.getInstance(), R.string.rastar_netcoll, Toast.LENGTH_SHORT).show();
			}else{
				handler.removeCallbacks(refreshRunnable);
				handler.postDelayed(refreshRunnable, 2000);
				handler.sendEmptyMessage(1);
		}
		}
	};
	Runnable refreshRunnable= new Runnable() {				
		@Override
		public void run() {
			listview.setrefreshok();	
			listview.onRefreshComplete();
		}
	};
	
	/*add end*/	
	@Override
	public void setStyle(CusPullListView listview2) {
		// TODO Auto-generated method stub
		listview.setonRefreshListener(refreshListen);

	}
	@Override
	protected String getRefoushtUrl() {
		// TODO Auto-generated method stub
		return Constants.APK_URL+"Joke/v1.php?qt=Jokelist&cid=4"+"&pi="
			    + 1 + "&ps="
			    + getPageInfo().getPageSize();
	}
	private class MybigicLisenler implements OnClickListener{
		private CateItem mcaItem;
		public MybigicLisenler(CateItem caItem){
			this.mcaItem = caItem;
		}
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//Log.d("mylog", "-----datu----"+mres);
			StatService.trackCustomEvent(getActivity(), "newsItemb", "");
			MobclickAgent.onEvent(getActivity(), "newsItemb");
			UtilToNews.toNews(getActivity(), mcaItem);
		}
	}
	// add   by  bobo-------
	private class MylayoutLisenler implements OnClickListener{
		
		private CateItem mcaItem;
		private String mtype = null;
		private String murl = null;
		public MylayoutLisenler(CateItem caItem,String type,String url){
			this.mcaItem = caItem;
			this.mtype = type;
			this.murl = url;
		}

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//String type = mlist.get(mpositon);
			LogCart.Log("图片-----"+mtype);
			if(mtype.equals("gif")){
				Intent intent = new Intent(NewsTextImageFragment2.this.getActivity(),PlayGifActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("joke_image_path", murl);
				bundle.putInt("sid", mcaItem.sid);
				intent.putExtras(bundle);			
				NewsTextImageFragment2.this.getActivity().startActivity(intent);
			}else if(mtype.equals("mp4")){
				Intent pintent = new Intent(NewsTextImageFragment2.this.getActivity(), HtmlTVpalyActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("url", murl);
				bundle.putInt("sid", mcaItem.sid);
				pintent.putExtras(bundle);
				NewsTextImageFragment2.this.getActivity().startActivity(pintent);
			}else{
				onAppClick(mcaItem, Constants.TOJOKETETAILS);
			}
		}
		
		
	}

	@Override
	protected List<BigItem> dblistData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
