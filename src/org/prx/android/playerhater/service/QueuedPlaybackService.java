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
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.util.SongQueue;
import org.prx.android.playerhater.util.SongQueue.OnQeueuedSongsChangedListener;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

@SuppressWarnings("unused")
public class QueuedPlaybackService extends AbsPlaybackService implements
		OnQeueuedSongsChangedListener, OnCompletionListener, OnErrorListener {

	private final SongQueue mSongQueue = new SongQueue();
	private Player mCurrentMediaPlayer;
	private Player mNextPlayer;
	private PlayerHaterPlugin mPlugin;
	private OnCompletionListener mOnCompletionListener;

	/* The Service Life Cycle */

	@Override
	public void onCreate() {
		super.onCreate();
		mSongQueue.setQueuedSongsChangedListener(this);
		getPlugin();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mPlugin = null;
	}

	@Override
	public void releaseMediaPlayer() {
		if (mCurrentMediaPlayer != null) {
			mCurrentMediaPlayer.release();
			mCurrentMediaPlayer = null;
		}
		if (mNextPlayer != null) {
			mNextPlayer.release();
			mNextPlayer = null;
		}
	}

	@Override
	protected PlayerHaterPlugin getPlugin() {
		if (mPlugin == null) {
			mPlugin = new BackgroundedPlugin(super.getPlugin());
		}
		return mPlugin;
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
			onPaused();
			return true;
		}
		return false;
	}

	@Override
	public boolean stop() {
		if (getMediaPlayer().conditionalStop()) {
			stopService(mSongQueue.toArray());
			stopClockThread();
			return true;
		}
		return false;
	}

	@Override
	public boolean seekTo(int msec) {
		getMediaPlayer().seekTo(msec);
		return true;
	}

	@Override
	public boolean play(int mSec) throws IllegalStateException {
		try {
			if (mSec == 0) {
				return play(getNowPlaying(), mSec);
			}
			pause(); // Might not be the right state, but who cares?
			seekTo(mSec);
			return play();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			throw e;
		}
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

	private Song getNextSong() {
		return mSongQueue.peekNextSong();
	}

	/* Queue Methods */
	@Override
	public synchronized int enqueue(Song song) {
		return mSongQueue.appendSong(song);
	}

	@Override
	public synchronized boolean skipTo(int position) {
		return mSongQueue.skipTo(position);
	}

	@Override
	public synchronized void emptyQueue() {
		if (isLoading() || isPlaying() || isPaused()) {
			Song nowPlayingSong = mSongQueue.getNowPlaying();
			mSongQueue.appendSong(nowPlayingSong);
			mSongQueue.skipToEnd();
		} else {
			mSongQueue.empty();
		}
	}

	@Override
	public synchronized int getQueueLength() {
		return mSongQueue.size();
	}

	@Override
	public synchronized void onNowPlayingChanged(Song nowPlaying) {
		play(nowPlaying, 0);
		mPlugin.onSongChanged(nowPlaying);
	}

	@Override
	public synchronized void onNextSongChanged(Song nextSong) {
		if (nextSong != null) {
			getNextPlayer().prepare(getApplicationContext(), nextSong.getUri());
			getMediaPlayer().setNextMediaPlayer(getNextPlayer());
			mPlugin.onNextSongAvailable(nextSong);
		} else {
			getMediaPlayer().setNextMediaPlayer(null);
			mPlugin.onNextSongUnavailable();
		}
	}

	@Override
	public int getQueuePosition() {
		int position = mSongQueue.getPosition();
		if (getMediaPlayer().getState() == Player.STARTED
				|| getMediaPlayer().getCurrentPosition() != 0) {
			position += 1;
		}
		return position;
	}

	@Override
	public boolean removeFromQueue(int position) {
		return mSongQueue.remove(position);
	}

	@Override
	public synchronized boolean skip() {
		Song currentSong = getNowPlaying();
		mSongQueue.next();
		mPlugin.onSongFinished(currentSong, PlayerHater.SKIP_BUTTON);
		return true;
	}

	@Override
	public synchronized boolean skipBack() {
		if (getCurrentPosition() > 2000 || getNowPlaying() == mSongQueue.back()) {
			onRemoteControlButtonPressed(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
		} else {
			getMediaPlayer().prepareAndPlay(getApplicationContext(),
					getNowPlaying().getUri(), 0);
		}
		return true;
	}

	/* END Queue Methods */

	/* Remote Control */

	@Override
	public synchronized void onRemoteControlButtonPressed(int button) {
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
	public boolean onError(MediaPlayer mediaPlayer, int arg1, int arg2) {
		if (getMediaPlayer().equals(mediaPlayer)) {
			mPlugin.onSongFinished(getNowPlaying(), PlayerHater.ERROR);
			mediaPlayer.reset();
			Player oldPlayer = mCurrentMediaPlayer;
			mCurrentMediaPlayer = getNextPlayer();
			mNextPlayer = oldPlayer;
			mNextPlayer.reset();
			mSongQueue.next();
			return true;
		} else if (getNextPlayer().equals(mediaPlayer)) {
			mSongQueue.remove(mSongQueue.getPosition() + 1);
			return true;
		}
		return false;
	}

	@Override
	public synchronized void onCompletion(MediaPlayer mp) {
		if (getMediaPlayer().equals(mp)) {
			mPlugin.onSongFinished(getNowPlaying(), PlayerHater.TRACK_END);
			mp.reset();
			Player oldPlayer = mCurrentMediaPlayer;
			mCurrentMediaPlayer = getNextPlayer();
			mNextPlayer = oldPlayer;
			mNextPlayer.reset();
		}
	}

	/* END Listeners */

	/* Required abstract methods */

	@Override
	protected synchronized Player getMediaPlayer() {
		if (mCurrentMediaPlayer == null) {
			mCurrentMediaPlayer = (Player) buildMediaPlayer();
		}
		return mCurrentMediaPlayer;
	}

	protected Player getNextPlayer() {
		if (mNextPlayer == null) {
			mNextPlayer = buildMediaPlayer();
		}
		return mNextPlayer;
	}

	protected Player buildMediaPlayer() {
		Player newPlayer;
		newPlayer = synchronous(new MediaPlayerWrapper());
		newPlayer = wakeLocked(newPlayer, getApplicationContext());
		newPlayer = gapless(newPlayer);
		newPlayer.setOnCompletionListener(this);
		newPlayer.setOnErrorListener(this);
		return newPlayer;
	}

	private static final int TICK_DURATION = 500;
	private ClockThread mClockThread;
	private final PlayerStateHandler mHandler = new PlayerStateHandler(this);

	public synchronized void onStateChanged(int from, int to) {
		if (to == Player.STARTED
				&& (from == Player.INVALID_STATE || from == Player.PAUSED)) {
			onResumed();
		} else if (to == Player.STARTED) {
			onStarted();
		}

		if (to == Player.PREPARING || to == Player.LOADING_CONTENT
				|| to == Player.PREPARING_CONTENT) {
			onLoading();
		}
	}

	private synchronized Thread getClockThread() {
		if (mClockThread == null) {
			mClockThread = new ClockThread(mHandler, TICK_DURATION);
		}
		return mClockThread;
	}

	private synchronized void startClockThread() {
		// We send a tick immediately because it takes some time to start the
		// thread;
		mHandler.sendEmptyMessage(0);
		if (!getClockThread().isAlive())
			getClockThread().start();
	}

	private synchronized void stopClockThread() {
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
		}
	}

}
