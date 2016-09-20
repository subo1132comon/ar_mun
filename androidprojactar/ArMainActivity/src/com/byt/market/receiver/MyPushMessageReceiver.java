package com.byt.market.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 百度云推送
 */
public class MyPushMessageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {

	}/*
	 * 
	 * @Override public void onBind(Context context, int errorCode, String
	 * appid, String userId, String channelId, String requestId) {
	 * LogUtil.e("cexo", "onBind()"); }
	 * 
	 * @Override public void onDelTags(Context context, int errorCode,
	 * List<String> sucessTags, List<String> failTags, String requestId) {
	 * LogUtil.e("cexo", "onDelTags()"); }
	 * 
	 * @Override public void onListTags(Context context, int errorCode,
	 * List<String> tags, String requestId) { LogUtil.e("cexo", "onListTags()");
	 * }
	 * 
	 * @Override public void onMessage(Context context, String message, String
	 * customContentString ) { LogUtil.e("cexo", "onMessage()"); JSONTokener
	 * token = null; JSONObject ret = null;
	 * 
	 * token = new JSONTokener (message); try { ret = (JSONObject) token
	 * .nextValue(); PushMessageInfo pushMessageInfo = new PushMessageInfo ();
	 * pushMessageInfo .setTitle (ret.optString ("title")); pushMessageInfo
	 * .setDesc (ret.optString ("description")); pushMessageInfo .setSid
	 * (ret.optInt ("sid")); pushMessageInfo .setUrl (ret.optString ("url"));
	 * pushMessageInfo .setChannelName (ret .optString("channel" ));
	 * pushMessageInfo .setGotoType (ret.optInt ("gototype")); pushMessageInfo
	 * .setVersionCode (ret .optInt("versioncode" )); showNotification (context,
	 * pushMessageInfo); } catch (Exception e) { } }
	 * 
	 * private void showNotification (Context context, PushMessageInfo
	 * pushMessageInfo) { // 渠道检测 if (!TextUtils .isEmpty (pushMessageInfo
	 * .getChannelName ())) { String myChannel = Util.getChannelName
	 * (MyApplication. getInstance()); if (!TextUtils.isEmpty (myChannel)) { if
	 * ( !myChannel.equals (pushMessageInfo. getChannelName ())) { return; } } }
	 * // 客户端版本号检测 if (pushMessageInfo .getVersionCode() > 0) { int myVersion =
	 * Integer .parseInt(Util .getVcode (MyApplication .getInstance())); if
	 * (myVersion != pushMessageInfo .getVersionCode ()) { return; } }
	 * LogUtil.e("cexo", "showPushNotification()" ); NotificationManager
	 * notificationManager = (NotificationManager ) context .getSystemService
	 * ("notification"); notificationManager .cancel(1); Notification
	 * notification = new Notification(R .drawable.icon, pushMessageInfo
	 * .getDesc(), System .currentTimeMillis ()); Intent intent = null; switch
	 * (pushMessageInfo .gotoType) { case Constants .GOTYPE.GO_HOME: // intent =
	 * new Intent(context, HomeActivity .class); break; case Constants.GOTYPE
	 * .GO_GAME_DETAIL: intent = new Intent(context, DetailFrame2 .class);
	 * intent.putExtra (Constants .ExtraKey.SID, pushMessageInfo .getSid());
	 * break; case Constants .GOTYPE.GO_MY_GAME : // intent = new
	 * Intent(context, MainActivity .class); // intent .putExtra(Constants .
	 * ExtraKey.TAB_INDEX , 1); break; case Constants .GOTYPE.GO_BROWER : if
	 * (!TextUtils.isEmpty (pushMessageInfo. getUrl())) { Uri uri = Uri.parse(
	 * pushMessageInfo .getUrl()); intent = new Intent (Intent.ACTION_VIEW ,
	 * uri); } break; } if (intent != null) { intent.addFlags (Intent.
	 * FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY ); intent.addFlags (Intent.
	 * FLAG_ACTIVITY_BROUGHT_TO_FRONT ); PendingIntent pendingIntent =
	 * PendingIntent .getActivity (context, 0, intent, PendingIntent
	 * .FLAG_UPDATE_CURRENT ); notification. setLatestEventInfo (context,
	 * pushMessageInfo .getTitle(), pushMessageInfo .getDesc(), pendingIntent);
	 * } notification .flags |= Notification .FLAG_AUTO_CANCEL ;
	 * notificationManager .notify(1, notification); }
	 * 
	 * @Override public void onNotificationClicked (Context context, String
	 * title, String description, String customContentString ) {
	 * LogUtil.e("cexo", "onNotificationClicked()" ); }
	 * 
	 * @Override public void onSetTags(Context context, int errorCode,
	 * List<String> sucessTags, List<String> failTags, String requestId) {
	 * LogUtil.e("cexo", "onSetTags()"); }
	 * 
	 * @Override public void onUnbind(Context context, int errorCode, String
	 * requestId) { LogUtil.e("cexo", "onUnbind()"); }
	 */
	/**
	 * 推送消息实体
	 */
	/*
	 * private static final class PushMessageInfo {
	 *//** 应用名称 **/
	/*
	 * private String title;
	 *//** 应用详情 **/
	/*
	 * private String desc;
	 *//** 跳转应用详情时,需要用到它 **/
	/*
	 * private int sid;
	 *//** 应用跳转浏览器地址,只针对跳转浏览器类型的推送消息 **/
	/*
	 * private String url;
	 *//** 跳转类型 **/
	/*
	 * private int gotoType;
	 *//** 渠道号,只有该渠道的才能进行推送 **/
	/*
	 * private String channelName;
	 *//** 客户端版本号 **/
	/*
	 * private int versionCode;
	 * 
	 * public String getTitle() { return title; }
	 * 
	 * public void setTitle(String title) { this.title = title; }
	 * 
	 * public String getDesc() { return desc; }
	 * 
	 * public void setDesc(String desc) { this.desc = desc; }
	 * 
	 * public String getUrl() { return url; }
	 * 
	 * public void setUrl(String url) { this.url = url; }
	 * 
	 * public int getGotoType() { return gotoType; }
	 * 
	 * public void setGotoType(int gotoType) { this.gotoType = gotoType; }
	 * 
	 * public int getSid() { return sid; }
	 * 
	 * public void setSid(int sid) { this.sid = sid; }
	 * 
	 * public String getChannelName() { return channelName; }
	 * 
	 * public void setChannelName(String channelName) { this.channelName =
	 * channelName; }
	 * 
	 * public int getVersionCode() { return versionCode; }
	 * 
	 * public void setVersionCode(int versionCode) { this.versionCode =
	 * versionCode; }
	 * 
	 * }
	 */
}
