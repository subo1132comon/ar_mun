package com.byt.market.mediaplayer;

import com.byt.ar.R;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.bitmaputil.core.display.RoundedBitmapDisplayer;
import com.byt.market.mediaplayer.data.VideoItem;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoDetailActivity extends Activity {
	ImageView videoImage;
    TextView videoactor;
    TextView videodirector;
    TextView videolocal;
    TextView videoyear;
    TextView listitemvideosdc;
    VideoItem playItem;
    TextView video_content;
    TextView videonameb;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_detail);
		playItem=getIntent().getParcelableExtra("sendstring");
		initview();
		initdata();
	}

	private void initdata() {
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
    	videonameb.setText(playItem.cTitle);
	}

	private void initview() {
		videoImage=(ImageView) findViewById(R.id.videoimage); 
		videoactor=(TextView) findViewById(R.id.videoactor); 
		videodirector=(TextView) findViewById(R.id.videodirector); 
		videolocal=(TextView) findViewById(R.id.videolocal); 
		videoyear=(TextView) findViewById(R.id.videoyear);
		video_content=(TextView) findViewById(R.id.video_content);
		listitemvideosdc=(TextView) findViewById(R.id.listitemvideosdc);
		videonameb=(TextView) findViewById(R.id.videoname);
	}

}
