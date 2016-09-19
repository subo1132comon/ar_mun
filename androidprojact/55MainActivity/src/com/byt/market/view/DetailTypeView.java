package com.byt.market.view;


import java.util.ArrayList;
import java.util.List;

import com.byt.market.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DetailTypeView extends FrameLayout {

	private boolean invisviable = true;
	private List<TypeItem> types = new ArrayList<TypeItem>();
	private TypeAdapter mAdapter;
	private TextView	detail_type_noad;
	private TextView	l_detail_type_free;
	private TextView	detail_type_safe;
	private TextView	l_detail_type_aut;

	public DetailTypeView(Context context) {
		super(context);
		inflate(context, R.layout.detail_type, this);
		
		initView();
	}

	/**
	 * @param context
	 * @param appadvertisetaskmark
	 */
	public DetailTypeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.detail_type, this);
		initView();
	}

	public void flushAdvertiseBand(String stype) {
		types.clear();
		if (stype.contains("6")) {
			l_detail_type_aut.setVisibility(View.VISIBLE);
		}
		if (stype.contains("7")) {
			detail_type_safe.setVisibility(View.VISIBLE);
		} 
		if (stype.contains("8")) {
			detail_type_noad.setVisibility(View.VISIBLE);
		} 
		if (stype.contains("9")) {
			/*types.add(new TypeItem(9));*/
			invisviable = false;
		}
			
	}

	private void initView() {
		detail_type_noad=(TextView) findViewById(R.id.detail_type_noad);
		l_detail_type_free=(TextView) findViewById(R.id.detail_type_free);
		detail_type_safe=(TextView) findViewById(R.id.detail_type_safe);
		l_detail_type_aut=(TextView) findViewById(R.id.detail_type_auto);
	}
	
	@SuppressLint("ResourceAsColor")
	class TypeAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return types.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return types.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@SuppressLint("ResourceAsColor")
		@Override
		public View getView(int position, View convertview, ViewGroup arg2) {
			// TODO Auto-generated method stub
			TypeItem  item = types.get(position);
			ViewHolder holder;
			if(convertview == null){
				holder = new ViewHolder();
				convertview = inflate(getContext(), R.layout.layout_detail_type_item, null);
				holder.layout = (LinearLayout) convertview.findViewById(R.id.detail_type_item_layout);
				holder.tag = (TextView) convertview.findViewById(R.id.detail_type_item_txt);
				holder.tagimg= (ImageView) convertview.findViewById(R.id.detail_type_item_img);
				convertview.setTag(holder);
			}else{
				holder = (ViewHolder) convertview.getTag();
			}
			if(item!=null){
				switch(item.type){
				case 6:
					holder.tagimg.setBackgroundResource(R.drawable.l_detail_type_aut); 
					holder.tag.setText(R.string.txt_detail_tag_aut);
					holder.tag.setTextColor(getResources().getColor(R.color.detail_type_color_free));
					break;
				case 7:
					holder.tagimg.setBackgroundResource(R.drawable.l_detail_type_safe);				
					holder.tag.setText(R.string.txt_detail_tag_safe);
					holder.tag.setTextColor(getResources().getColor(R.color.detail_type_color_free));
					break;
				case 8:
					holder.tagimg.setBackgroundResource(R.drawable.l_detail_type_no_ads);		
					holder.tag.setText(R.string.txt_detail_tag_no_ads);
					holder.tag.setTextColor(getResources().getColor(R.color.detail_type_color_free));
					break;
				case 9:
					holder.tagimg.setBackgroundResource(R.drawable.l_detail_type_free);		
					holder.tag.setText(R.string.txt_detail_tag_free);
					holder.tag.setTextColor(getResources().getColor(R.color.detail_type_color_free));
					break;
				}
			}
			return convertview;
		}
	}
	
	static class ViewHolder{
		LinearLayout layout;
		TextView		tag;
		ImageView tagimg;
	}
	
	class TypeItem{
		
		TypeItem(int type){
			this.type = type;
		}
		
		int type;
	}
}
