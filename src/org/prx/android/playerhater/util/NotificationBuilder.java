package org.prx.android.playerhater.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.os.Build;

public class NotificationBuilder {
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static Notification.Builder init(Context context) {
		return new Notification.Builder(context);
	} 
}
