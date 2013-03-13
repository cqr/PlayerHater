package org.prx.android.playerhater.remotecontrol;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

public class HeadphoneButtonGestureHelper {

	private static final int BUTTON_PRESSED = 0;
	private static final int PLAY_PAUSE = 1;
	private static final int NEXT = 2;
	private static final int PREV = 3;
	private static final int MILISECONDS_DELAY = 250;
	public static final String TAG = "GESTURES";
	
	
	private long mLastEventTime = 0;
	private int mCurrentAction = 1;
	private static Context lastContext;

	private final Handler mHandler = new ButtonHandler(this);
	private RemoteControlButtonReceiver mMediaButtonReceiver;

	public void onHeadsetButtonPressed(long eventTime, Context context) {
		if (eventTime - mLastEventTime <= MILISECONDS_DELAY + 100) {
			mCurrentAction += 1;
			if (mCurrentAction > 3) {
				mCurrentAction = 1;
			}
			mHandler.removeMessages(BUTTON_PRESSED);
		}
		lastContext = context;
		mLastEventTime = eventTime;
		mHandler.sendEmptyMessageDelayed(BUTTON_PRESSED, MILISECONDS_DELAY);
	}

	public void setReceiver(RemoteControlButtonReceiver receiver) {
		mMediaButtonReceiver = receiver;
	}

	private static class ButtonHandler extends Handler {

		private final HeadphoneButtonGestureHelper mButtonGestureHelper;

		private ButtonHandler(HeadphoneButtonGestureHelper ctx) {
			mButtonGestureHelper = ctx;
		}

		@Override
		public void dispatchMessage(Message message) {
			switch (mButtonGestureHelper.mCurrentAction) {

			case PLAY_PAUSE:
				mButtonGestureHelper.mMediaButtonReceiver
						.onRemoteControlButtonPressed(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, lastContext);
				break;
			case NEXT:
				mButtonGestureHelper.mMediaButtonReceiver
						.onRemoteControlButtonPressed(KeyEvent.KEYCODE_MEDIA_NEXT, lastContext);
				break;
			case PREV:
				mButtonGestureHelper.mMediaButtonReceiver
						.onRemoteControlButtonPressed(KeyEvent.KEYCODE_MEDIA_PREVIOUS, lastContext);
				break;
			}
			mButtonGestureHelper.mLastEventTime = 0;
			mButtonGestureHelper.mCurrentAction = 1;
		}
	}

}
