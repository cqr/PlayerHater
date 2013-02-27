package org.prx.android.playerhater.lifecycle;

import org.prx.android.playerhater.OnAudioFocusChangeListener;
import org.prx.android.playerhater.PlaybackService;
import org.prx.android.playerhater.Song;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.FROYO)
public class AudioFocusHandler implements LifecycleListener {
	private final AudioManager mAudioService;
	private final OnAudioFocusChangeListener mAudioFocusChangeListener;

	public AudioFocusHandler(PlaybackService context) {
		mAudioFocusChangeListener = new OnAudioFocusChangeListener(context);
		mAudioService = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	public void setIsPlaying(boolean isPlaying) {
		if (isPlaying) {
			start(null, 0);
		}
	}

	@Override
	public void start(Song forSong, int duration) {
		mAudioService.requestAudioFocus(mAudioFocusChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	}

	@Override
	public void stop() {
		mAudioService.abandonAudioFocus(mAudioFocusChangeListener);
	}

}
