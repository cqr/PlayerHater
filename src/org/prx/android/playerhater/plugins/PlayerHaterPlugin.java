package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.PlayerHaterServiceBinder;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;

public interface PlayerHaterPlugin {
	
	void onPlayerHaterLoaded(Context context, PlayerHater playerHater);
	
	void onServiceBound(PlayerHaterServiceBinder binder);
	
	void onServiceStopping();
	
	void onSongChanged(Song song);
	
	void onSongFinished(Song song, int reason);
	
	void onDurationChanged(int duration);

	void onAudioLoading();
	
	void onAudioPaused();
	
	void onAudioResumed();
	
	void onAudioStarted();

	void onAudioStopped();

	void onTitleChanged(String title);

	void onArtistChanged(String artist);

	void onAlbumArtChanged(int resourceId);

	void onAlbumArtChangedToUri(Uri url);
	
	void onNextSongAvailable(Song nextTrack);
	
	void onNextSongUnavailable();

	void onIntentActivityChanged(PendingIntent pending);

	void onChangesComplete(); 
}
