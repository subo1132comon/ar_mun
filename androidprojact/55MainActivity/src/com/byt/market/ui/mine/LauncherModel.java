/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.byt.market.ui.mine;


import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.SystemClock;

import com.byt.market.R;
import com.byt.market.Constants;
import com.byt.market.data.AppItem;
import com.byt.market.download.DownloadContent;
import com.byt.market.download.DownloadContent.DownloadTask;
import com.byt.market.util.BitmapUtil;
import com.byt.market.util.FileUtil;
import com.byt.market.util.LogUtil;
import com.byt.market.util.StringUtil;



/**
 * Maintains in-memory state of the Launcher. It is expected that there should be only one
 * LauncherModel object held in a static. Also provide APIs for updating the database state
 * for the Launcher.
 */
public class LauncherModel extends BroadcastReceiver {
    static final boolean DEBUG_LOADERS = true;
    static final boolean DEBUG_DB = true;
    static final boolean PROFILE_LOADERS = false;
    static final String TAG = "Launcher.Model";

    private IconCache mIconCache;

    private Bitmap mDefaultIcon;

    LauncherModel(Context context, IconCache iconCache) {
        mIconCache = iconCache;
        mDefaultIcon = Utilities.createIconBitmap(context.getResources().getDrawable(R.drawable.app_empty_icon)
                /*context.getPackageManager().getDefaultActivityIcon()*/, context ,null);
    }

    public Bitmap getFallbackIcon() {
        return Bitmap.createBitmap(mDefaultIcon);
    }



    /**
     * This is called from the code that adds shortcuts from the intent receiver.  This
     * doesn't have a Cursor, but
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context) {
        return getShortcutInfo(manager, intent, context, null, -1, -1,null);
    }
    
    /**
     * This is called from the code that adds shortcuts from the intent receiver.  This
     * doesn't have a Cursor, but
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context,AppItem item) {
        return getShortcutInfo(manager, intent, context, null, -1, -1,item);
    }

    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context,
            Cursor c, int iconIndex, int titleIndex,AppItem item) {
        Bitmap icon = item.icon;
        final ShortcutInfo info = new ShortcutInfo();
        ResolveInfo resolveInfo = null;
        ComponentName componentName = null;
        if(intent != null){
        	componentName = intent.getComponent();
            if (componentName == null) {
                return null;
            }

            // TODO: See if the PackageManager knows about this case.  If it doesn't
            // then return null & delete this.

            // the resource -- This may implicitly give us back the fallback icon,
            // but don't worry about that.  All we're doing with usingFallbackIcon is
            // to avoid saving lots of copies of that in the database, and most apps
            // have icons anyway.
            resolveInfo = manager.resolveActivity(intent, 0);
            if (resolveInfo != null) {
                icon = mIconCache.getIcon(componentName, resolveInfo);
            } else {
                return null;
            }
        }
        if(icon == null && item != null){
        	filAppIconFromUtil(item);
        	icon = item.icon;
        }
        // the fallback icon
        if (icon == null) {
            icon = getFallbackIcon();
            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);

        

        // from the resource
        if (resolveInfo != null) {
        	info.setFlags(resolveInfo.activityInfo.applicationInfo.flags);
            info.title = resolveInfo.activityInfo.loadLabel(manager);
        }

        // fall back to the class name of the activity
        if (info.title == null && componentName != null) {
            info.title = componentName.getClassName();
        }
        if(componentName != null){
        	info.packageName = componentName.getPackageName();
        }
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        
        info.intent = intent;
        return info;
    }
    private static int iconSize ;
    private void filAppIconFromUtil(AppItem item){
    	if(iconSize <= 0){
    		iconSize = Utilities.sIconWidth;//App.getInstance().getApplicationContext().getResources().getDimensionPixelSize(R.dimen.recent_applications_app_icon_mask);
    	}
    	if (item.iconUrl == null || item.iconUrl.trim().equals("")) {
			return;
		}
		String iconName ;
		if(item.iconUrl.startsWith("http://")){
			iconName = StringUtil.md5Encoding(item.iconUrl);
		}else{
			iconName = StringUtil.md5Encoding(Constants.IMG_URL
					+ item.iconUrl);
		}
		
		if (iconName == null) {
			return;
		}
		byte[] buffer = FileUtil.getBytesFromFile(iconName);
		if (buffer != null) {
			item.icon = BitmapUtil.Bytes2Bimap(buffer);
		} 
		if(item.icon != null){
			item.icon = item.icon.createScaledBitmap(item.icon, iconSize, iconSize, false);
		}
    }

    
        @Override
        public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                
        }
        
    final int DEFAULT_SCREEN = 1;
    final int MAX_SCREEN_COUNT = 5;
    final int MAX_X_COUNT = 4;
    final int MAX_Y_COUNT = 4;
    final boolean occupied[][][] = new boolean[MAX_SCREEN_COUNT][MAX_X_COUNT][MAX_Y_COUNT];

        
    public List<ShortcutInfo> syncAllAppsBatch(Context context,List<ActivityManager.RecentTaskInfo> recentTasks) {
        final PackageManager manager = context.getPackageManager();
        
        final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
        List<ShortcutInfo> items = new ArrayList<ShortcutInfo>();
        // Don't use these two variables in any of the callback runnables.
        // Otherwise we hold a reference to them.
        //final Callbacks oldCallbacks = mCallbacks.get();
        /*if (oldCallbacks == null) {
            // This launcher has exited and nobody bothered to tell us.  Just bail.
            LogUtil.w(TAG, "LoaderThread running with no launcher (syncAllAppsBatch)");
            return;
        }*/

