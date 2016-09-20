package com.byt.market.mediaplayer.music;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.ImageLoader;
import com.byt.market.bitmaputil.core.assist.FailReason;
import com.byt.market.bitmaputil.core.assist.ImageLoadingListener;
import com.byt.market.data.CateItem;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.MusicDownLoadManageActivity;
import com.byt.market.tools.LogCart;
import com.byt.market.util.FastBlur;

public class PlayMusicActivity extends Activity implements OnClickListener {
	public IPlayback service;
	private LinearLayout titleBar, playProBar;
	private RelativeLayout playerbg;
	private ImageView backBtn, disc, disc_head, playModeBtn, preBtn, playBtn,
			nextBtn, filelistBtn;
	private TextView currFileName, currPlayTime, tocalPlayTime;
	private FrameLayout discshow;
	private SeekBar music_progress;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private RotateAnimation poweronAnimation, poweroffAnimation, discAni;
	private ProgressBar loading;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			try {
				int secondaryProgress = 0;
				if (service.getDownloadFileSize() > 0) {
					secondaryProgress = service.getCacheFileSize() * 100
							/ service.getDownloadFileSize();
					if (secondaryProgress < 100) {
						loading.setVisibility(View.VISIBLE);
					} else {
						loading.setVisibility(View.GONE);
					}
				} else {
					loading.setVisibility(View.GONE);
				}
				music_progress.setSecondaryProgress(secondaryProgress);
				showTime();
				currFileName.setText(service.getCurMusicName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			sendEmptyMessageDelayed(0, 1000);
		};
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.play_music_activity);
		try {
			initView();
			bindService();
			initImageLoader();
			discAnimation();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void initImageLoader() {
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().cacheOnDisc().build();
	}

	private void initView() {
		titleBar = (LinearLayout) findViewById(R.id.titleBar);
		playerbg = (RelativeLayout) findViewById(R.id.playerbg);
		backBtn = (ImageView) findViewById(R.id.backBtn);
		backBtn.setOnClickListener(this);
		currFileName = (TextView) findViewById(R.id.currFileName);
		discshow = (FrameLayout) findViewById(R.id.discshow);
		disc = (ImageView) findViewById(R.id.disc);
		disc.setOnClickListener(this);
		disc_head = (ImageView) findViewById(R.id.disc_head);
		playModeBtn = (ImageView) findViewById(R.id.playModeBtn);
		playModeBtn.setOnClickListener(this);
		preBtn = (ImageView) findViewById(R.id.preBtn);
		preBtn.setOnClickListener(this);
		playBtn = (ImageView) findViewById(R.id.playBtn);
		playBtn.setOnClickListener(this);
		nextBtn = (ImageView) findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(this);
		filelistBtn = (ImageView) findViewById(R.id.filelistBtn);
		filelistBtn.setOnClickListener(this);
		playProBar = (LinearLayout) findViewById(R.id.playProBar);
		currPlayTime = (TextView) findViewById(R.id.currPlayTime);
		music_progress = (SeekBar) findViewById(R.id.music_progress);
		tocalPlayTime = (TextView) findViewById(R.id.tocalPlayTime);
		loading = (ProgressBar) findViewById(R.id.loading);
		music_progress
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						fast();
					}

					@Override
					public void onStartTrackingTouch(SeekBar arg0) {

					}

					@Override
					public void onProgressChanged(SeekBar arg0, int arg1,
							boolean arg2) {

					}
				});
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.backBtn:
			finish();
			break;
		case R.id.disc:

			break;
		case R.id.playModeBtn:
			try {
				int mode = service.getPlayMode();
				mode++;
				if (mode > PlayMusicService.ALL_MODE) {
					mode = PlayMusicService.ORDER_MODE;
				}
				service.setPlayMode(mode);
				setPlayShowMode(mode);
				showPlayMode(mode);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			break;
		case R.id.preBtn:
			try {
				if (service != null) {
					service.previous();
				}
				playBtn.setImageResource(R.drawable.byt_audplayer_pausebtnselector);
				closePlayAnim();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.playBtn:
			try {
				if (service != null) {
					if (service.isPlaying()) {
						service.pause();
						playBtn.setImageResource(R.drawable.byt_audplayer_playbtnselector);
						closePlayAnim();
					} else {
						service.start();
						playBtn.setImageResource(R.drawable.byt_audplayer_pausebtnselector);
						startPlayAnim();
					}
					service.setIsHandlePause();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.nextBtn:
			try {
				if (service != null) {
					service.next();
				}
				playBtn.setImageResource(R.drawable.byt_audplayer_pausebtnselector);
				closePlayAnim();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.filelistBtn:
			entryFileList();
			break;
		}
	}

	public void entryFileList() {
		try {
			if (service.getMusicCateName() == null) {
				Intent intent = new Intent();
				intent.setClass(this, MusicDownLoadManageActivity.class);
				intent.putExtra("isfrome", 1);
				startActivity(intent);
				this.finish();
			} else {
				CateItem cateItem = new CateItem();
				cateItem.id = service.getMusicCateId();
				cateItem.ImagePath = service.getMusicCateLogoUrl();
				cateItem.cTitle = service.getMusicCateName();
				cateItem.cDesc = service.getMusicCateUpdateTime();
				String action = Constants.TOMusicLIST;
				Intent intent = new Intent(action);
				intent.putExtra("app", cateItem);
				intent.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(intent);
				overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
				this.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showTime() {
		try {
			int allTime = service.getDuration();
			int curTime = service.getCurrentPosition();
			int progress = curTime * 100 / allTime;
			int pos = curTime;
			int min = (pos / 1000) / 60;
			int sec = (pos / 1000) % 60;
			if (min <= 60 && min >= 0 && sec >= 0 && sec <= 60) {
				if (sec < 10) {
					currPlayTime.setText("" + min + ":0" + sec);
				} else {
					currPlayTime.setText("" + min + ":" + sec);
				}
				music_progress.setProgress(progress);
			}

			pos = allTime;
			min = (pos / 1000) / 60;
			sec = (pos / 1000) % 60;
			if (min <= 60 && min >= 0 && sec >= 0 && sec <= 60) {
				if (sec < 10) {
					tocalPlayTime.setText("" + min + ":0" + sec);
				} else {
					tocalPlayTime.setText("" + min + ":" + sec);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fast() {
		try {
			int allTime = service.getDuration();
			int curTime = music_progress.getProgress() * allTime / 100;
			service.seek(curTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			System.gc();
			handler.removeMessages(0);
			unbindService(connection);
			closePlayAnim();
			disc_head.clearAnimation();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			imageLoader.cancelDisplayTask(disc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		handler.removeMessages(0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			handler.removeMessages(0);
			handler.sendEmptyMessage(0);
			initReceiver();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			unregisterReceiver(broadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void bindService() {
		Intent intent = new Intent(this, PlayMusicService.class);
		if (false == bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
			finish();
		}
	}

	private ServiceConnection connection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			try {
				service = IPlayback.Stub.asInterface(arg1);
				if (service.isPlaying()) {
					playBtn.setImageResource(R.drawable.byt_audplayer_pausebtnselector);
					startPlayAnim();
				} else {
					playBtn.setImageResource(R.drawable.byt_audplayer_playbtnselector);
					closePlayAnim();
				}
				int mode = service.getPlayMode();
				setPlayShowMode(mode);
				refreshMusicLogo();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName arg0) {
		}

	};

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (service.isPlaying()) {
					playBtn.setImageResource(R.drawable.byt_audplayer_pausebtnselector);
				} else {
					playBtn.setImageResource(R.drawable.byt_audplayer_playbtnselector);
				}
				if (PlayMusicService.START_PLAY_MUSIC_ACTION.equals(intent
						.getAction())) {
					refreshMusicLogo();
				} else if (PlayMusicService.PREPARED_PLAY_MUSIC_ACTION
						.equals(intent.getAction())) {
					startPlayAnim();
				} else if (PlayMusicService.COMPLETE_PLAY_MUSIC_ACTION
						.equals(intent.getAction())) {
					closePlayAnim();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(PlayMusicService.NOTI_PLAY_MUSIC_ACTION);
		filter.addAction(PlayMusicService.NEXT_PLAY_MUSIC_ACTION);
		filter.addAction(PlayMusicService.PREPARED_PLAY_MUSIC_ACTION);
		filter.addAction(PlayMusicService.START_PLAY_MUSIC_ACTION);
		filter.addAction(PlayMusicService.COMPLETE_PLAY_MUSIC_ACTION);
		registerReceiver(broadcastReceiver, filter);
	}

	private void setPlayShowMode(int mode) {
		try {
			switch (mode) {
			case PlayMusicService.ORDER_MODE:
				playModeBtn
						.setImageResource(R.drawable.byt_audplayer_shuffle_bg);
				break;
			case PlayMusicService.SINGLE_MODE:
				playModeBtn
						.setImageResource(R.drawable.byt_audplayer_repeat_one_bg);
				break;
			case PlayMusicService.ALL_MODE:
				playModeBtn
						.setImageResource(R.drawable.byt_audplayer_repeat_all_bg);	
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void showPlayMode(int mode)
	{
		try {
			switch (mode) 
			{
			case PlayMusicService.ORDER_MODE:
				Toast.makeText(this, getString(R.string.mode_shuffle),
						Toast.LENGTH_SHORT).show();
				break;
			case PlayMusicService.SINGLE_MODE:
				Toast.makeText(this, getString(R.string.mode_repeat_one),
						Toast.LENGTH_SHORT).show();
				break;
			case PlayMusicService.ALL_MODE:
				Toast.makeText(this, getString(R.string.mode_repeat_all),
						Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private void refreshMusicLogo() {
		try {
			String logoUrl = null;
			try {
				logoUrl = service.getMusicLogo();
				
				if (logoUrl == null || !(logoUrl.startsWith("http"))) {
					return;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			disc.setTag(disc.getId(), "");
			playerbg.setBackgroundColor(Color.BLACK);
			disc.setImageResource(R.drawable.aud_paly_disc);
			titleBar.setBackgroundColor(Color.BLACK);
			imageLoader.displayImage(logoUrl, disc, options,
					new ImageLoadingListener() {

						@Override
						public void onLoadingStarted() {
						}

						@Override
						public void onLoadingFailed(FailReason failReason) {
						}

						@Override
						public void onLoadingComplete(Bitmap loadedImage) {
							try {
								if (loadedImage != null) {
									Bitmap bitmap1 = BitmapFactory
											.decodeResource(
													getResources(),
													R.drawable.aud_paly_disc_empty);
									Bitmap bitmap2 = zoomBitmap(loadedImage,
											bitmap1.getWidth() * 0.6325,
											bitmap1.getHeight() * 0.6325);
									Bitmap bitmap3 = createCircleImage(bitmap2);
									Bitmap destBitmap = createBitmap(bitmap1,
											bitmap3);

									disc.setImageBitmap(destBitmap);
									bitmap1.recycle();
									bitmap2.recycle();
									bitmap3.recycle();
									//
									blur(loadedImage, playerbg);
									Bitmap bit_ti = BitmapFactory
											.decodeResource(getResources(),
													R.drawable.aud_ti);
									Bitmap bit_ti_dest = createBitmap(
											loadedImage, bit_ti);
									blur(bit_ti_dest, titleBar);
									loadedImage.recycle();
									bit_ti.recycle();
									bit_ti_dest.recycle();
									System.gc();
								} else {
									titleBar.setBackgroundColor(Color.BLACK);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

						}

						@Override
						public void onLoadingCancelled() {

						}

					});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private Bitmap zoomBitmap(Bitmap src, double d, double e) {
		Bitmap resizedBitmap = null;
		try {
			if (src == null) {
				return null;
			}
			int w = src.getWidth();
			int h = src.getHeight();

			float scaleWidth = ((float) d) / w;
			float scaleHeight = ((float) e) / h;
			Matrix m = new Matrix();
			m.postScale(scaleWidth, scaleHeight);
			resizedBitmap = Bitmap.createBitmap(src, 0, 0, w, h, m, true);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return resizedBitmap;
	}

	public static Bitmap createCircleImage(Bitmap bitmap) {
		Bitmap output = null;
		try {
			output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
					Config.ARGB_8888);
			Canvas canvas = new Canvas(output);

			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());
			final RectF rectF = new RectF(rect);
			final float roundPx = bitmap.getWidth() / 2;

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	private Bitmap createBitmap(Bitmap src, Bitmap watermark) {
		Bitmap newb = null;
		try {
			if (src == null) {
				return null;
			}

			int w = src.getWidth();
			int h = src.getHeight();
			int ww = watermark.getWidth();
			int wh = watermark.getHeight();

			newb = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			Canvas cv = new Canvas(newb);
			cv.drawBitmap(src, 0, 0, null);
			cv.drawBitmap(watermark, (w - ww) / 2, (h - wh) / 2, null);
			cv.save(Canvas.ALL_SAVE_FLAG);
			cv.restore();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newb;
	}

	private void blur(Bitmap bkg, View view) {
		try {
			int dst_w = playerbg.getWidth();
			int dst_h = playerbg.getHeight();

			float scaleFactor = 8;
			float radius = 20;

			Bitmap bit = zoomBitmap(bkg, dst_w, dst_h);
			Bitmap overlay = Bitmap.createBitmap(
					(int) (view.getMeasuredWidth() / scaleFactor),
					(int) (view.getMeasuredHeight() / scaleFactor),
					Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(overlay);
			canvas.translate(-view.getLeft() / scaleFactor, -view.getTop()
					/ scaleFactor);
			canvas.scale(1 / scaleFactor, 1 / scaleFactor);
			Paint paint = new Paint();
			paint.setFlags(Paint.FILTER_BITMAP_FLAG);
			canvas.drawBitmap(bit, 0, 0, paint);
			overlay = FastBlur.doBlur(overlay, (int) radius, true);
			overlay = lumHandleImage(overlay);
			view.setBackgroundDrawable(new BitmapDrawable(overlay));
			bit.recycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Bitmap lumHandleImage(Bitmap bm) {
		Bitmap bmp = null;
		try {
			bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(),
					Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);
			Paint paint = new Paint();
			paint.setAntiAlias(true);

			ColorMatrix cm = new ColorMatrix();

			cm.set(new float[] { (float) 0.8, 0, 0, 0, 1, 0, (float) 0.8, 0, 0,
					1, 0, 0, (float) 0.72, 0, 1, 0, 0, 0, 1, 0 });
			ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
			paint.setColorFilter(cf);
			canvas.drawBitmap(bm, 0, 0, paint);
			bm.recycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bmp;
	}

	private void discAnimation() {
		try {
			poweronAnimation = (RotateAnimation) AnimationUtils.loadAnimation(
					this, R.anim.disc_head_on);
			poweroffAnimation = (RotateAnimation) AnimationUtils.loadAnimation(
					this, R.anim.disc_head_off);
			discAni = (RotateAnimation) AnimationUtils.loadAnimation(this,
					R.anim.disc);
			AnimationListener animationListener = new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					poweronAnimation.setFillAfter(true);
					poweroffAnimation.setFillAfter(true);
					discAni.setFillAfter(true);
				}
			};
			poweronAnimation.setAnimationListener(animationListener);
			poweroffAnimation.setAnimationListener(animationListener);
			discAni.setAnimationListener(animationListener);
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}

	private void startPlayAnim() {
		closePlayAnim();
		disc_head.startAnimation(poweronAnimation);
		disc.startAnimation(discAni);
	}

	private void closePlayAnim() {
		disc_head.clearAnimation();
		disc.clearAnimation();
		poweronAnimation.cancel();
		discAni.cancel();
		poweroffAnimation.cancel();
		disc_head.startAnimation(poweroffAnimation);
	}
}
