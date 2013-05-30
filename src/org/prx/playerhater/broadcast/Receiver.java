package org.prx.playerhater.broadcast;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.ipc.IPlayerHaterServer;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

public class Receiver extends BroadcastReceiver implements
		RemoteControlButtonReceiver {

	private static final HeadphoneButtonGestureHelper sGestureHelper = new HeadphoneButtonGestureHelper();
	public static final String REMOTE_CONTROL_BUTTON = "org.prx.playerhater.REMOTE_CONTROL";
	
	public Receiver() {
		super();
		sGestureHelper.setReceiver(this);
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
					sGestureHelper.onHeadsetButtonPressed(event.getEventTime(),
							context);
				}
			}

			if (keyCode != -1) {
				boolean autoStart = keyCode != KeyEvent.KEYCODE_MEDIA_PAUSE
						&& keyCode != KeyEvent.KEYCODE_MEDIA_STOP;
				sendKeyCode(context, keyCode, autoStart);
			}
		}
	}

	private IPlayerHaterServer getService(Context context) {
		Intent intent = PlayerHater.buildServiceIntent(context);
		return IPlayerHaterServer.Stub
				.asInterface(peekService(context, intent));
	}

	private void sendKeyCode(Context context, int keyCode,
			boolean startIfNecessary) {
		if (getService(context) != null) {
			try {
				getService(context).onRemoteControlButtonPressed(keyCode);
			} catch (Exception e) {
				if (startIfNecessary && context != null) {
					Intent intent = context
							.getPackageManager()
							.getLaunchIntentForPackage(context.getPackageName());
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
