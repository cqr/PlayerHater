package org.prx.android.playerhater.service;

import org.prx.android.playerhater.Song;
import static org.prx.android.playerhater.player.Synchronous.synchronous;
import static org.prx.android.playerhater.player.WakeLocked.wakeLocked;
import static org.prx.android.playerhater.player.Gapless.gapless;

import org.prx.android.playerhater.player.MediaPlayerWithState;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.player.MediaPlayerWrapper;
import org.prx.android.playerhater.plugins.BackgroundedPlugin;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.util.SongQueue;
import org.prx.android.playerhater.util.SongQueue.OnQeueuedSongsChangedListener;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

public class QueuedPlaybackService extends NewAbstractPlaybackService implements
		OnQeueuedSongsChangedListener {

	private static final int TICK_DURATION = 500;

	private final SongQueue mSongQueue = new SongQueue();
	private Player mCurrentMediaPlayer;
	private MediaPlayerWithState mNextPlayer;
	private PlayerHaterPlugin mSlowPlugin;
	private ClockThread mClockThread;
	private final PlayerStateHandler mHandler = new PlayerStateHandler(this);

	/* The Service Life Cycle */

	@Override
	public void onCreate() {
		super.onCreate();
		mSongQueue.setQueuedSongsChangedListener(this);
		mPlugin.onNextTrackAvailable();
		mSlowPlugin = new BackgroundedPlugin(mPlugin);
	}
	
	/* END The Service Life Cycle */
	
	/* Playback Methods */

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
		if (getMediaPlayer().conditionalPause()) {
			stopClockThread();
			return true;
		}
		return false;
	}

	@Override
	public boolean stop() {
		if (getMediaPlayer().conditionalStop()) {
			stopClockThread();
			return true;
		}
		return false;
	}
	
	@Override
	public void seekTo(int msec) {
		getMediaPlayer().seekTo(msec);
	}
	
	@Override
	public boolean play(int mSec) throws IllegalStateException {
		pause(); // Might not be the right state, but who cares?
		seekTo(mSec);
		return play();
	}
	
	@Override
	public boolean play() throws IllegalStateException {
		return getMediaPlayer().conditionalPlay();
	}

	/* END Playback Methods */
	
	/* State Methods */
	@Override
	public Song getNowPlaying() {
		return mSongQueue.getNowPlaying();
	}
	
	/* Queue Methods */
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
	
	/* END Queue Methods */
	
	/* Remote Control */
	
	@Override
	public void onRemoteControlButtonPressed(int button) {
		switch (button) {
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			next();
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			if (getCurrentPosition() > 2000
					|| getNowPlaying() == mSongQueue.back()) {
				super.onRemoteControlButtonPressed(button);
			} else {
				// XXX TODO FIXME
				// the value of getNowPlaying() has changed;
			}
		default:
			super.onRemoteControlButtonPressed(button);
		}
	}
	
	/* END Remote Control */

	@Override
	protected Player getMediaPlayer() {
		if (mCurrentMediaPlayer == null) {
			mCurrentMediaPlayer = (Player) buildMediaPlayer();
		}
		return mCurrentMediaPlayer;
	}

	protected MediaPlayerWithState getNextPlayer() {
		if (mNextPlayer == null) {
			mNextPlayer = new MediaPlayerWrapper();
		}
		return mNextPlayer;
	}

//	@Override
//	public void onCompletion(MediaPlayer mp) {
//		if (mSongQueue.isAtLastSong()) {
//			getMediaPlayer().pause();
//		}
//		mSongQueue.next();
//	}

	
	protected Player buildMediaPlayer() {
		Player newPlayer;
		newPlayer = synchronous(new MediaPlayerWrapper());
		newPlayer = wakeLocked(newPlayer, getApplicationContext());
		newPlayer = gapless(newPlayer);
		return newPlayer;
	}

	public void onStateChanged(int from, int to) {
		if (to == Player.STARTED
				&& (from == Player.INVALID_STATE || from == Player.PAUSED)) {
			mPlugin.onPlaybackResumed();
		} else if (to == Player.STARTED) {
			mSlowPlugin.onSongChanged(getNowPlaying());
			mSlowPlugin.onDurationChanged(getDuration());
			mPlugin.onPlaybackStarted();
		}

		if (to == Player.PAUSED) {
			mPlayerHaterListener.onPaused(getNowPlaying());
			mPlugin.onPlaybackPaused();
		}

		if (to == Player.PREPARING || to == Player.PREPARED) {
			mPlayerHaterListener.onLoading(getNowPlaying());
			mSlowPlugin.onAudioLoading();
		}

		if (to == Player.STOPPED) {
			mPlugin.onPlaybackStopped();
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

	private static final class PlayerStateHandler extends Handler {

		private final QueuedPlaybackService mService;
		private int currentState = Player.INVALID_STATE;

		private PlayerStateHandler(QueuedPlaybackService service) {
			mService = service;
		}

		private void reset() {
			currentState = Player.INVALID_STATE;
		}

		@Override
		public void handleMessage(Message msg) {
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
		mPlugin.onSongChanged(nowPlaying);
	}

	@Override
	public void onNextSongChanged(Song nextSong) {
		if (nextSong != null) {
			mPlugin.onNextTrackAvailable();
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
	
	/* Private utility methods */
	
	private void next() {
		getMediaPlayer().skip();
	}

}
