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
import java.util.Iterator;
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
	public synchronized void onAudioStopped() {
		for (PlayerHaterPlugin listener : mPlugins)
			listener.onAudioStopped();
	}

	@Override
	public synchronized void onTitleChanged(String title) {
		for (PlayerHaterPlugin listener : mPlugins)
			listener.onTitleChanged(title);
	}

	@Override
	public synchronized void onArtistChanged(String artist) {
		for (PlayerHaterPlugin listener : mPlugins)
			listener.onArtistChanged(artist);
	}

	@Override
	public synchronized void onAlbumArtChanged(Uri url) {
		for (PlayerHaterPlugin listener : mPlugins)
			listener.onAlbumArtChanged(url);
	}

	@Override
	public synchronized void onSongChanged(Song song) {
		for (PlayerHaterPlugin plugin : mPlugins) {
			plugin.onSongChanged(song);
		}
	}

	@Override
	public synchronized void onDurationChanged(int duration) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onDurationChanged(duration);
	}

	@Override
	public synchronized void onAudioLoading() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onAudioLoading();
	}

	@Override
	public synchronized void onAudioPaused() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onAudioPaused();
	}

	@Override
	public synchronized void onAudioResumed() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onAudioResumed();
	}

	@Override
	public synchronized void onAudioStarted() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onAudioStarted();
	}

	@Override
	public synchronized void onNextSongAvailable(Song nextSong) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onNextSongAvailable(nextSong);
	}

	@Override
	public synchronized void onNextSongUnavailable() {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onNextSongUnavailable();
	}

	@Override
	public synchronized void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onPlayerHaterLoaded(context, playerHater);
	}

	@Override
	public synchronized void onPendingIntentChanged(PendingIntent pending) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onPendingIntentChanged(pending);
	}

	@Override
	public synchronized void onSongFinished(Song song, int reason) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onSongFinished(song, reason);
	}

	@Override
	public synchronized void onTransportControlFlagsChanged(int transportControlFlags) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onTransportControlFlagsChanged(transportControlFlags);
	}

	@Override
	public synchronized void onChangesComplete() {
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
	public synchronized void onAlbumTitleChanged(String albumTitle) {
		for (PlayerHaterPlugin plugin : mPlugins)
			plugin.onAlbumTitleChanged(albumTitle);
	}

	@Override
	public synchronized String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PluginCollection@").append(hashCode()).append("[ ");
		Iterator<PlayerHaterPlugin> iter = mPlugins.iterator();
		if (iter.hasNext()) {
			sb.append(iter.next().toString());
			while (iter.hasNext()) {
				sb.append(", ").append(iter.next().toString());
			}
		}
		return sb.append("]").toString();
	}
}
