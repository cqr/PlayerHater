package org.prx.android.playerhater;

public class ListenerEcho implements PlayerHaterListener {
	private enum Action { STOP, PAUSE, LOADING, PLAYING }
	
	private Action lastAction;
	private PlayerHaterListener mListener;
	private Song lastSong;
	private int lastProgress;
	
	public void setListener(PlayerHaterListener listener) {
		mListener = listener;
		sendLastAction();
	}


	@Override
	public void onStopped() {
		lastAction = Action.STOP;
		sendLastAction();
	}

	@Override
	public void onPaused(Song song) {
		lastAction = Action.PAUSE;
		lastSong = song;
		sendLastAction();
	}

	@Override
	public void onLoading(Song song) {
		lastAction = Action.LOADING;
		lastSong = song;
		sendLastAction();
	}

	@Override
	public void onPlaying(Song song, int progress) {
		lastAction = Action.PLAYING;
		lastSong = song;
		lastProgress = progress;
		sendLastAction();
	}

	private void sendLastAction() {
		if (lastAction != null && mListener != null) {
			switch (lastAction) {
			case STOP:
				mListener.onStopped();
				break;
			case PAUSE:
				mListener.onPaused(lastSong);
				break;
			case PLAYING:
				mListener.onPlaying(lastSong, lastProgress);
				break;
			case LOADING:
				mListener.onLoading(lastSong);
				break;
			default:
				//NOOP
			}
		}
	}
}
