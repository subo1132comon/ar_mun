package com.byt.market.util;

import java.util.Set;

import com.byt.market.MyApplication;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import android.os.Handler;
import android.util.Log;

public class JPushSetAlias {
	
	private static final int MSG_SET_ALIAS = 89;
	public static void setAlias(String alias){
		mhandler.sendMessage(mhandler.obtainMessage(MSG_SET_ALIAS, alias));
	}
	
	private final static TagAliasCallback mAliasCallback = new TagAliasCallback() {
	    @Override
	    public void gotResult(int code, String alias, Set<String> tags) {
	        switch (code) {
	        case 0:
	            // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
	        	Log.d("subo", "别名设置成功");
	            RapitUtile.setIsAlias(true);
	            break;
	        case 6002:
	            // 延迟 60 秒来调用 Handler 设置别名
	        	Log.d("subo", "别名设置延迟 60 秒");
	            mhandler.sendMessageDelayed(mhandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
	            break;
	        default:
	        	Log.d("subo", "别名设置失败");
	        }
	    }
	};
	
	private static Handler mhandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			switch (msg.what) {
            case MSG_SET_ALIAS:
                // 调用 JPush 接口来设置别名。
                JPushInterface.setAliasAndTags(MyApplication.getInstance().getApplicationContext(),
                                                (String) msg.obj,
                                                 null,
                                                 mAliasCallback);
            break;
        default:
        }
		}
	};

}
