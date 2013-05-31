package org.prx.playerhater.mediaplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prx.playerhater.util.Log;

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

	public synchronized void prepare(Context context, Uri uri) {
		Log.d("Being asked to prepare " + uri);
		if (mMediaPlayers.containsKey(uri)) {
			Log.d(".... but it's already prepared. Returning.");
			return;
		} else {
			SynchronousPlayer player = getPlayer();
			Log.d(".... preparing player with " + uri);
			player.prepare(context, uri);
			addPlayer(player, uri);
		}
	}

	public synchronized SynchronousPlayer getPlayer(Context context, Uri uri) {
		Log.d("Getting a player for " + uri);
		if (mMediaPlayers.containsKey(uri)) {
			Log.d("It was waiting for us.");
			mRequests.remove(uri);
			return mMediaPlayers.remove(uri);
		} else {
			SynchronousPlayer player = getPlayer();
			Log.d("Preparing " + uri);
			player.prepare(context, uri);
			return player;
		}
	}

	public synchronized void recycle(SynchronousPlayer player) {
		if (player != null) {
			Log.d("Adding a player to the idle pool");
			player.reset();
			mIdlePlayers.add(player);
		}
	}

	public synchronized void recycle(SynchronousPlayer player, Uri prepared) {
		if (player != null) {
			if (prepared == null) {
				recycle(player);
				return;
			}
			Log.d("Got an apparently prepared player for " + prepared);
			if (mMediaPlayers.containsKey(prepared)) {
				Log.d("but we already have another player for that uri, so we're scrapping it.");
				recycle(player);
			} else {
				Log.d("Let's see what kind of shape it's in.");
				switch (player.getState()) {
				case StatelyPlayer.IDLE:
				case StatelyPlayer.ERROR:
					Log.d("Unusable, resetting and scrapping");
					recycle(player);
					return;
				case StatelyPlayer.END:
					Log.d("Released? What the hell?");
					return;
				case StatelyPlayer.INITIALIZED:
				case StatelyPlayer.STOPPED:
					Log.d("Waiting for a call to prepare - doing so.");
					player.prepareAsync();
					break;
				case StatelyPlayer.STARTED:
					Log.d("Playing? Pausing it.");
					player.pause();
				case StatelyPlayer.PAUSED:
					Log.d("Skipping to the beginning...");
					player.seekTo(0);
				}
				addPlayer(player, prepared);
			}
		}
	}

	private synchronized SynchronousPlayer getPlayer() {
		Log.d("Trying to get a new player.");
		if (mIdlePlayers.size() > 0) {
			Log.d("Grabbing an idle player.");
			return mIdlePlayers.remove(0);
		} else if (mRequests.size() > 0) {
			Log.d("Grabbing our least recently used player.");
			Uri leastRecentReq = mRequests.remove(mRequests.size() - 1);
			Log.d("Which is " + leastRecentReq);
			SynchronousPlayer player = mMediaPlayers.remove(leastRecentReq);
			Log.d("Resetting");
			player.reset();
			return player;
		} else {
			throw new IllegalStateException(
					"MediaPlayer resources exhausted. Are you sure you're #recycle()ing on time?");
		}
	}

	private synchronized void addPlayer(SynchronousPlayer player, Uri uri) {
		Log.d("Adding a player to the pile of prepared ones for " + uri);
		mRequests.remove(uri);
		mRequests.add(0, uri);
		mMediaPlayers.put(uri, player);
	}
}
