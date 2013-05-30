package org.prx.playerhater.ipc;

import org.prx.playerhater.PlayerHaterPlugin;
import org.prx.playerhater.songs.SongHost;
import org.prx.playerhater.util.Log;

import android.app.PendingIntent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

public class PlayerHaterClient extends IPlayerHaterClient.Stub {
	
	private final PlayerHaterPlugin mPlugin;
	
	public PlayerHaterClient(PlayerHaterPlugin plugin) {
		mPlugin = plugin;
	}

	@Override
	public void onSongChanged(int songTag) throws RemoteException {
		Log.d("GOT CALLED " + songTag);
		mPlugin.onSongChanged(SongHost.getSong(songTag));
	}

	@Override
	public void onSongFinished(int songTag, int reason) throws RemoteException {
		mPlugin.onSongFinished(SongHost.getSong(songTag), reason);
	}

	@Override
	public void onDurationChanged(int duration) throws RemoteException {
		mPlugin.onDurationChanged(duration);
	}

	@Override
	public void onAudioLoading() throws RemoteException {
		mPlugin.onAudioLoading();
	}

	@Override
	public void onAudioPaused() throws RemoteException {
		mPlugin.onAudioPaused();
	}

	@Override
	public void onAudioResumed() throws RemoteException {
		mPlugin.onAudioResumed();
	}

	@Override
	public void onAudioStarted() throws RemoteException {
		mPlugin.onAudioStarted();
	}

	@Override
	public void onAudioStopped() throws RemoteException {
		mPlugin.onAudioStopped();
	}

	@Override
	public void onTitleChanged(String title) throws RemoteException {
		mPlugin.onTitleChanged(title);
	}

	@Override
	public void onArtistChanged(String artist) throws RemoteException {
		mPlugin.onArtistChanged(artist);
	}

	@Override
	public void onAlbumTitleChanged(String albumTitle) throws RemoteException {
		mPlugin.onAlbumTitleChanged(albumTitle);
	}

	@Override
	public void onAlbumArtChanged(Uri uri) throws RemoteException {
		mPlugin.onAlbumArtChanged(uri);
	}

	@Override
	public void onTransportControlFlagsChanged(int transportControlFlags)
			throws RemoteException {
		mPlugin.onTransportControlFlagsChanged(transportControlFlags);
	}

	@Override
	public void onNextSongAvailable(int songTag) throws RemoteException {
		mPlugin.onNextSongAvailable(SongHost.getSong(songTag));
	}

	@Override
	public void onNextSongUnavailable() throws RemoteException {
		mPlugin.onNextSongUnavailable();
	}

	@Override
	public void onChangesComplete() throws RemoteException {
		mPlugin.onChangesComplete();
	}

	@Override
	public void onIntentActivityChanged(PendingIntent intent)
			throws RemoteException {
		mPlugin.onPendingIntentChanged(intent);
	}

	@Override
	public String getSongTitle(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getTitle();
	}

	@Override
	public String getSongArtist(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getArtist();
	}

	@Override
	public String getSongAlbumTitle(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getAlbumTitle();
	}

	@Override
	public Uri getSongAlbumArt(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getAlbumArt();
	}

	@Override
	public Uri getSongUri(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getUri();
	}

	@Override
	public Bundle getSongExtra(int songTag) throws RemoteException {
		return SongHost.getSong(songTag).getExtra();
	}

}
