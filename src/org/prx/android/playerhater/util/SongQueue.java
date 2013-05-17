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

public class SongQueue {

	public interface OnQeueuedSongsChangedListener {
		public void onNowPlayingChanged(Song nowPlaying);

		public void onNextSongChanged(Song nextSong);
	}

	private int mPlayheadPosition = -1;
	private final List<Song> mSongs = new ArrayList<Song>();

	private Song mNextSongWas = null;
	private Song mCurrentSongWas = null;
	private OnQeueuedSongsChangedListener mListener;

	public void setQueuedSongsChangedListener(
			OnQeueuedSongsChangedListener listener) {
		mListener = listener;
	}

	public int appendSong(Song song) {
		addSongAtPosition(song, mSongs.size());
		return mSongs.size() - mPlayheadPosition;
	}

	public void addSongAtPosition(Song song, int position) {
		mSongs.add(position, song);
		songOrderChanged();
	}

	public Song next() {
		mPlayheadPosition += 1;
		if (mPlayheadPosition > mSongs.size()) {
			mPlayheadPosition = 1;
		}
		songOrderChanged();
		return getNowPlaying();
	}
	
	public Song back() {
		mPlayheadPosition -= 1;
		if (mPlayheadPosition <= 0) {
			mPlayheadPosition = 1;
		}
		songOrderChanged();
		return getNowPlaying();
	}
	
	public void skipToEnd() {
		mPlayheadPosition = mSongs.size();
		songOrderChanged();
	}
	
	public boolean isAtLastSong() {
		return mPlayheadPosition == mSongs.size();
	}

	public Song getNowPlaying() {
		if (mPlayheadPosition <= 0) {
			return null;
		}
		return mSongs.get(mPlayheadPosition - 1);
	}

	public Song peekNextSong() {
		if (mPlayheadPosition == -1) {
			return null;
		}
		if (mPlayheadPosition == mSongs.size()) {
			return null;
		}
		return mSongs.get(mPlayheadPosition);
	}

	public void empty() {
		mSongs.clear();
		mPlayheadPosition = -1;
		songOrderChanged();
	}

	private void songOrderChanged() {
		if (mSongs.size() > 0) {
			if (mPlayheadPosition == -1) {
				mPlayheadPosition = 1;
			}

			if (mCurrentSongWas == null || mCurrentSongWas != getNowPlaying()) {
				currentSongChanged();
			}
			if (mSongs.size() > 1) {
				if (mNextSongWas == null || mNextSongWas != peekNextSong()) {
					nextSongChanged();
				}
			} else if (mNextSongWas != null) {
				nextSongChanged();
			}
		} else {
			if (mCurrentSongWas != null) {
				currentSongChanged();
			}
			if (mNextSongWas != null) {
				nextSongChanged();
			}
		}
	}

	private void currentSongChanged() {
		mCurrentSongWas = getNowPlaying();
		if (mListener != null && mCurrentSongWas != null)
			mListener.onNowPlayingChanged(mCurrentSongWas);
	}

	private void nextSongChanged() {
		mNextSongWas = peekNextSong();
		if (mListener != null)
			mListener.onNextSongChanged(mNextSongWas);
	}

	public boolean skipTo(int position) {
		if (position <= mSongs.size()) {
			if (position < 0) {
				position = mSongs.size() + position + 1;
			}
			mPlayheadPosition = position;
			songOrderChanged();
			return true;
		}
		return false;
	}

	public List<Song> getSongsBefore() {
		List<Song> songs = new ArrayList<Song>();
		if (mPlayheadPosition > 1) {
			for (int i=1; i < mPlayheadPosition; i++) {
				songs.add(mSongs.get(i-1));
			}
		}
		return songs;
	}
	
	public List<Song> getSongsAfter() {
		List<Song> songs = new ArrayList<Song>();
		if (mPlayheadPosition > 0 && !isAtLastSong()) {
			for (int i=mPlayheadPosition+1; i <= mSongs.size(); i++) {
				songs.add(mSongs.get(i-1));
			}
		}
		return songs;
	}

	public int size() {
		return mSongs.size();
	}

	public Song[] toArray() {
		return mSongs.toArray(new Song[size()-1]);
	}

	public int getPosition() {
		return mPlayheadPosition - 1;
	}

	public boolean remove(int position) {
		if (mSongs.size() < position) {
			return false;
		} else {
			mSongs.remove(position - 1);
			songOrderChanged();
			return true;
		}
	}

}
