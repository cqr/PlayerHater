package com.chrisrhoden.glisten.wrappers;

import com.chrisrhoden.glisten.ipc.IPlayerHaterClient;
import com.chrisrhoden.glisten.service.PlayerHaterService;

import android.app.Notification;
import android.support.v4.media.session.MediaSessionCompat;

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

	public MediaSessionCompat.Token getMediaSessionToken() {
		return mService.getMediaSession();
	}

	public void duck() {
		mService.duck();
	}

	public void unduck() {
		mService.unduck();
	}
}
