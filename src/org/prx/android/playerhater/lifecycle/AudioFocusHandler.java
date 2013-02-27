package org.prx.android.playerhater.lifecycle;

import org.prx.android.playerhater.OnAudioFocusChangeListener;
import org.prx.android.playerhater.Song;

import android.content.Context;
import android.media.AudioManager;

public class AudioFocusHandler implements LifecycleListener {

	private final AudioManager mAudioService;
	private final OnAudioFocusChangeListener mAudioFocusChangeListener;

	public AudioFocusHandler(Context context,
			OnAudioFocusChangeListener audioFocusChangeListener) {
		mAudioFocusChangeListener = audioFocusChangeListener;
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
