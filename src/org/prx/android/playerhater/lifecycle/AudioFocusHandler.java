package org.prx.android.playerhater.lifecycle;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.PlayerHaterService;
import org.prx.android.playerhater.util.OnAudioFocusChangeListener;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.FROYO)
public class AudioFocusHandler implements LifecycleListener {
	private final AudioManager mAudioService;
	private final OnAudioFocusChangeListener mAudioFocusChangeListener;

	public AudioFocusHandler(PlayerHaterService context) {
		mAudioFocusChangeListener = new OnAudioFocusChangeListener(context);
		mAudioService = (AudioManager) context.getBaseContext()
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

	@Override
	public void setIsLoading(Song forSong) {
		// TODO Auto-generated method stub
		
	}

}
