package com.byt.market.mediaplayer.tv;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.MainActivity;
import com.byt.market.activity.SharePageActivity;
import com.byt.market.adapter.ImageAdapter;
import com.byt.market.bitmaputil.core.DisplayImageOptions;
import com.byt.market.data.BigItem;
import com.byt.market.data.RingItem;
import com.byt.market.database.ShareDatabase;
import com.byt.market.mediaplayer.PlayWebVideoActivity;
import com.byt.market.mediaplayer.ShareFacebookActivity;
import com.byt.market.mediaplayer.ShareMusicActivity;
import com.byt.market.mediaplayer.data.PlayDownloadItem;
import com.byt.market.mediaplayer.data.VideoItem;
import com.byt.market.mediaplayer.music.IPlayback;
import com.byt.market.mediaplayer.music.PlayMusicActivity;
import com.byt.market.mediaplayer.music.PlayMusicService;
import com.byt.market.mediaplayer.service.PlayDownloadService;
import com.byt.market.tools.DownLoadVdioapkTools;
import com.byt.market.tools.LogCart;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.util.JsonParse;
import com.byt.market.util.RapitUtile;
import com.byt.market.util.Singinstents;
import com.byt.market.util.StartVidioutil;
import com.byt.market.util.StopMusicUtil;
import com.byt.market.view.CusPullListView;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

/**
 * tv列表
 */
