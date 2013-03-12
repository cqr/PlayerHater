package org.prx.android.playerhater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.service.OnShutdownRequestListener;
import org.prx.android.playerhater.service.PlayerHaterBinder;
import org.prx.android.playerhater.util.AudioPlaybackInterface;
import org.prx.android.playerhater.util.BasicSong;
import org.prx.android.playerhater.util.ConfigurationManager;
import org.prx.android.playerhater.util.ListenerEcho;
import org.prx.android.playerhater.util.TransientPlayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class PlayerHater implements AudioPlaybackInterface,
		OnShutdownRequestListener {
	protected static final String TAG = "PLAYERHATER";

	private static PlayerHater sPlayerHater;

	public static boolean LOCK_SCREEN_CONTROLS = false;
	public static boolean MODERN_AUDIO_FOCUS = false;
	public static boolean TOUCHABLE_NOTIFICATIONS = false;
	public static boolean EXPANDING_NOTIFICATIONS = false;
	

	public static PlayerHater get(Context context) {
		if (sPlayerHater == null) {
			sPlayerHater = new PlayerHater(context);

			Resources resources = context.getResources();
			String applicationName = context.getPackageName();

			LOCK_SCREEN_CONTROLS = ConfigurationManager.getFlag(
					applicationName, resources, "playerhater_lockscreen");
			MODERN_AUDIO_FOCUS = ConfigurationManager.getFlag(applicationName,
					resources, "playerhater_audiofocus");
			TOUCHABLE_NOTIFICATIONS = ConfigurationManager.getFlag(applicationName,
					resources, "playerhater_touchable_notification");
			EXPANDING_NOTIFICATIONS = ConfigurationManager.getFlag(applicationName,
					resources, "playerhater_expanding_notification");

		} else if (!sPlayerHater.usingContext(context)) {
			sPlayerHater.setContext(context);
		}

		return sPlayerHater;
	}

	public static void release(Context context) {
		if (sPlayerHater != null && sPlayerHater.usingContext(context)) {
			sPlayerHater.requestRelease(context, true);
		}
	}

	private static final ServiceConnection sServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "CONNECTED");
			if (sPlayerHater != null) {
				sPlayerHater.bind((PlayerHaterBinder) service);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "DISCONNECTED");
			if (sPlayerHater != null) {
				sPlayerHater.unbind();
			}
		}

	};

	private static final String RESOURCE = "resource";
	private static final String URL = "url";

	private Context mContext;
	private Intent mServiceIntent;
	private PlayerHaterBinder mPlayerHater;
	private final List<Song> mPlayQueue;
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

	private PlayerHater(Context context) {
		mPlayQueue = new ArrayList<Song>();
		mStartPositions = new HashMap<Song, Integer>();
		mListener = new ListenerEcho();
		setContext(context);
	}

	private boolean usingContext(Context context) {
		return context.equals(mContext);
	}

	private void setContext(Context context) {
		Context contextWas = mContext;
		if (contextWas != context) {
			mContext = context;
			
			mServiceIntent = new Intent("org.prx.android.playerhater.SERVICE");
			mServiceIntent.setPackage(mContext.getPackageName());
			if (mContext.getPackageManager().queryIntentServices(mServiceIntent, 0).size() == 0) {
				mServiceIntent = new Intent(mContext, PlaybackService.class);
			}

			// If we're already bound, we need to rebind with the new context.
			// the way this works, the service will never become "disconnected"
			// because it will stay running. We add another reference and then
			// remove the old one.
			if (mPlayerHater != null) {
				Log.d(TAG, "We are starting to switch the context out...");
				startService();
				requestRelease(contextWas, false);
			}
		}
	}

	private void bind(PlayerHaterBinder service) {
		mPlayerHater = service;
		mPlayerHater.registerShutdownRequestListener(this);

		if (mPendingAlbumArtType != null) {
			if (mPendingAlbumArtType.equals(RESOURCE)) {
				setAlbumArt(mPendingAlbumArtResourceId);
			} else if (mPendingAlbumArtType.equals(URL)) {
				setAlbumArt(mPendingAlbumArtUrl);
			}
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
			Song song = mPlayQueue.remove(0);
			Integer startPosition = mStartPositions.remove(song);
			try {
				mPlayerHater.play(song, startPosition == null ? 0
						: startPosition);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mPlayQueue.clear();
		}
	}

	private void schedulePlay(Song song, int startTime) {
		mPlayQueue.clear();
		if (startTime != 0) {
			mStartPositions.put(song, startTime);
		}
		mPlayQueue.add(song);
	}

	private void requestRelease(Context context, boolean resetContext) {
		if (mPlayerHater != null && context != null) {
			context.unbindService(sServiceConnection);
			// If it worked, then we no longer have a handle on the context
			// and shouldn't be fooled.
			if (resetContext && mContext == context) {
				mContext = null;
			} else if (mContext == context) {
				mPlayerHater = null;
			}
		}
	}

	private void unbind() {
		mPlayerHater = null;
	}

	private void startService() {
		if (mPlayerHater == null) {
			Log.d(TAG, "Starting a new service up");
			mContext.startService(mServiceIntent);
		}
		Log.d(TAG, "Binding to our new context");
		mContext.bindService(mServiceIntent, sServiceConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean pause() {
		if (mPlayerHater == null) {
			return false;
		} else {
			return mPlayerHater.pause();
		}
	}

	// Let's unbind the service if it is bound on "stop"
	@Override
	public boolean stop() {
		if (mPlayerHater != null) {
			return false;
		} else {
			boolean stopped = mPlayerHater.stop();
			if (stopped) {
				mContext.unbindService(sServiceConnection);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean play() {
		if (mPlayerHater == null) {
			return false;
		} else {
			return mPlayerHater.play();
		}
	}

	//XXX FIXME TODO -- handle case where it is called when player hater is already playing
	// compare to seekTo to decide how to handle return vals/vs exceptions
	@Override
	public boolean play(int startTime) {
		if (mPlayerHater == null) {
			if (this.mPlayQueue.size() > 0) { 
				Song song = this.mPlayQueue.get(0); 
				schedulePlay(song, startTime);
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
			schedulePlay(song, startTime);
			startService();

			return true;
		} else {
			return mPlayerHater.play(song, startTime);
		}
	}
	
	/// XXX FIXME TODO -- handle when called while binding/illegal states, etc. 
	@Override
	public boolean seekTo(int startTime) { 
		if (mPlayerHater == null) { 
				if (this.mPlayQueue.size() > 0) { 
				Song song = this.mPlayQueue.get(0); 
				schedulePlay(song, startTime);
				startService();
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
		return TransientPlayer.play(mContext, url, isDuckable);
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
		if (mPlayerHater != null) {
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
		requestRelease(mContext, false);
	}

}
