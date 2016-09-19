package com.byt.market.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.CateMainFragmentActivity;
import com.byt.market.activity.DownLoadManageActivity;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.RankMainFragmentActivity;
import com.byt.market.activity.ShareActivity;
import com.byt.market.download.DownloadTaskManager;

import android.graphics.drawable.Drawable;

import com.byt.market.activity.UserActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.asynctask.ReadHttpGet;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.display.SimpleBitmapDisplayer;
import com.byt.market.bitmaputil.core.display.ZoomAndRounderBitmapDisplayer;
import com.byt.market.bitmaputil.core.display.ZoomBitmapDisplayer;
import com.byt.market.data.BigItem;
import com.byt.market.data.PageInfo;
import com.byt.market.data.SubjectItem;
import com.byt.market.data.UserData;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.VideoActivity;
import com.byt.market.mediaplayer.music.NewRingActivity;
import com.byt.market.qiushibaike.JokeActivity;
import com.byt.market.tools.LogCart;
import com.byt.market.util.JsonParse;
import com.byt.market.util.StringUtil;
import com.byt.market.util.TextUtil;
import com.byt.market.view.AppNoteDialog;
import com.byt.market.view.AppUninstallDialog;
import com.byt.market.view.CusPullListView;

/**
 * 专题列表
 * 
 * @author Administrator
 * 
 */
public class SubFragment extends ListViewFragment implements OnClickListener{
	
	public static final String SUB_IMG_TYPE = "sub_img";
	AppNoteDialog appnote;
	private DisplayImageOptions mOptions;
	private View view;
	@Override
	protected String tag() {
		return this.getClass().getSimpleName();
	}

	@Override
	protected String getRequestUrl() {
		PageInfo pageInfo = getPageInfo();
		pageInfo.setPageSize(10);
		return Constants.LIST_URL + "?qt=" + Constants.SUBJECT + "&pi="
				+ pageInfo.getNextPageIndex() + "&ps="
				+ pageInfo.getPageSize();
	}

