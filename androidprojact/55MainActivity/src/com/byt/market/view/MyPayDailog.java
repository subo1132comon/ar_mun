package com.byt.market.view;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

import com.bluepay.data.Config;
import com.bluepay.pay.BlueMessage;
import com.bluepay.pay.BluePay;
import com.bluepay.pay.ClientHelper;
import com.bluepay.pay.IPayCallback;
import com.bluepay.pay.PublisherCode;
import com.byt.market.R;
import com.byt.market.activity.PayWebviewActivity;
import com.byt.market.mediaplayer.ui.LiveFragment;
import com.byt.market.util.BluePayUtil;
import com.byt.market.util.DateUtil;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MyPayDailog extends Dialog implements android.view.View.OnClickListener{

	Context mcontext;
	private RelativeLayout mbank;
	private RelativeLayout mLinpay;
	private RelativeLayout mSMS;
	private RelativeLayout masi;
	private RelativeLayout mhappy;
	private RelativeLayout mtruemoney;
	private TextView mtext_props,mvalues;
	
	//支付所需 参数
	private Activity mactivity;
	private String mtransID;
	private String mcustomerId;
	private String mprice;
	private String mpropsName;
	private int msmsId = 0;
	private String mscheme;
	private PayBack mcallback;
	private int mfeeid = 0;
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
		registRecive();
	}
	
	private void initView(){
		mbank = (RelativeLayout) findViewById(R.id.layout_bank);
		mLinpay = (RelativeLayout) findViewById(R.id.layout_LINEPay);
		mSMS = (RelativeLayout) findViewById(R.id.layout_sms);
		mtext_props = (TextView) findViewById(R.id.text_props);
		mvalues = (TextView) findViewById(R.id.text_money);
		masi = (RelativeLayout) findViewById(R.id.layout_ias);
		mhappy = (RelativeLayout) findViewById(R.id.layout_happy);
		mtruemoney = (RelativeLayout) findViewById(R.id.layout_truemoney_logo);
		
		mtext_props.setText(mpropsName);
		mvalues.setText(String.valueOf(mprice)+" "+"THB");
		
		mbank.setOnClickListener(this);
		mLinpay.setOnClickListener(this);
		mSMS.setOnClickListener(this);
		masi.setOnClickListener(this);
		mhappy.setOnClickListener(this);
		mtruemoney.setOnClickListener(this);
		if(Integer.parseInt(mprice)>250){
			mSMS.setVisibility(View.GONE);
		}
	}
	
	public void initData(Activity activity,String transID,String customerId,String price,
			String propsName,int feeid,PayBack callback){
		
		this.mactivity = activity;
		this.mtransID = transID;
		this.mcustomerId = customerId;
		this.mprice = price;
		this.mpropsName = propsName;
		this.mfeeid = feeid;
		this.mcallback = callback;
//		mtext_props.setText(mpropsName);
//		int money = Integer.parseInt(mprice)/100;
//		mvalues.setText(String.valueOf(money));
		
	}
	@SuppressWarnings("serial")
	public class DailogIpayback extends IPayCallback{
		@Override
		public void onFinished(BlueMessage bluemsg) {
			// TODO Auto-generated method stub
			showTosat("test--");
			Log.d("subo", "desc---"+bluemsg.getDesc());
			if(bluemsg!=null){
				if(bluemsg.getCode() == 201){
					Toast.makeText(mcontext, mcontext.getString(R.string.pay_text), Toast.LENGTH_LONG).show();
				}else{
					mcallback.Resout(bluemsg);
				}
			}
			//MyDailog.this.dismiss();
			MyPayDailog.this.cancel();
		}
		
	}
	public interface PayBack{
		public void Resout(BlueMessage bluemsg);
		public void bankRsout(int re);
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
	private String getEtc(){
		
		return mfeeid+mcustomerId;
	}
	private void registRecive(){
		mcontext.registerReceiver(new BankBrodcast(), new IntentFilter("bank.pay.com"));
	}
	private class BankBrodcast extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if("bank.pay.com".equals(arg1.getAction())){
				//Toast.makeText(mcontext, "支付成功", Toast.LENGTH_LONG).show();
				if(arg1.getStringExtra("re").equals("success")){
					mcallback.bankRsout(1);
				}else if(arg1.getStringExtra("re").equals("fail")){
					mcallback.bankRsout(0);
				}
			}
		}
		
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.layout_bank:
			MobclickAgent.onEvent(mcontext, "bank");
			StatService.trackCustomEvent(mcontext, "bank", "");
			Intent intent = new Intent(mcontext, PayWebviewActivity.class);
			intent.putExtra("url", getUrl());
			mcontext.startActivity(intent);
			MyPayDailog.this.cancel();
			break;
		case R.id.layout_LINEPay:
			MobclickAgent.onEvent(mcontext, "LINEPay");
			StatService.trackCustomEvent(mcontext, "LINEPay", "");
			BluePay.getInstance().payByWallet(mactivity, mcustomerId, 
					mtransID, Config.K_CURRENCY_TRF,
					String.valueOf(Integer.parseInt(mprice)*100),
					mpropsName, PublisherCode.PUBLISHER_LINE,
					"mun://com.mun.pay", true, new DailogIpayback());
			break;
		case R.id.layout_sms:
			//Config.K_CURRENCY_THB
			MobclickAgent.onEvent(mcontext, "sms");
			StatService.trackCustomEvent(mcontext, "sms", "");
			BluePay.getInstance().payBySMS(mactivity, mtransID,
					Config.K_CURRENCY_THB, String.valueOf(Integer.parseInt(mprice)*100),
					msmsId, mpropsName, true, new DailogIpayback());
			break;
		case R.id.layout_ias:
			showTosat("ias");
			/**
			 * 支付卡计费:
		void  payByCashcard(final Activity activity, String customId,
		String transactionId, String propsName, String publisher,
		String cardNo, String serialNo, IPayCallback callback)
			 */
			BluePay.setShowCardLoading(false);// 不使用 sdk loading框
			BluePay.getInstance().payByCashcard(mactivity, mcustomerId,
					mtransID, mpropsName,
					PublisherCode.PUBLISHER_12CALL, null, null, new DailogIpayback());
			break;
		case R.id.layout_happy:
			showTosat("happy");
			BluePay.getInstance().payByCashcard(mactivity, mcustomerId,
					mtransID,mpropsName,
					PublisherCode.PUBLISHER_HAPPY, "", null, new DailogIpayback());
			
			
			break;
		case R.id.layout_truemoney_logo:
			showTosat("truemoney");
			BluePay.getInstance().payByCashcard(mactivity, mcustomerId,
					mtransID, "PropsName",
					PublisherCode.PUBLISHER_TRUEMONEY, "", null, new DailogIpayback());
			break;
		}
	}
	private void showTosat(String toast){
		Toast.makeText(mcontext, toast, Toast.LENGTH_LONG).show();
	}
}
