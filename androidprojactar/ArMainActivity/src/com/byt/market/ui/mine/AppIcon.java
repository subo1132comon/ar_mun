package com.byt.market.ui.mine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byt.ar.R;
import com.byt.market.util.LogUtil;

public class AppIcon extends RelativeLayout {
    private static final boolean DBG = false;
    private static final String TAG = "Launcher.AppIcon";

    public static final int STATE_NORMAL = 0;
    public static final int STATE_EDITABLE = 1 << 0;
    public static final int STATE_NOTIFICATION = 1 << 1;
    public static final String NOTIFY_UPDATE = "Up";
    public static final String NOTIFY_FAV = "FAV";
    public static final String NOTIFY_NULL = "";
    private int mState = STATE_NORMAL;

    private ImageView mCloseIconView = null;
    private TextView mNotify = null;
    private BubbleTextView mIconView = null;

    private boolean mRemovable = false;

    private int mNotificationNumber = 0;
    
    private String mNotificationStr = "" ;

    private MineViewManager mLauncher;

    private Context mContext;
    
    private float app_icon_text_size;
    
    private DrawFilter mDrawFilter = new PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);

    public interface NotificationCleannedListener {
        void onNotificationCleanned(AppIcon icon);
    };

    NotificationCleannedListener mNotificationCleannedListener;

    public void setNotificationCleannedListener(NotificationCleannedListener l) {
        mNotificationCleannedListener = l;
    }

    public AppIcon(Context context) {
        this(context, null);
    }

    /**
     * Used to inflate the AppIcon from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public AppIcon(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        app_icon_text_size = mContext.getResources().getDimension(R.dimen.app_icon_text_size);
    }
    
    
    @Override
        protected void dispatchDraw(Canvas canvas) {
        canvas.setDrawFilter(mDrawFilter);
        super.dispatchDraw(canvas);
    }

    private void updateNotification(TextView notify) {
    	if(!TextUtils.isEmpty(mNotificationStr)){
    		if(NOTIFY_UPDATE.equals(mNotificationStr)){
    			notify.setBackgroundResource(R.drawable.mine_prmt_update);
    			notify.setText(NOTIFY_NULL);
    		} else if(NOTIFY_FAV.equals(mNotificationStr)){
    			notify.setBackgroundResource(R.drawable.mine_prmt_fav);
    			notify.setText(NOTIFY_NULL);
    		} else {
    			notify.setBackgroundResource(R.drawable.icon_notification_bg);
    			notify.setText(mNotificationStr);
    		}
    		
    	} else {
    		notify.setBackgroundResource(R.drawable.icon_notification_bg);
    		notify.setText(String.valueOf(mNotificationNumber));
    	}
    }

    private TextView generateNotificationView() {
        TextView text = new TextView(mContext);
        
        text.setTextSize(15);//app_icon_text_size);
        text.setTypeface(null, Typeface.BOLD);
        text.setTextColor(Color.WHITE);
        
        text.setGravity(Gravity.CENTER);
        return text;
    }

    private ImageView generateCloseIcon() {
        Drawable d = getResources().getDrawable(R.drawable.close_button);

        ImageView closeIcon = new ImageView(mContext);
        closeIcon.setImageDrawable(d);

        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View view) {
                if (DBG) LogUtil.d(TAG, "onClick()");
                //mLauncher.removeTask(AppIcon.this);
                onRemoveClicked(view);
            }
        };

        closeIcon.setOnClickListener(listener);
        closeIcon.setVisibility(View.GONE);
        
        return closeIcon;
    }

    public void onRemoveClicked(View view){
    	
    }
    public void enterEditMode(boolean withAnimation) {
        setState(STATE_EDITABLE, withAnimation);
    }

    public void exitEditMode(boolean withAnimation) {
        clearState(STATE_EDITABLE, withAnimation);
    }

    public void setNotification(int number, boolean withAnimation) {
        if(DBG) LogUtil.d(TAG, "setNotification number:"+number);
        mNotificationNumber = Math.max(number, 0);

        if (number > 0) {
            setState(STATE_NOTIFICATION, withAnimation);
        } else {
            clearState(STATE_NOTIFICATION, withAnimation);
        }
    }
    
    public void setNotification(String notifyStr, boolean withAnimation) {
        if(DBG) LogUtil.d(TAG, "setNotification notifyStr:"+notifyStr);
        mNotificationStr = notifyStr;

        if (!TextUtils.isEmpty(notifyStr)) {
            setState(STATE_NOTIFICATION, withAnimation);
        } else {
            clearState(STATE_NOTIFICATION, withAnimation);
        }
    }

    private void clearState(int state, boolean withAnimation) {
        if ((mState & state) == 0) return;

        mState &= ~state;

        if (state == STATE_EDITABLE) {
            ImageView closeIcon = mCloseIconView;

            if (closeIcon != null) {
                closeIcon.setVisibility(View.GONE);

                /*if (withAnimation) {
                    Animation animation = AnimationUtils.loadAnimation(mLauncher, R.anim.fade_out_fast);
                    animation.setFillBefore(true);
                    closeIcon.startAnimation(animation);
                }*/
            }
        }

        if (state == STATE_NOTIFICATION) {
            TextView notify = mNotify;

            if (notify != null) {
                notify.setVisibility(View.GONE);

                if (withAnimation) {
                    Animation animation = AnimationUtils.loadAnimation(mLauncher.getContext(), R.anim.fade_out_fast);
                    animation.setFillBefore(true);
                    notify.startAnimation(animation);
                }
            }

            if (mNotificationCleannedListener != null) {
                mNotificationCleannedListener.onNotificationCleanned(this);
            }
        }
    }

    private void setState(int state, boolean withAnimation) {
        if ((mState & state) == state) {
            /* already set */

            if (state == STATE_NOTIFICATION) {
                TextView notify = mNotify;
                updateNotification(notify);
            }

            return;
        }

        mState |= state;

        if (state == STATE_EDITABLE && mRemovable) {
            ImageView closeIcon = mCloseIconView;

            if (closeIcon == null) {
                closeIcon = generateCloseIcon();
                addView(closeIcon, -1);

                mCloseIconView = closeIcon;
            }

            closeIcon.setVisibility(View.VISIBLE);

            /*if (withAnimation) {
                Animation animation = AnimationUtils.loadAnimation(mLauncher, R.anim.fade_in_fast);
                animation.setFillBefore(true);
                closeIcon.startAnimation(animation);
            }*/
        }

        if (state == STATE_NOTIFICATION) {
            TextView notify = mNotify;
            if (notify == null) {
                notify = generateNotificationView();

                /* init notify layout params */
                LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                int[] rules = lp.getRules();
                rules[ALIGN_PARENT_RIGHT] = 1;
                rules[ALIGN_PARENT_TOP] = 1;
                lp.topMargin = 0;
                lp.rightMargin = 2;

                addView(notify, -1, lp);

                mNotify = notify;
            }

            updateNotification(notify);

            notify.setVisibility(View.VISIBLE);
            if (withAnimation) {
                Animation animation = AnimationUtils.loadAnimation(mLauncher.getContext(), R.anim.fade_in_fast);
                animation.setFillBefore(true);
                notify.startAnimation(animation);
            }
        }
    }

    public void setRemovable(boolean removable) {
        mRemovable = removable;
    }

    public void setLauncher(MineViewManager launcher) {
        mLauncher = launcher;
    }

    public void setIconView(BubbleTextView iconView) {
        mIconView = iconView;

        addView(iconView, LayoutParams.MATCH_PARENT , LayoutParams.MATCH_PARENT);
    }

    public BubbleTextView getIconView() {
        return mIconView;
    }


    private void startDrag() {
        View v = (View) getParent();
        if (LauncherApplication.DBG) LogUtil.d(TAG, "startDrag() this:"+this+" parent:"+v);

        CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();

        // This happens when long clicking an item with the dpad/trackball
        if (cellInfo == null || cellInfo.cell == null) {
            if (LauncherApplication.DBG) LogUtil.d(TAG, "startDrag() cellInfo:"+cellInfo+" cellInfo.cell:"+(cellInfo==null?"null":cellInfo.cell));
            return;
        }

        v = (View)v.getParent();
        if (v instanceof Workspace) {
       	 	((Workspace)v).startDrag(cellInfo, false);
       }
    }

    long start_down = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
       
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIconView.setPressed(true);
                start_down = System.currentTimeMillis();
                //mHandler.sendEmptyMessageDelayed(MSG_ENTER_EDIT_MODE, 200);
                if ((mState & STATE_EDITABLE) == STATE_EDITABLE) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                mIconView.setPressed(true);
                if ((mState & STATE_EDITABLE) == STATE_EDITABLE) {
                        if(System.currentTimeMillis()-start_down>=200){
                                startDrag();
                                if(mHandler.hasMessages(MSG_ENTER_EDIT_MODE)){
                            		mHandler.removeMessages(MSG_ENTER_EDIT_MODE);
                            	}
                                return true;
                        }
                        
                }
                break;

            case MotionEvent.ACTION_UP:
            	if(mHandler.hasMessages(MSG_ENTER_EDIT_MODE)){
            		mHandler.removeMessages(MSG_ENTER_EDIT_MODE);
            	}
                mIconView.setPressed(false);
                if ((mState & STATE_EDITABLE) == STATE_EDITABLE) {
                    return true;
                }
                break;

            default:
                mIconView.setPressed(false);
                break;
        }

        return super.onTouchEvent(event);
    }
    
    private static final int MSG_ENTER_EDIT_MODE = 1;
    Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
				case MSG_ENTER_EDIT_MODE:{
					mIconView.setPressed(true);
	                if ((mState & STATE_EDITABLE) == STATE_EDITABLE) {
	                        if(System.currentTimeMillis()-start_down>=200){
	                                startDrag();
	                        }
	                        
	                }
					break;
				}
			}
			
		}
    	
    };
}