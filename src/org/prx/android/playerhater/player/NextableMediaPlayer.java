package org.prx.android.playerhater.player;

import android.media.MediaPlayer.OnCompletionListener;

public class NextableMediaPlayer extends MediaPlayerDecorator {

	private final MediaPlayerNexter mMediaPlayerNexter;

	public NextableMediaPlayer(StateManager stateManager) {
		super(stateManager);

		if (android.os.Build.VERSION.SDK_INT >= 16) {
			mMediaPlayerNexter = new MediaPlayerNexter.Modern(stateManager);
		} else {
			mMediaPlayerNexter = new MediaPlayerNexter.Compat(stateManager);
		}
	}

	@Override
	public void setNextMediaPlayer(StateManager mediaPlayer) {
		mMediaPlayerNexter.setNextMediaPlayer(mediaPlayer);
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener onCompletion) {
		mMediaPlayerNexter.setOnCompletionListener(onCompletion);
	}
}
