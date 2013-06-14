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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Build;

import org.prx.playerhater.Song;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LockScreenControlsPlugin extends AudioFocusPlugin {

	private RemoteControlClient mRemoteControlClient;
	private Bitmap mAlbumArt;
	private String mArtist;
	private String mTitle;

	private int mTransportControlFlags = RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
			| RemoteControlClient.FLAG_KEY_MEDIA_STOP
			| RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
			| RemoteControlClient.FLAG_KEY_MEDIA_NEXT;

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

		if (song == null) {
			onAlbumArtChanged(null);
			onTitleChanged(null);
			onArtistChanged(null);
			return;
		}
		if (song.getAlbumArt() != null) {
			onAlbumArtChanged(song.getAlbumArt());
		}

		onTitleChanged(song.getTitle());
		onArtistChanged(song.getArtist());
	}

	@Override
	public void onAudioStopped() {
		getRemoteControlClient().setPlaybackState(
				RemoteControlClient.PLAYSTATE_STOPPED);
		super.onAudioStopped();
	}

	@Override
	public void onTitleChanged(String title) {
		mTitle = title;
	}

	@Override
	public void onArtistChanged(String artist) {
		mArtist = artist;
	}

	@Override
	public void onAlbumArtChanged(Uri uri) {
		if (uri != null) {
			if (uri.getScheme().equals("android.resource") && uri.getLastPathSegment() != null) {
				mAlbumArt = BitmapFactory.decodeResource(getContext()
						.getResources(), Integer.valueOf(uri
						.getLastPathSegment()));
			} else if (uri.getScheme().equals("content")) {
				InputStream stream = null;
				try {
					stream = getContext().getContentResolver().openInputStream(
							uri);
					mAlbumArt = BitmapFactory.decodeStream(stream);
				} catch (FileNotFoundException e) {
					mAlbumArt = null;
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {}
					}
				}

			} else {
				InputStream stream = null;
				try {
					stream = new URL(uri.toString()).openStream();
					mAlbumArt = BitmapFactory.decodeStream(stream);
				} catch (MalformedURLException e) {
					mAlbumArt = null;
				} catch (IOException e) {
					mAlbumArt = null;
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {}
					}
				}
			}
		} else {
			mAlbumArt = null;
		}
		getRemoteControlClient().editMetadata(false).putBitmap(
				RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
				mAlbumArt);
	}

	@Override
	public void onTransportControlFlagsChanged(int transportControlFlags) {
		mTransportControlFlags = transportControlFlags;
		getRemoteControlClient()
				.setTransportControlFlags(transportControlFlags);
	}

	@Override
	public void onChangesComplete() {
		getRemoteControlClient().setTransportControlFlags(
				mTransportControlFlags);
		getRemoteControlClient()
				.editMetadata(false)
				.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, mTitle)
				.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, mArtist)
				.putBitmap(
						RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
						mAlbumArt).apply();
	}

	private RemoteControlClient getRemoteControlClient() {
		if (mRemoteControlClient == null) {
			Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			mediaButtonIntent.setComponent(getEventReceiver());
			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					getContext(), 0, mediaButtonIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			mRemoteControlClient = new RemoteControlClient(pendingIntent);
		}
		return mRemoteControlClient;
	}
}
