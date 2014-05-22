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
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import org.prx.playerhater.R;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ExpandableNotificationPlugin extends TouchableNotificationPlugin {

    private RemoteViews mExpandedView;

    @Override
    protected Notification getNotification() {
        if (mNotification == null) {
            mNotification = super.getNotification();
            mNotification.bigContentView = getExpandedView();
        }
        return mNotification;
    }

    private RemoteViews getExpandedView() {
        if (mExpandedView == null) {
            mExpandedView = new RemoteViews(getContext().getPackageName(),
                    R.layout.zzz_ph_jbb_notification);
            setListeners(mExpandedView);
            mExpandedView.setTextViewText(R.id.zzz_ph_notification_title,
                    mNotificationTitle);
            mExpandedView.setTextViewText(R.id.zzz_ph_notification_text,
                    mNotificationText);
            mExpandedView.setImageViewUri(R.id.zzz_ph_notification_image,
                    mNotificationImageUrl);
        }
        return mExpandedView;
    }

    @Override
    protected void setTextViewText(int viewId, String text) {
        super.setTextViewText(viewId, text);
        if (mExpandedView != null) {
            mExpandedView.setTextViewText(viewId, text);
        }
    }

    @Override
    protected void setViewEnabled(int viewId, boolean enabled) {
        if (mExpandedView != null) {
            mExpandedView.setBoolean(viewId, "setEnabled", enabled);
        }
        super.setViewEnabled(viewId, enabled);
    }

    @Override
    protected void setViewVisibility(int viewId, int visible) {
        if (mExpandedView != null) {
            mExpandedView.setViewVisibility(viewId, visible);
        }
        super.setViewVisibility(viewId, visible);
    }

    @Override
    protected void setImageViewResource(int viewId, int resourceId) {
        if (mExpandedView != null) {
            mExpandedView.setImageViewResource(viewId, resourceId);
        }
        super.setImageViewResource(viewId, resourceId);
    }

    @Override
    protected void setImageViewUri(int viewId, Uri contentUri) {
        super.setImageViewUri(viewId, contentUri);
        if (mExpandedView != null && contentUri != null) {
            mExpandedView.setImageViewUri(viewId, contentUri);
        }
    }

    @Override
    protected Notification buildNotification() {
        return getNotificationBuilder().build();
    }
}
