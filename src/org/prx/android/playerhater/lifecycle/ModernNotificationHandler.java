package org.prx.android.playerhater.lifecycle;

import org.prx.android.playerhater.R;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.PlayerHaterService;
import org.prx.android.playerhater.util.BroadcastReceiver;

import com.jakewharton.notificationcompat2.NotificationCompat2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ModernNotificationHandler implements
		LifecycleListener.RemoteControl {

	protected static final int NOTIFICATION_NU = 9747245;

	public static final int PLAY_PAUSE_CLICK_ID = 845832;
	public static final int STOP_CLICK_ID = 845833;
	public static final int SKIP_ACTION_ID = 845834;
	public static final String PLAY_PAUSE_ACTION = "org.prx.playerhater.PLAYPAUSE";
	public static final String STOP_ACTION = "org.prx.playerhater.STOP";
	public static final String SKIP_ACTION = "org.prx.playerhater.SKIP";

	private static final String TAG = "DAWG";

	private String mNotificationTitle = "PlayerHater";
	private String mNotificationText = "Version 0.1.0";
	private final PlayerHaterService mService;
	private PendingIntent mContentIntent;
	private RemoteViews mNotificationView;
	private int mNotificationIcon = R.drawable.__player_hater_icon;
	private Notification mNotification;
	private int mNotificationImageResourceId;
	private boolean mIsPlaying = false;
	private boolean mIsVisible = false;
	private boolean mCanSkipForward = false;

	private Uri mNotificationImageUrl;

	private NotificationManager mNotificationManager;

	public ModernNotificationHandler(PlayerHaterService service) {
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
		Log.d(TAG, "THIS IS NOT A JOKE");
	}

	@Override
	public void start(Song forSong, int duration) {
		Log.d(TAG, "THIS IS NOT A JOKE " + forSong);
		if (forSong != null) {
			setTitle(forSong.getTitle());
			setArtist(forSong.getArtist());
			setAlbumArt(forSong.getAlbumArt());
			setIsPlaying(true);
		}
		mService.startForeground(NOTIFICATION_NU, getNotification());
		mIsVisible = true;
	}

	@Override
	public void stop() {
		mIsVisible = false;
		mService.stopForeground(true);
	}

	@Override
	public void setTitle(String notificationTitle) {
		mNotificationTitle = notificationTitle;
		if (mNotificationView != null) {
			mNotificationView.setTextViewText(R.id.title, mNotificationTitle);
			updateNotification();
		}
	}

	@Override
	public void setArtist(String notificationText) {
		mNotificationText = notificationText;
		if (mNotificationView != null) {
			mNotificationView.setTextViewText(R.id.text, mNotificationText);
			updateNotification();
		}
	}

	@Override
	public void setAlbumArt(int resourceId) {
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

	@Override
	public void setAlbumArt(Uri url) {
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

	@Override
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
		Intent i = new Intent(mService.getBaseContext(), intentClass);
		i.putExtra("fromPlayerHaterNotification", true);
		mContentIntent = PendingIntent.getActivity(mService.getBaseContext(),
				777, i, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotification.contentIntent = mContentIntent;
	}

	private Notification getNotification() {
		if (this.mNotification != null) {
			return this.mNotification;
		}
		// Notification and intent of the notification
		this.mNotification = new NotificationCompat2.Builder(
				mService.getBaseContext()).setContentTitle(mNotificationTitle)
				.setContentText(mNotificationText)
				.setContentIntent(mContentIntent)
				.setSmallIcon(mNotificationIcon).build();

		Intent intent = new Intent(mService.getBaseContext(),
				BroadcastReceiver.class);
		intent.setAction(ModernNotificationHandler.PLAY_PAUSE_ACTION);
		PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(
				mService.getBaseContext(),
				ModernNotificationHandler.PLAY_PAUSE_CLICK_ID, intent, 0);

		Intent stopIntent = new Intent(mService.getBaseContext(),
				BroadcastReceiver.class);
		stopIntent.setAction(ModernNotificationHandler.STOP_ACTION);
		PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
				mService.getBaseContext(),
				ModernNotificationHandler.STOP_CLICK_ID, stopIntent, 0);

		Intent skipIntent = new Intent(mService.getBaseContext(),
				BroadcastReceiver.class);
		skipIntent.setAction(Intent.ACTION_MEDIA_BUTTON);
		skipIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
				KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
		PendingIntent skipPendingIntent = PendingIntent.getBroadcast(
				mService.getBaseContext(), SKIP_ACTION_ID, skipIntent, 0);

		// Remoteview and intent for my button
		mNotificationView = new RemoteViews(mService.getBaseContext()
				.getPackageName(), R.layout.__player_hater_notification);

		if (mNotificationView != null) {
			mNotificationView.setOnClickPendingIntent(R.id.button,
					playPausePendingIntent);
			mNotificationView.setOnClickPendingIntent(R.id.stop,
					stopPendingIntent);
			mNotificationView.setOnClickPendingIntent(R.id.skip,
					skipPendingIntent);
			setCanSkipForward(mCanSkipForward);
			setTitle(mNotificationTitle);
			setArtist(mNotificationText);
			setAlbumArt(mNotificationImageUrl);
			setAlbumArt(mNotificationImageResourceId);
			mNotification.contentView = mNotificationView;
		}

		return mNotification;
	}

	private void updateNotification() {
		if (mIsVisible) {
			mNotificationManager.notify(NOTIFICATION_NU, mNotification);
		}
	}

	@Override
	public void setCanSkipForward(boolean canSkipForward) {
		mCanSkipForward = canSkipForward;
		if (mNotificationView != null) {
			if (mCanSkipForward) {
				mNotificationView.setViewVisibility(R.id.skip, View.VISIBLE);
			} else {
				mNotificationView.setViewVisibility(R.id.skip, View.GONE);
			}
			updateNotification();
		}
	}

	@Override
	public void setCanSkipBack(boolean canSkipBack) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIsLoading(Song forSong) {
		// TODO Auto-generated method stub

	}
}
