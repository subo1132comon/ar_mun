package com.byt.market.mediaplayer.data;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.byt.market.MyApplication;
import com.byt.market.data.RingItem;
import com.byt.market.mediaplayer.db.InfoDao;
import com.byt.market.mediaplayer.ui.MusicDownedFragment;
import com.byt.market.receiver.DownloadReceiver;
import com.byt.market.tools.LogCart;
import com.byt.market.tools.MyMD5;
import com.byt.market.util.Base64;



/**
 * 下载详细
 * */
public class PlayDownloadItem implements Parcelable  {
	
	//下载大文件
	public DownloadManager downloadManager;
	public long Id;
	public long dSize; // 大小Size
	public String savePath; // 保存位置
	public long cursize;
	public long length;//文件总大小
	public String name;//歌名
	public String strLength;
	public String hash;
	public int state;//0.未下载，1.正在下载2.已下载
	public int sid;
	public int type;//1.音乐2.视频3.小说
	public String musicuri;
	public String downNum;
	public  Context mcontext;
	public  boolean isPause=false;
	public boolean isRuning = false;
	public Handler dhandler;
	private static final String DEFAULT_USER_AGENT_PHONE = "Mozilla/5.0 (Linux; U; "
			+ "Android 4.4.2; en-us; Xoom Build/HMJ25) AppleWebKit/534.13 "
			+ "(KHTML, like Gecko) Version/4.0 Safari/534.13";
	
	public boolean isPause() {
		return isPause;
	}

	public void setPause(boolean isPause) {
		this.isPause = isPause;
	}

	public long getCursize() {
		return cursize;
	}

	public void setCursize(long cursize) {
		this.cursize = cursize;
	}
	public PlayDownloadItem(int msid,long mcursize,String msavePath,long mlength,String mname,String mstrLength,String mhash,int mstate,int mtype,String muri) {
		this.sid=msid;
		this.cursize=mcursize;
		this.savePath=msavePath;
		this.length=mlength;
		this.name=mname;
		this.strLength=mstrLength;
		this.hash=mhash;
		this.state=mstate;
		this.type=mtype;
		this.musicuri=muri;

	}
	public static final int DOWNLOAD_SUCCESS = 1;
	public static final int DOWNLOAD_FAINL = 2;
	public static final int DOWNLOAD_FILE_NOT_FOURND = 3;
	public static final int DOWNLOAD_COMPLETE = 4;

	public static final String DOWNLOAD_URL_KEY = "download_url_key";
	public static final String ITEM_SAVE_PATH_DIR_KEY = "item_save_path_dir_key";
	public static final String ITEM_ALL_SIZE_KEY = "item_all_size_key";
	public static final String  DOWN_ITEM="musicdownitem";
	public static String MUSIC_DIR = Environment.getExternalStorageDirectory()
			.getPath() + "/SYNC/music/";
	public static String VIDEO_DIR = Environment.getExternalStorageDirectory()
			.getPath() + "/SYNC/video/";
	public static String VIDEOTMP_DIR = Environment.getExternalStorageDirectory()
			.getPath() + "/SYNC/videotmp/";
	public static String NOVEL_DIR = Environment.getExternalStorageDirectory()
			.getPath() + "/SYNC/novel/";
	public String adesc;
	
	public String contentmd5;
	public String fileMd5;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getSavePath() {
		return this.savePath;
	}

	public void setSavePath(String path) {
		this.savePath = path;
	}

	/**
	 * 取得已完成下载大小
	 * 
	 * @return
	 */
	public long getdSize() {
		return this.dSize;
	}

