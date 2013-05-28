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
package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.IPlayerHaterBinder;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;

/**
 * An interface that plugins for PlayerHater must implement.
 * <p>
 * Plugins must implement a default constructor that takes no arguments.
 * <p>
 * Most implementations should consider extending {@linkplain AbstractPlugin}
 * instead of implementing this interface.
 * 
 * @since 2.0.0
 * @version 2.1.0
 * 
 * @author Chris Rhoden
 */
public interface PlayerHaterPlugin {

	/**
	 * Called when the plugin can start doing work against PlayerHater.
	 * 
	 * @param context
	 *            A {@link Context} that the plugin can use to interact with the
	 *            application. This will either be the
	 *            {@link ApplicationContext} associated with the
	 *            {@link Application} running the plugin, or it will be the
	 *            context that is bound when this plugin was attached using
	 *            {@link BoundPlayerHater#setBoundPlugin(PlayerHaterPlugin)}
	 * @param playerHater
	 *            A instance of one of the subclasses of {@link PlayerHater}.
	 *            This is the plugin's handle to PlayerHater, and it should not
	 *            attempt to access it in any other way.
	 *            <p>
	 *            If this plugin is loaded in the standard way, this will be an
	 *            instance of {@link ServicePlayerHater}. Otherwise, it will be
	 *            a {@link BoundPlayerHater} or a {@link BinderPlayerHater},
	 *            depending on the current status of the Service.
	 */
	void onPlayerHaterLoaded(Context context, PlayerHater playerHater);

	/**
	 * Called when the service starts. In standard plugins, this will be called
	 * immediately after {@link #onPlayerHaterLoaded(Context, PlayerHater)}. In
	 * prebound or bound plugins, it will be called once the Service has been
	 * started.
	 * 
	 * @param binder
	 *            The {@link IPlayerHaterBinder} that this plugin can access.
	 */
	void onServiceBound(IPlayerHaterBinder binder);

	/**
	 * Called when the binder is no longer going to be available. For standard
	 * plugins, this also indicates that the plugin will be shutting down. For
	 * Bound or Prebound plugins, it is possible for the plugin to continue
	 * running even when the service is not bound.
	 */
	void onServiceStopping();

	/**
	 * Called when the song that is loaded into the player has changed.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 * 
	 * @param song
	 *            The Song which is now loaded into the player.
	 */
	void onSongChanged(Song song);

	/**
	 * Called when the song that was loaded into the player has completed.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 * 
	 * @param song
	 *            The Song that has finished playing.
	 * @param reason
	 *            The reason that the song reached the end. One of
	 *            {@link PlayerHater#SKIP_BUTTON } or
	 *            {@link PlayerHater#TRACK_END }
	 */
	void onSongFinished(Song song, int reason);

	/**
	 * Called when the duration of the currently loaded track has changed.
	 * Typically called whenever {@link PlayerHaterPlugin#onSongChanged(Song)}
	 * has been called, but provided as a convenience for plugins that do not
	 * care about other metdata.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 * 
	 * @param duration
	 *            The new duration of the currently loaded Song in milliseconds.
	 */
	void onDurationChanged(int duration);

	/**
	 * Called when the player has started preparing or buffering the Song loaded
	 * into it.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 */
	void onAudioLoading();

	/**
	 * Called when playback has been paused.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 */
	void onAudioPaused();

	/**
	 * Called when playback has been resumed after being paused.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 */
	void onAudioResumed();

	/**
	 * Called when audio begins playback for the first time.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 */
	void onAudioStarted();

	/**
	 * Called when the audio has been stopped without the possibility of
	 * resuming.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 */
	void onAudioStopped();

	/**
	 * Called when the title of the currently playing track has changed.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 * 
	 * @param title
	 *            The new title of the currently playing track.
	 */
	void onTitleChanged(String title);

	/**
	 * Called when the artist of the currently playing track has changed.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 * 
	 * @param artist
	 *            The new artist of the currently playing track.
	 */
	void onArtistChanged(String artist);

	/**
	 * Called when the album art resource for the currently playing track has
	 * changed.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 * 
	 * @param resourceId
	 *            The resource ID of the new album art.
	 */
	void onAlbumArtChanged(int resourceId);

	/**
	 * Called when the album art of the currently playing track has changed.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 * 
	 * @param url
	 *            The URI of the new album art for the currently playing track.
	 */
	void onAlbumArtChangedToUri(Uri url);

	/**
	 * Called when there is a song "on deck" to play next.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 * 
	 * @param nextTrack
	 *            The next song which will be played.
	 */
	void onNextSongAvailable(Song nextTrack);

	/**
	 * Called when there is no song "on deck" to play next.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 */
	void onNextSongUnavailable();

	/**
	 * Called when the requested Transport Control Flags have changed.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 * 
	 * @see {@link android.media.RemoteControlClient#setTransportControlFlags(int)}
	 * 
	 * @param transportControlFlags
	 *            A bitmask representing the transport control flags requested
	 */
	void onTransportControlFlagsChanged(int transportControlFlags);

	/**
	 * Called when the requested pending intent for resuming the host
	 * application has changed.
	 * <p>
	 * <b>NOTE:</b> This method, by default, is not guaranteed to run on the UI
	 * thread. If you need it to run on the UI Thread, you should ensure that
	 * your plugin is run in the context of a BackgroundedPlugin with the
	 * correct flags set.
	 * <p>
	 * If you just want a method which is guaranteed to run on the UI thread,
	 * {@link #onChangesComplete()} is guaranteed to be called shortly after any
	 * changes, and will always be run on the UI thread by default.
	 * 
	 * @param intent
	 *            A pending intent that the plugin should use if it wants to
	 *            resume the hosting application.
	 */
	void onIntentActivityChanged(PendingIntent intent);

	/**
	 * Called after one or more state-change callbacks have completed. This
	 * method is guaranteed, by default, to run on the UI thread.
	 */
	void onChangesComplete();
}
