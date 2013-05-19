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
