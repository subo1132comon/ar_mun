package com.byt.market.util;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static String md5Encoding(String source){
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            if(md5 != null){
                md5.update(source.getBytes("UTF-8"));
                return byte2hex(md5.digest());
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static String byte2hex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        String stmp;
        for (byte aB : b) {
            stmp = (Integer.toHexString(aB & 0XFF));
            if (stmp.length() == 1) {
                sb.append("0").append(stmp);
            } else {
                sb.append(stmp);
            }
        }
        return sb.toString().toUpperCase(Locale.ENGLISH);
    }
    
    /**
     * 将bit值转换为合适的值输出
     * 
     * @param value
     *            bit值
     * @return String 字符串
     */
    public static String resultBitTranslate(long value) {
        String result = "0";
        if (value > -0.00001 && value < 0.00001)
            return result + "K";
        int boundMillionTranslate = 1024 * 1024;
        int boundKilometerTranslate = 1024;
        float valueTranslate;
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
        df.setMaximumFractionDigits(1);
        if (value > boundMillionTranslate) {
            valueTranslate = value / boundMillionTranslate;
            result = df.format(valueTranslate) + "M";
        } else if (value > boundKilometerTranslate) {
            valueTranslate = value / boundKilometerTranslate;
            result = df.format(valueTranslate) + "K";
        } else {
            result = value + "b";

        }
        return result;
    }
    
    public static String formageDownloadSize(long value) {
        String result = "0";
        if (value > -0.00001 && value < 0.00001)
            return result + "K";
        int boundMillionTranslate = 1024 * 1024;
        int boundKilometerTranslate = 1024;
        DecimalFormat df = new DecimalFormat(".00");
        df.setMaximumFractionDigits(1);
        if (value > boundMillionTranslate) {
            BigDecimal size = new BigDecimal(((double)value) / boundMillionTranslate);  
            result = size.setScale(1,BigDecimal.ROUND_UP).doubleValue()+ "M";
        } else if (value > boundKilometerTranslate) {
        	BigDecimal size = new BigDecimal(((double)value) / boundKilometerTranslate);  
            result = size.setScale(1,BigDecimal.ROUND_UP).doubleValue()+ "K";
        } else {
            result = value + "b";

        }
        return result;
    }

    //set font color
    public static void setSpannableString(TextView tv, String source, int start,
                                           int end, int color) {
        SpannableString spannable = new SpannableString(source);
        spannable.setSpan(new ForegroundColorSpan(color), start, end, 0);
        tv.setText(spannable);
    }
    
    /**
     * 判断是否是数字
     */
    public static boolean isNumeric(String str){ 
		   Pattern pattern = Pattern.compile("[0-9]*"); 
		   Matcher isNum = pattern.matcher(str);
		   if( !isNum.matches() ){
		       return false; 
		   } 
		   return true; 
		}

}
