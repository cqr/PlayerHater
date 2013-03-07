package org.prx.android.playerhater.plugins;

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
import android.view.View;
import android.widget.RemoteViews;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TouchableNotificationPlugin extends NotificationPlugin {

	private Notification mNotification;
	protected int mNotificationImageResourceId;
	protected Uri mNotificationImageUrl;
	protected boolean mNotificationCanSkip = true; 

	private RemoteViews mCollapsedView;
	
	public TouchableNotificationPlugin(PlayerHaterService service) {
		super(service);
	}

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
			updateNotification();
		}
	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
		mNotificationImageUrl = url;
		if (mNotificationImageUrl != null) {
			mNotificationImageResourceId = 0;
			setImageViewUri(R.id.image, mNotificationImageUrl);
			updateNotification();
		}
	}
	
	@Override
	public void onPlaybackStarted() {
		super.onPlaybackStarted();
		setImageViewResource(R.id.button, R.drawable.__player_hater_pause);
		updateNotification();
	}

	@Override
	public void onPlaybackPaused() {
		setImageViewResource(R.id.button, R.drawable.__player_hater_play);
		updateNotification();
	}

	@Override
	protected Notification getNotification() {
		if (mNotification == null) {
			mNotification = buildNotification();
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
	public void onNextTrackAvailable() {
		this.mNotificationCanSkip = true; 
		setViewVisibility(R.id.skip, View.VISIBLE); 
		updateNotification();
	}
	
	@Override
	public void onNextTrackUnavailable() {
		this.mNotificationCanSkip = false; 
		setViewVisibility(R.id.skip, View.GONE); 
		updateNotification();
	}

	private RemoteViews getNotificationView() {
		if (mCollapsedView == null) {
			mCollapsedView = new RemoteViews(mService.getBaseContext()
					.getPackageName(), R.layout.__player_hater_notification);
			setListeners(mCollapsedView);
		}

		mCollapsedView.setTextViewText(R.id.title, mNotificationTitle);
		mCollapsedView.setTextViewText(R.id.text, mNotificationText);
		if (mNotificationImageUrl != null) { 
			setImageViewUri(R.id.image, mNotificationImageUrl);
		} else if (mNotificationImageResourceId != 0) { 
			mCollapsedView.setImageViewResource(R.id.image,
				mNotificationImageResourceId);
		}
		
		if (mNotificationCanSkip) { 
			this.onNextTrackAvailable(); 
		} else { 
			this.onNextTrackUnavailable(); 
		}

		return mCollapsedView;
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
	
	private PendingIntent getMediaButtonPendingIntent(int keycode) {
		Intent intent = new Intent(mService.getBaseContext(),
				BroadcastReceiver.class);
		intent.setAction(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
				KeyEvent.ACTION_UP, keycode));
		return PendingIntent.getBroadcast(mService.getBaseContext(), keycode, intent,
				0);
	}
	
	@SuppressWarnings("deprecation")
	protected Notification buildNotification() {
		return getNotificationBuilder().getNotification();
	}

	protected Notification.Builder getNotificationBuilder() {
		return new Notification.Builder(mService.getBaseContext())
		.setAutoCancel(false)
		.setSmallIcon(R.drawable.__player_hater_icon)
		.setTicker("Playing: " + mNotificationTitle)
		.setContent(getNotificationView());
	}
	
	protected void setTextViewText(int id, String text) {
		if (mCollapsedView != null) {
			mCollapsedView.setTextViewText(id, text);
		}
	}
	
	protected void setViewEnabled(int viewId, boolean enabled) {
		if (mCollapsedView != null) {
			mCollapsedView.setBoolean(viewId, "setEnabled", enabled);
		}
	}
	
	protected void setViewVisibility(int viewId, int visible) {
		if (mCollapsedView != null) {
			mCollapsedView.setViewVisibility(viewId, visible);
		}
	}
	
	protected void setImageViewResource(int id, int resourceId) {
		if (mCollapsedView != null) {
			mCollapsedView.setImageViewResource(id, resourceId);
		}
	}
	
	protected void setImageViewUri(int id, Uri contentUri) {
		if (mCollapsedView != null) {
			mCollapsedView.setImageViewUri(id, contentUri);
		}
	}
}
