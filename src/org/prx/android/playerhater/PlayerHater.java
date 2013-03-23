package org.prx.android.playerhater;

import java.util.HashSet;
import java.util.Set;

import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.service.PlayerHaterBinderPlugin;
import org.prx.android.playerhater.service.PlayerHaterServiceBinder;
import org.prx.android.playerhater.util.BinderPlayerHater;
import org.prx.android.playerhater.util.BoundPlayerHater;
import org.prx.android.playerhater.util.Config;
import org.prx.android.playerhater.util.IPlayerHater;
import org.prx.android.playerhater.util.ListenerEcho;
import org.prx.android.playerhater.util.SongQueue;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public abstract class PlayerHater implements IPlayerHater {

	public static final String TAG = "PLAYERHATER";

	public static final int TRACK_END = 4;
	public static final int SKIP_BUTTON = 5;

	protected static final SongQueue sPlayQueue = new SongQueue();
	protected static int sStartPosition = 0;
	private static Context sApplicationContext;

	protected static final Set<PlayerHaterPlugin> sPlugins = new HashSet<PlayerHaterPlugin>();

	protected static String sPendingAlbumArtType;
	protected static Uri sPendingAlbumArtUrl;
	protected static OnErrorListener sPendingErrorListener;
	protected static OnSeekCompleteListener sPendingSeekListener;
	protected static OnPreparedListener sPendingPreparedListener;
	protected static OnInfoListener sPendingInfoListener;
	protected static OnCompletionListener sPendingCompleteListener;
	protected static int sPendingAlbumArtResourceId;
	protected static String sPendingNotificationTitle;
	protected static String sPendingNotificationText;
	protected static Activity sPendingNotificationIntentActivity;
	protected static OnBufferingUpdateListener sPendingBufferingListener;
	protected static Config sConfig;
	protected static final ListenerEcho sListener = new ListenerEcho();

	protected static BinderPlayerHater sPlayerHater;

	private static final Set<AutoBindHandle> sHandles = new HashSet<AutoBindHandle>();

	protected static final String RESOURCE = "resource";
	protected static final String URL = "url";

	public static final String EXTRA_CONFIG = "config";

	public static BoundPlayerHater bind(Context context) {
		if (sConfig == null) {
			configure(context);
		}

		return new BoundPlayerHater(context);
	}

	private static void configure(Context context) {
		context = context.getApplicationContext();
		sConfig = new Config(context);
		for (Class<? extends PlayerHaterPlugin> pluginClass : sConfig
				.getPrebindPlugins()) {
			try {
				PlayerHaterPlugin plugin = pluginClass.newInstance();
				sPlugins.add(plugin);
				plugin.onPlayerHaterLoaded(context, new BoundPlayerHater(
						context));
			} catch (Exception e) {
				Log.e(TAG,
						"Could not instantiate plugin "
								+ pluginClass.getCanonicalName(), e);
				throw new IllegalArgumentException("Bad plugin in your config.");
			}
		}
		sApplicationContext = context;
	}

	protected static void startService() {
		sApplicationContext.bindService(
				buildServiceIntent(sApplicationContext), sServiceConnection,
				Context.BIND_AUTO_CREATE);
	}

	protected static interface AutoBindHandle {
		public void bind(PlayerHater playerHater);

		public void unbind();
	}

	public static Intent buildServiceIntent(Context context) {
		Intent intent = new Intent("org.prx.android.playerhater.SERVICE");
		intent.setPackage(context.getPackageName());
		intent.putExtra(EXTRA_CONFIG, sConfig);
		if (context.getPackageManager().queryIntentServices(intent, 0).size() == 0) {
			intent = new Intent(context, PlaybackService.class);
			intent.putExtra(EXTRA_CONFIG, sConfig);
			if (context.getPackageManager().queryIntentServices(intent, 0)
					.size() == 0) {
				IllegalArgumentException e = new IllegalArgumentException(
						"No usable service found.");
				String tag = context.getPackageName() + "/PlayerHater";
				String message = "Please define your Playback Service. For help, refer to: https://github.com/PRX/PlayerHater/wiki/Setting-Up-Your-Manifest";
				Log.e(tag, message, e);
				throw e;
			}
		}

		return intent;
	}

	protected static void release(AutoBindHandle handle) {
		sHandles.remove(handle);
		if (sHandles.size() < 1 && sPlayerHater != null) {
			sApplicationContext.unbindService(sServiceConnection);
		}
	}

	protected static final PlayerHaterBinderPlugin sBinderPlugin = new PlayerHaterBinderPlugin.Stub() {

		@Override
		public void onUnbindRequested() throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTitleChanged(String title) throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onTitleChanged(title);
			}
		}

		@Override
		public void onSongFinished(int songTag, int reason)
				throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onSongFinished(sPlayerHater.getSong(songTag), reason);
			}
		}

		@Override
		public void onSongChanged(int songTag) throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onSongChanged(sPlayerHater.getSong(songTag));
			}
		}

		@Override
		public void onNextSongUnavailable() throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onNextSongUnavailable();
			}
		}

		@Override
		public void onNextSongAvailable(int songTag) throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onNextSongAvailable(sPlayerHater.getSong(songTag));
			}
		}

		@Override
		public void onDurationChanged(int duration) throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onDurationChanged(duration);
			}
		}

		@Override
		public void onAudioStopped() throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onAudioStopped();
			}
		}

		@Override
		public void onAudioStarted() throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onAudioStarted();
			}

		}

		@Override
		public void onAudioResumed() throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onAudioResumed();
			}
		}

		@Override
		public void onAudioPaused() throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onAudioPaused();
			}
		}

		@Override
		public void onAudioLoading() throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onAudioLoading();
			}
		}

		@Override
		public void onArtistChanged(String artist) throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onArtistChanged(artist);
			}
		}

		@Override
		public void onAlbumArtUriChanged(Uri uri) throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onAlbumArtChangedToUri(uri);
			}
		}

		@Override
		public void onAlbumArtResourceChanged(int albumArtResource)
				throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onAlbumArtChanged(albumArtResource);
			}
		}

		@Override
		public void onServiceBound(PlayerHaterServiceBinder binder)
				throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onServiceBound(binder);
			}
			
		}

		@Override
		public void onIntentActivityChanged(PendingIntent intent)
				throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onIntentActivityChanged(intent);
			}
		}

		@Override
		public void onChangesComplete() throws RemoteException {
			for (PlayerHaterPlugin plugin : sPlugins) {
				plugin.onChangesComplete();
			}
		}
	};

	protected static final ServiceConnection sServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			PlayerHaterServiceBinder sBinder = PlayerHaterServiceBinder.Stub
					.asInterface(service);

			try {
				sBinder.setBinder(sBinderPlugin);
			} catch (RemoteException e) { }
			
			sPlayerHater = BinderPlayerHater.get(sBinder);

			if (sPendingAlbumArtType != null) {
				if (sPendingAlbumArtType.equals(RESOURCE)) {
					sPlayerHater.setAlbumArt(sPendingAlbumArtResourceId);
				} else if (sPendingAlbumArtType.equals(URL)) {
					sPlayerHater.setAlbumArt(sPendingAlbumArtUrl);
				}
			}

			// XXX
			// for (PlayerHaterPlugin plugin : sPlugins) {
			// phService.addPluginInstance(plugin);
			// }

			// XXX
			// phService.getPlayerHater().setListener(sListener);

			// if (sPendingErrorListener != null) {
			// phService.getPlayerHater().setOnErrorListener(
			// sPendingErrorListener);
			// }
			//
			// if (sPendingSeekListener != null) {
			// phService.getPlayerHater().setOnSeekCompleteListener(
			// sPendingSeekListener);
			// }
			//
			// if (sPendingPreparedListener != null) {
			// phService.getPlayerHater().setOnPreparedListener(
			// sPendingPreparedListener);
			// }
			//
			// if (sPendingInfoListener != null) {
			// phService.getPlayerHater().setOnInfoListener(
			// sPendingInfoListener);
			// }
			//
			// if (sPendingCompleteListener != null) {
			// phService.getPlayerHater().setOnCompletionListener(
			// sPendingCompleteListener);
			// }
			//
			// if (sPendingBufferingListener != null) {
			// phService.getPlayerHater().setOnBufferingUpdateListener(
			// sPendingBufferingListener);
			// }

			if (sPendingNotificationTitle != null) {
				sPlayerHater.setTitle(sPendingNotificationTitle);
			}

			if (sPendingNotificationText != null) {
				sPlayerHater.setArtist(sPendingNotificationText);
			}

			if (sPendingNotificationIntentActivity != null) {
				sPlayerHater.setActivity(sPendingNotificationIntentActivity);
			}

			if (sPlayQueue.getNowPlaying() != null) {
				for (Song song : sPlayQueue.getSongsBefore()) {
					Log.d(TAG, "Enqueueing: " + song);
					sPlayerHater.enqueue(song);
				}

				Song firstSong = sPlayQueue.getNowPlaying();
				Log.d(TAG, "Playing " + firstSong);
				sPlayerHater.play(firstSong, sStartPosition);

				for (Song song : sPlayQueue.getSongsAfter()) {
					Log.d(TAG, "Enqueueing: " + song);
					sPlayerHater.enqueue(song);
				}

				sPlayQueue.empty();
			}

			for (AutoBindHandle boundPlayer : sHandles) {
				boundPlayer.bind(sPlayerHater);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			BinderPlayerHater.detach();
			sPlayerHater = null;
			for (AutoBindHandle boundPlayer : sHandles) {
				boundPlayer.unbind();
			}
		}
	};

	protected PlayerHater() {
	}

	protected void requestAutoBind(AutoBindHandle handle) {
		sHandles.add(handle);
		if (sPlayerHater != null) {
			handle.bind(sPlayerHater);
		}
	}
}
