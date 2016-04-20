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
package com.chrisrhoden.glisten.songs;

import com.chrisrhoden.glisten.Song;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

class RemoteSong implements Song {

	private static SongHost.Remote getRemote() {
		return SongHost.sRemote;
	}

	private final int mTag;
	private Song mSong = null;
	private static final String remoteSongExceptionMessage = "Remote Process has died or become disconnected and song data has not been copied";

	RemoteSong(int tag) {
		mTag = tag;
	}

	@Override
	public String getTitle() {
		try {
			return getRemote().getSongTitle(mTag);
		} catch (RemoteException e) {
			if (mSong != null) {
				return mSong.getTitle();
			}
			throw new IllegalStateException(remoteSongExceptionMessage, e);
		}
	}

	@Override
	public String getArtist() {
		try {
			return getRemote().getSongArtist(mTag);
		} catch (RemoteException e) {
			if (mSong != null) {
				return mSong.getArtist();
			}
			throw new IllegalStateException(remoteSongExceptionMessage, e);
		}
	}

	@Override
	public Uri getAlbumArt() {
		try {
			return getRemote().getSongAlbumArt(mTag);
		} catch (RemoteException e) {
			if (mSong != null) {
				return mSong.getAlbumArt();
			}
			throw new IllegalStateException(remoteSongExceptionMessage, e);
		}
	}

	@Override
	public Uri getUri() {
		try {
			return getRemote().getSongUri(mTag);
		} catch (RemoteException e) {
			if (mSong != null) {
				return mSong.getUri();
			}
			throw new IllegalStateException(remoteSongExceptionMessage, e);
		}
	}

	@Override
	public Bundle getExtra() {
		try {
			return getRemote().getSongExtra(mTag);
		} catch (RemoteException e) {
			if (mSong != null) {
				return mSong.getExtra();
			}
			throw new IllegalStateException(remoteSongExceptionMessage, e);
		}
	}

	@Override
	public String getAlbumTitle() {
		try {
			return getRemote().getSongAlbumTitle(mTag);
		} catch (RemoteException e) {
			if (mSong != null) {
				return mSong.getAlbumTitle();
			}
			throw new IllegalStateException(remoteSongExceptionMessage, e);
		}
	}

	void setSong(Song song) {
		mSong = song;
	}
	
	Song getSong() { 
		return mSong; 
	}
}
