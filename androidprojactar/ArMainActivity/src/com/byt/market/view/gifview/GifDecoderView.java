package com.byt.market.view.gifview;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;

import com.byt.market.qiushibaike.JokeDetailsActivity;

public class GifDecoderView extends ImageView {

	private boolean isAni;
	private GifPlayer gp;
	private Bitmap bm;
	private Handler handler ;
	private int width=0;
	private int height=0;
	private Runnable playFrame = new Runnable() {

		@Override
		public void run() {
			if (null != bm && !bm.isRecycled()) {
				if(getDrawable()==null){
					handler.removeMessages(JokeDetailsActivity.SUCCESSED);
					handler.sendEmptyMessage(JokeDetailsActivity.SUCCESSED);
				}
				ViewGroup.LayoutParams lp = GifDecoderView.this.getLayoutParams();
				float widthtmp=width;
				float widthbm=bm.getWidth();
				float heightbm=bm.getHeight();
				 
				lp.height =(int) ((widthtmp/widthbm)*bm.getHeight());
				lp.width = width;
				
				
				GifDecoderView.this.setLayoutParams(lp);
				GifDecoderView.this.setImageBitmap(bm);
			}
		}
	};

//	//add by bobo
//	protected void onDraw(android.graphics.Canvas canvas) {
//		try {
//			super.draw(canvas);
//		} catch (Exception e) {
//			Log.d("nnlog", "draw--"+e);
//		}
//	};
	
	public GifDecoderView(Context context, InputStream is) {
		super(context);
//		playGif(is);
	}
	public void setWidthHeight(int height,int width) {
		this.width=width;
		this.height=height;
	}
	public GifDecoderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GifDecoderView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GifDecoderView(Context context) {
		super(context);
	}

	public void playGif(final InputStream is,final Handler handler) {
		this.handler=handler;
		new Thread() {
			public void run() {
				try {
					gp = new GifPlayer();
					gp.read(is);
					isAni = true;
					final int frameCount = gp.getFrameCount();
					final int loopCount = gp.getLoopCount();
					int repetitionCounter = 0;
					do {
						for (int i = 0; i < frameCount; i++) {
							if (isStop) {
								return;
							}
							bm = gp.getFrame(i);
							int t = gp.getDelay(i);
							handler.post(playFrame);
							try {
								Thread.sleep(t);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (0 != loopCount) {
								repetitionCounter++;
							}

						}
						if (isStop) {
							return;
						}
					} while (true);
				} catch (Exception e) {
					e.printStackTrace();
				}

			};
		}.start();
	}

	private boolean isStop = false;

	public void recycleGif() {
		try {
			isStop = true;
			if (null != bm && !bm.isRecycled()) {
				bm.recycle();
			}
			gp.clearImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
