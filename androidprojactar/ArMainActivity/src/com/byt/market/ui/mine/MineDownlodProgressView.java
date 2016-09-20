package com.byt.market.ui.mine;




import com.byt.ar.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MineDownlodProgressView extends RelativeLayout {

	private static final int STATE_PAUSED = 1; //该状态只显示进度，不显示mAnimationView                   --- 如下载暂停
	private static final int STATE_ONGOING = 2; //该状态只显示进度和mAnimationView                        --- 如下载中
	private static final int STATE_RUNNING = 3; //该状态不显示mAnimationView，mProgressView进行快速动画          --- 如安装中
	
	private int mState = STATE_PAUSED;
	private ProgressImageView mProgressView;
	private ProgressAnimationImageView mAnimationView;
	private int mProgress = 0;
	private Handler mHandler = new Handler();
	public MineDownlodProgressView(Context context) {
		this(context,null);
		// TODO Auto-generated constructor stub
	}
	
	public MineDownlodProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initViews(context);
	}
	
	private void initViews(Context context){
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
		mAnimationView = new ProgressAnimationImageView(context);
		mAnimationView.setBackgroundResource(R.drawable.mine_download_progress_animition_bg);
		this.addView(mAnimationView, lp);
		
		
		mProgressView = new ProgressImageView(context);
		//mProgressView.setBackgroundResource(R.drawable.mine_download_progress_bg);
		//mProgressView.setImageResource(R.drawable.mine_download_progress_fg);
		this.addView(mProgressView, lp);
	}

	public void setProgress(int progress){
		mProgress = progress;
		this.invalidate();
	
	}
	
	
	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
		onResume();
	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		onPause();
	}

	/**
	 * 停止所有动画，只显示进度
	 * */
	public void pause(){
		if(mState == STATE_PAUSED){
			return;
		}
		mState = STATE_PAUSED;
		updateAfterStateChanged();
	}
	
	/**
	 * 显示进度，并且表示正在进行
	 * */
	public void onGoing(){
		
		if(mState == STATE_ONGOING){
			return;
		}
		mState = STATE_ONGOING;
		updateAfterStateChanged();
	
		}
	
	/**
	 * 不显示进度，只有快速动画
	 * */
	public void running(){
		if(mState == STATE_RUNNING){
			return;
		}
		mState = STATE_RUNNING;
		updateAfterStateChanged();
	}
	
	private void updateAfterStateChanged(){
		switch(mState){
			case STATE_PAUSED:{
				mProgressView.mInnerProgress = -1;
				onPause();
				break;
			}
			case STATE_ONGOING:{
				mProgressView.mInnerProgress = -1;
				mHandler.removeCallbacks(mProgressView.getInnerRunnable());
				mHandler.removeCallbacks(mAnimationView.getInnerRunnable());
				mHandler.post(mAnimationView.getInnerRunnable());
				break;
			}
			case STATE_RUNNING:{
				mProgressView.mInnerProgress = mProgress;
				mHandler.removeCallbacks(mProgressView.getInnerRunnable());
				mHandler.post(mProgressView.getInnerRunnable());
				mHandler.removeCallbacks(mAnimationView.getInnerRunnable());
				break;
			}
		}
	}
	
	private void onResume(){
		updateAfterStateChanged();
	}
	
	private void onPause(){
		mHandler.removeCallbacks(mProgressView.getInnerRunnable());
		mHandler.removeCallbacks(mAnimationView.getInnerRunnable());
	}
	
	
	
	public int getProgress(){
		return mProgress;
	}
	Paint mPaint = new Paint();
	/*@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		final float radius = (float) Math.sqrt(this.getMeasuredWidth() / 2 * this.getMeasuredWidth() / 2 * 2);
		getSectorClip(canvas,this.getMeasuredWidth() / 2,this.getMeasuredHeight() / 2,
				radius,-90,	mProgress * 360 / 100 - 90);
		super.draw(canvas);
	}*/
	
	
	/** 
     * 返回一个扇形的剪裁区 
     * @param canvas  //画笔 
     * @param center_X  //圆心X坐标 
     * @param center_Y  //圆心Y坐标 
     * @param r         //半径 
     * @param startAngle  //起始角度 
     * @param sweepAngle  //终点角度 
     *  
     */  
   private void getSectorClip(Canvas canvas,float center_X,float center_Y,float r,float startAngle,float sweepAngle)  
   {  
       Path path = new Path();  
        //下面是获得一个三角形的剪裁区  
       path.moveTo(center_X, center_Y);  //圆心  
       path.lineTo((float)(center_X+r*Math.cos(startAngle* Math.PI / 180)),   //起始点角度在圆上对应的横坐标  
               (float)(center_Y+r*Math.sin(startAngle* Math.PI / 180)));    //起始点角度在圆上对应的纵坐标  
       path.lineTo((float)(center_X+r*Math.cos(sweepAngle* Math.PI / 180)),  //终点角度在圆上对应的横坐标  
               (float)(center_Y+r*Math.sin(sweepAngle* Math.PI / 180)));   //终点点角度在圆上对应的纵坐标  
       path.close();  
//     //设置一个正方形，内切圆  
       RectF rectF = new RectF(center_X-r,center_Y-r,center_X+r,center_Y+r);  
       //下面是获得弧形剪裁区的方法      
       path.addArc(rectF, startAngle, sweepAngle - startAngle);    
       canvas.clipPath(path);  
   }  
   
    
   
   
	class ProgressImageView extends ImageView {

		public int mInnerProgress = -1;
		private boolean mIncrease  = true;
		private Drawable mNotDownload;
		private Drawable mDownload;
		private Drawable mCurrentProgress;
		public ProgressImageView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			final int imageSize = Utilities.sIconTextureWidth;
			mNotDownload = context.getResources().getDrawable(R.drawable.mine_download_progress_fg);
			mNotDownload.setBounds(0, 0, imageSize, imageSize);
			mDownload = context.getResources().getDrawable(R.drawable.mine_download_progress_bg);
			mDownload.setBounds(0, 0, imageSize, imageSize);
			mCurrentProgress = context.getResources().getDrawable(R.drawable.mine_download_progress_current);
			mCurrentProgress.setBounds(0, 0, (int)(imageSize / 5.5) , (int)(imageSize / 5.5));
		}
		
		private Runnable mInnerRunnable = new Runnable(){        
	        @Override
	        public void run() {
	        	if(mIncrease){
	        		innerSetProgress(mInnerProgress + 5);  
	        	} else {
	        		innerSetProgress(mInnerProgress - 5);  
	        	}
	        	
	        }
	    };
	    
		private void innerSetProgress(int progress) {
			mInnerProgress = progress;
			this.invalidate();
			if (progress >= 100) {
				mIncrease = false;
				//mInnerProgress = 0;
			} else if (progress <= 0){
				mIncrease = true;
				
			}
			mHandler.postDelayed(mInnerRunnable, 200);
		}
	    
	    public Runnable getInnerRunnable(){
	    	return mInnerRunnable;
	    }
		Paint mPaint = new Paint();
		@Override
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			canvas.save();
			final float radius = (float) Math.sqrt(this.getMeasuredWidth() / 2 * this.getMeasuredWidth() / 2 * 2);
			/*getSectorClip(canvas,this.getMeasuredWidth() / 2,this.getMeasuredHeight() / 2,
					radius,-90,	mProgress * 360 / 100 - 90);*/
			getSectorClip(canvas,this.getMeasuredWidth() / 2,this.getMeasuredHeight() / 2,
					radius, - 90,	(mInnerProgress == -1 ? mProgress : mInnerProgress) * 360 / 100 - 90);
			mNotDownload.draw(canvas);
			canvas.restore();
			getSectorClip(canvas,this.getMeasuredWidth() / 2,this.getMeasuredHeight() / 2,
					radius,(mInnerProgress == -1 ? mProgress : mInnerProgress) * 360 / 100 - 90,	360 -90);
			mDownload.draw(canvas);
			canvas.restore();
			drawSector(canvas,this.getMeasuredWidth() / 2,this.getMeasuredHeight() / 2,
					radius, - 90,	(mInnerProgress == -1 ? mProgress : mInnerProgress) * 360 / 100 - 90);
		}
		
		private void drawSector(Canvas canvas,float center_X,float center_Y,float r,float startAngle,float sweepAngle)  
		   {  
			   if((sweepAngle > 35 - 90 && sweepAngle < 55 - 90) || (sweepAngle > 125 - 90 && sweepAngle < 145 - 90)
					   || (sweepAngle > 215 - 90 && sweepAngle < 235 - 90) || (sweepAngle > 305 - 90 && sweepAngle < 325 - 90)){
				  // r -= 8;
			   }
		
		       final float endX = (float)(center_X+r*Math.cos(sweepAngle* Math.PI / 180));
		       final float endY = (float)(center_Y+r*Math.sin(startAngle* Math.PI / 180));
		       canvas.save();
		       mPaint.setColor(Color.RED);
		       //canvas.drawLine(center_X, center_Y, endX, endY, mPaint);
		       //canvas.translate(endX, endY);
		      // mCurrentProgress.draw(canvas);
		       canvas.restore();
		   } 
	}
	
	class ProgressAnimationImageView extends ImageView {

		private int mCurrentAimationProgress = mProgress;
		
		private Runnable mInnerRunnable = new Runnable(){        
	        @Override
	        public void run() {  
	        	mCurrentAimationProgress += 2;
	        	reDraw();
	        }
	    };
	    
	    public Runnable getInnerRunnable(){
	    	return mInnerRunnable;
	    }
	    void reDraw(){
	    	ProgressAnimationImageView.this.invalidate();
	    	if(mCurrentAimationProgress >= 100){
	    		mCurrentAimationProgress = mProgress;
				mHandler.postDelayed(mInnerRunnable, 2000);
			} else {
				mHandler.postDelayed(mInnerRunnable, 50);
			}
	    }
		public ProgressAnimationImageView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			//reDraw();
		}
		
		
		@Override
		public void draw(Canvas canvas) {
			// TODO Auto-generated method stub
			final float radius = (float) Math.sqrt(this.getMeasuredWidth() / 2 * this.getMeasuredWidth() / 2 * 2);
			if(mCurrentAimationProgress < mProgress){
				mCurrentAimationProgress = mProgress;
			}
			final int start = mCurrentAimationProgress * 360 / 100 - 90;
			final int end = mCurrentAimationProgress == mProgress || mCurrentAimationProgress > 97 ? start : start + 10;
			getSectorClip(canvas,this.getMeasuredWidth() / 2,this.getMeasuredHeight() / 2,
					radius,start,	end);
			super.draw(canvas);
		}	
	}
	
}

	
	
	


