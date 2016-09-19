package com.byt.market.data;

import android.os.Parcel;
import android.os.Parcelable;

public class PushInfo implements Parcelable {

	public int id;
	public int ptype;
	public String pname;
	public String pdesc;
	public String pvalue;
	public AppItem app;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(ptype);
		dest.writeString(pname);
		dest.writeString(pdesc);
		dest.writeString(pvalue);
		dest.writeParcelable(app, 0);
	}

	public static final Parcelable.Creator<PushInfo> CREATOR = new Creator<PushInfo>() {
		public PushInfo createFromParcel(Parcel source) {
			PushInfo item = new PushInfo();
			item.id = source.readInt();
			item.ptype = source.readInt();
			item.pname = source.readString();
			item.pdesc = source.readString();
			item.pvalue = source.readString();
			item.app = source.readParcelable(null);
			return item;
		}

		public PushInfo[] newArray(int size) {
			return new PushInfo[size];
		}
	};
}
