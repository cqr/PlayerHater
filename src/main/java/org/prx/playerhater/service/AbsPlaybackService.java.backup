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
package org.prx.android.playerhater.service;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.playerhater.PlayerHaterBinderStub;
import org.prx.android.playerhater.playerhater.ServicePlayerHater;
import org.prx.android.playerhater.playerhater.ThreadsafePlayerHater;
import org.prx.android.playerhater.plugins.IRemotePlugin;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.plugins.PluginCollection;
import org.prx.android.playerhater.util.BroadcastReceiver;
import org.prx.android.playerhater.util.Config;
import org.prx.android.playerhater.util.Log;
import org.prx.android.playerhater.util.RemoteSong;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.view.KeyEvent;

public abstract class AbsPlaybackService extends Service implements
		PlayerHaterService {

	public static final int REMOTE_PLUGIN = 2525;
	public static String TAG = PlayerHater.TAG;
	protected BroadcastReceiver mBroadcastReceiver;
	private PlayerHaterPlugin mPlugin;
	private Config mConfig;
	private boolean mIsCreated = false;
	private PluginCollection mPluginCollection;
	private final PlayerHater mPlayerHater = new ThreadsafePlayerHater(
			new ServicePlayerHater(this));
	private IRemotePlugin mPluginBinder;
	private PlayerHaterBinderStub mRemoteBinder;

	abstract Player getMediaPlayer();

	/* The Service Life Cycle */

	@Override
	public void onCreate() {
		super.onCreate();
		mIsCreated = true;
		TAG = getPackageName() + "/PH/" + getClass().getSimpleName();
		mRemoteBinder = new PlayerHaterBinderStub(this);
		mBroadcastReceiver = new BroadcastReceiver(this, mRemoteBinder);
		Intent intent = new Intent(getBaseContext(), getClass());
		getBaseContext().startService(intent);
	}

	@Override
	public void onDestroy() {
		onStopped();
		mIsCreated = false;
		mConfig = null;
		getPlugin().onServiceStopping();
		releaseMediaPlayer();
		mPluginCollection = null;
		mPlugin = null;
		getBaseContext().unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	protected PlayerHaterPlugin getPlugin() {
		if (mPlugin == null) {
			mPlugin = getPluginCollection();
		}
		return mPlugin;
	}

	@Override
	public PluginCollection getPluginCollection() {
		if (mPluginCollection == null) {
			mPluginCollection = new PluginCollection();
		}
		return mPluginCollection;
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (!mIsCreated) {
			onCreate();
		}
		if (mConfig == null) {

			Config config = intent.getExtras().getParcelable(
					PlayerHater.EXTRA_CONFIG);
			if (config != null) {
				setConfig(config);
			}
		}
		return mRemoteBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		Log.d("And we have successfully rebound");
	}

	private synchronized void setConfig(Config config) {
		mConfig = config;
		for (Class<? extends PlayerHaterPlugin> pluginKlass : mConfig
				.getServicePlugins()) {
			try {
				PlayerHaterPlugin plugin = pluginKlass.newInstance();
				Log.d("Setting up a plugin " + pluginKlass);
				plugin.onPlayerHaterLoaded(getApplicationContext(),
						mPlayerHater);
				plugin.onServiceBound(mRemoteBinder);
				getPluginCollection().add(plugin);
				Log.d("Plugins: " + getPluginCollection().getSize());
			} catch (Exception e) {
				Log.e("Could not instantiate plugin "
						+ pluginKlass.getCanonicalName(), e);
			}
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d("It looks like all clients have unbound.");
		return true;
	}

	@Override
	public void stopService(Song[] songs) {
		onStopped();
		int[] songTags = new int[songs.length];
		int i = 0;
		for (Song song : songs) {
			songTags[i] = ((RemoteSong) song).getTag();
			i++;
		}
		try {
			mPluginBinder.onUnbindRequested(songTags);
		} catch (Exception e) {}
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

	/* Plug-In Stuff */

	@Override
	public void addPluginInstance(PlayerHaterPlugin plugin) {
		getPluginCollection().add(plugin);
	}

	@Override
	public void setSongInfo(Song song) {
		getPlugin().onSongChanged(song);
	}

	@Override
	public void setTitle(String title) {
		getPlugin().onTitleChanged(title);
	}

	@Override
	public void setArtist(String artist) {
		getPlugin().onArtistChanged(artist);
	}

	@Override
	public void setAlbumArt(int resourceId) {
		getPlugin().onAlbumArtChanged(resourceId);
	}

	@Override
	public void setAlbumArt(Uri url) {
		getPlugin().onAlbumArtChangedToUri(url);
	}

	@Override
	public void setTransportControlFlags(int transportControlFlags) {
		getPlugin().onTransportControlFlagsChanged(transportControlFlags);
	}

	@Override
	public void setIntentClass(Class<? extends Activity> klass) {
		Intent intent = new Intent(getApplicationContext(), klass);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pending = PendingIntent.getActivity(getBaseContext(),
				456, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		getPlugin().onIntentActivityChanged(pending);
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
		getPlugin().onSongChanged(getNowPlaying());
		getPlugin().onDurationChanged(getDuration());
	}

	protected void onResumed() {
		getPlugin().onAudioResumed();
	}

	/* END Events for Subclasses */

	public void removeRemotePlugin() {
		getPluginCollection().remove(REMOTE_PLUGIN);
	}

	public void setPluginBinder(IRemotePlugin binder) {
		mPluginBinder = binder;
	}
}
