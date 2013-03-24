package org.prx.android.playerhater;

import java.util.HashSet;
import java.util.Set;

import org.prx.android.playerhater.playerhater.BinderPlayerHater;
import org.prx.android.playerhater.playerhater.BoundPlayerHater;
import org.prx.android.playerhater.playerhater.IPlayerHater;
import org.prx.android.playerhater.plugins.BackgroundedPlugin;
import org.prx.android.playerhater.plugins.IRemotePlugin;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.plugins.PluginCollection;
import org.prx.android.playerhater.service.IPlayerHaterBinder;
import org.prx.android.playerhater.util.Config;
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
	protected static final PluginCollection sPluginCollection = new PluginCollection();
	protected static final PlayerHaterPlugin sPlugin = new BackgroundedPlugin(sPluginCollection);
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
	protected static BinderPlayerHater sPlayerHater;
	private static final Set<AutoBindHandle> sHandles = new HashSet<AutoBindHandle>();
	protected static final String RESOURCE = "resource";
	protected static final String URL = "url";
	protected static int sPendingTransportControlFlags;
	public static final String EXTRA_CONFIG = "config";

	/**
	 * Gets an instance of a {@linkplain BoundPlayerHater} which can be used to
	 * interact with the playback service.
	 * 
	 * Calling this method will also invoke
	 * {@linkplain PlayerHater#configure(Context)} if it has not yet been
	 * called.
	 * 
	 * @since 2.1.0
	 * 
	 * @param context
	 *            The context on which to bind the service.
	 * @return an instance of PlayerHater which one can use to interact with the
	 *         Playback Service.
	 */
	public static BoundPlayerHater bind(Context context) {
		return new BoundPlayerHater(context);
	}

	/**
	 * Gets an instance of {@link PlayerHater} which can be used to interact
	 * with the playback service.
	 * 
	 * @deprecated This method name is misleading and there are some important
	 *             differences between the interface provided by
	 *             {@linkplain PlayerHater} and {@linkplain BoundPlayerHater}
	 *             (namely, the {@linkplain BoundPlayerHater#release() #release}
	 *             method).
	 *             <p>
	 *             In version 2.0.0, you would call
	 *             {@code mPlayerHater = PlayerHater.get(this)} and then later
	 *             call {@code PlayerHater.release(mPlayerHater)}. This is
	 *             confusing, so it has been replaced with the {@link
	 *             PlayerHater.bind(Context)} {@link BoundPlayerHater#release()}
	 *             pair.
	 * 
	 * @see #bind(Context)
	 * @since 2.0.0
	 * 
	 * @param context
	 *            The context on which to bind the service.
	 * @return an instance of PlayerHater which one can use to interact with the
	 *         Playback Service.
	 */
	public static PlayerHater get(Context context) {
		return new BoundPlayerHater(context);
	}

	/**
	 * Releases a previously bound {@linkplain PlayerHater} instance.
	 * 
	 * @deprecated In version 2.0.0, you would call
	 *             {@code mPlayerHater = PlayerHater.get(this)} and then later
	 *             call {@code PlayerHater.release(mPlayerHater)}. This is
	 *             confusing, so it has been replaced with the
	 *             {@link PlayerHater#bind(Context)}
	 *             {@link BoundPlayerHater#release()} pair.
	 * 
	 * @since 2.0.0
	 * @see {@link BoundPlayerHater#release()}
	 * @param playerHater
	 *            The PlayerHater to be released.
	 */
	public static void release(PlayerHater playerHater) {
		if (playerHater instanceof BoundPlayerHater) {
			((BoundPlayerHater) playerHater).release();
		}
	}

	/**
	 * Configures PlayerHater.
	 * 
	 * @param context
	 *            A Context object from within the application to be used.
	 */
	public static void configure(Context context) {
		context = context.getApplicationContext();
		sConfig = new Config(context);
		for (Class<? extends PlayerHaterPlugin> pluginClass : sConfig
				.getPrebindPlugins()) {
			try {
				PlayerHaterPlugin plugin = pluginClass.newInstance();
				new BoundPlayerHater(context).setBoundPlugin(plugin);
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

	/**
	 * Constructs an {@linkplain Intent} which will start the appropriate
	 * {@linkplain PlayerHaterService} as configured in the project's
	 * AndroidManifest.xml file.
	 * 
	 * @param context
	 * @return An {@link Intent} which will start the correct service.
	 * @throws IllegalArgumentException
	 *             if there is no appropriate service configured in
	 *             AndroidManifest.xml
	 */
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

	protected static final IRemotePlugin sBinderPlugin = new IRemotePlugin.Stub() {

		@Override
		public void onUnbindRequested() throws RemoteException {
			sApplicationContext.unbindService(sServiceConnection);
			sPlayerHater = null;
			for (AutoBindHandle handle : sHandles) {
				handle.unbind();
			}
		}

		@Override
		public void onTitleChanged(String title) throws RemoteException {
			sPlugin.onTitleChanged(title);
		}

		@Override
		public void onSongFinished(int songTag, int reason)
				throws RemoteException {
			sPlugin.onSongFinished(sPlayerHater.getSong(songTag), reason);
		}

		@Override
		public void onSongChanged(int songTag) throws RemoteException {
			sPlugin.onSongChanged(sPlayerHater.getSong(songTag));
		}

		@Override
		public void onNextSongUnavailable() throws RemoteException {
			sPlugin.onNextSongUnavailable();
		}

		@Override
		public void onNextSongAvailable(int songTag) throws RemoteException {
			sPlugin.onNextSongAvailable(sPlayerHater.getSong(songTag));
		}

		@Override
		public void onDurationChanged(int duration) throws RemoteException {
			sPlugin.onDurationChanged(duration);
		}

		@Override
		public void onAudioStopped() throws RemoteException {
			sPlugin.onAudioStopped();
		}

		@Override
		public void onAudioStarted() throws RemoteException {
			sPlugin.onAudioStarted();
		}

		@Override
		public void onAudioResumed() throws RemoteException {
			sPlugin.onAudioResumed();
		}

		@Override
		public void onAudioPaused() throws RemoteException {
			sPlugin.onAudioPaused();
		}

		@Override
		public void onAudioLoading() throws RemoteException {
			sPlugin.onAudioLoading();
		}

		@Override
		public void onArtistChanged(String artist) throws RemoteException {
			sPlugin.onArtistChanged(artist);
		}

		@Override
		public void onAlbumArtUriChanged(Uri uri) throws RemoteException {
			sPlugin.onAlbumArtChangedToUri(uri);
		}

		@Override
		public void onAlbumArtResourceChanged(int albumArtResource)
				throws RemoteException {
			sPlugin.onAlbumArtChanged(albumArtResource);
		}
		
		@Override
		public void onTransportControlFlagsChanged(int transportControlFlags) {
			sPlugin.onTransportControlFlagsChanged(transportControlFlags);
		}

		@Override
		public void onServiceBound(IPlayerHaterBinder binder)
				throws RemoteException {
			sPlugin.onServiceBound(binder);
		}

		@Override
		public void onIntentActivityChanged(PendingIntent intent)
				throws RemoteException {
			sPlugin.onIntentActivityChanged(intent);
		}

		@Override
		public void onChangesComplete() throws RemoteException {
			sPlugin.onChangesComplete();
		}

		@Override
		public void releaseSong(int songTag) throws RemoteException {
			sPlayerHater.releaseSong(songTag);
		}
	};

	protected static final ServiceConnection sServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			IPlayerHaterBinder sBinder = IPlayerHaterBinder.Stub
					.asInterface(service);

			try {
				sBinder.setRemotePlugin(sBinderPlugin);
			} catch (RemoteException e) {}

			sPlayerHater = BinderPlayerHater.get(sBinder);

			if (sPendingAlbumArtType != null) {
				if (sPendingAlbumArtType.equals(RESOURCE)) {
					sPlayerHater.setAlbumArt(sPendingAlbumArtResourceId);
				} else if (sPendingAlbumArtType.equals(URL)) {
					sPlayerHater.setAlbumArt(sPendingAlbumArtUrl);
				}
			}

			// XXX
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
			
			if (sPendingTransportControlFlags != 0) {
				sPlayerHater.setTransportControlFlags(sPendingTransportControlFlags);
			}

			if (sPlayQueue.getNowPlaying() != null) {
				for (Song song : sPlayQueue.getSongsBefore()) {
					sPlayerHater.enqueue(song);
				}

				Song firstSong = sPlayQueue.getNowPlaying();
				sPlayerHater.play(firstSong, sStartPosition);

				for (Song song : sPlayQueue.getSongsAfter()) {
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
		} else if (sApplicationContext != null) {
			// This condition will occur once all prebound plugins are loaded.
			
			// We attempt to bind (but without the BIND_AUTO_CREATE flag), in
			// case we previously disconnected
			// but the service continued running.
			// I really wish we had a #peekService here because it would be
			// ideal.
			if (sApplicationContext.bindService(
					buildServiceIntent(sApplicationContext),
					sServiceConnection, 0)) {
				Log.d(TAG, "I guess we are starting the service again");
			}
		}
	}
}
