package com.byt.market.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.byt.market.Constants;
import com.byt.market.R;
import com.byt.market.data.AppItem;
import com.byt.market.log.LogModel;

/**
 * Created by Administrator on 2014/4/23.
 */
public class DownloadUIUtil {
    public static void toAppDetail(AppItem appItem,Activity activity) {
        Intent intent = new Intent(Constants.TODETAIL);
        intent.putExtra("app", appItem);
        intent.putExtra(Constants.LIST_FROM, LogModel.L_HOME_REC);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_left_in,
                R.anim.push_left_out);
    }

    public static void showPop(){

    }

}
