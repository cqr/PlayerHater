package org.prx.android.playerhater;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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
		updateCurrentNotification();
	}
	
	public void setText(String notificationText) {
		mNotificationText = notificationText;
		updateCurrentNotification();
	}


	public void setIntentClass(Class<? extends Activity> intentClass) {
		Intent i = new Intent(mService.getApplicationContext(), intentClass);
		i.putExtra("fromPlayerHaterNotification", true); 
		mContentIntent = PendingIntent.getActivity(
				mService.getApplicationContext(), 777, i,
				PendingIntent.FLAG_UPDATE_CURRENT); 
		updateCurrentNotification();
	}


	public void setView(RemoteViews remoteViews) {
		mNotificationView = remoteViews;
		updateCurrentNotification();
	}


	public void setNotificationIcon(int notificationIcon) {
		mNotificationIcon = notificationIcon;
		updateCurrentNotification();
	}
	
	private Notification getNotification() {
		if (mNotification == null) {
			mNotification = new Notification(mNotificationIcon, mNotificationTitle, 0);
		}
		if (mNotificationView != null) {
			mNotification.contentView = mNotificationView;
		} else {
			mNotification.setLatestEventInfo(mService.getApplicationContext(), mNotificationTitle, mNotificationText, mContentIntent);
		}
		return mNotification;
	}

	private void updateCurrentNotification() {
		if (notificationIsVisible) {
			startNotification();
		}
	}
	
}
