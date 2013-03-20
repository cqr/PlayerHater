package org.prx.android.playerhater;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.service.IPlayerHaterBinder;
import org.prx.android.playerhater.service.OnShutdownRequestListener;
import org.prx.android.playerhater.util.BasicSong;
import org.prx.android.playerhater.util.BroadcastReceiver;
import org.prx.android.playerhater.util.ClientPlayerHater;
import org.prx.android.playerhater.util.ConfigurationManager;
import org.prx.android.playerhater.util.ListenerEcho;
import org.prx.android.playerhater.util.TransientPlayer;
import android.app.Activity;
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
import android.util.Log;

public class PlayerHater implements ClientPlayerHater {
	public static final String TAG = "PLAYERHATER";
	
	private static ConfigurationManager sConfig;

	private static final int STATE_IDLE = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTING_TO_DISCONNECT = 2;
	private static final int STATE_CONNECTING_TO_DIE = 3;
	private static final int STATE_CONNECTED = 4;
	private static final int STATE_DISCONNECTING = 5;
	private static final int STATE_DISCONNECTING_TO_DIE = 6;
	private static final int STATE_DEAD = 7;

	private static final List<Song> sPlayQueue = new ArrayList<Song>();
	private static int sStartPosition = 0;

	private static final Set<PlayerHaterPlugin> sPlugins = new HashSet<PlayerHaterPlugin>();

	private static String sPendingAlbumArtType;
	private static Uri sPendingAlbumArtUrl;
	private static OnErrorListener sPendingErrorListener;
	private static OnSeekCompleteListener sPendingSeekListener;
	private static OnPreparedListener sPendingPreparedListener;
	private static OnInfoListener sPendingInfoListener;
	private static OnCompletionListener sPendingCompleteListener;
	private static int sPendingAlbumArtResourceId;
	private static String sPendingNotificationTitle;
	private static String sPendingNotificationText;
	private static Activity sPendingNotificationIntentActivity;
	private static OnBufferingUpdateListener sPendingBufferingListener;
	private static final ListenerEcho sListener = new ListenerEcho();

	private static final List<PlayerHater> sPlayerHaters = new ArrayList<PlayerHater>();
	private static final String RESOURCE = "resource";
	private static final String URL = "url";

	public static final String EXTRA_CONFIG = "config";

	private static int sIsBoundSomewhere = 0;

