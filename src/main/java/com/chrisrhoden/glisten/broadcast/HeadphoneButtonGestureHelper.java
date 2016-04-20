/*******************************************************************************
 * Copyright 2013 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.chrisrhoden.glisten.broadcast;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

class HeadphoneButtonGestureHelper {

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
						.onRemoteControlButtonPressed(
								KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, lastContext);
				break;
			case NEXT:
				mButtonGestureHelper.mMediaButtonReceiver
						.onRemoteControlButtonPressed(
								KeyEvent.KEYCODE_MEDIA_NEXT, lastContext);
				break;
			case PREV:
				mButtonGestureHelper.mMediaButtonReceiver
						.onRemoteControlButtonPressed(
								KeyEvent.KEYCODE_MEDIA_PREVIOUS, lastContext);
				break;
			}
			mButtonGestureHelper.mLastEventTime = 0;
			mButtonGestureHelper.mCurrentAction = 1;
		}
	}

}
