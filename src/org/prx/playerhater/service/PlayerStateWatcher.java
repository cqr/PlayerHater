package org.prx.playerhater.service;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.mediaplayer.Player;
import org.prx.playerhater.mediaplayer.Player.StateChangeListener;
import org.prx.playerhater.mediaplayer.StatelyPlayer;

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
	public synchronized void onStateChanged(Player mediaPlayer, int state) {
		boolean willPlay = StatelyPlayer.willPlay(state);
		state = StatelyPlayer.mediaPlayerState(state);
		
		switch (state) {
		case StatelyPlayer.STOPPED:
		case StatelyPlayer.END:
		case StatelyPlayer.ERROR:
		case StatelyPlayer.IDLE:
		case StatelyPlayer.INITIALIZED:
		case StatelyPlayer.PLAYBACK_COMPLETED:
		case StatelyPlayer.PREPARING:
		case StatelyPlayer.PREPARED:
			if (willPlay) {
				setCurrentState(PlayerHater.STATE_LOADING);
			} else {
				setCurrentState(PlayerHater.STATE_IDLE);
			}
			break;
		case StatelyPlayer.PAUSED:
			setCurrentState(PlayerHater.STATE_PAUSED);
			break;
		case StatelyPlayer.STARTED:
			setCurrentState(PlayerHater.STATE_PLAYING);
			break;
		default:
			throw new IllegalStateException("Illegal State: " + state);
		}
	}
	
	public synchronized int getCurrentState() {
		return mCurrentState;
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
