package org.prx.android.playerhater.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.util.Log;

public class Syncronous extends MediaPlayerDecorator implements OnPreparedListener, OnSeekCompleteListener {

	private static final String TAG = "PlayerHater/AutoStart";
	private boolean mShouldPlayWhenPrepared;
	private int mShouldSkipWhenPrepared;
	private OnPreparedListener mOnPreparedListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;

	public Syncronous(MediaPlayerWithState player) {
		super(player);
		super.setOnPreparedListener(this);
		super.setOnSeekCompleteListener(this);
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener preparedListener) {
		mOnPreparedListener = preparedListener;
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener seekCompleteListener) {
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
		switch(mPlayer.getState()) {
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
	
	private void startIfNecessary() {
		Log.d(TAG, "PREPARED, CHECKING TO SEE IF PLAYBACK SHOULD START");
		if (mShouldSkipWhenPrepared != 0) {
			seekTo(mShouldSkipWhenPrepared);
			mShouldSkipWhenPrepared = 0;
		} else if (mShouldPlayWhenPrepared) {
			start();
			mShouldPlayWhenPrepared = false;
		}
	}

}
