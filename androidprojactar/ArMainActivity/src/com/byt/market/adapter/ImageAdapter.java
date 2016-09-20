package com.byt.market.adapter;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.data.AppItem;
import com.byt.market.data.BigItem;
import com.byt.market.data.DetailItemHolder;
import com.byt.market.data.PageInfo;
import com.byt.market.data.ViewHolder;
import com.byt.market.util.BitmapUtil;
import com.byt.market.view.DetailTypeView;
import com.byt.market.view.HomeAdvertiseBand;
import com.byt.market.view.rapit.WaterDrop;
import com.byt.market.view.zhedie.CollapsableLinearLayout;

public abstract class ImageAdapter extends BaseAdapter {
	private static int RANGENULL = 20;

	// private ImageLoader mImageLoader;

	// protected Context mContext;
	protected LayoutInflater mInflater;

	/**
	 * 列表数据信息
	 */
	public List<BigItem> mListItems = new ArrayList<BigItem>();
	/**
	 * 列表页信息
	 */
	private PageInfo pageInfo = new PageInfo();

	//add by bobo
	private int dbstartID = 0;
	
	public int getDbstartID() {
		return dbstartID;
	}

	public void setDbstartID(int dbstartID) {
		this.dbstartID = dbstartID;
	}

	public ImageAdapter() {
		this.mInflater = LayoutInflater.from(MyApplication.getInstance());
	}

	public void setPageInfo(PageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}

	/**
	 * 返回adapter中列表数据的页信息
	 * 
	 * @return
	 */
	public PageInfo getPageInfo() {
		return this.pageInfo;
	}

