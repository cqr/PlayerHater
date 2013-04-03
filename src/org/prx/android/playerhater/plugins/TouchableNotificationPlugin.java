package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.R;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.util.BroadcastReceiver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TouchableNotificationPlugin extends NotificationPlugin {

	private Notification mNotification;
	protected int mNotificationImageResourceId;
	protected Uri mNotificationImageUrl;
	protected boolean mNotificationCanSkip = true;
	private RemoteViews mNotificationView;

	@Override
	public void onSongChanged(Song song) {
		super.onSongChanged(song);
		onAlbumArtChangedToUri(song.getAlbumArt());
	}

	@Override
	public void onTitleChanged(String title) {
		setTextViewText(R.id.title, title);
		super.onTitleChanged(title);
	}

	@Override
	public void onArtistChanged(String artist) {
		setTextViewText(R.id.text, artist);
		super.onArtistChanged(artist);
	}

	@Override
	public void onAlbumArtChanged(int resourceId) {
		mNotificationImageResourceId = resourceId;
		if (mNotificationImageResourceId != 0) {
			mNotificationImageUrl = null;
			setImageViewResource(R.id.image, mNotificationImageResourceId);
		}
	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
		mNotificationImageUrl = url;
		if (mNotificationImageUrl != null) {
			mNotificationImageResourceId = 0;
			setImageViewUri(R.id.image, mNotificationImageUrl);
		}
	}

	@Override
	public void onAudioStarted() {
		onAudioResumed();
		super.onAudioStarted();
	}

	@Override
	public void onAudioPaused() {
		setImageViewResource(R.id.button, R.drawable.zzz_ph_bt_play_enabled);
	}

	@Override
	public void onAudioResumed() {
		setImageViewResource(R.id.button, R.drawable.zzz_ph_bt_pause_enabled);
	}

	@Override
	public void onTransportControlFlagsChanged(int transportControlFlags) {
		if ((transportControlFlags & RemoteControlClient.FLAG_KEY_MEDIA_NEXT) == 0) {
			setViewVisibility(R.id.skip, View.GONE);
		} else {
			setViewVisibility(R.id.skip, View.VISIBLE);
		}

		if ((transportControlFlags & RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS) == 0) {
			setViewVisibility(R.id.back, View.GONE);
		} else {
			setViewVisibility(R.id.back, View.VISIBLE);
		}

		if ((transportControlFlags & (RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
				| RemoteControlClient.FLAG_KEY_MEDIA_PLAY | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE)) != 0) {
			setViewVisibility(R.id.button, View.VISIBLE);
		} else {
			setViewVisibility(R.id.button, View.GONE);
		}

		if ((transportControlFlags & RemoteControlClient.FLAG_KEY_MEDIA_STOP) != 0) {
			setViewVisibility(R.id.stop, View.VISIBLE);
		} else {
			setViewVisibility(R.id.stop, View.GONE);
		}
	}

	@Override
	protected Notification getNotification() {
		if (mNotification == null)
			mNotification = buildNotification();
		return mNotification;
	}

	public void setIntentClass(Class<? extends Activity> intentClass) {
		Intent i = new Intent(getContext(), intentClass);
		i.putExtra("fromPlayerHaterNotification", true);
		mContentIntent = PendingIntent.getActivity(getContext(), 777, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mNotification.contentIntent = mContentIntent;
	}

	protected RemoteViews getNotificationView() {
		if (mNotificationView == null) {
			mNotificationView = buildNotificationView();
			setListeners(mNotificationView);
		}

		mNotificationView.setTextViewText(R.id.title, mNotificationTitle);
		mNotificationView.setTextViewText(R.id.text, mNotificationText);
		if (mNotificationImageUrl != null) {
			setImageViewUri(R.id.image, mNotificationImageUrl);
		} else if (mNotificationImageResourceId != 0) {
			mNotificationView.setImageViewResource(R.id.image,
					mNotificationImageResourceId);
		}

		return mNotificationView;
	}

	protected void setListeners(RemoteViews view) {
		view.setOnClickPendingIntent(R.id.skip,
				getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_NEXT));
		view.setOnClickPendingIntent(R.id.button,
				getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
		view.setOnClickPendingIntent(R.id.stop,
				getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_STOP));
		view.setOnClickPendingIntent(R.id.back,
				getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_PREVIOUS));
	}

	protected RemoteViews buildNotificationView() {
		return new RemoteViews(getContext().getPackageName(),
				R.layout.zzz_ph_hc_notification);
	}

	private PendingIntent getMediaButtonPendingIntent(int keycode) {
		Intent intent = new Intent(getContext(), BroadcastReceiver.class);
		intent.setAction(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
				KeyEvent.ACTION_UP, keycode));
		return PendingIntent.getBroadcast(getContext(), keycode, intent, 0);
	}

	@SuppressWarnings("deprecation")
	protected Notification buildNotification() {
		return getNotificationBuilder().getNotification();
	}

	protected Notification.Builder getNotificationBuilder() {
		return new Notification.Builder(getContext()).setAutoCancel(false)
				.setSmallIcon(R.drawable.zzz_ph_ic_notification)
				.setTicker("Playing: " + mNotificationTitle)
				.setContent(getNotificationView())
				.setContentIntent(mContentIntent);
	}

	protected void setTextViewText(int id, String text) {
		if (mNotificationView != null) {
			mNotificationView.setTextViewText(id, text);
		}
	}

	protected void setViewEnabled(int viewId, boolean enabled) {
		if (mNotificationView != null) {
			mNotificationView.setBoolean(viewId, "setEnabled", enabled);
		}
	}

	protected void setViewVisibility(int viewId, int visible) {
		if (mNotificationView != null) {
			mNotificationView.setViewVisibility(viewId, visible);
		}
	}

	protected void setImageViewResource(int id, int resourceId) {
		if (mNotificationView != null) {
			mNotificationView.setImageViewResource(id, resourceId);
		}
	}

	protected void setImageViewUri(int id, Uri contentUri) {
		if (mNotificationView != null) {
			mNotificationView.setImageViewUri(id, contentUri);
		}
	}
}
