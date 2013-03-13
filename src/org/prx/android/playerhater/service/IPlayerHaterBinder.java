package org.prx.android.playerhater.service;

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

public interface IPlayerHaterBinder {

	public abstract void registerShutdownRequestListener(
			ServiceStopListener listener);

	public abstract boolean play(Song song, int position)
			throws IllegalArgumentException;

	public abstract boolean pause();

	public abstract boolean stop();

	public abstract boolean play() throws IllegalStateException;

	public abstract boolean play(int startTime) throws IllegalStateException;

	public abstract void seekTo(int startTime) throws IllegalStateException;

	public abstract void setTitle(String title);

	public abstract void setArtist(String artist);

	public abstract void setIntentActivity(Activity activity);

	public abstract int getCurrentPosition();

	public abstract int getDuration();

	public abstract void setOnBufferingUpdateListener(
			OnBufferingUpdateListener listener);

	public abstract void setOnCompletionListener(OnCompletionListener listener);

	public abstract void setOnInfoListener(OnInfoListener listener);

	public abstract void setOnSeekCompleteListener(
			OnSeekCompleteListener listener);

	public abstract void setOnErrorListener(OnErrorListener listener);

	public abstract void setOnPreparedListener(OnPreparedListener listener);

	public abstract void setListener(PlayerHaterListener listener);

	public abstract Song getNowPlaying();

	public abstract boolean isPlaying();

	public abstract boolean isLoading();

	public abstract int getState();

	public abstract void setAlbumArt(int resourceId);

	public abstract void setAlbumArt(Uri url);

	public abstract void enqueue(Song song);

	public abstract void emptyQueue();

	public abstract void onRemoteControlButtonPressed(int keyCode);

}