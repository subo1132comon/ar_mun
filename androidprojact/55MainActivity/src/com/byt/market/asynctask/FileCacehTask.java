package com.byt.market.asynctask;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.byt.market.R;
import com.byt.market.Constants;
import com.byt.market.util.Util;

public class FileCacehTask extends AsyncTask<Void, File, ArrayList<File>> implements OnClickListener {
	
	private Context mContext;
	private double mSize;
	private int mCount;
	private Dialog mDialog;
	private View mView;
	private TextView mTitle;
	private TextView mFileName;
	private TextView mTextCount;
	private TextView mTextSize;
	private Button mOk;
	private Button mCancel;
	private ArrayList<File> mList;
	
	private boolean showScan = false;
	
	public FileCacehTask(Context context) {
		mList = new ArrayList<File>();
		mContext = context;
		mView = LayoutInflater.from(mContext).inflate(R.layout.file_cache_dialog, null);
		mTitle = (TextView) mView.findViewById(R.id.textView1);
		mFileName = (TextView) mView.findViewById(R.id.file_cache_dialog_filename);
		mTextCount = (TextView) mView.findViewById(R.id.file_cache_dialog_count);
		mTextSize = (TextView) mView.findViewById(R.id.file_cache_dialog_size);
		mOk = (Button) mView.findViewById(R.id.file_cache_dialog_ok);
		mCancel = (Button) mView.findViewById(R.id.file_cache_dialog_cancel);
		mOk.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		mDialog = new AlertDialog.Builder(mContext).setTitle("提示").setIcon(R.drawable.icon).setView(mView).create();
	}
	
	@Override
	protected ArrayList<File> doInBackground(Void... arg0) {
		mSize = 0;
		//File[] dirs = new File[]{mContext.getCacheDir(), new File(Environment.getExternalStorageDirectory() + Constants.FOLDER_ROOT)};
		File[] dirs = new File[]{new File(Environment.getExternalStorageDirectory()+"/com.byt.market"),mContext.getExternalCacheDir(), new File(Environment.getExternalStorageDirectory() + Constants.FOLDER_ROOT)};
		ArrayList<File> list = new ArrayList<File>();
		for (File file : dirs) {
			list.addAll(scan(file));
		}
		return list;
	}

	private ArrayList<File> scan(File dir){
		ArrayList<File> list = new ArrayList<File>();
		File[] files = dir.listFiles();
		if(files == null){
			return list;
		}
		for (File file : files) {
			if(file.isDirectory()){
				list.addAll(scan(file));
			} else {
				mSize += file.length();
				publishProgress(file);
				mCount++;
				list.add(file);
			}
		}
		return list;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if(showScan){
			mTitle.setText(mContext.getString(R.string.scannow));
			mFileName.setText("");
			mTextCount.setText(mContext.getString(R.string.scan2) + mCount + mContext.getString(R.string.scan3));
			mTextSize.setText(mContext.getString(R.string.alltotal) + Util.apkSizeFormat((mSize / 1024 / 1024), "M"));
			mDialog.show();
		} else {
			Toast.makeText(mContext, mContext.getString(R.string.clearnow), Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onProgressUpdate(File... values) {
		super.onProgressUpdate(values);
		if(showScan){
			mFileName.setText(values[0].getName());
			mTextCount.setText(mContext.getString(R.string.scan2) + mCount + mContext.getString(R.string.scan3));
			mTextSize.setText(mContext.getString(R.string.alltotal) + Util.apkSizeFormat((mSize / 1024 / 1024), "M"));
		} else {
			if(values[0] != null){
				values[0].delete();
			}
		}
	}
	
	@Override
	protected void onPostExecute(ArrayList<File> result) {
		super.onPostExecute(result);
		if(showScan){
			Toast.makeText(mContext, mContext.getString(R.string.finishscan), Toast.LENGTH_SHORT).show();
			mTitle.setText(mContext.getString(R.string.finishscan));
			mFileName.setText("");
			mList = result;
		} 
		else 
		{
			if(Locale.getDefault().getCountry().equals("CN"))
			{
				Toast.makeText(mContext, mContext.getString(R.string.cleared) + Util.apkSizeFormat((mSize / 1024 / 1024), "M") + mContext.getString(R.string.cache), Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(mContext, Util.apkSizeFormat((mSize / 1024 / 1024), "M") +" "+ mContext.getString(R.string.cache) + " "+ mContext.getString(R.string.cleared), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.file_cache_dialog_ok:
			for (File file : mList) {
				file.delete();
			}
			mDialog.dismiss();
			Toast.makeText(mContext, mContext.getString(R.string.finishclear), Toast.LENGTH_SHORT).show();
			break;
		case R.id.file_cache_dialog_cancel:
			mDialog.dismiss();
			break;
		}
	}
}
