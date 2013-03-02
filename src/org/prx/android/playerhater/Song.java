package org.prx.android.playerhater;

import android.net.Uri;

public interface Song {
	String getTitle();
	String getArtist();
	Uri getAlbumArt();
	Uri getUri();
}
