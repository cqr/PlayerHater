/*******************************************************************************
 * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.prx.playerhater.plugins;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.PlayerHaterPlugin;
import org.prx.playerhater.Song;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;

public class BackgroundedPlugin extends Thread implements PlayerHaterPlugin,
		Callback {

	public static final int SONG_CHANGED = 0;
	public static final int DURATION_CHANGED = 1;
	public static final int NEXT_TRACK_UNAVAILABLE = 2;
	public static final int NEXT_TRACK_AVAILABLE = 3;
	public static final int ART_CHANGED_URI = 4;
	public static final int ALBUM_CHANGED = 5;
	public static final int ARTIST_CHANGED = 6;
	public static final int TITLE_CHANGED = 7;
	public static final int PLAYBACK_STOPPED = 8;
	public static final int PLAYBACK_STARTED = 9;
	public static final int PLAYBACK_RESUMED = 10;
	public static final int PLAYBACK_PAUSED = 11;
	public static final int AUDIO_LOADING = 12;
	public static final int INTENT_CHANGED = 13;
	public static final int SONG_FINISHED = 14;
	public static final int SERVICE_BOUND = 15;
	public static final int PLAYER_HATER_LOADED = 16;
	public static final int SERVICE_STOPPING = 17;
	public static final int CHANGES_COMPLETE = 18;
	public static final int TRANSPORT_CONTROL_FLAGS_CHANGED = 19;

	private static final int CHANGES_COMPLETE_INTERNAL = -1;

	public static final Integer[] DEFAULT_FOREGROUND_ACTIONS = {
			CHANGES_COMPLETE, SERVICE_BOUND, PLAYER_HATER_LOADED,
			SERVICE_STOPPING };

	private TargetableHandler mHandler;
	private final Looper mLooper;
	private final PlayerHaterPlugin mPlugin;
	private final Set<Integer> mForegroundActions;

	public BackgroundedPlugin(PlayerHaterPlugin plugin) {
		this(plugin, DEFAULT_FOREGROUND_ACTIONS);
	}

	public BackgroundedPlugin(PlayerHaterPlugin plugin,
			Integer... foregroundActions) {
		this(plugin, Looper.getMainLooper(), foregroundActions);
	}

	public BackgroundedPlugin(PlayerHaterPlugin plugin,
			Looper foregroundLooper, Integer... foregroundActions) {
		mPlugin = plugin;
		mForegroundActions = new HashSet<Integer>(
				Arrays.asList(foregroundActions));
		mLooper = foregroundLooper;
		mHandler = new TargetableHandler(this); // Temporary.
		start();
	}

	@Override
	public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
		if (shouldHandleMessage(PLAYER_HATER_LOADED)) {
			mPlugin.onPlayerHaterLoaded(context, playerHater);
		} else {
			mHandler.obtainTargettedMessage(PLAYER_HATER_LOADED,
					new LoadedObject(context, playerHater)).sendToTarget();
		}
	}

	private static class LoadedObject {
		public final Context context;
		public final PlayerHater playerHater;

		public LoadedObject(Context context, PlayerHater playerHater) {
			this.context = context;
			this.playerHater = playerHater;
		}
	}

	@Override
	public void run() {
		Looper.prepare();
		mHandler = new HandlerPair(this, mForegroundActions, mLooper);
		Looper.loop();
	}

	@Override
	public void onSongChanged(Song song) {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(SONG_CHANGED)) {
			mPlugin.onSongChanged(song);
		} else {
			mHandler.obtainTargettedMessage(SONG_CHANGED, song).sendToTarget();
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onDurationChanged(int duration) {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(DURATION_CHANGED)) {
			mPlugin.onDurationChanged(duration);
		} else {
			mHandler.obtainTargettedMessage(DURATION_CHANGED, duration)
					.sendToTarget();
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onAudioLoading() {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(AUDIO_LOADING)) {
			mPlugin.onAudioLoading();
		} else {
			mHandler.sendTargettedEmptyMessage(AUDIO_LOADING);
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onAudioPaused() {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(PLAYBACK_PAUSED)) {
			mPlugin.onAudioPaused();
		} else {
			mHandler.sendTargettedEmptyMessage(PLAYBACK_PAUSED);
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onAudioResumed() {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(PLAYBACK_RESUMED)) {
			mPlugin.onAudioResumed();
		} else {
			mHandler.sendTargettedEmptyMessage(PLAYBACK_RESUMED);
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onAudioStarted() {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(PLAYBACK_STARTED)) {
			mPlugin.onAudioStarted();
		} else {
			mHandler.sendTargettedEmptyMessage(PLAYBACK_STARTED);
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onAudioStopped() {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(PLAYBACK_STOPPED)) {
			mPlugin.onAudioStopped();
		} else {
			mHandler.sendTargettedEmptyMessage(PLAYBACK_STOPPED);
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onTitleChanged(String title) {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(TITLE_CHANGED)) {
			mPlugin.onTitleChanged(title);
		} else {
			mHandler.obtainTargettedMessage(TITLE_CHANGED, title)
					.sendToTarget();
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}
	
	@Override
	public void onAlbumTitleChanged(String title) {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(ALBUM_CHANGED)) {
			mPlugin.onAlbumTitleChanged(title);
		} else {
			mHandler.obtainTargettedMessage(ALBUM_CHANGED, title).sendToTarget();
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onArtistChanged(String artist) {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(ARTIST_CHANGED)) {
			mPlugin.onArtistChanged(artist);
		} else {
			mHandler.obtainTargettedMessage(ARTIST_CHANGED, artist)
					.sendToTarget();
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onAlbumArtChanged(Uri url) {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(ART_CHANGED_URI)) {
			mPlugin.onAlbumArtChanged(url);
		} else {
			mHandler.obtainTargettedMessage(ART_CHANGED_URI, url)
					.sendToTarget();
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onNextSongAvailable(Song nextTrack) {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(NEXT_TRACK_AVAILABLE)) {
			mPlugin.onNextSongAvailable(nextTrack);
		} else {
			mHandler.obtainTargettedMessage(NEXT_TRACK_AVAILABLE, nextTrack)
					.sendToTarget();
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onNextSongUnavailable() {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(NEXT_TRACK_UNAVAILABLE)) {
			mPlugin.onNextSongUnavailable();
		} else {
			mHandler.sendTargettedEmptyMessage(NEXT_TRACK_UNAVAILABLE);
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onIntentActivityChanged(PendingIntent pending) {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(INTENT_CHANGED)) {
			mPlugin.onIntentActivityChanged(pending);
		} else {
			mHandler.obtainTargettedMessage(INTENT_CHANGED, pending)
					.sendToTarget();
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onSongFinished(Song song, int reason) {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(SONG_FINISHED)) {
			mPlugin.onSongFinished(song, reason);
		} else {
			mHandler.obtainTargettedMessage(SONG_FINISHED, reason, 0, song)
					.sendToTarget();
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	@Override
	public void onTransportControlFlagsChanged(int transportControlFlags) {
		mHandler.removeTargettedMessages(CHANGES_COMPLETE_INTERNAL);
		if (shouldHandleMessage(TRANSPORT_CONTROL_FLAGS_CHANGED)) {
			mPlugin.onTransportControlFlagsChanged(transportControlFlags);
		} else {
			mHandler.obtainTargettedMessage(TRANSPORT_CONTROL_FLAGS_CHANGED,
					transportControlFlags, 0).sendToTarget();
		}
		mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE_INTERNAL);
	}

	private boolean shouldHandleMessage(int messageId) {
		// if (Looper.myLooper() == mLooper
		// && mForegroundActions.contains(messageId)) {
		// return true;
		// } else if (Looper.myLooper() != mLooper
		// && !mForegroundActions.contains(messageId)) {
		// return true;
		// } else {
		return false;
		// }
	}

	private static class TargetableHandler extends Handler {

		public TargetableHandler() {
			super();
		}

		public TargetableHandler(Callback callback) {
			super(callback);
		}

		@SuppressWarnings("unused")
		public Message obtainTargettedMessage(int what) {
			return obtainMessage(what);
		}

		public Message obtainTargettedMessage(int what, Object obj) {
			return obtainMessage(what, obj);
		}

		public Message obtainTargettedMessage(int what, int arg1, int arg2) {
			return obtainMessage(what, arg1, arg2);
		}

		public Message obtainTargettedMessage(int what, int arg1, int arg2,
				Object obj) {
			return obtainMessage(what, arg1, arg2, obj);
		}

		public boolean sendTargettedEmptyMessage(int what) {
			return sendEmptyMessage(what);
		}

		public void removeTargettedMessages(int what) {
			removeMessages(what);
		}

		public boolean hasTargettedMessages(int what) {
			return hasMessages(what);
		}
	}

	private static class HandlerPair extends TargetableHandler {

		private final Callback mCallback;
		private final OtherHandler mOtherHandler;
		private final Set<Integer> mOtherActions;

		private HandlerPair(Callback callback, Set<Integer> otherLooperActions,
				Looper otherLooper) {
			super();
			mCallback = callback;

			mOtherActions = otherLooperActions;
			mOtherHandler = new OtherHandler(otherLooper, this);
		}

		@Override
		public Message obtainTargettedMessage(int what) {
			if (mOtherActions.contains(what)) {
				return mOtherHandler.obtainMessage(what);
			}
			return obtainMessage(what);
		}

		@Override
		public Message obtainTargettedMessage(int what, Object obj) {
			if (mOtherActions.contains(what)) {
				return mOtherHandler.obtainMessage(what, obj);
			}
			return obtainMessage(what, obj);
		}

		@Override
		public Message obtainTargettedMessage(int what, int arg1, int arg2) {
			if (mOtherActions.contains(what)) {
				return mOtherHandler.obtainMessage(what, arg1, arg2);
			}
			return obtainMessage(what, arg1, arg2);
		}

		@Override
		public Message obtainTargettedMessage(int what, int arg1, int arg2,
				Object obj) {
			if (mOtherActions.contains(what)) {
				return mOtherHandler.obtainMessage(what, arg1, arg2, obj);
			}
			return obtainMessage(what, arg1, arg2, obj);
		}

		@Override
		public boolean sendTargettedEmptyMessage(int what) {
			if (mOtherActions.contains(what)) {
				return mOtherHandler.sendEmptyMessage(what);
			}
			return sendEmptyMessage(what);
		}

		@Override
		public void handleMessage(Message msg) {
			handleMessage(msg, false);
		}

		@Override
		public void removeTargettedMessages(int what) {
			if (mOtherActions.contains(what)) {
				mOtherHandler.removeMessages(what);
			} else {
				removeMessages(what);
			}
		}

		@Override
		public boolean hasTargettedMessages(int what) {
			if (mOtherActions.contains(what)) {
				return mOtherHandler.hasMessages(what);
			}
			return hasMessages(what);
		}

		public void handleMessage(Message msg, boolean inverted) {
			if ((inverted && !mOtherActions.contains(msg.what))
					|| (!inverted && mOtherActions.contains(msg.what))) {
				Message newMessage = obtainTargettedMessage(msg.what);
				newMessage.copyFrom(msg);
				newMessage.sendToTarget();
			} else {
				mCallback.handleMessage(msg);
			}
		}

		static class OtherHandler extends Handler {

			private final HandlerPair mHandler;

			private OtherHandler(Looper looper, HandlerPair handler) {
				super(looper);
				mHandler = handler;
			}

			@Override
			public void handleMessage(Message msg) {
				mHandler.handleMessage(msg, true);
			}

		}
	}

	@Override
	public void onChangesComplete() {
		// This is automatically handled by the thing.
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case CHANGES_COMPLETE_INTERNAL:
			if (!mHandler.hasTargettedMessages(CHANGES_COMPLETE)) {
				mHandler.sendTargettedEmptyMessage(CHANGES_COMPLETE);
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
			mPlugin.onAlbumArtChanged((Uri) msg.obj);
			break;
		case ALBUM_CHANGED:
			mPlugin.onAlbumTitleChanged((String) msg.obj);
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
			break;
		case PLAYER_HATER_LOADED:
			LoadedObject o = (LoadedObject) msg.obj;
			mPlugin.onPlayerHaterLoaded(o.context, o.playerHater);
			break;
		case TRANSPORT_CONTROL_FLAGS_CHANGED:
			mPlugin.onTransportControlFlagsChanged(msg.arg1);
			break;
		case CHANGES_COMPLETE:
			mPlugin.onChangesComplete();
			break;
		default:
			return false;
		}
		return true;
	}
}
