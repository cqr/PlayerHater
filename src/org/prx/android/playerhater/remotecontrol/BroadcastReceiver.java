package org.prx.android.playerhater.remotecontrol;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.service.PlayerHaterBinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

public class BroadcastReceiver extends android.content.BroadcastReceiver implements RemoteControlButtonReceiver {
	
	public static final String REMOTE_CONTROL_BUTTON = "org.prx.android.playerhater.REMOTE_CONTROL";

	private static final HeadphoneButtonGestureHelper sGestureHelper = new HeadphoneButtonGestureHelper();
	
	private PlayerHaterBinder mService;

	public BroadcastReceiver() {
		super();
	}

	public BroadcastReceiver(Context context, PlayerHaterBinder playbackService) {
		super();
		if (playbackService != null) {
			mService = playbackService;
		}
		sGestureHelper.setReceiver(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		filter.addAction(Intent.ACTION_MEDIA_BUTTON);
		filter.setPriority(10000);
		context.registerReceiver(this, filter);
	}
	
	@Override
	@SuppressLint("InlinedApi")
	public void onRemoteControlButtonPressed(int keyCode, Context context) {
		sendKeyCode(context, keyCode, keyCode != KeyEvent.KEYCODE_MEDIA_PAUSE);
	}

	@Override
	@SuppressLint("InlinedApi")
	public void onReceive(Context context, Intent intent) {
		int keyCode = -1;
		if (intent.getAction() != null) {
			if (intent.getIntExtra("state", 0) == 0) {
				if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
					keyCode = KeyEvent.KEYCODE_MEDIA_PAUSE;
				} else if (intent.getAction().equals(
						AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
					keyCode = KeyEvent.KEYCODE_MEDIA_PAUSE;
				}
			}
			if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
				KeyEvent event = (KeyEvent) intent
						.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					return;
				}

				keyCode = event.getKeyCode();

				if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) {
					sGestureHelper.onHeadsetButtonPressed(event.getEventTime(), context);
				}
			}
		
			if (keyCode != -1) {
				boolean autoStart = keyCode != KeyEvent.KEYCODE_MEDIA_PAUSE && keyCode != KeyEvent.KEYCODE_MEDIA_STOP;
				sendKeyCode(context, keyCode, autoStart);
			}
		}
	}
	
	private PlayerHaterBinder getService(Context context) {
		if (mService == null && context != null) {
			Intent intent = PlayerHater.buildServiceIntent(context);
			mService = (PlayerHaterBinder) peekService(context, intent);
		}
		return mService;
	}
	
	private void sendKeyCode(Context context, int keyCode, boolean startIfNecessary) {
		if (getService(context) != null) {
			try {
				getService(context).onRemoteControlButtonPressed(keyCode);
			} catch (Exception e) {
				Log.e("BroadcastReceiver", "Ugh", e);
				if (startIfNecessary && context != null) {
					Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
					intent.putExtra(REMOTE_CONTROL_BUTTON, keyCode);
					context.startActivity(intent);
				}
			}
			
		} else if (startIfNecessary && context != null) {
			Intent intent = new Intent(PlayerHater.buildServiceIntent(context));
			intent.putExtra(REMOTE_CONTROL_BUTTON, keyCode);
			context.startService(intent);
		}
	}

}
