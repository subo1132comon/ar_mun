package com.byt.market.mediaplayer.txtreader;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.byt.market.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ScrollView;

public class TxtViewerActivity extends Activity implements OnTouchListener{
    private static final String TAG = "TxtViewerActivity";
    private static final int MIN_ZOOM = -3;
    private static final int MAX_ZOOM = 6;
    private String bookPath = "";//
    private int zoom = 0;

   // private TxtBook txtBook;

    private ThaiLineBreakingTextView textView;
    private ScrollView mscroll;
    Intent intent;
    //private TextView textView;
    private ImageButton btnNeiht;
    private ImageButton btnLeiht;
    private ImageButton btnZoomOut;
    private ImageButton btnZoomIn;
    private String msposition = "0";
    private int mcrruetPosition;
    private boolean isthem = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Themutil.onActivityCreateSetTheme(TxtViewerActivity.this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.viewer);
        
        Intent intent = getIntent();
		bookPath = intent.getStringExtra("path");
		if(bookPath==null){
			isthem = true;
			SharedPreferences sh = getPreferences(MODE_PRIVATE);
			bookPath = sh.getString("bookpt", "");
		}
		btnNeiht = (ImageButton) findViewById(R.id.btn_night);
		btnNeiht.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Themutil.changeToTheme(TxtViewerActivity.this, 1);
				SharedPreferences sh = getPreferences(MODE_PRIVATE);
				Editor edit = sh.edit();
				edit.putString("bookpt", bookPath); 
				edit.commit(); 
				
				if(msposition.equals("0")){
					msposition = String.valueOf(mcrruetPosition);
				}
				 Log.d("logcart","onDestroy"+msposition);
				 ThemInstens.getTemInstens().setPosition(msposition);
			}
		});
		
		btnLeiht = (ImageButton) findViewById(R.id.btn_light);
		btnLeiht.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Themutil.changeToTheme(TxtViewerActivity.this, 2);
				SharedPreferences sh = getPreferences(MODE_PRIVATE);
				Editor edit = sh.edit();
				edit.putString("bookpt", bookPath); 
				edit.commit(); 
				
				if(msposition.equals("0")){
					msposition = String.valueOf(mcrruetPosition);
				}
				 Log.d("logcart","onDestroy"+msposition);
				 ThemInstens.getTemInstens().setPosition(msposition);
			}
		});
		
        btnZoomIn = (ImageButton)findViewById(R.id.btn_zoomin);
        btnZoomIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomIn();
            }
        });
        btnZoomOut = (ImageButton)findViewById(R.id.btn_zoomout);
        btnZoomOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomOut();
            }
        });
        textView = (ThaiLineBreakingTextView) findViewById(R.id.text);
        mscroll = (ScrollView) findViewById(R.id.scrollview);
        mscroll.setOnTouchListener(this);
      //  Log.d("log", shfName(bookPath)+"=============");
        SharedPreferences pref = getSharedPreferences(shfName(bookPath), 0);
        if(!isthem){
        	 String position = pref.getString("position", "");//如果没有，默认为"" 
        	 if(!position.equals("")){
            	 mcrruetPosition = Integer.parseInt(position);
            	 chergerText(textView);
            }
        }else{
        	//SharedPreferences sh = getPreferences(MODE_PRIVATE);
//        	SharedPreferences sh = getSharedPreferences("th", 0);
//        	mcrruetPosition = Integer.parseInt(sh.getString("osition","0"));
        	mcrruetPosition 
        	= Integer.parseInt(ThemInstens.getTemInstens().getPosition());
        	chergerText(textView);
        }
        updateTxtViewer();
    }
    private void updateTxtViewer() {
    	
        textView.setText2(getTextt());
    }
    private void updateButtonState(ImageButton btnLeft, ImageButton btnRight,
            int current_value, int min_value, int max_value) {
        if (current_value <= min_value) {
            btnLeft.setEnabled(false);
        } else if (current_value >= max_value) {
            btnRight.setEnabled(false);
        } else {
            btnLeft.setEnabled(true);
            btnRight.setEnabled(true);
        }
    }



    private void zoomIn() {
        if (zoom >= MAX_ZOOM) {
            return;
        }
        zoom++;
        updateZoomButtonState();

        float size = textView.getTextSize();

        Log.d(TAG, "Font size before zoom in: " + size);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size + 0.1f * size);
        Log.d(TAG, "Font size after zoom in: " + textView.getTextSize());
    }

    private void zoomOut() {
        if (zoom <= MIN_ZOOM) {
            return;
        }
        zoom--;
        updateZoomButtonState();
        float size = textView.getTextSize();
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size - 0.1f * size);
    }

    private void updateZoomButtonState() {
        updateButtonState(btnZoomOut, btnZoomIn, zoom, MIN_ZOOM, MAX_ZOOM);
    }
    
    @Override
	protected void onSaveInstanceState(Bundle outState) {
		//outState.putString("saved_string", textView.getText2().toString());
	}

    
    public String getTextt() {
        try {
//        	String path =Environment.getExternalStorageDirectory()
//    				.getPath() + "/SYNC/novel/AA1.txt";
            File f = new File(bookPath);
            InputStreamReader isr =
                new InputStreamReader(new FileInputStream(f), "utf-8");
            
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            isr.close();
            return sb.toString();
        } catch (IOException e) {
            // Should never happen!
        	
            throw new RuntimeException(e);
        }
    }
    private Handler mhandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		mscroll.scrollTo(0, mcrruetPosition);
    	};
    };
    private void chergerText(final ThaiLineBreakingTextView txt){
    	new Thread(){
    		public void run() {
    			while (txt.getText().length()==0) {
					try {
						sleep(100);
						Log.d("log", "jjj");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
    			Message msg = mhandler.obtainMessage();
    			mhandler.sendMessage(msg);
    			Log.d("log", "jjj==="+txt.getText());
    			return;
    		};
    	}.start();
    }
    
    //文件名截取
    /**
     * //截取#之前的字符串
String str = "sdfs#d";
str.substring(0, str.indexOf("#"));
//输出的结果为：sdfs
//indexOf返回的索引也是从0开始的，所以indexOf("#") = 4。
//java中的substring的第一个参数的索引是从0开始，而第二个参数是从1开始
     */
    private String shfName(String bookname){
    	int zong  = bookname.indexOf(".");
    	int i = zong-3;
    	return bookname.substring(i, zong);
    }
    
@Override
public boolean onTouch(View arg0, MotionEvent arg1) {
	// TODO Auto-generated method stub
	 if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
		msposition =  String.valueOf(mscroll.getScrollY());
	        Log.d("logcart","yy--"+mscroll.getScrollY()+"xx==="+mscroll.getScrollX() );
	    }
	return false;
}

//
//@Override
//protected void onStop() {
//	// TODO Auto-generated method stub
//	super.onStop();
//	// TODO Auto-generated method stub
//		if(msposition.equals("0")){
//			msposition = String.valueOf(mcrruetPosition);
//		}
//		 Log.d("logcart","onDestroy"+msposition);
//		 ThemInstens.getTemInstens().setPosition(msposition);
//		super.onDestroy();
//}

@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	// TODO Auto-generated method stub
	
	if(msposition.equals("0")){
		
		msposition = String.valueOf(mcrruetPosition);
	}
	 Log.d("logcart","yy-返回-"+msposition);
	//SharedPreferences pref = getPreferences(mode); 
	SharedPreferences pref = getSharedPreferences(shfName(bookPath), 0);
	Editor edit = pref.edit();
	edit.putString("position", msposition); 
	edit.commit(); 
	return super.onKeyDown(keyCode, event);
}
}
