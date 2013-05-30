/*******************************************************************************
 * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.prx.playerhater.service;

import org.prx.playerhater.BroadcastReceiver;
import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.PlayerHaterPlugin;
import org.prx.playerhater.Song;
import org.prx.playerhater.ipc.ClientPlugin;
import org.prx.playerhater.ipc.IPlayerHaterClient;
import org.prx.playerhater.ipc.PlayerHaterClient;
import org.prx.playerhater.ipc.PlayerHaterServer;
import org.prx.playerhater.mediaplayer.Player;
import org.prx.playerhater.mediaplayer.SynchronousPlayer;
import org.prx.playerhater.plugins.BackgroundedPlugin;
import org.prx.playerhater.plugins.PluginCollection;
import org.prx.playerhater.service.PlayerStateWatcher.PlayerHaterStateListener;
import org.prx.playerhater.songs.SongHost;
import org.prx.playerhater.util.Config;
import org.prx.playerhater.util.IPlayerHater;
import org.prx.playerhater.util.Log;
import org.prx.playerhater.wrappers.ServicePlayerHater;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.KeyEvent;

public abstract class PlayerHaterService extends Service implements
		IPlayerHater, PlayerHaterStateListener {

	private static final String SELF_STARTER = "org.prx.playerhater.service.PlayerHaterService.SELF_STARTER";

	private int mStarted = -1;

	private PlayerHaterPlugin mPlugin;
	private Config mConfig;
	private PluginCollection mPluginCollection;

	private final ServicePlayerHater mPlayerHater = new ServicePlayerHater(this);
	private final PlayerHaterServer mServer = new PlayerHaterServer(this);
	private final PlayerStateWatcher mPlayerStateWatcher = new PlayerStateWatcher(
			this);

	private ClientPlugin mClient;

	public void setClient(IPlayerHaterClient client) {
		if (mClient != null) {
			getPluginCollection().remove(mClient);
		}
		if (client != null) {
			mClient = new ClientPlugin(client);
			
			if (nowPlaying() != null) {
				mClient.onSongChanged(nowPlaying());
			}
			if (getNextSong() != null) {
				mClient.onNextSongAvailable(getNextSong());
			} else {
				mClient.onNextSongUnavailable();
			}
			switch (getState()) {
			case PlayerHater.STATE_IDLE:
				mClient.onAudioStopped();
				break;
			case PlayerHater.STATE_LOADING:
				mClient.onAudioLoading();
				break;
			case PlayerHater.STATE_PLAYING:
				mClient.onAudioStarted();
				break;
			case PlayerHater.STATE_PAUSED:
				mClient.onAudioPaused();
				break;
			}
			
			getPluginCollection().add(mClient);
			
			// If we're running remotely, set up the remote song host.
			// If this condition returns false, that indicates that
			// the two sides of the transaction are happening on the
			// same process.
			if (!(client instanceof PlayerHaterClient)) {
				SongHost.setRemote(client);
			}
		} else {
			mClient = null;
			SongHost.clear();
		}
	}

	/* The Service Life Cycle */

	@Override
	public void onCreate() {
		super.onCreate();
		BroadcastReceiver.register(getApplicationContext());
		Log.TAG = getPackageName() + "/PH/" + getClass().getSimpleName();
	}

	@Override
	public void onDestroy() {
		onStopped();
		getMediaPlayer().release();
		BroadcastReceiver.release(getApplicationContext());
		SongHost.clear();
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int requestId) {
		onStartCommand(intent, 0, requestId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int requestId) {
		if (isSelfStartCommand(intent)) {
			mStarted = requestId;
		} else {
			new Thread(new RemoteControlButtonTask(intent, this, requestId))
					.start();
		}
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		setConfig(Config.fromIntent(intent));
		return mServer;
	}

	// We don't want onBind called again when the next
	// bind request comes in.
	@Override
	public boolean onUnbind(Intent intent) {
		setClient(null);
		return true;
	}

	@Override
	public void onRebind(Intent intent) {
	}

	/* END Life Cycle Methods */

	/*
	 * Lazy Loaders for Plugins.
	 */
	protected PlayerHaterPlugin getPlugin() {
		if (mPlugin == null) {
			mPlugin = new BackgroundedPlugin(getPluginCollection());
		}
		return mPlugin;
	}

	protected PluginCollection getPluginCollection() {
		if (mPluginCollection == null) {
			mPluginCollection = new PluginCollection();
		}
		return mPluginCollection;
	}

	/* Player State Methods */

	@Override
	public boolean isPlaying() {
		return (getMediaPlayer().getState() == Player.STARTED);
	}

	@Override
	public boolean isLoading() {
		return getMediaPlayer().isWaitingToPlay();
	}

	@Override
	public int getState() {
		if (isPlaying()) {
			return PlayerHater.STATE_PLAYING;
		}
		if (isLoading()) {
			return PlayerHater.STATE_LOADING;
		}
		if (getMediaPlayer().getState() == Player.PAUSED) {
			return PlayerHater.STATE_PAUSED;
		}
		return PlayerHater.STATE_IDLE;
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

	public void duck() {
		getMediaPlayer().setVolume(0.1f, 0.1f);
	}

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

	/* Plug-In Stuff */

	@Override
	public void setTransportControlFlags(int transportControlFlags) {
		getPlugin().onTransportControlFlagsChanged(transportControlFlags);
	}

	/* END Plug-In Stuff */

	/* Remote Controls */

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

	/* END Remote Controls */

	/* Events for Subclasses */

	protected void onStopped() {
		getPlugin().onAudioStopped();
	}

	protected void onPaused() {
		getPlugin().onAudioPaused();
	}

	protected void onLoading() {
		getPlugin().onAudioLoading();
	}

	protected void onStarted() {
		getPlugin().onAudioStarted();
	}

	protected void onResumed() {
		getPlugin().onAudioResumed();
	}

	protected void onSongChanged() {
		getPlugin().onSongChanged(nowPlaying());
		getPlugin().onDurationChanged(getDuration());
	}

	public abstract Song getNextSong();

	protected void onNextSongChanged() {
		if (getNextSong() != null)
			getPlugin().onNextSongAvailable(getNextSong());
		else
			getPlugin().onNextSongUnavailable();
	}

	@Override
	public void setPendingIntent(PendingIntent intent) {
		getPlugin().onPendingIntentChanged(intent);
	}

	/* END Events for Subclasses */

	// ///////////////////////
	// Config stuff

	private void setConfig(Config config) {
		if (config != null) {
			mConfig = config;
			mConfig.run(getApplicationContext(), mPlayerHater,
					getPluginCollection());
		}
	}

	/*
	 * State things
	 * 
	 * @see
	 * org.prx.playerhater.service.PlayerStateWatcher.PlayerHaterStateListener
	 * #onStateChanged(int)
	 */

	private int mLastState = -1;

	@Override
	public void onStateChanged(int state) {
		if (!selfStarted() && state != PlayerHater.STATE_IDLE) {
			startSelf();
		}
		switch (state) {
		case PlayerHater.STATE_IDLE:
			onStopped();
			break;
		case PlayerHater.STATE_LOADING:
			onLoading();
			break;
		case PlayerHater.STATE_PAUSED:
			onPaused();
			break;
		case PlayerHater.STATE_PLAYING:
			if (mLastState == PlayerHater.STATE_PAUSED) {
				onResumed();
			} else {
				onStarted();
			}
		}
		mLastState = state;
	}

	// ///////////////////////
	// For dealing with
	// MediaPlayers.

	private Player mMediaPlayer;

	protected Player getMediaPlayer() {
		if (mMediaPlayer == null) {
			setMediaPlayer(buildMediaPlayer());
		}
		return mMediaPlayer;
	}

	protected void setMediaPlayer(Player mediaPlayer) {
		mPlayerStateWatcher.setMediaPlayer(mediaPlayer);
		mMediaPlayer = mediaPlayer;
	}

	protected Player buildMediaPlayer() {
		Player player = new SynchronousPlayer();
		return player;
	}

	/**
	 * For running Remote Control button presses in the background.
	 */
	private static class RemoteControlButtonTask implements Runnable {

		private final Intent mIntent;
		private final PlayerHaterService mService;
		private final int mRequestCode;

		RemoteControlButtonTask(Intent intent, PlayerHaterService service,
				int requestCode) {
			mIntent = intent;
			mService = service;
			mRequestCode = requestCode;
		}

		@Override
		public void run() {
			int keyCode = mIntent.getIntExtra(
					BroadcastReceiver.REMOTE_CONTROL_BUTTON, -1);
			if (keyCode != -1) {
				mService.onRemoteControlButtonPressed(keyCode);
				mService.stopSelfResult(mRequestCode);
			}
		}
	}

	private boolean selfStarted() {
		return mStarted != -1;
	}

	private void startSelf() {
		Intent intent = PlayerHater.buildServiceIntent(getApplicationContext());
		intent.putExtra(SELF_STARTER, true);
		startService(intent);
	}

	public void quit() {
		if (selfStarted()) {
			stopSelfResult(mStarted);
			mStarted = -1;
		}
	}

	private boolean isSelfStartCommand(Intent intent) {
		return intent.getBooleanExtra(SELF_STARTER, false);
	}
}
