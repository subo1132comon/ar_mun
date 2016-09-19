package com.byt.market.adapter;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;

import com.byt.market.qiushibaike.news.NewsTextImageFragment;
import com.byt.market.qiushibaike.news.NewsTextImageFragment2;
import com.byt.market.qiushibaike.ui.JokeTextImageFragment;
import com.byt.market.ui.AVFragment;
import com.byt.market.ui.CateFragment;
import com.byt.market.ui.CateMainFragment;
import com.byt.market.ui.HomeFragment;
import com.byt.market.ui.JokeFragment;
import com.byt.market.ui.ListViewFragment;
import com.byt.market.ui.ManagerFragment;
import com.byt.market.ui.MineFragment;
import com.byt.market.ui.RankMainFragment;
import com.byt.market.ui.RankTotalFragment;
import com.byt.market.ui.SubFragment;
import com.byt.market.ui.base.BaseUIFragment;

public class MainFrameAdapter extends FragmentPagerAdapter {

	FragmentManager fm;
	public List<Fragment> list;
	//boolean[] fragmentsUpdateFlag = { false, false, false, false };
	public MainFrameAdapter(FragmentManager fm) {
		super(fm);
		this.fm = fm;
		list = new ArrayList<Fragment>();
		list.add(BaseUIFragment.newInstance(HomeFragment.class));
		//list.add(BaseUIFragment.newInstance(CateMainFragment.class));
		list.add(BaseUIFragment.newInstance(JokeFragment.class));
		//list.add(BaseUIFragment.newInstance(RankMainFragment.class));
		list.add(BaseUIFragment.newInstance(NewsTextImageFragment2.class));
		list.add(BaseUIFragment.newInstance(AVFragment.class));
		//list.add(BaseUIFragment.newInstance(SubFragment.class));
		list.add(BaseUIFragment.newInstance(ManagerFragment.class));
	}

	@Override
	public Fragment getItem(int position) {
		if (position < list.size())
			return list.get(position);
		return null;
		
//		Fragment fragment = list.get(position % list.size());
//		return list.get(position % list.size());
		
	}

	@Override
	public int getCount() {
		return 5;
	}

//	 @Override
//	public int getItemPosition(Object object) {
//		// TODO Auto-generated method stub
//		return POSITION_NONE;
//	} 
	
//	@Override
//	public Object instantiateItem(ViewGroup container, int position) {
//		//得到缓存的fragment
//		Fragment fragment = (Fragment) super.instantiateItem(container,
//				position);
//		//得到tag，这点很重要
//		String fragmentTag = fragment.getTag();			
//
//		if (fragmentsUpdateFlag[position % fragmentsUpdateFlag.length]) {
//			//如果这个fragment需要更新
//			
//			FragmentTransaction ft = fm.beginTransaction();
//			//移除旧的fragment
//			ft.remove(fragment);
//			//换成新的fragment
//			fragment = list.get(position % list.size());
//			//添加新fragment时必须用前面获得的tag，这点很重要
//			ft.add(container.getId(), fragment, fragmentTag);
//			ft.attach(fragment);
//			ft.commit();
//			
//			//复位更新标志
//			fragmentsUpdateFlag[position % fragmentsUpdateFlag.length] = false;
//		}
//
//		return fragment;
//	}
	
	/**
	 * 清除图片
	 * 
	 * @param vp
	 */
	public void clearFragmentIcon(ViewPager vp) {
		int RANGENULL = 2;// 清除距离多远的图片
		int item = vp.getCurrentItem();
		int pre = item - RANGENULL;
		int next = item + RANGENULL;
		if (pre >= 0) {
			for (int i = 0; i < pre; i++) {
				Fragment fragment = getItem(i);
				if (fragment instanceof ListViewFragment) {
					ImageAdapter adapter = ((ListViewFragment) fragment)
							.getAdapter();
					adapter.clearListIconAll(((ListViewFragment) fragment)
							.getListView());
				}
			}
		}
		if (next < getCount()) {
			for (int i = next; i < getCount(); i++) {
				Fragment fragment = getItem(i);
				if (fragment instanceof ListViewFragment) {
					ImageAdapter adapter = ((ListViewFragment) fragment)
							.getAdapter();
					adapter.clearListIconAll(((ListViewFragment) fragment)
							.getListView());
				}
			}
		}
	}

	/**
	 * 加载图片
	 * 
	 * @param vp
	 */
	public void loadFragmentIcon(ViewPager vp) {
		Fragment item = getItem(vp.getCurrentItem());
		try {
			if (item instanceof ListViewFragment) {
				((ListViewFragment) item).getAdapter().loadIcon(
						((ListViewFragment) item).getListView());
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

	}
}
