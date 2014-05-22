/*******************************************************************************
 * Copyright 2013, 2014 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
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
package org.prx.playerhater.service;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.mediaplayer.Player;
import org.prx.playerhater.mediaplayer.Player.StateChangeListener;
import org.prx.playerhater.mediaplayer.StatelyPlayer;

public class PlayerStateWatcher implements StateChangeListener {
    private int mCurrentState = PlayerHater.STATE_IDLE;
    private int mCurrentDuration = 0;
    private PlayerHaterStateListener mListener;
    private Player mMediaPlayer;

    public PlayerStateWatcher() {
        this(null);
    }

    public PlayerStateWatcher(PlayerHaterStateListener listener) {
        setListener(listener);
    }

    public interface PlayerHaterStateListener {
        void onStateChanged(int state);

        void onDurationChanged(int duration);
    }

    public void setMediaPlayer(Player player) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setStateChangeListener(null);
        }
        mMediaPlayer = player;
        if (mMediaPlayer != null) {
            mMediaPlayer.setStateChangeListener(this);
            onStateChanged(mMediaPlayer, mMediaPlayer.getStateMask());
        } else {
            onStateChanged(null, StatelyPlayer.IDLE);
        }
    }

    public void setListener(PlayerHaterStateListener listener) {
        mListener = listener;
        notifyState();
    }

    @Override
    public synchronized void onStateChanged(Player mediaPlayer, int state) {
        boolean willPlay = StatelyPlayer.willPlay(state);
        boolean seekable = StatelyPlayer.seekable(state);
        state = StatelyPlayer.mediaPlayerState(state);
        setCurrentDuration(mediaPlayer.getDuration());

        switch (state) {
            case StatelyPlayer.END:
                setCurrentState(PlayerHater.STATE_IDLE);
            case StatelyPlayer.STOPPED:
            case StatelyPlayer.ERROR:
            case StatelyPlayer.IDLE:
            case StatelyPlayer.INITIALIZED:
            case StatelyPlayer.PLAYBACK_COMPLETED:
            case StatelyPlayer.PREPARING:
            case StatelyPlayer.PREPARED:
                if (willPlay) {
                    setCurrentState(PlayerHater.STATE_LOADING);
                } else {
                    setCurrentState(PlayerHater.STATE_IDLE);
                }
                break;
            case StatelyPlayer.PAUSED:
                setCurrentState(PlayerHater.STATE_PAUSED);
                break;
            case StatelyPlayer.STARTED:
                if (seekable) {
                    setCurrentState(PlayerHater.STATE_PLAYING);
                } else {
                    setCurrentState(PlayerHater.STATE_STREAMING);
                }
                break;
            default:
                throw new IllegalStateException("Illegal State: " + state);
        }
    }

    public synchronized int getCurrentState() {
        return mCurrentState;
    }

    private void setCurrentState(int currentState) {
        if (currentState != mCurrentState) {
            mCurrentState = currentState;
            notifyState();
        }
    }

    private void setCurrentDuration(int duration) {
        if (mCurrentDuration != duration) {
            mCurrentDuration = duration;
            mListener.onDurationChanged(duration);
        }
    }

    private void notifyState() {
        if (mListener != null) {
            mListener.onStateChanged(mCurrentState);
        }
    }
}
