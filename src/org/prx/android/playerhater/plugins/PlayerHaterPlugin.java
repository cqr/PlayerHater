package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.Song;

import android.net.Uri;

public interface PlayerHaterPlugin {
	void onSongChanged(Song song);
	
	void onDurationChanged(int duration);

	void onAudioLoading();
	
	void onPlaybackPaused();
	
	void onPlaybackResumed();
	
	void onPlaybackStarted();

	void onPlaybackStopped();

	void onTitleChanged(String title);

	void onArtistChanged(String artist);

	void onAlbumArtChanged(int resourceId);

	void onAlbumArtChangedToUri(Uri url);
	
	void onNextTrackAvailable();
	
	void onNextTrackUnavailable();
}
