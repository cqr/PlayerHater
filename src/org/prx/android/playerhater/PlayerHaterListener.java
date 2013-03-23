package org.prx.android.playerhater;

public interface PlayerHaterListener {
	void onStopped();

	void onPaused(Song song);

	void onLoading(Song song);

	void onPlaying(Song song, int progress);
}
