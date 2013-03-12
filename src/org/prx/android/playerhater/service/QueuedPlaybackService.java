package org.prx.android.playerhater.service;

import java.util.ArrayList;
import java.util.List;

import org.prx.android.playerhater.Song;
import static org.prx.android.playerhater.player.Syncronous.syncronous;
import static org.prx.android.playerhater.player.WakeLocked.wakeLocked;
import static org.prx.android.playerhater.player.Gapless.gapless;

import org.prx.android.playerhater.player.MediaPlayerWithState;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.player.MediaPlayerWrapper;
import org.prx.android.playerhater.plugins.BackgroundedPlugin;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

public class QueuedPlaybackService extends AbstractPlaybackService {

	private static final int TICK_DURATION = 500;

	private final List<Song> mSongs = new ArrayList<Song>();
	private Song mCurrentlyLoadedSong;
	private Song mNextLoadedSong;
	private int mCurrentPosition = 0;
	private Player mCurrentMediaPlayer;
	private MediaPlayerWithState mNextPlayer;
	private final StateTransitionHandler mHandler = new StateTransitionHandler(
			this);
	private ClockThread mClockThread;

	@Override
	public void onCreate() {
		super.onCreate();
		mLifecycleListener = new BackgroundedPlugin(mLifecycleListener);
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
			startClockThread();
			mCurrentlyLoadedSong = getNowPlaying();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean pause() {
		if (super.pause()) {
			stopClockThread();
			return true;
		}
		return false;
	}

	@Override
	public void resume() {
		super.resume();
		startClockThread();
	}

	@Override
	public boolean stop() {
		if (super.stop()) {
			stopClockThread();
			return true;
		}
		return false;
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

	protected MediaPlayerWithState getNextPlayer() {
		if (mNextPlayer == null) {
			mNextPlayer = new MediaPlayerWrapper();
		}
		return mNextPlayer;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (isLast()) {
			skipForward();
		} else {
			nextSongIsStarted();
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
				getMediaPlayer().prepare(getApplicationContext(),
						getNowPlaying().getUri());
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
				try {
					getNextPlayer().setDataSource(getApplicationContext(),
							nextSong.getUri());
				} catch (Exception e) {
					Log.e(TAG, "Problem preparing next player.", e);
				}
				getNextPlayer().prepareAsync();
				getMediaPlayer().setNextMediaPlayer(getNextPlayer());
				mNextLoadedSong = nextSong;
			}
		} else {
			getMediaPlayer().setNextMediaPlayer(null);
		}
	}

	@Override
	protected Player buildMediaPlayer() {
		Player newPlayer;
		newPlayer = syncronous(super.buildMediaPlayer());
		newPlayer = wakeLocked(newPlayer, getApplicationContext());
		newPlayer = gapless(newPlayer);
		return newPlayer;
	}

	public void onStateChanged(int from, int to) {
		if (to == Player.STARTED
				&& (from == Player.INVALID_STATE || from == Player.PAUSED)) {
			mLifecycleListener.onPlaybackResumed();
		} else if (to == Player.STARTED) {
			mLifecycleListener.onSongChanged(getNowPlaying());
			mLifecycleListener.onDurationChanged(getDuration());
			mLifecycleListener.onPlaybackStarted();
		}

		if (to == Player.PAUSED) {
			mPlayerHaterListener.onPaused(getNowPlaying());
			mLifecycleListener.onPlaybackPaused();
		}

		if (to == Player.PREPARING || to == Player.PREPARED) {
			mPlayerHaterListener.onLoading(getNowPlaying());
			mLifecycleListener.onAudioLoading();
		}

		if (to == Player.STOPPED) {
			mPlayerHaterListener.onStopped();
		}
	}

	public void onTick() {
		if (isPlaying() && mPlayerHaterListener != null) {
			mPlayerHaterListener.onPlaying(getNowPlaying(),
					getCurrentPosition());
		}
	}
	
	private Thread getClockThread() {
		if (mClockThread == null) {
			mClockThread = new ClockThread(mHandler, TICK_DURATION);
		}
		return mClockThread;
	}

	private void startClockThread() {
		// We send a tick immediately because it takes some time to start the thread;
		mHandler.sendEmptyMessage(0);
		if (!getClockThread().isAlive())
			getClockThread().start();
	}

	private void stopClockThread() {
		mClockThread.interrupt();
		mClockThread = null;
		mHandler.reset();
	}

	private static final class StateTransitionHandler extends Handler {

		private final QueuedPlaybackService mService;
		private int currentState = Player.INVALID_STATE;

		private StateTransitionHandler(QueuedPlaybackService service) {
			mService = service;
		}

		private void reset() {
			currentState = Player.INVALID_STATE;
		}

		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "Got a message with state: "
					+ mService.getMediaPlayer().getStateName());

			if (mService.getState() != currentState) {
				mService.onStateChanged(currentState, mService.getState());
				currentState = mService.getState();
			}
			mService.onTick();
		}
	}
}
