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

import com.chrisrhoden.glisten.PlayerHaterPlugin;
import com.chrisrhoden.glisten.songs.SongHost;

import android.app.PendingIntent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

public class PlayerHaterClient extends IPlayerHaterClient.Stub {
	
	private final PlayerHaterPlugin mPlugin;
	
	public PlayerHaterClient(PlayerHaterPlugin plugin) {
		mPlugin = plugin;
	}

	@Override
	public void onSongChanged(int songTag) throws RemoteException {
		mPlugin.onSongChanged(SongHost.getSong(songTag));
	}

	@Override
	public void onSongFinished(int songTag, int reason) throws RemoteException {
		mPlugin.onSongFinished(SongHost.getSong(songTag), reason);
	}

	@Override
	public void onDurationChanged(int duration) throws RemoteException {
		mPlugin.onDurationChanged(duration);
	}

	@Override
	public void onAudioLoading() throws RemoteException {
		mPlugin.onAudioLoading();
	}

	@Override
	public void onAudioPaused() throws RemoteException {
		mPlugin.onAudioPaused();
	}

	@Override
	public void onAudioResumed() throws RemoteException {
		mPlugin.onAudioResumed();
	}

	@Override
	public void onAudioStarted() throws RemoteException {
		mPlugin.onAudioStarted();
	}

	@Override
	public void onAudioStopped() throws RemoteException {
		mPlugin.onAudioStopped();
	}

	@Override
	public void onTitleChanged(String title) throws RemoteException {
		mPlugin.onTitleChanged(title);
	}

	@Override
	public void onArtistChanged(String artist) throws RemoteException {
		mPlugin.onArtistChanged(artist);
	}

	@Override
	public void onAlbumTitleChanged(String albumTitle) throws RemoteException {
		mPlugin.onAlbumTitleChanged(albumTitle);
	}

	@Override
	public void onAlbumArtChanged(Uri uri) throws RemoteException {
		mPlugin.onAlbumArtChanged(uri);
	}

	@Override
	public void onTransportControlFlagsChanged(int transportControlFlags)
			throws RemoteException {
		mPlugin.onTransportControlFlagsChanged(transportControlFlags);
	}

	@Override
	public void onNextSongAvailable(int songTag) throws RemoteException {
		mPlugin.onNextSongAvailable(SongHost.getSong(songTag));
	}

	@Override
	public void onNextSongUnavailable() throws RemoteException {
		mPlugin.onNextSongUnavailable();
	}

	@Override
	public void onChangesComplete() throws RemoteException {
		mPlugin.onChangesComplete();
	}

	@Override
	public void onIntentActivityChanged(PendingIntent intent)
			throws RemoteException {
		mPlugin.onPendingIntentChanged(intent);
	}

	@Override
	public String getSongTitle(int songTag) throws RemoteException {
		return SongHost.getLocalSong(songTag).getTitle();
	}

	@Override
	public String getSongArtist(int songTag) throws RemoteException {
		return SongHost.getLocalSong(songTag).getArtist();
	}

	@Override
	public String getSongAlbumTitle(int songTag) throws RemoteException {
		return SongHost.getLocalSong(songTag).getAlbumTitle();
	}

	@Override
	public Uri getSongAlbumArt(int songTag) throws RemoteException {
		return SongHost.getLocalSong(songTag).getAlbumArt();
	}

	@Override
	public Uri getSongUri(int songTag) throws RemoteException {
		return SongHost.getLocalSong(songTag).getUri();
	}

	@Override
	public Bundle getSongExtra(int songTag) throws RemoteException {
		return SongHost.getLocalSong(songTag).getExtra();
	}

    @Override
    public void onPlayerHaterShutdown() throws RemoteException {
        mPlugin.onPlayerHaterShutdown();
    }

}
