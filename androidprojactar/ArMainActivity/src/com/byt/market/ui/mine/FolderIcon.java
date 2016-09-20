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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byt.ar.R;
import com.byt.market.MyApplication;
import com.byt.market.util.LogUtil;


/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends BubbleTextView implements DropTarget {
    private static final String TAG = "Launcher.FolderIcon";

    private UserFolderInfo mInfo;
    private MineViewManager mLauncher;
    private Drawable mCloseIcon;
    private Drawable mOpenIcon;

    private static final int MAX_FOLDER_NUMBER = 12;
    private static final int COLUMN_MAX = 4;
    private static final int ROW_MAX = 3;

    private static float LEFT_PADDING;
    private static float TOP_PADDING;
    private static float ICON_WIDTH;
    private static float ICON_HEIGHT;
    private static float ROW_MARGIN;
    private static float COLUMN_MARGIN;

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FolderIcon(Context context) {
        this(context, null);
    }

    static Drawable generateFolderIcon(MineViewManager launcher, UserFolderInfo folderInfo) {
        final Resources resources = launcher == null ? MyApplication.getInstance().getApplicationContext().getResources() : launcher.getContext().getResources();
        Drawable bgDrawable = resources.getDrawable(R.drawable.folder_icon_bg);

        LEFT_PADDING = resources.getDimension(R.dimen.folder_icon_left_padding);
        TOP_PADDING = resources.getDimension(R.dimen.folder_icon_top_padding);
        ICON_WIDTH = resources.getDimension(R.dimen.folder_icon_icon_width);
        ICON_HEIGHT = resources.getDimension(R.dimen.folder_icon_icon_height);
        ROW_MARGIN = resources.getDimension(R.dimen.folder_icon_row_margin);
        COLUMN_MARGIN = resources.getDimension(R.dimen.folder_icon_column_margin);

        int width = Utilities.sIconWidth;//(int) resources.getDimension(R.dimen.recent_applications_app_icon_mask);
        int height = width;
        
        /*
        int sourceWidth = bgDrawable.getIntrinsicWidth();
        int sourceHeight = bgDrawable.getIntrinsicHeight();

        if (sourceWidth > 0 && sourceWidth > 0) {
            // There are intrinsic sizes.
            if (width < sourceWidth || height < sourceHeight) {
                // It's too big, scale it down.
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            } else if (sourceWidth < width && sourceHeight < height) {
                // It's small, use the size they gave us.
                width = sourceWidth;
                height = sourceHeight;
            }
        }
        */

        if (LauncherApplication.DBG) LogUtil.d(TAG, "generateFolderIcon() w:"+width+" h:"+height);
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Rect bounds = new Rect();
        bounds.set(bgDrawable.getBounds());
        bgDrawable.setBounds(0, 0, width, height);
        bgDrawable.draw(canvas);
        bgDrawable.setBounds(bounds);

        Bitmap icon = null;
        Rect src = new Rect();
        Rect dst = new Rect();
        int left = (int)LEFT_PADDING;
        int top = (int)TOP_PADDING;
        int num = 0;
        int line = 0;

        ArrayList<ShortcutInfo> contents = folderInfo.contents;
        for (ShortcutInfo info : contents) {
            icon = info.getIcon(null);
            BitmapDrawable drawable = new BitmapDrawable(icon);
            drawable.setBounds(left, top, left+(int)ICON_WIDTH, top+(int)ICON_HEIGHT);
            left += (ICON_WIDTH + COLUMN_MARGIN);
            num++;
            if (num >= (COLUMN_MAX - 1)) {
                left = (int)LEFT_PADDING;
                top += (ICON_HEIGHT + ROW_MARGIN);
                num = 0;
                line++;
            }
            drawable.draw(canvas);

            if (line >= ROW_MAX) break;
        }

        return new BitmapDrawable(resources, bitmap);
    }

    static FolderIcon fromXml(int resId, MineViewManager launcher, ViewGroup group,
            UserFolderInfo folderInfo) {
        if (LauncherApplication.DBG) LogUtil.d(TAG, "fromXml()");

        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher.getContext()).inflate(resId, group, false);
        /* generate folder icon */
        Drawable d = generateFolderIcon(launcher, folderInfo);
        icon.mCloseIcon = d;
        icon.mOpenIcon = d;//resources.getDrawable(R.drawable.ic_launcher_folder_open);
        icon.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
        icon.setText(folderInfo.title);
        icon.setTag(folderInfo);
        //icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        
        return icon;
    }

    public boolean isFull(Object dragInfo) {
        return false;
    }

    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        ItemInfo item = null;
        if (dragInfo instanceof ItemInfo) {
            item = (ItemInfo) dragInfo;
        } else {
            CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) dragInfo; 
            View child = cellInfo.cell;
            item = (ItemInfo) child.getTag();
        }

        final int itemType = item.itemType;
        return (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)
                && item.container != mInfo.id &&
                mInfo.contents.size() < MAX_FOLDER_NUMBER;
    }

    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo, Rect recycle) {
        return null;
    }

    public void reGenerateFolderIcon() {
        if (LauncherApplication.DBG) LogUtil.d(TAG, "reGenerateFolderIcon()");
        Drawable d = generateFolderIcon(mLauncher, mInfo);
        mCloseIcon = d;
        mOpenIcon = d;
        setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
    }

    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        CellLayout.CellInfo cellInfo = (CellLayout.CellInfo)dragInfo;
        ShortcutInfo info = (ShortcutInfo)cellInfo.cell.getTag();

        View child = mLauncher.createShortcut(R.layout.folder_icon, null, info);
        child.setLayoutParams(new CellLayout.LayoutParams(0, 0, 1, 1));

        addItem(child);
    }

    public void addItem(View view) {
        ShortcutInfo info = (ShortcutInfo) view.getTag();
        info.container = mInfo.id;
        info.screen = 0;

        if (LauncherApplication.DBG) LogUtil.d(TAG, "addItem() container "+info.container+":"+mInfo.id+" screen:"+info.screen);

        mInfo.add(info);

        //mLauncher.mSaveModel.addOrUpdate(view);

        Drawable d = generateFolderIcon(mLauncher, mInfo);
        mCloseIcon = d;
        mOpenIcon = d;
        setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
    }

    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        if (LauncherApplication.DBG) LogUtil.d(TAG, "onDragEnter...");

        setCompoundDrawablesWithIntrinsicBounds(null, mOpenIcon, null, null);
    }

    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }

    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo, DropTarget dropTarget) {
        if (LauncherApplication.DBG) LogUtil.d(TAG, "onDragExit...");

        setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null, null);
        
       // mLauncher.postProcessFolderIcon(this);
    }

    public Drawable getIcon() {
        return mCloseIcon;
    }

    public UserFolderInfo getInfo() {
        return mInfo;
    }

    private boolean mEditMode;

    public void enterEditMode() {
        mEditMode = true;
    }

    public void exitEditMode() {
        mEditMode = false;
    }
}
