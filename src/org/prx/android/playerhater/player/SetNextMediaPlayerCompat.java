package org.prx.android.playerhater.player;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
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
		public void skip(boolean autoPlay) {
			onCompletion(mStateManager.getBarePlayer(), autoPlay);
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			onCompletion(mp, true);
		}
		
		private void onCompletion(MediaPlayer mp, boolean autoPlay) {
			if (mNextMediaPlayer != null) {
				mStateManager.swap(mNextMediaPlayer);
				mNextMediaPlayer.reset();
				if (autoPlay) {
					synchronous(mStateManager).conditionalPlay();
				}
			} else {
				Player tmp = synchronous(mStateManager);
				tmp.conditionalPause();
				tmp.seekTo(0);
				if (autoPlay) {
					tmp.conditionalPlay();
				}
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
		public void setNextMediaPlayer(MediaPlayerWithState next) {
			mNextMediaPlayer = next;
			MediaPlayer tmp = null;
			if (next != null) {
				tmp = mNextMediaPlayer.getBarePlayer();
			}
			if (tmp != null) {
				mMediaPlayer.getBarePlayer().setNextMediaPlayer(tmp);
			}
		}

		@Override
		public void setOnCompletionListener(OnCompletionListener onCompletion) {
			mOnComplete = onCompletion;
		}

		@Override
		public void skip(boolean autoPlay) {
			onCompletion(mMediaPlayer.getBarePlayer());
			if (autoPlay) {
				synchronous(mMediaPlayer).conditionalPlay();
			}
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mNextMediaPlayer != null) {
				mMediaPlayer.swap(mNextMediaPlayer);
				mNextMediaPlayer.reset();
			} else {
				Player tmp = synchronous(mMediaPlayer);
				tmp.conditionalPause();
				tmp.seekTo(0);
			}
			if (mOnComplete != null) {
				mOnComplete.onCompletion(mp);
			}
		}

	}

	public void setNextMediaPlayer(MediaPlayerWithState mediaPlayer);

	public void skip(boolean autoPlay);

	public void setOnCompletionListener(OnCompletionListener onCompletion);
}
