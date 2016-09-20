package com.byt.market.qiushibaike.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.ShareActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.display.ZoomAndRounderBitmapDisplayer;
import com.byt.market.data.BigItem;
import com.byt.market.data.CateItem;
import com.byt.market.log.LogModel;
import com.byt.market.net.NetworkUtil;
import com.byt.market.ui.CommentFragment;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.NormalLoadPictrue;
import com.byt.market.util.SystemUtil;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;

public class JokeCommentFragment extends ListViewFragment{
	private static final String TAG = "JokeCommentFragment";
	String netType;
	private DisplayImageOptions mOptions;
	protected ImageAdapter adapter;

	int sid ;

	public void setSid(int sid)
	{
		this.sid = sid;
	}
	@Override
	protected String getRequestUrl() {
		
		String commurl = Constants.APK_URL+"Joke/v1.php?qt=Comm"+"&sid="
			    + sid+"&stype="+"notf";
		Log.d("nnlog", commurl+"---*******");
    	return commurl;
	}
	@Override
    public void canRequestGet() {
		request();
    }
	@Override
	protected String getRequestContent() {		
		return null;
	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
        try {       	
            return JsonParse.parseJokeList(getActivity(),result.getJSONArray("data"));
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
    		//mImageFetcher.setLoadingImage(R.drawable.category_loading);
    	}
    	else
    	{
    		//mImageFetcher.setLoadingImage(R.drawable.joke_empty_icon);
    	}
    	mOptions = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.joke_empty_icon)
		.showImageForEmptyUri(R.drawable.joke_empty_icon)
		.cacheOnDisc()
		.displayer(new ZoomAndRounderBitmapDisplayer((int) ((outMetrics.widthPixels - 15 * outMetrics.density) / 2))).build();
    }
    
	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) 
	{
        View view = inflater.inflate(res, container, false);
        return view;
	}
	
	@Override
	protected ImageAdapter createAdapter() 
	{
        return adapter = new JokeCommentAdapter();
	}    


    private class JokeCommentAdapter extends ImageAdapter{
		@Override
        public View newView(LayoutInflater inflater, BigItem item) {
            View view = null;
            BigHolder holder = new BigHolder();
            switch(item.layoutType){
                case BigItem.Type.LAYOUT_CAEGORYLIST:
                    view = inflater.inflate(R.layout.joke_comment_fragment, null);
                    holder.layoutType = item.layoutType;
                    JokeItemHolder itemHolder = new JokeItemHolder();  
                    itemHolder.joker = (TextView) view.findViewById(R.id.sender);  
                    itemHolder.content = (TextView) view.findViewById(R.id.content);    
                    itemHolder.headimag = (ImageView) view.findViewById(R.id.head_imageView);
                     holder.jokeCommentHolders.add(itemHolder);
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
                    ArrayList<JokeItemHolder> jokeCommentHolders = holder.jokeCommentHolders;  
                    for (int i = 0; i < item.cateItems.size() && i < jokeCommentHolders.size(); i++) 
                    {               
                        final CateItem cateItem = item.cateItems.get(i);
                        jokeCommentHolders.get(i).joker.setText(cateItem.sName);
                        jokeCommentHolders.get(i).content.setText(cateItem.content);
                        Log.d("nnlog","头像地址---"+cateItem.ImagePath);
                        new NormalLoadPictrue().getPicture(getActivity(),cateItem.ImagePath,jokeCommentHolders.get(i).headimag );
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
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setStyle(CusPullListView listview2) {
		// TODO Auto-generated method stub
		super.setStyle(listview2);
		loadfailed.setButtonVisible(View.GONE);
		loadfailed.setText(MyApplication.getInstance().getString(R.string.rastar_first));
	}
	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onAppClick(Object obj, String action) {
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
