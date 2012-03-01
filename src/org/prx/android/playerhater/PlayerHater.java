package org.prx.android.playerhater;

import java.io.FileDescriptor;
import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Bundle;

public interface PlayerHater {
	void seekTo(int position);
	
	boolean pause();
	boolean stop();
	
	boolean play() throws IllegalStateException, IOException;
	
	boolean play(String fileOrUrl) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(String fileOrUrl, boolean isUrl) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(String fileOrUrl, boolean isUrl, Activity activityTriggeredOnNotificationTouch) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(String fileOrUrl, boolean isUrl, Activity activityTriggeredOnNotificationTouch, int notificationView) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	
	boolean play(FileDescriptor fd) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(FileDescriptor fd, Activity activityTriggeredOnNotificationTouch) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(FileDescriptor fd, Activity activityTriggeredOnNotificationTouch, int notificationView) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	
	boolean play(Uri url) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(Uri url, Activity activityTriggeredOnNotificationTouch) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(Uri url, Activity activityTriggeredOnNotificationTouch, int notificationView) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	
	void transientPlay(String fileOrUrl);
	void transientPlay(String fileOrUrl, boolean isDuckable);
	void transientPlay(FileDescriptor file);
	void transientPlay(FileDescriptor file, boolean isDuckable);
	
	void setNotificationIntentActivity(Activity activityTriggeredOnNotificationTouch);
	void setNotificationIcon(int notificationIcon);
	void setNotificationView(int notificationView);
	void setNotificationTitle(String notificationTitle);
	void setNotificationText(String notificationText);
	
	void setAutoNotify(boolean autoNotify);
	void startForeground();
	void stopForeground();
	
	int getCurrentPosition();
	int getDuration();
	
	void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);
	void setOnCompletionListener(OnCompletionListener listener);
	void setOnInfoListener(OnInfoListener listener);
	void setOnSeekCompleteListener(OnSeekCompleteListener listener);
	void setOnErrorListener(OnErrorListener listener);
	void setOnPreparedListener(OnPreparedListener listener);
	
	void setListener(PlayerHaterListener listener);
	
	String getNowPlaying();
	boolean isPlaying();
	int getState();
	
	Bundle getBundle();
	void commitBundle(Bundle icicle);
}
