package com.byt.market.mediaplayer.ui;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.MyFaviorateActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.adapter.ImageAdapter.ViewHolder;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.bitmaputil.core.display.ZoomAndRounderBitmapDisplayer;
import com.byt.market.data.BigItem;
import com.byt.market.data.CateItem;
import com.byt.market.data.RingItem;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.MPConstants;
import com.byt.market.mediaplayer.PlayActivity;
import com.byt.market.mediaplayer.data.PlayDownloadItem;
import com.byt.market.mediaplayer.music.PlayMusicActivity;
import com.byt.market.mediaplayer.music.PlayMusicService;
import com.byt.market.mediaplayer.service.PlayDownloadService;
import com.byt.market.mediaplayer.tv.NewTVListFragment;
import com.byt.market.mediaplayer.ui.MusicDowningFragment.LoadDataTask;
import com.byt.market.mediaplayer.ui.MusicDowningFragment.MusicDowningAdapter;
import com.byt.market.mediaplayer.ui.MusicDowningFragment.MusicDowningAdapter.DowningHolder;
import com.byt.market.tools.DownLoadVdioapkTools;
import com.byt.market.ui.CateListViewFragment;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.Singinstents;
import com.byt.market.util.StartVidioutil;
import com.byt.market.util.StringUtil;
import com.byt.market.util.Util;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.LoadFailedView;

/**
 * 下载完成列表
 * @author qiuximing
 *
 */
