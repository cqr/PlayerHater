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
package org.prx.playerhater.playerhater;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.PlayerHaterListener;
import org.prx.playerhater.Song;
import org.prx.playerhater.player.Player;
import org.prx.playerhater.service.IPlayerHaterBinder;
import org.prx.playerhater.util.BasicSong;
import org.prx.playerhater.util.Log;

import android.app.Activity;
import android.net.Uri;
import android.os.RemoteException;
import android.util.SparseArray;

public class BinderPlayerHater extends PlayerHater {

	private static BinderPlayerHater sInstance;
	private final IPlayerHaterBinder mBinder;
	private final SparseArray<Song> mSongs = new SparseArray<Song>();

	public static BinderPlayerHater get(IPlayerHaterBinder binder) {
		if (sInstance == null) {
			sInstance = new BinderPlayerHater(binder);
		}
		return sInstance;
	}

	public static void detach() {
		sInstance = null;
	}

	private BinderPlayerHater(IPlayerHaterBinder binder) {
		mBinder = binder;
	}

	@Override
	public boolean pause() {
		try {
			return mBinder.pause();
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean stop() {
		try {
			return mBinder.stop();
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean play() {
		try {
			return mBinder.resume();
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean play(int startTime) {
		try {
			return mBinder.play(startTime);
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean play(Song song) {
		return play(song, 0);
	}

	@Override
	public boolean play(Song song, int startTime) {
		try {
			if (enqueue(song) != -1) {
				if (skipTo(mBinder.getQueueLength())) {
					play(startTime);
				}
			}
		} catch (RemoteException e) {
			removeSong(song);
		}
		return false;
	}

	@Override
	public boolean seekTo(int startTime) {
		try {
			return mBinder.seekTo(startTime);
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public int enqueue(Song song) {
		try {
			return mBinder.enqueue(tagSong(song));
		} catch (RemoteException e) {
			return -1;
		}
	}

	@Override
	public boolean skipTo(int position) {
		try {
			return mBinder.skipTo(position);
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public void skip() {
		try {
			mBinder.skip();
		} catch (RemoteException e) {}
	}

	@Override
	public void skipBack() {
		try {
			mBinder.skipBack();
		} catch (RemoteException e) {}
	}

	@Override
	public void emptyQueue() {
		try {
			mBinder.emptyQueue();
		} catch (RemoteException e) {}
	}

	@Override
	public void setAlbumArt(int resourceId) {
		try {
			mBinder.setAlbumArtResource(resourceId);
		} catch (RemoteException e) {}
	}

	@Override
	public void setAlbumArt(Uri url) {
		try {
			mBinder.setAlbumArtUrl(url);
		} catch (RemoteException e) {}
	}

	@Override
	public void setTitle(String title) {
		try {
			mBinder.setTitle(title);
		} catch (RemoteException e) {}
	}

	@Override
	public void setArtist(String artist) {
		try {
			mBinder.setArtist(artist);
		} catch (RemoteException e) {}
	}

	@Override
	public void setActivity(Activity activity) {
		// XXX
	}

	@Override
	public int getCurrentPosition() {
		try {
			return mBinder.getCurrentPosition();
		} catch (RemoteException e) {
			return -1;
		}
	}

	@Override
	public int getDuration() {
		try {
			return mBinder.getDuration();
		} catch (RemoteException e) {
			return -1;
		}
	}

	@Override
	@Deprecated
	/**
	 * @author chris
	 * @throws IllegalStateException Always.
	 */
	public void setListener(PlayerHaterListener listener) {
		throw new IllegalStateException("This is not supported.");
	}

	@Override
	public Song nowPlaying() {
		try {
			return getSong(mBinder.getNowPlayingTag());
		} catch (RemoteException e) {
			return null;
		}
	}

	@Override
	public boolean isPlaying() {
		try {
			return mBinder.isPlaying();
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean isLoading() {
		try {
			return mBinder.isLoading();
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public int getState() {
		try {
			return mBinder.getState();
		} catch (RemoteException e) {
			return Player.ERROR;
		}
	}

	@Override
	public void setTransportControlFlags(int transportControlFlags) {
		try {
			mBinder.setTransportControlFlags(transportControlFlags);
		} catch (RemoteException e) {}
	}

	private int tagSong(Song song) {
		mSongs.put(song.hashCode(), song);
		return song.hashCode();
	}

	private void removeSong(Song song) {
		mSongs.delete(song.hashCode());
	}

	public Song getSong(int nowPlayingTag) {
		if (nowPlayingTag == -1) {
			return null;
		}
		Song song = mSongs.get(nowPlayingTag);
		if (song == null) {
			stop();
			emptyQueue();
			return null;
		} else {
			return song;
		}
	}

	public IPlayerHaterBinder getBinder() {
		return mBinder;
	}

	public void releaseSong(int songTag) {
		// XXX TODO This probably isn't necessary (this method is only called by
		// the service when it is sure that it will never send this tag again),
		// but I am leaving it here for now. It is a leak, by its very
		// definition, so probably should be removed soon.
		Log.d("Releasing a song " + songTag);
		if (mSongs.get(songTag) != null) {
			Song newSong = new BasicSong(mSongs.get(songTag));
			mSongs.delete(songTag);
			mSongs.put(songTag, newSong);
		}
	}

	@Override
	public int getQueueLength() {
		try {
			return mBinder.getQueueLength();
		} catch (RemoteException e) {
			return -1;
		}
	}

	@Override
	public int getQueuePosition() {
		try {
			return mBinder.getQueuePosition();
		} catch (RemoteException e) {
			return -1;
		}
	}

	@Override
	public boolean removeFromQueue(int position) {
		try {
			return mBinder.removeFromQueue(position);
		} catch (RemoteException e) {
			return false;
		}
	}

}
