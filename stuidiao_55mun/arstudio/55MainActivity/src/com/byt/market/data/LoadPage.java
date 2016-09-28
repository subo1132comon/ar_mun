package com.byt.market.data;

import org.json.JSONObject;

/**
 * */
public class LoadPage {
	public String url;
	public int code;

	public static LoadPage parseLoadPage(JSONObject jsoObject) throws Exception {
		if (jsoObject == null)
			return null;
		LoadPage lPage = new LoadPage();
		if (!jsoObject.isNull("WPPATH"))
			lPage.url = jsoObject.getString("WPPATH");
		if (!jsoObject.isNull("WPCODE"))
			lPage.code = jsoObject.getInt("WPCODE");
		return lPage;
	}
}
