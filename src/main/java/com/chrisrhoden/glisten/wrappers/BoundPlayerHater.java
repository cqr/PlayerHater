/*******************************************************************************
 * Copyright 2013-2014 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
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
package com.chrisrhoden.glisten.wrappers;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import com.chrisrhoden.glisten.PlayerHater;
import com.chrisrhoden.glisten.PlayerHaterPlugin;
import com.chrisrhoden.glisten.Song;
import com.chrisrhoden.glisten.ipc.IPlayerHaterServer;
import com.chrisrhoden.glisten.ipc.PlayerHaterClient;
import com.chrisrhoden.glisten.ipc.PlayerHaterServer;
import com.chrisrhoden.glisten.ipc.ServerPlayerHater;
import com.chrisrhoden.glisten.plugins.BackgroundedPlugin;
import com.chrisrhoden.glisten.plugins.PluginCollection;
import com.chrisrhoden.glisten.songs.SongHost;
import com.chrisrhoden.glisten.songs.SongQueue;
import com.chrisrhoden.glisten.songs.SongQueue.OnQueuedSongsChangedListener;
import com.chrisrhoden.glisten.util.Config;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

public class BoundPlayerHater extends PlayerHater {

	private static Context sApplicationContext;
	private static Handler sHandler;
	private static Runnable sRunnable;
	private static PlayerHater sPlayerHater;
	private static Set<BoundPlayerHater> sInstances;
	private static PlayerHaterClient sClient;
	private static PluginCollection sPlugins;
	private static PlayerHaterPlugin sPlugin;
	private static SongQueue sSongQueue;

	private static PendingIntent sPendingPendingIntent;
	private static int sPendingTransportControlFlags = -1;
	private static int sStartSeekPosition = -1;

	private static Handler getHandler() {
		if (sHandler == null) {
			sHandler = new Handler();
		}
		return sHandler;
	}

	private static Runnable getDisconnectRunnable() {
		if (sRunnable == null) {
			sRunnable = new Runnable() {

				@Override
				public void run() {
					synchronized (BoundPlayerHater.class) {
						if (sPlayerHater != null) {
							((ServerPlayerHater) sPlayerHater).slurp(SongHost
									.localSongs());
						}
						if (sServiceConnection != null && sApplicationContext != null) {
							sApplicationContext.unbindService(sServiceConnection);
						}
						sPlayerHater = null;
					}
				}
			};
		}
		return sRunnable;
	}

	private static synchronized PlayerHater getPlayerHater() {
		return sPlayerHater;
	}

	private static Set<BoundPlayerHater> getInstances() {
		if (sInstances == null) {
			sInstances = new HashSet<BoundPlayerHater>();
		}
		return sInstances;
	}

	@SuppressLint("InlinedApi")
	private static void addInstance(BoundPlayerHater instance) {
		getInstances().add(instance);
		if (getInstances().size() == 1) {
			sApplicationContext.bindService(
					buildServiceIntent(sApplicationContext),
					sServiceConnection, Context.BIND_AUTO_CREATE
							| Context.BIND_NOT_FOREGROUND);
		}
		getHandler().removeCallbacks(getDisconnectRunnable());
	}

	private static void removeInstance(BoundPlayerHater instance) {
		getInstances().remove(instance);
		if (getInstances().size() < 1) {
			getHandler().removeCallbacks(getDisconnectRunnable());
			getHandler().postDelayed(getDisconnectRunnable(), 0);
		}
	}

	private static void setPendingPendingIntent(PendingIntent intent) {
		sPendingPendingIntent = intent;
	}

	private static PlayerHaterClient getPlayerHaterClient() {
		if (sClient == null) {
			sClient = new PlayerHaterClient(getPlugin());
		}
		return sClient;
	}

	private static PluginCollection getPluginCollection() {
		if (sPlugins == null) {
			sPlugins = new PluginCollection();
		}
		return sPlugins;
	}

	private static PlayerHaterPlugin getPlugin() {
		if (sPlugin == null) {
			sPlugin = new BackgroundedPlugin(getPluginCollection());
		}
		return sPlugin;
	}

	private static SongQueue getSongQueue() {
		if (sSongQueue == null) {
			sSongQueue = new SongQueue();
			sSongQueue
					.setQueuedSongsChangedListener(new OnQueuedSongsChangedListener() {

						@Override
						public void onNowPlayingChanged(Song nowPlaying,
								Song was) {
							getPlugin().onSongChanged(nowPlaying);
						}

						@Override
						public void onNextSongChanged(Song nextSong, Song was) {
							if (nextSong != null) {
								getPlugin().onNextSongAvailable(nextSong);
							} else {
								getPlugin().onNextSongUnavailable();
							}
						}
					});
		}
		return sSongQueue;
	}

	private static final ServiceConnection sServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			synchronized (BoundPlayerHater.class) {
				IPlayerHaterServer server = IPlayerHaterServer.Stub
						.asInterface(service);
				if (!(service instanceof PlayerHaterServer)) {
					SongHost.setRemote(server);
				}

				try {
					server.setClient(getPlayerHaterClient());
				} catch (RemoteException e) {
					throw new IllegalStateException("Server has gone away...", e);
				}

				sPlayerHater = new ServerPlayerHater(server);

				if (sPendingPendingIntent != null) {
					sPlayerHater.setPendingIntent(sPendingPendingIntent);
					sPendingPendingIntent = null;
				}

				if (sPendingTransportControlFlags != -1) {
					sPlayerHater
							.setTransportControlFlags(sPendingTransportControlFlags);
					sPendingTransportControlFlags = -1;
				}

				if (getSongQueue().size() > 0) {
					int position = getSongQueue().getPosition();
					getSongQueue().skipTo(1);
					while (!getSongQueue().isAtLastSong()) {
						sPlayerHater.enqueue(getSongQueue().getNowPlaying());
						getSongQueue().remove(1);
					}
					sPlayerHater.skipTo(position);
				}

				if (sStartSeekPosition != -1) {
					sPlayerHater.seekTo(sStartSeekPosition);
					sPlayerHater.play();
					sStartSeekPosition = -1;
				}
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			sPlayerHater = null;
		}
	};

	@SuppressLint("InlinedApi")
	private static void configureAndStart(Context context) {
		sApplicationContext = context;
		Config.getInstance(context);
	}

	private PlayerHaterPlugin mPlugin;
	private final WeakReference<Context> mContext;

	/**
	 * @see {@link PlayerHater#bind(Context)}
	 */
	public BoundPlayerHater(Context context) {
		if (sApplicationContext == null) {
			configureAndStart(context.getApplicationContext());
		}
		mContext = new WeakReference<Context>(context);
		addInstance(this);
	}

	@Override
	public boolean setLocalPlugin(PlayerHaterPlugin plugin) {
		removeCurrentPlugin();
		mPlugin = plugin;
		if (mPlugin != null) {
			mPlugin.onPlayerHaterLoaded(mContext.get(), this);
			getPluginCollection().add(plugin);
		}
		return true;
	}

	@Override
	public boolean release() {
		removeCurrentPlugin();
		mContext.clear();
		removeInstance(this);
		return true;
	}

	private void removeCurrentPlugin() {
		if (mPlugin != null) {
			getPluginCollection().remove(mPlugin);
		}
		mPlugin = null;
	}

	@Override
	public boolean pause() {
		if (getPlayerHater() == null) {
			return false;
		} else {
			return getPlayerHater().pause();
		}
	}

	@Override
	public boolean stop() {
		if (getPlayerHater() == null) {
			return true;
		} else {
			return getPlayerHater().stop();
		}
	}

	@Override
	public boolean play() {
		if (getPlayerHater() != null) {
			return getPlayerHater().play();
		} else {
			return play(0);
		}
	}

	@Override
	public boolean play(int startTime) {
		if (getPlayerHater() == null) {
			if (getSongQueue().getNowPlaying() != null) {
				sStartSeekPosition = startTime;
				return true;
			} else {
				return false;
			}
		} else {
			return getPlayerHater().play(startTime);
		}
	}

	@Override
	public boolean play(Song song) {
		return play(song, 0);
	}

	@Override
	public boolean play(Song song, int startTime) {
		if (getPlayerHater() == null) {
			sStartSeekPosition = startTime;
			getSongQueue().appendSong(song);
			getSongQueue().skipToEnd();
			return true;
		} else {
			return getPlayerHater().play(song, startTime);
		}
	}

	@Override
	public boolean seekTo(int startTime) {
		if (getPlayerHater() == null) {
			if (getSongQueue().getNowPlaying() != null) {
				sStartSeekPosition = startTime;
				return true;
			} else {
				return false;
			}
		} else {
			getPlayerHater().seekTo(startTime);
			return true;
		}
	}

	@Override
	public int getCurrentPosition() {
		if (getPlayerHater() == null) {
			return 0;
		}
		return getPlayerHater().getCurrentPosition();
	}

	@Override
	public int getDuration() {
		if (getPlayerHater() == null) {
			return 0;
		}
		return getPlayerHater().getDuration();
	}

	@Override
	public Song nowPlaying() {
		if (getPlayerHater() != null) {
			return getPlayerHater().nowPlaying();
		} else {
			return getSongQueue().getNowPlaying();
		}
	}

	@Override
	public boolean isPlaying() {
		if (getPlayerHater() == null) {
			return false;
		}
		return getPlayerHater().isPlaying();
	}

	@Override
	public boolean isLoading() {
		if (getSongQueue().getNowPlaying() != null) {
			return true;
		} else if (getPlayerHater() == null) {
			return false;
		}
		return getPlayerHater().isLoading();
	}

	@Override
	public int getState() {
		if (getPlayerHater() == null) {
			return PlayerHater.STATE_LOADING;
		}
		return getPlayerHater().getState();
	}

	@Override
	public int enqueue(Song song) {
		if (getPlayerHater() != null) {
			return getPlayerHater().enqueue(song);
		} else {
			return getSongQueue().appendSong(song);
		}
	}

	@Override
	public void enqueue(int position, Song song) {
		if (getPlayerHater() != null) {
			getPlayerHater().enqueue(position, song);
		} else {
			getSongQueue().addSongAtPosition(song, position);
		}
	}

	@Override
	public boolean skipTo(int position) {
		if (getPlayerHater() == null) {
			return getSongQueue().skipTo(position);
		} else {
			return getPlayerHater().skipTo(position);
		}
	}

	@Override
	public void emptyQueue() {
		if (getPlayerHater() == null) {
			getSongQueue().empty();
		} else {
			getPlayerHater().emptyQueue();
		}
	}

	@Override
	public void skip() {
		if (getPlayerHater() == null) {
			getSongQueue().next();
		} else {
			getPlayerHater().skip();
		}
	}

	@Override
	public void skipBack() {
		if (getPlayerHater() == null) {
			getSongQueue().back();
		} else {
			getPlayerHater().skipBack();
		}
	}

	@Override
	public void setTransportControlFlags(int transportControlFlags) {
		if (getPlayerHater() == null) {
			sPendingTransportControlFlags = transportControlFlags;
		} else {
			getPlayerHater().setTransportControlFlags(transportControlFlags);
		}
	}

	@Override
	public int getQueueLength() {
		if (getPlayerHater() == null) {
			return getSongQueue().size();
		} else {
			return getPlayerHater().getQueueLength();
		}
	}

	@Override
	public int getQueuePosition() {
		if (getPlayerHater() == null) {
			return getSongQueue().getPosition() - 1;
		} else {
			return getPlayerHater().getQueuePosition();
		}
	}

	@Override
	public boolean removeFromQueue(int position) {
		if (getPlayerHater() == null) {
			return getSongQueue().remove(position);
		} else {
			return getPlayerHater().removeFromQueue(position);
		}
	}

	@Override
	public void setPendingIntent(PendingIntent intent) {
		if (getPlayerHater() == null) {
			setPendingPendingIntent(intent);
		} else {
			getPlayerHater().setPendingIntent(intent);
		}
	}

	@Override
	public int getTransportControlFlags() {
		if (getPlayerHater() == null) {
			if (sPendingTransportControlFlags != -1) {
				return sPendingTransportControlFlags;
			} else {
				return PlayerHater.DEFAULT_TRANSPORT_CONTROL_FLAGS;
			}
		} else {
			return getPlayerHater().getTransportControlFlags();
		}
	}
}
