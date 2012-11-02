package org.prx.android.playerhater;

import com.jakewharton.notificationcompat2.NotificationCompat2;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.RemoteViews;

public class NotificationHandler {
	
	protected static final int NOTIFICATION_NU = 9747245;
	
	public static final int PLAY_PAUSE_CLICK_ID = 845832; 
	public static final String PLAY_PAUSE_ACTION = "org.prx.playerhater.PlayPause"; 
	
	
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
		//Notification and intent of the notification 
		Notification mNotification = new NotificationCompat2.Builder(mService.getApplicationContext())
        			.setContentTitle(mNotificationTitle)
        			.setContentText(mNotificationText)
        			.setContentIntent(mContentIntent)
        			.setSmallIcon(mNotificationIcon)
        			.setLargeIcon(BitmapFactory.decodeResource(mService.getResources(),mNotificationIcon))
        			.build();

		Intent intent = new Intent(mService.getApplicationContext(), BroadcastReceiver.class);
		intent.setAction(NotificationHandler.PLAY_PAUSE_ACTION); 
		PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(mService.getApplicationContext(), 
				NotificationHandler.PLAY_PAUSE_CLICK_ID, intent, 0);
		
		//Remoteview and intent for my button
		RemoteViews notificationView = new RemoteViews(mService.getBaseContext().getPackageName(), R.layout.notification);
		
		if (notificationView != null) { 
			notificationView.setOnClickPendingIntent(R.id.notPlayPause, playPausePendingIntent);
			notificationView.setTextViewText(R.id.notContentTitle, mNotificationTitle); 
			notificationView.setTextViewText(R.id.notContentText, mNotificationText); 
			notificationView.setImageViewResource(R.id.notImage, mNotificationIcon); 
			mNotification.contentView = notificationView;
		}

		return mNotification;
	}

	private void updateCurrentNotification() {
		if (notificationIsVisible) {
			startNotification();
		}
	}
	
}
