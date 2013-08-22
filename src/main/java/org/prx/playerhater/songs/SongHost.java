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
package org.prx.playerhater.songs;

import java.util.HashMap;
import java.util.Map;

import org.prx.playerhater.Song;
import org.prx.playerhater.ipc.IPlayerHaterClient;
import org.prx.playerhater.ipc.IPlayerHaterServer;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.SparseArray;

public class SongHost {

	public static final int INVALID_TAG = -1;

	private static Remote sRemote;
	private static SparseArray<Song> sSongs;
	private static Map<Song, Integer> sTags;

	public static void setRemote(Remote remote) {
		sRemote = remote;
	}

	public static void setRemote(IPlayerHaterClient client) {
		sRemote = new ClientRemote(client);
	}

	public static void setRemote(IPlayerHaterServer server) {
		sRemote = new ServerRemote(server);
	}

	public static void slurp(int songTag, Bundle songData) {
		Song song = getSong(songTag);
		if (song instanceof RemoteSong) {
			((RemoteSong) getSong(songTag)).setSong(Songs.fromBundle(songData));
		}
	}

	public static SparseArray<Bundle> localSongs() {
		SparseArray<Bundle> data = new SparseArray<Bundle>();
		for (Song song : getTags().keySet()) {
			if (!(song instanceof RemoteSong)) {
				data.put(getTag(song), Songs.toBundle(song));
			}
		}
		return data;
	}

	static Remote remote() {
		if (sRemote == null) { 
			return new NullRemote(); 
		}
		return sRemote;
	}

	public static void clear() {
		sRemote = null;
		sSongs = null;
		sTags = null;
	}

	public static int getTag(Song song) {
		if (song == null) {
			return INVALID_TAG;
		}
		if (getTags().containsKey(song)) {
			return getTags().get(song);
		} else {
			int tag = song.hashCode();
			getTags().put(song, tag);
			getSongs().put(tag, song);
			return tag;
		}
	}

	public static Song getSong(int tag) {
		if (tag == INVALID_TAG) {
			return null;
		}
		Song song = getSongs().get(tag);
		if (song != null) {
			return song;
		} else {
			song = new RemoteSong(tag);
			getTags().put(song, tag);
			getSongs().put(tag, song);
			return song;
		}
	}

	private static SparseArray<Song> getSongs() {
		if (sSongs == null) {
			sSongs = new SparseArray<Song>();
		}
		return sSongs;
	}

	private static Map<Song, Integer> getTags() {
		if (sTags == null) {
			sTags = new HashMap<Song, Integer>();
		}
		return sTags;
	}

	static interface Remote {
		Uri getSongAlbumArt(int tag) throws RemoteException;

		Uri getSongUri(int tag) throws RemoteException;

		String getSongAlbumTitle(int tag) throws RemoteException;

		String getSongTitle(int tag) throws RemoteException;

		String getSongArtist(int tag) throws RemoteException;

		Bundle getSongExtra(int tag) throws RemoteException;
	}

	private static final class ClientRemote implements Remote {
		private final IPlayerHaterClient mClient;

		private ClientRemote(IPlayerHaterClient client) {
			mClient = client;
		}

		@Override
		public Uri getSongAlbumArt(int tag) throws RemoteException {
			return mClient.getSongAlbumArt(tag);
		}

		@Override
		public Uri getSongUri(int tag) throws RemoteException {
			return mClient.getSongUri(tag);
		}

		@Override
		public String getSongTitle(int tag) throws RemoteException {
			return mClient.getSongTitle(tag);
		}

		@Override
		public String getSongArtist(int tag) throws RemoteException {
			return mClient.getSongArtist(tag);
		}

		@Override
		public Bundle getSongExtra(int tag) throws RemoteException {
			return mClient.getSongExtra(tag);
		}

		@Override
		public String getSongAlbumTitle(int tag) throws RemoteException {
			return mClient.getSongAlbumTitle(tag);
		}
	}

	private static class ServerRemote implements Remote {
		private final IPlayerHaterServer mServer;

		private ServerRemote(IPlayerHaterServer server) {
			mServer = server;
		}

		@Override
		public Uri getSongAlbumArt(int tag) throws RemoteException {
			return mServer.getSongAlbumArt(tag);
		}

		@Override
		public Uri getSongUri(int tag) throws RemoteException {
			return mServer.getSongUri(tag);
		}

		@Override
		public String getSongTitle(int tag) throws RemoteException {
			return mServer.getSongTitle(tag);
		}

		@Override
		public String getSongArtist(int tag) throws RemoteException {
			return mServer.getSongArtist(tag);
		}

		@Override
		public Bundle getSongExtra(int tag) throws RemoteException {
			return mServer.getSongExtra(tag);
		}

		@Override
		public String getSongAlbumTitle(int tag) throws RemoteException {
			return mServer.getSongAlbumTitle(tag);
		}
	}
	
	private static class NullRemote implements Remote {

		@Override
		public Uri getSongAlbumArt(int tag) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Uri getSongUri(int tag) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getSongAlbumTitle(int tag) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getSongTitle(int tag) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getSongArtist(int tag) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle getSongExtra(int tag) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
