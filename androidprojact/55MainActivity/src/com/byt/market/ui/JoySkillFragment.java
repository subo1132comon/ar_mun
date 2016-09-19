package com.byt.market.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.data.AppItem;
import com.byt.market.data.BigItem;
import com.byt.market.data.JoySkillItem;
import com.byt.market.util.JsonParse;
/**
 * 攻略列表
 * @author Administrator
 *
 */
public class JoySkillFragment extends ListViewFragment {


	Handler mHandler = new Handler();
    @Override
    protected String tag() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    protected String getRequestUrl() {
    	final AppItem appItem = this.getActivity().getIntent().getParcelableExtra("app");
    	if(appItem == null){
    		return "";
    	} else {
    		return Constants.LIST_URL + "?qt=" + Constants.GUIDE + "&pn="+appItem.pname;
    	}
    }

    @Override
    protected String getRequestContent() {
        return null;
    }
    @Override
    protected List<BigItem> parseListData(JSONObject result) {
        try {
        	if(JsonParse.parseResultStatus(result) == 1 && JsonParse.parseDataAllCount(result) == 0){
        		final AppItem appItem = this.getActivity().getIntent().getParcelableExtra("app");
        		Toast.makeText(this.getActivity(), this.getString(R.string.joy_skill_no_info, appItem.name), Toast.LENGTH_LONG).show();
        		mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(!JoySkillFragment.this.getActivity().isFinishing())
						((BaseActivity)JoySkillFragment.this.getActivity()).finishWindow();
						
					}
				}, 800);
        		return null;
        	}
            return JsonParse.parseJoySkillList(getActivity(),result.getJSONArray("data"));
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
    protected View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState, int res) {
        View view = inflater.inflate(res, container, false);
        return view;
    }

    @Override
    protected ImageAdapter createAdapter() {
        return new SubAdapter();
    }
    
    private class SubAdapter extends ImageAdapter{
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

        public SubAdapter() {
            df.setMaximumFractionDigits(1);
        }
        
        @Override
        public View newView(LayoutInflater inflater, BigItem item) {
            View view = null;
            BigHolder holder = new BigHolder();
            switch(item.layoutType){
	            case BigItem.Type.LAYOUT_SEARCH_TITLE:
					ResultTitleHolder resultTitleHolder = new ResultTitleHolder();
					view = inflater.inflate(R.layout.listitem_result_header, null);
					holder.layoutType = item.layoutType;
					resultTitleHolder.name = (TextView) view
							.findViewById(R.id.list_result_header_title);
					holder.reTitleHolder = resultTitleHolder;
					view.setTag(holder);
					break;
                case BigItem.Type.LAYOUT_JOY_SKILL:
                    view = inflater.inflate(R.layout.listitem_joyskill, null);
                    holder.layoutType = item.layoutType;
                    JoySkillItemHolder itemHolder = new JoySkillItemHolder();
                    itemHolder.icon = (ImageView) view.findViewById(R.id.listitem_joyskill_icon);
                    itemHolder.name = (TextView) view.findViewById(R.id.listitem_joyskill_name);
                    itemHolder.adEsc = (TextView) view.findViewById(R.id.listitem_joyskill_adesc);
                    itemHolder.upateData = (TextView) view.findViewById(R.id.listitem_joyskill_updatedate);
                    itemHolder.vCount = (TextView) view.findViewById(R.id.listitem_joyskill_vcount);
                    itemHolder.layout = view.findViewById(R.id.joyskill_sub);
                    holder.joySkilllistHolder = itemHolder;
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
	            case BigItem.Type.LAYOUT_SEARCH_TITLE:
					ResultTitleHolder resultTitleHolder = holder.reTitleHolder;
					for (final AppItem app : item.resultItems) {
						if (app.name != null)
							resultTitleHolder.name.setText(app.name);
					}
					break;
                case BigItem.Type.LAYOUT_JOY_SKILL:
                    for (final JoySkillItem app : item.joySkillItems) {
                        holder.joySkilllistHolder.name.setText(app.name);
                        holder.joySkilllistHolder.adEsc.setText(app.adEsc);
                        holder.joySkilllistHolder.upateData.setText(app.state);
                        holder.joySkilllistHolder.vCount.setText(JoySkillFragment.this.getString(R.string.joy_skill_vcount, app.vCount));
                        if(app.icon == null){
                            holder.joySkilllistHolder.icon.setImageResource(R.drawable.app_empty_icon);
                        } else {
                            holder.joySkilllistHolder.icon.setImageBitmap(app.icon);
                        }
                        holder.joySkilllistHolder.layout.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								onAppClick(app, Constants.TOLIST);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAppClick(Object obj, String action) {
		if(mOnJoySkillClickListener != null){
			mOnJoySkillClickListener.onJoySkillClicked((JoySkillItem) obj);
		}
		/*Intent intent = new Intent(action);
		if (obj instanceof SubjectItem) {
			SubjectItem app = (SubjectItem) obj;
			intent.putExtra("app", app);
		}
		intent.putExtra(Constants.LIST_FROM, LogModel.L_SUBJECT_HOME);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);*/
	}
	
	private OnJoySkillClickListener mOnJoySkillClickListener;
	
	public void setOnJoySkillClickListener(OnJoySkillClickListener listener){
		mOnJoySkillClickListener = listener;
	}
	
	public void removeOnJoySkillClickListener(){
		mOnJoySkillClickListener = null;
	}
	public interface OnJoySkillClickListener{
		
		public void onJoySkillClicked(JoySkillItem item);
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
	};
	
}
