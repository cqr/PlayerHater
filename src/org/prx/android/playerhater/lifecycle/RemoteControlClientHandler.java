package org.prx.android.playerhater.lifecycle;

import java.net.URL;

import org.prx.android.playerhater.BroadcastReceiver;
import org.prx.android.playerhater.Song;

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
public class RemoteControlClientHandler implements
		LifecycleListener.RemoteControl {

	private Context mContext;
	private RemoteControlClient mRemoteControlClient;
	private AudioManager mAudioManager;

	public RemoteControlClientHandler(Context context) {
		mContext = context;
	}

	@Override
	public void setIsPlaying(boolean isPlaying) {
		if (isPlaying) {
			getRemoteControlClient().setPlaybackState(
					RemoteControlClient.PLAYSTATE_PLAYING);
		} else {
			getRemoteControlClient().setPlaybackState(
					RemoteControlClient.PLAYSTATE_PAUSED);
		}
	}
	
	@Override
	public void start(Song forSong, int duration) {
		String imageUriScheme = forSong.getUri().getScheme();
		Bitmap image = null;

		try {
			if (imageUriScheme.equals(ContentResolver.SCHEME_CONTENT)
					|| imageUriScheme
							.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
				image = BitmapFactory
						.decodeStream(mContext.getContentResolver()
								.openInputStream(forSong.getUri()));
			} else {
				image = BitmapFactory.decodeStream(new URL(forSong.getUri()
						.toString()).openStream());
			}
		} catch (Exception e) {
		}

		getRemoteControlClient()
				.editMetadata(true)
				.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
						forSong.getTitle())
				.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
						forSong.getArtist())
				.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration)
				.putBitmap(
						RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
						image).apply();
		getRemoteControlClient().setPlaybackState(
				RemoteControlClient.PLAYSTATE_PLAYING);

		getAudioManager().registerRemoteControlClient(getRemoteControlClient());
	}

	@Override
	public void stop() {
		getAudioManager().unregisterRemoteControlClient(
				getRemoteControlClient());
	}

	@Override
	public void setTitle(String title) {
		getRemoteControlClient().editMetadata(false)
				.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, title)
				.apply();
	}

	@Override
	public void setArtist(String artist) {
		getRemoteControlClient().editMetadata(false)
				.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, artist)
				.apply();
	}

	@Override
	public void setAlbumArt(int resourceId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAlbumArt(Uri url) {
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
			mRemoteControlClient
					.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE);
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

}
