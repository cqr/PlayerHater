package org.prx.android.playerhater.plugins;

import java.util.LinkedList;
import java.util.Queue;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.PlayerHaterServiceBinder;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;

public class BackgroundedPlugin extends Thread implements PlayerHaterPlugin,
		Callback {

	private static final int SONG_CHANGED = 0;
	private static final int DURATION_CHANGED = 1;
	private static final int NEXT_TRACK_UNAVAILABLE = 2;
	private static final int NEXT_TRACK_AVAILABLE = 3;
	private static final int ART_CHANGED_URI = 4;
	private static final int ART_CHANGED_INT = 5;
	private static final int ARTIST_CHANGED = 6;
	private static final int TITLE_CHANGED = 7;
	private static final int PLAYBACK_STOPPED = 8;
	private static final int PLAYBACK_STARTED = 9;
	private static final int PLAYBACK_RESUMED = 10;
	private static final int PLAYBACK_PAUSED = 11;
	private static final int AUDIO_LOADING = 12;
	private static final int INTENT_CHANGED = 13;
	private static final int SONG_FINISHED = 15;

	private static final int CHANGES_COMPLETE = -1;

	private Handler mHandler;
	private final Handler mUiHandler = new Handler(this);
	private final PlayerHaterPlugin mPlugin;
	private final Queue<QueuedMessage> mQueue = new LinkedList<QueuedMessage>();

	public BackgroundedPlugin(PlayerHaterPlugin plugin) {
		mPlugin = plugin;
		start();
		// Temporary
		mHandler = new QueuedHandler(mQueue);
	}

	@Override
	public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
		mPlugin.onPlayerHaterLoaded(context, playerHater);
	}

	@Override
	public void onServiceStopping() {
		mPlugin.onServiceStopping();
	}

	@Override
	public void run() {
		Looper.prepare();
		mHandler = new PluginHandler(mPlugin, mUiHandler);
		QueuedMessage m;
		while (!mQueue.isEmpty()) {
			m = mQueue.poll();
			mHandler.obtainMessage(m.what, m.obj).sendToTarget();
		}
		Looper.loop();
	}

	@Override
	public void onSongChanged(Song song) {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.obtainMessage(SONG_CHANGED, song).sendToTarget();
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onDurationChanged(int duration) {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.obtainMessage(DURATION_CHANGED, duration).sendToTarget();
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onAudioLoading() {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.sendEmptyMessage(AUDIO_LOADING);
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onAudioPaused() {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.sendEmptyMessage(PLAYBACK_PAUSED);
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onAudioResumed() {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.sendEmptyMessage(PLAYBACK_RESUMED);
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onAudioStarted() {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.sendEmptyMessage(PLAYBACK_STARTED);
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onAudioStopped() {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.sendEmptyMessage(PLAYBACK_STOPPED);
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onTitleChanged(String title) {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.obtainMessage(TITLE_CHANGED, title).sendToTarget();
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onArtistChanged(String artist) {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.obtainMessage(ARTIST_CHANGED, artist).sendToTarget();
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onAlbumArtChanged(int resourceId) {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.obtainMessage(ART_CHANGED_INT, resourceId).sendToTarget();
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.obtainMessage(ART_CHANGED_URI, url).sendToTarget();
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onNextSongAvailable(Song nextTrack) {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.obtainMessage(NEXT_TRACK_AVAILABLE, nextTrack).sendToTarget();
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onNextSongUnavailable() {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.sendEmptyMessage(NEXT_TRACK_UNAVAILABLE);
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onIntentActivityChanged(PendingIntent pending) {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.obtainMessage(INTENT_CHANGED, pending).sendToTarget();
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	@Override
	public void onServiceBound(PlayerHaterServiceBinder playerHater) {
		mPlugin.onServiceBound(playerHater);
	}

	@Override
	public void onSongFinished(Song song, int reason) {
		mHandler.removeMessages(CHANGES_COMPLETE);
		mHandler.obtainMessage(SONG_FINISHED, reason, 0, song).sendToTarget();
		mHandler.sendEmptyMessage(CHANGES_COMPLETE);
	}

	private static final class QueuedMessage {
		public int what;
		public Object obj;
	}

	private static final class QueuedHandler extends Handler {
		private final Queue<QueuedMessage> mQueue;

		public QueuedHandler(Queue<QueuedMessage> queue) {
			mQueue = queue;
		}

		@Override
		public void handleMessage(Message msg) {
			QueuedMessage m = new QueuedMessage();
			m.what = msg.what;
			m.obj = msg.obj;
			mQueue.add(m);
		}
	}

	private static final class PluginHandler extends Handler {

		private final PlayerHaterPlugin mPlugin;
		private final Handler mUiHandler;

		private PluginHandler(PlayerHaterPlugin plugin, Handler uiHandler) {
			mPlugin = plugin;
			mUiHandler = uiHandler;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CHANGES_COMPLETE:
				if (!mUiHandler.hasMessages(CHANGES_COMPLETE)) {
					mUiHandler.sendEmptyMessage(CHANGES_COMPLETE);
				}
				break;
			case SONG_CHANGED:
				mPlugin.onSongChanged((Song) msg.obj);
				break;
			case DURATION_CHANGED:
				mPlugin.onDurationChanged((Integer) msg.obj);
				break;
			case NEXT_TRACK_UNAVAILABLE:
				mPlugin.onNextSongUnavailable();
				break;
			case NEXT_TRACK_AVAILABLE:
				mPlugin.onNextSongAvailable((Song) msg.obj);
				break;
			case ART_CHANGED_URI:
				mPlugin.onAlbumArtChangedToUri((Uri) msg.obj);
				break;
			case ART_CHANGED_INT:
				mPlugin.onAlbumArtChanged((Integer) msg.obj);
				break;
			case ARTIST_CHANGED:
				mPlugin.onArtistChanged((String) msg.obj);
				break;
			case TITLE_CHANGED:
				mPlugin.onTitleChanged((String) msg.obj);
				break;
			case PLAYBACK_STOPPED:
				mPlugin.onAudioStopped();
				break;
			case PLAYBACK_STARTED:
				mPlugin.onAudioStarted();
				break;
			case PLAYBACK_RESUMED:
				mPlugin.onAudioResumed();
				break;
			case PLAYBACK_PAUSED:
				mPlugin.onAudioPaused();
				break;
			case AUDIO_LOADING:
				mPlugin.onAudioLoading();
				break;
			case INTENT_CHANGED:
				mPlugin.onIntentActivityChanged((PendingIntent) msg.obj);
				break;
			case SONG_FINISHED:
				mPlugin.onSongFinished((Song) msg.obj, msg.arg1);
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == CHANGES_COMPLETE) {
			mPlugin.onChangesComplete();
		}
		return false;
	}

	@Override
	public void onChangesComplete() {
		// We handle this on our own.
	}
}
