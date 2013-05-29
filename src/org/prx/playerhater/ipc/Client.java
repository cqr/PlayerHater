package org.prx.playerhater.ipc;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.PlayerHaterPlugin;
import org.prx.playerhater.Song;
import org.prx.playerhater.songs.SongHost;
import org.prx.playerhater.util.Log;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;

public class Client implements PlayerHaterPlugin {

	private static final String CLIENT_ERROR = "Client has gone away...";

	private final IPlayerHaterClient mClient;

	public Client(IPlayerHaterClient client) {
		mClient = client;
	}

	@Override
	public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
	}

	@Override
	public void onSongChanged(Song song) {
		try {
			mClient.onSongChanged(SongHost.getTag(song));
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onSongFinished(Song song, int reason) {
		try {
			mClient.onSongFinished(SongHost.getTag(song), reason);
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onDurationChanged(int duration) {
		try {
			mClient.onDurationChanged(duration);
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onAudioLoading() {
		try {
			mClient.onAudioLoading();
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onAudioPaused() {
		try {
			mClient.onAudioPaused();
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onAudioResumed() {
		try {
			mClient.onAudioResumed();
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onAudioStarted() {
		try {
			mClient.onAudioStarted();
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onAudioStopped() {
		try {
			mClient.onAudioStopped();
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onTitleChanged(String title) {
		try {
			mClient.onTitleChanged(title);
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onArtistChanged(String artist) {
		try {
			mClient.onArtistChanged(artist);
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onAlbumArtChanged(Uri url) {
		try {
			mClient.onAlbumArtChanged(url);
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onNextSongAvailable(Song nextTrack) {
		try {
			mClient.onNextSongAvailable(SongHost.getTag(nextTrack));
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onNextSongUnavailable() {
		try {
			mClient.onNextSongUnavailable();
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onTransportControlFlagsChanged(int transportControlFlags) {
		try {
			mClient.onTransportControlFlagsChanged(transportControlFlags);
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onIntentActivityChanged(PendingIntent intent) {
		try {
			mClient.onIntentActivityChanged(intent);
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}

	@Override
	public void onChangesComplete() {
		try {
			mClient.onChangesComplete();
		} catch (RemoteException e) {
			Log.e(CLIENT_ERROR, e);
			throw new IllegalStateException(CLIENT_ERROR, e);
		}
	}
}
