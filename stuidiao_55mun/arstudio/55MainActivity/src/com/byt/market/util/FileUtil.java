package com.byt.market.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.byt.market.R;
import com.byt.market.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * 文件帮助类
 *@author qiuximing
 */
public class FileUtil {

	public static  int size = 0;
	/**
	 * 是否存储卡状态
	 * 
	 * @return 有存储卡则返回true，否则返回false
	 */
	public static boolean haveSDCard() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	/**
	 * 得到图片保存路径
	 * 
	 * @return 返回图片保存的路径
	 */
	public static String getIconPath() {
		String iconPath = null;
		File sdcardPath = android.os.Environment.getExternalStorageDirectory();// sdcard
		iconPath = sdcardPath.getPath() + Constants.FOLDER_ICONS;
		File iconPathFile = new File(iconPath);
		if (!iconPathFile.exists()) {
			iconPathFile.mkdirs();
		} else {
		}
		return iconPath;
	}
	
	   /**
     * 得到图片保存路径
     * 
     * @return 返回图片保存的路径
     */
    public static String getUserIconPath() {
        String iconPath = null;
        File sdcardPath = android.os.Environment.getExternalStorageDirectory();// sdcard
        iconPath = sdcardPath.getPath() + Constants.FOLDER_USERICONS;
        File iconPathFile = new File(iconPath);
        if (!iconPathFile.exists()) {
            iconPathFile.mkdirs();
        } else {
        }
        return iconPath;
    }

