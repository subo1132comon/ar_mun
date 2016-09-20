package com.byt.market.mediaplayer.ui;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.MyFaviorateActivity;
import com.byt.market.activity.SoftwareUninstallActivity;
import com.byt.market.activity.SoftwareUninstallActivity.AppEntry;
import com.byt.market.adapter.ImageAdapter.ViewHolder;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.mediaplayer.data.PlayDownloadItem;
import com.byt.market.mediaplayer.service.PlayDownloadService;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.StringUtil;
import com.byt.market.util.TextUtil;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.LoadFailedView;


/**
 * 正在下载列表
 * @author qiuximing
 *
 */
public class MusicDowningFragment  extends BaseUIFragment implements OnClickListener {
	private List<PlayDownloadItem> downedItems = new ArrayList<PlayDownloadItem>();
	private CusPullListView listviewed;
	private RelativeLayout loadinged;
	private LoadFailedView loadfaided;
	private MusicDowningAdapter adaptered;
	private Button downall,cancelall;
	@Override
	public void onDestroy() {
		super.onDestroy();
		handler.removeMessages(0);
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.listview, container, false);
		
		initViews(view);
		initListeners(view);						
		initData();	
		return view;
	}
	private void initData() {
		adaptered=new MusicDowningAdapter();
		listviewed.setAdapter(adaptered);
		new LoadDataTask().execute();
	}

	private void initListeners(View view) {
		downall.setOnClickListener(this);
		cancelall.setOnClickListener(this);
	}

	private void initViews(View view) {
		
		loadfaided=(LoadFailedView) view.findViewById(R.id.listview_loadfailed);
		loadinged=(RelativeLayout) view.findViewById(R.id.listview_loading);
		listviewed=(CusPullListView) view.findViewById(R.id.listview);
		View view2=LayoutInflater.from(MusicDowningFragment.this.getActivity()).inflate(R.layout.musicdowning_header, null);
		downall=(Button) view2.findViewById(R.id.goonalltask);
		cancelall=(Button) view2.findViewById(R.id.cancelalltask);
		listviewed.addHeaderView(view2);
		listviewed.setVisibility(View.GONE);
		loadfaided.setVisibility(View.GONE);
		loadfaided.setButtonVisible(View.GONE);
		loadfaided.setText(getString(R.string.nodata));
	}
	class  MusicDowningAdapter extends BaseAdapter{		
		@Override
		public int getCount() {
			return downedItems.size();
		}

		@Override
		public PlayDownloadItem getItem(int arg0) {
			return downedItems.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
			final PlayDownloadItem playdownloadItem=downedItems.get(position);
			ViewHolder holder;
			DowningHolder downingHolder=null;
			if (arg1 == null) {
				downingHolder = new DowningHolder();
				arg1 = LayoutInflater.from(MusicDowningFragment.this.getActivity()).inflate(R.layout.listitem_playdownlist,
						null);
				downingHolder.downitemname=(TextView) arg1.findViewById(R.id.downingitemname);
				downingHolder.downloadclosebutton=(ImageButton) arg1.findViewById(R.id.downloadingclosebutton);
				downingHolder.downloadProgressBar=(ProgressBar) arg1.findViewById(R.id.downloadingProgressBar);
				downingHolder.downsize=(TextView) arg1.findViewById(R.id.downingsize);
				arg1.setTag(downingHolder);
			} else {
				downingHolder = (DowningHolder) arg1.getTag();
			}
			if(playdownloadItem.name!=null)
			downingHolder.downitemname.setText(playdownloadItem.name);
			
			if(playdownloadItem.type == 2){
				downall.setVisibility(View.GONE);
				cancelall.setVisibility(View.GONE);
			}
			if(!(playdownloadItem.isPause))
			{
//			String lastSizeStr = StringUtil
//						.formageDownloadSize(playdownloadItem.cursize);
//				String dSizeStr = StringUtil
//						.formageDownloadSize(playdownloadItem.length);
//				String formatStr = MusicDowningFragment.this.getActivity().getString(
//						R.string.down_progress_detail, lastSizeStr,dSizeStr);
//				downingHolder.downsize.setText(formatStr);
				String lastSizeStr = StringUtil
						.formageDownloadSize(playdownloadItem.cursize);
				String dSizeStr = StringUtil
						.formageDownloadSize(playdownloadItem.length);
				if(playdownloadItem.type == 2){
					dSizeStr = "?";
				}
				String formatStr = MusicDowningFragment.this.getActivity().getString(
						R.string.down_progress_detail, lastSizeStr,
						dSizeStr);
				downingHolder.downsize.setText(formatStr);
				
			}else{
				downingHolder.downsize.setText(R.string.paused);
			}
			/*if(isDownloading(playdownloadItem.state)||playdownloadItem.dSize > 0 )
			{*/
				 long totleSize = playdownloadItem.length;
				 int percent = totleSize == 0 ? 100
						: (int) ((playdownloadItem.cursize * 100) / totleSize);
				downingHolder.downloadProgressBar.setVisibility(View.VISIBLE);
				downingHolder.downloadProgressBar.setProgress(percent);
			/*}else{
				downingHolder.downloadProgressBar.setVisibility(View.GONE);
			}*/
				downingHolder.downloadclosebutton.setVisibility(View.VISIBLE);
			downingHolder.downloadclosebutton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					new AlertDialog.Builder(MusicDowningFragment.this.getActivity())
					.setTitle(R.string.dialog_retry_title)  
					.setMessage(MusicDowningFragment.this.getActivity().getString(R.string.expend_child5_remove_msg))
					.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							PlayDownloadService.deletedownfile(playdownloadItem);
							//MusicDowningFragment.this.getActivity().sendBroadcast(new Intent(DownloadManager.ACTION_NOTIFICATION_CLICKED));
							if(playdownloadItem!=null&&playdownloadItem.type == 2){
								playdownloadItem.downloadManager.remove(playdownloadItem.Id);
							}
						}
					})  
					.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							arg0.dismiss();
							
						}
					})  					
					.show();  
				}
			});
			
			
			return arg1;
		}
		public class DowningHolder {
			public ImageButton downloadclosebutton;//删除下载按钮
			public TextView downitemname;//下载item名字
			public ProgressBar downloadProgressBar;//下载item进度条
			public TextView downsize;//下载大小

		}
	}
	/**
	 * 是否属于正在下载列表
	 * 
	 * @param state
	 * @return
	 */
	private boolean isDownloading(int state) {
		if (state < AppItemState.STATE_DOWNLOAD_FINISH
				&& state > AppItemState.STATE_IDLE) {
			return true;
		}
		return false;
	}
	class LoadDataTask extends AsyncTask<Void, Void, List<PlayDownloadItem>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected List<PlayDownloadItem> doInBackground(Void... params) {
			Iterator<PlayDownloadItem> it=PlayDownloadService.PlayDownloadItems.values().iterator();
			List<PlayDownloadItem> list=new ArrayList<PlayDownloadItem>();
			while (it.hasNext()) {
				list.add(it.next());
			}
//			for(PlayDownloadItem playdown:PlayDownloadService.PlayDownloadItems)
//			{
//				int i=0;
//				i++;
//			}
//			List<PlayDownloadItem> entries=new ArrayList<PlayDownloadItem>();
//				entries.addAll(PlayDownloadService.playDownloadItemsall);
				return list;
		}

		@Override
		protected void onPostExecute(List<PlayDownloadItem> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == null || result.size() == 0) {
					loadfaided.setVisibility(View.VISIBLE);
					return;
			}	
			downedItems.addAll(result);
			adaptered.notifyDataSetChanged();
			listviewed.setVisibility(View.VISIBLE);
			loadinged.setVisibility(View.GONE);
			handler.sendEmptyMessageDelayed(0,1000);

		}

	}
	@Override
	public void onClick(View arg0) {
		switch(arg0.getId())
		{
		case R.id.goonalltask://开始，暂停按钮
			if(downall.getText().equals(MyApplication.getInstance().getString(R.string.txt_download_all_start)))
			{
				PlayDownloadService.startAll();
				downall.setText(R.string.txt_download_all_pause);
			}else if(downall.getText().equals(MyApplication.getInstance().getString(R.string.txt_download_all_pause))){
				PlayDownloadService.pauseAll();
				downall.setText(R.string.txt_download_all_start);
			}
			break;
		case R.id.cancelalltask://取消所以下载任务
			new AlertDialog.Builder(MusicDowningFragment.this.getActivity())
			.setTitle(R.string.dialog_retry_title)  
			.setMessage(MusicDowningFragment.this.getActivity().getString(R.string.dialog_delete_confirm_tips))
			.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {						
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					PlayDownloadService.deleteAll();
				}
			})  
			.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					arg0.dismiss();
					
				}
			})  					
			.show();  
			
			break;
		}
	}
	/** android service   end**/	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try{
				boolean pause=false;
				for (int i=0;i<downedItems.size();i++) {
					PlayDownloadItem playloaditem=downedItems.get(i);
					String downloadUrl = null;
					String savaFIlePath=null;
					if(playloaditem.type==1){
						 downloadUrl=playloaditem.musicuri;
						 savaFIlePath=PlayDownloadItem.MUSIC_DIR + playloaditem.name
									+ downloadUrl.substring(downloadUrl.lastIndexOf("."));
					}else{
						 downloadUrl=playloaditem.adesc;
						 savaFIlePath=PlayDownloadItem.VIDEO_DIR + playloaditem.name
									+ downloadUrl.substring(downloadUrl.lastIndexOf("."));
					}
					PlayDownloadItem downloadItem = PlayDownloadService.PlayDownloadItems
							.get(savaFIlePath);
					if (downloadItem != null) {
						playloaditem.setCursize(downloadItem.getCursize());	
						if(downloadItem.isPause)
						{
							pause=true;
						}else{
						}
					}else{
						downedItems.remove(playloaditem);
						i--;
					}
				}
				if(pause)
				{
					downall.setText(R.string.txt_download_all_start);
				}
				else{
					downall.setText(R.string.txt_download_all_pause);
				}
				adaptered.notifyDataSetChanged();
				if(downedItems.size()>0)
				sendEmptyMessageDelayed(0, 1000);
				else{
					loadfaided.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

}
