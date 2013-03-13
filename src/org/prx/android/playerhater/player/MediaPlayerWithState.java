package org.prx.android.playerhater.player;

import java.io.FileDescriptor;
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

public interface MediaPlayerWithState {
	
	public static final int INVALID_STATE = -1;
	
	public static final int IDLE = 0;
	public static final int END = 1;
	public static final int INITIALIZED = 2;
	public static final int PREPARING = 3;
	public static final int PREPARED = 4;
	public static final int STARTED = 5;
	public static final int STOPPED = 6;
	public static final int PAUSED = 7;
	public static final int PLAYBACK_COMPLETED = 8;
	public static final int ERROR = 9;

	public abstract int getState();
	
	public abstract String getStateName();

	public abstract void reset();

	public abstract void release();

	public abstract void prepare() throws IOException,
			IllegalStateException;

	public abstract void prepareAsync() throws IllegalStateException;

	public abstract void start() throws IllegalStateException;

	public abstract void pause() throws IllegalStateException;

	public abstract void stop() throws IllegalStateException;

	public abstract void seekTo(int msec);

	public abstract boolean isPlaying();

	public abstract int getCurrentPosition();

	public abstract int getDuration();

	public abstract void setAudioStreamType(int streamtype);

	public abstract void setDataSource(FileDescriptor fd)
			throws IllegalStateException, IOException,
			IllegalArgumentException, SecurityException;

	public abstract void setDataSource(String path)
			throws IllegalStateException, IOException,
			IllegalArgumentException, SecurityException;

	public abstract void setDataSource(Context context, Uri uri)
			throws IllegalStateException, IOException,
			IllegalArgumentException, SecurityException;

	public abstract void setDataSource(FileDescriptor fd, long offset,
			long length) throws IllegalStateException, IOException,
			IllegalArgumentException;

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

	public abstract MediaPlayer getBarePlayer();

	public abstract MediaPlayer swapPlayer(MediaPlayer barePlayer, int state);

	void swap(MediaPlayerWithState player);
}