package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.Player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class PlayerHaterListenerPlugin extends AbstractPlugin {

	private static final String TAG = "PlayerHaterListenerPlugin";
	private final PlayerHaterListener mListener;
	private Song mSong;

	public PlayerHaterListenerPlugin(PlayerHaterListener listener) {
		mListener = listener;
	}
	
	@Override
	public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
		super.onPlayerHaterLoaded(context, playerHater);
		mSong = playerHater.nowPlaying();
		onChangesComplete();
	}

	@Override
	public void onSongChanged(Song song) {
		mSong = song;
	}
	
	@Override
	public void onChangesComplete() {
		Log.d(TAG, "mt(1): "+ Looper.myLooper());
		switch(getPlayerHater().getState()) {
		case Player.STARTED:
			mListener.onPlaying(mSong, getPlayerHater().getCurrentPosition());
			break;
		case Player.PREPARING:
			mListener.onLoading(mSong);
		default:
			if (mSong != null) {
				mListener.onPaused(mSong);
			} else {
				mListener.onStopped();
			}
		}
	}
}
