package org.prx.android.playerhater.service;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.MediaPlayerWithState;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.playerhater.ServicePlayerHater;
import org.prx.android.playerhater.plugins.RemotePlugin;
import org.prx.android.playerhater.plugins.IRemotePlugin;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.plugins.PluginCollection;
import org.prx.android.playerhater.util.BasicSong;
import org.prx.android.playerhater.util.BroadcastReceiver;
import org.prx.android.playerhater.util.Config;
import org.prx.android.playerhater.util.Log;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.KeyEvent;

public abstract class AbsPlaybackService extends Service implements
		PlayerHaterService {

	protected static final int REMOTE_PLUGIN = 2525;
	public static String TAG = PlayerHater.TAG;
	protected BroadcastReceiver mBroadcastReceiver;
	protected PlayerHaterPlugin mPlugin;
	private Config mConfig;
	private PluginCollection mPluginCollection;
	private final PlayerHater mPlayerHater = new ServicePlayerHater(this);
	private IRemotePlugin mPluginBinder;
	private ReferenceQueue<Song> mSongReferenceQueue = new ReferenceQueue<Song>();

	private final IPlayerHaterBinder.Stub mRemoteBinder = new IPlayerHaterBinder.Stub() {

		@Override
		public int enqueue(Uri uri, String title, String artist,
				Uri albumArt, int tag) throws RemoteException {
			Song song = new BasicSong(uri, title, artist, albumArt, tag);
			new PhantomSongReference(song, mSongReferenceQueue);
			return AbsPlaybackService.this.enqueue(song);
		}

		@Override
		public boolean skipTo(int position) throws RemoteException {
			return AbsPlaybackService.this.skipTo(position);
		}

		@Override
		public boolean pause() throws RemoteException {
			return AbsPlaybackService.this.pause();
		}

		@Override
		public boolean stop() throws RemoteException {
			return AbsPlaybackService.this.stop();
		}

		@Override
		public boolean play(int startTime) throws RemoteException {
			return AbsPlaybackService.this.play(startTime);
		}

		@Override
		public boolean seekTo(int startTime) throws RemoteException {
			return AbsPlaybackService.this.seekTo(startTime);
		}

		@Override
		public void setAlbumArtResource(int resourceId) throws RemoteException {
			AbsPlaybackService.this.setAlbumArt(resourceId);
		}

		@Override
		public void setAlbumArtUrl(Uri albumArt) throws RemoteException {
			AbsPlaybackService.this.setAlbumArt(albumArt);
		}

		@Override
		public void setTitle(String title) throws RemoteException {
			AbsPlaybackService.this.setTitle(title);
		}

		@Override
		public void setArtist(String artist) throws RemoteException {
			AbsPlaybackService.this.setArtist(artist);
		}

		@Override
		public void setTransportControlFlags(int transportControlFlags)
				throws RemoteException {
			AbsPlaybackService.this
					.setTransportControlFlags(transportControlFlags);

		}

		@Override
		public int getDuration() throws RemoteException {
			return AbsPlaybackService.this.getDuration();
		}

		@Override
		public int getCurrentPosition() throws RemoteException {
			return AbsPlaybackService.this.getCurrentPosition();
		}

		@Override
		public int getNowPlayingTag() throws RemoteException {
			if (getNowPlaying() != null) {
				return ((BasicSong) getNowPlaying()).tag;
			}
			return 0;
		}

		@Override
		public boolean isPlaying() throws RemoteException {
			return AbsPlaybackService.this.isPlaying();
		}

		@Override
		public boolean isLoading() throws RemoteException {
			return AbsPlaybackService.this.isLoading();
		}

		@Override
		public int getState() throws RemoteException {
			return AbsPlaybackService.this.getState();
		}

		@Override
		public void setRemotePlugin(IRemotePlugin binder)
				throws RemoteException {
			mPluginCollection.remove(REMOTE_PLUGIN);

			if (binder != null) {
				binder.onServiceBound(this);
				mPluginBinder = binder;
				mPluginCollection.add(new RemotePlugin(binder), REMOTE_PLUGIN);
			}
		}

		@Override
		public int getQueueLength() throws RemoteException {
			return AbsPlaybackService.this.getQueueLength();
		}

		@Override
		public void emptyQueue() throws RemoteException {
			AbsPlaybackService.this.emptyQueue();
		}

		@Override
		public boolean skip() throws RemoteException {
			return AbsPlaybackService.this.skip();
		}

		@Override
		public boolean skipBack() throws RemoteException {
			return AbsPlaybackService.this.skipBack();
		}

		@Override
		public boolean resume() throws RemoteException {
			return AbsPlaybackService.this.play();
		}

		@Override
		public void onRemoteControlButtonPressed(int keyCode) {
			AbsPlaybackService.this.onRemoteControlButtonPressed(keyCode);
		}

		@TargetApi(Build.VERSION_CODES.ECLAIR)
		@Override
		public void startForeground(int notificationNu,
				Notification notification) throws RemoteException {
			Log.d("Starting the notification");
			AbsPlaybackService.this.startForeground(notificationNu,
					notification);
		}

		@TargetApi(Build.VERSION_CODES.ECLAIR)
		@Override
		public void stopForeground(boolean fact) throws RemoteException {
			AbsPlaybackService.this.stopForeground(fact);
		}

		@Override
		public void duck() throws RemoteException {
			AbsPlaybackService.this.duck();
		}

		@Override
		public void unduck() throws RemoteException {
			AbsPlaybackService.this.unduck();
		}
	};

	abstract Player getMediaPlayer();

	/* The Service Life Cycle */

	@Override
	public void onCreate() {
		TAG = getPackageName() + "/PH/" + getClass().getSimpleName();
		mBroadcastReceiver = new BroadcastReceiver(this, mRemoteBinder);
		mPluginCollection = new PluginCollection();
		mPlugin = mPluginCollection;
		Intent intent = new Intent(getBaseContext(), getClass());
		getBaseContext().startService(intent);
	}

	@Override
	public void onDestroy() {
		onStopped();
		mConfig = null;
		mPlugin.onServiceStopping();
		getMediaPlayer().release();
		getBaseContext().unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	public IBinder onBind(Intent intent) {
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
				mPluginCollection.add(plugin);
				Log.d("Plugins: " + mPluginCollection.getSize());
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
			songTags[i] = ((BasicSong) song).tag;
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

	/* Plug-In Stuff */

	@Override
	public void addPluginInstance(PlayerHaterPlugin plugin) {
		mPluginCollection.add(plugin);
	}

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
	public void setTransportControlFlags(int transportControlFlags) {
		mPlugin.onTransportControlFlagsChanged(transportControlFlags);
	}

	@Override
	public void setIntentClass(Class<? extends Activity> klass) {
		Intent intent = new Intent(getApplicationContext(), klass);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pending = PendingIntent.getActivity(getBaseContext(),
				456, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		mPlugin.onIntentActivityChanged(pending);
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
		mPlugin.onAudioStopped();
	}

	protected void onPaused() {
		mPlugin.onAudioPaused();
	}

	protected void onLoading() {
		mPlugin.onAudioLoading();
	}

	protected void onStarted() {
		mPlugin.onAudioStarted();
		mPlugin.onSongChanged(getNowPlaying());
		mPlugin.onDurationChanged(getDuration());
	}

	protected void onResumed() {
		mPlugin.onAudioResumed();
		clearSongReferences();
	}

	/* END Events for Subclasses */

	private void clearSongReferences() {
		PhantomSongReference ref = (PhantomSongReference) mSongReferenceQueue
				.poll();
		if (ref != null) {
			try {
				mPluginBinder.releaseSong(ref.tag);
			} catch (RemoteException e) {}
		}
	}

	private static class PhantomSongReference extends PhantomReference<Song> {
		public final int tag;

		public PhantomSongReference(Song r, ReferenceQueue<? super Song> q) {
			super(r, q);
			tag = ((BasicSong) r).tag;
		}

	}
}
