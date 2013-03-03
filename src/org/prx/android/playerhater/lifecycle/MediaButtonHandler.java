package org.prx.android.playerhater.lifecycle;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.util.BroadcastReceiver;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.FROYO)
public class MediaButtonHandler implements LifecycleListener {
	private final AudioManager mAudioManager;
	private ComponentName mEventReceiver;
	private Context mContext;

	public MediaButtonHandler(Context context) {
		mContext = context;
		mAudioManager = (AudioManager) context
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
		mAudioManager.registerMediaButtonEventReceiver(getEventReceiver());
	}

	private ComponentName getEventReceiver() {
		if (mEventReceiver == null) {
			mEventReceiver = new ComponentName(mContext,
					BroadcastReceiver.class);
		}
		return mEventReceiver;
	}

	@Override
	public void stop() {
		mAudioManager.unregisterMediaButtonEventReceiver(getEventReceiver());
	}

	@Override
	public void setIsLoading(Song forSong) {
		// TODO Auto-generated method stub
		
	}
}
