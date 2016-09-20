package com.byt.market.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class SubjectItem implements Parcelable {

	public Bitmap icon;

	public int sid;
	public String name;
	public String updatetTime;
	public int count;
	public int visitCount;
	public String iconUrl;
	public String adesc;
	public int creditLimit=0;
	public int ulevel=0;
	 public int isshare ;
	 public float ispay ;
	 public float ispayday;//包天价格
	 public int feeID ;
	 public int price_day;
	public int feeid_day;
	public int priodid;
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(sid);
		dest.writeString(name);
		dest.writeString(updatetTime);
		dest.writeInt(count);
		dest.writeInt(visitCount);
		dest.writeString(iconUrl);
		dest.writeString(adesc);
		dest.writeInt(creditLimit);
		dest.writeInt(ulevel);
		dest.writeInt(isshare);
		dest.writeFloat(ispay);
		dest.writeInt(feeID);
		
		dest.writeInt(feeid_day);
		dest.writeInt(priodid);
		dest.writeFloat(ispayday);
	}

	public static final Parcelable.Creator<SubjectItem> CREATOR = new Creator<SubjectItem>() {
		public SubjectItem createFromParcel(Parcel source) {
			SubjectItem item = new SubjectItem();
			item.sid = source.readInt();
			item.name = source.readString();
			item.updatetTime = source.readString();
			item.count = source.readInt();
			item.visitCount = source.readInt();
			item.iconUrl = source.readString();
			item.adesc = source.readString();
			item.creditLimit = source.readInt();
			item.ulevel = source.readInt();
			item.isshare=source.readInt();
			item.ispay=source.readFloat();
			item.feeID=source.readInt();
			
			item.feeid_day = source.readInt();
			item.priodid = source.readInt();
			item.ispayday = source.readFloat();
			return item;
		}

		public SubjectItem[] newArray(int size) {
			return new SubjectItem[size];
		}
	};
}
