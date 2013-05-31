package org.prx.playerhater.mediaplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.net.Uri;

public class MediaPlayerPool {

	private final Map<Uri, SynchronousPlayer> mMediaPlayers = new HashMap<Uri, SynchronousPlayer>();
	private final List<SynchronousPlayer> mIdlePlayers = new ArrayList<SynchronousPlayer>();
	private final List<Uri> mRequests = new ArrayList<Uri>();
	private final int MAX_SIZE;

	private static final int DEFAULT_SIZE = 3;

	public MediaPlayerPool() {
		this(DEFAULT_SIZE);
	}

	public MediaPlayerPool(int size) {
		MAX_SIZE = size;
		for (int i = 0; i < MAX_SIZE; i++) {
			mIdlePlayers.add(new SynchronousPlayer());
		}
	}

	public void prepare(Context context, Uri uri) {
		if (mMediaPlayers.containsKey(uri)) {
			return;
		} else {
			SynchronousPlayer player = getPlayer();
			player.prepare(context, uri);
			addPlayer(player, uri);
		}
	}

	public SynchronousPlayer getPlayer(Context context, Uri uri) {
		if (mMediaPlayers.containsKey(uri)) {
			mRequests.remove(uri);
			return mMediaPlayers.remove(uri);
		} else {
			SynchronousPlayer player = getPlayer();
			player.prepare(context, uri);
			return player;
		}
	}

	public void recycle(SynchronousPlayer player) {
		if (player != null) {
			player.reset();
			mIdlePlayers.add(player);
		}
	}

	public void recycle(SynchronousPlayer player, Uri prepared) {
		if (player != null) {
			if (mMediaPlayers.containsKey(prepared)) {
				recycle(player);
			} else {
				switch (player.getState()) {
				case StatelyPlayer.IDLE:
				case StatelyPlayer.ERROR:
					recycle(player);
					return;
				case StatelyPlayer.END:
					return;
				case StatelyPlayer.INITIALIZED:
				case StatelyPlayer.STOPPED:
					player.prepareAsync();
					break;
				case StatelyPlayer.STARTED:
					player.pause();
				case StatelyPlayer.PAUSED:
					player.seekTo(0);
				}
				mRequests.add(mRequests.size(), prepared);
				mMediaPlayers.put(prepared, player);
			}
		}
	}

	private SynchronousPlayer getPlayer() {
		if (mIdlePlayers.size() > 0) {
			return mIdlePlayers.remove(0);
		} else if (mRequests.size() > 0) {
			SynchronousPlayer player = mMediaPlayers.remove(mRequests
					.remove(mRequests.size() - 1));
			player.reset();
			return player;
		} else {
			throw new IllegalStateException(
					"MediaPlayer resources exhausted. Are you sure you're #recycle()ing on time?");
		}
	}

	private void addPlayer(SynchronousPlayer player, Uri uri) {
		mRequests.remove(uri);
		mRequests.add(0, uri);
		mMediaPlayers.put(uri, player);
	}
}
