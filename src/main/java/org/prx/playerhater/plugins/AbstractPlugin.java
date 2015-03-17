/*
 * -/*******************************************************************************
 * - * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * - *
 * - * Licensed under the Apache License, Version 2.0 (the "License");
 * - * you may not use this file except in compliance with the License.
 * - * You may obtain a copy of the License at
 * - *
 * - *   http://www.apache.org/licenses/LICENSE-2.0
 * - *
 * - * Unless required by applicable law or agreed to in writing, software
 * - * distributed under the License is distributed on an "AS IS" BASIS,
 * - * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * - * See the License for the specific language governing permissions and
 * - * limitations under the License.
 * - *****************************************************************************
 */
package org.prx.playerhater.plugins;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.PlayerHaterPlugin;
import org.prx.playerhater.Song;

/**
 * A simple helper for writing {@linkplain org.prx.playerhater.PlayerHaterPlugin}s
 * <p/>
 * Subclasses MUST implement a default no-argument constructor.
 *
 * @author Chris Rhoden
 * @version 2.1.0
 * @see {@link org.prx.playerhater.PlayerHaterPlugin}
 * @since 2.0.0
 */
public abstract class AbstractPlugin implements PlayerHaterPlugin {
    private PlayerHater mPlayerHater;
    private Context mContext;

    public AbstractPlugin() {

    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden implementations should be sure to call
     * {@code super.onPlayerHaterLoaded} so that future calls to
     * {@link #getContext()} and {@link #getPlayerHater()} can succeed.
     */
    @Override
    public synchronized void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
        mContext = context;
        mPlayerHater = playerHater;
    }

    @Override
    public void onAudioStarted() {
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The default implementation forwards this method call to
     * {@link #onAudioStarted()}
     */
    @Override
    public void onAudioResumed() {
        onAudioStarted();
    }

    @Override
    public void onAudioStopped() {
    }

    @Override
    public void onTitleChanged(String title) {
    }

    @Override
    public void onArtistChanged(String artist) {
    }

    @Override
    public void onAlbumTitleChanged(String albumTitle) {
    }

    @Override
    public void onAlbumArtChanged(Uri uri) {
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The default implementation will call {@link #onAlbumArtChanged(android.net.Uri)}
     * , {@link #onTitleChanged(String)}, and {@link #onArtistChanged(String)}
     */
    @Override
    public void onSongChanged(Song song) {
        if (song != null) {
            onTitleChanged(song.getTitle());
            onArtistChanged(song.getArtist());
            onAlbumTitleChanged(song.getAlbumTitle());
            onAlbumArtChanged(song.getAlbumArt());
        }
    }

    @Override
    public void onDurationChanged(int duration) {
    }

    @Override
    public void onAudioLoading() {
    }

    @Override
    public void onAudioPaused() {
    }

    @Override
    public void onNextSongAvailable(Song nextSong) {
    }

    @Override
    public void onNextSongUnavailable() {
    }

    @Override
    public void onPendingIntentChanged(PendingIntent pending) {
    }

    @Override
    public void onSongFinished(Song song, int reason) {
    }

    @Override
    public void onChangesComplete() {
    }

    @Override
    public void onTransportControlFlagsChanged(int transportControlFlags) {
    }

    @Override
    public void onPlayerHaterShutdown() {
        mPlayerHater = null;
        mContext = null;
    }

    /**
     * Grants the plugin easy access to the instance of {@link org.prx.playerhater.PlayerHater} that
     * it is permitted to use.
     *
     * @return An instance of PlayerHater
     */
    protected synchronized final PlayerHater getPlayerHater() {
        if (mPlayerHater == null)
            throw new IllegalStateException("PlayerHater is not loaded yet or has been shut down.");
        return mPlayerHater;
    }

    /**
     * A method providing simple access to the plugin's context without having
     * to override {@link #onPlayerHaterLoaded(android.content.Context, org.prx.playerhater.PlayerHater)}
     *
     * @return The {@link android.content.Context} in which the plugin is running.
     */
    protected synchronized final Context getContext() {
        if (mContext == null)
            throw new IllegalStateException("PlayerHater is not loaded yet or has been shut down.");
        return mContext;
    }
}
