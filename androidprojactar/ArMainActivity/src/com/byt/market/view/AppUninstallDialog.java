package com.byt.market.view;

import com.byt.ar.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppUninstallDialog extends Dialog {
	private TextView uninstalltext;
	private TextView 	countText,txt_all_count,unistallapps;
	private TextView	pNameText;
	private String mOriginalName;
	private int mPreviousSelection;
	private Context con;
	private Button btnOk;
	
	private LinearLayout displayCountlayout;
	private LinearLayout displayPnameLayout;


	public AppUninstallDialog(Context context) {
		super(context,R.style.dialog);
		this.con = context;
		setContentView(R.layout.dialog_app_more_uninstall);
		uninstalltext=(TextView) findViewById(R.id.uninstalltext);
		unistallapps=(TextView) findViewById(R.id.unistallapps);
		displayCountlayout = (LinearLayout) findViewById(R.id.display_count_layout);
		displayPnameLayout = (LinearLayout) findViewById(R.id.display_pname_layout);
		pNameText = (TextView) findViewById(R.id.txt_packagename);
		countText = (TextView) findViewById(R.id.txt_all_count);
		btnOk = (Button) findViewById(R.id.btn_uninstall);
		Button cancel = (Button) findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(cancelListener);
	}
	
	public void setSelectCount(int count){
		displayCountlayout.setVisibility(View.VISIBLE);
		displayPnameLayout.setVisibility(View.GONE);
		countText.setVisibility(View.GONE);
		unistallapps.setVisibility(View.GONE);
		uninstalltext.setText(con.getString(R.string.ut_02)+count+con.getString(R.string.ut_01));
	}
	
	public void setPackageName(String pname){
		displayPnameLayout.setVisibility(View.VISIBLE);
		displayCountlayout.setVisibility(View.GONE);
		pNameText.setText(pname);
	}
	
	public void setBtnOkListener(View.OnClickListener onclick){
		btnOk.setOnClickListener(onclick);
		dismiss();
	}

	private View.OnClickListener cancelListener = new View.OnClickListener() {
		public void onClick(View view) {
			cancel();
		}
	};
	
}
