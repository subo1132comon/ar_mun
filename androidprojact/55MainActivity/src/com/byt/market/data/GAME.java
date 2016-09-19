package com.byt.market.data;

import java.io.Serializable;

import com.byt.market.log.LogModel;

/**
 * */
public class GAME implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int app_id;
	public String list_id = LogModel.P_LIST;
	public String list_c = LogModel.P_LIST; 
	public int d_c; 
	public int d_o; 
	public int d_f; 
	public int d_d; 
	public int i_o; 
	public int i_f;
}
