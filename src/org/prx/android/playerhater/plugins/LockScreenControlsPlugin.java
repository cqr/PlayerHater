package org.prx.android.playerhater.plugins;

import java.net.URL;

import org.prx.android.playerhater.Song;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LockScreenControlsPlugin extends AudioFocusPlugin {

	private RemoteControlClient mRemoteControlClient;

	@Override
	public void onAudioStarted() {
		super.onAudioStarted();
		getRemoteControlClient().setPlaybackState(
				RemoteControlClient.PLAYSTATE_PLAYING);
		getAudioManager().registerRemoteControlClient(getRemoteControlClient());
	}

	@Override
	public void onAudioPaused() {
		getRemoteControlClient().setPlaybackState(
				RemoteControlClient.PLAYSTATE_PAUSED);
	}

	@Override
	public void onDurationChanged(int duration) {
		getRemoteControlClient()
				.editMetadata(false)
				.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration)
				.apply();
	}

	@Override
	public void onSongChanged(Song song) {

		String imageUriScheme = song.getUri().getScheme();
		Bitmap image = null;

		try {
			if (imageUriScheme.equals(ContentResolver.SCHEME_CONTENT)
					|| imageUriScheme
							.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
				image = BitmapFactory.decodeStream(getContext()
						.getContentResolver().openInputStream(song.getUri()));
			} else {
				image = BitmapFactory.decodeStream(new URL(song.getUri()
						.toString()).openStream());
			}
		} catch (Exception e) {
		}

		getRemoteControlClient()
				.editMetadata(true)
				.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
						song.getTitle())
				.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
						song.getArtist())
				.putBitmap(
						RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
						image).apply();
	}

	@Override
	public void onAudioStopped() {
		super.onAudioStopped();
		getAudioManager().unregisterRemoteControlClient(
				getRemoteControlClient());
	}

	@Override
	public void onTitleChanged(String title) {
		getRemoteControlClient().editMetadata(false)
				.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, title)
				.apply();
	}

	@Override
	public void onArtistChanged(String artist) {
		getRemoteControlClient().editMetadata(false)
				.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, artist)
				.apply();
	}

	@Override
	public void onAlbumArtChanged(int resourceId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
		// TODO Auto-generated method stub

	}

	private RemoteControlClient getRemoteControlClient() {
		if (mRemoteControlClient == null) {
			Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			mediaButtonIntent.setComponent(getEventReceiver());
			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					getContext(), 0, mediaButtonIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			mRemoteControlClient = new RemoteControlClient(pendingIntent);
			mRemoteControlClient.setTransportControlFlags(getTCFs());
		}
		return mRemoteControlClient;
	}

	private int getTCFs() {
		int tcfs = RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
				| RemoteControlClient.FLAG_KEY_MEDIA_STOP
				| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
				| RemoteControlClient.FLAG_KEY_MEDIA_NEXT;
		return tcfs;
	}

}
