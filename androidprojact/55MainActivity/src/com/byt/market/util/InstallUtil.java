package com.byt.market.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.byt.market.tools.RootTools;
import com.byt.market.tools.RootTools.Result;

public class InstallUtil {

	public static boolean installData(File apk) {
		boolean res = true;
		final List<Boolean> result = new ArrayList<Boolean>();
		try {
			if (!RootTools.isAccessGiven()) {
				return false;
			}
			RootTools.sendShell(
					new String[] { "chmod 644 " + apk.getAbsolutePath(),
							"pm install -r -l " + apk.getAbsolutePath() }, 10,
					new Result() {

						public void processError(String line) throws Exception {
							if (!line.contains("pkg:")) {
								result.add(false);
							}
						}

						public void process(String line) throws Exception {
						}

						public void onFailure(Exception ex) {
							result.add(false);
						}

						public void onComplete(int diag) {
						}
					}, true, true);
		} catch (InterruptedException e) {
			result.add(false);
		}
		if (result.contains(Boolean.valueOf(false))) {
			res = false;
		}
		return res;
	}

	public static boolean uninstallApk(String p) {
		boolean res = false;
		final List<Boolean> result = new ArrayList<Boolean>();
		try {
			RootTools.sendShell("pm uninstall " + p, new Result() {

				@Override
				public void processError(String line) throws Exception {
					result.add(false);
				}

				@Override
				public void process(String line) throws Exception {
				}

				@Override
				public void onFailure(Exception ex) {
					result.add(false);
				}

				@Override
				public void onComplete(int diag) {
				}
			});
		} catch (InterruptedException e) {
			result.add(false);
		}
		if (!result.contains(Boolean.valueOf(false))) {
			res = true;
		}
		return res;
	}

	public static boolean installSlefData(final Context context, String path,
			String pname, String opname) {
		String[] commands = new String[] {
				"chmod 644 " + path + File.separator + pname + "\n",
				"chown system.system " + path + File.separator + pname + "\n",
				"busybox mount -o remount,rw /data" + "\n",
				"rm /data/app/" + opname + "\n",
				"rm /data/dalvik-cache/data@app@" + opname + "@classes.dex\n",
				"busybox cp -p " + path + File.separator + pname
						+ " /data/app/" + pname + "\n" };
		boolean res = false;
		final List<Boolean> result = new ArrayList<Boolean>();
		try {
			RootTools.sendShell(commands, 50, new Result() {

				@Override
				public void processError(String line) throws Exception {
					// write: No space left on device
					if ("write: No space left on device".equals(line)) {
						result.add(false);
					} else if (line != null
							&& line.contains("No such file or directory")) {
						result.add(false);
					} else if (line != null
							&& line.contains("Read-only file system")) {
						result.add(false);
					}
				}

				@Override
				public void process(String line) throws Exception {
				}

				@Override
				public void onFailure(Exception ex) {
				}

				@Override
				public void onComplete(int diag) {
				}
			}, true, true);
		} catch (InterruptedException e) {
			result.add(false);
		}
		if (!result.contains(Boolean.valueOf(false))) {
			res = true;
		}
		return res;
	}
}
