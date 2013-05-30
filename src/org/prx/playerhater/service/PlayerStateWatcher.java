package org.prx.playerhater.service;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.mediaplayer.Player;
import org.prx.playerhater.mediaplayer.Player.StateChangeListener;

public class PlayerStateWatcher implements StateChangeListener {
	private int mCurrentState = PlayerHater.STATE_IDLE;
	private PlayerHaterStateListener mListener;
	private Player mMediaPlayer;
	
	public PlayerStateWatcher() {
		this(null);
	}
	
	public PlayerStateWatcher(PlayerHaterStateListener listener) {
		setListener(listener);
	}

	public interface PlayerHaterStateListener {
		void onStateChanged(int state);
	}

	public void setMediaPlayer(Player player) {
		if (mMediaPlayer != null) {
			mMediaPlayer.setStateChangeListener(null);
		}
		mMediaPlayer = player;
		mMediaPlayer.setStateChangeListener(this);
		onStateChanged(mMediaPlayer, mMediaPlayer.getState());
	}

	public void setListener(PlayerHaterStateListener listener) {
		mListener = listener;
		notifyState();
	}

	@Override
	public void onStateChanged(Player mediaPlayer, int state) {
		switch (state) {
		case Player.STOPPED:
		case Player.END:
		case Player.ERROR:
		case Player.IDLE:
		case Player.INITIALIZED:
		case Player.PLAYBACK_COMPLETED:
			setCurrentState(PlayerHater.STATE_IDLE);
			break;
		case Player.LOADING_CONTENT:
		case Player.PREPARING:
		case Player.PREPARED:
		case Player.PREPARING_CONTENT:
			if (mediaPlayer.isWaitingToPlay()) {
				setCurrentState(PlayerHater.STATE_LOADING);
			} else {
				setCurrentState(PlayerHater.STATE_IDLE);
			}
			break;
		case Player.PAUSED:
			setCurrentState(PlayerHater.STATE_PAUSED);
			break;
		case Player.STARTED:
			setCurrentState(PlayerHater.STATE_PLAYING);
			break;
		default:
			throw new IllegalStateException("Illegal State: " + state);
		}
	}

	private void setCurrentState(int currentState) {
		if (currentState != mCurrentState) {
			mCurrentState = currentState;
			notifyState();
		}
	}
	
	private void notifyState() {
		if (mListener != null) {
			mListener.onStateChanged(mCurrentState);
		}
	}
}
