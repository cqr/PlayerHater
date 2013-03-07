package org.prx.android.playerhater.service;

import java.io.IOException;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.NotificationPlugin;
import org.prx.android.playerhater.util.MediaPlayerWrapper;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class LegacyPlaybackService extends AbstractPlaybackService implements
		OnErrorListener, OnPreparedListener, OnSeekCompleteListener,
		OnCompletionListener, PlayerHaterService {

	private Song mSong;
	private MediaPlayerWrapper mMediaPlayer;

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
		if (mMediaPlayer.getState() != MediaPlayerWrapper.IDLE)
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
		// TODO Auto-generated method stub

	}

	@Override
	public void emptyQueue() {
		// TODO Auto-generated method stub

	}

	@Override
	protected MediaPlayerWrapper getMediaPlayer() {
		return mMediaPlayer;
	}

}
