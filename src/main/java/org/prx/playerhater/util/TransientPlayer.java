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
package org.prx.playerhater.util;

import org.prx.playerhater.player.Player;
import org.prx.playerhater.player.MediaPlayerWrapper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;

public class TransientPlayer {

	public static TransientPlayer play(Context c, Uri url, boolean isDuckable) {
		return new TransientPlayer(c, url, isDuckable).play();
	}

	private final Context c;
	private final MediaPlayerWrapper wrapper;
	private final boolean isDuckable;
	private final Uri uri;
	final AudioManager audioManager;

	final AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			// I don't think we need to do anything here.
			// generally, we might want to stop the audio in
			// the event that we have lost focus in a non-
			// transient way, I think.
		}
	};

	public TransientPlayer(Context c, Uri url, boolean isDuckable) {
		this.c = c;
		this.wrapper = new MediaPlayerWrapper();
		this.uri = url;
		this.isDuckable = isDuckable;
		this.audioManager = (AudioManager) c
				.getSystemService(Context.AUDIO_SERVICE);
	}

	private TransientPlayer play() {
		TransientPlayerTask task = new TransientPlayerTask();
		Thread thread = new Thread(task);
		thread.run();
		return this;
	}

	@SuppressLint("NewApi")
	public void stop() {
		try {
			wrapper.stop();
			if (android.os.Build.VERSION.SDK_INT >= 8) {
				audioManager.abandonAudioFocus(audioFocusListener);
			}
			wrapper.release();
		} catch (Exception e) {

		}
	}

	public boolean isPlaying() {
		return (wrapper.getState() == Player.STARTED
				|| wrapper.getState() == Player.INITIALIZED
				|| wrapper.getState() == Player.PREPARED || wrapper.getState() == Player.PREPARING);
	}

	@SuppressLint("InlinedApi")
	private int getDurationHint() {
		if (isDuckable) {
			return AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
		} else {
			return AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
		}
	}

	private class TransientPlayerTask implements Runnable {

		@SuppressLint("NewApi")
		@Override
		public void run() {

			wrapper.setAudioStreamType(AudioManager.STREAM_MUSIC);

			try {
				wrapper.setDataSource(c, uri);
				wrapper.prepareAsync();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			wrapper.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					if (android.os.Build.VERSION.SDK_INT >= 8) {
						audioManager.abandonAudioFocus(audioFocusListener);
					}
					wrapper.release();
				}

			});

			if (android.os.Build.VERSION.SDK_INT >= 8) {
				int status = audioManager.requestAudioFocus(audioFocusListener,
						AudioManager.STREAM_MUSIC, getDurationHint());

				if (status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
					wrapper.start();
			} else {
				wrapper.start();
			}
		}

	}
}
