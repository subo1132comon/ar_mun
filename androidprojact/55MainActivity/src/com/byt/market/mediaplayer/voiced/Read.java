package com.byt.market.mediaplayer.voiced;

import java.io.IOException;
import java.util.Date;

import com.byt.market.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.UserDictionary.Words;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Read extends Activity implements OnClickListener,
		OnSeekBarChangeListener {
	private LinearLayout layout;
	private AlertDialog dialog;
	private static final String TAG = "Read2";
	private static int begin = 0;// ��¼���鼮��ʼλ��
	public static Canvas mCurPageCanvas, mNextPageCanvas;
	private static String word = "";// ��¼��ǰҳ�������
	private int a = 0, b = 0;// ��¼toolpop��λ��
	private TextView bookBtn1, bookBtn2;
	private String bookPath;// ��¼�������·��
	private String ccc = null;// ��¼�Ƿ�Ϊ��ݷ�ʽ����
	protected long count = 1;
	private SharedPreferences.Editor editor;
	private ImageButton imageBtn2, imageBtn3_1, imageBtn3_2;
	private ImageButton imageBtn4_1, imageBtn4_2;
	private Boolean isNight; // ����ģʽ,���������
	protected int jumpPage;// ��¼��ת������
	private int light; // ����ֵ
	private WindowManager.LayoutParams lp;
	private TextView markEdit4;
	private Bitmap mCurPageBitmap, mNextPageBitmap;
	private Context mContext = null;
	private PageWidget mPageWidget;
	private PopupWindow mPopupWindow, mToolpop, mToolpop1, mToolpop2,
			mToolpop3, mToolpop4, playpop;
	protected int PAGE = 1;
	private BookPageFactory pagefactory;
	private View popupwindwow, toolpop, toolpop1, toolpop2, toolpop3, toolpop4,
	playView;
	int screenHeight;
	int readHeight; // ��������ʾ�߶�
	int screenWidth;
	private SeekBar seekBar1, seekBar2, seekBar4;
	private Boolean show = false;// popwindow�Ƿ���ʾ
	private int size = 30; // �����С
	private SharedPreferences sp;
	int defaultSize = 0;
	public static String words;
	private boolean isStart;
	private InstallManager mManager = null;
	private Button button3;
	
	//private SpeekUtil speekUtil;
	// ʵ����Handler
	public Handler mHandler = new Handler() {
		// �������̷߳�������Ϣ��ͬʱ����UI
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				begin = msg.arg1;
				pagefactory.setM_mbBufBegin(begin);
				pagefactory.setM_mbBufEnd(begin);
				postInvalidateUI();
				break;
			case 1:
				pagefactory.setM_mbBufBegin(begin);
				pagefactory.setM_mbBufEnd(begin);
				postInvalidateUI();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * ��¼���� �����popupwindow
	 */
	private void clear() {
		getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		show = false;
		mPopupWindow.dismiss();
		popDismiss();
	}

	/**
	 * ��ȡ�����ļ�������ֵ
	 */
	private void getLight() {
		light = sp.getInt("light", 5);
		isNight = sp.getBoolean("night", false);
	}

	/**
	 * ��ȡ�����ļ��������С
	 */
	private void getSize() {
		size = sp.getInt("size", defaultSize);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// ���尴ť
		case R.id.bookBtn1:
			a = 1;
			setToolPop(a);
			break;
		// ���Ȱ�ť
		case R.id.bookBtn2:
			a = 2;
			setToolPop(a);
			break;
		// ��ǩ��ť
//		case R.id.bookBtn3:
//			a = 3;
//			setToolPop(a);
//			break;
		// ��ת��ť
//		case R.id.bookBtn4:
//			a = 4;
//			setToolPop(a);
//			break;
//		case R.id.text_speak:
//			Log.i("hck", "text_speak");
//			a = 5;
//			setToolPop(a);
//			break;
		// ҹ��ģʽ��ť
			
			
			//-----------0506-add  by  bobo
//		case R.id.imageBtn2:
//			if (isNight) {
//				layout.setBackgroundResource(R.drawable.titlebar_big);
//				pagefactory.setM_textColor(Color.rgb(28, 28, 28));
//				imageBtn2.setImageResource(R.drawable.reader_switch_off);
//				isNight = false;
//				pagefactory.setBgBitmap(BitmapFactory.decodeResource(
//						this.getResources(), R.drawable.bg));
//			} else {
//				layout.setBackgroundResource(R.drawable.tmall_bar_bg);
//				pagefactory.setM_textColor(Color.rgb(128, 128, 128));
//				imageBtn2.setImageResource(R.drawable.reader_switch_on);
//				isNight = true;
//				pagefactory.setBgBitmap(BitmapFactory.decodeResource(
//						this.getResources(), R.drawable.main_bg));
//			}
//			setLight();
//			pagefactory.setM_mbBufBegin(begin);
//			pagefactory.setM_mbBufEnd(begin);
//			postInvalidateUI();
//			break;
//		case R.id.imageBtn4_1:
//			clear();
//			pagefactory.setM_mbBufBegin(begin);
//			pagefactory.setM_mbBufEnd(begin);
//			postInvalidateUI();
//			break;
//		case R.id.imageBtn4_2:
//			clear();
//			break;
			//-----------0506-add  by  bobo  end
//		case R.id.play_set:
//			
//		case R.id.play_voice:
			
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mContext = getBaseContext();
		MangerActivitys.activitys.add(this);
		isStart = false;
		WindowManager manage = getWindowManager();
		Display display = manage.getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		defaultSize = (screenWidth * 20) / 320;
		readHeight = screenHeight - screenWidth / 320;

		mCurPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.ARGB_8888);
		mNextPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.ARGB_8888);
		mCurPageCanvas = new Canvas(mCurPageBitmap);
		mNextPageCanvas = new Canvas(mNextPageBitmap);

		mPageWidget = new PageWidget(this, screenWidth, readHeight);// ҳ��
		setContentView(R.layout.read);

		RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.readlayout);
		rlayout.addView(mPageWidget);

		Intent intent = getIntent();
		bookPath = intent.getStringExtra("path");
		
