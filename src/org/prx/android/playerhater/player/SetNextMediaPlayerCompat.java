package org.prx.android.playerhater.player;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;

public interface SetNextMediaPlayerCompat extends OnCompletionListener {

	public class Compat implements SetNextMediaPlayerCompat {
		private Player mNextMediaPlayer;
		private final Player mStateManager;
		private OnCompletionListener mOnCompletionListener;

		public Compat(Player stateManager) {
			mStateManager = stateManager;
			stateManager.setOnCompletionListener(this);
		}

		@Override
		public void setNextMediaPlayer(Player next) {
			mNextMediaPlayer = next;
		}

		@Override
		public void skip() {
			mStateManager.stop();
			onCompletion(mStateManager.getBarePlayer());
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mNextMediaPlayer != null) {
				mNextMediaPlayer.conditionalPlay();
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
		private final MediaPlayerWithState mMediaPlayer;
		private MediaPlayerWithState mNextMediaPlayer;
		private OnCompletionListener mOnComplete;

		public Modern(MediaPlayerWithState stateManager) {
			stateManager.setOnCompletionListener(this);
			mMediaPlayer = stateManager;
		}

		@Override
		public void setNextMediaPlayer(Player next) {
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
					((Player) mMediaPlayer).start();
				}
			}
			if (mOnComplete != null) {
				mOnComplete.onCompletion(mp);
			}
		}

	}

	void setNextMediaPlayer(Player next);

	public void skip();

	public void setOnCompletionListener(OnCompletionListener onCompletion);
}
