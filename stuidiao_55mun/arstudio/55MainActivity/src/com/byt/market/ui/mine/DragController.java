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
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.byt.market.MyApplication;
import com.byt.market.activity.MainActivity;
import com.byt.market.util.LogUtil;

/**
 * Class for initiating a drag within a view or across multiple views.
 */
public class DragController {
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Boolean DBG_UI = false;
    private static final String TAG = "Launcher.DragController";

    /** Indicates the drag is a move.  */
    public static int DRAG_ACTION_MOVE = 0;

    /** Indicates the drag is a copy.  */
    public static int DRAG_ACTION_COPY = 1;

    private static final int SCROLL_DELAY = 600;
    private static final int SCROLL_ZONE = 20;
    private static final int VIBRATE_DURATION = 35;

    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;

    private static final int SCROLL_OUTSIDE_ZONE = 0;
    private static final int SCROLL_WAITING_IN_ZONE = 1;

    private static final int SCROLL_LEFT = 0;
    private static final int SCROLL_RIGHT = 1;
    private static final int SCROLL_TOP = 2;
    private static final int SCROLL_BOTTOM = 3;

    private Context mContext;
    private Handler mHandler;
    private final Vibrator mVibrator;// = new Vibrator();

    // temporaries to avoid gc thrash
    private Rect mRectTemp = new Rect();
    private final int[] mCoordinatesTemp = new int[2];

    /** Whether or not we're dragging. */
    private boolean mDragging;

    /** X coordinate of the down event. */
    private float mMotionDownX;

    /** Y coordinate of the down event. */
    private float mMotionDownY;
    
    private long mMotionDownTime;

    /** Info about the screen for clamping. */
    private DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    /** Original view that is being dragged.  */
    private View mOriginator;

    /** X offset from the upper-left corner of the cell to where we touched.  */
    private float mTouchOffsetX;

    /** Y offset from the upper-left corner of the cell to where we touched.  */
    private float mTouchOffsetY;

    /** Where the drag originated */
    private DragSource mDragSource;

    /** The data associated with the object being dragged */
    private Object mDragInfo;

    /** The view that moves around while you drag.  */
    private DragView mDragView;

    /** Who can receive drop events */
    private ArrayList<DropTarget> mDropTargets = new ArrayList<DropTarget>();

    private DragListener mListener;

    /** The window token used as the parent for the DragView. */
    private IBinder mWindowToken;

    /** The view that will be scrolled when dragging to the left and right edges of the screen. */
    private View mScrollView;

    private View mMoveTarget;

    private DragScroller mDragScroller;
    private int mScrollState = SCROLL_OUTSIDE_ZONE;
    private ScrollRunnable mScrollRunnable = new ScrollRunnable();

    private RectF mDeleteRegion;
    private DropTarget mLastDropTarget;

    private InputMethodManager mInputMethodManager;

    private int mTop;
    
    
    public void setTopMargin(int top){
    	mTop = top;
    }
    /**
     * Interface to receive notifications when a drag starts or stops
     */
    interface DragListener {
        
        /**
         * A drag has begun
         * 
         * @param source An object representing where the drag originated
         * @param info The data associated with the object that is being dragged
         * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE}
         *        or {@link DragController#DRAG_ACTION_COPY}
         */
        void onDragStart(DragSource source, Object info, int dragAction);
        
