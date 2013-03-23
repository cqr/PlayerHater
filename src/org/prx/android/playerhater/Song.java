package org.prx.android.playerhater;

import android.net.Uri;

/**
 * An interface which can be used for playback in {@linkplain PlayerHater}. This
 * interface is primarily for use by {@linkplain PlayerHaterPlugin}s, which use the
 * metadata provided to display notifications, widgets, and lock screen
 * controls.
 * 
 * @see PlayerHater#play(Song)
 * @version 2.0.0
 * @author Chris Rhoden
 * @since 2.0.0
 */
public interface Song {

	/**
	 * An accessor for the title attribute of a {@linkplain Song} playable entity in
	 * PlayerHater
	 * 
	 * @return The title of the {@linkplain Song}
	 */
	String getTitle();

	/**
	 * An accessor for the artist attribute of a {@linkplain Song} playable entity in
	 * PlayerHater
	 * 
	 * @return The name of the artist performing the {@linkplain Song}
	 */
	String getArtist();

	Uri getAlbumArt();

	/**
	 * @see android.media.MediaPlayer#setDataSource(android.content.Context, Uri)
	 * @return A Uri which resolves to the {@linkplain Song}'s file, which can be
	 *         played using Android's {@linkplain android.media.MediaPlayer#setDataSource(android.content.Context, Uri) MediaPlayer} class.
	 *         <p>
	 *         Ex.
	 *         <p>
	 *             {@code http://www.example.com/track.mp3 }
	 *         <p>
	 *             {@code content://com.example.app/clips/21 }
	 */
	Uri getUri();
}
