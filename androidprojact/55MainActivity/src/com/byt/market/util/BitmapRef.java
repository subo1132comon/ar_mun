package com.byt.market.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import android.graphics.Bitmap;

public class BitmapRef extends SoftReference<Bitmap>{
    String _key;
	public BitmapRef(Bitmap r, ReferenceQueue<? super Bitmap> q) {
		super(r, q);
		// TODO Auto-generated constructor stub
	}
	public String get_key() {
		return _key;
	}
	public void set_key(String key) {
		_key = key;
	}
}
