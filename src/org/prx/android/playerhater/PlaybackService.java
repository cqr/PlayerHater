package org.prx.android.playerhater;

import java.io.IOException;
import org.prx.android.playerhater.lifecycle.AudioFocusHandler;
import org.prx.android.playerhater.lifecycle.ListenerCollection;
import org.prx.android.playerhater.lifecycle.MediaButtonHandler;
import org.prx.android.playerhater.lifecycle.ModernNotificationHandler;
import org.prx.android.playerhater.lifecycle.RemoteControlClientHandler;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;


public class PlaybackService extends Service implements OnErrorListener,
		OnPreparedListener, OnSeekCompleteListener, OnCompletionListener {

	protected static final String TAG = "PlayerHater/Service";
	protected static final int PROGRESS_UPDATE = 9747244;

	protected Song mNowPlaying;
	private MediaPlayerWrapper mediaPlayer;
	private UpdateProgressRunnable updateProgressRunner;
	private Thread updateProgressThread;
	private BroadcastReceiver mBroadcastReceiver;
	private PlayerListenerManager playerListenerManager;
	private OnSeekCompleteListener mOnSeekCompleteListener;
	private OnPreparedListener mOnPreparedListener;
	private PlayerHaterListener mPlayerHaterListener;
	private OnCompletionListener mOnCompletionListener;
	private OnErrorListener mOnErrorListener;

	private ListenerCollection mLifecycleListener;
	private boolean playAfterSeek;
	private boolean mSeekOnStart = false;
	private int mStartTime = 0;

	private Handler mHandler = new UpdateHandler(this);

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		if (playerListenerManager == null) {
			playerListenerManager = createPlayerListenerManager(this);
		}

		if (mediaPlayer == null) {
			mediaPlayer = createMediaPlayer(this);
			playerListenerManager.setMediaPlayer(mediaPlayer);
		}

		if (updateProgressRunner == null) {
			updateProgressRunner = createUpdateProgressRunner(mediaPlayer,
					mHandler);
		}

		if (mLifecycleListener == null) {
			mLifecycleListener = new ListenerCollection();
			if (PlayerHater.MODERN_NOTIFICATION) {
				mLifecycleListener.add(new ModernNotificationHandler(this));
			}
			if (PlayerHater.MODERN_AUDIO_FOCUS) {
				mLifecycleListener.add(new AudioFocusHandler(this));
				mLifecycleListener.add(new MediaButtonHandler(this));
			}
			if (PlayerHater.LOCK_SCREEN_CONTROLS) {
				mLifecycleListener.add(new RemoteControlClientHandler(this));
			}
		}

		if (mBroadcastReceiver == null) {
			mBroadcastReceiver = new BroadcastReceiver(this);
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_HEADSET_PLUG);
			filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
			filter.addAction(Intent.ACTION_MEDIA_BUTTON);
			filter.setPriority(10000);
			getBaseContext().registerReceiver(mBroadcastReceiver, filter);
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return new PlayerHaterBinder(this, playerListenerManager);
	}

	public boolean pause() throws IllegalStateException {
		mediaPlayer.pause();
		stopProgressThread();
		sendIsPaused();
		return true;
	}

	public int getState() {
		return mediaPlayer.getState();
	}

	public Song getNowPlaying() {
		return mNowPlaying;
	}

	public boolean isPlaying() {
		return (mediaPlayer.getState() == MediaPlayerWrapper.STARTED);
	}

	public boolean isPaused() {
		return (mediaPlayer.getState() == MediaPlayerWrapper.PAUSED);
	}

	public boolean isLoading() {
		return (mediaPlayer.getState() == MediaPlayerWrapper.INITIALIZED
				|| mediaPlayer.getState() == MediaPlayerWrapper.PREPARING || mediaPlayer
					.getState() == MediaPlayerWrapper.PREPARED);
	}

	public boolean play(Song song) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		return play(song, 0);
	}

	public boolean play(Song song, int startTime) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		mNowPlaying = song;
		if (mediaPlayer.getState() != MediaPlayerWrapper.IDLE)
			reset();
		mediaPlayer.setDataSource(getApplicationContext(), getNowPlaying()
				.getUri());
		return play(startTime);
	}
	
	public boolean play(int startTime) throws IllegalStateException,
			IOException {
		mSeekOnStart = (startTime != 0);
		mStartTime = startTime * 1000;
		return play();
	}

	private void stopProgressThread() {
		if (updateProgressThread != null && updateProgressThread.isAlive()) {
			mHandler.removeCallbacks(updateProgressRunner);
			updateProgressThread.interrupt();
			updateProgressThread = null;
		}
	}

	private void startProgressThread() {
		stopProgressThread();
		updateProgressThread = new Thread(updateProgressRunner);
		updateProgressThread.start();
	}

	public boolean play() throws IllegalStateException, IOException {
		switch (mediaPlayer.getState()) {
		case MediaPlayerWrapper.INITIALIZED:
		case MediaPlayerWrapper.STOPPED:
			performPrepare();
			break;
		case MediaPlayerWrapper.PREPARED:
		case MediaPlayerWrapper.PAUSED:
			mediaPlayer.start();
			sendStartedPlaying();
			startProgressThread();
			break;
		case MediaPlayerWrapper.IDLE:
			play(getNowPlaying());
			break;
		default:
			throw new IllegalStateException("State is "
					+ mediaPlayer.getState());
		}
		return true;
	}

	private void reset() {
		Log.d(TAG, "Resetting media player.");
		mediaPlayer.reset();
	}

	public int getDuration() {
		return mediaPlayer.getDuration();
	}

	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	public void seekTo(int pos) {
		playAfterSeek = (getState() == MediaPlayerWrapper.STARTED);

		try {
			mediaPlayer.pause();
			sendIsLoading();
			mediaPlayer.seekTo(pos);
		} catch (java.lang.IllegalStateException e) {
			// do nothing
		}
	}

	private void performPrepare() {
		Log.d(TAG, "Starting preparation of: " + getNowPlaying());
		sendIsLoading();
		mediaPlayer.prepareAsync();

		startProgressThread();
	}

	public boolean stop() {
		mediaPlayer.stop();
		sendIsStopped();
		stopProgressThread();
		getBaseContext().unregisterReceiver(mBroadcastReceiver);
		stopSelf();
		return true;
	}

	/*
	 * We proxy these events.
	 */
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mOnSeekCompleteListener = listener;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		if (playAfterSeek) {
			try {
				play();
			} catch (Exception e) {
				// oof.
			}
		}
		if (mOnSeekCompleteListener != null)
			mOnSeekCompleteListener.onSeekComplete(mp);
	}

	public void setOnPreparedListener(OnPreparedListener listener) {
		mOnPreparedListener = listener;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mediaPlayer.start();

		if (mSeekOnStart && mStartTime > 0) {
			mSeekOnStart = false;
			seekTo(mStartTime);
		}
		sendStartedPlaying();
		if (mOnPreparedListener != null) {
			mOnPreparedListener.onPrepared(mp);
		}
	}

	public void setOnErrorListener(OnErrorListener listener) {
		mOnErrorListener = listener;
	}

	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e(TAG, "Got MediaPlayer error: " + what + " / " + extra);
		stopProgressThread();
		reset();
		if (mOnErrorListener != null) {
			Log.e(TAG, "Passing error along.");
			return mOnErrorListener.onError(mp, what, extra);
		}
		return false;
	}

	/*
	 * This should be overridden by subclasses which wish to handle messages
	 * sent to mHandler without re-implementing the handler. It is a noop by
	 * default.
	 */
	protected void onHandlerMessage(Message m) { /* noop */
	}

	/*
	 * These are the events we send back to PlayerHaterListener;
	 */
	private void sendStartedPlaying() {
		mLifecycleListener.start(getNowPlaying(), getDuration());
		sendIsPlaying(getCurrentPosition());
	}

	private void sendIsPlaying(int progress) {
		if (getState() == MediaPlayerWrapper.STARTED
				&& mPlayerHaterListener != null) {
			mPlayerHaterListener.onPlaying(getNowPlaying(), progress);
		}
	}

	private void sendIsLoading() {
		if (mPlayerHaterListener != null) {
			mPlayerHaterListener.onLoading(getNowPlaying());
		}
	}

	private void sendIsPaused() {
		mLifecycleListener.setIsPlaying(false);
		if (mPlayerHaterListener != null) {
			mPlayerHaterListener.onPaused(getNowPlaying());
		}
	}

	private void sendIsStopped() {
		mLifecycleListener.stop();
		if (mPlayerHaterListener != null) {
			mPlayerHaterListener.onStopped();
		}
	}

	public void setListener(PlayerHaterListener listener) {
		mPlayerHaterListener = listener;
	}

	public void duck() {
		Log.d(TAG, "Ducking...");
		mediaPlayer.setVolume(0.1f, 0.1f);
	}

	public void unduck() {
		Log.d(TAG, "Unducking...");
		mediaPlayer.setVolume(1.0f, 1.0f);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		stopProgressThread();
		if (mOnCompletionListener != null) {
			mOnCompletionListener.onCompletion(mp);
		}
		stop();
	}

	/*
	 * creates a media player (wrapped, of course) and registers the listeners
	 * for all of the events.
	 */
	protected static MediaPlayerWrapper createMediaPlayer(
			PlaybackService service) {
		return new MediaPlayerWrapper();
	}

	/*
	 * creates a new update progress runner, which fires events back to this
	 * class' handler with the message we request and the duration which has
	 * passed
	 */
	protected static UpdateProgressRunnable createUpdateProgressRunner(
			MediaPlayerWrapper mediaPlayer, Handler handler) {
		return new UpdateProgressRunnable(mediaPlayer, handler, PROGRESS_UPDATE);
	}

	/*
	 * This class basically just makes sure that we never need to re-bind
	 * ourselves.
	 */
	protected static PlayerListenerManager createPlayerListenerManager(
			PlaybackService service) {
		PlayerListenerManager mgr = new PlayerListenerManager();
		mgr.setOnErrorListener(service);
		mgr.setOnSeekCompleteListener(service);
		mgr.setOnPreparedListener(service);
		mgr.setOnCompletionListener(service);
		return mgr;
	}

	public void setTitle(String title) {
		mLifecycleListener.setTitle(title);
	}

	public void setArtist(String artist) {
		mLifecycleListener.setArtist(artist);
	}

	public void setIntentClass(Class<? extends Activity> klass) {
		// XXX: TODO FIXME
		// NOOP FOR NOW;
	}

	public void setAlbumArt(int resourceId) {
		mLifecycleListener.setAlbumArt(resourceId);
	}

	public void setAlbumArt(Uri url) {
		mLifecycleListener.setAlbumArt(url);
	}
	
	private static class UpdateHandler extends Handler {
			private PlaybackService mService;
			
			private UpdateHandler(PlaybackService service) {
				mService = service;
			}
		
			@Override
			public void handleMessage(Message m) {
				switch (m.what) {
				case PROGRESS_UPDATE:
					mService.sendIsPlaying(m.arg1);
					break;
				default:
					mService.onHandlerMessage(m);
				}
		}
	}

}
