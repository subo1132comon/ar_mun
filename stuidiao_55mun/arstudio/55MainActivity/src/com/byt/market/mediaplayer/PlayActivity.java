package com.byt.market.mediaplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.httpclient.util.URIUtil;
import org.json.JSONObject;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.ShareActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.bitmaputil.core.display.RoundedBitmapDisplayer;
import com.byt.market.data.CateItem;
import com.byt.market.mediaplayer.data.PlayDownloadItem;
import com.byt.market.mediaplayer.data.VideoItem;
import com.byt.market.mediaplayer.music.PlayMusicService;
import com.byt.market.mediaplayer.service.PlayDownloadService;
import com.byt.market.mediaplayer.ui.MovieFragment;
import com.byt.market.mediaplayer.ui.VideoMainFragment;
import com.byt.market.mediaplayer.ui.VideoMainFragment.MyFragmentPagerAdapter;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Util;
import com.byt.market.view.LoadFailedView;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class PlayActivity extends Activity implements OnClickListener ,TaskListener{
	private boolean isreplay=false;
	private SeekBar seekbarwidget;
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying=false;
    private String path;
    private int position;
    private boolean isgohome=false;
    SurfaceCallback myCallBack;
    private Button playvideobutton;
    private Timer mTimer;    
    private boolean pause=false;
    private TimerTask mTimerTask;  
    private int Curtime=-1;
    private boolean isChanging=false;
    private TextView timeText;
    private static double  TIME_BASE=60;
    private static double  TIME_HOUR=60*60;
    String mTimerFormat;
    String hour_timer_format;
    private boolean islocalmovie=false;
    private RelativeLayout bommonlayout,titlelayout;
    VideoItem playItem;
    private RelativeLayout loading;
    private int secendpross=0;
    private boolean isdisplaytoast=false;
    private boolean ispor=false;
    private FrameLayout videodetails;
    RelativeLayout playlayout;
    private int starttouch;
    String Weburl=null;
    AudioManager audio ;
    private ImageButton playbigbutton;
    //简介模块
    ImageView videoImage;
    TextView videoactor;
    TextView videodirector;
    TextView videolocal;
    TextView videoyear;
    TextView listitemvideosdc;
    private ProtocolTask mTask;
    private int seektovideo=0;
    TextView video_content;
    //加载相关
    private boolean isshare=false;
    private RelativeLayout listview_loading;
    private LoadFailedView listview_loadfailed;
    //
    LinearLayout sharelayout;
    private OnCompletionListener myOnCompleteion=new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer arg0) {
			play(6);
			/*Log.d("playactivity", "onCompletion");
				seekbarwidget.setProgress(0);
				isPlaying=false;
				mediaPlayer.start();
				mediaPlayer.pause();
				playvideobutton.setEnabled(true);
				pause=true;
				if(!islocalmovie)
				{
					seekbarwidget.setEnabled(false);
					playvideobutton.setEnabled(false);
					loading.setVisibility(View.VISIBLE);
				}
				playbigbutton.setVisibility(View.VISIBLE);
				playvideobutton.setBackgroundResource(R.drawable.controller_small_play_drawable);
				isreplay=true;
*/
			
		}
	};
    private Handler myHandler=new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.what){
    			case 0:
    				if(mediaPlayer==null||mediaPlayer.getVideoHeight()<=0)
    					return;
                     seekbarwidget.setProgress((int) ((mediaPlayer.getCurrentPosition()/(double)mediaPlayer.getDuration())*100)); 
                    int alltime=getAllProgressInSecond();
                     int  time=getCurrentProgressInSecond();
                     String curtime;
                     String totaltime;
                     double xxx=alltime/TIME_HOUR;
                     if(alltime/TIME_HOUR>=1)
                     {
                    	 curtime=String.format(hour_timer_format,(int)(time / TIME_HOUR), (int)(time %TIME_HOUR/ TIME_BASE), (int)(time % TIME_BASE));  
                         totaltime=String.format(hour_timer_format,(int)(alltime / TIME_HOUR), (int)(alltime%TIME_HOUR/ TIME_BASE), (int)(alltime % TIME_BASE));    
                     }else
                     {
                     curtime=String.format(mTimerFormat, (int)(time / TIME_BASE), (int)(time % TIME_BASE));  
                     totaltime=String.format(mTimerFormat, (int)(alltime / TIME_BASE), (int)(alltime % TIME_BASE));   
                     }
                     timeText.setText(curtime+"/"+totaltime);
    			break;
    			case 1:
    					int sencendpross=msg.arg1;
						seekbarwidget.setSecondaryProgress(sencendpross);					
    				break;
    		}
    	}
    };
    @Override 
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("playactivity", "onCreate");
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);       
        DisplayMetrics dm = new DisplayMetrics();
        audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        setContentView(R.layout.play);
        closeMusicPlayer();
        initview();
        initdata();   
        addlister();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if(width>height)
        {
        	videodetails.setVisibility(View.GONE);
			hidebar();
			int widthtmp=(int) (height*mediaPlayer.getVideoWidth()/(double)mediaPlayer.getVideoHeight());
			
			 surfaceView.getHolder().setFixedSize(widthtmp, height);
        }
        mTimerFormat = getResources().getString(R.string.timer_format);
        hour_timer_format=getResources().getString(R.string.hour_timer_format);
