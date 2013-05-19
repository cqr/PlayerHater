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
package org.prx.android.playerhater.util;

import java.util.ArrayList;
import java.util.List;

import org.prx.android.playerhater.Song;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class SongQueue {

	private static final Handler sHandler;
	private static final int CURRENT_SONG = 1;
	private static final int NEXT_SONG = 2;

	static {
		HandlerThread thread = new HandlerThread("SongQueue");
		thread.start();

		sHandler = new Handler(thread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case CURRENT_SONG:
					((SongQueue) msg.obj).sendSongChanged();
					break;
				case NEXT_SONG:
					((SongQueue) msg.obj).sendNextSongChanged();
				}
			}

		};
	}

	public interface OnQeueuedSongsChangedListener {
		public void onNowPlayingChanged(Song nowPlaying);

		public void onNextSongChanged(Song nextSong);
	}

	private int mPlayheadPosition = -1;
	private final List<Song> mSongs = new ArrayList<Song>();

	private Song mNextSongWas = null;
	private Song mCurrentSongWas = null;
	private OnQeueuedSongsChangedListener mListener;

	public synchronized void setQueuedSongsChangedListener(
			OnQeueuedSongsChangedListener listener) {
		mListener = listener;
	}

	public synchronized int appendSong(Song song) {
		addSongAtPosition(song, mSongs.size());
		return mSongs.size() - getPlayheadPosition();
	}

	public synchronized void addSongAtPosition(Song song, int position) {
		mSongs.add(position, song);
		songOrderChanged();
	}

	public synchronized Song next() {
		return next(true);
	}

	public synchronized Song back() {
		setPlayheadPosition(getPlayheadPosition() - 1);
		if (getPlayheadPosition() <= 0) {
			setPlayheadPosition(1);
		}
		songOrderChanged();
		return getNowPlaying();
	}

	public synchronized void skipToEnd() {
		setPlayheadPosition(mSongs.size());
		songOrderChanged();
	}

	public synchronized boolean isAtLastSong() {
		return getPlayheadPosition() == mSongs.size();
	}

	public synchronized Song getNowPlaying() {
		if (getPlayheadPosition() <= 0) {
			return null;
		}
		return mSongs.get(getPlayheadPosition() - 1);
	}

	public synchronized Song peekNextSong() {
		if (getPlayheadPosition() == -1) {
			return null;
		}
		if (getPlayheadPosition() >= size()) {
			return null;
		}
		try {
			return mSongs.get(getPlayheadPosition());
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public synchronized void empty() {
		mSongs.clear();
		setPlayheadPosition(-1);
		songOrderChanged();
	}

	private void songOrderChanged() {
		songOrderChanged(true);
	}

	private void songOrderChanged(boolean notify) {
		if (mSongs.size() > 0) {
			if (getPlayheadPosition() == -1) {
				setPlayheadPosition(1);
			}

			if (mCurrentSongWas == null || mCurrentSongWas != getNowPlaying()) {
				currentSongChanged(notify);
			}
			if (mSongs.size() > 1) {
				if (mNextSongWas == null || mNextSongWas != peekNextSong()) {
					nextSongChanged(notify);
				}
			} else if (mNextSongWas != null) {
				nextSongChanged(notify);
			}
		} else {
			if (mCurrentSongWas != null) {
				currentSongChanged(notify);
			}
			if (mNextSongWas != null) {
				nextSongChanged(notify);
			}
		}
	}

	private void currentSongChanged(boolean notify) {
		mCurrentSongWas = getNowPlaying();
		if (notify && mListener != null && mCurrentSongWas != null)
			sHandler.obtainMessage(CURRENT_SONG, this).sendToTarget();
	}

	private void sendSongChanged() {
		mListener.onNowPlayingChanged(mCurrentSongWas);
	}

	private void nextSongChanged(boolean notify) {
		mNextSongWas = peekNextSong();
		if (notify && mListener != null)
			sHandler.obtainMessage(NEXT_SONG, this).sendToTarget();
	}

	private void sendNextSongChanged() {
		mListener.onNextSongChanged(mNextSongWas);
	}

	public synchronized boolean skipTo(int position) {
		if (position <= mSongs.size()) {
			if (position < 0) {
				position = mSongs.size() + position + 1;
			}
			setPlayheadPosition(position);
			songOrderChanged();
			return true;
		}
		return false;
	}

	public synchronized List<Song> getSongsBefore() {
		List<Song> songs = new ArrayList<Song>();
		if (getPlayheadPosition() > 1) {
			for (int i = 1; i < getPlayheadPosition(); i++) {
				songs.add(mSongs.get(i - 1));
			}
		}
		return songs;
	}

	public synchronized List<Song> getSongsAfter() {
		List<Song> songs = new ArrayList<Song>();
		if (getPlayheadPosition() > 0 && !isAtLastSong()) {
			for (int i = getPlayheadPosition() + 1; i <= mSongs.size(); i++) {
				songs.add(mSongs.get(i - 1));
			}
		}
		return songs;
	}

	public synchronized int size() {
		return mSongs.size();
	}

	public synchronized Song[] toArray() {
		return mSongs.toArray(new Song[size() - 1]);
	}

	public synchronized int getPosition() {
		return getPlayheadPosition() - 1;
	}

	public synchronized boolean remove(int position) {
		if (position < 1 || mSongs.size() < position) {
			return false;
		} else {
			mSongs.remove(position - 1);
			songOrderChanged();
			return true;
		}
	}

	public synchronized void forward() {
		setPlayheadPosition(getPlayheadPosition() + 1);
		if (getPlayheadPosition() > mSongs.size()) {
			setPlayheadPosition(1);
		}
		mCurrentSongWas = getNowPlaying();
		nextSongChanged(true);
	}

	public synchronized Song next(boolean notifyChanges) {
		setPlayheadPosition(getPlayheadPosition() + 1);
		if (getPlayheadPosition() > mSongs.size()) {
			setPlayheadPosition(1);
		}
		songOrderChanged(notifyChanges);
		return getNowPlaying();
	}

	private int getPlayheadPosition() {
		return mPlayheadPosition;
	}

	private void setPlayheadPosition(int playheadPosition) {
		mPlayheadPosition = playheadPosition;
	}

}
