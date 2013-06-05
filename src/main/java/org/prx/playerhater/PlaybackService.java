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

package org.prx.playerhater;

import org.prx.playerhater.mediaplayer.MediaPlayerPool;
import org.prx.playerhater.mediaplayer.SynchronousPlayer;
import org.prx.playerhater.service.PlayerHaterService;
import org.prx.playerhater.songs.SongQueue;
import org.prx.playerhater.songs.SongQueue.OnQueuedSongsChangedListener;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;

public class PlaybackService extends PlayerHaterService implements
		OnQueuedSongsChangedListener, OnErrorListener, OnCompletionListener {

	private MediaPlayerPool mMediaPlayerPool;

	@Override
	public void onCreate() {
		super.onCreate();
		mMediaPlayerPool = new MediaPlayerPool();
	}

	@Override
	public void onDestroy() {
		mMediaPlayerPool.release();
		super.onDestroy();
	}

	@Override
	public boolean play(Song song, int startTime) {
		onSongFinished(nowPlaying(), PlayerHater.FINISH_SKIP_BUTTON);
		onNowPlayingChanged(song, nowPlaying());
		getQueue().appendAndSkip(song);
		seekTo(startTime);
		return play();
	}

	@Override
	public boolean seekTo(int startTime) {
		getMediaPlayer().seekTo(startTime);
		return true;
	}

	@Override
	public int enqueue(Song song) {
		int q = getQueue().appendSong(song);
		return q;
	}

	@Override
	public void enqueue(int position, Song song) {
		getQueue().addSongAtPosition(song, position);
	}

	@Override
	public boolean skipTo(int position) {
		startTransaction();
		onSongFinished(nowPlaying(), PlayerHater.FINISH_SKIP_BUTTON);
		if (getQueue().skipTo(position)) {
			return true;
		} else {
			commitTransaction();
			return false;
		}
	}

	@Override
	public void skip() {
		startTransaction();
		onSongFinished(nowPlaying(), PlayerHater.FINISH_SKIP_BUTTON);
		getQueue().next();
	}

	@Override
	public void skipBack() {
		if (getCurrentPosition() < 2000) {
			startTransaction();
			onSongFinished(nowPlaying(), PlayerHater.FINISH_SKIP_BUTTON);
			getQueue().back();
		} else {
			seekTo(0);
		}
	}

	@Override
	public void emptyQueue() {
		getQueue().empty();
	}

	@Override
	public Song nowPlaying() {
		return getQueue().getNowPlaying();
	}

	private SongQueue mQueue;

	private SongQueue getQueue() {
		if (mQueue == null) {
			mQueue = new SongQueue();
			mQueue.setQueuedSongsChangedListener(this);
		}
		return mQueue;
	}

	@Override
	public int getQueueLength() {
		return getQueue().size();
	}

	@Override
	public int getQueuePosition() {
		return getQueue().getPosition() - (getCurrentPosition() > 0 ? 0 : 1);
	}

	@Override
	public boolean removeFromQueue(int position) {
		return getQueue().remove(position);
	}

	@Override
	public void onNowPlayingChanged(Song nowPlaying, Song was) {
		startTransaction();
		mMediaPlayerPool.recycle(peekMediaPlayer());
		if (nowPlaying == null) {
			setMediaPlayer(null);
		} else {
			setMediaPlayer(mMediaPlayerPool.getPlayer(getApplicationContext(),
					nowPlaying.getUri()));
			if (isPlaying()) {
				getMediaPlayer().start();
			}
		}
		commitTransaction();
		onSongChanged(nowPlaying);
	}

	@Override
	public void onNextSongChanged(Song nextSong, Song was) {
		if (nextSong != null) {
			mMediaPlayerPool
					.prepare(getApplicationContext(), nextSong.getUri());
		}
		onNextSongChanged(nextSong);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (peekMediaPlayer() != null && peekMediaPlayer().equals(mp)) {
			startTransaction();
			mMediaPlayerPool.recycle(peekMediaPlayer());
			setMediaPlayer(null);
			onSongFinished(nowPlaying(), PlayerHater.FINISH_SONG_END);
			getQueue().next();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (peekMediaPlayer() != null && peekMediaPlayer().equals(mp)) {
			startTransaction();
			mMediaPlayerPool.recycle(peekMediaPlayer());
			setMediaPlayer(null);
			onSongFinished(nowPlaying(), PlayerHater.FINISH_ERROR);
			getQueue().next();
			return true;
		}
		return false;
	}

	@Override
	public Song getNextSong() {
		return getQueue().getNextPlaying();
	}

	@Override
	protected void setMediaPlayer(SynchronousPlayer mediaPlayer) {
		SynchronousPlayer oldPlayer = peekMediaPlayer();
		if (oldPlayer != null) {
			oldPlayer.setOnErrorListener(null);
			oldPlayer.setOnCompletionListener(null);
		}
		super.setMediaPlayer(mediaPlayer);
		if (mediaPlayer != null) {
			mediaPlayer.setOnErrorListener(this);
			mediaPlayer.setOnCompletionListener(this);
		}
	}
}
