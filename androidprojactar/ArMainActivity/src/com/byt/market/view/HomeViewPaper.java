package com.byt.market.view;

import java.util.List;

import com.byt.market.Constants;
import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.WebCommonActivity;
import com.byt.market.data.AppItem;
import com.byt.market.data.CateItem;
import com.byt.market.data.SubjectItem;
import com.byt.market.log.LogModel;
import com.byt.market.mediaplayer.PlayActivity;
import com.byt.market.mediaplayer.PlayWebVideoActivity;
import com.byt.market.mediaplayer.data.PlayDownloadItem;
import com.byt.market.mediaplayer.data.VideoItem;
import com.byt.market.mediaplayer.service.PlayDownloadService;
import com.byt.market.tools.LogCart;
import com.byt.market.ui.HomeFragment;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Toast;

public class HomeViewPaper extends ViewPager {
	public HomeViewPaper(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	private Context mContext;
	private float startx=0;
	private VelocityTracker mVelocityTracker;
	private float starty=0;
	public Context getmContext() {
		return mContext;
	}

	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}
	private boolean canupshow=true;
	private List<AppItem> mdatas;
	private boolean isonmove=false;
	private int pageindex;
	
	public int getPageindex() {
		return pageindex;
	}

	public void setPageindex(int pageindex) {
		this.pageindex = pageindex;
	}

	public List<AppItem> getMdatas() {
		return mdatas;
	}

	public void setMdatas(List<AppItem> mdatas) {
		this.mdatas = mdatas;
	}

