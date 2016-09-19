package com.byt.market.data;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchHotword implements Parcelable {
	public int id;
	public String key;
	public String count;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(key);
		dest.writeString(count);
	}

	public static final Parcelable.Creator<SearchHotword> CREATOR = new Creator<SearchHotword>() {
		public SearchHotword createFromParcel(Parcel source) {
			SearchHotword item = new SearchHotword();
			item.id = source.readInt();
			item.key = source.readString();
			item.count = source.readString();
			return item;
		}

		public SearchHotword[] newArray(int size) {
			return new SearchHotword[size];
		}
	};

}
