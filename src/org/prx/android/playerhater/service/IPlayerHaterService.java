package org.prx.android.playerhater.service;

import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;

public interface IPlayerHaterService {

	public abstract boolean play(Song song, int position)
			throws IllegalArgumentException;

	public abstract boolean pause();

	public abstract boolean stop();

	public abstract boolean play() throws IllegalStateException;

	public abstract boolean play(int startTime) throws IllegalStateException; 

	public abstract void setTitle(String title);

	public abstract void setArtist(String artist);

	public abstract int getCurrentPosition();

	public abstract int getDuration();

	public abstract void setOnCompletionListener(OnCompletionListener listener);

	public abstract void setOnSeekCompleteListener(
			OnSeekCompleteListener listener);

	public abstract void setOnErrorListener(OnErrorListener listener);

	public abstract void setOnPreparedListener(OnPreparedListener listener);

	public abstract void setOnShutdownRequestListener(
			ServiceStopListener listener);

	public abstract void setListener(PlayerHaterListener listener);

	public abstract Song getNowPlaying();

	public abstract boolean isPlaying();

	public abstract boolean isLoading();

	public abstract int getState();

	public abstract void setAlbumArt(int resourceId);

	public abstract void setAlbumArt(Uri url);

	public abstract void enqueue(Song song);

	public abstract void emptyQueue();

	public abstract void setIntentClass(Class<? extends Activity> klass);

	public abstract Context getBaseContext();

	public abstract boolean isPaused();

	public abstract void startForeground(int notificationNu,
			Notification notification);

	public abstract void stopForeground(boolean b);

	public abstract void duck();

	public abstract void unduck();

	public abstract void seekTo(int max);

	public abstract boolean play(Song song) throws IllegalArgumentException;

	public abstract void setOnInfoListener(OnInfoListener listener);

	public abstract void setOnBufferingUpdateListener(
			OnBufferingUpdateListener listener);

	public abstract void onRemoteControlButtonPressed(int keycodeMediaNext);

	void setSongInfo(Song song);

	void stopService();

}