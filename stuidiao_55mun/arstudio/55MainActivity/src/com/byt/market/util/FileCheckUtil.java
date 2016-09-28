package com.byt.market.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.byt.market.Constants;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;

public class FileCheckUtil {
	public static void checkApkFile(Context context, String path){
		PackageManager packageManager = context.getPackageManager();
		
	}
	
	/** 
	 * ��ȡδ��װ��apk��Ϣ 
	 *  
	 * @param ctx Context
	 * @param apkPath apk·�������Է���SD��
	 * @return 
	 */  
	 public static long getApkFileInfo(Context ctx, String apkPath) 
	 {  
	     System.out.println(apkPath);  
	     File apkFile = new File(apkPath);  
	     if (!apkFile.exists())
	     {  
	         System.out.println("file path is not correct");  
	         return 0;  
	     }  
	     
	     String PATH_PackageParser = "android.content.pm.PackageParser";  
	     String PATH_AssetManager = "android.content.res.AssetManager";  
	     try
	     {  
	         //����õ�pkgParserCls����ʵ��,�в���  
	         Class<?> pkgParserCls = Class.forName(PATH_PackageParser);  
	         Class<?>[] typeArgs = {String.class};  
	         Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);  
	         Object[] valueArgs = {apkPath};  
	         Object pkgParser = pkgParserCt.newInstance(valueArgs);  
	         
	         //��pkgParserCls��õ�parsePackage����  
	         DisplayMetrics metrics = new DisplayMetrics();  
	         metrics.setToDefaults();//���������ʾ�йص�, ���ʹ��Ĭ��  
	         typeArgs = new Class<?>[]{File.class,String.class,  
	         DisplayMetrics.class,int.class};  
	         Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);  
	         
	         valueArgs=new Object[]{new File(apkPath),apkPath,metrics,0};  
	         
