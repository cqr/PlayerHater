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
package org.prx.playerhater.broadcast;

import org.prx.playerhater.wrappers.ServicePlayerHater;

import android.annotation.TargetApi;
import android.media.AudioManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.FROYO)
public class OnAudioFocusChangedListener implements
		AudioManager.OnAudioFocusChangeListener {

	// 5 seconds
	private static final int REWIND_ON_RESUME_DURATION = 5000;

	// 5 minutes
	private static final int SKIP_RESUME_AFTER_DURATION = 300000;

	private final ServicePlayerHater mPlayerHater;

	private long pausedAt;
	private boolean isBeingDucked;
	private boolean isBeingPaused;

	public OnAudioFocusChangedListener(ServicePlayerHater playerHater) {
		isBeingDucked = false;
		mPlayerHater = playerHater;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			// Good, glad to hear it.
			if (isBeingPaused && !mPlayerHater.isPlaying()) {
				isBeingPaused = false;
				if (pausedAt + (SKIP_RESUME_AFTER_DURATION) > System
						.currentTimeMillis()) {
					mPlayerHater.play();
				}
			}

			if (isBeingDucked) {
				isBeingDucked = false;
				mPlayerHater.unduck();
			}
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			// Oh, no! Ok, let's handle that.
			if (mPlayerHater.isPlaying()) {
				mPlayerHater.pause();
			}
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			// Let's pause, expecting it to come back.
			if (mPlayerHater.isPlaying()) {
				pausedAt = System.currentTimeMillis();
				isBeingPaused = true;
				mPlayerHater.pause();
				mPlayerHater.seekTo(Math.max(0,
						mPlayerHater.getCurrentPosition()
								- REWIND_ON_RESUME_DURATION));
			}
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			if (mPlayerHater.isPlaying() && !isBeingDucked) {
				isBeingDucked = true;
				mPlayerHater.duck();
			}
			break;
		default:
			// Dunno.
		}
	}

}
