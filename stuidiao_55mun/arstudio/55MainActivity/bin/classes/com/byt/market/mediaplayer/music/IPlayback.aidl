package com.byt.market.mediaplayer.music;

interface IPlayback{

	void start();
	void pause();
	void stop();
	void release();
	void previous();
	void next();
	int getDuration();
	int getTime();
	void seek(in int time);
	boolean isPlaying();
	int getCurrentPosition();
	int getDownloadFileSize();
	int getCacheFileSize();
	void setIsHandlePause();
	String getCurMusicPath();
	String getCurMusicName();
	String getCurMusicAuthor();
	void setPlayMode(in int mode);
	int getPlayMode();
	String getMusicLogo();
	int getMusicCateId();
	String getMusicCateLogoUrl();
	String getMusicCateName();
	String getMusicCateUpdateTime();
	int getMusicPlayListCount();
}