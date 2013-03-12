package org.prx.android.playerhater.service;

import java.util.ArrayList;
import java.util.List;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.Syncronous;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.player.MediaPlayerWrapper;
import org.prx.android.playerhater.player.Gapless;

import android.media.MediaPlayer;
import android.view.KeyEvent;

public class QueuedPlaybackService extends AbstractPlaybackService {

	private final List<Song> mSongs = new ArrayList<Song>();
	private Song mCurrentlyLoadedSong;
	private Song mNextLoadedSong;
	private int mCurrentPosition = 0;
	private Player mCurrentMediaPlayer;
	private Player mNextPlayer;

	@Override
	public void onCreate() {
		super.onCreate();
		mLifecycleListener.onNextTrackAvailable();
	}

	@Override
	public boolean play(Song song, int position) {

		// If the song we are being asked to play isn't currently loaded,
		// add it to the end of the playlist and skip there.
		if (getNowPlaying() != song) {
			enqueue(song);
			skipToLastSong();
		}

		if (getMediaPlayer().prepareAndPlay(getApplicationContext(),
				getNowPlaying().getUri(), position)) {
			sendIsLoading();
			mCurrentlyLoadedSong = getNowPlaying();
			startProgressThread(getMediaPlayer());
			return true;
		} else {
			return false;
		}
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
		if (mCurrentPosition < 1) {
			mCurrentPosition = 1;
		}
		setNextSong();
	}

	@Override
	public void emptyQueue() {
		mSongs.clear();
		if (isPlaying() || isPaused()) {
			mSongs.add(mCurrentlyLoadedSong);
			mCurrentPosition = 1;
		}
		setNextSong();
	}

	@Override
	protected Player getMediaPlayer() {
		if (mCurrentMediaPlayer == null) {
			mCurrentMediaPlayer = (Player) buildMediaPlayer(true);
		}
		return mCurrentMediaPlayer;
	}

	protected Player getNextPlayer() {
		if (mNextPlayer == null) {
			mNextPlayer = new Syncronous(new MediaPlayerWrapper());
		}
		return mNextPlayer;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (getMediaPlayer().isPlaying()) {
			nextSongIsStarted();
		} else {
			super.onCompletion(mp);
		}
	}

	private void nextSongIsStarted() {
		mCurrentPosition += 1;
		mCurrentlyLoadedSong = mNextLoadedSong;
		mNextLoadedSong = null;
		mLifecycleListener.onSongChanged(getNowPlaying());
		setNextSong();
	}

	@Override
	public void onRemoteControlButtonPressed(int button) {
		switch (button) {
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
		if (!isLast()) {
			mCurrentPosition += 1;
			skipWasPressed();
		} else {
			if (mCurrentPosition != 1) {
				mCurrentPosition = 1;
				mLifecycleListener.onSongChanged(getNowPlaying());
				getMediaPlayer().prepare(getApplicationContext(), getNowPlaying().getUri());
				setNextSong();
			}
			pause();
			seekTo(0);
		}
	}

	private void skipBackward() {
		if (!isFirst()) {
			mCurrentPosition -= 1;
		}
		skipWasPressed();
	}

	private void skipWasPressed() {
		if (getNowPlaying() != mCurrentlyLoadedSong) {
			if (getNowPlaying() == mNextLoadedSong) {
				promoteNextPlayer();
				play();
			} else {
				play(getNowPlaying());
			}
		} else {
			seekTo(0);
		}
	}

	private void promoteNextPlayer() {
		getMediaPlayer().swap(getNextPlayer());
		mNextPlayer = null;
		mCurrentlyLoadedSong = mNextLoadedSong;
		mNextLoadedSong = null;
		mLifecycleListener.onSongChanged(getNowPlaying());
	}

	private boolean isLast() {
		return mCurrentPosition <= 0 || mSongs.size() <= mCurrentPosition;
	}

	private boolean isFirst() {
		return mCurrentPosition <= 1;
	}

	private void skipToLastSong() {
		if (!isLast()) {
			mCurrentPosition = mSongs.size();
		}
	}

	private void setNextSong() {
		if (!isLast()) {
			Song nextSong = mSongs.get(mCurrentPosition);
			if (!nextSong.equals(mNextLoadedSong)) {
				getNextPlayer().prepare(getApplicationContext(),
						nextSong.getUri());
				getMediaPlayer().setNextMediaPlayer(getNextPlayer());
				mNextLoadedSong = nextSong;
			}
		} else {
			getMediaPlayer().setNextMediaPlayer(null);
		}
	}

	@Override
	protected Player buildMediaPlayer() {
		return new Syncronous(new Gapless(
				super.buildMediaPlayer()));
	}
}
