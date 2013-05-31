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

	public synchronized void release() {
		for (SynchronousPlayer player : mIdlePlayers) {
			player.release();
		}
		mIdlePlayers.clear();
		for (Uri uri : mRequests) {
			mMediaPlayers.remove(uri).release();
		}
		mRequests.clear();
		for (SynchronousPlayer player : mMediaPlayers.values()) {
			player.release();
		}
		mMediaPlayers.clear();
	}
	
	public synchronized void prepare(Context context, Uri uri) {
		if (mMediaPlayers.containsKey(uri)) {
			return;
		} else {
			SynchronousPlayer player = getPlayer();
			player.prepare(context, uri);
			addPlayer(player, uri);
		}
	}

	public synchronized SynchronousPlayer getPlayer(Context context, Uri uri) {
		if (mMediaPlayers.containsKey(uri)) {
			mRequests.remove(uri);
			return mMediaPlayers.remove(uri);
		} else {
			SynchronousPlayer player = getPlayer();
			player.prepare(context, uri);
			return player;
		}
	}

	public synchronized void recycle(SynchronousPlayer player) {
		if (player != null) {
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
				addPlayer(player, prepared);
			}
		}
	}

	private synchronized SynchronousPlayer getPlayer() {
		if (mIdlePlayers.size() > 0) {
			return mIdlePlayers.remove(0);
		} else if (mRequests.size() > 0) {
			Uri leastRecentReq = mRequests.remove(mRequests.size() - 1);
			SynchronousPlayer player = mMediaPlayers.remove(leastRecentReq);
			player.reset();
			return player;
		} else {
			throw new IllegalStateException(
					"MediaPlayer resources exhausted. Are you sure you're #recycle()ing on time?");
		}
	}

	private synchronized void addPlayer(SynchronousPlayer player, Uri uri) {
		mRequests.remove(uri);
		mRequests.add(0, uri);
		mMediaPlayers.put(uri, player);
	}
}
