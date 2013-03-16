package org.prx.android.playerhater.plugins;

import java.util.LinkedList;
import java.util.Queue;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.util.IPlayerHater;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class BackgroundedPlugin extends Thread implements PlayerHaterPlugin {
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

	private Handler mHandler;
	private final PlayerHaterPlugin mPlugin;
	private final Queue<QueuedMessage> mQueue = new LinkedList<QueuedMessage>();

	public BackgroundedPlugin(PlayerHaterPlugin plugin) {
		mPlugin = plugin;
		start();
		// Temporary
		mHandler = new QueuedHandler(mQueue);
	}
	
	@Override
	public void onServiceStarted(Context context, IPlayerHater playerHater) {
		mPlugin.onServiceStarted(context, playerHater);
	}

	@Override
	public void onServiceStopping() {
		mPlugin.onServiceStopping();
	}

	@Override
	public void run() {
		Looper.prepare();
		mHandler = new PluginHandler(mPlugin);
		QueuedMessage m;
		while (!mQueue.isEmpty()) {
			 m = mQueue.poll();
			 mHandler.obtainMessage(m.what, m.obj).sendToTarget();
		}
		Looper.loop();
	}

	@Override
	public void onSongChanged(Song song) {
		mHandler.obtainMessage(SONG_CHANGED, song).sendToTarget();
	}

	@Override
	public void onDurationChanged(int duration) {
		mHandler.obtainMessage(DURATION_CHANGED, duration).sendToTarget();
	}

	@Override
	public void onAudioLoading() {
		mHandler.sendEmptyMessage(AUDIO_LOADING);
	}

	@Override
	public void onPlaybackPaused() {
		mHandler.sendEmptyMessage(PLAYBACK_PAUSED);
	}

	@Override
	public void onPlaybackResumed() {
		mHandler.sendEmptyMessage(PLAYBACK_RESUMED);
	}

	@Override
	public void onPlaybackStarted() {
		mHandler.sendEmptyMessage(PLAYBACK_STARTED);
	}

	@Override
	public void onPlaybackStopped() {
		mHandler.sendEmptyMessage(PLAYBACK_STOPPED);
	}

	@Override
	public void onTitleChanged(String title) {
		mHandler.obtainMessage(TITLE_CHANGED, title).sendToTarget();
	}

	@Override
	public void onArtistChanged(String artist) {
		mHandler.obtainMessage(ARTIST_CHANGED, artist).sendToTarget();
	}

	@Override
	public void onAlbumArtChanged(int resourceId) {
		mHandler.obtainMessage(ART_CHANGED_INT, resourceId).sendToTarget();
	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
		mHandler.obtainMessage(ART_CHANGED_URI, url).sendToTarget();
	}

	@Override
	public void onNextTrackAvailable() {
		mHandler.sendEmptyMessage(NEXT_TRACK_AVAILABLE);
	}

	@Override
	public void onNextTrackUnavailable() {
		mHandler.sendEmptyMessage(NEXT_TRACK_UNAVAILABLE);
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

		private PluginHandler(PlayerHaterPlugin plugin) {
			mPlugin = plugin;
		}

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case SONG_CHANGED:
				mPlugin.onSongChanged((Song) msg.obj);
				break;
			case DURATION_CHANGED:
				mPlugin.onDurationChanged((Integer)msg.obj);
				break;
			case NEXT_TRACK_UNAVAILABLE:
				mPlugin.onNextTrackUnavailable();
				break;
			case NEXT_TRACK_AVAILABLE:
				mPlugin.onNextTrackAvailable();
				break;
			case ART_CHANGED_URI:
				mPlugin.onAlbumArtChangedToUri((Uri)msg.obj);
				break;
			case ART_CHANGED_INT:
				mPlugin.onAlbumArtChanged((Integer)msg.obj);
				break;
			case ARTIST_CHANGED:
				mPlugin.onArtistChanged((String)msg.obj);
				break;
			case TITLE_CHANGED:
				mPlugin.onTitleChanged((String)msg.obj);
				break;
			case PLAYBACK_STOPPED:
				mPlugin.onPlaybackStopped();
				break;
			case PLAYBACK_STARTED:
				mPlugin.onPlaybackStarted();
				break;
			case PLAYBACK_RESUMED:
				mPlugin.onPlaybackResumed();
				break;
			case PLAYBACK_PAUSED:
				mPlugin.onPlaybackPaused();
				break;
			case AUDIO_LOADING:
				mPlugin.onAudioLoading();
			}
		}
	}
}
