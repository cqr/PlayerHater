package org.prx.android.playerhater.service;

import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;

import android.app.Activity;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Binder;

public class PlayerHaterBinder extends Binder implements IPlayerHaterBinder {

	private final PlayerHaterService mService;

	public PlayerHaterBinder(PlayerHaterService service) {
		mService = service;
	}
	

	public void registerShutdownRequestListener(OnShutdownRequestListener listener) {
		mService.setOnShutdownRequestListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#play(org.prx.android.playerhater.Song, int)
	 */
	@Override
	public boolean play(Song song, int position)
			throws IllegalArgumentException {
		return mService.play(song, position);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#pause()
	 */
	@Override
	public boolean pause() {
		return mService.pause();
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#stop()
	 */
	@Override
	public boolean stop() {
		return mService.stop();
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#play()
	 */
	@Override
	public boolean play() throws IllegalStateException {
		return mService.play();
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#play(int)
	 */
	@Override
	public boolean play(int startTime) throws IllegalStateException {
		return mService.play(startTime);
	}
	
	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#seekTo(int)
	 */
	@Override
	public void seekTo(int startTime) throws IllegalStateException { 
		mService.seekTo(startTime); 
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
		mService.setTitle(title);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setArtist(java.lang.String)
	 */
	@Override
	public void setArtist(String artist) {
		mService.setArtist(artist);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setIntentActivity(android.app.Activity)
	 */
	@Override
	public void setIntentActivity(Activity activity) {
		mService.setIntentClass(activity.getClass());
	}
	
	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#getCurrentPosition()
	 */
	@Override
	public int getCurrentPosition() {
		return mService.getCurrentPosition();
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#getDuration()
	 */
	@Override
	public int getDuration() {
		return mService.getDuration();
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setOnBufferingUpdateListener(android.media.MediaPlayer.OnBufferingUpdateListener)
	 */
	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mService.setOnBufferingUpdateListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setOnCompletionListener(android.media.MediaPlayer.OnCompletionListener)
	 */
	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mService.setOnCompletionListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setOnInfoListener(android.media.MediaPlayer.OnInfoListener)
	 */
	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		mService.setOnInfoListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setOnSeekCompleteListener(android.media.MediaPlayer.OnSeekCompleteListener)
	 */
	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mService.setOnSeekCompleteListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setOnErrorListener(android.media.MediaPlayer.OnErrorListener)
	 */
	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		mService.setOnErrorListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setOnPreparedListener(android.media.MediaPlayer.OnPreparedListener)
	 */
	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		mService.setOnPreparedListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setListener(org.prx.android.playerhater.PlayerHaterListener)
	 */
	@Override
	public void setListener(PlayerHaterListener listener) {
		mService.setListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#getNowPlaying()
	 */
	@Override
	public Song getNowPlaying() {
		return mService.getNowPlaying();
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#isPlaying()
	 */
	@Override
	public boolean isPlaying() {
		return mService.isPlaying();
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#isLoading()
	 */
	@Override
	public boolean isLoading() {
		return mService.isLoading();
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#getState()
	 */
	@Override
	public int getState() {
		return mService.getState();
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setAlbumArt(int)
	 */
	@Override
	public void setAlbumArt(int resourceId) {
		mService.setAlbumArt(resourceId);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#setAlbumArt(android.net.Uri)
	 */
	@Override
	public void setAlbumArt(Uri url) {
		mService.setAlbumArt(url);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#enqueue(org.prx.android.playerhater.Song)
	 */
	@Override
	public void enqueue(Song song) {
		mService.enqueue(song);
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#emptyQueue()
	 */
	@Override
	public void emptyQueue() {
		mService.emptyQueue();
	}

	/* (non-Javadoc)
	 * @see org.prx.android.playerhater.service.IPlayerHaterBinder#onRemoteControlButtonPressed(int)
	 */
	@Override
	public void onRemoteControlButtonPressed(int keyCode) {
		mService.onRemoteControlButtonPressed(keyCode);
	}

}
