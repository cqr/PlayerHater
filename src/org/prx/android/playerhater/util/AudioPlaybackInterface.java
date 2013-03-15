package org.prx.android.playerhater.util;

import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;

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
	boolean play();
	boolean play(int startTime);
	boolean play(Uri url);
	boolean play(Uri url, int startTime);
	boolean play(Song song);
	boolean play(Song song, int startTime);
	boolean seekTo(int startTime); 
	
	// Queuing
	void enqueue(Song song);
	boolean skipTo(int position);
	void emptyQueue();

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

	// PlayerHater listener
	void setListener(PlayerHaterListener listener);
	void setListener(PlayerHaterListener listener, boolean withEcho);

	// Other Getters
	Song nowPlaying();
	boolean isPlaying();
	boolean isLoading();
	int getState();
	
	// Plugins
	void registerPlugin(PlayerHaterPlugin plugin);
	void unregisterPlugin(PlayerHaterPlugin plugin);
}
