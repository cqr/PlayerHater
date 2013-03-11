package org.prx.android.playerhater.player;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class NextableMediaPlayer extends MediaPlayerDecorator implements
		OnCompletionListener {

	private final MediaPlayerNexter mMediaPlayerNexter;
	private OnCompletionListener mOnCompletionListener;

	public NextableMediaPlayer(IPlayer player) {
		super(player);
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			mMediaPlayerNexter = new MediaPlayerNexter.Modern(getBarePlayer());
		} else {
			mMediaPlayerNexter = new MediaPlayerNexter.Compat();
			super.setOnCompletionListener(this);
		}
	}

	@Override
	public void setNextMediaPlayer(IPlayer mediaPlayer) {
		mMediaPlayerNexter.setNextMediaPlayer(mediaPlayer);
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener onCompletion) {
		if (mMediaPlayerNexter instanceof MediaPlayerNexter.Compat) {
			mOnCompletionListener = onCompletion;
		} else {
			super.setOnCompletionListener(onCompletion);
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mMediaPlayerNexter instanceof MediaPlayerNexter.Compat) {
			((MediaPlayerNexter.Compat) mMediaPlayerNexter).onCompletion(mp);
		}
		if (mOnCompletionListener != null)
			mOnCompletionListener.onCompletion(mp);
	}

}
