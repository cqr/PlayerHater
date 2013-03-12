package org.prx.android.playerhater.util;

import android.content.Context;

public interface RemoteControlButtonReceiver {
	public void onRemoteControlButtonPressed(int keyCode, Context context);
}