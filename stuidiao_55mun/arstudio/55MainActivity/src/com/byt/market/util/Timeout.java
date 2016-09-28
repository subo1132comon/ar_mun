package com.byt.market.util;

import android.widget.ProgressBar;

public class Timeout {

public static void tiMing(ProgressBar bar){
	new Thread(){
		public void run() {
			int n = 0;
			while(n<20){
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				n++;
			}
			
		};
	}.start();
}
}
