package org.prx.android.playerhater.util;

import org.prx.android.playerhater.service.PlayerHaterService;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

public class BroadcastReceiver extends android.content.BroadcastReceiver {

	private static final String TAG = "Broadcast Receiver";
	private static final HeadphoneButtonGestureHelper sGestureHelper = new HeadphoneButtonGestureHelper();
	private static PlayerHaterService mService;

	public BroadcastReceiver() {
		super();
	}

	public BroadcastReceiver(PlayerHaterService playbackService) {
		super();
		mService = playbackService;
		sGestureHelper.setReceiver(mService);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		filter.addAction(Intent.ACTION_MEDIA_BUTTON);
		filter.setPriority(10000);
		mService.getBaseContext().registerReceiver(this, filter);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		BroadcastReceiver.doReceive(context, intent);
	}

	@SuppressLint("InlinedApi")
	public static void doReceive(Context context, Intent intent) {
		if (mService.isPlaying()
				&& intent.getAction() != null
				&& (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG) || intent
						.getAction().equals(
								AudioManager.ACTION_AUDIO_BECOMING_NOISY))
				&& intent.getIntExtra("state", 0) == 0) {
			mService.pause();
		}
		if (intent.getAction() != null
				&& intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
			KeyEvent event = (KeyEvent) intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				return;
			}

			int keyCode = event.getKeyCode();
			
			if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) {
				sGestureHelper.onHeadsetButtonPressed(event.getEventTime());
			}
			
			mService.onRemoteControlButtonPressed(keyCode);
		}
	}

}
