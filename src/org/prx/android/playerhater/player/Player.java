package org.prx.android.playerhater.player;

import android.content.Context;
import android.net.Uri;

public interface Player extends MediaPlayerWithState {

	public abstract boolean prepare(Context context, Uri uri);

	public abstract void setNextMediaPlayer(MediaPlayerWithState mediaPlayer);

	public abstract boolean prepareAndPlay(Context applicationContext, Uri uri,
			int position);

}