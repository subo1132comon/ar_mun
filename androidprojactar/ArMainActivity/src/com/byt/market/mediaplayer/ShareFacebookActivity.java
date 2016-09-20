package com.byt.market.mediaplayer;

import com.byt.ar.R;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.bitmaputil.core.display.RoundedBitmapDisplayer;
import com.byt.market.data.RingItem;
import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ShareFacebookActivity extends Activity implements OnClickListener {
	ImageView musicicon;
	TextView musicname,musicauther;
	LinearLayout gotoshare;
	private UiLifecycleHelper uiHelper;
	private DisplayImageOptions mOptions;
	RingItem bundle;
	ImageLoader imageLoader;
	
	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.facebookshare);
	bundle=(RingItem) getIntent().getParcelableExtra("sendstring");
	uiHelper = new UiLifecycleHelper(this, callback);
	        uiHelper.onCreate(savedInstanceState);
	initlayout();
	initdata();
	}
	private void initdata() {
		musicname.setText(bundle.name);
		musicauther.setText(bundle.user);
		imageLoader.displayImage(bundle.logo, musicicon, mOptions);
	}
	private void initlayout(){
		imageLoader = ImageLoader.getInstance();
		mOptions = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.app_empty_icon)
		.showImageForEmptyUri(R.drawable.app_empty_icon)
		.cacheOnDisc()
		.displayer(new RoundedBitmapDisplayer(200)).build();
		musicicon=(ImageView) findViewById(R.id.sharebg);
		musicname=(TextView) findViewById(R.id.musicname);
		musicauther=(TextView) findViewById(R.id.musicauther);
		gotoshare=(LinearLayout) findViewById(R.id.gotoshare);
		gotoshare.setOnClickListener(this);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	// Session.getActiveSession().onActivityResult(this, requestCode,resultCode, data);
	uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
	       @Override
	       public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {

	       }

	       @Override
	       public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	           
	       }
	       
	   });
	}

	@Override
	public void onClick(View v) {
	switch (v.getId()) {
	case R.id.gotoshare:
		doShare();
		break;
	}
	}

	private void doLogin(){
	// Session.openActiveSession(this, true, new StatusCallback() {
	// @Override
	// public void call(Session session, SessionState state, Exception exception) {
	// if(session.isOpened()){
	// // get token
	// Toast.makeText(FaceBookTest.this, session.getAccessToken(), Toast.LENGTH_SHORT).show();
	// }
	// }
	// });
	 Session session = Session.getActiveSession();
	       if (!session.isOpened() && !session.isClosed()) {
	           session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
	       } else {
	           Session.openActiveSession(this, true, statusCallback);
	       }
	}
	private class SessionStatusCallback implements Session.StatusCallback {
	       @Override
	       public void call(Session session, SessionState state, Exception exception) {
	        if(session.isOpened()){
	// get token
	Toast.makeText(ShareFacebookActivity.this, session.getAccessToken(), Toast.LENGTH_SHORT).show();
	}
	       }
	   }
	private void onClickLogout() {
	       Session session = Session.getActiveSession();
	       if (!session.isClosed()) {
	           session.closeAndClearTokenInformation();
	       }
	   }
	public void doShare(){
	 
	       try {  
	           FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)  
	                   .setLink(bundle.musicuri)  
	                   .setApplicationName("SYNC")  
	                   .setDescription(bundle.user)  
	                   .setName(bundle.name)  
	                   .setCaption(bundle.spename)  
	                   .setPlace("place")  
	                   .setPicture(bundle.logo)
	                   .build();  
	           uiHelper.trackPendingDialogCall(shareDialog.present());  
	           this.finish();
	 
	       } catch (FacebookException e) {  
	           Toast.makeText(getApplication(), "Facebook app is not installed", Toast.LENGTH_SHORT).show();  
	       } catch (Exception e) {  
	           Toast.makeText(getApplication(), "Unexpect Exception", Toast.LENGTH_SHORT).show();  
	       }  
	   }
	 @Override    
	   public void onPause() {    
	       super.onPause();    
	       uiHelper.onPause();    
	   }    
	   
	   @Override    
	   public void onDestroy() {    
	       super.onDestroy();    
	       uiHelper.onDestroy();    
	   }    
	   
	   @Override    
	   public void onSaveInstanceState(Bundle outState) {    
	       super.onSaveInstanceState(outState);    
	       uiHelper.onSaveInstanceState(outState);    
	   }    
	     
	   private Session.StatusCallback callback = new Session.StatusCallback() {    
	       @Override    
	       public void call(Session session, SessionState state,Exception exception) {  
	           onSessionStateChange(session, state, exception);    
	       }    
	   };    
	     
	   private void onSessionStateChange(Session session, SessionState state,    
	           Exception exception) {    
	       if (state.isOpened()) {    
	           Log.i("tag", "Logged in...");    
	       } else if (state.isClosed()) {    
	           Log.i("tag", "Logged out...");    
	       }    
	   } 
}