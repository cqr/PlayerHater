package org.prx.playerhater.mediaplayer;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;

public abstract class Player {
	
	/**
	 * An invalid state for a {@linkplain MediaPlayer} to be in.
	 */
	public static final int INVALID_STATE = -1;

	/**
	 * The state a {@linkplain MediaPlayer} is in when initialized. Nothing is
	 * loaded into the player.
	 */
	public static final int IDLE = 0;

	/**
	 * The state a {@linkplain MediaPlayer} enters after
	 * {@linkplain MediaPlayer#release()} is called on it. This
	 * {@linkplain MediaPlayer} will be unusable until {@linkplain #reset()} is
	 * called on it.
	 */
	public static final int END = 1;

	/**
	 * The state a {@linkplain MediaPlayer} is in when
	 * {@linkplain #setDataSource(Context, Uri)} is called on it.
	 */
	public static final int INITIALIZED = 2;

	/**
	 * The state a {@linkplain MediaPlayer} is in when
	 * {@linkplain #prepareAsync()} is called on it.
	 */
	public static final int PREPARING = 3;

	/**
	 * The state a {@linkplain MediaPlayer} is in when
	 * {@linkplain #prepareAsync()} is complete or when {{@link #prepare()} is
	 * called.
	 */
	public static final int PREPARED = 4;

	/**
	 * The state a {@linkplain MediaPlayer} is in when playing.
	 */
	public static final int STARTED = 5;

	/**
	 * The state a {@linkplain MediaPlayer} is in when {{@link #stop()} is
	 * called.
	 */
	public static final int STOPPED = 6;

	/**
	 * The state a {@linkplain MediaPlayer} is in when {{@link #pause()} is
	 * called.
	 */
	public static final int PAUSED = 7;

	/**
	 * The state a {@linkplain MediaPlayer} is in when it has reached the end of
	 * the data loaded into it.
	 */
	public static final int PLAYBACK_COMPLETED = 8;
	
	/**
	 * Internal state used for content:// URIs
	 */
	public static final int LOADING_CONTENT = -2;
	
	/**
	 * Internal state used for content:// URIs
	 */
	public static final int PREPARING_CONTENT = -3;

	/**
	 * The state a {@linkplain MediaPlayer} is in when an error has occurred.
	 */
	public static final int ERROR = 9;

	public abstract void reset();

	public abstract void release();

	public abstract void prepareAsync() throws IllegalStateException;

	public abstract void start() throws IllegalStateException;

	public abstract void pause() throws IllegalStateException;

	public abstract void stop() throws IllegalStateException;

	public abstract void seekTo(int msec);

	public abstract boolean isPlaying();

	public abstract int getCurrentPosition();

	public abstract int getDuration();

	public abstract void setAudioStreamType(int streamtype);

	public abstract void setDataSource(Context context, Uri uri)
			throws IllegalStateException, IOException,
			IllegalArgumentException, SecurityException;

	public abstract void setOnErrorListener(OnErrorListener errorListener);

	public abstract void setOnPreparedListener(
			OnPreparedListener preparedListener);

	public abstract void setOnBufferingUpdateListener(
			OnBufferingUpdateListener bufferingUpdateListener);

	public abstract void setOnCompletionListener(
			OnCompletionListener completionListener);

	public abstract void setOnInfoListener(OnInfoListener infoListener);

	public abstract void setOnSeekCompleteListener(
			OnSeekCompleteListener seekCompleteListener);

	public abstract void setVolume(float leftVolume, float rightVolume);

	public abstract boolean equals(MediaPlayer mp);
	
	/* Stately API */
	
	public int getState() {
		throw new IllegalStateException("This player isn't stately enough.");
	}

	public String getStateName() {
		throw new IllegalStateException("This player isn't stately enough.");
	}
	
	/* Synchronous API */

	public boolean prepare(Context context, Uri uri) {
		throw new IllegalStateException("This player isn't down with synchronous calls.");
	}

	public boolean prepareAndPlay(Context applicationContext, Uri uri,
			int position) {
		throw new IllegalStateException("This player isn't down with synchronous calls.");
	}
	
	public boolean conditionalPause() {
		throw new IllegalStateException("This player isn't down with synchronous calls.");
	}

	public boolean conditionalStop() {
		throw new IllegalStateException("This player isn't down with synchronous calls.");
	}
	
	public boolean conditionalPlay() {
		throw new IllegalStateException("This player isn't down with synchronous calls.");
	}

}
