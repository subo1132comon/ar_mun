package com.byt.market.view;

import java.util.Random;

import com.byt.ar.R;
import com.byt.market.util.BluePayUtil;
import com.byt.market.util.DateUtil;
import com.payssion.android.sdk.PayssionActivity;
import com.payssion.android.sdk.model.PayRequest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyPayDailog extends Dialog implements android.view.View.OnClickListener{

	Context mcontext;
	private RelativeLayout mbank;
	private RelativeLayout mLinpay;
	private RelativeLayout mSMS;
	private RelativeLayout mcahu;
	private RelativeLayout mbitcoin;
	private TextView mtext_props,mvalues;
	
	//支付所需 参数
	private Activity mactivity;
	private String mtransID;
	private String mcustomerId;
	private String mprice;
	private String mpropsName;
	private int msmsId = 0;
	private String mscheme;
	//private PayBack mcallback;
	private int mfeeid = 0;
	private String mcurrency;
	public MyPayDailog(Context context) {
		super(context);
		this.mcontext = context;
	}
	public MyPayDailog(Context context,int theme){
		super(context, theme);
		this.mcontext = context;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dailog);
		initView();
		//registRecive();
	}
	
	private void initView(){
		mbank = (RelativeLayout) findViewById(R.id.layout_bank);
		mLinpay = (RelativeLayout) findViewById(R.id.layout_LINEPay);
		mSMS = (RelativeLayout) findViewById(R.id.layout_sms);
		mtext_props = (TextView) findViewById(R.id.text_props);
		mvalues = (TextView) findViewById(R.id.text_money);
		mcahu = (RelativeLayout) findViewById(R.id.layout_cashu);
		mbitcoin = (RelativeLayout) findViewById(R.id.layout_bitcoin);
		
		mtext_props.setText(mpropsName);
		mvalues.setText(String.valueOf(mprice)+" "+"USD");
		
		mbank.setOnClickListener(this);
		mLinpay.setOnClickListener(this);
		mSMS.setOnClickListener(this);
		mcahu.setOnClickListener(this);
		mbitcoin.setOnClickListener(this);
		
		if(Integer.parseInt(mprice)>250){
			mSMS.setVisibility(View.GONE);
		}
	}
	
	
	public void initArData(Activity activity,String transID,String currency,String price){
		
		this.mactivity = activity;
		this.mtransID = transID;
		this.mprice = price;
		this.mcurrency = currency;
	}
	
	private String getUrl(){
		//sid  feeid  
		//uid  userid
		//String url = "http://www.pixelmagicnet.com/PixelGate/api-v2.php?c=pay&channel=creditcard&sid=QWERasdf&uid=7&price=42&ref_id=A0106201335200108&app=E02&is_bypass=1&lang=th";
		String url = "http://www.pixelmagicnet.com/PixelGate/api-v2.php?" +
				"c=pay&channel=creditcard" +
				"&sid="+mfeeid+"&uid="+mcustomerId+"&price="+String.valueOf(mprice)+"&ref_id="+getRefID()+"&app=E03&is_bypass=1&lang=th"+"&etc="+getEtc();
		return url;
	}
	private String getRefID(){
		String ref =null;
		int math = 0;
		Random dom = new Random();//四位随机数
		math = BluePayUtil.getRondom(dom);
		ref = "E03"+DateUtil.getCurrentMonth()+DateUtil.getCurrentDay()+
				DateUtil.getFormatCurrentTime("HH")+DateUtil.getFormatCurrentTime("mm")
				+DateUtil.getFormatCurrentTime("ss")+math;
		//E030819155753

		return ref;
	}
	
	private void arPay(String pmid){
        Intent intent = new Intent(mactivity,
               PayssionActivity.class);
        intent.putExtra(
                PayssionActivity.ACTION_REQUEST,
                new PayRequest()
	                      .setLiveMode(true)//false if you are using sandbox environment
	                      .setAPIKey("5a10d27e413a4f4e")//Your API Key
	                      .setAmount(Double.parseDouble(mprice))
	                      .setPMId(pmid)
	                      .setCurrency(mcurrency)
	                      .setDescription("Payment")
	                      .setOrderId(mtransID)// your order id
	                      .setSecretKey("3ffeb446b8079f85b0223f5d6bb8cee2")
        				   );
        Log.d("logcart", "mtransID----"+mtransID);
        mactivity.startActivityForResult(intent, 0);
	}
	
	private String getEtc(){
		
		return mfeeid+mcustomerId;
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.layout_cashu:
			arPay("cashu");
			MyPayDailog.this.cancel();
			break;
		case R.id.layout_bitcoin:
			//  bitcoin  alipay_cn
			arPay("bitcoin");
			MyPayDailog.this.cancel();
			break;
		}
	}
}
