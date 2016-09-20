package com.byt.market.view;

import com.byt.ar.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyMoneyDailog extends Dialog implements android.view.View.OnClickListener{

	Context mcontext;
	private RelativeLayout mday;
	private RelativeLayout month;
	private RelativeLayout myear;
	private TextView mtext_day,mtext_month,mtext_yeat;
	private TextView bao_day,bao_month,bao_year;
	private PayBack mcallback;
	//支付所需 参数
	private String mpriceday,mpricemonth,mpriceyear;
	private String mpropsName;
	
	public MyMoneyDailog(Context context) {
		super(context);
		this.mcontext = context;
	}
	public MyMoneyDailog(Context context,int theme){
		super(context, theme);
		this.mcontext = context;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dailog_money_pay);
		mday = (RelativeLayout) findViewById(R.id.layout_pay_day);
		month = (RelativeLayout) findViewById(R.id.layout_pay_month);
		myear = (RelativeLayout) findViewById(R.id.layout_pay_yeas);
		bao_day = (TextView) findViewById(R.id.bao_day);
		bao_month = (TextView) findViewById(R.id.bao_math);
		bao_year = (TextView) findViewById(R.id.bao_yeas);
		mtext_day = (TextView) findViewById(R.id.bao_day_price);
		mtext_month = (TextView) findViewById(R.id.bao_math_price);
		mtext_yeat = (TextView) findViewById(R.id.bao_year_price);
		
//		mtext_props = (TextView) findViewById(R.id.text_props);
//		mvalues = (TextView) findViewById(R.id.text_money);
//		
//		mtext_props.setText(mpropsName);
//		int money = Integer.parseInt(mprice)/100;
//		mvalues.setText(String.valueOf(money)+" "+"THB");
		mtext_day.setText(mpriceday+"$");
		mtext_month.setText(mpricemonth+"$");
		mtext_yeat.setText(mpriceyear+"$");
		
		mday.setOnClickListener(this);
		month.setOnClickListener(this);
		myear.setOnClickListener(this);
	}
	public void initData(PayBack callback,String day,String month,String year){
		this.mcallback = callback;
		this.mpriceday = day;
		this.mpricemonth = month;
		this.mpriceyear = year;
	}
	public interface PayBack{
		public void Resout(int arg0,String propname);
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.layout_pay_day:
			mcallback.Resout(Integer.parseInt(mpriceday),bao_day.getText().toString());
			MyMoneyDailog.this.cancel();
			break;
		case R.id.layout_pay_month:
			mcallback.Resout(Integer.parseInt(mpricemonth),bao_month.getText().toString());
			MyMoneyDailog.this.cancel();
			break;
		case R.id.layout_pay_yeas:
			mcallback.Resout(Integer.parseInt(mpriceyear),bao_year.getText().toString());
			MyMoneyDailog.this.cancel();
			break;
		}
	}
}
