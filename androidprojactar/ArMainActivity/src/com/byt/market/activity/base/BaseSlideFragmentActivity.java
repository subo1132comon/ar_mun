package com.byt.market.activity.base;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.byt.ar.R;
import com.byt.market.ui.base.BaseFragment;
import com.byt.market.view.ControlScrollViewPager;

/**
 * 可滑动的基础框架页面,其中TAB是动态去添加的
 */
public abstract class BaseSlideFragmentActivity extends BaseActivity {

	// views
	protected ControlScrollViewPager viewPager;
	private TabHost tabHost;
	private TabWidget tabWidget;

	// vairables
	protected TabsAdapter tabsAdapter;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(getResLayoutId());
		initView();
		initData();
	}

	@Override
	public void initView() {
		viewPager = (ControlScrollViewPager) findViewById(R.id.view_pager);
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		tabWidget = (TabWidget) findViewById(android.R.id.tabs);
	}

	@Override
	public void initData() {
		tabsAdapter = new TabsAdapter(this, tabHost, viewPager);
		fillUpViewPager();
	}

	public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
		private ArrayList<Fragment> fragmentsList;

		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public MyFragmentPagerAdapter(FragmentManager fm,
				ArrayList<Fragment> fragments) {
			super(fm);
			this.fragmentsList = fragments;
		}

		@Override
		public int getCount() {
			return fragmentsList.size();
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragmentsList.get(arg0);
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

	}

	protected static class TabsAdapter extends FragmentPagerAdapter implements
			TabHost.OnTabChangeListener, OnPageChangeListener {
		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private OnPageChangeListener mPageChangeListener;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
		private LayoutInflater mInflater;

		static final class TabInfo {
			public final String tag;
			public final BaseFragment clss;
			public final Bundle args;

			TabInfo(String _tag, BaseFragment _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabsAdapter(FragmentActivity activity, TabHost tabHost,
				ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mInflater = LayoutInflater.from(mContext);
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, BaseFragment clss,
				Bundle args) {
			try {
				tabSpec.setContent(new DummyTabFactory(mContext));
				String tag = tabSpec.getTag();
				if (TextUtils.isEmpty(tag)) {
					return;
				}
				mTabHost.addTab(tabSpec);
				if (clss != null) {
					if (clss.isEmptyFragment()) {
						return;
					}
					TabInfo info = new TabInfo(tag, clss, args);
					mTabs.add(info);
					notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return info.clss;
		}

		@Override
		public void onTabChanged(String tabId) {
			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			if (mPageChangeListener != null) {
				mPageChangeListener.onPageScrolled(position, positionOffset,
						positionOffsetPixels);
			}
		}

		@Override
		public void onPageSelected(int position) {
			TabWidget widget = mTabHost.getTabWidget();
			int oldFocusability = widget.getDescendantFocusability();
			widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			mTabHost.setCurrentTab(position);
			widget.setDescendantFocusability(oldFocusability);
			
			for (int i = 0; i < getCount(); i++) {
				View tabView = widget.getChildTabViewAt(i);
				View line = tabView.findViewById(R.id.tab_line1);
				TextView tab_textview = (TextView) tabView
						.findViewById(R.id.tab_textview);
				/*modify by zengxiao for:修改tab栏字体颜色，下拉条颜色，去掉多余的分割线*/
				ImageView tab_imageview = (ImageView) tabView
						.findViewById(R.id.tab_divider);
				if(i==getCount()-1)
				{
					tab_imageview.setVisibility(View.GONE);
				}
				if (i != position) {
					tab_textview.setSelected(false);
					line.setVisibility(View.INVISIBLE);
				} else {
					tab_textview.setSelected(true);
					line.setVisibility(View.VISIBLE);
				}
					/*modify end */
			}
			if (mPageChangeListener != null) {
				mPageChangeListener.onPageSelected(position);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (mPageChangeListener != null) {
				mPageChangeListener.onPageScrollStateChanged(state);
			}
		}

		public void initViewPagerAdapter(List<BaseFragment> list) {
			mTabHost.clearAllTabs();
			for (int i = 0; i < list.size(); i++) {
				String title = (String) list.get(i).getTabTittle();
				TabView tabview = new TabView(R.layout.top_viewpager_tab_item,
						mInflater);
				tabview.setTitle(title);
				final BaseFragment bf = list.get(i);
				if (bf.isEmptyFragment()) {
					tabview.setTextColor(Color.parseColor("#77858d"));
				}
				addTab(mTabHost.newTabSpec(title).setIndicator(
						tabview.getView()), bf, null);
			}
			onPageSelected(0);
		}

		/*
		 * 设置空的滑动选项卡的选项标题
		 */
		public void initViewPagerTitlebar(List<String> list) {
			mTabHost.clearAllTabs();
			for (int i = 0; i < list.size(); i++) {
				TabView tabview = new TabView(R.layout.top_viewpager_tab_item,
						mInflater);
				tabview.setTitle(list.get(i));
				addTab(mTabHost.newTabSpec(list.get(i)).setIndicator(
						tabview.getView()), null, null);
			}
		}

		public void setViewPagerSingleTitle(int index, String title) {
			View tabview = mTabHost.getTabWidget().getChildAt(index);
			TextView tab_textview = (TextView) tabview
					.findViewById(R.id.tab_textview);
			tab_textview.setText(title);
		}

		public void setOnPageChangeListener(
				OnPageChangeListener onPageChangeListener) {
			mPageChangeListener = onPageChangeListener;
		}

		static private class TabView {
			private View mView;
			private TextView tab_textview;

			public View getView() {
				return mView;
			}

			public TabView(int layoutId, LayoutInflater mInflater) {
				mView = mInflater.inflate(layoutId, null);
				tab_textview = (TextView) mView.findViewById(R.id.tab_textview);

			}

			public void setTitle(String title) {
				tab_textview.setText(title);
			}

			public void setTextColor(int colors) {
				tab_textview.setTextColor(colors);
			}

		}

	}

	public void setViewPagerCurrentItem(int index) {
		if (tabsAdapter != null)
			tabsAdapter.onPageSelected(index);
	}

	public int getViewPagerCurrentItem() {
		if (viewPager != null)
			return viewPager.getCurrentItem();
		else
			return 0;
	}

	public void setTabGroupVisible(int visible) {
		if (tabWidget != null)
			tabWidget.setVisibility(visible);
	}

	/**
	 * 布局ID,由子类提供
	 */
	protected abstract int getResLayoutId();

	/**
	 * 填充ViewPager
	 */
	protected abstract void fillUpViewPager();

}
