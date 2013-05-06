package org.prx.android.playerhater.playerhater;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.MediaPlayerWithState;

import android.app.Activity;
import android.net.Uri;

/**
 * @version 2.1.0
 * @since 2.0.0
 * @author Chris Rhoden
 */
public interface IPlayerHater {

	/**
	 * Pauses the player.
	 * 
	 * @return true if false if the player is not playing.
	 */
	abstract public boolean pause();

	/**
	 * Stops the player.
	 * 
	 * @return {@code true} if successful, {@code false} if the player is not
	 *         playing or paused.
	 */
	abstract public boolean stop();

	/**
	 * Begins playback of the currently loaded {@linkplain Song}.
	 * 
	 * @return {@code true} if successful, {@code false} if there is no
	 *         {@linkplain Song} loaded.
	 */
	abstract public boolean play();

	/**
	 * Begins playback of the currently loaded {@linkplain Song} at
	 * {@code startTime} in the track.
	 * 
	 * @see {@link IPlayerHater#seekTo(int) seekTo(int)}
	 * @see {@link IPlayerHater#play() play()}
	 * @param startTime
	 *            The time in milliseconds at which to begin playback
	 * @return {@code true} if successful, {@code false} if there is no track
	 *         loaded.
	 */
	abstract public boolean play(int startTime);

	/**
	 * Begins playback of a song at the beginning.
	 * 
	 * @param song
	 *            A {@linkplain Song} to play back.
	 * @return {@code true} if successful, {@code false} if there is a problem.
	 */
	abstract public boolean play(Song song);

	/**
	 * Begins playback of {@code song} at {@code startTime}
	 * 
	 * @see {@link PlayerHater#play(int)}, {@link PlayerHater#play(Song)}
	 * @return {@code true} if successful, {@code false} if there is a problem.
	 */
	abstract public boolean play(Song song, int startTime);

	/**
	 * Moves the playhead to {@code startTime}
	 * 
	 * @param startTime
	 *            The time (in milliseconds) to move the playhead to.
	 * @see {@link PlayerHater#play(int)}
	 * @return {@code true} if successful, {@code false} if there is no song
	 *         loaded in the player.
	 */
	abstract public boolean seekTo(int startTime);

	/**
	 * Puts a song on the end of the play queue.
	 * 
	 * @param song
	 *            The {@linkplain Song} to add to the end of the queue.
	 * @return the queue position of the song, in relation to the playhead. If
	 *         this song has been loaded into the now playing position, this
	 *         will return 0. If the song will be played next, it will return 1,
	 *         and so on.
	 */
	abstract public int enqueue(Song song);

	/**
	 * Moves to a new position in the play queue.
	 * 
	 * @param position
	 *            The position in the queue (1-indexed) to skip to.
	 * @return {@code true} if successful, {@code false} if the {@code position}
	 *         requested was invalid.
	 */
	abstract public boolean skipTo(int position);

	/**
	 * Moves to the next song in the play queue.
	 * <p>
	 * If the player is already playing, it will continue to play with the new
	 * track. If it is not playing, it will move to the next track without
	 * playing.
	 * <p>
	 * If the player has loaded last track, the player will be moved to the
	 * first song in the queue and playback will be stopped, regardless of
	 * whether or not the player is already playing.
	 */
	abstract public void skip();

	/**
	 * Moves back in the play queue.
	 * <p>
	 * If the player is already playing, it will continue to play with the new
	 * track. If it is not playing, it will skip without playing.
	 * <p>
	 * If the playhead is currently fewer than 2 seconds from the beginning of
	 * the track, it will skip to the previous track. Otherwise, it will skip to
	 * the beginning of the currently loaded track.
	 * <p>
	 * If the player has loaded the first track in the queue, it will skip to
	 * the beginning of that track regardless of the playhead's position.
	 */
	abstract public void skipBack();

	/**
	 * Removes all songs from the queue.
	 * <p>
	 * If the player is currently playing, only the currently playing song will
	 * be preserved. All tracks, including those before and after the current
	 * track, will be removed from the queue.
	 * <p>
	 * If the player is not playing, all tracks, including the currently loaded
	 * one, will be removed.
	 */
	abstract public void emptyQueue();

	/**
	 * Sets the album art resource for use by plugins.
	 * <p>
	 * Many plugins (including built-in plugins such as
	 * {@linkplain TouchableNotificationPlugin} and
	 * {@linkplain LockScreenControlsPlugin} display information about the
	 * currently playing {@linkplain Song} to users from outside the hosting
	 * application. This allows you to provide a resource identifier for the
	 * player to go into the area usually reserved for album art.
	 * <p>
	 * Note that this method should usually only be used to provide a default in
	 * situations where no album art is available for each playable
	 * {@linkplain Song}. It will be overridden by any {@linkplain Song} which
	 * implements {@linkplain Song#getAlbumArt()} without returning null.
	 * 
	 * @param resourceId
	 *            The resource id of the drawable to be used as album art.
	 */
	abstract public void setAlbumArt(int resourceId);

