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
package org.prx.playerhater.plugins;

import org.prx.playerhater.BroadcastReceiver;
import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.broadcast.OnAudioFocusChangedListener;
import org.prx.playerhater.util.Log;
import org.prx.playerhater.wrappers.ServicePlayerHater;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.FROYO)
public class AudioFocusPlugin extends AbstractPlugin {
	private AudioManager mAudioService;
	private OnAudioFocusChangedListener mAudioFocusChangeListener;
	private ComponentName mEventReceiver;

	public AudioFocusPlugin() {
	}

	@Override
	public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
		super.onPlayerHaterLoaded(context, playerHater);
		if (!(playerHater instanceof ServicePlayerHater)) {
			throw new IllegalArgumentException("AudioFocusPlugin must be run on the server side");
		}
		mAudioFocusChangeListener = new OnAudioFocusChangedListener((ServicePlayerHater) playerHater);
	}
	
	@Override
	public void onAudioStarted() {
		Log.d("Getting Audio Focus");
		getAudioManager().requestAudioFocus(mAudioFocusChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		getAudioManager().registerMediaButtonEventReceiver(getEventReceiver());
	}

	@Override
	public void onAudioStopped() {
		getAudioManager().abandonAudioFocus(mAudioFocusChangeListener);
		getAudioManager()
				.unregisterMediaButtonEventReceiver(getEventReceiver());
	}

	protected AudioManager getAudioManager() {
		if (mAudioService == null) {
			mAudioService = (AudioManager) getContext().getSystemService(
					Context.AUDIO_SERVICE);
		}

		return mAudioService;
	}

	protected ComponentName getEventReceiver() {
		if (mEventReceiver == null) {
			mEventReceiver = new ComponentName(getContext(), BroadcastReceiver.class);
		}
		return mEventReceiver;
	}
}
