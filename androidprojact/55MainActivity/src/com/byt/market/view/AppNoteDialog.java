package com.byt.market.view;

import com.byt.market.R;
import com.byt.market.activity.ShareActivity;
import com.byt.market.ui.SubFragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class AppNoteDialog extends Dialog{
	private Context con;
	private Button btnOk;
	private View.OnClickListener cancelListener = new View.OnClickListener() {
		public void onClick(View view) {
			cancel();
		}
	};
	
	public AppNoteDialog(Context context) {
		super(context,R.style.dialog);
		this.con = context;
		setContentView(R.layout.dialog_app_note);
		btnOk = (Button) findViewById(R.id.btn_uninstall);
		Button cancel = (Button) findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(cancelListener);
	}
	public void setBtnOkListener(View.OnClickListener onclick){
		btnOk.setOnClickListener(onclick);
		dismiss();
	}

}