public class MusicDownedFragment extends BaseUIFragment {
	private List<Mysic> downItems = new ArrayList<Mysic>();
	private CusPullListView listview;
	private RelativeLayout loading;
	private LoadFailedView loadfaid;
	private MusicDownAdapter adapter;
	Handler mhandHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				new MusicDownLoadDataTask().execute();
				break;
			}
		}
	};
	BroadcastReceiver downreBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			mhandHandle.removeMessages(0);
			mhandHandle.sendEmptyMessage(0);
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.listview, container, false);
		getActivity().registerReceiver(downreBroadcastReceiver, new IntentFilter("com.byt.music.downcomplet"));
		initViews(view);
		initListeners(view);						
		initData();	
		return view;
	}

	private void initData() {
		adapter=new MusicDownAdapter();
		listview.setAdapter(adapter);
		new MusicDownLoadDataTask().execute();

	}

	private void initListeners(View view) {
		
	}

	private void initViews(View view) {
		loadfaid=(LoadFailedView) view.findViewById(R.id.listview_loadfailed);
		loading=(RelativeLayout) view.findViewById(R.id.listview_loading);
		listview=(CusPullListView) view.findViewById(R.id.listview);

		listview.setVisibility(View.GONE);
		loadfaid.setVisibility(View.GONE);
		loadfaid.setButtonVisible(View.GONE);
		loadfaid.setText(getString(R.string.nodata));
	}
	class Mysic {
		String musicname;
		String musicpath;
		String spname;
		long length;
		int state;//1 音乐 2 视频 3 小说
		
		public Mysic(String musicpathtmp,long lengthtmp) {
			this.musicname=musicpathtmp.substring(musicpathtmp.lastIndexOf("/")+1);
			this.musicpath=musicpathtmp;
			this.length=lengthtmp;
			
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}
	}
	class MusicDownLoadDataTask extends AsyncTask<Void, Void, List<Mysic>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected List<Mysic> doInBackground(Void... params) {
			File file = new File(PlayDownloadItem.MUSIC_DIR);
			//File file = new File(PlayDownloadItem.NOVEL_DIR);
			File[] f = file.listFiles();
			List<Mysic> list = new ArrayList<MusicDownedFragment.Mysic>();
			if (f != null) {
				for (int i = 0; i < f.length; i++) {
					File file2 = new File(f[i].getPath());
					long blockSize = 0;
					try {
						if (file2.isDirectory()) {
							blockSize = getFileSizes(file2);
						} else {
							blockSize = getFileSize(file2);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					Mysic mysic = new Mysic(f[i].getPath(), blockSize);
					if(PlayDownloadService.PlayDownloadItems.size()>0)
					{
						String savepaht=PlayDownloadItem.MUSIC_DIR+mysic.musicname;
						//String savepaht=PlayDownloadItem.NOVEL_DIR+mysic.musicname;
						PlayDownloadItem downloadItem =PlayDownloadService.PlayDownloadItems.get(savepaht);
						if(downloadItem!=null)
						{
							continue;
						}
					}
					mysic.setState(1);
					list.add(mysic);				

				}
			}
			File filevedio = new File(PlayDownloadItem.VIDEO_DIR);
			File[] fvidoe = filevedio.listFiles();
			if (fvidoe != null) {
				for (int i = 0; i < fvidoe.length; i++) {
					File file2 = new File(fvidoe[i].getPath());
					long blockSize = 0;
					try {
						if (file2.isDirectory()) {
							blockSize = getFileSizes(file2);
						} else {
							blockSize = getFileSize(file2);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					Mysic mysic = new Mysic(fvidoe[i].getPath(), blockSize);
					if(PlayDownloadService.PlayDownloadItems.size()>0)
					{
						String savepaht=PlayDownloadItem.VIDEO_DIR+mysic.musicname;
						PlayDownloadItem downloadItem =PlayDownloadService.PlayDownloadItems.get(savepaht);
						if(downloadItem!=null)
						{
							continue;
						}
					}
					mysic.setState(2);
					list.add(mysic);

				}
			}
			return list;
		}
		@Override
		protected void onPostExecute(List<Mysic> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == null || result.size() == 0) {
					loadfaid.setVisibility(View.VISIBLE);
					return;
			}	
			loadfaid.setVisibility(View.GONE);
			downItems.clear();
			downItems.addAll(result);
			adapter.notifyDataSetChanged();
			listview.setVisibility(View.VISIBLE);
			loading.setVisibility(View.GONE);
			

		}

	}
	class  MusicDownAdapter extends BaseAdapter{		
		@Override
		public int getCount() {
			return downItems.size();
		}

		@Override
		public Mysic getItem(int arg0) {
			return downItems.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
			final Mysic playdownloadItem=downItems.get(position);
			ViewHolder holder;
			DownedHolder downingHolder=null;
			if (arg1 == null) {
				downingHolder = new DownedHolder();
				arg1 = LayoutInflater.from(MusicDownedFragment.this.getActivity()).inflate(R.layout.listitem_playdownedlist,
						null);
				downingHolder.downitemname=(TextView) arg1.findViewById(R.id.downitemname);
				downingHolder.downloadclosebutton=(ImageButton) arg1.findViewById(R.id.downloadclosebutton);
				downingHolder.downsize=(TextView) arg1.findViewById(R.id.downsize);
				downingHolder.playdownedlayout=(TextView) arg1.findViewById(R.id.playdownedlayout);
				arg1.setTag(downingHolder);
			} else {
				downingHolder = (DownedHolder) arg1.getTag();
			}
			downingHolder.downitemname.setText(playdownloadItem.musicname);
			downingHolder.downsize.setText(StringUtil
					.formageDownloadSize(playdownloadItem.length));
			//点击正在下载界面的删除进入
			downingHolder.downloadclosebutton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					new AlertDialog.Builder(MusicDownedFragment.this.getActivity())
					.setTitle(R.string.dialog_retry_title)  
					.setMessage(MusicDownedFragment.this.getActivity().getString(R.string.dialog_delete_confirm_tips))
					.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							delteteFile(playdownloadItem);
							
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
			//点击正在下载界面进入播放
			downingHolder.playdownedlayout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {	
					if(playdownloadItem.state==1)
					{					
					    prepareMediaSource(playdownloadItem.musicpath);
					}else if(playdownloadItem.state==2){
//						Intent intent =new Intent();
//						intent.setClass(MusicDownedFragment.this.getActivity(),PlayActivity.class);						
//						intent.putExtra("sendlocalstring", playdownloadItem.musicpath); 
//						MusicDownedFragment.this.getActivity().startActivity(intent);
					//----------------
						Log.d("nnlog", "播放路径--"+playdownloadItem.musicpath);
						StartVidioutil.startVidiao(MusicDownedFragment.this.getActivity(), playdownloadItem.musicpath,"TV");
						
//						DownLoadVdioapkTools dt = new DownLoadVdioapkTools(MusicDownedFragment.this.getActivity());
//						if(dt.checkApkExist(MusicDownedFragment.this.getActivity(), "com.tyb.fun.palyer")){
//							dt.startAPP(playdownloadItem.musicpath);
//						}else{
//							Singinstents.getInstents().setVdiouri(playdownloadItem.musicpath);
//							Singinstents.getInstents().setAppPackageName("com.tyb.fun.palyer");
//							dt.showNoticeDialog();
//						}
					
					}
					
				}
			});
			return arg1;
		}
		public class DownedHolder {
			public ImageButton downloadclosebutton;//删除下载按钮
			public TextView downitemname;//下载item名字
			public TextView downsize;//下载大小
			public TextView playdownedlayout;

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
	private static long getFileSizes(File f) throws Exception
	{
	long size = 0;
	File flist[] = f.listFiles();
	for (int i = 0; i < flist.length; i++){
	if (flist[i].isDirectory()){
	size = size + getFileSizes(flist[i]);
	}
	else{
	size =size + getFileSize(flist[i]);
	}
	}
	return size;
	}
	private void delteteFile(Mysic playdownloadItem) {
		try {
			File file =new File(playdownloadItem.musicpath);
			if (file.exists())
		      {
		       if (file.isFile())
		       {
		        file.delete();
				downItems.remove(playdownloadItem);
				adapter.notifyDataSetChanged();

				Intent intent = new Intent("com.byt.music.downcomplet");
				if(playdownloadItem.state==1)
				{
					intent.putExtra("DeleteDownloadedItemName", playdownloadItem.musicname);
				}
				
				MyApplication.getInstance().sendBroadcast(intent);
		        Toast.makeText(MusicDownedFragment.this.getActivity(), R.string.deletesuccess, Toast.LENGTH_SHORT).show();
		       }
		      }
		} catch (Exception e) {
			Toast.makeText(MusicDownedFragment.this.getActivity(), R.string.deleteerror, Toast.LENGTH_SHORT).show();
		}
		
		
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getActivity().unregisterReceiver(downreBroadcastReceiver);
	}
	private void prepareMediaSource(String path)
	{	
		try {
			ArrayList<RingItem> ringItems=new ArrayList<RingItem>();
			RingItem item=new RingItem();
			item.musicuri=path;
			ringItems.add(item);
			Intent intent = new Intent(getActivity(), PlayMusicService.class);
			intent.putExtra(PlayMusicService.CUR_PLAY_ITEM_POSITION_KEY,item);
			intent.putExtra(PlayMusicService.CUR_PLAY_LIST_KEY, ringItems);
			getActivity().startService(intent);
			startActivity(new Intent(getActivity(),PlayMusicActivity.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
