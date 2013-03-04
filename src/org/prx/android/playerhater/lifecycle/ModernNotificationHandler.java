package org.prx.android.playerhater.lifecycle;

import org.prx.android.playerhater.R;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.PlayerHaterService;
import org.prx.android.playerhater.util.BroadcastReceiver;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.KeyEvent;
import android.widget.RemoteViews;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ModernNotificationHandler extends NotificationHandler implements
		LifecycleListener.RemoteControl {

	public static final int PLAY_PAUSE_CLICK_ID = 845832;
	public static final int STOP_CLICK_ID = 845833;
	public static final int SKIP_ACTION_ID = 845834;
	public static final String PLAY_PAUSE_ACTION = "org.prx.playerhater.PLAYPAUSE";
	public static final String STOP_ACTION = "org.prx.playerhater.STOP";
	public static final String SKIP_ACTION = "org.prx.playerhater.SKIP";

	private Notification mNotification;
	private int mNotificationImageResourceId;
	private boolean mIsPlaying = false;
	private boolean mCanSkipForward = false;

	private Uri mNotificationImageUrl;

	private RemoteViews mCollapsedView;
	private RemoteViews mExpandedView;

	public ModernNotificationHandler(PlayerHaterService service) {
		super(service);
	}

	@Override
	public void setTitle(String title) {
		setTextViewText(R.id.title, title);
		super.setTitle(title);
	}

	private void setTextViewText(int id, String text) {
		if (mCollapsedView != null) {
			mCollapsedView.setTextViewText(id, text);
		}
		if (mExpandedView != null) {
			mExpandedView.setTextViewText(id, text);
		}
	}

	@Override
	public void setArtist(String artist) {
		setTextViewText(R.id.text, artist);
		super.setArtist(artist);
	}

	@Override
	public void setAlbumArt(int resourceId) {
		mNotificationImageResourceId = resourceId;
		if (mNotificationImageResourceId != 0) {
			mNotificationImageUrl = null;
			setImageViewResource(R.id.image, mNotificationImageResourceId);

			updateNotification();
		}
	}

	private void setImageViewResource(int id, int resourceId) {
		if (mCollapsedView != null) {
			mCollapsedView.setImageViewResource(id, resourceId);
		}
		if (mExpandedView != null) {
			mExpandedView.setImageViewResource(id, resourceId);
		}
	}

	@Override
	public void setAlbumArt(Uri url) {
		mNotificationImageUrl = url;
		if (mNotificationImageUrl != null) {
			mNotificationImageResourceId = 0;
			setImageViewUri(R.id.image, mNotificationImageUrl);
			updateNotification();
		}
	}

	private void setImageViewUri(int id, Uri contentUri) {
		if (mCollapsedView != null) {
			mCollapsedView.setImageViewUri(id, contentUri);
		}
		if (mExpandedView != null) {
			mExpandedView.setImageViewUri(id, contentUri);
		}
	}

	@Override
	public void setIsPlaying(boolean isPlaying) {
		mIsPlaying = isPlaying;
		if (mIsPlaying) {
			setImageViewResource(R.id.button, R.drawable.__player_hater_pause);

		} else {
			setImageViewResource(R.id.button, R.drawable.__player_hater_play);
		}
		updateNotification();
	}

	@Override
	protected Notification getNotification() {
		if (mNotification == null) {
			mNotification = new Notification.Builder(mService.getBaseContext())
					.setAutoCancel(false)
					.setSmallIcon(R.drawable.__player_hater_icon)
					.setTicker("Playing: " + mNotificationTitle)
					.setContent(getCollapsedView()).build();
			mNotification.bigContentView = getExpandedView();
		}
		return mNotification;
	}

	public void setIntentClass(Class<? extends Activity> intentClass) {
		Intent i = new Intent(mService.getBaseContext(), intentClass);
		i.putExtra("fromPlayerHaterNotification", true);
		mContentIntent = PendingIntent.getActivity(mService.getBaseContext(),
				777, i, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotification.contentIntent = mContentIntent;
	}

	@Override
	public void setCanSkipForward(boolean canSkipForward) {
		mCanSkipForward = canSkipForward;
		if (mCanSkipForward) {
			setViewEnabled(R.id.skip, true);
		} else {
			setViewEnabled(R.id.skip, false);
		}
		updateNotification();
	}

	private void setViewEnabled(int viewId, boolean enabled) {
		if (mCollapsedView != null) {
			mCollapsedView.setBoolean(viewId, "setEnabled", enabled);
		}
		if (mExpandedView != null) {
			mExpandedView.setBoolean(viewId, "setEnabled", enabled);
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

	private RemoteViews getCollapsedView() {
		if (mCollapsedView == null) {
			mCollapsedView = new RemoteViews(mService.getBaseContext()
					.getPackageName(), R.layout.__player_hater_notification);
			setListeners(mCollapsedView);
		}

		mCollapsedView.setTextViewText(R.id.title, mNotificationTitle);
		mCollapsedView.setTextViewText(R.id.text, mNotificationText);
		mCollapsedView.setImageViewResource(R.id.image,
				mNotificationImageResourceId);

		return mCollapsedView;
	}

	private RemoteViews getExpandedView() {
		if (mExpandedView == null) {
			mExpandedView = new RemoteViews(mService.getBaseContext()
					.getPackageName(),
					R.layout.__player_hater_notification_expanded);
			setListeners(mExpandedView);
		}

		mExpandedView.setTextViewText(R.id.title, mNotificationTitle);
		mExpandedView.setTextViewText(R.id.text, mNotificationText);
		mExpandedView.setImageViewResource(R.id.image,
				mNotificationImageResourceId);

		return mExpandedView;
	}

	private PendingIntent getMediaButtonPendingIntent(int keycode) {
		Intent intent = new Intent(mService.getBaseContext(),
				BroadcastReceiver.class);
		intent.setAction(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
				KeyEvent.ACTION_UP, keycode));
		return PendingIntent.getBroadcast(mService.getBaseContext(), keycode, intent,
				0);
	}

	private void setListeners(RemoteViews view) {
		view.setOnClickPendingIntent(R.id.skip,
				getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_NEXT));
		view.setOnClickPendingIntent(R.id.button,
				getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
		view.setOnClickPendingIntent(R.id.stop,
				getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_STOP));
		view.setOnClickPendingIntent(R.id.back,
				getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_PREVIOUS));
	}

}
