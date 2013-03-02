package org.prx.android.playerhater;

import android.app.Activity;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Binder;

public class PlayerHaterBinder extends Binder {

	private final PlaybackService mService;
	private PlayerListenerManager mPlayerListenerManager;

	public PlayerHaterBinder(PlaybackService service,
			PlayerListenerManager playerListenerManager) {
		mService = service;
		mPlayerListenerManager = playerListenerManager;
	}

	public boolean play(Song song, int position)
			throws IllegalArgumentException {
		return mService.play(song, position);
	}

	public boolean pause() {
		return mService.pause();
	}

	public boolean stop() {
		return mService.stop();
	}

	public boolean play() throws IllegalStateException {
		return mService.play();
	}

	public boolean play(int startTime) throws IllegalStateException {
		return mService.play(startTime);
	}

	public void setTitle(String title) {
		mService.setTitle(title);
	}

	public void setArtist(String artist) {
		mService.setArtist(artist);
	}

	public void setIntentActivity(Activity activity) {
		mService.setIntentClass(activity.getClass());
	}

	public int getCurrentPosition() {
		return mService.getCurrentPosition();
	}

	public int getDuration() {
		return mService.getDuration();
	}

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mPlayerListenerManager.setOnBufferingUpdateListener(listener);
	}

	public void setOnCompletionListener(OnCompletionListener listener) {
		mPlayerListenerManager.setOnCompletionListener(listener);
	}

	public void setOnInfoListener(OnInfoListener listener) {
		mPlayerListenerManager.setOnInfoListener(listener);
	}

	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mPlayerListenerManager.setOnSeekCompleteListener(listener);
	}

	public void setOnErrorListener(OnErrorListener listener) {
		mPlayerListenerManager.setOnErrorListener(listener);
	}

	public void setOnPreparedListener(OnPreparedListener listener) {
		mPlayerListenerManager.setOnPreparedListener(listener);
	}

	public void setListener(PlayerHaterListener listener) {
		mService.setListener(listener);
	}

	public Song getNowPlaying() {
		return mService.getNowPlaying();
	}

	public boolean isPlaying() {
		return mService.isPlaying();
	}

	public boolean isLoading() {
		return mService.isLoading();
	}

	public int getState() {
		return mService.getState();
	}

	public void setAlbumArt(int resourceId) {
		mService.setAlbumArt(resourceId);
	}

	public void setAlbumArt(Uri url) {
		mService.setAlbumArt(url);
	}

}
