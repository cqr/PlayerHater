package org.prx.playerhater.wrappers;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.PlayerHaterPlugin;
import org.prx.playerhater.Song;
import org.prx.playerhater.ipc.IPlayerHaterServer;
import org.prx.playerhater.ipc.PlayerHaterClient;
import org.prx.playerhater.ipc.ServerPlayerHater;
import org.prx.playerhater.plugins.BackgroundedPlugin;
import org.prx.playerhater.plugins.PluginCollection;
import org.prx.playerhater.songs.RemoteSong;
import org.prx.playerhater.songs.SongQueue;
import org.prx.playerhater.songs.SongQueue.OnQueuedSongsChangedListener;
import org.prx.playerhater.util.Config;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class BoundPlayerHater extends PlayerHater {

	private static Context sApplicationContext;
	private static PlayerHater sPlayerHater;
	private static Set<BoundPlayerHater> sInstances;
	private static PlayerHaterClient sClient;
	private static PluginCollection sPlugins;
	private static PlayerHaterPlugin sPlugin;
	private static SongQueue sSongQueue;

	private static PendingIntent sPendingPendingIntent;
	private static int sPendingTransportControlFlags = -1;
	private static int sStartSeekPosition = -1;

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
	}

	private static void removeInstance(BoundPlayerHater instance) {
		getInstances().remove(instance);
		if (getInstances().size() < 1) {
			sApplicationContext.unbindService(sServiceConnection);
			sPlayerHater = null;
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
						public void onNowPlayingChanged(Song nowPlaying) {
							getPlugin().onSongChanged(nowPlaying);
						}

						@Override
						public void onNextSongChanged(Song nextSong) {
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
				
				RemoteSong.setSongHost(server);
				try {
					server.setClient(getPlayerHaterClient());
				} catch (RemoteException e) {
					throw new IllegalStateException("Server has gone away...",
							e);
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
			return getSongQueue().getPosition();
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
}
