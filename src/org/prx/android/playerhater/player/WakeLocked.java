package org.prx.android.playerhater.player;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.util.Log;

public class WakeLocked extends MediaPlayerDecorator {

	private static final String PERMISSION = "android.permission.WAKE_LOCK";
	private static final int GRANTED = PackageManager.PERMISSION_GRANTED;
	private static final int WAKE_LOCK = PowerManager.PARTIAL_WAKE_LOCK;

	private final Context mContext;
	private final boolean mEnabled;

	public WakeLocked(Context context, MediaPlayerWithState player) {
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
	public void swapPlayer(MediaPlayer mediaPlayer, int state) {
		super.swapPlayer(mediaPlayer, state);
		if (mEnabled) {
			mediaPlayer.setWakeMode(mContext, WAKE_LOCK);
		}
	}
}
