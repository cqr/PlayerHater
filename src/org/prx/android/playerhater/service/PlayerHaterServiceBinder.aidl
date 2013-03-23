package org.prx.android.playerhater.service;

import android.net.Uri;
import org.prx.android.playerhater.service.PlayerHaterBinderPlugin;
import android.app.Notification;


interface PlayerHaterServiceBinder {

	void setBinder(PlayerHaterBinderPlugin binder);

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