	public static PlayerHater get(Context context) {
		if (context instanceof Activity) {
			Activity activity = (Activity) context;
			int button = activity.getIntent().getIntExtra(
					BroadcastReceiver.REMOTE_CONTROL_BUTTON, -1);
			if (button != -1) {
				// XXX FIXME TODO
				// we have resumed the application because the media button
				// was pressed. Not sure what that means we should do here
				// for the sake of the most obvious thing, but it seems like
				// we should somehow ask the activity for the most
				// appropriate song to play.
			}
		}

		if (sConfig == null) {
			sConfig = new ConfigurationManager(context);
			for (Class<? extends PlayerHaterPlugin> pluginClass : sConfig.getPrebindPlugins()) {
				try {
					PlayerHaterPlugin plugin = pluginClass.newInstance();
					sPlugins.add(plugin);
					PlayerHater ph = new PlayerHater(context.getApplicationContext());
					plugin.onServiceStarted(context.getApplicationContext(), ph);
				} catch (Exception e) {
					Log.e(TAG, "Could not instantiate plugin " + pluginClass.getCanonicalName(), e);
				}
			}
		}

		return new PlayerHater(context);
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

	private static final OnShutdownRequestListener sShutdownListener = new OnShutdownRequestListener() {
		@Override
		public void onShutdownRequested() {
			for (PlayerHater instance : sPlayerHaters) {
				if (instance != null) {
					instance.onShutdownRequested();
				}
			}
		}
	};

	private static class PHServiceConnection implements ServiceConnection {

		private final PlayerHater myPlayerHater;
		private final int mId;

		private PHServiceConnection(PlayerHater me, int id) {
			myPlayerHater = me;
			mId = id;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d("PlayerHater", "bind succeeded for player hater id " + mId);
			IPlayerHaterBinder phService = (IPlayerHaterBinder) service;
			sIsBoundSomewhere += 1;

			switch (myPlayerHater.mState) {
			case STATE_CONNECTING:
				myPlayerHater.bind(phService);
				if (sIsBoundSomewhere == 1) {
					onFirstConnection(phService);
				}
				break;
			case STATE_CONNECTING_TO_DISCONNECT:
				myPlayerHater.bind(phService);
				// In this event, the service is asking to shut down but there
				// were some pending requests.
				// best close up shop.
				myPlayerHater.onShutdownRequested();
				break;
			case STATE_CONNECTING_TO_DIE:
				myPlayerHater.bind(phService);
				// In this case, we asked to release this playerHater while it
				// was connecting.
				myPlayerHater.release();
				break;
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			sIsBoundSomewhere -= 1;
			switch (myPlayerHater.mState) {
			case STATE_DISCONNECTING:
				myPlayerHater.unbind();
				break;
			case STATE_DISCONNECTING_TO_DIE:
				myPlayerHater.unbind();
				myPlayerHater.release();
			}
		}

		@Override
		public String toString() {
			return super.toString() + " (" + mId + ")";
		}

		private void onFirstConnection(IPlayerHaterBinder phService) {
			phService.registerShutdownRequestListener(sShutdownListener);

			if (sPendingAlbumArtType != null) {
				if (sPendingAlbumArtType.equals(RESOURCE)) {
					phService.setAlbumArt(sPendingAlbumArtResourceId);
				} else if (sPendingAlbumArtType.equals(URL)) {
					phService.setAlbumArt(sPendingAlbumArtUrl);
				}
			}

			for (PlayerHaterPlugin plugin : sPlugins) {
				phService.addPluginInstance(plugin);
			}

			phService.setListener(sListener);

			if (sPendingErrorListener != null) {
				phService.setOnErrorListener(sPendingErrorListener);
			}

			if (sPendingSeekListener != null) {
				phService.setOnSeekCompleteListener(sPendingSeekListener);
			}

			if (sPendingPreparedListener != null) {
				phService.setOnPreparedListener(sPendingPreparedListener);
			}

			if (sPendingInfoListener != null) {
				phService.setOnInfoListener(sPendingInfoListener);
			}

			if (sPendingCompleteListener != null) {
				phService.setOnCompletionListener(sPendingCompleteListener);
			}

			if (sPendingBufferingListener != null) {
				phService
						.setOnBufferingUpdateListener(sPendingBufferingListener);
			}

			if (sPendingNotificationTitle != null) {
				phService.setTitle(sPendingNotificationTitle);
			}

			if (sPendingNotificationText != null) {
				phService.setArtist(sPendingNotificationText);
			}

			if (sPendingNotificationIntentActivity != null) {
				phService.setActivity(sPendingNotificationIntentActivity);
			}

			if (!sPlayQueue.isEmpty()) {
				Song firstSong = sPlayQueue.remove(0);
				phService.play(firstSong, sStartPosition);

				for (Song song : sPlayQueue) {
					phService.enqueue(song);
				}
				sPlayQueue.clear();
			}

			for (PlayerHater instance : sPlayerHaters) {
				if (instance != null && instance != myPlayerHater) {
					if (instance.mState == STATE_IDLE) {
						instance.startService();
					}
				}
			}
		}
	}

	private IPlayerHaterBinder mPlayerHater;
	private Context mContext;

	private int mId = sPlayerHaters.size() + 1;
	private final ServiceConnection mServiceConnection = new PHServiceConnection(
			this, mId);
	private int mState = STATE_IDLE;

	public PlayerHater(Context context) {
		mContext = context;
		sPlayerHaters.add(this);
		if (sIsBoundSomewhere > 0) {
			startService();
		}
	}

	public String getId() {
		return "" + mId;
	}

	private void bind(IPlayerHaterBinder service) {
		Log.d("PlayerHater", "And we are locked to it on " + mId);
		mPlayerHater = service;
		mState = STATE_CONNECTED;
	}

	private void unbind() {
		mPlayerHater = null;
		mState = STATE_IDLE;
	}

	private void startService() {
		Log.d("PlayerHater", "got a request to bind on player hater " + mId);
		if (mState == STATE_IDLE) {
			if (mContext.bindService(buildServiceIntent(mContext),
					mServiceConnection, Context.BIND_AUTO_CREATE)) {
				mState = STATE_CONNECTING;
			}
		}
	}

	@Override
	public boolean pause() {
		if (mPlayerHater == null) {
			return false;
		} else {
			return mPlayerHater.pause();
		}
	}

	@Override
	public boolean stop() {
		if (mPlayerHater == null) {
			return true;
		} else {
			return mPlayerHater.stop();
		}
	}

	@Override
	public boolean play() {
		if (mPlayerHater != null) {
			return mPlayerHater.play();
		} else {
			return play(0);
		}
	}

	@Override
	public boolean play(int startTime) {
		if (mPlayerHater == null) {
			if (sPlayQueue.size() > 0) {
				if (startTime > 0) {
					sStartPosition = startTime;
				}
				startService();
				return true;
			} else {
				throw new IllegalStateException();
			}
		} else {
			return mPlayerHater.play(startTime);
		}
	}

	@Override
	public boolean play(Uri url) {
		return play(new BasicSong(url, null, null, null), 0);
	}

	@Override
	public boolean play(Uri url, int startTime) {
		return play(new BasicSong(url, null, null, null), startTime);
	}

	@Override
	public boolean play(Song song) {
		return play(song, 0);
	}

	@Override
	public boolean play(Song song, int startTime) {
		if (mPlayerHater == null) {
			sPlayQueue.clear();
			sStartPosition = startTime;
			sPlayQueue.add(song);
			startService();
			return true;
		} else {
			return mPlayerHater.play(song, startTime);
		}
	}

	@Override
	public boolean seekTo(int startTime) {
		if (mPlayerHater == null) {
			if (sPlayQueue.size() > 0) {
				sStartPosition = startTime;
				return true;
			} else {
				return false;
			}
		} else {
			mPlayerHater.seekTo(startTime);
			return true;
		}
	}

	@Override
	public void setTitle(String title) {
		sPendingNotificationTitle = title;
		if (mPlayerHater != null) {
			mPlayerHater.setTitle(title);
		}
	}

	@Override
	public void setArtist(String artist) {
		sPendingNotificationText = artist;
		if (mPlayerHater != null) {
			mPlayerHater.setArtist(artist);
		}
	}

	@Override
	public void setActivity(Activity activity) {
		sPendingNotificationIntentActivity = activity;
		if (mPlayerHater != null) {
			mPlayerHater.setActivity(activity);
		}
	}

	@Override
	public int getCurrentPosition() {
		if (mPlayerHater == null) {
			return 0;
		}
		return mPlayerHater.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		if (mPlayerHater == null) {
			return 0;
		}
		return mPlayerHater.getDuration();
	}

	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		sPendingBufferingListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnBufferingUpdateListener(listener);
		}
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		sPendingCompleteListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnCompletionListener(listener);
		}
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		sPendingInfoListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnInfoListener(listener);
		}
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		sPendingSeekListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnSeekCompleteListener(listener);
		}
	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		sPendingErrorListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnErrorListener(listener);
		}
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		sPendingPreparedListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnPreparedListener(listener);
		}
	}

	@Override
	public void setListener(PlayerHaterListener listener) {
		setListener(listener, true);
	}

	@Override
	public void setListener(PlayerHaterListener listener, boolean withEcho) {
		sListener.setListener(listener, withEcho);
	}

	@Override
	public Song nowPlaying() {
		if (mPlayerHater == null && sPlayQueue.size() > 0) {
			return sPlayQueue.get(0);
		} else if (mPlayerHater == null) {
			return null;
		}
		return mPlayerHater.nowPlaying();
	}

	@Override
	public boolean isPlaying() {
		if (mPlayerHater == null) {
			return false;
		}
		return mPlayerHater.isPlaying();
	}

	@Override
	public boolean isLoading() {
		if (mPlayerHater == null && !sPlayQueue.isEmpty()) {
			return true;
		} else if (mPlayerHater == null) {
			return false;
		}
		return mPlayerHater.isLoading();
	}

	@Override
	public int getState() {
		if (mPlayerHater == null) {
			return Player.IDLE;
		}
		return mPlayerHater.getState();
	}

	@Override
	public void setAlbumArt(int resourceId) {
		sPendingAlbumArtType = RESOURCE;
		sPendingAlbumArtResourceId = resourceId;
		if (mPlayerHater != null) {
			mPlayerHater.setAlbumArt(resourceId);
		}
	}

	@Override
	public void setAlbumArt(Uri url) {
		sPendingAlbumArtType = URL;
		sPendingAlbumArtUrl = url;
		if (mPlayerHater != null) {
			mPlayerHater.setAlbumArt(url);
		}
	}

	@Override
	public TransientPlayer playEffect(Uri url) {
		return playEffect(url, true);
	}

	@Override
	public TransientPlayer playEffect(Uri url, boolean isDuckable) {
		return TransientPlayer.play(mContext, url, isDuckable);
	}

	@Override
	public void enqueue(Song song) {
		if (mPlayerHater != null) {
			mPlayerHater.enqueue(song);
		} else {
			sPlayQueue.add(song);
		}
	}

	@Override
	public boolean skipTo(int position) {
		// XXX TODO FIXME OBVIOUSLY
		return false;
	}

	@Override
	public void emptyQueue() {
		if (mPlayerHater == null) {
			sPlayQueue.clear();
		} else {
			mPlayerHater.emptyQueue();
		}
	}

	private void onShutdownRequested() {
		if (mState == STATE_CONNECTED) {
			mState = STATE_DISCONNECTING;
			mContext.unbindService(mServiceConnection);
		} else if (mState == STATE_CONNECTING) {
			mState = STATE_CONNECTING_TO_DISCONNECT;
		}
	}

	public void release() {
		Log.d("PlayerHater", "We got a request to shut down id " + mId);
		Log.d("PlayerHater", "Current state is " + mState);
		switch (mState) {
		case STATE_IDLE:
			delist();
			kill();
			break;
		case STATE_CONNECTING:
		case STATE_CONNECTING_TO_DISCONNECT:
			mState = STATE_CONNECTING_TO_DIE;
			delist();
			break;
		case STATE_CONNECTED:
			onShutdownRequested();
			mState = STATE_DISCONNECTING_TO_DIE;
			delist();
			break;
		case STATE_DISCONNECTING:
			mState = STATE_DISCONNECTING_TO_DIE;
			delist();
			break;
		}
	}

	private void kill() {
		Log.d("PlayerHater", "Kill requested for " + mId);
		Log.d("PlayerHater", "Current state is " + mState);
		mState = STATE_DEAD;
		mContext = null;
	}

	private void delist() {
		int index = sPlayerHaters.indexOf(this);
		if (index != -1)
			sPlayerHaters.set(index, null);
	}

	@Override
	public void skip() {
		if (mPlayerHater != null) {
			mPlayerHater.skip();
		} else if (sPlayQueue.size() > 1) {
			sPlayQueue.remove(0);
		}
	}

	@Override
	public void skipBack() {
		if (mPlayerHater != null) {
			mPlayerHater.skipBack();
		}
	}
}
