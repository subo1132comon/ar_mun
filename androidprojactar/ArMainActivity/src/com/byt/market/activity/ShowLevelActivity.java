package com.byt.market.activity;

import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.data.UserData;
import com.byt.market.ui.ManagerFragment;
import com.byt.market.util.DisplayParams;
import com.byt.market.util.DisplayUtil;
import com.byt.market.util.NormalLoadPictrue;
import com.byt.market.view.MyTextView;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShowLevelActivity extends Activity implements OnClickListener {
		TextView textcontent,shareto;
		private TextView username = null;
		private TextView mineIntegral = null;
		private LinearLayout integral_icon;
		private Button shartBtn = null;
		private LinearLayout  mine_vip_level = null;
		private ImageView m_usericon;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.showintegral);
			initview();
			addlistenler();
			initdata();
			initVIPPointDescribe();			
		}

		private void addlistenler() {
			// TODO Auto-generated method stub
			
		}

		private void initview() {
			// TODO Auto-generated method stub
			textcontent=(TextView) findViewById(R.id.level_context);
			shareto = (TextView) findViewById(R.id.shareto);
			View titlebar_back_arrow = findViewById(R.id.titlebar_back_arrow);
			titlebar_back_arrow.setOnClickListener(this);
			View titlebar_icon = findViewById(R.id.titlebar_icon);
			TextView titlebar_title = (TextView) findViewById(R.id.titlebar_title);
			View titlebar_search_button = findViewById(R.id.titlebar_search_button);
			View titlebar_applist_button_container = findViewById(R.id.titlebar_applist_button_container);

			titlebar_back_arrow.setVisibility(View.VISIBLE);
			titlebar_icon.setVisibility(View.GONE);
			titlebar_search_button.setVisibility(View.GONE);
			titlebar_applist_button_container.setVisibility(View.INVISIBLE);
			findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
			titlebar_title.setText(R.string.mine_integral);
			
			m_usericon = (ImageView) findViewById(R.id.user_icon);
			username = (TextView)findViewById(R.id.appusername);
			mineIntegral = (TextView)findViewById(R.id.mine_ingegral); 
			integral_icon = (LinearLayout)findViewById(R.id.integralicon);
			mine_vip_level = (LinearLayout)findViewById(R.id.mine_vip_level);
			shartBtn = (Button)findViewById(R.id.sharebtn);
			shartBtn.setOnClickListener(this);
		}

		private void initdata() {
			String userNickName ;
			int currIntegralValue = 0;
			UserData user = MyApplication.getInstance().getUser();
			String usericon = MyApplication.getInstance().getUser().getIconUrl();
			try {
				new NormalLoadPictrue().getPicture(this,usericon, m_usericon);
			} catch (Exception e) {
				// TODO: handle exception
				
			}
			// TODO Auto-generated method stub
			textcontent.setText(getString(R.string.integral_help_text));
			shareto.setText(getString(R.string.shareto));
			
			if (user.isLogin()) 
			{				
				userNickName = user.getUid();	
				int userlevel=user.getUlevel();
				
				currIntegralValue = user.getCredit();
				if (!TextUtils.isEmpty(userNickName)) 
				{					
					username.setText(userNickName);	
					mineIntegral.setText(getString(R.string.integral_value)+ ": " + currIntegralValue);
					
					/** 用于级别信息 start**/
					Resources r = getResources(); 
					TextView textView = new TextView(this);  
					Paint paint = textView .getPaint(); 
					float textWidth = paint.measureText(getString(R.string.VIP_LEVEL1+userlevel - 1) + " 6"); 
					int height = 25;
					DisplayParams displayParams = DisplayParams.getInstance(this);	
					mine_vip_level.addView(fillupMyTextView(R.string.VIP_LEVEL1+userlevel - 1), (int)textWidth, DisplayUtil.dip2px(height, displayParams.scale));
					/** 用于级别信息 end**/
				}
				
				if(userlevel > 0)
				{
					for(int i=0; i< userlevel; i++)
					{
						ImageView imageView = new ImageView(this);
						imageView.setBackgroundResource(R.drawable.level_bg);
						imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,R.dimen.level_height));
						integral_icon.addView(imageView);
					}
				}
				else if(userlevel == 0)/** 默认0分时也显示一颗星**/
				{						
					ImageView imageView = new ImageView(this);
					imageView.setBackgroundResource(R.drawable.level_bg);
					imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,R.dimen.level_height));
					integral_icon.addView(imageView);
				}
			}	
		}

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch(arg0.getId()){
			case R.id.titlebar_back_arrow:// 返回
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				break;
			case R.id.sharebtn:
				Intent intent =new Intent();
				intent.setClass(ShowLevelActivity.this,ShareActivity.class);		
				ShowLevelActivity.this.startActivity(intent);				
			}
		}
		@Override
		public void finish() {
			super.finish();
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		}
		
		private MyTextView fillupMyTextView(int resId)
		{
			MyTextView myTextView = new MyTextView(this);
			myTextView.setText(getString(resId));
			myTextView.setTextSize(17);
			myTextView.setTextAlign(MyTextView.TEXT_ALIGN_BOTTOM_WITHOUT_PADDING);
			myTextView.setTextColor(Color.BLACK);
			myTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			return myTextView;
		}
		public void initVIPPointDescribe()
		{
			Resources r = getResources(); 
		//	int width = (int)r.getDimension(R.dimen.mytextview_width); 
			int width = 127; 
			int height = 25;
			DisplayParams displayParams = DisplayParams.getInstance(this);		
			
			LinearLayout container1 = (LinearLayout) findViewById(R.id.tv_container_1);
			LinearLayout container2 = (LinearLayout) findViewById(R.id.tv_container_2);
			LinearLayout container3 = (LinearLayout) findViewById(R.id.tv_container_3);
			LinearLayout container4 = (LinearLayout) findViewById(R.id.tv_container_4);			
			LinearLayout container5 = (LinearLayout) findViewById(R.id.tv_container_5);		

			container1.addView(fillupMyTextView(R.string.level_1), DisplayUtil.dip2px(width, displayParams.scale), DisplayUtil.dip2px(height, displayParams.scale));
			container2.addView(fillupMyTextView(R.string.level_2), DisplayUtil.dip2px(width, displayParams.scale), DisplayUtil.dip2px(height, displayParams.scale));
			container3.addView(fillupMyTextView(R.string.level_3), DisplayUtil.dip2px(width, displayParams.scale), DisplayUtil.dip2px(height, displayParams.scale));
			container4.addView(fillupMyTextView(R.string.level_4), DisplayUtil.dip2px(width, displayParams.scale), DisplayUtil.dip2px(height, displayParams.scale));
			container5.addView(fillupMyTextView(R.string.level_5), DisplayUtil.dip2px(width, displayParams.scale), DisplayUtil.dip2px(height, displayParams.scale));
		}
}
