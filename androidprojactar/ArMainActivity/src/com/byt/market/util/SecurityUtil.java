package com.byt.market.util;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 安全加密工具类
 * @author Administrator
 *
 */
public class SecurityUtil {
	private static String base64_random = "httpstd";
	
	  /**
	   * 把Bytes 转换为十六进制字符串 
	 * @param data
	 * @return
	 */
	private static String binToHex(byte[] b)
	  {
	      String hs="";
	      String stmp="";
	      for (int n=0;n<b.length;n++) {
	          stmp=(Integer.toHexString(b[n] & 0XFF));
	          if (stmp.length()==1) hs=hs+"0"+stmp;
	          else hs=hs+stmp;
	      }
	      return hs.toLowerCase();
	  }
	  
	
	  /**
	   * 解密
	 * @param src
	 * @return
	 */
	public static String decodeBase64(String src)
	  {
		  if (src!=null)
		  {
			  int srcLen=src.length();
			  int Rlen=base64_random.length();
			  if (srcLen>Rlen)
			  {
				  return Base64.decode(src.substring(Rlen,srcLen),"utf-8");
			  }
		  }
		  
		  return null;
	  }

	  /**
	   * 加密
	 * @param src
	 * @return
	 */
	public static String encodeBase64(String src)
	  {
        return base64_random+Base64.encode(src);
	  }

	  /**
	   * MD5加密
	 * @param src
	 * @return
	 */
	public static String md5Encode(String src)
	  {

		String Content=null;
		try {
			MessageDigest messagedigest = MessageDigest.getInstance("MD5");
	        
			messagedigest.update(src.getBytes("utf8"));
			
			Content=binToHex(messagedigest.digest());

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Content;
	  }
	public static String getMD5(byte[] sourcebyte) {

		String s = null;
		char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
				'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			md.update(sourcebyte);
			byte tmp[] = md.digest(); // MD5 的计算结果是一个 128 位的长整数，
										// 用字节表示就是 16 个字节
			char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
											// 所以表示成 16 进制需要 32 个字符
			int k = 0; // 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
											// 转换成 16 进制字符的转换
				byte byte0 = tmp[i]; // 取第 i 个字节
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
															// >>>
															// 为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
			}
			s = new String(str); // 换后的结果转换为字符串

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	public static String getMD5(String sourceString) {
		byte[] source = sourceString.getBytes();
		return getMD5(source);
	}
}
