package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;

import android.net.Uri;
import android.util.Log;

public abstract class AbstractPlugin implements PlayerHaterPlugin {

	private static final String TAG = "AbstractPlugin";

	@Override
	public void onPlaybackStarted() {
	}
	
	@Override
	public void onPlaybackResumed() {
		Log.w(TAG, "Forwarding a call to onPlaybackResumed => onPlaybackStarted.");
		onPlaybackStarted();
	}

	@Override
	public void onPlaybackStopped() {
	}

	@Override
	public void onTitleChanged(String title) {
	}

	@Override
	public void onArtistChanged(String artist) {
	}

	@Override
	public void onAlbumArtChanged(int resourceId) {
	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
	}

	@Override
	public void onSongChanged(Song song) {
	}

	@Override
	public void onDurationChanged(int duration) {
	}

	@Override
	public void onAudioLoading() {
	}

	@Override
	public void onPlaybackPaused() {
	}

	@Override
	public void onNextTrackAvailable() {
	}

	@Override
	public void onNextTrackUnavailable() {
	}

}
