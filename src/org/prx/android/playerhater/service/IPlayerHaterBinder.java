package org.prx.android.playerhater.service;

import org.prx.android.playerhater.util.IPlayerHater;

public interface IPlayerHaterBinder extends IPlayerHater {

	void registerShutdownRequestListener(
			OnShutdownRequestListener sshutdownlistener);

	void onRemoteControlButtonPressed(int keyCode);
}