	/**
	 *设置已下载文件大小
	 * 
	 * @param d
	 */
	public void setdSize(long d) {
		this.dSize = d;
	}
	public void fillWithOutScreenAndListInfo(RingItem ringItem){
		this.length = ringItem.length;
		this.name = ringItem.name;
		this.adesc = ringItem.adesc;
		this.sid = ringItem.sid;
		this.strLength = ringItem.strLength;
		this.state=ringItem.state;
		this.musicuri=ringItem.musicuri;
	}
	public void fillWithOutScreenAndListInfo(VideoItem ringItem){
		this.length = ringItem.length;
		this.name = ringItem.cTitle;
		this.adesc = ringItem.webURL;
		this.sid = ringItem.sid;
		this.strLength = ringItem.strLength;
		this.state=0;
		this.musicuri=ringItem.webURL;
	}
	public PlayDownloadItem(Context context) {
		mcontext=context;
	}
	public void downloadFile() {
		File file = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		RandomAccessFile fos = null;
		try {
			file = new File(savePath);
			if(type==2){
				musicuri = adesc;
			}
			URL url = new URL(musicuri);
			conn = (HttpURLConnection) url.openConnection();
		} catch (MalformedURLException e1) {
			state = DOWNLOAD_FILE_NOT_FOURND;
			e1.printStackTrace();
		} catch (Exception e1) {
			state = DOWNLOAD_FILE_NOT_FOURND;
			e1.printStackTrace();
		}
		try {
			fos = new RandomAccessFile(savePath, "rw");
			if (file.exists()) {
				conn.setRequestMethod("GET");
				String start = "bytes=" + file.length() + "-";
				conn.setRequestProperty("RANGE", start);
				fos.seek(file.length());
			}
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (ProtocolException e2) {
			state = DOWNLOAD_FILE_NOT_FOURND;
			e2.printStackTrace();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		try {
			is = conn.getInputStream();
			conn.connect();
		} catch (Exception e1) {
			state = DOWNLOAD_FILE_NOT_FOURND;
			e1.printStackTrace();
		}
		try {
			try {
				byte[] buf = new byte[250];
				double count = 0;
				int code = conn.getResponseCode();
//				//重定向
//				if(code == 301){
//					Log.d("nnlog", "code  su  ---"+code);
//					String repath = redirectPath(musicuri, DEFAULT_USER_AGENT_PHONE);
//					downloadFile(repath);
//					Log.d("nnlog", "code pp  ---"+repath);
//					
//				}
				Log.d("nnlog", "code  su  ---"+code);
				if (code < 400) {
					while (count <= 100) {
						if(isPause==true)
						{
							break;
						}
						if (is != null) {
							int numRead = is.read(buf);
							if (numRead <= 0) {
								break;
							} else {
								fos.write(buf, 0, numRead);
							}
						} else {
							break;
						}
						if (file != null) {
							cursize = file.length();
						}
					}
					if(isPause==true)
					{
						state = DOWNLOAD_FAINL;
					}else{
						state = DOWNLOAD_SUCCESS;
					}
					
				} else {
					state = DOWNLOAD_FAINL;
				}
				conn.disconnect();
				fos.close();
				is.close();
			} catch (Exception e) {
				Log.d("errormyzx", "1----"+e.toString());
				state = DOWNLOAD_FAINL;
				e.printStackTrace();
			}
		} catch (Exception e) {
			Log.d("errormyzx", "2----"+e.toString());
			state = DOWNLOAD_FAINL;
			e.printStackTrace();
		}
		if (state != DOWNLOAD_SUCCESS) {
			isPause=true;
		}
		else{
			isPause=false;
		}
		Log.d("rmyzx", "distory="+this.name);
	}
	
	private Handler vdioHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.arg1 == 1){
				isPause = false;
				dhandler.sendMessage(dhandler.obtainMessage());
			}else{
				cursize = msg.what;
			}
		};
	};
	//视频 
	@SuppressLint("NewApi")
	public void downloadFileVdio(Handler handler) {
		this.dhandler = handler;
		IntentFilter intentf = new IntentFilter();
		intentf.addAction("android.intent.action.DOWNLOAD_COMPLETE");
		intentf.addAction("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED");
		//intentf.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
		//DownloadManager.ACTION_NOTIFICATION_CLICKED
		mcontext.registerReceiver(new DownloadReceiver(vdioHandler), intentf);
		downloadManager = (DownloadManager)mcontext.getSystemService(Context.DOWNLOAD_SERVICE);  
		  
        // 假设从这一个链接下载一个大文件。  
        Request request = new Request(  
                Uri.parse(adesc));  
  
        // 仅允许在WIFI连接情况下下载  
        request.setAllowedNetworkTypes(Request.NETWORK_WIFI);  
  
        // 通知栏中将出现的内容  
      //  request.setTitle("我的下载");  
      //  request.setDescription("下载一个大文件");  
        // 下载过程和下载完成后通知栏有通知消息。  
        request.setNotificationVisibility(Request.VISIBILITY_HIDDEN);  
      //  request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);  
  
        // 此处可以由开发者自己指定一个文件存放下载文件。  
        // 如果不指定则Android将使用系统默认的  
         request.setDestinationUri(Uri.fromFile(new File(savePath)));  
  
        // 默认的Android系统下载存储目录  
       // request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ChatPayakEP8-8.mp4");  
  
        // enqueue 开始启动下载...  
        Id = downloadManager.enqueue(request);  
        queryStatus();
        if(isRuning){
        	new Thread(){
				public void run() {
					while (true) {
						try {
							sleep(3000);
							query();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
			}.start();
        }
      //  cursize = 
		
	}
	/**
	 * 
	 */
	 @SuppressLint("NewApi")
		private void query() {  
	        Query downloadQuery = new Query();  
	        downloadQuery.setFilterById(Id);  
	        Cursor cursor = downloadManager.query(downloadQuery);  
	        if (cursor != null && cursor.moveToFirst()) {  
	            int fileName = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);  
	            int fileUri = cursor.getColumnIndex(DownloadManager.COLUMN_URI);  
	            String fn = cursor.getString(fileName);  
	            String fu = cursor.getString(fileUri);  
	  
	            int totalSizeBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);  
	            int bytesDownloadSoFarIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);  
	  
	            // 下载的文件总大小  
	            int totalSizeBytes = cursor.getInt(totalSizeBytesIndex);  
	  
	            // 截止目前已经下载的文件总大小  
	            int bytesDownloadSoFar = cursor.getInt(bytesDownloadSoFarIndex);  
	  
	            Message msg = vdioHandler.obtainMessage();
	            msg.what = bytesDownloadSoFar;
	            vdioHandler.sendMessage(msg);
	            cursor.close();  
	        }  
	    }  
	@SuppressLint("NewApi")
	private void queryStatus() {  
        DownloadManager.Query query = new DownloadManager.Query();  
        query.setFilterById(Id);  
        Cursor cursor = downloadManager.query(query);  
  
        if (cursor.moveToFirst()) {  
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));  
            switch (status) {  
            case DownloadManager.STATUS_PAUSED:  
            case DownloadManager.STATUS_PENDING:  
            case DownloadManager.STATUS_RUNNING:  
               // isPause = true;
                isRuning = true;
                break;  
            case DownloadManager.STATUS_SUCCESSFUL:  
                isPause=false;
                break;  
            case DownloadManager.STATUS_FAILED:  
            	isPause=false;
                break;  
            default:  
                break;  
            }  
        }
    }  
	
