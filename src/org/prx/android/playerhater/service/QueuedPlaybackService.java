package org.prx.android.playerhater.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.IPlayer;
import org.prx.android.playerhater.player.IPlayer.Player;
import org.prx.android.playerhater.player.NextableMediaPlayer;

import android.media.MediaPlayer;
import android.view.KeyEvent;

public class QueuedPlaybackService extends AbstractPlaybackService {

	private final List<Song> mSongs = new ArrayList<Song>();
	private Song mCurrentlyLoadedSong;
	private Song mNextLoadedSong;
	private int mCurrentPosition = 0;
	private Player mCurrentMediaPlayer;
	private Player mNextMediaPlayer;

	@Override
	public void onCreate() {
		super.onCreate();
		mLifecycleListener.onNextTrackAvailable();
	}

	@Override
	public boolean play(Song song, int position)
			throws IllegalArgumentException {
		if (getNowPlaying() != song) {
			mSongs.add(song);
			moveCurrentToLast();
		}
		if (getMediaPlayer().getState() != IPlayer.IDLE)
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
	public Song getNowPlaying() {
		if (mCurrentPosition > 0)
			return mSongs.get(mCurrentPosition - 1);
		return null;
	}

	@Override
	public void enqueue(Song song) {
		mSongs.add(song);
		setNextSong();
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
	protected Player getMediaPlayer() {
		if (mCurrentMediaPlayer == null) {
			mCurrentMediaPlayer = buildMediaPlayer(true);
		}
		return mCurrentMediaPlayer;
	}

	private Player getNextMediaPlayer() {
		if (mNextMediaPlayer == null) {
			mNextMediaPlayer = buildMediaPlayer(false);
		}
		return mNextMediaPlayer;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (!isLast()) {
			Player tmp = mNextMediaPlayer;
			mNextMediaPlayer = mCurrentMediaPlayer;
			mCurrentMediaPlayer = tmp;
			tmp = null;
			mCurrentPosition += 1;
			
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
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			if (getCurrentPosition() < 2000) {
				skipBackward();
			} else {
				super.onRemoteControlButtonPressed(button);
			}
		default:
			super.onRemoteControlButtonPressed(button);
		}
	}

	private void skipForward() {
		seekTo(getDuration() - 5000);
//		if (!isLast()) {
//			mCurrentPosition +=1;
//			skipWasPressed();
//		} else {
//			mCurrentPosition = 1;
//			mLifecycleListener.onSongChanged(getNowPlaying());
//			pause();
//			seekTo(0);
//		}
	}
	
	private void skipBackward() {
		if (!isFirst()) {
			mCurrentPosition -=1;
		}
		skipWasPressed();
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
	
	private boolean isFirst() {
		return mCurrentPosition == 1;
	}
	
	private void moveCurrentToLast() {
		if (!isLast()) {
			mCurrentPosition = mSongs.size();
		}
	}
	
	private void setNextSong() {
		if (!isLast()) {
			Song nextSong = mSongs.get(mCurrentPosition);
			if (!nextSong.equals(mNextLoadedSong)) {
				getNextMediaPlayer().prepare(getApplicationContext(), nextSong.getUri());
			}
		}
	}
	
	@Override
	protected Player buildMediaPlayer(boolean setAsCurrent) {
		return new NextableMediaPlayer(super.buildMediaPlayer(setAsCurrent));
	}
}
