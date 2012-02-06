package org.prx.android.playerhater;

public interface PlayerHaterListener {
	void onPaused();
	void onLoading();
	void onStopped();
	void onPlaying(int progress);
}
