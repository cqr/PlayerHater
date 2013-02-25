package org.prx.android.playerhater;

import com.jakewharton.notificationcompat2.NotificationCompat2;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class NotificationHandler {

	protected static final int NOTIFICATION_NU = 9747245;

	public static final int PLAY_PAUSE_CLICK_ID = 845832;
	public static final int STOP_CLICK_ID = 845833;
	public static final String PLAY_PAUSE_ACTION = "org.prx.playerhater.PlayPause";
	public static final String STOP_ACTION = "org.prx.playerhater.Stop";

	private String mNotificationTitle = "PlayerHater";
	private String mNotificationText = "Version 0.1.0";
	private final PlaybackService mService;
	private PendingIntent mContentIntent;
	private RemoteViews mNotificationView;
	private int mNotificationIcon = R.drawable.__player_hater_icon;
	private Notification mNotification;
	private int mNotificationImageResourceId;
	private boolean mIsPlaying = false;
	private boolean mIsVisible = false;

	private Uri mNotificationImageUrl;

	private NotificationManager mNotificationManager;

	public NotificationHandler(PlaybackService service) {
		mService = service;
		mNotificationManager = (NotificationManager) mService
				.getApplicationContext().getSystemService(
						Context.NOTIFICATION_SERVICE);
	}

	public void startNotification(Song forSong) {
		Log.d("NOTIFY", forSong.toString());
		if (forSong != null) {
			setTitle(forSong.getTitle());
			setText(forSong.getArtist());
			setImage(forSong.getAlbumArt());
		}
		mService.startForeground(NOTIFICATION_NU, getNotification());
		mIsVisible = true;
	}
	
	public void resume() {
		if (!mIsVisible) {
			startNotification(null);
		} else {
			updateNotification();
		}
		mIsVisible = true;
	}

	public void stopNotification() {
		mIsVisible = false;
		mService.stopForeground(true);
	}

	public void setTitle(String notificationTitle) {
		mNotificationTitle = notificationTitle;
		if (mNotificationView != null) {
			mNotificationView.setTextViewText(R.id.title, mNotificationTitle);
			updateNotification();
		}
	}

	public void setText(String notificationText) {
		mNotificationText = notificationText;
		if (mNotificationView != null) {
			mNotificationView.setTextViewText(R.id.text, mNotificationText);
			updateNotification();
		}
	}

	public void setImage(int resourceId) {
		mNotificationImageResourceId = resourceId;
		if (mNotificationImageResourceId != 0) {
			mNotificationImageUrl = null;
			if (mNotificationView != null) {
				mNotificationView.setImageViewResource(R.id.image,
						mNotificationImageResourceId);
			}
			updateNotification();
		}
	}

	public void setImage(Uri url) {
		mNotificationImageUrl = url;
		if (mNotificationImageUrl != null) {
			mNotificationImageResourceId = 0;
			if (mNotificationView != null) {
				mNotificationView.setImageViewUri(R.id.image,
						mNotificationImageUrl);
			}
			updateNotification();
		}
	}

	public void setToPause() {
		setIsPlaying(true);
	}

	public void setToPlay() {
		setIsPlaying(false);
	}

	public void setIsPlaying(boolean isPlaying) {
		mIsPlaying = isPlaying;
		if (mNotificationView != null) {
			if (mIsPlaying) {
				mNotificationView.setImageViewResource(R.id.button,
						R.drawable.__player_hater_pause);
			} else {
				mNotificationView.setImageViewResource(R.id.button,
						R.drawable.__player_hater_play);
			}
			updateNotification();
		}
	}

	public void setIntentClass(Class<? extends Activity> intentClass) {
		Intent i = new Intent(mService.getApplicationContext(), intentClass);
		i.putExtra("fromPlayerHaterNotification", true);
		mContentIntent = PendingIntent.getActivity(
				mService.getApplicationContext(), 777, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mNotification.contentIntent = mContentIntent;
	}

	private Notification getNotification() {
		if (this.mNotification != null) {
			return this.mNotification;
		}
		// Notification and intent of the notification
		this.mNotification = new NotificationCompat2.Builder(
				mService.getApplicationContext())
				.setContentTitle(mNotificationTitle)
				.setContentText(mNotificationText)
				.setContentIntent(mContentIntent)
				.setSmallIcon(mNotificationIcon).build();

		Intent intent = new Intent(mService.getApplicationContext(),
				BroadcastReceiver.class);
		intent.setAction(NotificationHandler.PLAY_PAUSE_ACTION);
		PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(
				mService.getApplicationContext(),
				NotificationHandler.PLAY_PAUSE_CLICK_ID, intent, 0);

		Intent stopIntent = new Intent(mService.getApplicationContext(),
				BroadcastReceiver.class);
		stopIntent.setAction(NotificationHandler.STOP_ACTION);
		PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
				mService.getApplicationContext(),
				NotificationHandler.STOP_CLICK_ID, stopIntent, 0);

		// Remoteview and intent for my button
		mNotificationView = new RemoteViews(mService.getBaseContext()
				.getPackageName(), R.layout.__player_hater_notification);

		if (mNotificationView != null) {
			mNotificationView.setOnClickPendingIntent(R.id.button,
					playPausePendingIntent);
			mNotificationView.setOnClickPendingIntent(R.id.stop,
					stopPendingIntent);
			setTitle(mNotificationTitle);
			setText(mNotificationText);
			setImage(mNotificationImageUrl);
			setImage(mNotificationImageResourceId);
			mNotification.contentView = mNotificationView;
		}

		return mNotification;
	}

	private void updateNotification() {
		if (mIsVisible) {
			mNotificationManager.notify(NOTIFICATION_NU, mNotification);
		}
	}

}
