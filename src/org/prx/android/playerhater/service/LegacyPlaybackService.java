package org.prx.android.playerhater.service;

import java.io.IOException;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.MediaPlayerWithState;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.util.UpdateProgressRunnable;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LegacyPlaybackService extends AbstractPlaybackService implements
		OnErrorListener, OnPreparedListener, OnSeekCompleteListener,
		OnCompletionListener, PlayerHaterService {

	private Song mSong;
	private MediaPlayerWithState mMediaPlayer;
	private OnPreparedListener mOnPreparedListener;
	private int mStartTime;
	
	private Thread mUpdateProgressThread;
	private final Handler mHandler = new UpdateHandler(this);
	private final UpdateProgressRunnable mUpdateProgressRunner = new UpdateProgressRunnable(
			mHandler, PROGRESS_UPDATE);

	@Override
	public void onCreate() {
		super.onCreate(); 
		mMediaPlayer = buildMediaPlayer(true); 
		this.mLifecycleListener.onNextTrackUnavailable(); 
		mPlayerListenerManager.setOnPreparedListener(this);
	}
	
	@Override
	public void onDestroy() {
		stopProgressThread();
		super.onDestroy();
	}
	
	@Override
	public boolean pause() {
		if (super.pause()) {
			stopProgressThread();
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean stop() {
		if (super.stop()) {
			stopProgressThread();
			return true;
		}
		return false;
	}

	@Override
	public void resume() {
		startProgressThread(getMediaPlayer());
		super.resume();
	}
	
	@Override
	public Song getNowPlaying() {
		return mSong;
	}

	@Override
	public boolean play(Song song, int startTime)
			throws IllegalArgumentException {
		mSong = song;
		if (mMediaPlayer.getState() != Player.IDLE)
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
	protected MediaPlayerWithState getMediaPlayer() {
		return mMediaPlayer;
	}
	
	@Override
	public void setOnPreparedListener(OnPreparedListener onPrepared) {
		mOnPreparedListener = onPrepared;
	}
	
	@Override
	public boolean play(int startTime) throws IllegalStateException {
		mStartTime = startTime;
		return play();
	}


	@Override
	public void onPrepared(MediaPlayer mp) {
		if (mStartTime > 0) {
			int seekTo = mStartTime;
			mStartTime = 0;
			seekTo(seekTo);
		}
		
		getMediaPlayer().start();
		sendStartedPlaying();
		startProgressThread(getMediaPlayer());
		
		if (mOnPreparedListener != null) {
			mOnPreparedListener.onPrepared(mp);
		}
	}

	
	private static class UpdateHandler extends Handler {
		private AbstractPlaybackService mService;

		private UpdateHandler(AbstractPlaybackService playbackService) {
			mService = playbackService;
		}

		@Override
		public void handleMessage(Message m) {
			switch (m.what) {
			case PROGRESS_UPDATE:
				mService.sendIsPlaying(m.arg1);
				break;
			}
		}
	}
	
	protected void stopProgressThread() {
		Log.d(TAG, "STOPPING PROGRESS THREAD");
		if (mUpdateProgressThread != null && mUpdateProgressThread.isAlive()) {
			mHandler.removeCallbacks(mUpdateProgressRunner);
			mUpdateProgressThread.interrupt();
			mUpdateProgressThread = null;
		}
	}

	protected void startProgressThread(MediaPlayerWithState mp) {
		stopProgressThread();
		mUpdateProgressRunner.setMediaPlayer(mp);
		mUpdateProgressThread = new Thread(mUpdateProgressRunner);
		mUpdateProgressThread.start();
	}
}
