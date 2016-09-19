package com.byt.market.util;

import java.lang.reflect.Field;
import java.text.NumberFormat;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SystemUtil {
	public static final String ERR_TAG = "SystemUtil.java";

	/**
	 * 判断应用是否可以移动到sdcard
	 */
	public static boolean getPhoneMoveApp(ApplicationInfo applicationInfo,
			Context context) {
		PackageManager mPm;
		String pkgName = applicationInfo.packageName;
		boolean dataOnly = false;
		PackageInfo pkgInfo = null;
		int flag = -1001;
		try {
			mPm = context.getPackageManager();
			pkgInfo = mPm.getPackageInfo(pkgName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			if (pkgInfo == null)
				return false;
			Field fields = pkgInfo.getClass().getField("installLocation");
			if (fields == null)
				return false;
			flag = fields.getInt(pkgInfo);
			dataOnly = (pkgInfo == null) && (applicationInfo != null);
			if (dataOnly) {
				return false;
			} else {
				if ((applicationInfo.flags & 1 << 20) == 0
						&& (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
						&& pkgInfo != null) {
					if (flag == 2 || flag == 0) {
						return true;
					}
				} else {
					return false;
				}
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public static String dataFormat(double data) {
		double d = data >= 0 ? data : 0 - data;

		String buffer = "";

		NumberFormat df = NumberFormat.getNumberInstance();
		df.setMaximumFractionDigits(1);

		String unitStr = "B";

		int value;

		if (d < 1000) {
			buffer = d + "";
		} else if (d < 1024000) {
			buffer = df.format(((double) d) / 1024) + "";
			unitStr = "K";
		} else if (d < 1048576000) {
			buffer = df.format(((double) d) / 1048576) + "";
			unitStr = "M";
		} else {
			buffer = df.format(((double) d) / 1073741824) + "";
			unitStr = "G";
		}

		/* 优化数字数值大于100小数点后的数字就隐去 */
		if (buffer.length() >= 5) {
			if ((value = buffer.indexOf(".")) > 0) {
				buffer = buffer.toString().substring(0, value);
			}
		}

		return buffer + unitStr;
	}

	/**
	 * 格式化流量排行流量大小,要求：B K M 都是取整 1B 1K 1M GB 流量除外 1.2G
	 */
	public static String formatFlowRankingSize(double data) {
		double d = data >= 0 ? data : 0 - data;

		String buffer = "";

		NumberFormat df = NumberFormat.getNumberInstance();
		df.setMaximumFractionDigits(0);

		String unitStr = "B";

		int value;

		if (d < 1000) {
			buffer = df.format(d) + "";
		} else if (d < 1024000) {
			buffer = df.format(((double) d) / 1024) + "";
			unitStr = "K";
		} else if (d < 1048576000) {
			buffer = df.format(((double) d) / 1048576) + "";
			unitStr = "M";
		} else {
			df.setMaximumFractionDigits(1);
			buffer = df.format(((double) d) / 1073741824) + "";
			unitStr = "G";
		}

		/* 优化数字数值大于100小数点后的数字就隐去 */
		if (buffer.length() >= 5) {
			if ((value = buffer.indexOf(".")) > 0) {
				buffer = buffer.toString().substring(0, value);
			}
		}

		return buffer + unitStr;
	}

	/**
	 * @Title: formatSize
	 * @Description:
	 * @param size
	 *            需要格式化的数据
	 * @param flag
	 *            需保留小数点位数
	 * @return
	 * @author wuxu
	 * @date 2011-12-6
	 */
	public static String formatSize(long size, int flag) {
		NumberFormat df = NumberFormat.getNumberInstance();
		df.setMaximumFractionDigits(flag);

		StringBuffer buffer = new StringBuffer();
		if (size < 0) {
			buffer.append(0);
			buffer.append("B");
		} else if (size < 1000) {
			buffer.append(size);
			buffer.append("B");
		} else if (size < 1024000) {
			buffer.append(df.format(((double) size) / 1024));
			buffer.append("K");
		} else if (size < 1048576000) {
			buffer.append(df.format(((double) size) / 1048576));
			buffer.append("M");
		} else {
			buffer.append(df.format(((double) size) / 1073741824));
			buffer.append("G");
		}
		return buffer.toString();
	}

	public static void setGpsStatue(Context context) {
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
		gpsIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}

	public static String getRandomNum(final int n) {
		String str = "";
		for (int i = 0; i < n; i++) {
			// str = str + (char) (Math.random() * 26 + 'a'); // 注意一定要加 (char)
			// 把他转换成单个字符
			str = str + (int) (Math.random() * 10);
		}
		return str;
	}

	/**
	 * @Title: isExit
	 * @Description:
	 * @param context
	 *            对象，以获取PackageManager
	 * @param 软件包名
	 * @return 软件存在 true，否则 false
	 * @author wuxu
	 * @date 2011-11-17
	 */
	public static boolean isExist(Context context, String pname) {
		try {
			context.getPackageManager().getApplicationInfo(pname, 0);
			return true;
		} catch (Exception e) {
			// ErrorReport.outException(e);
			return false;
		}

	}

	/**
	 * @Title: isExistAndVersion
	 * @Description: 对应的versioncode 的报名是否存在
	 * @param context
	 *            对象，以获取PackageManager
	 * @param 软件包名
	 * @return 软件存在 true，否则 false
	 * @author wuxu
	 * @date 2011-11-17
	 */
	public static boolean isExistAndVersion(Context context, String pname,
			int oldvesioncode) {
		try {
			if (SystemUtil.isExist(context, pname)) {
				try {
					PackageInfo p = context.getPackageManager().getPackageInfo(
							pname, 0);
					if (p.versionCode < oldvesioncode)
						return false;
					else
						return true;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

			} else {
				return false;
			}
		} catch (Exception e) {
			// ErrorReport.outException(e);
			return false;
		}
		return false;

	}

	//
	public static boolean isScreenNotLocked(Context con) {
		KeyguardManager mKeyguardManager = (KeyguardManager) con
				.getSystemService(Context.KEYGUARD_SERVICE);
		return !mKeyguardManager.inKeyguardRestrictedInputMode();
	}

	/**
	 * @Title: inflateView
	 * @Description: 将xml转化为View
	 * @param resource
	 *            需要转化的xml
	 * @return 返回转化好的View
	 * @author wuxu
	 * @date 2011-11-23
	 */
	public static View inflateView(Context context, int resource) {
		LayoutInflater vi = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return vi.inflate(resource, null, false);
	}

	/**
	 * @Title: setTextBold
	 * @Description: 文字加粗
	 * @date 2012-5-25
	 */
	static public void setTextBold(TextView textView) {
		TextPaint paint = textView.getPaint();
		paint.setFakeBoldText(true);
	}

	/**
	 * 根据包名得到程序图标
	 */
	public static Drawable getAppIcon(Context context, String packname) {
		PackageManager packageManager = context.getPackageManager();
		try {
			ApplicationInfo info = packageManager.getApplicationInfo(packname,
					0);
			return resizeDrawable(info.loadIcon(packageManager), 2);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 根据包名得到程序名称
	 */
	public static String getAppName(Context context, String packname) {
		PackageManager packageManager = context.getPackageManager();
		try {
			ApplicationInfo info = packageManager.getApplicationInfo(packname,
					0);
			if (info == null) {
				return null;
			}
			CharSequence label = info.loadLabel(packageManager);
			return TextUtils.isEmpty(label) ? "" : label.toString();
		} catch (Exception e) {
			return null;
		}
	}

	public static final int DRAWABLE_SIZE_TYPE_ZERO = 0;
	public static final int DRAWABLE_SIZE_TYPE_ONE = 1;
	public static final int DRAWABLE_SIZE_TYPE_TWO = 2;
	public static final int DRAWABLE_SIZE_TYPE_THREE = 3;
	public static final int DRAWABLE_SIZE_TYPE_FOUR = 4;

	public static Drawable resizeDrawable(Drawable mDrawable, int type) {
		Drawable mNewIcon = null;
		try {
			if (mDrawable == null)
				return mNewIcon;
			if (!(mDrawable instanceof BitmapDrawable)) {
				return mDrawable;
			}
			Bitmap mBitmap = (Bitmap) ((BitmapDrawable) mDrawable).getBitmap();
			if (mBitmap == null || mBitmap.isRecycled()) {
				return mNewIcon;
			}

			if (mBitmap.getWidth() > 768 || mBitmap.getHeight() > 768) {// 处理异常尺寸
				mBitmap = null;
				return null;
			}
			switch (type) {
			case DRAWABLE_SIZE_TYPE_ZERO:
				mNewIcon = new BitmapDrawable(Bitmap.createScaledBitmap(
						mBitmap, 30, 30, false));

				break;
			case DRAWABLE_SIZE_TYPE_ONE:
				if (mBitmap.getWidth() > 48 && mBitmap.getHeight() > 48) {
					// Log.i("ydy","SystemUtil resizeDrawable DRAWABLE_SIZE_TYPE_ONE");
					mNewIcon = new BitmapDrawable(Bitmap.createScaledBitmap(
							mBitmap, 48, 48, false));
				}
				break;
			case DRAWABLE_SIZE_TYPE_TWO:
				if (mBitmap.getWidth() > 96 && mBitmap.getHeight() > 96) {
					// Log.i("ydy","SystemUtil resizeDrawable DRAWABLE_SIZE_TYPE_TWO");
					mNewIcon = new BitmapDrawable(Bitmap.createScaledBitmap(
							mBitmap, 96, 96, false));
				}
				break;
			case DRAWABLE_SIZE_TYPE_THREE:
				if (mBitmap.getWidth() > 144 && mBitmap.getHeight() > 144) {
					mNewIcon = new BitmapDrawable(Bitmap.createScaledBitmap(
							mBitmap, 144, 144, false));
				}
				break;
			case DRAWABLE_SIZE_TYPE_FOUR:
				if (mBitmap.getWidth() > 256 && mBitmap.getHeight() > 256) {
					mNewIcon = new BitmapDrawable(Bitmap.createScaledBitmap(
							mBitmap, 256, 256, false));
				}
				break;
			default:
				break;
			}
			// mBitmap.recycle();
			mBitmap = null;
			if (mNewIcon == null)
				mNewIcon = mDrawable;
			mDrawable = null;
			return mNewIcon;
		} catch (Exception e) {
			e.printStackTrace();
			mNewIcon = mDrawable;
		}
		return mNewIcon;
	}

	/**
	 * 弹出一个自定义Toast
	 */
	public static void showToast(Context context, int titleResId) {
		if (titleResId <= 0 || context == null
				|| context.getResources() == null) {
			return;
		}
		showToast(context, context.getResources().getString(titleResId));
	}

	public static void showToast(Context context, String title) {
		if (title == null || context == null || context.getResources() == null) {
			return;
		}
		// LayoutInflater inflater = LayoutInflater.from(context);
		// View layout = inflater.inflate(R.layout.my_toast, null);
		// TextView text = (TextView) layout.findViewById(R.id.tv_toast_title);
		// text.setText(title);

		// Toast toast = new Toast(context);
		// toast.setDuration(Toast.LENGTH_SHORT);
		// toast.setView(layout);
		// toast.show();
		// Toast.makeText(context, title, Toast.LENGTH_SHORT).show();
		showToast(context, title, -1);
	}

	public static void showToast(Context context, String title, int location) {
		if (title == null || context == null || context.getResources() == null) {
			return;
		}
		Toast toast = Toast.makeText(context, title, Toast.LENGTH_SHORT);
		if (location > 0)
			toast.setGravity(location, 0, 0);
		toast.show();
	}

	/**
	 * 将dip转化为px
	 */
	public static int dip2px(Context context, float dipValue) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * 将px转化成dip
	 */
	public static int px2dip(Context context, float pxValue) {

		final float scale = context.getResources().getDisplayMetrics().density;

		return (int) (pxValue / scale + 0.5f);
	}

	public static Drawable bitmapToDrawable(Bitmap bitmap) {
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		return bd;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (null == drawable) {
			return null;
		}
		BitmapDrawable bd = (BitmapDrawable) drawable;
		return bd.getBitmap();
	}

	/**
	 * 拨打电话,但是不自动拨打
	 */
	public static void makeCall(Context context, String phoneNumber) {
		if (TextUtils.isEmpty(phoneNumber) || context == null) {
			return;
		}
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:" + phoneNumber));
		context.startActivity(intent);
	}

}
