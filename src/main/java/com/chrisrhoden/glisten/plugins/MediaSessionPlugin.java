package com.chrisrhoden.glisten.plugins;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaDescription;
import android.media.Rating;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.chrisrhoden.glisten.BroadcastReceiver;
import com.chrisrhoden.glisten.PlayerHater;
import com.chrisrhoden.glisten.PlayerHaterPlugin;
import com.chrisrhoden.glisten.R;
import com.chrisrhoden.glisten.Song;
import com.chrisrhoden.glisten.broadcast.OnAudioFocusChangedListener;
import com.chrisrhoden.glisten.util.Log;
import com.chrisrhoden.glisten.wrappers.ServicePlayerHater;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2016 Chris Rhoden.
 */
@TargetApi(21)
public class MediaSessionPlugin extends AbstractPlugin {
    private MediaSessionCompat mMediaSession;
    private final MediaSessionCallback mCallback = new MediaSessionCallback(this);
    private boolean mIsPlaying = false;
    private static final long BASE_ACTIONS = PlaybackStateCompat.ACTION_PLAY_PAUSE;
    private static final long SEEK_ACTIONS = PlaybackStateCompat.ACTION_REWIND | PlaybackStateCompat.ACTION_FAST_FORWARD | PlaybackStateCompat.ACTION_SEEK_TO;
    private Bitmap mAlbumArt;


    @Override
    public void onPlayerHaterLoaded(Context context, PlayerHater playerHater) {
        Log.d("Doing phLoaded in MediaSessionPlugin");
        super.onPlayerHaterLoaded(context, playerHater);
        if (!(playerHater instanceof ServicePlayerHater)) {
            throw new IllegalArgumentException(
                    "MediaSessionPlugin must be run on the server side");
        }
        ComponentName broadcastReceiver = new ComponentName(context, BroadcastReceiver.class);
        mMediaSession = new MediaSessionCompat(context, "MediaPlayer", broadcastReceiver, null);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());
        mMediaSession.setCallback(mCallback);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                // Ignore
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        ((ServicePlayerHater) playerHater).setMediaSession(mMediaSession.getSessionToken());
    }

    @Override
    public void onSongChanged(Song song) {
//        ArrayList<MediaSession.QueueItem> list = new ArrayList<>();
//        list.add(new MediaSession.QueueItem(new MediaDescription.Builder().setTitle(song.getTitle()).setSubtitle(song.getArtist()).setDescription(song.getAlbumTitle()).setIconUri(song.getAlbumArt()).setMediaUri(song.getUri()).build(), 0));
//        mMediaSession.setQueue(list);
    }

    @Override
    public void onSongFinished(Song song, int reason) {

    }

    @Override
    public void onDurationChanged(int duration) {

    }

    @Override
    public void onAudioLoading() {
        mIsPlaying = false;
        mMediaSession.setActive(true);
    }

    @Override
    public void onAudioPaused() {
        mIsPlaying = false;
    }

    @Override
    public void onAudioResumed() {
        mIsPlaying = true;
    }

    @Override
    public void onAudioStarted() {
        mIsPlaying = true;

    }

    @Override
    public void onAudioStopped() {
        mIsPlaying = false;
        if (mMediaSession != null) {
            mMediaSession.setActive(false);
        }
    }

    @Override
    public void onTitleChanged(String title) {
    }

    @Override
    public void onArtistChanged(String artist) {

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
    public void onTransportControlFlagsChanged(int transportControlFlags) {

    }

    @Override
    public void onPendingIntentChanged(PendingIntent intent) {
//        mMediaSession.setSessionActivity(intent);
    }

    @Override
    public void onChangesComplete() {
        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        if (mIsPlaying) {
            Song song = getPlayerHater().nowPlaying();
            mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbumTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getPlayerHater().getDuration())
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, mAlbumArt)
                    .build());
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT).setState(PlaybackStateCompat.STATE_PLAYING, 50, 1);
        } else if (getPlayerHater().isLoading()) {
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT).setState(PlaybackStateCompat.STATE_BUFFERING, 50, 1);
        } else {
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT).setState(PlaybackStateCompat.STATE_PAUSED, 50, 1);
        }
        mMediaSession.setPlaybackState(stateBuilder.build());
    }

    @Override
    public void onAlbumTitleChanged(String albumTitle) {

    }

    @Override
    public void onPlayerHaterShutdown() {
//        mMediaSession.release();
//        mMediaSession = null;
    }

    private static final class MediaSessionCallback extends MediaSessionCompat.Callback {
        private final MediaSessionPlugin mMediaSessionPlugin;

        MediaSessionCallback(@NotNull MediaSessionPlugin plugin) {
            mMediaSessionPlugin = plugin;
        }

        @Override
        public void onPlay() {
            super.onPlay();
            mMediaSessionPlugin.play();
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
            mMediaSessionPlugin.skipToQueueItem(id);
        }

        @Override
        public void onPause() {
            super.onPause();
            mMediaSessionPlugin.pause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            mMediaSessionPlugin.skipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            mMediaSessionPlugin.skipToPrevious();
        }

        @Override
        public void onFastForward() {
            super.onFastForward();
            mMediaSessionPlugin.fastForward();
        }

        @Override
        public void onRewind() {
            super.onRewind();
            mMediaSessionPlugin.rewind();
        }

        @Override
        public void onStop() {
            super.onStop();
            mMediaSessionPlugin.stop();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            mMediaSessionPlugin.seekTo(pos);
        }
    }

    private void rewind() {
        getPlayerHater().seekTo(Math.max(0, getPlayerHater().getCurrentPosition()));
    }

    private void play() {
        getPlayerHater().play();
    }

    private void skipToQueueItem(long id) {
        getPlayerHater().skipTo((int)id);
    }

    private void seekTo(long pos) {
        getPlayerHater().seekTo((int)pos);
    }

    private void stop() {
        getPlayerHater().stop();
    }

    private void fastForward() {
        getPlayerHater().seekTo(Math.min(getPlayerHater().getDuration(), getPlayerHater().getCurrentPosition() + 30));
    }

    private void skipToPrevious() {
        getPlayerHater().skipBack();
    }

    private void skipToNext() {
        getPlayerHater().skip();
    }

    private void pause() {
        getPlayerHater().pause();
    }

}
