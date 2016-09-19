/*
 * Copyright
 */

package com.byt.market.ui.mine;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.byt.market.R;
import com.byt.market.data.AppItem;
import com.byt.market.util.LogUtil;

public class WorkspaceContainer extends RelativeLayout {
    private static final String TAG = "ProcessManagerView";
    private static final boolean DBG_LAYOUT = false;

    private FrameLayout mMask;
    private Workspace mWorkspace;
    
    private MineViewManager mLauncher;
    
    public WorkspaceContainer(Context context) {
        this(context, null);
    }

    public WorkspaceContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorkspaceContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mWorkspace = (Workspace) findViewById(R.id.mine_container);
        mMask = (FrameLayout)findViewById(R.id.process_mgr_mask);

        /*View.OnTouchListener l = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        };
        mMask.setOnTouchListener(l);*/
        mWorkspace.moveToDefaultScreen(false);
    }
    
    public void initViewFromData(List<AppItem> items){
    	mWorkspace.initViewFromData(items);
    }
    
    public void updateItem(AppItem item){
    	mWorkspace.updateItem(item);
    }

    public void setLauncher(MineViewManager launcher) {
        mLauncher = launcher;
        mWorkspace.setLauncher(launcher);
    }
    
    
    
	public void setDragController(DragController dragger) {
		// TODO Auto-generated method stub
		mWorkspace.setDragController(dragger);
	}
    
	public Workspace getWorkSpace(){
		return mWorkspace;
	}
    



    /* layout */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new WorkspaceContainer.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof WorkspaceContainer.LayoutParams;
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new WorkspaceContainer.LayoutParams(p);
    }

    public static class LayoutParams extends RelativeLayout.LayoutParams {
        public int x;
        public int y;

        public LayoutParams(int x, int y) {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            this.x = x;
            this.y = y;
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
        
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        
        if(DBG_LAYOUT) LogUtil.d(TAG, "onMeasure() widthSpecSize: " + widthSpecSize + ", heightSpecSize: " + heightSpecSize);
        
        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("ProcessManagerView cannot have UNSPECIFIED dimensions");
        }

        /* Mask */
        WorkspaceContainer.LayoutParams lp = (WorkspaceContainer.LayoutParams) mMask.getLayoutParams();
        lp.width = widthSpecSize;
        lp.height = heightSpecSize;
        mMask.measure(widthMeasureSpec, heightMeasureSpec);

        /* Workspace */
        lp = (WorkspaceContainer.LayoutParams) mWorkspace.getLayoutParams();
        lp.y = 0;//heightSpecSize - getDockHeight();
        lp.width = widthSpecSize;
        lp.height = heightSpecSize;//getDockHeight();
        mWorkspace.measure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                WorkspaceContainer.LayoutParams lp = (WorkspaceContainer.LayoutParams) child.getLayoutParams();

                if (DBG_LAYOUT) LogUtil.d(TAG, "onLayout() i:"+i+" count:"+count+" x:"+lp.x+" y:"+lp.y+" w:"+lp.width+" h:"+lp.height);

                int childLeft = lp.x;
                int childTop = lp.y;

                child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);
            }
        }
    }
}
