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
import org.prx.playerhater.PlayerHaterPlugin;
import org.prx.playerhater.Song;
import org.prx.playerhater.ipc.ClientPlugin;
import org.prx.playerhater.ipc.IPlayerHaterClient;
import org.prx.playerhater.ipc.PlayerHaterServer;
import org.prx.playerhater.mediaplayer.Player;
import org.prx.playerhater.mediaplayer.SynchronousPlayer;
import org.prx.playerhater.plugins.PluginCollection;
import org.prx.playerhater.songs.RemoteSong;
import org.prx.playerhater.util.Config;
import org.prx.playerhater.util.IPlayerHater;
import org.prx.playerhater.util.Log;
import org.prx.playerhater.wrappers.ServicePlayerHater;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.KeyEvent;

public abstract class PlayerHaterService extends Service implements
		IPlayerHater {

	public static final int REMOTE_PLUGIN = 2525;
	protected BroadcastReceiver mBroadcastReceiver;
	private PlayerHaterPlugin mPlugin;
	private Config mConfig;
	private PluginCollection mPluginCollection;

	private final ServicePlayerHater mPlayerHater = new ServicePlayerHater(this);

	private PlayerHaterServer mServer;
	private ClientPlugin mClient;

	public void setClient(IPlayerHaterClient client) {
		if (mClient != null) {
			getPluginCollection().remove(mClient);
		}
		mClient = new ClientPlugin(client);
		getPluginCollection().add(mClient);
		RemoteSong.setSongHost(client);
	}

	/* The Service Life Cycle */

	@Override
	public void onCreate() {
		super.onCreate();
		Log.TAG = getPackageName() + "/PH/" + getClass().getSimpleName();
	}

	@Override
	public void onDestroy() {
		onStopped();
		super.onDestroy();
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

	@Override
	public IBinder onBind(Intent intent) {
		if (mConfig == null) {
			Config config = intent.getExtras().getParcelable(
					Config.EXTRA_CONFIG);
			if (config != null) {
				setConfig(config);
			}
		}
		return getServer();
	}

	/* END Life Cycle Methods */

	protected PlayerHaterPlugin getPlugin() {
		if (mPlugin == null) {
			mPlugin = getPluginCollection();
		}
		return mPlugin;
	}

	public PluginCollection getPluginCollection() {
		if (mPluginCollection == null) {
			mPluginCollection = new PluginCollection();
		}
		return mPluginCollection;
	}

	public PlayerHaterServer getServer() {
		if (mServer == null) {
			mServer = new PlayerHaterServer(this);
		}
		return mServer;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return true;
	}

	@Override
	public void onRebind(Intent intent) {
	}

	/* END The Service Life Cycle */

	/* Player State Methods */

	@Override
	public boolean isPlaying() {
		return (getMediaPlayer().getState() == Player.STARTED);
	}

	@Override
	public boolean isLoading() {
		int state = getState();
		return (state == Player.INITIALIZED || state == Player.PREPARING
				|| state == Player.PREPARED || state == Player.LOADING_CONTENT || state == Player.PREPARING_CONTENT);
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
		getPlugin().onSongChanged(nowPlaying());
		getPlugin().onDurationChanged(getDuration());
	}

	protected void onResumed() {
		getPlugin().onAudioResumed();
	}

	/* END Events for Subclasses */
	
	///////////////////////////
	////  Config stuff
	
	private void setConfig(Config config) {
		mConfig = config;
		mConfig.run(getApplicationContext(), mPlayerHater, getPluginCollection());
	}
	
	private Player mMediaPlayer;

	protected Player getMediaPlayer() {
		if (mMediaPlayer == null) {
			mMediaPlayer = new SynchronousPlayer();
		}
		return mMediaPlayer;
	}
}
