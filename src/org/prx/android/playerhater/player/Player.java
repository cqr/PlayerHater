package org.prx.android.playerhater.player;

import android.content.Context;
import android.net.Uri;

public interface Player extends MediaPlayerWithState {
	
	/* Synchronous API */

	public abstract boolean prepare(Context context, Uri uri);

	public abstract boolean prepareAndPlay(Context applicationContext, Uri uri,
			int position);
	
	public abstract boolean conditionalPause();

	public abstract boolean conditionalStop();
	
	public abstract boolean conditionalPlay();
	
	/* Gapless API */
	
	public abstract void setNextMediaPlayer(MediaPlayerWithState mediaPlayer);
	
	public abstract void skip();
	
	public abstract void skip(boolean autoPlay);

}