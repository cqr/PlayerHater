package org.prx.android.playerhater.plugins;

import java.util.ArrayList;
import java.util.List;

import org.prx.android.playerhater.Song;
import android.net.Uri;

public class PluginCollection implements PlayerHaterPlugin {

	private final List<PlayerHaterPlugin> mListeners;

	public PluginCollection() {
		mListeners = new ArrayList<PlayerHaterPlugin>();
	}

	public void add(PlayerHaterPlugin listener) {
		mListeners.add(listener);
	}

	@Override
	public void onPlaybackStopped() {
		for (PlayerHaterPlugin listener : mListeners)
			listener.onPlaybackStopped();
	}

	@Override
	public void onTitleChanged(String title) {
		for (PlayerHaterPlugin listener : mListeners)
			listener.onTitleChanged(title);
	}

	@Override
	public void onArtistChanged(String artist) {
		for (PlayerHaterPlugin listener : mListeners)
			listener.onArtistChanged(artist);
	}

	@Override
	public void onAlbumArtChanged(int resourceId) {
		for (PlayerHaterPlugin listener : mListeners)
			listener.onAlbumArtChanged(resourceId);
	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
		for (PlayerHaterPlugin listener : mListeners)
			listener.onAlbumArtChangedToUri(url);
	}

	@Override
	public void onSongChanged(Song song) {
		for (PlayerHaterPlugin plugin : mListeners)
			plugin.onSongChanged(song);
	}

	@Override
	public void onDurationChanged(int duration) {
		for (PlayerHaterPlugin plugin : mListeners)
			plugin.onDurationChanged(duration);
	}

	@Override
	public void onAudioLoading() {
		for (PlayerHaterPlugin plugin : mListeners)
			plugin.onAudioLoading();
	}

	@Override
	public void onPlaybackPaused() {
		for (PlayerHaterPlugin plugin : mListeners)
			plugin.onPlaybackPaused();
	}

	@Override
	public void onPlaybackResumed() {
		for (PlayerHaterPlugin plugin : mListeners)
			plugin.onPlaybackResumed();
	}

	@Override
	public void onPlaybackStarted() {
		for (PlayerHaterPlugin plugin : mListeners)
			plugin.onPlaybackStarted();
	}

	@Override
	public void onNextTrackAvailable() {
		for (PlayerHaterPlugin plugin : mListeners)
			plugin.onNextTrackAvailable();
	}

	@Override
	public void onNextTrackUnavailable() {
		for (PlayerHaterPlugin plugin : mListeners)
			plugin.onNextTrackUnavailable();
	}

}
