package org.prx.android.playerhater.plugins;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.PlayerHaterBinderPlugin;
import org.prx.android.playerhater.service.PlayerHaterServiceBinder;
import org.prx.android.playerhater.util.BasicSong;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;

public class BinderPlugin implements PlayerHaterPlugin {

	private PlayerHaterBinderPlugin mBinder;

	public BinderPlugin(PlayerHaterBinderPlugin binder) {
		mBinder = binder;
	}

	@Override
	public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
		// We assume this has already been taken care of. If not, there's not
		// much we can do about it.
	}

	@Override
	public void onServiceBound(PlayerHaterServiceBinder binder) {
		try {
			mBinder.onServiceBound(binder);
		} catch (RemoteException e) {}
	}

	@Override
	public void onServiceStopping() {
		// XXX
	}

	@Override
	public void onSongChanged(Song song) {
		try {
			mBinder.onSongChanged(((BasicSong)song).tag);
		} catch (RemoteException e) {}
	}

	@Override
	public void onSongFinished(Song song, int reason) {
		try {
			mBinder.onSongFinished(((BasicSong)song).tag, reason);
		} catch (RemoteException e) {}
	}

	@Override
	public void onDurationChanged(int duration) {
		try {
			mBinder.onDurationChanged(duration);
		} catch (RemoteException e) {}
	}

	@Override
	public void onAudioLoading() {
		try {
			mBinder.onAudioLoading();
		} catch (RemoteException e) {}
	}

	@Override
	public void onAudioPaused() {
		try {
			mBinder.onAudioPaused();
		} catch (RemoteException e) {}
	}

	@Override
	public void onAudioResumed() {
		try {
			mBinder.onAudioResumed();
		} catch (RemoteException e) {}
	}

	@Override
	public void onAudioStarted() {
		try {
			mBinder.onAudioStarted();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onAudioStopped() {
		try {
			mBinder.onAudioStopped();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onTitleChanged(String title) {
		try {
			mBinder.onTitleChanged(title);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onArtistChanged(String artist) {
		try {
			mBinder.onArtistChanged(artist);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onAlbumArtChanged(int resourceId) {
		try {
			mBinder.onAlbumArtResourceChanged(resourceId);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onAlbumArtChangedToUri(Uri url) {
		try {
			mBinder.onAlbumArtUriChanged(url);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onNextSongAvailable(Song nextTrack) {
		try {
			mBinder.onNextSongAvailable(((BasicSong)nextTrack).tag);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onNextSongUnavailable() {
		try {
			mBinder.onNextSongUnavailable();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onIntentActivityChanged(PendingIntent pending) {
		try {
			mBinder.onIntentActivityChanged(pending);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onChangesComplete() {
		try {
			mBinder.onChangesComplete();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
