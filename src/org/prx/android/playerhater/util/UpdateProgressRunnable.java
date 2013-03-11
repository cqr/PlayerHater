package org.prx.android.playerhater.util;

import org.prx.android.playerhater.player.IPlayer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UpdateProgressRunnable implements Runnable {
	private final static String TAG = "PlayerHater/ProgressUpdate";
	private final Handler mHandler;
	private IPlayer mMediaPlayer;
	private final int mMessage;

	public UpdateProgressRunnable(Handler handler, int message) {
		mHandler = handler;
		mMessage = message;
	}
	
	public synchronized void setMediaPlayer(IPlayer mediaPlayer) {
		mMediaPlayer = mediaPlayer;
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
