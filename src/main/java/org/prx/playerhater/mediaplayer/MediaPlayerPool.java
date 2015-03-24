/*
 * -/*******************************************************************************
 * - * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * - *
 * - * Licensed under the Apache License, Version 2.0 (the "License");
 * - * you may not use this file except in compliance with the License.
 * - * You may obtain a copy of the License at
 * - *
 * - *   http://www.apache.org/licenses/LICENSE-2.0
 * - *
 * - * Unless required by applicable law or agreed to in writing, software
 * - * distributed under the License is distributed on an "AS IS" BASIS,
 * - * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * - * See the License for the specific language governing permissions and
 * - * limitations under the License.
 * - *****************************************************************************
 */
package org.prx.playerhater.mediaplayer;

import android.content.Context;
import android.net.Uri;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.prx.playerhater.util.Log;

public class MediaPlayerPool<P extends SynchronousPlayer> {

	private final Map<Uri, P> mMediaPlayers = new HashMap<Uri, P>();
	private final Set<P> mIdlePlayers = new HashSet<P>();
	private final List<Uri> mRequests = new ArrayList<Uri>();
	private final Class<? extends P> mClass;

	public static <SynchronousPlayerClass extends SynchronousPlayer> MediaPlayerPool<SynchronousPlayerClass> getInstance(Context context,
			Class<SynchronousPlayerClass> klass) {
		return new MediaPlayerPool<SynchronousPlayerClass>(context, klass);
	}

	public MediaPlayerPool(Context context, Class<P> mediaPlayerClass) {
		this(context, mediaPlayerClass, 3);
	}

	public MediaPlayerPool(Context context, Class<P> mediaPlayerClass, int size) {
		mClass = mediaPlayerClass;
		for (int i = 0; i < size; i++) {
			try {
				mIdlePlayers.add(mClass.getConstructor(Context.class).newInstance(context));
			} catch (InstantiationException e) {
				throw new IllegalArgumentException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			} catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
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
		if (uri == null) {
			throw new IllegalArgumentException(
					"can't prepare a player for a null uri!");
		}
		if (!mMediaPlayers.containsKey(uri)) {
			P player = getPlayer();
			Log.d("Preparing " + player + " for " + uri);
			player.prepare(context, uri);
			addPlayer(player, uri);
		}
	}

	public synchronized P getPlayer(Context context, Uri uri) {
		Log.d("Getting player for " + uri);
		if (mMediaPlayers.containsKey(uri)) {
			mRequests.remove(uri);
			Log.d("Found one (" + mMediaPlayers.get(uri) + ")");
			return mMediaPlayers.remove(uri);
		} else {
			P player = getPlayer();
			player.prepare(context, uri);
			return player;
		}
	}

	public synchronized void recycle(P player) {
		if (player != null && player.getState() != StatelyPlayer.END) {
			player.reset();
			mIdlePlayers.add(player);
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized P getPlayer() {
		if (mIdlePlayers.size() > 0) {
			P player = (P) mIdlePlayers.toArray()[0];
			mIdlePlayers.remove(player);
			Log.d("Getting idle player (" + player + ")");
			return player;
		} else if (mRequests.size() > 0) {
			Uri leastRecentReq = mRequests.remove(mRequests.size() - 1);
			Log.d("Recycling the player that is prepared for " + leastRecentReq);
			P player = mMediaPlayers.remove(leastRecentReq);
			Log.d("Player: " + player);
			player.reset();
			return player;
		} else {
			throw new IllegalStateException(
					"MediaPlayer resources exhausted. Are you sure you're #recycle()ing on time?");
		}
	}

	private synchronized void addPlayer(P player, Uri uri) {
		mRequests.remove(uri);
		mRequests.add(0, uri);
		mMediaPlayers.put(uri, player);
	}
}
