package com.byt.market.data;

import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.byt.market.download.DownloadContent;
import com.byt.market.log.LogModel;
import com.byt.market.util.PackageUtil;

public class AppItem implements Parcelable, AppItemState{

	/**
	 * 列表项图标
	 */
	public Bitmap icon;

	/**
	 * 列表项图标下载地址
	 */
	public String iconUrl;

	// apk属性
	public String name;
	public String pname;
	public String vname;
	public int vcode;
	public int mark ;
	//add by bobo
	public int musicRid;
	public String ImagePath;
	public String cTitle;
	public String cDesc;
	public int cCount;
	public String markiconURL;
	//end--
	public long length;
	public String strLength;
	public String hash;
	public int sid;
	public String apk;
	public String downNum;
	
	public String adesc;
	public boolean isSelected;
	/*
	 * 1、新品 2、首发 3、活动 4、热门 5、推荐 6、官方
	 */
	public String stype;
	public float rating;

	public int cateid;
	public String cateName;

	public int state = STATE_IDLE;
	public int cacheState = STATE_IDLE;
	public int rankcount;
	public int commcount;
	public String updatetime;

	/** 是否是在游戏盒子内的下载的游戏:1表示在,0表示不在 **/
	public int isInnerGame = 1;
	/** 是否已经打开过该游戏,如果已安装未打开过的游戏,则会显示new标识;0表示没打开过,1表示打开过 **/
	public int isOpenned;

	public String list_cateid = LogModel.P_LIST;
	public String list_id = LogModel.P_LIST;

	/**
	 * 我的游戏，存储应用所在屏幕编号
	 * */
	public int screen = DownloadContent.POSITION_NULL;
	/**
	 * 我的游戏，存储应用在当该屏的位置
	 * */
	public int position = DownloadContent.POSITION_NULL;
	public boolean isShowPopup;
	
	//积分应用限制
	public int creditLimit=0;
	//用于判断是否是谷歌应用0不是，1是
	public int googlemarket;
	//谷歌应用的价格
	public double googlePrice;
	 public int ulevel=0;
	 public int isshare ;
	 public float ispay ;
	 public String oobpackage;
	 public int gotype=-1;//该应用放在哪里
	 public int pointtype=-1;//该应用指向哪里
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(iconUrl);
		dest.writeString(name);
		dest.writeString(pname);
		dest.writeString(vname);
		dest.writeInt(vcode);
		dest.writeLong(length);
		dest.writeString(strLength);
		dest.writeString(hash);
		dest.writeInt(sid);
		dest.writeString(apk);
		dest.writeString(downNum);
		

		dest.writeString(adesc);
		dest.writeString(stype);
		dest.writeFloat(rating);
		dest.writeInt(cateid);
		dest.writeString(cateName);
		dest.writeInt(state);
		dest.writeInt(rankcount);
		dest.writeInt(commcount);
		dest.writeString(updatetime);
		dest.writeString(list_id);
		dest.writeString(list_cateid);
		dest.writeInt(isInnerGame);
		dest.writeInt(isOpenned);
		
		dest.writeInt(creditLimit);
		dest.writeInt(googlemarket);
		dest.writeDouble(googlePrice);
		dest.writeInt(ulevel);
		dest.writeInt(cacheState);
		dest.writeInt(isshare);
		dest.writeFloat(ispay);
		dest.writeString(oobpackage);
		dest.writeInt(gotype);
		dest.writeInt(pointtype);
	}

	public static final Parcelable.Creator<AppItem> CREATOR = new Creator<AppItem>() {
		public AppItem createFromParcel(Parcel source) {
			AppItem item = new AppItem();
			item.iconUrl = source.readString();
			item.name = source.readString();
			item.pname = source.readString();
			item.vname = source.readString();
			item.vcode = source.readInt();
			item.length = source.readLong();
			item.strLength = source.readString();
			item.hash = source.readString();
			item.sid = source.readInt();
			item.apk = source.readString();
			item.downNum = source.readString();
			
			item.adesc = source.readString();
			item.stype = source.readString();
			item.rating = source.readFloat();
			item.cateid = source.readInt();
			item.cateName = source.readString();
			item.state = source.readInt();
			item.rankcount = source.readInt();
			item.commcount = source.readInt();
			item.updatetime = source.readString();
			item.list_id = source.readString();
			item.list_cateid = source.readString();
			item.isInnerGame = source.readInt();
			item.isOpenned = source.readInt();
			
			item.creditLimit = source.readInt();
			item.googlemarket = source.readInt();
			item.googlePrice = source.readDouble();
			item.ulevel=source.readInt();
			item.cacheState=source.readInt();
			
			item.isshare=source.readInt();
			item.ispay=source.readFloat();
			item.oobpackage=source.readString();
			item.gotype=source.readInt();
			item.pointtype=source.readInt();			
			return item;
		}

		public AppItem[] newArray(int size) {
			return new AppItem[size];
		}
	};

	/**
	 * 是否已安装但是还未打开过该应用
	 */
	public boolean isInstalledButNotLauchered(Context context) {
		return isInnerGame == 1 && isOpenned == 0
				&& PackageUtil.isInstalledApk(context, pname, null);
	}
}
