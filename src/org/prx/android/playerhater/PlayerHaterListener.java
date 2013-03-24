package org.prx.android.playerhater;

/**
 * A simple interface for receiving events from the {@linkplain PlayerHater} service.
 * 
 * @since 1.0.0
 * @version 2.0.0
 * 
 * @author Chris Rhoden
 */
public interface PlayerHaterListener {
	void onStopped();

	void onPaused(Song song);

	void onLoading(Song song);

	void onPlaying(Song song, int progress);
}
