package com.byt.market.oauth2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.byt.market.util.LogUtil;

/**
 * 新浪微博工具类
 * 
 * @author cstdingran@gmail.com
 * 
 */
public class SinaWeiboUtil {/*

	private static final String TAG = "SinaWeiboUtil";

	private static Context mContext;

	private static SinaWeiboUtil mInstantce;

	private static Weibo mWeibo;

	*//** 保存token等参数 **//*
	private static SinaAccessToken mAccessToken;

	*//** 调用SSO授权 **//*
	private static SsoHandler mSsoHandler;

	private WeiboListener listener;
	
	*//**
	 * 授权回调类
	 *//*
	public class WeiboListener {

		public void init(boolean isValid) {
		}

		public void onResult() {
		}
	}
	
	public SinaWeiboUtil() {
		mWeibo = Weibo.getInstance(SinaOauth2.APP_KEY, SinaOauth2.REDIRECT_URL, SinaOauth2.SCOPE);
	}

	public static SinaWeiboUtil getInstance(Context context) {
		mContext = context;
		if (mInstantce == null) {
			mInstantce = new SinaWeiboUtil();
		}
		return mInstantce;
	}

	*//**
	 * 初始化新浪微博
	 * 
	 * @param l
	 *            授权是否过期回调函数
	 *//*
	public void initSinaWeibo(WeiboListener l) {
		mAccessToken = SinaAccessTokenKeeper.readAccessToken(mContext);

		if (mAccessToken.isSessionValid()) { // 判断是否已授权
			l.init(true);
		} else {
			l.init(false);
		}
	}

	*//**
	 * SSO授权
	 * 
	 * @param l
	 *//*
	public void auth(WeiboListener l) {
		// SSO授权
		mSsoHandler = new SsoHandler((Activity) mContext, mWeibo);
		mSsoHandler.authorize(new AuthDialogListener());

		listener = l;

		// test 网页授权
		// mWeibo.authorize(mContext, new AuthDialogListener());
	}

	*//**
	 * 授权回调函数
	 *//*
	class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onCancel() {
			LogUtil.d(TAG, "===================AuathDialogListener=Auth cancel==========");
			showToast(mContext, "取消授权操作。");
		}

		@Override
		public void onComplete(Bundle values) {
			LogUtil.d(TAG, "===================AuthDialogListener=onComplete==========");
			for (String key : values.keySet()) {
				LogUtil.d(TAG, "values:key = " + key + " value = " + values.getString(key));
			}
			String code = values.getString("code");
			getAccessTokenByCode(code);
		}

		@Override
		public void onError(WeiboDialogError e) {
			LogUtil.d(TAG, "===================AuthDialogListener=onError=WeiboDialogError = " + e.getMessage());
			showToast(mContext, "授权失败，请检查网络连接。出错信息：" + e.getMessage());
		}

		@Override
		public void onWeiboException(WeiboException e) {
			LogUtil.d(TAG, "===================AuthDialogListener=onWeiboException=WeiboException = " + e.getMessage());
			showToast(mContext, "授权失败，请检查网络连接。出错信息：" + e.getMessage());
		}
	}

	*//**
	 * SSO授权回调函数
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 *//*
	public void authCallBack(int requestCode, int resultCode, Intent data) {
		if (mSsoHandler != null) {
			LogUtil.d(TAG, "=====onActivityResult=mSsoHandler resultCode = " + resultCode + " requestCode = "
					+ requestCode);
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	*//**
	 * 根据code获取AccessToken
	 * 
	 * @param code
	 *//*
	private void getAccessTokenByCode(String code) {
		SinaWeiboAPI api = new SinaWeiboAPI(mAccessToken);
		api.getAccessTokenByCode(code, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				LogUtil.d(TAG,
						"===================getAccessTokenByCode=onIOException=WeiboDialogError = " + e.getMessage());
				showToast(mContext, "根据code获取AccessToken失败，请检查网络连接。出错信息：" + e.getMessage());
			}

			@Override
			public void onError(WeiboException e) {
				LogUtil.d(TAG, "===================getAccessTokenByCode=onError=WeiboDialogError = " + e.getMessage());
				showToast(mContext, "根据code获取AccessToken失败，请检查网络连接。出错信息：" + e.getMessage());
			}

			@Override
			public void onComplete4binary(ByteArrayOutputStream arg0) {
			}

			@Override
			public void onComplete(String json) {
				LogUtil.d(TAG, "===================getAccessTokenByCode=onComplete==========");
				LogUtil.d(TAG, "json = " + json);
				JSONObject o;
				try {
					o = new JSONObject(json);
					String access_token = o.getString("access_token");
	                String express_in = o.getString("expires_in");
	                String uid = o.getString("uid");
	                // 这里按照 毫秒计算
	                long time = Long.parseLong(express_in) * 1000 + System.currentTimeMillis();
	                mAccessToken = new SinaAccessToken();
	                mAccessToken.setExpiresTime(time);
	                mAccessToken.setToken(access_token);
	                mAccessToken.setUid(uid);
	                
					if (mAccessToken.isSessionValid()) {
						SinaAccessTokenKeeper.keepAccessToken(mContext, mAccessToken);
//						PreferenceUtil.getInstance(mContext).saveString(Constants.PREF_SINA_ACCESS_TOKEN, token);
//						PreferenceUtil.getInstance(mContext).saveString(Constants.PREF_SINA_UID, uid);
//						PreferenceUtil.getInstance(mContext).saveLong(Constants.PREF_SINA_EXPIRES_TIME,
//								mAccessToken.getExpiresTime()); // 存入的是到期时间
//						PreferenceUtil.getInstance(mContext).saveString(Constants.PREF_SINA_REMIND_IN, remindIn);
//						show(Long.parseLong(uid));
//						LogUtil.d(TAG, "isSessionValid~~~~~~~token = " + token + " uid = " + uid + " expiresIn = "
//								+ expiresIn + " remindIn = " + remindIn);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	*//**
	 * 网页授权时，需要单独获取UserName
	 * 
	 * @param uid
	 *//*
	public void show(long uid) {
		SinaWeiboAPI api = new SinaWeiboAPI(mAccessToken);
		api.show(uid, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				LogUtil.d(TAG, "onIOException---e = " + e.getMessage());
				showToast(mContext, "获取UserName失败，请检查网络连接。出错信息：" + e.getMessage());
			}

			@Override
			public void onError(WeiboException e) {
				LogUtil.d(TAG, "WeiboException---e = " + e.getMessage());
				showToast(mContext, "获取UserName失败，请检查网络连接。出错信息：" + e.getMessage());
			}

			@Override
			public void onComplete(String json) {
				JSONObject object;
				try {
					object = new JSONObject(json);
					String userName = object.optString("screen_name");
					LogUtil.d(TAG, "show---onComplete---userName = " + userName);
					if (listener != null) {
						listener.onResult();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onComplete4binary(ByteArrayOutputStream arg0) {
			}
		});
	}

	*//**
	 * 发布一条新微博(连续两次发布的微博不可以重复)
	 * 
	 * @param content
	 *            要发布的微博文本内容，内容不超过140个汉字。
	 * @param lat
	 *            纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0。
	 * @param lon
	 *            经度，有效范围：-180.0到+180.0，+表示东经，默认为0.0。
	 * @param listener
	 *//*
	public void update(String content, String lat, String lon) {
		SinaWeiboAPI api = new SinaWeiboAPI(mAccessToken);
		api.update(content, lat, lon, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				LogUtil.d(TAG, "onIOException---e = " + e.getMessage());
				showToast(mContext, "分享失败，请检查网络连接。出错信息：" + e.getMessage());
			}

			@Override
			public void onError(WeiboException e) {
				LogUtil.d(TAG, "onError---e = " + e.getMessage() + " e.getStatusCode() = " + e.getStatusCode());
				if (e.getStatusCode() == 400) { // 相同内容短时间内不能分享
					showToast(mContext, "分享失败，相同内容短时间内不能分享，请稍候再试吧。出错信息：" + e.getMessage());
				} else {
					showToast(mContext, "分享失败，请检查网络连接。出错信息：" + e.getMessage());
				}
			}

			@Override
			public void onComplete(String str) {
				LogUtil.d(TAG, "onComplete---str = " + str);
				showToast(mContext, "分享成功，去你绑定的新浪微博看看吧！");
			}

			@Override
			public void onComplete4binary(ByteArrayOutputStream arg0) {
			}
		});
	}

	*//**
	 * 指定一个图片URL地址抓取后上传并同时发布一条新微博
	 * 
	 * @param content
	 *            要发布的微博文本内容，内容不超过140个汉字
	 * @param url
	 *            图片的URL地址，必须以http开头。
	 * @param lat
	 *            纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0。
	 * @param lon
	 *            经度，有效范围：-180.0到+180.0，+表示东经，默认为0.0。
	 *//*
	public void uploadUrlText(String content, String url, String lat, String lon) {
		SinaWeiboAPI api = new SinaWeiboAPI(mAccessToken);
		api.uploadUrlText(content, url, lat, lon, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				LogUtil.d(TAG, "onIOException---e = " + e.getMessage());
				showToast(mContext, "分享失败，请检查网络连接。出错信息：" + e.getMessage());
			}

			@Override
			public void onError(WeiboException e) {
				LogUtil.d(TAG, "onError---e = " + e.getMessage() + " e.getStatusCode() = " + e.getStatusCode());
				if (e.getStatusCode() == 400) { // 相同内容短时间内不能分享，判断还以促出错信息为准
					showToast(mContext, "分享失败，相同内容短时间内不能分享，请稍候再试吧。出错信息：" + e.getMessage());
				} else {
					showToast(mContext, "分享失败，请检查网络连接。出错信息：" + e.getMessage());
				}
			}

			@Override
			public void onComplete(String str) {
				LogUtil.d(TAG, "onComplete---str = " + str);
				showToast(mContext, "分享成功，去你绑定的新浪微博看看吧！");
			}

			@Override
			public void onComplete4binary(ByteArrayOutputStream arg0) {
			}
		});
	}

	*//**
	 * 上传图片并发布一条新微博，此方法会处理urlencode
	 * 
	 * @param content
	 *            要发布的微博文本内容，内容不超过140个汉字
	 * @param file
	 *            要上传的图片，仅支持JPEG、GIF、PNG格式，图片大小小于5M。
	 * @param lat
	 *            纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0。
	 * @param lon
	 *            经度，有效范围：-180.0到+180.0，+表示东经，默认为0.0。
	 *//*
	public void upload(String content, String file, String lat, String lon) {
		SinaWeiboAPI api = new SinaWeiboAPI(mAccessToken);
		api.upload(content, file, lat, lon, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				LogUtil.d(TAG, "onIOException---e = " + e.getMessage());
				showToast(mContext, "分享失败，请检查网络连接。出错信息：" + e.getMessage());
			}

			@Override
			public void onError(WeiboException e) {
				LogUtil.d(TAG, "onError---e = " + e.getMessage() + " e.getStatusCode() = " + e.getStatusCode());
				if (e.getStatusCode() == 400) { // 相同内容短时间内不能分享，判断还以促出错信息为准
					showToast(mContext, "分享失败，相同内容短时间内不能分享，请稍候再试吧。出错信息：" + e.getMessage());
				} else {
					showToast(mContext, "分享失败，请检查网络连接。出错信息：" + e.getMessage());
				}
			}

			@Override
			public void onComplete(String str) {
				LogUtil.d(TAG, "onComplete---str = " + str);
				showToast(mContext, "分享成功，去你绑定的新浪微博看看吧！");
			}

			@Override
			public void onComplete4binary(ByteArrayOutputStream arg0) {
			}
		});
	}

	*//**
	 * 注销授权
	 * 
	 * @param l
	 *//*
	public void logout(final WeiboListener l) {
		SinaWeiboAPI api = new SinaWeiboAPI(mAccessToken);
		api.endSession(new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				LogUtil.d(TAG, "onIOException---e = " + e.getMessage());
				showToast(mContext, "注销授权失败，请检查网络连接。出错信息：" + e.getMessage());
			}

			@Override
			public void onError(WeiboException e) {
				LogUtil.d(TAG, "onError---e = " + e.getMessage());
				showToast(mContext, "注销授权失败，请检查网络连接。出错信息：" + e.getMessage());
			}

			@Override
			public void onComplete4binary(ByteArrayOutputStream arg0) {
			}

			@Override
			public void onComplete(String arg0) {
				SinaAccessTokenKeeper.clear(mContext);
				l.onResult();
			}
		});
	}

	*//**
	 * 检查是否已授权
	 * 
	 * @return true 已授权，false 未授权
	 *//*
	public boolean isAuth() {
		mAccessToken = SinaAccessTokenKeeper.readAccessToken(mContext);
		if (mAccessToken.isSessionValid()) { // 判断是否已授权
			String date = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new java.util.Date(mAccessToken
					.getExpiresTime()));
			LogUtil.d(TAG, "access_token 仍在有效期内,无需再次登录: \naccess_token:" + mAccessToken.getToken() + "\n有效期：" + date);
			return true;
		}
		LogUtil.d(TAG, "access_token 过期");
		return false;
	}
	
	private void showToast(Context context, String text){
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

*/}
