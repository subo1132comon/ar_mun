package com.byt.market.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class HLeaderItem implements Parcelable,AppItemState{
	
    /**
     * 列表项图标
     */
    public Bitmap icon;
    
    /**
     * 列表项图标下载地址
     */
    public String iconUrl;

    public int sid;
    public String name;
    public int stype;


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(sid);
		dest.writeString(name);
		dest.writeInt(stype);
		dest.writeString(iconUrl);
	}
	
	 public static final Parcelable.Creator<HLeaderItem> CREATOR = new Creator<HLeaderItem>() {  
	        public HLeaderItem createFromParcel(Parcel source) {  
	        	HLeaderItem item = new HLeaderItem();  
	        	item.sid = source.readInt();
	        	item.name = source.readString();
	        	item.stype = source.readInt();
	        	item.iconUrl = source.readString();
	            return item;  
	        }  
	        public HLeaderItem[] newArray(int size) {  
	            return new HLeaderItem[size];  
	        }  
	    }; 
}
