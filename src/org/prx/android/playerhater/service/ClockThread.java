package org.prx.android.playerhater.service;

import android.os.Handler;

public class ClockThread extends Thread {

	private final int mTickDuration;
	private final Handler mHandler;

	public static final int TICK = 210101;

	public ClockThread(Handler handler, int tickDuration) {
		mHandler = handler;
		mTickDuration = tickDuration;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(mTickDuration);
				if (Thread.interrupted()) {
					break;
				}
				mHandler.sendEmptyMessage(TICK);
			} catch (InterruptedException ie) {
				break;
			}
		}
	}

}
