package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.Player;

import android.content.Context;

/**
 * A wrapper around the {@link PlayerHaterListener} interface allowing
 * {@linkplain PlayerHaterListener}s to be treated as plugins.
 * <p>
 * All callbacks made to the underlying {@link PlayerHaterListener} will happen
 * in the same thread as this plugin's
 * {@link PlayerHaterPlugin#onPlayerHaterLoaded(Context, PlayerHater)} or
 * {@link PlayerHaterPlugin#onChangesComplete()}. In the default arrangement,
 * both of these callbacks are made on the UI thread.
 * 
 * @version 2.1.0
 * @since 2.1.0
 * @author Chris Rhoden
 */
public class PlayerHaterListenerPlugin extends AbstractPlugin {

	private final PlayerHaterListener mListener;
	private final boolean mEcho;
	private Song mSong;

	/**
	 * Instantiates a PlayerHaterListenerPlugin with echo enabled.
	 * 
	 * @see {link PlayerHaterListenerPlugin}
	 * @see {@link PlayerHaterListenerPlugin#PlayerHaterListenerPlugin(PlayerHaterListener, boolean)}
	 * @param listener
	 *            The {@link PlayerHaterListener} that this plugin wraps.
	 */
	public PlayerHaterListenerPlugin(PlayerHaterListener listener) {
		this(listener, true);
	}

	/**
	 * Instantiates a PlayerHaterListenerPlugin with optional echo.
	 * 
	 * @see {@link PlayerHaterListenerPlugin}
	 * @see {@link PlayerHaterListenerPlugin#PlayerHaterListenerPlugin(PlayerHaterListener)}
	 * @param listener
	 *            {@link PlayerHaterListener} That this plugin wraps.
	 * @param echo
	 *            A flag to determine whether the most recent callback that
	 *            would have been called should be called as soon as this plugin
	 *            is attached.
	 */
	public PlayerHaterListenerPlugin(PlayerHaterListener listener, boolean echo) {
		mListener = listener;
		mEcho = echo;
	}

	@Override
	public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
		super.onPlayerHaterLoaded(context, playerHater);
		mSong = playerHater.nowPlaying();
		if (mEcho) {
			onChangesComplete();
		}
	}

	@Override
	public void onSongChanged(Song song) {
		mSong = song;
	}

	@Override
	public void onChangesComplete() {
		switch (getPlayerHater().getState()) {
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