	/**
	 * 将字节数组保存为.png格式文件
	 * 
	 * @param buffer
	 *            字节数组
	 * @param fileName
	 *            文件名
	 * @return 字节数组保存成功则返回true，否则返回false
	 */
	public static boolean saveBytesToFile(byte[] buffer, String fileName) {
		if (fileName == null) {
			return false;
		}
		if (!haveSDCard())
			return false;
		File file = new File(getIconPath() + fileName + ".png");
		if (file.exists())
			file.delete();
		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(buffer);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 从.png格式文件中得到字节数组
	 * 
	 * @param fileName
	 *            图片文件 .png格式
	 * @return 返回文件字节数组
	 */
	public static byte[] getBytesFromFile(String fileName) {
		if (fileName == null)
			return null;
		if (!haveSDCard())
			return null;
		File file = new File(getIconPath() + fileName + ".png");
		if (!file.exists())
			return null;
		FileInputStream fis;
		byte[] buffer;
		try {
			fis = new FileInputStream(file);
			buffer = new byte[fis.available()];
			fis.read(buffer);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			buffer = null;
		}
		return buffer;
	}
	
	   /**
     * 将字节数组保存为.png格式文件
     * 
     * @param buffer
     *            字节数组
     * @param fileName
     *            文件名
     * @return 字节数组保存成功则返回true，否则返回false
     */
    public static boolean saveUserBytesToFile(byte[] buffer, String fileName) {
        if (fileName == null) {
            return false;
        }
        if (!haveSDCard())
            return false;
        File file = new File(getUserIconPath() + fileName + ".png");
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 从.png格式文件中得到字节数组
     * 
     * @param fileName
     *            图片文件 .png格式
     * @return 返回文件字节数组
     */
    public static byte[] getUserBytesFromFile(String fileName) {
        if (fileName == null)
            return null;
        if (!haveSDCard())
            return null;
        File file = new File(getUserIconPath() + fileName + ".png");
        if (!file.exists())
            return null;
        FileInputStream fis;
        byte[] buffer;
        try {
            fis = new FileInputStream(file);
            buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            buffer = null;
        }
        return buffer;
    }
    
    /**
     * 删除文件协议
     * 
     * @param fileName
     *            协议名
     * @return 删除成功则返回true，否则返回false
     */
    public static boolean deleteUserIconFromFile(String fileName) {
        if (fileName == null)
            return false;
        if (!haveSDCard())
            return false;
        File file = new File(getUserIconPath() + fileName + ".png");
        return file.delete();
    }

	/**
	 * 得到协议保存路径
	 * 
	 * @return 协议保存路径
	 */
	private static String getProtocolPath() {
		String protocolPath = null;
		File sdcardPath = android.os.Environment.getExternalStorageDirectory();// sdcard
		protocolPath = sdcardPath.getPath() + Constants.FOLDER_PROTOCEL;
		File protocolPathFile = new File(protocolPath);
		if (!protocolPathFile.exists()) {
			protocolPathFile.mkdirs();
		} else {
		}
		return protocolPath;
	}

	/**
	 * 将协议保存为xml文件
	 * 
	 * @param buffer
	 *            协议字节数组
	 * @param fileName
	 *            协议名称
	 * @return 协议保存成功则返回true，否则返回false
	 */
	public static boolean saveProtocelToFile(byte[] buffer, String fileName) {
		if (!haveSDCard())
			return false;
		File file = new File(getProtocolPath() + fileName + ".xml");
		if (file.exists())
			file.delete();
		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(buffer);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

    /**
     * 获取协议字节数组
     * 
     * @param fileName
     *            协议名
     * @return 返回协议字节数组
     */
    public static byte[] getProtocolFromFile(String fileName) {
        if (fileName == null)
            return null;
        if (!haveSDCard())
            return null;
        File file = new File(getProtocolPath() + fileName + ".xml");
        if (!file.exists())
            return null;
        FileInputStream fis;
        byte[] buffer;
        try {
            fis = new FileInputStream(file);
            buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            buffer = null;
        }
        return buffer;
    }

    /**
     * 删除文件协议
     * 
     * @param fileName
     *            协议名
     * @return 删除成功则返回true，否则返回false
     */
    public static boolean deleteProtocolFromFile(String fileName) {
        if (fileName == null)
            return false;
        if (!haveSDCard())
            return false;
        File file = new File(getProtocolPath() + fileName + ".xml");
        return file.delete();
    }
    
    public static boolean isApkValid(Context ctx, String apkPath) {
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            return false;
        }
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
            Class<?>[] typeArgs = {
                    String.class
            };
            Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = {
                    apkPath
            };
            Object pkgParser = pkgParserCt.newInstance(valueArgs);

            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            typeArgs = new Class<?>[] {
                    File.class, String.class,
                    DisplayMetrics.class, int.class
            };
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage",
                    typeArgs);

            valueArgs = new Object[] {
                    new File(apkPath), apkPath, metrics, 0
            };
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
            if (pkgParserPkg == null) {
                return false;
            }
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
            if (appInfoFld.get(pkgParserPkg) == null) {
                return false;
            }
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);

            Class<?> assetMagCls = Class.forName(PATH_AssetManager);
            Object assetMag = assetMagCls.newInstance();
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath",
                    typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);

            Resources res = ctx.getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor<Resources> resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = (Resources) resCt.newInstance(valueArgs);

