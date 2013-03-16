package org.prx.android.playerhater.plugins;

import java.util.HashSet;
import java.util.Set;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.util.IPlayerHater;

import android.content.Context;
import android.net.Uri;

public class PluginCollection implements PlayerHaterPlugin {

	private final Set<PlayerHaterPlugin> mPlugins;

	public PluginCollection() {
		mPlugins = new HashSet<PlayerHaterPlugin>();
	}

	public void add(PlayerHaterPlugin plugin) {
		mPlugins.add(plugin);
	}

	public void remove(PlayerHaterPlugin plugin) {
		mPlugins.remove(plugin);
	}

	@Override
	public void onPlaybackStopped() {
		for (PlayerHaterPlugin listener : mPlugins)
			listener.onPlaybackStopped();
	}

	@Override
	public void onTitleChanged(String title) {
		for (PlayerHaterPlugin listener : mPlugins)
			listener.onTitleChanged(title);
	}

	@Override
	public void onArtistChanged(String artist) {
		for (PlayerHaterPlugin listener : mPlugins)
			listener.onArtistChanged(artist);
	}

	@Override
	public void onAlbumArtChanged(int resourceId) {
		for (PlayerHaterPlugin listener : mPlugins)
			listener.onAlbumArtChanged(resourceId);
	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
		for (PlayerHaterPlugin listener : mPlugins)
			listener.onAlbumArtChangedToUri(url);
	}

	@Override
	public void onSongChanged(Song song) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onSongChanged(song);
	}

	@Override
	public void onDurationChanged(int duration) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onDurationChanged(duration);
	}

	@Override
	public void onAudioLoading() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onAudioLoading();
	}

	@Override
	public void onPlaybackPaused() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onPlaybackPaused();
	}

	@Override
	public void onPlaybackResumed() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onPlaybackResumed();
	}

	@Override
	public void onPlaybackStarted() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onPlaybackStarted();
	}

	@Override
	public void onNextTrackAvailable() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onNextTrackAvailable();
	}

	@Override
	public void onNextTrackUnavailable() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onNextTrackUnavailable();
	}

	@Override
	public void onServiceStarted(Context context, IPlayerHater playerHater) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onServiceStarted(context, playerHater);
	}

	@Override
	public void onServiceStopping() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onServiceStopping();
		
	}

}
