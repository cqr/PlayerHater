/*******************************************************************************
 * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.prx.playerhater.plugins;

import java.util.HashSet;
import java.util.Set;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.PlayerHaterPlugin;
import org.prx.playerhater.Song;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.util.SparseArray;

public class PluginCollection implements PlayerHaterPlugin {

	private final Set<PlayerHaterPlugin> mPlugins;
	private final SparseArray<PlayerHaterPlugin> mPluginTags;

	public PluginCollection() {
		mPlugins = new HashSet<PlayerHaterPlugin>();
		mPluginTags = new SparseArray<PlayerHaterPlugin>();
	}

	public PluginCollection(PlayerHaterPlugin... plugins) {
		this();
		for (PlayerHaterPlugin plugin : plugins) {
			add(plugin);
		}
	}

	public synchronized void add(PlayerHaterPlugin plugin) {
		add(plugin, 0);
	}

	public synchronized void add(PlayerHaterPlugin plugin, int tag) {
		if (tag != 0) {
			mPluginTags.put(tag, plugin);
		}
		mPlugins.add(plugin);
	}

	public synchronized void remove(PlayerHaterPlugin plugin) {
		mPlugins.remove(plugin);
	}

	public synchronized PlayerHaterPlugin get(int tag) {
		return mPluginTags.get(tag);
	}

	public synchronized void remove(int tag) {
		if (tag != 0 && mPluginTags.get(tag) != null) {
			mPlugins.remove(mPluginTags.get(tag));
			mPluginTags.delete(tag);
		}
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
	public void onAlbumArtChanged(Uri url) {
		for (PlayerHaterPlugin listener : mPlugins)
			listener.onAlbumArtChanged(url);
	}

	@Override
	public void onSongChanged(Song song) {
		for (PlayerHaterPlugin plugin : mPlugins) {
			plugin.onSongChanged(song);
		}
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
	public void onIntentActivityChanged(PendingIntent pending) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onIntentActivityChanged(pending);
	}

	@Override
	public void onSongFinished(Song song, int reason) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onSongFinished(song, reason);
	}

	@Override
	public void onTransportControlFlagsChanged(int transportControlFlags) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onTransportControlFlagsChanged(transportControlFlags);
	}

	@Override
	public void onChangesComplete() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onChangesComplete();
	}

	public synchronized int getSize() {
		return mPlugins.size();
	}

	public synchronized void removeAll() {
		mPluginTags.clear();
		mPlugins.clear();
	}

	@Override
	public void onAlbumTitleChanged(String albumTitle) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onAlbumTitleChanged(albumTitle);
	}

}