            if (info == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getDownloadPath(){
        String downloadPath = null;
        File sdcardPath = android.os.Environment.getExternalStorageDirectory();// sdcard
        downloadPath = sdcardPath.getPath() + Constants.FOLDER_PROTOCEL;
        File downloadPathFile = new File(downloadPath);
        if (!downloadPathFile.exists()) {
            downloadPathFile.mkdirs();
        } else {
        }
        return downloadPath;
    }
    
    // 复制文件
    public static void copyFile(InputStream is, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(is);
            if(targetFile.exists()){
            	targetFile.delete();
            }
            targetFile.createNewFile();
            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

            // 缓冲数组
            byte[] b = new byte[1024];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } finally {
            // 关闭流
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }
    
    
    private static final boolean Use_Also_Data_Dir_Cache_APK_If_Sdcard_Not_Avaiable = true;
    /**
	 * 检查SD状态及记录状态
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean checkSDCardStateAndNote() {
		boolean flag = false;
		// 取得SDCard当前的状态
		String sDcString = Environment.getExternalStorageState();

		String stateDecs = "sdcard state: " + sDcString;
		LogUtil.i("MarketManager", stateDecs);

		// 拥有可读可写权限
		if (sDcString.equals(Environment.MEDIA_MOUNTED)
				|| Use_Also_Data_Dir_Cache_APK_If_Sdcard_Not_Avaiable) {
			flag = true;
		}
		// 已经移除扩展设备
		else if (sDcString.equals(Environment.MEDIA_REMOVED)) {
			// insert_sdcard_first
			String sdFirst = com.byt.market.MyApplication.getInstance()
					.getString(R.string.insert_sdcard_first);
			Toast.makeText(com.byt.market.MyApplication.getInstance(), sdFirst, Toast.LENGTH_SHORT).show();
		}
		// 如果SDCard未挂载,并通过USB大容量存储共享
		else if (sDcString.equals(Environment.MEDIA_SHARED)) {
			// sdcard_using_close_first
			String sdShared = com.byt.market.MyApplication.getInstance()
					.getString(R.string.sdcard_using_close_first);
			Toast.makeText(com.byt.market.MyApplication.getInstance(), sdShared, Toast.LENGTH_SHORT).show();
		}
		// SD卡不可用
		else {
			// sdcard_state_unusual
			String sdOther = com.byt.market.MyApplication.getInstance()
					.getString(R.string.sdcard_state_unusual);
			Toast.makeText(com.byt.market.MyApplication.getInstance(), sdOther, Toast.LENGTH_SHORT).show();
		}

		return flag;
	}
	/**
	  * 递归删除文件和文件夹
	  *
	  * @param file
	  *            要删除的根目录
	  */
   public static void DeleteFile(File file) 
	{
       if (file.exists() == false) 
	    {	   
	        return;
	    } 
	    else
	    {
	        if (file.isFile()) 
	        {
	            file.delete();
	            return;
	        }
	        if (file.isDirectory()) 
	        {
	            File[] childFile = file.listFiles();
	            Log.d("nnlog", childFile.length+"----lenght");
	            if (childFile == null || childFile.length == 0) 
	            {
	                file.delete();
	                return;
	            }
	            for (File f : childFile) 
	            {
	                DeleteFile(f);
	            }
	            file.delete();
	        }
	    }
   }
   
   public static int DeleteFile2(File file) 
  	{
         if (file.exists() == false) 
  	    {	   
  	        return size;
  	    } 
  	    else
  	    {
  	        if (file.isFile()) 
  	        {
  	        	size+=file.length();
  	            file.delete();
  	            return size;
  	        }
  	        if (file.isDirectory()) 
  	        {
  	            File[] childFile = file.listFiles();
  	            if (childFile == null || childFile.length == 0) 
  	            {
  	                file.delete();
  	                return size;
  	            }
  	            for (File f : childFile) 
  	            {
  	                DeleteFile2(f);
  	            }
  	            size+=file.length();
  	            file.delete();
  	        }
  	    }
         return size;
     }
   
   //add by bobo  解压文件 
   public static void Unzip(String zipFile, String targetDir) {
	   int BUFFER = 4096; //这里缓冲区我们使用4KB，
	   String strEntry; //保存每个zip的条目名称

	   try {
	   BufferedOutputStream dest = null; //缓冲输出流
	   FileInputStream fis = new FileInputStream(zipFile);
	   ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
	   ZipEntry entry; //每个zip条目的实例


	   while ((entry = zis.getNextEntry()) != null) {


	   try {
	   int count; 
	   byte data[] = new byte[BUFFER];
	   strEntry = entry.getName();


	   File entryFile = new File(targetDir + strEntry);
	   File entryDir = new File(entryFile.getParent());
	   if (!entryDir.exists()) {
	   entryDir.mkdirs();
	   }


	   FileOutputStream fos = new FileOutputStream(entryFile);
	   dest = new BufferedOutputStream(fos, BUFFER);
	   while ((count = zis.read(data, 0, BUFFER)) != -1) {
	   dest.write(data, 0, count);
	   }
	   dest.flush();
	   dest.close();
	   } catch (Exception ex) {
	   ex.printStackTrace();
	   }
	   }
	   zis.close();
	   } catch (Exception cwj) {
	   cwj.printStackTrace();
	   }
	   }
}
