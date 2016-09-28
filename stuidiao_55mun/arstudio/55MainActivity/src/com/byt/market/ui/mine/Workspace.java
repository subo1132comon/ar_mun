/*
 * Copyright
 */

package com.byt.market.ui.mine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Scroller;

import com.byt.market.R;
import com.byt.market.MyApplication;
import com.byt.market.data.AppItem;
import com.byt.market.data.AppItemState;
import com.byt.market.download.DownloadContent;
import com.byt.market.download.DownloadTaskManager;
import com.byt.market.util.LogUtil;

public class Workspace extends ViewGroup implements View.OnLongClickListener
	,DropTarget,DragSource, DragScroller{
	private static final boolean SCROLL_VERTICAL = true;
    private static final int MAX_ITEM_COUNT = 64;    // allow for some discards
    private static final String TAG = "RunningProcessesContainer";
    private static final boolean DBG = false;
    private static final boolean DBG_LAYOUT = false;
    private static final boolean DBG_PROC = false;
    private static final int INVALID_SCREEN = -1;

    /**
     * The velocity at which a fling gesture will cause us to snap to the next screen
     */
    private static final int SNAP_VELOCITY = 600;

    private int mDefaultScreen = 1;

    private boolean mFirstLayout = true;

    protected int mCurrentScreen = 0;
    private int mNextScreen = INVALID_SCREEN;
    protected Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    public static final int MAX_SCREEN_COUNT = 11;
    public static final int DEFAULT_SCREEN_NUM = 0;
    public static final int DEFAULT_CURRENT_SCREEN = 0;

    private float mLastMotionX;
    private float mLastMotionY;
    
    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_REST;

    private MineViewManager mLauncher;
    private IconCache mIconCache;
    /**
     * Cache of vacant cells, used during drag events and invalidated as needed.
     */
    private CellLayout.CellInfo mVacantCache = null;
    
    private int[] mTempCell = new int[2];
    private int[] mTempEstimate = new int[2];

    private boolean mAllowLongPress = true;

    private int mTouchSlop;
    private int mMaximumVelocity;
    
    private static final int INVALID_POINTER = -1;

    private int mActivePointerId = INVALID_POINTER;
    
    private static final float NANOTIME_DIV = 1000000000.0f;
    private static final float SMOOTHING_SPEED = 0.75f;
    private static final float SMOOTHING_CONSTANT = (float) (0.016 / Math.log(SMOOTHING_SPEED));
    private float mSmoothingTime;
    private float mTouchX;
    private float mTouchY;
    
    protected WorkspaceOvershootInterpolator mScrollInterpolator;
        
    private DragController mDragController;
    /**
	 * CellInfo for the cell that is currently being dragged
	 */
	private CellLayout.CellInfo mDragInfo;
	
	/**
	 * Target drop area calculated during last acceptDrop call.
	 */
	private int[] mTargetCell = null;
	private boolean mIsForFolder;
	
	
    protected static class WorkspaceOvershootInterpolator implements Interpolator {
        private static final float DEFAULT_TENSION = 1.3f;
        private float mTension;

        public WorkspaceOvershootInterpolator() {
            mTension = DEFAULT_TENSION;
        }
        
        public void setDistance(int distance) {
            mTension = distance > 0 ? DEFAULT_TENSION / distance : DEFAULT_TENSION;
        }

        public void disableSettle() {
            mTension = 0.f;
        }

        public float getInterpolation(float t) {
            // _o(t) = t * t * ((tension + 1) * t + tension)
            // o(t) = _o(t - 1) + 1
            t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }

    public Workspace(Context context) {
        this(context, null);
    }

    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
 
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Workspace, defStyle, 0);
        mDefaultScreen = 1;
        a.recycle();

        setHapticFeedbackEnabled(false);

        init(context);
    }

    private void init(Context context) {
        mScrollInterpolator = new WorkspaceOvershootInterpolator();
        mScroller = new Scroller(context, mScrollInterpolator);
        mCurrentScreen = 0;
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }
    
	public void initScreens() {
		addScreen(1);
	}
    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                Workspace.this.autoAlignment();
                View cell = (View)msg.obj;
                final CellLayout cellLayout = (CellLayout) getChildAt(mCurrentScreen);
                cellLayout.removeView(cell);
                updateScreensAfterItemRemoved();
                if(mLauncher != null && mLauncher.mWorkSpaceChildRemovedListener != null){
                	mLauncher.mWorkSpaceChildRemovedListener.onChildRemoved(msg.arg1);
                }
        }

};
    /**
     * Returns the index of the currently displayed screen.
     *
     * @return The index of the currently displayed screen.
     */
    int getCurrentScreen() {
        return mCurrentScreen;
    }

    boolean isCurrentScreen(CellLayout layout) {
        return indexOfChild(layout) == mCurrentScreen;
    }

    /**
     * Sets the current screen.
     *
     * @param currentScreen
     */
    void setCurrentScreen(int currentScreen) {
        clearVacantCache();

        if(DBG) LogUtil.d(TAG, "setCurrentScreen:"+currentScreen);
        mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
        snapToScreen(mCurrentScreen);
    }

    /**
     * Adds the specified child in the current screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    void addInCurrentScreen(View child, int x, int y, int spanX, int spanY) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, false);
    }

    /**
     * Adds the specified child in the current screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     */
    void addInCurrentScreen(View child, int x, int y, int spanX, int spanY, boolean insert) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, insert);
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    void addInScreen(View child, int screen, int x, int y, int spanX, int spanY) {
        addInScreen(child, screen, x, y, spanX, spanY, false);
    }
    
    private Handler mRemoveItemHandler = new Handler();
    public void removeItem(final AppItem item,final View view){
        final Runnable run = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				animationGone(item,view);
			}
		};
		mRemoveItemHandler.post(run);
    }

    private void animationGone(final AppItem item,final View cell){
        final long SCALE_DURATION = 300l;
        final Interpolator interpolator = new AccelerateInterpolator(1.5f);
        final ScaleAnimation animation = new ScaleAnimation(1f, 0f, 1f, 0f, cell.getWidth() / 2, cell.getHeight() /2);
        animation.setDuration(SCALE_DURATION);
        AnimationSet animationset = (AnimationSet)cell.getAnimation();
        if (animationset == null) {
            animationset = new AnimationSet(false);
        }
        animationset.addAnimation(animation);
                animation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                                // TODO Auto-generated method stub

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                                // TODO Auto-generated method stub
                                Message msg = mHandler.obtainMessage(0);
                                msg.obj  = cell;
                                msg.arg1 = item.sid;
                                mHandler.sendMessage(msg);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                                // TODO Auto-generated method stub

                        }

                });
        cell.startAnimation(animationset);
        
    }

    private void updateScreensAfterItemRemoved(){
        final int screenSize = this.getChildCount();
        if(screenSize == mCurrentScreen + 1){
                //currrnt screen is last screen ,do noting
            /*final CellLayout cellLayout = (CellLayout) getChildAt(mCurrentScreen);
                cellLayout.autoAlignment();
                return;*/
        }
        CellLayout tempCellLayout = null;
        CellLayout nextCellLayout = null;
        for(int i = mCurrentScreen ; i < screenSize ; i++){
                tempCellLayout = (CellLayout) getChildAt(i);
                if(i == screenSize - 1){
                        //last screen
                        if(tempCellLayout.getChildCount() == 0){
                                //last screen is empty ,remove it
                                this.removeView(tempCellLayout);
                        }
                                break; 
                }
                                nextCellLayout = (CellLayout) getChildAt(i + 1);
                        if(nextCellLayout.getChildCount() > 0){
                    View tempView = nextCellLayout.getChildAt(0);
                                        nextCellLayout.removeView(tempView);
                                //this.addInScreen(tempView, i, 3, 0, 1, 1);
                                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) tempView.getLayoutParams();
                        if (lp == null) {
                            lp = new CellLayout.LayoutParams(COLUME_COUNT - 1, ROW_COUNT - 1, 1, 1);
                        } else {
                            lp.cellX = COLUME_COUNT - 1;
                            lp.cellY = ROW_COUNT - 1;
                            lp.cellHSpan = 1;
                            lp.cellVSpan = 1;
                        }
                        final ItemInfo cellInfo = (ItemInfo) tempView.getTag();
                        cellInfo.screen = i	;
                        tempCellLayout.addView(tempView, -1, lp);

                        tempView.setHapticFeedbackEnabled(false);
                        tempView.setOnLongClickListener(this);
                        tempCellLayout.autoAlignmentAdded(tempView);
                        }
                        tempCellLayout.autoAlignment();
                        if(nextCellLayout != null && nextCellLayout.getChildCount() > 0){
                                nextCellLayout.autoAlignment();
                        }
        }
        
    }

    CellLayout createCellLayout(ViewGroup parent) {
        return (CellLayout) mLauncher.mInflater.inflate(R.layout.mine_workspace_screen, parent, false);
    }
    
    /* return the last added layout */
    CellLayout addScreen(int num) {
        CellLayout layout = null;
        for (int i = 0; i < num; i++) {
            layout = createCellLayout(this);
            layout.setClickable(true); 
            layout.setFocusable(true);
            layout.enableFolderCreation(!mIsForFolder);
            layout.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
            layout.setAnimationCacheEnabled(true);
            addView(layout);
           
        }
        return layout;
    }

        
    /**
	 * Adds the specified child in the specified screen. The position and
	 * dimension of the child are defined by x, y, spanX and spanY.
	 * 
	 * @param child
	 *            The child to add in one of the workspace's screens.
	 * @param screen
	 *            The screen in which to add the child.
	 * @param x
	 *            The X position of the child in the screen's grid.
	 * @param y
	 *            The Y position of the child in the screen's grid.
	 * @param spanX
	 *            The number of cells spanned horizontally by the child.
	 * @param spanY
	 *            The number of cells spanned vertically by the child.
	 * @param insert
	 *            When true, the child is inserted at the beginning of the
	 *            children list.
	 */
	void addInScreen(View child, int screen, int x, int y, int spanX,
			int spanY, boolean insert) {
		if (screen < 0 || screen >= MAX_SCREEN_COUNT) {
			LogUtil.e(TAG, "The screen must be >= 0 and < " + MAX_SCREEN_COUNT
					+ " (was " + screen + "); skipping child");
			return;
		}
		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "addInScreen @ screen:" + screen);

		final int count = getChildCount();
		if (screen >= count) {
			if (LauncherApplication.DBG)
				LogUtil.d(TAG, "addInScreen @ screen:" + screen);
			addScreen(screen - count + 1);
		}

		clearVacantCache();

		if (!mIsForFolder && screen < DEFAULT_CURRENT_SCREEN) {
			if (LauncherApplication.DBG)
				LogUtil.e(TAG, "addInScreen() met BUG: to add icon to screen: "
						+ screen);
			return;
		}

		final CellLayout group = (CellLayout) getChildAt(screen);
		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
				.getLayoutParams();
		
		if (lp == null) {
			lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
		} else {
			lp.cellX = x;
			lp.cellY = y;
			lp.cellHSpan = spanX;
			lp.cellVSpan = spanY;
		}
		group.addView(child, insert ? 0 : -1, lp);

		child.setHapticFeedbackEnabled(false);
		child.setOnLongClickListener(this);

		/*if (((AppIcon) child).isFolderIcon()
				&& (((AppIcon) child).getIconView()) instanceof DropTarget) {
			mDragController.addDropTarget((DropTarget) ((AppIcon) child)
					.getIconView());
		}*/
		
		/*if (mInEditMode) {
			group.enterChildEditMode(child);
		}*/
	}

    CellLayout.CellInfo findAllVacantCells(boolean[] occupied) {
        CellLayout group = (CellLayout) getChildAt(mCurrentScreen);
        if (group != null) {
            return group.findAllVacantCells(occupied, null);
        }
        return null;
    }

    private void clearVacantCache() {
        if (mVacantCache != null) {
            mVacantCache.clearVacantCells();
            mVacantCache = null;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            //Launcher.setScreen(mCurrentScreen);
            mNextScreen = INVALID_SCREEN;
            clearChildrenCache();
        } else if (mTouchState == TOUCH_STATE_SCROLLING) {
            final float now = System.nanoTime() / NANOTIME_DIV;
            final float e = (float) Math.exp((now - mSmoothingTime) / SMOOTHING_CONSTANT);
            final float dx = mTouchX - this.getScrollX();
            mSmoothingTime = now;

            // Keep generating points as long as we're more than 1px away from the target
            if (dx > 1.f || dx < -1.f) {
                postInvalidate();
            }
        }
    }
    @Override
    protected void dispatchDraw(Canvas canvas) {
        boolean restore = false;
        int restoreCount = 0;
		
        // ViewGroup.dispatchDraw() supports many features we don't need:
        // clip to padding, layout animation, animation listener, disappearing
        // children, etc. The following implementation attempts to fast-track
        // the drawing dispatch by drawing only what we know needs to be drawn.
        if(this.getChildCount() == 0 ){
        	return;
        }
        boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mNextScreen == INVALID_SCREEN;
        // If we are not scrolling or flinging, draw only the current screen
        if (fastDraw) {
        	if(getChildAt(mCurrentScreen) != null){
        		drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
        	}
        } else {
            drawScreens(canvas);
        }

        if (restore) {
            canvas.restoreToCount(restoreCount);
        }
    }

    protected void onAttachedToWindow() {
        if(DBG) LogUtil.d(TAG, "onAttachedToWindow");
        super.onAttachedToWindow();
        computeScroll();
        if (mDragController != null)
			mDragController.setWindowToken(getWindowToken());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY && !mIsForFolder) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY && !mIsForFolder) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        // The children are given the same width and height as the workspace
        // minus dock height
        int height = MeasureSpec.getSize(heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode);

        final int count = getChildCount();

        if (DBG_LAYOUT) LogUtil.d(TAG, "onMeasure() height:"+height+" width:"+width+" child count:"+count);
        int maxWidth = 0;
		int maxHeight = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (mIsForFolder && child instanceof CellLayout) {
				((CellLayout) child).enableWrapContent(mIsForFolder);
			}

            child.measure(widthMeasureSpec, heightMeasureSpec);
            maxWidth = maxWidth > child.getMeasuredWidth() ? maxWidth : child
					.getMeasuredWidth();
			maxHeight = maxHeight > child.getMeasuredHeight() ? maxHeight
					: child.getMeasuredHeight();
        }
        if (mIsForFolder) {
			if (DBG_LAYOUT)
				LogUtil.d(TAG, "onMeasure() got maxWidth:" + maxWidth
						+ " maxHeight:" + maxHeight);
			setMeasuredDimension(maxWidth, maxHeight);
		}
        if (mFirstLayout) {
            setHorizontalScrollBarEnabled(false);
            scrollTo(mCurrentScreen * width, 0);
            setHorizontalScrollBarEnabled(true);
            mFirstLayout = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    	if(SCROLL_VERTICAL){
    		int childTop = 0;

            final int count = getChildCount();
            if (DBG_LAYOUT) LogUtil.d(TAG, "onLayout() left:"+left+" top:"+top+" right:"+right+" bottom:"+bottom+" child count:"+count);

            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != View.GONE) {
                    final int childWidth = child.getMeasuredWidth();
                    final int childHeight = child.getMeasuredHeight();
                    child.layout(0, childTop, childWidth, childTop + childHeight);
                    childTop += childHeight;
                }
            }
    	} else {
    		int childLeft = 0;

            final int count = getChildCount();
            if (DBG_LAYOUT) LogUtil.d(TAG, "onLayout() left:"+left+" top:"+top+" right:"+right+" bottom:"+bottom+" child count:"+count);

            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != View.GONE) {
                    final int childWidth = child.getMeasuredWidth();
                    final int childHeight = child.getMeasuredHeight();
                    child.layout(childLeft, 0, childLeft + childWidth, childHeight);
                    childLeft += childWidth;
                }
            }
    	}
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        int screen = indexOfChild(child);
        if (screen != mCurrentScreen || !mScroller.isFinished()) {
            snapToScreen(screen);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int focusableScreen;
        if (mNextScreen != INVALID_SCREEN) {
            focusableScreen = mNextScreen;
        } else {
            focusableScreen = mCurrentScreen;
        }
        //LogUtil.d(TAG, "onRequestFocusInDescendants() focusableScreen:"+focusableScreen);
     //   if(null != getChildAt(focusableScreen))
            //getChildAt(focusableScreen).requestFocus(direction, previouslyFocusedRect);

        return false;
    }

    @Override
        public boolean dispatchUnhandledMove(View focused, int direction) {
            if(DBG) LogUtil.d(TAG, "dispatchUnhandledMove direction " + direction);
            if (direction == View.FOCUS_LEFT) {
            if (getCurrentScreen() > 0) {
                snapToScreen(getCurrentScreen() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentScreen() < getChildCount() - 1) {
                snapToScreen(getCurrentScreen() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
    	if(getChildAt(mCurrentScreen) == null){
    		return ;
    	}
        getChildAt(mCurrentScreen).addFocusables(views, direction);
        if (direction == View.FOCUS_LEFT) {
            if (mCurrentScreen > 0) {
                getChildAt(mCurrentScreen - 1).addFocusables(views, direction);
            }
        } else if (direction == View.FOCUS_RIGHT){
            if (mCurrentScreen < getChildCount() - 1) {
                getChildAt(mCurrentScreen + 1).addFocusables(views, direction);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */
    	
    	if (mIsForFolder) {
			return false; // We don't want the events. Let them fall through to
							// the all apps view.
		}
    	
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(ev);
        
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
            	if (mIsForFolder)
    				return false;
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionX is set to the y value
                 * of the down event.
                 */

                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) break;

                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                final int touchSlop = mTouchSlop + 2;
                boolean xMoved = xDiff > touchSlop;
                boolean yMoved = yDiff > touchSlop;
                
                if (xMoved || yMoved) {
                	if(SCROLL_VERTICAL){
                		if (yMoved) {
                            // Scroll if the user moved far enough along the Y axis
                            mTouchState = TOUCH_STATE_SCROLLING;
                            mLastMotionY = y;
                            mTouchX = this.getScrollX();
                            mTouchY = this.getScrollY();
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
                        }
                	} else {
                		if (xMoved) {
                            // Scroll if the user moved far enough along the X axis
                            mTouchState = TOUCH_STATE_SCROLLING;
                            mLastMotionX = x;
                            mTouchX = this.getScrollX();
                            mTouchY = this.getScrollY();
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
                        }
                	}
                    
                    // Either way, cancel any pending longpress
                    if (mAllowLongPress) {
                        mAllowLongPress = false;
                        // Try canceling the long press. It could also have been scheduled
                        // by a distant descendant, so use the mAllowLongPress flag to block
                        // everything
                        final View currentScreen = getChildAt(mCurrentScreen);
                        currentScreen.cancelLongPress();
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                // Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;
                mActivePointerId = ev.getPointerId(0);
                //if (DBG) LogUtil.d(TAG, "onInterceptTouchEvent() ACTION_DOWN active pointer id:"+mActivePointerId);
                mAllowLongPress = true;

                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't.  mScroller.isFinished should be false when
                 * being flinged.
                 */
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (mTouchState != TOUCH_STATE_SCROLLING) {
                    if (getChildAt(mCurrentScreen) instanceof CellLayout) {
                    final CellLayout currentScreen = (CellLayout)getChildAt(mCurrentScreen);
                    if (!currentScreen.lastDownOnOccupiedCell()) {
                        getLocationOnScreen(mTempCell);
                        // Send a tap to the wallpaper if the last down was on empty space
                        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                        //if (DBG) LogUtil.d(TAG, "onInterceptTouchEvent() ACTION_UP active pointer id:"+mActivePointerId+"pointerIndex:"+pointerIndex);
                        if (pointerIndex < 0) break;
                    }
                    }
                }
                
                // Release the drag
                clearChildrenCache();
                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                mAllowLongPress = false;
                
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break;
                
                /*
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
                */
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mTouchState != TOUCH_STATE_REST;
    }
    

    /**
     * If one of our descendant views decides that it could be focused now, only
     * pass that along if it's on the current screen.
     *
     * This happens when live folders requery, and if they're off screen, they
     * end up calling requestFocus, which pulls it on screen.
     */
    @Override
    public void focusableViewAvailable(View focused) {
        View current = getChildAt(mCurrentScreen);
        View v = focused;
        while (true) {
            if (v == current) {
                super.focusableViewAvailable(focused);
                return;
            }
            if (v == this) {
                return;
            }
            ViewParent parent = v.getParent();
            if (parent instanceof View) {
                v = (View)v.getParent();
            } else {
                return;
            }
        }
    }

    void enableChildrenCache(int fromScreen, int toScreen) {
        if (fromScreen > toScreen) {
            final int temp = fromScreen;
            fromScreen = toScreen;
            toScreen = temp;
        }
        
        final int count = getChildCount();

        fromScreen = Math.max(fromScreen, 0);
        toScreen = Math.min(toScreen, count - 1);

        for (int i = fromScreen; i <= toScreen; i++) {
            {
                final CellLayout layout = (CellLayout) getChildAt(i);
                layout.setChildrenDrawnWithCacheEnabled(true);
                layout.setChildrenDrawingCacheEnabled(true);
            }
        }
    }

    void clearChildrenCache() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            {
                final CellLayout layout = (CellLayout) getChildAt(i);
                layout.setChildrenDrawnWithCacheEnabled(false);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	if (mIsForFolder) {
			return false; // We don't want the events. Let them fall through to
							// the all apps view.
		}
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            /*
             * If being flinged and user touches, stop the fling. isFinished
             * will be false if being flinged.
             */
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            // Remember where the motion event started
            mLastMotionX = ev.getX();
            mLastMotionY = ev.getY();
            mActivePointerId = ev.getPointerId(0);
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
            }
            break;

        case MotionEvent.ACTION_MOVE:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                // Scroll to follow the motion event
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) break;

                final float x = ev.getX(pointerIndex);
                final float deltaX = mLastMotionX - x;
                mLastMotionX = x;

                final float y = ev.getY(pointerIndex);
                final float deltaY = mLastMotionY - y;
                mLastMotionY = y;
                if(SCROLL_VERTICAL){
                	if (deltaY < 0) {
                        if (mTouchY > 0) {
                        	mTouchY += Math.max(-mTouchY, deltaY);
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            invalidate();
                        }
                    } else if (deltaY > 0) {
                        final float availableToScroll = getChildAt(getChildCount() - 1).getBottom() -
                                mTouchY - getHeight();
                        if (availableToScroll > 0) {
                            mTouchY += Math.min(availableToScroll, deltaY);
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            invalidate();
                        }
                    } else {
                        awakenScrollBars();
                    }

                    if(deltaY != 0) {
                            scrollBy(0, (int)deltaY);
                    }
                } else {
                	if (deltaX < 0) {
                        if (mTouchX > 0) {
                            mTouchX += Math.max(-mTouchX, deltaX);
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            invalidate();
                        }
                    } else if (deltaX > 0) {
                        final float availableToScroll = getChildAt(getChildCount() - 1).getRight() -
                                mTouchX - getWidth();
                        if (availableToScroll > 0) {
                            mTouchX += Math.min(availableToScroll, deltaX);
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            invalidate();
                        }
                    } else {
                        awakenScrollBars();
                    }

                    if(deltaX != 0) {
                            scrollBy((int)deltaX, 0);
                    }
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocity = -1;
                if(SCROLL_VERTICAL){
                	velocity = (int) velocityTracker.getYVelocity(mActivePointerId);
                } else {
                	velocity = (int) velocityTracker.getXVelocity(mActivePointerId);
                }
                final int screenWidth = getWidth();
                int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
                float scrolledPos = (float) getScrollX() / screenWidth;
                if(SCROLL_VERTICAL){
                	final int screenHeight = getHeight();
                    whichScreen = (getScrollY() + (screenHeight / 2)) / screenHeight;
                	scrolledPos = (float) getScrollY() / screenHeight;
                }
                
                if (velocity > SNAP_VELOCITY && mCurrentScreen > 0) {
                    // Fling hard enough to move left.
                    // Don't fling across more than one screen at a time.
                    final int bound = scrolledPos < whichScreen ?
                            mCurrentScreen - 1 : mCurrentScreen;
                    snapToScreen(Math.min(whichScreen, bound), velocity, true);
                } else if (velocity < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
                    // Fling hard enough to move right
                    // Don't fling across more than one screen at a time.
                    final int bound = scrolledPos > whichScreen ?
                            mCurrentScreen + 1 : mCurrentScreen;
                    snapToScreen(Math.max(whichScreen, bound), velocity, true);
                } else {
                    snapToScreen(whichScreen, 0, true);
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            mTouchState = TOUCH_STATE_REST;
            mActivePointerId = INVALID_POINTER;
            break;
        case MotionEvent.ACTION_CANCEL:
            mTouchState = TOUCH_STATE_REST;
            mActivePointerId = INVALID_POINTER;
            break;
            /*
        case MotionEvent.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            break;
            */
        }

        return true;
    }
    
    void snapToScreen(int whichScreen) {
        snapToScreen(whichScreen, 0, false);
    }

    private void snapToScreen(int whichScreen, int velocity, boolean settle) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        
        clearVacantCache();
        enableChildrenCache(mCurrentScreen, whichScreen);

        mNextScreen = whichScreen;

        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichScreen != mCurrentScreen &&
                focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        scrollToScreen(whichScreen, velocity, settle);
    }

    void enterEditMode() {
        mLauncher.mInEdtiMode = true;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (!(getChildAt(i) instanceof CellLayout)) continue;
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.enterEditMode();
        }
    }

    void exitEditMode(boolean withAnimation) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (!(getChildAt(i) instanceof CellLayout)) continue;
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.exitEditMode(withAnimation);
        }
        mLauncher.mInEdtiMode = false;
    }

    public void autoAlignment() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (!(getChildAt(i) instanceof CellLayout)) continue;
            CellLayout cellLayout = (CellLayout) getChildAt(i);
            cellLayout.autoAlignment();
        }
    }

    public void setLauncher(MineViewManager launcher) {
        mLauncher = launcher;
         mIconCache = launcher.mIconCache;
        //initScreens();
    }

    public void scrollLeft() {
    	if(SCROLL_VERTICAL){
    		return;
    	}
        clearVacantCache();
        if (mScroller.isFinished()) {
            if (mCurrentScreen > 0) snapToScreen(mCurrentScreen - 1); //to avoid to drop icon to search screen
        } else {
            if (mNextScreen > 0) snapToScreen(mNextScreen - 1);
        }
    }

    public void scrollRight() {
    	if(SCROLL_VERTICAL){
    		return;
    	}
        clearVacantCache();
        if (mScroller.isFinished()) {
            if (mCurrentScreen < getChildCount() -1) snapToScreen(mCurrentScreen + 1);
        } else {
            if (mNextScreen < getChildCount() -1) snapToScreen(mNextScreen + 1);
        }
    }

    public void scrollTop() {
    	if(!SCROLL_VERTICAL){
    		return;
    	}
        clearVacantCache();
        if (mScroller.isFinished()) {
            if (mCurrentScreen > 0) snapToScreen(mCurrentScreen - 1); //to avoid to drop icon to search screen
        } else {
            if (mNextScreen > 0) snapToScreen(mNextScreen - 1);
        }
    }

    public void scrollBottom() {
    	if(!SCROLL_VERTICAL){
    		return;
    	}
        clearVacantCache();
        if (mScroller.isFinished()) {
            if (mCurrentScreen < getChildCount() -1) snapToScreen(mCurrentScreen + 1);
        } else {
            if (mNextScreen < getChildCount() -1) snapToScreen(mNextScreen + 1);
        }
    }
    
    public int getScreenForView(View v) {
        int result = -1;
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getChildAt(i)) {
                    return i;
                }
            }
        }
        return result;
    }

    public View getViewForTag(Object tag) {
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            if (!(getChildAt(screen) instanceof CellLayout)) continue;
            CellLayout currentScreen = ((CellLayout) getChildAt(screen));
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                if (child.getTag() == tag) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * @return True is long presses are still allowed for the current touch
     */
    public boolean allowLongPress() {
        return mAllowLongPress;
    }

    /**
     * Set true to allow long-press events to be triggered, usually checked by
     * {@link Launcher} to accept or block dpad-initiated long-presses.
     */
    public void setAllowLongPress(boolean allowLongPress) {
        mAllowLongPress = allowLongPress;
    }

    void moveToDefaultScreen(boolean animate) {
        if(mDefaultScreen == mCurrentScreen){
                return;
        }
        if (animate) {
            snapToScreen(mDefaultScreen);
        } else {
            setCurrentScreen(mDefaultScreen);
        }
        //LogUtil.e(TAG, "mDefaultScreen: " + mDefaultScreen);
        View temp = getChildAt(mDefaultScreen);
        if(null!=temp)
            temp.requestFocus();
    }

    /**
     * Returns how many screens there are.
     */
    int getScreenCount() {
        return getChildCount();
    }

    protected void drawScreens(Canvas canvas) {
        final long drawingTime = getDrawingTime();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            drawChild(canvas, getChildAt(i), drawingTime);
        }
    }

    private static final float BASELINE_FLING_VELOCITY = 2000.f;
    private static final float FLING_VELOCITY_INFLUENCE = 0.4f;

    public void scrollToScreen(int screen, int velocity, boolean settle) {
         final int screenDelta = Math.max(1, Math.abs(screen - mCurrentScreen));
        //int newX = screen * this.getWidth();
        int newX = screen * getWidth();
        int delta = newX - getScrollX();
        int newY = screen * getHeight();
        int duration = (screenDelta + 1) * 100;
        if(SCROLL_VERTICAL){
        	delta = newY - getScrollY();
        }
        //maybe screen's layout is not finished
        if(SCROLL_VERTICAL){
        	if(getHeight()<1){
                final WindowManager am = (WindowManager)
                getContext().getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics displayMetrics = new DisplayMetrics();  
                am.getDefaultDisplay().getMetrics(displayMetrics);  
                newY = displayMetrics.heightPixels * screen;  
                delta = newY - getScrollY();
        	}
        } else {
        	 if(getWidth()<1){
                 final WindowManager am = (WindowManager)
                 getContext().getSystemService(Context.WINDOW_SERVICE);
                 DisplayMetrics displayMetrics = new DisplayMetrics();  
                 am.getDefaultDisplay().getMetrics(displayMetrics);  
                 newX = displayMetrics.widthPixels * screen;  
                 delta = newX - getScrollX();
             }
        }

        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration += (duration / (velocity / BASELINE_FLING_VELOCITY))
                    * FLING_VELOCITY_INFLUENCE;
        } else {
            duration += 100;
        }

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        
        if (settle) {
            mScrollInterpolator.setDistance(screenDelta);
        } else {
            mScrollInterpolator.disableSettle();
        }

        awakenScrollBars(duration);
        //LogUtil.e(TAG, "getScrollX() is: " + getScrollX() + "getScrollY() is :" + getScrollY() + "dx:" + delta + "dy:" + getScrollY() + "duration: "  + duration);
        if(SCROLL_VERTICAL){
        	mScroller.startScroll(getScrollX(), getScrollY(), getScrollX(), delta, duration);
        } else {
        	mScroller.startScroll(getScrollX(), getScrollY(), delta, getScrollY(), duration);
        }
        invalidate();
    }


    private void clearViews() {
        int count = this.getChildCount();
        removeViews(0, count);            //remain first screen for  apple running task style function

//        if(count > 1)
//            removeViews(1, count - 1);            //remain first screen for  apple running task style function
    }
    
    public static int COLUME_COUNT = 4;
    public static int ROW_COUNT = 4;
    public void initViewFromData(List<AppItem> items) {
    	final int height = this.getMeasuredHeight() > 0 ? this.getMeasuredHeight() : MyApplication.mScreenHeight;
    	final int width = this.getMeasuredWidth() > 0 ? this.getMeasuredWidth() : MyApplication.mScreenWidth;
    	if(height > width){
    		final int cessWidth = width / COLUME_COUNT;
        	ROW_COUNT = (height - 50 )/ cessWidth;
    	} else {
    		/*ROW_COUNT = 3;
    		final int cessHeight = height / ROW_COUNT;
    		
    		COLUME_COUNT = (width - 50 )/ cessHeight;*/
    	}
    	
        clearViews();
        initScreens();
        int index = 0;
        int lastScreen = 0;
        int lastPosition = -1;
        int numTasks = items.size();
        Collections.sort(items, comparator);
        boolean doSave = false;
        for (int i = 0; i < numTasks && (index < MAX_ITEM_COUNT); ++i) {
        	final AppItem item = items.get(i);
            //android.util.Log.d("cx"," name is "+item.name + " screen "+item.screen + " position  "+item.position);
            Intent intent = this.getContext().getPackageManager().getLaunchIntentForPackage(item.pname);

            if(item.screen < DownloadContent.POSITION_NULL && item.position < DownloadContent.POSITION_NULL){
            	View view = mLauncher.createTaskIcon(getContext(), (ViewGroup)getChildAt(item.screen), intent,item);
            	if (view == null) continue;
            	//android.util.Log.d("cx","position is "+item.position);
            	addInScreen(view, item.screen, item.position%COLUME_COUNT, getRealY(item.position/COLUME_COUNT) , 1, 1);
            } else {
            	int screen = 0;
            	int position = 0;
            	//android.util.Log.d("cx","lastPosition is "+lastPosition);
            	//has no position
            	if(lastPosition < COLUME_COUNT * ROW_COUNT - 1){
            		//has postion is lastScree
            		screen = lastScreen;
            		position = lastPosition + 1;
            	} else {
            		screen = lastScreen + 1;
            		position = 0;
            	}
            	
            	item.screen = screen;
            	item.position = position;
            	//android.util.Log.d("cx","--- position is "+position);
            	View view = mLauncher.createTaskIcon(getContext(), (ViewGroup)getChildAt(screen), intent,item);
            	if (view == null) continue;
            	addInScreen(view, screen, position%COLUME_COUNT, getRealY(position/COLUME_COUNT) , 1, 1);
            	DownloadTaskManager.getInstance().updateScreenPositionInMemory(String.valueOf(item.sid), screen, position);
            	MyApplication.getInstance().mMineViewManager.mSaveModel.addOrUpdate(view);
            	doSave = true;
            	
            }
            //android.util.Log.d("cx","doSave is "+doSave);
            if(doSave){
            	//android.util.Log.d("cx","-------mSaveModel.save---------");
            	MyApplication.getInstance().mMineViewManager.mSaveModel.save();
            }
        	lastScreen = item.screen;
        	lastPosition = item.position;
            index++;
        }
        this.requestLayout();
        this.invalidate();
    }
    
	Comparator<AppItem> comparator = new Comparator<AppItem>() {
		public int compare(AppItem s1, AppItem s2) {
			// 先排屏幕
			if (s1.screen != s2.screen) {
				return s1.screen - s2.screen;
			} else {
				// 屏幕相同则按位置排序
				return s1.position - s2.position;
			}
		}
	};
    	  
    public void updateItem(AppItem item){
    	final int cellCount = this.getChildCount();
    	int cellIndex = 0;
    	int position = 0;
    	for( ; cellIndex < cellCount ; cellIndex++){
    		final CellLayout current = ((CellLayout) getChildAt(cellIndex));
        	if(current == null){
        		return;
        	}
        	final int count = current.getChildCount();
        	for( position = 0 ; position < count ; position++){
        		final View view = current.getChildAt(position);
        		if(view instanceof DownloadAppIcon){
        			final DownloadAppIcon downloadAppIcon = (DownloadAppIcon)view;
        			if(downloadAppIcon.getAppItem().sid == item.sid){
        				((DownloadAppIcon)view).updateAppItem(item);
        				ExpendAppInfo info = (ExpendAppInfo)downloadAppIcon.getTag();
        				info.mAppItem = item;
        				downloadAppIcon.setTag(info);
        				if(item.icon != null){
        					downloadAppIcon.getIconView().setCompoundDrawablesWithIntrinsicBounds(null,
            		                new FastBitmapDrawable(item.icon),
            		                null, null);
        				}
        				
        				return;
        			}
        		}
        	}   
    	}
    	if(position > COLUME_COUNT * ROW_COUNT -1){
    		//no space ,add to next screen
    		
    		position = 0;
    	} else {
    		cellIndex -=  1  ;
    		if(cellIndex < 0){
    			cellIndex = 0;
    		}
    	}
    	item.screen = cellIndex;
    	item.position = position;
    	Intent intent = this.getContext().getPackageManager().getLaunchIntentForPackage(item.pname);
    	View view = mLauncher.createTaskIcon(getContext(), (ViewGroup)getChildAt(item.screen), intent,item);
    	addInScreen(view, item.screen, item.position%COLUME_COUNT, getRealY(item.position/COLUME_COUNT) , 1, 1);   	
    	MyApplication.getInstance().mMineViewManager.mSaveModel.addOrUpdate(view);
    	MyApplication.getInstance().mMineViewManager.mSaveModel.save();
    }
    
    public View getItemBySid(int sid){
    	final int cellCount = this.getChildCount();
    	int cellIndex = 0;
    	int position = 0;
    	for( ; cellIndex < cellCount ; cellIndex++){
    		final CellLayout current = ((CellLayout) getChildAt(cellIndex));
        	if(current == null){
        		continue;
        	}
        	final int count = current.getChildCount();
        	for( position = 0 ; position < count ; position++){
        		final View view = current.getChildAt(position);
        		if(view instanceof DownloadAppIcon){
        			final DownloadAppIcon downloadAppIcon = (DownloadAppIcon)view;
        			if(downloadAppIcon.getAppItem().sid == sid){
        				return downloadAppIcon;
        			}
        		}
        	}   
    	}
    	return null;
    }
    
    private int getRealY(int index){
    	if(index >= ROW_COUNT){
    		final int y =  index - ROW_COUNT;
    		return getRealY(y);
    	} else {
    		return index;
    	}
    }

    @Override
    public boolean onLongClick(View v) {
        if (DBG) LogUtil.d(TAG, "onLongClick() view id: " + v.getId() + ", view: " + v);
        enterEditMode();

        return true;
    }

    
	@Override
	public void setDragController(DragController dragger) {
		// TODO Auto-generated method stub
		mDragController = dragger;
	}
	
	void startDrag(CellLayout.CellInfo cellInfo, boolean vibrate) {
		View child = cellInfo.cell;

		// Make sure the drag was started by a long press as opposed to a long
		// click.
		if (!child.isInTouchMode()) {
			return;
		}

		mDragInfo = cellInfo;
		mDragInfo.screen = mCurrentScreen;
		mDragInfo.originScreen = mCurrentScreen;

		CellLayout current = ((CellLayout) getChildAt(mCurrentScreen));
		current.onDragChild(child);
		mDragController.startDrag(child, this, cellInfo,
				DragController.DRAG_ACTION_MOVE, false);

		LogUtil.d(TAG, "startDrag mDragInfo:" + mDragInfo);

		invalidate();
	}
	
	/* added for move drag info from closed folder to workspace. */
	public CellLayout.CellInfo getDragInfo() {
		return mDragInfo;
	}
	
	protected long getContainerId() {
		return LauncherSettings.Favorites.CONTAINER_DESKTOP;
	}
	
	/* added for move drag info from closed folder to workspace. */
	void updateDrag(CellLayout.CellInfo cellInfo) {
		if (cellInfo == null)
			return;

		int screen = mCurrentScreen;
		int[] cellXY = new int[2];
		CellLayout current = null;

		do {
			current = ((CellLayout) getChildAt(screen));
			if (current == null) {
				addScreen(1);
				continue;
			}

			current.findTheLastVacantCell(cellXY, null);
			if (cellXY[0] >= 0 && cellXY[1] >= 0) {
				if (LauncherApplication.DBG)
					LogUtil.d(TAG, "updateDrag() Got cell @ x:" + cellXY[0] + " y:"
							+ cellXY[1] + " in screen:" + screen);
				break;
			}

			if (LauncherApplication.DBG)
				LogUtil.d(TAG, "updateDrag() Cannot get valid cell in screen:"
						+ screen);
			screen++;
		} while (true);

		final View child = cellInfo.cell;
		final CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
				.getLayoutParams();
		final ItemInfo info = (ItemInfo) child.getTag();

		lp.cellX = cellXY[0];
		lp.cellY = cellXY[1];

		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "updateDrag() switch cell screen " + info.screen + ":"
					+ screen + " container id " + info.container + ":"
					+ getContainerId());
		info.container = getContainerId();
		info.screen = screen;

		mDragInfo = cellInfo;
		mDragInfo.originScreen = screen;
		mDragInfo.screen = screen;

		current.onDragChild(child);

		child.setVisibility(View.GONE);

		invalidate();
	}

	@Override
	public void onDropCompleted(View target, boolean success, Object dragInfo,
			DragView dragView) {
		if (LauncherApplication.DBG) {
			LogUtil.d(TAG, "onDropCompleted() target:" + target + " this:" + this);
		}
		CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) dragInfo;
		View child = cellInfo.cell;
		ItemInfo info = (ItemInfo) child.getTag();

		clearVacantCache();

		if (success) {
			if (target != this && mDragInfo != null) {
				if (LauncherApplication.DBG) {
					final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.originScreen);
					if (cellLayout.indexOfChild(mDragInfo.cell) >= 0)
						cellLayout.removeView(mDragInfo.cell);
					LogUtil.d(TAG, "onDropCompleted() child count:"
							+ cellLayout.getChildCount() + " index:"
							+ cellLayout.indexOfChild(mDragInfo.cell));
				}

				autoAlignment();

				/*
				 * if (mDragInfo.cell instanceof DropTarget) {
				 * mDragController.removeDropTarget((DropTarget)mDragInfo.cell);
				 * }
				 */
			}
		} else {
			if (mDragInfo != null) {
				final CellLayout originCellLayout = (CellLayout) getChildAt(mDragInfo.originScreen);
				final CellLayout cellLayout = (CellLayout) getChildAt(mCurrentScreen);
				LogUtil.d(TAG, "current screen " + mCurrentScreen
						+ ",draginfo.screen " + mDragInfo.screen);
				if (mCurrentScreen != mDragInfo.screen) {
					originCellLayout.removeView(mDragInfo.cell);
				}
				if (!cellLayout.onDropAborted(mDragInfo.cell, mTempCell)) { // celllayout
																			// is
																			// not
																			// space
																			// to
																			// accept
																			// cell
					CellLayout.CellInfo cellinfo = getDragInfo();

					updateDrag(cellinfo);
					cellinfo.cell.setVisibility(View.VISIBLE);
					CellLayout cly = (CellLayout) getChildAt(cellinfo.screen);
					cly.onDropAborted(mDragInfo.cell, mTempCell);
				}
				
				/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					mLauncher.getDragLayer().animateViewIntoPosition(dragView, mDragInfo.cell);
		        }*/
			}
		}

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			if (!(getChildAt(i) instanceof CellLayout))
				continue;
			final CellLayout layout = (CellLayout) getChildAt(i);

			layout.clearArrangeAction();
			layout.clearDrawRects();
		}

	}

	
	/**
	 * Return the current {@link CellLayout}, correctly picking the destination
	 * screen while a scroll is in progress.
	 */
	private CellLayout getCurrentDropLayout() {
		int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;

		return (CellLayout) getChildAt(index);
	}
	
	
	private void onDropExternal(int x, int y, Object dragInfo,
			CellLayout cellLayout, DragView dragView) {
		onDropExternal(x, y, dragInfo, cellLayout, false, dragView);
	}

	private void onDropExternal(int x, int y, Object dragInfo,
			CellLayout cellLayout, boolean insertAtFirst, DragView dragView) {
		// Drag from somewhere else
		CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) dragInfo;
		View child = cellInfo.cell;
		// if child has parent now,remove from parent at first
		if (child != null && child.getParent() != null) {
			ViewGroup childParent = (ViewGroup) child.getParent();
			childParent.removeView(child);
		}
		cellLayout.addView(child, insertAtFirst ? 0 : -1);

		child.setHapticFeedbackEnabled(false);
		//child.setOnLongClickListener(mLongClickListener);

		mTargetCell = estimateDropCell(x, y, 1, 1, child, cellLayout,
				mTargetCell);
		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
				.getLayoutParams();
		lp.cellX = mTargetCell[0];
		lp.cellY = mTargetCell[1];
		final ItemInfo info = (ItemInfo) child.getTag();
		info.container = getContainerId();
		info.screen = mCurrentScreen;

		cellLayout.onDropChild(child, mTargetCell);

		/*if (mDragInfo != null && dragView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mLauncher.getDragLayer().animateViewIntoPosition(dragView, mDragInfo.cell);
        }*/

	}
	
	
	/**
	 * Calculate the nearest cell where the given object would be dropped.
	 */
	private int[] estimateDropCell(int pixelX, int pixelY, int spanX,
			int spanY, View ignoreView, CellLayout layout, int[] recycle) {
		// Create vacant cell cache if none exists
		if (mVacantCache == null) {
			if (LauncherApplication.DBG)
				LogUtil.d(TAG, "estimationDropCell() to find all vacant cells.");
			mVacantCache = layout.findAllVacantCells(null, ignoreView);
		}

		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "estimationDropCell() to find the nearest vacant cell.");
		// Find the best target drop location
		return layout.findNearestVacantArea(pixelX, pixelY, spanX, spanY,
				mVacantCache, recycle);
	}
	
	@Override
	public void onDrop(com.byt.market.ui.mine.DragSource source, int x,
			int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
		final CellLayout cellLayout = getCurrentDropLayout();
		if (source != this) {
			if (LauncherApplication.DBG)
				LogUtil.d(TAG, "onDrop() externally...");
			onDropExternal(x - xOffset, y - yOffset, dragInfo, cellLayout, dragView);
		} else {
			// Move internally
			if (LauncherApplication.DBG)
				LogUtil.d(TAG, "onDrop() internally mDragInfo:" + mDragInfo);
			if (mDragInfo != null) {
				final View cell = mDragInfo.cell;
				int index = mScroller.isFinished() ? mCurrentScreen
						: mNextScreen;
				if (index != mDragInfo.screen) {
					final CellLayout originalCellLayout = (CellLayout) getChildAt(mDragInfo.screen);
					originalCellLayout.removeView(cell);
					cellLayout.addView(cell);

					/* auto align all cell layouts */
					autoAlignment();
				}

				mTargetCell = estimateDropCell(x - xOffset, y - yOffset,
						mDragInfo.spanX, mDragInfo.spanY, cell, cellLayout,
						mTargetCell);
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell
						.getLayoutParams();
				lp.cellX = mTargetCell[0];
				lp.cellY = mTargetCell[1];

				final ItemInfo cellInfo = (ItemInfo) cell.getTag();
				cellInfo.container = getContainerId();
				cellInfo.screen = mCurrentScreen;

				cellLayout.onDropChild(cell, mTargetCell);

				/*if (((AppIcon) cell).isFolderIcon()
						&& ((AppIcon) cell).getIconView() instanceof DropTarget) {
					mDragController.addDropTarget((DropTarget) ((AppIcon) cell)
							.getIconView());
				}
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					mLauncher.getDragLayer().animateViewIntoPosition(dragView, cell);
		        }*/
			}

			mDragInfo = null;
		}
	}

	@Override
	public void onDragEnter(com.byt.market.ui.mine.DragSource source, int x,
			int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		clearVacantCache();

		mDragInfo = (CellLayout.CellInfo) dragInfo;
		mDragInfo.screen = mCurrentScreen;

		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "onDragEnter() screen:" + mDragInfo.screen);
		
	}

	@Override
	public void onDragOver(com.byt.market.ui.mine.DragSource source, int x,
			int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
		final CellLayout cellLayout = (CellLayout) getChildAt(mCurrentScreen);
		if (source != this) {
			if (mDragInfo != null) {
				CellLayout.CellInfo info = (CellLayout.CellInfo) mDragInfo;
				if (mNextScreen == INVALID_SCREEN && mScroller.isFinished()) {
					if (LauncherApplication.DBG)
						LogUtil.d(TAG, "onDragOver() external processing.");
					final View cell = info.cell;
					cellLayout.onDragOverChild(cell, x - xOffset, y - yOffset);
				}
			}
		} else {
			// Drag over internally
			if (mDragInfo != null) {
				if (mNextScreen == INVALID_SCREEN && mScroller.isFinished()) {
					if (LauncherApplication.DBG)
						LogUtil.d(TAG,
								"onDragOver() internal processing. mDragInfo:"
										+ mDragInfo);
					final View cell = mDragInfo.cell;
					cellLayout.onDragOverChild(cell, x - xOffset, y - yOffset);
				}
			}
		}
	}

	@Override
	public void onDragExit(com.byt.market.ui.mine.DragSource source, int x,
			int y, int xOffset, int yOffset, DragView dragView,
			Object dragInfo, com.byt.market.ui.mine.DropTarget dropTarget) {
		if (mDragInfo == null)
			return;
		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "onDragExit() mCurrentScreen:" + mCurrentScreen
					+ " mDragInfo.screen:" + mDragInfo.screen);

		clearVacantCache();

		CellLayout.CellInfo info = (CellLayout.CellInfo) mDragInfo;
		final CellLayout cellLayout = (CellLayout) getChildAt(info.screen);

		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "onDragExit() info.cell:" + info.cell);
		cellLayout.removeView(info.cell);
		cellLayout.clearDragStatus();
		
		/* clear cellX & cellY */
		info.cellX = -1;
		info.cellY = -1;

		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) info.cell.getLayoutParams();
        lp.cellX = -1;
        lp.cellY = -1;

		info.originScreen = mCurrentScreen;
		mDragController.updateDragSource(this);

		if (true) {
			/*
			 * disable it if drop to folder icon. if enable, folder auto
			 * creation will trigger auto alignment.
			 */
			cellLayout.autoAlignment(info == null ? null : info.cell);
			cellLayout.clearFolderCreationAction();
		}
	}

	@Override
	public boolean acceptDrop(com.byt.market.ui.mine.DragSource source,
			int x, int y, int xOffset, int yOffset, DragView dragView,
			Object dragInfo) {
		// LogUtil.d(TAG,"Enter AcceptDrop");
		final CellLayout layout = getCurrentDropLayout();
		final CellLayout.CellInfo cellInfo = mDragInfo;
		final int spanX = cellInfo == null ? 1 : cellInfo.spanX;

		final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
		boolean ret = false;
		synchronized (layout.mArrangeLock) {

			// if (mVacantCache == null) {
			final View ignoreView = cellInfo == null ? null : cellInfo.cell;
			mVacantCache = layout.findAllVacantCells(null, ignoreView);
			// }

			ret = mVacantCache.findCellForSpan(mTempEstimate, spanX, spanY,
					false);
			if (ret == false) {
				layout.clearArrangeAction();
				layout.clearFolderCreationAction();

			}
			// LogUtil.d(TAG,"AcceptDrop return  "+ret+",est x,Y:"+mTempEstimate[0]+","+mTempEstimate[1]);
		}
		return ret;
	}

	@Override
	public boolean isFull(Object dragInfo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Rect estimateDropLocation(
			com.byt.market.ui.mine.DragSource source, int x, int y,
			int xOffset, int yOffset, DragView dragView, Object dragInfo,
			Rect recycle) {
		final CellLayout layout = getCurrentDropLayout();

		if (LauncherApplication.DBG)
			LogUtil.d(TAG, "estimateDropLocation()");
		final CellLayout.CellInfo cellInfo = mDragInfo;
		final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
		final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
		final View ignoreView = cellInfo == null ? null : cellInfo.cell;

		final Rect location = recycle != null ? recycle : new Rect();

		// Find drop cell and convert into rectangle
		int[] dropCell = estimateDropCell(x - xOffset, y - yOffset, spanX,
				spanY, ignoreView, layout, mTempCell);

		if (dropCell == null) {
			return null;
		}

		layout.cellToPoint(dropCell[0], dropCell[1], mTempEstimate);
		location.left = mTempEstimate[0];
		location.top = mTempEstimate[1];

		layout.cellToPoint(dropCell[0] + spanX, dropCell[1] + spanY,
				mTempEstimate);
		location.right = mTempEstimate[0];
		location.bottom = mTempEstimate[1];

		return location;
	}
	
	/* added for move drag info from closed folder to workspace. */
	void removeDragView() {
		if (mDragInfo != null && mDragInfo.cell != null) {
			CellLayout current = ((CellLayout) getChildAt(mDragInfo.screen));
			current.removeView(mDragInfo.cell);
		}
	}
	
	public void isForFolder(boolean enable) {
		mIsForFolder = enable;
		if (enable) {
			setCurrentScreen(0);
		}
	}
	
	public int getCellHeight() {
		final CellLayout layout = getCurrentDropLayout();

		// search layout
		if (layout == null) {
			return this.getContext().getResources().getDimensionPixelSize(
					R.dimen.workspace_cell_height);
		}
		return layout.getCellHeight();
	}
	
	public Folder getFolderForTag(Object tag) {
		int screenCount = getChildCount();
		for (int screen = 0; screen < screenCount; screen++) {
			if (!(getChildAt(screen) instanceof CellLayout))
				continue;
			CellLayout currentScreen = ((CellLayout) getChildAt(screen));
			int count = currentScreen.getChildCount();
			for (int i = 0; i < count; i++) {
				View child = currentScreen.getChildAt(i);
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
						.getLayoutParams();
				if (lp.cellHSpan == 4 && lp.cellVSpan == 4
						&& child instanceof Folder) {
					Folder f = (Folder) child;
					if (f.getInfo() == tag) {
						return f;
					}
				}
			}
		}
		return null;
	}
	public float getWallpaperOffsetX() {
		return 0.5f;
	}
    
    

}
