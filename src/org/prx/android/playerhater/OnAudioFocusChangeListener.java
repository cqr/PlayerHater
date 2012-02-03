package org.prx.android.playerhater;

import android.media.AudioManager;

public class OnAudioFocusChangeListener implements
		AudioManager.OnAudioFocusChangeListener {

	// 5 seconds
	public static final int REWIND_ON_RESUME_DURATION = 5000;

	// 5 minutes
	public static final int SKIP_RESUME_AFTER_DURATION = 300000;

	private PlayerHaterService mService;
	private long pausedAt;
	private boolean isBeingDucked;

	public OnAudioFocusChangeListener(PlayerHaterService service) {
		mService = service;
		isBeingDucked = false;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			// Good, glad to hear it.
			if (isBeingDucked && !mService.isPlaying()) {
				isBeingDucked = false;
				if (pausedAt + (SKIP_RESUME_AFTER_DURATION) > System
						.currentTimeMillis()) {
					try {
						mService.play();
					} catch (Exception e) {
						// Probably illegal state, don't care.
					}
				}
			}
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			// Oh, no! Ok, let's handle that.
			if (mService.isPlaying()) {
				mService.stop();
			}
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			// Let's pause, expecting it to come back.
			if (mService.isPlaying()) {
				pausedAt = System.currentTimeMillis();
				isBeingDucked = true;
				mService.pause();
				mService.seekTo(Math.max(0, mService.getCurrentPosition()
						- REWIND_ON_RESUME_DURATION));
			}
			break;
		default:
			// Dunno.
		}
	}

}