package org.prx.playerhater;

import org.prx.playerhater.service.PlayerHaterService;
import org.prx.playerhater.songs.SongQueue;
import org.prx.playerhater.songs.SongQueue.OnQueuedSongsChangedListener;

public class PlaybackService extends PlayerHaterService implements
		OnQueuedSongsChangedListener {

	@Override
	public boolean pause() {
		return getMediaPlayer().conditionalPause();
	}

	@Override
	public boolean stop() {
		onStopped();
		return getMediaPlayer().conditionalStop();
	}

	@Override
	public boolean play() {
		return getMediaPlayer().conditionalPlay();
	}

	@Override
	public boolean play(int startTime) {
		getMediaPlayer().conditionalPause();
		getMediaPlayer().seekTo(startTime);
		getMediaPlayer().conditionalPlay();
		return true;
	}

	@Override
	public boolean play(Song song, int startTime) {
		getMediaPlayer().prepareAndPlay(getApplicationContext(), song.getUri(),
				startTime);
		return true;
	}

	@Override
	public boolean seekTo(int startTime) {
		getMediaPlayer().seekTo(startTime);
		return true;
	}

	@Override
	public int enqueue(Song song) {
		return getQueue().appendSong(song);
	}

	@Override
	public boolean skipTo(int position) {
		return getQueue().skipTo(position);
	}

	@Override
	public void skip() {
		getQueue().next();
	}

	@Override
	public void skipBack() {
		getQueue().back();
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
		return getQueue().getPosition();
	}

	@Override
	public boolean removeFromQueue(int position) {
		return getQueue().remove(position);
	}

	@Override
	public void onNowPlayingChanged(Song nowPlaying) {
		getMediaPlayer().prepare(getApplicationContext(), nowPlaying.getUri());
		onSongChanged();
	}

	@Override
	public void onNextSongChanged(Song nextSong) {
		onNextSongChanged();
	}
	
	@Override
	public Song getNextSong() {
		return getQueue().getNextPlaying();
	}
}
