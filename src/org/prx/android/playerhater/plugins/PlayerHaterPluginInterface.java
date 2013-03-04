package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.Song;

import android.net.Uri;

public interface PlayerHaterPluginInterface {
	void onSongChanged(Song song);
	
	void onDurationChanged(int duration);

	void onLoading();
	
	void onPause();
	
	void onResume();
	
	void onPlay();

	void onStop();

	void onTitleChanged(String title);

	void onArtistChanged(String artist);

	void onAlbumArtChanged(int resourceId);

	void onAlbumArtChangedToUri(Uri url);
	
	void onNextTrackAvailable();
	
	void onNextTrackUnavailable();
}
