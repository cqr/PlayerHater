package org.prx.android.playerhater;

public interface PlayerHaterListener {
	void onPaused();
	void onLoading();
	void onPlaying(int progress);
}