/*       
        if (savedInstanceState != null) {
			Curtime = savedInstanceState.getInt(CUTDURING);
			String timetextmy=savedInstanceState.getString(CUTDUTEXT);
			islocalmovie=savedInstanceState.getBoolean(ISLOCAL);
			timeText.setText(timetextmy);
		}*/                
        //((TextView)findViewById(R.id.videoname)).setText(playItem.cTitle);


      
		
    }
    private void addlister() {
    	findViewById(R.id.downloadbutton).setOnClickListener(this);
    	findViewById(R.id.playvideobg).setOnClickListener(this);
    	playvideobutton.setOnClickListener(this);
    	if(!(Weburl!=null&&Weburl.trim().length()>0))
    	{
    	surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	//surfaceView.getHolder().setFixedSize(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    	surfaceView.getHolder().setKeepScreenOn(true);
    	myCallBack=new SurfaceCallback();
    	surfaceView.getHolder().addCallback(myCallBack);
    	mediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			
			@Override
			public void onSeekComplete(MediaPlayer arg0) {
				Log.d("playactivity", "onSeekComplete");  
				
			}
		});
    	mediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {			
			@Override
			public void onBufferingUpdate(MediaPlayer arg0, int secendprosstmp) {
				Log.d("playactivity", "onBufferingUpdate");  
				if(isreplay&&secendprosstmp>3||(pause&&(secendprosstmp>seekbarwidget.getProgress())))
				{
					seekbarwidget.setEnabled(true);
					playvideobutton.setEnabled(true);

					loading.setVisibility(View.GONE);
					if(pause)
					{
						playbigbutton.setVisibility(View.VISIBLE);
					}
				}
				secendpross=secendprosstmp;								
				Message msg=new Message();
				msg.what=1;
				msg.arg1=secendpross;
				myHandler.removeMessages(1);
				myHandler.sendMessage(msg);
			}
		});
    	seekbarwidget.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

    		@Override
    		public void onStopTrackingTouch(SeekBar seekbar) {
    				try {
    			
    			int x=seekbar.getProgress();
    			
    		//	Log.d("myzx", "seekbar.getProgress()="+seekbar.getProgress()+"all="+mediaPlayer.getDuration()+"now="+(x*mediaPlayer.getDuration()/100));
    			if(mediaPlayer!=null)
    			{
    				if(x>secendpross+4&&!islocalmovie)
        			{	seekbarwidget.setEnabled(false);
        				if(ispor)       	
        				{
        					Log.d("playactivity", "play__--false1");	
    					playvideobutton.setEnabled(false);
        				}
        				loading.setVisibility(View.VISIBLE);
        			}
    				int setdur=(int) (x*mediaPlayer.getDuration()/100f);
    				seektovideo=setdur;
    				
    				mediaPlayer.seekTo(setdur); 
    				if(islocalmovie&&pause){
    					playbigbutton.setVisibility(View.VISIBLE);
    				}else
    				{
    				playbigbutton.setVisibility(View.GONE);
    				}
    				isChanging=false;
    				myHandler.removeMessages(0);
    				myHandler.sendEmptyMessage(0);
    			}else{
    				myHandler.removeMessages(0);
    				myHandler.sendEmptyMessage(0);
    			}
    			starttouch=0;
    			} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	
    		@Override
    		public void onStartTrackingTouch(SeekBar arg0) {
    			starttouch=seekbarwidget.getProgress();
    			isChanging=true;
    		}

    		@Override
    		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

    		}
    	});
    	mediaPlayer.setOnCompletionListener(myOnCompleteion); 
    	mediaPlayer.setOnInfoListener(new OnInfoListener() {
			
			@Override
			public boolean onInfo(MediaPlayer arg0, int isloading, int arg2) 
			{
				Log.d("playactivity","-------------isloading="+isloading+"arg2="+arg2+"pause="
						+pause);
				if(isloading==MediaPlayer.MEDIA_INFO_BUFFERING_END||isloading==861||isloading==700)
				{	
					Log.d("playactivity","isloading="+isloading+"arg2="+arg2+"pause="
							+pause);
					seekbarwidget.setEnabled(true);
					playvideobutton.setEnabled(true);
					
					loading.setVisibility(View.GONE);
					if(pause)
					{
						playbigbutton.setVisibility(View.VISIBLE);
					}else{
						playbigbutton.setVisibility(View.GONE);
					}
				}else if(isloading==701){
					seekbarwidget.setEnabled(false);
					if(ispor) 
					{
						Log.d("playactivity", "play__--false2");	
					playvideobutton.setEnabled(false);
					}
    				loading.setVisibility(View.VISIBLE);
    				playbigbutton.setVisibility(View.GONE);
				}
				return false;
			}
		});
    	mediaPlayer.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				myOnCompleteion.onCompletion(mediaPlayer);
				return false;
			}
		});
    	}
    	
    	}
	private void initdata() {
			
	        mediaPlayer = new MediaPlayer();
	        timeText.setText("00:00/00:00");
	        playItem=getIntent().getParcelableExtra("sendstring");
	        Weburl=getIntent().getStringExtra("videoUrl");
			if(Weburl!=null&&Weburl.trim().length()>0)
			{
				  if(playItem!=null)
			        {
			        	initVideoDetail();
			        }
				  loading.setVisibility(View.GONE);
				sharelayout.setVisibility(View.VISIBLE);
				playbigbutton.setVisibility(View.VISIBLE);
				 sharelayout.setVisibility(View.VISIBLE);
		    		listview_loading.setVisibility(View.GONE);
		    		seekbarwidget.setEnabled(false);
				return;				
			}
	        if(playItem==null)
	        {
	        	path=getIntent().getStringExtra("sendlocalstring");
	        	mTask = new ProtocolTask(MyApplication.getInstance());
	    		mTask.setListener(this);
	    		mTask.execute(getRequestUrl(), getRequestContent(), tag(), getHeader());
	        	islocalmovie=true;
	    		listview_loadfailed.setButtonVisible(View.GONE);
	    		listview_loading.setVisibility(View.VISIBLE);
	        	loading.setVisibility(View.GONE);
	        }
	        else{
	        	Log.d("playactivity", "play__--false3");	
	        	 playvideobutton.setEnabled(false);  
	        	 seekbarwidget.setEnabled(false);
	        }
	        if(playItem!=null)
	        {
	        	initVideoDetail();
	        }
	}
	private void initVideoDetail() {
		ImageLoader	imageLoader = ImageLoader.getInstance();
    	DisplayImageOptions	options = new DisplayImageOptions.Builder()
		.cacheOnDisc().delayBeforeLoading(200).build();
    	imageLoader.displayImage(playItem.ImagePath, videoImage,
    			options);
    	listitemvideosdc.setText(playItem.playsum);
    	videoactor.setText(getString(R.string.videoactor)+playItem.actors);
    	videodirector.setText(getString(R.string.videodirector)+playItem.directors);
    	videolocal.setText(getString(R.string.videoarea)+playItem.area);
    	videoyear.setText(getString(R.string.videoyear)+playItem.year);
    	video_content.setText(playItem.cDesc);
	}
	private void initview() {
		loading=(RelativeLayout) findViewById(R.id.loading);
		bommonlayout=(RelativeLayout) findViewById(R.id.bommonlayout);
		titlelayout=(RelativeLayout) findViewById(R.id.titlelayout);;
		surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
		playvideobutton=(Button) findViewById(R.id.playvideobutton);
		seekbarwidget=(SeekBar) findViewById(R.id.seekbarplay);
		seekbarwidget.setSecondaryProgress(100);
		timeText=(TextView) findViewById(R.id.playtime);
		videodetails=(FrameLayout) findViewById(R.id.videodetails); 
		playlayout=(RelativeLayout) findViewById(R.id.playlayout); 
		videoImage=(ImageView) findViewById(R.id.videoimage); 
		videoactor=(TextView) findViewById(R.id.videoactor); 
		videodirector=(TextView) findViewById(R.id.videodirector); 
		videolocal=(TextView) findViewById(R.id.videolocal); 
		videoyear=(TextView) findViewById(R.id.videoyear);
		listitemvideosdc=(TextView) findViewById(R.id.listitemvideosdc);
		video_content=(TextView) findViewById(R.id.video_content);
		findViewById(R.id.backactivity).setOnClickListener(this);;
		playbigbutton=(ImageButton) findViewById(R.id.playbigbutton);
		listview_loading=(RelativeLayout) findViewById(R.id.listview_loading);
		listview_loadfailed= (LoadFailedView)findViewById(R.id.listview_loadfailed);
		sharelayout=(LinearLayout) findViewById(R.id.sharelayout);
		sharelayout.setOnClickListener(this);
		playbigbutton.setOnClickListener(this);
		
	}
	@Override
    protected void onResume() {
    	// TODO Auto-generated method stub
		Log.d("playactivity", "onResume");
		if(pause&&!isgohome&&ispor){
			mediaPlayer.start();
			playbigbutton.setVisibility(View.GONE);
			pause=false;
			playvideobutton.setBackgroundResource(R.drawable.controller_small_pause_drawable);			
		}
    	super.onResume();
    	
    	
    }
    private final class SurfaceCallback implements Callback{
		public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
		
		}
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d("playactivity","surfaceCreated");
		       		try {
						if((playItem!=null||path!=null)&&!isgohome)
							{
							Log.d("playactivity", "surfaceCreated-------play");
								String fileName=null;
								if(!islocalmovie){
									fileName =playItem.videuri;
								}else{
									fileName=path;
								}
								if(fileName.startsWith("http")){
									path=fileName;
									play(0);
									seekbarwidget.setSecondaryProgress(0);					
									myHandler.sendEmptyMessageDelayed(1, 1000);
								}else{
									File file = new File(fileName);
									if(file.exists()){
										path=file.getAbsolutePath();
										play(0);
									}else{
										path=null;
										Toast.makeText(PlayActivity.this, R.string.listview_loadfailed,1).show();
									}
								}
							}else{
								mediaPlayer.setDisplay(surfaceView.getHolder());
								if(ispor)
								{
								mediaPlayer.start();
								playbigbutton.setVisibility(View.GONE);
								pause=false;
								playvideobutton.setBackgroundResource(R.drawable.controller_small_pause_drawable);
								}
							}
					} catch (Exception e) {
						e.printStackTrace();
					} 
		       		

		        	

		/*	if(position>0 && path!=null){				
				play(position);
				position=0;
			}*/
		}
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d("playactivity","surfaceDestroyed");
			if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
				
	    		position = mediaPlayer.getCurrentPosition();
	    		mediaPlayer.stop();
	    	}
			isgohome=true;
		}
    }
    private void closeMusicPlayer()
	{
    	Intent intent=new Intent(this,PlayMusicService.class);
    	stopService(intent);
	}
	public  void mediaplay(View v) {
    	switch (v.getId()) {
		case R.id.playbutton:
			String fileName =playItem.videuri;
			//String fileName ="http://www...com/uploads/video/oop.3gp";
			if(fileName.startsWith("http")){
				path=fileName;
				play(0);
			}else{
				File file = new File(Environment.getExternalStorageDirectory(),fileName);
				if(file.exists()){
					path=	file.getAbsolutePath();
					play(0);
				}else{
					path=null;
					Toast.makeText(this, "无文件",1).show();
				}
			}
			break;
		/*case R.id.pausebutton:
			if(mediaPlayer.isPlaying()){
				mediaPlayer.pause();
				pause=true;
			}else{
				if(pause){
					mediaPlayer.start();
					pause=false;
				}
			}
			break;
		case R.id.resetbutton:
			if(mediaPlayer.isPlaying()){
				mediaPlayer.seekTo(0);
			}else{
				if(path!=null){
					play(0);
				}
			}
			break;
		case R.id.stopbutton:
			if(mediaPlayer.isPlaying()){
				mediaPlayer.stop();
			}
			break;*/
		default:
			break;
		}
	}
    
    public void play(int position){
    	Log.d("playactivity", "play-------");
    	
    	try {
    		mediaPlayer.reset();
			mediaPlayer.setDataSource(path);		
			mediaPlayer.setLooping(true);
			mediaPlayer.prepareAsync();
			Display display=getWindowManager().getDefaultDisplay();
			int displayheight=display.getHeight();
			int displaywidth=display.getWidth();
			if(displayheight>displaywidth)
			{
				int heighttmp=(int) (displaywidth*mediaPlayer.getVideoHeight()/(double)mediaPlayer.getVideoWidth());
				
				 surfaceView.getHolder().setFixedSize(displaywidth, heighttmp);
				
			}else{
				int widthtmp=(int) (displayheight*mediaPlayer.getVideoWidth()/(double)mediaPlayer.getVideoHeight());
				
				 surfaceView.getHolder().setFixedSize(widthtmp, displayheight);
			}
			if(Curtime!=-1)
 			{
				position=Curtime;
 				Curtime=-1;				
 			}
			mediaPlayer.setOnPreparedListener(new PreparedListener(position));
			if(position!=6)
			{
			 mTimer = new Timer();    
             mTimerTask = new TimerTask() {    
                 @Override    
                 public void run() {   
                	 Log.d("playactivity", "TimerTask------------");  
                	 try
                	 {
                     if(isChanging==true||mediaPlayer==null||!(mediaPlayer.isPlaying())) {   
                         return;    
                     }  
                	 }catch(Exception e){
                		 return;
                	 }
                     myHandler.removeMessages(0);
                     myHandler.sendEmptyMessage(0);                   
                 }    
             };   
             mTimer.schedule(mTimerTask, 0, 500);
			}
				playvideobutton.setBackgroundResource(R.drawable.controller_small_pause_drawable);

             isPlaying=true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e){
			
		}
    	
    }
    @Override
    protected void onPause() {
    	Log.d("playactivity", "onPause");  
    	if(mediaPlayer!=null&&mediaPlayer.isPlaying())
    	{
    	mediaPlayer.pause();
    	pause=true;
    	playvideobutton.setBackgroundResource(R.drawable.controller_small_play_drawable);
    	}
    	super.onPause();
    }
    @Override
    protected void onStart() {
    	Log.d("playactivity", "onStart");  
    	super.onStart();
    }
    @Override
    protected void onStop() {
    	Log.d("playactivity", "onStop");
    	super.onStop();
    }
    @Override
	protected void onDestroy() {
    	Log.d("playactivity", "onDestroy");
    	mediaPlayer.release();
		mediaPlayer =null;
      	myCallBack=null;
    	if(mTimer!=null)
    	mTimer.cancel();
    	mTimerTask=null;	
    	
		super.onDestroy();
	}
    
    private final  class PreparedListener implements OnPreparedListener{
    	private int position;
    	public PreparedListener(int position) {
			this.position = position;
		}

		@Override
    	public void onPrepared(MediaPlayer mp) {
			Log.d("playactivity", "onPrepared");
			try{
    		mediaPlayer.setDisplay(surfaceView.getHolder());
    		Display display=getWindowManager().getDefaultDisplay();
    		int displayheight=display.getHeight();
    		int displaywidth=display.getWidth();
           	if(displayheight>displaywidth)
    		{
    			int heighttmp=(int) (displaywidth*mediaPlayer.getVideoHeight()/(double)mediaPlayer.getVideoWidth());
    			
    			 surfaceView.getHolder().setFixedSize(displaywidth, heighttmp);
    			
    		}else{
    			int widthtmp=(int) (displayheight*mediaPlayer.getVideoWidth()/(double)mediaPlayer.getVideoHeight());
    			
    			 surfaceView.getHolder().setFixedSize(widthtmp, displayheight);
    		}
			mediaPlayer.start();
			mediaPlayer.pause();	
    			
    			pause=true;
    			seekbarwidget.setProgress(0);
    			seekbarwidget.setEnabled(true);
				playvideobutton.setEnabled(true);
				loading.setVisibility(View.GONE);
				if(playItem.isshare!=1&&isshare)
				{
				playbigbutton.setClickable(false);
				sharelayout.setVisibility(View.VISIBLE);
				}
    			playbigbutton.setVisibility(View.VISIBLE);
			pause=true;
			playvideobutton.setBackgroundResource(R.drawable.controller_small_play_drawable);
    		if(position>0){mediaPlayer.seekTo(position);
    		
    		}
			}catch(Exception e){
				
			}

    	}
    }
