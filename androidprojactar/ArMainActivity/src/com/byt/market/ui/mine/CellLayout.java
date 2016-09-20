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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;

import com.byt.ar.R;
import com.byt.market.MyApplication;
import com.byt.market.util.LogUtil;

public class CellLayout extends ViewGroup {
    private static final Boolean DBG = false;
    private static final Boolean DBG_LAYOUT = false;
    private static final String TAG = "Launcher.CellLayout";

    private boolean mPortrait = true;

    private static final int AREA_OUTSIDE = 0;
    private static final int AREA_INSIDE_LEFT = 1;
    private static final int AREA_INSIDE_CENTER = 2;
    private static final int AREA_INSIDE_RIGHT = 3;
    private static final int AREA_OUTSIDE_LEFT = 4;
    private static final int AREA_OUTSIDE_RIGHT = 5;

    private static final int ICON_MOVE_TIMEOUT = 0;
    private static final int FOLDER_CREATION_TIMEOUT = 300;

    private boolean mInEditMode;

    private int mCellWidth;
    private int mCellHeight;
    
    private int mLongAxisStartPadding;
    private int mLongAxisEndPadding;

    private int mShortAxisStartPadding;
    private int mShortAxisEndPadding;

    private int mShortAxisCells;
    private int mLongAxisCells;

    private int mWidthGap;
    private int mHeightGap;

    private boolean mEnableFolderCreation = false;

    private boolean mWrapContent;

