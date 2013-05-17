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
package org.prx.android.playerhater.util;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.service.IPlayerHaterBinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.view.KeyEvent;

public class BroadcastReceiver extends android.content.BroadcastReceiver implements RemoteControlButtonReceiver {
	
	public static final String REMOTE_CONTROL_BUTTON = "org.prx.android.playerhater.REMOTE_CONTROL";

	private static final HeadphoneButtonGestureHelper sGestureHelper = new HeadphoneButtonGestureHelper();
	
	private IPlayerHaterBinder mService;

	public BroadcastReceiver() {
		super();
	}

	public BroadcastReceiver(Context context, IPlayerHaterBinder playbackService) {
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
	
	private IPlayerHaterBinder getService(Context context) {
		if (mService == null && context != null) {
			Intent intent = PlayerHater.buildServiceIntent(context);
			mService = IPlayerHaterBinder.Stub.asInterface(peekService(context, intent));
		}
		return mService;
	}
	
	private void sendKeyCode(Context context, int keyCode, boolean startIfNecessary) {
		if (getService(context) != null) {
			try {
				getService(context).onRemoteControlButtonPressed(keyCode);
			} catch (Exception e) {
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
