package com.byt.market.mediaplayer.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.adapter.ImageAdapter.BigHolder;
import com.byt.market.adapter.ImageAdapter.CategoryItemHolder;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.display.ZoomAndRounderBitmapDisplayer;
import com.byt.market.data.BigItem;
import com.byt.market.data.CateItem;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.PlayActivity;
import com.byt.market.mediaplayer.data.VideoItem;
import com.byt.market.ui.CateListViewFragment;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;

/**
 * 分类列表
 * @author qiuximing
 *
 */
public class VallayFragment extends ListViewFragment {
	
	String netType;
	private TextView musiclandtitle;
	private DisplayImageOptions mOptions;

    @Override
    protected String getRequestUrl() {
//    	return Constants.LIST_URL + "?qt=" + Constants.CATE+"&type=wifi";
//    	if(!TextUtils.isEmpty(netType) && "wifi".equals(netType)){
//    	}else{
    	return Constants.APK_URL+"Video/v1.php?qt=videolist&cid=3"+"&pi="
		+ getPageInfo().getNextPageIndex() + "&ps="
		+ getPageInfo().getPageSize();
//    	}
    }

    @Override
    protected String getRequestContent() {
        return null;
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    }

    @Override
    protected List<BigItem> parseListData(JSONObject result) {
   /*     try {       	
           // return JsonParse.parseCateVideoList(result.getJSONArray("data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        return null;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.listview;
    }
    @Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	netType = Util.getNetAvialbleType(MyApplication.getInstance());
    	if(!TextUtils.isEmpty(netType)&&"wifi".equals(netType)){
//    		mImageFetcher.setLoadingImage(R.drawable.category_loading);
    	}else{
//    		mImageFetcher.setLoadingImage(R.drawable.app_empty_icon);
    	}
    	mOptions = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.videodefaultbg)
		.showImageForEmptyUri(R.drawable.videodefaultbg)
		.cacheOnDisc()
		.displayer(new ZoomAndRounderBitmapDisplayer((int) ((outMetrics.widthPixels - 15 * outMetrics.density) / 2))).build();
    }

    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState, int res) {
        View view = inflater.inflate(res, container, false);
//        if(this.getActivity() != null && this.getActivity() instanceof MainActivity){
//			final MainActivity activity = (MainActivity)this.getActivity();
//			if(activity.mAdapter != null  && activity.mAdapter.list != null&& activity.mAdapter.list.size() > MainActivity.MAIN_CATE ){
//				activity.mAdapter.list.set(MainActivity.MAIN_CATE, this);
//			}
//		}
        return view;
    }

    // edit by wangxin
    @Override
    protected ImageAdapter createAdapter() {
    	String netType = Util.getNetAvialbleType(MyApplication.getInstance());
    	if(!TextUtils.isEmpty(netType)){
//    		if("wifi".equals(netType)){
//    			return new WifiCateAdapter();
//    		}
    	}
        return new WifiCateAdapter();
    }
    
    
    private class WifiCateAdapter extends ImageAdapter{


		@Override
        public View newView(LayoutInflater inflater, BigItem item) {
            View view = null;
            BigHolder holder = new BigHolder();
            switch(item.layoutType){
                case BigItem.Type.LAYOUT_VIDEOCAEGORYLIST:
                    view = inflater.inflate(R.layout.listitem_videolandcate, null);
                    holder.layoutType = item.layoutType;
                    CategoryItemHolder itemHolder1 = new CategoryItemHolder();
                    itemHolder1.content=(TextView) view.findViewById(R.id.listitem_cate_name_left);
                    itemHolder1.listitemvideosdc=(TextView) view.findViewById(R.id.listitemvideosdc);
                    itemHolder1.icon = (ImageView) view.findViewById(R.id.musicrankpic);    
                    itemHolder1.layout = view.findViewById(R.id.musicranklandlayout);
                    CategoryItemHolder itemHolder2 = new CategoryItemHolder();
                    itemHolder2.icon = (ImageView) view.findViewById(R.id.musicrankpic2);
                    itemHolder2.layout = view.findViewById(R.id.musicranklandlayout2);
                    itemHolder2.content=(TextView) view.findViewById(R.id.listitem_cate_name_left2);    
                    itemHolder2.listitemvideosdc=(TextView) view.findViewById(R.id.listitemvideosdc2);
                    holder.cateHolders.add(itemHolder1);
                    holder.cateHolders.add(itemHolder2);
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
            switch (item.layoutType){
                case BigItem.Type.LAYOUT_VIDEOCAEGORYLIST:
                    ArrayList<CategoryItemHolder> cateHolders = holder.cateHolders;            
                    if(item.videoItems.size()<2)
                    {
                    	cateHolders.get(1).icon.setVisibility(View.INVISIBLE);
                    	cateHolders.get(1).layout.setVisibility(View.INVISIBLE);
                    	cateHolders.get(1).content.setVisibility(View.INVISIBLE);   
                    	cateHolders.get(1).listitemvideosdc.setVisibility(View.INVISIBLE);
                    }else{
                    	cateHolders.get(1).icon.setVisibility(View.VISIBLE);
                    	cateHolders.get(1).layout.setVisibility(View.VISIBLE);
                    	cateHolders.get(1).content.setVisibility(View.VISIBLE);  
                    	cateHolders.get(1).listitemvideosdc.setVisibility(View.VISIBLE);
                    }
                    for (int i = 0; i < item.videoItems.size() && i < cateHolders.size(); i++) {
               
                        final VideoItem cateItem = item.videoItems.get(i);

                       if(cateItem.cDesc!=null)
                       {                   	 
                    	   cateHolders.get(i).listitemvideosdc.setText(cateItem.cDesc);
                           cateHolders.get(i).content.setText(cateItem.cTitle);
                       }
                        if(cateItem.ImagePath == null||isScrolling){
                            cateHolders.get(i).icon.setImageResource(R.drawable.videodefaultbg);
                        } else {
                        	if (!cateItem.ImagePath.startsWith("http://")) {
                            	imageLoader.displayImage(cateItem.ImagePath, cateHolders.get(i).icon, mOptions);
                            	}else{
                            	imageLoader.displayImage(cateItem.ImagePath, cateHolders.get(i).icon, mOptions);
                            	}
                        }
                        cateHolders.get(i).layout.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent =new Intent();
								intent.setClass(VallayFragment.this.getActivity(),PlayActivity.class);						
								intent.putExtra("sendstring", cateItem); 
								VallayFragment.this.getActivity().startActivity(intent);
							}
						});
                    }
                    /*}*/
                    break;
                case BigItem.Type.LAYOUT_LOADING:
                    
                    break;
                case BigItem.Type.LAYOUT_LOADFAILED:
                    
                    break;
            }
        }
        
    }
    
    /**
     * 2g 网络 使用此Adapter,主要是List样式 
     * @author z
     *
     */
    private class CateAdapter extends ImageAdapter{


		@Override
        public View newView(LayoutInflater inflater, BigItem item) {
            View view = null;
            BigHolder holder = new BigHolder();
            switch(item.layoutType){
                case BigItem.Type.LAYOUT_CAEGORYLIST:
                    view = inflater.inflate(R.layout.listitem_cate_list, null);
                    holder.layoutType = item.layoutType;
                    CategoryItemHolder itemHolder1 = new CategoryItemHolder();
                    itemHolder1.icon = (ImageView) view.findViewById(R.id.cate_icon);
                    itemHolder1.name = (TextView) view.findViewById(R.id.cate_title);
                    itemHolder1.layout = view.findViewById(R.id.cate_item_layout);
                    itemHolder1.content=(TextView) view.findViewById(R.id.cate_content);
                    CategoryItemHolder itemHolder2 = new CategoryItemHolder();
                    itemHolder2.icon = (ImageView) view.findViewById(R.id.cate_icon1);
                    itemHolder2.name = (TextView) view.findViewById(R.id.cate_title1);
                    itemHolder2.layout = view.findViewById(R.id.cate_item_layout1);
                    itemHolder2.line = view.findViewById(R.id.listitem_rank_line2);
                    itemHolder2.content=(TextView) view.findViewById(R.id.cate_content1);                                     
                    holder.cateHolders.add(itemHolder1);
                    holder.cateHolders.add(itemHolder2);
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
            switch (item.layoutType){
                case BigItem.Type.LAYOUT_CAEGORYLIST:
                    ArrayList<CategoryItemHolder> cateHolders = holder.cateHolders;
                    if(item.cateItems.size()<2)
                    {
                    	cateHolders.get(1).layout.setVisibility(View.INVISIBLE);	
                    	cateHolders.get(1).line.setVisibility(View.INVISIBLE);
                    }
                    for (int i = 0; i < item.cateItems.size() && i < cateHolders.size(); i++) {
                        final CateItem cateItem = item.cateItems.get(i);
                        cateHolders.get(i).name.setText(cateItem.cTitle);
                      if(TextUtils.isEmpty(cateItem.ImagePath)){
                        	cateHolders.get(i).icon.setImageResource(R.drawable.app_empty_icon);
                       }else{
                        	if (!cateItem.ImagePath.startsWith("http://")) {
//                        	mImageFetcher.loadImage(cateItem.ImagePath, cateHolders.get(i).icon);
                        	imageLoader.displayImage(cateItem.ImagePath, cateHolders.get(i).icon, mOptions);
                        	}else{
//                        	mImageFetcher.loadImage(cateItem.ImagePath, cateHolders.get(i).icon);
                        	imageLoader.displayImage(cateItem.ImagePath, cateHolders.get(i).icon, mOptions);
                        	}
                        }
                      /*bestone add by zengxiao for :添加分类详情介绍*/
                      cateHolders.get(i).content.setText(cateItem.cDesc);
                      /*add end*/
                        cateHolders.get(i).layout.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								onAppClick(cateItem, Constants.TOLIST);
							}
						});
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
		if (obj instanceof CateItem) {
			CateItem caItem = (CateItem) obj;
			intent.putExtra("app", caItem);
		}
		intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);		
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setStyle(CusPullListView listview2){
		// TODO Auto-generated method stub
		super.setStyle(listview2);
		View view=LayoutInflater.from(getActivity()).inflate(R.layout.videoland_header, null);
		listview2.addHeaderView(view);
	}
	//修改提示语
	@Override
	public void setButtonInvi() {
		super.setButtonInvi();
	loadfailed.setText(getString(R.string.nodata));	
	loadfailed.setButtonVisible(View.GONE);
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
