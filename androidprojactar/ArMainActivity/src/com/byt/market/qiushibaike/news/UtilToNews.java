package com.byt.market.qiushibaike.news;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.byt.market.Constants;
import com.byt.ar.R;
import com.byt.market.activity.HtmlTVpalyActivity;
import com.byt.market.activity.JokeDetailsWebViewActivity;
import com.byt.market.data.CateItem;
import com.byt.market.qiushibaike.JokeDetailsActivity;
import com.byt.market.tools.LogCart;
import com.byt.market.view.gifview.GifDecoderView;

public class UtilToNews {
	public static void toNews(Context context,CateItem cateItem){
		 String type  = "";
         String url="";
			String contentstring = cateItem.content;
			if(contentstring!=null&&contentstring.trim().length()>0)
			{
				contentstring=contentstring.replaceAll("<p>", "");
				if(!contentstring.equals(""))
				{
					int start=0;
					int end=0;
					TextView textview=new TextView(context);
					GifDecoderView gif=new GifDecoderView(context);
					LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
					lp1.setMargins(20, 0, 20, 0);//(int left, int top, int right, int bottom)
					LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); 
					lp2.setMargins(20, 12, 20, 12);
					FrameLayout.LayoutParams lp3=new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					lp3.setMargins(20, 12, 20, 12);
					textview.setTextColor(context.getResources().getColor(R.color.jokedetailcolr));
					textview.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
					textview.setPadding(0, 0, 0,0);
					textview.setGravity(Gravity.TOP|Gravity.LEFT);
					android.util.Log.d("newzx",contentstring);
					android.util.Log.d("newzx",contentstring);
					LogCart.Log("视频-----"+contentstring);
					if(contentstring.startsWith("<img src="))
					{
						start=contentstring.indexOf("<img src=");
						contentstring=contentstring.substring(start+10, contentstring.length());
						end=contentstring.indexOf("\"");
						if(!contentstring.startsWith("http://")){
							url=Constants.APK_URL+contentstring.substring(0,end);
						}else{
							url=contentstring.substring(0,end); 
						}
						end=contentstring.indexOf("/>");
						contentstring=contentstring.substring(end+2, contentstring.length());
						type = url.substring(url.lastIndexOf('.') + 1);


					}else{
						LogCart.Log("视频--2---"+contentstring);
						if(contentstring.startsWith("<iframe")||contentstring.startsWith("&lt;iframe")){
							int st = 0;
					        st = contentstring.indexOf("http");
					        String h = contentstring.substring(st);
					        String[] ss =h.split("\"");
					        String s = ss[0];
					        type = "mp4";
					        url = s;
						}else{
							//ispay_img.setVisibility(View.GONE);
						}
					}
				}
			}
			
			if(type.equals("gif")){
				Intent intent = new Intent(context,PlayGifActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("joke_image_path", url);
				bundle.putInt("sid", cateItem.sid);
				intent.putExtras(bundle);			
				context.startActivity(intent);
			}else if(type.equals("mp4")){
				Intent pintent = new Intent(context, HtmlTVpalyActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("url", url);
				bundle.putInt("sid", cateItem.sid);
				pintent.putExtras(bundle);
				context.startActivity(pintent);
			}else{
				//onAppClick(cateItem, Constants.TOJOKETETAILS);
				//Intent intent = new Intent(context,JokeDetailsActivity2.class);
				Intent intent = new Intent(context,JokeDetailsWebViewActivity.class);
					Bundle bundle = new Bundle();
					//bundle.putString("joke_content", cateItem.content);
					//bundle.putInt("isshare", cateItem.isshare);
				//	bundle.putString("joke_image_path", cateItem.ImagePath);
					bundle.putInt(("msid"), cateItem.sid);
					bundle.putString("type", "new");
					bundle.putString("title", cateItem.cTitle);
				//	bundle.putString("time",cateItem.publish_time);
					intent.putExtras(bundle);			
				context.startActivity(intent);
				((Activity) context).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);	
			}
	}
}
