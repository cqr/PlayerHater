package org.prx.android.playerhater.util;

import java.util.ArrayList;
import java.util.List;

import org.prx.android.playerhater.Song;

import android.util.Log;

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

	public void appendSong(Song song) {
		addSongAtPosition(song, mSongs.size());
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
		if (mPlayheadPosition == -1) {
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
			mPlayheadPosition = position;
			songOrderChanged();
			return true;
		}
		return false;
	}


}
