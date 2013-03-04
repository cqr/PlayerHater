package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.PlayerHaterPluginInterface;

import android.net.Uri;

public class PlayerHaterPlugin implements PlayerHaterPluginInterface {
	private Song mSong;
	private int mDuration;

	@Override
	public void setIsPlaying(boolean isPlaying) {
		if (isPlaying) {
			onPlaybackStarted(mSong, mDuration);
		}
	}

	@Override
	public void onLoading(Song forSong) {
	}

	@Override
	public void onPlaybackStarted(Song forSong, int duration) {
		mSong = forSong;
		mDuration = duration;
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
	public void setCanSkipForward(boolean canSkipForward) {
	}

	@Override
	public void setCanSkipBack(boolean canSkipBack) {
	}

}
