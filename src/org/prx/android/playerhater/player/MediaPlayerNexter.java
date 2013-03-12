package org.prx.android.playerhater.player;

import org.prx.android.playerhater.player.IPlayer.Player;
import org.prx.android.playerhater.player.IPlayer.StateManager;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Handler;

public interface MediaPlayerNexter {

	public class Compat implements MediaPlayerNexter, OnCompletionListener,
			Runnable {
		private StateManager mNextMediaPlayer;
		private final Handler mHandler = new Handler();
		private final StateManager mStateManager;
		private OnCompletionListener mOnCompletionListener;
		private MediaPlayer mPlayer;

		public Compat(StateManager stateManager) {
			mStateManager = stateManager;
			stateManager.setOnCompletionListener(this);
		}

		@Override
		public void setNextMediaPlayer(StateManager next) {
			mNextMediaPlayer = next;
			if (mNextMediaPlayer != null) {
				switch (mNextMediaPlayer.getState()) {
				case IPlayer.STARTED:
				case IPlayer.PAUSED:
				case IPlayer.PLAYBACK_COMPLETED:
					mNextMediaPlayer.stop();
				case IPlayer.STOPPED:
				case IPlayer.INITIALIZED:
					mNextMediaPlayer.prepareAsync();
					break;
				}
			}
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mNextMediaPlayer != null) {
				if (mNextMediaPlayer.getState() == Player.PREPARED) {
					mStateManager.swap(mNextMediaPlayer);
					if (mOnCompletionListener != null) {
						mOnCompletionListener.onCompletion(mp);
					}
					mPlayer = null;
				} else {
					mPlayer = mp;
					mHandler.postDelayed(this, 200);
				}
			}
		}

		@Override
		public void run() {
			onCompletion(mPlayer);
		}

		@Override
		public void setOnCompletionListener(OnCompletionListener onCompletion) {
			mOnCompletionListener = onCompletion;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public class Modern implements MediaPlayerNexter, OnCompletionListener {
		private final StateManager mMediaPlayer;
		private StateManager mNextMediaPlayer;
		private OnCompletionListener mOnComplete;

		public Modern(StateManager stateManager) {
			stateManager.setOnCompletionListener(this);
			mMediaPlayer = stateManager;
		}

		@Override
		public void setNextMediaPlayer(StateManager next) {
			StateManager was = mNextMediaPlayer;
			mNextMediaPlayer = next;
			MediaPlayer tmp = null;
			if (next != null) {
				tmp = mNextMediaPlayer.getBarePlayer();
			}
			if (tmp != null || was != null) {
				mMediaPlayer.getBarePlayer().setNextMediaPlayer(tmp);
			}
		}

		@Override
		public void setOnCompletionListener(OnCompletionListener onCompletion) {
			mOnComplete = onCompletion;
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mNextMediaPlayer != null)
				mMediaPlayer.swap(mNextMediaPlayer);
			if (mOnComplete != null) {
				mOnComplete.onCompletion(mp);
			}
		}

	}

	public void setNextMediaPlayer(StateManager mediaPlayer);

	public void setOnCompletionListener(OnCompletionListener onCompletion);
}
