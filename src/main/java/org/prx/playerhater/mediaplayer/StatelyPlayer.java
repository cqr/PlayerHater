/*******************************************************************************
 * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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

	/**
	 * An invalid state for a {@linkplain MediaPlayer} to be in.
	 */
	public static final int INVALID_STATE = -1;

	/**
	 * The state a {@linkplain MediaPlayer} is in when initialized. Nothing is
	 * loaded into the player.
	 */
	public static final int IDLE = 0;

	/**
	 * The state a {@linkplain MediaPlayer} enters after
	 * {@linkplain MediaPlayer#release()} is called on it. This
	 * {@linkplain MediaPlayer} will be unusable until {@linkplain #reset()} is
	 * called on it.
	 */
	public static final int END = 1;

	/**
	 * The state a {@linkplain MediaPlayer} is in when
	 * {@linkplain #setDataSource(Context, Uri)} is called on it.
	 */
	public static final int INITIALIZED = 2;

	/**
	 * The state a {@linkplain MediaPlayer} is in when
	 * {@linkplain #prepareAsync()} is called on it.
	 */
	public static final int PREPARING = 4;

	/**
	 * The state a {@linkplain MediaPlayer} is in when
	 * {@linkplain #prepareAsync()} is complete or when {{@link #prepare()} is
	 * called.
	 */
	public static final int PREPARED = 8;

	/**
	 * The state a {@linkplain MediaPlayer} is in when playing.
	 */
	public static final int STARTED = 16;

	/**
	 * The state a {@linkplain MediaPlayer} is in when {{@link #stop()} is
	 * called.
	 */
	public static final int STOPPED = 32;

	/**
	 * The state a {@linkplain MediaPlayer} is in when {{@link #pause()} is
	 * called.
	 */
	public static final int PAUSED = 64;

	/**
	 * The state a {@linkplain MediaPlayer} is in when it has reached the end of
	 * the data loaded into it.
	 */
	public static final int PLAYBACK_COMPLETED = 128;

	/**
	 * Internal state used for content:// URIs
	 */
	public static final int LOADING_CONTENT = -256;

	/**
	 * Internal state used for content:// URIs
	 */
	public static final int PREPARING_CONTENT = -512;

	/**
	 * The state a {@linkplain MediaPlayer} is in when an error has occurred.
	 */
	public static final int ERROR = 1024;

	/**
	 * Used in a statemask to indicate that the player is getting ready to play.
	 */
	public static final int WILL_PLAY = 65536;

	/**
	 * Used in a statemask to indicate that the player is not able to seek.
	 */
	public static final int NOT_SEEKABLE = 2048;

	public static boolean willPlay(int stateMask) {
		return (stateMask & WILL_PLAY) == WILL_PLAY;
	}

	public static boolean seekable(int stateMask) {
		return (stateMask & NOT_SEEKABLE) != NOT_SEEKABLE;
	}

	public static int mediaPlayerState(int stateMask) {
		if (willPlay(stateMask)) {
			stateMask &= ~WILL_PLAY;
		}
		if (!seekable(stateMask)) {
			stateMask &= ~NOT_SEEKABLE;
		}
		return stateMask;
	}

	private final MediaPlayer mMediaPlayer;
	private StateChangeListener mStateChangeListener;
	private boolean mBuffering = false;
	private boolean mNotSeekable = false;

	public static class ListenerCollection {
		public OnErrorListener errorListener;
		public OnPreparedListener preparedListener;
		public OnCompletionListener completionListener;
		public OnBufferingUpdateListener bufferingUpdateListener;
		public OnInfoListener infoListener;
		public OnSeekCompleteListener seekCompleteListener;
	}

	private final ListenerCollection mListenerCollection;

	private int mState;
	private int mPrevState;

	private boolean mInErrorCallback;

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
	public void setStateChangeListener(StateChangeListener listener) {
		mStateChangeListener = listener;
	}

	@Override
	public synchronized int getState() {
		return getPublicState(getInternalState());
	}

	public synchronized int getStateMask() {
		return getState() | (isWaitingToPlay() ? WILL_PLAY : 0)
				| (mNotSeekable ? NOT_SEEKABLE : 0);
	}

	private synchronized int getInternalState() {
		return mState;
	}

	private int getPublicState(int internalState) {
		if (internalState >= 0) {
			return internalState;
		}
		switch (internalState) {
		case PREPARING_CONTENT:
			return PREPARING;
		case LOADING_CONTENT:
			return INITIALIZED;
		default:
			return INVALID_STATE;
		}
	}

	private synchronized void setState(int state) {
		mState = state;
		mInErrorCallback = false;
		onStateChanged();
	}

	protected void onStateChanged() {
		if (mStateChangeListener != null) {
			mStateChangeListener.onStateChanged(this, getStateMask());
		}
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
			return "initialized(content uri)";
		case PAUSED:
			return "paused";
		case PLAYBACK_COMPLETED:
			return "playback completed";
		case PREPARED:
			return "prepared";
		case PREPARING:
			return "preparing";
		case PREPARING_CONTENT:
			return "preparing(content uri)";
		case STARTED:
			return "started";
		case STOPPED:
			return "stopped";
		}
		throw new IllegalStateException("Impossible state index: " + state);
	}

	@Override
	public synchronized String getStateName() {
		return getStateName(getInternalState());
	}

	@Override
	public synchronized void reset() {
		if (getState() != IDLE) {
			setState(IDLE);
			mMediaPlayer.reset();
		}
	}

	@Override
	public synchronized void release() {
		mMediaPlayer.release();
		setState(END);
	}

	@Override
	public synchronized void prepareAsync() throws IllegalStateException {
		int state = getInternalState();
		if ((state & (INITIALIZED | STOPPED)) != 0) {
			mMediaPlayer.prepareAsync();
			setState(PREPARING);
		} else if (state == LOADING_CONTENT) {
			setState(PREPARING_CONTENT);
		} else {
			throw illegalState("prepareAsync");
		}
	}

	private static final int START_BITMASK = PREPARED | STARTED | PAUSED
			| PLAYBACK_COMPLETED;

	@Override
	public synchronized void start() throws IllegalStateException {
		int state = getInternalState();
		if ((state & START_BITMASK) != 0) {
			mMediaPlayer.start();
			setState(STARTED);
		} else {
			throw illegalState("start");
		}
	}

	private static final int PAUSE_BITMASK = STARTED | PAUSED;

	@Override
	public synchronized void pause() throws IllegalStateException {
		int state = getInternalState();
		if ((state & PAUSE_BITMASK) != 0) {
			mMediaPlayer.pause();
			setState(PAUSED);
		} else {
			throw illegalState("pause");
		}
	}

	private static final int STOP_BITMASK = PREPARED | STARTED | STOPPED
			| PAUSED | PLAYBACK_COMPLETED;

	@Override
	public synchronized void stop() throws IllegalStateException {
		int state = getInternalState();
		if ((state & (STOP_BITMASK)) != 0) {
			mMediaPlayer.stop();
			setState(STOPPED);
		} else {
			throw illegalState("stop");
		}
	}

	private static final int SEEK_TO_BITMASK = PREPARED | STARTED | PAUSED
			| PLAYBACK_COMPLETED;

	@Override
	public synchronized void seekTo(int msec) {
		int state = getInternalState();
		if ((state & SEEK_TO_BITMASK) != 0) {
			mPrevState = getInternalState();
			setState(PREPARING);
			mMediaPlayer.seekTo(msec);
		} else {
			throw illegalState("seekTo");
		}
	}

	@Override
	public synchronized boolean isPlaying() {
		return mMediaPlayer.isPlaying();
	}

	private static final int GET_POSITION_BITMASK = STARTED | PAUSED | STOPPED
			| PLAYBACK_COMPLETED;

	@Override
	public synchronized int getCurrentPosition() {
		int state = getInternalState();
		if ((state & GET_POSITION_BITMASK) != 0) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	private static final int GET_DURATION_BITMASK = PREPARED | STARTED | PAUSED
			| PLAYBACK_COMPLETED;

	@Override
	public synchronized int getDuration() {
		int state = getInternalState();
		if ((state & GET_DURATION_BITMASK) != 0) {
			return mMediaPlayer.getDuration();
		}
		return 0;
	}

	@Override
	public synchronized void setAudioStreamType(int streamtype) {
		mMediaPlayer.setAudioStreamType(streamtype);
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
					synchronized (StatelyPlayer.this) {
						try {
							ParcelFileDescriptor fd = context
									.getContentResolver().openFileDescriptor(
											uri, "r");
							mMediaPlayer.setDataSource(fd.getFileDescriptor());
							int state = getInternalState();
							setState(INITIALIZED);
							if ((state & PREPARING_CONTENT) != 0) {
								prepareAsync();
							}
						} catch (Exception e) {
							try {
								mMediaPlayer.setDataSource(uri.toString());
								int state = getInternalState();
								setState(INITIALIZED);
								if ((state & PREPARING_CONTENT) != 0) {
									prepareAsync();
								}
							} catch (Exception e1) {
								Log.e("Whoops", e1);
							}
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
		if (mListenerCollection.completionListener != null) {
			mListenerCollection.completionListener.onCompletion(mp);
		}
		setState(PLAYBACK_COMPLETED);
	}

	@Override
	public synchronized boolean onError(MediaPlayer mp, int what, int extra) {
		if (mListenerCollection.errorListener != null) {
			mInErrorCallback = true;
			if (mListenerCollection.errorListener.onError(mp, what, extra)) {
				if (mInErrorCallback) {
					setState(ERROR);
				}
				return true;
			}
		}
		setState(ERROR);
		return false;
	}

	@Override
	public synchronized boolean onInfo(MediaPlayer mp, int what, int extra) {
		boolean handled = false;
		if (mListenerCollection.infoListener != null) {
			handled = mListenerCollection.infoListener.onInfo(mp, what, extra);
		}

		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			mBuffering = true;
			setState(PREPARING);
			return true;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			mBuffering = false;
			setState(STARTED);
			return true;
		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			mNotSeekable = true;
			return true;
		default:
			return handled;
		}
	}

	@Override
	public synchronized void onPrepared(MediaPlayer mp) {
		if (mListenerCollection.preparedListener != null) {
			mListenerCollection.preparedListener.onPrepared(mp);
		}
		setState(PREPARED);
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

	@Override
	public boolean isWaitingToPlay() {
		return super.isWaitingToPlay() || mBuffering;
	}

	private synchronized MediaPlayer getBarePlayer() {
		return mMediaPlayer;
	}

	@Override
	public String toString() {
		return mMediaPlayer.toString() + " (" + getStateName() + ")";
	}

	private IllegalStateException illegalState(String methodName) {
		IllegalStateException e = new IllegalStateException("Cannot call "
				+ methodName + " in the " + getStateName(getState())
				+ " state.");
		e.printStackTrace();
		return e;
	}
}
