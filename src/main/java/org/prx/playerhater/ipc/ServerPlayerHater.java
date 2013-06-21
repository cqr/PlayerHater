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

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.Song;
import org.prx.playerhater.songs.SongHost;
import org.prx.playerhater.util.Log;

import android.app.PendingIntent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.SparseArray;

public class ServerPlayerHater extends PlayerHater {

	private static final String SERVER_ERROR = "Server has gone away...";

	private final IPlayerHaterServer mServer;

	public ServerPlayerHater(IPlayerHaterServer server) {
		mServer = server;
	}

	@Override
	public boolean pause() {
		try {
			return mServer.pause();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public boolean stop() {
		try {
			return mServer.stop();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public boolean play() {
		try {
			return mServer.resume();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public boolean play(int startTime) {
		try {
			return mServer.playAtTime(startTime);
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public boolean play(Song song) {
		try {
			return mServer.play(SongHost.getTag(song), 0);
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public boolean play(Song song, int startTime) {
		try {
			return mServer.play(SongHost.getTag(song), startTime);
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public boolean seekTo(int startTime) {
		try {
			return mServer.seekTo(startTime);
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public int enqueue(Song song) {
		try {
			return mServer.enqueue(SongHost.getTag(song));
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public void enqueue(int position, Song song) {
		try {
			mServer.enqueueAtPosition(position, SongHost.getTag(song));
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public boolean skipTo(int position) {
		try {
			return mServer.skipTo(position);
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public void skip() {
		try {
			mServer.skip();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public void skipBack() {
		try {
			mServer.skipBack();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public void emptyQueue() {
		try {
			mServer.emptyQueue();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public int getCurrentPosition() {
		try {
			return mServer.getCurrentPosition();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public int getDuration() {
		try {
			return mServer.getDuration();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public Song nowPlaying() {
		try {
			return SongHost.getSong(mServer.nowPlaying());
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public boolean isPlaying() {
		try {
			return mServer.isPlaying();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public boolean isLoading() {
		try {
			return mServer.isLoading();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public int getState() {
		try {
			return mServer.getState();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public void setTransportControlFlags(int transportControlFlags) {
		try {
			mServer.setTransportControlFlags(transportControlFlags);
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public int getQueueLength() {
		try {
			return mServer.getQueueLength();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public int getQueuePosition() {
		try {
			return mServer.getQueuePosition();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public boolean removeFromQueue(int position) {
		try {
			return mServer.removeFromQueue(position);
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public void setPendingIntent(PendingIntent intent) {
		try {
			mServer.setPendingIntent(intent);
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	public void slurp(SparseArray<Bundle> songs) {
		try {
			int tag = 0;
			for (int i = 0; i < songs.size(); i++) {
				tag = songs.keyAt(i);
				mServer.slurp(tag, songs.get(tag));
			}
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}

	@Override
	public int getTransportControlFlags() {
		try {
			return mServer.getTransportControlFlags();
		} catch (RemoteException e) {
			Log.e(SERVER_ERROR, e);
			throw new IllegalStateException(SERVER_ERROR, e);
		}
	}
}