	@Override
	protected String getRequestContent() {
		return null;
	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		try {
			return JsonParse.parseSubjectList(result.getJSONArray("data"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.listview;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		mImageFetcher.setLoadingImage(R.drawable.l_sub_default_bg);
		mOptions = new DisplayImageOptions.Builder()
		.cacheOnDisc().build();
	}
	
	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);
		if(this.getActivity() != null && this.getActivity() instanceof MainActivity){
			final MainActivity activity = (MainActivity)this.getActivity();
			if(activity.mAdapter != null  && activity.mAdapter.list != null&& activity.mAdapter.list.size() > MainActivity.MAIN_SUB ){
				activity.mAdapter.list.set(MainActivity.MAIN_SUB, this);
			}
		}
		return view;
	}

	@Override
	protected ImageAdapter createAdapter() {
		return new SubAdapter();
	}

	private class SubAdapter extends ImageAdapter {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		boolean isshow=false;
		public void setIsshow(boolean isshow) {
			this.isshow = isshow;
		}
		public SubAdapter() {
			df.setMaximumFractionDigits(1);
		}

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_SUBJECTLIST:
				view = inflater.inflate(R.layout.listitem_sub, null);
				holder.layoutType = item.layoutType;
				SubjectItemHolder itemHolder = new SubjectItemHolder();
				itemHolder.icon = (ImageView) view
						.findViewById(R.id.listitem_sub_icon);
				itemHolder.name = (TextView) view
						.findViewById(R.id.listitem_sub_name);
				itemHolder.bgview=(ImageView) view
						.findViewById(R.id.subbg);
//				itemHolder.comm = (TextView) view
//						.findViewById(R.id.listitem_sub_comm);				
				/**				for (SubjectItem app : item.subjectItems) {
				if(app.ulevel>0)
				{
					for(int i=0;i<app.ulevel;i++)
					{
					ImageView ImageView=new ImageView(SubFragment.this.getActivity());
					
					ImageView.setBackgroundResource(R.drawable.level);
					int sizeicon=MyApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.point_start_size);
					ImageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, sizeicon));
					itemHolder.point_image.addView(ImageView);
					}
				}
				}*/
//				itemHolder.num = (TextView) view
//						.findViewById(R.id.listitem_sub_num);
			
				
				holder.subjectHolder = itemHolder;
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
			case BigItem.Type.LAYOUT_SUBJECTLIST:
				for (final SubjectItem app : item.subjectItems) {
					
					holder.subjectHolder.name.setText(app.name);
					//holder.subjectHolder.updatetime.setText(app.updatetTime);
					//holder.subjectHolder.point.setText("VIP"+app.ulevel);
				/*	if(!TextUtils.isEmpty(app.adesc)&&app.adesc.length()>55) {
						holder.subjectHolder.subDesc.setText(TextUtil.toDBC(app.adesc.substring(0, 55)+ "..."));
					} else {
						holder.subjectHolder.subDesc.setText(TextUtil.toDBC(app.adesc));
					}*/
//					holder.subjectHolder.comm.setText(String
//							.valueOf(app.visitCount) + "浏览");
//					holder.subjectHolder.num.setText(app.count + "款");
					//edit by wangxin.load image by Bitmap
					/*bestenone add by zengxiao
					 * for：增加积分显示
					 * */
					
						if(TextUtils.isEmpty(app.iconUrl)){
						//holder.subjectHolder.icon
					//	.setImageResource(R.drawable.l_sub_empty_bg);
					} else if(!imageLoader.getPause().get()){					
						imageLoader.displayImage(app.iconUrl, holder.subjectHolder.icon, mOptions);
					}
						holder.subjectHolder.bgview
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
								/*	UserData user = MyApplication.getInstance()
											.getUser();
								if(user.getUlevel()>=app.ulevel||app.ulevel==0)
								{*/
									LogCart.Log("APPPPP"+app);
									onAppClick(app, Constants.TOLIST);
								/*}
								else{
									showisshare();
											
								}*/
								}
							});
					if(!imageLoader.getPause().get()){
						holder.subjectHolder.bgview.setBackgroundResource(R.drawable.beston_subbutton_onclick);
					}
				}
				break;
			case BigItem.Type.LAYOUT_LOADING:

				break;
			case BigItem.Type.LAYOUT_LOADFAILED:

				break;
			}
		}

	}

	@Override
	protected void onDownloadStateChanged() {

	}

	@Override
	public void onAppClick(Object obj, String action) {
		Intent intent = new Intent(action);
		if (obj instanceof SubjectItem) {
			SubjectItem app = (SubjectItem) obj;
			intent.putExtra("app", app);
		}
		intent.putExtra(Constants.LIST_FROM, LogModel.L_SUBJECT_HOME);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}
	@Override
	public void onPostExecute(byte[] bytes) {
		// TODO Auto-generated method stub
		super.onPostExecute(bytes);
		try {
			musicResUpdate();
			videoResUpdate();
			txtResUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub
	}
	//add by zengxiao 弹出是否分享对话框
	public void showisshare(){
		 appnote = new AppNoteDialog(this.getActivity());
		appnote.setBtnOkListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent =new Intent();
				intent.setClass(SubFragment.this.getActivity(),ShareActivity.class);
				SubFragment.this.getActivity().startActivity(intent);	
				appnote.dismiss();
			}
		});
		appnote.show();		
	}
	//add by SY 设置顶部空隙
	@Override
	public void setStyle(CusPullListView listview2) {
		// TODO Auto-generated method stub
		super.setStyle(listview2);
		view=LayoutInflater.from(this.getActivity()).inflate(R.layout.yulehead,null);
		view.findViewById(R.id.musictitle).setOnClickListener(this);
		view.findViewById(R.id.videotitle).setOnClickListener(this);	
		view.findViewById(R.id.txttitle).setOnClickListener(this);
		musicResUpdate();
		videoResUpdate();
		txtResUpdate();
		listview2.addHeaderView(view);
		
	}