	/**
	 * 是否为空列表
	 */
	public boolean isEmpty() {
		if (mListItems == null || mListItems.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 在列表尾端加入多条数据
	 * 
	 * @param list
	 */
	public void add(List<BigItem> list) {
		if (list != null && list.size() > 0) {
			mListItems.addAll(list);
			notifyDataSetChanged();
		}
	}

	/**
	 * 在列表尾端加入多条数据
	 * 
	 * @param list
	 */
	public void addFirst(List<BigItem> list) {
		if (list != null && list.size() > 0) {
			mListItems.addAll(0, list);
			notifyDataSetChanged();
		}
	}

	/**
	 * 在列表尾端加入一条数据
	 * 
	 * @param item
	 */
	public void add(BigItem item) {
		if (item != null) {
			mListItems.add(item);
			notifyDataSetChanged();
		}
	}

	/**
	 * 删除指定位置的一条数据
	 * 
	 * @param index
	 */
	public void remove(int index) {
		if (index > 0 && index < getCount()) {
			mListItems.remove(index);
			notifyDataSetChanged();
		}
	}

	/**
	 * 添加最后一条布局
	 * 
	 * @param type
	 */
	public void addLast(int type) {
		if (!isEmpty()) {
			BigItem item = new BigItem();
			item.layoutType = type;
			mListItems.add(item);
			notifyDataSetChanged();
		}
	}

	/**
	 * 删除最后一条数据
	 */
	public void removeLast() {
		remove(getCount() - 1);
	}

	/**
	 * 获取最后一个item的类型
	 * 
	 * @return
	 */
	public int getLastItemType() {
		if (isEmpty()) {
			return BigItem.Type.LAYOUT_ERROR;
		} else {
			BigItem item = getItem(getCount() - 1);
			return item.layoutType;
		}
	}

	public BigItem[] getListItems()
	{
		BigItem[] items = new BigItem[getCount()];
		for(int i = 0; i < getCount(); i++)
		{
			items[i] = getItem(i);
		}
		return items;
	}
	/**
	 * 清空列表数据
	 */
	public void clear() {
		if (!isEmpty()) {
			mListItems.clear();
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		if (isEmpty()) {
			return 0;
		} else {
			return mListItems.size();
		}
	}

	@Override
	public BigItem getItem(int position) {
		if (!isEmpty() && position < mListItems.size()) {
			return mListItems.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BigItem item = getItem(position);
		if (item == null) {
			return convertView;
		}
		if (convertView == null
				|| (convertView.getTag()!= null && ((BigHolder) convertView
						.getTag()).layoutType != item.layoutType)) {
			convertView = newView(mInflater, item);
		}
		if (convertView != null) {
			bindView(position, item, (BigHolder) convertView.getTag());
		}
		return convertView;
	}

	public void showStype(ViewHolder viewHolder, AppItem app) {
		if (app.stype != null) {
			if (app.stype.contains("1"))
				viewHolder.icon_type
						.setBackgroundResource(R.drawable.l_home_type_new);
			else if (app.stype.contains("2"))
				viewHolder.icon_type.setImageBitmap(BitmapUtil.createTextImage(
						MyApplication.getInstance(),
						R.drawable.l_home_type_new, R.string.first_product));
			else if (app.stype.contains("3"))
				viewHolder.icon_type.setImageBitmap(BitmapUtil.createTextImage(
						MyApplication.getInstance(),
						R.drawable.l_home_type_new, R.string.act_product));
			else if (app.stype.contains("4"))
				viewHolder.icon_type.setImageBitmap(BitmapUtil.createTextImage(
						MyApplication.getInstance(),
						R.drawable.l_home_type_new, R.string.hot_product));
			// .setBackgroundResource(R.drawable.l_home_type_hot);
			else if (app.stype.contains("5"))
				viewHolder.icon_type.setImageBitmap(BitmapUtil.createTextImage(
						MyApplication.getInstance(),
						R.drawable.l_home_type_new, R.string.rec_product));
			else
				viewHolder.icon_type.setBackgroundColor(MyApplication
						.getInstance().getResources()
						.getColor(android.R.color.transparent));
		} else
			viewHolder.icon_type.setBackgroundColor(MyApplication.getInstance()
					.getResources().getColor(android.R.color.transparent));
	}

	/**
	 * 创建视图
	 * 
	 * @param item
	 * @param convertView
	 * @return
	 */
	public abstract View newView(LayoutInflater inflater, BigItem item);

	/**
	 * 刷新视图
	 * 
	 * @param position
	 * @param item
	 * @param holder
	 *            有可能为空
	 */
	public abstract void bindView(int position, BigItem item, BigHolder holder);

	public  class BigHolder {
		public int layoutType;

		public NewsItemspeesTime spitem;
		public NewsItemHolder newsitemhodler;
		public ChangHolder changer;
		/** 公共listitemholder **/
		public CommonItemHolder commonItemHolder;
		public HomeItemHolder homeItemhodler;
		
		public FavItemHolder favItemHolder;
		public MusicItemHolder musicItemHolder;
		/**
		 * 排行类型item用
		 */
		public RankItemHolder rankHolder;

		public ResultSafeHolder reSafeHolder;

		public ResultTitleHolder reTitleHolder;

		public SubjectItemHolder subjectHolder;

		public DetailItemHolder detailHolder;

		public CommentItemHolder commentHolder;

		public HotwordHolder hotwordHolder;

		public HistoryHolder historyHolder;

		public ThinkHolder thinkHolder;
		public ArrayList<CategoryItemHolder> cateHoldershead = new ArrayList<CategoryItemHolder>();
		public ArrayList<CategoryItemHolder> cateHolders = new ArrayList<CategoryItemHolder>();
		public ArrayList<RecommendHolder> recommendHolders = new ArrayList<RecommendHolder>();

		public NewItemHolder newHolder;

		public HomeBannerHolder bannerHolder;

		public HomeLeaderHolder leaderHolder;

		public HomeRecHolder recHolder;

		public HomeHolder homeHolder;

		public NewUserItemHolder newuserHolder;

		public SubListItemHolder sublistHolder;

		public JoySkillItemHolder joySkilllistHolder;

		public StartRecHomeHolder startRecHolder;

		public ArrayList<LocalItemHolder> mygameHolder = new ArrayList<LocalItemHolder>();
		
		public ArrayList<JokeItemHolder> jokeTextImageHolders = new ArrayList<JokeItemHolder>();	
		public ArrayList<JokeItemHolder> jokeTextHolders = new ArrayList<JokeItemHolder>();	
		public ArrayList<JokeItemHolder> jokeImageHolders = new ArrayList<JokeItemHolder>();
		public ArrayList<JokeItemHolder> jokeCommentHolders = new ArrayList<JokeItemHolder>();	
	}

	//------
	public class ChangHolder{
		public TextView changText;
		public TextView perviousText;

	}
	// 大家一起玩
	public  class RecommendHolder {
		public View views;
		public ImageView icons;
		public TextView names;
		public ImageView imgCheck;
		public RatingBar ratingbar;

	}

	/**
	 * 排行item,holder
	 */
	public  class RankItemHolder extends CommonItemHolder {
		/** 排行数字 **/
		public TextView tv_rank_number;
		public TextView tv_rank_other_number;
		/** 排行角标 上升或下降 **/
		public ImageView img_rank;
	}

	public  class ResultSafeHolder extends ViewHolder {
		public TextView name;
		public TextView subLine;
		public TextView listitem_rec_atip;
		public TextView rating;
		public RatingBar rec_rating;
		public TextView rec_comm;
	}

	public  class ResultTitleHolder extends ViewHolder {
		public TextView name;
	}

	/**
	 * 专题item,holder
	 * 
	 * @author qiuximing
	 * 
	 */
	public  class SubjectItemHolder extends ViewHolder {
		public TextView name;
		public TextView updatetime;
		public TextView subDesc;
		public ImageView bgview;
		// public TextView comm;
		// public TextView num;
	}

	//by  bobo 
		public class HomeItemHolder extends ViewHolder{
			public TextView title_text;
			public TextView more_text;
			//首页三列风格
			public ArrayList<HomecildItemHolder> homeItemHolders = new ArrayList<HomecildItemHolder>();
			
			
			
		}
		public class HomecildItemHolder extends ViewHolder{
			public ImageView image_head_one;
			//ImageView image_head_tow;
			//ImageView image_head_tree;
			public TextView one_text1;
			//TextView tow_text1;
			//TextView tree_text1;
			
			public TextView one_text2;
			//TextView tow_text2;
			//TextView tree_text2;
			
			public TextView one_text3;
			//TextView tow_text3;
			//TextView tree_text3;
			public RelativeLayout bgrelativelayout;
		}
		
	//by  bobo 
//	public class HomeItemHolder extends ViewHolder{
//		TextView title_text;
//		
//		ImageView image_head_one;
//		ImageView image_head_tow;
//		ImageView image_head_tree;
//		TextView one_text1;
//		TextView tow_text1;
//		TextView tree_text1;
//		
//		TextView one_text2;
//		TextView tow_text2;
//		TextView tree_text2;
//		
//		TextView one_text3;
//		TextView tow_text3;
//		TextView tree_text3;
//		
//		
//		
//	}
	
	public  class CommonItemHolder extends ViewHolder {
		public RelativeLayout adv_layout;
		public ImageView appIcon;
		public ImageView appType;
		public TextView name;
		public TextView downum;
		public RatingBar rating;
		public TextView bt_free_btn;
		public TextView size;
		public TextView downBtn;
		public Button gotosearch;
		public TextView sdesc; // 描述 or 小编点评
		public LinearLayout descLayout;
		public TextView appprice;//应用价格
		public TextView freestype;//限时免费
		public TextView appvip;//应用所需等级
		public FrameLayout pricelayout;//价格布局
		public RelativeLayout itemLayout;
		public FrameLayout googlepriceFram;//google价格布局
		public View bommline1;//猜你喜欢的底部线
		public ProgressBar DownloadProgressBar;//下载进度条
		public TextView progressnumtext;//下载进度
		public ProgressBar DownloadProgressBar2;//分享下载进度条
		public TextView progressnumtext2;//分享下载进度
		public TextView downBtn2;//分享下载按钮
		public RelativeLayout homelayout;
		public TextView myfy;
		public ImageView homegraybg;
		public ImageView homeadimage;
		//public ImageView share_icon;//分享按钮icon
		public RelativeLayout sharelayout;//分享按钮icon
		public TextView googlefreebg;
		public  ImageView googlepriceline;
		public  ImageView googleicon;//
		public  TextView item_up_bg;
		public  TextView sizedivider;//分割线
	}
	public  class FavItemHolder extends ViewHolder {
		public ImageView appIcon;
		public ImageView appType;
		public TextView name;
		public TextView downum;
		public RatingBar rating;
		public TextView bt_free_btn;
		public TextView size;
		public TextView downBtn;
		public Button gotosearch;
		public TextView sdesc; // 描述 or 小编点评
		public LinearLayout descLayout;
		public TextView appprice;//应用价格
		public TextView freestype;//限时免费
		public TextView appvip;//应用所需等级
		public FrameLayout pricelayout;//价格布局
		public LinearLayout itemLayout;
		public FrameLayout googlepriceFram;//google价格布局
		public View bommline1;//猜你喜欢的底部线
		public ProgressBar DownloadProgressBar;//下载进度条
		public TextView progressnumtext;//下载进度
		public ProgressBar DownloadProgressBar2;//分享下载进度条
		public TextView progressnumtext2;//分享下载进度
		public TextView downBtn2;//分享下载按钮
		public ImageView share_icon;//分享按钮icon
		public RelativeLayout sharelayout;//分享按钮icon
		public TextView googlefreebg;
		public  ImageView googlepriceline;
		public  ImageView googleicon;//
		public  TextView item_up_bg;
		public  TextView sizedivider;//分割线
	}

	/**
	 * 专题item,holder
	 * 
	 * @author qiuximing
	 * 
	 */
	public  class DetailItemHolder extends ViewHolder {
		public TextView name;
		public TextView type;
		public TextView downnum;
		public RatingBar rating;

		public TextView size;
		public TextView catename;
		public TextView vname;
		public TextView updatetime;
		public TextView lang;
		public TextView feetype;
		public View v_ades;
		public View v_sdes;

		public TextView ades;
		public TextView des;
		public ImageButton exButton;

		public DetailTypeView tv_type;
		// public DetailTipView listitem_d_tip;
		public GridView sBand;
		// public CustomDetailCommBand cdc_band;
		/*add by zengxiao*/
		public Button btn_down;
		public LinearLayout btn_copareprice;//价格对比
		public TextView textviewprice;//应用价格
		public ImageView freeline;
		/*add end*/
		// public TextView more_comm;
		public TextView tag;
		public LinearLayout tagLayout;
	}

	public  class MusicItemHolder{
		public TextView name;//音乐名
		public TextView ranknum;//排名
		public TextView musicuser;//歌手
		public TextView musicspe;//专辑
		public TextView moreButton;//更多操作
		public LinearLayout choosemoreLayout;//更多操作布局
		public LinearLayout downLayout;//下载布局
		public LinearLayout shareLayout;//分享布局
		public RelativeLayout playLayout;//item布局
		public ImageView musicdivider ;
		public ImageView isdownitem;//已下载
		public ProgressBar musicisdowning;//下载进度
		public TextView isPlayingView;//正在播放的图片
		public ImageView mredpoint;
		public LinearLayout downlod;
		public LinearLayout share;
		//public ImageView moreimg;
		
	}
	/**
	 * 专题item,holder
	 * 
	 * @author qiuximing
	 * 
	 */
	public  class CommentItemHolder extends ViewHolder {
		public TextView cname;
		public RatingBar cranting;
		public TextView cupdatetime;
		public TextView cdes;
		public TextView cmodle;
		public TextView cvname;
	}

	/**
	 * 搜索热词,holder
	 * 
	 * @author qiuximing
	 * 
	 */
	public  class HotwordHolder extends ViewHolder {
		public TextView rank;
		public TextView name;
		public TextView count;
	}

	/**
	 * 搜索历史holder
	 * 
	 * @author Administrator
	 * 
	 */
	public  class HistoryHolder extends ViewHolder {
		public TextView name;
		public ImageView del;
		public View ll_history;
		public View ll_clear_all;
	}

	/**
	 * 搜索联想holder
	 * 
	 * @author Administrator
	 * 
	 */
	public  class ThinkHolder extends ViewHolder {
		public TextView name;
		public TextView des;
		public ImageView img;
	}

	/**
	 * 分类子item,holder
	 * 
	 * @author qiuximing
	 * 
	 */
	public  class CategoryItemHolder extends ViewHolder {
		public TextView name;
		public TextView content;//添加分类详情介绍
		public TextView musicHolder;
		public RelativeLayout videolayout;//视频整体布局
		public ImageView mimagedowload;
		public ProgressBar mprobar;
		public ImageView mdownloaded;
	}

	public  class JokeItemHolder extends ViewHolder {
		public TextView joker;
		public TextView content;
		public ImageView image;
		public ImageView isPaly_img;
		public ImageView gif_text;
		public LinearLayout comment;
		public LinearLayout collect;
		public LinearLayout share;
		public int comment_count;
		public int vote_count;
		public TextView comment_count_tv;
		public TextView collect_count_tv;
		public TextView share_count_tv;
		public ImageView comment_img;
		public ImageView collect_img;	
		public FrameLayout comment_layout;	
		public TextView readview;
		public TextView title_tv;
		public TextView time_tv;
		public ImageView headimag;
	}	
	
	
	/**
	 *  新闻 时间 间隔
	 */
	public class NewsItemspeesTime extends ViewHolder{
		public TextView speas;
	}
	/**
	 * 新闻 大图
	 */
	public class NewsItemHolder extends ViewHolder{
		public TextView big_text;
		public ImageView big_image;
		public RelativeLayout big_r_layout;
		public RelativeLayout big_text_layout;
		
	}
	
//	/**
//	 * 新闻换行
//	 */
//	public class Newsspes extends ViewHolder{
//		public RelativeLayout r_spes;
//	}
	/**
	 * 最新排行item,holder
	 * 
	 * @author qiuximing
	 * 
	 */
	public  class NewItemHolder extends ViewHolder {
		public TextView name;
		public TextView subLine;
	}

	/**
	 * 新手必备item,holder
	 * 
	 * @author qiuximing
	 * 
	 */
	public  class NewUserItemHolder extends ViewHolder {
		public TextView name;
		public TextView subLine;
	}

	/**
	 * 二级列表item,holder
	 * 
	 * @author qiuximing
	 * 
	 */
	public  class SubListItemHolder extends ViewHolder {
		public TextView name;
		public TextView subLine;
	}

	public  class HomeBannerHolder extends ViewHolder {
		public HomeAdvertiseBand hBand;
	}

	public  class HomeLeaderHolder extends ViewHolder {
		public WaterDrop[] leadericons = new WaterDrop[12];
		public TextView[] leadernames = new TextView[12];
		public View[] leaderviews = new View[12];
		public RelativeLayout title_rela;
		public ImageView zhedie_imag;
		public CollapsableLinearLayout collaslinerlayout;
	}

	public  class HomeRecHolder extends ViewHolder {
		public ImageView icon;
		public TextView name;
		public TextView subLine;
		public RatingBar rec_rating;
		public TextView rec_comm;
		public TextView listitem_rec_atip;
		public View ll_rec_today;
	
		
	}

	public  class HomeHolder extends ViewHolder {
		public ImageView[] icons = new ImageView[3];
		public TextView[] names = new TextView[3];
		public TextView[] sublines = new TextView[3];
		public Button[] btns = new Button[3];
		public ProgressBar[] progresses = new ProgressBar[3];
		public View[] views = new View[3];
		public ImageView[] icon_types = new ImageView[3];
	}

	public  class StartRecHomeHolder extends ViewHolder {
		public ImageView[] icons = new ImageView[3];
		public TextView[] names = new TextView[3];
		public TextView[] sublines = new TextView[3];
		public Button[] btns = new Button[3];
		public ProgressBar[] progresses = new ProgressBar[3];
		public View[] views = new View[3];
		public ImageView[] icon_types = new ImageView[3];
	}

	/**
	 * 攻略item,holder
	 * 
	 * @author qiuximing
	 * 
	 */
	public  class JoySkillItemHolder extends ViewHolder {
		public TextView name;
		public TextView adEsc;
		public TextView upateData;
		public TextView vCount;
	}

	//
	// /**
	// * 分类二级列表item,holder
	// * @author qiuximing
	// *
	// */
	// public static class SubCateListItemHolder extends ViewHolder{
	// public TextView name;
	// public TextView subLine;
	// }

	/**
	 * 我的游戏列表item,holder
	 * 
	 * @author qiuximing
	 * 
	 */
	public  class LocalItemHolder extends ViewHolder {
		public TextView name;
	}

	public  class ViewHolder {

		public ImageView icon;
		public ImageView icon_type;
		public ProgressBar progress;
		public TextView progressText;
		public Button btn;
		public View layout;
		public TextView point;//积分
		public LinearLayout point_image;//用户级别
		public View line;//分割线
		public FrameLayout mycontent;
		public TextView listitemvideosdc;//视频介绍
		public TextView text_pay;
		

	}
	/**
	 * 列表停止时加载图标
	 */
	public void loadIcon(AbsListView view) {
		if (view == null) {
			return;
		}
	/*	int first = view.getFirstVisiblePosition();
		Handler handler = view.getHandler();
		int count = view.getChildCount();
		if (handler != null) {
			if ((count == 1 || count == 0) && getCount() > 0) {
				count = getCount() > 7 ? 7 : getCount();
			}
		} else {
			if (count == 0 && getCount() > 0) {
				count = getCount() > 7 ? 7 : getCount();
			}
		}
		ArrayList<BigItem> list = new ArrayList<BigItem>();
		for (int i = 0; i < count; i++) {
			BigItem item = getItem(first + i);
			if (item != null) {
				list.add(item);
			}
		}*/
		// mImageLoader = new ImageLoader(MyApplication.getInstance(), list,
		// this);
		// edit by wangxin. use Bitmapfun load image
		// mImageLoader.loading();
	}

	public void clearListIcon(AbsListView view) {
		clearListIcon(view, false);
	}

	public void clearListIconAll(AbsListView view) {
		clearListIcon(view, true);
		notifyDataSetChanged();
	}

	/**
	 * 列表停止时回收资源图片
	 * 
	 * @param view
	 */
	public void clearListIcon(AbsListView view, boolean all) {
		try {
			// int first = view.getFirstVisiblePosition();
			// int count = view.getChildCount();
			// int listcount = view.getCount();
			//
			// int rangeUpNull = first - RANGENULL <= 0 ? 0 : first - RANGENULL;
			// int rangeDownNull = first + count + RANGENULL >= listcount - 1 ?
			// listcount - 1
			// : first + count + RANGENULL;
			//
			// for (int i = 0; i < listcount; i++) {
			// if (i < rangeUpNull || i > rangeDownNull || all) {
			// BigItem data = getItem(i);
			// if (data == null) {
			// continue;
			// }
			// switch (data.layoutType) {
			// case BigItem.Type.LAYOUT_RANKLIST:
			// for (AppItem item : data.rankItems) {
			// if (item.icon != null && !item.icon.isRecycled()) {
			// item.icon.recycle();
			// item.icon = null;
			// }
			// }
			// break;
			// case BigItem.Type.LAYOUT_CAEGORYLIST:
			// for (CateItem item : data.cateItems) {
			// if (item.icon != null && !item.icon.isRecycled()) {
			// item.icon.recycle();
			// item.icon = null;
			// }
			// }
			// break;
			// case BigItem.Type.LAYOUT_SUBJECTLIST:
			// for (SubjectItem item : data.subjectItems) {
			// if (item.icon != null && !item.icon.isRecycled()) {
			// item.icon.recycle();
			// item.icon = null;
			// }
			// }
			// break;
			// case BigItem.Type.LAYOUT_SEARCH_SAFE:
			// for (AppItem item : data.resultItems) {
			// if (item.icon != null && !item.icon.isRecycled()) {
			// item.icon.recycle();
			// item.icon = null;
			// }
			// }
			// break;
			// case BigItem.Type.LAYOUT_SEARCH_RELA:
			// for (AppItem item : data.resultItems) {
			// if (item.icon != null && !item.icon.isRecycled()) {
			// item.icon.recycle();
			// item.icon = null;
			// }
			// }
			// break;
			// case BigItem.Type.LAYOUT_SEARCH_NO_RESULT:
			// for (AppItem item : data.resultItems) {
			// if (item.icon != null && !item.icon.isRecycled()) {
			// item.icon.recycle();
			// item.icon = null;
			// }
			// }
			// break;
			// case BigItem.Type.LAYOUT_APPLIST_THREE:
			// for (AppItem item : data.homeItems) {
			// if (item.icon != null && !item.icon.isRecycled()) {
			// item.icon.recycle();
			// item.icon = null;
			// }
			// }
			// break;
			// case BigItem.Type.LAYOUT_APPLIST_ONE:
			// for (AppItem item : data.homeItems) {
			// if (item.icon != null && !item.icon.isRecycled()) {
			// item.icon.recycle();
			// item.icon = null;
			// }
			// }
			// break;
			// case BigItem.Type.LAYOUT_LEADER_THREE:
			// for (HLeaderItem item : data.leaderItems) {
			// if (item.icon != null && !item.icon.isRecycled()) {
			// item.icon.recycle();
			// item.icon = null;
			// }
			// }
			// break;
			// case BigItem.Type.LAYOUT_APPLIST_MUTILE:
			// for (AppItem item : data.homeItems) {
			// if (item.icon != null && !item.icon.isRecycled()) {
			// item.icon.recycle();
			// item.icon = null;
			// }
			// }
			// break;
			// case BigItem.Type.LAYOUT_COMMENTS:
			// for (AppComment item : data.comments) {
			// if (item.icon != null && !item.icon.isRecycled()) {
			// item.icon.recycle();
			// item.icon = null;
			// }
			// }
			// break;
			// }
			// }
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clearListIcon() {
		// for (int i = 0; i < getCount(); i++) {
		// BigItem bigItem = getItem(i);
		// switch (bigItem.layoutType) {
		// case BigItem.Type.LAYOUT_RANKLIST:
		// for (AppItem item : bigItem.rankItems) {
		// if (item.icon != null && !item.icon.isRecycled()) {
		// item.icon.recycle();
		// item.icon = null;
		// }
		// }
		// break;
		// case BigItem.Type.LAYOUT_CAEGORYLIST:
		// for (CateItem item : bigItem.cateItems) {
		// if (item.icon != null && !item.icon.isRecycled()) {
		// item.icon.recycle();
		// item.icon = null;
		// }
		// }
		// break;
		// case BigItem.Type.LAYOUT_SUBJECTLIST:
		// for (SubjectItem item : bigItem.subjectItems) {
		// if (item.icon != null && !item.icon.isRecycled()) {
		// item.icon.recycle();
		// item.icon = null;
		// }
		// }
		// break;
		// case BigItem.Type.LAYOUT_APPLIST_THREE:
		// break;
		// }
		// }
		notifyDataSetChanged();
	}

	/**
	 * 列表滑动时停止图标加载
	 */
	public void cancelLoadIcon() {
		// if (mImageLoader != null) {
		// mImageLoader.cancel();
		// mImageLoader = null;
		// }
	}

	// /**
	// * 图标加载器
	// *
	// * @author qiuximing
	// *
	// */
	// public static class ImageLoader implements Runnable {
	//
	// private Context mContext;
	// private UrlHttpConnection huc;
	// private List<BigItem> mListItems;
	// private NotifyHandler mHandler;
	// private boolean canceled;
	// private DisplayMetrics outMetrics = new DisplayMetrics();
	//
	// private static class NotifyHandler extends Handler {
	//
	// WeakReference<ImageAdapter> mAdapter;
	//
	// public NotifyHandler(ImageAdapter adapter) {
	// mAdapter = new WeakReference<ImageAdapter>(adapter);
	// }
	//
	// public void handleMessage(Message msg) {
	// if (mAdapter != null) {
	// ImageAdapter iAdapter = mAdapter.get();
	// if (iAdapter != null)
	// iAdapter.notifyDataSetChanged();
	// }
	// }
	// };
	//
	// public ImageLoader(Context context, List<BigItem> listItems,
	// ImageAdapter adapter) {
	// this.mContext = context;
	// this.mListItems = listItems;
	// this.mHandler = new NotifyHandler(adapter);
	// WindowManager wm = (WindowManager) context
	// .getSystemService(Context.WINDOW_SERVICE);
	// wm.getDefaultDisplay().getMetrics(outMetrics);
	// }
	//
	// /**
	// * 开始加载图标
	// */
	// public void loading() {
	// if (SharedPrefManager.isDisplayIconScreenshot(mContext)) {
	// new Thread(this).start();
	// }
	// }
	//
	// /**
	// * 停止图标加载
	// */
	// public void cancel() {
	// canceled = true;
	// if (huc != null) {
	// huc.cancel();
	// }
	// }
	//
	// @Override
	// public void run() {
	// for (int i = 0; i < mListItems.size(); i++) {
	// BigItem bigitem = mListItems.get(i);
	// if (canceled) {
	// return;
	// }
	// switch (bigitem.layoutType) {
	// case BigItem.Type.LAYOUT_RANKLIST:
	// for (int ranklist = 0; ranklist < bigitem.rankItems.size(); ranklist++) {
	// AppItem item = bigitem.rankItems.get(ranklist);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	// break;
	// case BigItem.Type.LAYOUT_CAEGORYLIST:
	// for (int catelist = 0; catelist < bigitem.cateItems.size(); catelist++) {
	// CateItem item = bigitem.cateItems.get(catelist);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	//
	// break;
	// case BigItem.Type.LAYOUT_SUBJECTLIST:
	// for (int subjectlist = 0; subjectlist < bigitem.subjectItems
	// .size(); subjectlist++) {
	// SubjectItem item = bigitem.subjectItems
	// .get(subjectlist);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	// break;
	// case BigItem.Type.LAYOUT_SUBLIST:
	// for (int sublist = 0; sublist < bigitem.subListItems.size(); sublist++) {
	// AppItem item = bigitem.subListItems.get(sublist);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	// break;
	// case BigItem.Type.LAYOUT_APPLIST_ONE:
	// for (int applistOne = 0; applistOne < bigitem.recItems
	// .size(); applistOne++) {
	// AppItem item = bigitem.recItems.get(applistOne);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	//
	// break;
	// case BigItem.Type.LAYOUT_APPLIST_THREE:
	// for (int applistThree = 0; applistThree < bigitem.homeItems
	// .size(); applistThree++) {
	// AppItem item = bigitem.homeItems.get(applistThree);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	//
	// break;
	// case BigItem.Type.LAYOUT_LEADER_THREE:
	// for (int leaderThree = 0; leaderThree < bigitem.leaderItems
	// .size(); leaderThree++) {
	// HLeaderItem item = bigitem.leaderItems.get(leaderThree);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	// break;
	// case BigItem.Type.LAYOUT_DETAILS:
	// for (int details = 0; details < bigitem.details.size(); details++) {
	// AppDetail item = bigitem.details.get(details);
	// if (item.icon == null) {
	// loadDetailImage(item);
	// }
	// }
	//
	// break;
	// case BigItem.Type.LAYOUT_SEARCH_SAFE:
	// for (int searchSafe = 0; searchSafe < bigitem.resultItems
	// .size(); searchSafe++) {
	// AppItem item = bigitem.resultItems.get(searchSafe);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	//
	// break;
	// case BigItem.Type.LAYOUT_SEARCH_RELA:
	// for (int searchRela = 0; searchRela < bigitem.resultItems
	// .size(); searchRela++) {
	// AppItem item = bigitem.resultItems.get(searchRela);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	//
	// break;
	// case BigItem.Type.LAYOUT_SEARCH_NO_RESULT:
	// for (int searchNoResult = 0; searchNoResult < bigitem.resultItems
	// .size(); searchNoResult++) {
	// AppItem item = bigitem.resultItems.get(searchNoResult);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	//
	// break;
	// case BigItem.Type.LAYOUT_COMMENTS:
	// for (int comments = 0; comments < bigitem.comments.size(); comments++) {
	// AppComment item = bigitem.comments.get(comments);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	//
	// break;
	// case BigItem.Type.LAYOUT_JOY_SKILL:
	// for (int joySkill = 0; joySkill < bigitem.joySkillItems
	// .size(); joySkill++) {
	// JoySkillItem item = bigitem.joySkillItems.get(joySkill);
	// if (item.icon == null) {
	// loadImage(item);
	// }
	// }
	//
	// break;
	// }
	//
	// }
	// }
	//
	// /**
	// * 优先SD卡读取图片，其次网络读取图片
	// *
	// * @param item
	// */
	// private void loadImage(AppComment item) {
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// return;
	// }
	// String iconName = StringUtil.md5Encoding(Constants.IMG_URL
	// + item.iconUrl);
	// if (iconName == null) {
	// return;
	// }
	// byte[] buffer = FileUtil.getBytesFromFile(iconName);
	// if (buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// } else {
	// huc = new UrlHttpConnection(mContext);
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// return;
	// }
	// Result result = huc.downloadIcon(Constants.IMG_URL
	// + item.iconUrl);
	// buffer = result.getResult();
	// if (result.getState() == UrlHttpConnection.OK && buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// FileUtil.saveBytesToFile(buffer, iconName);
	// }
	// }
	// mHandler.sendMessage(mHandler.obtainMessage());
	// }
	//
	// /**
	// * 优先SD卡读取图片，其次网络读取图片
	// *
	// * @param item
	// */
	// private void loadImage(AppItem item) {
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// return;
	// }
	// String iconName = StringUtil.md5Encoding(Constants.IMG_URL
	// + item.iconUrl);
	// if (iconName == null) {
	// return;
	// }
	// byte[] buffer = FileUtil.getBytesFromFile(iconName);
	// if (buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// } else {
	// huc = new UrlHttpConnection(mContext);
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// return;
	// }
	// Result result = huc.downloadIcon(Constants.IMG_URL
	// + item.iconUrl);
	// buffer = result.getResult();
	// if (result.getState() == UrlHttpConnection.OK && buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// FileUtil.saveBytesToFile(buffer, iconName);
	// }
	// }
	// mHandler.sendMessage(mHandler.obtainMessage());
	// }
	//
	// /**
	// * 优先SD卡读取图片，其次网络读取图片
	// *
	// * @param item
	// */
	// private void loadImage(SubjectItem item) {
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// // item.icon = BitmapUtil
	// // .zoomImg(
	// // BitmapFactory.decodeResource(
	// // mContext.getResources(),
	// // R.drawable.l_sub_default_bg),
	// // (int) (outMetrics.widthPixels - 15 * outMetrics.density));
	// mHandler.sendMessage(mHandler.obtainMessage());
	// return;
	// }
	//
	// String iconName = StringUtil.md5Encoding(Constants.IMG_URL
	// + item.iconUrl);
	// if (iconName == null) {
	// return;
	// }
	// byte[] buffer = FileUtil.getBytesFromFile(iconName);
	// if (buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// } else {
	// huc = new UrlHttpConnection(mContext);
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// return;
	// }
	// Result result = huc.downloadIcon(Constants.IMG_URL
	// + item.iconUrl);
	// buffer = result.getResult();
	// if (result.getState() == UrlHttpConnection.OK && buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// FileUtil.saveBytesToFile(buffer, iconName);
	// }
	// }
	// if (item.icon != null) {
	// item.icon = BitmapUtil
	// .zoomImg(
	// item.icon,
	// (int) (outMetrics.widthPixels - 15 * outMetrics.density));
	// } else
	// item.icon = BitmapUtil
	// .zoomImg(
	// BitmapFactory.decodeResource(
	// mContext.getResources(),
	// R.drawable.l_sub_default_bg),
	// (int) (outMetrics.widthPixels - 15 * outMetrics.density));
	// mHandler.sendMessage(mHandler.obtainMessage());
	// }
	//
	// /**
	// * 优先SD卡读取图片，其次网络读取图片
	// *
	// * @param item
	// */
	// private void loadImage(HLeaderItem item) {
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// return;
	// }
	// String iconName = StringUtil.md5Encoding(Constants.IMG_URL
	// + item.iconUrl);
	// if (iconName == null) {
	// return;
	// }
	// byte[] buffer = FileUtil.getBytesFromFile(iconName);
	// if (buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// } else {
	// huc = new UrlHttpConnection(mContext);
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// return;
	// }
	// Result result = huc.downloadIcon(Constants.IMG_URL
	// + item.iconUrl);
	// buffer = result.getResult();
	// if (result.getState() == UrlHttpConnection.OK && buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// FileUtil.saveBytesToFile(buffer, iconName);
	// }
	// }
	// mHandler.sendMessage(mHandler.obtainMessage());
	// }
	//
	// /**
	// * 优先SD卡读取图片，其次网络读取图片
	// *
	// * @param item
	// */
	// private void loadDetailImage(AppDetail item) {
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// return;
	// }
	// String iconName = StringUtil.md5Encoding(Constants.IMG_URL
	// + item.iconUrl);
	// if (iconName == null) {
	// return;
	// }
	// byte[] buffer = FileUtil.getBytesFromFile(iconName);
	// if (buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// } else {
	// huc = new UrlHttpConnection(mContext);
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// return;
	// }
	// Result result = huc.downloadIcon(Constants.IMG_URL
	// + item.iconUrl);
	// buffer = result.getResult();
	// if (result.getState() == UrlHttpConnection.OK && buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// FileUtil.saveBytesToFile(buffer, iconName);
	// }
	// }
	// mHandler.sendMessage(mHandler.obtainMessage());
	// }
	//
	// /**
	// * 优先SD卡读取图片，其次网络读取图片
	// *
	// * @param item
	// */
	// private void loadImage(CateItem item) {
	// if (item.ImagePath == null || item.ImagePath.trim().equals("")) {
	// return;
	// }
	// String iconName = StringUtil.md5Encoding(Constants.IMG_URL
	// + item.ImagePath);
	// if (iconName == null) {
	// return;
	// }
	// byte[] buffer = FileUtil.getBytesFromFile(iconName);
	// if (buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// } else {
	// huc = new UrlHttpConnection(mContext);
	// if (item.ImagePath == null || item.ImagePath.trim().equals("")) {
	// return;
	// }
	// Result result = huc.downloadIcon(Constants.IMG_URL
	// + item.ImagePath);
	// buffer = result.getResult();
	// if (result.getState() == UrlHttpConnection.OK && buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// FileUtil.saveBytesToFile(buffer, iconName);
	// }
	// }
	// if (item.icon != null && !item.icon.isRecycled()) {
	// item.icon = BitmapUtil
	// .zoomImg(
	// item.icon,
	// (int) ((outMetrics.widthPixels - 15 * outMetrics.density) / 2));
	// }
	// mHandler.sendMessage(mHandler.obtainMessage());
	// }
	//
	// /**
	// * 优先SD卡读取图片，其次网络读取图片
	// *
	// * @param item
	// */
	// private void loadImage(JoySkillItem item) {
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// return;
	// }
	// String iconName = StringUtil.md5Encoding(Constants.IMG_URL
	// + item.iconUrl);
	// if (iconName == null) {
	// return;
	// }
	// byte[] buffer = FileUtil.getBytesFromFile(iconName);
	// if (buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// } else {
	// huc = new UrlHttpConnection(mContext);
	// if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
	// return;
	// }
	// Result result = huc.downloadIcon(Constants.IMG_URL
	// + item.iconUrl);
	// buffer = result.getResult();
	// if (result.getState() == UrlHttpConnection.OK && buffer != null) {
	// item.icon = BitmapUtil.Bytes2Bimap(buffer);
	// FileUtil.saveBytesToFile(buffer, iconName);
	// }
	// }
	// mHandler.sendMessage(mHandler.obtainMessage());
	// }
	// }

}
