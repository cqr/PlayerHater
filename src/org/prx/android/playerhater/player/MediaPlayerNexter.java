package org.prx.android.playerhater.player;

import org.prx.android.playerhater.player.IPlayer.StateManager;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Handler;

public interface MediaPlayerNexter {
	public class Compat implements MediaPlayerNexter, OnCompletionListener, Runnable {
		private StateManager mNextMediaPlayer;
		private final Handler mHandler = new Handler();

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
				if (mNextMediaPlayer.getState() == IPlayer.PREPARED) {
					mNextMediaPlayer.start();
				} else {
					mHandler.postDelayed(this, 200);
				}
			}
		}

		@Override
		public void run() {
			onCompletion(null);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public class Modern implements MediaPlayerNexter {
		private final MediaPlayer mMediaPlayer;

		public Modern(MediaPlayer mediaPlayer) {
			mMediaPlayer = mediaPlayer;
		}

		@Override
		public void setNextMediaPlayer(StateManager next) {
			mMediaPlayer.setNextMediaPlayer(next.getBarePlayer());
		}

	}

	public void setNextMediaPlayer(StateManager mediaPlayer);
}
