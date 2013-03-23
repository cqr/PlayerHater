package org.prx.android.playerhater.util;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;

import android.app.Activity;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;

public interface IPlayerHater {

	/**
	 * Pauses the player.
	 * 
	 * @return true if false if the player is not playing.
	 */
	abstract public boolean pause();

	/**
	 * Stops the player. Returns false if the player is not playing or
	 * paused. Causes the service to be stopped.
	 */
	abstract public boolean stop();

	// Playback

	/**
	 * Begins playback of the currently loaded {@linkplain Song}
	 * 
	 * @return {@code true} if successful, {@code false} if there is no
	 *         track loaded.
	 */
	abstract public boolean play();

	/**
	 * Begins playback of the currently loaded {@linkplain Song} at
	 * {@code startTime} in the track.
	 * 
	 * @see {@link PlayerHater#seekTo(int)}
	 * @return {@code true} if successful, {@code false} if there is no
	 *         track loaded.
	 */
	abstract public boolean play(int startTime);

	/**
	 * Begins playback of {@code song}
	 * 
	 * @return {@code true} if successful, {@code false} if there is a
	 *         problem.
	 */
	abstract public boolean play(Song song);

	/**
	 * Begins playback of {@code song} at {@code startTime}
	 * 
	 * @see {@link PlayerHater#play(int)}, {@link PlayerHater#play(Song)}
	 * @return {@code true} if successful, {@code false} if there is a
	 *         problem.
	 */
	abstract public boolean play(Song song, int startTime);

	/**
	 * Moves the playhead to {@code startTime}
	 * 
	 * @see {@link PlayerHater#play(int)}
	 * @return {@code true} if successful, {@code false} if there is no song
	 *         loaded in the player.
	 */
	abstract public boolean seekTo(int startTime);

	// Queuing
	abstract public boolean enqueue(Song song);

	abstract public boolean skipTo(int position);

	abstract public void skip();

	abstract public void skipBack();

	abstract public void emptyQueue();

	// Notification data
	abstract public void setAlbumArt(int resourceId);

	abstract public void setAlbumArt(Uri url);

	abstract public void setTitle(String title);

	abstract public void setArtist(String artist);

	abstract public void setActivity(Activity activity);

	// Scubber-related data
	abstract public int getCurrentPosition();

	abstract public int getDuration();

	// Media Player listeners
	abstract public void setOnBufferingUpdateListener(
			OnBufferingUpdateListener listener);

	abstract public void setOnCompletionListener(
			OnCompletionListener listener);

	abstract public void setOnInfoListener(OnInfoListener listener);

	abstract public void setOnSeekCompleteListener(
			OnSeekCompleteListener listener);

	abstract public void setOnErrorListener(OnErrorListener listener);

	abstract public void setOnPreparedListener(OnPreparedListener listener);

	// PlayerHater listener
	@Deprecated
	abstract public void setListener(PlayerHaterListener listener);

	// Other Getters
	abstract public Song nowPlaying();

	abstract public boolean isPlaying();

	abstract public boolean isLoading();

	abstract public int getState();
}