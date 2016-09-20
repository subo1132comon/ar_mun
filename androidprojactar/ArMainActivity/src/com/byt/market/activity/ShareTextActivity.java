package com.byt.market.activity;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.data.AppItem;
import com.byt.market.data.UserData;
import com.byt.market.download.DownloadTaskManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShareTextActivity extends Activity {
	
	boolean issendonclick=false;
	TextView  shareText;
	Button shareButton;
	GridView mylistview;
	TextView shareChoice;
	ResolveInfo appInfo;
	TextView sharegzText;
	List<ResolveInfo> showapplist;
	List<ResolveInfo> dishowapplist;
	AppItem bundle;
	MyListAdapter adapter;
	private String sendtext;
			@Override
			protected void onCreate(Bundle savedInstanceState) {
				// TODO Auto-generated method stub
				super.onCreate(savedInstanceState);
				Intent intent=getIntent();				
				 showapplist=new ArrayList<ResolveInfo>();
				 dishowapplist=new ArrayList<ResolveInfo>();
				 sendtext=intent.getStringExtra("sendstring");
				setContentView(R.layout.share_main);
				  List<ResolveInfo> applist=getShareApps(this);
			         adapter=new MyListAdapter(showapplist);
			         mylistview=(GridView) findViewById(R.id.testlistviewid);
			        mylistview.setAdapter(adapter);
			        shareText=(TextView) findViewById(R.id.sharetext);
			        shareChoice=(TextView) findViewById(R.id.sharechoice); 
			        shareButton=(Button) findViewById(R.id.appsharebutton);
			        sharegzText=(TextView) findViewById(R.id.sharegztext);
			    	findViewById(R.id.getmoreshare).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							for(ResolveInfo res:dishowapplist)
							{
							showapplist.add(res);
							}
							adapter.notifyDataSetChanged();
							arg0.setVisibility(View.GONE);
						}
					});
			        shareButton.setOnClickListener(new OnClickListener() {
			        
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							shareText();
						}
					});
			        shareButton.setVisibility(View.GONE);
			}
			
			//得到所有可以分享的应用
			
			public  List<ResolveInfo> getShareApps(Context context) {
				
				List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
				Intent intent = new Intent(Intent.ACTION_SEND, null);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.setType("text/plain");
				PackageManager pManager = context.getPackageManager();
				mApps = pManager.queryIntentActivities(intent,
						PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
				for(ResolveInfo resolveInfo:mApps){
					if(resolveInfo.activityInfo.name.equals("com.mediatek.bluetooth.BluetoothShareGatewayActivity"))
					{
						mApps.remove(resolveInfo);
						break;
					}
				}
				if(mApps.size()>3)
				{
				for(ResolveInfo resolve:mApps){	
				if (showapplist.size() < 3) {
					if (resolve.activityInfo.name
							.equals("com.facebook.composer.activity.ImplicitShareIntentHandler")) {
						showapplist.add(resolve);
					} else if (resolve.activityInfo.name
							.equals("jp.naver.line.android.activity.selectchat.SelectChatActivity")) {
						showapplist.add(resolve);
					} else if (resolve.activityInfo.name
							.equals("com.tencent.mm.ui.tools.ShareImgUI")) {
						showapplist.add(resolve);
					}else if (resolve.activityInfo.name
							.equals("com.facebook.katana.activity.composer.ImplicitShareIntentHandler")) {
						showapplist.add(resolve);
					} else if(resolve.activityInfo.name
							.equals("com.facebook.composer.shareintent.ImplicitShareIntentHandler")){
						showapplist.add(resolve);
					}
					
					
				}
				else{
					break;
				}
				}
				for(ResolveInfo resolve:mApps){	
					if (showapplist.size() < 3) {
						if (resolve.activityInfo.name
								.equals("com.twitter.android.PostActivity")) {
							showapplist.add(resolve);
						} else if (resolve.activityInfo.name
								.equals("com.whatsapp.ContactPicker")) {
							showapplist.add(resolve);
						} else if (resolve.activityInfo.name
								.equals("com.android.mail.compose.ComposeActivityGmail")) {
							showapplist.add(resolve);
						}
					}
					else{
						 if (resolve.activityInfo.name
								.equals("com.twitter.android.PostActivity")) {
							dishowapplist.add(resolve);
						} else if (resolve.activityInfo.name
								.equals("com.whatsapp.ContactPicker")) {
							dishowapplist.add(resolve);
						} else if (resolve.activityInfo.name
								.equals("com.android.mail.compose.ComposeActivityGmail")) {
							dishowapplist.add(resolve);
						}
					}
						
					}
				mApps.removeAll(showapplist);
				if(dishowapplist.size()>0){
					mApps.removeAll(dishowapplist);
				}
				
				int j=showapplist.size();
				for(int i=0;i<3-j;i++)
				{
					showapplist.add(mApps.get(0));
					mApps.remove(0);
				}
				if(mApps.size()>0)
				{
					dishowapplist.addAll(mApps);
				}
				}else
				{
					findViewById(R.id.getmoreshare).setVisibility(View.GONE);
					showapplist=mApps;
				}
				
				return showapplist;
			}
			 /*-----------------Bestone add end-----*/
		    

			
			public class MyListAdapter extends BaseAdapter {  
						 private   List<ResolveInfo> myArrList=null; 
				     
				        LayoutInflater inflater;  
				        View view;  
				        recordLayoutHolder recordLayoutHolder;  
				        TextView myfileicon = null;  
				        LinearLayout shareLinea;
				        ImageView iconbar=null;
				  
				        private int selectedPosition = -1;//
				  
				        public MyListAdapter(List<ResolveInfo> applist) {
							// TODO Auto-generated constructor stub
				        	myArrList = applist; 
						}

						
				  
				        @Override  
				        public int getCount() {  
				            // TODO Auto-generated method stub  
				            return myArrList.size();  
				        }  
				  
				        @Override  
				        public Object getItem(int position) {  
				            // TODO Auto-generated method stub  
				            return myArrList.get(position);  
				        }  
				  
				        @Override  
				        public long getItemId(int position) {  
				            // TODO Auto-generated method stub  
				            return position;  
				        }  
				  
				        public void setSelectedPosition(int position) {  
				            selectedPosition = position;  
				        }  
				  
						@Override  
				        public View getView(int position, View convertView, ViewGroup parent) {  
				            // TODO Auto-generated method stub  
				        	final int myposition=position;
				        
				            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
				            view = inflater.inflate(R.layout.bestone_choicebutton_share, null, false);  
				            recordLayoutHolder = (recordLayoutHolder) view.getTag();  
				  
				            if (recordLayoutHolder == null) {  
				            	recordLayoutHolder = new recordLayoutHolder();  
				            	recordLayoutHolder.setas = (TextView) view  
				                        .findViewById(R.id.setas);  
				            	recordLayoutHolder.seticon = (ImageView) view  
				                        .findViewById(R.id.seticon); 
				            	recordLayoutHolder.shareActivity=(LinearLayout)view.findViewById(R.id.sharelayout);
				            	
				                view.setTag(recordLayoutHolder);  
				            }  
				            iconbar=recordLayoutHolder.seticon;
				            myfileicon = recordLayoutHolder.setas;  
				            shareLinea=recordLayoutHolder.shareActivity;
				        	
				        		 PackageManager pm = ShareTextActivity.this.getPackageManager(); 
				        		
								Drawable appIcon=myArrList.get(position).loadIcon(pm);
								iconbar.setBackgroundDrawable(appIcon);
								myfileicon.setText(myArrList.get(position).loadLabel(pm));
								
							
				            	
				        	shareLinea.setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View arg0) {
										appInfo=(ResolveInfo) myArrList.get(myposition);
									/*	mylistview.setVisibility(View.GONE);
										 
										 shareChoice.setVisibility(View.GONE);
										 shareText.setVisibility(View.VISIBLE);
										 shareButton.setVisibility(View.VISIBLE);
										 sharegzText.setVisibility(View.VISIBLE);*/
										shareText();
									
									}
								});
							
							
				            
				            return view;  
				  
				        }  
				         
				    
				        
			   }
					public class recordLayoutHolder{
						LinearLayout shareActivity; 
						TextView setas;
						ImageView seticon;
					}
					@Override
					protected void onDestroy() {
					
					
						// TODO Auto-generated method stub
						super.onDestroy();
						
					}
					@Override
				    public void onBackPressed() {
					     super.onBackPressed();
				    }
					//
					private void shareText(){
						
						// TODO Auto-generated method stub
					//分享网址出去
						UserData user = MyApplication.getInstance()
								.getUser();		
						if(user==null||user.getUid()==null||user.getUid().equals(""))
						{
							Toast.makeText(ShareTextActivity.this, R.string.toast_uid_cannot_null, Toast.LENGTH_SHORT).show();
							ShareTextActivity.this.finish();
							return;
							
						}									
					    Intent shareIntent=new Intent(Intent.ACTION_SEND);
					    shareIntent.setComponent(new ComponentName(appInfo.activityInfo.packageName, appInfo.activityInfo.name));  
					   // shareIntent.setType("image/png"); 
					    shareIntent.setType("text/plain"); 
					    shareIntent.putExtra(Intent.EXTRA_TEXT, sendtext);
					 //  shareIntent.putExtra("sms_body", sendtext);//解决短信分享获取不到图片信息
					   
					   // Uri uri =Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.ic_app+".png");					 
					 //   shareIntent.putExtra(Intent.EXTRA_STREAM, uri); 
					    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
					    startActivity(shareIntent);
					    issendonclick=true;
						Intent intent =new Intent();
						if(bundle!=null)
						{
							DownloadTaskManager.getInstance().onDownloadBtnClick(bundle);
						}
						intent.putExtra("isok", issendonclick);
						setResult(RESULT_OK, intent);					   
					    ShareTextActivity.this.finish();
					}
				
}
