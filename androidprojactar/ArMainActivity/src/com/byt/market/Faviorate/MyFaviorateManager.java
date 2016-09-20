package com.byt.market.Faviorate;
import android.widget.Toast;

import com.byt.market.MarketContext;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.data.AppItem;
import com.byt.market.util.Util;

import java.util.ArrayList;

/**
 * Created by Administrator on 2014/4/25.
 */
public class MyFaviorateManager {
    private ArrayList<MyFaviorateListener> faviorateListeners = new ArrayList<MyFaviorateListener>();
    private static MyFaviorateManager mInstance;
    private int myFavCount = 0;

    public synchronized static MyFaviorateManager getInstance() {
        if (mInstance == null) {
            mInstance = new MyFaviorateManager();
        }
        return mInstance;
    }
    private MyFaviorateManager() {

    }

    public void addFavApp(MarketContext maContext,AppItem app){
		MyApplication mMyApplication = (MyApplication)MyApplication.getInstance().getApplicationContext();
		myFavCount = mMyApplication.getCollectNum();		
		mMyApplication.setCollectNum(++myFavCount);
		
        //Toast.makeText(MyApplication.getInstance(), "addFavApp"+myFavCount, 150).show();

        Util.addData(maContext, app);
        for(MyFaviorateListener listener:faviorateListeners){
            listener.FaviorateAppAdded(app);
        }
    }

    public void delFavApp(MarketContext maContext,AppItem app){
		MyApplication mMyApplication = (MyApplication)MyApplication.getInstance().getApplicationContext();
		myFavCount = mMyApplication.getCollectNum();		
		mMyApplication.setCollectNum(--myFavCount);
		//Toast.makeText(MyApplication.getInstance(), "addFavApp"+myFavCount, 150).show();
		 
        Util.delData(maContext, app.sid);
        for(MyFaviorateListener listener:faviorateListeners){
            listener.FaviorateAppDeled(app);
        }
    }

    /**
     * 添加游戏收藏观察者
     *
     * @param listener
     */
    public void addListener(MyFaviorateListener listener) {
       faviorateListeners.add(listener);
    }

    /**
     * 移除游戏收藏观察者
     *
     * @param listener
     */
    public void removeListener(MyFaviorateListener listener) {
        faviorateListeners.remove(listener);
    }


}
