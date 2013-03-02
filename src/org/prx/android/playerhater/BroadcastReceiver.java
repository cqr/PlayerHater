package org.prx.android.playerhater;

import java.io.IOException;

import org.prx.android.playerhater.lifecycle.ModernNotificationHandler;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

public class BroadcastReceiver extends android.content.BroadcastReceiver {

	private static final String TAG = "BroadcastReceiver";
	private static PlaybackService mService;

	public BroadcastReceiver() {
		super();
	}

	public BroadcastReceiver(PlaybackService service) {
		super();
		mService = service;
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
			Log.d(TAG, "Key event is " + event);
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				return;
			}
			if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()
					|| KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()
					|| KeyEvent.KEYCODE_HEADSETHOOK == event.getKeyCode()
					|| KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()) {
				if (mService.isPlaying()) {
					mService.pause();
				} else if (mService.isPaused()) {
					try {
						mService.play();
					} catch (java.lang.IllegalStateException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (intent.getAction() != null
				&& intent.getAction().equals(
						ModernNotificationHandler.PLAY_PAUSE_ACTION)) {
			try {
				if (mService.isPlaying()) {
					mService.pause();
				} else {
					mService.play();
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		if (intent.getAction() != null
				&& intent.getAction().equals(
						ModernNotificationHandler.STOP_ACTION)) {
			try {
				mService.stop();
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}

}
