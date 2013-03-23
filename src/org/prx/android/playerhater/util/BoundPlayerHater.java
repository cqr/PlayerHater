package org.prx.android.playerhater.util;

import java.lang.ref.WeakReference;

import org.prx.android.playerhater.PlayerHater;
import org.prx.android.playerhater.PlayerHaterListener;
import org.prx.android.playerhater.Song;
import org.prx.android.playerhater.player.Player;
import org.prx.android.playerhater.plugins.PlayerHaterPlugin;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;

public class BoundPlayerHater extends PlayerHater {

	private final WeakReference<Context> mContext;
	private PlayerHaterPlugin mPlugin;
	private PlayerHater mPlayerHater;
	private final AutoBindHandle mAutoBindHandle = new AutoBindHandle() {

		@Override
		public void bind(PlayerHater playerHater) {
			BoundPlayerHater.this.bind(playerHater);
		}

		@Override
		public void unbind() {
			BoundPlayerHater.this.unbind();
		}

	};

	public BoundPlayerHater(Context context) {
		mContext = new WeakReference<Context>(context);
		requestAutoBind(mAutoBindHandle);
	}

	protected void bind(PlayerHater playerHater) {
		mPlayerHater = playerHater;
	}

	protected void unbind() {
		mPlayerHater = null;
	}

	public void setBoundPlugin(PlayerHaterPlugin plugin) {
		removeCurrentPlugin();
		mPlugin = plugin;
		sPlugins.add(plugin);
	}

	public void release() {
		removeCurrentPlugin();
		mContext.clear();
		release(mAutoBindHandle);
	}

	private void removeCurrentPlugin() {
		if (mPlugin != null) {
			sPlugins.remove(mPlugin);
		}
		mPlugin = null;
	}

	@Override
	public boolean pause() {
		if (mPlayerHater == null) {
			return false;
		} else {
			return mPlayerHater.pause();
		}
	}

	@Override
	public boolean stop() {
		if (mPlayerHater == null) {
			return true;
		} else {
			return mPlayerHater.stop();
		}
	}

	@Override
	public boolean play() {
		if (mPlayerHater != null) {
			return mPlayerHater.play();
		} else {
			return play(0);
		}
	}

	@Override
	public boolean play(int startTime) {
		if (mPlayerHater == null) {
			if (sPlayQueue.getNowPlaying() != null) {
				sStartPosition = startTime;
				startService();
				return true;
			} else {
				return false;
			}
		} else {
			return mPlayerHater.play(startTime);
		}
	}

	// @Override
	// public boolean play(Uri url, int startTime) {
	// return play(new BasicSong(url, null, null, null), startTime);
	// }

	@Override
	public boolean play(Song song) {
		return play(song, 0);
	}

	@Override
	public boolean play(Song song, int startTime) {
		if (mPlayerHater == null) {
			sStartPosition = startTime;
			sPlayQueue.appendSong(song);
			sPlayQueue.skipToEnd();
			startService();
			return true;
		} else {
			return mPlayerHater.play(song, startTime);
		}
	}

	@Override
	public boolean seekTo(int startTime) {
		if (mPlayerHater == null) {
			if (sPlayQueue.getNowPlaying() != null) {
				sStartPosition = startTime;
				return true;
			} else {
				return false;
			}
		} else {
			mPlayerHater.seekTo(startTime);
			return true;
		}
	}

	@Override
	public void setTitle(String title) {
		sPendingNotificationTitle = title;
		if (mPlayerHater != null) {
			mPlayerHater.setTitle(title);
		}
	}

	@Override
	public void setArtist(String artist) {
		sPendingNotificationText = artist;
		if (mPlayerHater != null) {
			mPlayerHater.setArtist(artist);
		}
	}

	@Override
	public void setActivity(Activity activity) {
		sPendingNotificationIntentActivity = activity;
		if (mPlayerHater != null) {
			mPlayerHater.setActivity(activity);
		}
	}

	@Override
	public int getCurrentPosition() {
		if (mPlayerHater == null) {
			return 0;
		}
		return mPlayerHater.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		if (mPlayerHater == null) {
			return 0;
		}
		return mPlayerHater.getDuration();
	}

	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		sPendingBufferingListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnBufferingUpdateListener(listener);
		}
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		sPendingCompleteListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnCompletionListener(listener);
		}
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		sPendingInfoListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnInfoListener(listener);
		}
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		sPendingSeekListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnSeekCompleteListener(listener);
		}
	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		sPendingErrorListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnErrorListener(listener);
		}
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		sPendingPreparedListener = listener;
		if (mPlayerHater != null) {
			mPlayerHater.setOnPreparedListener(listener);
		}
	}

	// @Override
	// public void setListener(PlayerHaterListener listener) {
	// setListener(listener, true);
	// }

	// @Override
	// public void setListener(PlayerHaterListener listener, boolean withEcho) {
	// sListener.setListener(listener, withEcho);
	// }

	@Override
	public Song nowPlaying() {
		if (sPlayQueue.getNowPlaying() != null) {
			return sPlayQueue.getNowPlaying();
		} else if (mPlayerHater == null) {
			return null;
		}
		return mPlayerHater.nowPlaying();
	}

	@Override
	public boolean isPlaying() {
		if (mPlayerHater == null) {
			return false;
		}
		return mPlayerHater.isPlaying();
	}

	@Override
	public boolean isLoading() {
		if (sPlayQueue.getNowPlaying() != null) {
			return true;
		} else if (mPlayerHater == null) {
			return false;
		}
		return mPlayerHater.isLoading();
	}

	@Override
	public int getState() {
		if (mPlayerHater == null) {
			return Player.IDLE;
		}
		return mPlayerHater.getState();
	}

	@Override
	public void setAlbumArt(int resourceId) {
		sPendingAlbumArtType = RESOURCE;
		sPendingAlbumArtResourceId = resourceId;
		if (mPlayerHater != null) {
			mPlayerHater.setAlbumArt(resourceId);
		}
	}

	@Override
	public void setAlbumArt(Uri url) {
		sPendingAlbumArtType = URL;
		sPendingAlbumArtUrl = url;
		if (mPlayerHater != null) {
			mPlayerHater.setAlbumArt(url);
		}
	}

	// @Override
	// public TransientPlayer playEffect(Uri url, boolean isDuckable) {
	// return TransientPlayer.play(mContext, url, isDuckable);
	// }

	@Override
	public boolean enqueue(Song song) {
		if (mPlayerHater != null) {
			return mPlayerHater.enqueue(song);
		} else {
			sPlayQueue.appendSong(song);
			return true;
		}
	}

	@Override
	public boolean skipTo(int position) {
		// XXX TODO FIXME OBVIOUSLY
		return false;
	}

	@Override
	public void emptyQueue() {
		if (mPlayerHater == null) {
			sPlayQueue.empty();
		} else {
			mPlayerHater.emptyQueue();
		}
	}

	@Override
	public void skip() {
		if (mPlayerHater == null) {
			sPlayQueue.next();
		} else {
			mPlayerHater.skip();
		}
	}

	@Override
	public void skipBack() {
		if (mPlayerHater == null) {
			sPlayQueue.back();
		} else {
			mPlayerHater.skipBack();
		}
	}

	@Override
	@Deprecated
	public void setListener(PlayerHaterListener listener) {
		// TODO Auto-generated method stub

	}
}
