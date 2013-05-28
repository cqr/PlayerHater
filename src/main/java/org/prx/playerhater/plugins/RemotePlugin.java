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

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.Song;
import org.prx.playerhater.service.IPlayerHaterBinder;
import org.prx.playerhater.util.RemoteSong;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;

public class RemotePlugin implements PlayerHaterPlugin {

	private IRemotePlugin mBinder;

	public RemotePlugin(IRemotePlugin binder) {
		mBinder = binder;
	}

	@Override
	public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
		// We assume this has already been taken care of. If not, there's not
		// much we can do about it.
	}

	@Override
	public void onServiceBound(IPlayerHaterBinder binder) {
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
			mBinder.onSongChanged(((RemoteSong) song).getTag());
		} catch (RemoteException e) {}
	}

	@Override
	public void onSongFinished(Song song, int reason) {
		try {
			mBinder.onSongFinished(((RemoteSong) song).getTag(), reason);
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
			mBinder.onNextSongAvailable(((RemoteSong) nextTrack).getTag());
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

	@Override
	public void onTransportControlFlagsChanged(int transportControlFlags) {
		try {
			mBinder.onTransportControlFlagsChanged(transportControlFlags);
		} catch (RemoteException e) {}
	}

}