//		//test  by bobo-----
//		bookPath =Environment.getExternalStorageDirectory()
//				.getPath() + "/SYNC/novel/NN3.txt";
		
		ccc = intent.getStringExtra("ccc");

		

		mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);
		mPageWidget.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent e) {
				boolean ret = false;
				

				if (v == mPageWidget) {
					if (!show) {

						if (e.getAction() == MotionEvent.ACTION_DOWN) {
							if (e.getY() > readHeight) {// ������Χ�ˣ���ʾ�������������������ҳ
								return false;
							}
							mPageWidget.abortAnimation();
							mPageWidget.calcCornerXY(e.getX(), e.getY());
							pagefactory.onDraw(mCurPageCanvas);
							if (mPageWidget.DragToRight()) {// ��
								try {
									pagefactory.prePage();
									begin = pagefactory.getM_mbBufBegin();// ��ȡ��ǰ�Ķ�λ��
									word = pagefactory.getFirstLineText();// ��ȡ��ǰ�Ķ�λ�õ���������
								} catch (IOException e1) {
									Log.e(TAG, "onTouch->prePage error", e1);
								}
								if (pagefactory.isfirstPage()) {
									Toast.makeText(mContext, getString(R.string.fist_page),
											Toast.LENGTH_SHORT).show();
									return false;
								}
								pagefactory.onDraw(mNextPageCanvas);
							} else {// �ҷ�
								try {
									pagefactory.nextPage();
									begin = pagefactory.getM_mbBufBegin();// ��ȡ��ǰ�Ķ�λ��
									word = pagefactory.getFirstLineText();// ��ȡ��ǰ�Ķ�λ�õ���������
								} catch (IOException e1) {
									Log.e(TAG, "onTouch->nextPage error", e1);
								}
								if (pagefactory.islastPage()) {
									Toast.makeText(mContext, getString(R.string.last_page),          
											Toast.LENGTH_SHORT).show();
									return false;
								}
								pagefactory.onDraw(mNextPageCanvas);
							}
							mPageWidget.setBitmaps(mCurPageBitmap,
									mNextPageBitmap);
						}
						editor.putInt(bookPath + "begin", begin).commit();
						ret = mPageWidget.doTouchEvent(e);
						return ret;
					}
				}
				return false;
			}
		});
		
		setPop();

		// ��ȡ��¼��sharedpreferences�ĸ���״̬
		sp = getSharedPreferences("config", MODE_PRIVATE);
		editor = sp.edit();
		getSize();// ��ȡ�����ļ��е�size��С
		getLight();// ��ȡ�����ļ��е�lightֵ
		count = sp.getLong(bookPath + "count", 1);

		lp = getWindow().getAttributes();
		lp.screenBrightness = light / 10.0f < 0.01f ? 0.01f : light / 10.0f;
		getWindow().setAttributes(lp);
		pagefactory = new BookPageFactory(screenWidth, readHeight);// �鹤��
		if (isNight) {
			pagefactory.setBgBitmap(BitmapFactory.decodeResource(
					this.getResources(), R.drawable.main_bg));
			pagefactory.setM_textColor(Color.rgb(128, 128, 128));
		} else {
			pagefactory.setBgBitmap(BitmapFactory.decodeResource(
					this.getResources(), R.drawable.bg));
			pagefactory.setM_textColor(Color.rgb(28, 28, 28));
		}
		begin = sp.getInt(bookPath + "begin", 0);
		try {
			pagefactory.openbook(bookPath, begin);// ��ָ��λ�ô��鼮��Ĭ�ϴӿ�ʼ��
			pagefactory.setM_fontSize(size);
			pagefactory.onDraw(mCurPageCanvas);

		} catch (Exception e1) {
			
			Toast.makeText(this, getString(R.string.open_erro), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		pagefactory = null;
		mPageWidget = null;
		
		finish();
	}

	/**
	 * �ж��Ǵ��ĸ���������READ
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (ccc == null) {
				if (show) {// ���popwindow������ʾ
					popDismiss();
					getWindow()
							.clearFlags(
									WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
					show = false;
					mPopupWindow.dismiss();
				} else {
					Read.this.finish();
				}
			} else {
				if (!ccc.equals("ccc")) {
					if (show) {// ���popwindow������ʾ
						getWindow()
								.clearFlags(
										WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
						show = false;
						mPopupWindow.dismiss();
						popDismiss();
					} else {
						this.finish();
					}
				} else {
					this.finish();
				}
			}
		}

		return true;
	}

	/**
	 * ��Ӷ�menu��ť�ļ���
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (show) {
				show = false;
				mPopupWindow.dismiss();
				popDismiss();

			} else {
				show = true;
				pop();
			}
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		switch (seekBar.getId()) {
		
		//------------------bb
		// ���������
//		case R.id.seekBar1:
//			size = seekBar1.getProgress() + 15;
//			setSize();
//			pagefactory.setM_fontSize(size);
//			pagefactory.setM_mbBufBegin(begin);
//			pagefactory.setM_mbBufEnd(begin);
//			postInvalidateUI();
//			break;
//		// ���Ƚ�����
//		case R.id.seekBar2:
//			light = seekBar2.getProgress();
//			setLight();
//			lp.screenBrightness = light / 10.0f < 0.01f ? 0.01f : light / 10.0f;
//			getWindow().setAttributes(lp);
//			break;
			//------------------bb
		// ��ת������
//		case R.id.seekBar4:
//			int s = seekBar4.getProgress();
//			markEdit4.setText(s + "%");
//			begin = (pagefactory.getM_mbBufLen() * s) / 100;
//			editor.putInt(bookPath + "begin", begin).commit();
//			pagefactory.setM_mbBufBegin(begin);
//			pagefactory.setM_mbBufEnd(begin);
//			try {
//				if (s == 100) {
//					pagefactory.prePage();
//					pagefactory.getM_mbBufBegin();
//					begin = pagefactory.getM_mbBufEnd();
//					pagefactory.setM_mbBufBegin(begin);
//					pagefactory.setM_mbBufBegin(begin);
//				}
//			} catch (IOException e) {
//				Log.e(TAG, "onProgressChanged seekBar4-> IOException error", e);
//			}
//			postInvalidateUI();
//			break;
//		case R.id.text_speak:
//		  
//			break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	/**
	 * popupwindow�ĵ���,������
	 */
	public void pop() {

		mPopupWindow.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 0);
		bookBtn1 = (TextView) popupwindwow.findViewById(R.id.bookBtn1);
		bookBtn2 = (TextView) popupwindwow.findViewById(R.id.bookBtn2);
	//	TextView button = (TextView) popupwindwow.findViewById(R.id.text_speak);
		layout = (LinearLayout) popupwindwow.findViewById(R.id.book_pop);
		getLight();
		if (isNight) {
			layout.setBackgroundResource(R.drawable.tmall_bar_bg);
		} else {
			layout.setBackgroundResource(R.drawable.titlebar_big);

		}
		bookBtn1.setOnClickListener(this);
		bookBtn2.setOnClickListener(this);
	//	button.setOnClickListener(this);
	}

	/**
	 * �ر�55������pop
	 */
	public void popDismiss() {
		mToolpop.dismiss();
		mToolpop1.dismiss();
		mToolpop2.dismiss();
		mToolpop3.dismiss();
		mToolpop4.dismiss();
		playpop.dismiss();
	}

	/**
	 * ��¼�����ļ�������ֵ�ͺ�����
	 */
	private void setLight() {
		try {
			light = seekBar2.getProgress();
			editor.putInt("light", light);
			if (isNight) {
				editor.putBoolean("night", true);
			} else {
				editor.putBoolean("night", false);
			}
			editor.commit();
		} catch (Exception e) {
			Log.e(TAG, "setLight-> Exception error", e);
		}
	}

	/**
	 * ��ʼ������POPUPWINDOW
	 */
	private void setPop() {
		popupwindwow = this.getLayoutInflater().inflate(R.layout.bookpop, null);
		toolpop = this.getLayoutInflater().inflate(R.layout.toolpop, null);
		mPopupWindow = new PopupWindow(popupwindwow, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		mToolpop = new PopupWindow(toolpop, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
//		toolpop1 = this.getLayoutInflater().inflate(R.layout.tool11, null);
//		mToolpop1 = new PopupWindow(toolpop1, LayoutParams.FILL_PARENT,
//				LayoutParams.WRAP_CONTENT);
//		toolpop2 = this.getLayoutInflater().inflate(R.layout.tool22, null);
//		mToolpop2 = new PopupWindow(toolpop2, LayoutParams.FILL_PARENT,
//				LayoutParams.WRAP_CONTENT);
//		toolpop3 = this.getLayoutInflater().inflate(R.layout.tool33, null);
//		mToolpop3 = new PopupWindow(toolpop3, LayoutParams.FILL_PARENT,
//				LayoutParams.WRAP_CONTENT);
//		toolpop4 = this.getLayoutInflater().inflate(R.layout.tool44, null);
//		mToolpop4 = new PopupWindow(toolpop4, LayoutParams.FILL_PARENT,
//				LayoutParams.WRAP_CONTENT);
//		playView = this.getLayoutInflater().inflate(R.layout.play_pop, null);
//		playpop = new PopupWindow(playView, LayoutParams.WRAP_CONTENT,
//				LayoutParams.WRAP_CONTENT);

	}

	/**
	 * ��¼�����ļ��������С
	 */
	private void setSize() {
		try {
			size = seekBar1.getProgress() + 15;
			editor.putInt("size", size);
			editor.commit();
		} catch (Exception e) {
			Log.e(TAG, "setSize-> Exception error", e);
		}
	}

	/**
	 * ����popupwindow����ʾ������
	 * 
	 * @param a
	 */
	public void setToolPop(int a) {
		Log.i("hck", "setToolPop: " + "b:" + b + "  a:" + a);
		if (a == b && a != 0) {
			if (mToolpop.isShowing()) {
				popDismiss();
			} else {
				mToolpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
						screenWidth * 45 / 320);
				// ��������尴ť
				//------------------0506
//				if (a == 1) {
//					mToolpop1.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
//							screenWidth * 45 / 320);
//					seekBar1 = (SeekBar) toolpop1.findViewById(R.id.seekBar1);
//					size = sp.getInt("size", 20);
//					seekBar1.setProgress((size - 15));
//					seekBar1.setOnSeekBarChangeListener(this);
//				}
//				// ��������Ȱ�ť
//				if (a == 2) {
//					mToolpop2.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
//							screenWidth * 45 / 320);
//					seekBar2 = (SeekBar) toolpop2.findViewById(R.id.seekBar2);
//					imageBtn2 = (ImageButton) toolpop2
//							.findViewById(R.id.imageBtn2);
//					getLight();
//
//					seekBar2.setProgress(light);
//					if (isNight) {
//
//						imageBtn2.setImageResource(R.drawable.reader_switch_on);
//					} else {
//						layout.setBackgroundResource(R.drawable.titlebar_big);
//						imageBtn2
//								.setImageResource(R.drawable.reader_switch_off);
//					}
//					imageBtn2.setOnClickListener(this);
//					seekBar2.setOnSeekBarChangeListener(this);
//				}
				//------------------0506
				if (a == 5) {
//					playpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
//							screenWidth * 45 / 320);
//					Button button = (Button) playView
//							.findViewById(R.id.play_set);
//					button3 = (Button) playView.findViewById(R.id.play_voice);
//					button.setOnClickListener(this);
//					button3.setOnClickListener(this);
//					button3.setText("����");
					
//					 if (speekUtil==null) {
//							speekUtil=new SpeekUtil(this);
//						}
//					 Log.i("hck", "hhhh:"+words);
//						   speekUtil.start(words);
				}

			}
		} else {
			if (mToolpop.isShowing()) {
				// �����ݵļ�¼
				popDismiss();
			}
			mToolpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
					screenWidth * 45 / 320);
			// ������尴ť
			//---------------0506
//			if (a == 1) {
//				mToolpop1.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
//						screenWidth * 45 / 320);
//				seekBar1 = (SeekBar) toolpop1.findViewById(R.id.seekBar1);
//				size = sp.getInt("size", 20);
//				seekBar1.setProgress(size - 15);
//				seekBar1.setOnSeekBarChangeListener(this);
//			}
//			// ������Ȱ�ť
//			if (a == 2) {
//				mToolpop2.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
//						screenWidth * 45 / 320);
//				seekBar2 = (SeekBar) toolpop2.findViewById(R.id.seekBar2);
//				imageBtn2 = (ImageButton) toolpop2.findViewById(R.id.imageBtn2);
//				getLight();
//				seekBar2.setProgress(light);
//
//				if (isNight) {
//					pagefactory.setBgBitmap(BitmapFactory.decodeResource(
//							this.getResources(), R.drawable.main_bg));
//				} else {
//					pagefactory.setBgBitmap(BitmapFactory.decodeResource(
//							this.getResources(), R.drawable.bg));
//				}
//
//				if (isNight) {
//					imageBtn2.setImageResource(R.drawable.reader_switch_on);
//				} else {
//					imageBtn2.setImageResource(R.drawable.reader_switch_off);
//				}
//				imageBtn2.setOnClickListener(this);
//				seekBar2.setOnSeekBarChangeListener(this);
//			}
			//-------------------0506
			if (a == 5) {
//				 if (speekUtil==null) {
//						speekUtil=new SpeekUtil(this);
//					}
//				 Log.i("hck", "rrrr:"+words);
//					   speekUtil.start(words);
			}
		}
		// ��¼�ϴε��������һ��
		b = a;
	}

	

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * ˢ�½���
	 */
	public void postInvalidateUI() {
		mPageWidget.abortAnimation();
		pagefactory.onDraw(mCurPageCanvas);
		try {
			pagefactory.currentPage();
			begin = pagefactory.getM_mbBufBegin();// ��ȡ��ǰ�Ķ�λ��
			word = pagefactory.getFirstLineText();// ��ȡ��ǰ�Ķ�λ�õ���������
		} catch (IOException e1) {
			Log.e(TAG, "postInvalidateUI->IOException error", e1);
		}

		pagefactory.onDraw(mNextPageCanvas);

		mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
		mPageWidget.postInvalidate();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	

	public void nextPage() {
		mPageWidget.abortAnimation();
		mPageWidget.calcCornerXY(478, 459);
		pagefactory.onDraw(mCurPageCanvas);
		try {
			pagefactory.nextPage();
			begin = pagefactory.getM_mbBufBegin();// ��ȡ��ǰ�Ķ�λ��
			word = pagefactory.getFirstLineText();// ��ȡ��ǰ�Ķ�λ�õ���������
		} catch (IOException e1) {
			Log.e(TAG, "onTouch->nextPage error", e1);
		}
		if (pagefactory.islastPage()) {
			Toast.makeText(mContext, getString(R.string.last_page), Toast.LENGTH_SHORT).show();

		} else {
			pagefactory.onDraw(mNextPageCanvas);

			mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
			editor.putInt(bookPath + "begin", begin).commit();
			MotionEvent e = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE,
					427, 470, 1);
			mPageWidget.doTouchEvent(e);

	}
	//speekUtil.start(words);
	Log.i("hck", "nest content2: "+words);
	}

}