    private DrawFilter mDrawFilter = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);

    /* To check if the drag view has been changed location */
    int[] mLastDragCellXY = new int[2];
    int mLastDragArea = AREA_OUTSIDE;

    private AutoAlignmentRunnable mAutoAlignmentRunnable = new AutoAlignmentRunnable();
    private ArrangeRunnable mArrangeRunnable = new ArrangeRunnable();
    private FolderCreationRunnable mFolderCreationRunnable = new FolderCreationRunnable();

    public Object mArrangeLock = new Object();

    private final Rect mRect = new Rect();
    private final CellInfo mCellInfo = new CellInfo();
    private int  mScreenIndex = 0;

    int[] mCellXY = new int[2];
    boolean[][] mOccupied;

    private RectF mDragRect = new RectF();
        private Paint mDragRectPaint = new Paint();

    private RectF mCoveredRect = new RectF();
    private RectF mCellRect = new RectF();

    private boolean mDirtyTag;
    private boolean mLastDownOnOccupiedCell = false;
    
    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void enableWrapContent(boolean enable) {
        mWrapContent = enable;
    }


    private static final int ANIMATION_NUM = 4;
    private static final int ROTATE_DURATION = 100;
    private Animation[] mAnimation = new Animation[ANIMATION_NUM];
    private Random mRandom = new Random();


    public CellLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CellLayout, defStyle, 0);

        mCellWidth = a.getDimensionPixelSize(R.styleable.CellLayout_cellWidth, 10);
        mCellHeight = a.getDimensionPixelSize(R.styleable.CellLayout_cellHeight, 10);
        //if(DBG) LogUtil.d(TAG, "CellLayout cell width: " + mCellWidth + ", cell height: " + mCellHeight);

        mLongAxisStartPadding = 
            a.getDimensionPixelSize(R.styleable.CellLayout_longAxisStartPadding, 10);
        mLongAxisEndPadding = 
            a.getDimensionPixelSize(R.styleable.CellLayout_longAxisEndPadding, 10);
        mShortAxisStartPadding =
            a.getDimensionPixelSize(R.styleable.CellLayout_shortAxisStartPadding, 10);
        mShortAxisEndPadding = 
            a.getDimensionPixelSize(R.styleable.CellLayout_shortAxisEndPadding, 10);
        
        mShortAxisCells = a.getInt(R.styleable.CellLayout_shortAxisCells, 4);
        mLongAxisCells = a.getInt(R.styleable.CellLayout_longAxisCells, 4);
        mLongAxisCells = Workspace.ROW_COUNT;
        a.recycle();

        setAlwaysDrawnWithCacheEnabled(false);

        if (mOccupied == null) {
                /*
            if (mPortrait) {
                mOccupied = new boolean[mShortAxisCells][mLongAxisCells];
            } else {
                mOccupied = new boolean[mLongAxisCells][mShortAxisCells];
            }
            */
                int size = mShortAxisCells > mLongAxisCells ? mShortAxisCells : mLongAxisCells;
                mOccupied = new boolean[size][size];
        }
        
        //mWallpaperManager = WallpaperManager.getInstance(getContext());

        if (DBG_DRAW) {
            mDragRectPaint.setColor(Color.GREEN);
            mDragRectPaint.setAlpha(60);
        }

        /* init animation */
        mAnimation[0] = generateAnimation(-2.0F, 2.0F, 110L);
        mAnimation[1] = generateAnimation(2.0F, -2.0F, 120L);
        mAnimation[2] = generateAnimation(2.25F, -2.0F, 100L);
        mAnimation[3] = generateAnimation(-2.25F, 2.0F, 110L);
    }

    private Animation generateAnimation(float fromDegrees, float toDegrees, long duration) {
        SwingAnimation animation = new SwingAnimation(fromDegrees, toDegrees);
        animation.setDuration(duration);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);
        
        return animation;
    }
    
    private static final boolean DBG_DRAW = false;

    @Override
    public void dispatchDraw(Canvas canvas) {
        canvas.setDrawFilter(mDrawFilter);

        super.dispatchDraw(canvas);

        if (DBG_DRAW) {
            if(mDragRect != null && !mDragRect.isEmpty()){
                canvas.drawRoundRect(mDragRect, 5, 5, mDragRectPaint);
            }
        }

        if (DBG_DRAW) {
            if (mCoveredRect != null && !mCoveredRect.isEmpty()) {
                canvas.drawRoundRect(mCoveredRect, 5, 5, mDragRectPaint);
            }
        }

        if (DBG_DRAW) {
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setAntiAlias(true);

                Rect r = new Rect();
                final View child = getChildAt(i);
                child.getHitRect(r);
                canvas.drawRect(r, paint);
            }
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        // Cancel long press for all children
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.cancelLongPress();
        }
    }

    int getCountX() {
        return mPortrait ? mShortAxisCells : mLongAxisCells;
    }

    int getCountY() {
        return mPortrait ? mLongAxisCells : mShortAxisCells;
    }

    public void enableFolderCreation(boolean enable) {
        if (DBG) LogUtil.d(TAG, "enableFolderCreation enable:"+enable);
        mEnableFolderCreation = enable;
    }

    private int mLastAniIdx = 0;
    
    private void enterChildEditMode(View child) {
        if (child.getVisibility() != View.VISIBLE) return;

        if (child instanceof AppIcon) {
            ((AppIcon)child).enterEditMode(getParent() instanceof Workspace && ((Workspace) getParent()).isCurrentScreen(this));
        }

        child.clearAnimation();

        int idx = 0;
        do {
                idx = mRandom.nextInt(ANIMATION_NUM);
        } while (idx == mLastAniIdx);
        
        mLastAniIdx = idx;
        
        AnimationSet animationset = (AnimationSet)child.getAnimation();
        if (animationset == null) {
            animationset = new AnimationSet(false);
        }

        animationset.addAnimation(mAnimation[idx]);
        if (child instanceof AppIcon) {
                child.startAnimation(animationset);
        }
    }

    public boolean isInEditMode() {
        return mInEditMode;
    }

    public void enterEditMode() {
        if (mInEditMode) return;
        
        mInEditMode = true;

        final int count = getChildCount();

        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            enterChildEditMode(child);
        }
    }

    public void exitEditMode(boolean withAnimation) {
        if (!mInEditMode) return;
        
        mInEditMode = false;

        final int count = getChildCount();

        for (int i = count - 1; i >= 0; i--) {
                if(getChildAt(i) instanceof AppIcon){
                        final AppIcon icon = (AppIcon)getChildAt(i);
                        icon.exitEditMode(withAnimation);
                        icon.clearAnimation();
                }
        }
    }

    /* for icon overflow */
    public void addChildAtPosition(View child, int[] cellXY) {
        //LogUtil.d(TAG, "addChildAtPosition() current screen:"+mCellInfo.screen);
        if (indexOfChild(child) < 0) addView(child);

        LayoutParams lp = (LayoutParams) child.getLayoutParams();

        /* find cells */
        List<View> cellGroup = findCells(cellXY[0], cellXY[1], lp.cellHSpan, lp.cellVSpan, child);

        boolean accept = cellGroup == null;

        clearArrangeAction();
        clearAutoAlignmentAction();

        if(accept) {
            lp.cellX = cellXY[0];
            lp.cellY = cellXY[1];

            final ItemInfo ci = (ItemInfo)child.getTag();
            ci.screen = mCellInfo.screen;
            final ItemInfo info = (ItemInfo)child.getTag();
            MyApplication.getInstance().mMineViewManager.mSaveModel.addOrUpdate(child);
        } else {
            mArrangeRunnable.setViews(cellXY, child, cellGroup);
            postDelayed(mArrangeRunnable, 0);
        }

        invalidate();
    }

    @Override
    public void removeView(View view) {
        view.clearAnimation();
        super.removeView(view);
    }

    public void addViewInLayout(View child) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        addViewInLayout(child, -1, lp, true);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        // Generate an id for each view, this assumes we have at most 256x256 cells
        // per workspace screen
        
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params);
        }

        final LayoutParams cellParams = (LayoutParams) params;
        cellParams.regenerateId = true;

        super.addView(child, index, params);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (child != null) {
            Rect r = new Rect();
            child.getDrawingRect(r);
            requestRectangleOnScreen(r);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mCellInfo.screen = ((ViewGroup) getParent()).indexOfChild(this);
        mScreenIndex = mCellInfo.screen;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        final CellInfo cellInfo = mCellInfo;

        //if (DBG) LogUtil.d(TAG, "onInterceptTouchEvent() action:"+action);
        if (action == MotionEvent.ACTION_DOWN) {
            final Rect frame = mRect;
            final int x = (int) ev.getX() + this.getScrollX();
            final int y = (int) ev.getY() + this.getScrollY();
            final int count = getChildCount();

            boolean found = false;
            for (int i = count - 1; i >= 0; i--) {
                final View child = getChildAt(i);

                if ((child.getVisibility()) == VISIBLE || child.getAnimation() != null) {
                    child.getHitRect(frame);
                    if (frame.contains(x, y)) {
                        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                        cellInfo.cell = child;
                        cellInfo.cellX = lp.cellX;
                        cellInfo.cellY = lp.cellY;
                        cellInfo.spanX = lp.cellHSpan;
                        cellInfo.spanY = lp.cellVSpan;
                        cellInfo.valid = true;
                        found = true;
                        mDirtyTag = false;
                        break;
                    }
                }
            }
            
            mLastDownOnOccupiedCell = found;

            if (!found) {
                int cellXY[] = mCellXY;
                pointToCellExact(x, y, cellXY);

                final boolean portrait = mPortrait;
                final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
                final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

                final boolean[][] occupied = mOccupied;
                findOccupiedCells(xCount, yCount, occupied, null);

                cellInfo.cell = null;
                cellInfo.cellX = cellXY[0];
                cellInfo.cellY = cellXY[1];
                cellInfo.spanX = 1;
                cellInfo.spanY = 1;
                cellInfo.valid = cellXY[0] >= 0 && cellXY[1] >= 0 && cellXY[0] < xCount &&
                        cellXY[1] < yCount && !occupied[cellXY[0]][cellXY[1]];

                // Instead of finding the interesting vacant cells here, wait until a
                // caller invokes getTag() to retrieve the result. Finding the vacant
                // cells is a bit expensive and can generate many new objects, it's
                // therefore better to defer it until we know we actually need it.

                mDirtyTag = true;
            }
            setTag(cellInfo);
        } else if (action == MotionEvent.ACTION_UP) {
            cellInfo.cell = null;
            cellInfo.cellX = -1;
            cellInfo.cellY = -1;
            cellInfo.spanX = 0;
            cellInfo.spanY = 0;
            cellInfo.valid = false;
            mDirtyTag = false;
            setTag(cellInfo);
        }

        return false;
    }

    @Override
    public CellInfo getTag() {
        final CellInfo info = (CellInfo) super.getTag();
        if (mDirtyTag && info.valid) {
            final boolean portrait = mPortrait;
            final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
            final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

            final boolean[][] occupied = mOccupied;
            findOccupiedCells(xCount, yCount, occupied, null);

            findIntersectingVacantCells(info, info.cellX, info.cellY, xCount, yCount, occupied);

            mDirtyTag = false;
        }
        return info;
    }

    private static void findIntersectingVacantCells(CellInfo cellInfo, int x, int y,
            int xCount, int yCount, boolean[][] occupied) {

        cellInfo.maxVacantSpanX = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanXSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanYSpanX = Integer.MIN_VALUE;
        cellInfo.clearVacantCells();

        if (occupied[x][y]) {
            return;
        }

        cellInfo.current.set(x, y, x, y);

        findVacantCell(cellInfo.current, xCount, yCount, occupied, cellInfo);
    }

    private static void findVacantCell(Rect current, int xCount, int yCount, boolean[][] occupied,
            CellInfo cellInfo) {

        addVacantCell(current, cellInfo);

        if (current.left > 0) {
            if (isColumnEmpty(current.left - 1, current.top, current.bottom, occupied)) {
                current.left--;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.left++;
            }
        }

        if (current.right < xCount - 1) {
            if (isColumnEmpty(current.right + 1, current.top, current.bottom, occupied)) {
                current.right++;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.right--;
            }
        }

        if (current.top > 0) {
            if (isRowEmpty(current.top - 1, current.left, current.right, occupied)) {
                current.top--;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.top++;
            }
        }

        if (current.bottom < yCount - 1) {
            if (isRowEmpty(current.bottom + 1, current.left, current.right, occupied)) {
                current.bottom++;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.bottom--;
            }
        }
    }

    private static void addVacantCell(Rect current, CellInfo cellInfo) {
        CellInfo.VacantCell cell = CellInfo.VacantCell.acquire();
        cell.cellX = current.left;
        cell.cellY = current.top;
        cell.spanX = current.right - current.left + 1;
        cell.spanY = current.bottom - current.top + 1;
        if (cell.spanX > cellInfo.maxVacantSpanX) {
            cellInfo.maxVacantSpanX = cell.spanX;
            cellInfo.maxVacantSpanXSpanY = cell.spanY;
        }
        if (cell.spanY > cellInfo.maxVacantSpanY) {
            cellInfo.maxVacantSpanY = cell.spanY;
            cellInfo.maxVacantSpanYSpanX = cell.spanX;
        }
        //if (DBG) LogUtil.d(TAG, "addVacantCell() cellX:"+cell.cellX+" cellY:"+cell.cellY);
        cellInfo.vacantCells.add(cell);
    }

    private static boolean isColumnEmpty(int x, int top, int bottom, boolean[][] occupied) {
        for (int y = top; y <= bottom; y++) {
            if (occupied[x][y]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRowEmpty(int y, int left, int right, boolean[][] occupied) {
        for (int x = left; x <= right; x++) {
            if (occupied[x][y]) {
                return false;
            }
        }
        return true;
    }

    CellInfo findAllVacantCells(boolean[] occupiedCells, View ignoreView) {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

        //LogUtil.d(TAG, "findAllVacantCells()");
        boolean[][] occupied = mOccupied;

        if (occupiedCells != null) {
            for (int y = 0; y < yCount; y++) {
                for (int x = 0; x < xCount; x++) {
                    occupied[x][y] = occupiedCells[y * xCount + x];
                }
            }
        } else {
            findOccupiedCells(xCount, yCount, occupied, ignoreView);
        }

        CellInfo cellInfo = new CellInfo();

        cellInfo.cellX = -1;
        cellInfo.cellY = -1;
        cellInfo.spanY = 0;
        cellInfo.spanX = 0;
        cellInfo.maxVacantSpanX = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanXSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanYSpanX = Integer.MIN_VALUE;
        cellInfo.screen = mCellInfo.screen;

        Rect current = cellInfo.current;

        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                if (!occupied[x][y]) {
                    current.set(x, y, x, y);
                    findVacantCell(current, xCount, yCount, occupied, cellInfo);
                    occupied[x][y] = true;
                    //LogUtil.d(TAG, "findAllVacantCells() change x:"+x+" y:"+y+" occupied true");
                }
            }
        }

        cellInfo.valid = cellInfo.vacantCells.size() > 0;

        // Assume the caller will perform their own cell searching, otherwise we
        // risk causing an unnecessary rebuild after findCellForSpan()
        
        return cellInfo;
    }

    /**
     * Given a point, return the cell that strictly encloses that point 
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @param result Array of 2 ints to hold the x and y coordinate of the cell
     */
    void pointToCellExact(int x, int y, int[] result) {
        final boolean portrait = mPortrait;
        
        final int hStartPadding = portrait ? mShortAxisStartPadding : mLongAxisStartPadding;
        final int vStartPadding = portrait ? mLongAxisStartPadding : mShortAxisStartPadding;

        //if(DBG) LogUtil.d(TAG, "pointToCellExact() cellw: " + mCellWidth + ", widthGap: " + mWidthGap);
        //if(DBG) LogUtil.d(TAG, "pointToCellExact() x: " + x + ", startpadding: " + hStartPadding);
        //if(DBG) LogUtil.d(TAG, "pointToCellExact() cellh: " + mCellHeight+ ", heightGap: " + mHeightGap);
        result[0] = (x - hStartPadding) / (mCellWidth + mWidthGap);
        result[1] = (y - vStartPadding) / (mCellHeight + mHeightGap);

        final int xAxis = portrait ? mShortAxisCells : mLongAxisCells;
        final int yAxis = portrait ? mLongAxisCells : mShortAxisCells;

        if (result[0] < 0) result[0] = 0;
        if (result[0] >= xAxis) result[0] = xAxis - 1;
        if (result[1] < 0) result[1] = 0;
        if (result[1] >= yAxis) result[1] = yAxis - 1;
    }
    
    /**
     * Given a point, return the cell that most closely encloses that point
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @param result Array of 2 ints to hold the x and y coordinate of the cell
     */
    void pointToCellRounded(int x, int y, int[] result) {
        pointToCellExact(x + (mCellWidth / 2), y + (mCellHeight / 2), result);
    }

    /**
     * Given a cell coordinate, return the point that represents the upper left corner of that cell
     * 
     * @param cellX X coordinate of the cell 
     * @param cellY Y coordinate of the cell
     * 
     * @param result Array of 2 ints to hold the x and y coordinate of the point
     */
   

    void cellToPoint(int cellX, int cellY, int[] result) {
        final boolean portrait = mPortrait;
        
        final int hStartPadding = portrait ? mShortAxisStartPadding : mLongAxisStartPadding;
        final int vStartPadding = portrait ? mLongAxisStartPadding : mShortAxisStartPadding;

        if (false) {
            final int count = getChildCount();
            int left = (getWidth() - count * mCellWidth - (count - 1) * mWidthGap) / 2 - hStartPadding;
            result[0] = hStartPadding + left + cellX * (mCellWidth + mWidthGap);
            result[1] = vStartPadding + cellY * (mCellHeight + mHeightGap);
        } else {
            result[0] = hStartPadding + cellX * (mCellWidth + mWidthGap);
            result[1] = vStartPadding + cellY * (mCellHeight + mHeightGap);
        }
    }

    int getCellWidth() {
        return mCellWidth;
    }

    int getCellHeight() {
        return mCellHeight;
    }

    int getLeftPadding() {
        return mPortrait ? mShortAxisStartPadding : mLongAxisStartPadding;
    }

    int getTopPadding() {
        return mPortrait ? mLongAxisStartPadding : mShortAxisStartPadding;        
    }

    int getRightPadding() {
        return mPortrait ? mShortAxisEndPadding : mLongAxisEndPadding;
    }

    int getBottomPadding() {
        return mPortrait ? mLongAxisEndPadding : mShortAxisEndPadding;        
    }

    public void measureChild(View child) {
        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

        lp.setup(cellWidth, cellHeight, mWidthGap, mHeightGap, mShortAxisStartPadding,
                mLongAxisStartPadding, getChildCount(), getWidth(), this);

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
        int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height,
                MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childheightMeasureSpec);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO: currently ignoring padding
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
        
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        
        if(DBG_LAYOUT) LogUtil.d(TAG, "onMeasure() widthSpecSize: " + widthSpecSize + ", heightSpecSize: " + heightSpecSize);
        
        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED && !mWrapContent) {
            throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
        }

        final int shortAxisCells = mShortAxisCells;
        final int longAxisCells = mLongAxisCells;
        int longAxisStartPadding = mLongAxisStartPadding;
        int longAxisEndPadding = mLongAxisEndPadding;
        final int shortAxisStartPadding = mShortAxisStartPadding;
        final int shortAxisEndPadding = mShortAxisEndPadding;
        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;

        if (mWrapContent) {
            mPortrait = false;
            //longAxisStartPadding = 0;
            //longAxisEndPadding = 0;
        } else {
            mPortrait = heightSpecSize > widthSpecSize;
        }

        int numShortGaps = shortAxisCells - 1;
        int numLongGaps = longAxisCells - 1;

        if (mPortrait) {
            int vSpaceLeft = heightSpecSize - longAxisStartPadding - longAxisEndPadding
                    - (cellHeight * longAxisCells);
            mHeightGap = vSpaceLeft / numLongGaps;

            if(DBG_LAYOUT) {
                LogUtil.d(TAG, "Portrait.");
                LogUtil.d(TAG, "onMeasure() heightSpecSize: " + heightSpecSize);
                LogUtil.d(TAG, "onMeasure() longAxisStartPadding: " + longAxisStartPadding);
                LogUtil.d(TAG, "onMeasure() longAxisEndPadding: " + longAxisEndPadding);
                LogUtil.d(TAG, "onMeasure() longAxisCells: " + longAxisCells);
                LogUtil.d(TAG, "onMeasure() vSpaceLeft : " + vSpaceLeft );
                LogUtil.d(TAG, "onMeasure() numLongGaps: " + numLongGaps);
                LogUtil.d(TAG, "onMeasure() mHeightGap : " + mHeightGap );
                LogUtil.d(TAG, "onMeasure() cellWidth : " + cellWidth);
                LogUtil.d(TAG, "onMeasure() cellHeight : " + cellHeight);
            }

            int hSpaceLeft = widthSpecSize - shortAxisStartPadding - shortAxisEndPadding
                    - (cellWidth * shortAxisCells);
            if (numShortGaps > 0) {
                mWidthGap = hSpaceLeft / numShortGaps;
            } else {
                mWidthGap = 0;
            }
        } /* else {
            int hSpaceLeft = widthSpecSize - longAxisStartPadding - longAxisEndPadding
                    - (cellWidth * longAxisCells);
            mWidthGap = hSpaceLeft / numLongGaps;

            if(DBG_LAYOUT) {
                LogUtil.d(TAG, "Landscape.");
                LogUtil.d(TAG, "onMeasure() heightSpecSize: " + heightSpecSize);
                LogUtil.d(TAG, "onMeasure() longAxisStartPadding: " + longAxisStartPadding);
                LogUtil.d(TAG, "onMeasure() longAxisEndPadding: " + longAxisEndPadding);
                LogUtil.d(TAG, "onMeasure() longAxisCells: " + longAxisCells);
                LogUtil.d(TAG, "onMeasure() hSpaceLeft : " + hSpaceLeft );
                LogUtil.d(TAG, "onMeasure() numLongGaps: " + numLongGaps);
                LogUtil.d(TAG, "onMeasure() mHeightGap : " + mHeightGap );
                LogUtil.d(TAG, "onMeasure() cellWidth : " + cellWidth);
                LogUtil.d(TAG, "onMeasure() cellHeight : " + cellHeight);
            }

        } */else {
            int hSpaceLeft = widthSpecSize - longAxisStartPadding - longAxisEndPadding
                    - (cellWidth * longAxisCells);
            mWidthGap = hSpaceLeft / numLongGaps;

            int vSpaceLeft = heightSpecSize - shortAxisStartPadding - shortAxisEndPadding
                    - (cellHeight * shortAxisCells);
            if (numShortGaps > 0) {
                mHeightGap = mWrapContent ? 2 : (vSpaceLeft / numShortGaps);
            } else {
                mHeightGap = 0;
            }

            if(DBG_LAYOUT) LogUtil.d(TAG, "onMeasure() land mHeightGap : " + mHeightGap );
        }
        
        /* gap shall not be < 0 */
        mHeightGap = mHeightGap < 0 ? 0 : mHeightGap;
        mWidthGap = mWidthGap < 0 ? 0 : mWidthGap;
        
        int count = getChildCount();

        if(DBG_LAYOUT) LogUtil.d(TAG, "onMeasure() child count: " + count);

        if (mWrapContent) {
            heightSpecSize = 0;
        }

        int maxLine = 0;

        for (int i = 0; i < count; i++) {

            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (mPortrait) {
                lp.setup(cellWidth, cellHeight, mWidthGap, mHeightGap, shortAxisStartPadding,
                        longAxisStartPadding, count, widthSpecSize, this);
            } else {
                lp.setup(cellWidth, cellHeight, mWidthGap, mHeightGap, longAxisStartPadding,
                        shortAxisStartPadding, count, widthSpecSize, this);
            }
            
            if (DBG_LAYOUT) LogUtil.d(TAG, "onMeasure() lp.w:"+lp.width+" lp.h:"+lp.height+" lp.cellX:"+lp.cellX+" lp.cellY:"+lp.cellY);
            if (DBG_LAYOUT) LogUtil.d(TAG, "onMeasure() lp.x:"+lp.x+" lp.y:"+lp.y);

            if (mWrapContent) {
                maxLine = lp.cellY > maxLine ? lp.cellY : maxLine;
            }

            if (lp.regenerateId) {
                child.setId(((getId() & 0xFF) << 16) | (lp.cellX & 0xFF) << 8 | (lp.cellY & 0xFF));
                lp.regenerateId = false;
            }

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
            int childheightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }

        if (mWrapContent) {
            maxLine += 1;
            heightSpecSize += maxLine * (cellHeight + mHeightGap);
            if (mPortrait) {
                heightSpecSize += longAxisStartPadding + longAxisEndPadding;
            } else {
                heightSpecSize += shortAxisStartPadding + shortAxisEndPadding;
            }

            if (DBG_LAYOUT) LogUtil.d(TAG, "onMeasure() got max line num:"+maxLine+" height:"+heightSpecSize);
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

                int childLeft = lp.x;
                int childTop = lp.y;
                if(DBG_LAYOUT) {
                    LogUtil.d(TAG, "onLayout() i:"+i+" count:"+count+" child: " + child);
                    LogUtil.d(TAG, "onLayout() l: " + childLeft + ", t: " + childTop + ", w: " + lp.width + ", h: " + lp.height);
                }
                child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);
            }
        }
    }

    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            // Update the drawing caches
            view.buildDrawingCache(true);
        }
    }

    @Override
    protected void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        super.setChildrenDrawnWithCacheEnabled(enabled);
    }

    int[] findTheFrontVacantArea() {
        int count = getChildCount();

        int[] area = new int[2];
        /* currently only for dock, so will not consider child count > 4 */
        area[0] = count-1;
        area[1] = 0;

        return area;
    }

    /**
     * Find a vacant area that will fit the given bounds nearest the requested
     * cell location. Uses Euclidean distance to score multiple vacant areas.
     * 
     * @param pixelX The X location at which you want to search for a vacant area.
     * @param pixelY The Y location at which you want to search for a vacant area.
     * @param spanX Horizontal span of the object.
     * @param spanY Vertical span of the object.
     * @param vacantCells Pre-computed set of vacant cells to search.
     * @param recycle Previously returned value to possibly recycle.
     * @return The X, Y cell of a vacant area that can contain this object,
     *         nearest the requested location.
     */
    int[] findNearestVacantArea(int pixelX, int pixelY, int spanX, int spanY,
            CellInfo vacantCells, int[] recycle) {
        
        // Keep track of best-scoring drop area
        final int[] bestXY = recycle != null ? recycle : new int[2];
        final int[] cellXY = mCellXY;
        double bestDistance = Double.MAX_VALUE;
        
        // Bail early if vacant cells aren't valid
        if (!vacantCells.valid) {
            return null;
        }

        // Look across all vacant cells for best fit
        final int size = vacantCells.vacantCells.size();
        for (int i = 0; i < size; i++) {
            final CellInfo.VacantCell cell = vacantCells.vacantCells.get(i);
            
            // Reject if vacant cell isn't our exact size
            if (cell.spanX != spanX || cell.spanY != spanY) {
                continue;
            }
            
            // Score is center distance from requested pixel
            cellToPoint(cell.cellX, cell.cellY, cellXY);
            
            double distance = Math.sqrt(Math.pow(cellXY[0] - pixelX, 2) +
                    Math.pow(cellXY[1] - pixelY, 2));
            if (distance <= bestDistance) {
                bestDistance = distance;
                bestXY[0] = cell.cellX;
                bestXY[1] = cell.cellY;
            }
        }

        if (DBG) LogUtil.d(TAG, "findNearestVacantArea() x:"+bestXY[0]+" y:"+bestXY[1]);
        // Return null if no suitable location found 
        if (bestDistance < Double.MAX_VALUE) {
            return bestXY;
        } else {
            return null;
        }
    }
    
    /**
     * Drop a child at the specified position
     *
     * @param child The child that is being dropped
     * @param targetXY Destination area to move to
     */
    void onDropChild(View child, int[] targetXY) {
        if (child != null) {
            clearDragStatus();
            //clearFolderCreationAction();
            if (indexOfChild(child) < 0) 
            {
                if (child.getParent()!=null)
                {
                    if (child.getParent() instanceof ViewGroup)
                        ((ViewGroup)child.getParent()).removeView(child);
                }
                addView(child);
            }

            boolean changed = false;

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int[] t = new int[2];
            t[0] = lp.cellX;
            t[1] = lp.cellY;

            //LogUtil.d(TAG, "onDropChild() x:"+t[0]+" y:"+t[1]);
            if (isTheLastCell(child) && !hasHole(child, t)) {
                findTheLastVacantCell(t, child);
            }

            if ((lp.cellX != t[0] || lp.cellY != t[1]) && (t[0] != -1 && t[1] != -1)) {
                //LogUtil.d(TAG, "onDropChild() new x:"+t[0]+" y:"+t[1]);
                changed = true;
                lp.cellX = t[0];
                lp.cellY = t[1];
            }

            lp.isDragging = false;

            if (changed) {
                cellToPoint(lp.cellX, lp.cellY, targetXY);
            } else {
                targetXY[0] = -1;
                targetXY[1] = -1;
            }

            if (DBG_DRAW) {
                mDragRect.setEmpty();
                mCoveredRect.setEmpty();
            }

            if (mInEditMode) {
                enterChildEditMode(child);
            }
            final ItemInfo cellInfo = (ItemInfo) child.getTag();
            MyApplication.getInstance().mMineViewManager.mSaveModel.addOrUpdate(child);
            MyApplication.getInstance().mMineViewManager.mSaveModel.save();

            child.requestLayout();
            invalidate();
        }
    }

    boolean onDropAborted(View child, int[] target) {
        if (child != null) {
            clearDragStatus();

            if (indexOfChild(child) < 0) addView(child);

            if (target == null) {
                target = new int[2];
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            target[0] = lp.cellX;
            target[1] = lp.cellY;

            //LogUtil.d(TAG, "onDropAborted() x:"+target[0]+" y:"+target[1]+" v.visibility:"+child.getVisibility()+" view:"+child);
            if (!hasHole(child, target)) {
                findTheLastVacantCell(target, child);
            }

            if ((lp.cellX != target[0] || lp.cellY != target[1]) && (target[0] != -1 && target[1] != -1)) {
                //LogUtil.d(TAG, "onDropAborted() new x:"+target[0]+" y:"+target[1]);
                lp.cellX = target[0];
                lp.cellY = target[1];
            }

            lp.isDragging = false;

            if (mInEditMode) {
                enterChildEditMode(child);
            }
           
            final ItemInfo info = (ItemInfo)child.getTag();
                if (info.screen!=mScreenIndex)
                {
                     if (indexOfChild(child) > 0) removeView(child);
                         LogUtil.e(TAG,"=============error in drop ,remove view ");
                         return false;
                     
                }
            MyApplication.getInstance().mMineViewManager.mSaveModel.addOrUpdate(child);

            child.requestLayout();
            invalidate();
        }
         return true;
    }

    /**
     * Start dragging the specified child
     * 
     * @param child The child that is being dragged
     */
    void onDragChild(View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (DBG) LogUtil.d(TAG, "onDragChild() x:"+lp.cellX+" y:"+lp.cellY);

        lp.isDragging = true;

        /* init drag rect */
        if (DBG_DRAW) {
            mDragRect.setEmpty();
            cellToRect(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, mDragRect);
            mCoveredRect.setEmpty();
        }

        /* init drag information */
        clearDragStatus();

        mLastDragCellXY[0] = lp.cellX;
        mLastDragCellXY[1] = lp.cellY;

        clearArrangeAction();
        clearFolderCreationAction();

        child.clearAnimation();
    }

    private static final int AT_LEFT = 0;
    private static final int AT_RIGHT = 1;
    private static final int SAME_LOCATION = 2;

    private int checkLocation(int srcX, int srcY, int dstX, int dstY) {
        if (srcY > dstY) {
            return AT_RIGHT;
        } else if (srcY < dstY) {
            return AT_LEFT;
        } else { // srcY == dstY
            return ((srcX > dstX) ? AT_RIGHT : ((srcX < dstX) ? AT_LEFT : SAME_LOCATION));
        }
    }

    private boolean hasHole(View ignoreView, int[] target) {
        int[] t = new int[2];
        findTheLastVacantCell(t, ignoreView);

        if ((t[0] == -1) || t[1] == -1) {
            if (DBG) LogUtil.d(TAG, "hasHole no vacant cell.");
            return false;
        }

        View view = null;

        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

        int x = t[0]-1; int y = t[1];
        while (true) {
            if (x < 0) {
                x = xCount-1; y--;
                if (y < 0) break;
            }

            view = findCell(x, y);

            //LogUtil.d(TAG, "hasHole() x:"+x+" y:"+y+" view:"+view);
            if (view == null || view == ignoreView) {
                if (DBG) LogUtil.d(TAG, "hasHole() true x:"+x+" y:"+y);
                target[0] = x; target[1] = y;
                return true;
            }

            x--;
        }

        //LogUtil.d(TAG, "hasHole() no hole");
        return false;
    }

    private boolean isTheLastCell(View cell) {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

        View view = null;

        for (int j = yCount - 1; j >= 0; j--) {
            for (int i = xCount - 1; i >= 0; i--) {
                view = findCell(i, j);
                if (view != null) {
                    if (DBG) LogUtil.d(TAG, "isTheLastCell()"+(view==cell));
                    return (view == cell);
                }
            }
        }

        return false;
    }

    public View findTheLastCell(int[] cellXY, View ignoreView) {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

        View view = null;

        for (int j = yCount - 1; j >= 0; j--) {
            for (int i = xCount - 1; i >= 0; i--) {
                view = findCell(i, j);
                if (view != null && view != ignoreView) {
                    cellXY[0] = i;
                    cellXY[1] = j;
                    return view;
                }
            }
        }

        return null;
    }
    final boolean portrait = mPortrait;

    public void findTheLastVacantCell(int[] cellXY, View ignoreView) {

        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

        View view = null;

        int lastX = -1;
        int lastY = -1;

        for (int j = yCount - 1; j >= 0; j--) {
            for (int i = xCount - 1; i >= 0; i--) {
                view = findCell(i, j);
                if (view != null && view != ignoreView) {
                    if (DBG) LogUtil.d(TAG, "findTheLastVacantCell x:"+lastX+" y: "+lastY);
                    cellXY[0] = lastX;
                    cellXY[1] = lastY;
                    return;
                }

                lastX = i; lastY = j;
            }
        }

        cellXY[0] = 0;
        cellXY[1] = 0;

        return;
    }

    private List<View> findTheLastCells(View ignoreCell) {
        List<View> cells = new ArrayList<View>();

        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

        View view = null;

        for (int j = yCount - 1; j >= 0; j--) {
            for (int i = xCount - 1; i >= 0; i--) {
                view = findCell(i, j);
                if (view != null && view != ignoreCell) {
                    if (DBG) LogUtil.d(TAG, "the last cell x:"+i+" y:"+j);
                    cells.add(view);
                    return cells;
                }
            }
        }

        return null;
    }

    /**
     * Drag a child over the specified position
     * 
     * @param child The child that is being dropped
     * @param cellX The child's new x cell location
     * @param cellY The child's new y cell location 
     */
    void onDragOverChild(View child, int cellX, int cellY) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();

        int[] cellXY = mCellXY;

        pointToCellRounded(cellX, cellY, cellXY);
        cellFixSpan(lp.cellHSpan, lp.cellVSpan, cellXY);

        if (DBG) LogUtil.d(TAG, "onDragOverChild() lastx:"+mLastDragCellXY[0]+" lasty:"+mLastDragCellXY[1]);
        if (DBG) LogUtil.d(TAG, "onDragOverChild() x:"+cellXY[0]+" y:"+cellXY[1]);
        final boolean changedXY = (mLastDragCellXY[0] != cellXY[0] || mLastDragCellXY[1] != cellXY[1]);

        if (changedXY) { //if XY changed, area changed too.
            mLastDragArea = AREA_OUTSIDE;
        }

        int area = detectOverArea(cellX, cellY, cellXY[0], cellXY[1]);
        final boolean changedArea = (mLastDragArea != area);

        List<View> cellGroup = null;
        if (changedXY || changedArea) {
            /* remember changed values */
            mLastDragArea = area;

            mLastDragCellXY[0] = cellXY[0];
            mLastDragCellXY[1] = cellXY[1];

            if (DBG_DRAW) {
                cellToRect(cellXY[0], cellXY[1], lp.cellHSpan, lp.cellVSpan, mDragRect);
            }

            /* caculate correct target position */
            final boolean portrait = mPortrait;
            final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
            final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

            /* if there is one icon and drag over on left/right of the icon */
            if (null != findCell(cellXY[0], cellXY[1])) {
                if (checkLocation(lp.cellX, lp.cellY, cellXY[0], cellXY[1]) == AT_RIGHT) {
                    if (area == AREA_INSIDE_RIGHT || area == AREA_OUTSIDE_RIGHT) {
                        if (++cellXY[0] >= xCount) {
                            cellXY[0] = 0;
                            cellXY[1]++;
                        }
                    }
                }

                if (checkLocation(lp.cellX, lp.cellY, cellXY[0], cellXY[1]) == AT_LEFT) {
                    if (area == AREA_INSIDE_LEFT || area == AREA_OUTSIDE_LEFT) {
                        if (--cellXY[0] < 0) {
                            cellXY[0] = xCount-1;
                            cellXY[1]--;
                        }
                    }
                }
            }

            /* find cells */
            cellGroup = findCells(cellXY[0], cellXY[1], lp.cellHSpan, lp.cellVSpan, child);
        }

        boolean accept = cellGroup == null;

        if (false && DBG && (changedXY || changedArea)) {
            LogUtil.d(TAG, "onDragOverChild() changedXY:"+changedXY+" changedArea:"+changedArea+" area:"+area+" accept:"+accept);
            LogUtil.d(TAG, "onDragOverChild() mLastDragCellXY:"+mLastDragCellXY[0]+":"+mLastDragCellXY[1]);
        }

        if (changedXY) {
            clearArrangeAction();
            clearFolderCreationAction();

            if(accept) {
                if (DBG_DRAW) {
                    mDragRectPaint.setColor(Color.GREEN);
                    mDragRectPaint.setAlpha(60);
                }

                lp.cellX = cellXY[0];
                lp.cellY = cellXY[1];

                if (isTheLastCell(child)) {
                    /* to fill the hole */
                    List<View> cg = findTheLastCells(child);
                    if (cg != null) {
                        mArrangeRunnable.setViews(cellXY, child, cg);
                        postDelayed(mArrangeRunnable, ICON_MOVE_TIMEOUT);
                    }
                }
            }

            invalidate();
        }

        if (changedArea && !accept) {
            //wait for arrange action finish
            clearArrangeAction();
            clearFolderCreationAction();

            if (DBG_DRAW) {
                mDragRectPaint.setColor(Color.RED);
                mDragRectPaint.setAlpha(60);
            }

            /* To check if is over cell center to trigger folder logic,
             * or arrange icon if left/right. */
            //if (Launcher.DBG) LogUtil.d(TAG, "to move or create folder. ");
            if ((lp.cellHSpan == 1 && lp.cellVSpan == 1) &&
                    (area == AREA_INSIDE_CENTER)) {
                if (false && mEnableFolderCreation) {
                    if (DBG) LogUtil.d(TAG, "to create folder. ");
                    clearArrangeAction();

                    mFolderCreationRunnable.setViews(cellGroup);
                    postDelayed(mFolderCreationRunnable, FOLDER_CREATION_TIMEOUT);
                } else {
                    if (DBG) LogUtil.d(TAG, "to move during center. ");
                    mArrangeRunnable.setViews(cellXY, child, cellGroup);
                    postDelayed(mArrangeRunnable, ICON_MOVE_TIMEOUT);
                }
            } else if (area == AREA_INSIDE_LEFT || area == AREA_INSIDE_RIGHT ||
                       area == AREA_OUTSIDE_LEFT || area == AREA_OUTSIDE_RIGHT) {
                if (DBG) LogUtil.d(TAG, "to move during left/right. ");
                mArrangeRunnable.setViews(cellXY, child, cellGroup);
                postDelayed(mArrangeRunnable, ICON_MOVE_TIMEOUT);
            }

            invalidate();
        }
    }

    private int detectOverArea(int x, int y, int cellX, int cellY) {
        RectF rect = mCellRect;
        RectF crect = mCoveredRect;

        cellToRect(cellX, cellY, 1, 1, rect);

        float w = rect.right - rect.left; 

        crect.set(rect);
        crect.left += w/3; crect.right -= w/3;

        int area = AREA_OUTSIDE;

        int tx = x + (mCellWidth / 2);
        int ty = y + (mCellHeight / 2);

        //if (DBG) LogUtil.d(TAG, "detectOverArea() x:"+x+" y:"+y+" left:"+rect.left+" right:"+rect.right+" top:"+rect.top+" bottom:"+rect.bottom);
        //if (DBG) LogUtil.d(TAG, "detectOverArea() left:"+crect.left+" right:"+crect.right+" top:"+crect.top+" bottom:"+crect.bottom);
        if (rect.contains(tx, ty)) {
            if (crect.contains(tx, ty)) {
                area = AREA_INSIDE_CENTER;
            } else if (tx < crect.left) { // "equal left" logic is handled in contains() func
                area = AREA_INSIDE_LEFT;
            } else {
                area = AREA_INSIDE_RIGHT;
            }
        } else {
            if (tx < rect.left) {
                area = AREA_OUTSIDE_LEFT;
            } else if (tx >= rect.right) {
                area = AREA_OUTSIDE_RIGHT;
            }
        }

        return area;
    }

    public void clearDrawRects() {
        if (DBG_DRAW) {
            mDragRect.setEmpty();
            mCoveredRect.setEmpty();
            invalidate();
        }
        }

    void cellFixSpan(int spanX, int spanY, int[] cellXY) {
        final boolean portrait = mPortrait;
        final int xAxisCells = portrait?mShortAxisCells:mLongAxisCells;
        final int yAxisCells = portrait?mLongAxisCells:mShortAxisCells;  
        
        if(cellXY[0] + spanX > xAxisCells){cellXY[0] = xAxisCells - spanX;}
        if(cellXY[1] + spanY > yAxisCells){cellXY[1] = yAxisCells - spanY;}
    }

    public void clearFolderCreationAction() {
        mFolderCreationRunnable.setViews(null);
        removeCallbacks(mFolderCreationRunnable);
    }

    public void clearAutoAlignmentAction() {
        mAutoAlignmentRunnable.setDragChild(null);
        removeCallbacks(mAutoAlignmentRunnable);
    }

    public void autoAlignment() {
        autoAlignment(null, 0);
    }

    public void autoAlignmentAdded(View cell){
        final long MOVE_DURATION = 300l;
        final Interpolator interpolator = new DecelerateInterpolator(1.5f);
        final int[] destPoints = new int[2];
        final int[] orgPoints = new int[2];
        cellToPoint(Workspace.COLUME_COUNT, Workspace.ROW_COUNT, orgPoints);
        cellToPoint(Workspace.COLUME_COUNT - 1, Workspace.ROW_COUNT, destPoints);
        Animation moveAnimation = new TranslateAnimation(orgPoints[0]-destPoints[0], 0, orgPoints[1]-destPoints[1], 0);
        moveAnimation.setDuration(MOVE_DURATION);
        moveAnimation.setInterpolator(interpolator);

        if (mInEditMode) {
            enterChildEditMode(cell);
        }

        AnimationSet animationset = (AnimationSet)cell.getAnimation();
        if (animationset == null) {
            animationset = new AnimationSet(false);
        }

        moveAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis()+320 );

        animationset.addAnimation(moveAnimation);

        cell.setAnimation(animationset);
        MyApplication.getInstance().mMineViewManager.mSaveModel.addOrUpdate(cell);
        MyApplication.getInstance().mMineViewManager.mSaveModel.save();
    }
    
    public void autoAlignment(View dragChild) {
        autoAlignment(dragChild, 0);
    }

    private void autoAlignment(View dragChild, int delay) {
        clearAutoAlignmentAction();
        mAutoAlignmentRunnable.setDragChild(dragChild);
        postDelayed(mAutoAlignmentRunnable, delay);
    }

    public void clearArrangeAction() {
        mArrangeRunnable.setViews(new int[]{-1,-1}, null, null);
        removeCallbacks(mArrangeRunnable);
    }

    public void clearDragStatus() {
        mLastDragCellXY[0] = -1;
        mLastDragCellXY[1] = -1;
        mLastDragArea = AREA_OUTSIDE;
    }
 
    /**
     * Finds the Views intersecting with the specified cell. If the cell is outside
     * of the layout, this is returned.
     *
     * @param cellX The X location of the cell to test.
     * @param cellY The Y location of the cell to test.
     * @param spanX The horizontal span of the cell to test.
     * @param spanY The vertical span of the cell to test.
     * @param ignoreCell View to ignore during the test.
     *
     * @return Returns the first View intersecting with the specified cell, this if the cell
     *         lies outside of this layout's grid or null if no View was found.
     */    
    List<View> findCells(int cellX, int cellY, int spanX, int spanY, View ignoreCell) {
        List<View> cells = new ArrayList<View>();

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            if (view == ignoreCell) {
                if (DBG) LogUtil.d(TAG, "findCells() ignore");
                continue;
            }

            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp == null) {
                if (DBG) LogUtil.d(TAG, "findCells() lp null.");
                continue;
            }

            if (false && DBG) {
                LogUtil.d(TAG, "findCells() i: "+i);
                LogUtil.d(TAG, "findCells() cellX:"+cellX+" spanX:"+spanX+" lp.cellX:"+lp.cellX+" lp.cellHSpan:"+lp.cellHSpan);
                LogUtil.d(TAG, "findCells() cellY:"+cellY+" spanY:"+spanY+" lp.cellY:"+lp.cellY+" lp.cellVSpan:"+lp.cellVSpan);
            }

            if (cellX < lp.cellX + lp.cellHSpan&& lp.cellX < cellX + spanX &&
                    cellY < lp.cellY + lp.cellVSpan&& lp.cellY < cellY + spanY) {
            if (DBG) LogUtil.d(TAG, "findCells() add i: "+i);
                cells.add(view);
            }
        }

        if(cells.isEmpty()) {
            return null;
        }else {
            return cells;
        }
    }

    View findCell(int cellX, int cellY) {
        if (cellX < 0 || cellY < 0) return null;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);

            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp == null) continue;

            if ((lp.cellX <= cellX && cellX < lp.cellX + lp.cellHSpan) &&
                (lp.cellY <= cellY && cellY < lp.cellY + lp.cellVSpan)) {
                //if (DBG) LogUtil.d(TAG, "findCell @ "+lp.cellX+":"+lp.cellY+" with span "+lp.cellVSpan+":"+lp.cellHSpan);
                return view;
            }
        }

        //if (DBG) LogUtil.d(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! findCell() return NULL.");
        return null;
    }


    /**
     * Computes a bounding rectangle for a range of cells
     *  
     * @param cellX X coordinate of upper left corner expressed as a cell position
     * @param cellY Y coordinate of upper left corner expressed as a cell position
     * @param cellHSpan Width in cells 
     * @param cellVSpan Height in cells
     * @param dragRect Rectnagle into which to put the results
     */
    public void cellToRect(int cellX, int cellY, int cellHSpan, int cellVSpan, RectF dragRect) {
        final boolean portrait = mPortrait;
        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;
        final int widthGap = mWidthGap;
        final int heightGap = mHeightGap;
        
        final int hStartPadding = portrait ? mShortAxisStartPadding : mLongAxisStartPadding;
        final int vStartPadding = portrait ? mLongAxisStartPadding : mShortAxisStartPadding;
        
        int width = cellHSpan * cellWidth + ((cellHSpan - 1) * widthGap);
        int height = cellVSpan * cellHeight + ((cellVSpan - 1) * heightGap);

        int x = hStartPadding + cellX * (cellWidth + widthGap);
        int y = vStartPadding + cellY * (cellHeight + heightGap);
        
        dragRect.set(x, y, x + width, y + height);
    }
    
    /**
     * Computes the required horizontal and vertical cell spans to always 
     * fit the given rectangle.
     *  
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public int[] rectToCell(int width, int height) {
        // Always assume we're working with the smallest span to make sure we
        // reserve enough space in both orientations.
        final Resources resources = getResources();
        int actualWidth = resources.getDimensionPixelSize(R.dimen.workspace_cell_width);
        int actualHeight = resources.getDimensionPixelSize(R.dimen.workspace_cell_height);
        int smallerSize = Math.min(actualWidth, actualHeight);

        // Always round up to next largest cell
        int spanX = (width + smallerSize) / smallerSize;
        int spanY = (height + smallerSize) / smallerSize;

        return new int[] { spanX, spanY };
    }

    /**
     * Find the first vacant cell, if there is one.
     *
     * @param vacant Holds the x and y coordinate of the vacant cell
     * @param spanX Horizontal cell span.
     * @param spanY Vertical cell span.
     * 
     * @return True if a vacant cell was found
     */
    public boolean getVacantCell(int[] vacant, int spanX, int spanY) {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
        final boolean[][] occupied = mOccupied;

        findOccupiedCells(xCount, yCount, occupied, null);

        return findVacantCell(vacant, spanX, spanY, xCount, yCount, occupied);
    }

    static boolean findVacantCell(int[] vacant, int spanX, int spanY,
            int xCount, int yCount, boolean[][] occupied) {

        //if (DBG) LogUtil.d(TAG, "findVacantCell() xCount:"+xCount+" yCount:"+yCount);
        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                //boolean available = !occupied[x][y];
                boolean available = !occupied[x][y] && x + spanX <= xCount && y + spanY <= yCount;

                //if (DBG) LogUtil.d(TAG, "findVacantCell() x:"+x+" y:"+y+" available:"+available);
out:            for (int i = x; i < x + spanX && x < xCount; i++) {
                    for (int j = y; j < y + spanY && y < yCount; j++) {
                        available = available && !occupied[i][j];
                        if (!available) break out;
                    }
                }

                if (available) {
                    vacant[0] = x;
                    vacant[1] = y;
                    if (DBG) LogUtil.d(TAG, "findVacantCell() x:"+x+" y:"+y);
                    return true;
                }
            }
        }

        if (DBG) LogUtil.d(TAG, "findAllVacantCell() cannot find any vacant cell.");
        return false;
    }

    boolean[] getOccupiedCells() {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
        final boolean[][] occupied = mOccupied;

        findOccupiedCells(xCount, yCount, occupied, null);

        final boolean[] flat = new boolean[xCount * yCount];
        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                flat[y * xCount + x] = occupied[x][y];
            }
        }

        return flat;
    }

    private void findOccupiedCells(int xCount, int yCount, boolean[][] occupied, View ignoreView) {
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                occupied[x][y] = false;
            }
        }

        int count = getChildCount();
        //if(DBG) LogUtil.d(TAG, "findOccupiedCells() count: " + count);
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (false || child.equals(ignoreView)) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if ((lp.cellX < 0) || (lp.cellY < 0)) continue;

            for (int x = lp.cellX; x < lp.cellX + lp.cellHSpan && x < xCount; x++) {
                for (int y = lp.cellY; y < lp.cellY + lp.cellVSpan && y < yCount; y++) {
                    //if(DBG) LogUtil.d(TAG, "findOccupiedCells() occupied true x: " + x + ", y: " + y);
                    occupied[x][y] = true;
                }
            }
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CellLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof CellLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new CellLayout.LayoutParams(p);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        /**
         * Horizontal location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellX;

        /**
         * Vertical location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellY;

        /**
         * Number of cells spanned horizontally by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellHSpan;

        /**
         * Number of cells spanned vertically by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellVSpan;
        
        /**
         * Is this item currently being dragged
         */
        public boolean isDragging;

        // X coordinate of the view in the layout.
        @ViewDebug.ExportedProperty
        int x;
        // Y coordinate of the view in the layout.
        @ViewDebug.ExportedProperty
        int y;

        boolean regenerateId;
        
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            cellHSpan = 1;
            cellVSpan = 1;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            cellHSpan = 1;
            cellVSpan = 1;
        }
        
        public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
            super(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = cellHSpan;
            this.cellVSpan = cellVSpan;
        }

        public void setup(int cellWidth, int cellHeight, int widthGap, int heightGap,
                int hStartPadding, int vStartPadding, int count, int totalWidth, View context) {
            
            final int myCellHSpan = cellHSpan;
            final int myCellVSpan = cellVSpan;
            final int myCellX = cellX;
            final int myCellY = cellY;
            
            /*
            if (DBG) LogUtil.d(TAG, "myCellHSpan:"+myCellHSpan);
            if (DBG) LogUtil.d(TAG, "cellWidth:"+cellWidth);
            if (DBG) LogUtil.d(TAG, "widthGap:"+widthGap);
            if (DBG) LogUtil.d(TAG, "leftMargin:"+leftMargin);
            if (DBG) LogUtil.d(TAG, "rightMargin:"+rightMargin);
            */
            width = myCellHSpan * cellWidth + ((myCellHSpan - 1) * widthGap) -
                    leftMargin - rightMargin;
            height = myCellVSpan * cellHeight + ((myCellVSpan - 1) * heightGap) -
                    topMargin - bottomMargin;

            if (false) {
                count = count > 4 ? 4 : count; // hack for dock
                int left = (totalWidth - count * cellWidth - (count - 1) * widthGap) / 2 - hStartPadding;
                if (DBG_LAYOUT) {
                    LogUtil.d(TAG, "LayoutParams.setup() left:"+left);
                    LogUtil.d(TAG, "LayoutParams.setup() totalWidth:"+totalWidth);
                    LogUtil.d(TAG, "LayoutParams.setup() widthGap:"+widthGap);
                    LogUtil.d(TAG, "LayoutParams.setup() hStartPadding:"+hStartPadding);
                    LogUtil.d(TAG, "LayoutParams.setup() myCellX:"+myCellX);
                    LogUtil.d(TAG, "LayoutParams.setup() cellWidth:"+cellWidth);
                    LogUtil.d(TAG, "LayoutParams.setup() leftMargin:"+leftMargin);
                }
                
                left = left > 0 ? left : 0;
                
                x = hStartPadding + left + myCellX * (cellWidth + widthGap) + leftMargin;
                y = vStartPadding + myCellY * (cellHeight + heightGap) + topMargin;
            } else {
                x = hStartPadding + myCellX * (cellWidth + widthGap) + leftMargin;
                y = vStartPadding + myCellY * (cellHeight + heightGap) + topMargin;
            }
        }
    }

    static class CellInfo implements ContextMenu.ContextMenuInfo {
        /**
         * See View.AttachInfo.InvalidateInfo for futher explanations about
         * the recycling mechanism. In this case, we recycle the vacant cells
         * instances because up to several hundreds can be instanciated when
         * the user long presses an empty cell.
         */
        static  class VacantCell {
            int cellX;
            int cellY;
            int spanX;
            int spanY;

            // We can create up to 523 vacant cells on a 4x4 grid, 100 seems
            // like a reasonable compromise given the size of a VacantCell and
            // the fact that the user is not likely to touch an empty 4x4 grid
            // very often 
            private static final int POOL_LIMIT = 100;
            private static final Object sLock = new Object();

            private static int sAcquiredCount = 0;
            private static VacantCell sRoot;

            private VacantCell next;

            static VacantCell acquire() {
                synchronized (sLock) {
                    if (sRoot == null) {
                        return new VacantCell();
                    }

                    VacantCell info = sRoot;
                    sRoot = info.next;
                    sAcquiredCount--;

                    return info;
                }
            }

            void release() {
                synchronized (sLock) {
                    if (sAcquiredCount < POOL_LIMIT) {
                        sAcquiredCount++;
                        next = sRoot;
                        sRoot = this;
                    }
                }
            }

            @Override
            public String toString() {
                return "VacantCell[x=" + cellX + ", y=" + cellY + ", spanX=" + spanX +
                        ", spanY=" + spanY + "]";
            }
        }

        View cell;
        int cellX;
        int cellY;
        int spanX;
        int spanY;
        int screen;
        int originScreen;
        boolean valid;

        final ArrayList<VacantCell> vacantCells = new ArrayList<VacantCell>(VacantCell.POOL_LIMIT);
        int maxVacantSpanX;
        int maxVacantSpanXSpanY;
        int maxVacantSpanY;
        int maxVacantSpanYSpanX;
        final Rect current = new Rect();

        void clearVacantCells() {
            final ArrayList<VacantCell> list = vacantCells;
            final int count = list.size();

            for (int i = 0; i < count; i++) list.get(i).release();

            list.clear();
        }

        void findVacantCellsFromOccupied(boolean[] occupied, int xCount, int yCount) {
            if (cellX < 0 || cellY < 0) {
                maxVacantSpanX = maxVacantSpanXSpanY = Integer.MIN_VALUE;
                maxVacantSpanY = maxVacantSpanYSpanX = Integer.MIN_VALUE;
                clearVacantCells();
                return;
            }

            final boolean[][] unflattened = new boolean[xCount][yCount];
            for (int y = 0; y < yCount; y++) {
                for (int x = 0; x < xCount; x++) {
                    unflattened[x][y] = occupied[y * xCount + x];
                }
            }
            CellLayout.findIntersectingVacantCells(this, cellX, cellY, xCount, yCount, unflattened);
        }

        /**
         * This method can be called only once! Calling #findVacantCellsFromOccupied will
         * restore the ability to call this method.
         *
         * Finds the upper-left coordinate of the first rectangle in the grid that can
         * hold a cell of the specified dimensions.
         *
         * @param cellXY The array that will contain the position of a vacant cell if such a cell
         *               can be found.
         * @param spanX The horizontal span of the cell we want to find.
         * @param spanY The vertical span of the cell we want to find.
         *
         * @return True if a vacant cell of the specified dimension was found, false otherwise.
         */
        boolean findCellForSpan(int[] cellXY, int spanX, int spanY) {
            return findCellForSpan(cellXY, spanX, spanY, true);
        }

        boolean findCellForSpan(int[] cellXY, int spanX, int spanY, boolean clear) {
            final ArrayList<VacantCell> list = vacantCells;
            final int count = list.size();

            boolean found = false;

            if (this.spanX >= spanX && this.spanY >= spanY) {
                cellXY[0] = cellX;
                cellXY[1] = cellY;
                found = true;
            }

            // Look for an exact match first
            for (int i = 0; i < count; i++) {
                VacantCell cell = list.get(i);
                if (cell.spanX == spanX && cell.spanY == spanY) {
                    cellXY[0] = cell.cellX;
                    cellXY[1] = cell.cellY;
                    found = true;
                    break;
                }
            }

            // Look for the first cell large enough
            for (int i = 0; i < count; i++) {
                VacantCell cell = list.get(i);
                if (cell.spanX >= spanX && cell.spanY >= spanY) {
                    cellXY[0] = cell.cellX;
                    cellXY[1] = cell.cellY;
                    found = true;
                    break;
                }
            }

            if (clear) clearVacantCells();

            return found;
        }

        @Override
        public String toString() {
            return "Cell[view=" + (cell == null ? "null" : cell.getClass()) + ", x=" + cellX +
                    ", y=" + cellY + "]";
        }
    }

    public boolean lastDownOnOccupiedCell() {
        return mLastDownOnOccupiedCell;
    }

    private class FolderCreationRunnable implements Runnable {
        private List<View> mCells;

        FolderCreationRunnable() {
        }

        public void run() {
            /*Workspace workspace = (Workspace)getParent();

            if (mCells == null) return;

            if (Launcher.DBG) LogUtil.d(TAG, "FolderCreationRunnable:run()...");
            for (View view : mCells) {
                if (view.getTag() instanceof ShortcutInfo) {
                    AppIcon icon = workspace.onCreateFolder(CellLayout.this, view);
                    if (mInEditMode) {
                        enterChildEditMode(icon);
                    }
                    break;
                }
            } */
        }

        void setViews(List<View> cells) {
            mCells = cells;
        }
    }

    int[] cellPrivousPosition(int[] cellXY) {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        //final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

        int[] t = new int[2];
        t[0] = cellXY[0];
        t[1] = cellXY[1];

        t[0]--;

        if (t[0] < 0) {
            t[0] = xCount - 1;
            t[1]--;
        }

        if (t[1] < 0) {
            t[0] = -1;
            t[1] = -1;
        }

        return t;
    }

    int[] cellNextPosition(int[] cellXY) {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

        int[] t = new int[2];
        t[0] = cellXY[0];
        t[1] = cellXY[1];

        t[0]++;

        if (t[0] >= xCount) {
            t[0] = 0;
            t[1]++;
        }

        if (t[1] >= yCount) {
            t[0] = -1;
            t[1] = -1;
        }

        return t;
    }

    boolean cellEqual(int[] srcCellXY, int[]targetCellXY) {
        if (srcCellXY == null || targetCellXY == null) return false;
        return (srcCellXY[0] == targetCellXY[0] && srcCellXY[1] == targetCellXY[1]);
    }

    private class AutoAlignmentRunnable implements Runnable {
        private final long MOVE_DURATION = 200l;
        private final Interpolator interpolator = new DecelerateInterpolator(1.5f);
        private View dragChild;

        AutoAlignmentRunnable() {
        }

        public void setDragChild(View child) {
            dragChild = child;
        }

        private int[] nextOccupiedCell(boolean[][] occupied, int[] cellXY) {
            int[] t = new int[2];
            t[0] = cellXY[0];
            t[1] = cellXY[1];

            while (true) {
                t = cellNextPosition(t);
                if (isOccupied(occupied, t)) {
                    return t;
                }
            }
        }

        private int[] lastVacantCell(boolean[][] occupied, int xCount, int yCount) {
            for (int j = yCount - 1; j >= 0; j--) {
                for (int i = xCount - 1; i >= 0; i--) {
                    if (occupied[i][j] == true) {
                        /* Last vacant cell */
                        int[] t = new int[2];
                        t[0] = i;
                        t[1] = j;

                        return cellNextPosition(t);
                    }
                }
            }

            return null;
        }

        private int[] firstVacantCell(boolean[][] occupied, int xCount, int yCount) {
            for (int j = 0; j < yCount; j++) {
                for (int i = 0; i < xCount; i++) {
                    if (occupied[i][j] == false) {
                        /* First vacant cell */
                        int[] t = new int[2];
                        t[0] = i;
                        t[1] = j;

                        return t;
                    }
                }
            }

            return null;
        }

        private int[] firstHole(boolean[][] occupied, int xCount, int yCount) {
            int[] t = firstVacantCell(occupied, xCount, yCount);
            int[] t2 = lastVacantCell(occupied, xCount, yCount);
            if (cellEqual(t, t2)) {
                return null;
            } else {
                return t;
            }
        }

        public void run() {
            LayoutParams llp = null;

            Map<View, int[]> moveMap = new HashMap<View, int[]>();
            List<View> tmpCells = new ArrayList<View>();

            final boolean portrait = mPortrait;
            final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
            final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

            final boolean[][] occupied = mOccupied;
            findOccupiedCells(xCount, yCount, occupied, dragChild);

            do {
                int[] t = firstHole(occupied, xCount, yCount);
                
                if (t == null) break;

                if (DBG) LogUtil.d(TAG, "firstHole :" + t[0] + ":" + t[1]);
                int[] t2 = nextOccupiedCell(occupied, t);

                View c = findCell(t2[0], t2[1]);
                if (c == null) break;

                moveMap.put(c, t);
                tmpCells.add(c);

                changeOccupied(occupied, t[0], t[1], 1, 1, true);
                changeOccupied(occupied, t2[0], t2[1], 1, 1, false);
            } while (true);

            long delay = 0;
            for (View cell : tmpCells) {
                final LayoutParams clp = (LayoutParams) cell.getLayoutParams();
                final int[] vacant = moveMap.get(cell);

                if(vacant == null) break;

                final int[] orgPoints = new int[2]; 
                orgPoints[0] = clp.x;
                orgPoints[1] = clp.y;
                //cellToPoint(clp.cellX, clp.cellY, orgPoints);

                final int[] destPoints = new int[2];
                cellToPoint(vacant[0], vacant[1], destPoints);

                if (DBG) {
                    LogUtil.d(TAG, "AutoAlignmentRunnable::Run() cell:"+cell+" from "+
                        clp.cellX+":"+clp.cellY+" to "+vacant[0]+":"+vacant[1]);
                    LogUtil.d(TAG, "AutoAlignmentRunnable::Run() points from "+
                        orgPoints[0]+":"+orgPoints[1]+" to "+destPoints[0]+":"+destPoints[1]);
                }

                Animation moveAnimation = new TranslateAnimation(orgPoints[0] - destPoints[0], 0, orgPoints[1] - destPoints[1], 0);
                moveAnimation.setDuration(MOVE_DURATION);
                moveAnimation.setInterpolator(interpolator);

                if (mInEditMode) {
                    enterChildEditMode(cell);
                }

                AnimationSet animationset = (AnimationSet)cell.getAnimation();
                if (animationset == null) {
                    animationset = new AnimationSet(false);
                }

                moveAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis()+delay);
                delay += 30;

                animationset.addAnimation(moveAnimation);

                cell.setAnimation(animationset);

                clp.cellX = vacant[0];
                clp.cellY = vacant[1];
                final ItemInfo info = (ItemInfo)cell.getTag();
                MyApplication.getInstance().mMineViewManager.mSaveModel.addOrUpdate(cell);
            }

            MyApplication.getInstance().mMineViewManager.mSaveModel.save();

            CellLayout.this.requestLayout();
            CellLayout.this.invalidate();
        }
    }

    private class ArrangeRunnable implements Runnable {
        private View dragChild;
        private List<View> cells;
        private int[] targetXY = new int[2];

        private final Interpolator interpolator = new DecelerateInterpolator(1.5f);
        private final long MOVE_DURATION = 300l;
        
        ArrangeRunnable() {
        }

        public void run() {
            synchronized (mArrangeLock) {
            
            List<View> tmpCells = new ArrayList<View>();

            if(cells == null) {return;}

            LayoutParams lp = (LayoutParams) dragChild.getLayoutParams();
            final ItemInfo ci = (ItemInfo)dragChild.getTag();
            
            final boolean portrait = mPortrait;
            final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
            final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
            final boolean[][] occupied = mOccupied;

            int cellX = targetXY[0];
            int cellY = targetXY[1];
            
            Map<View, int[]> moveMap = new HashMap<View, int[]>();
            boolean canMove = true;
            
            findOccupiedCells(xCount, yCount, occupied, dragChild);

            if (DBG) {
                LogUtil.d(TAG, "ArrangeRunnable:run() current screen:"+mCellInfo.screen+", cell screen:"+ci.screen+",mScreenIndex "+mScreenIndex+"container "+ci.container+",id "+ci.id);
                LogUtil.d(TAG, "ArrangeRunnable:run() lp.cellXY "+lp.cellX+":"+lp.cellY+" lp.cellHSpan "+lp.cellHSpan+":"+lp.cellVSpan);
                LogUtil.d(TAG, "ArrangeRunnable:run() target "+cellX+":"+cellY+" child count:"+CellLayout.this.getChildCount()+",dragchild "+dragChild);
            }

            //workaround for lose or overlap icons bug
            if (mCellInfo.screen != mScreenIndex) {
                if (LauncherSettings.Favorites.CONTAINER_DOCK==ci.container) {
                    /*
                       if (mCellInfo.screen==0)
                       mCellInfo.screen=mScreenIndex;
                       else if (mScreenIndex==0)
                       */
                }
            }

            if (LauncherSettings.Favorites.CONTAINER_DOCK == ci.container)
                ci.screen = 0;

            if ((mScreenIndex== ci.screen) && (lp.cellX >= 0 && lp.cellY >= 0)) {
                changeOccupied(occupied, lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, false);
            }

            // to generate move map
            // assume only one cell in cells temporarily
            for (View cell : cells) {
                final LayoutParams clp = (LayoutParams) cell.getLayoutParams();
                int[] vacant = new int[2];

                // clear org cell occupied & add a span 1 cell for change the new vacant cell pos
                changeOccupied(occupied, clp.cellX, clp.cellY, clp.cellHSpan, clp.cellVSpan, false);                
                changeOccupied(occupied, cellX, cellY, lp.cellHSpan, lp.cellVSpan, true);

                boolean found = findVacantCell(vacant, clp.cellHSpan, clp.cellVSpan, xCount, yCount, occupied);


                if(found) {
                    if (DBG) LogUtil.d(TAG, "ArrangeRunnable:run() vacant "+vacant[0]+":"+vacant[1]+" cell "+cellX+":"+cellY);

                    boolean forward = false; 
                    if (cellY < vacant[1]) forward = true;
                    else if (cellY > vacant[1]) forward = false;
                    else forward = (cellX < vacant[0]); // cellX != vacant[0]

                    if (!forward) {
                        int i = vacant[0];
                        int j = vacant[1];
                        int ti = i;
                        int tj = j;

                        LayoutParams llp = clp;

                        while (j <= cellY) {
                            if ((j == cellY) && (i >= cellX)) break;

                            int[] t = new int[2];
                            t[0] = ti; t[1] = tj;

                            if ((i+1) < xCount) {
                                i = i+1;
                            } else {
                                i = 0; j = j+1;
                            }

                            if ((i == lp.cellX) && (j == lp.cellY)) {
                                if (DBG) LogUtil.d(TAG, "ArrangeRunnable:run() ignore the drag cell...");
                                continue;
                            }

                            View c = findCell(i, j);
                            if (DBG) LogUtil.d(TAG, "ArrangeRunnable:run() backward i:"+i+" j:"+j+" t[0]:"+t[0]+" t[1]:"+t[1]);

                            if (c == null) {
                                if (DBG) LogUtil.d(TAG, "ArrangeRunnable:run() backward v null");
                                break;
                            }

                            /* To check target cell span. */
                            llp = (LayoutParams) c.getLayoutParams();
                            //if (Launcher.DBG) LogUtil.d(TAG, "ArrangeRunnable:run() llp.cellHSpan:"+llp.cellHSpan+" llp.cellVSpan:"+llp.cellVSpan);
                            //if (Launcher.DBG) LogUtil.d(TAG, "ArrangeRunnable:run() clp.cellHSpan:"+clp.cellHSpan+" clp.cellVSpan:"+clp.cellVSpan);
                            if (llp.cellHSpan > clp.cellHSpan || llp.cellVSpan > clp.cellVSpan ||
                                    (!(c instanceof AppIcon))) {
                                if (DBG) LogUtil.d(TAG, "ArrangeRunnable:run() continue...");
                                continue;
                            }

                            ti = i; tj = j;

                            if (DBG) LogUtil.d(TAG, "ArrangeRunnable:run() to put move map...");
                            moveMap.put(c, t);
                            tmpCells.add(c);

                            changeOccupied(occupied, t[0], t[1], clp.cellHSpan, clp.cellVSpan, true);
                        };
                    } else { // backward
                        int i = cellX;
                        int j = cellY;
                        int[] tt = new int[2];
                        tt[0] = cellX;
                        tt[1] = cellY;

                        LayoutParams llp = clp;

                        while (j <= vacant[1]) {
                            if ((j == vacant[1]) && (i >= vacant[0])) break;

                            int[] t = new int[2];
                            t[0] = tt[0];
                            t[1] = tt[1];

                            if ((t[0] + llp.cellHSpan) < xCount) {
                                t[0] += llp.cellHSpan;
                            } else {
                                t[0] = 0; t[1]++;
                            }

                            View c = findCell(i, j);
                            if (DBG) LogUtil.d(TAG, "ArrangeRunnable:run() forward i:"+i+" j:"+j+" t[0]:"+t[0]+" t[1]:"+t[1]);

                            if (c == null) {
                                if (DBG) LogUtil.d(TAG, "ArrangeRunnable:run() forward v null");
                                break;
                            }

                            /* To check target cell span. */
                            View tc = findCell(t[0], t[1]);
                            if (tc != null) {
                                llp = (LayoutParams) tc.getLayoutParams();
                                //if (Launcher.DBG) LogUtil.d(TAG, "ArrangeRunnable:run() llp.cellHSpan:"+llp.cellHSpan+" llp.cellVSpan:"+llp.cellVSpan);
                                //if (Launcher.DBG) LogUtil.d(TAG, "ArrangeRunnable:run() clp.cellHSpan:"+clp.cellHSpan+" clp.cellVSpan:"+clp.cellVSpan);
                                if (llp.cellHSpan > clp.cellHSpan || llp.cellVSpan > clp.cellVSpan ||
                                        (!(c instanceof AppIcon))) {
                                    if (DBG) LogUtil.d(TAG, "ArrangeRunnable:run() continue...");
                                    tt[0] = t[0]; tt[1] = t[1];
                                    continue;
                                }
                            }

                            i = tt[0] = t[0]; j = tt[1] = t[1];

                            //if (Launcher.DBG) LogUtil.d(TAG, "ArrangeRunnable:run() to put move map...");
                            moveMap.put(c, t);
                            tmpCells.add(c);

                            changeOccupied(occupied, t[0], t[1], clp.cellHSpan, clp.cellVSpan, true);
                        }
                    }
                } else { // if not (findVacantCell(...))
                    canMove = false;
                    break;
                }
            } // for(...)

            if(canMove) {
                long delay = 300;
                for (View cell : tmpCells) {
                    final LayoutParams clp = (LayoutParams) cell.getLayoutParams();
                    final int[] vacant = moveMap.get(cell);

                    if(vacant == null) break;

                    final int[] orgPoints = new int[2]; 
                    orgPoints[0] = clp.x;
                    orgPoints[1] = clp.y;
                    //cellToPoint(clp.cellX, clp.cellY, orgPoints);

                    final int[] destPoints = new int[2];
                    cellToPoint(vacant[0], vacant[1], destPoints);

                    if (DBG) {
                        LogUtil.d(TAG, "ArrangeRunnable() cell from "+clp.cellX+":"+clp.cellY+" to "+vacant[0]+":"+vacant[1]);
                        LogUtil.d(TAG, "ArrangeRunnable() points from "+orgPoints[0]+":"+orgPoints[1]+" to "+destPoints[0]+":"+destPoints[1]);
                    }

                    Animation moveAnimation = new TranslateAnimation(orgPoints[0]-destPoints[0], 0, orgPoints[1]-destPoints[1], 0);
                    moveAnimation.setDuration(MOVE_DURATION);
                    moveAnimation.setInterpolator(interpolator);

                    if (mInEditMode) {
                        enterChildEditMode(cell);
                    }

                    AnimationSet animationset = (AnimationSet)cell.getAnimation();
                    if (animationset == null) {
                        animationset = new AnimationSet(false);
                    }

                    moveAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis()+delay);
                    delay += 30;

                    animationset.addAnimation(moveAnimation);

                    cell.setAnimation(animationset);

                    clp.cellX = vacant[0];
                    clp.cellY = vacant[1];
                    final ItemInfo info = (ItemInfo)cell.getTag();
                    MyApplication.getInstance().mMineViewManager.mSaveModel.addOrUpdate(cell);
                }

                lp.cellX = cellX;
                lp.cellY = cellY;

                ci.screen = mCellInfo.screen;
                final ItemInfo info = (ItemInfo)dragChild.getTag();
                MyApplication.getInstance().mMineViewManager.mSaveModel.addOrUpdate(dragChild);

                if (DBG_DRAW) {
                    mDragRectPaint.setColor(Color.GREEN);
                    mDragRectPaint.setAlpha(60);
                }
            } else if (DBG_DRAW) {
                mDragRectPaint.setColor(Color.RED);
                mDragRectPaint.setAlpha(60);
            }

            CellLayout.this.requestLayout();
            CellLayout.this.invalidate();

            } //mArrangeLock
        }

        void setViews(int[] targetXY, View dragChild, List<View> cells) {
            if (DBG) LogUtil.d(TAG, "setViews targetX:"+targetXY[0]+" targetY:"+targetXY[1]);
            this.dragChild = dragChild;
            this.cells = cells;
            this.targetXY[0] = targetXY[0];
            this.targetXY[1] = targetXY[1];
        }
    }

    private void dumpOccupied() {
        final boolean[][] occupied = mOccupied;

        int ycount = 0;
        int xcount = 0;
        if (mPortrait) {
            xcount = mShortAxisCells;
            ycount = mLongAxisCells;
        } else {
            xcount = mLongAxisCells;
            ycount = mShortAxisCells;
        }

        for (int y = 0; y < ycount; y++) {
            String line = "";
            for (int x = 0; x < xcount; x++) {
                line += ((occupied[x][y]) ? "#" : ".");
            }
            //LogUtil.d(TAG, "[ " + line + " ]");
        }
    }

    private void changeOccupied(boolean[][] occupied, int cellX, int cellY,
            int spanX, int spanY, boolean status) {
        for (int x = 0; x < occupied.length; x++) {
            for (int y = 0; y < occupied[x].length; y++) {
                if(x >= cellX && x < cellX + spanX && y >= cellY && y < cellY + spanY) {
                    if (DBG) LogUtil.d(TAG, "changeOccupied() x:"+x+" y:"+y+" status:"+status);
                    occupied[x][y] = status;
                }
            }
        }            
    }

    private boolean isOccupied(boolean[][] occupied, int[] cellXY) {
        if (cellXY[0] < 0 || cellXY[1] < 0) return true;
        return occupied[cellXY[0]][cellXY[1]];
    }

    public boolean isEmpty() {
        return getChildCount() <= 0;
    }

    public boolean isFull() {
        return getChildCount() >= (mShortAxisCells * mLongAxisCells);
    }
}
