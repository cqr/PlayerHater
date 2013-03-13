package org.prx.android.playerhater.player;

import java.io.IOException;


import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.util.Log;

public class MediaPlayerWrapper implements OnBufferingUpdateListener,
		OnCompletionListener, OnErrorListener, OnInfoListener,
		OnPreparedListener, OnSeekCompleteListener, MediaPlayerWithState {

	private MediaPlayer mMediaPlayer;

	private static final String TAG = "MediaPlayerWrapper";

	private OnErrorListener mErrorListener;
	private OnPreparedListener mPreparedListener;
	private OnCompletionListener mCompletionListener;
	private OnBufferingUpdateListener mBufferingUpdateListener;
	private OnInfoListener mInfoListener;
	private OnSeekCompleteListener mSeekCompleteListener;

	private int mState;
	private int mPrevState;

	public MediaPlayerWrapper() {
		swapPlayer(new MediaPlayer(), IDLE);
	}

	@Override
	public int getState() {
		return this.mState;
	}
	
	@Override
	public String getStateName() {
		switch(getState()) {
		case END:
			return "end";
		case ERROR:
			return "error";
		case IDLE:
			return "idle";
		case INITIALIZED:
			return "initialized";
		case PAUSED:
			return "paused";
		case PLAYBACK_COMPLETED:
			return "playback completed";
		case PREPARED:
			return "prepared";
		case PREPARING:
			return "preparing";
		case STARTED:
			return "started";
		case STOPPED:
			return "stopped";
		}
		throw new IllegalStateException("Impossible state index: " + getState());
	}

	@Override
	public void reset() {
		this.mState = IDLE;
		this.mMediaPlayer.reset();
	}

	@Override
	public void release() {
		this.mMediaPlayer.release();
		this.mState = END;
	}

	@Override
	public void prepare() throws IOException, IllegalStateException {
		if (this.mState == INITIALIZED || this.mState == STOPPED) {
			this.mMediaPlayer.prepare();
			this.mState = PREPARED;
		} else {
			Log.d(TAG, "state is " + this.mState); 
			throw (new IllegalStateException());
		}
	}

	@Override
	public void prepareAsync() throws IllegalStateException {
		if (this.mState == INITIALIZED || this.mState == STOPPED) {
			this.mMediaPlayer.prepareAsync();
			this.mState = PREPARING;
		} else {
			throw (new IllegalStateException());
		}
	}

	@Override
	public void start() throws IllegalStateException {
		if (this.mState == PREPARED || this.mState == STARTED
				|| this.mState == PAUSED || this.mState == PLAYBACK_COMPLETED) {
			this.mMediaPlayer.start();
			this.mState = STARTED;
		} else {
			throw (new IllegalStateException());
		}
	}

	@Override
	public void pause() throws IllegalStateException {
		if (this.mState == STARTED || this.mState == PAUSED) {
			this.mMediaPlayer.pause();
			this.mState = PAUSED;
		} else {
			throw (new IllegalStateException());
		}
	}

	@Override
	public void stop() throws IllegalStateException {
		if (this.mState == PREPARED || this.mState == STARTED
				|| this.mState == STOPPED || this.mState == PAUSED
				|| this.mState == PLAYBACK_COMPLETED) {
			this.mMediaPlayer.stop();
			this.mState = STOPPED;
		} else {
			throw (new IllegalStateException());
		}
	}

	@Override
	public void seekTo(int msec) {
		if (this.mState == PREPARED || this.mState == STARTED
				|| this.mState == PAUSED || this.mState == PLAYBACK_COMPLETED) {
			this.mPrevState = this.mState;
			this.mState = PREPARING;
			this.mMediaPlayer.seekTo(msec);
		} else {
			throw (new IllegalStateException());
		}
	}

	@Override
	public boolean isPlaying() {
		return this.mMediaPlayer.isPlaying();
	}

	@Override
	public int getCurrentPosition() {
		if (this.mState == STARTED || this.mState == PAUSED
				|| this.mState == STOPPED || this.mState == PLAYBACK_COMPLETED) {
			return this.mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	@Override
	public int getDuration() {
		if (this.mState == PREPARED || this.mState == STARTED
				|| this.mState == PAUSED || this.mState == PLAYBACK_COMPLETED) {
			return this.mMediaPlayer.getDuration();
		}
		return 0;
	}

	@Override
	public void setAudioStreamType(int streamtype) {
		this.mMediaPlayer.setAudioStreamType(streamtype);
	}
	
	@Override
	public void setDataSource(Context context, Uri uri)
			throws IllegalStateException, IOException,
			IllegalArgumentException, SecurityException {
		this.mMediaPlayer.setDataSource(context, uri);
		this.mState = INITIALIZED;
	}

	@Override
	public void setOnErrorListener(OnErrorListener errorListener) {
		this.mErrorListener = errorListener;
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener preparedListener) {
		this.mPreparedListener = preparedListener;
	}

	@Override
	public void setOnBufferingUpdateListener(
			OnBufferingUpdateListener bufferingUpdateListener) {
		this.mBufferingUpdateListener = bufferingUpdateListener;
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener completionListener) {
		this.mCompletionListener = completionListener;
	}

	@Override
	public void setOnInfoListener(OnInfoListener infoListener) {
		this.mInfoListener = infoListener;
	}

	@Override
	public void setOnSeekCompleteListener(
			OnSeekCompleteListener seekCompleteListener) {
		this.mSeekCompleteListener = seekCompleteListener;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		//Log.d(TAG, "Buffering Update"); 
		if (this.mBufferingUpdateListener != null) {
			this.mBufferingUpdateListener.onBufferingUpdate(mp, percent);
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "Completion"); 
		this.mState = PLAYBACK_COMPLETED;
		if (this.mCompletionListener != null) {
			this.mCompletionListener.onCompletion(mp);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d(TAG, "Error"); 
		this.mState = ERROR;
		Boolean response = false;
		if (this.mErrorListener != null) {
			response = this.mErrorListener.onError(mp, what, extra);
		}
		return response;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.d(TAG, "info"); 
		if (this.mInfoListener != null) {
			return this.mInfoListener.onInfo(mp, what, extra);
		}
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		this.mState = PREPARED;
		if (this.mPreparedListener != null) {
			this.mPreparedListener.onPrepared(mp);
		}
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		this.mState = this.mPrevState;
		if (this.mSeekCompleteListener != null) {
			this.mSeekCompleteListener.onSeekComplete(mp);
		}
	}

	@Override
	public void setVolume(float leftVolume, float rightVolume) {
		mMediaPlayer.setVolume(leftVolume, rightVolume);
	}
	
	@Override
	public boolean equals(MediaPlayer mp) {
		return mp == mMediaPlayer;
	}
	
	@Override
	public MediaPlayer getBarePlayer() {
		return mMediaPlayer;
	}

	@Override
	public MediaPlayer swapPlayer(MediaPlayer barePlayer, int state) {
		MediaPlayer tmp = mMediaPlayer;
		mMediaPlayer = barePlayer;
		mState = state;
		setListeners();
		return tmp;
	}
	
	@Override
	public void swap(MediaPlayerWithState player) {
		int state = getState();
		MediaPlayer mediaPlayer = swapPlayer(player.getBarePlayer(), player.getState());
		player.swapPlayer(mediaPlayer, state);
	}
	
	private void setListeners() {
		mMediaPlayer.setOnBufferingUpdateListener(this);
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.setOnInfoListener(this);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnSeekCompleteListener(this);
	}
}
