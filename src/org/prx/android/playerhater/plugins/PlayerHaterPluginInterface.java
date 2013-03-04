package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.Song;

import android.net.Uri;

public interface PlayerHaterPluginInterface {
	void setIsPlaying(boolean isPlaying);

	void onLoading(Song forSong);

	void onPlaybackStarted(Song forSong, int duration);

	void onStop();

	void onTitleChanged(String title);

	void onArtistChanged(String artist);

	void onAlbumArtChanged(int resourceId);

	void onAlbumArtChangedToUri(Uri url);

	void setCanSkipForward(boolean canSkipForward);

	void setCanSkipBack(boolean canSkipBack);
}
