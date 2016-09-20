package com.byt.market;

/**
 * 主要是定义一些Activity的公共初始化方法，供BaseActivity来使用
 */
public interface ActivityInit {

	/**
	 * 初始化控件
	 */
	void initView();

	/**
	 * 初始化数据（非耗时）
	 */
	void initData();

	/**
	 * 添加控件事件
	 */
	void addEvent();
}
