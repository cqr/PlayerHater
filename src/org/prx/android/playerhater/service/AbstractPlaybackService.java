package org.prx.android.playerhater.service;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.MediaPlayerWithState;
import org.prx.android.playerhater.player.MediaPlayerWrapper;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.plugins.AudioFocusPlugin;
import org.prx.android.playerhater.plugins.ExpandableNotificationPlugin;
import org.prx.android.playerhater.plugins.NotificationPlugin;
import org.prx.android.playerhater.plugins.PluginCollection;
import org.prx.android.playerhater.plugins.LockScreenControlsPlugin;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.plugins.TouchableNotificationPlugin;
import org.prx.android.playerhater.util.BroadcastReceiver;
import org.prx.android.playerhater.util.PlayerListenerManager;
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
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;

public abstract class AbstractPlaybackService extends Service implements
		OnErrorListener, OnSeekCompleteListener,
		OnCompletionListener, PlayerHaterService {

	protected static final String TAG = "PlayerHater/Service";
	protected static final int PROGRESS_UPDATE = 9747244;

	protected BroadcastReceiver mBroadcastReceiver;
	protected PlayerHaterPlugin mLifecycleListener;
	protected final PlayerListenerManager mPlayerListenerManager = new PlayerListenerManager();
	private OnCompletionListener mOnCompletionListener;
	protected PlayerHaterListener mPlayerHaterListener;
	private OnErrorListener mOnErrorListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;

	private boolean mPlayAfterSeek;
	private OnShutdownRequestListener mShutdownRequestListener;
	private NotificationPlugin mNotificationPlugin;

	protected abstract MediaPlayerWithState getMediaPlayer();

	@Override
	public void onCreate() {
		
		mBroadcastReceiver = new BroadcastReceiver(this, new PlayerHaterBinder(this));

		PluginCollection collection = new PluginCollection();
		
		if (PlayerHater.MODERN_AUDIO_FOCUS) {
			collection.add(new AudioFocusPlugin(this));
		}

		if (PlayerHater.EXPANDING_NOTIFICATIONS) {
			mNotificationPlugin = new ExpandableNotificationPlugin(this);
		} else if (PlayerHater.TOUCHABLE_NOTIFICATIONS) {
			mNotificationPlugin = new TouchableNotificationPlugin(this);
		} else {
			mNotificationPlugin = new NotificationPlugin(this);
		}
		collection.add(mNotificationPlugin);

		if (PlayerHater.LOCK_SCREEN_CONTROLS) {
			collection.add(new LockScreenControlsPlugin(this));
		}

		mLifecycleListener = collection;

		mPlayerListenerManager.setOnCompletionListener(this);
		mPlayerListenerManager.setOnErrorListener(this);
		mPlayerListenerManager.setOnSeekCompleteListener(this);
	}
	
	@Override
	public void onStart(Intent intent, int requestId) {
		onStartCommand(intent, 0, requestId);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int requestId) {
		int keyCode = intent.getIntExtra(BroadcastReceiver.REMOTE_CONTROL_BUTTON, -1);
		if (keyCode != -1) {
			try {
			onRemoteControlButtonPressed(keyCode);
			} catch (Exception e) {
				Log.e(TAG, "Trying to start service with button code " + keyCode + " failed. ", e);
			}
			stopSelfResult(requestId);
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		release();
		getBaseContext().unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new PlayerHaterBinder(this);
	}

	@Override
	public boolean isPlaying() {
		return (getMediaPlayer().getState() == Player.STARTED);
	}

	@Override
	public boolean isPaused() {
		return (getMediaPlayer().getState() == Player.PAUSED);
	}

	@Override
	public boolean isLoading() {
		MediaPlayerWithState mp = getMediaPlayer();
		return (mp.getState() == Player.INITIALIZED
				|| mp.getState() == Player.PREPARING || mp.getState() == Player.PREPARED);
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
		mPlayAfterSeek = (mPlayAfterSeek || getState() == Player.STARTED);

		try {
			getMediaPlayer().pause();
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
		mPlayerListenerManager.setOnPreparedListener(listener);
	}

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
		case Player.INITIALIZED:
		case Player.STOPPED:
			prepare();
			break;
		case Player.IDLE:
			play(getNowPlaying());
			break;
		default:
			resume();
			break;
		}
		return true;
	}

	/*
	 * creates a media player (wrapped, of course) and registers the listeners
	 * for all of the events.
	 */
	protected MediaPlayerWithState buildMediaPlayer() {
		return new MediaPlayerWrapper();
	}

	protected MediaPlayerWithState buildMediaPlayer(boolean setAsCurrent) {
		MediaPlayerWithState mp = buildMediaPlayer();
		if (setAsCurrent) {
			setCurrentMediaPlayer(mp);
		}
		return mp;
	}

	protected void setCurrentMediaPlayer(MediaPlayerWithState player) {
		mPlayerListenerManager.setMediaPlayer(player);
	}

	// PROTECTED STUFF

	protected void prepare() {
		Log.d(TAG, "PREPARING");
		getMediaPlayer().prepareAsync();
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
		Intent intent = new Intent(getApplicationContext(), klass);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP );
		PendingIntent pending = PendingIntent.getActivity(getBaseContext(),
				456, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		mNotificationPlugin.onIntentActivityChanged(pending);
	}

}
