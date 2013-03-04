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
	public void onStop() {
		for (PlayerHaterPluginInterface listener : mListeners)
			listener.onStop();
	}

	@Override
	public void onTitleChanged(String title) {
		for (PlayerHaterPluginInterface listener : mListeners)
			listener.onTitleChanged(title);
	}

	@Override
	public void onArtistChanged(String artist) {
		for (PlayerHaterPluginInterface listener : mListeners)
			listener.onArtistChanged(artist);
	}

	@Override
	public void onAlbumArtChanged(int resourceId) {
		for (PlayerHaterPluginInterface listener : mListeners)
			listener.onAlbumArtChanged(resourceId);
	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
		for (PlayerHaterPluginInterface listener : mListeners)
			listener.onAlbumArtChangedToUri(url);
	}

	@Override
	public void onSongChanged(Song song) {
		for (PlayerHaterPluginInterface plugin : mListeners)
			plugin.onSongChanged(song);
	}

	@Override
	public void onDurationChanged(int duration) {
		for (PlayerHaterPluginInterface plugin : mListeners)
			plugin.onDurationChanged(duration);
	}

	@Override
	public void onLoading() {
		for (PlayerHaterPluginInterface plugin : mListeners)
			plugin.onLoading();
	}

	@Override
	public void onPause() {
		for (PlayerHaterPluginInterface plugin : mListeners)
			plugin.onPause();
	}

	@Override
	public void onResume() {
		for (PlayerHaterPluginInterface plugin : mListeners)
			plugin.onResume();
	}

	@Override
	public void onPlay() {
		for (PlayerHaterPluginInterface plugin : mListeners)
			plugin.onPlay();
	}

	@Override
	public void onNextTrackAvailable() {
		for (PlayerHaterPluginInterface plugin : mListeners)
			plugin.onNextTrackAvailable();
	}

	@Override
	public void onNextTrackUnavailable() {
		for (PlayerHaterPluginInterface plugin : mListeners)
			plugin.onNextTrackUnavailable();
	}

}
