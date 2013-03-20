package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.service.IPlayerHaterBinder;
import org.prx.android.playerhater.util.BroadcastReceiver;
import org.prx.android.playerhater.util.IPlayerHater;
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
	private Context mContext;

	public AudioFocusPlugin() {

	}

	@Override
	public void onServiceStarted(Context context, IPlayerHater playerHater) {
		mContext = context;
		mAudioFocusChangeListener = new OnAudioFocusChangeListener(
				(IPlayerHaterBinder) playerHater);
		mAudioService = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	public void onPlaybackStarted() {
		mAudioService.requestAudioFocus(mAudioFocusChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		mAudioService.registerMediaButtonEventReceiver(getEventReceiver());
	}

	@Override
	public void onPlaybackStopped() {
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
