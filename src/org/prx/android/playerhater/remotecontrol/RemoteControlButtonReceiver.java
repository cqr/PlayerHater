package org.prx.android.playerhater.remotecontrol;

import android.content.Context;

public interface RemoteControlButtonReceiver {
	public void onRemoteControlButtonPressed(int keyCode, Context context);
}