        /**
         * The drag has eneded
         */
        void onDragEnd();
    }
    
    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     */
    public DragController(Context context) {
        mContext = context;
        mHandler = new Handler();
        
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    public void setContext(Context context){
    	mContext = context;
    }

    /**
     * Starts a drag.
     * 
     * @param v The view that is being dragged
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     */
    public void startDrag(View v, DragSource source, Object dragInfo, int dragAction, boolean vibrate) {
		if (mContext instanceof MainActivity) {
			((MainActivity) mContext).getSlidingMenu().setSlidingEnabled(false);
		}
        mOriginator = v;
        Bitmap b = getViewBitmap(v);

        if (b == null) {
            // out of memory?
            return;
        }

        int[] loc = mCoordinatesTemp;
        v.getLocationOnScreen(loc);
        int screenX = loc[0];
        int screenY = loc[1];

        startDrag(b, screenX, screenY, 0, 0, b.getWidth(), b.getHeight(),
                source, dragInfo, dragAction, vibrate);

        b.recycle();

        if (dragAction == DRAG_ACTION_MOVE) {
            v.setVisibility(View.GONE);
            if (v instanceof DropTarget) {
                removeDropTarget((DropTarget)v);
            }
        }
    }

    /**
     * Starts a drag.
     * 
     * @param b The bitmap to display as the drag image.  It will be re-scaled to the
     *          enlarged size.
     * @param screenX The x position on screen of the left-top of the bitmap.
     * @param screenY The y position on screen of the left-top of the bitmap.
     * @param textureLeft The left edge of the region inside b to use.
     * @param textureTop The top edge of the region inside b to use.
     * @param textureWidth The width of the region inside b to use.
     * @param textureHeight The height of the region inside b to use.
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     */
    public void startDrag(Bitmap b, int screenX, int screenY,
            int textureLeft, int textureTop, int textureWidth, int textureHeight,
            DragSource source, Object dragInfo, int dragAction, boolean vibrate) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        if (LauncherApplication.DBG) LogUtil.d(TAG, "startDrag()");

        // Hide soft keyboard, if visible
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager)
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);

        if (mListener != null) {
            mListener.onDragStart(source, dragInfo, dragAction);
        }

        int registrationX = ((int)mMotionDownX) - screenX;
        int registrationY = ((int)mMotionDownY) - screenY;

        mTouchOffsetX = mMotionDownX - screenX;
        mTouchOffsetY = mMotionDownY - screenY;

        mDragExit = false;
        mDragging = true;
        mDragSource = source;
        mDragInfo = dragInfo;

        if (vibrate) mVibrator.vibrate(VIBRATE_DURATION);

        DragView dragView = mDragView = new DragView(mContext, b, registrationX, registrationY,
                textureLeft, textureTop, textureWidth, textureHeight);

        dragView.setPaint(new Paint());
        dragView.setAlpha(0.5f);

        dragView.show(mWindowToken, (int)mMotionDownX, (int)mMotionDownY);
    }

    /* added for move drag info from closed folder to workspace. */
    public void updateDragSource(DragSource source) {
        if (LauncherApplication.DBG) LogUtil.d(TAG, "updateDragSource:"+source);
        mDragSource = source;
    }

    /**
     * Draw the view into a bitmap.
     */
    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            if (LauncherApplication.DBG) LogUtil.e(TAG, "failed getViewBitmap(" + v + ")");
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    /**
     * Call this from a drag source view like this:
     *
     * <pre>
     *  @Override
     *  public boolean dispatchKeyEvent(KeyEvent event) {
     *      return mDragController.dispatchKeyEvent(this, event)
     *              || super.dispatchKeyEvent(event);
     * </pre>
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging;
    }

    /**
     * Stop dragging without dropping.
     */
    public void cancelDrag() {
        if (mDragging) {
            drop(mDragView.getX(), mDragView.getY());
        }

        endDrag();
    }

    public void endDrag() {
    	if (mContext instanceof MainActivity) {
			((MainActivity) mContext).getSlidingMenu().setSlidingEnabled(false);
		}
        if (mDragging) {
            mDragging = false;
            /*
            if (mOriginator != null) {
                mOriginator.setVisibility(View.VISIBLE);
                if (mOriginator instanceof DropTarget) {
                    addDropTarget((DropTarget)mOriginator);
                }
            }
            */
            if (mListener != null) {
                mListener.onDragEnd();
            }
            if (mDragView != null) {
                mDragView.remove();
                mDragView = null;
            }
        }
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            recordScreenSize();
        }

        final int screenX = clamp((int)ev.getRawX(), 0, mDisplayMetrics.widthPixels);
        final int screenY = clamp((int)ev.getRawY(), 0, mDisplayMetrics.heightPixels);

        if (DBG_UI) LogUtil.d(TAG, "onInterceptTouchEvent screenX:"+screenX+" screenY:"+screenY+" mDragging:"+mDragging);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                if (DBG_UI) LogUtil.d(TAG, "onInterceptTouchEvent ACTION_MOVE");
                break;

            case MotionEvent.ACTION_DOWN:
            	 
                // Remember location of down touch
                mMotionDownX = screenX;
                mMotionDownY = screenY;
                mMotionDownTime = System.currentTimeMillis();
                final int[] coordinates = mCoordinatesTemp;
                /* To update last drop target, to avoid ignore icon fast move out from folder area,
                 * the foler will not be closed so. */
                mLastDropTarget = findDropTarget(screenX, screenY, coordinates);
                if (DBG_UI) LogUtil.d(TAG, "onInterceptTouchEvent ACTION_DOWN");
                break;

            case MotionEvent.ACTION_CANCEL:
                //if (DBG_UI) LogUtil.d(TAG, "onInterceptTouchEvent ACTION_CANCEL");
            case MotionEvent.ACTION_UP:
                if (DBG_UI) LogUtil.d(TAG, "onInterceptTouchEvent ACTION_UP");
                if (mDragging) {
                    drop(screenX, screenY);
                }
                endDrag();
                if(MyApplication.getInstance().mMineViewManager.mInEdtiMode && (System.currentTimeMillis() - mMotionDownTime < 400) && (Math.abs(mMotionDownX - screenX) < 8) && (Math.abs(mMotionDownY - screenY) < 8)){
                	MyApplication.getInstance().mMineViewManager.getWorkspace().exitEditMode(true);
                }
                break;
        }

        return mDragging;
    }

    /**
     * Sets the view that should handle move events.
     */
    public void setMoveTarget(View view) {
        mMoveTarget = view;
    }    

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mMoveTarget != null && mMoveTarget.dispatchUnhandledMove(focused, direction);
    }

    /* if drag too fast from folder to ouside, onTouchEvent MOVE will not be called.
     * So should double check if should call onDragExit before calling onDrop. */
    private boolean mDragExit = false;

    /**
     * Call this from a drag source view.
     */
    public boolean onTouchEvent(MotionEvent ev) {
        View scrollView = mScrollView;

        if (DBG_UI) LogUtil.d(TAG, "onTouchEvent mDragging:"+mDragging+",action "+ev.getAction());
        
        if (!mDragging) {
            return false;
        }

        final int action = ev.getAction();
        final int screenX = clamp((int)ev.getRawX(), 0, mDisplayMetrics.widthPixels);
        final int screenY = clamp((int)ev.getRawY(), 0, mDisplayMetrics.heightPixels);

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            // Remember where the motion event started
            mMotionDownX = screenX;
            mMotionDownY = screenY;

            if ((screenX < SCROLL_ZONE) || (screenX > scrollView.getWidth() - SCROLL_ZONE)) {
                mScrollState = SCROLL_WAITING_IN_ZONE;
                mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
            } else if ((screenY < SCROLL_ZONE) || (screenY > scrollView.getHeight() - SCROLL_ZONE)) {
                mScrollState = SCROLL_WAITING_IN_ZONE;
                mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
            } else {
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }

            if (DBG_UI) LogUtil.d(TAG, "ACTION_DOWN x:"+screenX+" y:"+screenY);
            break;
        case MotionEvent.ACTION_MOVE:
            // Update the drag view.  Don't use the clamped pos here so the dragging looks
            // like it goes off screen a little, intead of bumping up against the edge.
            mDragView.move((int)ev.getRawX(), (int)ev.getRawY());

            final int[] coordinates = mCoordinatesTemp;

            // Drop on someone?
            DropTarget dropTarget = findDropTarget(screenX, screenY, coordinates);
            if (DBG_UI) LogUtil.d(TAG, "ACTION_MOVE x:"+screenX+" y:"+screenY+" dropTarget:"+
                    dropTarget+" lastDropTarget:"+mLastDropTarget);
            if (dropTarget != null) {
                if (mLastDropTarget == dropTarget) {
                    dropTarget.onDragOver(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                } else {
                    if (mLastDropTarget != null) {
                        if (LauncherApplication.DBG) LogUtil.d(TAG, "call onDragExit()...");
                        mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                            (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo, dropTarget);
                        mDragExit = true;
                    }

                    if (LauncherApplication.DBG) LogUtil.d(TAG, "call onDragEnter()...");
                    dropTarget.onDragEnter(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                }
            }
	        else
	        {
	            if (mLastDropTarget != null)
		        {
           	         mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                         (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo, dropTarget);
                     mDragExit = true;
           	    }
	        }

            mLastDropTarget = dropTarget;

            // Scroll, maybe, but not if we're in the delete region.
            boolean inDeleteRegion = false;
            if (mDeleteRegion != null) {
                inDeleteRegion = mDeleteRegion.contains(screenX, screenY);
            }
            if (!inDeleteRegion && screenX < SCROLL_ZONE) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_LEFT);
                    mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            } else if (!inDeleteRegion && screenX > scrollView.getWidth() - SCROLL_ZONE) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_RIGHT);
                    mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            } else if (!inDeleteRegion && screenY < SCROLL_ZONE + mTop) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_TOP);
                    mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            } else if (!inDeleteRegion && screenY > scrollView.getHeight() - SCROLL_ZONE) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_BOTTOM);
                    mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            } else {
                if (mScrollState == SCROLL_WAITING_IN_ZONE) {
                    mScrollState = SCROLL_OUTSIDE_ZONE;
                    mScrollRunnable.setDirection(SCROLL_RIGHT);
                    mHandler.removeCallbacks(mScrollRunnable);
                }
            }

            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            mHandler.removeCallbacks(mScrollRunnable);
            if (mDragging) {
                drop(screenX, screenY);
            }
            endDrag();
            break;
        case MotionEvent.ACTION_CANCEL:
            cancelDrag();
        }

        return true;
    }

    private boolean drop(float x, float y) {
        final int[] coordinates = mCoordinatesTemp;
        if (LauncherApplication.DBG) LogUtil.d(TAG, "drop() x: " + x + ", y: " + y + ", TouchOffsetX: " + mTouchOffsetX + ", TouchOffsetY: " + mTouchOffsetY);
        DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);

        if (dropTarget != mLastDropTarget && !mDragExit) {
            if (mLastDropTarget != null) mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                    (int) x, (int) y, mDragView, mDragInfo, dropTarget);
            mDragExit = true;
        }

        /* should be visible before drop, or the animation will be ignored by workspace. */
        if (mOriginator != null) {
            mOriginator.setVisibility(View.VISIBLE);
            if (mOriginator instanceof DropTarget) {
                addDropTarget((DropTarget)mOriginator);
            }
            mOriginator = null;
        }

        if (dropTarget != null) {
            if (dropTarget.acceptDrop(mDragSource, coordinates[0], coordinates[1],
                    (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo)) {
                mDragSource.onDropCompleted((View) dropTarget, true, mDragInfo, mDragView);
                dropTarget.onDrop(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragView, mDragInfo);
                return true;
            } else {
                mDragSource.onDropCompleted((View) dropTarget, false, mDragInfo, mDragView);
                return true;
            }
        } else {
            mDragSource.onDropCompleted((View) dropTarget, false, mDragInfo, mDragView);
        }

        return false;
    }

    private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        final Rect r = mRectTemp;

        final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i=count-1; i>=0; i--) {
            final DropTarget target = dropTargets.get(i);
            target.getHitRect(r);
            target.getLocationOnScreen(dropCoordinates);
            r.offset(dropCoordinates[0] - target.getLeft(), dropCoordinates[1] - target.getTop());

            if (r.contains(x, y)) {
                if (target.isFull(mDragInfo)) {
                    if (LauncherApplication.DBG) LogUtil.d(TAG, "findDropTarget target is full: "+target);
                    return null;
                }
                dropCoordinates[0] = x - dropCoordinates[0];
                dropCoordinates[1] = y - dropCoordinates[1];
                if (LauncherApplication.DBG) LogUtil.d(TAG, "findDropTarget x: "+dropCoordinates[0]+", y: "+dropCoordinates[1]+" i: "+i+" count:"+count+" target:"+target);
                return target;
            }
//            else
//            {
//                if ((target instanceof FolderIcon)&&(r.left!=0 || r.right!=0) &&(((FolderIcon)target).getInfo().contents.size()<=0))
//                {
//                    //workaround to avoid new foldericon is added,but we have moved out from the folder area. then the folder icon can not resume to normal appicon
//                    mLastDropTarget=target;
//                }
//            }
        }
        if (LauncherApplication.DBG) LogUtil.d(TAG, "findDropTarget no target");
        return null;
    }

    /**
     * Get the screen size so we can clamp events to the screen size so even if
     * you drag off the edge of the screen, we find something.
     */
    private void recordScreenSize() {
        ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(mDisplayMetrics);
    }

    /**
     * Clamp val to be &gt;= min and &lt; max.
     */
    private static int clamp(int val, int min, int max) {
        if (val < min) {
            return min;
        } else if (val >= max) {
            return max - 1;
        } else {
            return val;
        }
    }

    public void setDragScoller(DragScroller scroller) {
        mDragScroller = scroller;
    }

    public void setWindowToken(IBinder token) {
        mWindowToken = token;
    }

    /**
     * Sets the drag listner which will be notified when a drag starts or ends.
     */
    public void setDragListener(DragListener l) {
        mListener = l;
    }

    /**
     * Remove a previously installed drag listener.
     */
    public void removeDragListener(DragListener l) {
        mListener = null;
    }

    /**
     * Add a DropTarget to the list of potential places to receive drop events.
     */
    public void addDropTarget(DropTarget target) {
       if (mDropTargets.indexOf(target) < 0) mDropTargets.add(target);

        if (LauncherApplication.DBG) LogUtil.d(TAG, "target: "+target+" addDropTarget size:"+mDropTargets.size());
    }

    /**
     * Don't send drop events to <em>target</em> any more.
     */
    public void removeDropTarget(DropTarget target) {
        mDropTargets.remove(target);
        if (target == mLastDropTarget) {
            final int[] coordinates = mCoordinatesTemp;
            target.getLocationOnScreen(coordinates);
            mLastDropTarget = findDropTarget(coordinates[0], coordinates[1], coordinates);
        }
        if (LauncherApplication.DBG) LogUtil.d(TAG, "target:"+target+" removeDropTarget size:"+mDropTargets.size()+" last drop:"+mLastDropTarget);
    }

    /**
     * Set which view scrolls for touch events near the edge of the screen.
     */
    public void setScrollView(View v) {
        mScrollView = v;
    }

    /**
     * Specifies the delete region.  We won't scroll on touch events over the delete region.
     *
     * @param region The rectangle in screen coordinates of the delete region.
     */
    void setDeleteRegion(RectF region) {
        mDeleteRegion = region;
    }

    private class ScrollRunnable implements Runnable {
        private int mDirection;

        ScrollRunnable() {
        }

        public void run() {
            if (mDragScroller != null) {
                if (mDirection == SCROLL_LEFT) {
                    mDragScroller.scrollLeft();
                } else if (mDirection == SCROLL_RIGHT){
                    mDragScroller.scrollRight();
                } else if (mDirection == SCROLL_TOP) {
                    mDragScroller.scrollTop();
                } else if (mDirection == SCROLL_BOTTOM){
                    mDragScroller.scrollBottom();
                }
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }
        }

        void setDirection(int direction) {
            mDirection = direction;
        }
    }
}
