package org.prx.android.playerhater.service;

import org.prx.android.playerhater.Song;
import static org.prx.android.playerhater.player.Syncronous.syncronous;
import static org.prx.android.playerhater.player.WakeLocked.wakeLocked;
import static org.prx.android.playerhater.player.Gapless.gapless;

import org.prx.android.playerhater.player.MediaPlayerWithState;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.player.MediaPlayerWrapper;
import org.prx.android.playerhater.plugins.BackgroundedPlugin;
import org.prx.android.playerhater.util.SongQueue;
import org.prx.android.playerhater.util.SongQueue.OnQeueuedSongsChangedListener;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

public class QueuedPlaybackService extends AbstractPlaybackService implements
		OnQeueuedSongsChangedListener {

	private static final int TICK_DURATION = 500;

	private final SongQueue mSongQueue = new SongQueue();
	private Player mCurrentMediaPlayer;
	private MediaPlayerWithState mNextPlayer;
	private final StateTransitionHandler mHandler = new StateTransitionHandler(
			this);
	private ClockThread mClockThread;

	@Override
	public void onCreate() {
		super.onCreate();
		mSongQueue.setQueuedSongsChangedListener(this);
		mLifecycleListener = new BackgroundedPlugin(mLifecycleListener);
		mLifecycleListener.onNextTrackAvailable();
	}

	@Override
	public boolean play(Song song, int position) {

		// If the song we are being asked to play isn't currently loaded,
		// add it to the end of the playlist and skip there.
		if (getNowPlaying() != song) {
			mSongQueue.appendSong(song);
			mSongQueue.skipToEnd();
		}

		if (getMediaPlayer().prepareAndPlay(getApplicationContext(),
				getNowPlaying().getUri(), position)) {
			startClockThread();
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
		return mSongQueue.getNowPlaying();
	}

	@Override
	public void enqueue(Song song) {
		mSongQueue.appendSong(song);
	}

	@Override
	public void emptyQueue() {
		if (isPlaying() || isPaused()) {
			Song nowPlayingSong = mSongQueue.getNowPlaying();
			mSongQueue.empty();
			mSongQueue.appendSong(nowPlayingSong);
		} else {
			mSongQueue.empty();
		}
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
		if (mSongQueue.isAtLastSong()) {
			getMediaPlayer().pause();
		}
		mSongQueue.next();
	}

	@Override
	public void onRemoteControlButtonPressed(int button) {
		switch (button) {
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			getMediaPlayer().skip();
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			if (getCurrentPosition() > 2000
					|| getNowPlaying() == mSongQueue.back()) {
				super.onRemoteControlButtonPressed(button);
			} else {
				// the value of getNowPlaying() has changed;
			}
		default:
			super.onRemoteControlButtonPressed(button);
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
			mLifecycleListener.onPlaybackStopped();
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
		// We send a tick immediately because it takes some time to start the
		// thread;
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

	@Override
	public void onNowPlayingChanged(Song nowPlaying) {
		// XXX TODO SHOULD PROBABLY PLAY HERE.
		mLifecycleListener.onSongChanged(nowPlaying);
	}

	@Override
	public void onNextSongChanged(Song nextSong) {
		if (nextSong != null) {
			mLifecycleListener.onNextTrackAvailable();
			try {
				getNextPlayer().reset();
				getNextPlayer().setDataSource(getApplicationContext(),
						nextSong.getUri());
			} catch (Exception e) {
				Log.e(TAG, "Problem preparing next player.", e);
			}
			getNextPlayer().prepareAsync();
			getMediaPlayer().setNextMediaPlayer(getNextPlayer());
		}
	}
}
