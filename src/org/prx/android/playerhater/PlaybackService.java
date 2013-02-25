package org.prx.android.playerhater;

import java.io.FileDescriptor;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.RemoteControlClient;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

@SuppressLint("NewApi")
public class PlaybackService extends Service implements OnErrorListener,
		OnPreparedListener, OnSeekCompleteListener, OnCompletionListener {

	protected static final String TAG = "PlayerHater/Service";
	protected static final int PROGRESS_UPDATE = 9747244;

	protected Song nowPlaying;

	private MediaPlayerWrapper mediaPlayer;
	private UpdateProgressRunnable updateProgressRunner;
	private Thread updateProgressThread;
	private BroadcastReceiver mBroadcastReceiver;
	private PlayerListenerManager playerListenerManager;
	private OnErrorListener mOnErrorListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;
	private OnPreparedListener mOnPreparedListener;
	private AudioManager mAudioManager;
	private ComponentName mRemoteControlResponder;
	private PlayerHaterListener mPlayerHaterListener;
	private OnAudioFocusChangeListener mAudioFocusChangeListener;
	private OnCompletionListener mOnCompletionListener;
	private RemoteControlClient mRemoteControlClient;

	private String lockScreenTitle;
	private Bitmap lockScreenImage;

	private NotificationHandler mNotificationHandler;
	private boolean playAfterSeek;
	private boolean seekOnStart = false;
	private int startTime = 0;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {

			switch (m.what) {
			case PROGRESS_UPDATE:
				sendIsPlaying(m.arg1);
				break;
			default:
				onHandlerMessage(m);
			}
		}
	};

	@SuppressLint("NewApi")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "I AM BEING ASKED TO START");

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

		if (mAudioManager == null) {
			mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		}

		if (mAudioFocusChangeListener == null) {
			mAudioFocusChangeListener = createAudioFocusChangeListener(this);
		}

		if (mNotificationHandler == null) {
			mNotificationHandler = createNotificationHandler(this);
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
		mRemoteControlResponder = new ComponentName(getPackageName(),
				BroadcastReceiver.class.getName());
		mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);
		// build the PendingIntent for the remote control client
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setComponent(mRemoteControlResponder);
		PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, mediaButtonIntent, 0);
		// create and register the remote control client
		try {
			mRemoteControlClient = new RemoteControlClient(mediaPendingIntent);
			mAudioManager.registerRemoteControlClient(mRemoteControlClient);
		} catch (NoClassDefFoundError e) {

		} catch (java.lang.NoSuchMethodError e) {

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
		mNotificationHandler.setToPlay();
		try {
			mRemoteControlClient
					.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
		} catch (NoClassDefFoundError e) {

		} catch (java.lang.NoSuchMethodError e) {

		}
		return true;
	}

	public void setLockScreenImage(FileDescriptor fd) {
		if (fd != null) {
			this.lockScreenImage = BitmapFactory.decodeFileDescriptor(fd);
		}
	}

	public void setLockScreenImage(Resources res, int id) {
		if (res != null) {
			this.lockScreenImage = BitmapFactory.decodeResource(res, id);
		}
	}

	public void setLockScreenTitle(String title) {
		this.lockScreenTitle = title;
	}

	public RemoteControlClient getRemoteControlClient() {
		return this.mRemoteControlClient;
	}

	public int getState() {
		return mediaPlayer.getState();
	}

	public Song getNowPlaying() {
		return nowPlaying;
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
		nowPlaying = song;
		if (mediaPlayer.getState() != MediaPlayerWrapper.IDLE) {
			reset();
		}
		mediaPlayer.setDataSource(getApplicationContext(), nowPlaying.getUri());
		return play();

	}

	public boolean play(Song song, int startTime) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		nowPlaying = song;
		if (mediaPlayer.getState() != MediaPlayerWrapper.IDLE)
			reset();
		mediaPlayer.setDataSource(getApplicationContext(), nowPlaying.getUri());
		return play(startTime);

	}

	public boolean play(int startTime) throws IllegalStateException,
			IOException {
		seekOnStart = true;
		this.startTime = startTime * 1000;
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
			sendIsPlaying();
			mNotificationHandler.resume();
			startProgressThread();
			break;
		case MediaPlayerWrapper.IDLE:
			play(nowPlaying);
			break;
		default:
			throw new IllegalStateException("State is "
					+ mediaPlayer.getState());
		}
		try {
			mAudioManager
					.registerMediaButtonEventReceiver(mRemoteControlResponder);
			if (this.lockScreenTitle != null && this.lockScreenImage != null) {
				mRemoteControlClient
						.editMetadata(true)
						.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
								this.lockScreenTitle)
						.putBitmap(100, this.lockScreenImage).apply();
			} else if (this.lockScreenTitle != null) {
				mRemoteControlClient
						.editMetadata(true)
						.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
								this.lockScreenTitle).apply();
			} else if (this.lockScreenImage != null) {
				mRemoteControlClient.editMetadata(true)
						.putBitmap(100, this.lockScreenImage).apply();
			}
			mRemoteControlClient
					.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
			mRemoteControlClient
					.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
							| RemoteControlClient.FLAG_KEY_MEDIA_STOP);
			mAudioManager.registerRemoteControlClient(mRemoteControlClient);
		} catch (NoClassDefFoundError e) {

		} catch (java.lang.NoSuchMethodError e) {

		}
		this.mNotificationHandler.setToPause();
		return true;

	}

	public void resetLockScreenControls() {
		try {
			mAudioManager
					.registerMediaButtonEventReceiver(mRemoteControlResponder);
			if (this.lockScreenTitle != null && this.lockScreenImage != null) {
				mRemoteControlClient
						.editMetadata(true)
						.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
								this.lockScreenTitle)
						.putBitmap(100, this.lockScreenImage).apply();
			} else if (this.lockScreenTitle != null) {
				mRemoteControlClient
						.editMetadata(true)
						.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
								this.lockScreenTitle).apply();
			} else if (this.lockScreenImage != null) {
				mRemoteControlClient.editMetadata(true)
						.putBitmap(100, this.lockScreenImage).apply();
			}
			mRemoteControlClient
					.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
							| RemoteControlClient.FLAG_KEY_MEDIA_STOP);
			mAudioManager.registerRemoteControlClient(mRemoteControlClient);
			if (this.isPlaying() || this.isLoading()) {
				mRemoteControlClient
						.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
			} else {
				mRemoteControlClient
						.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
			}
		} catch (NoClassDefFoundError e) {

		} catch (java.lang.NoSuchMethodError e) {

		}
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
		playAfterSeek = false;

		if (getState() == MediaPlayerWrapper.STARTED)
			playAfterSeek = true;

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
		mNotificationHandler.stopNotification();
		sendIsStopped();
		stopProgressThread();
		getBaseContext().unregisterReceiver(mBroadcastReceiver);
		mAudioManager
				.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
		try {
			mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
		} catch (NoClassDefFoundError e) {

		} catch (java.lang.NoSuchMethodError e) {

		}
		super.stopSelf();
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
		Log.d(TAG, getNowPlaying().toString());
		mNotificationHandler.startNotification(getNowPlaying());
		sendIsPlaying();

		mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		if (mOnPreparedListener != null) {
			Log.d(TAG, "Passing prepared along.");
			mOnPreparedListener.onPrepared(mp);
		}

		if (this.seekOnStart && this.startTime > 0) {
			this.seekOnStart = false;
			this.seekTo(startTime);
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
	private void sendIsPlaying() {
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
			mPlayerHaterListener.onLoading();
		}
	}

	private void sendIsPaused() {
		if (mPlayerHaterListener != null) {
			mPlayerHaterListener.onPaused();
		}
	}

	private void sendIsStopped() {
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

	/*
	 * These methods just exist so that they can be overridden.
	 */
	protected static OnAudioFocusChangeListener createAudioFocusChangeListener(
			PlaybackService service) {
		return new OnAudioFocusChangeListener(service);
	}

	/*
	 * One more...
	 */
	protected static NotificationHandler createNotificationHandler(
			PlaybackService service) {
		return new NotificationHandler(service);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		stopProgressThread();
		if (mOnCompletionListener != null) {
			mOnCompletionListener.onCompletion(mp);
		}
		stop();
	}

	public NotificationHandler getNotification() {
		return mNotificationHandler;
	}

}
