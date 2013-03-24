package org.prx.android.playerhater.plugins;

import android.net.Uri;
import org.prx.android.playerhater.service.IPlayerHaterBinder;

interface IRemotePlugin {

	void onUnbindRequested();
	
	void onSongChanged(int songTag);
	
	void onSongFinished(int songTag, int reason);
	
	void onDurationChanged(int duration);
	
	void onAudioLoading();
	
	void onAudioPaused();
	
	void onAudioResumed();
	
	void onAudioStarted();
	
	void onAudioStopped();
	
	void onTitleChanged(String title);
	
	void onArtistChanged(String artist);
	
	void onAlbumArtResourceChanged(int albumArtResource);
	
	void onAlbumArtUriChanged(in Uri uri);
	
	void onNextSongAvailable(int songTag);
	
	void onNextSongUnavailable();
	
	void onServiceBound(IPlayerHaterBinder binder);
	
	void onIntentActivityChanged(in PendingIntent intent);
	
	void onChangesComplete();
	
	void releaseSong(int songTag);
	
	void onTransportControlFlagsChanged(int transportControlFlags);
}