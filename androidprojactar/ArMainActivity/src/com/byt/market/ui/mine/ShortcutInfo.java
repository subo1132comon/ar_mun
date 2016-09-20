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

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;

import com.byt.market.util.LogUtil;

/**
 * Represents a launchable icon on the workspaces and in folders.
 */
class ShortcutInfo extends ItemInfo {
        
        int taskId;
        
        int persistentTaskId;
        
        /* preinstall type */
        CharSequence type;
        
    /**
     * The application name.
     */
    CharSequence title;

    /**
     * The intent used to start the application.
     */
    Intent intent;

    /**
     * Indicates whether the icon comes from an application's resource (if false)
     * or from a custom Bitmap (if true.)
     */
    boolean customIcon;

    /**
     * Indicates whether we're using the default fallback icon instead of something from the
     * app.
     */
    boolean usingFallbackIcon;

    /**
     * Indicates whether the shortcut is on external storage and may go away at any time.
     */
    boolean onExternalStorage;

    /**
     * If isShortcut=true and customIcon=false, this contains a reference to the
     * shortcut icon as an application's resource.
     */
    Intent.ShortcutIconResource iconResource;

    int flags;

    /**
     * The application icon.
     */
    private Bitmap mIcon;

    String packageName;

    ShortcutInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }
    
    public ShortcutInfo(ShortcutInfo info) {
        super(info);
        title = info.title.toString();
        intent = new Intent(info.intent);
        if (info.iconResource != null) {
            iconResource = new Intent.ShortcutIconResource();
            iconResource.packageName = info.iconResource.packageName;
            iconResource.resourceName = info.iconResource.resourceName;
        }
        mIcon = info.mIcon; // TODO: should make a copy here.  maybe we don't need this ctor at all
        customIcon = info.customIcon;
        packageName = info.packageName;
    }

    /** TODO: Remove this.  It's only called by ApplicationInfo.makeShortcut. */
    public ShortcutInfo(ApplicationInfo info) {
        super(info);
        title = info.title.toString();
        intent = new Intent(info.intent);
        customIcon = false;
        packageName = info.componentName.getPackageName();
        //LogUtil.d("ShortcutInfo", "packageName:"+packageName);
    }

    public void setIcon(Bitmap b) {
        mIcon = b;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public Bitmap updateIcon(IconCache iconCache) {
        mIcon = iconCache.updateIcon(this.intent);
        return mIcon;
    }

    public Bitmap getIcon(IconCache iconCache) {
        if (mIcon == null) {
            mIcon = iconCache.getIcon(this.intent);
        }
        return mIcon;
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
    }


    @Override
    public String toString() {
        return "ShortcutInfo(title=" + title.toString() + ")";
    }

    @Override
    void unbind() {
        super.unbind();
    }


    public static void dumpShortcutInfoList(String tag, String label,
            ArrayList<ShortcutInfo> list) {
        LogUtil.d(tag, label + " size=" + list.size());
        for (ShortcutInfo info: list) {
            LogUtil.d(tag, "   title=\"" + info.title + " icon=" + info.mIcon
                    + " customIcon=" + info.customIcon);
        }
    }
}
