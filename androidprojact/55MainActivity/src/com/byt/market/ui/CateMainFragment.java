package com.byt.market.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.activity.DownLoadManageActivity;
import com.byt.market.activity.MainActivity;
import com.byt.market.ui.base.BaseUIFragment;
import com.byt.market.view.AlwsydMarqueeTextView;

/**
 * 游戏总页面 */
public class CateMainFragment extends BaseUIFragment implements OnClickListener, OnPageChangeListener {
	private View recArea, cateArea,rankArea;
	private GameCateFragment cateFragment;
	private RecGameFragment recFragment;
	private RankWeekFragment rankFragment;
	private View tab_line1,tab_line2,tab_line3;
	Button buttonrec,buttoncate,buttonrank;
	ViewPager mPager;
    private ArrayList<BaseUIFragment> fragmentList;  

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.cate_main_fragment, container, false);
		initView(view);
		initPaper(view);
		return view;
	}

	private void initPaper(View view) {
		 mPager = (ViewPager)view.findViewById(R.id.viewpager);  
	        fragmentList = new ArrayList<BaseUIFragment>();  
	    	cateFragment = new GameCateFragment();
			recFragment = new RecGameFragment();
			rankFragment=new RankWeekFragment();
			fragmentList.clear();
	        fragmentList.add(recFragment);  
	        fragmentList.add(cateFragment);  
	        fragmentList.add(rankFragment);  
	              //给ViewPager设置适配器  	       
	        mPager.setAdapter(new MyFragmentPagerAdapter(this.getActivity().getSupportFragmentManager(), fragmentList));  
	        mPager.setCurrentItem(0);//设置当前显示标签页为第一页  
	        mPager.setOnPageChangeListener(this);//页面变化时的监听器  
		
	}

	private void initView(View view) {
		try {
			/*bestone add by "zengxiao" for:添加标题头
			view.findViewById(R.id.titlebar_title).setVisibility(View.GONE);
			view.findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
			view.findViewById(R.id.iv_settings).setVisibility(View.GONE);
			view.findViewById(R.id.search_page_view).setVisibility(View.VISIBLE);
			view.findViewById(R.id.search_page_view).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					CateMainFragment.this.getActivity().startActivity(new Intent(Constants.TOSEARCH));
					CateMainFragment.this.getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);	
				}
			});
			view.findViewById(R.id.top_downbutton).setVisibility(View.VISIBLE);
			view.findViewById(R.id.top_downbutton).setOnClickListener(this);//添加下载界面按钮
			view.findViewById(R.id.titlebar_applist_button_container).setVisibility(View.GONE);
			bestone add end*/
			
			buttonrec=(Button) view.findViewById(R.id.bt_rec_button);
			buttonrec.setSelected(true);
			buttonrec.setOnClickListener(this);
			buttoncate=(Button) view.findViewById(R.id.bt_cate_button);
			buttoncate.setOnClickListener(this);
			buttonrank=(Button) view.findViewById(R.id.bt_rank_button);
			buttonrank.setOnClickListener(this);
			recArea = view.findViewById(R.id.game_rec_area);
			tab_line2= view.findViewById(R.id.cate_tab_line2);
			tab_line1= view.findViewById(R.id.cate_tab_line1);
			tab_line3= view.findViewById(R.id.cate_tab_line3);
			tab_line2.setVisibility(View.INVISIBLE);
			tab_line3.setVisibility(View.INVISIBLE);
			tab_line1.setVisibility(View.VISIBLE);
			recArea.setVisibility(View.VISIBLE);
			cateArea.setVisibility(View.GONE);
			rankArea.setVisibility(View.GONE);
		
		} catch (Exception e) {
			Log.d("myzx","initViewerror");
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View view) {
		try {
			switch (view.getId()) {
			case R.id.bt_rec_button:
				 mPager.setCurrentItem(0);
//				 onMYPageChange(0);
				break;
			case R.id.bt_rank_button:
				 mPager.setCurrentItem(2);
//				 onMYPageChange(2);
				break;
			case R.id.bt_cate_button:
				 mPager.setCurrentItem(1);
//				 onMYPageChange(1);
				break;
			/*case R.id.top_downbutton:
				Intent downloadIntent = new Intent(this.getActivity(),
						DownLoadManageActivity.class);
				downloadIntent.putExtra(DownLoadManageActivity.TYPE_FROM,
						DownLoadManageActivity.TYPE_FROM_DOWNLOAD);
				this.getActivity().startActivity(downloadIntent);
				this.getActivity().overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
				break;*/

			default:
				break;
			}
		} catch (Exception e) {
			Log.d("myzx","ionClickViewerror");
			e.printStackTrace();
		}
	}

	public void requestDelay() {
		try {
			if (recFragment != null) {
				recFragment.request();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public class MyFragmentPagerAdapter extends FragmentPagerAdapter{  
        ArrayList<BaseUIFragment> list;  
        public MyFragmentPagerAdapter(FragmentManager fm,ArrayList<BaseUIFragment> list) {  
            super(CateMainFragment.this.getChildFragmentManager());  
            this.list = list;  
              
        }  
          
          
        @Override  
        public int getCount() {  
            return list.size();  
        }  
          
        @Override  
        public BaseUIFragment getItem(int arg0) {  
            return list.get(arg0);  
        }  
          
          
          
          
    }
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		 switch(arg0){
         case 0:
        	onMYPageChange(0);
         	break;
         case 1:
        	 onMYPageChange(1);
         	 break;
              
               
         case 2:
        	 onMYPageChange(2);
         	break;
        
         }		
	}  
	private void onMYPageChange(int page) {
		switch(page)
		{
		case 0:
			 buttonrec.setSelected(true);
				buttoncate.setSelected(false);
				buttonrank.setSelected(false);
				requestDelay();
				tab_line3.setVisibility(View.INVISIBLE);
				tab_line2.setVisibility(View.INVISIBLE);
				tab_line1.setVisibility(View.VISIBLE);
        	break;
        case 1:
        	buttonrec.setSelected(false);
			buttoncate.setSelected(true);
			buttonrank.setSelected(false);
			tab_line1.setVisibility(View.INVISIBLE);
			tab_line3.setVisibility(View.INVISIBLE);
			tab_line2.setVisibility(View.VISIBLE);
			if (cateFragment != null) {
				cateFragment.request();
			}
        	 break;
             
              
        case 2:
        	buttonrec.setSelected(false);
			buttoncate.setSelected(false);
			buttonrank.setSelected(true);
			tab_line1.setVisibility(View.INVISIBLE);
			tab_line3.setVisibility(View.VISIBLE);
			tab_line2.setVisibility(View.INVISIBLE);
			if (rankFragment!= null) {
				rankFragment.request();
			}
        break;
		}

	}

}
