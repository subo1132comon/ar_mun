package com.byt.market;

public abstract interface OnAppClickListener{
	/**
	 * 
	 * @param obj
	 * @param action
	 */
	public void onAppClick(Object obj, String action);
	/**
	 * 
	 * @param obj
	 * @param what
	 * @param action
	 */
	public void onAppClick(Object obj, int what, String action);
}