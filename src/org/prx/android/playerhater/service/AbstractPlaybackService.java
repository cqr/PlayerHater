package org.prx.android.playerhater.service;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.IPlayer;
import org.prx.android.playerhater.player.IPlayer.Player;
import org.prx.android.playerhater.player.IPlayer.StateManager;
import org.prx.android.playerhater.player.MediaPlayerWrapper;
import org.prx.android.playerhater.plugins.AudioFocusPlugin;
import org.prx.android.playerhater.plugins.ExpandableNotificationPlugin;
import org.prx.android.playerhater.plugins.NotificationPlugin;
import org.prx.android.playerhater.plugins.PluginCollection;
import org.prx.android.playerhater.plugins.LockScreenControlsPlugin;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.plugins.TouchableNotificationPlugin;
import org.prx.android.playerhater.util.BroadcastReceiver;
import org.prx.android.playerhater.util.PlayerListenerManager;
import org.prx.android.playerhater.util.UpdateProgressRunnable;

import android.app.Activity;
import android.app.PendingIntent;
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
	protected PlayerHaterPlugin mLifecycleListener;
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
	private NotificationPlugin mNotificationPlugin;

	protected abstract StateManager getMediaPlayer();

	@Override
	public void onCreate() {
		mBroadcastReceiver = new BroadcastReceiver(this);

		PluginCollection collection = new PluginCollection();

		if (PlayerHater.EXPANDING_NOTIFICATIONS) {
			mNotificationPlugin = new ExpandableNotificationPlugin(this);
		} else if (PlayerHater.TOUCHABLE_NOTIFICATIONS) {
			mNotificationPlugin = new TouchableNotificationPlugin(this);
		} else {
			mNotificationPlugin = new NotificationPlugin(this);
		}
		collection.add(mNotificationPlugin);

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
		release();
		getBaseContext().unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new PlayerHaterBinder(this);
	}

	@Override
	public boolean isPlaying() {
		return (getMediaPlayer().getState() == IPlayer.STARTED);
	}

	@Override
	public boolean isPaused() {
		return (getMediaPlayer().getState() == IPlayer.PAUSED);
	}

	@Override
	public boolean isLoading() {
		StateManager mp = getMediaPlayer();
		return (mp.getState() == IPlayer.INITIALIZED
				|| mp.getState() == IPlayer.PREPARING || mp.getState() == IPlayer.PREPARED);
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
		mPlayAfterSeek = (mPlayAfterSeek || getState() == IPlayer.STARTED);

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
			mPlayAfterSeek = false;
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
		mLifecycleListener.onSongChanged(getNowPlaying());
		mLifecycleListener.onDurationChanged(getDuration());
		mLifecycleListener.onPlaybackStarted();
		sendIsPlaying(getCurrentPosition());
	}

	protected void sendIsPlaying(int progress) {
		if (getState() == Player.STARTED && mPlayerHaterListener != null) {
			mPlayerHaterListener.onPlaying(getNowPlaying(), progress);
		}
	}

	protected void sendIsLoading() {
		Log.d(TAG, "SENDING IS LOADING " + mLifecycleListener + " "
				+ getNowPlaying());
		mLifecycleListener.onSongChanged(getNowPlaying());
		mLifecycleListener.onAudioLoading();
		if (mPlayerHaterListener != null) {
			mPlayerHaterListener.onLoading(getNowPlaying());
		}
	}

	protected void sendIsPaused() {
		Log.d(TAG, "SENDING IS PAUSED");
		mLifecycleListener.onPlaybackPaused();
		if (mPlayerHaterListener != null) {
			mPlayerHaterListener.onPaused(getNowPlaying());
		}
	}

	protected void sendIsStopped() {
		Log.d(TAG, "SENDING IS STOPPED");
		mLifecycleListener.onPlaybackStopped();
		if (mPlayerHaterListener != null) {
			mPlayerHaterListener.onStopped();
		}
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
		case IPlayer.INITIALIZED:
		case IPlayer.STOPPED:
			prepare();
			break;
		case IPlayer.PREPARED:
		case IPlayer.PAUSED:
			resume();
			startProgressThread(getMediaPlayer());
			break;
		case IPlayer.IDLE:
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
	protected StateManager buildMediaPlayer() {
		return new MediaPlayerWrapper();
	}

	protected StateManager buildMediaPlayer(boolean setAsCurrent) {
		StateManager mp = buildMediaPlayer();
		if (setAsCurrent) {
			setCurrentMediaPlayer(mp);
		}
		return mp;
	}

	protected void setCurrentMediaPlayer(StateManager player) {
		mPlayerListenerManager.setMediaPlayer(player);
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

	protected void startProgressThread(StateManager mp) {
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
			}
		}
	}

	@Override
	public void onRemoteControlButtonPressed(int button) {
		switch (button) {
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			if (isPlaying()) {
				pause();
			} else {
				play();
			}
			break;
		case KeyEvent.KEYCODE_MEDIA_PLAY:
			play();
			break;
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
			pause();
			break;
		case KeyEvent.KEYCODE_MEDIA_STOP:
			stop();
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			seekTo(0);
			break;
		}
	}

	@Override
	public void setOnShutdownRequestListener(OnShutdownRequestListener listener) {
		mShutdownRequestListener = listener;
	}

	protected void release() {
		getMediaPlayer().release();
	}

	@Override
	public void setIntentClass(Class<? extends Activity> klass) {
		Intent intent = new Intent(getBaseContext(), klass);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pending = PendingIntent.getActivity(getBaseContext(),
				456, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		mNotificationPlugin.onIntentActivityChanged(pending);
	}

}
