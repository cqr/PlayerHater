package org.prx.android.playerhater;

import android.media.AudioManager;

public class OnAudioFocusChangeListener implements
		android.media.AudioManager.OnAudioFocusChangeListener {

	private PlayerHaterService mService;

	public OnAudioFocusChangeListener(PlayerHaterService service) {
		mService = service;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			// Good, glad to hear it.
			if (!mService.isPlaying()) {
				try {
					mService.play();
				} catch (Exception e) {
					// Probably illegal state, don't care.
				}
			}
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			// Oh, no! Ok, let's handle that.
			if (mService.isPlaying()) {
				mService.pause();
			}
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			// Let's pause, expecting it to come back.
			if (mService.isPlaying()) {
				mService.pause();
			}
			break;
		default:
			// Dunno.
		}
	}

}
