package org.prx.android.playerhater.plugins;

import java.util.HashSet;
import java.util.Set;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.IPlayerHaterBinder;

import android.app.PendingIntent;
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
	public void onAudioStopped() {
		for (PlayerHaterPlugin listener : mPlugins)
			listener.onAudioStopped();
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
	public void onAudioPaused() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onAudioPaused();
	}

	@Override
	public void onAudioResumed() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onAudioResumed();
	}

	@Override
	public void onAudioStarted() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onAudioStarted();
	}

	@Override
	public void onNextSongAvailable(Song nextSong) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onNextSongAvailable(nextSong);
	}

	@Override
	public void onNextSongUnavailable() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onNextSongUnavailable();
	}

	@Override
	public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onPlayerHaterLoaded(context, playerHater);
	}

	@Override
	public void onServiceStopping() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onServiceStopping();
		
	}

	@Override
	public void onIntentActivityChanged(PendingIntent pending) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onIntentActivityChanged(pending);
	}

	@Override
	public void onServiceBound(IPlayerHaterBinder binder) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onServiceBound(binder);
	}

	@Override
	public void onSongFinished(Song song, int reason) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onSongFinished(song, reason);
	}

	@Override
	public void onChangesComplete() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onChangesComplete();
	}

}
