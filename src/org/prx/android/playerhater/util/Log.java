package org.prx.android.playerhater.util;

import org.prx.android.playerhater.BuildConfig;
import org.prx.android.playerhater.service.AbsPlaybackService;

public class Log {

	public static void v(String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.v(AbsPlaybackService.TAG, msg);
		}
	}

	public static void d(String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.d(AbsPlaybackService.TAG, msg);
		}
	}

	public static void e(String string, Exception e) {
		android.util.Log.e(AbsPlaybackService.TAG, string, e);
	}

}
