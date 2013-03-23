package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.service.PlayerHaterServiceBinder;
import org.prx.android.playerhater.util.BroadcastReceiver;
import org.prx.android.playerhater.util.OnAudioFocusChangeListener;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.FROYO)
public class AudioFocusPlugin extends AbstractPlugin {
	private AudioManager mAudioService;
	private OnAudioFocusChangeListener mAudioFocusChangeListener;
	private ComponentName mEventReceiver;

	public AudioFocusPlugin() {

	}

	@Override
	public void onServiceBound(PlayerHaterServiceBinder binder) {
		super.onServiceBound(binder);
		mAudioFocusChangeListener = new OnAudioFocusChangeListener(binder);
	}

	@Override
	public void onAudioStarted() {
		getAudioManager().requestAudioFocus(mAudioFocusChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		getAudioManager().registerMediaButtonEventReceiver(getEventReceiver());
	}

	@Override
	public void onAudioStopped() {
		getAudioManager().abandonAudioFocus(mAudioFocusChangeListener);
		getAudioManager()
				.unregisterMediaButtonEventReceiver(getEventReceiver());
	}

	protected AudioManager getAudioManager() {
		if (mAudioService == null) {
			mAudioService = (AudioManager) getContext().getSystemService(
					Context.AUDIO_SERVICE);
		}

		return mAudioService;
	}

	protected ComponentName getEventReceiver() {
		if (mEventReceiver == null) {
			mEventReceiver = new ComponentName(getContext(),
					BroadcastReceiver.class);
		}
		return mEventReceiver;
	}
}
