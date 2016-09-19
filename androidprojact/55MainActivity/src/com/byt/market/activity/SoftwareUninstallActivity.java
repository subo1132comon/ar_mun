package com.byt.market.activity;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.market.R;
import com.byt.market.activity.base.BaseActivity;
import com.byt.market.data.AppItem;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.download.DownloadException;
import com.byt.market.download.DownloadTaskListener;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.util.DateUtil;
import com.byt.market.util.PackageUtil;
import com.byt.market.util.SystemUtil;
import com.byt.market.util.TextUtil;
import com.byt.market.util.Util;
import com.byt.market.view.AppUninstallDialog;
import com.byt.market.view.CusPullListView;
import com.byt.market.view.CustomListView;
import com.byt.market.view.LoadFailedView;

/**
 * 应用卸载
 */
public class SoftwareUninstallActivity extends BaseActivity implements
		DownloadTaskListener, OnClickListener {

	// views
	private TextView tvTitle;
	private LoadFailedView emptyView;
	private Button findMoreGame;
	protected View loading;
	protected View loadfailed;
	protected CusPullListView mListView;
	protected LayoutInflater inflater;
	protected AppUnistallAdapter mAdapter;
	public TextView listview_loadfailed_text;
	private ImageView listview_loadfailed_icon;
	private InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
	static PackageManager mPm;
	List<AppEntry> mApps = new ArrayList<AppEntry>();
	private int sort_type = 1;// 排序方式 1.名称 3.大小 2.时间 4.卸载

	private ImageView imgTitleSort;
	private String uninstallname="";
	private ImageView selectAllImg;
	private Button selectAllBtn;
	boolean isSelectAll = false;
	private List<AppEntry> checkItems = new ArrayList<AppEntry>();
	private Button moreUninstallBtn;

	private PopupWindow popupWindow;

	private AppUninstallDialog moreUninstallDialog;
	private AppUninstallDialog singleUninstallDialog;

	private boolean isMoreUninstall = false;

	/** 对应排序的选中图标 **/
	private ImageView icon_sort_size;
	private ImageView icon_sort_name;
	private ImageView icon_sort_date;
	private ImageView iconTitleSort;
	private ImageView iconUninstall;
	private View bottomArea;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_app_uninstall_layout);
		inflater = LayoutInflater.from(this);
		initView();
		initPopu();
		mPm = getPackageManager();
		moreUninstallDialog = new AppUninstallDialog(this);
		singleUninstallDialog = new AppUninstallDialog(this);
		DownloadTaskManager.getInstance().addListener(this);
	}

	protected void initPopu() {
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(
				R.layout.layout_uninstall_top_popu, null, true);

		popupWindow = new PopupWindow(menuView, SystemUtil.dip2px(this, 120),
				LinearLayout.LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		icon_sort_size = (ImageView) menuView.findViewById(R.id.icon_sort_size);
		icon_sort_date = (ImageView) menuView.findViewById(R.id.icon_sort_date);
		icon_sort_name = (ImageView) menuView.findViewById(R.id.icon_sort_name);
		iconUninstall= (ImageView) menuView.findViewById(R.id.icon_uninstall);
		setOnclickEvent(menuView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(uninstallname!=null&&!uninstallname.equals(""))
		{
			for (AppEntry app : mApps) {
				if (app.getPackageName().equals(uninstallname)) {
					mApps.remove(app);
					break;
				}
			}
			for (AppEntry checkApp : checkItems) {
				if (checkApp.getPackageName().equals(uninstallname)) {
					checkItems.remove(checkApp);
					position--;
					break;
				}
			}
		
			
			initMoreBtnText(checkItems.size());
			//showShortToast(getString(R.string.unstallsucc));
			mAdapter.notifyDataSetChanged();
			
			if (isMoreUninstall) {
				if (checkItems.size() > 0&&position<checkItems.size()) {
					PackageUtil.uninstallApp(MyApplication.getInstance(),
							checkItems.get(position).getPackageName(), "", false);
					position++;
				} else {
					isMoreUninstall = false;
					position=0;
				}
			}
		}else{
			if (isMoreUninstall) {
				if (checkItems.size() > 0 && position<=checkItems.size()-1) {
					PackageUtil.uninstallApp(MyApplication.getInstance(),
							checkItems.get(position).getPackageName(), "", false);
					position++;
				} else {
					isMoreUninstall = false;
					position = 0;
				}
			}
			
			uninstallname="";
		}
		DownloadTaskManager
		.getInstance().clearallnotopen();

	}

	protected void showPopu(View view) {
		popupWindow.showAsDropDown(view, SystemUtil.dip2px(this, -10),
				SystemUtil.dip2px(this, -6));
		popupWindow.update();
		switch (sort_type) {
		case 1:
			icon_sort_name.setVisibility(View.VISIBLE);
			icon_sort_date.setVisibility(View.INVISIBLE);
			icon_sort_size.setVisibility(View.INVISIBLE);
			iconUninstall.setVisibility(View.INVISIBLE);
			break;
		case 2:
			icon_sort_name.setVisibility(View.INVISIBLE);
			icon_sort_date.setVisibility(View.INVISIBLE);
			icon_sort_size.setVisibility(View.VISIBLE);
			iconUninstall.setVisibility(View.INVISIBLE);
			break;
		case 3:
			icon_sort_name.setVisibility(View.INVISIBLE);
			icon_sort_date.setVisibility(View.VISIBLE);
			icon_sort_size.setVisibility(View.INVISIBLE);
			iconUninstall.setVisibility(View.INVISIBLE);
			break;
		case 4:
			icon_sort_name.setVisibility(View.INVISIBLE);
			icon_sort_date.setVisibility(View.INVISIBLE);
			icon_sort_size.setVisibility(View.INVISIBLE);
			iconUninstall.setVisibility(View.VISIBLE);
			break;
		}
	}

	public void initView() {
		bottomArea=findViewById(R.id.uninstall_bottom);
		/** init title bar **/
		findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		findViewById(R.id.titlebar_back_arrow).setVisibility(View.VISIBLE);
		iconTitleSort = (ImageView) findViewById(R.id.iv_settings);
		iconTitleSort.setImageResource(R.drawable.icon_sort);
		iconTitleSort.setVisibility(View.GONE);
		iconTitleSort.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				showPopu(view);
			}
		});
		findViewById(R.id.titlebar_icon).setVisibility(View.GONE);
		TextView title = (TextView) findViewById(R.id.titlebar_title);
		title.setText(R.string.app_managerst);
		findViewById(R.id.titlebar_search_button).setVisibility(View.GONE);
		findViewById(R.id.titlebar_back_arrow).setOnClickListener(this);
		/** init title bar end **/

		listview_loadfailed_text = (TextView) findViewById(R.id.listview_loadfailed_text);
		listview_loadfailed_icon = (ImageView) findViewById(R.id.listview_loadfailed_icon);
		mListView = (CusPullListView) findViewById(R.id.listview);
		moreUninstallBtn = (Button) findViewById(R.id.btn_more_uninstall);

		moreUninstallBtn.setOnClickListener(this);
		initMoreBtnText(0);
		mAdapter = new AppUnistallAdapter();
		findMoreGame = (Button) findViewById(R.id.btn_find_game);
		findMoreGame.setOnClickListener(this);
		emptyView = (LoadFailedView) findViewById(R.id.listview_loadfailed);
		emptyView.setText(getString(R.string.listview_loadding4));
		emptyView.setButtonVisible(View.GONE);
		loading = findViewById(R.id.listview_loading);
		loadfailed = findViewById(R.id.listview_loadfailed);
	//	selectAllImg = (ImageView) findViewById(R.id.check_select_all);
		selectAllBtn = (Button) findViewById(R.id.btn_select_all);
		selectAllBtn.setOnClickListener(SelectAllListener);

		loadfailed.setVisibility(View.GONE);
		emptyView.setVisibility(View.GONE);
		mListView.setAdapter(mAdapter);
		/*mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long arg3) {
				AppEntry entry = mAdapter.getItem(position-1);
				if(sort_type==4){
					uninstallApp(entry, v);
				}else {
					runApp(entry);
				}
			}

		});*/
		new LoadDataTask().execute();
	}
	private void uninstallApp(AppEntry entry,View v){
		boolean isSelected = entry.isSelected;
		ImageView checkBox = (ImageView) v.findViewById(R.id.img_check);
		if (isSelected) {
			checkBox.setSelected(false);
			checkItems.remove(entry);
		} else {
			checkBox.setSelected(true);
			checkItems.add(entry);
		}
		/*modify by zengxiao */
		if (checkItems.size() == mApps.size()) {
			isSelectAll = true;
			selectAllBtn.setText(R.string.cance_all);
		
		} else {
			selectAllBtn.setText(R.string.select_all);
			isSelectAll = false;
			
		}		
		/*modify end*/
		initMoreBtnText(checkItems.size());
		entry.isSelected = !isSelected;
	}
	private void runApp(AppEntry entry){
		try {
			AppItem appItem=null;
			List<AppItem> mapp=DownloadTaskManager.getInstance().getAllInstalledApps();
			for(AppItem item :mapp)
			{
				if(item.pname.equals((entry.getPackageName())))
						{
					appItem=item;
						}
			}
			if(appItem!=null)
			{
			boolean isReload = false;
			if (appItem.isInstalledButNotLauchered(this)) {
				isReload = true;
			}
			appItem.isOpenned = 1;
			DownloadTaskManager.getInstance().updateAppItem(appItem);
			}
			Intent intent = this.getPackageManager().getLaunchIntentForPackage(entry.packageName);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
			        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.no_run_app_alert, Toast.LENGTH_SHORT).show();
		}
	}

	public void initMoreBtnText(int count) {
		if (count == 0) {
			moreUninstallBtn.setEnabled(false);
			
			moreUninstallBtn.setText(getString(R.string.unstallmush));
		} else {
			moreUninstallBtn.setEnabled(true);
			
			moreUninstallBtn.setText(String
					.format(getResources()
							.getString(R.string.btn_all_uninstall), count));
		}
	}

	public void setOnclickEvent(ViewGroup menuView) {
		// 按名称排序
		LinearLayout btnSortName = (LinearLayout) menuView
				.findViewById(R.id.lay_sort_name);
		btnSortName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByName();
				popupWindow.dismiss();
			}
		});
		// 按大小排序
		LinearLayout btnSortSize = (LinearLayout) menuView
				.findViewById(R.id.lay_sort_size);
		btnSortSize.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortBySize();
				popupWindow.dismiss();
			}
		});
		// 按日期排序
		LinearLayout btnSortDate = (LinearLayout) menuView
				.findViewById(R.id.lay_sort_date);
		btnSortDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByInstallDate();
				popupWindow.dismiss();
			}
		});
		// 卸载
		LinearLayout uninstallType = (LinearLayout) menuView
				.findViewById(R.id.menu_uninstall);
		uninstallType.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showUninstall();
				popupWindow.dismiss();
			}
		});
	}

	class LoadDataTask extends AsyncTask<Void, Void, List<AppEntry>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected List<AppEntry> doInBackground(Void... params) {
			// TODO Auto-generated method stub
			List<PackageInfo> apps = getPackageManager()
					.getInstalledPackages(0);
			if (apps == null) {
				apps = new ArrayList<PackageInfo>();
			}

			// Create corresponding array of entries and load their labels.
			List<AppEntry> entries = new ArrayList<AppEntry>();
			for (int i = 0; i < apps.size(); i++) {
				AppEntry entry = new AppEntry(SoftwareUninstallActivity.this,
						apps.get(i));				
				entry.packageName=apps.get(i).packageName;
				entry.loadLabel(SoftwareUninstallActivity.this);
				entry.alpha = getAlpha(TextUtil.converterToFirstSpell(entry
						.getLabel()));
				if ((entry.getIsUser()||((entry.mInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)!= 0))
						&& !entry.getPackageName().contains(
								Constants.PACKAGENAME_NAME)) {	
					
					entries.add(entry);
				}
			}

			// Sort the list.
			Collections.sort(entries, ALPHA_COMPARATOR);
			return entries;
		}

		@Override
		protected void onPostExecute(List<AppEntry> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == null || result.size() == 0) {
				iconTitleSort.setVisibility(View.GONE);
				moreUninstallBtn.setEnabled(false);
				moreUninstallBtn.setText(getString(R.string.unstallmush));
				showNoResultView();
				return;
			}
			iconTitleSort.setVisibility(View.GONE);
			loading.setVisibility(View.GONE);
			mApps.clear();
			Collections.sort(result, ALPHA_COMPARATOR);
			mApps.addAll(result);
			for (int i = 0; i < mApps.size(); i++) {
			try {
				queryPacakgeSize(mApps.get(i).mInfo.packageName);
				
			} catch (Exception e) {
				
			}
			}
			try {
			Thread.sleep(1000);
			}catch(Exception e)
			{}
			
			mAdapter.notifyDataSetChanged();

		}

	}

	public void sortByInstallDate() {
		bottomArea.setVisibility(View.GONE);
		sort_type = 3;
		Collections.sort(mApps, new DateCompareBle());
		mAdapter.notifyDataSetChanged();
	}

	public void sortByName() {
		bottomArea.setVisibility(View.GONE);
		sort_type = 1;
		Collections.sort(mApps, ALPHA_COMPARATOR);
		mAdapter.notifyDataSetChanged();
	}

	public void sortBySize() {
		bottomArea.setVisibility(View.GONE);
		sort_type = 2;
		Collections.sort(mApps, new SizeCompareBle());
		mAdapter.notifyDataSetChanged();
	}
	
	public void showUninstall() {
		sort_type = 4;
		bottomArea.setVisibility(View.VISIBLE);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * This class holds the per-item data in our Loader.
	 */
	public static class AppEntry {

		private final PackageInfo mInfo;
		private final File mApkFile;
		private String mLabel;
		private Drawable mIcon;
		private boolean mMounted;
		private boolean isUserApp;
		private Context mContext;
		private String versionName;
		private String packageName;
		private long apkSize;
		private int startCount;
		public boolean isSelected;
		public String alpha;
		public float packgeSize;
		public String userTime;
		
		public String getUserTime() {
			return userTime;
		}

		public void setUserTime(String userTime) {
			this.userTime = userTime;
		}

		public float getPackgeSize() {
			return packgeSize;
		}

		public void setPackgeSize(float packgeSize) {
			this.packgeSize = packgeSize;
		}

		public AppEntry(Context context, PackageInfo info) {
			this.mContext = context;
			mInfo = info;
			mApkFile = new File(info.applicationInfo.sourceDir);
		}

		public int getStartCount() {
			return startCount;
		}

		public PackageInfo getPackageInfo() {
			return mInfo;
		}

		public String getVersionName() {
			return mInfo.versionName;
		}

		public String getPackageName() {
			return mInfo.packageName;
		}

		public long getSize() {
			if (mApkFile.exists()) {
				return mApkFile.length();
			}
			return 0;
		}

		public String getLabel() {
			return mLabel;
		}

		public Drawable getIcon() {
			if (mIcon == null) {
				if (mApkFile.exists()) {
					mIcon = mInfo.applicationInfo.loadIcon(mPm);
					return mIcon;
				} else {
					mMounted = false;
				}
			} else if (!mMounted) {
				// If the app wasn't mounted but is now mounted, reload
				// its icon.
				if (mApkFile.exists()) {
					mMounted = true;
					mIcon = mInfo.applicationInfo.loadIcon(mPm);
					return mIcon;
				}
			} else {
				return mIcon;
			}

			return mContext.getResources().getDrawable(
					android.R.drawable.sym_def_app_icon);
		}

		public boolean getIsUser() {
			this.isUserApp = (mInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
			return isUserApp;
		}

		@Override
		public String toString() {
			return mLabel;
		}

		void loadLabel(Context context) {
			if (mLabel == null || !mMounted) {
				if (!mApkFile.exists()) {
					mMounted = false;
					mLabel = mInfo.packageName;
				} else {
					mMounted = true;
					CharSequence label = mInfo.applicationInfo
							.loadLabel(context.getPackageManager());
					mLabel = label != null ? label.toString()
							: mInfo.packageName;
				}
			}

		}

	}

	/**
	 * Helper for determining if the configuration has changed in an interesting
	 * way so we need to rebuild the app list.
	 */
	public static class InterestingConfigChanges {
		final Configuration mLastConfiguration = new Configuration();
		int mLastDensity;

		boolean applyNewConfig(Resources res) {
			int configChanges = mLastConfiguration.updateFrom(res
					.getConfiguration());
			boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
			if (densityChanged
					|| (configChanges & (ActivityInfo.CONFIG_LOCALE
							| ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
				mLastDensity = res.getDisplayMetrics().densityDpi;
				return true;
			}
			return false;
		}
	}

	OnClickListener SelectAllListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mApps != null && mApps.size() > 0) {
				checkItems.clear();
				for (AppEntry entry : mApps) {
					entry.isSelected = !isSelectAll;
					if (!isSelectAll) {
						checkItems.add(entry);
					}
				}
				initMoreBtnText(checkItems.size());
				mAdapter.notifyDataSetChanged();
				if(isSelectAll){
					selectAllBtn.setText(R.string.select_all);
				}
				else{
					selectAllBtn.setText(R.string.cance_all);
				}
					
				//selectAllImg.setSelected(!isSelectAll);
				isSelectAll = !isSelectAll;
			}
		}
	};

	public void showNoResultView() {
		if (mAdapter.isEmpty()) {
			loading.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
		} else {
			mAdapter.notifyDataSetChanged();
		}
	}

	class AppUnistallAdapter extends BaseAdapter {

		public void clear() {
			mApps.clear();
			notifyDataSetChanged();
		}

		public void removeItem(AppItem item) {
			if (mApps.contains(item)) {
				mApps.remove(item);
				notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			return mApps.size();
		}

		@Override
		public AppEntry getItem(int arg0) {
			return mApps.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			final AppEntry entry = (AppEntry) getItem(arg0);
			ViewHolder holder;
			if (arg1 == null) {
				holder = new ViewHolder();
				arg1 = inflater.inflate(R.layout.app_uninstall_item_layout,
						null);
				holder.appIcon = (ImageView) arg1
						.findViewById(R.id.uninstall_app_icon);
				holder.appName = (TextView) arg1
						.findViewById(R.id.uninstall_app_packagename);
				holder.appInfo = (TextView) arg1
						.findViewById(R.id.unistall_app_info);
				holder.appUninstallBtn = (LinearLayout) arg1
						.findViewById(R.id.icon_btn_uninstall);
				holder.imgCheck = (ImageView) arg1.findViewById(R.id.img_check);
				holder.tvAlpha = (TextView) arg1.findViewById(R.id.tv_alpha);
				holder.driverLine = arg1.findViewById(R.id.driver_line_view);
				holder.installphone=(TextView) arg1.findViewById(R.id.installphone);
				holder.installsd=(TextView) arg1.findViewById(R.id.installsd);
				holder.bestone_appsize=(TextView) arg1.findViewById(R.id.bestone_appsize);
				holder.bestone_btn_uninstall=(Button) arg1.findViewById(R.id.bestone_btn_uninstall);	
				holder.uninstallitembg=(LinearLayout) arg1.findViewById(R.id.uninstallitembg);
				arg1.setTag(holder);
			} else {
				holder = (ViewHolder) arg1.getTag();
			}
			/*add by zengxiao for:修改应用卸载*/			
			if ((entry.mInfo.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) { 
				holder.installphone.setVisibility(View.GONE);
				holder.installsd.setVisibility(View.VISIBLE);
			}
			else{
				holder.installsd.setVisibility(View.GONE);
				holder.installphone.setVisibility(View.VISIBLE);
			}
			if(entry.userTime!=null&&!(entry.userTime.equals("")))
			{
			holder.appInfo.setText(entry.userTime);
			}
		/*	holder.installwhere=(TextView) arg1.findViewById(R.id.installwhere);
			holder.bestone_appsize=(TextView) arg1.findViewById(R.id.bestone_appsize);*/
			if(entry.packgeSize!=0)
			{
				DecimalFormat decimalFormat=new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
				String p=decimalFormat.format(entry.packgeSize);//format 返回的是字符串
				holder.bestone_appsize.setText(SoftwareUninstallActivity.this.getString(R.string.allusene)+p+"M");
			}
			holder.bestone_btn_uninstall.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					showShortToast(getString(R.string.unstallnow));
					PackageUtil.uninstallApp(MyApplication.getInstance(),
							entry.getPackageName(), "", false);					
				}
			});	
			/*add end*/
			holder.imgCheck.setSelected(entry.isSelected);

			holder.appIcon.setBackgroundDrawable(entry.getIcon());
			holder.appName.setText(entry.getLabel());
			/*holder.appInfo.setText(getString(R.string.version_soft) + entry.getVersionName());*/
			/*holder.appUninstallBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					showShortToast(getString(R.string.unstallnow));
					PackageUtil.uninstallApp(MyApplication.getInstance(),
							entry.getPackageName(), "", false);
					//uninstallByPName(entry.getPackageName(), entry.getLabel());

				}
			});*/
			holder.uninstallitembg.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					runApp(entry);
				}
			});
			String currentAlpha = entry.alpha;
			String priviewAlpha = (arg0 - 1) >= 0 ? mApps.get(arg0 - 1).alpha
					: " ";
			String nextAlpha = (arg0 + 1) <= mApps.size() - 1 ? mApps
					.get(arg0 + 1).alpha : "";

			/*if (!priviewAlpha.equals(currentAlpha)) {
				holder.tvAlpha.setVisibility(View.VISIBLE);
				holder.tvAlpha.setText(currentAlpha);
			} else {
				holder.tvAlpha.setVisibility(View.GONE);
			}*/
					holder.tvAlpha.setVisibility(View.GONE);

			/*if (nextAlpha.equals(currentAlpha)) {
				holder.driverLine.setVisibility(View.VISIBLE);
			} else {
			*/	holder.driverLine.setVisibility(View.GONE);
			/*}*/
			holder.imgCheck.setVisibility(View.GONE);
			holder.appUninstallBtn.setVisibility(View.GONE);
			switch (sort_type) {
			// 按名称排序
			case 1:
				holder.appInfo.setText(getString(R.string.version_soft) + entry.getVersionName());
				holder.appUninstallBtn.setVisibility(View.VISIBLE);
				break;
			// 按大小排序
			case 2:
				holder.driverLine.setVisibility(View.VISIBLE);
				holder.tvAlpha.setVisibility(View.GONE);
				holder.appInfo.setText(Util.bytes2kb(entry.getSize()));
				break;
			// 按日期排序
			case 3:
				holder.driverLine.setVisibility(View.VISIBLE);
				holder.tvAlpha.setVisibility(View.GONE);
				holder.appInfo.setText(DateUtil
						.getFormatShortTime(entry.mApkFile.lastModified()));
				break;
			case 4:
				holder.imgCheck.setVisibility(View.VISIBLE);
				holder.appUninstallBtn.setVisibility(View.VISIBLE);
				break;
			default:
				holder.appInfo.setText(getString(R.string.version_soft) + entry.getVersionName());
				break;
			}

			return arg1;
		}

	}

	static class ViewHolder {
		ImageView imgCheck;
		ImageView appIcon;
		TextView appName;
		TextView appInfo;
		TextView tvAlpha;
		View driverLine;
		LinearLayout appUninstallBtn;
		TextView installphone;//应用安装在手机
		TextView installsd;//应用安装在sd卡
		TextView bestone_appsize;//应用占用的空间
		Button bestone_btn_uninstall;//应用卸载按钮
		LinearLayout uninstallitembg;//卸载linearLayout的背景
	}

	public void uninstallByPName(final String pname, String appName) {
		if (!TextUtils.isEmpty(pname)) {
			singleUninstallDialog.setPackageName(appName);
			singleUninstallDialog.setBtnOkListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showShortToast(getString(R.string.unstallnow));
					PackageUtil.uninstallApp(MyApplication.getInstance(),
							pname, "", false);
					singleUninstallDialog.dismiss();
				}
			});
			singleUninstallDialog.show();
		}
	}

	@Override
	public void startConnecting(DownloadTask task, int totalTask,
			int progressTask) {
	}

	@Override
	public void downloadStarted(DownloadTask task, int totalTask,
			int progressTask, long totalSize) {
	}

	@Override
	public void downloadProgress(DownloadTask task, int totalTask,
			int progressTask, long progressSize, long totalSize) {
	}

	@Override
	public void downloadEnded(DownloadTask task, int totalTask, int progressTask) {

	}

	@Override
	public void endConnecting(DownloadTask task, int totalTask,
			int progressTask, DownloadException result) {
	}

	@Override
	public void downloadTaskDone(int totalTask, int progressTask,
			boolean success) {
	}

	@Override
	public void refreshUI() {
	}

	int position = 0;
	@Override
	public void unInstalledSucess(String pName) {
	if (!TextUtils.isEmpty(pName)) {	
			if (!PackageUtil.isInstalledApk(this, pName, null)) {
				uninstallname=pName;
			}
		}
		
	
	}

	/**
	 * Perform alphabetical comparison of application entry objects.
	 */
	public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
		private final Collator sCollator = Collator.getInstance();

		@Override
		public int compare(AppEntry object1, AppEntry object2) {
			return sCollator.compare(object1.alpha, object2.alpha);
		}
	};

	class SizeCompareBle implements Comparator {
		@Override
		public int compare(Object arg0, Object arg1) {
			// TODO Auto-generated method stub
			AppEntry entry1 = (AppEntry) arg0;
			AppEntry entry2 = (AppEntry) arg1;
			long result = entry1.getSize() - entry2.getSize();
			if (result > 0) {
				return -1;
			} else if (result == 0) {
				return 0;
			} else if (result < 0) {
				return 1;
			}
			return 0;
		}

	}

	class DateCompareBle implements Comparator {
		@Override
		public int compare(Object arg0, Object arg1) {
			// TODO Auto-generated method stub
			AppEntry entry1 = (AppEntry) arg0;
			AppEntry entry2 = (AppEntry) arg1;
			long result = entry1.mApkFile.lastModified()
					- entry2.mApkFile.lastModified();
			if (result > 0) {
				return -1;
			} else if (result == 0) {
				return 0;
			} else if (result < 0) {
				return 1;
			}
			return 0;
		}

	}

	class StartCountCompareBle implements Comparator {
		@Override
		public int compare(Object arg0, Object arg1) {
			// TODO Auto-generated method stub
			AppEntry entry1 = (AppEntry) arg0;
			AppEntry entry2 = (AppEntry) arg1;
			long result = entry1.getStartCount() - entry2.getStartCount();
			if (result > 0) {
				return -1;
			} else if (result == 0) {
				return 0;
			} else if (result < 0) {
				return 1;
			}
			return 0;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*delete by zengxiao */
	/*	if (keyCode == KeyEvent.KEYCODE_MENU) {
			showPopu(iconTitleSort);
		}
		else if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(sort_type==4)
			{
				sortByName();
				return  false;
			}
		}*/
		/*delete by zengxiao */
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btn_find_game:
			// Intent intent = new Intent(mContext, MoreGameActivity.class);
			// startActivity(intent);
			finish();
			break;
		case R.id.titlebar_back_arrow:
			finish();
			break;
		case R.id.btn_more_uninstall:
			// 批量卸载
			if (checkItems.size() > 0) {
				moreUninstallDialog.setSelectCount(checkItems.size());
				moreUninstallDialog.setBtnOkListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						position=0;
						if(checkItems.size()>0)
						{
						isMoreUninstall = true;
						PackageUtil.uninstallApp(MyApplication.getInstance(),
								checkItems.get(0).getPackageName(), "", false);
						position++;
						
						}
						moreUninstallDialog.dismiss();
					}
				});
				moreUninstallDialog.show();
			} else {

			}
			break;
		}
	}

	public String getAlpha(String str) {
		if (str == null) {
			return "#";
		}

		if (str.trim().length() == 0) {
			return "#";
		}

		char c = str.trim().substring(0, 1).charAt(0);
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		if (pattern.matcher(c + "").matches()) {
			return (c + "").toUpperCase();
		} else {
			return "#";
		}
	}

	@Override
	public void initData() {
	}

	@Override
	public void addEvent() {
	}

	@Override
	public void installedSucess(DownloadTask downloadTask) {
	}

	@Override
	public void networkIsOk() {
		// TODO Auto-generated method stub

	}

	@Override
	public void installedSucess(String packageName) {
		// TODO Auto-generated method stub
		
	}
	 public void  queryPacakgeSize(String pkgName) throws Exception{  
	        if ( pkgName != null){  
	            //使用放射机制得到PackageManager类的隐藏函数getPackageSizeInfo  
	            PackageManager pm = getPackageManager();  //得到pm对象  
	            try {  
	                //通过反射机制获得该隐藏函数  
	                Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class,IPackageStatsObserver.class);  
	                //调用该函数，并且给其分配参数 ，待调用流程完成后会回调PkgSizeObserver类的函数  
	                getPackageSizeInfo.invoke(pm, pkgName,new PkgSizeObserver());  
	            }   
	            catch(Exception ex){  
	            	Log.d("myzx","ex="+ex.toString());
	            }   
	        }  
	    }  
	 public class PkgSizeObserver extends IPackageStatsObserver.Stub{

		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			long pagesize=pStats.codeSize+pStats.dataSize;
			float pagesizem=pagesize/1024.0f/1024.0f;
			Log.d("myzx","onGetStatsCompleted="+pStats.packageName+"pagesize="+pagesizem);
			for (AppEntry app : mApps) {
				if (app.getPackageName().equals(pStats.packageName)) {
					Log.d("myzx", "myapp--------"+app.getPackageName());
					app.setPackgeSize(pagesizem);
					break;
				}
			}
			
		} 
		 
	 }

}
