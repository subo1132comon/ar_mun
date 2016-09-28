package com.byt.market.util;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.byt.market.R;
import com.byt.market.mediaplayer.tv.NewTVListFragment;
import com.byt.market.mediaplayer.ui.MovieFragment;
import com.byt.market.tools.DownLoadVdioapkTools;
import com.tencent.stat.StatService;

public class StartVidioutil {
	
	public static void startVidiao(Context context,String url,String type){
		
		
		if("Move".equals(type)||"AV".equals(type)){
			if(!RapitUtile.tvToastShow(type)){
				//Toast.makeText(context, context.getString(R.string.tv_toast), Toast.LENGTH_LONG).show();
				new ToastUtile(context).show(context.getString(R.string.av_toast), 3);
				RapitUtile.deletTVtoastShow(type);
			}
		}else{
			new ToastUtile(context).show(context.getString(R.string.tv_dowlod_toast), 3);
		}
		//先关闭音乐
		StopMusicUtil.stopMusic(context);
		
		//String phoneName = android.os.Build.MANUFACTURER;
		//Log.d("nnlog", "vvvvvvdio--"+phoneName);
		//if("Xiaomi".equals(android.os.Build.MANUFACTURER)){
			DownLoadVdioapkTools dt = new DownLoadVdioapkTools(context);
			if(dt.checkApkExist(context, "com.tyb.fun.palyer")){
				StatService.trackCustomEvent(context,"installedPlayer","");
				dt.startAPP(url);
			}else{
//				Singinstents.getInstents().setVdiouri(url);
//				Singinstents.getInstents().setAppPackageName("com.tyb.fun.palyer");
//				dt.showNoticeDialog();
				try {
					Uri uri = null;
					if("TV".equals(type)){
						uri = Uri.fromFile(new File(url));//播放本地路径
					}else{
						uri = Uri.parse(url);//播放网络地址
					}
			        // 调用系统自带的播放器来播放流媒体视频
			        Intent intent = new Intent(Intent.ACTION_VIEW);
			        intent.setDataAndType(uri, "video/*");
			        context.startActivity(intent);
				} catch (Exception e) {
					// TODO: handle exception
					Log.d("nnlog", e+"------播放异常");
					
				}
				
			}
		
	}

}
