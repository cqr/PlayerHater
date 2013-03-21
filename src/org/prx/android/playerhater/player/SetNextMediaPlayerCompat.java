package org.prx.android.playerhater.player;

import org.prx.android.playerhater.PlayerHater;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.util.Log;
import static org.prx.android.playerhater.player.Synchronous.synchronous;

public interface SetNextMediaPlayerCompat extends OnCompletionListener {

	public class Compat implements SetNextMediaPlayerCompat {
		private MediaPlayerWithState mNextMediaPlayer;
		private final MediaPlayerWithState mStateManager;
		private OnCompletionListener mOnCompletionListener;

		public Compat(MediaPlayerWithState stateManager) {
			mStateManager = stateManager;
			stateManager.setOnCompletionListener(this);
		}

		@Override
		public void setNextMediaPlayer(MediaPlayerWithState next) {
			mNextMediaPlayer = next;
			if (mNextMediaPlayer != null) {
				switch (mNextMediaPlayer.getState()) {
				case Player.STARTED:
				case Player.PAUSED:
				case Player.PLAYBACK_COMPLETED:
					mNextMediaPlayer.stop();
				case Player.STOPPED:
				case Player.INITIALIZED:
					mNextMediaPlayer.prepareAsync();
					break;
				}
			}
		}

		@Override
		public void skip() {
			onCompletion(mStateManager.getBarePlayer());
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mNextMediaPlayer != null) {
				mStateManager.swap(mNextMediaPlayer);
				mNextMediaPlayer.reset();
				synchronous(mStateManager).conditionalPlay();
			} else {
				mStateManager.reset();
			}
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mp);
			}
		}

		@Override
		public void setOnCompletionListener(OnCompletionListener onCompletion) {
			mOnCompletionListener = onCompletion;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public class Modern implements SetNextMediaPlayerCompat {
		private static final String TAG = PlayerHater.TAG;
		private final MediaPlayerWithState mMediaPlayer;
		private MediaPlayerWithState mNextMediaPlayer;
		private OnCompletionListener mOnComplete;

		public Modern(MediaPlayerWithState stateManager) {
			stateManager.setOnCompletionListener(this);
			mMediaPlayer = stateManager;
		}

		@Override
		public void setNextMediaPlayer(MediaPlayerWithState next) {
			mNextMediaPlayer = next;
			MediaPlayer tmp = null;
			if (next != null) {
				tmp = mNextMediaPlayer.getBarePlayer();
			}
			if (tmp != null && tmp != mMediaPlayer.getBarePlayer()) {
				mMediaPlayer.getBarePlayer().setNextMediaPlayer(tmp);
			}
		}

		@Override
		public void setOnCompletionListener(OnCompletionListener onCompletion) {
			mOnComplete = onCompletion;
		}

		@Override
		public void skip() {
			if (mNextMediaPlayer == null) {
				mMediaPlayer.reset();
			}
			onCompletion(mMediaPlayer.getBarePlayer(), true);
		}
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			onCompletion(mp, false);
		}
		
		private void onCompletion(MediaPlayer mp, boolean start) {
			if (mNextMediaPlayer != null) {
				mMediaPlayer.swap(mNextMediaPlayer);
				mNextMediaPlayer.reset();
				if (start) {
					synchronous(mMediaPlayer).start();
				}
			}
			if (mOnComplete != null) {
				mOnComplete.onCompletion(mp);
			}
		}

	}

	public void setNextMediaPlayer(MediaPlayerWithState mediaPlayer);

	public void skip();

	public void setOnCompletionListener(OnCompletionListener onCompletion);
}
