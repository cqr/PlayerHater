package org.prx.playerhater.mediaplayer;

import java.io.IOException;

import org.prx.playerhater.util.Log;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class StatelyPlayer extends Player implements OnBufferingUpdateListener,
		OnCompletionListener, OnErrorListener, OnInfoListener,
		OnPreparedListener, OnSeekCompleteListener {

	private MediaPlayer mMediaPlayer;

	public static class ListenerCollection {
		public OnErrorListener errorListener;
		public OnPreparedListener preparedListener;
		public OnCompletionListener completionListener;
		public OnBufferingUpdateListener bufferingUpdateListener;
		public OnInfoListener infoListener;
		public OnSeekCompleteListener seekCompleteListener;
	}

	private ListenerCollection mListenerCollection;

	private int mState;
	private int mPrevState;

	public StatelyPlayer() {
		mMediaPlayer = new MediaPlayer();
		setState(IDLE);
		mListenerCollection = new ListenerCollection();
		getBarePlayer().setOnBufferingUpdateListener(this);
		getBarePlayer().setOnCompletionListener(this);
		getBarePlayer().setOnErrorListener(this);
		getBarePlayer().setOnInfoListener(this);
		getBarePlayer().setOnPreparedListener(this);
		getBarePlayer().setOnSeekCompleteListener(this);
	}

	@Override
	public synchronized int getState() {
		return this.mState;
	}

	private synchronized void setState(int state) {
		mState = state;
	}

	public static String getStateName(int state) {
		switch (state) {
		case END:
			return "end";
		case ERROR:
			return "error";
		case IDLE:
			return "idle";
		case INITIALIZED:
			return "initialized";
		case LOADING_CONTENT:
			return "loading content";
		case PAUSED:
			return "paused";
		case PLAYBACK_COMPLETED:
			return "playback completed";
		case PREPARED:
			return "prepared";
		case PREPARING:
			return "preparing";
		case PREPARING_CONTENT:
			return "preparing content";
		case STARTED:
			return "started";
		case STOPPED:
			return "stopped";
		}
		throw new IllegalStateException("Impossible state index: " + state);
	}

	@Override
	public synchronized String getStateName() {
		return getStateName(getState());
	}

	@Override
	public synchronized void reset() {
		setState(IDLE);
		this.mMediaPlayer.reset();
	}

	@Override
	public synchronized void release() {
		this.mMediaPlayer.release();
		setState(END);
	}

	@Override
	public synchronized void prepareAsync() throws IllegalStateException {
		if (getState() == INITIALIZED || getState() == STOPPED) {
			mMediaPlayer.prepareAsync();
			setState(PREPARING);
		} else if (getState() == LOADING_CONTENT) {
			setState(PREPARING_CONTENT);
		} else {
			throw illegalState("prepareAsync");
		}
	}

	@Override
	public synchronized void start() throws IllegalStateException {
		if (getState() == PREPARED || getState() == STARTED
				|| getState() == PAUSED || getState() == PLAYBACK_COMPLETED) {
			mMediaPlayer.start();
			setState(STARTED);
		} else {
			throw illegalState("start");
		}
	}

	@Override
	public synchronized void pause() throws IllegalStateException {
		if (getState() == STARTED || getState() == PAUSED) {
			mMediaPlayer.pause();
			setState(PAUSED);
		} else {
			throw illegalState("pause");
		}
	}

	private IllegalStateException illegalState(String methodName) {
		IllegalStateException e = new IllegalStateException("Cannot call "
				+ methodName + " in the " + getStateName(getState())
				+ " state.");
		e.printStackTrace();
		return e;
	}

	@Override
	public synchronized void stop() throws IllegalStateException {
		if (getState() == PREPARED || getState() == STARTED
				|| this.getState() == STOPPED || this.getState() == PAUSED
				|| this.getState() == PLAYBACK_COMPLETED) {
			this.mMediaPlayer.stop();
			setState(STOPPED);
		} else {
			throw illegalState("stop");
		}
	}

	@Override
	public synchronized void seekTo(int msec) {
		if (this.getState() == PREPARED || this.getState() == STARTED
				|| this.getState() == PAUSED
				|| this.getState() == PLAYBACK_COMPLETED) {
			this.mPrevState = this.getState();
			setState(PREPARING);
			this.mMediaPlayer.seekTo(msec);
		} else {
			throw illegalState("seekTo");
		}
	}

	@Override
	public synchronized boolean isPlaying() {
		return this.mMediaPlayer.isPlaying();
	}

	@Override
	public synchronized int getCurrentPosition() {
		if (this.getState() == STARTED || this.getState() == PAUSED
				|| this.getState() == STOPPED
				|| this.getState() == PLAYBACK_COMPLETED) {
			return this.mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	@Override
	public synchronized int getDuration() {
		if (this.getState() == PREPARED || this.getState() == STARTED
				|| this.getState() == PAUSED
				|| this.getState() == PLAYBACK_COMPLETED) {
			return this.mMediaPlayer.getDuration();
		}
		return 0;
	}

	@Override
	public synchronized void setAudioStreamType(int streamtype) {
		this.mMediaPlayer.setAudioStreamType(streamtype);
	}

	@Override
	public synchronized void setDataSource(final Context context, final Uri uri)
			throws IllegalStateException, IOException,
			IllegalArgumentException, SecurityException {
		if (uri.getScheme().equals("content")) {
			setState(LOADING_CONTENT);
			(new Thread() {
				@Override
				public void run() {
					try {
						ParcelFileDescriptor fd = context.getContentResolver()
								.openFileDescriptor(uri, "r");
						mMediaPlayer.setDataSource(fd.getFileDescriptor());
						if (StatelyPlayer.this.getState() == PREPARING_CONTENT) {
							setState(INITIALIZED);
							prepareAsync();
						} else {
							setState(INITIALIZED);
						}
					} catch (Exception e) {
						try {
							mMediaPlayer.setDataSource(uri.toString());
							if (StatelyPlayer.this.getState() == PREPARING_CONTENT) {
								setState(INITIALIZED);
								prepareAsync();
							} else {
								setState(INITIALIZED);
							}
						} catch (Exception e1) {
							Log.e("Whoops", e1);
						}
					}
				}
			}).start();
		} else {
			mMediaPlayer.setDataSource(uri.toString());
			setState(INITIALIZED);
		}
	}

	@Override
	public synchronized void setOnErrorListener(OnErrorListener errorListener) {
		mListenerCollection.errorListener = errorListener;
	}

	@Override
	public synchronized void setOnPreparedListener(
			OnPreparedListener preparedListener) {
		mListenerCollection.preparedListener = preparedListener;
	}

	@Override
	public synchronized void setOnBufferingUpdateListener(
			OnBufferingUpdateListener bufferingUpdateListener) {
		mListenerCollection.bufferingUpdateListener = bufferingUpdateListener;
	}

	@Override
	public synchronized void setOnCompletionListener(
			OnCompletionListener completionListener) {
		mListenerCollection.completionListener = completionListener;
	}

	@Override
	public synchronized void setOnInfoListener(OnInfoListener infoListener) {
		mListenerCollection.infoListener = infoListener;
	}

	@Override
	public synchronized void setOnSeekCompleteListener(
			OnSeekCompleteListener seekCompleteListener) {
		mListenerCollection.seekCompleteListener = seekCompleteListener;
	}

	@Override
	public synchronized void onBufferingUpdate(MediaPlayer mp, int percent) {
		if (mListenerCollection.bufferingUpdateListener != null) {
			mListenerCollection.bufferingUpdateListener.onBufferingUpdate(mp,
					percent);
		}
	}

	@Override
	public synchronized void onCompletion(MediaPlayer mp) {
		setState(PLAYBACK_COMPLETED);
		if (mListenerCollection.completionListener != null) {
			mListenerCollection.completionListener.onCompletion(mp);
		}
	}

	@Override
	public synchronized boolean onError(MediaPlayer mp, int what, int extra) {
		setState(ERROR);
		if (mListenerCollection.errorListener != null) {
			return mListenerCollection.errorListener.onError(mp, what, extra);
		}
		return false;
	}

	@Override
	public synchronized boolean onInfo(MediaPlayer mp, int what, int extra) {
		if (mListenerCollection.infoListener != null) {
			return mListenerCollection.infoListener.onInfo(mp, what, extra);
		}
		return false;
	}

	@Override
	public synchronized void onPrepared(MediaPlayer mp) {
		setState(PREPARED);
		if (mListenerCollection.preparedListener != null) {
			mListenerCollection.preparedListener.onPrepared(mp);
		}
	}

	@Override
	public synchronized void onSeekComplete(MediaPlayer mp) {
		setState(mPrevState);
		if (mListenerCollection.seekCompleteListener != null) {
			mListenerCollection.seekCompleteListener.onSeekComplete(mp);
		}
	}

	@Override
	public synchronized void setVolume(float leftVolume, float rightVolume) {
		mMediaPlayer.setVolume(leftVolume, rightVolume);
	}

	@Override
	public synchronized boolean equals(MediaPlayer mp) {
		return mp == mMediaPlayer;
	}

	private synchronized MediaPlayer getBarePlayer() {
		return mMediaPlayer;
	}

	@Override
	public String toString() {
		return mMediaPlayer.toString() + " (" + getStateName() + ")";
	}
}
