package org.prx.playerhater.ipc;

import android.net.Uri;
import org.prx.playerhater.ipc.IPlayerHaterClient;
import android.app.Notification;
import android.app.PendingIntent;

interface IPlayerHaterServer {

	/**
	 * Server-specific methods
	 */
    void setClient(IPlayerHaterClient client);
    void onRemoteControlButtonPressed(int keyCode);
    void startForeground(int notificationNu, in Notification notification);
    void stopForeground(boolean fact);
    void duck();
    void unduck();

    /**
     * PlayerHater Methods
     */
    boolean pause();
    boolean stop();
    boolean resume();
    boolean playAtTime(int startTime);
    boolean play(int songTag, int startTime);
    boolean seekTo(int startTime);
    int enqueue(int songTag);
    boolean skipTo(int position);
    void skip();
    void skipBack();
    void emptyQueue();
    int getCurrentPosition();
    int getDuration();
    int nowPlaying();
    boolean isPlaying();
    boolean isLoading();
    int getState();
    void setTransportControlFlags(int transportControlFlags);
    void setPendingIntent(in PendingIntent intent);
    int getQueueLength();
    int getQueuePosition();
    boolean removeFromQueue(int position);
    
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