/*	@Override
	public void initViewBYT(View view) {
		bestone add by "zengxiao" for:添加标题头
		view.findViewById(R.id.titlebar_title).setVisibility(View.GONE);
		view.findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
		view.findViewById(R.id.iv_settings).setVisibility(View.GONE);
		view.findViewById(R.id.search_page_view).setVisibility(View.VISIBLE);
		view.findViewById(R.id.search_page_view).setOnClickListener(this);
		view.findViewById(R.id.top_downbutton).setVisibility(View.VISIBLE);
		view.findViewById(R.id.top_downbutton).setOnClickListener(this);//添加下载界面按钮
		view.findViewById(R.id.titlebar_applist_button_container).setVisibility(View.GONE);
		bestone add end
	}*/
	/*@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.search_page_view:
			this.getActivity().startActivity(new Intent(Constants.TOSEARCH));
			this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		case R.id.top_downbutton:
			Intent downloadIntent = new Intent(this.getActivity(),
					DownLoadManageActivity.class);
			downloadIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
					DownLoadManageActivity.TYPE_FROM_DOWNLOAD);
			this.getActivity().startActivity(downloadIntent);
			this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
			
		}
		
	}*/
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch(v.getId()){
		case R.id.musictitle:
//			SharedPreferences audNetResVer = MyApplication.getInstance().getSharedPreferences("yulever",0);		
//		    int net_ver1 = audNetResVer .getInt("aud_net_ver_update", 0);
//		    
//			SharedPreferences audResVer = MyApplication.getInstance().getSharedPreferences("audio_resource_updated",0);
//			SharedPreferences.Editor editor1 = audResVer.edit(); 
//			editor1.putInt("audresver", net_ver1);
//			editor1.commit();
//			musicResUpdate();

			startActivity(new Intent(MyApplication.getInstance(),
					CateMainFragmentActivity.class));
			SubFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
		    
			break;
		case R.id.videotitle:
//			SharedPreferences vdoNetResVer = MyApplication.getInstance().getSharedPreferences("yulever",0);		
//		    int net_ver2 = vdoNetResVer .getInt("vdo_net_ver_update", 0);
//		    
//			SharedPreferences vdoResVer = MyApplication.getInstance().getSharedPreferences("vdo_resource_updated",0);
//			SharedPreferences.Editor editor2 = vdoResVer.edit(); 
//			editor2.putInt("vdoresver", net_ver2);
//			editor2.commit();	
//			videoResUpdate();
//	
			startActivity(new Intent(MyApplication.getInstance(),
					RankMainFragmentActivity.class));
			SubFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			
			
//		    		
			break;
		case R.id.txttitle:
//			  SharedPreferences txtNetResVer = MyApplication.getInstance().getSharedPreferences("yulever",0);		
//			    int net_ver3 = txtNetResVer .getInt("txt_net_ver_update", 0);
//				
//				SharedPreferences txtResVer = MyApplication.getInstance().getSharedPreferences("txt_resource_updated",0);
//				SharedPreferences.Editor editor3 = txtResVer.edit(); 
//				editor3.putInt("txtresver", net_ver3);
//				editor3.commit();	
//				txtResUpdate();
//	
//			startActivity(new Intent(MyApplication.getInstance(),
//					JokeActivity.class));
//			SubFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in,
//					R.anim.push_left_out);
//		  		
			break;				
		}
	}
	
    
	
	
	public boolean musicResUpdate()
	{
		if(view==null)
			return false;
	    SharedPreferences audNetResVer = MyApplication.getInstance().getSharedPreferences("yulever",0);		
	    int net_ver = audNetResVer .getInt("aud_net_ver_update", 0);
	    
	    SharedPreferences audResVer = MyApplication.getInstance().getSharedPreferences("audio_resource_updated",0);		
	    int ver = audResVer .getInt("audresver", 0);
	    if(ver != net_ver)
	    {
	    	view.findViewById(R.id.aud_redpoint).setVisibility(View.GONE);
	    	return false;
	    }
	    view.findViewById(R.id.aud_redpoint).setVisibility(View.GONE);
	    return false;
		
	}
	public boolean videoResUpdate()
	{
		if(view==null)
			return false;
	    SharedPreferences vdoNetResVer = MyApplication.getInstance().getSharedPreferences("yulever",0);		
	    int net_ver = vdoNetResVer .getInt("vdo_net_ver_update", 0);
	    
	    SharedPreferences vdoResVer = MyApplication.getInstance().getSharedPreferences("vdo_resource_updated",0);		
	    int ver = vdoResVer .getInt("vdoresver", 0);
	    if(ver != net_ver)
	    {
	    	view.findViewById(R.id.vdo_redpoint).setVisibility(View.VISIBLE);
	    	return true;
	    }	
	    view.findViewById(R.id.vdo_redpoint).setVisibility(View.GONE);
	    this.getActivity().sendBroadcast(new Intent("UPDATEREDPOINT"));		
	    return true;
	}
	public boolean txtResUpdate()
	{
		if(view==null)
			return false;
	    SharedPreferences txtNetResVer = MyApplication.getInstance().getSharedPreferences("yulever",0);		
	    int net_ver = txtNetResVer .getInt("txt_net_ver_update", 0);
	    
	    SharedPreferences txtResVer = MyApplication.getInstance().getSharedPreferences("txt_resource_updated",0);		
	    int ver = txtResVer .getInt("txtresver", 0);
	    if(ver != net_ver)
	    {
	    	view.findViewById(R.id.txt_redpoint).setVisibility(View.VISIBLE);
	    	return true;
	    }	
	    view.findViewById(R.id.txt_redpoint).setVisibility(View.GONE);
	    this.getActivity().sendBroadcast(new Intent("UPDATEREDPOINT"));
		return true;
	}
	public boolean yuleRepointNeedShow()
	{
		boolean i1 = musicResUpdate();
		boolean i2 = videoResUpdate();
		boolean i3 = txtResUpdate();
	    if(musicResUpdate()||videoResUpdate()||txtResUpdate())
		{
			return true;
		}
		return false;
	}
	/*add for 修改提示语*/
	@Override
	public void setButtonInvi() {
		// TODO Auto-generated method stub
		loadfailed.setText(MyApplication.getInstance().getString(R.string.nodata));
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollState = scrollState;
		
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			// add by wangxin
			// mImageFetcher.setPauseWork(false);
			if(imageLoader!=null){
				imageLoader.resume();
			}
			isScrolling=false;
			mAdapter.notifyDataSetChanged();
			mAdapter.loadIcon(view);
			mAdapter.clearListIcon(view);
//			if (mNeedNotifyChanged) {
//				onDownloadStateChanged();
//				mNeedNotifyChanged = false;
//			}
			Log.d("rmyzx","refresh----------------------------");
			addNewDataOnce();
//			if(imageLoader!=null){
//				imageLoader.clearMemoryCache();
//			}

			break;
		case OnScrollListener.SCROLL_STATE_FLING:
//			options=options2;
			isScrolling=true;
			if(imageLoader!=null){
				imageLoader.pause();
			}
			time=System.currentTimeMillis();
			
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
//			options=options1;
			isScrolling=false;
			
			break;
		default:
			// add by wangxin
			// mImageFetcher.setPauseWork(true);
			mAdapter.cancelLoadIcon();
			break;
		}
	}

	@Override
	protected String getRefoushtUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<BigItem> dblistData() {
		// TODO Auto-generated method stub
		return null;
	}

}
