package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.PlayerHaterPluginInterface;

import android.net.Uri;

public class PlayerHaterPlugin implements PlayerHaterPluginInterface {

	@Override
	public void onPlay() {
	}
	
	@Override
	public void onResume() {
		onPlay();
	}

	@Override
	public void onStop() {
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
	public void onLoading() {
	}

	@Override
	public void onPause() {
	}

	@Override
	public void onNextTrackAvailable() {
	}

	@Override
	public void onNextTrackUnavailable() {
	}

}
