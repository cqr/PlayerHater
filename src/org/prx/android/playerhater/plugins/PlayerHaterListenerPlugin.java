package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;

public class PlayerHaterListenerPlugin extends AbstractPlugin {
	
	private final PlayerHaterListener mListener;
	private Song mSong;
	
	public PlayerHaterListenerPlugin(PlayerHaterListener listener) {
		mListener = listener;
	}

	@Override
	public void onSongChanged(Song song) {
		mSong = song;
	}

	@Override
	public void onSongFinished(Song song, int reason) {
		mListener.onStopped();
	}

	@Override
	public void onAudioLoading() {
		mListener.onLoading(mSong);
	}

	@Override
	public void onAudioPaused() {
		mListener.onPaused(mSong);
	}

	@Override
	public void onAudioResumed() {
		mListener.onPlaying(mSong, getPlayerHater().getCurrentPosition());
	}

	@Override
	public void onAudioStarted() {
		mListener.onPlaying(mSong, getPlayerHater().getCurrentPosition());
	}

	@Override
	public void onAudioStopped() {
		mListener.onStopped();
	}
}
