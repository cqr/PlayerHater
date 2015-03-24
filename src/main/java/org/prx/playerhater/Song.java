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
package org.prx.playerhater;

import android.net.Uri;
import android.os.Bundle;

/**
 * An interface which can be used for playback in {@linkplain org.prx.playerhater.PlayerHater}. This
 * interface is primarily for use by {@linkplain org.prx.playerhater.PlayerHaterPlugin}s, which use
 * the metadata provided to display notifications, widgets, and lock screen
 * controls.
 * 
 * @see org.prx.playerhater.PlayerHater#play(org.prx.playerhater.Song)
 * @version 3.0.0
 * @author Chris Rhoden
 * @since 2.0.0
 */
public interface Song {

	/**
	 * An accessor for the title attribute of a {@linkplain org.prx.playerhater.Song} playable
	 * entity in PlayerHater
	 * 
	 * @return The title of the {@linkplain org.prx.playerhater.Song}
	 */
	String getTitle();

    String getSongJson();

	/**
	 * An accessor for the artist attribute of a {@linkplain org.prx.playerhater.Song} playable
	 * entity in PlayerHater
	 * 
	 * @return The name of the artist performing the {@linkplain org.prx.playerhater.Song}
	 */
	String getArtist();
	
	/**
	 * An accessor for the album attribute of a {@linkplain org.prx.playerhater.Song} playable
	 * entity in PlayerHater
	 * 
	 * @return The name of the album on which the {@linkplain org.prx.playerhater.Song} is performed
	 */
	String getAlbumTitle();
	
	/**
	 * An accessor for the the album art attribute of a {@linkplain org.prx.playerhater.Song}
	 * playable entity in PlayerHater
	 * 
	 * @return The Uri representing the album art for this {@linkplain org.prx.playerhater.Song}
	 */
	Uri getAlbumArt();

	/**
	 * @see android.media.MediaPlayer#setDataSource(android.content.Context,
	 *      android.net.Uri)
	 * @return A Uri which resolves to the {@linkplain org.prx.playerhater.Song}'s file, which can
	 *         be played using Android's
	 *         {@linkplain android.media.MediaPlayer#setDataSource(android.content.Context, android.net.Uri)
	 *         MediaPlayer} class.
	 *         <p>
	 *         Ex.
	 *         <p>
	 *         {@code http://www.example.com/track.mp3 }
	 *         <p>
	 *         {@code content://com.example.app/clips/21 }
	 */
	Uri getUri();

	/**
	 * @return A Bundle whose meaning is user-defined. This is to enable easy
	 *         inter-process communication of additional data about Songs.
	 *         <p>
	 *         PlayerHater will not do anything with this.
	 */
	Bundle getExtra();
}
