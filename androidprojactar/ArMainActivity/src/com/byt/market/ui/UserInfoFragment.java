package com.byt.market.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.byt.market.Constants;
import com.byt.market.MyApplication;
import com.byt.ar.R;
import com.byt.market.activity.UserActivity;
import com.byt.market.asynctask.ProtocolTask;
import com.byt.market.asynctask.ProtocolTask.TaskListener;
import com.byt.market.data.UserData;
import com.byt.market.data.UserData.OnUserInfoChangeListener;
import com.byt.market.net.NetworkUtil;
import com.byt.market.util.BitmapUtil;
import com.byt.market.util.FileUtil;
import com.byt.market.util.LogUtil;
import com.byt.market.util.Util;
import com.byt.market.view.RoundAngleImageView;

public class UserInfoFragment extends UserBaseFragment implements
		OnClickListener, OnUserInfoChangeListener {
	/** 无操作 */
	public static final int NONE = 0;
	/** 拍照请求 */
	public static final int REQUEST_CAMERA = 1;
	/** 相册请求 */
	public static final int REQUEST_PHOTOS = 2;
	/** 裁剪请求 */
	public static final int REQUEST_CUTRESULT = 3;

	private View mGoBack;
	private View mTitleBarIcon;
	private TextView mTitle;
	private View mSearchBtn;
	private View mRightMenu;

	private EditText mName;
	private ImageView mIcon;
	private String iconPath;
	private View mSaveBtn;
	private boolean isExit;

	// private Dialog mUpdateIconDialog;

	private Handler mHander = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (isExit) {
				return;
			}
			LogUtil.e("cexo", "handleMessage()");
			UserData user = MyApplication.getInstance().getUser();
			Bitmap bmpIcon = user.getBmpIcon();
			if (bmpIcon != null) {
				mIcon.setImageBitmap(BitmapUtil.getRoundedCornerBitmap(bmpIcon));
			} else {
				mIcon.setImageBitmap(BitmapUtil
						.getRoundedCornerBitmap(BitmapUtil
								.drawableToBitmap(getResources().getDrawable(
										R.drawable.default_user))));
			}
		}
	};

	public View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.user_info, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mGoBack = view.findViewById(R.id.titlebar_back_arrow);
		mTitleBarIcon = view.findViewById(R.id.titlebar_icon);
		mTitle = (TextView) view.findViewById(R.id.titlebar_title);
		mSearchBtn = view.findViewById(R.id.titlebar_search_button);
		mRightMenu = view.findViewById(R.id.titlebar_applist_button_container);

		mName = (EditText) view.findViewById(R.id.user_info_nikename);
		mIcon = (ImageView) view.findViewById(R.id.user_info_icon);
		mSaveBtn = view.findViewById(R.id.user_info_save);

		mGoBack.setVisibility(View.VISIBLE);
		mTitleBarIcon.setVisibility(View.GONE);
		mSearchBtn.setVisibility(View.GONE);
		mRightMenu.setVisibility(View.INVISIBLE);
		view.findViewById(R.id.titlebar_navi_icon).setVisibility(View.GONE);
		mTitle.setText(R.string.titlebar_title_change_userinfo);

		mGoBack.setOnClickListener(this);
		mTitle.setOnClickListener(this);

		// mIcon.setOnClickListener(this); // 暂时去掉用户上传头像的功能,下个版本,头像做成预制化(本地化)
		mSaveBtn.setOnClickListener(this);

		MyApplication.getInstance().mUserInfoChangeList.add(this);
		onUserInfoChange();
	}

	@Override
	public void onDestroyView() {
		LogUtil.e("cexo", "onDestroyView()");
		super.onDestroyView();
		MyApplication.getInstance().mUserInfoChangeList.remove(this);
		mHander.removeMessages(0);
	}

	@Override
	public void onUserInfoChange() {
		UserData user = MyApplication.getInstance().getUser();
		String nickName = user.getNickname();
		if (nickName != null) {
			mName.setText(nickName);
		}
		MyApplication.getInstance().loadUserIcon(new Runnable() {

			@Override
			public void run() {
				mHander.sendEmptyMessage(0);
			}
		});
	}

	public HashMap<String, String> getHeader() {
		String model = Util.mobile;
		String imei = Util.imie;
		String vcode = Util.vcode;
		String channel = Util.channel;
		if (model == null)
			model = Util.getModel();
		if (imei == null)
			imei = Util.getIMEI(getActivity());
		if (vcode == null)
			vcode = Util.getVcode(getActivity());
		if (TextUtils.isEmpty(channel)) {
			channel = Util.getChannelName(MyApplication.getInstance());
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("imei", imei);
		map.put("vcode", vcode);
		NetworkUtil.isNetWorking(getActivity());
		map.put("net", NetworkUtil.getNetworkType());
		map.put("model", model);
		map.put("channel", channel);

		return map;
	}

	private boolean checkName() {
		if (mName.getText().toString().trim().equals("")) {
			return false;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.titlebar_back_arrow:
		case R.id.titlebar_title:
			UserActivity userActivity = getUserActivity();
			if (userActivity != null) {
				userActivity.onFragmentGoBack();
			}
			break;
		case R.id.user_info_icon:
			new AlertDialog.Builder(getActivity())
					.setItems(
							new String[] {
									getString(R.string.change_icon_camera),
									getString(R.string.change_icon_local) },
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										openCamera();
										break;
									case 1:
										openPhotos();
										break;
									}
								}
							}).setTitle("").show();
			break;
		case R.id.user_info_save:
			if (!checkName()) {
				showShortToast(getString(R.string.edit_nicknamenull));
				return;
			}
			ProtocolTask task = new ProtocolTask(getActivity());
			task.setListener(new TaskListener() {

				@Override
				public void onPostExecute(byte[] bytes) {
					JSONObject json;
					try {
						json = new JSONObject(new String(bytes));
						int resultStatus = json.getInt("resultStatus");
						if (resultStatus == 1) {
							showShortToast(getString(R.string.edit_succ));
							MyApplication
									.getInstance()
									.getUser()
									.setNickname(
											mName.getText().toString().trim());
							MyApplication.getInstance().updateUserInfo();
							UserActivity userActivity = getUserActivity();
							if (userActivity != null) {
								userActivity.onFragmentGoBack();
								isExit = true;
							}
						} else {
							showShortToast(getString(R.string.edit_error));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onNoNetworking() {

				}

				@Override
				public void onNetworkingError() {

				}
			});
			try {

				String pwd = MyApplication.getInstance().getUser().getMd5();
				if (pwd == null) {
					pwd = "";
				}
				JSONObject json = new JSONObject();
				json.put("NICKNAME", mName.getText().toString().trim());
				task.execute(Constants.LIST_URL + "?qt=" + Constants.UPUSER
						+ "&uid="
						+ MyApplication.getInstance().getUser().getUid()
						+ "&pwd=" + pwd, json.toString(), "TAG", getHeader());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	// 调用系统拍照回调
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CAMERA:
			if (Activity.RESULT_OK == resultCode) {
				if (data != null) {
					// 拍照后照片uri
					Uri uri = data.getData();
					if (uri != null) {
						startPhotoCut(uri);
					} else {
						// 部分机型不会返回拍照Uri，尝试获取extra中的bitmap
						Object bitmap = data.getExtras().get("data");
						if (bitmap instanceof Bitmap) {
							Bitmap bmap = (Bitmap) bitmap;
							// 保存该图片并返回图片uri
							uri = BitmapUtil.addImage(getActivity()
									.getContentResolver(), "camera", System
									.currentTimeMillis(), FileUtil
									.getUserIconPath(), "camera.png", bmap);
							startPhotoCut(uri);
						} else {
							// 既没有uri，又没有bitmap
							showShortToast(getString(R.string.toast_camera_not_return_bitmap));
						}
					}
				}
			} else {
				showShortToast(getString(R.string.toast_user_cancel_change_icon));
			}
			break;
		case REQUEST_PHOTOS:
			if (Activity.RESULT_OK == resultCode) {
				if (data != null) {
					// 从相册获取照片uri
					startPhotoCut(data.getData());
				}
			} else {
				showShortToast(getString(R.string.toast_user_cancel_change_icon));
			}
			break;
		case REQUEST_CUTRESULT:
			if (resultCode == Activity.RESULT_OK) {
				if (data != null && data.getAction() != null) {
					uploadPhoto(Uri.parse(data.getAction()));
				} else if (data != null && data.getData() != null) {
					uploadPhoto(data.getData());
				} else {
					showShortToast(getString(R.string.toast_system_cat_fail));
				}
			} else {
				showShortToast(getString(R.string.toast_user_cancel_change_icon));
			}
			break;
		}

	}

	/**
	 * 打开照相机(第一步)
	 * 
	 * @param activity
	 */
	private void openCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra("return-data", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
		startActivityForResult(intent, REQUEST_CAMERA);
	}

	/**
	 * 打开相册(第一步)
	 * 
	 * @param activity
	 */
	private void openPhotos() {
		Intent intent = new Intent(Intent.ACTION_PICK, null);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				"image/*");
		startActivityForResult(intent, REQUEST_PHOTOS);
	}

	/**
	 * 打开系统的裁剪工具(第二步)
	 * 
	 * @param uri
	 */
	public void startPhotoCut(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectY", 1);
		// intent.putExtra("outputX", 640);// outputX outputY 是裁剪图片宽高
		// intent.putExtra("outputY", 640);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra("noFaceDetection", false);
		// if(AtomData.ua == null){
		// ApplicationUtil.initAtom(this);
		// }
		// if("E16i".equals(AtomData.ua)){
		// intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new
		// File(FileUtil.getHeadPortraitPath(), "temp.png")));
		// }
		intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
		getActivity().startActivityForResult(intent, REQUEST_CUTRESULT);
	}

	/**
	 * 上传头像(第三步)
	 * 
	 * @param uri
	 */
	private void uploadPhoto(Uri uri) {
		Bitmap photo = null;
		// 返回裁剪结果照片uri
		String scheme = uri.getScheme();
		if ("content".equals(scheme)) {
			try {
				InputStream is = getActivity().getContentResolver()
						.openInputStream(uri);
				photo = BitmapFactory.decodeStream(is);
			} catch (FileNotFoundException e) {
				LogUtil.d("UserInfoFragment",
						"File not found " + uri.toString());
			}
		} /*
		 * else if ("file".equals(scheme)){//这种情况可能没有了，照相和相册都返回的是content类型的uri
		 * photo = BitmapUtil.compressBitmap(uri.getPath(), 300); }
		 */
		if (photo == null) {
			showShortToast(getString(R.string.toast_system_cat_fail));
			return;
		}
		if (uri.getScheme().equals("content")) {
			getActivity().getContentResolver().delete(uri, null, null);// 从contentProvider中删除裁剪记录
			Cursor c = getActivity().getContentResolver().query(uri,
					new String[] { ImageColumns.DATA }, null, null, null);
			if (c.moveToFirst()) {
				String path = c.getString(c.getColumnIndex(ImageColumns.DATA));
				new File(path).delete();// 删除裁剪图片
			}
		}
		// 如果是照相，并且产生临时数据camera.png，删除ContentProvider中的记录
		getActivity().getContentResolver().delete(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				ImageColumns.DATA + "=?",
				new String[] { new File(getActivity().getFilesDir()
						.getAbsoluteFile(), "camera.png").getAbsolutePath() });
		File camera = new File(getActivity().getFilesDir().getAbsolutePath());
		if (camera.exists()) {
			String[] list = camera.list(new FilenameFilter() {

				public boolean accept(File dir, String filename) {
					if (filename.startsWith("camera")) {
						return true;
					}
					return false;
				}
			});
			for (String name : list) {
				new File(camera, name).delete();
			}
		}

		// mUpdateIconDialog =
		// createDialog(getString(R.string.dialog_title_hint),
		// LayoutInflater.from(getActivity()).inflate(R.layout.uplode_icon_dialog,
		// null));
		// TextView txt = (TextView)
		// mUpdateIconDialog.findViewById(R.id.textView1);
		// txt.setText("");
		// mUpdateIconDialog.show();
		showShortToast(getString(R.string.edit_commitnow));
		DisplayMetrics outMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(outMetrics);
		final Bitmap icon = BitmapUtil.zoomImg(photo,
				(int) (100 * outMetrics.density));
		photo.recycle();
		ProtocolTask task = new ProtocolTask(getActivity());
		task.setListener(new TaskListener() {

			@Override
			public void onPostExecute(byte[] bytes) {
				// dismissDialog(mUpdateIconDialog);
				try {
					String string = new String(bytes);
					JSONObject json = new JSONObject(string);
					int resultStatus = json.getInt("resultStatus");
					if (resultStatus == 1) {
						JSONObject data = json.getJSONObject("data");
						// mIcon.setImageBitmap(icon);
						// FileUtil.saveUserBytesToFile(
						// BitmapUtil.Bitmap2Bytes(icon),
						// App.getInstance().getUser().getUid());
						MyApplication.getInstance().getUser()
								.setIconUrl(data.getString("ICON"));
						MyApplication.getInstance().getUser().setBmpIcon(icon);
						onUserInfoChange();
						MyApplication.getInstance().updateUserInfo();
						showShortToast(getString(R.string.edit_commitesucc));
					} else {
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onNoNetworking() {
				// dismissDialog(mUpdateIconDialog);
				showShortToast(getString(R.string.edit_commiterror));
			}

			@Override
			public void onNetworkingError() {
				// dismissDialog(mUpdateIconDialog);
				showShortToast(getString(R.string.edit_commiterror));
			}
		});
		String pwd = MyApplication.getInstance().getUser().getMd5();
		if (pwd == null) {
			pwd = "";
		}

		task.execute(Constants.LIST_URL + "?qt=" + Constants.UPUSERHEAD
				+ "&uid=" + MyApplication.getInstance().getUser().getUid()
				+ "&pwd=" + pwd, BitmapUtil.Bitmap2Bytes(icon), "TAG",
				getHeader());
		photo.recycle();
		photo = null;

	}
}
