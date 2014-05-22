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
package org.prx.playerhater.plugins;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import org.prx.playerhater.PlayerHater;
import org.prx.playerhater.R;
import org.prx.playerhater.wrappers.ServicePlayerHater;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class NotificationPlugin extends AbstractPlugin {

    private static final int NOTIFICATION_NU = 0x974732;
    private NotificationManager mNotificationManager;
    protected PendingIntent mContentIntent;
    protected String mNotificationTitle = "PlayerHater";
    protected String mNotificationText = "Version 0.1.0";
    private boolean mIsVisible = false;
    protected Notification mNotification;
    private boolean mShouldBeVisible;

    public NotificationPlugin() {
    }

    @Override
    public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
        super.onPlayerHaterLoaded(context, playerHater);
        if (!(playerHater instanceof ServicePlayerHater)) {
            throw new IllegalArgumentException(
                    "NotificationPlugin must be run on the server side");
        }

        PackageManager packageManager = context.getPackageManager();
        mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resumeActivityIntent = packageManager
                .getLaunchIntentForPackage(getContext().getPackageName());
        resumeActivityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resumeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mContentIntent = PendingIntent.getActivity(getContext(),
                NOTIFICATION_NU, resumeActivityIntent, 0);
    }

    @Override
    public void onAudioLoading() {
        mShouldBeVisible = true;
    }

    @Override
    public void onAudioStarted() {
        mShouldBeVisible = true;
    }

    @SuppressWarnings("deprecation")
    protected Notification getNotification() {
        if (mNotification == null) {
            mNotification = new Notification(R.drawable.zzz_ph_ic_notification,
                    "Playing: " + mNotificationTitle, 0);
        } else {
            mNotification.tickerText = "Playing: " + mNotificationTitle;
        }
        mNotification.setLatestEventInfo(getContext(), mNotificationTitle,
                mNotificationText, mContentIntent);
        return mNotification;
    }

    @Override
    public void onAudioPaused() {
        onAudioStopped();
    }

    @Override
    public void onAudioStopped() {
        mShouldBeVisible = false;
        mIsVisible = false;
        mNotificationManager.cancel(NOTIFICATION_NU);
        mNotification = null;
        getBinder().stopForeground(true);
    }

    @Override
    public void onTitleChanged(String notificationTitle) {
        mNotificationTitle = notificationTitle;
    }

    public void onPendingIntentChanged(PendingIntent contentIntent) {
        mContentIntent = contentIntent;
        if (mNotification != null) {
            mNotification.contentIntent = mContentIntent;
        }
    }

    @Override
    public void onArtistChanged(String notificationText) {
        mNotificationText = notificationText;
    }

    @Override
    public void onChangesComplete() {
        if (mShouldBeVisible && !mIsVisible) {
            getBinder().startForeground(NOTIFICATION_NU, getNotification());
            mIsVisible = true;
        } else if (mIsVisible && !mShouldBeVisible) {
            getBinder().stopForeground(true);
        } else if (mIsVisible && mShouldBeVisible) {
            updateNotification();
        }
    }

    protected void updateNotification() {
        mNotificationManager.notify(NOTIFICATION_NU, getNotification());
    }

    protected ServicePlayerHater getBinder() {
        return (ServicePlayerHater) getPlayerHater();
    }
}
