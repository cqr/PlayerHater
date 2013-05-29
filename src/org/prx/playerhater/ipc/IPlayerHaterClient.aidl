package org.prx.playerhater.ipc;

import android.net.Uri;
import android.app.PendingIntent;

interface IPlayerHaterClient {
    
	/**
	 * Plugin Methods
	 */
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
    void onAlbumTitleChanged(String albumTitle);
    void onAlbumArtChanged(in Uri uri);
    void onTransportControlFlagsChanged(int transportControlFlags);
    void onNextSongAvailable(int songTag);
    void onNextSongUnavailable();
    void onChangesComplete();
    void onIntentActivityChanged(in PendingIntent intent);
    
    /**
     * SongHost Methods
     */
    String getSongTitle(int songTag);
    String getSongArtist(int songTag);
    String getSongAlbumTitle(int songTag);
    Uri getSongAlbumArt(int songTag);
    Uri getSongUri(int songTag);
    Bundle getSongExtra(int songTag);
}