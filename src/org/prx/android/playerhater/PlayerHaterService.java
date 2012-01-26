package org.prx.android.playerhater;

import java.io.FileDescriptor;
import java.io.IOException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
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
	protected static final int NOTIFICATION_NU = 9747245;

	protected NotificationManager mNotificationManager;
	protected Class<?> mNotificationIntentClass;
	protected RemoteViews mNotificationView;
	protected int mNotificationIcon;

	protected String nowPlayingString;
	protected String nowPlayingUrl;
	protected FileDescriptor nowPlayingFile;
	protected int nowPlayingType;
	protected static final int URL = 55;
	protected static final int FILE = 66;

	private MediaPlayerWrapper mediaPlayer;
	private Runnable playerRunner;
	private UpdateProgressRunnable updateProgressRunner;
	private Thread updateProgressThread;
	private PlayerListenerManager playerListenerManager;
	private OnErrorListener mOnErrorListener;
	private OnSeekBarChangeListener mOnSeekbarChangeListener;
	private OnPreparedListener mOnPreparedListener;
	private Thread playThread;

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

	/* We override a couple of methods in PlayerHaterBinder so that we can see
	 * MediaPlayer onError and onPrepared events
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return new PlayerHaterBinder(this, playerListenerManager) {
			@Override
			public void setOnErrorListener(OnErrorListener listener) {
				mOnErrorListener = listener;
			}

			@Override
			public void setOnPreparedListener(OnPreparedListener listener) {
				mOnPreparedListener = listener;
			}
		};
	}

	/* Oh, snap Ð we're blowing up!
	 */
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
	
	/* We register ourselves to listen to the onPrepared event so we can start
	 * playing immediately.
	 */
	@Override
	public void onPrepared(MediaPlayer mp) {
		mediaPlayer.start();
		if (mOnPreparedListener != null) {
			mOnPreparedListener.onPrepared(mp);
		}
	}
	

	public boolean isPlaying() {
		return (mediaPlayer.getState() == MediaPlayerWrapper.STARTED);
	}

	public boolean play(String stream) {
		if (stream != null) {
			nowPlayingUrl = stream;
		}
		listen();
		mNotificationManager.notify(NOTIFICATION_NU,
				buildNotification(PendingIntent.FLAG_UPDATE_CURRENT));
		return true;

	}

	public boolean play(FileDescriptor fd) {
		nowPlayingType = FILE;
		nowPlayingString = fd.toString();
		nowPlayingFile = fd;
		return _play((String) null);
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
			@Override
			public void run() {
				try {
					initializeMediaPlayer();
				} catch (IllegalStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SecurityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
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

	public void initializeMediaPlayer() throws IllegalStateException,
			IllegalArgumentException, SecurityException, IOException {
		mediaPlayer.reset();
		mediaPlayer.setDataSource(nowPlaying);
	}

	public boolean pause() throws IllegalStateException {
		mediaPlayer.pause();
		return true;
	}

	public void setNotificationIntentActivity(Activity activity) {
		mNotificationIntentClass = activity.getClass();
	}

	public void setNotificationView(int view) {
		mNotificationView = new RemoteViews(getPackageName(), view);
	}

	public int getState() {
		return mediaPlayer.getState();
	}

	public String getNowPlaying() {
		return nowPlayingString;
	}

	/*
	 * These methods concern the creation of notifications. They should be
	 * ignored.
	 */

	protected Notification buildNotification() {
		return buildNotification("Playing...", 0);
	}

	protected Notification buildNotification(int pendingFlag) {
		return buildNotification("Playing...", pendingFlag);
	}

	protected Notification buildNotification(String text) {
		return buildNotification(text, 0);
	}

	protected Notification buildNotification(String text, int pendingFlag) {
		Notification notification = new Notification(mNotificationIcon, text,
				System.currentTimeMillis());

		if (mNotificationIntentClass != null && mNotificationView != null) {
			notification.contentView = mNotificationView;
			notification.contentIntent = PendingIntent.getActivity(this, 0,
					new Intent(this, mNotificationIntentClass), pendingFlag);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
		}

		return notification;
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
	 * creates a new update progress runner, which fires events back to this
	 * class' handler with the message we request and the duration which has
	 * passed
	 */
	private void createUpdateProgressRunner() {
		updateProgressRunner = new UpdateProgressRunnable(mediaPlayer,
				mHandler, PROGRESS_UPDATE);
	}

	/*
	 * This class basically just makes sure that we never need to re-bind
	 * ourselves.
	 */
	private void createPlayerListenerManager() {
		playerListenerManager = new PlayerListenerManager();
		playerListenerManager.setOnErrorListener(this);
		playerListenerManager.setOnPreparedListener(this);
	}

	/*
	 * This should be overridden by subclasses which wish to handle messages
	 * sent to mHandler without re-implementing the handler. It is a noop by
	 * default.
	 */
	protected void onHandlerMessage(Message m) { /* noop */
	}

}
