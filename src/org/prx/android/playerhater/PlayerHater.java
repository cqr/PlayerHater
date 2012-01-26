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
import android.widget.SeekBar.OnSeekBarChangeListener;

public interface PlayerHater {
	void seekTo(int position);
	
	boolean pause();
	boolean stop();
	
	boolean play() throws IllegalStateException, IOException;
	
	boolean play(String fileOrUrl) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(String fileOrUrl, boolean isUrl) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(String fileOrUrl, boolean isUrl, Activity activity) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(String fileOrUrl, boolean isUrl, Activity activity, int view) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	
	boolean play(FileDescriptor fd) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(FileDescriptor fd, Activity activity) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(FileDescriptor fd, Activity activity, int view) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	
	boolean play(Uri url) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(Uri url, Activity activity) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(Uri url, Activity activity, int view) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	
	void setNotificationIntentActivity(Activity activity);
	void setNotificationView(int notificationView);
	
	int getCurrentPosition();
	
	void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);
	void setOnCompletionListener(OnCompletionListener listener);
	void setOnInfoListener(OnInfoListener listener);
	void setOnSeekCompleteListener(OnSeekCompleteListener listener);
	void setOnErrorListener(OnErrorListener listener);
	void setOnPreparedListener(OnPreparedListener listener);
	void setOnProgressChangeListener(OnSeekBarChangeListener listener);
	
	String getNowPlaying();
	boolean isPlaying();
	int getState();
}
