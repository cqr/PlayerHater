package org.prx.android.playerhater;

import android.content.Context;
import android.content.Intent;

public class BroadcastReceiver extends android.content.BroadcastReceiver {

	private PlayerHaterService mService;

	public BroadcastReceiver(PlayerHaterService service) {
		super();
		mService = service;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (mService.isPlaying() && intent.getAction() == Intent.ACTION_HEADSET_PLUG
				&& intent.getIntExtra("state", 0) == 0) {
			mService.pause();
		}
	}

}
