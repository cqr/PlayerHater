package com.chrisrhoden.glisten.wrappers;

import com.chrisrhoden.glisten.PlayerHater;
import com.chrisrhoden.glisten.Song;
import com.chrisrhoden.glisten.ipc.IPlayerHaterClient;
import com.chrisrhoden.glisten.service.PlayerHaterService;

import android.app.Notification;
import android.app.PendingIntent;
import android.support.v4.media.session.MediaSessionCompat;

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
		return play(song, 0);
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
	public int enqueue(Song song) {
		return mService.enqueue(song);
	}

	@Override
	public void enqueue(int position, Song song) {
		mService.enqueue(position, song);
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
	public int getCurrentPosition() {
		return mService.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return mService.getDuration();
	}

	@Override
	public Song nowPlaying() {
		return mService.nowPlaying();
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

	@Override
	public void setTransportControlFlags(int transportControlFlags) {
		mService.setTransportControlFlags(transportControlFlags);
	}

	@Override
	public int getQueueLength() {
		return mService.getQueueLength();
	}

	@Override
	public int getQueuePosition() {
		return mService.getQueuePosition();
	}

	@Override
	public boolean removeFromQueue(int position) {
		return mService.removeFromQueue(position);
	}

	@Override
	public void setPendingIntent(PendingIntent intent) {
		mService.setPendingIntent(intent);
	}

	/*
	 * Service Specific stuff.
	 */

	public void setClient(IPlayerHaterClient client) {
		mService.setClient(client);
	}

	public void onRemoteControlButtonPressed(int keyCode) {
		mService.onRemoteControlButtonPressed(keyCode);
	}

	public void startForeground(int notificationNu, Notification notification) {
		mService.startForeground(notificationNu, notification);
	}

	public void stopForeground(boolean removeNotification) {
		mService.stopForeground(removeNotification);
//		mService.quit();
	}

	public void duck() {
		mService.duck();
	}

	public void unduck() {
		mService.unduck();
	}

	public MediaSessionCompat.Token getMediaSession() {
		return mService.getMediaSession();
	}

	public void setMediaSession(MediaSessionCompat.Token token) {
		mService.setMediaSession(token);
	}

	@Override
	public int getTransportControlFlags() {
		return mService.getTransportControlFlags();
	}
}
