package org.prx.android.playerhater.service;

import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.util.IPlayerHater;

public interface IPlayerHaterBinder extends IPlayerHater {
	
	void addPluginInstance(PlayerHaterPlugin plugin);

	void registerShutdownRequestListener(
			OnShutdownRequestListener sshutdownlistener);

	void onRemoteControlButtonPressed(int keyCode);
}