	         //ִ��pkgParser_parsePackageMtd����������  
	         Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);  
	         
	         //�ӷ��صĶ���õ���Ϊ"applicationInfo"���ֶζ���   
	         if (pkgParserPkg==null)
	         {  
	             return 0;  
	         }  
	         Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");  
	         
	         //�Ӷ���"pkgParserPkg"�õ��ֶ�"appInfoFld"��ֵ  
	         if (appInfoFld.get(pkgParserPkg)==null)
	         {  
	             return 0;  
	         }  
	         ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);     
	         
	         //����õ�assetMagCls����ʵ��,�޲�  
	         Class<?> assetMagCls = Class.forName(PATH_AssetManager);     
	         Object assetMag = assetMagCls.newInstance();  
	         //��assetMagCls��õ�addAssetPath����  
	         typeArgs = new Class[1];  
	         typeArgs[0] = String.class;  
	         Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);  
	         valueArgs = new Object[1];  
	         valueArgs[0] = apkPath;  
	         //ִ��assetMag_addAssetPathMtd����  
	         assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);  
	         
	         //�õ�Resources����ʵ��,�в���  
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
	         //������ص�
	         //�õ�Resource���������кܶ��ô�
	         res = (Resources) resCt.newInstance(valueArgs);  
	         
	         // ��ȡapk�ļ�����Ϣ  
	         if (info!=null)
	         {  
	             if (info.icon != 0) 
	             {
	                 // ͼƬ���ڣ����ȡ�����Ϣ  
	                 Drawable icon = res.getDrawable(info.icon);// ͼ��  
//	                 appInfoData.setAppicon(icon);  
	             }  
	             if (info.labelRes != 0) 
	             {  
	                 String neme = (String) res.getText(info.labelRes);// ����  
//	                 appInfoData.setAppname(neme);  
	             }else 
	             {  
	                 String apkName=apkFile.getName();  
//	                 appInfoData.setAppname(apkName.substring(0,apkName.lastIndexOf(".")));  
	             }  
	             String pkgName = info.packageName;// ����     
//	             appInfoData.setApppackage(pkgName);  
	         }
	         else 
	         {  
	             return 0;  
	         }     
	         PackageManager pm = ctx.getPackageManager();  
	         PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);  
	         if (packageInfo != null)
	         {  
//	             appInfoData.setAppversion(packageInfo.versionName);//�汾��  
//	             appInfoData.setAppversionCode(packageInfo.versionCode+"");//�汾��  
	         }  
	         return apkFile.length();  
	     } catch (Exception e)
	     {   
	         e.printStackTrace();  
	     }  
	     return 0;  
	 }
	 
	 

		/** 
		 * ��ȡδ��װ��apk�İ汾��
		 *  
		 * @param ctx Context
		 * @param apkPath apk·�������Է���SD��
		 * @return 
		 */  
		 public static int getApkFileVcode(Context ctx, String apkPath) 
		 {  
		     System.out.println(apkPath);  
		     File apkFile = new File(apkPath);  
		     if (!apkFile.exists())
		     {  
		         System.out.println("file path is not correct");  
		         return -1;  
		     }  
		     
		     String PATH_PackageParser = "android.content.pm.PackageParser";  
		     String PATH_AssetManager = "android.content.res.AssetManager";  
		     try
		     {  
		         //����õ�pkgParserCls����ʵ��,�в���  
		         Class<?> pkgParserCls = Class.forName(PATH_PackageParser);  
		         Class<?>[] typeArgs = {String.class};  
		         Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);  
		         Object[] valueArgs = {apkPath};  
		         Object pkgParser = pkgParserCt.newInstance(valueArgs);  
		         
		         //��pkgParserCls��õ�parsePackage����  
		         DisplayMetrics metrics = new DisplayMetrics();  
		         metrics.setToDefaults();//���������ʾ�йص�, ���ʹ��Ĭ��  
		         typeArgs = new Class<?>[]{File.class,String.class,  
		         DisplayMetrics.class,int.class};  
		         Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);  
		         
		         valueArgs=new Object[]{new File(apkPath),apkPath,metrics,0};  
		         
		         //ִ��pkgParser_parsePackageMtd����������  
		         Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);  
		         
		         //�ӷ��صĶ���õ���Ϊ"applicationInfo"���ֶζ���   
		         if (pkgParserPkg==null)
		         {  
		             return -1;  
		         }  
		         Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");  
		         
		         //�Ӷ���"pkgParserPkg"�õ��ֶ�"appInfoFld"��ֵ  
		         if (appInfoFld.get(pkgParserPkg)==null)
		         {  
		             return -1;  
		         }  
		         ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);     
		         
		         //����õ�assetMagCls����ʵ��,�޲�  
		         Class<?> assetMagCls = Class.forName(PATH_AssetManager);     
		         Object assetMag = assetMagCls.newInstance();  
		         //��assetMagCls��õ�addAssetPath����  
		         typeArgs = new Class[1];  
		         typeArgs[0] = String.class;  
		         Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);  
		         valueArgs = new Object[1];  
		         valueArgs[0] = apkPath;  
		         //ִ��assetMag_addAssetPathMtd����  
		         assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);  
		         
		         //�õ�Resources����ʵ��,�в���  
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
		         //������ص�
		         //�õ�Resource���������кܶ��ô�
		         res = (Resources) resCt.newInstance(valueArgs);  
		         
		         // ��ȡapk�ļ�����Ϣ  
		         if (info!=null)
		         {  
		             if (info.icon != 0) 
		             {
		                 // ͼƬ���ڣ����ȡ�����Ϣ  
		                 Drawable icon = res.getDrawable(info.icon);// ͼ��  
//		                 appInfoData.setAppicon(icon);  
		             }  
		             if (info.labelRes != 0) 
		             {  
		                 String neme = (String) res.getText(info.labelRes);// ����  
//		                 appInfoData.setAppname(neme);  
		             }else 
		             {  
		                 String apkName=apkFile.getName();  
//		                 appInfoData.setAppname(apkName.substring(0,apkName.lastIndexOf(".")));  
		             }  
		             String pkgName = info.packageName;// ����     
//		             appInfoData.setApppackage(pkgName);  
		         }
		         else 
		         {  
		             return 0;  
		         }     
		         PackageManager pm = ctx.getPackageManager();  
		         PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);  
		         if (packageInfo != null)
		         {  
		        	 return packageInfo.versionCode;
//		             appInfoData.setAppversion(packageInfo.versionName);//�汾��  
//		             appInfoData.setAppversionCode(packageInfo.versionCode+"");//�汾��  
		         }  
		         return -1;  
		     } catch (Exception e)
		     {   
		         e.printStackTrace();  
		     }  
		     return -1;  
		 }
		 /**
		  * 清楚缓存
		  * BY BOBO
		  */
		 public static void cleraCacel(Context context,Handler handler){
			 new Thread(new MycleraCacellisenler(context,handler)).start();
		 }
		static class MycleraCacellisenler implements Runnable{
			 
			 private Context mcontext;
			 private Handler mhandler;
			 private int msize = 0;
			 public MycleraCacellisenler(Context context,Handler handler){
				 this.mcontext = context;
				 this.mhandler = handler;
			 }
			@Override
			public void run() {
				// TODO Auto-generated method stub
				File[] dirs = new File[]{new File(Environment.getExternalStorageDirectory()+"/com.byt.market"),
						mcontext.getExternalCacheDir(),mcontext.getCacheDir()};
				//ArrayList<File> list = new ArrayList<File>();
				for (File file : dirs) {
					msize+= FileUtil.DeleteFile2(file);
				}
				Message msg = mhandler.obtainMessage();
				msg.arg1 = msize;
				FileUtil.size = 0;
				mhandler.sendMessage(msg);
			}
			 
		 }
		
}
