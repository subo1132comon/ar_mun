package com.byt.market.qiushibaike.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
	 ImageLoadedCallback callback = null;
	 String url = null;

	 public DownloadImageTask(ImageLoadedCallback callback) {
	  this.callback = callback;
	 }

	 protected Bitmap doInBackground(String... urls) {
	  URL myFileUrl = null;
	  Bitmap bitmap = null;
	  url = urls[0];
	  try {
	   myFileUrl = new URL(url);
	  } catch (MalformedURLException e) {
	   e.printStackTrace();
	  }
	  HttpURLConnection conn=null;
	  InputStream is=null;
	  try {
	   conn = (HttpURLConnection) myFileUrl
	     .openConnection();
	   conn.setDoInput(true);
	   conn.connect();
	   is = conn.getInputStream();
	   bitmap = BitmapFactory.decodeStream(is);
	   is.close();
	  } catch (IOException e) {
	   e.printStackTrace();
	  }
	  finally
	  {
	   if(conn!=null)
	   {
	    try {
	     conn.connect();
	    } catch (IOException e) {
	     // TODO Auto-generated catch block
	     e.printStackTrace();
	    }
	    conn=null;
	   }
	   if(is!=null)
	   {
	    try {
	     is.close();
	    } catch (IOException e) {
	     // TODO Auto-generated catch block
	     e.printStackTrace();
	    }
	    is=null;
	   }
	  }
	  return bitmap;
	 }

	 protected void onPostExecute(Bitmap bitMap) {
	  callback.loaded(bitMap, url);
	 }
	}
