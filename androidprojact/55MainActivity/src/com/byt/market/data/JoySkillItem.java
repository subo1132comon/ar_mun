package com.byt.market.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class JoySkillItem implements Parcelable {

	public Bitmap icon;

	public String pname;
	public String name;
	public String state;
	public String iconUrl;
	public String wUrl;
	public String adEsc;
	public String vCount;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(pname);
		dest.writeString(name);
		dest.writeString(state);
		dest.writeString(iconUrl);
		dest.writeString(wUrl);
		dest.writeString(adEsc);
		dest.writeString(vCount);
	}

	public static final Parcelable.Creator<JoySkillItem> CREATOR = new Creator<JoySkillItem>() {
		public JoySkillItem createFromParcel(Parcel source) {
			JoySkillItem item = new JoySkillItem();
			item.pname = source.readString();
			item.name = source.readString();
			item.state = source.readString();
			item.iconUrl = source.readString();
			item.wUrl = source.readString();
			item.adEsc = source.readString();
			item.vCount = source.readString();
			return item;
		}

		public JoySkillItem[] newArray(int size) {
			return new JoySkillItem[size];
		}
	};
}
