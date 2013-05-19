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
package org.prx.android.playerhater.util;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.IRemotePlugin;

import android.net.Uri;
import android.os.RemoteException;

public class RemoteSong implements Song {

	private final int mTag;
	private final IRemotePlugin mPlugin;

	public RemoteSong(IRemotePlugin plugin, int tag) {
		mPlugin = plugin;
		mTag = tag;
	}

	@Override
	public String getTitle() {
		try {
			return mPlugin.getSongTitle(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public String getArtist() {
		try {
			return mPlugin.getSongArtist(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public Uri getAlbumArt() {
		try {
			return mPlugin.getSongAlbumArt(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	@Override
	public Uri getUri() {
		try {
			return mPlugin.getSongUri(mTag);
		} catch (RemoteException e) {
			throw new IllegalStateException(
					"Remote Process has died or become disconnected", e);
		}
	}

	public int getTag() {
		return mTag;
	}

}
