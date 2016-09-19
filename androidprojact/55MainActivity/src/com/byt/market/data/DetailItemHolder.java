package com.byt.market.data;

import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byt.market.view.DetailTypeView;

public class DetailItemHolder extends ViewHolder{

	public TextView name;
	public TextView type;
	public TextView downnum;
	public RatingBar rating;

	public TextView size;
	public TextView catename;
	public TextView vname;
	public TextView updatetime;
	public TextView lang;
	public TextView feetype;
	public View v_ades;
	public View v_sdes;

	public TextView ades;
	public TextView des;
	public ImageButton exButton;

	public DetailTypeView tv_type;
	// public DetailTipView listitem_d_tip;
	public GridView sBand;
	// public CustomDetailCommBand cdc_band;
	/*add by zengxiao*/
	public Button btn_down;
	public LinearLayout btn_copareprice;//价格对比
	public TextView textviewprice;//应用价格
	public ImageView freeline;
	/*add end*/
	// public TextView more_comm;
	public TextView tag;
	public LinearLayout tagLayout;

}
