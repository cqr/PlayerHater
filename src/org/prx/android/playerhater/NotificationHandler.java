package org.prx.android.playerhater;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.RemoteViews;

public class NotificationHandler {
	
	
	
	protected static final int NOTIFICATION_NU = 9747245;
	
	private boolean notificationIsVisible = false;
	
	private String mNotificationTitle = "PlayerHater";
	private String mNotificationText = "Version 0.1.0";
	private final PlaybackService mService;
	private PendingIntent mContentIntent;
	private RemoteViews mNotificationView;
	private int mNotificationIcon;
	private Notification mNotification;
	
	public NotificationHandler(PlaybackService service) {
		mService = service;
	}
	
	
	public void startNotification() {
		notificationIsVisible = true;
		mService.startForeground(NOTIFICATION_NU, getNotification());
	}
	
	public void stopNotification() {
		notificationIsVisible = false;
		mService.stopForeground(true);
	}

	public void setTitle(String notificationTitle) {
		mNotificationTitle = notificationTitle;
		if (notificationIsVisible) {
			startNotification();
		}
	}
	
	public void setText(String notificationText) {
		mNotificationText = notificationText;
		if (notificationIsVisible) {
			startNotification();
		}
	}


	public void setIntentClass(Class<? extends Activity> intentClass) {
		mContentIntent = PendingIntent.getActivity(
				mService.getApplicationContext(), 777, new Intent(
						mService.getApplicationContext(), intentClass),
				PendingIntent.FLAG_UPDATE_CURRENT);
		if (notificationIsVisible) {
			startNotification();
		}
	}


	public void setView(RemoteViews remoteViews) {
		mNotificationView = remoteViews;
		if (notificationIsVisible) {
			startNotification();
		}
	}


	public void setNotificationIcon(int notificationIcon) {
		mNotificationIcon = notificationIcon;
		if (notificationIsVisible) {
			startNotification();
		}
	}
	
	private Notification getNotification() {
		if (mNotification == null) {
			mNotification = new Notification(mNotificationIcon, mNotificationTitle, 0);
		}
		mNotification.setLatestEventInfo(mService, mNotificationTitle, mNotificationText, mContentIntent);
		mNotification.contentView = mNotificationView;
		return mNotification;
	}

	
}
