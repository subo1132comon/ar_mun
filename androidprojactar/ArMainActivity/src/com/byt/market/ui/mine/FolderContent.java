/*
 * Copyright
 */

package com.byt.market.ui.mine;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.byt.market.util.LogUtil;

/**
 * Descriptions
 */
public class FolderContent extends Workspace {
	private static final String TAG = "Launcher.FolderContent";

	/* public operations */
	public FolderContent(Context context) {
		super(context, null);
	}

	public FolderContent(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FolderContent(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void scrollToScreen(int screen, int velocity, boolean settle) {
	}

	protected void drawScreens(Canvas canvas) {
	}

	private Folder mFolder;

	public void setFolder(Folder folder) {
		mFolder = folder;
	}

	@Override
	protected long getContainerId() {
		return mFolder.getInfo().id;
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo,
			DropTarget dropTarget) {
		super.onDragExit(source, x, y, xOffset, yOffset, dragView, dragInfo,
				dropTarget);

		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "onDragExit...");

		/*
		 * to dup call onIconDragOut, because fast drop icon outside of folder
		 * will not call onDragExit, but only onDropCompleted.
		 */
		if (mFolder != null) {
			mFolder.onIconDragOut(dragInfo);
		}
	}

	@Override
	public void onDropCompleted(View target, boolean success, Object dragInfo,
			DragView dragView) {
		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "onDropCompleted...");
		if (mFolder != null && target != this) {
			mFolder.onIconDragOut(dragInfo);
		}

		super.onDropCompleted(target, success, dragInfo, dragView);

	}
}
