package com.byt.market.qiushibaike.news;

import java.io.File;
import java.io.FileInputStream;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.qiushibaike.download.DownloadInfo;
import com.byt.market.tools.LogCart;
import com.byt.market.util.StringUtil;
import com.byt.market.view.gifview.GifDecoderView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlayGifActivity extends Activity {
	GifDecoderView mGifView;
	private TextView dlpercent;
	String imageUrl;
	int msid = 0;
	String savePath;
	public static final int LOADING = 5;
	public static final int SUCCESSED = 1;
	DownloadInfo downloadItem = null;
	private static final int HANDLER_MSG_UPDATE_PROGRESS = 2;
	// private ProgressDialog dialog;
	private ProtocolTask mTask;//浏览量  请求接口
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUCCESSED:
				try {
					loading_icon.setVisibility(View.GONE);
					dlpercent.setVisibility(View.GONE);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case HANDLER_MSG_UPDATE_PROGRESS:
				try {
					if (downloadItem != null) {
						String temp = String
								.valueOf(((double) downloadItem.curSize)
										/ downloadItem.allSize * 100);
						if (temp.contains(".")) {
							temp = temp.substring(0, temp.indexOf(".")) + "%";
						}
						dlpercent.setText(temp);
						removeMessages(HANDLER_MSG_UPDATE_PROGRESS);
						sendEmptyMessageDelayed(HANDLER_MSG_UPDATE_PROGRESS, 1000);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				break;
			case LOADING:
				try {
					removeMessages(HANDLER_MSG_UPDATE_PROGRESS);
					dlpercent.setText("100%");
					mGifView.playGif(new FileInputStream(savePath), this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}

		};

	};
	private ProgressBar loading_icon;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.joke_details_gif);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			Intent intent = this.getIntent();
			Bundle bundle = intent.getExtras();
			imageUrl = bundle.getString("joke_image_path");
			msid = bundle.getInt("sid");
			LogCart.Log("gif-----------url-----"+imageUrl);
			mGifView = (GifDecoderView) findViewById(R.id.gif);
			
			int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
			int height=getWindowManager().getDefaultDisplay().getHeight();
			mGifView.setWidthHeight(height,screenWidth);
			 getWindow().getDecorView().setPadding(0, 0, 0, 0);
			ViewGroup.LayoutParams lp = mGifView.getLayoutParams();
			lp.width = screenWidth;
			lp.height = LayoutParams.WRAP_CONTENT;
			mGifView.setLayoutParams(lp);

			mGifView.setMaxWidth(screenWidth);
			mGifView.setMaxHeight(screenWidth * 6);
			loading_icon = (ProgressBar) findViewById(R.id.loading_icon);
			loading_icon.setVisibility(View.GONE);

			dlpercent = (TextView) findViewById(R.id.dlpercent);
			dlpercent.setVisibility(View.GONE);
			downloadGif();
			mTask = new ProtocolTask(this);
    		mTask.execute(Constants.JOKE_COMMENT_URL+"?qt=view&sid="+msid,null,null,null);
			
		}
		private void downloadGif() {
			try {
				if (!(imageUrl.startsWith("http"))) {
					loading_icon.setVisibility(View.GONE);
					dlpercent.setVisibility(View.GONE);
					return;
				}
					downloadInBackground(imageUrl);
					loading_icon.setVisibility(View.VISIBLE);
					dlpercent.setVisibility(View.VISIBLE);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		private void downloadInBackground(String downloadUrl) {
			String saveDir = Environment.getExternalStorageDirectory()
					+ Constants.JOKE_FOLDER;
			if (downloadUrl != null) {
				File dir = new File(saveDir);
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			savePath = saveDir + StringUtil.md5Encoding(downloadUrl)
					+ downloadUrl.substring(downloadUrl.lastIndexOf("."));
			if (new File(savePath).exists()) {
				handler.removeMessages(LOADING);
				handler.sendEmptyMessage(LOADING);
			} else {
				downloadItem = new DownloadInfo(this, downloadUrl, savePath);
				new Thread(new DownloadTask(downloadItem)).start();
			}

		}
		class DownloadTask implements Runnable {
			private DownloadInfo downloadItem;

			public DownloadTask(DownloadInfo downloadItem) {
				this.downloadItem = downloadItem;
			}

			@Override
			public void run() {
				try {
					handler.sendEmptyMessageDelayed(HANDLER_MSG_UPDATE_PROGRESS, 1000);
					downloadItem.downloadFile();
					handler.removeMessages(HANDLER_MSG_UPDATE_PROGRESS);
					if (downloadItem.downloadState == DownloadInfo.DOWNLOAD_SUCCESS) {
						handler.removeMessages(LOADING);
						handler.sendEmptyMessage(LOADING);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		@Override
		protected void onStop() {
			// TODO Auto-generated method stub
			super.onStop();
			try {
				if(downloadItem!=null){
					downloadItem.isStop = true;
				}
				mGifView.recycleGif();
				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
}
