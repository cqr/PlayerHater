package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.Song;

import android.net.Uri;

public interface PlayerHaterPluginInterface {
	void setIsPlaying(boolean isPlaying);

	void setIsLoading(Song forSong);

	void start(Song forSong, int duration);

	void stop();

	void setTitle(String title);

	void setArtist(String artist);

	void setAlbumArt(int resourceId);

	void setAlbumArt(Uri url);

	void setCanSkipForward(boolean canSkipForward);

	void setCanSkipBack(boolean canSkipBack);
}
