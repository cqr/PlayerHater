package org.prx.android.playerhater.lifecycle;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.PlayerHaterService;
import org.prx.android.playerhater.util.BroadcastReceiver;
import org.prx.android.playerhater.util.OnAudioFocusChangeListener;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.FROYO)
public class AudioFocusPlugin extends PlayerHaterPlugin {
	private final AudioManager mAudioService;
	private final OnAudioFocusChangeListener mAudioFocusChangeListener;
	private ComponentName mEventReceiver;
	private Context mContext;

	public AudioFocusPlugin(PlayerHaterService context) {
		mContext = context.getBaseContext();
		mAudioFocusChangeListener = new OnAudioFocusChangeListener(context);
		mAudioService = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	public void start(Song forSong, int duration) {
		super.start(forSong, duration);
		mAudioService.requestAudioFocus(mAudioFocusChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		mAudioService.registerMediaButtonEventReceiver(getEventReceiver());
	}

	@Override
	public void stop() {
		mAudioService.abandonAudioFocus(mAudioFocusChangeListener);
		mAudioService.unregisterMediaButtonEventReceiver(getEventReceiver());
	}

	private ComponentName getEventReceiver() {
		if (mEventReceiver == null) {
			mEventReceiver = new ComponentName(mContext,
					BroadcastReceiver.class);
		}
		return mEventReceiver;
	}
}
