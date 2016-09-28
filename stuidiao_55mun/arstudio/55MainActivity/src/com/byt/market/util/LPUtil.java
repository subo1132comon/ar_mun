package com.byt.market.util;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.byt.market.data.LoadPage;
import com.byt.market.data.LoginInfo;
import com.byt.market.data.MarketUpdateInfo;
import com.byt.market.data.PushInfo;
import com.byt.market.log.LogModel;

/**
 * 
 * @author Administrator 0
 */
public class LPUtil {

	LoadPage lPage;
	LoginInfo lInfo;
	MarketUpdateInfo marketUpdateInfo;
	PushInfo pInfo;

	long pturntime;
	int resultstate;

	public LoginInfo getlInfo() {
		return lInfo;
	}

	public void setlInfo(LoginInfo lInfo) {
		this.lInfo = lInfo;
	}

	/*
	 * 解析m
	 */
	public void parserContent(JSONObject jObject) throws Exception {
		if (jObject == null)
			return;
		if (!jObject.isNull("wpinfo")) {
			JSONObject wel = jObject.getJSONObject("wpinfo");
			parserWel(wel);
		}
		if (!jObject.isNull("clinfo")) {
			JSONObject update = jObject.getJSONObject("clinfo");
			parserUpdate(update);
		}
		if (!jObject.isNull("pturntime")) {
			pturntime = jObject.getLong("pturntime");
		}
		if (!jObject.isNull("pinfo")) {
			JSONObject pinfo = jObject.getJSONObject("pinfo");
			parserPushInfo(pinfo);
		}
		if (!jObject.isNull("datapoint")) {
			String datapoint = jObject.getString("datapoint");
			parserDatapoint(datapoint);
		}

		lInfo = new LoginInfo();
		lInfo.page = lPage;
		lInfo.upate = marketUpdateInfo;

		lInfo.pturntime = pturntime;
		lInfo.resultstate = resultstate;
		lInfo.pInfo = pInfo;
	}

	private void parserDatapoint(String point) throws JSONException {
		if (point == null)
			return;
		if (point != null && point.length() > 0) {
			if (point.contains(",") && point.contains("0")) {
				String[] p0s = point.split(",");
				LogModel.all0Point(p0s);
			} else if (point.equals("1")) {
				LogModel.all1Point();
			} else if (point.equals("0")) {
				LogModel.all0Point();
			}
		}
	}

	private void parserPushInfo(JSONObject pinfo) throws JSONException {
		if (pinfo == null)
			return;
		pInfo = new PushInfo();
		if (!pinfo.isNull("PID"))
			pInfo.id = pinfo.getInt("PID");
		if (!pinfo.isNull("PTYPE"))
			pInfo.ptype = pinfo.getInt("PTYPE");
		if (!pinfo.isNull("PNAME"))
			pInfo.pname = pinfo.getString("PNAME");
		if (!pinfo.isNull("PDESC"))
			pInfo.pdesc = pinfo.getString("PDESC");
		if (!pinfo.isNull("PVALUE"))
			pInfo.pvalue = pinfo.getString("PVALUE");
	}

	private void parserUpdate(JSONObject update) throws Exception {
		if (update == null)
			return;
		this.marketUpdateInfo = new MarketUpdateInfo();
		if (!update.isNull("CLVNAME")) {
			this.marketUpdateInfo.vname = update.getString("CLVNAME");
		}
		if (!update.isNull("CLCODE")) {
			this.marketUpdateInfo.vcode = update.getInt("CLCODE");
		}
		if (!update.isNull("CLSIZE")) {
			this.marketUpdateInfo.length = update.getLong("CLSIZE");
		}
		if (!update.isNull("CLUPDATE")) {
			this.marketUpdateInfo.updatetime = update.getString("CLUPDATE");
		}
		if (!update.isNull("APK")) {
			this.marketUpdateInfo.apk = update.getString("APK");
		}
		if (!update.isNull("UPINFO")) {
			this.marketUpdateInfo.describe = update.getString("UPINFO");
		}
		if (!update.isNull("RIGHT")) {
			this.marketUpdateInfo.right = update.getInt("RIGHT");
		}
	}

	private void parserWel(JSONObject wel) throws Exception {
		lPage = LoadPage.parseLoadPage(wel);
	}
}
