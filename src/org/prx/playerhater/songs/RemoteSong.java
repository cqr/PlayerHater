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

import org.prx.playerhater.Song;
import org.prx.playerhater.ipc.IPlayerHaterClient;
import org.prx.playerhater.ipc.IPlayerHaterServer;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

public class RemoteSong implements Song {

	private static SongHost sSongHost;

	public static interface SongHost {
		Uri getSongAlbumArt(int tag) throws RemoteException;

		Uri getSongUri(int tag) throws RemoteException;

		String getSongAlbumTitle(int tag) throws RemoteException;

		String getSongTitle(int tag) throws RemoteException;

		String getSongArtist(int tag) throws RemoteException;

		Bundle getSongExtra(int tag) throws RemoteException;
	}

	private static final class ClientSongHost implements SongHost {
		private final IPlayerHaterClient mClient;

		private ClientSongHost(IPlayerHaterClient client) {
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

	private static class ServerSongHost implements SongHost {
		private final IPlayerHaterServer mServer;

		private ServerSongHost(IPlayerHaterServer server) {
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

	public static void setSongHost(SongHost host) {
		sSongHost = host;
	}

	public static void setSongHost(IPlayerHaterClient client) {
		sSongHost = new ClientSongHost(client);
	}

	public static void setSongHost(IPlayerHaterServer server) {
		sSongHost = new ServerSongHost(server);
	}

	private static SongHost getSongHost() {
		return sSongHost;
	}

	private final int mTag;

	RemoteSong(int tag) {
		mTag = tag;
	}

	@Override
	public String getTitle() {
		try {
			return getSongHost().getSongTitle(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public String getArtist() {
		try {
			return getSongHost().getSongArtist(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public Uri getAlbumArt() {
		try {
			return getSongHost().getSongAlbumArt(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public Uri getUri() {
		try {
			return getSongHost().getSongUri(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public Bundle getExtra() {
		try {
			return getSongHost().getSongExtra(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public String getAlbumTitle() {
		try {
			return getSongHost().getSongAlbumTitle(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}
}
