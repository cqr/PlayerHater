package org.prx.playerhater.ipc;

import org.prx.playerhater.service.PlayerHaterService;
import org.prx.playerhater.songs.SongHost;
import org.prx.playerhater.util.Log;
import org.prx.playerhater.wrappers.ServicePlayerHater;

import android.app.Notification;
import android.app.PendingIntent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

public class PlayerHaterServer extends IPlayerHaterServer.Stub {

	private final ServicePlayerHater mService;

	public PlayerHaterServer(PlayerHaterService service) {
		mService = new ServicePlayerHater(service);
	}
	
	public PlayerHaterServer(ServicePlayerHater playerHater) {
		mService = playerHater;
	}

	@Override
	public void setClient(IPlayerHaterClient client) throws RemoteException {
		mService.setClient(client);
	}

	@Override
	public void onRemoteControlButtonPressed(int keyCode)
			throws RemoteException {
		mService.onRemoteControlButtonPressed(keyCode);
	}

	@Override
	public void startForeground(int notificationNu, Notification notification)
			throws RemoteException {
		mService.startForeground(notificationNu, notification);
	}

	@Override
	public void stopForeground(boolean fact) throws RemoteException {
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
	public boolean pause() throws RemoteException {
		return mService.pause();
	}

	@Override
	public boolean stop() throws RemoteException {
		return mService.stop();
	}

	@Override
	public boolean resume() throws RemoteException {
		Log.d("Got it on the other side");
		return mService.play();
	}

	@Override
	public boolean playAtTime(int startTime) throws RemoteException {
		return mService.play(startTime);
	}

	@Override
	public boolean play(int songTag, int startTime) throws RemoteException {
		return mService.play(SongHost.getSong(songTag), startTime);
	}

	@Override
	public boolean seekTo(int startTime) throws RemoteException {
		return mService.seekTo(startTime);
	}

	@Override
	public int enqueue(int songTag) throws RemoteException {
		return mService.enqueue(SongHost.getSong(songTag));
	}

	@Override
	public boolean skipTo(int position) throws RemoteException {
		return mService.skipTo(position);
	}

	@Override
	public void skip() throws RemoteException {
		mService.skip();
	}

	@Override
	public void skipBack() throws RemoteException {
		mService.skipBack();
	}

	@Override
	public void emptyQueue() throws RemoteException {
		mService.emptyQueue();
	}

	@Override
	public int getCurrentPosition() throws RemoteException {
		return mService.getCurrentPosition();
	}

	@Override
	public int getDuration() throws RemoteException {
		return mService.getDuration();
	}

	@Override
	public int nowPlaying() throws RemoteException {
		return SongHost.getTag(mService.nowPlaying());
	}

	@Override
	public boolean isPlaying() throws RemoteException {
		return mService.isPlaying();
	}

	@Override
	public boolean isLoading() throws RemoteException {
		return mService.isLoading();
	}

	@Override
	public int getState() throws RemoteException {
		return mService.getState();
	}

	@Override
	public void setTransportControlFlags(int transportControlFlags)
			throws RemoteException {
		mService.setTransportControlFlags(transportControlFlags);
	}

	@Override
	public int getQueueLength() throws RemoteException {
		return mService.getQueueLength();
	}

	@Override
	public int getQueuePosition() throws RemoteException {
		return mService.getQueuePosition();
	}

	@Override
	public boolean removeFromQueue(int position) throws RemoteException {
		return mService.removeFromQueue(position);
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

	@Override
	public void setPendingIntent(PendingIntent intent) throws RemoteException {
		mService.setPendingIntent(intent);
	}

}