	public HomeViewPaper(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		
		switch (arg0.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (mVelocityTracker != null) {
				mVelocityTracker.addMovement(arg0);
			}
			if(!isonmove)
			{
				int speedy=(int) mVelocityTracker.getYVelocity();
				float endx=arg0.getRawX();
				float endy=arg0.getRawY();
				if(Math.abs(endy-starty)<10&&Math.abs(endx-startx)>12)
				{
				//	Log.d("rmyzx","HomeViewPager MotionEvent.ACTION_MOVE----1");
					isonmove=true;
					MarketContext.getInstance().isGalleryMove = true;
				}else if(Math.abs(endy-starty)>10&&Math.abs(endx-startx)<10){
					//Log.d("rmyzx","HomeViewPager MotionEvent.ACTION_MOVE----2");
					isonmove=true;
					MarketContext.getInstance().isGalleryMove = false;
				}else{					
					isonmove=false;
					MarketContext.getInstance().isGalleryMove = true;	
					
				}
			}
			break;
		case MotionEvent.ACTION_DOWN:
			//Log.d("rmyzx","HomeViewPager MotionEvent.ACTION_DOWN");
			if (mVelocityTracker == null) {
				mVelocityTracker = VelocityTracker.obtain();
				mVelocityTracker.addMovement(arg0);
			}
			MarketContext.getInstance().isGalleryMove = true;
			startx=arg0.getRawX();
			starty=arg0.getRawY();
			isonmove=false;
			break;
		case MotionEvent.ACTION_UP:
			//Log.d("rmyzx","HomeViewPager MotionEvent.ACTION_UP");
			int velocityX =0;
			if (mVelocityTracker != null) {
				// System.out.println(mVelocityTracker.getYVelocity());
				mVelocityTracker.addMovement(arg0);
				mVelocityTracker.computeCurrentVelocity(1000);
				 velocityX = (int) mVelocityTracker.getXVelocity();
			}
			float endx=arg0.getRawX();
			float endy=arg0.getRawY();
			//Math.abs(velocityX) <= 10
		//	Log.d("nnlog","--------velocityX)"+Math.abs(velocityX)+"---"+Math.abs(endx-startx));
		//	Toast.makeText(mContext, "----endy)"+Math.abs(endy-starty)+"--endx-"+Math.abs(endx-startx), Toast.LENGTH_LONG).show();
			//if (Math.abs(velocityX) <= 40 && Math.abs(velocityX) >= 0)
			//{
				//<4
				if(Math.abs(endy-starty)<10&&Math.abs(endx-startx)<10)
				{
					MobclickAgent.onEvent(mContext, "HomeView");//友盟统计
					StatService.trackCustomEvent(mContext, "HomeView", "");
					AppItem appItem=mdatas.get(pageindex);
					if (appItem != null) {

						Intent intent ;
						LogCart.Log("pointtype-----"+appItem.pointtype);
						switch (appItem.pointtype) {
						case 1://糗百	
							intent=new Intent(Constants.TOJOKETETAILS);
							Bundle bundle = new Bundle();
							bundle.putString("joke_content", appItem.adesc);
							bundle.putString("joke_image_path", appItem.iconUrl);
							bundle.putInt("isshare", appItem.isshare);
							intent.putExtras(bundle);														
							((FragmentActivity) mContext).startActivity(intent);
							((FragmentActivity) mContext).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);		
							break;
						case 2://视频
							try {
								intent=new Intent();
								VideoItem videoitem=new VideoItem();
								videoitem.id = appItem.cateid;
								videoitem.cTitle =  appItem.name;
								videoitem.ImagePath = appItem.iconUrl;
								videoitem.cDesc =  appItem.adesc;
								videoitem.sid =  appItem.sid;
								videoitem.strLength =  appItem.strLength;
								videoitem.length =  appItem.length;
								videoitem.hash =appItem.hash;
								videoitem.videuri =  appItem.cateName;
								videoitem.actors = appItem.list_cateid;
								videoitem.area = appItem.oobpackage;
								videoitem.directors = appItem.pname;
								videoitem.year = appItem.stype;
								videoitem.playsum =appItem.downNum;
								videoitem.isshare= appItem.isshare;
								videoitem.ispay= appItem.ispay;
								videoitem.webURL=  appItem.updatetime;
								if(videoitem.webURL!=null&&videoitem.webURL.trim().length()>0&appItem.isshare!=1){
									intent.setClass(((FragmentActivity) mContext),PlayWebVideoActivity.class);
									intent.putExtra("videoUrl", videoitem.webURL);
									((FragmentActivity) mContext).startActivity(intent);
								}else {
									intent.setClass(((FragmentActivity) mContext),PlayActivity.class);
									if(PlayDownloadService.isVideodownedFile(videoitem))
									{
										intent.putExtra("sendlocalstring", PlayDownloadItem.VIDEO_DIR+videoitem.cTitle+videoitem.videuri.substring(videoitem.videuri.lastIndexOf("."))); 		
									}else{
										intent.putExtra("sendstring", videoitem); 	
									}
									if(videoitem.webURL!=null&&videoitem.webURL.trim().length()>0)
									{
										intent.putExtra("videoUrl", videoitem.webURL);
									}
									/*if(cateItem.isshare==1){
										intent =new Intent();
										intent.setClass(MovieFragment.this.getActivity(),ShareActivity.class);						
										MovieFragment.this.getActivity().startActivity(intent);
									}else {*/
									((FragmentActivity) mContext).startActivity(intent);
									/*}*/
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							break;
						case 3://专题
							intent=new Intent(Constants.TOLIST);									
								SubjectItem cateItem = new SubjectItem();
									cateItem.sid =appItem.sid;
									cateItem.name = appItem.name;
									cateItem.updatetTime =appItem.updatetime;
									cateItem.adesc =appItem.adesc;
									cateItem.count = appItem.commcount;
									cateItem.visitCount = appItem.rankcount;
									cateItem.iconUrl = appItem.iconUrl;
									cateItem.creditLimit = appItem.creditLimit;
									cateItem.ulevel = appItem.ulevel;
									cateItem.isshare =appItem.isshare;
									cateItem.ispay =appItem.ispay;
									cateItem.feeID =appItem.vcode;
									intent.putExtra("app", cateItem);
							intent.putExtra(Constants.LIST_FROM, LogModel.L_SUBJECT_HOME);
							((FragmentActivity) mContext).startActivity(intent);
							((FragmentActivity) mContext).overridePendingTransition(R.anim.push_left_in,
									R.anim.push_left_out);
							break;
						case 4://url
							intent=new Intent();
							intent.setClass(((FragmentActivity) mContext),WebCommonActivity.class);
							intent.putExtra("videoUrl", appItem.adesc);
							((FragmentActivity) mContext).startActivity(intent);									
							break;
						case 5://app
							int cateid = appItem.cateid;
							if (cateid == -1 || cateid == -2) {
								 intent = new Intent(Constants.TOLIST);
								intent.putExtra("app", appItem);
								intent.putExtra("from", LogModel.L_BANNER);
								((FragmentActivity) mContext).startActivityForResult(intent, 0);
								((FragmentActivity) mContext).overridePendingTransition(R.anim.push_left_in,
										R.anim.push_left_out);
							} else {
								//--------------
								if(appItem.mark>0){
									Intent intent1 = new Intent(Constants.TOLIST);
									
									SubjectItem app = new SubjectItem();
									app.sid = appItem.mark;
									LogCart.Log("MARK------"+appItem.mark);
										intent1.putExtra("app", app);
									intent1.putExtra(Constants.LIST_FROM, LogModel.L_SUBJECT_HOME);
									((FragmentActivity) mContext).startActivity(intent1);
									((FragmentActivity) mContext).overridePendingTransition(R.anim.push_left_in,
											R.anim.push_left_out);
								}else{
									LogCart.Log("toutou--------头");
									 intent = new Intent(Constants.TODETAIL);
									intent.putExtra("app", appItem);
									intent.putExtra("from", LogModel.L_BANNER);
									((FragmentActivity) mContext).startActivityForResult(intent, 0);
									((FragmentActivity) mContext).overridePendingTransition(R.anim.push_left_in,
											R.anim.push_left_out);
								}
								//--------
								
							}
							break;
						case 6:
							Intent intent1 = new Intent(Constants.TOMusicLIST);
							//SubjectItem app = new SubjectItem();
							CateItem app = new CateItem();
							//app.sid = appItem.musicRid;
							//app.id = appItem.musicRid;
							app.id = appItem.musicRid;
							
							LogCart.Log("图片---"+appItem.markiconURL);
							app.ImagePath = appItem.markiconURL;
							app.cTitle = appItem.cTitle;
							app.cDesc = appItem.cDesc;
							app.cCount = 2;
								intent1.putExtra("app", app);
							intent1.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
							((FragmentActivity) mContext).startActivity(intent1);
							((FragmentActivity) mContext).overridePendingTransition(R.anim.push_left_in,
									R.anim.push_left_out);
							break;
						case 7:
							Intent intent2 = new Intent(Constants.TONOVELLIST);
							//SubjectItem app = new SubjectItem();
							CateItem app2 = new CateItem();
							//app.sid = appItem.musicRid;
							//app.id = appItem.musicRid;
							app2.id = appItem.musicRid;
							
							LogCart.Log("图片---"+appItem.markiconURL);
							app2.ImagePath = appItem.markiconURL;
							app2.cTitle = appItem.cTitle;
							app2.cDesc = appItem.cDesc;
							app2.cCount = 2;
								intent2.putExtra("app", app2);
							intent2.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
							((FragmentActivity) mContext).startActivity(intent2);
							((FragmentActivity) mContext).overridePendingTransition(R.anim.push_left_in,
									R.anim.push_left_out);
							break;
						case 8:
							Intent intent3 = new Intent(Constants.TOTVLIST);
							//SubjectItem app = new SubjectItem();
							CateItem app3 = new CateItem();
							//app.sid = appItem.musicRid;
							//app.id = appItem.musicRid;
							app3.id = appItem.musicRid;
							
							LogCart.Log("图片---"+appItem.markiconURL);
							app3.ImagePath = appItem.markiconURL;
							app3.cTitle = appItem.cTitle;
							app3.cDesc = appItem.cDesc;
							app3.cCount = 2;
							intent3.putExtra("app", app3);
							intent3.putExtra(Constants.LIST_FROM, LogModel.L_MUSIC_CATE_HOME);
							((FragmentActivity) mContext).startActivity(intent3);
							((FragmentActivity) mContext).overridePendingTransition(R.anim.push_left_in,
									R.anim.push_left_out);
							break;
						default:
							break;
						}
						
					
						
					}
				}
			//}// add  by  bobo 
			isonmove=false;
			canupshow=true;
			MarketContext.getInstance().isGalleryMove = false;
	
	break;
		default:
			break;
		}
		
		return super.onTouchEvent(arg0);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub	
			if(arg0.getAction()== MotionEvent.ACTION_MOVE){
				
			}
			return true;
			
	}

}