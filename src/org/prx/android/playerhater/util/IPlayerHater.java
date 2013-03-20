package org.prx.android.playerhater.util;

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

public interface IPlayerHater {
	// Controls
	boolean pause();

	boolean stop();

	// Playback
	boolean play();

	boolean play(int startTime);

	boolean play(Song song);

	boolean play(Song song, int startTime);

	boolean seekTo(int startTime);

	// Queuing
	void enqueue(Song song);

	boolean skipTo(int position);
	
	void skip();
	
	void skipBack();

	void emptyQueue();

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

	// Other Getters
	Song nowPlaying();

	boolean isPlaying();

	boolean isLoading();

	int getState();
}