            int N = recentTasks.size();

            if (DEBUG_LOADERS) LogUtil.d(TAG, "syncAllAppsBatch() N:"+N);

            int i=0;
 
            while (i < N) {
                final long t2 = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                //final Callbacks callbacks = tryGetCallbacks(oldCallbacks);

                int[] sxy = new int[3];

                for (int j=0; i<N && j<N; j++) {
                    // This builds the icon bitmaps.
                	if(recentTasks.get(i).origActivity == null){
                		i++;
                		continue;
                	}
                    ApplicationInfo app = new ApplicationInfo(findActivitiesForPackage(context,recentTasks.get(i++).origActivity.getPackageName()).get(0), mIconCache);

                    if (!getValidCell(occupied, sxy)) return items;

                    ShortcutInfo info = app.makeShortcut();

                    if (info == null) {
                        return items;
                    }

                    /*addItemToDatabase(mContext, info,
                            LauncherSettings.Favorites.CONTAINER_DESKTOP,
                            sxy[0], sxy[1], sxy[2], false);*/

                    /* to add icon to workspace. */
                    info.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                    info.screen = sxy[0];
                    info.cellX = sxy[1];
                    info.cellY = sxy[2];

                    items.add(info);
                }

                if (DEBUG_LOADERS) {
                    LogUtil.d(TAG, "batch of " + i + " icons processed in "
                            + (SystemClock.uptimeMillis()-t2) + "ms");
                }

               /* if (mAllAppsLoadDelay > 0 && i < N) {
                    try {
                        if (DEBUG_LOADERS) {
                            LogUtil.d(TAG, "sleeping for " + mAllAppsLoadDelay + "ms");
                        }
                        Thread.sleep(mAllAppsLoadDelay);
                    } catch (InterruptedException exc) { }
                }*/
            }

            /*if (DEBUG_LOADERS) {
                LogUtil.d(TAG, "cached all " + N + " apps in "
                        + (SystemClock.uptimeMillis()-t) + "ms"
                        + (mAllAppsLoadDelay > 0 ? " (including delay)" : ""));
            }*/
            return items;
    }
    
    private boolean getValidCell(boolean[][][] occupied, int[] sxy) {
        for (int s = DEFAULT_SCREEN; s < MAX_SCREEN_COUNT; s++) {
            for (int y = 0; y < MAX_Y_COUNT; y++) {
                for (int x = 0; x < MAX_X_COUNT; x++) {
                    if (!occupied[s][x][y]) {
                        sxy[0] = s; sxy[1] = x; sxy[2] = y;
                        occupied[s][x][y] = true;
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    private static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        final List<ResolveInfo> matches = new ArrayList<ResolveInfo>();

        if (apps != null) {
            // Find all activities that match the packageName
            int count = apps.size();
            for (int i = 0; i < count; i++) {
                final ResolveInfo info = apps.get(i);
                final ActivityInfo activityInfo = info.activityInfo;
                if (packageName.equals(activityInfo.packageName)) {
                    matches.add(info);
                }
            }
        }

        return matches;
    }
    
    /**
     * Add an item to the database in a specified container. Sets the container, screen, cellX and
     * cellY fields of the item. Also assigns an ID to the item.
     */
    static void addItemToDatabase(Context context, ItemInfo item, long container,
            int screen, int cellX, int cellY, boolean notify) {
        item.container = container;
        item.screen = screen;
        item.cellX = cellX;
        item.cellY = cellY;

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();

        item.onAddToDatabase(values);

        Uri result = cr.insert(DownloadTask.CONTENT_URI, values);

        if (result != null) {
            item.id = Integer.parseInt(result.getPathSegments().get(1));
        }

        if (DEBUG_DB) LogUtil.d(TAG, "@@@@ addItemToDatabase() item new id:"+item.id+" container:"+container+" screen:"+screen+" cellX:"+cellX+" cellY:"+cellY);
    }
    
    
    /**
     * Move an item in the DB to a new <container, screen, cellX, cellY>
     */
    static void moveItemInDatabase(Context context, ItemInfo item, long container, int screen,
            int cellX, int cellY) {
        if (DEBUG_DB) LogUtil.d(TAG, "@@@@ MoveItemInDatabase() item id:"+item.id+" container:"+container+" screen:"+screen+" cellX:"+cellX+" cellY:"+cellY);

        item.container = container;
        item.screen = screen;
        item.cellX = cellX;
        item.cellY = cellY;

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();

        item.onAddToDatabase(values);

        cr.update(DownloadTask.CONTENT_URI, values, DownloadContent.DownloadTaskColumn.COLUMN_SID + " = '"+item.id+"'", null);
    }


}
