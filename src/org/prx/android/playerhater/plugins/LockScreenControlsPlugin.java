package org.prx.android.playerhater.plugins;

import java.net.URL;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.util.BroadcastReceiver;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LockScreenControlsPlugin extends AbstractPlugin {

	private Context mContext;
	private RemoteControlClient mRemoteControlClient;
	private AudioManager mAudioManager;
	private boolean mCanSkipForward = false;

	public LockScreenControlsPlugin(Context context) {
		mContext = context;
	}

	@Override
	public void onPlaybackResumed() {
		getRemoteControlClient().setPlaybackState(
				RemoteControlClient.PLAYSTATE_PLAYING);
	}

	@Override
	public void onPlaybackPaused() {
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
				image = BitmapFactory.decodeStream(mContext
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
	public void onPlaybackStarted() {
		getRemoteControlClient().setPlaybackState(
				RemoteControlClient.PLAYSTATE_PLAYING);

		getAudioManager().registerRemoteControlClient(getRemoteControlClient());
	}

	@Override
	public void onPlaybackStopped() {
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
			ComponentName eventReciever = new ComponentName(mContext,
					BroadcastReceiver.class);
			Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			mediaButtonIntent.setComponent(eventReciever);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
					0, mediaButtonIntent, 0);
			mRemoteControlClient = new RemoteControlClient(pendingIntent);
			mRemoteControlClient.setTransportControlFlags(getTCFs());
		}
		return mRemoteControlClient;
	}

	private AudioManager getAudioManager() {
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) mContext
					.getSystemService(Context.AUDIO_SERVICE);
		}

		return mAudioManager;
	}

	@Override
	public void onNextTrackAvailable() {
		mCanSkipForward = true;
		getRemoteControlClient().setTransportControlFlags(getTCFs());
	}

	@Override
	public void onNextTrackUnavailable() {
		mCanSkipForward = false;
		getRemoteControlClient().setTransportControlFlags(getTCFs());
	}

	private int getTCFs() {
		int tcfs = RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
				| RemoteControlClient.FLAG_KEY_MEDIA_STOP
				| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS;
		if (mCanSkipForward)
			tcfs = tcfs | RemoteControlClient.FLAG_KEY_MEDIA_NEXT;

		return tcfs;
	}

}
