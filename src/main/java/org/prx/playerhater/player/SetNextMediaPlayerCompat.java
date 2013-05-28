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
package org.prx.playerhater.player;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;

public interface SetNextMediaPlayerCompat extends OnCompletionListener {

	public class Compat implements SetNextMediaPlayerCompat {
		private Player mNextMediaPlayer;
		private final Player mStateManager;
		private OnCompletionListener mOnCompletionListener;

		public Compat(Player stateManager) {
			mStateManager = stateManager;
			stateManager.setOnCompletionListener(this);
		}

		@Override
		public void setNextMediaPlayer(Player next) {
			mNextMediaPlayer = next;
		}

		@Override
		public void skip() {
			if (!mStateManager.conditionalStop()) {
				mStateManager.reset();
			}
			onCompletion(mStateManager.getBarePlayer());
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mNextMediaPlayer != null) {
				mNextMediaPlayer.conditionalPlay();
			}
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mp);
			}
		}

		@Override
		public void setOnCompletionListener(OnCompletionListener onCompletion) {
			mOnCompletionListener = onCompletion;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public class Modern implements SetNextMediaPlayerCompat {
		private final MediaPlayerWithState mMediaPlayer;
		private MediaPlayerWithState mNextMediaPlayer;
		private OnCompletionListener mOnComplete;

		public Modern(MediaPlayerWithState stateManager) {
			stateManager.setOnCompletionListener(this);
			mMediaPlayer = stateManager;
		}

		@Override
		public void setNextMediaPlayer(Player next) {
			mNextMediaPlayer = next;
			MediaPlayer tmp = null;
			if (next != null) {
				tmp = mNextMediaPlayer.getBarePlayer();
			}
			if (tmp != null && tmp != mMediaPlayer.getBarePlayer()) {
				mMediaPlayer.getBarePlayer().setNextMediaPlayer(tmp);
			}
		}

		@Override
		public void setOnCompletionListener(OnCompletionListener onCompletion) {
			mOnComplete = onCompletion;
		}

		@Override
		public void skip() {
			if (mNextMediaPlayer == null) {
				mMediaPlayer.reset();
			}
			onCompletion(mMediaPlayer.getBarePlayer(), true);
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			onCompletion(mp, false);
		}

		private void onCompletion(MediaPlayer mp, boolean start) {
			if (mNextMediaPlayer != null) {
				mMediaPlayer.swap(mNextMediaPlayer);
				mNextMediaPlayer.reset();
				if (start) {
					((Player) mMediaPlayer).start();
				}
			}
			if (mOnComplete != null) {
				mOnComplete.onCompletion(mp);
			}
		}

	}

	void setNextMediaPlayer(Player next);

	public void skip();

	public void setOnCompletionListener(OnCompletionListener onCompletion);
}
