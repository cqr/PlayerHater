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

	public static interface StateChangeListener {
		public void onStateChanged(Player mediaPlayer, int state);
	}

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

	public void setStateChangeListener(StateChangeListener listener) {
		throw new IllegalStateException("This player isn't stately enough.");
	}

	public String getStateName() {
		throw new IllegalStateException("This player isn't stately enough.");
	}

	/* Synchronous API */

	public boolean prepare(Context context, Uri uri) {
		throw new IllegalStateException(
				"This player isn't down with synchronous calls.");
	}

	public boolean prepareAndPlay(Context applicationContext, Uri uri,
			int position) {
		throw new IllegalStateException(
				"This player isn't down with synchronous calls.");
	}

	public boolean conditionalPause() {
		throw new IllegalStateException(
				"This player isn't down with synchronous calls.");
	}

	public boolean conditionalStop() {
		throw new IllegalStateException(
				"This player isn't down with synchronous calls.");
	}

	public boolean conditionalPlay() {
		throw new IllegalStateException(
				"This player isn't down with synchronous calls.");
	}

	public boolean isWaitingToPlay() {
		return false;
	}

	public int getStateMask() {
		return getState();
	}

}
