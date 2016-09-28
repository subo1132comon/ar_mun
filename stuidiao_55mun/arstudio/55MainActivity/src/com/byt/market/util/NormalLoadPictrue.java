package com.byt.market.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.byt.market.tools.MD5Tools;
import com.byt.market.util.filecache.FileCacheUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

public class NormalLoadPictrue {
	
	 private String uri;
	    private ImageView imageView;
	    private byte[] picByte;
	    private String md5 = ""; 
	    private Context mcontext;
	     
	    public void getPicture(Context context,String uri,ImageView imageView){
	        this.uri = uri;
	        this.imageView = imageView;
	        this.mcontext = context;
	        md5 = MD5Tools.MD5(uri);
	        String resout = FileCacheUtil.getUsercancel(md5, context);
	        if(resout==null){
	        	new Thread(runnable).start();
	        }else{
	        	 byte[] b = Base64.decode(resout , Base64.DEFAULT);
	             Bitmap  bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                imageView.setImageBitmap(bitmap);
	        }
	    }
	     
	    @SuppressLint("HandlerLeak")
	    Handler handle = new Handler(){
	        @Override
	        public void handleMessage(Message msg) {
	            super.handleMessage(msg);
	            if (msg.what == 1) {
	                if (picByte != null) {
	                    Bitmap bitmap = BitmapFactory.decodeByteArray(picByte, 0, picByte.length);
	                    imageView.setImageBitmap(bitmap);
	                    Log.d("nnlog", "bitmap---"+bitmap+"---byt--"+picByte);
	                    
	                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	                    byte imageInByte[] = stream.toByteArray();
	                    String encodedImage = Base64.encodeToString(imageInByte, Base64.DEFAULT);
	                    locationCancal(encodedImage);//本地缓存
	                }
	            }
	        }
	    };
	 
	    Runnable runnable = new Runnable() {
	        @Override
	        public void run() {
	            try {
	                URL url = new URL(uri);
	                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	                conn.setRequestMethod("GET");
	                conn.setReadTimeout(10000);
	                 
	                if (conn.getResponseCode() == 200) {
	                    InputStream fis =  conn.getInputStream();
	                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	                    byte[] bytes = new byte[1024];
	                    int length = -1;
	                    while ((length = fis.read(bytes)) != -1) {
	                        bos.write(bytes, 0, length);
	                    }
	                    picByte = bos.toByteArray();
	                    bos.close();
	                    fis.close();
	                     
	                    Message message = new Message();
	                    message.what = 1;
	                    handle.sendMessage(message);
	                }
	                 
	                 
	            }catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    };
	    
	    private void locationCancal(String res){
	    	FileCacheUtil.setUrlCache(mcontext
	    			,res, md5);
	    }
}
