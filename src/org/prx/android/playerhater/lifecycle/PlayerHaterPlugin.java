package org.prx.android.playerhater.lifecycle;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.lifecycle.LifecycleListener.RemoteControl;

import android.net.Uri;

public class PlayerHaterPlugin implements RemoteControl {
	private Song mSong;
	private int mDuration;

	@Override
	public void setIsPlaying(boolean isPlaying) {
		if (isPlaying) {
			start(mSong, mDuration);
		}
	}

	@Override
	public void setIsLoading(Song forSong) {}

	@Override
	public void start(Song forSong, int duration) {
		mSong = forSong;
		mDuration = duration;
	}

	@Override
	public void stop() {}

	@Override
	public void setTitle(String title) {}

	@Override
	public void setArtist(String artist) {}

	@Override
	public void setAlbumArt(int resourceId) {}

	@Override
	public void setAlbumArt(Uri url) {}

	@Override
	public void setCanSkipForward(boolean canSkipForward) {}

	@Override
	public void setCanSkipBack(boolean canSkipBack) {}

}
