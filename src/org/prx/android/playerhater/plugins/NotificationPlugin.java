package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.R;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.PlayerHaterService;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class NotificationPlugin extends PlayerHaterPlugin {
	
	protected static final int NOTIFICATION_NU = 9747245;
	protected PlayerHaterService mService;
	protected NotificationManager mNotificationManager;
	protected PendingIntent mContentIntent;
	protected String mNotificationTitle = "PlayerHater";
	protected String mNotificationText = "Version 0.1.0";
	private boolean mIsVisible = false;
	private Notification mNotification;
	
	public NotificationPlugin(PlayerHaterService service) {
		mService = service;
		Context c = mService.getBaseContext();
		PackageManager packageManager = c.getPackageManager();
		mNotificationManager = (NotificationManager) c
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent resumeActivityIntent = packageManager
				.getLaunchIntentForPackage(c.getPackageName());
		resumeActivityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mContentIntent = PendingIntent.getActivity(c, NOTIFICATION_NU,
				resumeActivityIntent, 0);
	}

	@Override
	public void setIsPlaying(boolean isPlaying) {
		// XXX TODO FIXME
	}

	@Override
	public void setIsLoading(Song forSong) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(Song forSong, int duration) {
		if (forSong != null) {
			setTitle(forSong.getTitle());
			setArtist(forSong.getArtist());
			setAlbumArt(forSong.getAlbumArt());
			setIsPlaying(true);
		}
		mService.startForeground(NOTIFICATION_NU, getNotification());
		mIsVisible  = true;
	}

	@SuppressWarnings("deprecation")
	protected Notification getNotification() {
		if (mNotification == null)
			mNotification = new Notification(R.drawable.__player_hater_icon, "Playing: " + mNotificationTitle, 0);
		
		mNotification.setLatestEventInfo(mService.getBaseContext(), mNotificationTitle, mNotificationText, mContentIntent);

		return mNotification;
	}
	
	@Override
	public void setAlbumArt(Uri albumArt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		mIsVisible = false;
		mService.stopForeground(true);
	}
	
	@Override
	public void setTitle(String notificationTitle) {
		mNotificationTitle = notificationTitle;
		updateNotification();
	}
	
	
	@Override
	public void setArtist(String notificationText) {
		mNotificationText = notificationText;
		updateNotification();
	}
	
	protected void updateNotification() {
		if (mIsVisible) {
			mNotificationManager.notify(NOTIFICATION_NU, getNotification());
		}
	}

}
