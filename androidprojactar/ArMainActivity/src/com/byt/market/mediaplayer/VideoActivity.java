package com.byt.market.mediaplayer;
import java.util.ArrayList;



import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.mediaplayer.MusicDownLoadManageActivity;
import com.byt.market.mediaplayer.ui.VideoMainFragment;
import com.byt.market.tools.LogCart;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.RapitUtile;
import com.byt.market.view.AlwsydMarqueeTextView;
import com.payssion.android.sdk.Payssion;
import com.payssion.android.sdk.PayssionActivity;
import com.payssion.android.sdk.constant.PPaymentState;
import com.payssion.android.sdk.model.GetDetailRequest;
import com.payssion.android.sdk.model.GetDetailResponse;
import com.payssion.android.sdk.model.PayResponse;
import com.payssion.android.sdk.model.PayssionResponse;
import com.payssion.android.sdk.model.PayssionResponseHandler;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
public class VideoActivity extends FragmentActivity  implements OnClickListener{

	private View recArea;
	AlwsydMarqueeTextView tv_title;
	private VideoMainFragment movieFragment;
	//private VallayFragment vallayFragment;
	private View movie_line,holly_line;
	Button buttonmovie,buttonholly;
	ViewPager mPager;
    private ArrayList<BaseUIFragment> fragmentList;  
    public static long umtime; 
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_main_fragment);
		initView();
		initPaper();
		umtime = System.currentTimeMillis();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String text = getString(R.string.happy_more);
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	private void initPaper() {
	
		
	}
	private void initView() {
		try {
			findViewById(R.id.topline4).setVisibility(View.GONE);
			findViewById(R.id.top_downbutton).setVisibility(View.VISIBLE);
			findViewById(R.id.titlebar_applist_button_container).setVisibility(
					View.GONE);
			findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
			findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
			findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
			findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
			findViewById(R.id.top_downbutton).setOnClickListener(this);
			tv_title = (AlwsydMarqueeTextView) findViewById(R.id.titlebar_title);
			tv_title.setText(R.string.movie_string);
			findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
			recArea.setVisibility(View.VISIBLE);
			
		} catch (Exception e) {
			Log.d("myzx","initViewerror");
			e.printStackTrace();
		}

	}
	

	public void requestDelay() {
	movieFragment.requestDelay();
		
	}
	public class MyFragmentPagerAdapter extends FragmentPagerAdapter{  
        ArrayList<BaseUIFragment> list;  
        public MyFragmentPagerAdapter(FragmentManager fm,ArrayList<BaseUIFragment> list) {  
            super(getSupportFragmentManager());  
            this.list = list;  
              
        }  
          
          
        @Override  
        public int getCount() {  
            return list.size();  
        }  
          
        @Override  
        public BaseUIFragment getItem(int arg0) {  
            return list.get(arg0);  
        }  
          
          
          
          
    }

	private void onMYPageChange(int page) {
		switch(page)
		{
		case 0:
			movie_line.setVisibility(View.VISIBLE);
			holly_line.setVisibility(View.INVISIBLE);	
			buttonmovie.setSelected(true);
			buttonholly.setSelected(false);
			requestDelay();	
        	break; 

			/*case 3:
			buttonringrec.setSelected(false);
			buttonringhome.setSelected(false);
			buttonringhot.setSelected(false);
			buttonringrank.setSelected(true);
			if (homeFragment4 != null) {
				homeFragment4.request();
			}
			ringrec_line.setVisibility(View.INVISIBLE);
			ringhome_line.setVisibility(View.INVISIBLE);
			ringhot_line.setVisibility(View.INVISIBLE);
			ringrank_line.setVisibility(View.VISIBLE);	
    	break; */
		}

	}
	public void finishWindow() {
		finish();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}
	
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		StatService.trackCustomEndEvent(this, "move", "");
		int endtime = (int) (System.currentTimeMillis()-umtime);
		MobclickAgent.onEventValue(this, "Move", null,endtime/1000);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		StatService.trackCustomBeginEvent(this, "move", "");
	}
	
	@Override
	protected void onStop() {
		super.onStop();	
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
        case PayssionActivity.RESULT_OK:
            if (null != data) {
                PayResponse response = (PayResponse)data.getSerializableExtra(PayssionActivity.RESULT_DATA);
                if (null != response) {
                    String transId = response.getTransactionId(); //获取Payssion交易Id G912663851191110
					String orderId = response.getOrderId(); //获取您的订单Id
					Log.d("logcart", "orderId-------"+orderId);//orderId444412345987654321
                    //you will have to query the payment state with the transId or orderId from your server
                    //as we will notify you server whenever there is a payment state change
					Payssion.getDetail(new GetDetailRequest()
					.setAPIKey("5a10d27e413a4f4e")
					.setSecretKey("3ffeb446b8079f85b0223f5d6bb8cee2")
					.setLiveMode(true)
					.setTransactionId(transId)
					
					.setOrderId(orderId), new PayssionResponseHandler() {
						@Override
						public void onError(int arg0, String arg1,
								Throwable arg2) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onFinish() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onStart() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onSuccess(PayssionResponse response) {
							if (response.isSuccess()) {
								GetDetailResponse detail = (GetDetailResponse)response;
								if (null != detail) {
									Toast.makeText(VideoActivity.this, detail.getStateStr(), Toast.LENGTH_SHORT).show();
									if (PPaymentState.COMPLETED == detail.getState()) {
										//the payment was paid successfully
										VideoActivity.this.sendBroadcast(new Intent("COM.AR.PAY"));
									} else if (PPaymentState.FAILED == detail.getState()) {
										//the payment was failed
									} else if (PPaymentState.PENDING == detail.getState()) {
										//the payment was still pending, please save the transId and orderId
										//and recheck the state later as the payment may not be confirmed instantly
									}
								}
							} else {
								Toast.makeText(VideoActivity.this, response.getDescription(), Toast.LENGTH_SHORT).show();
								Log.d("logcart", "description---"+response.getDescription());
							}
						}
					});
                } else {
                    //should never go here
                }
            }
            break;
        case PayssionActivity.RESULT_CANCELED:
            //the transation has been cancelled, for example, the users doesn't pay but get back
            break;
        case PayssionActivity.RESULT_ERROR:
            //there is some error
            if (null != data) {
                String err_des = data.getStringExtra(PayssionActivity.RESULT_DESCRIPTION);
                Log.v(this.getClass().getSimpleName(), "RESULT_ERROR" + err_des);   
            }
            break;
        }
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.titlebar_back_arrow:
				finishWindow();
				break;
			case R.id.top_downbutton:
				Intent downloadIntent = new Intent(this,
						MusicDownLoadManageActivity.class);
				startActivity(downloadIntent);
				this.overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
				break;
			}
		
	}

	
}
