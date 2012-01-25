package org.prx.android.playerhater;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PlayerHaterService extends Service implements OnErrorListener,
		OnPreparedListener {

	protected static final String TAG = "PlayerHater/Service";
	protected static final int PROGRESS_UPDATE = 9747244;
	protected OnPlayerLoadingListener playerActivity;
	protected NotificationManager mNotificationManager;
	protected Notification notification;
	protected PendingIntent contentIntent;
	protected String nowPlaying;

	protected Class<?> mNotificationIntentClass;
	protected int mNotificationView;
	protected int mNotificationIcon;
	
	private final HashMap<String, Object> mKeyValuePairs = new HashMap<String, Object>();
	private MediaPlayerWrapper mediaPlayer;
	private Runnable playerRunner;
	private UpdateProgressRunnable updateProgressRunner;
	private Thread updateProgressThread;
	private PlayerListenerManager playerListenerManager;
	private OnErrorListener mOnErrorListener;
	private OnSeekBarChangeListener mOnSeekbarChangeListener;
	private OnPreparedListener mOnPreparedListener;
	private Thread playThread;
	private RemoteViews contentView;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {

			switch (m.what) {
			case PROGRESS_UPDATE:
				onProgressChanged(null, m.arg1, false);
				break;
			default:
				onHandlerMessage(m);
			}
		}
	};
	private Intent notificationIntent;

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		if (playerListenerManager == null)
			createPlayerListenerManager();

		if (mediaPlayer == null)
			createMediaPlayer();

		if (updateProgressRunner == null)
			createUpdateProgressRunner();

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return new PlayerHaterBinder(this);
	}

	public void setActivity(OnPlayerLoadingListener activity) {
		playerActivity = activity;
	}

	public boolean play(String stream) {
		Log.w(TAG,
				"#play(file) on PlayerService is deprecated. Please use the PlayerHater#pause();");
		return _play(stream);
	}

	public boolean playStream(String stream) {
		Log.w(TAG, "#playStream() is deprecated. Please use #play(url, true)");
		return _play(stream);
	}

	public boolean pause() {
		Log.w(TAG,
				"#pause() on PlayerService is deprecated. Please use PlayerHater#pause();");
		return _pause();
	}

	public boolean unpause() {
		Log.w(TAG, "#unpause() is deprecated. Please use #play()");
		return _play((String) null);
	}

	public String getStreamURL() {
		Log.w(TAG,
				"#getStreamURL() is deprecated. Please use PlayerHater#getNowPlaying()");
		return _getNowPlaying();
	}

	public String getNowPlaying() {
		Log.w(TAG,
				"Calling methods directly on PlayerService is deprecated. Please use PlayerHater#getNowPlaying();");
		return _getNowPlaying();
	}

	public String _getNowPlaying() {
		return nowPlaying;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (updateProgressThread != null && updateProgressThread.isAlive()) {
			mHandler.removeCallbacks(updateProgressRunner);
			Log.d(TAG, "INTERRUPTING THE UPDATE PROGRESS THREAD");
			updateProgressThread.interrupt();
			updateProgressThread = null;
		}
		if (mOnErrorListener != null) {
			return mOnErrorListener.onError(mp, what, extra);
		}
		return false;
	}

	/*
	 * creates a media player (wrapped, of course) and registers the listeners
	 * for all of the events.
	 */
	private void createMediaPlayer() {
		mediaPlayer = new MediaPlayerWrapper();
		playerListenerManager.setMediaPlayer(mediaPlayer);
	}

	/*
	 * We use the delegation pattern here, rather than doing things
	 * automatically
	 */
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		playerListenerManager.setOnBufferingUpdateListener(listener);
	}

	public void setOnCompletionListener(OnCompletionListener listener) {
		playerListenerManager.setOnCompletionListener(listener);
	}

	public void setOnInfoListener(OnInfoListener listener) {
		playerListenerManager.setOnInfoListener(listener);
	}

	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		playerListenerManager.setOnSeekCompleteListener(listener);
	}

	/*
	 * End delegated listener methods
	 */

	/*
	 * These are special cases, because we actually need to do something in
	 * error conditions and when the player is prepared.
	 */
	public void setOnErrorListener(OnErrorListener listener) {
		mOnErrorListener = listener;
	}

	public void setOnPreparedListener(OnPreparedListener listener) {
		mOnPreparedListener = listener;
	}

	/*
	 * creates a new update progress runner, which fires events back to this
	 * class' handler with the message we request and the duration which has
	 * passed
	 */
	private void createUpdateProgressRunner() {
		updateProgressRunner = new UpdateProgressRunnable(mediaPlayer,
				mHandler, PROGRESS_UPDATE);
	}

	/*
	 * This class basically just makes sure that we never need to rebind
	 * ourselves.
	 */
	private void createPlayerListenerManager() {
		playerListenerManager = new PlayerListenerManager();
		playerListenerManager.setOnErrorListener(this);
		playerListenerManager.setOnPreparedListener(this);
	}

	/*
	 * This should be overridden by subclasses which wish to handle messages
	 * sent to mHandler without reimplementing the handler. It is a noop by
	 * default.
	 */
	protected void onHandlerMessage(Message m) { /* noop */
	}

	public boolean _isPlaying() {
		return (mediaPlayer.getState() == MediaPlayerWrapper.STARTED);
	}

	public boolean _play(String stream) {
		try {
			if (stream != null) {
				nowPlaying = stream;
			}
			listen();
			notificationIntent = new Intent(this, playerActivity.getClass());
			contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			updateNotification();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;

	}

	public boolean _play(FileDescriptor fd) {
		nowPlaying = fd.toString();
		return _play((String) null);
	}

	public void updateNotification() {

		if (notification == null) {
			notification = new Notification(mNotificationIcon, "Playing..",
					System.currentTimeMillis());
		}

		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		if (contentView == null && this.mNotificationIntentClass != null && mNotificationView != 0) {
			contentView = new RemoteViews(getPackageName(), mNotificationView);
		}

		if (notificationIntent == null && mNotificationIntentClass != null) {
			notificationIntent = new Intent(this, mNotificationIntentClass);
			contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
		}
		
		notification.contentView = contentView;
		notification.contentIntent = contentIntent;
		mNotificationManager.notify(60666, notification);
	}

	public int getPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	public int getDuration() {
		try {
			return this.mediaPlayer.getDuration();
		} catch (IllegalStateException e) {
			return 0;
		}
	}

	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	public void seekTo(int pos) {
		try {
			playerActivity.onLoading();
			mediaPlayer.seekTo(pos);
		} catch (Exception e) {
			Log.d(TAG,
					"Could not seek -- state is " + this.mediaPlayer.getState());
			e.printStackTrace();
		}

	}

	public void rewind() {
		try {
			int pos = mediaPlayer.getCurrentPosition();
			playerActivity.onLoading();
			mediaPlayer.seekTo(pos - 30000);
		} catch (Exception e) {
			Log.d(TAG, "Could not skip backwards");
			e.printStackTrace();
		}
	}

	public void skipForward() {
		try {
			int pos = mediaPlayer.getCurrentPosition();
			playerActivity.onLoading();
			mediaPlayer.seekTo(pos + 30000);
		} catch (Exception e) {
			Log.d(TAG, "Could not skip forward.");
			e.printStackTrace();
		}
	}

	public void onPrepared(MediaPlayer mp) {
		mediaPlayer.start();
		if (mOnPreparedListener != null) {
			mOnPreparedListener.onPrepared(mp);
		}
	}

	private void listen() throws IllegalArgumentException,
			IllegalStateException, IOException {

		// XXX: Using a runnable hangs the GUI and causes ocassional ANRs, which
		// are recoverable
		// XXX: Using a new Thread relieves the GUI, but causes bizarre error
		// codes
		// XXX: ??????
		// XXX: Soln:
		// http://www.xoriant.com/blog/mobile-application-development/android-async-task.html
		// ?
		if (playThread != null && playerRunner != null) {
			mHandler.removeCallbacks(playerRunner);
			if (playThread != null && playThread.isAlive()) {
				playThread.interrupt();
			}
			playerRunner = null;
			playThread = null;
		}
		if (updateProgressThread != null && updateProgressThread.isAlive()) {
			mHandler.removeCallbacks(updateProgressRunner);
			Log.d(TAG, "INTERRUPTING THE UPDATE PROGRESS THREAD");
			updateProgressThread.interrupt();
			updateProgressThread = null;
		}

		playerRunner = new Runnable() {
			public void run() {

				System.out.println(nowPlaying);
				System.out.println("GOING FOR IT");

				initializeMediaPlayer();
				try {
					playerActivity.onLoading();
					mediaPlayer.prepareAsync();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		playThread = new Thread(playerRunner);
		playThread.start();
		Log.d(TAG, "starting update progress thread");
		updateProgressThread = new Thread(updateProgressRunner);
		updateProgressThread.start();
	}

	private void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (mOnSeekbarChangeListener != null)
			mOnSeekbarChangeListener.onProgressChanged(seekBar, progress,
					fromUser);
	}

	public void initializeMediaPlayer() {
		mediaPlayer.reset();
		try {
			mediaPlayer.setDataSource(nowPlaying);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean _pause() {
		try {
			mediaPlayer.pause();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void setNotificationIntentActivity(Activity activity) {
		mNotificationIntentClass = activity.getClass();
	}

	public void setNotificationView(int view) {
		mNotificationView = view;
	}

	public void set(String key, Object value) {
		mKeyValuePairs.put(key, value);
	}
	
	public Object get(String key) {
		return mKeyValuePairs.get(key);
	}

	public int getState() {
		return mediaPlayer.getState();
	}
	
}
