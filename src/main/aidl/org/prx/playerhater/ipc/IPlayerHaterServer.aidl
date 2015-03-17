/*******************************************************************************
 * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
    boolean play(int songTag, in Bundle songData, int startTime);
    boolean seekTo(int startTime);
    int enqueue(int songTag, in Bundle songData);
    void enqueueAtPosition(int position, int songTag, in Bundle songData);
    boolean skipTo(int position);
    void skip();
    void skipBack();
    void emptyQueue();
    int getCurrentPosition();
    int getDuration();
    int nowPlaying();
    int getQueueSong(int position);
    boolean isPlaying();
    boolean isLoading();
    int getState();
    void setTransportControlFlags(int transportControlFlags);
    int getTransportControlFlags();
    void setPendingIntent(in PendingIntent intent);
    int getQueueLength();
    int getQueuePosition();
    boolean removeFromQueue(int position);
    
    /**
     * SongHost Methods
     */
    String getSongTitle(int songTag);
    String getSongJson(int songTag);
    String getSongArtist(int songTag);
    String getSongAlbumTitle(int songTag);
    Uri getSongAlbumArt(int songTag);
    Uri getSongUri(int songTag);
    Bundle getSongExtra(int songTag);
    void slurp(int songTag, in Bundle songData);
}