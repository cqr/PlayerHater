package org.prx.android.playerhater.util;

import org.prx.android.playerhater.PlayerHaterListener;

import android.net.Uri;

public interface ClientPlayerHater extends IPlayerHater {

	// Useful
	void setListener(PlayerHaterListener listener, boolean withEcho);

	// For sound effects
	TransientPlayer playEffect(Uri url);

	TransientPlayer playEffect(Uri url, boolean isDuckable);

	// For convenience
	boolean play(Uri url);

	boolean play(Uri url, int startTime);

}
