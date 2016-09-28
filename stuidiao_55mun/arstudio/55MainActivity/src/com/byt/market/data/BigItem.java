package com.byt.market.data;

import java.io.Serializable;
import java.util.ArrayList;

import com.byt.market.mediaplayer.data.VideoItem;

public class BigItem implements Serializable{

    /**
     * 首页导航列表数据
     */
    public ArrayList<HLeaderItem> leaderItems = new ArrayList<HLeaderItem>();
    /**
     * 首页推荐列表数据
     */
    public ArrayList<AppItem> recItems = new ArrayList<AppItem>();
    /**
     * 首页推荐列表数据
     */
    public ArrayList<VideoItem> videoItems = new ArrayList<VideoItem>();
    public ArrayList<VideoItem> videoHeadItems = new ArrayList<VideoItem>();
	/**
     * 首页列表数据
     */
    public ArrayList<AppItem> homeItems = new ArrayList<AppItem>();
    /**
     * 首页列表三行展示 add  by bobo
     */
    public ArrayList<HomeItemBean> homeItembeas = new ArrayList<HomeItemBean>();
    /**
     * 首页随机广告数据
     */
    public ArrayList<AppItem> homeADItems = new ArrayList<AppItem>();
    /**
     * 排行列表数据
     */
    public ArrayList<AppItem> rankItems = new ArrayList<AppItem>();
    /**
     * 专题列表数据
     */
    public ArrayList<SubjectItem> subjectItems = new ArrayList<SubjectItem>();
    /**
     * 分类列表数据
     */
    public ArrayList<CateItem> cateItems = new ArrayList<CateItem>();
    /**
     * 最新收录列表数据
     */
    public ArrayList<AppItem> newItems = new ArrayList<AppItem>();
    /**
     * 新手推荐列表数据
     */
    public ArrayList<AppItem> newUserItems = new ArrayList<AppItem>();
    /**
     * 二级列表数据
     */
    public ArrayList<AppItem> subListItems = new ArrayList<AppItem>();
    
    /**
     * 我的游戏列表数据
     */
    public ArrayList<AppItem> localItems = new ArrayList<AppItem>();
    /**
     * 游戏详情列表
     */
    public ArrayList<AppDetail> details = new ArrayList<AppDetail>();
    /**
     * 游戏全部评论列表
     */
    public ArrayList<AppComment> comments = new ArrayList<AppComment>();
    /**
     * 搜索热词列表
     */
    public ArrayList<SearchHotword> hotwords = new ArrayList<SearchHotword>();
    /**
     * 搜索热词列表
     */
    public ArrayList<SearchHistory> historys = new ArrayList<SearchHistory>();
    /**
     * 搜索联想词列表
     */
    public ArrayList<SearchHint> thinks = new ArrayList<SearchHint>();
    /**
     * 搜索结果列表数据
     */
    public ArrayList<AppItem> resultItems = new ArrayList<AppItem>();
    //铃声结果列表数据 
    public ArrayList<RingItem> ringhomeItems = new ArrayList<RingItem>();
    
    /**
     * 攻略列表数据
     */
    public ArrayList<JoySkillItem> joySkillItems = new ArrayList<JoySkillItem>();
    
    /**
     * 大家都在玩
     * 
     */
    public ArrayList<AppItem> splashRecItems = new ArrayList<AppItem>();
    
    /**
     * 列表项类型
     */
    public int layoutType = Type.LAYOUT_APPLIST_ONE;
    
    /**
     * listview 列表项类型
     * @author qiuximing
     *
     */
    public class Type{
    	
    	
    	/**
    	 * 新闻 大图
    	 */
    	public static final int LAYOUT_CAEGORYBIG_IC = 133;
    	
    	/**
    	 * 新闻 间隔
    	 */
    	public static final int LAYOUT_CAEGORYSPES = 132;
    	
        /**
         * 排行列表类型
         */
        public static final int LAYOUT_RANKLIST = 11;
        /**
         * 专题列表类型
         */
        public static final int LAYOUT_SUBJECTLIST = 12;
        /**
         * 分类列表类型
         */
        public static final int LAYOUT_CAEGORYLIST = 13;
        /**
         * add  by  bobo
         * 显示 切换天
         */
        public static final int LAYOUT_CHANGE = 103;
        
        /**
         * 最新收录列表类型
         */
        public static final int LAYOUT_NEWITEMLIST = 14;
        /**
         * 新手推荐列表类型
         */
        public static final int LAYOUT_NEWUSERLIST = 15;
        /**
         * 二级列表列表类型
         */
        public static final int LAYOUT_SUBLIST = 16;
        /**
         * 详情列表
         */
        public static final int LAYOUT_DETAILS = 18;
        /**
         * 评论列表
         */
        public static final int LAYOUT_COMMENTS = 19;
        /**
         * 搜索热词
         */
        public static final int LAYOUT_HOTWORDS = 20;
        /**
         * 搜索历史词
         */
        public static final int LAYOUT_HISTORY = 21;
        /**
         * 搜索联想词
         */
        public static final int LAYOUT_THINK = 22;
        /**
         * 搜索结果安全认证
         */
        public static final int LAYOUT_SEARCH_SAFE = 23;
        /**
         * 搜索结果相关
         */
        public static final int LAYOUT_SEARCH_RELA = 24;
        /**
         * 搜索无结果
         */
        public static final int LAYOUT_SEARCH_NO_RESULT = 25;
        /**
         * 搜索结果标题
         */
        public static final int LAYOUT_SEARCH_TITLE = 26;
        /**
         * 攻略列表
         */
        public static final int LAYOUT_JOY_SKILL = 27;
        
        /**
         * 大家都在玩
         */
        public static final int LAYOUT_APPLIST_REC_TWO = 30;
        /**
         * 二级列表列表类型
         */
        public static final int LAYOUT_MUSICSUBLIST = 31;
        public static final int LAYOUT_VIDEOCAEGORYLIST = 32;
        public static final int LAYOUT_VIDEOHEADLIST = 33;
        
        /**
         * 我的游戏列表类型
         */
        public static final int LAYOUT_MYGAME1 = 171;
        public static final int LAYOUT_MYGAME2 = 172;
        public static final int LAYOUT_MYGAME3 = 173;
        public static final int LAYOUT_MYGAME4 = 174;
        
        
        
        /**
         * 普通单列列表（首页三列列表，只显示一列）
         */
        public static final int LAYOUT_APPLIST_ONE = 1;
        /**
         * 首页三列列表，只显示两列
         */
        public static final int LAYOUT_APPLIST_TWO = 2;
        /**
         * 首页三列列表，显示三列
         */
        public static final int LAYOUT_APPLIST_THREE = 3;
        /**
         * 导航三列列表
         */
        public static final int LAYOUT_LEADER_THREE = 4;
        /**
         * 轮播三列列表
         */
        public static final int LAYOUT_APPLIST_MUTILE = 5;
        
        
        /**
         * 正在加载
         */
        public static final int LAYOUT_LOADING = -2;
        /**
         * 加载失败
         */
        public static final int LAYOUT_LOADFAILED = -1;
        /**
         * 错误类型
         */
        public static final int LAYOUT_ERROR = -100;
    }
}
