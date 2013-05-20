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
package org.prx.android.playerhater.util;

import org.prx.android.playerhater.player.MediaPlayerWithState;

import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;

public class PlayerListenerManager {
	
	
	private OnErrorListener mOnErrorListener;
	private OnBufferingUpdateListener mOnBufferingUpdateListener;
	private OnCompletionListener mOnCompletionListener;
	private OnInfoListener mOnInfoListener;
	private OnPreparedListener mOnPreparedListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;

	private MediaPlayerWithState mMediaPlayer;

	public void setMediaPlayer(MediaPlayerWithState mediaPlayer) {
		mMediaPlayer = mediaPlayer;
		setOnBufferingUpdateListener();
		setOnCompletionListener();
		setOnErrorListener();
		setOnInfoListener();
		setOnPreparedListener();
		setOnSeekCompleteListener();
	}

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mOnBufferingUpdateListener = listener;
		if (mMediaPlayer != null)
			setOnBufferingUpdateListener();
	}

	private void setOnBufferingUpdateListener() {
		mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
	}

	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListener = listener;
		if (mMediaPlayer != null)
			setOnCompletionListener();
	}

	private void setOnCompletionListener() {
		mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
	}

	public void setOnInfoListener(OnInfoListener listener) {
		mOnInfoListener = listener;
		if (mMediaPlayer != null)
			setOnInfoListener();
	}

	private void setOnInfoListener() {
		mMediaPlayer.setOnInfoListener(mOnInfoListener);
	}

	public void setOnPreparedListener(OnPreparedListener listener) {
		mOnPreparedListener = listener;
		if (mMediaPlayer != null)
			setOnPreparedListener();
	}

	private void setOnPreparedListener() {
		mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
	}

	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mOnSeekCompleteListener = listener;
		if (mMediaPlayer != null)
			setOnSeekCompleteListener();
	}

	private void setOnSeekCompleteListener() {
		mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
	}

	public void setOnErrorListener(OnErrorListener listener) {
		mOnErrorListener = listener;
		if (mMediaPlayer != null)
			setOnErrorListener();
	}

	private void setOnErrorListener() {
		mMediaPlayer.setOnErrorListener(mOnErrorListener);
	}
}
