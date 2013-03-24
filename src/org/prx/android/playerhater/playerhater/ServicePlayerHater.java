package org.prx.android.playerhater.playerhater;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.PlayerHaterListenerPlugin;
import org.prx.android.playerhater.service.PlayerHaterService;

import android.app.Activity;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;

public class ServicePlayerHater extends PlayerHater {
	private final PlayerHaterService mService;
	
	public ServicePlayerHater(PlayerHaterService service) {
		mService = service;
	}

	@Override
	public boolean pause() {
		return mService.pause();
	}

	@Override
	public boolean stop() {
		return mService.stop();
	}

	@Override
	public boolean play() {
		return mService.play();
	}

	@Override
	public boolean play(int startTime) {
		return mService.play(startTime);
	}

	@Override
	public boolean play(Song song) {
		return mService.play(song);
	}

	@Override
	public boolean play(Song song, int startTime) {
		return mService.play(song, startTime);
	}

	@Override
	public boolean seekTo(int startTime) {
		return mService.seekTo(startTime);
	}

	@Override
	public boolean enqueue(Song song) {
		return mService.enqueue(song);
	}

	@Override
	public boolean skipTo(int position) {
		return mService.skipTo(position);
	}

	@Override
	public void skip() {
		mService.skip();
	}

	@Override
	public void skipBack() {
		mService.skipBack();
	}

	@Override
	public void emptyQueue() {
		mService.emptyQueue();
	}

	@Override
	public void setAlbumArt(int resourceId) {
		mService.setAlbumArt(resourceId);
	}

	@Override
	public void setAlbumArt(Uri url) {
		mService.setAlbumArt(url);
	}

	@Override
	public void setTitle(String title) {
		mService.setTitle(title);
	}

	@Override
	public void setArtist(String artist) {
		mService.setArtist(artist);
	}

	@Override
	public void setActivity(Activity activity) {
		mService.setIntentClass(activity.getClass());
	}

	@Override
	public int getCurrentPosition() {
		return mService.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return mService.getDuration();
	}

//	@Override
//	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
//		mService.setOnBufferingUpdateListener(listener);
//	}
//
//	@Override
//	public void setOnCompletionListener(OnCompletionListener listener) {
//		mService.setOnCompletionListener(listener);
//	}
//
//	@Override
//	public void setOnInfoListener(OnInfoListener listener) {
//		mService.setOnInfoListener(listener);
//	}
//
//	@Override
//	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
//		mService.setOnSeekCompleteListener(listener);
//	}
//
//	@Override
//	public void setOnErrorListener(OnErrorListener listener) {
//		mService.setOnErrorListener(listener);
//	}
//
//	@Override
//	public void setOnPreparedListener(OnPreparedListener listener) {
//		mService.setOnPreparedListener(listener);
//	}

	@Override
	@Deprecated
	public void setListener(PlayerHaterListener listener) {
		mService.addPluginInstance(new PlayerHaterListenerPlugin(listener));
	}

	@Override
	public Song nowPlaying() {
		return mService.getNowPlaying();
	}

	@Override
	public boolean isPlaying() {
		return mService.isPlaying();
	}

	@Override
	public boolean isLoading() {
		return mService.isLoading();
	}

	@Override
	public int getState() {
		return mService.getState();
	}

}
