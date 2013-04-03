package org.prx.android.playerhater.playerhater;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.service.IPlayerHaterBinder;
import org.prx.android.playerhater.util.BasicSong;

import android.app.Activity;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

public class BinderPlayerHater extends PlayerHater {

	private static BinderPlayerHater sInstance;
	private final IPlayerHaterBinder mBinder;
	private final SparseArray<Song> mSongs = new SparseArray<Song>();

	public static BinderPlayerHater get(IPlayerHaterBinder binder) {
		if (sInstance == null) {
			sInstance = new BinderPlayerHater(binder);
		}
		return sInstance;
	}

	public static void detach() {
		sInstance = null;
	}

	private BinderPlayerHater(IPlayerHaterBinder binder) {
		mBinder = binder;
	}

	@Override
	public boolean pause() {
		try {
			return mBinder.pause();
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean stop() {
		try {
			return mBinder.stop();
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean play() {
		try {
			return mBinder.resume();
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean play(int startTime) {
		try {
			return mBinder.play(startTime);
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean play(Song song) {
		return play(song, 0);
	}

	@Override
	public boolean play(Song song, int startTime) {
		try {
			if (enqueue(song)) {
				if (skipTo(mBinder.getQueueLength())) {
					play(startTime);
				}
			}
		} catch (RemoteException e) {
			removeSong(song);
		}
		return false;
	}

	@Override
	public boolean seekTo(int startTime) {
		try {
			return mBinder.seekTo(startTime);
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean enqueue(Song song) {
		try {
			return mBinder.enqueue(song.getUri(), song.getTitle(),
					song.getArtist(), song.getAlbumArt(), tagSong(song));
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean skipTo(int position) {
		try {
			return mBinder.skipTo(position);
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public void skip() {
		try {
			mBinder.skip();
		} catch (RemoteException e) {}
	}

	@Override
	public void skipBack() {
		try {
			mBinder.skipBack();
		} catch (RemoteException e) {}
	}

	@Override
	public void emptyQueue() {
		try {
			mBinder.emptyQueue();
		} catch (RemoteException e) {}
	}

	@Override
	public void setAlbumArt(int resourceId) {
		try {
			mBinder.setAlbumArtResource(resourceId);
		} catch (RemoteException e) {}
	}

	@Override
	public void setAlbumArt(Uri url) {
		try {
			mBinder.setAlbumArtUrl(url);
		} catch (RemoteException e) {}
	}

	@Override
	public void setTitle(String title) {
		try {
			mBinder.setTitle(title);
		} catch (RemoteException e) {}
	}

	@Override
	public void setArtist(String artist) {
		try {
			mBinder.setArtist(artist);
		} catch (RemoteException e) {}
	}

	@Override
	public void setActivity(Activity activity) {
		// XXX
	}

	@Override
	public int getCurrentPosition() {
		try {
			return mBinder.getCurrentPosition();
		} catch (RemoteException e) {
			return -1;
		}
	}

	@Override
	public int getDuration() {
		try {
			return mBinder.getDuration();
		} catch (RemoteException e) {
			return -1;
		}
	}

	@Override
	@Deprecated
	/**
	 * @author chris
	 * @throws IllegalStateException Always.
	 */
	public void setListener(PlayerHaterListener listener) {
		throw new IllegalStateException("This is not supported.");
	}

	@Override
	public Song nowPlaying() {
		try {
			return getSong(mBinder.getNowPlayingTag());
		} catch (RemoteException e) {
			return null;
		}
	}

	@Override
	public boolean isPlaying() {
		try {
			return mBinder.isPlaying();
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean isLoading() {
		try {
			return mBinder.isLoading();
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public int getState() {
		try {
			return mBinder.getState();
		} catch (RemoteException e) {
			return -1;
		}
	}

	@Override
	public void setTransportControlFlags(int transportControlFlags) {
		try {
			mBinder.setTransportControlFlags(transportControlFlags);
		} catch (RemoteException e) {}
	}

	private int tagSong(Song song) {
		mSongs.put(song.hashCode(), song);
		return song.hashCode();
	}

	private void removeSong(Song song) {
		mSongs.delete(song.hashCode());
	}

	public Song getSong(int nowPlayingTag) {
		return mSongs.get(nowPlayingTag);
	}

	public IPlayerHaterBinder getBinder() {
		return mBinder;
	}

	public void releaseSong(int songTag) {
		// XXX TODO This probably isn't necessary (this method is only called by
		// the service when it is sure that it will never send this tag again),
		// but I am leaving it here for now. It is a leak, by its very
		// definition, so probably should be removed soon.
		Log.d("release", "Releasing a song " + songTag);
		if (mSongs.get(songTag) != null) {
			Song newSong = new BasicSong(mSongs.get(songTag));
			mSongs.delete(songTag);
			mSongs.put(songTag, newSong);
		}
	}

}
