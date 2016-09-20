package com.byt.market.ui;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.adapter.ImageAdapter.CategoryItemHolder;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.display.ZoomAndRounderBitmapDisplayer;
import com.byt.market.data.BigItem;
import com.byt.market.data.CateItem;
import com.byt.market.log.LogModel;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;

/**
 * 分类列表
 * @author qiuximing
 *
 */
public class GameCateFragment extends ListViewFragment {
	
	String netType;
	
//	private DisplayImageOptions mOptions;

    @Override
    protected String getRequestUrl() {
//    	return Constants.LIST_URL + "?qt=" + Constants.CATE+"&type=wifi";
//    	if(!TextUtils.isEmpty(netType) && "wifi".equals(netType)){
//    	}else{
    		return Constants.LIST_URL + "?qt=" + Constants.CATE+"&ctype=game"+ "&pi="
    				+ getPageInfo().getNextPageIndex() + "&ps="
    				+ 40;
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
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	request();
    }

    @Override
    protected List<BigItem> parseListData(JSONObject result) {
        try {
            return JsonParse.parseCateList(result.getJSONArray("data"));
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
    	netType = Util.getNetAvialbleType(MyApplication.getInstance());
    	if(!TextUtils.isEmpty(netType)&&"wifi".equals(netType)){
//    		mImageFetcher.setLoadingImage(R.drawable.category_loading);
    	}else{
//    		mImageFetcher.setLoadingImage(R.drawable.app_empty_icon);
    	}
//    	mOptions = new DisplayImageOptions.Builder()
//		.showStubImage(R.drawable.app_empty_icon)
//		.showImageForEmptyUri(R.drawable.app_empty_icon)
//		.cacheInMemory().cacheOnDisc()
//		.displayer(new ZoomAndRounderBitmapDisplayer((int) ((outMetrics.widthPixels - 15 * outMetrics.density) / 2))).build();
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
                case BigItem.Type.LAYOUT_CAEGORYLIST:
                    view = inflater.inflate(R.layout.listitem_cate, null);
                    holder.layoutType = item.layoutType;
                    CategoryItemHolder itemHolder1 = new CategoryItemHolder();
                    itemHolder1.content=(TextView) view.findViewById(R.id.cate_content);
                    itemHolder1.name = (TextView) view.findViewById(R.id.listitem_cate_name_left);
                    itemHolder1.layout = view.findViewById(R.id.listitem_cate_left);
                    CategoryItemHolder itemHolder2 = new CategoryItemHolder();
                    itemHolder2.content=(TextView) view.findViewById(R.id.cate_content_right);
                    itemHolder2.name = (TextView) view.findViewById(R.id.listitem_cate_name_right);
                    itemHolder2.layout = view.findViewById(R.id.listitem_cate_right);
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
                    if(item.cateItems.size()==1&&holder.cateHolders.size()==2){
                    	holder.cateHolders.get(1).layout.setVisibility(View.INVISIBLE);
                    }else if(holder.cateHolders.size()>1){
                    	holder.cateHolders.get(1).layout.setVisibility(View.VISIBLE);
					}
                    for (int i = 0; i < item.cateItems.size() && i < cateHolders.size(); i++) {
                        final CateItem cateItem = item.cateItems.get(i);
                        cateHolders.get(i).name.setText(cateItem.cTitle);
                        //edit by wangxin.
                        if(cateItem.cDesc!=null)
                        {
                     	   /*bestone add by zengxiao for :添加分类详情介绍*/
                            cateHolders.get(i).content.setText(cateItem.cDesc);
                            /*add end*/
                        }
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
                    
                    CategoryItemHolder itemHolder2 = new CategoryItemHolder();
                    itemHolder2.icon = (ImageView) view.findViewById(R.id.cate_icon1);
                    itemHolder2.name = (TextView) view.findViewById(R.id.cate_title1);
                    itemHolder2.layout = view.findViewById(R.id.cate_item_layout1);
                    itemHolder2.line = view.findViewById(R.id.listitem_rank_line2);
                    
                    
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
                case BigItem.Type.LAYOUT_CAEGORYLIST:/*
                    ArrayList<CategoryItemHolder> cateHolders = holder.cateHolders;
                    if(item.cateItems.size()<2)
                    {
                    	cateHolders.get(1).layout.setVisibility(View.GONE);	
                    	cateHolders.get(1).line.setVisibility(View.GONE);
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
                        cateHolders.get(i).layout.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								onAppClick(cateItem, Constants.TOLIST);
							}
						});
                    }*/
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
		intent.putExtra(Constants.LIST_FROM, LogModel.L_CATE_HOME);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub
		
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
