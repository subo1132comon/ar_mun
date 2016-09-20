package com.byt.market.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

@DatabaseTable(tableName = "cateis")
public class CateItem implements Parcelable {
	/**
	 * 
	 */
	//public Bitmap icon;
	@DatabaseField(id = true)
	public int id;
	@DatabaseField
	public String cTitle;
	@DatabaseField
	public String ImagePath;
	@DatabaseField
	public int softCount;
	@DatabaseField
	public String cDesc;
	@DatabaseField
	public int sid;
	@DatabaseField
	public String sName;
	@DatabaseField
	public String sDate;
	@DatabaseField
	public int cCount;
	@DatabaseField
	public String content;//分类内容介绍
	@DatabaseField
	public int comment_count;
	@DatabaseField
	public int vote_count;
	@DatabaseField
	public int comment_img_resid;
	@DatabaseField
	public int vote_img_resid;
	@DatabaseField
	public int isshare;
	@DatabaseField
	public int price_day;
	@DatabaseField
	public int feeid;
	@DatabaseField
	public int feeid_day;
	@DatabaseField
	public int priodid;
	public String publish_time;
	public int FEEID_WEEK ;        
	public int FEEID_2MONTHS;
	public int FEEID_YEAR;
	public int PRICE_WEEK;
	public int PRICE_2MONTHS;
	public int PRICE_YEAR;
	@DatabaseField
	public int myH = 2;//区分新闻 大图 和间隔
	@DatabaseField
	public boolean changjoke = false;
	public boolean isNotif = false;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(cTitle);
		dest.writeString(ImagePath);
		dest.writeInt(softCount);
		dest.writeString(cDesc);
		dest.writeInt(sid);
		dest.writeString(sName);
		dest.writeString(sDate);
		dest.writeInt(cCount);
		dest.writeString(content);
		dest.writeInt(comment_count);
		dest.writeInt(vote_count);
		dest.writeInt(comment_img_resid);
		dest.writeInt(vote_img_resid);
		dest.writeInt(isshare);
		dest.writeInt(FEEID_WEEK);
		dest.writeInt(FEEID_2MONTHS);
		dest.writeInt(FEEID_YEAR);
		dest.writeInt(PRICE_WEEK);
		dest.writeInt(PRICE_2MONTHS);
		dest.writeInt(PRICE_YEAR);
	}

	public static final Parcelable.Creator<CateItem> CREATOR = new Creator<CateItem>() {
		public CateItem createFromParcel(Parcel source) {
			CateItem item = new CateItem();
			item.id = source.readInt();
			item.cTitle = source.readString();
			item.ImagePath = source.readString();
			item.softCount = source.readInt();
			item.cDesc = source.readString();
			item.sid = source.readInt();
			item.sName = source.readString();
			item.sDate = source.readString();
			item.cCount = source.readInt();
			item.content = source.readString();
			item.comment_count = source.readInt();
			item.vote_count = source.readInt();
			item.comment_img_resid = source.readInt();
			item.vote_img_resid = source.readInt();
			item.isshare=source.readInt();
			item.FEEID_WEEK = source.readInt();
			item.FEEID_2MONTHS = source.readInt();
			item.FEEID_YEAR = source.readInt();
			item.PRICE_WEEK = source.readInt();
			item.PRICE_2MONTHS = source.readInt();
			item.PRICE_YEAR = source.readInt();
			return item;
		}

		public CateItem[] newArray(int size) {
			return new CateItem[size];
		}
	};


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getcTitle() {
		return cTitle;
	}

	public void setcTitle(String cTitle) {
		this.cTitle = cTitle;
	}

	public String getImagePath() {
		return ImagePath;
	}

	public void setImagePath(String imagePath) {
		ImagePath = imagePath;
	}

	public int getSoftCount() {
		return softCount;
	}

	public void setSoftCount(int softCount) {
		this.softCount = softCount;
	}

	public String getcDesc() {
		return cDesc;
	}

	public void setcDesc(String cDesc) {
		this.cDesc = cDesc;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public String getsName() {
		return sName;
	}

	public void setsName(String sName) {
		this.sName = sName;
	}

	public String getsDate() {
		return sDate;
	}

	public void setsDate(String sDate) {
		this.sDate = sDate;
	}

	public int getcCount() {
		return cCount;
	}

	public void setcCount(int cCount) {
		this.cCount = cCount;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getComment_count() {
		return comment_count;
	}

	public void setComment_count(int comment_count) {
		this.comment_count = comment_count;
	}

	public int getVote_count() {
		return vote_count;
	}

	public void setVote_count(int vote_count) {
		this.vote_count = vote_count;
	}

	public int getComment_img_resid() {
		return comment_img_resid;
	}

	public void setComment_img_resid(int comment_img_resid) {
		this.comment_img_resid = comment_img_resid;
	}

	public int getVote_img_resid() {
		return vote_img_resid;
	}

	public void setVote_img_resid(int vote_img_resid) {
		this.vote_img_resid = vote_img_resid;
	}

	public int getIsshare() {
		return isshare;
	}

	public void setIsshare(int isshare) {
		this.isshare = isshare;
	}

	public int getPrice_day() {
		return price_day;
	}

	public void setPrice_day(int price_day) {
		this.price_day = price_day;
	}

	public int getFeeid() {
		return feeid;
	}

	public void setFeeid(int feeid) {
		this.feeid = feeid;
	}

	public int getFeeid_day() {
		return feeid_day;
	}

	public void setFeeid_day(int feeid_day) {
		this.feeid_day = feeid_day;
	}

	public int getPriodid() {
		return priodid;
	}

	public void setPriodid(int priodid) {
		this.priodid = priodid;
	}

	public int getMyH() {
		return myH;
	}

	public void setMyH(int myH) {
		this.myH = myH;
	}

	public boolean isChangjoke() {
		return changjoke;
	}

	public void setChangjoke(boolean changjoke) {
		this.changjoke = changjoke;
	}

	
	
}