/*    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	Log.d("rmyzx", "onSaveInstanceState");
    	super.onSaveInstanceState(outState);
    	choice=1;
    	if(mediaPlayer.isPlaying())
    	{
    	outState.putBoolean(ISLOCAL,islocalmovie);		
    	outState.putString(CUTDUTEXT, timeText.getText().toString());
    	outState.putInt(CUTDURING, mediaPlayer.getCurrentPosition());
    	}else if(pause){
    		outState.putBoolean(ISLOCAL,islocalmovie);	
    		outState.putBoolean(pauseSTRING, pause);
    		outState.putString(CUTDUTEXT, timeText.getText().toString());
    		outState.putInt(CUTDURING, mediaPlayer.getCurrentPosition());
    	}
    }*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
       	super.onConfigurationChanged(newConfig);
       	
       	Display display=getWindowManager().getDefaultDisplay();
		int displayheight=display.getHeight();
		int displaywidth=display.getWidth();
       	if(displayheight>displaywidth)
		{
       		videodetails.setVisibility(View.VISIBLE);
			int heighttmp=(int) (displaywidth*mediaPlayer.getVideoHeight()/(double)mediaPlayer.getVideoWidth());
			showbar();
			 surfaceView.getHolder().setFixedSize(displaywidth, heighttmp);
			
		}else{
			videodetails.setVisibility(View.GONE);
			hidebar();
			int widthtmp=(int) (displayheight*mediaPlayer.getVideoWidth()/(double)mediaPlayer.getVideoHeight());
			
			 surfaceView.getHolder().setFixedSize(widthtmp, displayheight);
		}
    }
    public int getCurrentProgressInSecond() {
        return (int) (mediaPlayer.getCurrentPosition() / 1000L);
    }
    public int getAllProgressInSecond() {
        return (int) (mediaPlayer.getDuration() / 1000L);
    }

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.sharelayout:
			shareoption();			
			break;
			
		case R.id.backactivity:
			if(ispor)
			{//在横屏的时候返回竖屏
		        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
		        mediaPlayer.pause();
		        pause=true;
				playvideobutton.setBackgroundResource(R.drawable.controller_small_play_drawable);
				playbigbutton.setImageResource(R.drawable.playactivitbig);
				playbigbutton.setVisibility(View.VISIBLE);
				ispor=false;
				return;
			}
			
			finish();
			
			break;
		case R.id.playbigbutton:			
		case R.id.playvideobutton:
			if(Weburl!=null&&Weburl.trim().length()>0)
			{
				if(isshare)
				{
				Intent intent =new Intent();
				intent.setClass(PlayActivity.this,PlayWebVideoActivity.class);
				intent.putExtra("videoUrl", Weburl);
				PlayActivity.this.startActivity(intent);
				this.finish();
				}else{
					return;
				}
			}
			if(!ispor)
			{
				ispor=true;
				if(pause)
				mediaPlayer.start();
				pause=false;
				playbigbutton.setVisibility(View.GONE);
				playvideobutton.setBackgroundResource(R.drawable.controller_small_pause_drawable);
				playbigbutton.setImageResource(R.drawable.playbuttonbig2);
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
				return;
			}
			if(mediaPlayer.isPlaying()){
				mediaPlayer.pause();				
				pause=true;
				playvideobutton.setBackgroundResource(R.drawable.controller_small_play_drawable);
				playbigbutton.setVisibility(View.VISIBLE);
			}else{
				if(pause){
					mediaPlayer.start();
					pause=false;
					playbigbutton.setVisibility(View.GONE);
					playvideobutton.setBackgroundResource(R.drawable.controller_small_pause_drawable);
				}else{
					play(0);
				}
			}
			break;
		case R.id.downloadbutton:
			try {
				if(islocalmovie)
				{
					if(!isdisplaytoast)
					{
					Toast.makeText(PlayActivity.this, R.string.download_allready, Toast.LENGTH_SHORT).show();
					isdisplaytoast=true;
					new Handler().postDelayed(new Runnable() {
						
						@Override
						public void run() {
							isdisplaytoast=false;
						}
					}, 2000);
					}
					return;
				}
				File file=new File(PlayDownloadItem.VIDEO_DIR+playItem.cTitle+playItem.videuri.substring(playItem.videuri.lastIndexOf(".")));
				if(file.exists())
				{
					if(!isdisplaytoast)
					{
					Toast.makeText(PlayActivity.this, R.string.download_allready, Toast.LENGTH_SHORT).show();
					isdisplaytoast=true;
					new Handler().postDelayed(new Runnable() {
						
						@Override
						public void run() {
							isdisplaytoast=false;
						}
					}, 2000);
					}
					return;
				}
				Intent intent = new Intent(PlayActivity.this, PlayDownloadService.class);
				intent.putExtra(PlayDownloadItem.DOWN_ITEM, playItem); 
				PlayActivity.this.startService(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}		
			break;
		case R.id.playvideobg:
			if(titlelayout.getVisibility()==View.VISIBLE)
			{
				titlelayout.setVisibility(View.GONE);
				bommonlayout.setVisibility(View.GONE);
			}else{
				titlelayout.setVisibility(View.VISIBLE);
				bommonlayout.setVisibility(View.VISIBLE);
			}
			break;
		default:
			break;
		}
	}
	//隐藏状态栏
	private void hidebar() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	}
	
	//显示状态栏
	private void showbar() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void downloadFile() {
		if (islocalmovie) {
					return;
		}		
		Intent intent = new Intent(PlayActivity.this, PlayDownloadService.class);
		intent.putExtra(PlayDownloadItem.DOWN_ITEM, playItem);
		PlayActivity.this.startService(intent);
	}
	//删除文件
	private void delteteFile(String name) {
		try {
			File file =new File(name);
			if (file.exists())
			{
				if (file.isFile())
				{
					file.delete();
				}
			}
		} catch (Exception e) {
		}				
	}
	private static long getFileSize(File file) throws Exception
	{
		
	long size = 0;
	 if (file.exists()){
	 FileInputStream fis = null;
	 fis = new FileInputStream(file);
	 size = fis.available();
	 }
	 else{
	 file.createNewFile();
	 Log.e("获取文件大小","文件不存在!");
	 }
	 return size;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			audio.adjustStreamVolume(
					AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE,
					AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			audio.adjustStreamVolume(
					AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER,
					AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
			return true;
		case KeyEvent.KEYCODE_BACK:
			if(ispor)
			{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
				if(mediaPlayer!=null&&mediaPlayer.isPlaying())
					mediaPlayer.pause();
				pause=true;
				playbigbutton.setImageResource(R.drawable.playactivitbig);
				playvideobutton.setBackgroundResource(R.drawable.controller_small_play_drawable);
				if(loading.getVisibility()==View.GONE)
					playbigbutton.setVisibility(View.VISIBLE);
				ispor=false;
				return true;
			}
			break;
		case KeyEvent.KEYCODE_HOME:
			isgohome=true;
			break;


		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public void onNoNetworking() {
		listview_loadfailed.setVisibility(View.GONE);
	}
	@Override
	public void onNetworkingError() {
		listview_loading.setVisibility(View.GONE);
		listview_loadfailed.setVisibility(View.VISIBLE);
	}
	@Override
	public void onPostExecute(byte[] bytes) {
					
		try {
			if (bytes != null) {
				JSONObject result = new JSONObject(new String(bytes));
				int status = JsonParse.parseResultStatus(result);
				if (status == 1) {
					VideoItem videoItem = JsonParse.parseCateVideo(result);
					if (videoItem != null) {
						playItem=videoItem;
						initVideoDetail();
						listview_loading.setVisibility(View.GONE);
					} else {
						showNoResultView();
					}
				} else {
					setLoadfailedView();
				}
			} else {
				setLoadfailedView();
			}
		} catch (Exception e) {
			setLoadfailedView();
		}
	}
	private void showNoResultView() {
		listview_loadfailed.setText(MyApplication.getInstance().getString(R.string.data_loadingerr));
		listview_loading.setVisibility(View.GONE);
		listview_loadfailed.setVisibility(View.VISIBLE);
	}



	private void setLoadfailedView() {
		listview_loading.setVisibility(View.GONE);
		listview_loadfailed.setText(MyApplication.getInstance().getString(R.string.data_loadingerr));
		listview_loadfailed.setVisibility(View.VISIBLE);
	}
	private String getRequestUrl() {
    	//http://210.21.246.61:8022/Video/v1.php?qt=Info&name=Laurence%20Anyways
		String myname=path.substring(path.lastIndexOf("/")+1,path.lastIndexOf("."));
		return Constants.APK_URL+"Video/v1.php?qt=Info&name="+Util.encodeContentForUrl(myname);
	}



	private Object getRequestContent() {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<String, String> getHeader() {
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		if (imei == null)
			imei = Util.getIMEI(MyApplication.getInstance());
		if (vcode == null)
			vcode = Util.getVcode(MyApplication.getInstance());
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(MyApplication.getInstance());
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		map.put("channel", channel);
		return map;
	}
	protected String tag() {
		return getClass().getSimpleName();
	}
	private void shareoption() {
		Intent intent =new Intent();
		intent.setClass(this,ShareActivity.class);						
		startActivityForResult(intent, 1000);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK&&data.getBooleanExtra("isok", false))
		{
			 isshare=true;
			 playbigbutton.setClickable(true);
			 sharelayout.setVisibility(View.GONE);
		}
	}
}