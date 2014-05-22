/*******************************************************************************
 * Copyright 2014 Chris Rhoden, Rebecca Nesson, Public Radio Exchange
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
package org.prx.playerhater.plugins;

import android.annotation.TargetApi;
import android.media.RemoteControlClient;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ScrubbableLockScreenControlsPlugin extends LockScreenControlsPlugin implements RemoteControlClient.OnPlaybackPositionUpdateListener, RemoteControlClient.OnGetPlaybackPositionListener {
    private RemoteControlClient mRemoteControlClient;

    @Override
    public void onPlaybackPositionUpdate(long l) {
        if (getPlayerHater().seekTo((int) l)) {
            getRemoteControlClient().setPlaybackState(getPlaybackState(), l, 1f);
        } else {
            onGetPlaybackPosition();
        }
    }

    @Override
    public long onGetPlaybackPosition() {
        if (getPlayerHater().isLoading()) {
            return -1;
        }
        setPlaybackState(getPlaybackState());
        return getPlayerHater().getCurrentPosition();
    }

    @Override
    protected RemoteControlClient getRemoteControlClient() {
        if (mRemoteControlClient == null) {
            mRemoteControlClient = super.getRemoteControlClient();
            mRemoteControlClient.setOnGetPlaybackPositionListener(this);
            mRemoteControlClient.setPlaybackPositionUpdateListener(this);
        }
        return mRemoteControlClient;
    }

    @Override
    protected void setPlaybackState(int state) {
        getRemoteControlClient().setPlaybackState(state, getPlayerHater().getCurrentPosition(), 1f);
        getAudioManager().registerRemoteControlClient(getRemoteControlClient());
    }
}
