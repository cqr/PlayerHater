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
package com.chrisrhoden.glisten.ipc;

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
    void onPlayerHaterShutdown();
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