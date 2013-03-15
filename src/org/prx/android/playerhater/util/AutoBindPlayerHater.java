package org.prx.android.playerhater.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;
import org.prx.android.playerhater.service.IPlayerHaterBinder;
import org.prx.android.playerhater.service.OnShutdownRequestListener;

import android.app.Activity;
import android.app.Service;
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

public class AutoBindPlayerHater implements AudioPlaybackInterface,
		OnShutdownRequestListener {
	protected static final String TAG = "PLAYERHATER";

	private static AutoBindPlayerHater sInstance;

	public static AutoBindPlayerHater getInstance() {
		if (sInstance == null) {
			sInstance = new AutoBindPlayerHater();
		}
		return sInstance;
	}

	private static final ServiceConnection sServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "CONNECTED");
			if (sInstance != null) {
				sInstance.bind((IPlayerHaterBinder) service);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "DISCONNECTED");
			if (sInstance != null) {
				sInstance.unbind();
			}
		}

	};

	private static final String RESOURCE = "resource";
	private static final String URL = "url";

	private IPlayerHaterBinder mPlayerHater;
	private Context mBoundContext;
	private final List<Song> mPlayQueue;
	private final Set<Context> mContexts;
	private final Set<ContextRefSpec> mContextRefSpecs;
	private final Map<Context, Integer> mContextRefCounts;
	private final Map<Song, Integer> mStartPositions;

	private String mPendingAlbumArtType;
	private Uri mPendingAlbumArtUrl;
	private OnErrorListener mPendingErrorListener;
	private OnSeekCompleteListener mPendingSeekListener;
	private OnPreparedListener mPendingPreparedListener;
	private OnInfoListener mPendingInfoListener;
	private OnCompletionListener mPendingCompleteListener;
	private int mPendingAlbumArtResourceId;
	private String mPendingNotificationTitle;
	private String mPendingNotificationText;
	private Activity mPendingNotificationIntentActivity;
	private OnBufferingUpdateListener mPendingBufferingListener;
	private ListenerEcho mListener;
	private Set<PlayerHaterPlugin> mPlugins;

	private AutoBindPlayerHater() {
		mPlayQueue = new ArrayList<Song>();
		mStartPositions = new HashMap<Song, Integer>();
		mListener = new ListenerEcho();
		mPlugins = new HashSet<PlayerHaterPlugin>();
		mContexts = new HashSet<Context>();
		mContextRefSpecs = new HashSet<ContextRefSpec>();
		mContextRefCounts = new HashMap<Context, Integer>();
	}

	public void addContext(Context context, int id) {
		if (!boundOn(context, id)) {
			mContextRefSpecs.add(new ContextRefSpec(context, id));
			if (mContextRefCounts.containsKey(context)) {
				mContextRefCounts.put(context,
						mContextRefCounts.get(context) + 1);
			} else {
				mContextRefCounts.put(context, 1);
			}
			mContexts.add(context);
		}
	}

	private boolean boundOn(Context context, int id) {
		return mContextRefSpecs.contains(new ContextRefSpec(context, id));
	}

	private void bind(IPlayerHaterBinder service) {
		Log.d("org.prx.remix", " I HAVE A BOUND SERVICE! YIPPEEE! ");
		mPlayerHater = service;
		mPlayerHater.registerShutdownRequestListener(this);

		if (mPendingAlbumArtType != null) {
			if (mPendingAlbumArtType.equals(RESOURCE)) {
				setAlbumArt(mPendingAlbumArtResourceId);
			} else if (mPendingAlbumArtType.equals(URL)) {
				setAlbumArt(mPendingAlbumArtUrl);
			}
		}

		for (PlayerHaterPlugin plugin : mPlugins) {
			service.registerPlugin(plugin);
		}

		service.setListener(mListener);

		if (mPendingErrorListener != null) {
			setOnErrorListener(mPendingErrorListener);
		}

		if (mPendingSeekListener != null) {
			setOnSeekCompleteListener(mPendingSeekListener);
		}

		if (mPendingPreparedListener != null) {
			setOnPreparedListener(mPendingPreparedListener);
		}

		if (mPendingInfoListener != null) {
			setOnInfoListener(mPendingInfoListener);
		}

		if (mPendingCompleteListener != null) {
			setOnCompletionListener(mPendingCompleteListener);
		}

		if (mPendingBufferingListener != null) {
			setOnBufferingUpdateListener(mPendingBufferingListener);
		}

		if (mPendingNotificationTitle != null) {
			setTitle(mPendingNotificationTitle);
		}

		if (mPendingNotificationText != null) {
			setArtist(mPendingNotificationText);
		}

		if (mPendingNotificationIntentActivity != null) {
			setActivity(mPendingNotificationIntentActivity);
		}

		if (!mPlayQueue.isEmpty()) {
			int startPosition = 0;
			Song firstSong = mPlayQueue.remove(0);
			if (mStartPositions.containsKey(firstSong)) {
				mPlayerHater.play(firstSong, mStartPositions.remove(firstSong));
			} else {
				mPlayerHater.play(firstSong, 0);
			}

			for (Song song : mPlayQueue) {
				try {
					startPosition = mStartPositions.remove(song);
				} catch (NullPointerException e) {
					startPosition = 0;
				}
				Log.d(TAG, "Enqueueing" + song);
				mPlayerHater.enqueue(song);
			}
			try {
				mPlayerHater.play(startPosition);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mPlayQueue.clear();
		}
	}

	public void requestRelease(Context context, int id, boolean resetContext) {
		if (mPlayerHater != null && context != null) {
			if (resetContext && boundOn(context, id)) {
				int count = mContextRefCounts.get(context);
				if (count > 1) {
					mContextRefCounts.put(context, count - 1);
				} else {
					mContextRefCounts.remove(context);
					mContexts.remove(context);
				}
				mContextRefSpecs.remove(new ContextRefSpec(context, id));
			}

			if (mBoundContext == context) {
				for (Context c : mContexts) {
					if (c != context) {
						mBoundContext = c;
						Intent serviceIntent = PlayerHater
								.buildServiceIntent(mBoundContext);
						context.unbindService(sServiceConnection);
						mBoundContext.bindService(serviceIntent,
								sServiceConnection, Service.BIND_AUTO_CREATE);
						return;
					}
				}
				context.unbindService(sServiceConnection);
				mPlayerHater = null;
			}
		}
	}

	private void unbind() {
		mPlayerHater = null;
	}

	private void startService() {
		if (mPlayerHater == null) {
			Context context = mContexts.iterator().next();
			context.bindService(PlayerHater.buildServiceIntent(context),
					sServiceConnection, Context.BIND_AUTO_CREATE);
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
			if (this.mPlayQueue.size() > 0) {
				Log.d("org.prx.remix", "Ooooooooooof");
				if (startTime > 0) {
					mStartPositions.put(mPlayQueue.get(0), startTime);
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
			mPlayQueue.clear();
			mStartPositions.clear();
			mStartPositions.put(song, startTime);
			mPlayQueue.add(song);
			startService();
			return true;
		} else {
			return mPlayerHater.play(song, startTime);
		}
	}

	@Override
	public boolean seekTo(int startTime) {
		if (mPlayerHater == null) {
			if (this.mPlayQueue.size() > 0) {
				mStartPositions.put(mPlayQueue.get(0), startTime);
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
		mPendingNotificationTitle = title;
		if (mPlayerHater != null) {
			mPlayerHater.setTitle(title);
		}
	}

	@Override
	public void setArtist(String artist) {
		mPendingNotificationText = artist;
		if (mPlayerHater != null) {
			mPlayerHater.setArtist(artist);
		}
	}

	@Override
	public void setActivity(Activity activity) {
		mPendingNotificationIntentActivity = activity;
		if (mPlayerHater != null) {
			mPlayerHater.setIntentActivity(activity);
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
		mPendingBufferingListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnBufferingUpdateListener(listener);
		}
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mPendingCompleteListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnCompletionListener(listener);
		}
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		mPendingInfoListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnInfoListener(listener);
		}
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mPendingSeekListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnSeekCompleteListener(listener);
		}
	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		mPendingErrorListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnErrorListener(listener);
		}
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		mPendingPreparedListener = listener;
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
		mListener.setListener(listener, withEcho);
	}

	@Override
	public Song nowPlaying() {
		if (mPlayerHater == null && mPlayQueue.size() > 0) {
			return mPlayQueue.get(0);
		} else if (mPlayerHater == null) {
			return null;
		}
		return mPlayerHater.getNowPlaying();
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
		if (mPlayerHater == null && !mPlayQueue.isEmpty()) {
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
		mPendingAlbumArtType = RESOURCE;
		mPendingAlbumArtResourceId = resourceId;
		if (mPlayerHater != null) {
			mPlayerHater.setAlbumArt(resourceId);
		}
	}

	@Override
	public void setAlbumArt(Uri url) {
		mPendingAlbumArtType = URL;
		mPendingAlbumArtUrl = url;
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
		return TransientPlayer.play((Context) mContexts.toArray()[0], url,
				isDuckable);
	}

	@Override
	public void enqueue(Song song) {
		if (mPlayerHater != null) {
			mPlayerHater.enqueue(song);
		} else {
			mPlayQueue.add(song);
		}
	}

	@Override
	public boolean skipTo(int position) {
		return false;
	}

	@Override
	public void emptyQueue() {
		if (mPlayerHater == null) {
			mPlayQueue.clear();
		} else {
			mPlayerHater.emptyQueue();
		}
	}

	// When the player asks us to let go, we can do that.
	// but we should hold onto the context we have so that
	// we can start back up again.
	@Override
	public void onShutdownRequested() {
		for (ContextRefSpec refSpec : mContextRefSpecs) {
			requestRelease(refSpec.getContext(), refSpec.getId(), false);
		}
	}

	@Override
	public void registerPlugin(PlayerHaterPlugin plugin) {
		mPlugins.add(plugin);
		if (mPlayerHater != null) {
			mPlayerHater.registerPlugin(plugin);
		}
	}

	@Override
	public void unregisterPlugin(PlayerHaterPlugin plugin) {
		mPlugins.remove(plugin);
		if (mPlayerHater != null) {
			mPlayerHater.unregisterPlugin(plugin);
		}
	}
}
