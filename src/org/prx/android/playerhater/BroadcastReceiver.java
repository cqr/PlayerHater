package org.prx.android.playerhater;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

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
		if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) { 
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()) {
                if (mService.isPlaying()) { 
                	mService.pause(); 
                } 
            }
		}
	}

}
