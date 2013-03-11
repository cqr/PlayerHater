package org.prx.android.playerhater.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.util.MediaPlayerWrapper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;

public class QueuedPlaybackService extends AbstractPlaybackService {

	private final List<Song> mSongs = new ArrayList<Song>();
	private Song mCurrentlyLoadedSong;
	private int mCurrentPosition = 0;
	private MediaPlayerWrapper mCurrentMediaPlayer;
	private MediaPlayerWrapper mNextMediaPlayer;

	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		super.onCreate();
		mLifecycleListener.onNextTrackAvailable();
		DisplayManager mgr = (DisplayManager) getSystemService(DISPLAY_SERVICE);
		Log.d(TAG, "DISPLAYS");
		for (Display display : mgr.getDisplays()) {
			Log.d(TAG, display.getName());
		}
		Log.d(TAG, "DISPLAYS");
	}

	@Override
	public boolean play(Song song, int position)
			throws IllegalArgumentException {
		if (getNowPlaying() != song) {
			mSongs.add(song);
			moveCurrentToLast();
		}
		if (getMediaPlayer().getState() != MediaPlayerWrapper.IDLE)
			getMediaPlayer().reset();
		try {
			mCurrentlyLoadedSong = song;
			getMediaPlayer().setDataSource(getApplicationContext(),
					song.getUri());
		} catch (IllegalStateException e) {
			return false;
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Illegal :" + song);
		} catch (IOException e) {
			throw new IllegalArgumentException("Illegal :" + song);
		}
		return play(position);
	}

	@Override
	public void setIntentActivity(Activity activity) {
		// TODO Auto-generated method stub

	}

	@Override
	public Song getNowPlaying() {
		if (mCurrentPosition > 0)
			return mSongs.get(mCurrentPosition - 1);
		return null;
	}

	@Override
	public void enqueue(Song song) {
		mSongs.add(song);
	}

	@Override
	public void emptyQueue() {
		mSongs.clear();
		if (isPlaying() || isPaused()) {
			mSongs.add(mCurrentlyLoadedSong);
			mCurrentPosition = 1;
		}
	}

	@Override
	public void setIntentClass(Class<? extends Activity> klass) {
		// TODO Auto-generated method stub

	}

	@Override
	protected MediaPlayerWrapper getMediaPlayer() {
		if (mCurrentMediaPlayer == null) {
			mCurrentMediaPlayer = buildMediaPlayer(true);
		}
		return mCurrentMediaPlayer;
	}

	protected MediaPlayerWrapper getNextMediaPlayer() {
		if (mNextMediaPlayer == null) {
			mNextMediaPlayer = buildMediaPlayer();
		}
		return mNextMediaPlayer;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (!isLast()) {
			skipForward();
		} else {
			super.onCompletion(mp);
		}
	}
	
	@Override
	public void onRemoteControlButtonPressed(int button) {
		switch(button) {
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			skipForward();
			break;
		default:
			super.onRemoteControlButtonPressed(button);
		}
	}

	private void skipForward() {
		if (!isLast()) {
			mCurrentPosition +=1;
			skipWasPressed();
		} else {
			mCurrentPosition = 1;
			mLifecycleListener.onSongChanged(getNowPlaying());
			pause();
			seekTo(0);
		}
	}

	private void skipWasPressed() {
		if (getNowPlaying() != mCurrentlyLoadedSong) {
			play(getNowPlaying());
		} else {
			seekTo(0);
		}
	}

	private boolean isLast() {
		return mSongs.size() <= mCurrentPosition;
	}
	
	private void moveCurrentToLast() {
		if (!isLast()) {
			mCurrentPosition = mSongs.size();
		}
	}
}
