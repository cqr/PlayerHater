package org.prx.android.playerhater.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.util.Log;

public class Synchronous extends MediaPlayerDecorator implements
		OnPreparedListener, OnSeekCompleteListener {

	public static final Synchronous synchronous(MediaPlayerWithState mediaPlayer) {
		return new Synchronous(mediaPlayer);
	}

	private static final String TAG = "PlayerHater/AutoStart";
	private boolean mShouldPlayWhenPrepared;
	private int mShouldSkipWhenPrepared;
	private OnPreparedListener mOnPreparedListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;

	public Synchronous(MediaPlayerWithState player) {
		super(player);
		super.setOnPreparedListener(this);
		super.setOnSeekCompleteListener(this);
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener preparedListener) {
		mOnPreparedListener = preparedListener;
	}

	@Override
	public void setOnSeekCompleteListener(
			OnSeekCompleteListener seekCompleteListener) {
		mOnSeekCompleteListener = seekCompleteListener;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		startIfNecessary();
		if (mOnPreparedListener != null) {
			mOnPreparedListener.onPrepared(mp);
		}
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		if (mOnSeekCompleteListener != null) {
			mOnSeekCompleteListener.onSeekComplete(mp);
		}
		startIfNecessary();
	}

	@Override
	public boolean prepare(Context context, Uri uri) {
		mShouldPlayWhenPrepared = false;
		mShouldSkipWhenPrepared = 0;
		switch (mPlayer.getState()) {
		case IDLE:
			try {
				mPlayer.setDataSource(context, uri);
			} catch (Exception e) {
				return false;
			}
		case INITIALIZED:
			mPlayer.prepareAsync();
			break;
		default:
			reset();
			try {
				setDataSource(context, uri);
			} catch (Exception e) {
				return false;
			}
			prepareAsync();
		}
		return true;
	}

	@Override
	public boolean prepareAndPlay(Context context, Uri uri, int position) {
		if (prepare(context, uri)) {
			mShouldPlayWhenPrepared = true;
			mShouldSkipWhenPrepared = position;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void start() {
		if (getState() == PREPARING) {
			mShouldPlayWhenPrepared = true;
		} else if (getState() == INITIALIZED) {
			mShouldPlayWhenPrepared = true;
			try {
				prepare();
			} catch (Exception e) {
				return;
			}
		} else {
			super.start();
		}
	}

	@Override
	public void seekTo(int msec) {
		int state = getState();
		if (state == PREPARING || state == INITIALIZED) {
			mShouldSkipWhenPrepared = msec;
			if (state == INITIALIZED) {
				prepareAsync();
			}
		} else if (state == PREPARED || state == PAUSED
				|| state == PLAYBACK_COMPLETED) {
			super.seekTo(msec);
		} else if (state == STARTED) {
			super.pause();
			mShouldPlayWhenPrepared = true;
			super.seekTo(msec);
		}
	}

	@Override
	public boolean conditionalPause() {
		if (mShouldPlayWhenPrepared == true) {
			mShouldPlayWhenPrepared = false;
			return true;
		} else if (getState() == STARTED) {
			pause();
			return true;
		}
		return false;
	}

	@Override
	public boolean conditionalPlay() {
		int state = getState();
		if (state == PREPARED || state == PAUSED || state == PLAYBACK_COMPLETED) {
			start();
			return true;
		} else if (state == PREPARING) {
			mShouldPlayWhenPrepared = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean conditionalStop() {
		if (mShouldPlayWhenPrepared == true) {
			mShouldPlayWhenPrepared = false;
			return true;
		}
		int state = getState();
		if (state != PREPARED && state != STARTED && state != PAUSED
				&& state != PLAYBACK_COMPLETED) {
			return false;
		}
		stop();
		return true;
	}

	private void startIfNecessary() {
		if (mShouldSkipWhenPrepared != 0) {
			seekTo(mShouldSkipWhenPrepared);
			mShouldSkipWhenPrepared = 0;
		} else if (mShouldPlayWhenPrepared) {
			start();
			mShouldPlayWhenPrepared = false;
		}
	}

}
