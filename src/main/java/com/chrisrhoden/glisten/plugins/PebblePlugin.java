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
package com.chrisrhoden.glisten.plugins;

import android.content.Intent;

import com.chrisrhoden.glisten.Song;

public class PebblePlugin extends AbstractPlugin {

    private Song mSong;

    @Override
    public void onSongChanged(Song song) {
        mSong = song;
        if (getPlayerHater().isPlaying()) {
            onAudioStarted();
        }
    }

    @Override
    public void onAudioStarted() {
        if (mSong != null) {
            Intent intent = new Intent("com.getpebble.action.NOW_PLAYING");
            intent.putExtra("artist", mSong.getArtist());
            intent.putExtra("track", mSong.getTitle());
            intent.putExtra("album", mSong.getAlbumTitle());

            getContext().sendBroadcast(intent);
        }
    }

}
