package org.prx.android.playerhater.plugins;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.MediaPlayerWrapper;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.service.IPlayerHaterBinder;
import org.prx.android.playerhater.util.Log;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

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

	private static final List<WeakReference<PlayerHaterListenerPlugin>> sInstances = new ArrayList<WeakReference<PlayerHaterListenerPlugin>>();

	private static final int ADD = 1;
	private static final int REM = 2;
	private static final int TCK = 3;
	
	private synchronized static void addInstance(PlayerHaterListenerPlugin plugin) {
		WeakReference<PlayerHaterListenerPlugin> ref = new WeakReference<PlayerHaterListenerPlugin>(
				plugin);
		sHandler.obtainMessage(ADD, ref).sendToTarget();
	}

	private static final Handler sHandler = new Handler() {

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case ADD:
				sInstances.add((WeakReference<PlayerHaterListenerPlugin>) msg.obj);
				break;
			case REM:
				sInstances.remove(msg.obj);
				break;
			case TCK:
				for (WeakReference<PlayerHaterListenerPlugin> ref : sInstances) {
					if (ref.get() != null) {
						ref.get().onTick();
					} else {
						obtainMessage(REM, ref).sendToTarget();
					}
				}
			}
		}

	};

	private static final Thread sClockThread = new Thread() {

		@Override
		public void run() {
			while (true) {
				sHandler.removeMessages(TCK);
				sHandler.sendEmptyMessage(TCK);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					return;
				}
			}
		}

	};

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
		addInstance(this);
		if (!sClockThread.isAlive()) {
			sClockThread.start();
		}
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
	public void onServiceBound(IPlayerHaterBinder binder) {
		super.onServiceBound(binder);
		mSong = getPlayerHater().nowPlaying();
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
		Log.d(MediaPlayerWrapper.getStateName(getPlayerHater().getState()));
		switch (getPlayerHater().getState()) {
		case Player.STARTED:
			mListener.onPlaying(mSong, getPlayerHater().getCurrentPosition());
			break;
		case Player.PREPARING:
		case Player.LOADING_CONTENT:
		case Player.PREPARING_CONTENT:
			mListener.onLoading(mSong);
			break;
		default:
			if (mSong != null) {
				mListener.onPaused(mSong);
			} else {
				mListener.onStopped();
			}
		}
	}
	
	private void onTick() {
		if (getPlayerHater().getState() == Player.STARTED) {
			mListener.onPlaying(getPlayerHater().nowPlaying(), getPlayerHater().getCurrentPosition());
		}
	}
}
