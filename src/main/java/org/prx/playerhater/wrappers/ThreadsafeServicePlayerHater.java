package org.prx.playerhater.wrappers;

import org.prx.playerhater.ipc.IPlayerHaterClient;
import org.prx.playerhater.service.PlayerHaterService;

import android.app.Notification;

public class ThreadsafeServicePlayerHater extends ThreadsafePlayerHater {
	private final PlayerHaterService mService;

	public ThreadsafeServicePlayerHater(PlayerHaterService service) {
		super(new ServicePlayerHater(service));
		mService = service;
	}

	/*
	 * Service Specific stuff.
	 */

	public void setClient(IPlayerHaterClient client) {
		mService.setClient(client);
	}

	public void onRemoteControlButtonPressed(int keyCode) {
		mService.onRemoteControlButtonPressed(keyCode);
	}

	public void startForeground(int notificationNu, Notification notification) {
		mService.startForeground(notificationNu, notification);
	}

	public void stopForeground(boolean removeNotification) {
		mService.stopForeground(removeNotification);
		mService.quit();
	}

    public boolean pause(final boolean fromApp) {
        return new PlayerHaterTask<Boolean>(mHandler) {
            @Override
            protected Boolean run() {
                return mService.pause(fromApp);
            }
        }.get();
    }

	public void duck() {
		mService.duck();
	}

	public void unduck() {
		mService.unduck();
	}
}
