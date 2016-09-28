package com.byt.market.receiver;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class DownloadReceiver extends BroadcastReceiver{
	

	private Handler mhandler;
	
	public DownloadReceiver(Handler handler){
		this.mhandler = handler;
	}
	 // Handler handler;
   @SuppressLint("NewApi")
	@Override
   public void onReceive(Context context, Intent intent) {
     
     DownloadManager manager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
     if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())){
       DownloadManager.Query query = new DownloadManager.Query(); 
       //在广播中取出下载任务的id
       long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
         query.setFilterById(id); 
         Cursor c = manager.query(query); 
         if(c.moveToFirst()) { 
         	//获取文件下载路径
             String filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
             //如果文件名不为空，说明已经存在了，拿到文件名想干嘛都好
             if(filename != null){
           	 // vdioHandler.
            	 Message msg = mhandler.obtainMessage();
            	 msg.arg1 = 1;
            	 mhandler.sendMessage(msg);
         }
         }
     }
     //else if("com.byt.mark.delet".equals(intent.getAction())){
//       long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
//       //点击通知栏取消下载
//      // Toast.makeText(mcontext, "取消下载", Toast.LENGTH_LONG).show();
//       Log.d("nnlog", "删除了------");
//       manager.remove(ids);
//       
//     //  ShowToastUtil.showShortToast(context, "已经取消下载");
//     }
     
   }

}
