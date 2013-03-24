package org.prx.android.playerhater.player;

import android.media.MediaPlayer.OnCompletionListener;

public class Gapless extends MediaPlayerDecorator {
	
	public static final Gapless gapless(MediaPlayerWithState mediaPlayer) {
		return new Gapless(mediaPlayer);
	}

	private final SetNextMediaPlayerCompat mMediaPlayerNexter;

	public Gapless(MediaPlayerWithState stateManager) {
		super(stateManager);

		if (android.os.Build.VERSION.SDK_INT >= 16) {
			mMediaPlayerNexter = new SetNextMediaPlayerCompat.Modern(stateManager);
		} else {
			mMediaPlayerNexter = new SetNextMediaPlayerCompat.Compat(stateManager);
		}
	}

	@Override
	public void setNextMediaPlayer(MediaPlayerWithState mediaPlayer) {
		mMediaPlayerNexter.setNextMediaPlayer(mediaPlayer);
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener onCompletion) {
		mMediaPlayerNexter.setOnCompletionListener(onCompletion);
	}
	
	@Override
	public void skip() {
		mMediaPlayerNexter.skip();
	}
}
