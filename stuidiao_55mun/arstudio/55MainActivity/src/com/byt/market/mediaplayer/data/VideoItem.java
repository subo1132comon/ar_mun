package com.byt.market.mediaplayer.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

@DatabaseTable(tableName="dVideoItem")
public class VideoItem  implements Parcelable{
	/**
	 * 
	 */
	@DatabaseField(id = true )
	public int id;
	@DatabaseField
	public String cTitle;
	@DatabaseField
	public String ImagePath;
	@DatabaseField
	public String cDesc;
	@DatabaseField
	public int sid;
	@DatabaseField
	public String hash;
	@DatabaseField
	public long length;
	@DatabaseField
	public String strLength;
	@DatabaseField
	public String videuri;
	@DatabaseField
	public String actors;//演员
	@DatabaseField
	public String area;//地区
	@DatabaseField
	public String directors;//导演
	@DatabaseField
	public String year;//年份
	@DatabaseField
	public String playsum;//播放次数
	@DatabaseField
	public int isshare ;
	@DatabaseField
	 public float ispay ;//包月价格
	@DatabaseField
	 public float ispayday;//包天价格
	@DatabaseField
	 public String webURL ;
	 public String name;
	 public int fieeid_day;
	 public int feeid_month;
	 public int feeid_year;
	 public int price_day;
	 public int price_month;
	 public int price_year;
	 public int FEEID_WEEK ;  
	 public int FEEID_2MONTHS;
	 public int FEEID_YEAR;
	 public int PRICE_WEEK;
	 public int PRICE_2MONTHS;
	 public int PRICE_YEAR;
	 public int state = -1;
	 public int getFEEID_WEEK() {
		return FEEID_WEEK;
	}
	public void setFEEID_WEEK(int fEEID_WEEK) {
		FEEID_WEEK = fEEID_WEEK;
	}

	public int getFEEID_2MONTHS() {
		return FEEID_2MONTHS;
	}

	public void setFEEID_2MONTHS(int fEEID_2MONTHS) {
		FEEID_2MONTHS = fEEID_2MONTHS;
	}

	public int getFEEID_YEAR() {
		return FEEID_YEAR;
	}

	public void setFEEID_YEAR(int fEEID_YEAR) {
		FEEID_YEAR = fEEID_YEAR;
	}

	public int getPRICE_WEEK() {
		return PRICE_WEEK;
	}

	public void setPRICE_WEEK(int pRICE_WEEK) {
		PRICE_WEEK = pRICE_WEEK;
	}

	public int getPRICE_2MONTHS() {
		return PRICE_2MONTHS;
	}

	public void setPRICE_2MONTHS(int pRICE_2MONTHS) {
		PRICE_2MONTHS = pRICE_2MONTHS;
	}

	public int getPRICE_YEAR() {
		return PRICE_YEAR;
	}

	public void setPRICE_YEAR(int pRICE_YEAR) {
		PRICE_YEAR = pRICE_YEAR;
	}
	 public int getFieeid_day() {
		return fieeid_day;
	}

	public void setFieeid_day(int fieeid_day) {
		this.fieeid_day = fieeid_day;
	}

	public int getFeeid_month() {
		return feeid_month;
	}

	public void setFeeid_month(int feeid_month) {
		this.feeid_month = feeid_month;
	}

	public int getFeeid_year() {
		return feeid_year;
	}

	public void setFeeid_year(int feeid_year) {
		this.feeid_year = feeid_year;
	}

	public int getPrice_day() {
		return price_day;
	}

	public void setPrice_day(int price_day) {
		this.price_day = price_day;
	}

	public int getPrice_month() {
		return price_month;
	}

	public void setPrice_month(int price_month) {
		this.price_month = price_month;
	}

	public int getPrice_year() {
		return price_year;
	}

	public void setPrice_year(int price_year) {
		this.price_year = price_year;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int describeContents() {
		return 0;
	}

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

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getStrLength() {
		return strLength;
	}

	public void setStrLength(String strLength) {
		this.strLength = strLength;
	}

	public String getVideuri() {
		return videuri;
	}

	public void setVideuri(String videuri) {
		this.videuri = videuri;
	}

	public String getActors() {
		return actors;
	}

	public void setActors(String actors) {
		this.actors = actors;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getDirectors() {
		return directors;
	}

	public void setDirectors(String directors) {
		this.directors = directors;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getPlaysum() {
		return playsum;
	}

	public void setPlaysum(String playsum) {
		this.playsum = playsum;
	}

	public int getIsshare() {
		return isshare;
	}

	public void setIsshare(int isshare) {
		this.isshare = isshare;
	}

	public float getIspay() {
		return ispay;
	}

	public void setIspay(float ispay) {
		this.ispay = ispay;
	}

	public float getIspayday() {
		return ispayday;
	}

	public void setIspayday(float ispayday) {
		this.ispayday = ispayday;
	}

	public String getWebURL() {
		return webURL;
	}

	public void setWebURL(String webURL) {
		this.webURL = webURL;
	}

	public static Parcelable.Creator<VideoItem> getCreator() {
		return CREATOR;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(cTitle);
		dest.writeString(ImagePath);
		dest.writeString(cDesc);
		dest.writeInt(sid);
		dest.writeString(strLength);
		dest.writeLong(length);
		dest.writeString(hash);
		dest.writeString(videuri);
		dest.writeString(actors);
		dest.writeString(area);
		dest.writeString(directors);
		dest.writeString(year);
		dest.writeString(playsum);
		dest.writeInt(isshare);
		dest.writeFloat(ispay);
		dest.writeString(webURL);
		dest.writeString(name);
		dest.writeInt(fieeid_day);
		dest.writeInt(feeid_month);
		dest.writeInt(feeid_year);
		dest.writeInt(price_day);
		dest.writeInt(price_month);
		dest.writeInt(price_year);
		dest.writeInt(PRICE_WEEK);
		dest.writeInt(PRICE_2MONTHS);
		dest.writeInt(PRICE_YEAR);
		dest.writeInt(FEEID_WEEK);
		dest.writeInt(FEEID_2MONTHS);
		dest.writeInt(FEEID_YEAR);
		dest.writeInt(state);
		
	}

	public static final Parcelable.Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
		public VideoItem createFromParcel(Parcel source) {
			VideoItem item = new VideoItem();
			item.id = source.readInt();
			item.cTitle = source.readString();
			item.ImagePath = source.readString();
			item.cDesc = source.readString();
			item.sid = source.readInt();
			item.strLength = source.readString();
			item.length = source.readLong();
			item.hash = source.readString();
			item.videuri = source.readString();	
			item.actors = source.readString();	
			item.area = source.readString();	
			item.directors = source.readString();	
			item.year = source.readString();	
			item.playsum = source.readString();	
			item.isshare=source.readInt();
			item.ispay=source.readFloat();
			item.webURL= source.readString();
			item.name= source.readString();
			item.fieeid_day = source.readInt();
			item.feeid_month = source.readInt();
			item.feeid_year = source.readInt();
			item.price_day = source.readInt();
			item.price_month = source.readInt();
			item.price_year = source.readInt();
			item.PRICE_WEEK = source.readInt();
			item.PRICE_2MONTHS = source.readInt();
			item.PRICE_YEAR = source.readInt();
			item.FEEID_WEEK = source.readInt();
			item.FEEID_2MONTHS = source.readInt();
			item.FEEID_YEAR = source.readInt();
			item.state = source.readInt();
			return item;
		}

		public VideoItem[] newArray(int size) {
			return new VideoItem[size];
		}
	};
}