public class NewTVListFragment extends ListViewFragment {
	private int cateId;
	private boolean misnotf = false;
	private DisplayImageOptions mOptions;
	private int whichMusicType = 1;
	public IPlayback service;
	private RingItem currMoreRingItem;
	private RelativeLayout mloading;
	private ArrayList<RingItem> curPlayList = new ArrayList<RingItem>();
	@SuppressLint("HandlerLeak")
	Handler mhandHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				PlayDownloadService.isdownedFile(getAdapter().mListItems);
				getAdapter().notifyDataSetChanged();
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cateId = getArguments().getInt("cateId");
		mOptions = new DisplayImageOptions.Builder().cacheOnDisc().build();
		whichMusicType = getArguments().getInt("iswhichMusic");
		getActivity().registerReceiver(downreBroadcastReceiver,
				new IntentFilter("com.byt.music.downcomplet"));
		
	}

	@Override
	protected String tag() {
		return this.getClass().getSimpleName();
	}

	@Override
	protected String getRequestUrl() {
		
		//此处可能有问题  0729   bobo
		if(misnotf){
			
		}
		String url = Constants.APK_URL + "Music/v1.php?qt=Musiclist&rid="
				+ cateId + "&pi=" + getPageInfo().getNextPageIndex() + "&ps="
				+ getPageInfo().getPageSize()+"&stype="+"notf";
		return url;
	}

	@Override
	protected String getRequestContent() {
		return null;
	}

	public void setService(IPlayback service) {
		this.service = service;
	}

	@Override
	protected List<BigItem> parseListData(JSONObject result) {
		try {
			List<BigItem> list = JsonParse.parseMusicSubList(
					result.getJSONArray("data"), null, String.valueOf(cateId));
			try {
				if (list != null) {
					
					PlayDownloadService.isdownedFile(list);
//					for (int i = 0; i < list.size(); i++) {
//						curPlayList.add(list.get(i).ringhomeItems.get(0));
//					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return list;
		} catch (JSONException e) {

		}
		return null;
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.listview;
	}

	@Override
	protected View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, int res) {
		View view = inflater.inflate(res, container, false);
		view.findViewById(R.id.have_header).setVisibility(View.INVISIBLE);
		mloading = (RelativeLayout) view.findViewById(R.id.listview_loading);
		if(RapitUtile.isShowRapit()){
			
		}
		return view;
	}

	@Override
	public void showNoResultView() {
		super.showNoResultView();
		loadfailed.setButtonVisible(View.VISIBLE);
	}

	@Override
	protected ImageAdapter createAdapter() {
		return new CateListAdapter();
	}

	private class CateListAdapter extends ImageAdapter {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

		public CateListAdapter() {
			df.setMaximumFractionDigits(1);
		}

		@Override
		public View newView(LayoutInflater inflater, BigItem item) {
			View view = null;
			BigHolder holder = new BigHolder();
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_MUSICSUBLIST:
				view = inflater.inflate(R.layout.listitem_music_layout, null);
				holder.layoutType = item.layoutType;
				MusicItemHolder commonItemHolder = new MusicItemHolder();
				commonItemHolder.name = (TextView) view
						.findViewById(R.id.music_name_lable);
				commonItemHolder.ranknum = (TextView) view
						.findViewById(R.id.ranknum);
				commonItemHolder.musicuser = (TextView) view
						.findViewById(R.id.musicuser);
				commonItemHolder.musicspe = (TextView) view
						.findViewById(R.id.musicspe);
				//commonItemHolder.moreButton = (TextView) view
				//		.findViewById(R.id.music_morechoice);
				commonItemHolder.downlod = (LinearLayout) view
						.findViewById(R.id.dowlod);
				commonItemHolder.share = (LinearLayout) view
						.findViewById(R.id.share);
				commonItemHolder.downLayout = (LinearLayout) view
						.findViewById(R.id.downbutton);
				commonItemHolder.shareLayout = (LinearLayout) view
						.findViewById(R.id.sharebutton);
				commonItemHolder.choosemoreLayout = (LinearLayout) view
						.findViewById(R.id.item_choose_layout);
				commonItemHolder.playLayout = (RelativeLayout) view
						.findViewById(R.id.more_item_layout);
				commonItemHolder.musicdivider = (ImageView) view
						.findViewById(R.id.musicdivider);
				commonItemHolder.isdownitem = (ImageView) view
						.findViewById(R.id.isdownitem);
				commonItemHolder.musicisdowning = (ProgressBar) view
						.findViewById(R.id.musicisdowning);
				commonItemHolder.isPlayingView = (TextView) view
						.findViewById(R.id.musicisplay);
			//	commonItemHolder.mredpoint = (ImageView) view.findViewById(R.id.redpoint);
				/* modify end */

				holder.musicItemHolder = commonItemHolder;
				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_LOADING:
				view = inflater.inflate(R.layout.listitem_loading, null);
				holder.layoutType = item.layoutType;

				view.setTag(holder);
				break;
			case BigItem.Type.LAYOUT_LOADFAILED:
				view = inflater.inflate(R.layout.listitem_loadfailed2, null);
				holder.layoutType = item.layoutType;
				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						retry();
					}
				});
				view.setTag(holder);
				break;
			}

			return view;
		}

		@Override
		public void bindView(final int position, BigItem item, BigHolder holder) {
			switch (item.layoutType) {
			case BigItem.Type.LAYOUT_MUSICSUBLIST:

				try {
					final MusicItemHolder itemHolder = holder.musicItemHolder;
					
					
//					if(!RapitUtile.isShowRapit()){
//						itemHolder.mredpoint.setVisibility(View.GONE);
//					}
					
					for (final RingItem ringItem : item.ringhomeItems) {

						itemHolder.name.setText(ringItem.name);
						itemHolder.musicuser.setText(ringItem.user + "-");
						itemHolder.musicspe.setText(ringItem.spename);
						Log.d("nnlog", "stat11--"+ringItem.state);
						if (ringItem.state == 2) {
							itemHolder.isdownitem.setVisibility(View.VISIBLE);
							itemHolder.musicisdowning.setVisibility(View.GONE);
							itemHolder.isdownitem
									.setImageResource(R.drawable.list_icn_dld_ok);
						} else if (ringItem.state == 0) {
							itemHolder.musicisdowning.setVisibility(View.GONE);
							itemHolder.isdownitem.setVisibility(View.GONE);
						} else {
							itemHolder.musicisdowning
									.setVisibility(View.VISIBLE);
							itemHolder.isdownitem.setVisibility(View.GONE);
						}
						try {
							if (service != null
									&& ringItem.name.equals(service
											.getCurMusicName())) {
								itemHolder.isPlayingView
										.setVisibility(View.VISIBLE);
							} else {
								itemHolder.isPlayingView
										.setVisibility(View.INVISIBLE);
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						if (ringItem.rankcount != 0) {
							if (whichMusicType == 1) {
								if (position < 3)
									itemHolder.ranknum.setTextColor(0xffe94c4c);
								else
									itemHolder.ranknum.setTextColor(0xff888888);
							} else {

								itemHolder.ranknum.setTextColor(0xff888888);

							}
							itemHolder.ranknum.setText((position + 1) + "");

						}
//						itemHolder.moreButton.setTag(ringItem);
//						itemHolder.moreButton
//								.setOnClickListener(new OnClickListener() {
//
//									@Override
//									public void onClick(View v) {
//										try {
////											itemHolder.mredpoint.setVisibility(View.GONE);
////											RapitUtile.deletRapit();
//											currMoreRingItem = (RingItem) v
//													.getTag();
//											NewTVListFragment.this
//													.getAdapter()
//													.notifyDataSetChanged();
//										} catch (Exception e) {
//											e.printStackTrace();
//										}
//									}
//								});
						if (ringItem.equals(currMoreRingItem)) {
							itemHolder.choosemoreLayout
									.setVisibility(View.VISIBLE);
							itemHolder.musicdivider.setVisibility(View.GONE);

						} else {
							itemHolder.musicdivider.setVisibility(View.VISIBLE);
							itemHolder.choosemoreLayout
									.setVisibility(View.GONE);
						}
						itemHolder.downlod
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										try {
											currMoreRingItem = null;
											//Log.d("nnlog", ",,,,,,,,,"+ringItem.musicuri);
											File file = new File(
													PlayDownloadItem.VIDEO_DIR
															+ ringItem.name
															+ ringItem.adesc
																	.substring(ringItem.adesc
																			.lastIndexOf(".")));
											if (file.exists()) {
												Toast.makeText(
														NewTVListFragment.this
																.getActivity(),
														R.string.download_allready,
														Toast.LENGTH_SHORT)
														.show();
												return;
											}
//											VideoItem videitem = new VideoItem();//add by bobo
//											videitem.cTitle = ringItem.name;
//											videitem.videuri = ringItem.adesc;
//											videitem.length = ringItem.length;
//											videitem.cDesc = ringItem.logo;
//											videitem.sid = ringItem.sid;
											Intent intent = new Intent(
													NewTVListFragment.this
															.getActivity(),
													PlayDownloadService.class);
											intent.putExtra(
													PlayDownloadItem.DOWN_ITEM,
													ringItem);
											mhandHandle.removeMessages(0);
											mhandHandle
													.sendEmptyMessageDelayed(0,
															1000);
											NewTVListFragment.this
													.getActivity()
													.startService(intent);
//											NewTVListFragment.this
//													.getAdapter()
//													.notifyDataSetChanged();
											//DowlodVdioe.dowLod(getActivity(), ringItem.adesc, ringItem.name, mloading);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								});
						itemHolder.share
								.setOnClickListener(new OnClickListener() {
									// 分享音乐
									@Override
									public void onClick(View v) {
										try {
											currMoreRingItem = null;
											try {
												Intent intent = new Intent();
												Bundle bundle = new Bundle();
												bundle.putString("type", "tv");
												intent.putExtras(bundle);
												intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
												intent.setClass(
														MyApplication
																.getInstance(),
														ShareMusicActivity.class);
												intent.putExtra("sendstring",
														ringItem);
												MyApplication.getInstance()
														.startActivity(intent);
											} catch (Exception e) {
												Toast.makeText(
														MyApplication
																.getInstance(),
														MyApplication
																.getInstance()
																.getString(
																		R.string.can_not_share_info),
														Toast.LENGTH_SHORT)
														.show();
											}
											NewTVListFragment.this
													.getAdapter()
													.notifyDataSetChanged();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
						itemHolder.playLayout.setTag(R.id.more_item_layout,
								ringItem);
						itemHolder.playLayout
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										try {
											RingItem item = (RingItem) itemHolder.playLayout
													.getTag(R.id.more_item_layout);
											Intent intent =new Intent();
											if(item.share!=null&&item.share.equals("1")){
												ShareDatabase dataHaple = new ShareDatabase(NewTVListFragment.this.getActivity());
												SQLiteDatabase db = dataHaple.getReadableDatabase();

												Cursor cursor = db.query("usershare", new String[]{"id","name"}, "id=?", new String[]{String.valueOf(item.sid)}, null, null, null, null);  
									            //利用游标遍历所有数据对象  
												String name = null;
									            while(cursor.moveToNext()){  
									                name = cursor.getString(cursor.getColumnIndex("name"));  
									                //日志打印输出  
									                Log.d("nnlog", name+"---数据库");
									            }  
												if(name!=null&&name.equals(item.name)){
													playVdiao(ringItem);
												}else{
													Bundle bundle = new Bundle();
													bundle.putString("type", "tv");
													intent.putExtras(bundle);
													intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
													intent.setClass(MyApplication.getInstance(),SharePageActivity.class);						
													intent.putExtra("sendstring",item); 
													MyApplication.getInstance().startActivity(intent);
												}
											}else{
												playVdiao(ringItem);
											}
											
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case BigItem.Type.LAYOUT_LOADING:

				break;
			case BigItem.Type.LAYOUT_LOADFAILED:

				break;
			}
		}
	}
	//
	public void playVdiao(RingItem item){
		Intent webintent = new Intent(); 
		String ptype = "";
		if(item.adesc.contains("youtube"))
		{
			webintent.setClass(NewTVListFragment.this.getActivity(),PlayWebVideoActivity.class);
			MyApplication.getInstance().startActivity(webintent);
		}
		else
		{
			String savepath = PlayDownloadItem.VIDEO_DIR+item.name
					+ item.adesc.substring(item.adesc
							.lastIndexOf("."));
			if(new File(savepath).exists()){
				item.adesc = savepath;
				ptype = "TV";
			}
			StartVidioutil.startVidiao(NewTVListFragment.this.getActivity(),item.adesc ,ptype);
				return;
			
		}
	}
	
//	private void playMusic(RingItem ringItem) {
//		try {
//			Intent intent = new Intent(getActivity(), PlayMusicService.class);
//			intent.putExtra(PlayMusicService.CUR_PLAY_LIST_KEY, curPlayList);
//			try {
//				intent.putExtra("app", getActivity().getIntent()
//						.getParcelableExtra("app"));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			if (ringItem == null) {
//				ringItem = curPlayList.get(0);
//			}
//			intent.putExtra(PlayMusicService.CUR_PLAY_ITEM_POSITION_KEY,
//					ringItem);
//			getActivity().startService(intent);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	@Override
	protected void onDownloadStateChanged() {

	}

	@Override
	public void onAppClick(Object obj, String action) {
	}

	@Override
	public void addNewDataOnce() {
	}

	@Override
	public void onPost(List<BigItem> appendList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStyle(CusPullListView listview2) {
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.tvcat_header, null);
		cateId = getArguments().getInt("cateId");
		TextView appname = (TextView) view.findViewById(R.id.musicname);
		TextView appupdate = (TextView) view.findViewById(R.id.musicupdatetiem);
		ImageView appupicon = (ImageView) view.findViewById(R.id.musicicon);
		
		final RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parent_layout);
		RelativeLayout dowload = (RelativeLayout) view.findViewById(R.id.re_dow);
		TextView tv = (TextView) view.findViewById(R.id.dowload_textView);
		ImageView delet = (ImageView) view.findViewById(R.id.imageView_delet);
		if(RapitUtile.showTVbutton()){
			parent.setVisibility(View.GONE);
		}
		delet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				parent.setVisibility(View.GONE);
				RapitUtile.cancenButton();
			}
		});
		DownLoadVdioapkTools dt = new DownLoadVdioapkTools(getActivity());
		if(dt.checkApkExist(getActivity(), "com.tyb.fun.palyer")){
			dowload.setVisibility(View.GONE);
		}
		dowload.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				DownLoadVdioapkTools dt = new DownLoadVdioapkTools(getActivity());
				//if(dt.checkApkExist(context, "com.tyb.fun.palyer")){
					StatService.trackCustomEvent(getActivity(),"installPlayer","");
					Singinstents.getInstents().setVdiouri("");
					Singinstents.getInstents().setAppPackageName("com.tyb.fun.palyer");
					dt.showNoticeDialog();
				//}
			}
		});
		String name = getArguments().getString("appname");
		String updatesdc = getArguments().getString("appsdc");
		String appicon = getArguments().getString("appicon");
		appname.setText(name);
		appupdate.setText(updatesdc);
		if (!appicon.startsWith("http://")) {
			 imageLoader.displayImage(appicon, appupicon, mOptions);
		} else {
			imageLoader.displayImage(appicon, appupicon, mOptions);
		}
		listview2.addHeaderView(view);

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			getActivity().unregisterReceiver(downreBroadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//curPlayList.clear();
		mhandHandle.removeMessages(0);
	}

	@Override
	protected String getRefoushtUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<BigItem> dblistData() {
		// TODO Auto-generated method stub
		return null;
	}

}
