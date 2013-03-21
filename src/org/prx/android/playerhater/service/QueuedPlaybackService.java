package org.prx.android.playerhater.service;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import static org.prx.android.playerhater.player.Synchronous.synchronous;
import static org.prx.android.playerhater.player.WakeLocked.wakeLocked;
import static org.prx.android.playerhater.player.Gapless.gapless;

import org.prx.android.playerhater.player.MediaPlayerWithState;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.player.MediaPlayerWrapper;
import org.prx.android.playerhater.plugins.BackgroundedPlugin;
import org.prx.android.playerhater.util.SongQueue;
import org.prx.android.playerhater.util.SongQueue.OnQeueuedSongsChangedListener;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

@SuppressWarnings("unused")
public class QueuedPlaybackService extends NewAbstractPlaybackService implements
		OnQeueuedSongsChangedListener, OnCompletionListener {

	private static final int TICK_DURATION = 500;

	private final SongQueue mSongQueue = new SongQueue();
	private Player mCurrentMediaPlayer;
	private MediaPlayerWithState mNextPlayer;
	private ClockThread mClockThread;
	private final PlayerStateHandler mHandler = new PlayerStateHandler(this);
	private OnCompletionListener mOnCompletionListener;

	/* The Service Life Cycle */

	@Override
	public void onCreate() {
		super.onCreate();
		mSongQueue.setQueuedSongsChangedListener(this);
		mPlugin = new BackgroundedPlugin(mPlugin);
		super.setOnCompletionListener(this);
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
			// stopClockThread();
			onPaused();
			return true;
		}
		return false;
	}

	@Override
	public boolean stop() {
		if (getMediaPlayer().conditionalStop()) {
			stopClockThread();
			stopService();
			return true;
		}
		return false;
	}

	@Override
	public boolean seekTo(int msec) {
		getMediaPlayer().seekTo(msec);
		startClockThread();
		return true;
	}

	@Override
	public boolean play(int mSec) throws IllegalStateException {
		pause(); // Might not be the right state, but who cares?
		seekTo(mSec);
		return play();
	}

	@Override
	public boolean play() throws IllegalStateException {
		if (getMediaPlayer().conditionalPlay()) {
			startClockThread();
			return true;
		}
		return false;

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
	public boolean skipTo(int position) {
		return mSongQueue.skipTo(position);
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
	public void onNowPlayingChanged(Song nowPlaying) {
		// If we got here by looping around the queue
		if (getMediaPlayer().getState() == Player.IDLE) {
			getMediaPlayer().prepare(getApplicationContext(),
					nowPlaying.getUri());
		}
		mPlugin.onSongChanged(nowPlaying);
	}

	@Override
	public void onNextSongChanged(Song nextSong) {
		if (nextSong != null) {
			mPlugin.onNextTrackAvailable(nextSong);
			synchronous(getNextPlayer()).prepare(getApplicationContext(),
					nextSong.getUri());
			getMediaPlayer().setNextMediaPlayer(getNextPlayer());
		} else {
			getMediaPlayer().setNextMediaPlayer(null);
			mPlugin.onNextTrackUnavailable();
		}
	}

	/* END Queue Methods */

	/* Remote Control */

	@Override
	public void onRemoteControlButtonPressed(int button) {
		switch (button) {
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			skip();
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			skipBack();
			break;
		default:
			super.onRemoteControlButtonPressed(button);
		}
	}

	/* END Remote Control */

	/* Listeners */

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mPlugin.onSongFinished(getNowPlaying(), PlayerHater.TRACK_END);
		mSongQueue.next();
		if (mOnCompletionListener != null) {
			mOnCompletionListener.onCompletion(mp);
		}
	}

	/* END Listeners */

	/* Required abstract methods */

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

	protected Player buildMediaPlayer() {
		Player newPlayer;
		newPlayer = synchronous(new MediaPlayerWrapper());
		newPlayer = wakeLocked(newPlayer, getApplicationContext());
		newPlayer = gapless(newPlayer);
		mPlayerListenerManager.setMediaPlayer(newPlayer);
		return newPlayer;
	}

	public void onStateChanged(int from, int to) {
		if (to == Player.STARTED
				&& (from == Player.INVALID_STATE || from == Player.PAUSED)) {
			onResumed();
		} else if (to == Player.STARTED) {
			onStarted();
		}

		if (to == Player.PAUSED || to == Player.PREPARED
				|| to == Player.STOPPED) {
			onPaused();
		}

		if (to == Player.PREPARING) {
			onLoading();
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
		if (getClockThread().isAlive() && !getClockThread().isInterrupted()) {
			getClockThread().interrupt();
			mClockThread = null;
			mHandler.reset();
		}
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
	public void skip() {
		getMediaPlayer().skip();
	}

	@Override
	public void skipBack() {
		if (getCurrentPosition() > 2000 || getNowPlaying() == mSongQueue.back()) {
			super.onRemoteControlButtonPressed(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
		} else {
			getMediaPlayer().prepareAndPlay(getApplicationContext(),
					getNowPlaying().getUri(), 0);
		}
	}
}
