package org.prx.android.playerhater.util;

import java.io.FileDescriptor;

import org.prx.android.playerhater.player.IPlayer;
import org.prx.android.playerhater.player.MediaPlayerWrapper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;

public class TransientPlayer {
	
	public static TransientPlayer play(Context c, Uri url,
			boolean isDuckable) {
		return new TransientPlayer(c, url, isDuckable).play();
	}

	public static TransientPlayer play(Context c, String url, boolean isDuckable) {
		return new TransientPlayer(c, url, isDuckable).play();
	}

	public static TransientPlayer play(Context c, FileDescriptor file, boolean isDuckable) {
		return new TransientPlayer(c, file, isDuckable).play();
	}

	private final Context c;
	private final MediaPlayerWrapper wrapper;
	private final boolean isDuckable;
	private final String url;
	private final Uri uri;
	private final FileDescriptor file;
	private final int playType;
	private static final int FILE = 1;
	private static final int URL = 2;
	private static final int URI = 3;
	
	final AudioManager audioManager; 

	final AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			// I don't think we need to do anything here.
			// generally, we might want to stop the audio in
			// the event that we have lost focus in a non-
			// transient way, I think.
		}
	};

	protected TransientPlayer(Context c, String url, boolean isDuckable) {
		this.c = c;
		this.wrapper = new MediaPlayerWrapper();
		this.file = null;
		this.uri = null;
		this.url = url;
		this.isDuckable = isDuckable;
		this.playType = URL;
		this.audioManager = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
	}

	protected TransientPlayer(Context c, FileDescriptor file, boolean isDuckable) {
		this.c = c;
		this.wrapper = new MediaPlayerWrapper();
		this.url = null;
		this.uri = null;
		this.file = file;
		this.isDuckable = isDuckable;
		this.playType = FILE;
		this.audioManager = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
	}

	public TransientPlayer(Context c, Uri url, boolean isDuckable) {
		this.c = c;
		this.wrapper = new MediaPlayerWrapper();
		this.url = null;
		this.file = null;
		this.uri = url;
		this.isDuckable = isDuckable;
		this.playType = URI;
		this.audioManager = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
	}

	private TransientPlayer play() {
		TransientPlayerTask task = new TransientPlayerTask();
		Thread thread = new Thread(task);
		thread.run();
		return this; 
	}
	
	@SuppressLint("NewApi")
	public void stop() { 
		try { 
			wrapper.stop(); 
			audioManager.abandonAudioFocus(audioFocusListener);
			wrapper.release();
		} catch (Exception e) { 
			
		}
	}
	
	public boolean isPlaying() { 
		return (wrapper.getState() == IPlayer.STARTED ||
				wrapper.getState() == IPlayer.INITIALIZED ||
				wrapper.getState() == IPlayer.PREPARED ||
				wrapper.getState() == IPlayer.PREPARING); 
	}

	private int getDurationHint() {
		if (isDuckable) {
			return AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
		} else  {
			return AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
		}
	}

	private class TransientPlayerTask implements Runnable {

		@TargetApi(Build.VERSION_CODES.FROYO)
		@Override
		public void run() {

			wrapper.setAudioStreamType(AudioManager.STREAM_MUSIC);

			try {
				switch (playType) {
				case FILE:
					wrapper.setDataSource(file);
					break;
				case URL:
					wrapper.setDataSource(url);
					break;
				case URI:
					wrapper.setDataSource(c, uri);
					break;
				}
				wrapper.prepare();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			wrapper.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					audioManager.abandonAudioFocus(audioFocusListener);
					wrapper.release();
				}

			});

			int status = audioManager.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, getDurationHint());

			if (status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
				wrapper.start();
		}

	}
}
