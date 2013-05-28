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
package org.prx.android.playerhater.player;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

public class MediaPlayerWrapper implements OnBufferingUpdateListener,
		OnCompletionListener, OnErrorListener, OnInfoListener,
		OnPreparedListener, OnSeekCompleteListener, MediaPlayerWithState {

	private static Map<MediaPlayer, MediaPlayerWrapper> sPlayers = new HashMap<MediaPlayer, MediaPlayerWrapper>();

	private MediaPlayer mMediaPlayer;

	private static void registerSelf(MediaPlayer barePlayer,
			MediaPlayerWrapper mediaPlayerWrapper) {
		sPlayers.put(barePlayer, mediaPlayerWrapper);
	}

	private static MediaPlayerWrapper getWrapper(MediaPlayer barePlayer) {
		return sPlayers.get(barePlayer);
	}

	private static final OnErrorListener sErrorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
			return getWrapper(arg0).onError(arg0, arg1, arg2);
		}

	};

	private static final OnPreparedListener sPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			getWrapper(mp).onPrepared(mp);
		}

	};

	private static final OnCompletionListener sCompletionListener = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			getWrapper(mp).onCompletion(mp);
		}

	};

	private static final OnBufferingUpdateListener sBufferingUpdateListener = new OnBufferingUpdateListener() {

		@Override
		public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
			getWrapper(arg0).onBufferingUpdate(arg0, arg1);
		}

	};

	private static final OnInfoListener sInfoListener = new OnInfoListener() {

		@Override
		public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
			return getWrapper(arg0).onInfo(arg0, arg1, arg2);
		}

	};

	private static final OnSeekCompleteListener sSeekCompleteListener = new OnSeekCompleteListener() {
		@Override
		public void onSeekComplete(MediaPlayer arg0) {
			getWrapper(arg0).onSeekComplete(arg0);
		}

	};

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

	public MediaPlayerWrapper() {
		swapPlayer(new MediaPlayer(), IDLE, new ListenerCollection());
		getBarePlayer().setOnBufferingUpdateListener(sBufferingUpdateListener);
		getBarePlayer().setOnCompletionListener(sCompletionListener);
		getBarePlayer().setOnErrorListener(sErrorListener);
		getBarePlayer().setOnInfoListener(sInfoListener);
		getBarePlayer().setOnPreparedListener(sPreparedListener);
		getBarePlayer().setOnSeekCompleteListener(sSeekCompleteListener);
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
		case LOADING_CONTENT:
			return "initialized";
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
		IllegalStateException e = new IllegalStateException("Cannot call "+ methodName +" in the " + getStateName(getState()) + " state.");
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
						if (MediaPlayerWrapper.this.getState() == PREPARING_CONTENT) {
							setState(INITIALIZED);
							prepareAsync();
						} else {
							setState(INITIALIZED);
						}
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						try {
							mMediaPlayer.setDataSource(uri.toString());
							if (MediaPlayerWrapper.this.getState() == PREPARING_CONTENT) {
								setState(INITIALIZED);
								prepareAsync();
							} else {
								setState(INITIALIZED);
							}
						} catch (IllegalArgumentException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (SecurityException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IllegalStateException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
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

	@Override
	public synchronized MediaPlayer getBarePlayer() {
		return mMediaPlayer;
	}

	@Override
	public synchronized MediaPlayer swapPlayer(MediaPlayer barePlayer,
			int state, ListenerCollection collection) {
		MediaPlayer tmp = mMediaPlayer;
		mMediaPlayer = barePlayer;
		setState(state);
		mListenerCollection = collection;
		registerSelf(barePlayer, this);
		return tmp;
	}

	@Override
	public synchronized void swap(MediaPlayerWithState player) {
		int state = getState();
		ListenerCollection listeners = getListeners();
		MediaPlayer mediaPlayer = swapPlayer(player.getBarePlayer(),
				player.getState(), player.getListeners());
		player.swapPlayer(mediaPlayer, state, listeners);
	}

	@Override
	public synchronized ListenerCollection getListeners() {
		return mListenerCollection;
	}

	@Override
	public String toString() {
		return mMediaPlayer.toString() + " (" + getStateName() + ")";
	}
}
