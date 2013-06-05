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

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;

public class SynchronousPlayer extends StatelyPlayer implements
		OnPreparedListener, OnSeekCompleteListener {

	private boolean mShouldPlayWhenPrepared;
	private int mShouldSkipWhenPrepared;
	private Uri mShouldSetDataSourceUri;
	private Context mShouldSetPrepareContext;

	public SynchronousPlayer() {
		super();
	}

	@Override
	public synchronized void onPrepared(MediaPlayer mp) {
		super.onPrepared(mp);
		startIfNecessary();
	}

	@Override
	public synchronized void onSeekComplete(MediaPlayer mp) {
		super.onSeekComplete(mp);
		startIfNecessary();
	}

	@Override
	public synchronized boolean prepare(Context context, Uri uri) {
		mShouldPlayWhenPrepared = false;
		mShouldSkipWhenPrepared = 0;
		mShouldSetDataSourceUri = null;
		mShouldSetPrepareContext = null;
		switch (getState()) {
		case IDLE:
			try {
				setDataSource(context, uri);
			} catch (Exception e) {
				return false;
			}
		case INITIALIZED:
		case STOPPED:
		case LOADING_CONTENT:
			prepareAsync();
			break;
		case PREPARING:
		case PREPARING_CONTENT:
			mShouldSetDataSourceUri = uri;
			mShouldSetPrepareContext = context;
			break;
		default:
			reset();
			try {
				setDataSource(context, uri);
			} catch (Exception e) {
				return false;
			}
			try {
				prepareAsync();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public synchronized boolean prepareAndPlay(Context context, Uri uri, int position) {
		if (prepare(context, uri)) {
			if (position != 0) {
				mShouldPlayWhenPrepared = true;
				seekTo(position);
			} else {
				start();
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized void start() {
		if (getState() == PREPARING) {
			mShouldPlayWhenPrepared = true;
			onStateChanged();
		} else if (getState() == INITIALIZED || getState() == STOPPED) {
			mShouldPlayWhenPrepared = true;
			try {
				prepareAsync();
			} catch (Exception e) {}
		} else {
			super.start();
		}
	}

	@Override
	public synchronized void seekTo(int msec) {
		int state = getState();
		if (state == PREPARING || state == INITIALIZED || state == STOPPED
				|| state == LOADING_CONTENT || state == PREPARING_CONTENT) {
			mShouldSkipWhenPrepared = msec;
			if (state == INITIALIZED || state == STOPPED) {
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
	public synchronized boolean conditionalPause() {
		if (mShouldPlayWhenPrepared) {
			mShouldPlayWhenPrepared = false;
			return true;
		} else if (getState() == STARTED) {
			pause();
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean conditionalPlay() {
		try {
			start();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public synchronized boolean conditionalStop() {
		if (mShouldPlayWhenPrepared) {
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

	@Override
	public synchronized boolean isWaitingToPlay() {
		return super.isWaitingToPlay()
				|| (mShouldSkipWhenPrepared != 0 | mShouldPlayWhenPrepared);
	}

	private void startIfNecessary() {
		if (mShouldSetDataSourceUri != null) {
			if (mShouldPlayWhenPrepared) {
				prepareAndPlay(mShouldSetPrepareContext,
						mShouldSetDataSourceUri, mShouldSkipWhenPrepared);
			} else {
				prepare(mShouldSetPrepareContext, mShouldSetDataSourceUri);
			}
		} else if (mShouldSkipWhenPrepared != 0) {
			seekTo(mShouldSkipWhenPrepared);
			mShouldSkipWhenPrepared = 0;
		} else if (mShouldPlayWhenPrepared) {
			start();
			mShouldPlayWhenPrepared = false;
		}
	}

}