	/**
	 * Sets the album art Uri for use by plugins.
	 * <p>
	 * Many plugins (including built-in plugins such as
	 * {@linkplain TouchableNotificationPlugin} and
	 * {@linkplain LockScreenControlsPlugin} display information about the
	 * currently playing {@linkplain Song} to users from outside the hosting
	 * application. This allows you to provide a Uri for the player to go into
	 * the area usually reserved for album art.
	 * <p>
	 * Note that this method should usually only be used to provide a default in
	 * situations where no album art is available for each playable
	 * {@linkplain Song}. It will be overridden by any {@linkplain Song} which
	 * implements {@linkplain Song#getAlbumArt()} without returning null.
	 * 
	 * @see {link #setAlbumArt(int)}
	 * 
	 * @param uri
	 *            And instance of Uri that refers to an image resource. Note
	 *            that this can use any of the schemes supported by android,
	 *            including {@code file:///android-asset/*}, {@code content://*}
	 *            , and of course {@code http://*}
	 */
	abstract public void setAlbumArt(Uri uri);

	/**
	 * Sets the song title for use by plugins.
	 * <p>
	 * Many plugins (including built-in plugins such as
	 * {@linkplain TouchableNotificationPlugin} and
	 * {@linkplain LockScreenControlsPlugin} display information about the
	 * currently playing {@linkplain Song} to users from outside the hosting
	 * application. This allows you to provide a String for the player to go
	 * into the area usually reserved for the Song title.
	 * <p>
	 * Note that this method should usually only be used to provide a default in
	 * situations where no title is available for each playable
	 * {@linkplain Song}. It will be overridden by any {@linkplain Song} which
	 * implements {@linkplain Song#getTitle()} without returning null.
	 * 
	 * @param title
	 *            the title of the currently loaded track
	 */
	abstract public void setTitle(String title);

	/**
	 * Sets the song's artist for use by plugins.
	 * <p>
	 * Many plugins (including built-in plugins such as
	 * {@linkplain TouchableNotificationPlugin} and
	 * {@linkplain LockScreenControlsPlugin} display information about the
	 * currently playing {@linkplain Song} to users from outside the hosting
	 * application. This allows you to provide a String for the player to go
	 * into the area usually reserved for the Song's artist.
	 * <p>
	 * Note that this method should usually only be used to provide a default in
	 * situations where no artist is available for each playable
	 * {@linkplain Song}. It will be overridden by any {@linkplain Song} which
	 * implements {@linkplain Song#getArtist()} without returning null.
	 * 
	 * @param artist
	 *            the artist of the currently loaded track
	 */
	abstract public void setArtist(String artist);

	abstract public void setActivity(Activity activity);

	/**
	 * Gets the location of the playhead in milliseconds.
	 * 
	 * @return the current position of the play head in milliseconds
	 */
	abstract public int getCurrentPosition();

	/**
	 * Gets the duration of the currently loaded Song in milliseconds.
	 * 
	 * @return the duration of the currently loaded Song in milliseconds.
	 */
	abstract public int getDuration();

	/**
	 * Sets up a receiver of periodic events from the PlayerHater service.
	 * <p>
	 * The listener will receive callbacks when the audio starts, stops and is
	 * paused, as well as at regular intervals during playback.
	 * 
	 * @param listener
	 */
	abstract public void setListener(PlayerHaterListener listener);

	/**
	 * Gets the {@linkplain Song} representation of the track that is currently
	 * loaded in the player.
	 * 
	 * @return the {@linkplain Song} of the track that is currently loaded in
	 *         the player.
	 */
	abstract public Song nowPlaying();

	/**
	 * Checks to see if the player is currently playing back audio.
	 * 
	 * @return {@code true} if the player is currently playing, {@code false}
	 *         otherwise.
	 */
	abstract public boolean isPlaying();

	/**
	 * Checks to see if the player is currently loading audio.
	 * 
	 * @return {@code true} if the player is currently loading, {@code false}
	 *         otherwise.
	 */
	abstract public boolean isLoading();

	/**
	 * Gets the state of the currently loaded
	 * {@linkplain android.media.MediaPlayer MediaPlayer}, represented as an
	 * int.
	 * 
	 * @return One of: {@link MediaPlayerWithState#IDLE IDLE},
	 *         {@link MediaPlayerWithState#INITIALIZED INITIALIZED},
	 *         {@link MediaPlayerWithState#PREPARING PREPARING},
	 *         {@link MediaPlayerWithState#PREPARED PREPARED},
	 *         {@link MediaPlayerWithState#STARTED STARTED},
	 *         {@link MediaPlayerWithState#PAUSED PAUSED},
	 *         {@link MediaPlayerWithState#PLAYBACK_COMPLETED
	 *         PLAYBACK_COMPLETED}, {@link MediaPlayerWithState#STOPPED STOPPED}
	 *         , {@link MediaPlayerWithState#END END}, or
	 *         {@link MediaPlayerWithState#ERROR ERROR}
	 */
	abstract public int getState();

	/**
	 * Sets the visible buttons for plugins. It is up to the implementation of
	 * the plugin to honor these settings.
	 * 
	 * @see {@link android.media.RemoteControlClient#setTransportControlFlags(int) }
	 * @param transportControlFlags
	 */
	abstract public void setTransportControlFlags(int transportControlFlags);
}
