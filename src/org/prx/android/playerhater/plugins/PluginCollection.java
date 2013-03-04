package org.prx.android.playerhater.plugins;

import java.util.ArrayList;
import java.util.List;

import org.prx.android.playerhater.Song;
import android.net.Uri;

public class PluginCollection implements PlayerHaterPluginInterface {

	private final List<PlayerHaterPluginInterface> mListeners;

	public PluginCollection() {
		mListeners = new ArrayList<PlayerHaterPluginInterface>();
	}

	public void add(PlayerHaterPluginInterface listener) {
		mListeners.add(listener);
	}

	@Override
	public void setIsPlaying(boolean isPlaying) {
		for (PlayerHaterPluginInterface listener : mListeners) {
			listener.setIsPlaying(isPlaying);
		}
	}

	@Override
	public void onPlaybackStarted(Song forSong, int duration) {
		for (PlayerHaterPluginInterface listener : mListeners) {
			listener.onPlaybackStarted(forSong, duration);
		}
	}

	@Override
	public void onStop() {
		for (PlayerHaterPluginInterface listener : mListeners) {
			listener.onStop();
		}
	}

	@Override
	public void onTitleChanged(String title) {
		for (PlayerHaterPluginInterface listener : mListeners) {
			listener.onTitleChanged(title);
		}
	}

	@Override
	public void onArtistChanged(String artist) {
		for (PlayerHaterPluginInterface listener : mListeners) {
			listener.onArtistChanged(artist);
		}
	}

	@Override
	public void onAlbumArtChanged(int resourceId) {
		for (PlayerHaterPluginInterface listener : mListeners) {
			listener.onAlbumArtChanged(resourceId);
		}
	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
		for (PlayerHaterPluginInterface listener : mListeners) {
			listener.onAlbumArtChangedToUri(url);
		}
	}

	@Override
	public void setCanSkipForward(boolean canSkipForward) {
		for (PlayerHaterPluginInterface listener : mListeners) {
			listener.setCanSkipForward(canSkipForward);
		}
	}

	@Override
	public void setCanSkipBack(boolean canSkipBack) {
		for (PlayerHaterPluginInterface listener : mListeners) {
			listener.setCanSkipBack(canSkipBack);
		}
	}

	@Override
	public void onLoading(Song forSong) {
		for (PlayerHaterPluginInterface listener : mListeners) {
			listener.onLoading(forSong);
		}
	}

}
