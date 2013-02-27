package org.prx.android.playerhater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class PlayerHater implements AudioPlaybackInterface {
	private static PlayerHater sPlayerHater;

	public static PlayerHater get(Context context) {
		if (sPlayerHater == null) {
			sPlayerHater = new PlayerHater(context);
		} else if (!sPlayerHater.usingContext(context)) {
			sPlayerHater.setContext(context);
		}

		return sPlayerHater;
	}

	private static final ServiceConnection sServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (sPlayerHater != null) {
				sPlayerHater.bind((PlayerHaterBinder) service);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
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
		mContext = context;
		mServiceIntent = new Intent(mContext, PlaybackService.class);

		// If we're already bound (but in another context), we should rebind on
		// this one.
		if (mPlayerHater != null) {
			startService();
		}
	}

	private void bind(PlayerHaterBinder service) {
		mPlayerHater = service;

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

	private void unbind() {
		mPlayerHater = null;
	}

	private void startService() {
		if (mPlayerHater == null) {
			mContext.startService(mServiceIntent);
		}
		mContext.bindService(mServiceIntent, sServiceConnection, 0);
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
	public boolean play() throws IllegalStateException, IOException {
		if (mPlayerHater == null) {
			throw new IllegalStateException();
		} else {
			return mPlayerHater.play();
		}
	}

	@Override
	public boolean play(int startTime) throws IllegalStateException,
			IOException {
		if (mPlayerHater == null) {
			throw new IllegalStateException();
		} else {
			return mPlayerHater.play(startTime);
		}
	}

	@Override
	public boolean play(Uri url) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		return play(new BasicSong(url, null, null, null), 0);
	}

	@Override
	public boolean play(Uri url, int startTime) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		return play(new BasicSong(url, null, null, null), startTime);
	}

	@Override
	public boolean play(Song song) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		return play(song, 0);
	}

	@Override
	public boolean play(Song song, int startTime) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		if (mPlayerHater == null) {
			schedulePlay(song, startTime);
			startService();

			return true;
		} else {
			return mPlayerHater.play(song, startTime);
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
		if (mPlayerHater == null) {
			return mPlayQueue.get(0);
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
			return MediaPlayerWrapper.IDLE;
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

}
