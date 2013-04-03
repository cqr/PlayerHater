package org.prx.android.playerhater.player;

import org.prx.android.playerhater.player.MediaPlayerWrapper.ListenerCollection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.util.Log;

public class WakeLocked extends MediaPlayerDecorator {
	
	public static final WakeLocked wakeLocked(MediaPlayerWithState player, Context context) {
		return new WakeLocked(player, context);
	}

	private static final String PERMISSION = "android.permission.WAKE_LOCK";
	private static final int GRANTED = PackageManager.PERMISSION_GRANTED;
	private static final int WAKE_LOCK = PowerManager.PARTIAL_WAKE_LOCK;

	private final Context mContext;
	private final boolean mEnabled;

	public WakeLocked(MediaPlayerWithState player, Context context) {
		super(player);
		mContext = context;

		PackageManager pm = mContext.getPackageManager();
		String packageName = mContext.getPackageName();

		if (pm.checkPermission(PERMISSION, packageName) == GRANTED) {
			mEnabled = true;
		} else {
			Log.w(packageName + "/PlayerHater",
					"You need to request wake lock permission to enable wake locking during playback.");
			mEnabled = false;
		}
	}

	@Override
	public MediaPlayer swapPlayer(MediaPlayer mediaPlayer, int state, ListenerCollection collection) {
		MediaPlayer mp = super.swapPlayer(mediaPlayer, state, collection);
		if (mEnabled) {
			mediaPlayer.setWakeMode(mContext, WAKE_LOCK);
		}
		return mp;
	}
}
