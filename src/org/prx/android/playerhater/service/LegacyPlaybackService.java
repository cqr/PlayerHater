package org.prx.android.playerhater.service;

import java.io.IOException;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.IPlayer;
import org.prx.android.playerhater.player.IPlayer.StateManager;

import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;

public class LegacyPlaybackService extends AbstractPlaybackService implements
		OnErrorListener, OnPreparedListener, OnSeekCompleteListener,
		OnCompletionListener, PlayerHaterService {

	private Song mSong;
	private StateManager mMediaPlayer;

	@Override
	public void onCreate() {
		super.onCreate(); 
		mMediaPlayer = buildMediaPlayer(true); 
		this.mLifecycleListener.onNextTrackUnavailable(); 
	}

	@Override
	public Song getNowPlaying() {
		return mSong;
	}

	@Override
	public boolean play(Song song, int startTime)
			throws IllegalArgumentException {
		mSong = song;
		if (mMediaPlayer.getState() != IPlayer.IDLE)
			reset();
		try {
			mMediaPlayer.setDataSource(getApplicationContext(), mSong.getUri());
		} catch (IllegalStateException e) {
			return false;
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Illegal :" + song);
		} catch (IOException e) {
			throw new IllegalArgumentException("Illegal :" + song);
		}
		return play(startTime);
	}

	private void reset() {
		mMediaPlayer.reset();
	}

	@Override
	public void enqueue(Song song) {
		throw new IllegalStateException("You can't enqueue when using the legacy service.");
	}

	@Override
	public void emptyQueue() {}

	@Override
	protected StateManager getMediaPlayer() {
		return mMediaPlayer;
	}

}
