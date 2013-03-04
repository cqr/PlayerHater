package org.prx.android.playerhater.service;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.AudioFocusPlugin;
import org.prx.android.playerhater.plugins.ExpandableNotificationPlugin;
import org.prx.android.playerhater.plugins.PluginCollection;
import org.prx.android.playerhater.plugins.LockScreenControlsPlugin;
import org.prx.android.playerhater.plugins.PlayerHaterPluginInterface;
import org.prx.android.playerhater.plugins.TouchableNotificationPlugin;
import org.prx.android.playerhater.util.BroadcastReceiver;
import org.prx.android.playerhater.util.MediaPlayerWrapper;
import org.prx.android.playerhater.util.PlayerListenerManager;
import org.prx.android.playerhater.util.UpdateProgressRunnable;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

public abstract class AbstractPlaybackService extends Service implements
		OnErrorListener, OnPreparedListener, OnSeekCompleteListener,
		OnCompletionListener, PlayerHaterService {

	protected static final String TAG = "PlayerHater/Service";
	protected static final int PROGRESS_UPDATE = 9747244;

	protected BroadcastReceiver mBroadcastReceiver;
	protected PlayerHaterPluginInterface mLifecycleListener;
	protected final PlayerListenerManager mPlayerListenerManager = new PlayerListenerManager();
	private OnCompletionListener mOnCompletionListener;
	private PlayerHaterListener mPlayerHaterListener;
	private OnErrorListener mOnErrorListener;
	private int mStartTime;
	private OnPreparedListener mOnPreparedListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;
	private final Handler mHandler = new UpdateHandler(this);
	private final UpdateProgressRunnable mUpdateProgressRunner = new UpdateProgressRunnable(
			mHandler, PROGRESS_UPDATE);

	private Thread mUpdateProgressThread;
	private boolean mPlayAfterSeek;
	private OnShutdownRequestListener mShutdownRequestListener;

	protected abstract MediaPlayerWrapper getMediaPlayer();

	@Override
	public void onCreate() {
		mBroadcastReceiver = new BroadcastReceiver(this);

		PluginCollection collection = new PluginCollection();

		if (PlayerHater.EXPANDING_NOTIFICATIONS) {
			collection.add(new ExpandableNotificationPlugin(this));
		} else if (PlayerHater.TOUCHABLE_NOTIFICATIONS) {
			collection.add(new TouchableNotificationPlugin(this));
		}
		if (PlayerHater.MODERN_AUDIO_FOCUS) {
			collection.add(new AudioFocusPlugin(this));
		}
		if (PlayerHater.LOCK_SCREEN_CONTROLS) {
			collection.add(new LockScreenControlsPlugin(this));
		}

		mLifecycleListener = collection;

		mPlayerListenerManager.setOnCompletionListener(this);
		mPlayerListenerManager.setOnErrorListener(this);
		mPlayerListenerManager.setOnPreparedListener(this);
		mPlayerListenerManager.setOnSeekCompleteListener(this);
	}
	
	@Override
	public void onDestroy() {
		stopProgressThread();
		sendIsStopped();
		getMediaPlayer().stop();
		getBaseContext().unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new PlayerHaterBinder(this);
	}

	@Override
	public boolean isPlaying() {
		return (getMediaPlayer().getState() == MediaPlayerWrapper.STARTED);
	}

	@Override
	public boolean isPaused() {
		return (getMediaPlayer().getState() == MediaPlayerWrapper.PAUSED);
	}

	@Override
	public boolean isLoading() {
		MediaPlayerWrapper mp = getMediaPlayer();
		return (mp.getState() == MediaPlayerWrapper.INITIALIZED
				|| mp.getState() == MediaPlayerWrapper.PREPARING || mp
					.getState() == MediaPlayerWrapper.PREPARED);
	}

	@Override
	public int getState() {
		return getMediaPlayer().getState();
	}

	@Override
	public int getDuration() {
		return getMediaPlayer().getDuration();
	}

	@Override
	public int getCurrentPosition() {
		return getMediaPlayer().getCurrentPosition();
	}

	@Override
	public void duck() {
		getMediaPlayer().setVolume(0.1f, 0.1f);
	}

	@Override
	public void unduck() {
		getMediaPlayer().setVolume(1.0f, 1.0f);
	}

	protected void resume() {
		Log.d(TAG, "GOT RESUME()");
		getMediaPlayer().start();
	}

	// PLAY AND PAUSE

	@Override
	public boolean pause() {
		Log.d(TAG, "GOT PAUSE()");
		try {
			getMediaPlayer().pause();
			sendIsPaused();
			stopProgressThread();
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}

	@Override
	public boolean play(Song song) throws IllegalArgumentException {
		return play(song, 0);
	}

	@Override
	public boolean play(int startTime) throws IllegalStateException {
		mStartTime = startTime * 1000;
		return play();
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "STOPPING");
		mShutdownRequestListener.onShutdownRequested();
		stopSelf();
		return true;
	}

	@Override
	public void seekTo(int pos) {
		Log.d(TAG, "SEEKING TO " + pos);
		mPlayAfterSeek = (getState() == MediaPlayerWrapper.STARTED);

		try {
			getMediaPlayer().pause();
			sendIsLoading();
			getMediaPlayer().seekTo(pos);
		} catch (java.lang.IllegalStateException e) {
			// do nothing
		}
	}

	// COMPLETION
	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "GOT ON COMPLETION");
		stopProgressThread();
		sendIsStopped();
		if (mOnCompletionListener != null) {
			mOnCompletionListener.onCompletion(mp);
		}
		stop();
	}

	// SEEK COMPLETE

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mOnSeekCompleteListener = listener;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.d(TAG, "GOT SEEK COMPLETE");
		if (mPlayAfterSeek) {
			try {
				play();
			} catch (Exception e) {
				// oof.
			}
		}
		if (mOnSeekCompleteListener != null)
			mOnSeekCompleteListener.onSeekComplete(mp);
	}

	// ERROR
	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		mOnErrorListener = listener;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d(TAG, "GOT ON ERROR: " + what + " / " + extra);
		if (mOnErrorListener != null) {
			return mOnErrorListener.onError(mp, what, extra);
		}
		return false;
	}

	// PREPARED

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		mOnPreparedListener = listener;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "GOT ON PREPARED");
		if (mStartTime > 0) {
			int seekTo = mStartTime;
			mStartTime = 0;
			seekTo(seekTo);
		}
		if (getMediaPlayer().equals(mp)) {
			getMediaPlayer().start();
			sendStartedPlaying();
		}
		if (mOnPreparedListener != null) {
			mOnPreparedListener.onPrepared(mp);
		}
	}

	// NON-PROXIED

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		mPlayerListenerManager.setOnInfoListener(listener);
	}

	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mPlayerListenerManager.setOnBufferingUpdateListener(listener);
	}

	// PlayerHaterListener

	@Override
	public void setListener(PlayerHaterListener listener) {
		mPlayerHaterListener = listener;
	}

	/*
	 * These are the events we send back to PlayerHaterListener;
	 */
	protected void sendStartedPlaying() {
		Log.d(TAG, "SENDING START PLAY");
		mLifecycleListener.onPlaybackStarted(getNowPlaying(), getDuration());
		sendIsPlaying(getCurrentPosition());
	}

	protected void sendIsPlaying(int progress) {
		Log.d(TAG, "SENDING IS PLAYING");
		if (getState() == MediaPlayerWrapper.STARTED
				&& mPlayerHaterListener != null) {
			mPlayerHaterListener.onPlaying(getNowPlaying(), progress);
		}
	}

	protected void sendIsLoading() {
		Log.d(TAG, "SENDING IS LOADING");
		mLifecycleListener.onLoading(getNowPlaying());
		if (mPlayerHaterListener != null) {
			mPlayerHaterListener.onLoading(getNowPlaying());
		}
	}

	protected void sendIsPaused() {
		Log.d(TAG, "SENDING IS PAUSED");
		mLifecycleListener.setIsPlaying(false);
		if (mPlayerHaterListener != null) {
			mPlayerHaterListener.onPaused(getNowPlaying());
		}
	}

	protected void sendIsStopped() {
		Log.d(TAG, "SENDING IS STOPPED");
		mLifecycleListener.onStop();
		if (mPlayerHaterListener != null) {
			mPlayerHaterListener.onStopped();
		}
	}

	protected void sendCanSkipForward(boolean canSkip) {
		mLifecycleListener.setCanSkipForward(canSkip);
	}

	@Override
	public void setTitle(String title) {
		mLifecycleListener.onTitleChanged(title);
	}

	@Override
	public void setArtist(String artist) {
		mLifecycleListener.onArtistChanged(artist);
	}

	@Override
	public void setAlbumArt(int resourceId) {
		mLifecycleListener.onAlbumArtChanged(resourceId);
	}

	@Override
	public void setAlbumArt(Uri url) {
		mLifecycleListener.onAlbumArtChangedToUri(url);
	}

	@Override
	public boolean play() throws IllegalStateException {
		Log.d(TAG, "GOT PLAY()");
		switch (getMediaPlayer().getState()) {
		case MediaPlayerWrapper.INITIALIZED:
		case MediaPlayerWrapper.STOPPED:
			prepare();
			break;
		case MediaPlayerWrapper.PREPARED:
		case MediaPlayerWrapper.PAUSED:
			resume();
			startProgressThread(getMediaPlayer());
			sendStartedPlaying();
			break;
		case MediaPlayerWrapper.IDLE:
			play(getNowPlaying());
			break;
		default:
			throw new IllegalStateException("State is "
					+ getMediaPlayer().getState());
		}
		return true;
	}

	/*
	 * creates a media player (wrapped, of course) and registers the listeners
	 * for all of the events.
	 */
	protected MediaPlayerWrapper buildMediaPlayer() {
		return buildMediaPlayer(false);
	}

	protected MediaPlayerWrapper buildMediaPlayer(boolean setAsCurrent) {
		MediaPlayerWrapper mp = new MediaPlayerWrapper();
		mPlayerListenerManager.setMediaPlayer(mp);
		return mp;
	}

	// PROTECTED STUFF

	protected void stopProgressThread() {
		Log.d(TAG, "STOPPING PROGRESS THREAD");
		if (mUpdateProgressThread != null && mUpdateProgressThread.isAlive()) {
			mHandler.removeCallbacks(mUpdateProgressRunner);
			mUpdateProgressThread.interrupt();
			mUpdateProgressThread = null;
		}
	}

	protected void startProgressThread(MediaPlayerWrapper mp) {
		Log.d(TAG, "STARTING PROGRESS THREAD");
		stopProgressThread();
		mUpdateProgressRunner.setMediaPlayer(mp);
		mUpdateProgressThread = new Thread(mUpdateProgressRunner);
		mUpdateProgressThread.start();
	}

	protected void prepare() {
		Log.d(TAG, "PREPARING");
		sendIsLoading();
		getMediaPlayer().prepareAsync();
		startProgressThread(getMediaPlayer());
	}

	/*
	 * This should be overridden by subclasses which wish to handle messages
	 * sent to mHandler without re-implementing the handler. It is a noop by
	 * default.
	 */
	protected void onHandlerMessage(Message m) { /* noop */
	}

	private static class UpdateHandler extends Handler {
		private AbstractPlaybackService mService;

		private UpdateHandler(AbstractPlaybackService playbackService) {
			mService = playbackService;
		}

		@Override
		public void handleMessage(Message m) {
			switch (m.what) {
			case PROGRESS_UPDATE:
				mService.sendIsPlaying(m.arg1);
				break;
			default:
				mService.onHandlerMessage(m);
			}
		}
	}

	@Override
	public void onRemoteControlButtonPressed(int button) {
		switch (button) {
		case KeyEvent.KEYCODE_MEDIA_PLAY:
			play();
			break;
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
			pause();
			break;
		case KeyEvent.KEYCODE_MEDIA_STOP:
			stop();
			break;
		}
	}
	
	@Override
	public void setOnShutdownRequestListener(OnShutdownRequestListener listener) {
		mShutdownRequestListener = listener;
	}

}
