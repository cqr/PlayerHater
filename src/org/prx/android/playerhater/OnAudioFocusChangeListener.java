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

	private boolean isBeingPaused;

	public OnAudioFocusChangeListener(PlayerHaterService service) {
		mService = service;
		isBeingDucked = false;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			// Good, glad to hear it.
			if (isBeingPaused && !mService.isPlaying()) {
				isBeingPaused = false;
				if (pausedAt + (SKIP_RESUME_AFTER_DURATION) > System
						.currentTimeMillis()) {
					try {
						mService.play();
					} catch (Exception e) {
						// Probably illegal state, don't care.
					}
				}
			}
			
			if (isBeingDucked) {
				mService.unduck();
			}
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			// Oh, no! Ok, let's handle that.
			if (mService.isPlaying()) {
				mService.stop();
			}
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			// Let's pause, expecting it to come back.
			if (mService.isPlaying()) {
				pausedAt = System.currentTimeMillis();
				isBeingPaused = true;
				mService.pause();
				mService.seekTo(Math.max(0, mService.getCurrentPosition()
						- REWIND_ON_RESUME_DURATION));
			}
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			if (mService.isPlaying() && !isBeingDucked) {
				isBeingDucked = true;
				mService.duck();
			}
			break;
		default:
			// Dunno.
		}
	}

}