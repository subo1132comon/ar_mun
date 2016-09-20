package com.byt.market.data;

import com.byt.market.log.LogModel;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class RingItem implements Parcelable, AppItemState {

	/**
	 * 列表项图标
	 */
	public Bitmap icon;

	/**
	 * 列表项图标下载地址
	 */
	public String iconUrl;

	// apk属性
	public boolean isshowPop = false;
	public String name;// 歌名
	public String user;// 作者
	public String spename;// 专辑名
	public String logo; // 专辑图片
	public long length;
	public String strLength;
	public String hash;
	public boolean isplaying = false;// 歌曲是否正在播放
	public int sid;
	public String musicuri;
	public String downNum;

	public String adesc;
	public int state = STATE_IDLE;
	public int rankcount;
	public String list_cateid = LogModel.P_LIST;
	public String list_id = LogModel.P_LIST;
	public String share;

	@Override
	public boolean equals(Object o) {
		if (o instanceof RingItem) {
			RingItem item = (RingItem) o;
			if (item.name.equals(this.name)) {
				return true;
			}
		}
		return super.equals(o);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(iconUrl);
		dest.writeString(name);
		dest.writeString(user);
		dest.writeString(spename);
		dest.writeString(logo);
		dest.writeLong(length);
		dest.writeString(strLength);
		dest.writeString(hash);
		dest.writeInt(sid);
		dest.writeString(musicuri);
		dest.writeString(downNum);

		dest.writeString(list_id);
		dest.writeString(list_cateid);
		dest.writeString(adesc);
		dest.writeInt(state);
		dest.writeInt(rankcount);
		dest.writeString(share);

	}

	public static final Parcelable.Creator<RingItem> CREATOR = new Creator<RingItem>() {
		public RingItem createFromParcel(Parcel source) {
			RingItem item = new RingItem();
			item.iconUrl = source.readString();
			item.name = source.readString();
			item.user = source.readString();
			item.spename = source.readString();
			item.logo = source.readString();
			item.length = source.readLong();
			item.strLength = source.readString();
			item.hash = source.readString();
			item.sid = source.readInt();
			item.musicuri = source.readString();
			item.downNum = source.readString();
			item.list_id = source.readString();
			item.list_cateid = source.readString();
			item.adesc = source.readString();
			item.state = source.readInt();
			item.rankcount = source.readInt();
			item.share = source.readString();
			return item;
		}

		public RingItem[] newArray(int size) {
			return new RingItem[size];
		}
	};

}
