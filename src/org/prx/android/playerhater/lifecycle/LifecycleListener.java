package org.prx.android.playerhater.lifecycle;

import org.prx.android.playerhater.Song;

import android.net.Uri;

public interface LifecycleListener {
	
	void setIsPlaying(boolean isPlaying);
	void setIsLoading(Song forSong);
	void start(Song forSong, int duration);
	void stop();

	public interface RemoteControl extends LifecycleListener {
		void setTitle(String title);
		void setArtist(String artist);
		void setAlbumArt(int resourceId);
		void setAlbumArt(Uri url);
		void setCanSkipForward(boolean canSkipForward);
		void setCanSkipBack(boolean canSkipBack);
	}
}
