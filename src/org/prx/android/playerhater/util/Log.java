package org.prx.android.playerhater.util;

import org.prx.android.playerhater.BuildConfig;
import org.prx.android.playerhater.PlayerHater;

public class Log {

	public static void v(String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.v(PlayerHater.TAG, msg);
		}
	}

	public static void d(String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.d(PlayerHater.TAG, msg);
		}
	}

	public static void e(String string, Exception e) {
		android.util.Log.e(PlayerHater.TAG, string, e);
	}

}
