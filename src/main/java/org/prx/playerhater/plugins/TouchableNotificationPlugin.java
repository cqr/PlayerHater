/*******************************************************************************
 * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.prx.playerhater.plugins;

import org.prx.playerhater.BroadcastReceiver;
import org.prx.playerhater.R;
import org.prx.playerhater.Song;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Build;
import android.view.KeyEvent;
import android.widget.RemoteViews;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TouchableNotificationPlugin extends NotificationPlugin {

	private Notification mNotification;
	protected Uri mNotificationImageUrl;
	protected boolean mNotificationCanSkip = true;
	protected int mTransportControlFlags = -1;
	private RemoteViews mNotificationView;

	@Override
	public void onSongChanged(Song song) {
		super.onSongChanged(song);
		if (song == null) {
			onAlbumArtChanged(null);
		} else {
			onAlbumArtChanged(song.getAlbumArt());
		}
	}

	@Override
	public void onTitleChanged(String title) {
		setTextViewText(R.id.zzz_ph_notification_title, title);
		super.onTitleChanged(title);
	}

	@Override
	public void onArtistChanged(String artist) {
		setTextViewText(R.id.zzz_ph_notification_text, artist);
		super.onArtistChanged(artist);
	}

	@Override
	public void onAlbumArtChanged(Uri url) {
		mNotificationImageUrl = url;
		setImageViewUri(R.id.zzz_ph_notification_image, mNotificationImageUrl);
	}

	@Override
	public void onAudioStarted() {
		onAudioResumed();
		super.onAudioStarted();
	}

	@Override
	public void onAudioPaused() {
		setImageViewResource(R.id.zzz_ph_play_pause_button,
				R.drawable.zzz_ph_bt_play);
	}

	@Override
	public void onAudioResumed() {
		setImageViewResource(R.id.zzz_ph_play_pause_button,
				R.drawable.zzz_ph_bt_pause);
	}

	@Override
	public void onTransportControlFlagsChanged(int transportControlFlags) {
		mTransportControlFlags = transportControlFlags;
		if (mNotificationView != null) {
			if ((transportControlFlags & RemoteControlClient.FLAG_KEY_MEDIA_NEXT) == 0) {
				setViewEnabled(R.id.zzz_ph_skip_button, false);
			} else {
				setViewEnabled(R.id.zzz_ph_skip_button, true);
			}

			if ((transportControlFlags & RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS) == 0) {
				setViewEnabled(R.id.zzz_ph_back_button, false);
			} else {
				setViewEnabled(R.id.zzz_ph_back_button, true);
			}
		}
	}

	@Override
	protected Notification getNotification() {
		if (mNotification == null)
			mNotification = buildNotification();
		mNotification.tickerText = "Playing: " +  mNotificationTitle; 
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
			mNotificationView.setTextViewText(R.id.zzz_ph_notification_title,
					mNotificationTitle);
			mNotificationView.setTextViewText(R.id.zzz_ph_notification_text,
					mNotificationText);
			if (mNotificationImageUrl != null) {
				setImageViewUri(R.id.zzz_ph_notification_image,
						mNotificationImageUrl);
			}

			if (mTransportControlFlags != -1) {
				onTransportControlFlagsChanged(mTransportControlFlags);
			}
		}

		return mNotificationView;
	}

	protected void setListeners(RemoteViews view) {
		view.setOnClickPendingIntent(R.id.zzz_ph_skip_button,
				getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_NEXT));
		view.setOnClickPendingIntent(R.id.zzz_ph_play_pause_button,
				getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
		view.setOnClickPendingIntent(R.id.zzz_ph_stop_button,
				getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_STOP));
		view.setOnClickPendingIntent(R.id.zzz_ph_back_button,
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
				.setContentIntent(mContentIntent).setOngoing(true).setWhen(0)
				.setOnlyAlertOnce(true);
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
		if (mNotificationView != null && contentUri != null) {
			mNotificationView.setImageViewUri(id, contentUri);
		}
	}
}
