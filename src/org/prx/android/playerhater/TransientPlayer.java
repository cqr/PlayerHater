package org.prx.android.playerhater;

import java.io.FileDescriptor;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class TransientPlayer {

	public static void play(Context c, String url, boolean isDuckable) {
		new TransientPlayer(c, url, isDuckable).play();
	}

	public static void play(Context c, FileDescriptor file, boolean isDuckable) {
		new TransientPlayer(c, file, isDuckable).play();
	}

	private final Context c;
	private final MediaPlayerWrapper wrapper;
	private final boolean isDuckable;
	private final String url;
	private final FileDescriptor file;
	private final int playType;
	private static final int FILE = 1;
	private static final int URL = 2;

	protected TransientPlayer(Context c, String url, boolean isDuckable) {
		this.c = c;
		this.wrapper = new MediaPlayerWrapper();
		this.file = null;
		this.url = url;
		this.isDuckable = isDuckable;
		this.playType = URL;
	}

	protected TransientPlayer(Context c, FileDescriptor file, boolean isDuckable) {
		this.c = c;
		this.wrapper = new MediaPlayerWrapper();
		this.url = null;
		this.file = file;
		this.isDuckable = isDuckable;
		this.playType = FILE;
	}
	
	private void play() {
		TransientPlayerTask task = new TransientPlayerTask();
		Thread thread = new Thread(task);
		thread.run();
	}
	
	private int getDurationHint() {
		if (isDuckable) {
			return AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
		} else  {
			return AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
		}
	}
	
	private class TransientPlayerTask implements Runnable {
		
		@Override
		public void run() {
			
			wrapper.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
			
			try {
				switch (playType) {
				case FILE:
					wrapper.setDataSource(file);
					break;
				case URL:
					wrapper.setDataSource(url);
					break;
				}
				wrapper.prepare();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			
			final AudioManager audioManager = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
			
			final AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
				@Override
				public void onAudioFocusChange(int focusChange) {
					if (focusChange == getDurationHint())
						wrapper.start();
				}
			};
			
			audioManager.requestAudioFocus(audioFocusListener, AudioManager.STREAM_NOTIFICATION, getDurationHint());
			
			wrapper.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					audioManager.abandonAudioFocus(audioFocusListener);
					wrapper.release();
				}
				
			});
		}
		
	}
}
