package org.prx.android.playerhater.service;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.MediaPlayerWithState;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.plugins.AudioFocusPlugin;
import org.prx.android.playerhater.plugins.ExpandableNotificationPlugin;
import org.prx.android.playerhater.plugins.LockScreenControlsPlugin;
import org.prx.android.playerhater.plugins.NotificationPlugin;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.plugins.PluginCollection;
import org.prx.android.playerhater.plugins.TouchableNotificationPlugin;
import org.prx.android.playerhater.util.BroadcastReceiver;
import org.prx.android.playerhater.util.PlayerListenerManager;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.IBinder;
import android.view.KeyEvent;

public abstract class NewAbstractPlaybackService extends Service implements
		PlayerHaterService {

	protected String TAG;
	protected BroadcastReceiver mBroadcastReceiver;
	protected PlayerHaterPlugin mPlugin;
	private NotificationPlugin mNotificationPlugin;
	protected final PlayerListenerManager mPlayerListenerManager = new PlayerListenerManager();
	protected PlayerHaterListener mPlayerHaterListener;
	private PlayerHaterBinder mBinder;
	private OnShutdownRequestListener mShutdownRequestListener;

	abstract Player getMediaPlayer();

	/* The Service Life Cycle */

	@Override
	public void onCreate() {
		TAG = getPackageName() + "/PH/" + getClass().getSimpleName();
		
		mBroadcastReceiver = new BroadcastReceiver(this, getBinder());

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

		mPlugin = collection;
	}

	@Override
	public void onDestroy() {
		getMediaPlayer().release();
		getBaseContext().unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return getBinder();
	}
	
	@Override
	public void stopService() {
		mShutdownRequestListener.onShutdownRequested();
		super.stopSelf();
	}

	/* END The Service Life Cycle */

	/* Player State Methods */

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

	/* END Player State Methods */

	/* Generic Player Controls */

	@Override
	public void duck() {
		getMediaPlayer().setVolume(0.1f, 0.1f);
	}

	@Override
	public void unduck() {
		getMediaPlayer().setVolume(1.0f, 1.0f);
	}

	/* END Generic Player Controls */
	
	/* Decomposed Player Methods */
	
	@Override
	public boolean play(Song song) throws IllegalArgumentException {
		return play(song, 0);
	}
	
	/* END Decomposed Player Methods */

	/* Player Listeners */

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mPlayerListenerManager.setOnCompletionListener(listener);
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mPlayerListenerManager.setOnSeekCompleteListener(listener);
	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		mPlayerListenerManager.setOnErrorListener(listener);
	}

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

	/* The Anointed One */

	@Override
	public void setListener(PlayerHaterListener listener) {
		mPlayerHaterListener = listener;
	}

	/* END The Anointed One */
	
	@Override
	public void setOnShutdownRequestListener(OnShutdownRequestListener listener) {
		mShutdownRequestListener = listener;
	}

	/* END Player Listeners */

	/* Plug-In Stuff */
	
	@Override
	public void setSongInfo(Song song) {
		mPlugin.onSongChanged(song);
	}

	@Override
	public void setTitle(String title) {
		mPlugin.onTitleChanged(title);
	}

	@Override
	public void setArtist(String artist) {
		mPlugin.onArtistChanged(artist);
	}

	@Override
	public void setAlbumArt(int resourceId) {
		mPlugin.onAlbumArtChanged(resourceId);
	}

	@Override
	public void setAlbumArt(Uri url) {
		mPlugin.onAlbumArtChangedToUri(url);
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

	/* END Plug-In Stuff */

	/* Remote Controls */

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
	public void onStart(Intent intent, int requestId) {
		onStartCommand(intent, 0, requestId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int requestId) {
		int keyCode = intent.getIntExtra(
				BroadcastReceiver.REMOTE_CONTROL_BUTTON, -1);
		if (keyCode != -1) {
			try {
				onRemoteControlButtonPressed(keyCode);
			} catch (Exception e) {
				// Nah.
			}
			stopSelfResult(requestId);
		}
		return START_NOT_STICKY;
	}

	/* END Remote Controls */
	
	/* Events for Subclasses */
	
	protected void onStopped() {
		mPlugin.onPlaybackStopped();
		mPlayerHaterListener.onStopped();
	}
	
	protected void onPaused() {
		mPlugin.onPlaybackPaused();
		mPlayerHaterListener.onPaused(getNowPlaying());
	}
	
	protected void onLoading() {
		mPlayerHaterListener.onLoading(getNowPlaying());
		mPlugin.onAudioLoading();
	}
	
	protected void onStarted() {
		mPlugin.onPlaybackStarted();
		mPlugin.onSongChanged(getNowPlaying());
		mPlugin.onDurationChanged(getDuration());
	}
	
	protected void onResumed() {
		mPlugin.onPlaybackResumed();
	}
	
	/* END Events for Subclasses */

	/* Private utility methods */

	private PlayerHaterBinder getBinder() {
		if (mBinder == null) {
			mBinder = new PlayerHaterBinder(this);
		}
		return mBinder;
	}

	/* END Private utility methods */
}