//	//冲in定向
//	public String redirectPath(final String str, String userAgent)
//			 throws MalformedURLException {
//			 URL url = null;
//			 String realURL = null;
//			 HttpURLConnection conn = null;
//			 try {
//			 url = new URL(str);
//			 conn = (HttpURLConnection) url.openConnection();
//			 conn.setConnectTimeout(30000);
//			 conn.setReadTimeout(30000);
//			 conn.setRequestProperty("User-Agent", userAgent);
//			 conn.setInstanceFollowRedirects(true);
//			 conn.getResponseCode();// trigger server redirect
//			 realURL = conn.getURL().toString();
//			 
//			 Log.d("nnlog", str + "\r\n" + "redirect to \r\n" + realURL);
//			 } catch (MalformedURLException e) {
//			 throw e;
//			 } catch (IOException e) {
//			 e.printStackTrace();
//			 } finally {
//			 if (conn != null) {
//			 conn.disconnect();
//			 }
//			 }
//			 
//			 return realURL;
//			 }
	
	
	public void deleDownloadFailFile() {
		try {
			File file = new File(savePath);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleSaveNativeInfo() {
		try {
			SharedPreferences sharedata = mcontext.getSharedPreferences(
					"downloadData", 0);
			Editor editor = sharedata.edit();
			editor.remove(new File(savePath).getName());
			editor.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {

		dest.writeLong(dSize);
		dest.writeLong(cursize);
		dest.writeLong(length);
		dest.writeString(strLength);
		dest.writeString(hash);
		dest.writeString(savePath);
		dest.writeString(musicuri);
		dest.writeString(downNum);
		dest.writeInt(state);		
		dest.writeInt(type);
		dest.writeString(hash);
		dest.writeInt(sid);			
	}
	
}
