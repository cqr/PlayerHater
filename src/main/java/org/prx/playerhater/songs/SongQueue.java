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

import java.util.ArrayList;
import java.util.List;

import org.prx.playerhater.Song;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class SongQueue {

//	private static Handler sHandler;
//	private static final int CURRENT_SONG = 1;
//	private static final int NEXT_SONG = 2;
//
//	private static Handler getHandler() {
//		if (sHandler == null) {
//			HandlerThread thread = new HandlerThread("SongQueue");
//			thread.start();
//			sHandler = new Handler(thread.getLooper()) {
//
//				@Override
//				public void handleMessage(Message msg) {
//					SongMessage m = (SongMessage) msg.obj;
//					switch (msg.what) {
//					case CURRENT_SONG:
//						m.queue.sendSongChanged(m.song);
//						break;
//					case NEXT_SONG:
//						m.queue.sendNextSongChanged(m.song);
//					}
//				}
//
//			};
//		}
//		return sHandler;
//	}
//
//	private static class SongMessage {
//		private final SongQueue queue;
//		private final Song song;
//
//		public SongMessage(SongQueue songQueue, Song theSong) {
//			queue = songQueue;
//			song = theSong;
//		}
//	}

	public interface OnQueuedSongsChangedListener {
		public void onNowPlayingChanged(Song nowPlaying, Song nowPlayingWas);

		public void onNextSongChanged(Song nextSong, Song nextSongWas);
	}

	private int mPlayheadPosition = -1;
	private final List<Song> mSongs = new ArrayList<Song>();

	private Song mNextSongWas = null;
	private Song mCurrentSongWas = null;
	private OnQueuedSongsChangedListener mListener;

	public synchronized void setQueuedSongsChangedListener(
			OnQueuedSongsChangedListener listener) {
		mListener = listener;
	}

	public synchronized int appendSong(Song song) {
		return addSongAtPosition(song, mSongs.size());
	}

	public synchronized int addSongAtPosition(Song song, int position) {
		mSongs.add(position, song);
		songOrderChanged();
		return mSongs.size() - getPlayheadPosition();
	}

	public synchronized Song next() {
		setPlayheadPosition(getPlayheadPosition() + 1);
		if (getPlayheadPosition() > size()) {
			setPlayheadPosition(1);
		}
		songOrderChanged();
		return getNowPlaying();
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

	public synchronized Song getNextPlaying() {
		return getNextSong();
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
		songOrderChanged(notify, notify);
	}

	private void songOrderChanged(boolean notifyCurrent, boolean notifyNext) {
		if (mSongs.size() > 0) {
			if (getPlayheadPosition() == -1) {
				setPlayheadPosition(1);
			}

			if (mCurrentSongWas == null || mCurrentSongWas != getNowPlaying()) {
				currentSongChanged(notifyCurrent);
			}
			if (mSongs.size() > 1) {
				if (mNextSongWas == null || mNextSongWas != getNextSong()) {
					nextSongChanged(notifyNext);
				}
			} else if (mNextSongWas != null) {
				nextSongChanged(notifyNext);
			}
		} else {
			if (mCurrentSongWas != null) {
				currentSongChanged(notifyCurrent);
			}
			if (mNextSongWas != null) {
				nextSongChanged(notifyNext);
			}
		}
	}

	private void currentSongChanged(boolean notify) {
		if (notify && mListener != null)
			sendSongChanged(mCurrentSongWas); 
//			getHandler().obtainMessage(CURRENT_SONG,
//					new SongMessage(this, mCurrentSongWas)).sendToTarget();
		mCurrentSongWas = getNowPlaying();
	}

	private void sendSongChanged(Song reallyWas) {
		mListener.onNowPlayingChanged(getNowPlaying(), reallyWas);
	}

	private void nextSongChanged(boolean notify) {
		if (notify && mListener != null)
			sendNextSongChanged(mNextSongWas);
//			getHandler().obtainMessage(NEXT_SONG,
//					new SongMessage(this, mNextSongWas)).sendToTarget();
		mNextSongWas = getNextSong();
	}

	private void sendNextSongChanged(Song reallyWas) {
		mListener.onNextSongChanged(getNextSong(), reallyWas);
	}

	private Song getNextSong() {
		if (getPlayheadPosition() >= mSongs.size()
				|| getPlayheadPosition() <= 0) {
			return null;
		} else {
			return mSongs.get(getPlayheadPosition());
		}
	}

	public synchronized boolean skipTo(int position) {
		if (position <= mSongs.size()) {
			if (position < 0) {
				return skipTo(mSongs.size() + position + 1);
			}
			setPlayheadPosition(position);
			songOrderChanged();
			return true;
		}
		return false;
	}

	public synchronized int size() {
		return mSongs.size();
	}

	public synchronized boolean remove(int position) {
		if (position < 1 || position > mSongs.size()) {
			return false;
		} else {
			mSongs.remove(position - 1);
			if (position < getPlayheadPosition()) {
				setPlayheadPosition(getPlayheadPosition() - 1);
			}
			if (getPlayheadPosition() >= mSongs.size()) {
				setPlayheadPosition(mSongs.size() >= 1 ? -1 : 1);
			}
			songOrderChanged();
			return true;
		}
	}

	private int getPlayheadPosition() {
		return mPlayheadPosition;
	}

	private void setPlayheadPosition(int playheadPosition) {
		mPlayheadPosition = playheadPosition;
	}

	// XXX Should do some more here.
	public int getPosition() {
		return getPlayheadPosition();
	}
}
