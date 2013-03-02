package org.prx.android.playerhater.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.lifecycle.LifecycleListener;

import android.net.Uri;

public class ListenerCollection implements LifecycleListener.RemoteControl {

	private final List<LifecycleListener> mListeners;

	public ListenerCollection() {
		mListeners = new ArrayList<LifecycleListener>();
	}

	public void add(LifecycleListener listener) {
		mListeners.add(listener);
	}

	@Override
	public void setIsPlaying(boolean isPlaying) {
		for (LifecycleListener listener : mListeners) {
			listener.setIsPlaying(isPlaying);
		}
	}

	@Override
	public void start(Song forSong, int duration) {
		for (LifecycleListener listener : mListeners) {
			listener.start(forSong, duration);
		}
	}

	@Override
	public void stop() {
		for (LifecycleListener listener : mListeners) {
			listener.stop();
		}
	}

	@Override
	public void setTitle(String title) {
		for (LifecycleListener listener : mListeners) {
			if (listener instanceof LifecycleListener.RemoteControl)
				((RemoteControl) listener).setTitle(title);
		}
	}

	@Override
	public void setArtist(String artist) {
		for (LifecycleListener listener : mListeners) {
			if (listener instanceof LifecycleListener.RemoteControl)
				((RemoteControl) listener).setArtist(artist);
		}
	}

	@Override
	public void setAlbumArt(int resourceId) {
		for (LifecycleListener listener : mListeners) {
			if (listener instanceof LifecycleListener.RemoteControl)
				((RemoteControl) listener).setAlbumArt(resourceId);
		}
	}

	@Override
	public void setAlbumArt(Uri url) {
		for (LifecycleListener listener : mListeners) {
			if (listener instanceof LifecycleListener.RemoteControl)
				((RemoteControl) listener).setAlbumArt(url);
		}
	}

}
