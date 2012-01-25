package org.prx.android.playerhater;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UpdateProgressRunnable implements Runnable {
	private final static String TAG = "PlayerHater/ProgressUpdate";
	private final Handler mHandler;
	private final MediaPlayerWrapper mMediaPlayer;
	private final int mMessage;

	public UpdateProgressRunnable(MediaPlayerWrapper mediaPlayer,
			Handler handler, int message) {
		mHandler = handler;
		mMediaPlayer = mediaPlayer;
		mMessage = message;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				if (Thread.interrupted()) {
					break;
				}
				try {
					Message m = Message.obtain(mHandler, mMessage,
							mMediaPlayer.getCurrentPosition(), 0);
					// Log.d(TAG, "got message");
					m.sendToTarget();
				} catch (IllegalStateException ise) {
					Log.d(TAG,
							"illegal state when requesting current position "
									+ mMediaPlayer.getState());
				}
			} catch (InterruptedException ie) {
				Log.d(TAG, "CAUGHT Interrupted EXCEPTION");
				break;
			}
		}
		Log.d(TAG, "EXITING UPDATE PROGRESS");
	}

}
