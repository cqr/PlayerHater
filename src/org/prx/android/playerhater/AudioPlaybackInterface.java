package org.prx.android.playerhater;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;

public interface AudioPlaybackInterface {
	
	// Controls
	boolean pause();
	boolean stop();
	
	// Playback
	boolean play() throws IllegalStateException, IOException;
	boolean play(int startTime) throws IllegalStateException, IOException;
	boolean play(Uri url) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(Uri url, int startTime) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(Song song) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	boolean play(Song song, int startTime) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException;
	
	// For sound effects
	TransientPlayer playEffect(Uri url);
	TransientPlayer playEffect(Uri url, boolean isDuckable);
	
	// Notification data
	void setAlbumArt(int resourceId);
	void setAlbumArt(Uri url);
	void setTitle(String title);
	void setArtist(String artist);
	void setActivity(Activity activity);
	
	// Scubber-related data
	int getCurrentPosition();
	int getDuration();
	
	// Media Player listeners
	void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);
	void setOnCompletionListener(OnCompletionListener listener);
	void setOnInfoListener(OnInfoListener listener);
	void setOnSeekCompleteListener(OnSeekCompleteListener listener);
	void setOnErrorListener(OnErrorListener listener);
	void setOnPreparedListener(OnPreparedListener listener);
	
	//PlayerHater listener
	void setListener(PlayerHaterListener listener);
	
	
	// Other Getters
	Song nowPlaying();
	boolean isPlaying();
	boolean isLoading();
	int getState();
}
