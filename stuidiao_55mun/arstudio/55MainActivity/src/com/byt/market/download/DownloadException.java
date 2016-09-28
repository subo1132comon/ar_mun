package com.byt.market.download;

public class DownloadException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public static final int CODE_UNKNOWN              = 0;
    public static final int CODE_CANCELLED            = 1;
    public static final int CODE_SDCARDAVAILABLE      = 2;
    public static final int CODE_SDCARDSPACE          = 3;
    public static final int CODE_DOWNLOADFILE         = 4;
    public static final int CODE_DOWNLOAD_FAILURE     = 5;
    public static final int CODE_IO                   = 6;
    public static final int CODE_HTTP                 = 7;
    public static final int CODE_NETWORK_ERROR        = 8;

    protected int mHttpCode;
    
    protected long partialLength = 0;

    private int code;

    public DownloadException(int code){
    	super("download Error");
    	this.code = code;
    }
    
    public DownloadException(int code, String message){
    	super(message);
    	this.code = code;
    }
    
    public DownloadException(String msg, long partialLength) {
        this(msg, partialLength, CODE_UNKNOWN);
    }

    public DownloadException(String msg, long partialLength, int code) {
        super(msg);
        this.partialLength = partialLength;
        this.code = code;
    }
    
    public DownloadException(String msg, long partialLength, int code, int httpcode) {
    	this(msg, partialLength, code);
    	mHttpCode = httpcode;
    }
    
    public int getHttpCode(){
    	return mHttpCode;
    }
    
    public long getPartialLength() {
        return partialLength;
    }

    public int getCode() {
        return code;
    }
}