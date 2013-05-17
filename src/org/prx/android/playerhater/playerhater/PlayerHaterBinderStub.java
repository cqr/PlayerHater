package org.prx.android.playerhater.playerhater;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.IRemotePlugin;
import org.prx.android.playerhater.plugins.RemotePlugin;
import org.prx.android.playerhater.service.AbsPlaybackService;
import org.prx.android.playerhater.service.IPlayerHaterBinder;
import org.prx.android.playerhater.service.PlayerHaterService;
import org.prx.android.playerhater.util.BasicSong;

import android.annotation.TargetApi;
import android.app.Notification;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;

public class PlayerHaterBinderStub extends IPlayerHaterBinder.Stub {

	private final ReferenceQueue<Song> mSongReferenceQueue;
	private final PlayerHater mPlayerHater;
	private final PlayerHaterService mService;

	public PlayerHaterBinderStub(PlayerHaterService service) {
		mPlayerHater = new ThreadsafePlayerHater(
				new ServicePlayerHater(service));
		mService = service;
		mSongReferenceQueue = new ReferenceQueue<Song>();
	}

	@Override
	public int enqueue(Uri uri, String title, String artist, Uri albumArt,
			int tag) throws RemoteException {
		final Song song = new BasicSong(uri, title, artist, albumArt, tag);
		new PhantomSongReference(song, mSongReferenceQueue);
		return mPlayerHater.enqueue(song);
	}

	@Override
	public boolean skipTo(int position) throws RemoteException {
		return mPlayerHater.skipTo(position);
	}

	@Override
	public boolean pause() throws RemoteException {
		return mPlayerHater.pause();
	}

	@Override
	public boolean stop() throws RemoteException {
		return mPlayerHater.stop();
	}

	@Override
	public boolean play(final int startTime) throws RemoteException {
		return mPlayerHater.play();
	}

	@Override
	public boolean seekTo(final int startTime) throws RemoteException {
		return mPlayerHater.seekTo(startTime);
	}

	@Override
	public void setAlbumArtResource(final int resourceId)
			throws RemoteException {
		mPlayerHater.setAlbumArt(resourceId);
	}

	@Override
	public void setAlbumArtUrl(final Uri albumArt) throws RemoteException {
		mPlayerHater.setAlbumArt(albumArt);
	}

	@Override
	public void setTitle(final String title) throws RemoteException {
		mPlayerHater.setTitle(title);
	}

	@Override
	public void setArtist(final String artist) throws RemoteException {
		mPlayerHater.setArtist(artist);
	}

	@Override
	public void setTransportControlFlags(final int transportControlFlags)
			throws RemoteException {
		mPlayerHater.setTransportControlFlags(transportControlFlags);
	}

	@Override
	public int getDuration() throws RemoteException {
		return mPlayerHater.getDuration();
	}

	@Override
	public int getCurrentPosition() throws RemoteException {
		return mPlayerHater.getCurrentPosition();
	}

	@Override
	public int getNowPlayingTag() throws RemoteException {
		return ((BasicSong) mPlayerHater.nowPlaying()).tag;
	}

	@Override
	public boolean isPlaying() throws RemoteException {
		return mPlayerHater.isPlaying();
	}

	@Override
	public boolean isLoading() throws RemoteException {
		return mPlayerHater.isLoading();
	}

	@Override
	public int getState() throws RemoteException {
		return mPlayerHater.getState();
	}

	@Override
	public void setRemotePlugin(IRemotePlugin binder) throws RemoteException {
		mService.removeRemotePlugin();

		if (binder != null) {
			binder.onServiceBound(this);
			mService.setPluginBinder(binder);
			mService.getPluginCollection().add(new RemotePlugin(binder),
					AbsPlaybackService.REMOTE_PLUGIN);
		}
	}

	@Override
	public int getQueueLength() throws RemoteException {
		return mPlayerHater.getQueueLength();
	}

	@Override
	public void emptyQueue() throws RemoteException {
		mPlayerHater.emptyQueue();
	}

	@Override
	public boolean skip() throws RemoteException {
		mPlayerHater.skip();
		return true;
	}

	@Override
	public boolean skipBack() throws RemoteException {
		mPlayerHater.skipBack();
		return true;
	}

	@Override
	public boolean resume() throws RemoteException {
		return mPlayerHater.play();
	}

	@Override
	public void onRemoteControlButtonPressed(final int keyCode) {
		mService.onRemoteControlButtonPressed(keyCode);
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	@Override
	public void startForeground(final int notificationNu,
			final Notification notification) throws RemoteException {
		mService.startForeground(notificationNu, notification);
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	@Override
	public void stopForeground(final boolean fact) throws RemoteException {
		mService.stopForeground(fact);
	}

	@Override
	public void duck() throws RemoteException {
		mService.duck();
	}

	@Override
	public void unduck() throws RemoteException {
		mService.unduck();
	}

	@Override
	public int getQueuePosition() throws RemoteException {
		return mPlayerHater.getQueuePosition();
	}

	@Override
	public boolean removeFromQueue(final int position) throws RemoteException {
		return mPlayerHater.removeFromQueue(position);
	}

	private static class PhantomSongReference extends PhantomReference<Song> {
		@SuppressWarnings("unused")
		public final int tag;

		public PhantomSongReference(Song r, ReferenceQueue<? super Song> q) {
			super(r, q);
			tag = ((BasicSong) r).tag;
		}

	}

}
