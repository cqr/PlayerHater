package com.chrisrhoden.glisten.plugins;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import android.net.Uri;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.view.KeyEvent;

import com.chrisrhoden.glisten.BroadcastReceiver;
import com.chrisrhoden.glisten.PlayerHater;
import com.chrisrhoden.glisten.PlayerHaterPlugin;
import com.chrisrhoden.glisten.R;
import com.chrisrhoden.glisten.Song;
import com.chrisrhoden.glisten.util.Log;
import com.chrisrhoden.glisten.wrappers.ServicePlayerHater;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Copyright 2016 Chris Rhoden.
 */
@TargetApi(21)
public class MediaStyleNotificationPlugin extends AbstractPlugin {
    private static final int NOTIFICATION_NU = 0x974732;

    private NotificationManager mNotificationManager;
    private PendingIntent mContentIntent;
    private NotificationCompat.Builder mNotificationBuilder;
    private String mTitle;
    private String mArtist;
    private String mAlbum;
    private Bitmap mAlbumArt;

    public synchronized void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
        super.onPlayerHaterLoaded(context, playerHater);
        Log.v("****************** FANCY PANTS notification loaded *****************");
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
    public void onAudioStopped() {
//        mShouldBeVisible = false;
        mNotificationManager.cancel(NOTIFICATION_NU);
        ServicePlayerHater binder = getBinder();
        if (binder != null) {
            binder.stopForeground(true);
        }
    }

    @Override
    public void onSongFinished(Song song, int reason) {

    }

    @Override
    public void onAudioLoading() {
//        mShouldBeVisible = true;
    }

    @Override
    public void onAudioPaused() {

//        mShouldBeVisible = true;
    }

    @Override
    public void onAudioStarted() {
//        mShouldBeVisible = true;
    }

    @Override
    public void onTitleChanged(String title) {
        mTitle = title;
    }

    @Override
    public void onArtistChanged(String artist) {
        mArtist = artist;

    }

    @Override
    public void onAlbumArtChanged(Uri albumArtUri) {
        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL(albumArtUri.toString());
            connection = (HttpURLConnection) url.openConnection();
            Log.d("DOING AN ALBUM ART");
            Log.d(url.toString());
            mAlbumArt = BitmapFactory.decodeStream(new BufferedInputStream(connection.getInputStream()));
        } catch (MalformedURLException e) {
            mAlbumArt = null;
            e.printStackTrace();
        } catch (IOException e) {
            mAlbumArt = null;
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public void onNextSongAvailable(Song nextTrack) {

    }

    @Override
    public void onNextSongUnavailable() {

    }

    @Override
    public void onTransportControlFlagsChanged(int transportControlFlags) {

    }

    @Override
    public void onPendingIntentChanged(PendingIntent intent) {
        mContentIntent = intent;
    }

    @Override
    public void onChangesComplete() {
        mNotificationBuilder = new NotificationCompat.Builder(getContext());
        mNotificationBuilder.setContentIntent(mContentIntent)
                .setSmallIcon(R.drawable.zzz_ph_ic_notification)
                .setAutoCancel(false).setOngoing(getPlayerHater().isPlaying()).setOnlyAlertOnce(true)
                .setContentTitle(mTitle).setContentText(getContentText());
        mNotificationBuilder.addAction(new NotificationCompat.Action(R.drawable.zzz_ph_bt_back, "Previous", getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)));

        if (getPlayerHater().isPlaying()) {
            mNotificationBuilder.addAction(new NotificationCompat.Action(R.drawable.zzz_ph_bt_pause, "Pause", getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_PAUSE)));
        } else {
            mNotificationBuilder.addAction(new NotificationCompat.Action(R.drawable.zzz_ph_bt_play, "Play", getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_PLAY)));
        }
        mNotificationBuilder.setShowWhen(false);
        mNotificationBuilder.setWhen(0);

        mNotificationBuilder.addAction(new NotificationCompat.Action(R.drawable.zzz_ph_bt_skip, "Next", getMediaButtonPendingIntent(KeyEvent.KEYCODE_MEDIA_NEXT)));
        mNotificationBuilder.setStyle(new NotificationCompat.MediaStyle().setMediaSession(getMediaSession()).setShowActionsInCompactView(1, 2));
        mNotificationBuilder.setLargeIcon(mAlbumArt);
        Notification not = mNotificationBuilder.build();
        if (getPlayerHater().isPlaying()) {
            getBinder().startForeground(NOTIFICATION_NU, not);
        } else {
            mNotificationManager.notify(NOTIFICATION_NU, not);
            getBinder().stopForeground(false);
        }

    }

    @Override
    public void onAlbumTitleChanged(String albumTitle) {
        mAlbum = albumTitle;
    }

    @Override
    public void onPlayerHaterShutdown() {

    }

    protected ServicePlayerHater getBinder() {
        try {
            return (ServicePlayerHater) getPlayerHater();
        } catch (IllegalStateException exception) {
            return null;
        }
    }

    private String getContentText() {
        if (mArtist != null && mAlbum != null) {
            return mArtist + " - " + mAlbum;
        } else if (mArtist != null) {
            return mArtist;
        } else if (mAlbum != null) {
            return mAlbum;
        } else {
            return null;
        }
    }

    private MediaSessionCompat.Token getMediaSession() {
        return getBinder().getMediaSession();
    }

    private PendingIntent getMediaButtonPendingIntent(int keycode) {
        Intent intent = new Intent(getContext(), BroadcastReceiver.class);
        intent.setAction(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
                KeyEvent.ACTION_UP, keycode));
        return PendingIntent.getBroadcast(getContext(), keycode, intent, 0);
    }

}
