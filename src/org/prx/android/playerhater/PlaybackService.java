package org.prx.android.playerhater;

import java.io.FileDescriptor;
import java.io.IOException;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

public class PlaybackService extends Service implements OnErrorListener,
		OnPreparedListener, OnSeekCompleteListener, OnCompletionListener {

	protected static final String TAG = "PlayerHater/Service";
	protected static final int PROGRESS_UPDATE = 9747244;
	

	protected String nowPlayingString;
	protected String nowPlayingUrl;
	protected FileDescriptor nowPlayingFile;
	protected int nowPlayingType;
	protected static final int URL = 55;
	protected static final int FILE = 66;

	private MediaPlayerWrapper mediaPlayer;
	private UpdateProgressRunnable updateProgressRunner;
	private Thread updateProgressThread;
	private BroadcastReceiver mBroadcastReceiver;
	private PlayerListenerManager playerListenerManager;
	private OnErrorListener mOnErrorListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;
	private OnPreparedListener mOnPreparedListener;
	private AudioManager mAudioManager;
	private PlayerHaterListener mPlayerHaterListener;
	private OnAudioFocusChangeListener mAudioFocusChangeListener;
	private OnCompletionListener mOnCompletionListener; 
	
	private NotificationHandler mNotificationHandler;

	private boolean mAutoNotify = true;

	private Bundle mBundle;

	private boolean playAfterSeek;

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

		if (mAudioManager == null) {
			mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		}

		if (mAudioFocusChangeListener == null) {
			mAudioFocusChangeListener = createAudioFocusChangeListener(this);
		}

		if (mNotificationHandler == null) {
			mNotificationHandler = createNotificationHandler(this);
		}
		
		if (mBundle == null) {
			mBundle = new Bundle(10);
		}

		if (mBroadcastReceiver == null) {
			mBroadcastReceiver = new BroadcastReceiver(this);
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_HEADSET_PLUG);
			filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY); 
			getBaseContext().registerReceiver(mBroadcastReceiver, filter);
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return new PlayerHaterBinder(this, playerListenerManager);
	}

	public boolean pause() throws IllegalStateException {
		Log.d(TAG, "PAUSE"); 
		mediaPlayer.pause();
		mNotificationHandler.stopNotification();		
		if (updateProgressThread != null && updateProgressThread.isAlive()) {
			mHandler.removeCallbacks(updateProgressRunner);
			updateProgressThread.interrupt();
			updateProgressThread = null;
		}
		sendIsPaused();
		if (updateProgressThread != null && updateProgressThread.isAlive()) {
			mHandler.removeCallbacks(updateProgressRunner);
			updateProgressThread.interrupt();
			updateProgressThread = null;
		}
		return true;
	}

	public void setNotificationIntentActivity(Activity activity) {
		mNotificationHandler.setIntentClass(activity.getClass());
	}

	public void setNotificationView(int view) {
		mNotificationHandler.setView(new RemoteViews(getPackageName(), view));
	}

	public int getState() {
		return mediaPlayer.getState();
	}

	public String getNowPlaying() {
		if (nowPlayingString == null) {
			return "<#null>";
		}
		return nowPlayingString;
	}

	public boolean isPlaying() {
		return (mediaPlayer.getState() == MediaPlayerWrapper.STARTED);
	}
	
	public boolean isLoading() { 
		return (mediaPlayer.getState() == MediaPlayerWrapper.INITIALIZED ||
				mediaPlayer.getState() == MediaPlayerWrapper.PREPARING ||
				mediaPlayer.getState() == MediaPlayerWrapper.PREPARED); 
	}

	public boolean play(String stream) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		nowPlayingType = URL;
		nowPlayingString = stream;
		nowPlayingUrl = stream;
		if (mediaPlayer.getState() != MediaPlayerWrapper.IDLE)
			reset();
		mediaPlayer.setDataSource(nowPlayingUrl);
		return play();

	}

	public boolean play(FileDescriptor fd) throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		nowPlayingType = FILE;
		nowPlayingString = fd.toString();
		nowPlayingFile = fd;
		if (mediaPlayer.getState() != MediaPlayerWrapper.IDLE)
			reset();
		mediaPlayer.setDataSource(nowPlayingFile);
		return play();
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
			if (mAutoNotify)
				mNotificationHandler.startNotification();
			break;
		case MediaPlayerWrapper.IDLE: 
			if (nowPlayingType == URL) { 
				play(nowPlayingUrl); 
			} else { 
				play(nowPlayingFile); 
			}
			break; 
		default:
			System.out.println("State is " + mediaPlayer.getState()); 
			throw new IllegalStateException();
		}
		return true;

	}

	public TransientPlayer transientPlay(FileDescriptor file, boolean isDuckable) {
		return TransientPlayer.play(this, file, isDuckable);
	}

	public TransientPlayer transientPlay(String url, boolean isDuckable) {
		return TransientPlayer.play(this, url, isDuckable);
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

		mediaPlayer.pause();
		sendIsLoading();
		mediaPlayer.seekTo(pos);
	}

	private void performPrepare() {
		Log.d(TAG, "Starting preparation of: " + getNowPlaying());
		sendIsLoading();
		mediaPlayer.prepareAsync();

		if (updateProgressThread != null && updateProgressThread.isAlive()) {
			mHandler.removeCallbacks(updateProgressRunner);
			updateProgressThread.interrupt();
			updateProgressThread = null;
		}

		updateProgressThread = new Thread(updateProgressRunner);
		updateProgressThread.start();
	}

	public boolean stop() {
		Log.d(TAG, "STOP"); 
		mediaPlayer.stop();
		mNotificationHandler.stopNotification();
		sendIsStopped();
		if (updateProgressThread != null && updateProgressThread.isAlive()) {
			mHandler.removeCallbacks(updateProgressRunner);
			updateProgressThread.interrupt();
			updateProgressThread = null;
		}
		return true;
	}

	/*
	 * THE BUNDLE
	 */

	public Bundle getBundle() {
		return mBundle;
	}

	public void commitBundle(Bundle icicle) {
		mBundle = icicle;
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
		Log.d(TAG, "MediaPlayer is prepared, beginning playback of "
				+ getNowPlaying());
		mediaPlayer.start();
		if (mAutoNotify)
			mNotificationHandler.startNotification();
		sendIsPlaying();

		mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		if (mOnPreparedListener != null) {
			Log.d(TAG, "Passing prepared along.");
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
		if (updateProgressThread != null && updateProgressThread.isAlive()) {
			mHandler.removeCallbacks(updateProgressRunner);
			updateProgressThread.interrupt();
			updateProgressThread = null;
		}
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
			mPlayerHaterListener.onPlaying(progress);
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

	public void setNotificationIcon(int notificationIcon) {
		mNotificationHandler.setNotificationIcon(notificationIcon);
	}

	public void setAutoNotify(boolean autoNotify) {
		mAutoNotify = autoNotify;
	}

	public void doStartForeground() {
		if (!mAutoNotify) {
			mNotificationHandler.startNotification();
		} else {
			Log.e(TAG,
					"startForeground() was called, but set to do this automatically. Ignoring request.");
		}
	}

	public void doStopForeground() {
		mNotificationHandler.stopNotification();
	}
	
	public void setNotificationTitle(String notificationTitle) {
		mNotificationHandler.setTitle(notificationTitle);
	}
	
	public void setNotificationText(String notificationText) {
		mNotificationHandler.setText(notificationText);
	}
	
	/*
	 * creates a media player (wrapped, of course) and registers the listeners
	 * for all of the events.
	 */
	protected static MediaPlayerWrapper createMediaPlayer(PlaybackService service) {
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
	protected static OnAudioFocusChangeListener createAudioFocusChangeListener(PlaybackService service) {
		return new OnAudioFocusChangeListener(service);
	}
	
	/* 
	 * One more...
	 */
	protected static NotificationHandler createNotificationHandler(PlaybackService service) {
		return new NotificationHandler(service);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.e(TAG, "Got MediaPlayer completion");
		if (updateProgressThread != null && updateProgressThread.isAlive()) {
			mHandler.removeCallbacks(updateProgressRunner);
			updateProgressThread.interrupt();
			updateProgressThread = null;
		}
		if (mOnCompletionListener != null) {
			Log.e(TAG, "Passing completion along.");
			mOnCompletionListener.onCompletion(mp); 
		}
		stop(); 
	}

}
