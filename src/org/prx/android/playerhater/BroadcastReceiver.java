package org.prx.android.playerhater;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class BroadcastReceiver extends android.content.BroadcastReceiver {

	private PlaybackService mService;

	public BroadcastReceiver(PlaybackService service) {
		super();
		mService = service;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (mService.isPlaying() && 
				(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG) ||
			     intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
				&& intent.getIntExtra("state", 0) == 0) {
			mService.pause();
		}
	}

}
