package org.prx.android.playerhater;

import java.io.FileDescriptor;

import android.app.Activity;
import android.net.Uri;

public interface PlayerHater {
	void seekTo(int position);
	
	boolean pause();
	boolean stop();
	
	boolean play();
	
	boolean play(String fileOrUrl);
	boolean play(String fileOrUrl, boolean isUrl);
	boolean play(String fileOrUrl, boolean isUrl, Activity activity);
	boolean play(String fileOrUrl, boolean isUrl, Activity activity, int view);
	
	boolean play(FileDescriptor fd);
	boolean play(FileDescriptor fd, Activity activity);
	boolean play(FileDescriptor fd, Activity activity, int view);
	
	boolean play(Uri url);
	boolean play(Uri url, Activity activity);
	boolean play(Uri url, Activity activity, int view);
	
	void setNotificationIntentActivity(Activity activity);
	void setNotificationView(int notificationView);
	
	int getPosition();
	
	void set(String key, Object value);
	Object get(String key);
	
	
	
	String getNowPlaying();
	boolean isPlaying();
	int getState();
}
