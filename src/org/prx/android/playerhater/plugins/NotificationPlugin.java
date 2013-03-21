package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.R;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.IPlayerHaterBinder;
import org.prx.android.playerhater.util.IPlayerHater;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class NotificationPlugin extends AbstractPlugin {

	protected static final int NOTIFICATION_NU = 0x974732;
	private static final String TAG = "NotificationPlugin";
	protected IPlayerHaterBinder mService;
	protected NotificationManager mNotificationManager;
	protected PendingIntent mContentIntent;
	protected String mNotificationTitle = "PlayerHater";
	protected String mNotificationText = "Version 0.1.0";
	private boolean mIsVisible = false;
	private Notification mNotification;

	public NotificationPlugin() {
	}

	@Override
	public void onServiceStarted(Context context, IPlayerHater playerHater) {
		super.onServiceStarted(context, playerHater);
		mService = (IPlayerHaterBinder) playerHater;

		PackageManager packageManager = context.getPackageManager();
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent resumeActivityIntent = packageManager
				.getLaunchIntentForPackage(getContext().getPackageName());
		resumeActivityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resumeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		mContentIntent = PendingIntent.getActivity(getContext(),
				NOTIFICATION_NU, resumeActivityIntent, 0);
	}

	@Override
	public void onSongChanged(Song song) {
		if (song != null) {
			onTitleChanged(song.getTitle());
			onArtistChanged(song.getArtist());
			onAlbumArtChangedToUri(song.getAlbumArt());
		}
	}

	@Override
	public void onPlaybackStarted() {
		mService.startForeground(NOTIFICATION_NU, getNotification());
		mIsVisible = true;
	}

	@SuppressWarnings("deprecation")
	protected Notification getNotification() {
		if (mNotification == null)
			mNotification = new Notification(R.drawable.zzz_ph_ic_notification,
					"Playing: " + mNotificationTitle, 0);

		mNotification.setLatestEventInfo(getContext(), mNotificationTitle,
				mNotificationText, mContentIntent);
		return mNotification;
	}

	@Override
	public void onPlaybackStopped() {
		mIsVisible = false;
		mService.stopForeground(true);
	}

	@Override
	public void onTitleChanged(String notificationTitle) {
		mNotificationTitle = notificationTitle;
	}

	public void onIntentActivityChanged(PendingIntent contentIntent) {
		mContentIntent = contentIntent;
		if (mNotification != null) {
			mNotification.contentIntent = mContentIntent;
		}
	}

	@Override
	public void onArtistChanged(String notificationText) {
		mNotificationText = notificationText;
	}

	@Override
	public void onChangesComplete() {
		updateNotification();
	}

	protected void updateNotification() {
		Log.d(TAG, getNotification().toString());
		if (mIsVisible) {
			mNotificationManager.notify(NOTIFICATION_NU, getNotification());
		}
	}

}
