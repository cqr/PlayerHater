package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.Song;

import android.content.Intent;

public class PebblePlugin extends AbstractPlugin {

	private Song mSong;

	@Override
	public void onSongChanged(Song song) {
		mSong = song;
		if (getPlayerHater().isPlaying()) {
			onAudioStarted();
		}
	}
	
	@Override
	public void onAudioStarted() {
		if (mSong != null) {
			Intent intent = new Intent("com.getpebble.action.NOW_PLAYING");
			intent.putExtra("artist", mSong.getArtist());
			intent.putExtra("track", mSong.getTitle());
			intent.putExtra("album", "");
			
			getContext().sendBroadcast(intent);
		}
	}
	
}
