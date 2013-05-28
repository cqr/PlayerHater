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

import org.prx.playerhater.service.IPlayerHaterBinder;
import org.prx.playerhater.util.BroadcastReceiver;
import org.prx.playerhater.util.OnAudioFocusChangeListener;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.FROYO)
public class AudioFocusPlugin extends AbstractPlugin {
	private AudioManager mAudioService;
	private OnAudioFocusChangeListener mAudioFocusChangeListener;
	private ComponentName mEventReceiver;

	public AudioFocusPlugin() {

	}

	@Override
	public void onServiceBound(IPlayerHaterBinder binder) {
		super.onServiceBound(binder);
		mAudioFocusChangeListener = new OnAudioFocusChangeListener(binder);
	}

	@Override
	public void onAudioStarted() {
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
			mEventReceiver = new ComponentName(getContext(),
					BroadcastReceiver.class);
		}
		return mEventReceiver;
	}
}
