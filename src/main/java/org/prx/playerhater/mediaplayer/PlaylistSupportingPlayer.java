package org.prx.playerhater.mediaplayer;

import java.io.IOException;

import org.prx.playerhater.util.PlaylistParser;
import org.prx.playerhater.mediaplayer.Player.StateChangeListener;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class PlaylistSupportingPlayer extends SynchronousPlayer implements
		StateChangeListener {
	private static final String HTTP = "http";
	private static final String HTTPS = "https";
	private Uri[] mPlaylist;
	private Context mContext = null;
	private int mQueuePosition = 0;
	private int streamType = -1;
	private PlaylistSupportingPlayer mCurrentPlayer = this;
	private PlaylistSupportingPlayer mNextPlayer = null;
	private boolean mDieOnCompletion = false;

	@Override
	public synchronized void setDataSource(Context context, Uri uri)
			throws IllegalStateException, IOException,
			IllegalArgumentException, SecurityException {
		if (mNextPlayer != null) {
			if (mNextPlayer != this) {
				mNextPlayer.release();
			}
			mNextPlayer = null;
		}
		if (mCurrentPlayer != this) {
			mCurrentPlayer.release();
			mCurrentPlayer = this;
		}
		mPlaylist = null;
		mContext = null;
		if (uri.getScheme().equals(HTTP) || uri.getScheme().equals(HTTPS)) {
			mPlaylist = PlaylistParser.parsePlaylist(uri);
			mContext = context;
			mQueuePosition = 0;
			mCurrentPlayer = this;
			if (uri.equals(mPlaylist[0])) {
				super.setDataSource(context, uri);
			} else {
				mCurrentPlayer.setDataSource(context, mPlaylist[0]);
			}
			if (mPlaylist.length > 1) {
				mNextPlayer = newPlayer();
				mNextPlayer.setDataSource(context, uri);
			}
		} else {
			super.setDataSource(context, uri);
			mCurrentPlayer = this;
		}
	}

	@Override
	public void prepareAsync() {
		if (mCurrentPlayer == this) {
			super.prepareAsync();
		} else {
			mCurrentPlayer.prepareAsync();
		}
		if (mNextPlayer != null) {
			mNextPlayer.prepareAsync();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		boolean handled = false;
		if (super.equals(mp)) { // This came from our own player.
			handled = super.onError(mp, what, extra);
		} else { // We're getting this callback from one of our other players.
			handled = super.onError(getBarePlayer(), what, extra);
		}
		if (!handled) {
			mDieOnCompletion = true;
		}
		return handled;
	}

	@Override
	public synchronized void onCompletion(MediaPlayer mp) {
		if (mDieOnCompletion) {
			mDieOnCompletion = false;
		} else if (mPlaylist != null) {
			mQueuePosition += 1;
			if (mQueuePosition < mPlaylist.length) {
				PlaylistSupportingPlayer tmp = mCurrentPlayer;
				mCurrentPlayer = mNextPlayer;
				mNextPlayer = tmp;
				mNextPlayer.start();
				if (mQueuePosition + 1 < mPlaylist.length) {
					mNextPlayer.reset();
					try {
						mNextPlayer.setDataSource(mContext,
								mPlaylist[mQueuePosition + 1]);
						mNextPlayer.prepareAsync();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					if (mNextPlayer != this) {
						mNextPlayer.release();
					}
					mNextPlayer = null;
				}
				return;
			}
		}
		super.onCompletion(getBarePlayer());
	}

	@Override
	public void onStateChanged(Player mediaPlayer, int state) {
		super.onStateChanged();
	}

	@Override
	public int getStateMask() {
		if (mCurrentPlayer == this) {
			return super.getStateMask();
		} else {
			return mCurrentPlayer.getStateMask();
		}
	}

	@Override
	public void reset() {
		super.reset();
		if (mNextPlayer != null && mNextPlayer != this) {
			mNextPlayer.reset();
		}
		if (mCurrentPlayer != null && mCurrentPlayer != this) {
			mCurrentPlayer.reset();
		}
	}

	@Override
	public void release() {
		super.release();
		if (mNextPlayer != null && mNextPlayer != this) {
			mNextPlayer.release();
		}
		if (mCurrentPlayer != null && mCurrentPlayer != this) {
			mCurrentPlayer.release();
		}
	}

	@Override
	public void start() throws IllegalStateException {
		if (mCurrentPlayer == this) {
			super.start();
		} else {
			mCurrentPlayer.start();
		}
	}

	@Override
	public void pause() throws IllegalStateException {
		if (mCurrentPlayer == this) {
			super.pause();
		} else {
			mCurrentPlayer.pause();
		}
	}

	@Override
	public void stop() throws IllegalStateException {
		if (mCurrentPlayer == this) {
			super.stop();
		} else {
			mCurrentPlayer.stop();
		}
	}

	@Override
	public void seekTo(int msec) {
		if (mCurrentPlayer == this) {
			super.seekTo(msec);
		} else {
			mCurrentPlayer.seekTo(msec);
		}
	}

	@Override
	public boolean isPlaying() {
		if (mCurrentPlayer == this) {
			return super.isPlaying();
		} else {
			return mCurrentPlayer.isPlaying();
		}
	}

	@Override
	public int getCurrentPosition() {
		if (mCurrentPlayer == this) {
			return super.getCurrentPosition();
		} else {
			return mCurrentPlayer.getCurrentPosition();
		}
	}

	@Override
	public int getDuration() {
		int duration = super.getDuration();
		if (mCurrentPlayer != this) {
			duration += mCurrentPlayer.getDuration();
		}
		if (mNextPlayer != this) {
			duration += mNextPlayer.getDuration();
		}
		return duration;
	}

	@Override
	public void setAudioStreamType(int streamtype) {
		streamType = streamtype;
		super.setAudioStreamType(streamType);
		if (mCurrentPlayer != this && mCurrentPlayer != null) {
			mCurrentPlayer.setAudioStreamType(streamType);
		}
		if (mNextPlayer != null && mNextPlayer != this) {
			mNextPlayer.setAudioStreamType(streamType);
		}
	}

	@Override
	public void setVolume(float leftVolume, float rightVolume) {
		super.setVolume(leftVolume, rightVolume);
		if (mCurrentPlayer != this && mCurrentPlayer != null) {
			mCurrentPlayer.setVolume(leftVolume, rightVolume);
		}
		if (mNextPlayer != this && mNextPlayer != null) {
			mNextPlayer.setVolume(leftVolume, rightVolume);
		}
	}

	@Override
	public boolean equals(MediaPlayer mp) {
		if (mCurrentPlayer != null && mCurrentPlayer != this) {
			if (mCurrentPlayer.equals(mp)) {
				return true;
			}
		}
		if (mNextPlayer != null && mNextPlayer != this) {
			if (mNextPlayer.equals(mp)) {
				return true;
			}
		}
		return super.equals(mp);
	}

	@Override
	public boolean conditionalPlay() {
		if (mCurrentPlayer == this) {
			return super.conditionalPlay();
		} else {
			return mCurrentPlayer.conditionalPlay();
		}
	}

	@Override
	public synchronized boolean conditionalPause() {
		if (mCurrentPlayer == this) {
			return super.conditionalPause();
		} else {
			return mCurrentPlayer.conditionalPause();
		}
	}

	@Override
	public synchronized boolean conditionalStop() {
		if (mCurrentPlayer == this) {
			return super.conditionalStop();
		} else {
			return mCurrentPlayer.conditionalStop();
		}
	}

	@Override
	public synchronized boolean isWaitingToPlay() {
		if (mCurrentPlayer == this) {
			return super.isWaitingToPlay();
		} else {
			return mCurrentPlayer.isWaitingToPlay();
		}
	}

	private PlaylistSupportingPlayer newPlayer() {
		PlaylistSupportingPlayer player = new PlaylistSupportingPlayer();
		player.setOnErrorListener(this);
		player.setOnCompletionListener(this);
		player.setStateChangeListener(this);
		if (streamType != -1) {
			player.setAudioStreamType(streamType);
		}
		return player;
	}
}
