package org.prx.android.playerhater.service;

import android.net.Uri;
import org.prx.android.playerhater.plugins.IRemotePlugin;
import android.app.Notification;


interface IPlayerHaterBinder {

	void setRemotePlugin(IRemotePlugin binder);

	boolean enqueue(in Uri uri, String title, String artist, in Uri albumArt, int tag);
	int getQueueLength();
	void emptyQueue();
	boolean skipTo(int position);
	boolean skip();
	boolean skipBack();
	
	boolean resume();
	boolean pause();
	boolean stop();
	boolean play(int startTime);
	boolean seekTo(int startTime);
	
	void setAlbumArtResource(int resourceId);
	void setAlbumArtUrl(in Uri uri);
	void setTitle(String title);
	void setArtist(String artist);
	void setTransportControlFlags(int transportControlFlags);
	
	int getDuration();
	int getCurrentPosition();
	
	int getNowPlayingTag();
	
	boolean isPlaying();
	boolean isLoading();
	int getState();
	
	void onRemoteControlButtonPressed(int keyCode);
	
	void startForeground(int notificationNu, in Notification notification);
	void stopForeground(boolean fact);

	void duck();
	void unduck();
}