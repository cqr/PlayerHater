package org.prx.android.playerhater;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import org.prx.android.playerhater.PlayerHater;

import android.app.Activity;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PlayerHaterBinder extends Binder implements PlayerHater {

	private final PlayerHaterService mService;
	private PlayerListenerManager mPlayerListenerManager;

	public PlayerHaterBinder(PlayerHaterService service,
			PlayerListenerManager playerListenerManager) {
		mService = service;
		mPlayerListenerManager = playerListenerManager;
	}

	@Override
	public boolean play() throws IllegalStateException, IOException {
		return mService.play();
	}

	@Override
	public boolean pause() {
		return mService.pause();
	}

	@Override
	public boolean stop() {
		return mService.stop();
	}

	@Override
	public boolean play(String fileOrUrl) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException {
		return play(fileOrUrl, fileOrUrl.charAt(0) != '/');
	}

	@Override
	public boolean play(String fileOrUrl, boolean isUrl) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException {
		if (isUrl) {
			return mService.play(fileOrUrl);
		} else {
			try {
				return play((new FileInputStream(new File(fileOrUrl))).getFD());
			} catch (Exception e) {
				return false;
			}
		}
	}

	@Override
	public String getNowPlaying() {
		return mService.getNowPlaying();
	}

	@Override
	public boolean isPlaying() {
		return mService.isPlaying();
	}

	@Override
	public boolean play(String fileOrUrl, boolean isUrl, Activity activity) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException {
		setNotificationIntentActivity(activity);
		return play(fileOrUrl, isUrl);
	}

	@Override
	public boolean play(FileDescriptor fd) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException {
		return mService.play(fd);
	}

	@Override
	public boolean play(FileDescriptor fd, Activity activity) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException {
		setNotificationIntentActivity(activity);
		return play(fd);
	}

	@Override
	public boolean play(FileDescriptor fd, Activity activity, int view) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException {
		setNotificationIntentActivity(activity);
		setNotificationView(view);
		return play(fd);
	}

	@Override
	public boolean play(Uri url) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException {
		return play(url.toString(), true);
	}

	@Override
	public boolean play(Uri url, Activity activity) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException {
		setNotificationIntentActivity(activity);
		return play(url);
	}

	@Override
	public void setNotificationIntentActivity(Activity activity) {
		mService.setNotificationIntentActivity(activity);
	}

	@Override
	public void setNotificationView(int view) {
		mService.setNotificationView(view);
	}

	@Override
	public boolean play(String fileOrUrl, boolean isUrl, Activity activity,
			int view) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException {
		setNotificationIntentActivity(activity);
		setNotificationView(view);
		return play(fileOrUrl, isUrl);
	}

	@Override
	public boolean play(Uri url, Activity activity, int view) throws IllegalStateException, IllegalArgumentException, SecurityException, IOException {
		setNotificationIntentActivity(activity);
		setNotificationView(view);
		return play(url);
	}

	@Override
	public void seekTo(int position) {
		mService.seekTo(position);
	}

	@Override
	public int getCurrentPosition() {
		return mService.getCurrentPosition();
	}

	@Override
	public int getState() {
		return mService.getState();
	}

	/*
	 * We use the delegation pattern here, rather than doing things
	 * automatically
	 */
	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mPlayerListenerManager.setOnBufferingUpdateListener(listener);
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mPlayerListenerManager.setOnCompletionListener(listener);
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		mPlayerListenerManager.setOnInfoListener(listener);
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mPlayerListenerManager.setOnSeekCompleteListener(listener);
	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		mPlayerListenerManager.setOnErrorListener(listener);
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		mPlayerListenerManager.setOnPreparedListener(listener);
	}
	
	@Override
	public void setPlayerHaterListener(PlayerHaterListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	
	/*
	 * End delegated listener methods
	 */

	@Override
	public void setOnProgressChangeListener(OnSeekBarChangeListener listener) {
		// noop
	}

	@Override
	public Bundle getBundle() {
		return mService.getBundle();
	}

	@Override
	public void commitBundle(Bundle icicle) {
		mService.commitBundle(icicle);
	}

	@Override
	public void transientPlay(String fileOrUrl) {
		transientPlay(fileOrUrl, false);
	}

	@Override
	public void transientPlay(String fileOrUrl, boolean isDuckable) {
		if (fileOrUrl.charAt(0) == '/') {
			try {
				transientPlay((new FileInputStream(new File(fileOrUrl))).getFD(), isDuckable);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			mService.transientPlay(fileOrUrl, isDuckable);
		}
	}

	@Override
	public void transientPlay(FileDescriptor file) {
		transientPlay(file, false);
	}

	@Override
	public void transientPlay(FileDescriptor file, boolean isDuckable) {
		mService.transientPlay(file, isDuckable);
	}
}
